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
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.kuali.kfs.krad.exception.ValidationException;
import org.springframework.http.HttpMethod;

import com.opencsv.CSVReader;

import edu.cornell.kfs.pmw.PaymentWorksTestConstants.SupplierUploadErrorMessages;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksUploadFileColumn;
import edu.cornell.kfs.pmw.batch.businessobject.fixture.PaymentWorksVendorFixture;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants.PaymentWorksCommonJsonConstants;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants.PaymentWorksSupplierUploadConstants;
import edu.cornell.kfs.sys.web.mock.MockServiceCore5EndpointBase;
import edu.cornell.kfs.sys.web.mock.MockServiceEndpointBase;

/**
 * Utility class that mocks the endpoint for uploading vendors back to PaymentWorks.
 * 
 * Note that the returned error messages and status codes do not necessarily line up
 * with what PaymentWorks would return under similar circumstances.
 */
public class MockPaymentWorksUploadSuppliersEndpoint extends MockServiceCore5EndpointBase {

    private static final String UPLOAD_SUPPLIERS_ENDPOINT_HANDLER_PATTERN = "/suppliers/load/";

    private String expectedAuthorizationToken;
    private PaymentWorksVendorFixture[] expectedVendorsForNextUpload;
    private boolean calledUploadSuppliersService;
    private boolean forceVendorCountMismatch;

    public MockPaymentWorksUploadSuppliersEndpoint(String multiPartContentDirectory, String expectedAuthorizationToken) {
        this.multiPartContentDirectory = multiPartContentDirectory;
        this.expectedAuthorizationToken = expectedAuthorizationToken;
    }

    @Override
    public String getRelativeUrlPatternForHandlerRegistration() {
        return UPLOAD_SUPPLIERS_ENDPOINT_HANDLER_PATTERN;
    }

    public void setExpectedVendorsForNextUpload(PaymentWorksVendorFixture[] expectedVendorsForNextUpload) {
        this.expectedVendorsForNextUpload = expectedVendorsForNextUpload;
    }

    public void setForceVendorCountMismatch(boolean forceVendorCountMismatch) {
        this.forceVendorCountMismatch = forceVendorCountMismatch;
    }

    public boolean isCalledUploadSuppliersService() {
        return calledUploadSuppliersService;
    }
    
    @Override
    protected void processRequest(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
            throws HttpException, IOException {
        this.calledUploadSuppliersService = true;
        assertRequestHasCorrectHttpMethod(request, HttpMethod.POST);
        assertRequestHasCorrectContentType(request, ContentType.MULTIPART_FORM_DATA);
        assertHeaderHasNonBlankValue(request, HttpHeaders.CONTENT_LENGTH);
        
        String authorizationHeader = getNonBlankHeaderValue(request, PaymentWorksWebServiceConstants.AUTHORIZATION_HEADER_KEY);
        assertEquals("Wrong authorization token value",
                PaymentWorksWebServiceConstants.AUTHORIZATION_TOKEN_VALUE_STARTER + expectedAuthorizationToken, authorizationHeader);
        
        Pair<Boolean, Object> processingResult = processMultiPartRequestContent(request, this::validateAndProcessMultiPartContent);
        boolean processingSucceeded = processingResult.getKey().booleanValue();
        if (processingSucceeded) {
            setupSuccessResponse(response, (Integer) processingResult.getValue());
        } else {
            setupErrorResponse(response, (String) processingResult.getValue());
        }
        
    }

    private Pair<Boolean, Object> validateAndProcessMultiPartContent(ClassicHttpRequest request, List<FileItem> fileItems) {
        assertEquals("Wrong number of multipart sections in upload-suppliers request", 1, fileItems.size());
        FileItem csvSection = fileItems.get(0);
        assertEquals("Wrong field name for multipart section", PaymentWorksSupplierUploadConstants.SUPPLIERS_FIELD, csvSection.getFieldName());
        assertEquals("Wrong filename for multipart section", PaymentWorksSupplierUploadConstants.DUMMY_SUPPLIERS_FILENAME, csvSection.getName());
        
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
            throw new ValidationException(SupplierUploadErrorMessages.FILE_CONTAINED_ZERO_ROWS);
        }
        validateCsvHeader(csvContent.get(0));
        if (csvContent.size() == 1) {
            throw new ValidationException(SupplierUploadErrorMessages.FILE_ONLY_CONTAINED_HEADER_ROW);
        }
        validateUploadedVendorsIfPresent(csvContent);
    }

    private void validateCsvHeader(String[] firstCsvRow) {
        String[] expectedHeader = Arrays.stream(PaymentWorksUploadFileColumn.values())
                .map(PaymentWorksUploadFileColumn::getHeaderLabel)
                .toArray(String[]::new);
        
        if (!Arrays.equals(expectedHeader, firstCsvRow)) {
            throw new ValidationException(SupplierUploadErrorMessages.FILE_CONTAINED_INVALID_HEADERS);
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
                throw new ValidationException(SupplierUploadErrorMessages.VENDOR_DATA_MISMATCH_ERROR_PREFIX + i);
            }
        }
    }

    private void setupSuccessResponse(ClassicHttpResponse response, Integer numReceivedSuppliers) {
        Integer numReceivedSuppliersToReturn = forceVendorCountMismatch
                ? Integer.valueOf(numReceivedSuppliers.intValue() - 1) : numReceivedSuppliers;
        
        String jsonText = buildJsonTextFromNode((rootNode) -> {
            rootNode.put(PaymentWorksCommonJsonConstants.STATUS_FIELD, PaymentWorksCommonJsonConstants.STATUS_OK);
            rootNode.put(PaymentWorksSupplierUploadConstants.NUM_RCVD_SUPPLIERS_FIELD, numReceivedSuppliersToReturn);
        });
        
        response.setCode(HttpStatus.SC_OK);
        response.setEntity(new StringEntity(jsonText, ContentType.TEXT_HTML));
    }

    private void setupErrorResponse(ClassicHttpResponse response, String errorMessage) {
        String jsonText = buildJsonTextFromNode((rootNode) -> {
            rootNode.put(PaymentWorksCommonJsonConstants.STATUS_FIELD, PaymentWorksSupplierUploadConstants.ERROR_FIELD);
            rootNode.put(PaymentWorksSupplierUploadConstants.ERROR_FIELD, errorMessage);
        });
        
        response.setCode(HttpStatus.SC_BAD_REQUEST);
        response.setEntity(new StringEntity(jsonText, ContentType.TEXT_HTML));
    }
    
    @Override 
    protected void prepareResponseForFailedAssertion(ClassicHttpResponse response, AssertionError assertionError) throws HttpException, IOException {
        setupErrorResponse(response, assertionError.getMessage());
    }

}
