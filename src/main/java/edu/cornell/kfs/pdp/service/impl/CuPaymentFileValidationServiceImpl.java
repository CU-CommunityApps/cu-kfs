package edu.cornell.kfs.pdp.service.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.businessobject.PayeeType;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentFileLoad;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.impl.PaymentFileValidationServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.OriginationCode;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.util.MessageMap;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuPaymentFileValidationServiceImpl extends PaymentFileValidationServiceImpl {
    
    @Override
    protected void processGroupValidation(PaymentFileLoad paymentFile, MessageMap errorMap) {
        int groupCount = 0;
        for (PaymentGroup paymentGroup : paymentFile.getPaymentGroups()) {
            groupCount++;
            int noteLineCount = 0;
            int detailCount = 0;

            // We've encountered Payment Files that have address lines exceeding the column size in DB table;
            // so adding extra validation on payment group BO, especially the max length, based on DD definitions.
            // Check that PaymentGroup String properties don't exceed maximum allowed length
            checkPaymentGroupPropertyMaxLength(paymentGroup, errorMap);

            // verify payee id and owner code if customer requires them to be filled in
            if (paymentFile.getCustomer().getPayeeIdRequired() && StringUtils.isBlank(paymentGroup.getPayeeId())) {
                errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_PAYEE_ID_REQUIRED, Integer.toString(groupCount));
            }

            if (paymentFile.getCustomer().getOwnershipCodeRequired() && StringUtils.isBlank(paymentGroup.getPayeeOwnerCd())) {
                errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_PAYEE_OWNER_CODE, Integer.toString(groupCount));
            }

            // validate payee id type
            if (StringUtils.isNotBlank(paymentGroup.getPayeeIdTypeCd())) {
                PayeeType payeeType = businessObjectService.findBySinglePrimaryKey(PayeeType.class, paymentGroup.getPayeeIdTypeCd());
                if (payeeType == null) {
                    errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_INVALID_PAYEE_ID_TYPE, Integer.toString(groupCount), paymentGroup.getPayeeIdTypeCd());
                }
            }
            
            // validate vendor id and customer institution number
            try {
                paymentGroup.validateVendorIdAndCustomerInstitutionIdentifier(); 
            } catch(RuntimeException e1) {
                errorMap.putError(KFSConstants.GLOBAL_ERRORS, CUKFSKeyConstants.ERROR_BATCH_UPLOAD_PARSING_XML, new String[] { e1.getMessage() });
            }
            
            // validate bank
            String bankCode = paymentGroup.getBankCode();
            if (StringUtils.isNotBlank(bankCode)) {
                Bank bank = bankService.getByPrimaryId(bankCode);
                if (bank == null) {
                    errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_INVALID_BANK_CODE, Integer.toString(groupCount), bankCode);
                }
                else if (!bank.isActive()) {
                    errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_INACTIVE_BANK_CODE, Integer.toString(groupCount), bankCode);
                }
            }

            KualiDecimal groupTotal = KualiDecimal.ZERO;
            for (PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {
                detailCount++;

                noteLineCount++; // Add a line to print the invoice number
                noteLineCount = noteLineCount + paymentDetail.getNotes().size();

                if ((paymentDetail.getNetPaymentAmount() == null) && (!paymentDetail.isDetailAmountProvided())) {
                    paymentDetail.setNetPaymentAmount(paymentDetail.getAccountTotal());
                }
                else if ((paymentDetail.getNetPaymentAmount() == null) && (paymentDetail.isDetailAmountProvided())) {
                    paymentDetail.setNetPaymentAmount(paymentDetail.getCalculatedPaymentAmount());
                }

                // compare net to accounting segments
                if (paymentDetail.getAccountTotal().compareTo(paymentDetail.getNetPaymentAmount()) != 0) {
                    errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_DETAIL_TOTAL_MISMATCH, Integer.toString(groupCount), Integer.toString(detailCount), paymentDetail.getAccountTotal().toString(), paymentDetail.getNetPaymentAmount().toString());
                }

                // validate origin code if given
                if (StringUtils.isNotBlank(paymentDetail.getFinancialSystemOriginCode())) {
                    OriginationCode originationCode = originationCodeService.getByPrimaryKey(paymentDetail.getFinancialSystemOriginCode());
                    if (originationCode == null) {
                        errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_INVALID_ORIGIN_CODE, Integer.toString(groupCount), Integer.toString(detailCount), paymentDetail.getFinancialSystemOriginCode());
                    }
                }

                // validate doc type if given
                if (StringUtils.isNotBlank(paymentDetail.getFinancialDocumentTypeCode())) {
                    if ( !documentTypeService.isActiveByName(paymentDetail.getFinancialDocumentTypeCode()) ) {
                        errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_INVALID_DOC_TYPE, Integer.toString(groupCount), Integer.toString(detailCount), paymentDetail.getFinancialDocumentTypeCode());
                    }
                }

                groupTotal = groupTotal.add(paymentDetail.getNetPaymentAmount());
            }

            // verify total for group is not negative
            if (groupTotal.doubleValue() < 0) {
                errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_NEGATIVE_GROUP_TOTAL, Integer.toString(groupCount));
            }

            // check that the number of detail items and note lines will fit on a check stub
            if (noteLineCount > getMaxNoteLines()) {
                errorMap.putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.ERROR_PAYMENT_LOAD_MAX_NOTE_LINES, Integer.toString(groupCount), Integer.toString(noteLineCount), Integer.toString(getMaxNoteLines()));
            }
        }
    }


}
