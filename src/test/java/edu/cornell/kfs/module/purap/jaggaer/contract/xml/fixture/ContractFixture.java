package edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractCustomFields;

public enum ContractFixture {
    EXAMPLE_TEST_ADOBE_SIGN("1890123", "Example for Testing Adobe Sign", "9876-PQR-000111-2323",
            "ProfessionalServicesAgreement", "", "for testing only", 1000.00,
            contractParties(ContractPartyFixture.CORNELL_UNIVERSITY, ContractPartyFixture.TESTING_AND_TRYING_LLC),
            customFields(
                    customField(JaggaerContractCustomFields.PROCUREMENT_AGENT, "Test User - Janet Smith"),
                    customField(JaggaerContractCustomFields.ACCOUNT, "1234567"),
                    emptyCustomField(JaggaerContractCustomFields.SUB_ACCOUNT),
                    emptyCustomField(JaggaerContractCustomFields.OBJECT_CODE),
                    emptyCustomField(JaggaerContractCustomFields.SUB_OBJECT_CODE),
                    emptyCustomField(JaggaerContractCustomFields.PROJECT),
                    emptyCustomField(JaggaerContractCustomFields.ORG_REF_ID)
            ),
            managers("jqd555"),
            attachments(
                    AttachmentFileFixture.JOHN_COMPILED_DOCUMENT,
                    AttachmentFileFixture.JOHN_SIGN_TEST2,
                    AttachmentFileFixture.JOHN_MAIN_DOCUMENT,
                    AttachmentFileFixture.JOHN_SIGN_TEST2_NO_PRESET
            ));

    public final String contractId;
    public final String contractName;
    public final String contractNumber;
    public final String contractType;
    public final String contractStatus;
    public final String summary;
    public final KualiDecimal contractValue;
    public final List<ContractPartyFixture> contractParties;
    public final Map<String, List<String>> customFields;
    public final List<String> managers;
    public final List<AttachmentFileFixture> attachments;

    private ContractFixture(String contractId, String contractName, String contractNumber, String contractType,
            String contractStatus, String summary, double contractValue,
            ContractPartyFixture[] contractParties, Map.Entry<String, List<String>>[] customFields,
            String[] managers, AttachmentFileFixture[] attachments) {
        this.contractId = contractId;
        this.contractName = contractName;
        this.contractNumber = contractNumber;
        this.contractType = contractType;
        this.contractStatus = contractStatus;
        this.summary = summary;
        this.contractValue = new KualiDecimal(contractValue);
        this.contractParties = List.of(contractParties);
        this.customFields = Map.ofEntries(customFields);
        this.managers = List.of(managers);
        this.attachments = List.of(attachments);
    }

    /*
     * The following methods are only meant for simplifying the setup of the enum constants.
     */

    private static ContractPartyFixture[] contractParties(ContractPartyFixture... contractParties) {
        return contractParties;
    }

    @SafeVarargs
    private static Map.Entry<String, List<String>>[] customFields(
            Map.Entry<String, List<String>>... customFields) {
        return customFields;
    }

    private static Map.Entry<String, List<String>> customField(String name, String... values) {
        return Map.entry(name, List.of(values));
    }

    private static Map.Entry<String, List<String>> emptyCustomField(String name) {
        return Map.entry(name, List.of());
    }

    private static String[] managers(String... managers) {
        return managers;
    }

    private static AttachmentFileFixture[] attachments(AttachmentFileFixture... attachments) {
        return attachments;
    }

}
