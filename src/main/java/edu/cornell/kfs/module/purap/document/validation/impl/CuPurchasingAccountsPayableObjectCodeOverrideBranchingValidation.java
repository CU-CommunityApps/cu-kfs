package edu.cornell.kfs.module.purap.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.validation.impl.PurchasingAccountsPayableObjectCodeOverrideBranchingValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.krad.util.ObjectUtils;

public class CuPurchasingAccountsPayableObjectCodeOverrideBranchingValidation extends PurchasingAccountsPayableObjectCodeOverrideBranchingValidation {

    @Override
    protected String determineBranch(final AttributedDocumentEvent event) {
        if (!StringUtils.isBlank(propertyPath)) {
            refreshByPath(accountingLineForValidation);
        }
        
        boolean isTaxApproval = false;
        // KFSPTS-1891 : treasury manager also skip this object type allowed check
        boolean isTreasuryApproval = false;
        //if payment request, skip object code check when this is a tax approval, 
        // or if this accounting line is from a Tax Charge line.
        if (accountingDocumentForValidation instanceof PaymentRequestDocument) {
            final PaymentRequestDocument preq = (PaymentRequestDocument)accountingDocumentForValidation;
            final PurApAccountingLine purapAccountingLine = (PurApAccountingLine)accountingLineForValidation;
            final PurApItem item = purapAccountingLine.getPurapItem();
            
            if (StringUtils.equals(PaymentRequestStatuses.APPDOC_AWAITING_TAX_REVIEW,
                    preq.getApplicationDocumentStatus())) {
            		isTaxApproval = true;
            }
            else if(StringUtils.equals(PaymentRequestStatuses.APPDOC_AWAITING_PAYMENT_METHOD_REVIEW, preq.getApplicationDocumentStatus())) {
            		isTreasuryApproval = true;
            } else if (StringUtils.equals(
                    PaymentRequestStatuses.APPDOC_DEPARTMENT_APPROVED,
                    preq.getApplicationDocumentStatus()
            )
                       && ObjectUtils.isNotNull(item) && item.getItemType().getIsTaxCharge()) {
                isTaxApproval = true;
            }
        }
        
        if (isTaxApproval  || isTreasuryApproval) {
            return null;
        } else if (isAccountingLineValueAllowed(accountingDocumentForValidation.getClass(),
                accountingLineForValidation, parameterToCheckAgainst, propertyPath,
                responsibleProperty != null ? responsibleProperty : propertyPath)) {
            return OBJECT_CODE_OVERRIDEN;
        } else {
            return OBJECT_CODE_NOT_OVERRIDEN;
        }                
    }
}
