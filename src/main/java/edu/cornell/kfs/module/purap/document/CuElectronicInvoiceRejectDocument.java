package edu.cornell.kfs.module.purap.document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceRejectItem;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceRejectReason;
import org.kuali.kfs.module.purap.document.ElectronicInvoiceRejectDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.NoteType;

import edu.cornell.kfs.coa.document.validation.impl.GlobalIndirectCostRecoveryAccountsRule;
import edu.cornell.kfs.module.purap.service.impl.CuElectronicInvoiceItemHolder;

public class CuElectronicInvoiceRejectDocument extends ElectronicInvoiceRejectDocument {
    private static final Logger LOG = LogManager.getLogger();

    // KFSPTS-1719
    private List<CuElectronicInvoiceItemHolder> nonMatchItems;

    @Override
    public void prepareForSave() {
    	sanitizeRejectReasons();
    	super.prepareForSave();
    }

    public void sanitizeRejectReasons() {

    	List<ElectronicInvoiceRejectReason> sanitized = new ArrayList<ElectronicInvoiceRejectReason>();
    	int maxLength = SpringContext.getBean(DataDictionaryService.class).getAttributeMaxLength(ElectronicInvoiceRejectReason.class, "invoiceRejectReasonDescription");
    	for (ElectronicInvoiceRejectReason rejectReason : invoiceRejectReasons) {
    		if (rejectReason.getInvoiceRejectReasonDescription().length() > maxLength) {
    			String reason = rejectReason.getInvoiceRejectReasonDescription();
    			String type = rejectReason.getInvoiceRejectReasonTypeCode();
    			String fileName = rejectReason.getInvoiceFileName();    			
    			while (reason.length() > maxLength) {
    				ElectronicInvoiceRejectReason split = new ElectronicInvoiceRejectReason(type, fileName, reason.substring(0,maxLength));
    				reason = reason.substring(maxLength);
    				sanitized.add(split);
    			}
				ElectronicInvoiceRejectReason split = new ElectronicInvoiceRejectReason(type, fileName, reason);
				sanitized.add(split);
    		}
    		else {
    			sanitized.add(rejectReason);
    		}
    	}
    	invoiceRejectReasons = sanitized;
    }
    
	public List<CuElectronicInvoiceItemHolder> getNonMatchItems() {
		return nonMatchItems;
	}

	public void setNonMatchItems(List<CuElectronicInvoiceItemHolder> nonMatchItems) {
		this.nonMatchItems = nonMatchItems;
	}

    public KualiDecimal getGrandTotalAmount() {
        KualiDecimal returnValue = new KualiDecimal(zero);
        try {
            for (ElectronicInvoiceRejectItem eiri : this.invoiceRejectItems) {
                KualiDecimal toAddAmount = new KualiDecimal(eiri.getInvoiceItemNetAmount());
                LOG.debug(
                        "getGrandTotalAmount() setting returnValue with arithmetic => '{}' + '{}'",
                        returnValue::doubleValue,
                        toAddAmount::doubleValue
                );
                returnValue = returnValue.add(toAddAmount);
            }
            LOG.debug("getGrandTotalAmount() returning amount {}", returnValue::doubleValue);

            if (this.getInvoiceItemSpecialHandlingAmount() != null && zero.compareTo(this.getInvoiceItemSpecialHandlingAmount()) != 0) {
                returnValue = returnValue.add(new KualiDecimal(this.getInvoiceItemSpecialHandlingAmount()));
            }
            if (this.getInvoiceItemShippingAmount() != null && zero.compareTo(this.getInvoiceItemShippingAmount()) != 0) {
                returnValue = returnValue.add(new KualiDecimal(this.getInvoiceItemShippingAmount()));
            }
            // KFSUPGRADE-485/kfspts-1719 : uncomment this tax amount if statement
            if (this.getInvoiceItemTaxAmount() != null && zero.compareTo(this.getInvoiceItemTaxAmount()) != 0) {
                returnValue = returnValue.add(new KualiDecimal(this.getInvoiceItemTaxAmount()));
            }
            if (this.getInvoiceItemDiscountAmount() != null && zero.compareTo(this.getInvoiceItemDiscountAmount()) != 0) {
                returnValue = returnValue.subtract(new KualiDecimal(this.getInvoiceItemDiscountAmount()));
            }
            LOG.debug("getGrandTotalAmount() returning amount {}", returnValue::doubleValue);
            return returnValue;
        }
        catch (NumberFormatException n) {
            // do nothing this is already rejected
            LOG.error(
                    "getGrandTotalAmount() Error attempting to calculate total amount for invoice with filename {}",
                    this.invoiceFileName
            );
            return new KualiDecimal(zero);
        }
    }

    public BigDecimal getInvoiceItemTaxAmount() {
        BigDecimal returnValue = zero;
        boolean enableSalesTaxInd = SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.ENABLE_SALES_TAX_IND);

        try {
            //if sales tax enabled, calculate total by totaling items
            if(enableSalesTaxInd){
                for (ElectronicInvoiceRejectItem eiri : this.invoiceRejectItems) {
                    BigDecimal toAddAmount = eiri.getInvoiceItemTaxAmount();
                    LOG.debug(
                            "getInvoiceItemTaxAmount() setting returnValue with arithmetic => '{}' + '{}'",
                            returnValue::doubleValue,
                            toAddAmount::doubleValue
                    );
                    returnValue = returnValue.add(toAddAmount);
                }
            } else { 
                //else take the total, which should be the summary tax total
                returnValue = returnValue.add(this.invoiceItemTaxAmount);
            }

            LOG.debug("getInvoiceItemTaxAmount() returning amount {}", returnValue::doubleValue);
//            return returnValue;
        }
        catch (NumberFormatException n) {
            // do nothing this is already rejected
            LOG.error(
                    "getInvoiceItemTaxAmount() Error attempting to calculate total amount for invoice with filename {}",
                    this.invoiceFileName
            );
            return zero;
        }
        // return invoiceItemTaxAmount;
        // KFSUPGRADE_487
        if(returnValue.equals(zero)) {
        	return this.invoiceItemTaxAmount;
        }
        return returnValue;

    }

    @Override
    public PersistableBusinessObject getNoteTarget() {
        return this;
    }

    @Override
    public NoteType getNoteType() {
        return NoteType.BUSINESS_OBJECT;
    }

}
