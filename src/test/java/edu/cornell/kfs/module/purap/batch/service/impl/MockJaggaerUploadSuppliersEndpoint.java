package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.IOException;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.module.purap.CuPurapTestConstants.JaggaerMockServerCongiration;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Status;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierResponseMessage;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.web.mock.MockServiceEndpointBase;
import jakarta.xml.bind.JAXBException;

public class MockJaggaerUploadSuppliersEndpoint extends MockServiceEndpointBase {
    private static final String UPLOAD_SUPPLIERS_ENDPOINT_HANDLER_PATTERN = "/apps/Router/TSMSupplierXMLImport";
    private static final Logger LOG = LogManager.getLogger();

    private CUMarshalService cuMarshalService;
    private JaggaerMockServerCongiration jaggaerMockServerCongiration;

    public MockJaggaerUploadSuppliersEndpoint(CUMarshalService cuMarshalService) {
        this.cuMarshalService = cuMarshalService;
        this.jaggaerMockServerCongiration = JaggaerMockServerCongiration.OK;
    }

    @Override
    public String getRelativeUrlPatternForHandlerRegistration() {
        return UPLOAD_SUPPLIERS_ENDPOINT_HANDLER_PATTERN;
    }

    @Override
    protected void processRequest(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
            throws HttpException, IOException {
        LOG.info("processRequest, entered");
        response.setCode(jaggaerMockServerCongiration.statusCode);
        response.setEntity(new StringEntity(buildResponseMessage()));
    }

    private String buildResponseMessage() {
        SupplierSyncMessage supplierSyncMessage = new SupplierSyncMessage();
        SupplierResponseMessage responseMessage = new SupplierResponseMessage();

        Status status = new Status();
        status.setStatusCode(String.valueOf(jaggaerMockServerCongiration.statusCode));
        status.setStatusText(jaggaerMockServerCongiration.responseMessage);

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
    
    public JaggaerMockServerCongiration getJaggaerMockServerCongiration() {
        return jaggaerMockServerCongiration;
    }

    public void setJaggaerMockServerCongiration(JaggaerMockServerCongiration jaggaerMockServerCongiration) {
        this.jaggaerMockServerCongiration = jaggaerMockServerCongiration;
    }

}
