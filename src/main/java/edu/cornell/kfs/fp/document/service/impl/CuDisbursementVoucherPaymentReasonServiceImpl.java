package edu.cornell.kfs.fp.document.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kuali.kfs.fp.document.service.impl.DisbursementVoucherPaymentReasonServiceImpl;

public class CuDisbursementVoucherPaymentReasonServiceImpl extends DisbursementVoucherPaymentReasonServiceImpl {
    
    protected String getDescriptivePayeeTypesAsString(Collection<String> payeeTypeCodes) {
        List<String> payeeTypeDescriptions = new ArrayList<>();

        for (String payeeTypeCode : payeeTypeCodes) {
            String description = disbursementVoucherPayeeService.getPayeeTypeDescription(payeeTypeCode);
            if (payeeTypeDescriptions.indexOf(description) == -1) {
                payeeTypeDescriptions.add(description);
            }
        }

        return this.convertListToString(payeeTypeDescriptions);
    }
}
