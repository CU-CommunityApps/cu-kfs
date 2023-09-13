package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.IOException;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.entity.HttpEntityWrapper;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;import org.apache.http.protocol.HTTP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierResponseMessage;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Status;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.web.mock.MockServiceEndpointBase;
import jakarta.xml.bind.JAXBException;

public class MockJaggaerUploadSuppliersEndpoint extends MockServiceEndpointBase {
    private static final String UPLOAD_SUPPLIERS_ENDPOINT_HANDLER_PATTERN = "/apps/Router/TSMSupplierXMLImport";
    private static final String OK_RESPONSE_MESSAGE = "Success (Counts:  Total documents attempted=1, Total documents completed=1.  Documents successful without warnings=1)";
    
    private static final Logger LOG = LogManager.getLogger();
    
    private CUMarshalService cuMarshalService;
    
    public MockJaggaerUploadSuppliersEndpoint(CUMarshalService cuMarshalService) {
        this.cuMarshalService = cuMarshalService;
    }

    @Override
    public String getRelativeUrlPatternForHandlerRegistration() {
        return UPLOAD_SUPPLIERS_ENDPOINT_HANDLER_PATTERN;
    }

    @Override
    protected void processRequest(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
            throws HttpException, IOException {
        LOG.info("processRequest, entered");
        response.setCode(HttpStatus.OK.value());
        response.setEntity(new StringEntity(buildOkResponseMessage()));
    }
    
    private String buildOkResponseMessage() {
        SupplierSyncMessage supplierSyncMessage = new SupplierSyncMessage();
        SupplierResponseMessage responseMessage = new SupplierResponseMessage();
        
        Status status = new Status();
        status.setStatusCode(String.valueOf(HttpStatus.OK.value()));
        status.setStatusText(OK_RESPONSE_MESSAGE);
        
        responseMessage.setStatus(status);
        supplierSyncMessage.getSupplierSyncMessageItems().add(responseMessage);
        return marshalSynchMessageToString(supplierSyncMessage);
    }

    private String marshalSynchMessageToString(SupplierSyncMessage supplierSyncMessage) {
        try {
            return cuMarshalService.marshalObjectToXmlString(supplierSyncMessage);
        } catch (JAXBException | IOException e) {
            LOG.error("marshalSynchMessageToString, had an error creating response string", e);
            throw new RuntimeException(e);
        }
    }

}
