package edu.cornell.kfs.module.purap.jaggaer.contract.xml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.sys.KFSConstants;
import org.xmlunit.builder.Input;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerXmlConstants;
import edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture.DocumentExportResponseFixture;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.util.CuXMLUnitTestUtils;

public class DocumentExportResponseMarshalTest {

    private static final String XML_TEST_FILE_PATH =
            "classpath:edu/cornell/kfs/module/purap/jaggaer/contract/xml/";

    private CUMarshalService cuMarshalService;

    @BeforeEach
    void setUp() throws Exception {
        cuMarshalService = new CUMarshalServiceImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        cuMarshalService = null;
    }

    static Stream<Arguments> exportResponses() {
        return Stream.of(
                Arguments.of(DocumentExportResponseFixture.RESPONSE_200_OK, "Response200.xml"),
                Arguments.of(DocumentExportResponseFixture.RESPONSE_201_CREATED, "Response201.xml"),
                Arguments.of(DocumentExportResponseFixture.RESPONSE_400_BAD_REQUEST, "Response400.xml"),
                Arguments.of(DocumentExportResponseFixture.RESPONSE_403_FORBIDDEN, "Response403.xml"),
                Arguments.of(DocumentExportResponseFixture.RESPONSE_500_INTERNAL_SERVER_ERROR, "Response500.xml")
        );
    }

    @ParameterizedTest
    @MethodSource("exportResponses")
    void testMarshalDocumentExportResponse(DocumentExportResponseFixture fixture, String responseXmlFileName)
            throws Exception {
        try (
            InputStream expectedXmlStream = CuCoreUtilities.getResourceAsStream(
                    XML_TEST_FILE_PATH + responseXmlFileName);
            InputStreamReader expectedXmlReader = new InputStreamReader(expectedXmlStream, StandardCharsets.UTF_8);
        ) {
            Input.Builder expectedXmlResult = Input.fromReader(expectedXmlReader);
            
            DocumentExportResponse actualResponse = fixture.toDocumentExportResponse();
            String actualResponseXml = generateResponseXmlFromDTO(actualResponse);
            Input.Builder actualXmlResult = Input.fromString(actualResponseXml);
            
            CuXMLUnitTestUtils.compareXML(expectedXmlResult, actualXmlResult);
        }
    }

    private String generateResponseXmlFromDTO(DocumentExportResponse exportResponse) throws Exception {
        String exportResponseXmlString = cuMarshalService.marshalObjectToXmlFragmentString(exportResponse);
        return StringUtils.join(JaggaerXmlConstants.XML_DECLARATION, KFSConstants.NEWLINE,
                JaggaerXmlConstants.DOCTYPE_DOCUMENT_EXPORT_RESPONSE_DECLARATION, KFSConstants.NEWLINE,
                exportResponseXmlString, KFSConstants.NEWLINE);
    }

}
