package edu.cornell.kfs.krad.service.impl;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.krad.CUKRADConstants.ClamAVCommands;
import edu.cornell.kfs.krad.CUKRADConstants.ClamAVDelimiters;
import edu.cornell.kfs.krad.CUKRADConstants.ClamAVResponses;
import edu.cornell.kfs.krad.service.AntiVirusService;
import edu.cornell.kfs.krad.util.ClamAVUtils;

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
                OutputStream socketOutput = socket.getOutputStream();
                DataOutputStream socketDataOutput = new DataOutputStream(socketOutput);
            ) {
                return operation.run(socketInput, socketDataOutput);
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
        return StringUtils.equals(ClamAVResponses.PING_RESPONSE_OK, pingResponse);
    }

    private String runSimpleCommand(String command) {
        return performClamAVOperation(
                (socketInput, socketOutput) -> runSimpleCommand(command, socketInput, socketOutput),
                exception -> KFSConstants.EMPTY_STRING);
    }

    private String runSimpleCommand(String command, InputStream socketInput, DataOutputStream socketOutput)
            throws IOException {
        sendSimpleCommandToClamAV(command, socketOutput);
        socketOutput.flush();
        return readClamAVResponse(socketInput);
    }

    private void sendSimpleCommandToClamAV(String command, DataOutputStream socketOutput) throws IOException {
        byte[] commandAsBytes = command.getBytes(StandardCharsets.UTF_8);
        byte[] delimitedCommandAsBytes = new byte[commandAsBytes.length + 2];
        delimitedCommandAsBytes[0] = ClamAVDelimiters.Z_PREFIX_BYTE;
        System.arraycopy(commandAsBytes, 0, delimitedCommandAsBytes, 1, commandAsBytes.length);
        delimitedCommandAsBytes[delimitedCommandAsBytes.length - 1] = ClamAVDelimiters.NULL_SUFFIX_BYTE;
        socketOutput.write(delimitedCommandAsBytes);
    }

    private String readClamAVResponse(InputStream socketInput) throws IOException {
        byte[] responseData = new byte[DEFAULT_CHUNK_SIZE];
        int previousLength = 0;
        int currentLength = 0;
        int responseTerminatorIndex = -1;
        int chunkLength;
        
        do {
            chunkLength = socketInput.read(responseData, currentLength, responseData.length - currentLength);
            if (chunkLength > 0) {
                previousLength = currentLength;
                currentLength += chunkLength;
                responseTerminatorIndex = ClamAVUtils.indexOfNullCharDelimiter(
                        responseData, previousLength, currentLength);
                if (responseData.length == currentLength) {
                    responseData = Arrays.copyOf(responseData, responseData.length + DEFAULT_CHUNK_SIZE);
                }
            }
        } while (responseTerminatorIndex == -1 && chunkLength > 0);
        
        if (responseTerminatorIndex == -1) {
            LOG.error("readClamAVResponse, End of stream was reached before reading full ClamAV response!  " +
                    "No content will be returned.  " + currentLength + " bytes were received prior to abrupt end.");
            if (LOG.isDebugEnabled()) {
                logPartialClamAVResponse(responseData, 0, currentLength);
            }
            return KFSConstants.EMPTY_STRING;
        } else if (responseTerminatorIndex < currentLength - 1) {
            int extraContentLength = currentLength - responseTerminatorIndex - 1;
            LOG.warn("readClamAVResponse, ClamAV sent an extra " + extraContentLength +
                    " bytes of response data beyond the delimiter!  The extra bytes will be ignored.");
            if (LOG.isDebugEnabled()) {
                logPartialClamAVResponse(responseData, responseTerminatorIndex + 1, extraContentLength);
            }
        }
        
        String responsePrecedingTerminator = new String(
                responseData, 0, responseTerminatorIndex, StandardCharsets.UTF_8);
        return responsePrecedingTerminator;
    }

    private void logPartialClamAVResponse(byte[] responseData, int start, int length) {
        try {
            String responseString = new String(responseData, start, length, StandardCharsets.UTF_8);
            LOG.debug("logPartialClamAVResponse, Approximate text of partial response data: " + responseString);
        } catch (RuntimeException e) {
            LOG.debug("logPartialClamAVResponse, Partial response data could not be represented as text");
        }
    }

    @Override
    public ClamAVScanResult scan(byte[] fileContents) throws IOException {
        try (InputStream preLoadedFileStream = new ByteArrayInputStream(fileContents);) {
            return scan(preLoadedFileStream);
        }
    }

    @Override
    public ClamAVScanResult scan(InputStream fileStream) {
        return performClamAVOperation(
                (socketInput, socketOutput) -> performStreamedScan(fileStream, socketInput, socketOutput),
                exception -> new ClamAVScanResult(exception));
    }

    private ClamAVScanResult performStreamedScan(
            InputStream fileStream, InputStream socketInput, DataOutputStream socketOutput) throws IOException {
        sendSimpleCommandToClamAV(ClamAVCommands.INSTREAM, socketOutput);
        sendFileContentsToClamAV(fileStream, socketOutput);
        String response = readClamAVResponse(socketInput);
        return new ClamAVScanResult(response);
    }

    private void sendFileContentsToClamAV(InputStream fileStream, DataOutputStream socketOutput) throws IOException {
        byte[] fileChunk = new byte[DEFAULT_CHUNK_SIZE];
        boolean continueSendingFileContents = true;
        int chunkLength;
        
        do {
            chunkLength = fileStream.read(fileChunk);
            if (chunkLength <= 0) {
                continueSendingFileContents = false;
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
        
        try {
            socketOutput.writeInt(0);
            socketOutput.flush();
        } catch (IOException e) {
            if (chunkLength > 0) {
                LOG.error("sendFileContentsToClamAV, Exception occurred when ending stream of file to ClamAV, " +
                        "but an exception also occurred while sending the file content itself.  " +
                        "Refer to the previous log entries to identify the prior error.");
            } else {
                LOG.error("sendFileContentsToClamAV, Exception occurred when ending stream of file to ClamAV", e);
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
        T run(InputStream socketInput, DataOutputStream socketOutput) throws IOException;
    }

}
