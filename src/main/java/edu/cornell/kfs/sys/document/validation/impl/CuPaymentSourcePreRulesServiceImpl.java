package edu.cornell.kfs.sys.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.businessobject.PaymentSourceWireTransfer;
import org.kuali.kfs.sys.document.validation.impl.PaymentSourcePreRulesServiceImpl;

import edu.cornell.kfs.sys.businessobject.PaymentSourceWireTransferExtendedAttribute;

public class CuPaymentSourcePreRulesServiceImpl extends PaymentSourcePreRulesServiceImpl {
    
    
    @Override
    public boolean hasWireTransferValues(final PaymentSourceWireTransfer wireTransfer) {
        boolean hasValues = super.hasWireTransferValues(wireTransfer);
        final PaymentSourceWireTransferExtendedAttribute wireExtension =
                (PaymentSourceWireTransferExtendedAttribute) wireTransfer.getExtension();
        hasValues |= StringUtils.isNotBlank(wireExtension.getBankStreetAddress());
        hasValues |= StringUtils.isNotBlank(wireExtension.getBankProvince());
        hasValues |= StringUtils.isNotBlank(wireExtension.getBankSWIFTCode());
        hasValues |= StringUtils.isNotBlank(wireExtension.getBankIBAN());
        hasValues |= StringUtils.isNotBlank(wireExtension.getSortOrTransitCode());
        hasValues |= StringUtils.isNotBlank(wireExtension.getCorrespondentBankName());
        hasValues |= StringUtils.isNotBlank(wireExtension.getCorrespondentBankAddress());
        hasValues |= StringUtils.isNotBlank(wireExtension.getCorrespondentBankRoutingNumber());
        hasValues |= StringUtils.isNotBlank(wireExtension.getCorrespondentBankAccountNumber());
        hasValues |= StringUtils.isNotBlank(wireExtension.getCorrespondentBankSwiftCode());
        return hasValues;
    }

    @Override
    public void clearWireTransferValues(final PaymentSourceWireTransfer wireTransfer) {
        super.clearWireTransferValues(wireTransfer);
        final PaymentSourceWireTransferExtendedAttribute wireExtension =
                (PaymentSourceWireTransferExtendedAttribute) wireTransfer.getExtension();
        wireExtension.setBankStreetAddress(null);
        wireExtension.setBankProvince(null);
        wireExtension.setBankSWIFTCode(null);
        wireExtension.setBankIBAN(null);
        wireExtension.setSortOrTransitCode(null);
        wireExtension.setCorrespondentBankName(null);
        wireExtension.setCorrespondentBankAddress(null);
        wireExtension.setCorrespondentBankRoutingNumber(null);
        wireExtension.setCorrespondentBankAccountNumber(null);
        wireExtension.setCorrespondentBankSwiftCode(null);
    }
}