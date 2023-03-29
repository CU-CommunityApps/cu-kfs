package edu.cornell.kfs.module.purap.jaggaer.contract.xml;

import static edu.cornell.kfs.sys.util.CuAssertions.assertDateEquals;
import static edu.cornell.kfs.sys.util.CuAssertions.assertListEquals;
import static edu.cornell.kfs.sys.util.CuAssertions.assertStringEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractCustomFields;
import edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture.AttachmentFileFixture;
import edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture.ContractFixture;
import edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture.ContractMessageFixture;
import edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture.ContractPartyFixture;
import edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture.HeaderFixture;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.util.CuXMLStreamUtils;
import edu.cornell.kfs.sys.util.XMLStreamReaderWrapper;

public class ContractMessageUnmarshalTest {

    private static final String CONTRACT_TEST_FILE_PATH =
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

    @Test
    void testUnmarshalMessageWithSingleContract() throws Exception {
        assertContractMessageUnmarshalsCorrectly(
                ContractMessageFixture.SINGLE_TEST_CONTRACT, "SingleTestContract.xml");
    }

    private void assertContractMessageUnmarshalsCorrectly(
            ContractMessageFixture expectedMessage, String fileName) throws Exception {
        ContractMessage actualMessage = readContractMessageFromXml(fileName);
        assertContractMessageIsCorrect(expectedMessage, actualMessage);
    }

    private ContractMessage readContractMessageFromXml(String fileName) throws Exception {
        try (
            InputStream inputStream = CuCoreUtilities.getResourceAsStream(CONTRACT_TEST_FILE_PATH + fileName);
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            XMLStreamReaderWrapper wrappedXmlReader = buildXMLStreamReader(reader);
        ) {
            return cuMarshalService.unmarshalXMLStreamReader(
                    wrappedXmlReader.getXMLStreamReader(), ContractMessage.class);
        }
    }

    private XMLStreamReaderWrapper buildXMLStreamReader(Reader reader) throws XMLStreamException {
        XMLStreamReader xmlReader = null;
        boolean success = false;
        
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            xmlReader = inputFactory.createXMLStreamReader(reader);
            XMLStreamReaderWrapper wrappedXmlReader = new XMLStreamReaderWrapper(xmlReader);
            success = true;
            return wrappedXmlReader;
        } catch (Exception e) {
            success = false;
            throw e;
        } finally {
            if (!success) {
                CuXMLStreamUtils.closeQuietly(xmlReader);
            }
        }
    }

    private void assertContractMessageIsCorrect(ContractMessageFixture fixture, ContractMessage contractMessage) {
        assertStringEquals(fixture.version, contractMessage.getVersion(), "Wrong message version");
        assertHeaderIsCorrect(fixture.header, contractMessage.getHeader());
        assertListEquals(fixture.contracts, contractMessage.getContracts(),
                this::assertContractIsCorrect, "Contracts");
    }

    private void assertHeaderIsCorrect(HeaderFixture fixture, Header header) {
        assertNotNull(header, "Header DTO should not have been null");
        assertStringEquals(fixture.messageId, header.getMessageId(), "Wrong message ID");
        assertDateEquals(fixture.timestamp, header.getTimestamp(), "Wrong timestamp");
        
        Authentication authentication = header.getAuthentication();
        assertNotNull(authentication, "Authentication DTO should not have been null");
        assertStringEquals(fixture.identity, authentication.getIdentity(), "Wrong identity string");
        assertStringEquals(fixture.sharedSecret, authentication.getSharedSecret(), "Wrong shared secret");
    }

    private void assertContractIsCorrect(ContractFixture fixture, Contract contract) {
        assertStringEquals(fixture.contractId, contract.getContractId(), "Wrong contract ID");
        assertStringEquals(fixture.contractName, contract.getContractName(), "Wrong contract name");
        assertStringEquals(fixture.contractNumber, contract.getContractNumber(), "Wrong contract number");
        assertStringEquals(fixture.contractType, contract.getContractType(), "Wrong contract type");
        assertStringEquals(fixture.contractStatus, contract.getContractStatus(), "Wrong contract status");
        assertStringEquals(fixture.summary, contract.getSummary(), "Wrong contract summary");
        assertEquals(fixture.contractValue, contract.getContractValue(), "Wrong contract value");
        assertListEquals(fixture.contractParties, contract.getContractParties(),
                this::assertContractPartyIsCorrect, "Contract Parties");
        assertCustomFieldsAreCorrect(fixture.customFields, contract.getCustomFields());
        assertManagersAreCorrect(fixture.managers, contract.getManagerList());
        assertListEquals(fixture.attachments, contract.getAttachments(),
                this::assertAttachmentIsCorrect, "Attachments");
    }

    private void assertContractPartyIsCorrect(ContractPartyFixture fixture, ContractPartyBase contractParty) {
        if (fixture.isFirstParty) {
            assertTrue(contractParty instanceof FirstParty, "Contract party should have been a 'first' party");
        } else {
            assertTrue(contractParty instanceof SecondParty, "Contract party should have been a 'second' party");
        }
        assertEquals(fixture.isPrimary, contractParty.isPrimary(), "Wrong primary party setting");
        assertStringEquals(fixture.name, contractParty.getName(), "Wrong name");
        assertStringEquals(fixture.sciquestId, contractParty.getSciquestId(), "Wrong SciQuest ID");
        assertStringEquals(fixture.erpNumber, contractParty.getErpNumber(), "Wrong ERP number");
        assertStringEquals(fixture.contactId, contractParty.getContactId(), "Wrong contact ID");
        assertStringEquals(fixture.addressId, contractParty.getAddressId(), "Wrong address ID");
        
    }

    private void assertCustomFieldsAreCorrect(
            Map<String, List<String>> expectedFields, List<CustomField> customFields) {
        Set<String> actualFieldsOfInterest = new HashSet<>();
        assertTrue(CollectionUtils.size(customFields) >= expectedFields.size(),
                "Contract should have had at least " + expectedFields.size() + " custom fields");
        
        for (CustomField customField : customFields) {
            String customFieldName = customField.getInternalName();
            Optional<Map.Entry<String, List<String>>> expectedEntry = getExpectedEntryForCustomField(
                    expectedFields, customFieldName);
            if (expectedEntry.isEmpty()) {
                continue;
            }
            String cleanedFieldName = expectedEntry.get().getKey();
            List<String> expectedValues = expectedEntry.get().getValue();
            assertTrue(actualFieldsOfInterest.add(cleanedFieldName),
                    "Duplicate custom field found (possibly under different numeric suffixes): " + cleanedFieldName);
            assertListEquals(expectedValues, customField.getCustomFieldValues(),
                    this::assertCustomFieldValueIsCorrect, "Custom Field Values");
        }
        
        assertEquals(expectedFields.size(), actualFieldsOfInterest.size(),
                "Wrong number of processable custom fields");
    }

    private Optional<Map.Entry<String, List<String>>> getExpectedEntryForCustomField(
            Map<String, List<String>> expectedFields, String customFieldName) {
        Map.Entry<String, List<String>> expectedEntry = null;
        List<String> expectedValues = expectedFields.get(customFieldName);
        if (expectedValues != null) {
            expectedEntry = Map.entry(customFieldName, expectedValues);
        } else {
            Matcher fieldMatcher = JaggaerContractCustomFields.SUFFIXED_FIELD_PATTERN.matcher(customFieldName);
            if (fieldMatcher.matches()) {
                String nonSuffixedFieldName = fieldMatcher.group(
                        JaggaerContractCustomFields.FIELD_NAME_PATTERN_GROUP);
                expectedValues = expectedFields.get(nonSuffixedFieldName);
                if (expectedValues != null) {
                    expectedEntry = Map.entry(nonSuffixedFieldName, expectedValues);
                }
            }
        }
        return Optional.ofNullable(expectedEntry);
    }

    private void assertCustomFieldValueIsCorrect(String expected, String actual) {
        assertStringEquals(expected, actual, "Wrong custom field value");
    }

    private void assertManagersAreCorrect(List<String> expected, ManagerList managerList) {
        assertNotNull(managerList, "ManagerList DTO should not have been null");
        assertListEquals(expected, managerList.getUsers(), this::assertManagerIsCorrect, "Manager Users");
    }

    private void assertManagerIsCorrect(String expected, String actual) {
        assertStringEquals(expected, actual, "Wrong manager");
    }

    private void assertAttachmentIsCorrect(AttachmentFileFixture fixture, Attachment attachment) {
        AttachmentFile attachmentFile = attachment.getAttachmentFile();
        assertNotNull(attachmentFile, "AttachmentFile DTO should not have been null");
        
        assertStringEquals(fixture.id, attachmentFile.getId(), "Wrong attachment ID");
        assertEquals(fixture.version, attachmentFile.getVersion(), "Wrong attachment version");
        assertEquals(fixture.size, attachmentFile.getSize(), "Wrong attachment size");
        assertDateEquals(fixture.dateUploaded, attachmentFile.getDateUploaded(), "Wrong upload date");
        assertStringEquals(fixture.attachmentDisplayName, attachmentFile.getAttachmentDisplayName(),
                "Wrong display name");
        assertStringEquals(fixture.attachmentFileName, attachmentFile.getAttachmentFileName(), "Wrong file name");
        assertStringEquals(fixture.attachmentType, attachmentFile.getAttachmentType(), "Wrong attachment type");
        assertStringEquals(fixture.attachmentFTPpath, attachmentFile.getAttachmentFTPpath(), "Wrong FTP path");
        assertStringEquals(fixture.getAttachmentAsBase64String(), attachmentFile.getAttachmentBase64(),
                "Wrong Base64-encoded value for attachment with text: " + fixture.attachmentAsPlainText);
    }

}
