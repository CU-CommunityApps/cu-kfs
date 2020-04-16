package edu.cornell.kfs.concur.service.impl;

import static org.junit.Assert.*;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ConcurReportsServiceImplTest {
    
    private TestableoncurReportsServiceImpl reportsService;

    @Before
    public void setUp() throws Exception {
        reportsService = new TestableoncurReportsServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        reportsService = null;
    }

    @Test
    public void test() {
        fail("Not yet implemented");
    }
    
    private class TestableoncurReportsServiceImpl extends ConcurReportsServiceImpl {
        @Override
        public String getConcurFailedRequestQueueEndpoint() {
            return "https://dummy.test.concur.com/api/platform/notifications/v1.0/notification?status=failed";
        }
        
        @Override
        protected Invocation buildReportDetailsClientRequest(String reportURI, String httpMethod) {
            Invocation invocation = Mockito.mock(Invocation.class);
            
            Response response = Mockito.mock(Response.class);
            
            return invocation;
        }
        
    }

}
