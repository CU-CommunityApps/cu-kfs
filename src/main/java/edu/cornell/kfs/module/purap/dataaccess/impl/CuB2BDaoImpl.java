package edu.cornell.kfs.module.purap.dataaccess.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.dataaccess.impl.B2BDaoImpl;
import org.kuali.kfs.module.purap.exception.B2BConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import edu.cornell.kfs.module.purap.CUPurapConstants;

public class CuB2BDaoImpl extends B2BDaoImpl {
    private static final Logger LOG = LogManager.getLogger();

    /**
     * @see org.kuali.kfs.module.purap.dataaccess.impl.B2BDaoImpl#sendPunchOutRequest(java.lang.String, java.lang.String)
     */
    public String sendPunchOutRequest(final String request, final String punchoutUrl) {
        LOG.debug("sendPunchOutRequest() started");

        try {
            URL url = new URL(punchoutUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
			String charSet = "UTF-8";
			if (request.contains("MIME_BOUNDARY_FOR_ATTACHMENTS")) {
				// KFSPTS-794 : for attachments
				conn.setRequestProperty("Content-type", "multipart/related; boundary=" + CUPurapConstants.MIME_BOUNDARY_FOR_ATTACHMENTS);
				charSet = "ISO-8859-1";
				LOG.info("content-type is multipart/related; boundary=" + CUPurapConstants.MIME_BOUNDARY_FOR_ATTACHMENTS);
			} else {
				conn.setRequestProperty("Content-type", "text/xml");

				LOG.info("content-type is text/xml");
			}

            OutputStream out = conn.getOutputStream();
            OutputStreamWriter outw = new OutputStreamWriter(out, charSet);
            outw.write(request);
            outw.flush();
            outw.close();
            out.flush();
            out.close();

            InputStream inp = conn.getInputStream();

            StringBuffer response = new StringBuffer();
            int i = inp.read();
            while (i >= 0) {
                if (i >= 0) {
                    response.append((char) i);
                }
                i = inp.read();
            }
            return response.toString();
        }
        catch (MalformedURLException e) {
            LOG.error("postPunchOutSetupRequestMessage() Error posting setup", e);
            throw new B2BConnectionException("Unable to connect to remote site for punchout.", e);
        }
        catch (ProtocolException e) {
            LOG.error("postPunchOutSetupRequestMessage() Error posting setup", e);
            throw new B2BConnectionException("Unable to connect to remote site for punchout.", e);
        }
        catch (UnsupportedEncodingException e) {
            LOG.error("postPunchOutSetupRequestMessage() Error posting setup", e);
            throw new B2BConnectionException("Unable to connect to remote site for punchout.", e);
        }
        catch (IOException e) {
            LOG.error("postPunchOutSetupRequestMessage() Error posting setup", e);
            throw new B2BConnectionException("Unable to connect to remote site for punchout.", e);
        }
    }

}
