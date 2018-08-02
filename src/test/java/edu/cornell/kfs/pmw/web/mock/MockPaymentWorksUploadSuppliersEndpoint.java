package edu.cornell.kfs.pmw.web.mock;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.kuali.kfs.krad.exception.ValidationException;
import org.springframework.http.HttpMethod;

import com.opencsv.CSVReader;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksUploadFileColumn;
import edu.cornell.kfs.pmw.batch.businessobject.fixture.PaymentWorksVendorFixture;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants.PaymentWorksCommonJsonConstants;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants.PaymentWorksSupplierUploadConstants;
import edu.cornell.kfs.sys.web.mock.MockServiceEndpointBase;

public class MockPaymentWorksUploadSuppliersEndpoint extends MockServiceEndpointBase {

    private static final String UPLOAD_SUPPLIERS_ENDPOINT_HANDLER_PATTERN = "/suppliers/load/";

    private PaymentWorksVendorFixture[] expectedVendorsForNextUpload;

    public MockPaymentWorksUploadSuppliersEndpoint(String multiPartContentDirectory) {
        this.multiPartContentDirectory = multiPartContentDirectory;
    }

    @Override
    public String getRelativeUrlPatternForHandlerRegistration() {
        return UPLOAD_SUPPLIERS_ENDPOINT_HANDLER_PATTERN;
    }

    public void setExpectedVendorsForNextUpload(PaymentWorksVendorFixture[] expectedVendorsForNextUpload) {
        this.expectedVendorsForNextUpload = expectedVendorsForNextUpload;
    }

    @Override
    protected void processRequest(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        assertRequestHasCorrectHttpMethod(request, HttpMethod.POST);
        assertRequestHasCorrectContentType(request, ContentType.MULTIPART_FORM_DATA);
        
        Pair<Boolean, Object> processingResult = processMultiPartRequestContent(request, this::validateAndProcessMultiPartContent);
        boolean processingSucceeded = processingResult.getKey().booleanValue();
        if (processingSucceeded) {
            setupSuccessResponse(response, (Integer) processingResult.getValue());
        } else {
            setupErrorResponse(response, (String) processingResult.getValue());
        }
    }

    private Pair<Boolean, Object> validateAndProcessMultiPartContent(HttpRequest request, List<FileItem> fileItems) {
        assertEquals("Wrong number of multipart sections in upload-suppliers request", 1, fileItems.size());
        FileItem csvSection = fileItems.get(0);
        
        try {
            List<String[]> csvContent = readCsvContentFromSection(csvSection);
            validateCsvContent(csvContent);
            return Pair.of(Boolean.TRUE, Integer.valueOf(csvContent.size() - 1));
        } catch (RuntimeException e) {
            return Pair.of(Boolean.FALSE, e.getMessage());
        }
    }

    private List<String[]> readCsvContentFromSection(FileItem csvSection) {
        InputStream csvStream = null;
        InputStreamReader streamReader = null;
        BufferedReader bufferedReader = null;
        CSVReader csvReader = null;
        
        try {
            csvStream = csvSection.getInputStream();
            streamReader = new InputStreamReader(csvStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(streamReader);
            csvReader = new CSVReader(bufferedReader);
            return csvReader.readAll();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            IOUtils.closeQuietly(csvReader);
            IOUtils.closeQuietly(bufferedReader);
            IOUtils.closeQuietly(streamReader);
            IOUtils.closeQuietly(csvStream);
        }
    }

    private void validateCsvContent(List<String[]> csvContent) {
        if (csvContent.size() == 0) {
            throw new ValidationException("File contained zero rows");
        }
        validateCsvHeader(csvContent.get(0));
        if (csvContent.size() == 1) {
            throw new ValidationException("File contained a header row but no data rows");
        }
        validateUploadedVendorsIfPresent(csvContent);
    }

    private void validateCsvHeader(String[] firstCsvRow) {
        String[] expectedHeader = Arrays.stream(PaymentWorksUploadFileColumn.values())
                .map(PaymentWorksUploadFileColumn::getHeaderLabel)
                .toArray(String[]::new);
        
        if (!Arrays.equals(expectedHeader, firstCsvRow)) {
            throw new ValidationException("File did not contain the correct headers");
        }
    }

    private void validateUploadedVendorsIfPresent(List<String[]> csvContent) {
        if (expectedVendorsForNextUpload == null) {
            throw new IllegalStateException("No vendor data expectations have been configured");
        }
        
        for (int i = 0; i < expectedVendorsForNextUpload.length && i < csvContent.size() - 1; i++) {
            String[] expectedData = expectedVendorsForNextUpload[i].toParsedCsvFieldArray();
            String[] actualData = csvContent.get(i + 1);
            if (!Arrays.equals(expectedData, actualData)) {
                throw new ValidationException("Unexpected data values detected on vendor at index " + i);
            }
        }
    }

    private void setupSuccessResponse(HttpResponse response, Integer numReceivedSuppliers) {
        String jsonText = buildJsonTextFromNode((rootNode) -> {
            rootNode.put(PaymentWorksCommonJsonConstants.STATUS_FIELD, PaymentWorksCommonJsonConstants.STATUS_OK);
            rootNode.put(PaymentWorksSupplierUploadConstants.NUM_RCVD_SUPPLIERS_FIELD, numReceivedSuppliers);
        });
        
        response.setStatusCode(HttpStatus.SC_OK);
        response.setEntity(new StringEntity(jsonText, ContentType.TEXT_HTML));
    }

    private void setupErrorResponse(HttpResponse response, String errorMessage) {
        String jsonText = buildJsonTextFromNode((rootNode) -> {
            rootNode.put(PaymentWorksCommonJsonConstants.STATUS_FIELD, PaymentWorksSupplierUploadConstants.ERROR_FIELD);
            rootNode.put(PaymentWorksSupplierUploadConstants.ERROR_FIELD, errorMessage);
        });
        
        response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        response.setEntity(new StringEntity(jsonText, ContentType.TEXT_HTML));
    }

}
