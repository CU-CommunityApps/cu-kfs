package edu.cornell.kfs.concur.batch.fixture;

import java.util.EnumSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedHeaderEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedPayeeIdEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedTrailerEntry;
import edu.cornell.kfs.concur.ConcurTestConstants.PdpFeedFileConstants;

public enum PdpFeedFileBaseEntryFixture {

    MARSHAL_TEST(HeaderEntry.BASIC_HEADER, TrailerEntry.BASIC_TRAILER, EnumSet.of(GroupEntry.MARSHAL_TEST)),
    FEED_ONE_TRANS_ZERO(HeaderEntry.BASIC_HEADER, TrailerEntry.BASIC_TRAILER, EnumSet.of(GroupEntry.GROUP_ONE_TRANS_ZERO)),
    FEED_ONE_TRANS_ZER_ONE_TRANS_POSITIVE(HeaderEntry.BASIC_HEADER, TrailerEntry.BASIC_TRAILER, EnumSet.of(GroupEntry.GROUP_ONE_TRANS_ZERO_ONE_TRANS_POSITIVE)),
    FEED_TWO_TRANS_SUM_TO_POSITIVE(HeaderEntry.BASIC_HEADER, TrailerEntry.BASIC_TRAILER, EnumSet.of(GroupEntry.GROUP_TWO_TRANS_SUM_TO_POSITIVE)),
    FEED_TWO_TRANS_SUM_TO_ZERO(HeaderEntry.BASIC_HEADER, TrailerEntry.BASIC_TRAILER, EnumSet.of(GroupEntry.GROUP_TWO_TRANS_SUM_TO_ZERO)),
    FEED_TWO_TRANS_SUM_TO_NEGATIVE(HeaderEntry.BASIC_HEADER, TrailerEntry.BASIC_TRAILER, EnumSet.of(GroupEntry.GROUP_TWO_TRANS_SUM_TO_NEGATIVE)),
    FEED_TWO_DETAILS_ONE_POSITIVE_ONE_ZERO(HeaderEntry.BASIC_HEADER, TrailerEntry.BASIC_TRAILER, EnumSet.of(GroupEntry.GROUP_TWO_DETAILS_ONE_POSITIVE_ONE_ZERO)),
    FEED_TWO_GROUPS_ONE_ZERO(HeaderEntry.BASIC_HEADER, TrailerEntry.BASIC_TRAILER, EnumSet.of(GroupEntry.GROUP_TWO_TRANS_SUM_TO_ZERO, GroupEntry.GROUP_TWO_TRANS_SUM_TO_POSITIVE));
    
    public final HeaderEntry headerEntry;
    public final EnumSet<GroupEntry> groupEntries;
    public final TrailerEntry trailerEntry;
    
    private PdpFeedFileBaseEntryFixture(HeaderEntry headerEntry, TrailerEntry trailerEntry, EnumSet<GroupEntry> groupEntries) {
        this.headerEntry = headerEntry;
        this.trailerEntry = trailerEntry;
        this.groupEntries = groupEntries;
    }
    
    public PdpFeedFileBaseEntry toPdpFeedFileBaseEntry() {
        PdpFeedFileBaseEntry pdpFeedFileBaseEntry = new PdpFeedFileBaseEntry();
        pdpFeedFileBaseEntry.setVersion("1.0");
        pdpFeedFileBaseEntry.setHeader(headerEntry.toPdpFeedHeaderEntry());
        pdpFeedFileBaseEntry.setTrailer(trailerEntry.toPdpFeedTrailerEntry());
        for (GroupEntry group : groupEntries) {
            pdpFeedFileBaseEntry.getGroup().add(group.toPdpFeedGroupEntry());
        }
        return pdpFeedFileBaseEntry;
    }

    public enum HeaderEntry {

        BASIC_HEADER(PdpFeedFileConstants.CHART, PdpFeedFileConstants.BATCH_DATE, PdpFeedFileConstants.UNIT, PdpFeedFileConstants.SUB_UNIT);

        public final String chart;
        public final String creationDate;
        public final String unit;
        public final String subUnit;

        private HeaderEntry(String chart, String creationDate, String unit, String subUnit) {
            this.chart = chart;
            this.creationDate = creationDate;
            this.unit = unit;
            this.subUnit = subUnit;
        }
        
        public PdpFeedHeaderEntry toPdpFeedHeaderEntry() {
            PdpFeedHeaderEntry header = new PdpFeedHeaderEntry();
            header.setChart(chart);
            header.setCreationDate(creationDate);
            header.setSubUnit(subUnit);
            header.setUnit(unit);
            return header;
        }
    }
    
    public enum TrailerEntry {
        
        BASIC_TRAILER(1, 96.95);
        
        public final int count;
        public final double amount;
        
        private TrailerEntry(int count, double amount) {
            this.count = count;
            this.amount = amount;
        }
        
        public PdpFeedTrailerEntry toPdpFeedTrailerEntry() {
            PdpFeedTrailerEntry trailer = new PdpFeedTrailerEntry();
            trailer.setDetailCount(new Integer(count));
            trailer.setDetailTotAmt(new KualiDecimal(amount));
            return trailer;
        }
    }
    
    public enum GroupEntry {
        MARSHAL_TEST(PdpFeedFileConstants.PAYEE_NAME_BIMBO, PdpFeedFileConstants.PAYEE_TYPE_VENDOR, PdpFeedFileConstants.PAYEE_ID_BIMBO, 
                PdpFeedFileConstants.CUSTOM_INSTITUTION_IDENTIFIER_BIMBO, EnumSet.of(DetailEntry.MARSHAL_TEST)),
       
        GROUP_ONE_TRANS_ZERO(PdpFeedFileConstants.PAYEE_NAME_JAY, PdpFeedFileConstants.PAYEE_ID_JAY, PdpFeedFileConstants.PAYEE_ID_TYPE, 
                StringUtils.EMPTY, EnumSet.of(DetailEntry.DETAIL_ONE_TRANS_ZERO)),
        GROUP_ONE_TRANS_ZERO_ONE_TRANS_POSITIVE(PdpFeedFileConstants.PAYEE_NAME_JAY, PdpFeedFileConstants.PAYEE_ID_JAY, PdpFeedFileConstants.PAYEE_ID_TYPE, 
                StringUtils.EMPTY, EnumSet.of(DetailEntry.DETAIL_ONE_TRANS_ZERO_ONE_TRANS_POSITIVE)),
        GROUP_TWO_TRANS_SUM_TO_POSITIVE(PdpFeedFileConstants.PAYEE_NAME_JAY, PdpFeedFileConstants.PAYEE_ID_JAY, PdpFeedFileConstants.PAYEE_ID_TYPE, 
                StringUtils.EMPTY, EnumSet.of(DetailEntry.DETAIL_TWO_TRANS_SUM_TO_POSITIVE)),
        GROUP_TWO_TRANS_SUM_TO_ZERO(PdpFeedFileConstants.PAYEE_NAME_JAY, PdpFeedFileConstants.PAYEE_ID_JAY, PdpFeedFileConstants.PAYEE_ID_TYPE, 
                StringUtils.EMPTY, EnumSet.of(DetailEntry.DETAIL_TWO_TRANS_SUM_TO_ZERO)),
        GROUP_TWO_TRANS_SUM_TO_NEGATIVE(PdpFeedFileConstants.PAYEE_NAME_JAY, PdpFeedFileConstants.PAYEE_ID_JAY, PdpFeedFileConstants.PAYEE_ID_TYPE, 
                StringUtils.EMPTY, EnumSet.of(DetailEntry.DETAIL_TWO_TRANS_SUM_TO_NEGATIVE)),
        GROUP_TWO_DETAILS_ONE_POSITIVE_ONE_ZERO(PdpFeedFileConstants.PAYEE_NAME_JAY, PdpFeedFileConstants.PAYEE_ID_JAY, PdpFeedFileConstants.PAYEE_ID_TYPE, 
                StringUtils.EMPTY, EnumSet.of(DetailEntry.DETAIL_TWO_TRANS_SUM_TO_ZERO, DetailEntry.DETAIL_TWO_TRANS_SUM_TO_POSITIVE));
        
        public final String payeeName;
        public final String payeeIdType;
        public final String payeeId;
        public final String payeeOwnershipCode;
        public final String customerInstitutionIdentifier;
        public final String paymentDate;
        public final String combineGroupInd;
        public final String bankCode;
        public final EnumSet<DetailEntry> detailEntries;
        
        private GroupEntry(String payeeName, String payeeIdType, String payeeId, String payeeOwnershipCode, String customerInstitutionIdentifier, String paymentDate, 
                String combineGroupInd, String bankCode, EnumSet<DetailEntry> detailEntries) {
            this.payeeName = payeeName;
            this.payeeIdType = payeeIdType;
            this.payeeId = payeeId;
            this.payeeOwnershipCode = payeeOwnershipCode;
            this.customerInstitutionIdentifier = customerInstitutionIdentifier;
            this.paymentDate = paymentDate;
            this.combineGroupInd = combineGroupInd;
            this.bankCode = bankCode;
            this.detailEntries = detailEntries;
        }
        
        private GroupEntry(String payeeName, String payeeIdType, String payeeId, String customerInstitutionIdentifier, EnumSet<DetailEntry> detailEntries) {
            this(payeeName, payeeIdType, payeeId, "xyz", customerInstitutionIdentifier, PdpFeedFileConstants.PAYMENT_DATE, PdpFeedFileConstants.COMBINE_GROUP_INDICATOR_YES,
                    PdpFeedFileConstants.BANK_CODE, detailEntries);
        }
        
        public PdpFeedGroupEntry toPdpFeedGroupEntry() {
            PdpFeedGroupEntry group = new PdpFeedGroupEntry();
            group.setPayeeName(payeeName);

            PdpFeedPayeeIdEntry payeeIdEntry = new PdpFeedPayeeIdEntry();
            payeeIdEntry.setIdType(payeeIdType);
            payeeIdEntry.setContent(payeeId);
            group.setPayeeId(payeeIdEntry);
            
            group.setPayeeOwnCd(payeeOwnershipCode);
            group.setCustomerInstitutionIdentifier(customerInstitutionIdentifier);
            group.setPaymentDate(paymentDate);
            group.setCombineGroupInd(combineGroupInd);
            group.setBankCode(bankCode);
            for (DetailEntry detailentry : detailEntries) {
                group.getDetail().add(detailentry.toPdpFeedDetailEntry());
            }
            return group;
        }
    }
    
    public enum DetailEntry {
        MARSHAL_TEST( 105.28, EnumSet.of(AccountingEntry.TRANSACTION_AMOUNT_25_50, AccountingEntry.TRANSACTION_AMOUNT_71_45)),
        DETAIL_ONE_TRANS_ZERO(0, EnumSet.of(AccountingEntry.TRANSACTION_AMOUNT_0)),
        DETAIL_ONE_TRANS_ZERO_ONE_TRANS_POSITIVE(25.50, EnumSet.of(AccountingEntry.TRANSACTION_AMOUNT_0, AccountingEntry.TRANSACTION_AMOUNT_25_50)),
        DETAIL_TWO_TRANS_SUM_TO_POSITIVE(61.45, EnumSet.of(AccountingEntry.TRANSACTION_AMOUNT_71_45, AccountingEntry.TRANSACTION_AMOUNT_NEGATIVE_10)),
        DETAIL_TWO_TRANS_SUM_TO_ZERO(0, EnumSet.of(AccountingEntry.TRANSACTION_AMOUNT_10, AccountingEntry.TRANSACTION_AMOUNT_NEGATIVE_10)),
        DETAIL_TWO_TRANS_SUM_TO_NEGATIVE(-5, EnumSet.of(AccountingEntry.TRANSACTION_AMOUNT_5, AccountingEntry.TRANSACTION_AMOUNT_NEGATIVE_10));
        
        public final String sourceDocNbr;
        public final String invoiceNbr;
        public final String invoiceDate;
        public final double origInvoiceAmt;
        public final String fsOriginCd;
        public final String fdocTypCd;
        public final EnumSet<AccountingEntry> accountingEntries;
        
        private DetailEntry(String sourceDocNbr, String invoiceNbr, String invoiceDate, double origInvoiceAmt, String fsOriginCd, String fdocTypCde, 
                EnumSet<AccountingEntry> accountingEntries) {
            this.sourceDocNbr = sourceDocNbr;
            this.invoiceNbr = invoiceNbr;
            this.invoiceDate = invoiceDate;
            this.origInvoiceAmt = origInvoiceAmt;
            this.fsOriginCd = fsOriginCd;
            this.fdocTypCd = fdocTypCde;
            this.accountingEntries = accountingEntries;
        }
        
        private DetailEntry(double origInvoiceAmt, EnumSet<AccountingEntry> accountingEntries) {
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
            for (AccountingEntry accountingEntry : accountingEntries) {
                detail.getAccounting().add(accountingEntry.toPdpFeedAccountingEntry());
            }
            return detail;
        }
    }
    
    public enum AccountingEntry {
        TRANSACTION_AMOUNT_25_50(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, null, PdpFeedFileConstants.OBJECT_CODE, "365", null, null, 25.50),
        TRANSACTION_AMOUNT_71_45(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, null, PdpFeedFileConstants.OBJECT_CODE, "350", null, null, 71.45),
        TRANSACTION_AMOUNT_10(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, null, PdpFeedFileConstants.OBJECT_CODE, "350", null, null, 10),
        TRANSACTION_AMOUNT_5(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, null, PdpFeedFileConstants.OBJECT_CODE, "350", null, null, 5),
        TRANSACTION_AMOUNT_0(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, null, PdpFeedFileConstants.OBJECT_CODE, null, null, null, 0),
        TRANSACTION_AMOUNT_NEGATIVE_10(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, null, PdpFeedFileConstants.OBJECT_CODE, null, null, null, -10);
        
        public final String coaCode;
        public final String accountNumber;
        public final String subAccountNumber;
        public final String objectCode;
        public final String subObjectCode;
        public final String projectCode;
        public final String orgRefId;
        public final double amount;
        
        private AccountingEntry(String coaCode, String accountNumber, String subAccountNumber, String objectCode, String subObjectCode, String projectCode, 
                String orgRefId, double amount) {
            this.coaCode = coaCode;
            this.accountNumber = accountNumber;
            this.subAccountNumber = subAccountNumber;
            this.objectCode = objectCode;
            this.subObjectCode = subObjectCode;
            this.projectCode = projectCode;
            this.orgRefId = orgRefId;
            this.amount = amount;
        }
        
        public PdpFeedAccountingEntry toPdpFeedAccountingEntry() {
            PdpFeedAccountingEntry accounting = new PdpFeedAccountingEntry();
            accounting.setCoaCd(coaCode);
            accounting.setAccountNbr(accountNumber);
            accounting.setSubAccountNbr(subAccountNumber);
            accounting.setObjectCd(objectCode);
            accounting.setSubObjectCd(subObjectCode);
            accounting.setProjectCd(projectCode);
            accounting.setOrgRefId(orgRefId);
            accounting.setAmount(new KualiDecimal(amount));
            return accounting;
        }
    }
}
