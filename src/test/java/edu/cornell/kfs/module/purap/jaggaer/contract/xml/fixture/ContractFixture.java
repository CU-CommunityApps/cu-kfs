package edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

public enum ContractFixture {
    CONTRACT01("", "", "", "", "", "", 100.00, contractParties(), customFields(), managers(), attachments());

    public final String contractId;
    public final String contractName;
    public final String contractNumber;
    public final String contractType;
    public final String contractStatus;
    public final String contractSummary;
    public final KualiDecimal contractValue;
    public final List<ContractPartyFixture> contractParties;
    public final Map<FieldType, List<String>> customFields;
    public final List<String> managers;
    public final List<AttachmentFileFixture> attachments;

    private ContractFixture(String contractId, String contractName, String contractNumber, String contractType,
            String contractStatus, String contractSummary, double contractValue,
            ContractPartyFixture[] contractParties, Map.Entry<FieldType, List<String>>[] customFields,
            String[] managers, AttachmentFileFixture[] attachments) {
        this.contractId = contractId;
        this.contractName = contractName;
        this.contractNumber = contractNumber;
        this.contractType = contractType;
        this.contractStatus = contractStatus;
        this.contractSummary = contractSummary;
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
    private static Map.Entry<FieldType, List<String>>[] customFields(
            Map.Entry<FieldType, List<String>>... customFields) {
        return customFields;
    }

    private static Map.Entry<FieldType, List<String>> customField(FieldType fieldType, String... values) {
        return Map.entry(fieldType, List.of(values));
    }

    private static String[] managers(String... managers) {
        return managers;
    }

    private static AttachmentFileFixture[] attachments(AttachmentFileFixture... attachments) {
        return attachments;
    }

}
