package edu.cornell.kfs.tax.batch.service.impl;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.CUTaxKeyConstants;
import edu.cornell.kfs.tax.CUTaxConstants.TaxCommonParameterNames;
import edu.cornell.kfs.tax.batch.TaxStatisticsHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailProcessorDao;
import edu.cornell.kfs.tax.batch.dto.TaxPayeeBase;
import edu.cornell.kfs.tax.batch.dto.VendorAddressLite;
import edu.cornell.kfs.tax.batch.dto.VendorDetailLite;
import edu.cornell.kfs.tax.batch.dto.VendorQueryResults;
import edu.cornell.kfs.tax.batch.service.TaxPayeeHelperService;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;
import edu.cornell.kfs.tax.service.TaxParameterService;

public class TaxPayeeHelperServiceImpl implements TaxPayeeHelperService {

    private TransactionDetailProcessorDao transactionDetailProcessorDao;
    private ConfigurationService configurationService;
    private TaxParameterService taxParameterService;

    @Override
    public <T extends TaxPayeeBase> T createTaxPayeeWithPopulatedVendorData(final Supplier<T> payeeConstructor,
            final TransactionDetail transactionDetail, final TaxStatisticsHandler statistics) throws SQLException {
        final String taxType = determineTaxType(transactionDetail);
        final T payee = payeeConstructor.get();
        initializePayeeIdInformation(payee, transactionDetail.getPayeeId());
        payee.setVendorTaxNumber(transactionDetail.getVendorTaxNumber());
        initializeVendorData(payee, statistics, taxType);
        return payee;
    }

    private String determineTaxType(final TransactionDetail transactionDetail) {
        if (StringUtils.isNotBlank(transactionDetail.getForm1099Box())) {
            throw new IllegalStateException("This service currently does not support 1099 processing");
        } else if (StringUtils.isNotBlank(transactionDetail.getForm1042SBox())) {
            return CUTaxConstants.TAX_TYPE_1042S;
        } else {
            throw new IllegalStateException("Transaction Detail was not configured for 1099 or 1042-S");
        }
    }

    private void initializePayeeIdInformation(final TaxPayeeBase payee, final String payeeId) {
        final String vendorHeaderId = StringUtils.substringBefore(payeeId, KFSConstants.DASH);
        final String vendorDetailId = StringUtils.substringAfter(payeeId, KFSConstants.DASH);
        payee.setPayeeId(payeeId);
        payee.setVendorHeaderId(Integer.valueOf(vendorHeaderId));
        payee.setVendorDetailId(Integer.valueOf(vendorDetailId));
    }

    private void initializeVendorData(final TaxPayeeBase payee, final TaxStatisticsHandler statistics,
            final String taxType) throws SQLException {
        final VendorQueryResults vendorResults = transactionDetailProcessorDao.getVendor(
                payee.getVendorHeaderId(), payee.getVendorDetailId());
        final VendorDetailLite matchingVendor = vendorResults.getVendor();
        final VendorDetailLite vendorToProcess;

        if (ObjectUtils.isNull(matchingVendor)) {
            statistics.increment(TaxStatType.NUM_NO_VENDOR);
            payee.setVendorName(createMessage(CUTaxKeyConstants.MESSAGE_TAX_OUTPUT_VENDOR_NOT_FOUND,
                    payee.getVendorHeaderId(), payee.getVendorDetailId()));
            vendorToProcess = null;
        } else if (matchingVendor.isVendorParentIndicator()) {
            vendorToProcess = matchingVendor;
        } else if (ObjectUtils.isNull(vendorResults.getParentVendor())) {
            initializeAlternateVendorNameIfSoleProprietor(payee, matchingVendor, taxType);
            statistics.increment(TaxStatType.NUM_NO_PARENT_VENDOR);
            payee.setVendorName(createMessage(
                    CUTaxKeyConstants.MESSAGE_TAX_OUTPUT_VENDOR_PARENT_NOT_FOUND,
                    payee.getVendorHeaderId(), payee.getVendorDetailId()));
            vendorToProcess = null;
        } else {
            initializeAlternateVendorNameIfSoleProprietor(payee, matchingVendor, taxType);
            vendorToProcess = vendorResults.getParentVendor();
        }

        if (ObjectUtils.isNotNull(vendorToProcess)) {
            payee.setVendorTypeCode(vendorToProcess.getVendorTypeCode());
            payee.setVendorOwnershipCode(vendorToProcess.getVendorOwnershipCode());
            payee.setVendorGIIN(vendorToProcess.getVendorGIIN());
            payee.setVendorChapter4StatusCode(vendorToProcess.getVendorChapter4StatusCode());
            initializeVendorName(payee, vendorToProcess, statistics);
            initializeVendorAddressData(payee, vendorToProcess, statistics);
        } else if (ObjectUtils.isNotNull(matchingVendor)) {
            initializeVendorAddressData(payee, matchingVendor, statistics);
        }
    }

    /*
     * TODO: We need to revisit the parent-vendor-name setup and determine whether
     * it should be renamed to something else.
     */
    private void initializeAlternateVendorNameIfSoleProprietor(final TaxPayeeBase payee,
            final VendorDetailLite vendorDetail, final String taxType) {
        final String soleProprietorOwnershipCode = getSuffixedParameter(
                TaxCommonParameterNames.SOLE_PROPRIETOR_OWNER_CODE_PARAMETER_SUFFIX, taxType);
        if (StringUtils.equals(vendorDetail.getVendorOwnershipCode(), soleProprietorOwnershipCode)) {
            payee.setParentVendorName(vendorDetail.getVendorName());
        }
    }

    private void initializeVendorName(final TaxPayeeBase payee,
            final VendorDetailLite vendorDetail, final TaxStatisticsHandler statistics) {
        final String vendorName = vendorDetail.getVendorName();
        payee.setVendorName(vendorName);
        if (StringUtils.isNotBlank(vendorName)) {
            statistics.increment(TaxStatType.NUM_VENDOR_NAMES_PARSED);
            if (vendorDetail.isVendorFirstLastNameIndicator()
                    && StringUtils.contains(vendorName, KFSConstants.COMMA)) {
                payee.setVendorLastName(StringUtils.trim(
                        StringUtils.substringBefore(vendorName, KFSConstants.COMMA)));
                payee.setVendorFirstName(StringUtils.trim(
                        StringUtils.substringAfter(vendorName, KFSConstants.COMMA)));
            } else {
                payee.setVendorLastName(StringUtils.trim(vendorName));
            }
        } else {
            statistics.increment(TaxStatType.NUM_VENDOR_NAMES_NOT_PARSED);
        }
    }

    private void initializeVendorAddressData(final TaxPayeeBase payee,
            final VendorDetailLite vendor, final TaxStatisticsHandler statistics) throws SQLException {
        final VendorAddressLite usAddress = transactionDetailProcessorDao.getHighestPriorityUSVendorAddress(
                vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());
        final VendorAddressLite foreignAddress = transactionDetailProcessorDao.getHighestPriorityForeignVendorAddress(
                vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());

        if (ObjectUtils.isNotNull(usAddress)) {
            payee.setVendorLine1Address(usAddress.getVendorLine1Address());
            payee.setVendorLine2Address(usAddress.getVendorLine2Address());
            payee.setVendorCityName(usAddress.getVendorCityName());
            payee.setVendorStateCode(usAddress.getVendorStateCode());
            payee.setVendorZipCode(usAddress.getVendorZipCode());
            if (ObjectUtils.isNull(foreignAddress)) {
                payee.setVendorEmailAddress(usAddress.getVendorAddressEmailAddress());
            }
        } else {
            payee.setVendorLine1Address(CUTaxConstants.NO_US_VENDOR_ADDRESS);
            statistics.increment(TaxStatType.NUM_NO_VENDOR_ADDRESS_US);
        }

        if (ObjectUtils.isNotNull(foreignAddress)) {
            payee.setVendorForeignLine1Address(foreignAddress.getVendorLine1Address());
            payee.setVendorForeignLine2Address(foreignAddress.getVendorLine2Address());
            payee.setVendorForeignCityName(foreignAddress.getVendorCityName());
            payee.setVendorForeignProvinceName(foreignAddress.getVendorAddressInternationalProvinceName());
            payee.setVendorForeignZipCode(foreignAddress.getVendorZipCode());
            payee.setVendorForeignCountryCode(foreignAddress.getVendorCountryCode());
            payee.setVendorEmailAddress(foreignAddress.getVendorAddressEmailAddress());
        } else {
            payee.setVendorForeignLine1Address(CUTaxConstants.NO_FOREIGN_VENDOR_ADDRESS);
            statistics.increment(TaxStatType.NUM_NO_VENDOR_ADDRESS_FOREIGN);
        }
    }

    private String getSuffixedParameter(final String parameterNameSuffix, final String taxType) {
        final String componentCode = StringUtils.equals(taxType, CUTaxConstants.TAX_TYPE_1099)
                ? CUTaxConstants.TAX_1099_PARM_DETAIL : CUTaxConstants.TAX_1042S_PARM_DETAIL;
        return taxParameterService.getParameterValueAsString(componentCode, taxType + parameterNameSuffix);
    }

    private String createMessage(final String key, final Object... arguments) {
        final String messagePattern = configurationService.getPropertyValueAsString(key);
        return MessageFormat.format(messagePattern, arguments);
    }

    public void setTransactionDetailProcessorDao(final TransactionDetailProcessorDao transactionDetailProcessorDao) {
        this.transactionDetailProcessorDao = transactionDetailProcessorDao;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setTaxParameterService(final TaxParameterService taxParameterService) {
        this.taxParameterService = taxParameterService;
    }

}
