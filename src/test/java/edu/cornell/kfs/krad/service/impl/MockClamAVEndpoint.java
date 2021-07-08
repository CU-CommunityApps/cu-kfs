package edu.cornell.kfs.krad.service.impl;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.util.StreamUtils;

import edu.cornell.kfs.krad.CUKRADConstants.ClamAVCommands;
import edu.cornell.kfs.krad.CUKRADConstants.ClamAVResponses;
import edu.cornell.kfs.krad.CUKRADTestConstants;

/**
 * Testing class for imitating a local ClamAV server.
 * 
 * The responses sent from this server may not completely match those that ClamAV would actually send.
 */
public class MockClamAVEndpoint implements Closeable {

    private static final Logger LOG = LogManager.getLogger();

    private static final String MOCK_INFECTED_FILE_INDICATOR = "INFECT";
    private static final String NUL = "\0";
    private static final byte NUL_AS_BYTE = 0;
    private static final byte LINEFEED_AS_BYTE = 10;
    private static final int BUFFER_SIZE = 1024;

    private ServerSocket serverSocket;
    private AtomicBoolean serverActive;
    private AtomicBoolean forceTempFileError;
    private Thread connectionHandler;

    public MockClamAVEndpoint() throws IOException {
        this.serverSocket = new ServerSocket(0);
        this.serverActive = new AtomicBoolean(true);
        this.forceTempFileError = new AtomicBoolean(false);
        this.connectionHandler = new Thread(this::handleClients);
        connectionHandler.start();
    }

    public void setForceTempFileError(boolean forceTempFileError) {
        this.forceTempFileError.set(forceTempFileError);
    }

    private void handleClients() {
        Socket socket = null;
        AtomicBoolean serverActiveFlag = serverActive;
        
        try {
            while (serverActiveFlag.get()) {
                socket = serverSocket.accept();
                handleCurrentClient(socket);
                IOUtils.closeQuietly(socket);
            }
        } catch (SocketException e) {
            if (serverActiveFlag.get()) {
                LOG.error("handleClients, Encountered a SocketException while mock server was still running", e);
            }
        } catch (Exception e) {
            LOG.error("handleClients, Encountered an unexpected error", e);
        } finally {
            IOUtils.closeQuietly(socket);
        }
    }

    private void handleCurrentClient(Socket socket) throws IOException {
        try (
            InputStream socketInput = socket.getInputStream();
            OutputStream socketOutput = socket.getOutputStream();
            Writer socketWriter = new OutputStreamWriter(socketOutput, StandardCharsets.UTF_8);
        ) {
            readAndProcessClientCommands(socket, socketInput, socketWriter);
        }
    }

    private void readAndProcessClientCommands(
            Socket socket, InputStream socketInput, Writer socketWriter) throws IOException {
        
        byte[] currentData = new byte[BUFFER_SIZE];
        int currentLength = 0;
        boolean waitForClientToSendEndOfInput = true;
        
        for (int chunkLength = socketInput.read(currentData, currentLength, currentData.length - currentLength);
                waitForClientToSendEndOfInput && chunkLength != -1;
                chunkLength = socketInput.read(currentData, currentLength, currentData.length - currentLength)) {
            int commandEndIndex = indexOfCommandEnd(currentData, currentLength, currentLength + chunkLength);
            currentLength += chunkLength;
            
            if (commandEndIndex != -1) {
                String command = new String(currentData, 0, commandEndIndex + 1, StandardCharsets.UTF_8);
                byte[] dataAfterCommand = Arrays.copyOfRange(currentData, commandEndIndex + 1, currentLength);
                String responseData = handleClientCommand(socketInput, command, dataAfterCommand);
                writeResponseToClient(socketWriter, responseData);
                if (StringUtils.endsWith(responseData, ClamAVResponses.ERROR_SUFFIX + NUL)) {
                    waitForClientToSendEndOfInput = false;
                }
                currentData = new byte[BUFFER_SIZE];
                currentLength = 0;
            } else if (currentLength == currentData.length) {
                currentData = Arrays.copyOf(currentData, currentData.length + BUFFER_SIZE);
            }
        }
    }

    private int indexOfCommandEnd(byte[] currentData, int startIndex, int endIndex) {
        int commandEndIndex = -1;
        byte currentByte;
        for (int i = 0; commandEndIndex == -1 && i < endIndex; i++) {
            currentByte = currentData[i];
            if (currentByte == NUL_AS_BYTE || currentByte == LINEFEED_AS_BYTE) {
                commandEndIndex = i;
            }
        }
        return commandEndIndex;
    }

    private String handleClientCommand(InputStream socketInput, String command, byte[] dataAfterCommand)
            throws IOException {
         switch (command) {
             case ClamAVCommands.PING :
                 return ClamAVResponses.PING_RESPONSE_OK;
                 
             case ClamAVCommands.INSTREAM :
                 return handleScan(socketInput, dataAfterCommand);
                 
             case ClamAVCommands.STATS :
                 return CUKRADTestConstants.TEST_STATS_OUTPUT;
                 
             default :
                 LOG.error("handleClientCommand, Invalid command: " + command);
                 throw new SocketException("Invalid command");
         }
    }

    private String handleScan(InputStream socketInput, byte[] dataAfterCommand) throws IOException {
        try (
            InputStream preFetchedInput = new ByteArrayInputStream(dataAfterCommand);
            InputStream wrappedSocketInput = StreamUtils.nonClosing(socketInput);
            InputStream fullInput = new SequenceInputStream(preFetchedInput, wrappedSocketInput);
        ) {
            byte[] fileContent = new byte[BUFFER_SIZE];
            int fileContentLength = 0;
            String fileStringContent;
            
            if (forceTempFileError.get()) {
                return ClamAVResponses.RESPONSE_ERROR_WRITING_FILE + NUL;
            }
            
            for (int nextChunkLength = readLengthOfNextChunkFromStream(fullInput);
                    nextChunkLength > 0; nextChunkLength = readLengthOfNextChunkFromStream(fullInput)) {
                if (fileContentLength + nextChunkLength > fileContent.length) {
                    fileContent = Arrays.copyOf(
                            fileContent, fileContent.length + Math.max(nextChunkLength, BUFFER_SIZE));
                }
                int actualChunkLength = fullInput.readNBytes(fileContent, fileContentLength, nextChunkLength);
                if (actualChunkLength != nextChunkLength) {
                    throw new SocketException("Unexpected end of stream while reading next file chunk");
                }
                fileContentLength += actualChunkLength;
                if (fileContentLength > CUKRADTestConstants.TEST_MAX_FILE_LENGTH) {
                    return ClamAVResponses.RESPONSE_SIZE_EXCEEDED + NUL;
                }
            }
            
            fileStringContent = new String(fileContent, 0, fileContentLength, StandardCharsets.UTF_8);
            return buildMockScanResponse(fileStringContent) + NUL;
        }
    }

    private int readLengthOfNextChunkFromStream(InputStream socketInput) throws IOException {
        byte[] nextChunkLengthAsBytes = socketInput.readNBytes(4);
        if (nextChunkLengthAsBytes.length < 4) {
            throw new SocketException("Unexpected close of stream while reading length of next file chunk");
        }
        int nextChunkLength = nextChunkLengthAsBytes[3] & 0xFF;
        nextChunkLength |= (nextChunkLengthAsBytes[2] & 0xFF) << 8;
        nextChunkLength |= (nextChunkLengthAsBytes[1] & 0xFF) << 16;
        nextChunkLength |= (nextChunkLengthAsBytes[0] & 0xFF) << 24;
        System.out.println("Next chunk length: " + nextChunkLength);
        return nextChunkLength;
    }

    private String buildMockScanResponse(String fileData) {
        if (StringUtils.containsIgnoreCase(fileData, MOCK_INFECTED_FILE_INDICATOR)) {
            return StringUtils.join(ClamAVResponses.STREAM_PREFIX, CUKRADTestConstants.MOCK_VIRUS_INDICATOR,
                    KFSConstants.BLANK_SPACE, ClamAVResponses.FOUND_SUFFIX);
        }
        return ClamAVResponses.RESPONSE_OK;
    }

    private void writeResponseToClient(Writer socketWriter, String responseData) throws IOException {
        socketWriter.write(responseData);
        socketWriter.flush();
    }

    public String getHostAddress() {
        return serverSocket.getInetAddress().getHostAddress();
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    @Override
    public void close() throws IOException {
        if (serverActive != null) {
            serverActive.set(false);
            serverActive = null;
        }
        IOUtils.closeQuietly(serverSocket);
        forceTempFileError = null;
        connectionHandler = null;
    }

}
