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
    DOCUMENT_1_AWARD_12345(AwardFixture.AWARD_12345_INV_AWARD, InvoiceAccountDetailFixture.ACCOUNT_IT_1122333_IT_9000000),
    DOCUMENT_2_AWARD_12345(AwardFixture.AWARD_12345_INV_AWARD,
            InvoiceAccountDetailFixture.ACCOUNT_IT_1122333_IT_9000000, InvoiceAccountDetailFixture.ACCOUNT_IT_9988777_IT_9000000),
    DOCUMENT_3_AWARD_66666(AwardFixture.AWARD_66666_INV_AWARD, InvoiceAccountDetailFixture.ACCOUNT_PZ_1122333_JJ_9000000),
    DOCUMENT_4_AWARD_97979(AwardFixture.AWARD_97979_INV_AWARD, InvoiceAccountDetailFixture.ACCOUNT_JJ_1122333_PZ_9000000),
    DOCUMENT_5_AWARD_97979(AwardFixture.AWARD_97979_INV_AWARD,
            InvoiceAccountDetailFixture.ACCOUNT_JJ_1122333_PZ_9000000, InvoiceAccountDetailFixture.ACCOUNT_JJ_9988777_PZ_9000000),
    DOCUMENT_6_AWARD_97979(AwardFixture.AWARD_97979_INV_AWARD, InvoiceAccountDetailFixture.ACCOUNT_PZ_5555555_PZ_9000000),
    DOCUMENT_7_AWARD_30000(AwardFixture.AWARD_30000_INV_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_IT_2000000_IT_3575357),
    DOCUMENT_8_AWARD_30000(AwardFixture.AWARD_30000_INV_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_IT_2000000_IT_3575357),
    DOCUMENT_9_AWARD_11114(AwardFixture.AWARD_11114_INV_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_JJ_2000000_JJ_3575357),
    DOCUMENT_10_AWARD_11114(AwardFixture.AWARD_11114_INV_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_JJ_2000000_JJ_3575357),
    DOCUMENT_11_AWARD_11114(AwardFixture.AWARD_11114_INV_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_JJ_5555555_JJ_3575357),
    DOCUMENT_12_AWARD_24680(AwardFixture.AWARD_24680_INV_SCHEDULE, InvoiceAccountDetailFixture.ACCOUNT_HG_2000000_HG_3575357),
    DOCUMENT_13_AWARD_24680(AwardFixture.AWARD_24680_INV_SCHEDULE, InvoiceAccountDetailFixture.ACCOUNT_HG_2000000_HG_3575357),
    DOCUMENT_14_AWARD_97531(AwardFixture.AWARD_97531_INV_SCHEDULE, InvoiceAccountDetailFixture.ACCOUNT_BC_2000000_BC_3575357),
    DOCUMENT_15_AWARD_97531(AwardFixture.AWARD_97531_INV_SCHEDULE, InvoiceAccountDetailFixture.ACCOUNT_BC_2000000_BC_3575357),
    DOCUMENT_16_AWARD_97531(AwardFixture.AWARD_97531_INV_SCHEDULE, InvoiceAccountDetailFixture.ACCOUNT_BC_5555555_BC_3575357),
    DOCUMENT_17_AWARD_33433(AwardFixture.AWARD_33433_INV_CC_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_IT_5555555_BC_9000000),
    DOCUMENT_18_AWARD_33433(AwardFixture.AWARD_33433_INV_CC_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_IT_5555555_BC_9000000),
    DOCUMENT_19_AWARD_99899(AwardFixture.AWARD_99899_INV_CC_ACCOUNT,
            InvoiceAccountDetailFixture.ACCOUNT_HG_1122333_PZ_3575357, InvoiceAccountDetailFixture.ACCOUNT_HG_5555555_PZ_3575357),
    DOCUMENT_20_AWARD_99899(AwardFixture.AWARD_99899_INV_CC_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_PZ_2000000_PZ_2244668),
    DOCUMENT_21_AWARD_99899(AwardFixture.AWARD_99899_INV_CC_ACCOUNT, InvoiceAccountDetailFixture.ACCOUNT_HG_9988777_PZ_3575357);

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

    public static ContractsGrantsInvoiceDocumentFixture getFixtureByDocumentNumber(String documentNumber) {
        try {
            int ordinalValue = Integer.parseInt(documentNumber) - 1;
            return ContractsGrantsInvoiceDocumentFixture.values()[ordinalValue];
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not find fixture for document number: " + documentNumber, e);
        }
    }

}
