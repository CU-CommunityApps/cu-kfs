/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.fp.document.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonresidentTax;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.DisbursementVoucherTaxService;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherNonresidentInformationValidation;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.krad.exception.InfrastructureException;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// CU Customization: Overlayed this class to fix areas that need to use BigDecimal instead of KualiDecimal.
/**
 * This is the default implementation of the PaymentSourceExtractionService interface.
 * This class handles queries and validation on tax id numbers.
 */
public class DisbursementVoucherTaxServiceImpl implements DisbursementVoucherTaxService {

    private static final Logger LOG = LogManager.getLogger();

    private BusinessObjectService businessObjectService;
    private DisbursementVoucherNonresidentInformationValidation nonresidentInformationValidation;
    private ParameterService parameterService;
    private PersonService personService;

    /**
     * This method retrieves the universal id of the individual or business entity who matches the tax id number and
     * type code given.
     *
     * @param taxIDNumber      The tax identification number of the user being retrieved.
     * @param taxPayerTypeCode The tax payer type code of the user being retrieved.  See the TAX_TYPE_* constants
     *                         defined in DisbursementVoucherRuleConstants for examples of valid tax type codes.
     * @return The universal id of the individual who matches the tax id and type code given. Null if no matching
     *         user is found.
     */
    @Override
    public String getUniversalId(String taxIDNumber, String taxPayerTypeCode) {
        if (DisbursementVoucherConstants.TAX_TYPE_FEIN.equals(taxPayerTypeCode)) {
            return null;
        }

        Person person = personService.getPersonByExternalIdentifier(
                KimConstants.PersonExternalIdentifierTypes.TAX, taxIDNumber).get(0);

        String universalId = null;
        if (person != null) {
            universalId = person.getPrincipalId();
        }
        return universalId;
    }

    /**
     * This method retrieves the vendor identification code for the vendor found who has a matching tax id and tax
     * payer type code.
     *
     * @param taxIDNumber      The tax id number used to retrieve the associated vendor.
     * @param taxPayerTypeCode The tax payer type code used to retrieve the associated vendor. See the TAX_TYPE_*
     *                         constants defined in DisbursementVoucherRuleConstants for examples of valid tax type
     *                         codes.
     * @return The id of the vendor found matching the tax id and type code provided. Null if no matching vendor is
     *         found.
     */
    @Override
    public String getVendorId(String taxIDNumber, String taxPayerTypeCode) {
        String vendorId = null;

        Map<String, String> taxIDCrit = new HashMap<>();
        taxIDCrit.put("taxIdNumber", taxIDNumber);
        taxIDCrit.put("taxpayerTypeCode", taxPayerTypeCode);
        Collection<VendorDetail> foundPayees = businessObjectService.findMatching(VendorDetail.class, taxIDCrit);

        if (!foundPayees.isEmpty()) {
            VendorDetail vendor = foundPayees.iterator().next();
            vendorId = vendor.getVendorHeaderGeneratedIdentifier().toString();
        }

        return vendorId;
    }

    /**
     * This method generates nonresident tax lines for the given disbursement voucher.
     * <p>
     * The nonresident tax lines consist of three possible sets of tax lines:
     * - Gross up tax lines
     * - Federal tax lines
     * - State tax lines
     * <p>
     * Gross up tax lines are generated if the income tax gross up code is set on the
     * DisbursementVoucherNonresidentTax attribute of the disbursement voucher.
     * <p>
     * Federal tax lines are generated if the federal tax rate in the DisbursementVoucherNonresidentTax attribute
     * is other than zero.
     * <p>
     * State tax lines are generated if the state tax rate in the DisbursementVoucherNonresidentTax attribute is
     * other than zero.
     *
     * @param document The disbursement voucher the nonresident tax lines will be added to.
     */
    protected void generateNonresidentTaxLines(DisbursementVoucherDocument document) {
        // retrieve first accounting line for tax line attributes
        AccountingLine line1 = document.getSourceAccountingLine(0);

        List<Integer> taxLineNumbers = new ArrayList<>();

        // generate gross up
        if (document.getDvNonresidentTax().isIncomeTaxGrossUpCode()) {
            AccountingLine grossLine;
            try {
                grossLine = (SourceAccountingLine) document.getSourceAccountingLineClass().newInstance();
            } catch (IllegalAccessException e) {
                throw new InfrastructureException("unable to access sourceAccountingLineClass", e);
            } catch (InstantiationException e) {
                throw new InfrastructureException("unable to instantiate sourceAccountingLineClass", e);
            }

            grossLine.setDocumentNumber(document.getDocumentNumber());
            grossLine.setSequenceNumber(document.getNextSourceLineNumber());
            grossLine.setChartOfAccountsCode(line1.getChartOfAccountsCode());
            grossLine.setAccountNumber(line1.getAccountNumber());
            grossLine.setFinancialObjectCode(line1.getFinancialObjectCode());

            // calculate gross up amount and set as line amount
            BigDecimal federalTaxPercent = document.getDvNonresidentTax().getFederalIncomeTaxPercent();
            BigDecimal stateTaxPercent = document.getDvNonresidentTax().getStateIncomeTaxPercent();
            BigDecimal documentAmount = document.getDisbVchrCheckTotalAmount().bigDecimalValue();

            KualiDecimal grossAmount1 = new KualiDecimal(
                    documentAmount.multiply(federalTaxPercent).divide(new BigDecimal(100).subtract(federalTaxPercent)
                            .subtract(stateTaxPercent), 5, BigDecimal.ROUND_HALF_UP));
            KualiDecimal grossAmount2 = new KualiDecimal(
                    documentAmount.multiply(stateTaxPercent).divide(new BigDecimal(100).subtract(federalTaxPercent)
                            .subtract(stateTaxPercent), 5, BigDecimal.ROUND_HALF_UP));
            grossLine.setAmount(grossAmount1.add(grossAmount2));

            // put line number in line number list, and update next line property in document
            taxLineNumbers.add(grossLine.getSequenceNumber());
            document.setNextSourceLineNumber(document.getNextSourceLineNumber() + 1);

            // add to source accounting lines
            grossLine.refresh();
            document.getSourceAccountingLines().add(grossLine);

            // update check total, is added because line amount is negative, so this will take check amount down
            document.setDisbVchrCheckTotalAmount(document.getDisbVchrCheckTotalAmount().add(grossLine.getAmount()));
        }

        KualiDecimal taxableAmount = document.getDisbVchrCheckTotalAmount();

        // generate federal tax line
        // CU Customization: Fix comparison to use BigDecimal
        if (BigDecimal.ZERO.compareTo(document.getDvNonresidentTax().getFederalIncomeTaxPercent()) != 0) {
            String federalTaxChart = parameterService.getParameterValueAsString(DisbursementVoucherDocument.class,
                    DisbursementVoucherConstants.FEDERAL_TAX_PARM_PREFIX +
                            DisbursementVoucherConstants.TAX_PARM_CHART_SUFFIX);
            String federalTaxAccount = parameterService.getParameterValueAsString(DisbursementVoucherDocument.class,
                    DisbursementVoucherConstants.FEDERAL_TAX_PARM_PREFIX +
                            DisbursementVoucherConstants.TAX_PARM_ACCOUNT_SUFFIX);
            String federalTaxObjectCode = parameterService.getSubParameterValueAsString(DisbursementVoucherDocument.class,
                    DisbursementVoucherConstants.FEDERAL_TAX_PARM_PREFIX +
                            DisbursementVoucherConstants.TAX_PARM_OBJECT_BY_INCOME_CLASS_SUFFIX,
                    document.getDvNonresidentTax().getIncomeClassCode());
            if (StringUtils.isBlank(federalTaxChart) || StringUtils.isBlank(federalTaxAccount)
                    || StringUtils.isBlank(federalTaxObjectCode)) {
                LOG.error("Unable to retrieve federal tax parameters.");
                throw new RuntimeException("Unable to retrieve federal tax parameters.");
            }

            AccountingLine federalTaxLine = generateTaxAccountingLine(document, federalTaxChart, federalTaxAccount,
                    federalTaxObjectCode, document.getDvNonresidentTax().getFederalIncomeTaxPercent(),
                    taxableAmount);

            // put line number in line number list, and update next line property in document
            taxLineNumbers.add(federalTaxLine.getSequenceNumber());
            document.setNextSourceLineNumber(document.getNextSourceLineNumber() + 1);

            // add to source accounting lines
            federalTaxLine.refresh();
            document.getSourceAccountingLines().add(federalTaxLine);

            // update check total, is added because line amount is negative, so this will take check amount down
            document.setDisbVchrCheckTotalAmount(document.getDisbVchrCheckTotalAmount().add(federalTaxLine.getAmount()));
        }

        // generate state tax line
        // CU Customization: Fix comparison to use BigDecimal
        if (BigDecimal.ZERO.compareTo(document.getDvNonresidentTax().getStateIncomeTaxPercent()) != 0) {
            String stateTaxChart = parameterService.getParameterValueAsString(DisbursementVoucherDocument.class,
                    DisbursementVoucherConstants.STATE_TAX_PARM_PREFIX +
                            DisbursementVoucherConstants.TAX_PARM_CHART_SUFFIX);
            String stateTaxAccount = parameterService.getParameterValueAsString(DisbursementVoucherDocument.class,
                    DisbursementVoucherConstants.STATE_TAX_PARM_PREFIX +
                            DisbursementVoucherConstants.TAX_PARM_ACCOUNT_SUFFIX);
            String stateTaxObjectCode = parameterService.getSubParameterValueAsString(DisbursementVoucherDocument.class,
                    DisbursementVoucherConstants.STATE_TAX_PARM_PREFIX +
                            DisbursementVoucherConstants.TAX_PARM_OBJECT_BY_INCOME_CLASS_SUFFIX,
                    document.getDvNonresidentTax().getIncomeClassCode());

            if (StringUtils.isBlank(stateTaxChart) || StringUtils.isBlank(stateTaxAccount)
                    || StringUtils.isBlank(stateTaxObjectCode)) {
                LOG.error("Unable to retrieve state tax parameters.");
                throw new RuntimeException("Unable to retrieve state tax parameters.");
            }

            AccountingLine stateTaxLine = generateTaxAccountingLine(document, stateTaxChart, stateTaxAccount,
                    stateTaxObjectCode, document.getDvNonresidentTax().getStateIncomeTaxPercent(), taxableAmount);

            // put line number in line number list, and update next line property in document
            taxLineNumbers.add(stateTaxLine.getSequenceNumber());
            document.setNextSourceLineNumber(document.getNextSourceLineNumber() + 1);

            // add to source accounting lines
            stateTaxLine.refresh();
            document.getSourceAccountingLines().add(stateTaxLine);

            // update check total, is added because line amount is negative, so this will take check amount down
            document.setDisbVchrCheckTotalAmount(document.getDisbVchrCheckTotalAmount().add(stateTaxLine.getAmount()));
        }

        // update line number field
        document.getDvNonresidentTax().setFinancialDocumentAccountingLineText(
                StringUtils.join(taxLineNumbers.iterator(), ","));
    }

    /**
     * Generates an accounting line for the chart, account, object code & tax percentage values given.
     *
     * @param document      The disbursement voucher the tax will be applied to.
     * @param chart         The chart code to be assigned to the accounting line generated.
     * @param account       The account code to be assigned to the accounting line generated.
     * @param objectCode    The object code used on the accounting line generated.
     * @param taxPercent    The tax rate to be used to calculate the tax amount.
     * @param taxableAmount The total amount that is taxable.  This amount is used in conjunction with the tax percent
     *                      to calculate the amount for the accounting lined being generated.
     * @return A fully populated AccountingLine instance representing the amount of tax that will be applied to the
     *         disbursement voucher provided.
     */
    protected AccountingLine generateTaxAccountingLine(DisbursementVoucherDocument document, String chart,
            String account, String objectCode, BigDecimal taxPercent, KualiDecimal taxableAmount) {
        AccountingLine taxLine;
        try {
            taxLine = (SourceAccountingLine) document.getSourceAccountingLineClass().newInstance();
        } catch (IllegalAccessException e) {
            throw new InfrastructureException("unable to access sourceAccountingLineClass", e);
        } catch (InstantiationException e) {
            throw new InfrastructureException("unable to instantiate sourceAccountingLineClass", e);
        }

        taxLine.setDocumentNumber(document.getDocumentNumber());
        taxLine.setSequenceNumber(document.getNextSourceLineNumber());
        taxLine.setChartOfAccountsCode(chart);
        taxLine.setAccountNumber(account);
        taxLine.setFinancialObjectCode(objectCode);

        // calculate tax amount and set as line amount
        BigDecimal amount = taxableAmount.bigDecimalValue();
        BigDecimal taxDecimal = taxPercent.divide(new BigDecimal(100), 5, BigDecimal.ROUND_HALF_UP);
        KualiDecimal taxAmount = new KualiDecimal(amount.multiply(taxDecimal).setScale(KualiDecimal.SCALE,
                KualiDecimal.ROUND_BEHAVIOR));
        taxLine.setAmount(taxAmount.negated());

        return taxLine;
    }

    /**
     * This method validates the nonresident tax information for the document and if the information
     * validates, the nonresident tax lines are generated.
     *
     * @param document The disbursement voucher document the nonresident tax information will be validated and the
     *                 subsequent tax lines generated for.
     */
    @Override
    public void processNonresidentTax(DisbursementVoucherDocument document) {
        if (validateNonresidentTaxInformation(document)) {
            generateNonresidentTaxLines(document);
        }
    }

    /**
     * Removes nonresident check boxes and sets information to empty values.
     *
     * @param document The disbursement voucher the nonresident tax lines will be removed from.
     */
    @Override
    public void clearNonresidentTaxInfo(DisbursementVoucherDocument document) {
        document.getDvNonresidentTax().setIncomeClassCode(null);
        document.getDvNonresidentTax().setFederalIncomeTaxPercent(null);
        document.getDvNonresidentTax().setStateIncomeTaxPercent(null);
        document.getDvNonresidentTax().setPostalCountryCode(null);
        document.getDvNonresidentTax().setTaxNQIId(null);
        document.getDvNonresidentTax().setReferenceFinancialDocumentNumber(null);
        document.getDvNonresidentTax().setForeignSourceIncomeCode(false);
        document.getDvNonresidentTax().setIncomeTaxTreatyExemptCode(false);
        document.getDvNonresidentTax().setTaxOtherExemptIndicator(false);
        document.getDvNonresidentTax().setIncomeTaxGrossUpCode(false);
        document.getDvNonresidentTax().setTaxUSAIDPerDiemIndicator(false);
        document.getDvNonresidentTax().setTaxSpecialW4Amount(null);

        clearNonresidentTaxLines(document);
    }

    /**
     * Removes nonresident tax lines from the document's accounting lines and updates the check total.
     *
     * @param document The disbursement voucher the nonresident tax lines will be removed from.
     */
    @Override
    public void clearNonresidentTaxLines(DisbursementVoucherDocument document) {
        ArrayList<SourceAccountingLine> taxLines = new ArrayList<>();
        KualiDecimal taxTotal = KualiDecimal.ZERO;

        DisbursementVoucherNonresidentTax dvNonresidentTax = document.getDvNonresidentTax();
        if (dvNonresidentTax != null) {
            List<Integer> previousTaxLineNumbers = getNonresidentTaxLineNumbers(dvNonresidentTax.getFinancialDocumentAccountingLineText());

            // get tax lines out of source lines
            boolean previousGrossUp = false;
            List<SourceAccountingLine> srcLines = document.getSourceAccountingLines();
            for (SourceAccountingLine line : srcLines) {
                if (previousTaxLineNumbers.contains(line.getSequenceNumber())) {
                    taxLines.add(line);

                    // check if tax line was a positive amount, in which case we had a gross up
                    if ((KualiDecimal.ZERO).compareTo(line.getAmount()) < 0) {
                        previousGrossUp = true;
                    } else {
                        taxTotal = taxTotal.add(line.getAmount().abs());
                    }
                }
            }

            // remove tax lines
            /*
             * NOTE: a custom remove method needed to be used here because the .equals() method for
             * AccountingLineBase does not take amount into account when determining equality.
             * This lead to the issues described in KULRNE-6201.
             */
            Iterator<SourceAccountingLine> saLineIter = document.getSourceAccountingLines().iterator();
            while (saLineIter.hasNext()) {
                SourceAccountingLine saLine = saLineIter.next();
                for (SourceAccountingLine taxLine : taxLines) {
                    if (saLine.equals(taxLine)) {
                        if (saLine.getAmount().equals(taxLine.getAmount())) {
                            saLineIter.remove();
                        }
                    }
                }
            }

            // update check total if not grossed up
            if (!previousGrossUp) {
                document.setDisbVchrCheckTotalAmount(document.getDisbVchrCheckTotalAmount().add(taxTotal));
            }

            // clear line string
            dvNonresidentTax.setFinancialDocumentAccountingLineText("");
        }
    }

    /**
     * This method retrieves the nonresident tax amount using the disbursement voucher given to calculate the amount.
     * If the vendor is not a nonresident or they are and there is no gross up code set, the amount returned will be
     * zero.  If the vendor is a nonresident and gross up has been set, the amount is calculated by retrieving all
     * the source accounting lines for the disbursement voucher provided and summing the amounts of all the lines
     * that are nonresident tax lines.
     *
     * @param document The disbursement voucher the nonresident tax line amount will be calculated for.
     * @return The nonresident tax amount applicable to the given disbursement voucher or zero if the voucher does
     *         not have any nonresident tax lines.
     */
    @Override
    public KualiDecimal getNonresidentTaxAmount(DisbursementVoucherDocument document) {
        KualiDecimal taxAmount = KualiDecimal.ZERO;

        // if not nonresident payment or gross has been done, no tax amount should have been taken out
        if (!document.getDvPayeeDetail().isDisbVchrNonresidentPaymentCode()
                || (document.getDvPayeeDetail().isDisbVchrNonresidentPaymentCode()
                && document.getDvNonresidentTax().isIncomeTaxGrossUpCode())) {
            return taxAmount;
        }

        // get tax line numbers
        List taxLineNumbers = getNonresidentTaxLineNumbers(document.getDvNonresidentTax()
                .getFinancialDocumentAccountingLineText());

        for (Object acctLine : document.getSourceAccountingLines()) {
            SourceAccountingLine line = (SourceAccountingLine) acctLine;
            // check if line is nonresident tax line
            if (taxLineNumbers.contains(line.getSequenceNumber())) {
                taxAmount = taxAmount.add(line.getAmount().negated());
            }
        }

        return taxAmount;
    }

    /**
     * This method performs a series of validation checks to ensure that the disbursement voucher given contains
     * nonresident specific information and nonresident tax lines are necessary.
     * <p>
     * The following steps are taken to validate the disbursement voucher given:
     * - Set all percentages (ie. federal, state) to zero if their current value is null.
     * - Call DisbursementVoucherDocumentRule.validateNonresidentInformation to perform more in-depth validation.
     * - The vendor for the disbursement voucher given is a nonresident.
     * - No reference document exists for the assigned DisbursementVoucherNonresidentTax attribute of the voucher
     * given.
     * - There is at least one source accounting line to generate the tax line from.
     * - Both the state and federal tax percentages are greater than zero.
     * - The total check amount is not negative.
     * - The total of the accounting lines is not negative.
     * - The total check amount is equal to the total of the accounting lines.
     *
     * @param document The disbursement voucher document to validate the tax lines for.
     * @return True if the information associated with nonresident tax is correct and valid, false otherwise.
     */
    protected boolean validateNonresidentTaxInformation(DisbursementVoucherDocument document) {
        MessageMap errors = GlobalVariables.getMessageMap();

        nonresidentInformationValidation.setAccountingDocumentForValidation(document);
        nonresidentInformationValidation.setValidationType("GENERATE");

        if (!nonresidentInformationValidation.validate(null)) {
            return false;
        }

        if (GlobalVariables.getMessageMap().hasErrors()) {
            return false;
        }

        /* make sure vendor is nonresident */
        if (!document.getDvPayeeDetail().isDisbVchrNonresidentPaymentCode()) {
            errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                    FPKeyConstants.ERROR_DV_GENERATE_TAX_NOT_NONRESIDENT
            );
            return false;
        }

        /* don't generate tax if reference doc is given */
        if (StringUtils.isNotBlank(document.getDvNonresidentTax().getReferenceFinancialDocumentNumber())) {
            errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                    FPKeyConstants.ERROR_DV_GENERATE_TAX_DOC_REFERENCE);
            return false;
        }

        // check attributes needed to generate lines
        /* need at least 1 line */
        if (!(document.getSourceAccountingLines().size() >= 1)) {
            errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                    FPKeyConstants.ERROR_DV_GENERATE_TAX_NO_SOURCE);
            return false;
        }

        /* make sure both fed and state tax percents are not 0, in which case there is no need to generate lines */
        // CU Customization: Fix comparisons to use BigDecimal
        if (BigDecimal.ZERO.compareTo(document.getDvNonresidentTax().getFederalIncomeTaxPercent()) == 0
                && BigDecimal.ZERO.compareTo(document.getDvNonresidentTax().getStateIncomeTaxPercent()) == 0) {
            errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                    FPKeyConstants.ERROR_DV_GENERATE_TAX_BOTH_0);
            return false;
        }

        /* check total cannot be negative */
        if (KualiDecimal.ZERO.compareTo(document.getDisbVchrCheckTotalAmount()) == 1) {
            errors.putErrorWithoutFullErrorPath("document.disbVchrCheckTotalAmount",
                    FPKeyConstants.ERROR_NEGATIVE_OR_ZERO_CHECK_TOTAL);
            return false;
        }

        /* total accounting lines cannot be negative */
        if (KualiDecimal.ZERO.compareTo(document.getSourceTotal()) == 1) {
            errors.putErrorWithoutFullErrorPath(KFSConstants.ACCOUNTING_LINE_ERRORS,
                    FPKeyConstants.ERROR_NEGATIVE_ACCOUNTING_TOTAL);
            return false;
        }

        /* total of accounting lines must match check total */
        if (document.getDisbVchrCheckTotalAmount().compareTo(document.getSourceTotal()) != 0) {
            errors.putErrorWithoutFullErrorPath(KFSConstants.ACCOUNTING_LINE_ERRORS,
                    FPKeyConstants.ERROR_CHECK_ACCOUNTING_TOTAL);
            return false;
        }
        return true;
    }

    /**
     * Parses the tax line string given and returns a list of line numbers as Integers.
     *
     * @param taxLineString The string to be parsed.
     * @return A collection of line numbers represented as Integers.
     */
    @Override
    public List<Integer> getNonresidentTaxLineNumbers(String taxLineString) {
        List<Integer> taxLineNumbers = new ArrayList<>();
        if (StringUtils.isNotBlank(taxLineString)) {
            List<String> taxLineNumberStrings = Arrays.asList(StringUtils.split(taxLineString, ","));
            for (String lineNumber : taxLineNumberStrings) {
                taxLineNumbers.add(Integer.valueOf(lineNumber));
            }
        }

        return taxLineNumbers;
    }

    // known user: UCI
    protected BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setNonresidentInformationValidation(
            DisbursementVoucherNonresidentInformationValidation nonresidentInformationValidation
    ) {
        this.nonresidentInformationValidation = nonresidentInformationValidation;
    }

    // known user: UCI
    protected ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}

