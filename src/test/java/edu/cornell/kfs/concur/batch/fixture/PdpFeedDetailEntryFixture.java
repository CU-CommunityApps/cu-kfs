package edu.cornell.kfs.concur.batch.fixture;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurTestConstants.PdpFeedFileConstants;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;

public enum PdpFeedDetailEntryFixture {
    MARSHAL_TEST( 105.28, EnumSet.of(PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_25_50_WITH_SUBOBJECT_350,
            PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_71_45_WITH_SUBOBJECT_350)),
    DETAIL_ONE_TRANS_ZERO(0, EnumSet.of(PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_0)),
    DETAIL_ONE_TRANS_ZERO_ONE_TRANS_POSITIVE(25.50, EnumSet.of(PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_0, 
            PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_25_50_WITH_SUBOBJECT_350)),
    DETAIL_TWO_TRANS_SUM_TO_POSITIVE(61.45, EnumSet.of(PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_71_45_WITH_SUBOBJECT_350, 
            PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_NEGATIVE_10)),
    DETAIL_TWO_TRANS_SUM_TO_ZERO(0, EnumSet.of(PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_10_WITH_SUBOBJECT_350, 
            PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_NEGATIVE_10)),
    DETAIL_TWO_TRANS_SUM_TO_NEGATIVE(-5, EnumSet.of(PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_5_WITH_SUBOBJECT_350, 
            PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_NEGATIVE_10)),
    
    DETAIL_TWO_POSITIVE_1_NEGATIVE(5, EnumSet.of(PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_5, PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_NEGATIVE_10, 
            PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_10)),
    DETAIL_TWO_POSITIVE_2_NEGATIVE(4, EnumSet.of(PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_5, PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_NEGATIVE_10, 
            PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_10, PdpFeedAccountingEntryFixture.TRANSACTION_AMOUNT_NEGATIVE_1));
    
    public final String sourceDocNbr;
    public final String invoiceNbr;
    public final String invoiceDate;
    public final double origInvoiceAmt;
    public final String fsOriginCd;
    public final String fdocTypCd;
    public final Collection<PdpFeedAccountingEntryFixture> accountingEntries;
    
    private PdpFeedDetailEntryFixture(String sourceDocNbr, String invoiceNbr, String invoiceDate, double origInvoiceAmt, String fsOriginCd, String fdocTypCde, 
            EnumSet<PdpFeedAccountingEntryFixture> accountingEntries) {
        this.sourceDocNbr = sourceDocNbr;
        this.invoiceNbr = invoiceNbr;
        this.invoiceDate = invoiceDate;
        this.origInvoiceAmt = origInvoiceAmt;
        this.fsOriginCd = fsOriginCd;
        this.fdocTypCd = fdocTypCde;
        this.accountingEntries = Collections.unmodifiableSet(accountingEntries);
    }
    
    private PdpFeedDetailEntryFixture(double origInvoiceAmt, EnumSet<PdpFeedAccountingEntryFixture> accountingEntries) {
        this(PdpFeedFileConstants.SOURCE_DOC_NUMBER, PdpFeedFileConstants.INVOICE_NUMBER, PdpFeedFileConstants.PAYMENT_DATE, origInvoiceAmt,
                PdpFeedFileConstants.ORIGIN_CODE, PdpFeedFileConstants.FDOC_NUMBER, accountingEntries);
    }
    
    public PdpFeedDetailEntry toPdpFeedDetailEntry() {
        PdpFeedDetailEntry detail = new PdpFeedDetailEntry();
        detail.setSourceDocNbr(sourceDocNbr);
        detail.setInvoiceNbr(invoiceNbr);
        detail.setInvoiceDate(invoiceDate);
        detail.setOrigInvoiceAmt(new KualiDecimal(origInvoiceAmt));
        detail.setFsOriginCd(fsOriginCd);
        detail.setFdocTypCd(fdocTypCd);
        for (PdpFeedAccountingEntryFixture accountingEntry : accountingEntries) {
            detail.getAccounting().add(accountingEntry.toPdpFeedAccountingEntry());
        }
        return detail;
    }
}
