package edu.cornell.kfs.module.ar.document.fixture;

import edu.cornell.kfs.module.ar.CuArTestConstants;

public enum ContractsGrantsInvoiceDocumentFixture {
    DOCUMENT_1_PROPOSAL_12345(CuArTestConstants.PROPOSAL_12345),
    DOCUMENT_2_PROPOSAL_12345(CuArTestConstants.PROPOSAL_12345),
    DOCUMENT_3_PROPOSAL_66666(CuArTestConstants.PROPOSAL_66666),
    DOCUMENT_4_PROPOSAL_97979(CuArTestConstants.PROPOSAL_97979),
    DOCUMENT_5_PROPOSAL_97979(CuArTestConstants.PROPOSAL_97979),
    DOCUMENT_6_PROPOSAL_97979(CuArTestConstants.PROPOSAL_97979);

    public final String documentNumber;
    public final String proposalNumber;

    private ContractsGrantsInvoiceDocumentFixture(String proposalNumber) {
        this.documentNumber = String.valueOf(ordinal() + 1);
        this.proposalNumber = proposalNumber;
    }

    public static ContractsGrantsInvoiceDocumentFixture getByDocumentNumber(String documentNumber) {
        try {
            int ordinalValue = Integer.parseInt(documentNumber) - 1;
            return ContractsGrantsInvoiceDocumentFixture.values()[ordinalValue];
        } catch (Exception e) {
            return null;
        }
    }

}
