package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.vnd.businessobject.VendorContact;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactFileEntityContactTabRowBo;

@SuppressWarnings("deprecation")
public class CemiEntityContactFileEntityContactTabRowBoFactory {
    
    private VendorContact vendorContact;
    private String supplierId;
    private int supplierIndex;
    private int contactIndex;
    private boolean maskSensitiveData = true;

    public CemiEntityContactFileEntityContactTabRowBoFactory (final VendorContact vendorContact,
            final String supplierId, final int supplierIndex, final int contactIndex, final boolean maskSensitiveData) {
        this.vendorContact = vendorContact;
        this.supplierId = supplierId;
        this.supplierIndex = supplierIndex;
        this.contactIndex = contactIndex;
        this.maskSensitiveData = maskSensitiveData;
    }
     
    public CemiEntityContactFileEntityContactTabRowBo createCemiEntityContactFileEntityContactTabRowBo() {
        Validate.validState(ObjectUtils.isNotNull(vendorContact), "Vendor Contact cannot be null");
        Validate.validState(StringUtils.isNotBlank(supplierId), "Supplier ID cannot be blank");
        Validate.validState(supplierIndex > 0, "Supplier Index must be a positive integer");
        Validate.validState(contactIndex > 0, "Contact Index must be a positive integer");

        final CemiEntityContactFileEntityContactTabRowBo entityContactRow
                = new CemiEntityContactFileEntityContactTabRowBo();

//        final String rowSpreadsheetKey = buildSpreadsheetKey(award.getProposalNumber());
//        final String rowAwardScheduleReferenceId = buildAwardScheduleReferenceId(award.getProposalNumber());
//        final String rowAwardScheduleName = determineAwardScheduleName(award.getAwardProjectTitle());
//        final String rowAwardPeriodReferenceId = buildAwardPeriodReferenceId(award.getProposalNumber());
//        final String rowAwardPostingIntervalId = buildAwardPostingIntervalId(award.getProposalNumber());
//        final String rowAwardIntervalStartDate = determineFormattedDate(awardExtendedAttribute.getBudgetBeginningDate());
//        final String rowAwardIntervalEndDate = determineFormattedDate(awardExtendedAttribute.getBudgetEndingDate());

        entityContactRow.setVendorContactGeneratedIdentifier(vendorContact.getVendorContactGeneratedIdentifier());

        // ...

        return entityContactRow;
    }

//
// All of these methods are examples of data value conversion routines.
//    
//    private static String buildSpreadsheetKey(final String awardProposalNumber) {
//        return MessageFormat.format(CemiAwardScheduleConstants.SPREADSHEET_KEY_FORMAT, awardProposalNumber);
//    }
//    
//    private static String buildAwardPeriodReferenceId(final String awardProposalNumber) {
//        return MessageFormat.format(CemiAwardScheduleConstants.AWARD_PERIOD_REFERENCE_ID_FORMAT, awardProposalNumber);
//    }
//    
//    private static String buildAwardPostingIntervalId(final String awardProposalNumber) {
//        return MessageFormat.format(CemiAwardScheduleConstants.AWARD_POSTING_INTERVAL_ID_FORMAT, awardProposalNumber);
//    }
//    
//    private String determineAwardScheduleName(String awardScheduleName) {
//        return StringUtils.isNotBlank(awardScheduleName) ? awardScheduleName : KFSConstants.EMPTY_STRING;
//    }
//    
//    private String determineFormattedDate(Date dateToFormat) {
//        return ObjectUtils.isNotNull(dateToFormat)
//                ? dateTimeService.toString(dateToFormat, CemiBaseConstants.DATE_FORMAT_yyyy_MM_dd)
//                        : KFSConstants.EMPTY_STRING;
//    }
//    
//    private static String buildAwardScheduleReferenceId(final String awardProposalNumber) {
//        return MessageFormat.format(CemiAwardScheduleConstants.AWARD_SCHEDULE_REFERENCE_ID_FORMAT, awardProposalNumber);
//    }
    
}
