package edu.cornell.kfs.krad.service.impl;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.krad.CUKRADConstants.ClamAVCommand;
import edu.cornell.kfs.krad.CUKRADConstants.ClamAVResponses;
import edu.cornell.kfs.krad.service.AntiVirusService;

public class ClamAVAntiVirusServiceImpl implements AntiVirusService {

    private static final Logger LOG = LogManager.getLogger();

    private static final int DEFAULT_CHUNK_SIZE = 2048;
    //  IDSESSION, END
//    It is mandatory to prefix this command with n or z, and all commands inside IDSESSION must  be
//    prefixed.
//
//    Start/end  a  clamd  session. Within a session multiple SCAN, INSTREAM, FILDES, VERSION, STATS
//    commands can be sent on the same socket without opening new connections.  Replies  from  clamd
//    will  be  in  the form '<id>: <response>' where <id> is the request number (in ascii, starting
//    from 1) and <response> is the usual clamd reply.  The reply lines have same delimiter  as  the
//    corresponding  command had.  Clamd will process the commands asynchronously, and reply as soon
//    as it has finished processing.
//
//    Clamd requires clients to read all the replies it sent, before sending more commands  to  pre-vent prevent
//    vent  send()  deadlocks. The recommended way to implement a client that uses IDSESSION is with
//    non-blocking sockets, and  a  select()/poll()  loop:  whenever  send  would  block,  sleep  in
//    select/poll  until either you can write more data, or read more replies.  Note that using non-blocking nonblocking
//    blocking sockets without the select/poll loop and  alternating  recv()/send()  doesn't  comply
//    with clamd's requirements.
//
//    If  clamd detects that a client has deadlocked,  it will close the connection. Note that clamd
//    may close an IDSESSION connection too if you don't follow the protocol's requirements.


    private int timeout;
    private String host;
    private int port;

    public ClamAVAntiVirusServiceImpl() {}

    public ClamAVAntiVirusServiceImpl(String host, int port, int timeout) {
        setHost(host);
        setPort(port);
        setTimeout(timeout);
    }

    public String stats() {
        return cmd(ClamAVCommand.STATS.asBytes());
    }

    public boolean ping() {
        return ClamAVResponses.PING_RESPONSE_OK.equals(cmd(ClamAVCommand.PING.asBytes()));
    }

    public String cmd(byte[] cmd){

        Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(getHost(), getPort()));
        } catch (IOException e) {
            LOG.error("could not connect to clamd server", e);
            try {
                socket.close();
            } catch (IOException e2) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception closing socket", e2);
                }
            }
            return null;    
        }

        try {
            socket.setSoTimeout(getTimeout());
        } catch (SocketException e) {
                LOG.error("Could not set socket timeout to " + getTimeout() + "ms", e);
        }

        DataOutputStream dos = null;
        String response = "";
        try {  // finally to close resources

            try {
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                LOG.error("could not open socket OutputStream", e);
                return null;
            }

            try {
                dos.write(cmd);
                dos.flush();
            } catch (IOException e){
                LOG.debug("error writing " + new String(cmd) + " command", e);
                return null;
            }

            InputStream is = null;
            try {
                is = socket.getInputStream();
            } catch (IOException e){
                LOG.error("error getting InputStream from socket", e);
                return response;
            }

            int read = DEFAULT_CHUNK_SIZE;
            byte[] buffer = new byte[DEFAULT_CHUNK_SIZE];
            StringBuffer sb = new StringBuffer();
            
            while (read == DEFAULT_CHUNK_SIZE){
                try {
                    read = is.read(buffer);
                } catch (IOException e){
                    LOG.error("error reading result from socket", e);
                    break;
                }
                sb.append(new String(buffer, 0, read));
            }

            response = sb.toString();

        } finally {
            if (dos != null) try { dos.close(); } catch (IOException e) { LOG.debug("exception closing DOS", e); }
            try { socket.close(); } catch (IOException e) { LOG.debug("exception closing socket", e); }
        }

        if (LOG.isDebugEnabled()) LOG.debug( "Response: " + response);

        return response;
        }

    /**
     *
     * The method to call if you already have the content to scan in-memory as a byte array.
     *
     * @param in the byte array to scan
     * @return
     * @throws IOException
     */
    @Override
    public ClamAVScanResult scan(byte[] in) throws IOException {
        return scan(new ByteArrayInputStream(in));
    }

    /**
     *
     * The preferred method to call. This streams the contents of the InputStream to clamd, so
     * the entire content is not loaded into memory at the same time.
     * @param in the InputStream to read.  The stream is NOT closed by this method.
     * @return a ScanResult representing the server response
     * @throws IOException if anything goes awry other than setting the socket timeout or closing the socket
     * or the DataOutputStream wrapping the socket's OutputStream
     */
    @Override
    public ClamAVScanResult scan(InputStream in) {

        Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(getHost(), getPort()));
        } catch (IOException e) {
            LOG.error("could not connect to clamd server", e);
            try {
                socket.close();
            } catch (IOException e2) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("exception closing socket", e2);
                }
            }
            return new ClamAVScanResult(e);    
        }

        try {
            socket.setSoTimeout(getTimeout());
        } catch (SocketException e) {
                LOG.error("Could not set socket timeout to " + getTimeout() + "ms", e);        
        }

        DataOutputStream dos = null;
        String response = "";
        try {  // finally to close resources

            try {
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                LOG.error("could not open socket OutputStream", e);
                return new ClamAVScanResult(e);
            }

            try {
                dos.write(ClamAVCommand.INSTREAM.asBytes());
            } catch (IOException e){
                LOG.debug("error writing INSTREAM command", e);
                return new ClamAVScanResult(e);
            }

            int read = DEFAULT_CHUNK_SIZE;
            byte[] buffer = new byte[DEFAULT_CHUNK_SIZE];
            while (read == DEFAULT_CHUNK_SIZE){
                try {
                    read = in.read(buffer);
                } catch (IOException e) {
                    LOG.debug("error reading from InputStream", e);
                    return new ClamAVScanResult(e);                    
                }
                
                if (read <= 0) { break; }
                
                // we may exceed the clamd size limit, so we don't immediately return
                // if we get an error here.
                try {
                    dos.writeInt(read);
                    dos.write(buffer, 0, read);
                } catch (IOException e){
                    LOG.debug("error writing data to socket", e);
                    break;
                }
            }

            try {
                dos.writeInt(0);
                dos.flush();
            } catch (IOException e){
                LOG.debug("error writing zero-length chunk to socket", e);
            }

            try {
                read = socket.getInputStream().read(buffer);
            } catch (IOException e){
                LOG.debug("error reading result from socket", e);
                read = 0;
            }

            if (read > 0) response = new String(buffer, 0, read);

        } finally {
            if (dos != null) try { dos.close(); } catch (IOException e) { LOG.debug("exception closing DOS", e); }
            try { socket.close(); } catch (IOException e) { LOG.debug("exception closing socket", e); }
        }

        if (LOG.isDebugEnabled()) LOG.debug( "Response: " + response);

        return new ClamAVScanResult(response.trim());
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    /**
     * Socket timeout in milliseconds
     * @param timeout socket timeout in milliseconds
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}
