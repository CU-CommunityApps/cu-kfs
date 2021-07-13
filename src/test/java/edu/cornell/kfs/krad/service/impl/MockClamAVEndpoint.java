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
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.util.StreamUtils;

import edu.cornell.kfs.krad.CUKRADConstants.ClamAVCommands;
import edu.cornell.kfs.krad.CUKRADConstants.ClamAVDelimiters;
import edu.cornell.kfs.krad.CUKRADConstants.ClamAVResponses;
import edu.cornell.kfs.krad.CUKRADTestConstants;
import edu.cornell.kfs.krad.util.ClamAVUtils;

/**
 * Testing class for imitating a local ClamAV process.
 * 
 * The responses sent from this process may not completely match those that ClamAV would actually send.
 * Also, for simplicity, it is assumed that only text files are being scanned by this mock endpoint.
 */
public class MockClamAVEndpoint implements Closeable {

    private static final Logger LOG = LogManager.getLogger();

    private static final int BUFFER_SIZE = 1024;
    private static final int NUM_BYTES_FOR_CHUNK_LENGTH = 4;

    private ServerSocket serverSocket;
    private AtomicBoolean serverActive;
    private AtomicBoolean forceTempFileError;
    private FutureTask<String> connectionHandler;
    private Thread connectionHandlerThread;

    public MockClamAVEndpoint() throws IOException {
        this.serverSocket = new ServerSocket(0);
        this.serverActive = new AtomicBoolean(true);
        this.forceTempFileError = new AtomicBoolean(false);
        this.connectionHandler = new FutureTask<>(this::handleClients, KFSConstants.EMPTY_STRING);
        this.connectionHandlerThread = new Thread(connectionHandler);
        serverSocket.setSoTimeout(CUKRADTestConstants.TEST_SOCKET_TIMEOUT);
        connectionHandlerThread.start();
    }

    public void setForceTempFileError(boolean forceTempFileError) {
        this.forceTempFileError.set(forceTempFileError);
    }

    private void handleClients() {
        Socket socket = null;
        
        try {
            while (serverActive.get()) {
                socket = serverSocket.accept();
                handleCurrentClient(socket);
                IOUtils.closeQuietly(socket);
            }
        } catch (SocketException e) {
            if (serverActive.get()) {
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
        int previousLength = 0;
        int currentLength = 0;
        boolean continueReadingClientData = true;
        int commandEndIndex;
        int chunkLength;
        
        do {
            chunkLength = socketInput.read(currentData, currentLength, currentData.length - currentLength);
            if (chunkLength <= 0) {
                continueReadingClientData = false;
            } else {
                previousLength = currentLength;
                currentLength += chunkLength;
                if (previousLength == 0 && currentData[0] != ClamAVDelimiters.Z_PREFIX_BYTE) {
                    throw new SocketException("This mock implementation only supports commands in " +
                            "'zCOMMANDNAME\\0' format.  If you formatted your command as 'nCOMMANDNAME\\n' " +
                            "instead, please update your code accordingly.");
                }
                commandEndIndex = ClamAVUtils.indexOfNullCharDelimiter(currentData, previousLength, currentLength);
                
                if (commandEndIndex != -1) {
                    String command = new String(currentData, 1, commandEndIndex - 1, StandardCharsets.UTF_8);
                    byte[] dataAfterCommand = Arrays.copyOfRange(currentData, commandEndIndex + 1, currentLength);
                    String response = handleClientCommand(socketInput, command, dataAfterCommand);
                    writeResponseToClient(socketWriter, response);
                    continueReadingClientData = false;
                } else if (currentLength == currentData.length) {
                    currentData = Arrays.copyOf(currentData, currentData.length + BUFFER_SIZE);
                }
            }
        } while (continueReadingClientData);
    }

    private String handleClientCommand(InputStream socketInput, String command, byte[] dataAfterCommand)
            throws IOException {
         switch (command) {
             case ClamAVCommands.PING :
                 return buildPingResponse(dataAfterCommand);
                 
             case ClamAVCommands.INSTREAM :
                 return handleScan(socketInput, dataAfterCommand);
                 
             case ClamAVCommands.STATS :
                 return buildStatsResponse(dataAfterCommand);
                 
             default :
                 LOG.error("handleClientCommand, Invalid command: " + command);
                 throw new SocketException("Invalid command");
         }
    }

    private String buildPingResponse(byte[] dataAfterCommand) {
        return dataAfterCommand.length == 0 ? ClamAVResponses.PING_RESPONSE_OK : ClamAVResponses.ERROR_SUFFIX;
    }

    private String buildStatsResponse(byte[] dataAfterCommand) {
        return dataAfterCommand.length == 0 ? CUKRADTestConstants.TEST_STATS_OUTPUT : ClamAVResponses.ERROR_SUFFIX;
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
                return ClamAVResponses.RESPONSE_ERROR_WRITING_FILE;
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
                    return ClamAVResponses.RESPONSE_SIZE_EXCEEDED;
                }
            }
            
            fileStringContent = new String(fileContent, 0, fileContentLength, StandardCharsets.UTF_8);
            return buildMockScanResponse(fileStringContent);
        }
    }

    private int readLengthOfNextChunkFromStream(InputStream socketInput) throws IOException {
        byte[] nextChunkLengthAsBytes = socketInput.readNBytes(NUM_BYTES_FOR_CHUNK_LENGTH);
        if (nextChunkLengthAsBytes.length < NUM_BYTES_FOR_CHUNK_LENGTH) {
            throw new SocketException("Unexpected close of stream while reading length of next file chunk");
        }
        int nextChunkLength = nextChunkLengthAsBytes[3] & 0xFF;
        nextChunkLength |= (nextChunkLengthAsBytes[2] & 0xFF) << 8;
        nextChunkLength |= (nextChunkLengthAsBytes[1] & 0xFF) << 16;
        nextChunkLength |= (nextChunkLengthAsBytes[0] & 0xFF) << 24;
        return nextChunkLength;
    }

    private String buildMockScanResponse(String fileData) {
        if (StringUtils.containsIgnoreCase(fileData, CUKRADTestConstants.MOCK_INFECTED_FILE_INDICATOR)) {
            return StringUtils.join(ClamAVResponses.STREAM_PREFIX, KFSConstants.BLANK_SPACE,
                    CUKRADTestConstants.MOCK_VIRUS_MESSAGE, KFSConstants.BLANK_SPACE, ClamAVResponses.FOUND_SUFFIX);
        }
        return ClamAVResponses.RESPONSE_OK;
    }

    private void writeResponseToClient(Writer socketWriter, String responseData) throws IOException {
        socketWriter.write(responseData + ClamAVDelimiters.NULL_SUFFIX);
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
        }
        IOUtils.closeQuietly(serverSocket);
        if (connectionHandler != null) {
            waitForHandlerShutdown();
        }
        connectionHandlerThread = null;
        connectionHandler = null;
        forceTempFileError = null;
        serverActive = null;
        serverSocket = null;
    }

    private void waitForHandlerShutdown() {
        try {
            connectionHandler.get(1L, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("waitForHandlerShutdown, Unexpected error while waiting for shutdown", e);
        }
    }

}
