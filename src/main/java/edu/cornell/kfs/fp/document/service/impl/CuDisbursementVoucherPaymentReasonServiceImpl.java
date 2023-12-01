package edu.cornell.kfs.fp.document.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kuali.kfs.fp.document.service.impl.DisbursementVoucherPaymentReasonServiceImpl;

public class CuDisbursementVoucherPaymentReasonServiceImpl extends DisbursementVoucherPaymentReasonServiceImpl {
    
    @Override
    protected String getDescriptivePayeeTypesAsString(final Collection<String> payeeTypeCodes) {
        final List<String> payeeTypeDescriptions = new ArrayList<>();

        for (final String payeeTypeCode : payeeTypeCodes) {
            final String description = disbursementVoucherPayeeService.getPayeeTypeDescription(payeeTypeCode);
            if (payeeTypeDescriptions.indexOf(description) == -1) {
                payeeTypeDescriptions.add(description);
            }
        }

        return convertListToString(payeeTypeDescriptions);
    }
}
