package edu.cornell.kfs.module.purap.dataaccess.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.dataaccess.impl.B2BDaoImpl;
import org.kuali.kfs.module.purap.exception.B2BConnectionException;
import org.springframework.http.MediaType;

import edu.cornell.kfs.module.purap.CUPurapConstants;

public class CuB2BDaoImpl extends B2BDaoImpl {
    private static final Logger LOG = LogManager.getLogger();

    /**
     * @see org.kuali.kfs.module.purap.dataaccess.impl.B2BDaoImpl#sendPunchOutRequest(java.lang.String, java.lang.String)
     */
    @Override
    public String sendPunchOutRequest(final String request, final String punchoutUrl) {
    	 LOG.debug("sendPunchOutRequest() started");

         try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
             final HttpPost httpPost = new HttpPost(punchoutUrl);
             if (request.contains("MIME_BOUNDARY_FOR_ATTACHMENTS")) {
            	// KFSPTS-794 : for attachments
                 final Map<String, String> parameters = new HashMap<String, String>();
                 parameters.put("boundary", CUPurapConstants.MIME_BOUNDARY_FOR_ATTACHMENTS);
                 httpPost.addHeader(HttpHeaders.CONTENT_TYPE, new MediaType(MediaType.MULTIPART_RELATED, parameters));
                 httpPost.setEntity(new StringEntity(request, StandardCharsets.ISO_8859_1));
                 LOG.info("content-type is multipart/related; boundary=" + CUPurapConstants.MIME_BOUNDARY_FOR_ATTACHMENTS);
             }
             else {
                 httpPost.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML);
                 httpPost.setEntity(new StringEntity(request,  StandardCharsets.UTF_8));
                 LOG.info("content-type is text/xml");
             }
             
             try (
                 CloseableHttpResponse response = httpClient.execute(httpPost);
                 HttpEntity entity = response.getEntity()
             ) {
                return EntityUtils.toString(entity);
             }

         } catch (IOException | ParseException e) {
             LOG.error("postPunchOutSetupRequestMessage() Error posting setup", e);
             throw new B2BConnectionException("Unable to connect to remote site for punchout.", e);
         }
    }

}
