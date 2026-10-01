package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiForeignTaxIdType;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierTaxIdBo;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;

public class CemiSupplierTaxIdBoFactory {

    private VendorHeader vendorHeader;
    private boolean foreign;
    private boolean maskSensitiveData;
    private ISOFIPSConversionService isoFipsConversionService;

    public CemiSupplierTaxIdBoFactory(final VendorHeader vendorHeader, final boolean foreign,
            final boolean maskSensitiveData, final ISOFIPSConversionService isoFipsConversionService) {
        Validate.notNull(vendorHeader, "vendorHeader cannot be null");
        Validate.notNull(isoFipsConversionService, "isoFipsConversionService cannot be null");
        this.vendorHeader = vendorHeader;
        this.foreign = foreign;
        this.maskSensitiveData = maskSensitiveData;
        this.isoFipsConversionService = isoFipsConversionService;
    }

    public static CemiSupplierTaxIdBo createTaxIdBoFrom(final VendorHeader vendorHeader, final boolean foreign,
            final boolean maskSensitiveData, final ISOFIPSConversionService isoFipsConversionService) {
        final CemiSupplierTaxIdBoFactory factory = new CemiSupplierTaxIdBoFactory(
                vendorHeader, foreign, maskSensitiveData, isoFipsConversionService);
        return factory.createCemiSupplierTaxIdBo();
    }

    public CemiSupplierTaxIdBo createCemiSupplierTaxIdBo() {
        final CemiSupplierTaxIdBo taxIdBo = new CemiSupplierTaxIdBo();

        final String taxIdText = determineTaxIdText();
        final String taxIdType = determineTaxIdType(taxIdText);

        taxIdBo.setTaxIdType(taxIdType);
        taxIdBo.setTaxIdText(taxIdText);
        taxIdBo.setTransactionTaxId(determineTransactionTaxId(taxIdText, taxIdType));
        taxIdBo.setPrimaryTaxId(determinePrimaryTaxId(taxIdText));
        taxIdBo.setCountryTaxId(CemiBaseConstants.EMPTY_STRING);

        return taxIdBo;
    }

    private String determineTaxIdText() {
        final String unmaskedTaxId = determineUnmaskedTaxIdText();
        if (StringUtils.isBlank(unmaskedTaxId)) {
            return CemiBaseConstants.EMPTY_STRING;
        } else {
            return maskSensitiveData ? CemiSupplierConstants.DUMMY_TAX_ID : unmaskedTaxId;
        }
    }

    private String determineUnmaskedTaxIdText() {
        return foreign ? vendorHeader.getVendorForeignTaxId() : vendorHeader.getVendorTaxNumber();
    }

    private String determineTaxIdType(final String taxIdText) {
        if (StringUtils.isBlank(taxIdText)) {
            return CemiBaseConstants.EMPTY_STRING;
        } else {
            if (foreign) {
                String fipsCountry = vendorHeader.getVendorCorpCitizenCode();
                if (StringUtils.isNotBlank(fipsCountry)) {
                    final String isoCountryCode = isoFipsConversionService.convertFIPSCountryCodeToActiveISOCountryCode(
                            vendorHeader.getVendorCorpCitizenCode());
                    final String foreignTaxType = CemiForeignTaxIdType.fromIsoCode(isoCountryCode)
                            .map(CemiForeignTaxIdType::getTaxIdType)
                            .orElse(CemiBaseConstants.EMPTY_STRING);
                    return foreignTaxType;
                } else {
                    return CemiBaseConstants.EMPTY_STRING;
                }
            }
            final String kfsTaxType = StringUtils.defaultString(vendorHeader.getVendorTaxTypeCode());
            return CemiSupplierConstants.TAX_ID_TYPES.get(kfsTaxType);
        }
    }

    // default to true if tax id is present, FALSE if tax type USA_SSN
    private String determineTransactionTaxId(final String taxIdText, final String taxIdType) {
        if (StringUtils.isNotBlank(taxIdText)) {
            boolean transactionTaxId = StringUtils.isNotBlank(taxIdType)
                    && !CemiSupplierConstants.USA_SSN_TAX_TYPE.equalsIgnoreCase(taxIdType);
            return CemiUtils.convertToBooleanValueForFileExtract(transactionTaxId);
        } else {
            return CemiBaseConstants.EMPTY_STRING;
        }
    }

    private String determinePrimaryTaxId(final String taxIdText) {
        if (StringUtils.isBlank(taxIdText)) {
            return CemiBaseConstants.EMPTY_STRING;
        }

        final boolean isVendorForeign = vendorHeader.getVendorForeignIndicator();
        final boolean hasDomesticTaxId = StringUtils.isNotBlank(vendorHeader.getVendorTaxNumber());
        final boolean hasForeignTaxId = StringUtils.isNotBlank(vendorHeader.getVendorForeignTaxId());
        final boolean isPrimary;

        if (foreign) {
            isPrimary = isVendorForeign || !hasDomesticTaxId;
        } else {
            isPrimary = !isVendorForeign || !hasForeignTaxId;
        }

        return CemiUtils.convertToBooleanValueForFileExtract(isPrimary);
    }

}
