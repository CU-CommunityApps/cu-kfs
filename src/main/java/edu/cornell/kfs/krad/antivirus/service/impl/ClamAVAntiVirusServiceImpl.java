package edu.cornell.kfs.krad.antivirus.service.impl;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.krad.CUKRADConstants.ClamAVCommands;
import edu.cornell.kfs.krad.CUKRADConstants.ClamAVDelimiters;
import edu.cornell.kfs.krad.CUKRADConstants.ClamAVResponses;
import edu.cornell.kfs.krad.antivirus.service.AntiVirusService;

public class ClamAVAntiVirusServiceImpl implements AntiVirusService {

    private static final Logger LOG = LogManager.getLogger();

    private static final int DEFAULT_CHUNK_SIZE = 2048;

    private int timeout;
    private String host;
    private int port;

    public ClamAVAntiVirusServiceImpl() {}

    public ClamAVAntiVirusServiceImpl(String host, int port, int timeout) {
        setHost(host);
        setPort(port);
        setTimeout(timeout);
    }

    private <T> T performClamAVOperation(ClamAVOperation<T> operation,
            Function<Exception, T> errorResultBuilder) {
        try (
            Socket socket = new Socket();
        ) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            try (
                InputStream socketInput = socket.getInputStream();
                Reader socketReader = new InputStreamReader(socketInput, StandardCharsets.UTF_8);
                OutputStream socketOutput = socket.getOutputStream();
                DataOutputStream socketDataOutput = new DataOutputStream(socketOutput);
            ) {
                return operation.run(socketReader, socketDataOutput);
            }
        } catch (Exception e) {
            LOG.error("performClamAVOperation, Exception occurred while running ClamAV operation", e);
            return errorResultBuilder.apply(e);
        }
    }

    public String stats() {
        return runSimpleCommand(ClamAVCommands.STATS);
    }

    public boolean ping() {
        String pingResponse = runSimpleCommand(ClamAVCommands.PING);
        return StringUtils.equals(ClamAVResponses.RESPONSE_PING_SUCCESS, pingResponse);
    }

    private String runSimpleCommand(String command) {
        return performClamAVOperation(
                (socketReader, socketOutput) -> runSimpleCommand(command, socketReader, socketOutput),
                exception -> KFSConstants.EMPTY_STRING);
    }

    private String runSimpleCommand(String command, Reader socketReader, DataOutputStream socketOutput)
            throws IOException {
        sendSimpleCommandToClamAV(command, socketOutput);
        socketOutput.flush();
        return readClamAVResponse(socketReader);
    }

    private void sendSimpleCommandToClamAV(String command, DataOutputStream socketOutput) throws IOException {
        if (StringUtils.isBlank(command)) {
            throw new IllegalArgumentException("command cannot be blank");
        }
        String delimitedCommand = ClamAVDelimiters.Z_PREFIX + command + ClamAVDelimiters.NULL_SUFFIX;
        byte[] delimitedCommandAsBytes = delimitedCommand.getBytes(StandardCharsets.UTF_8);
        socketOutput.write(delimitedCommandAsBytes);
    }

    private String readClamAVResponse(Reader socketReader) throws IOException {
        char[] responseChunk = new char[DEFAULT_CHUNK_SIZE];
        StringBuilder fullResponse = new StringBuilder(DEFAULT_CHUNK_SIZE);
        int responseTerminatorIndex = -1;
        int chunkLength;
        
        do {
            chunkLength = socketReader.read(responseChunk);
            if (chunkLength > 0) {
                fullResponse.append(responseChunk, 0, chunkLength);
                responseTerminatorIndex = fullResponse.indexOf(
                        ClamAVDelimiters.NULL_SUFFIX, fullResponse.length() - chunkLength);
            }
        } while (responseTerminatorIndex == -1 && chunkLength > 0);
        
        if (responseTerminatorIndex == -1) {
            LOG.error("readClamAVResponse, End of stream was reached before reading full ClamAV response!  " +
                    "No content will be returned.\n" +
                    "Response text received prior to stream end: " + fullResponse);
            return KFSConstants.EMPTY_STRING;
        } else if (responseTerminatorIndex < fullResponse.length() - 1) {
            int extraContentLength = fullResponse.length() - responseTerminatorIndex - 1;
            LOG.warn("readClamAVResponse, ClamAV sent at least an extra " + extraContentLength +
                    " characters of response data beyond the delimiter!  The extra characters will be ignored.\n" +
                    "Response text received beyond end-of-content delimiter: " +
                    fullResponse.substring(responseTerminatorIndex + 1));
        }
        
        String responsePrecedingTerminator = fullResponse.substring(0, responseTerminatorIndex);
        return responsePrecedingTerminator;
    }

    @Override
    public ClamAVScanResult scan(byte[] fileContents) throws IOException {
        try (
            InputStream preLoadedFileStream = new ByteArrayInputStream(fileContents);
        ) {
            return scan(preLoadedFileStream);
        } catch (IOException e) {
            LOG.error("scan, Unexpected error encountered while scanning pre-loaded file", e);
            throw e;
        }
    }

    @Override
    public ClamAVScanResult scan(InputStream fileStream) {
        return performClamAVOperation(
                (socketReader, socketOutput) -> performStreamedScan(fileStream, socketReader, socketOutput),
                exception -> new ClamAVScanResult(exception));
    }

    private ClamAVScanResult performStreamedScan(
            InputStream fileStream, Reader socketReader, DataOutputStream socketOutput) throws IOException {
        sendSimpleCommandToClamAV(ClamAVCommands.INSTREAM, socketOutput);
        sendFileContentsToClamAV(fileStream, socketOutput);
        String response = readClamAVResponse(socketReader);
        return new ClamAVScanResult(response);
    }

    private void sendFileContentsToClamAV(InputStream fileStream, DataOutputStream socketOutput) throws IOException {
        byte[] fileChunk = new byte[DEFAULT_CHUNK_SIZE];
        boolean continueSendingFileContents = true;
        boolean fileWasFullySent = false;
        int chunkLength;
        
        do {
            chunkLength = fileStream.read(fileChunk);
            if (chunkLength <= 0) {
                continueSendingFileContents = false;
                fileWasFullySent = true;
            } else {
                try {
                    socketOutput.writeInt(chunkLength);
                    socketOutput.write(fileChunk, 0, chunkLength);
                } catch (IOException e) {
                    LOG.error("sendFileContentsToClamAV, Exception occurred when streaming file contents to ClamAV; " +
                            "the file size limit may have been exceeded.", e);
                    continueSendingFileContents = false;
                }
            }
        } while (continueSendingFileContents);
        
        sendEndOfFileContentToClamAV(socketOutput, fileWasFullySent);
    }

    private void sendEndOfFileContentToClamAV(DataOutputStream socketOutput, boolean fileWasFullySent) {
        try {
            socketOutput.writeInt(0);
            socketOutput.flush();
        } catch (IOException e) {
            if (fileWasFullySent) {
                LOG.error("sendEndOfFileContentToClamAV, Exception occurred when ending stream of file to ClamAV", e);
            } else {
                LOG.error("sendEndOfFileContentToClamAV, Exception occurred when ending stream of file to ClamAV, " +
                        "but an exception also occurred while sending the file content itself.  " +
                        "Refer to the previous log entries to identify the prior error.");
            }
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /*
     * TODO: When base financials upgrades to commons-lang3 version 3.9 or later,
     * remove this interface and replace its usage with the lang3 FailableBiFunction interface (or equivalent).
     */
    @FunctionalInterface
    private static interface ClamAVOperation<T> {
        T run(Reader socketReader, DataOutputStream socketOutput) throws IOException;
    }

}
