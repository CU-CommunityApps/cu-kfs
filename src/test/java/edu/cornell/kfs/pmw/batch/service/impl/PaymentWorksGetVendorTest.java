package edu.cornell.kfs.pmw.batch.service.impl;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import edu.cornell.kfs.pmw.web.mock.MockPaymentWorksGetVendorController;
import edu.cornell.kfs.sys.web.mock.MockMvcWebServerExtension;

public class PaymentWorksGetVendorTest {

    @RegisterExtension
    MockMvcWebServerExtension mockServerExtension = new MockMvcWebServerExtension();

    private String serverUrl;

    @BeforeEach
    void setUp() throws Exception {
        this.serverUrl = mockServerExtension.getServerUrl();
        mockServerExtension.initializeStandaloneMockMvcWithControllers(
                new MockPaymentWorksGetVendorController());
    }

    @AfterEach
    void tearDown() throws Exception {
        serverUrl = null;
    }

    @Test
    void testSomething() throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        Client client = ClientBuilder.newClient(clientConfig);
        Response response = client.target(serverUrl + "/ping/something?id=2")
                .request().buildGet().invoke();
        System.out.println(response.getStatus());
        System.out.println(response.readEntity(String.class));
    }

    @Test
    void testSomething2() throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        Client client = ClientBuilder.newClient(clientConfig);
        Response response = client.target(serverUrl + "/getsomething")
                .request().buildGet().invoke();
        System.out.println(response.getStatus());
        System.out.println(response.readEntity(String.class));
    }

}
