package edu.cornell.kfs.pmw.web.mock;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;

import edu.cornell.kfs.sys.web.mock.MockServiceEndpointBase;

public class MockPaymentWorksUploadSuppliersEndpoint extends MockServiceEndpointBase {

    private static final String UPLOAD_SUPPLIERS_ENDPOINT_HANDLER_PATTERN = "/suppliers/load/";

    @Override
    public String getRelativeUrlPatternForHandlerRegistration() {
        return UPLOAD_SUPPLIERS_ENDPOINT_HANDLER_PATTERN;
    }

    @Override
    protected void processRequest(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        assertRequestHasCorrectHttpMethod(request, HttpMethod.POST);
        assertRequestHasCorrectContentType(request, ContentType.MULTIPART_FORM_DATA);
        String textContent = getRequestContentAsString(request);
        System.out.println("Request content:\n\n" + textContent);
    }

}
