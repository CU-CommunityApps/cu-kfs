package edu.cornell.kfs.concur.batch.fixture;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.concur.ConcurTestConstants.PdpFeedFileConstants;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedPayeeIdEntry;

public enum PdpFeedGroupEntryFixture {
    MARSHAL_TEST(PdpFeedFileConstants.PAYEE_NAME_BIMBO, PdpFeedFileConstants.PAYEE_TYPE_VENDOR, PdpFeedFileConstants.PAYEE_ID_BIMBO, 
            PdpFeedFileConstants.CUSTOM_INSTITUTION_IDENTIFIER_BIMBO, EnumSet.of(PdpFeedDetailEntryFixture.MARSHAL_TEST)),
   
    GROUP_ONE_TRANS_ZERO(PdpFeedFileConstants.PAYEE_NAME_JAY, PdpFeedFileConstants.PAYEE_ID_JAY, PdpFeedFileConstants.PAYEE_ID_TYPE, 
            StringUtils.EMPTY, EnumSet.of(PdpFeedDetailEntryFixture.DETAIL_ONE_TRANS_ZERO)),
    GROUP_ONE_TRANS_ZERO_ONE_TRANS_POSITIVE(PdpFeedFileConstants.PAYEE_NAME_JAY, PdpFeedFileConstants.PAYEE_ID_JAY, PdpFeedFileConstants.PAYEE_ID_TYPE, 
            StringUtils.EMPTY, EnumSet.of(PdpFeedDetailEntryFixture.DETAIL_ONE_TRANS_ZERO_ONE_TRANS_POSITIVE)),
    GROUP_TWO_TRANS_SUM_TO_POSITIVE(PdpFeedFileConstants.PAYEE_NAME_JAY, PdpFeedFileConstants.PAYEE_ID_JAY, PdpFeedFileConstants.PAYEE_ID_TYPE, 
            StringUtils.EMPTY, EnumSet.of(PdpFeedDetailEntryFixture.DETAIL_TWO_TRANS_SUM_TO_POSITIVE)),
    GROUP_TWO_TRANS_SUM_TO_ZERO(PdpFeedFileConstants.PAYEE_NAME_JAY, PdpFeedFileConstants.PAYEE_ID_JAY, PdpFeedFileConstants.PAYEE_ID_TYPE, 
            StringUtils.EMPTY, EnumSet.of(PdpFeedDetailEntryFixture.DETAIL_TWO_TRANS_SUM_TO_ZERO)),
    GROUP_TWO_TRANS_SUM_TO_NEGATIVE(PdpFeedFileConstants.PAYEE_NAME_JAY, PdpFeedFileConstants.PAYEE_ID_JAY, PdpFeedFileConstants.PAYEE_ID_TYPE, 
            StringUtils.EMPTY, EnumSet.of(PdpFeedDetailEntryFixture.DETAIL_TWO_TRANS_SUM_TO_NEGATIVE)),
    GROUP_TWO_DETAILS_ONE_POSITIVE_ONE_ZERO(PdpFeedFileConstants.PAYEE_NAME_JAY, PdpFeedFileConstants.PAYEE_ID_JAY, PdpFeedFileConstants.PAYEE_ID_TYPE, 
            StringUtils.EMPTY, EnumSet.of(PdpFeedDetailEntryFixture.DETAIL_TWO_TRANS_SUM_TO_ZERO, PdpFeedDetailEntryFixture.DETAIL_TWO_TRANS_SUM_TO_POSITIVE));
    
    public final String payeeName;
    public final String payeeIdType;
    public final String payeeId;
    public final String payeeOwnershipCode;
    public final String customerInstitutionIdentifier;
    public final String paymentDate;
    public final String combineGroupInd;
    public final String bankCode;
    public final Collection<PdpFeedDetailEntryFixture> detailEntries;
    
    private PdpFeedGroupEntryFixture(String payeeName, String payeeIdType, String payeeId, String payeeOwnershipCode, String customerInstitutionIdentifier, String paymentDate, 
            String combineGroupInd, String bankCode, EnumSet<PdpFeedDetailEntryFixture> detailEntries) {
        this.payeeName = payeeName;
        this.payeeIdType = payeeIdType;
        this.payeeId = payeeId;
        this.payeeOwnershipCode = payeeOwnershipCode;
        this.customerInstitutionIdentifier = customerInstitutionIdentifier;
        this.paymentDate = paymentDate;
        this.combineGroupInd = combineGroupInd;
        this.bankCode = bankCode;
        this.detailEntries = Collections.unmodifiableSet(detailEntries);
    }
    
    private PdpFeedGroupEntryFixture(String payeeName, String payeeIdType, String payeeId, String customerInstitutionIdentifier, EnumSet<PdpFeedDetailEntryFixture> detailEntries) {
        this(payeeName, payeeIdType, payeeId, PdpFeedFileConstants.PAYMENT_OWNERSHIP_CODE, customerInstitutionIdentifier, PdpFeedFileConstants.PAYMENT_DATE, PdpFeedFileConstants.COMBINE_GROUP_INDICATOR_YES,
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
        for (PdpFeedDetailEntryFixture detailentry : detailEntries) {
            group.getDetail().add(detailentry.toPdpFeedDetailEntry());
        }
        return group;
    }
}
