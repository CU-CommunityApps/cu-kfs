/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.module.ld.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.COAParameterConstants;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.LaborPropertyConstants;
import org.kuali.kfs.module.ld.businessobject.BenefitsCalculation;
import org.kuali.kfs.module.ld.businessobject.LaborObject;
import org.kuali.kfs.module.ld.businessobject.PositionObjectBenefit;
import org.kuali.kfs.module.ld.service.LaborBenefitsCalculationService;
import org.kuali.kfs.module.ld.service.LaborPositionObjectBenefitService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * To provide its clients with access to the benefit calculation.
 */
@Transactional
public class LaborBenefitsCalculationServiceImpl implements LaborBenefitsCalculationService {

    private static final Logger LOG = LogManager.getLogger();

    private BusinessObjectService businessObjectService;
    private LaborPositionObjectBenefitService laborPositionObjectBenefitService;
    private AccountService accountService;
    private ParameterService parameterService;
    private String costSharingSourceAccountNumber;
    private String costSharingSourceSubAccountNumber;
    private String costSharingSourceAccountChartOfAccountsCode;
    
    @Override
    public BenefitsCalculation getBenefitsCalculation(final PositionObjectBenefit positionObjectBenefit) {
        return getBenefitsCalculation(
                positionObjectBenefit.getUniversityFiscalYear(),
                positionObjectBenefit.getChartOfAccountsCode(),
                positionObjectBenefit.getFinancialObjectBenefitsTypeCode(),
                positionObjectBenefit.getLaborBenefitRateCategoryCode()
        );
    }


    @Override
    public BenefitsCalculation getBenefitsCalculation(
            final Integer universityFiscalYear, final String chartOfAccountsCode,
            final String benefitTypeCode) {
        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, universityFiscalYear);
        fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        fieldValues.put(LaborPropertyConstants.POSITION_BENEFIT_TYPE_CODE, benefitTypeCode);
        return businessObjectService.findByPrimaryKey(BenefitsCalculation.class, fieldValues);
    }

    @Override
    public BenefitsCalculation getBenefitsCalculation(
            final Integer universityFiscalYear, final String chartOfAccountsCode,
            final String benefitTypeCode, final String laborBenefitRateCategoryCode) {
        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, universityFiscalYear);
        fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        fieldValues.put(LaborPropertyConstants.POSITION_BENEFIT_TYPE_CODE, benefitTypeCode);
        fieldValues.put(LaborPropertyConstants.LABOR_BENEFIT_RATE_CATEGORY_CODE, laborBenefitRateCategoryCode);
        return businessObjectService.findByPrimaryKey(BenefitsCalculation.class, fieldValues);
    }

    @Override
    public KualiDecimal calculateFringeBenefit(
            final Integer fiscalYear, final String chartCode, final String objectCode,
            final KualiDecimal salaryAmount, final String accountNumber, final String subAccountNumber) {
        LaborObject laborObject = new LaborObject();

        laborObject.setUniversityFiscalYear(fiscalYear);
        laborObject.setChartOfAccountsCode(chartCode);
        laborObject.setFinancialObjectCode(objectCode);

        laborObject = (LaborObject) businessObjectService.retrieve(laborObject);

        return calculateFringeBenefit(laborObject, salaryAmount, accountNumber, subAccountNumber);
    }

    @Override
    public KualiDecimal calculateFringeBenefit(
            final LaborObject laborObject, final KualiDecimal salaryAmount,
            final String accountNumber, final String subAccountNumber) {
        KualiDecimal fringeBenefit = KualiDecimal.ZERO;

        if (salaryAmount == null || salaryAmount.isZero() || ObjectUtils.isNull(laborObject)) {
            return fringeBenefit;
        }

        final String FringeOrSalaryCode = laborObject.getFinancialObjectFringeOrSalaryCode();
        if (!LaborConstants.SalaryExpenseTransfer.LABOR_LEDGER_SALARY_CODE.equals(FringeOrSalaryCode)) {
            return fringeBenefit;
        }

        final Integer fiscalYear = laborObject.getUniversityFiscalYear();
        final String chartOfAccountsCode = laborObject.getChartOfAccountsCode();
        final String objectCode = laborObject.getFinancialObjectCode();

        final Collection<PositionObjectBenefit> positionObjectBenefits = laborPositionObjectBenefitService
                .getActivePositionObjectBenefits(fiscalYear, chartOfAccountsCode, objectCode);
        for (final PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
            final KualiDecimal benefitAmount = calculateFringeBenefit(positionObjectBenefit, salaryAmount,
                    accountNumber, subAccountNumber);
            fringeBenefit = fringeBenefit.add(benefitAmount);
        }

        return fringeBenefit;
    }

    @Override
    public KualiDecimal calculateFringeBenefit(
            final PositionObjectBenefit positionObjectBenefit, final KualiDecimal salaryAmount,
            final String accountNumber, final String subAccountNumber) {
        if (salaryAmount == null || salaryAmount.isZero() || ObjectUtils.isNull(positionObjectBenefit)) {
            return KualiDecimal.ZERO;
        }

        KualiDecimal fringeBenefitAmount = new KualiDecimal(0);

        //create a  map for the search criteria to lookup the fringe benefit percentage
        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, positionObjectBenefit.getUniversityFiscalYear());
        fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, positionObjectBenefit.getChartOfAccountsCode());
        fieldValues.put(LaborPropertyConstants.POSITION_BENEFIT_TYPE_CODE,
                positionObjectBenefit.getFinancialObjectBenefitsTypeCode());

        final Boolean enableFringeBenefitCalculationByBenefitRate = parameterService.getParameterValueAsBoolean(
                KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                LaborConstants.BenefitCalculation.ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY_PARAMETER);

        //If system parameter is evaluated to use calculation by benefit rate category
        if (enableFringeBenefitCalculationByBenefitRate) {
            //get the benefit rate based off of the university fiscal year, chart of account code, labor benefit type
            // code and labor benefit rate category code
            final String laborBenefitRateCategoryCode = getBenefitRateCategoryCode(
                    positionObjectBenefit.getChartOfAccountsCode(), accountNumber, subAccountNumber);

            //add the Labor Benefit Rate Category Code to the search criteria
            fieldValues.put("laborBenefitRateCategoryCode", laborBenefitRateCategoryCode);

            final String search = fieldValues.get(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR) + "," +
                                  fieldValues.get(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE) + "," +
                                  fieldValues.get(LaborPropertyConstants.POSITION_BENEFIT_TYPE_CODE) + "," +
                                  fieldValues.get("laborBenefitRateCategoryCode");
            LOG.info("Searching for Benefits Calculation {{}}", search);
            //perform the lookup based off the map
            final BenefitsCalculation bc = businessObjectService.findByPrimaryKey(BenefitsCalculation.class, fieldValues);

            //make sure the benefits calculation isn't null and is active
            if (bc != null && bc.isActive()) {
                LOG.info("Found a Benefit Calculation for {{}}", search);
                //lookup from the db the fringe benefit percentage from the list that is return.  ***Should only
                // return one value from the database.
                final BigDecimal fringeBenefitPercent = bc.getPositionFringeBenefitPercent();

                LOG.debug("fringeBenefitPercent: {}", fringeBenefitPercent);

                // calculate the benefit amount (ledger amt * (benefit pct/100) )
                fringeBenefitAmount = new KualiDecimal(fringeBenefitPercent.multiply(salaryAmount.bigDecimalValue())
                        .divide(KFSConstants.ONE_HUNDRED.bigDecimalValue()));
            } else {
                LOG.info("Did not locate a Benefits Calculation for {{}}.", search);
                //set the benefit amount to 0
                fringeBenefitAmount = new KualiDecimal(0);
            }
        } else {
            // calculate the benefit amount (ledger amt * (benefit pct/100) )
            final BenefitsCalculation benefitsCalculation = getBenefitsCalculation(positionObjectBenefit);
            if (ObjectUtils.isNotNull(benefitsCalculation) && benefitsCalculation.isActive()) {
                final BigDecimal fringeBenefitPercent = benefitsCalculation.getPositionFringeBenefitPercent();
                fringeBenefitAmount = new KualiDecimal(fringeBenefitPercent.multiply(salaryAmount.bigDecimalValue())
                        .divide(KFSConstants.ONE_HUNDRED.bigDecimalValue()));
            }
        }

        LOG.debug("fringeBenefitAmount: {}", fringeBenefitAmount);
        return fringeBenefitAmount;
    }

    @Override
    public String getBenefitRateCategoryCode(
            final String chartOfAccountsCode, String accountNumber,
            final String subAccountNumber) {
        setCostSharingSourceAccountNumber(null);
        setCostSharingSourceAccountChartOfAccountsCode(null);
        setCostSharingSourceSubAccountNumber(null);
        //make sure the sub account number is filled in
        if (subAccountNumber != null) {
            LOG.info("Sub Account Number was filled in. Checking to see if it is a Cost Sharing Sub Account.");

            //make sure the system parameter exists
            if (parameterService.parameterExists(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                    "USE_COST_SHARE_SOURCE_ACCOUNT_BENEFIT_RATE_IND")) {
                //parameter exists, determine the value of the parameter
                final String sysParam2 = parameterService.getParameterValueAsString(
                        KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                        "USE_COST_SHARE_SOURCE_ACCOUNT_BENEFIT_RATE_IND");
                LOG.debug("sysParam2: {}", sysParam2);

                //if sysParam2 == Y then check to see if it's a cost sharing sub account
                if ("Y".equalsIgnoreCase(sysParam2)) {
                    //lookup the A21SubAccount to get the cost sharing source account
                    final Map<String, Object> subFieldValues = new HashMap<>();
                    subFieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
                    subFieldValues.put(KFSPropertyConstants.SUB_ACCOUNT_NUMBER, subAccountNumber);
                    subFieldValues.put(KFSPropertyConstants.SUB_ACCOUNT_TYPE_CODE, "CS");
                    LOG.info("Looking for a cost sharing sub account for sub account number {}", subAccountNumber);

                    //perform the lookup
                    final List<A21SubAccount> subAccountList = (List<A21SubAccount>) businessObjectService
                            .findMatching(A21SubAccount.class, subFieldValues);
                    //check to see if the lookup returns an empty list
                    if (subAccountList.size() > 0) {
                        LOG.info("Found A21 Sub Account. Retrieving source account number for cost sharing.");
                        accountNumber = subAccountList.get(0).getCostShareSourceAccountNumber();
                        LOG.debug("Cost Sharing Source Account Number : {}", accountNumber);
                        setCostSharingSourceAccountNumber(accountNumber);
                        setCostSharingSourceAccountChartOfAccountsCode(subAccountList.get(0)
                                .getCostShareChartOfAccountCode());
                        setCostSharingSourceSubAccountNumber(subAccountList.get(0)
                                .getCostShareSourceSubAccountNumber());
                    } else {
                        LOG.info(
                                "{} is not a cost sharing account.  Using the Labor Benefit Rate Category from the "
                                + "account number.",
                                subAccountNumber
                        );
                    }
                } else {
                    LOG.info("Using the Grant Account to determine the labor benefit rate category code.");

                }
            }
        }

        LOG.info("Looking up Account {{},{}}", chartOfAccountsCode, accountNumber);
        //lookup the account from the db based off the account code and the account number
        final Account account = accountService.getByPrimaryId(chartOfAccountsCode, accountNumber);

        String laborBenefitRateCategoryCode = null;
        if (account == null) {
            LOG.info("The Account {{},{}} could not be found.", chartOfAccountsCode, accountNumber);
        } else {
            laborBenefitRateCategoryCode = account.getLaborBenefitRateCategoryCode();
        }

        //make sure the laborBenefitRateCategoryCode is not null or blank
        if (StringUtils.isBlank(laborBenefitRateCategoryCode)) {
            LOG.info("The Account did not have a Labor Benefit Rate Category Code. Will use the system parameter " +
                    "default.");
            //The system parameter does not exist. Using a blank Labor Benefit Rate Category Code
            laborBenefitRateCategoryCode = StringUtils.defaultString(parameterService
                    .getParameterValueAsString(Account.class, COAParameterConstants.BENEFIT_RATE));
        } else {
            LOG.debug(
                    "Labor Benefit Rate Category Code for Account {} is {}",
                    accountNumber,
                    laborBenefitRateCategoryCode
            );
        }

        return laborBenefitRateCategoryCode;
    }

    public void setLaborPositionObjectBenefitService(final LaborPositionObjectBenefitService laborPositionObjectBenefitService) {
        this.laborPositionObjectBenefitService = laborPositionObjectBenefitService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    // known user: Cornell
    protected BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setAccountService(final AccountService accountService) {
        this.accountService = accountService;
    }

    // known user: Cornell
    protected AccountService getAccountService() {
        return accountService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    // known user: Cornell
    public ParameterService getParameterService() {
        return parameterService;
    }

    @Override
    public String getCostSharingSourceAccountNumber() {
        return costSharingSourceAccountNumber;
    }

    public void setCostSharingSourceAccountNumber(final String costSharingSourceAccountNumber) {
        this.costSharingSourceAccountNumber = costSharingSourceAccountNumber;
    }

    @Override
    public String getCostSharingSourceSubAccountNumber() {
        return costSharingSourceSubAccountNumber;
    }

    public void setCostSharingSourceSubAccountNumber(final String costSharingSourceSubAccountNumber) {
        this.costSharingSourceSubAccountNumber = costSharingSourceSubAccountNumber;
    }

    @Override
    public String getCostSharingSourceAccountChartOfAccountsCode() {
        return costSharingSourceAccountChartOfAccountsCode;
    }

    public void setCostSharingSourceAccountChartOfAccountsCode(final String costSharingSourceAccountChartOfAccountsCode) {
        this.costSharingSourceAccountChartOfAccountsCode = costSharingSourceAccountChartOfAccountsCode;
    }
}
