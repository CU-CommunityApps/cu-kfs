package edu.cornell.kfs.sys.util;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CURestClientUtils {

	private static final Logger LOG = LogManager.getLogger(CURestClientUtils.class);

    public static void closeQuietly(Client client) {
        try {
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            LOG.error("closeQuietly(): Error closing REST client", e);
        }
    }

    public static void closeQuietly(Response response) {
        try {
            if (response != null) {
                response.close();
            }
        } catch (Exception e) {
            LOG.error("closeQuietly(): Error closing REST response", e);
        }
    }

}
