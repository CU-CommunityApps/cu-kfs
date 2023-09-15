package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.IOException;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.module.purap.CuPurapTestConstants.JaggaerMockServerConfiguration;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Status;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierResponseMessage;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.web.mock.MockServiceEndpointBase;
import jakarta.xml.bind.JAXBException;

public class MockJaggaerUploadSuppliersEndpoint extends MockServiceEndpointBase {
    private static final String UPLOAD_SUPPLIERS_ENDPOINT_HANDLER_PATTERN = "/apps/Router/TSMSupplierXMLImport";
    private static final Logger LOG = LogManager.getLogger();

    private final CUMarshalService cuMarshalService;
    private JaggaerMockServerConfiguration jaggaerMockServerConfiguration;

    public MockJaggaerUploadSuppliersEndpoint(CUMarshalService cuMarshalService) {
        this.cuMarshalService = cuMarshalService;
        this.jaggaerMockServerConfiguration = JaggaerMockServerConfiguration.OK;
    }

    @Override
    public String getRelativeUrlPatternForHandlerRegistration() {
        return UPLOAD_SUPPLIERS_ENDPOINT_HANDLER_PATTERN;
    }

    @Override
    protected void processRequest(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
            throws HttpException, IOException {
        LOG.info("processRequest, entered");
        response.setCode(jaggaerMockServerConfiguration.statusCode);
        response.setEntity(new StringEntity(buildResponseMessage()));
    }

    private String buildResponseMessage() {
        SupplierSyncMessage supplierSyncMessage = new SupplierSyncMessage();
        SupplierResponseMessage responseMessage = new SupplierResponseMessage();

        Status status = new Status();
        status.setStatusCode(String.valueOf(jaggaerMockServerConfiguration.statusCode));
        status.setStatusText(jaggaerMockServerConfiguration.responseMessage);

        responseMessage.setStatus(status);
        supplierSyncMessage.getSupplierSyncMessageItems().add(responseMessage);
        return marshalSyncMessageToString(supplierSyncMessage);
    }

    private String marshalSyncMessageToString(SupplierSyncMessage supplierSyncMessage) {
        try {
            return cuMarshalService.marshalObjectToXmlString(supplierSyncMessage);
        } catch (JAXBException | IOException e) {
            LOG.error("marshalSyncMessageToString, had an error creating response string", e);
            throw new RuntimeException(e);
        }
    }

    public JaggaerMockServerConfiguration getJaggaerMockServerConfiguration() {
        return jaggaerMockServerConfiguration;
    }

    public void setJaggaerMockServerConfiguration(JaggaerMockServerConfiguration jaggaerMockServerConfiguration) {
        this.jaggaerMockServerConfiguration = jaggaerMockServerConfiguration;
    }

}
