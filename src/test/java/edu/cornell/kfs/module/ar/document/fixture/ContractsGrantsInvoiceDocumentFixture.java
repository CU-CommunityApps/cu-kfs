package edu.cornell.kfs.module.ar.document.fixture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.mockito.Mockito;

import edu.cornell.kfs.module.cg.businessobject.fixture.AwardFixture;

public enum ContractsGrantsInvoiceDocumentFixture {
    DOCUMENT_1_PROPOSAL_12345(AwardFixture.AWARD_12345_INV_AWARD, InvoiceAccountDetailFixture.ACCOUNT_IT_1122333_IT_9000000),
    DOCUMENT_2_PROPOSAL_12345(AwardFixture.AWARD_12345_INV_AWARD, InvoiceAccountDetailFixture.ACCOUNT_IT_9988777_IT_9000000),
    DOCUMENT_3_PROPOSAL_66666(AwardFixture.AWARD_66666_INV_AWARD, InvoiceAccountDetailFixture.ACCOUNT_IT_1122333_IT_9000000),
    DOCUMENT_4_PROPOSAL_97979(AwardFixture.AWARD_97979_INV_AWARD, InvoiceAccountDetailFixture.ACCOUNT_IT_1122333_IT_9000000),
    DOCUMENT_5_PROPOSAL_97979(AwardFixture.AWARD_97979_INV_AWARD, InvoiceAccountDetailFixture.ACCOUNT_IT_9988777_IT_9000000),
    DOCUMENT_6_PROPOSAL_97979(AwardFixture.AWARD_97979_INV_AWARD, InvoiceAccountDetailFixture.ACCOUNT_PZ_5555555_IT_9000000),
    DOCUMENT_7_PROPOSAL_97979(AwardFixture.AWARD_30000_INV_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_PZ_5555555_IT_9000000),
    DOCUMENT_8_PROPOSAL_97979(AwardFixture.AWARD_30000_INV_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_PZ_5555555_IT_9000000),
    DOCUMENT_9_PROPOSAL_97979(AwardFixture.AWARD_11114_INV_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_PZ_5555555_IT_9000000),
    DOCUMENT_10_PROPOSAL_97979(AwardFixture.AWARD_11114_INV_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_PZ_5555555_IT_9000000),
    DOCUMENT_11_PROPOSAL_97979(AwardFixture.AWARD_11114_INV_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_PZ_5555555_IT_9000000);

    public final String documentNumber;
    public final AwardFixture awardFixture;
    public final List<InvoiceAccountDetailFixture> accountDetails;

    private ContractsGrantsInvoiceDocumentFixture(AwardFixture awardFixture, InvoiceAccountDetailFixture... accountDetails) {
        this.documentNumber = String.valueOf(ordinal() + 1);
        this.awardFixture = awardFixture;
        this.accountDetails = Collections.unmodifiableList(Arrays.asList(accountDetails));
    }

    public ContractsGrantsInvoiceDocument toMockContractsGrantsInvoiceDocument(
            Supplier<ContractsGrantsInvoiceDocument> mockDocumentGenerator) {
        ContractsGrantsInvoiceDocument document = mockDocumentGenerator.get();
        document.setDocumentNumber(documentNumber);
        document.setInvoiceGeneralDetail(toMockInvoiceGeneralDetail());
        document.setAccountDetails(buildInvoiceAccountDetails());
        return document;
    }

    public InvoiceGeneralDetail toMockInvoiceGeneralDetail() {
        InvoiceGeneralDetail invoiceGeneralDetail = Mockito.spy(new InvoiceGeneralDetail());
        invoiceGeneralDetail.setDocumentNumber(documentNumber);
        invoiceGeneralDetail.setProposalNumber(awardFixture.proposalNumber);
        Mockito.doReturn(awardFixture.toAward())
                .when(invoiceGeneralDetail).getAward();
        return invoiceGeneralDetail;
    }

    private List<InvoiceAccountDetail> buildInvoiceAccountDetails() {
        return accountDetails.stream()
                .map(detailFixture -> detailFixture.toInvoiceAccountDetail(documentNumber))
                .collect(Collectors.toCollection(ArrayList::new));
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
