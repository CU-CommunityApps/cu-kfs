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
package org.kuali.kfs.sys.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.OffsetDefinition;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.BalanceTypeService;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.ObjectTypeService;
import org.kuali.kfs.coa.service.OffsetDefinitionService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.fp.businessobject.OffsetAccount;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.businessobject.Encumbrance;
import org.kuali.kfs.gl.service.SufficientFundsService;
import org.kuali.kfs.gl.service.SufficientFundsServiceConstants;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.sys.dataaccess.GeneralLedgerPendingEntryDao;
import org.kuali.kfs.sys.document.GeneralLedgerPendingEntrySource;
import org.kuali.kfs.sys.document.GeneralLedgerPostingDocument;
import org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants;
import org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.GENERAL_LEDGER_PENDING_ENTRY_CODE;
import org.kuali.kfs.sys.service.FlexibleOffsetAccountService;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.sys.service.HomeOriginationService;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is the service implementation for the GeneralLedgerPendingEntry structure. This is the default
 * implementation, that is delivered with Kuali.
 */
@Transactional
public class GeneralLedgerPendingEntryServiceImpl implements GeneralLedgerPendingEntryService {

    private static final Logger LOG = LogManager.getLogger();

    protected GeneralLedgerPendingEntryDao generalLedgerPendingEntryDao;
    protected KualiRuleService kualiRuleService;
    protected ChartService chartService;
    protected OptionsService optionsService;
    protected ParameterService parameterService;
    protected BalanceTypeService balanceTypeService;
    protected DateTimeService dateTimeService;
    protected DataDictionaryService dataDictionaryService;
    protected PersistenceStructureService persistenceStructureService;
    protected UniversityDateService universityDateService;
    protected AccountService accountService;
    protected SufficientFundsService sufficientFundsService;
    protected FlexibleOffsetAccountService flexibleOffsetAccountService;
    protected OffsetDefinitionService offsetDefinitionService;
    protected ObjectTypeService objectTypeService;
    private BusinessObjectService businessObjectService;
    private ObjectCodeService objectCodeService;
    private HomeOriginationService homeOriginationService;

    @Override
    public KualiDecimal getExpenseSummary(Integer universityFiscalYear, String chartOfAccountsCode,
            String accountNumber, String sufficientFundsObjectCode, boolean isDebit, boolean isYearEnd,
            List<String> transactionDocumentNumbers) {
        LOG.debug("getExpenseSummary() started");

        ObjectTypeService objectTypeService = getObjectTypeService();
        List<String> objectTypes = objectTypeService.getExpenseObjectTypes(universityFiscalYear);

        // FIXME! - cache this list - balance type code will not change during the lifetime of the server
        SystemOptions options = optionsService.getOptions(universityFiscalYear);

        Collection<String> balanceTypeCodes = new ArrayList<>();
        balanceTypeCodes.add(options.getActualFinancialBalanceTypeCd());

        return generalLedgerPendingEntryDao.getTransactionSummary(universityFiscalYear, chartOfAccountsCode,
                accountNumber, objectTypes, balanceTypeCodes, sufficientFundsObjectCode, isDebit, isYearEnd,
                transactionDocumentNumbers);
    }

    @Override
    public KualiDecimal getEncumbranceSummary(Integer universityFiscalYear, String chartOfAccountsCode,
            String accountNumber, String sufficientFundsObjectCode, boolean isDebit, boolean isYearEnd,
            List<String> transactionDocumentNumbers) {
        LOG.debug("getEncumbranceSummary() started");

        // FIXME! - this ObjectTypeService should be injected
        ObjectTypeService objectTypeService = getObjectTypeService();
        List<String> objectTypes = objectTypeService.getExpenseObjectTypes(universityFiscalYear);

        // FIXME! - cache this list - balance type code will not change during the lifetime of the server
        List<String> balanceTypeCodes = balanceTypeService.getEncumbranceBalanceTypes(universityFiscalYear);

        return generalLedgerPendingEntryDao.getTransactionSummary(universityFiscalYear, chartOfAccountsCode,
                accountNumber, objectTypes, balanceTypeCodes, sufficientFundsObjectCode, isDebit, isYearEnd,
                transactionDocumentNumbers);
    }

    @Override
    public KualiDecimal getBudgetSummary(Integer universityFiscalYear, String chartOfAccountsCode, String accountNumber,
            String sufficientFundsObjectCode, boolean isYearEnd) {
        LOG.debug("getBudgetSummary() started");

        ObjectTypeService objectTypeService = getObjectTypeService();
        List<String> objectTypes = objectTypeService.getExpenseObjectTypes(universityFiscalYear);

        SystemOptions options = optionsService.getOptions(universityFiscalYear);

        // FIXME! - cache this list - balance type code will not change during the lifetime of the server
        Collection<String> balanceTypeCodes = new ArrayList<>();
        balanceTypeCodes.add(options.getBudgetCheckingBalanceTypeCd());

        return generalLedgerPendingEntryDao.getTransactionSummary(universityFiscalYear, chartOfAccountsCode,
                accountNumber, objectTypes, balanceTypeCodes, sufficientFundsObjectCode, isYearEnd);
    }

    @Override
    public KualiDecimal getCashSummary(List universityFiscalYears, String chartOfAccountsCode, String accountNumber,
            boolean isDebit) {
        LOG.debug("getCashSummary() started");

        Chart c = chartService.getByPrimaryId(chartOfAccountsCode);

        // Note, we are getting the options from the first fiscal year in the list. We are assuming that the
        // balance type code for actual is the same in all the years in the list.
        SystemOptions options = optionsService.getOptions((Integer) universityFiscalYears.get(0));

        // FIXME! - cache this list - will not change during the lifetime of the server
        Collection<String> objectCodes = new ArrayList<>();
        objectCodes.add(c.getFinancialCashObjectCode());

        // FIXME! - cache this list - balance type code will not change during the lifetime of the server
        Collection<String> balanceTypeCodes = new ArrayList<>();
        balanceTypeCodes.add(options.getActualFinancialBalanceTypeCd());

        return generalLedgerPendingEntryDao.getTransactionSummary(universityFiscalYears, chartOfAccountsCode,
                accountNumber, objectCodes, balanceTypeCodes, isDebit);
    }

    @Override
    public KualiDecimal getActualSummary(List universityFiscalYears, String chartOfAccountsCode, String accountNumber,
            boolean isDebit) {
        LOG.debug("getActualSummary() started");

        List<String> codes = new ArrayList<>(parameterService
                .getParameterValuesAsString(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                        SufficientFundsServiceConstants.SUFFICIENT_FUNDS_OBJECT_CODE_SPECIALS));

        // Note, we are getting the options from the first fiscal year in the list. We are assuming that the
        // balance type code for actual is the same in all the years in the list.
        SystemOptions options = optionsService.getOptions((Integer) universityFiscalYears.get(0));

        // FIXME! - cache this list - balance type code will not change during the lifetime of the server
        Collection<String> balanceTypeCodes = new ArrayList<>();
        balanceTypeCodes.add(options.getActualFinancialBalanceTypeCd());

        return generalLedgerPendingEntryDao.getTransactionSummary(universityFiscalYears, chartOfAccountsCode,
                accountNumber, codes, balanceTypeCodes, isDebit);
    }

    @Override
    public GeneralLedgerPendingEntry getByPrimaryId(Integer transactionEntrySequenceId, String documentHeaderId) {
        LOG.debug("getByPrimaryId() started");
        Map<String, Object> keys = new HashMap<>();
        keys.put(KFSPropertyConstants.DOCUMENT_NUMBER, documentHeaderId);
        keys.put("transactionLedgerEntrySequenceNumber", transactionEntrySequenceId);
        return businessObjectService.findByPrimaryKey(GeneralLedgerPendingEntry.class, keys);
    }

    /**
     * Invokes generateEntries method on the financial document.
     *
     * @param glpeSource document whose pending entries need generated
     * @return whether the business rules succeeded
     */
    @Override
    public boolean generateGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySource glpeSource) {
        boolean success = true;

        // we must clear them first before creating new ones
        LOG.info("Clearing existing gl pending ledger entries on document " +
                glpeSource.getDocumentHeader().getDocumentNumber());
        glpeSource.clearAnyGeneralLedgerPendingEntries();

        LOG.info("deleting existing gl pending ledger entries for document " +
                glpeSource.getDocumentHeader().getDocumentNumber());
        delete(glpeSource.getDocumentHeader().getDocumentNumber());

        LOG.info("generating gl pending ledger entries for document " +
                glpeSource.getDocumentHeader().getDocumentNumber());

        GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper();
        for (GeneralLedgerPendingEntrySourceDetail glpeSourceDetail : glpeSource
                .getGeneralLedgerPendingEntrySourceDetails()) {
            success &= glpeSource.generateGeneralLedgerPendingEntries(glpeSourceDetail, sequenceHelper);
            sequenceHelper.increment();
        }

        LOG.info("generated gl pending entry source detail pending entries, now generating document gl pending " +
                "entries for document " + glpeSource.getDocumentHeader().getDocumentNumber());

        // doc specific pending entries generation
        success &= glpeSource.generateDocumentGeneralLedgerPendingEntries(sequenceHelper);

        LOG.info("gl pending entry generation complete " + glpeSource.getDocumentHeader().getDocumentNumber());

        return success;
    }

    /**
     * This populates an empty GeneralLedgerPendingEntry explicitEntry object instance with default values.
     *
     * @param glpeSource
     * @param glpeSourceDetail
     * @param sequenceHelper
     * @param explicitEntry
     */
    @Override
    public void populateExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySource glpeSource,
            GeneralLedgerPendingEntrySourceDetail glpeSourceDetail,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper, GeneralLedgerPendingEntry explicitEntry) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("populateExplicitGeneralLedgerPendingEntry(AccountingDocument, AccountingLine," +
                    "GeneralLedgerPendingEntrySequenceHelper, GeneralLedgerPendingEntry) - start");
        }

        explicitEntry.setFinancialDocumentTypeCode(glpeSource.getFinancialDocumentTypeCode());
        explicitEntry.setVersionNumber(1L);
        explicitEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
        Timestamp transactionTimestamp = new Timestamp(dateTimeService.getCurrentDate().getTime());
        explicitEntry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        explicitEntry.setTransactionEntryProcessedTs(transactionTimestamp);
        explicitEntry.setAccountNumber(glpeSourceDetail.getAccountNumber());

        Account account = getAccountService().getByPrimaryIdWithCaching(glpeSourceDetail.getChartOfAccountsCode(),
                glpeSourceDetail.getAccountNumber());
        ObjectCode objectCode = objectCodeService.getByPrimaryIdWithCaching(glpeSource.getPostingYear(),
                glpeSourceDetail.getChartOfAccountsCode(), glpeSourceDetail.getFinancialObjectCode());

        if (account != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("GLPE: Testing to see what should be used for SF Object Code: " + glpeSourceDetail);
            }
            String sufficientFundsCode = account.getAccountSufficientFundsCode();
            if (StringUtils.isBlank(sufficientFundsCode)) {
                sufficientFundsCode = KFSConstants.SF_TYPE_NO_CHECKING;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Code was blank on the account - using 'N'");
                }
            }

            if (objectCode != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("SF Code / Object: " + sufficientFundsCode + " / " + objectCode);
                }
                String sifficientFundsObjectCode = getSufficientFundsService().getSufficientFundsObjectCode(objectCode,
                        sufficientFundsCode);
                explicitEntry.setAcctSufficientFundsFinObjCd(sifficientFundsObjectCode);
            } else {
                LOG.debug("Object code object was null, skipping setting of SF object field.");
            }
        }

        if (objectCode != null) {
            explicitEntry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());
        }

        explicitEntry.setFinancialDocumentApprovedCode(KFSConstants.PENDING_ENTRY_APPROVED_STATUS_CODE.NOT_PROCESSED);
        explicitEntry.setTransactionEncumbranceUpdateCode(KFSConstants.BLANK_SPACE);
        // this is the default that most documents use
        explicitEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
        explicitEntry.setChartOfAccountsCode(glpeSourceDetail.getChartOfAccountsCode());
        explicitEntry.setTransactionDebitCreditCode(glpeSource.isDebit(
                glpeSourceDetail) ? KFSConstants.GL_DEBIT_CODE : KFSConstants.GL_CREDIT_CODE);
        explicitEntry.setFinancialSystemOriginationCode(homeOriginationService.getHomeOrigination()
                .getFinSystemHomeOriginationCode());
        explicitEntry.setDocumentNumber(glpeSourceDetail.getDocumentNumber());
        explicitEntry.setFinancialObjectCode(glpeSourceDetail.getFinancialObjectCode());

        explicitEntry.setOrganizationDocumentNumber(glpeSource.getDocumentHeader().getOrganizationDocumentNumber());
        explicitEntry.setOrganizationReferenceId(glpeSourceDetail.getOrganizationReferenceId());
        explicitEntry.setProjectCode(getEntryValue(glpeSourceDetail.getProjectCode(),
                GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankProjectCode()));
        explicitEntry.setReferenceFinancialDocumentNumber(getEntryValue(glpeSourceDetail.getReferenceNumber(),
                KFSConstants.BLANK_SPACE));
        explicitEntry.setReferenceFinancialDocumentTypeCode(getEntryValue(glpeSourceDetail.getReferenceTypeCode(),
                KFSConstants.BLANK_SPACE));
        explicitEntry.setReferenceFinancialSystemOriginationCode(getEntryValue(glpeSourceDetail.getReferenceOriginCode(),
                KFSConstants.BLANK_SPACE));
        explicitEntry.setSubAccountNumber(getEntryValue(glpeSourceDetail.getSubAccountNumber(),
                GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankSubAccountNumber()));
        explicitEntry.setFinancialSubObjectCode(getEntryValue(glpeSourceDetail.getFinancialSubObjectCode(),
                GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialSubObjectCode()));
        explicitEntry.setTransactionEntryOffsetIndicator(false);
        explicitEntry.setTransactionLedgerEntryAmount(
                glpeSource.getGeneralLedgerPendingEntryAmountForDetail(glpeSourceDetail));
        explicitEntry.setTransactionLedgerEntryDescription(
                getEntryValue(glpeSourceDetail.getFinancialDocumentLineDescription(),
                        glpeSource.getDocumentHeader().getDocumentDescription()));
        
        /*
         * CU Customization
         * Base code has the following line
         * explicitEntry.setUniversityFiscalPeriodCode(glpeSource.getPostingPeriodCode());
         * but this was a new change, and we think it causing documents to post to the wrong period
         * Updating the code to be the previous setting
         */
        // null here, is assigned during batch or in specific document rule classes
        explicitEntry.setUniversityFiscalPeriodCode(null);
        
        explicitEntry.setUniversityFiscalPeriodCode(glpeSource.getPostingPeriodCode());
        explicitEntry.setUniversityFiscalYear(glpeSource.getPostingYear());

        if (LOG.isDebugEnabled()) {
            LOG.debug("populateExplicitGeneralLedgerPendingEntry(AccountingDocument, AccountingLine, " +
                    "GeneralLedgerPendingEntrySequenceHelper, GeneralLedgerPendingEntry) - end");
        }
    }

    /**
     * This populates an GeneralLedgerPendingEntry offsetEntry object instance with values that differ from the values
     * supplied in the explicit entry that it was cloned from. Note that the entries do not contain BOs now.
     *
     * @param universityFiscalYear
     * @param explicitEntry
     * @param sequenceHelper
     * @param offsetEntry          Cloned from the explicit entry
     * @return whether the offset generation is successful
     */
    @Override
    public boolean populateOffsetGeneralLedgerPendingEntry(Integer universityFiscalYear,
            GeneralLedgerPendingEntry explicitEntry, GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
            GeneralLedgerPendingEntry offsetEntry) {
        LOG.debug("populateOffsetGeneralLedgerPendingEntry(Integer, GeneralLedgerPendingEntry," +
                " GeneralLedgerPendingEntrySequenceHelper, GeneralLedgerPendingEntry) - start");

        boolean success = true;

        // lookup offset object info
        OffsetDefinition offsetDefinition = getOffsetDefinitionService().getByPrimaryId(universityFiscalYear,
                explicitEntry.getChartOfAccountsCode(), explicitEntry.getFinancialDocumentTypeCode(),
                explicitEntry.getFinancialBalanceTypeCode());
        if (ObjectUtils.isNull(offsetDefinition)) {
            success = false;
            GlobalVariables.getMessageMap().putError(KFSConstants.GENERAL_LEDGER_PENDING_ENTRIES_TAB_ERRORS,
                    KFSKeyConstants.ERROR_DOCUMENT_NO_OFFSET_DEFINITION, universityFiscalYear.toString(),
                    explicitEntry.getChartOfAccountsCode(), explicitEntry.getFinancialDocumentTypeCode(),
                    explicitEntry.getFinancialBalanceTypeCode());
        } else {
            OffsetAccount flexibleOffsetAccount = getFlexibleOffsetAccountService()
                    .getByPrimaryIdIfEnabled(explicitEntry.getChartOfAccountsCode(), explicitEntry.getAccountNumber(),
                            getOffsetFinancialObjectCode(offsetDefinition));
            flexOffsetAccountIfNecessary(flexibleOffsetAccount, offsetEntry);
        }

        // update offset entry fields that are different from the explicit entry that it was created from
        offsetEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
        offsetEntry.setTransactionDebitCreditCode(getOffsetEntryDebitCreditCode(explicitEntry));

        String offsetObjectCode = getOffsetFinancialObjectCode(offsetDefinition);
        offsetEntry.setFinancialObjectCode(offsetObjectCode);
        if (offsetObjectCode.equals(AccountingDocumentRuleBaseConstants.GENERAL_LEDGER_PENDING_ENTRY_CODE
                .getBlankFinancialObjectCode())) {
            // no BO, so punt
            offsetEntry.setAcctSufficientFundsFinObjCd(
                    AccountingDocumentRuleBaseConstants.GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialObjectCode());
        } else {
            // Need current ObjectCode and Account BOs to get sufficient funds code. (Entries originally have no BOs.)
            // todo: private or other methods to get these BOs, instead of using the entry and leaving some BOs filled in?
            offsetEntry.refreshReferenceObject(KFSPropertyConstants.FINANCIAL_OBJECT);
            offsetEntry.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
            ObjectCode financialObject = offsetEntry.getFinancialObject();
            // The ObjectCode reference may be invalid because a flexible offset account changed its chart code.
            if (ObjectUtils.isNull(financialObject)) {
                throw new RuntimeException("offset object code " + offsetEntry.getUniversityFiscalYear() + "-" +
                        offsetEntry.getChartOfAccountsCode() + "-" + offsetEntry.getFinancialObjectCode());
            }
            Account account = getAccountService().getByPrimaryId(offsetEntry.getChartOfAccountsCode(),
                    offsetEntry.getAccountNumber());
            if (account != null) {
                offsetEntry.setAcctSufficientFundsFinObjCd(getSufficientFundsService().getSufficientFundsObjectCode(
                        financialObject, account.getAccountSufficientFundsCode()));
            }
        }

        offsetEntry.setFinancialObjectTypeCode(getOffsetFinancialObjectTypeCode(offsetDefinition));
        offsetEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        offsetEntry.setTransactionEntryOffsetIndicator(true);
        offsetEntry.setTransactionLedgerEntryDescription(KFSConstants.GL_PE_OFFSET_STRING);
        offsetEntry.setFinancialSystemOriginationCode(explicitEntry.getFinancialSystemOriginationCode());

        LOG.debug("populateOffsetGeneralLedgerPendingEntry(Integer, GeneralLedgerPendingEntry, " +
                "GeneralLedgerPendingEntrySequenceHelper, GeneralLedgerPendingEntry) - end");
        return success;
    }

    /**
     * Applies the given flexible offset account to the given offset entry. Does nothing if flexibleOffsetAccount is
     * null or its COA and account number are the same as the offset entry's.
     *
     * @param flexibleOffsetAccount may be null
     * @param offsetEntry           may be modified
     */
    protected void flexOffsetAccountIfNecessary(OffsetAccount flexibleOffsetAccount,
            GeneralLedgerPendingEntry offsetEntry) {
        LOG.debug("flexOffsetAccountIfNecessary(OffsetAccount, GeneralLedgerPendingEntry) - start");

        if (flexibleOffsetAccount == null) {
            LOG.debug("flexOffsetAccountIfNecessary(OffsetAccount, GeneralLedgerPendingEntry) - end");
            // They are not required and may also be disabled.
            return;
        }
        String flexCoa = flexibleOffsetAccount.getFinancialOffsetChartOfAccountCode();
        String flexAccountNumber = flexibleOffsetAccount.getFinancialOffsetAccountNumber();
        if (flexCoa.equals(offsetEntry.getChartOfAccountsCode())
                && flexAccountNumber.equals(offsetEntry.getAccountNumber())) {
            LOG.debug("flexOffsetAccountIfNecessary(OffsetAccount, GeneralLedgerPendingEntry) - end");
            // no change, so leave sub-account as is
            return;
        }
        if (ObjectUtils.isNull(flexibleOffsetAccount.getFinancialOffsetAccount())) {
            throw new RuntimeException("flexible offset account " + flexCoa + "-" + flexAccountNumber);
        }
        offsetEntry.setChartOfAccountsCode(flexCoa);
        offsetEntry.setAccountNumber(flexAccountNumber);
        // COA and account number are part of the sub-account's key, so the original sub-account would be invalid.
        offsetEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());

        LOG.debug("flexOffsetAccountIfNecessary(OffsetAccount, GeneralLedgerPendingEntry) - end");
    }

    /**
     * Helper method that determines the offset entry's financial object type code.
     *
     * @param offsetDefinition
     * @return String
     */
    protected String getOffsetFinancialObjectTypeCode(OffsetDefinition offsetDefinition) {
        LOG.debug("getOffsetFinancialObjectTypeCode(OffsetDefinition) - start");

        if (null != offsetDefinition && null != offsetDefinition.getFinancialObject()) {
            String returnString = getEntryValue(offsetDefinition.getFinancialObject().getFinancialObjectTypeCode(),
                    AccountingDocumentRuleBaseConstants.GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialObjectType());
            LOG.debug("getOffsetFinancialObjectTypeCode(OffsetDefinition) - end");
            return returnString;
        } else {
            LOG.debug("getOffsetFinancialObjectTypeCode(OffsetDefinition) - end");
            return AccountingDocumentRuleBaseConstants.GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialObjectType();
        }
    }

    /**
     * Helper method that determines the debit/credit code for the offset entry. If the explicit was a debit, the
     * offset is a credit. Otherwise, it's opposite.
     *
     * @param explicitEntry
     * @return String
     */
    protected String getOffsetEntryDebitCreditCode(GeneralLedgerPendingEntry explicitEntry) {
        LOG.debug("getOffsetEntryDebitCreditCode(GeneralLedgerPendingEntry) - start");

        String offsetDebitCreditCode = KFSConstants.GL_BUDGET_CODE;
        if (KFSConstants.GL_DEBIT_CODE.equals(explicitEntry.getTransactionDebitCreditCode())) {
            offsetDebitCreditCode = KFSConstants.GL_CREDIT_CODE;
        } else if (KFSConstants.GL_CREDIT_CODE.equals(explicitEntry.getTransactionDebitCreditCode())) {
            offsetDebitCreditCode = KFSConstants.GL_DEBIT_CODE;
        }

        LOG.debug("getOffsetEntryDebitCreditCode(GeneralLedgerPendingEntry) - end");
        return offsetDebitCreditCode;
    }

    /**
     * Helper method that determines the offset entry's financial object code.
     *
     * @param offsetDefinition
     * @return String
     */
    protected String getOffsetFinancialObjectCode(OffsetDefinition offsetDefinition) {
        LOG.debug("getOffsetFinancialObjectCode(OffsetDefinition) - start");

        if (null != offsetDefinition) {
            String returnString = getEntryValue(offsetDefinition.getFinancialObjectCode(),
                    AccountingDocumentRuleBaseConstants.GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialObjectCode());
            LOG.debug("getOffsetFinancialObjectCode(OffsetDefinition) - end");
            return returnString;
        } else {
            LOG.debug("getOffsetFinancialObjectCode(OffsetDefinition) - end");
            return AccountingDocumentRuleBaseConstants.GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialObjectCode();
        }

    }

    /**
     * This populates an empty GeneralLedgerPendingEntry instance with default values for a bank offset. A global
     * error will be posted as a side-effect if the given bank has not defined the necessary bank offset relations.
     *
     * @param bank
     * @param depositAmount
     * @param financialDocument
     * @param universityFiscalYear
     * @param sequenceHelper
     * @param bankOffsetEntry
     * @param errorPropertyName
     * @return whether the entry was populated successfully
     */
    @Override
    public boolean populateBankOffsetGeneralLedgerPendingEntry(Bank bank, KualiDecimal depositAmount,
            GeneralLedgerPostingDocument financialDocument, Integer universityFiscalYear,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper, GeneralLedgerPendingEntry bankOffsetEntry,
            String errorPropertyName) {
        bankOffsetEntry.setFinancialDocumentTypeCode(dataDictionaryService.getDocumentTypeNameByClass(
                financialDocument.getClass()));
        bankOffsetEntry.setVersionNumber(1L);
        bankOffsetEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
        Timestamp transactionTimestamp = new Timestamp(dateTimeService.getCurrentDate().getTime());
        bankOffsetEntry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        bankOffsetEntry.setTransactionEntryProcessedTs(transactionTimestamp);
        Account cashOffsetAccount = bank.getCashOffsetAccount();

        if (ObjectUtils.isNull(cashOffsetAccount)) {
            GlobalVariables.getMessageMap().putError(errorPropertyName,
                    KFSKeyConstants.ERROR_DOCUMENT_BANK_OFFSET_NO_ACCOUNT, bank.getBankCode());
            return false;
        }

        if (!cashOffsetAccount.isActive()) {
            GlobalVariables.getMessageMap().putError(errorPropertyName,
                    KFSKeyConstants.ERROR_DOCUMENT_BANK_OFFSET_ACCOUNT_CLOSED, bank.getBankCode(),
                    cashOffsetAccount.getChartOfAccountsCode(), cashOffsetAccount.getAccountNumber());
            return false;
        }

        if (cashOffsetAccount.isExpired()) {
            GlobalVariables.getMessageMap().putError(errorPropertyName,
                    KFSKeyConstants.ERROR_DOCUMENT_BANK_OFFSET_ACCOUNT_EXPIRED, bank.getBankCode(),
                    cashOffsetAccount.getChartOfAccountsCode(), cashOffsetAccount.getAccountNumber());
            return false;
        }

        bankOffsetEntry.setChartOfAccountsCode(bank.getCashOffsetFinancialChartOfAccountCode());
        bankOffsetEntry.setAccountNumber(bank.getCashOffsetAccountNumber());
        bankOffsetEntry.setFinancialDocumentApprovedCode(
                AccountingDocumentRuleBaseConstants.GENERAL_LEDGER_PENDING_ENTRY_CODE.NO);
        bankOffsetEntry.setTransactionEncumbranceUpdateCode(KFSConstants.BLANK_SPACE);
        bankOffsetEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
        bankOffsetEntry.setTransactionDebitCreditCode(
                depositAmount.isPositive() ? KFSConstants.GL_DEBIT_CODE : KFSConstants.GL_CREDIT_CODE);
        bankOffsetEntry.setFinancialSystemOriginationCode(homeOriginationService.getHomeOrigination()
                .getFinSystemHomeOriginationCode());
        bankOffsetEntry.setDocumentNumber(financialDocument.getDocumentNumber());

        ObjectCode cashOffsetObject = bank.getCashOffsetObject();
        if (ObjectUtils.isNull(cashOffsetObject)) {
            GlobalVariables.getMessageMap().putError(errorPropertyName,
                    KFSKeyConstants.ERROR_DOCUMENT_BANK_OFFSET_NO_OBJECT_CODE, bank.getBankCode());
            return false;
        }

        if (!cashOffsetObject.isFinancialObjectActiveCode()) {
            GlobalVariables.getMessageMap().putError(errorPropertyName,
                    KFSKeyConstants.ERROR_DOCUMENT_BANK_OFFSET_INACTIVE_OBJECT_CODE, bank.getBankCode(),
                    cashOffsetObject.getFinancialObjectCode());
            return false;
        }

        bankOffsetEntry.setFinancialObjectCode(bank.getCashOffsetObjectCode());
        bankOffsetEntry.setFinancialObjectTypeCode(bank.getCashOffsetObject().getFinancialObjectTypeCode());
        bankOffsetEntry.setOrganizationDocumentNumber(financialDocument.getDocumentHeader().getOrganizationDocumentNumber());
        bankOffsetEntry.setOrganizationReferenceId(null);
        bankOffsetEntry.setProjectCode(KFSConstants.getDashProjectCode());
        bankOffsetEntry.setReferenceFinancialDocumentNumber(null);
        bankOffsetEntry.setReferenceFinancialDocumentTypeCode(null);
        bankOffsetEntry.setReferenceFinancialSystemOriginationCode(null);

        if (StringUtils.isBlank(bank.getCashOffsetSubAccountNumber())) {
            bankOffsetEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
        } else {
            SubAccount cashOffsetSubAccount = bank.getCashOffsetSubAccount();
            if (ObjectUtils.isNull(cashOffsetSubAccount)) {
                GlobalVariables.getMessageMap().putError(errorPropertyName,
                        KFSKeyConstants.ERROR_DOCUMENT_BANK_OFFSET_NONEXISTENT_SUB_ACCOUNT, bank.getBankCode(),
                        cashOffsetAccount.getChartOfAccountsCode(), cashOffsetAccount.getAccountNumber(),
                        bank.getCashOffsetSubAccountNumber());
                return false;
            }

            if (!cashOffsetSubAccount.isActive()) {
                GlobalVariables.getMessageMap().putError(errorPropertyName,
                        KFSKeyConstants.ERROR_DOCUMENT_BANK_OFFSET_INACTIVE_SUB_ACCOUNT, bank.getBankCode(),
                        cashOffsetAccount.getChartOfAccountsCode(), cashOffsetAccount.getAccountNumber(),
                        bank.getCashOffsetSubAccountNumber());
                return false;
            }

            bankOffsetEntry.setSubAccountNumber(bank.getCashOffsetSubAccountNumber());
        }

        if (StringUtils.isBlank(bank.getCashOffsetSubObjectCode())) {
            bankOffsetEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        } else {
            SubObjectCode cashOffsetSubObject = bank.getCashOffsetSubObject();
            if (ObjectUtils.isNull(cashOffsetSubObject)) {
                GlobalVariables.getMessageMap().putError(errorPropertyName,
                        KFSKeyConstants.ERROR_DOCUMENT_BANK_OFFSET_NONEXISTENT_SUB_OBJ, bank.getBankCode(),
                        cashOffsetAccount.getChartOfAccountsCode(), cashOffsetAccount.getAccountNumber(),
                        cashOffsetObject.getFinancialObjectCode(), bank.getCashOffsetSubObjectCode());
                return false;
            }

            if (!cashOffsetSubObject.isActive()) {
                GlobalVariables.getMessageMap().putError(errorPropertyName,
                        KFSKeyConstants.ERROR_DOCUMENT_BANK_OFFSET_INACTIVE_SUB_OBJ, bank.getBankCode(),
                        cashOffsetAccount.getChartOfAccountsCode(), cashOffsetAccount.getAccountNumber(),
                        cashOffsetObject.getFinancialObjectCode(), bank.getCashOffsetSubObjectCode());
                return false;
            }

            bankOffsetEntry.setFinancialSubObjectCode(bank.getCashOffsetSubObjectCode());
        }

        bankOffsetEntry.setTransactionEntryOffsetIndicator(true);
        bankOffsetEntry.setTransactionLedgerEntryAmount(depositAmount.abs());
        bankOffsetEntry.setUniversityFiscalPeriodCode(financialDocument.getPostingPeriodCode());
        bankOffsetEntry.setUniversityFiscalYear(universityFiscalYear);
        bankOffsetEntry.setAcctSufficientFundsFinObjCd(getSufficientFundsService()
                .getSufficientFundsObjectCode(cashOffsetObject, cashOffsetAccount.getAccountSufficientFundsCode()));

        return true;
    }

    @Override
    public void save(GeneralLedgerPendingEntry generalLedgerPendingEntry) {
        LOG.debug("save() started");
        businessObjectService.save(generalLedgerPendingEntry);
    }

    @Override
    public void delete(String documentHeaderId) {
        LOG.debug("delete() started");

        this.generalLedgerPendingEntryDao.delete(documentHeaderId);
    }

    @Override
    public void deleteByFinancialDocumentApprovedCode(String financialDocumentApprovedCode) {
        LOG.debug("deleteByFinancialDocumentApprovedCode() started");

        this.generalLedgerPendingEntryDao.deleteByFinancialDocumentApprovedCode(financialDocumentApprovedCode);
    }

    @Override
    public Iterator findApprovedPendingLedgerEntries() {
        LOG.debug("findApprovedPendingLedgerEntries() started");

        return generalLedgerPendingEntryDao.findApprovedPendingLedgerEntries();
    }

    @Override
    public Iterator findPendingLedgerEntries(Encumbrance encumbrance, boolean isApproved) {
        LOG.debug("findPendingLedgerEntries() started");

        return generalLedgerPendingEntryDao.findPendingLedgerEntries(encumbrance, isApproved);
    }

    @Override
    public Iterator findPendingLedgerEntries(Balance balance, boolean isApproved, boolean isConsolidated) {
        LOG.debug("findPendingLedgerEntries() started");

        return generalLedgerPendingEntryDao.findPendingLedgerEntries(balance, isApproved, isConsolidated);
    }

    @Override
    public boolean hasPendingGeneralLedgerEntry(Account account) {
        LOG.debug("hasPendingGeneralLedgerEntry() started");

        return generalLedgerPendingEntryDao.countPendingLedgerEntries(account) > 0;
    }

    @Override
    public Iterator findPendingLedgerEntriesForEntry(Map fieldValues, boolean isApproved) {
        LOG.debug("findPendingLedgerEntriesForEntry() started");

        UniversityDate currentUniversityDate = universityDateService.getCurrentUniversityDate();
        String currentFiscalPeriodCode = currentUniversityDate.getUniversityFiscalAccountingPeriod();
        Integer currentFiscalYear = currentUniversityDate.getUniversityFiscalYear();
        List<String> encumbranceBalanceTypes = getEncumbranceBalanceTypes(fieldValues, currentFiscalYear);

        return generalLedgerPendingEntryDao.findPendingLedgerEntriesForEntry(fieldValues, isApproved,
                currentFiscalPeriodCode, currentFiscalYear, encumbranceBalanceTypes);
    }

    @Override
    public Iterator findPendingLedgerEntriesForEncumbrance(Map fieldValues, boolean isApproved) {
        LOG.debug("findPendingLedgerEntriesForEncumbrance() started");

        UniversityDate currentUniversityDate = universityDateService.getCurrentUniversityDate();
        String currentFiscalPeriodCode = currentUniversityDate.getUniversityFiscalAccountingPeriod();
        Integer currentFiscalYear = currentUniversityDate.getUniversityFiscalYear();
        SystemOptions currentYearOptions = optionsService.getCurrentYearOptions();
        List<String> encumbranceBalanceTypes = getEncumbranceBalanceTypes(fieldValues, currentFiscalYear);

        return generalLedgerPendingEntryDao.findPendingLedgerEntriesForEncumbrance(fieldValues, isApproved,
                currentFiscalPeriodCode, currentFiscalYear, currentYearOptions, encumbranceBalanceTypes);
    }

    @Override
    public Iterator findPendingLedgerEntriesForCashBalance(Map fieldValues, boolean isApproved) {
        LOG.debug("findPendingLedgerEntriesForCashBalance() started");

        UniversityDate currentUniversityDate = universityDateService.getCurrentUniversityDate();
        String currentFiscalPeriodCode = currentUniversityDate.getUniversityFiscalAccountingPeriod();
        Integer currentFiscalYear = currentUniversityDate.getUniversityFiscalYear();
        List<String> encumbranceBalanceTypes = getEncumbranceBalanceTypes(fieldValues, currentFiscalYear);

        return generalLedgerPendingEntryDao.findPendingLedgerEntriesForCashBalance(fieldValues, isApproved,
                currentFiscalPeriodCode, currentFiscalYear, encumbranceBalanceTypes);
    }

    @Override
    public Iterator findPendingLedgerEntriesForBalance(Map fieldValues, boolean isApproved) {
        LOG.debug("findPendingLedgerEntriesForBalance() started");

        UniversityDate currentUniversityDate = universityDateService.getCurrentUniversityDate();
        String currentFiscalPeriodCode = currentUniversityDate.getUniversityFiscalAccountingPeriod();
        Integer currentFiscalYear = currentUniversityDate.getUniversityFiscalYear();
        List<String> encumbranceBalanceTypes = getEncumbranceBalanceTypes(fieldValues, currentFiscalYear);

        return generalLedgerPendingEntryDao.findPendingLedgerEntriesForBalance(fieldValues, isApproved,
                currentFiscalPeriodCode, currentFiscalYear, encumbranceBalanceTypes);
    }

    @Override
    public Iterator findPendingLedgerEntriesForAccountBalance(Map fieldValues, boolean isApproved) {
        LOG.debug("findPendingLedgerEntriesForAccountBalance() started");

        UniversityDate currentUniversityDate = universityDateService.getCurrentUniversityDate();
        String currentFiscalPeriodCode = currentUniversityDate.getUniversityFiscalAccountingPeriod();
        Integer currentFiscalYear = currentUniversityDate.getUniversityFiscalYear();
        List<String> encumbranceBalanceTypes = getEncumbranceBalanceTypes(fieldValues, currentFiscalYear);

        return generalLedgerPendingEntryDao.findPendingLedgerEntriesForAccountBalance(fieldValues, isApproved,
                currentFiscalPeriodCode, currentFiscalYear, encumbranceBalanceTypes);
    }

    @Override
    public Iterator findPendingLedgerEntrySummaryForAccountBalance(Map fieldValues, boolean isApproved) {
        LOG.debug("findPendingLedgerEntrySummaryForAccountBalance() started");

        UniversityDate currentUniversityDate = universityDateService.getCurrentUniversityDate();
        String currentFiscalPeriodCode = currentUniversityDate.getUniversityFiscalAccountingPeriod();
        Integer currentFiscalYear = currentUniversityDate.getUniversityFiscalYear();
        List<String> encumbranceBalanceTypes = getEncumbranceBalanceTypes(fieldValues, currentFiscalYear);

        return generalLedgerPendingEntryDao.findPendingLedgerEntrySummaryForAccountBalance(fieldValues, isApproved,
                currentFiscalPeriodCode, currentFiscalYear, encumbranceBalanceTypes);
    }

    @Override
    public Collection findPendingEntries(Map fieldValues, boolean isApproved) {
        LOG.debug("findPendingEntries() started");

        UniversityDate currentUniversityDate = universityDateService.getCurrentUniversityDate();
        String currentFiscalPeriodCode = currentUniversityDate.getUniversityFiscalAccountingPeriod();
        Integer currentFiscalYear = currentUniversityDate.getUniversityFiscalYear();
        List<String> encumbranceBalanceTypes = getEncumbranceBalanceTypes(fieldValues, currentFiscalYear);

        return generalLedgerPendingEntryDao.findPendingEntries(fieldValues, isApproved, currentFiscalPeriodCode,
                currentFiscalYear, encumbranceBalanceTypes);
    }

    /**
     * A helper method that checks the intended target value for null and empty strings. If the intended target value
     * is not null or an empty string, it returns that value, ohterwise, it returns a backup value.
     *
     * @param targetValue
     * @param backupValue
     * @return String
     */
    protected final String getEntryValue(String targetValue, String backupValue) {
        LOG.debug("getEntryValue(String, String) - start");

        if (StringUtils.isNotBlank(targetValue)) {
            LOG.debug("getEntryValue(String, String) - end");
            return targetValue;
        } else {
            LOG.debug("getEntryValue(String, String) - end");
            return backupValue;
        }
    }

    /**
     * Determines if the given GeneralLedgerPendingEntry represents offsets to cash
     *
     * @param generalLedgerPendingEntry the GeneralLedgerPendingEntry to check
     * @return true if the GeneralLedgerPendingEntry represents an offset to cash; false otherwise
     */
    @Override
    public boolean isOffsetToCash(GeneralLedgerPendingEntry generalLedgerPendingEntry) {
        if (generalLedgerPendingEntry.isTransactionEntryOffsetIndicator()) {
            final Chart entryChart = chartService.getByPrimaryId(generalLedgerPendingEntry.getChartOfAccountsCode());
            if (!ObjectUtils.isNull(entryChart)) {
                return entryChart.getFinancialCashObjectCode().equals(generalLedgerPendingEntry.getFinancialObjectCode());
            }
        }
        return false;
    }

    /**
     * Adds up the amounts of all cash to offset GeneralLedgerPendingEntry records on the given AccountingDocument
     *
     * @param glPostingDocument the accounting document total the offset to cash amount for
     * @return the offset to cash amount, where debited values have been subtracted and credited values have been added
     */
    @Override
    public KualiDecimal getOffsetToCashAmount(GeneralLedgerPostingDocument glPostingDocument) {
        KualiDecimal total = KualiDecimal.ZERO;
        for (GeneralLedgerPendingEntry glpe : glPostingDocument.getGeneralLedgerPendingEntries()) {
            if (isOffsetToCash(glpe)) {
                if (glpe.getTransactionDebitCreditCode().equals(KFSConstants.GL_DEBIT_CODE)) {
                    total = total.subtract(glpe.getTransactionLedgerEntryAmount());
                } else if (glpe.getTransactionDebitCreditCode().equals(KFSConstants.GL_CREDIT_CODE)) {
                    total = total.add(glpe.getTransactionLedgerEntryAmount());
                }
            }
        }
        return total;
    }

    @Override
    public List<String> getEncumbranceBalanceTypes(Map fieldValues, Integer currentFiscalYear) {
        String fiscalYearFromForm = null;
        if (fieldValues.containsKey(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR)) {
            fiscalYearFromForm = (String) fieldValues.get(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR);
        }
        boolean includeNullFiscalYearInLookup = null != currentFiscalYear
                && currentFiscalYear.toString().equals(fiscalYearFromForm);

        // handle encumbrance balance type
        Map<String, Object> localFieldValues = new HashMap<>(fieldValues);

        if (includeNullFiscalYearInLookup) {
            localFieldValues.remove(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR);
        }

        // parse the fiscal year (it's not a required field on the lookup screens
        String universityFiscalYearStr = (String) localFieldValues.get(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR);
        if (StringUtils.isNotBlank(universityFiscalYearStr)) {
            Integer universityFiscalYear = new Integer(universityFiscalYearStr);
            return balanceTypeService.getEncumbranceBalanceTypes(universityFiscalYear);
        } else {
            return balanceTypeService.getCurrentYearEncumbranceBalanceTypes();
        }
    }

    public void setBalanceTypeService(BalanceTypeService balanceTypeService) {
        this.balanceTypeService = balanceTypeService;
    }

    public void setChartService(ChartService chartService) {
        this.chartService = chartService;
    }

    public void setGeneralLedgerPendingEntryDao(GeneralLedgerPendingEntryDao generalLedgerPendingEntryDao) {
        this.generalLedgerPendingEntryDao = generalLedgerPendingEntryDao;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setKualiRuleService(KualiRuleService kualiRuleService) {
        this.kualiRuleService = kualiRuleService;
    }

    public void setOptionsService(OptionsService optionsService) {
        this.optionsService = optionsService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public PersistenceStructureService getPersistenceStructureService() {
        return persistenceStructureService;
    }

    public void setPersistenceStructureService(PersistenceStructureService persistenceStructureService) {
        this.persistenceStructureService = persistenceStructureService;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public SufficientFundsService getSufficientFundsService() {
        return sufficientFundsService;
    }

    public void setSufficientFundsService(SufficientFundsService sufficientFundsService) {
        this.sufficientFundsService = sufficientFundsService;
    }

    public FlexibleOffsetAccountService getFlexibleOffsetAccountService() {
        return flexibleOffsetAccountService;
    }

    public void setFlexibleOffsetAccountService(FlexibleOffsetAccountService flexibleOffsetAccountService) {
        this.flexibleOffsetAccountService = flexibleOffsetAccountService;
    }

    public OffsetDefinitionService getOffsetDefinitionService() {
        return offsetDefinitionService;
    }

    public void setOffsetDefinitionService(OffsetDefinitionService offsetDefinitionService) {
        this.offsetDefinitionService = offsetDefinitionService;
    }

    public ObjectTypeService getObjectTypeService() {
        return objectTypeService;
    }

    public void setObjectTypeService(ObjectTypeService objectTypeService) {
        this.objectTypeService = objectTypeService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setHomeOriginationService(HomeOriginationService homeOriginationService) {
        this.homeOriginationService = homeOriginationService;
    }

    public void setObjectCodeService(ObjectCodeService objectCodeService) {
        this.objectCodeService = objectCodeService;
    }
}
