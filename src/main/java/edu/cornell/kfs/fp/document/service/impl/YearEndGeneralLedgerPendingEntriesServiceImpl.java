package edu.cornell.kfs.fp.document.service.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.ObjectTypeService;
import org.kuali.kfs.coa.service.OffsetDefinitionService;
import org.kuali.kfs.coa.service.SubFundGroupService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.batch.BalanceForwardStep;
import org.kuali.kfs.gl.batch.NominalActivityClosingStep;
import org.kuali.kfs.gl.service.SufficientFundsService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants;
import org.kuali.kfs.sys.service.HomeOriginationService;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.document.service.YearEndGeneralLedgerPendingEntriesService;

public class YearEndGeneralLedgerPendingEntriesServiceImpl implements YearEndGeneralLedgerPendingEntriesService{
	 protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(YearEndGeneralLedgerPendingEntriesServiceImpl.class);
	 
	 protected ParameterService parameterService;
	 protected ObjectTypeService objectTypeService;
	 protected OptionsService optionsService;
	 protected HomeOriginationService homeOriginationService;
	 protected ConfigurationService configurationService;
	 protected DateTimeService dateTimeService;
	 protected OffsetDefinitionService offsetDefinitionService;

	@Override
	public boolean generateYearEndGeneralLedgerPendingEntries(AccountingDocumentBase document, String documentTypeCode, List<AccountingLine> accountingLines, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, Collection<String> closingCharts, Date reversalDate) {
		Integer fiscalYear = new Integer(getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_FISCAL_YEAR_PARM));
		List<String> nominalActivityObjectTypeCodes = objectTypeService.getNominalActivityClosingAllowedObjectTypes(fiscalYear);
		List<String> balanceForwardObjectTypeCodes = objectTypeService.getGeneralForwardBalanceObjectTypes(fiscalYear);
		List<String> expenseObjectCodeTypes = objectTypeService.getExpenseObjectTypes(fiscalYear);
		List<String> incomeObjectCodeTypes = objectTypeService.getBasicIncomeObjectTypes(fiscalYear);

		Map<String, AccountingLine> incomeAccounts = new HashMap<String, AccountingLine>();
		Map<String, AccountingLine> expenseAccounts = new HashMap<String, AccountingLine>();
		List<AccountingLine> bbAccounts = new ArrayList<AccountingLine>();
		Map<String, AccountingLine> accountCredit = new HashMap<String, AccountingLine>();
		Map<String, AccountingLine> accountDebit = new HashMap<String, AccountingLine>();

		for (AccountingLine accountingLine : accountingLines) {
			if (ObjectUtils.isNull(closingCharts) || closingCharts.isEmpty()) {

				// do nothing
			}

			else {
				if (closingCharts.contains(accountingLine.getChartOfAccountsCode())) {
					// BB
					if (balanceForwardObjectTypeCodes.contains(accountingLine.getFinancialObjectCode()) && accountingLine.getAmount().isNonZero()) {
						bbAccounts.add(accountingLine);
					}

					// get the expense accounts
					if (expenseObjectCodeTypes.contains(accountingLine.getObjectCode().getFinancialObjectTypeCode()) && nominalActivityObjectTypeCodes.contains(accountingLine.getObjectCode().getFinancialObjectTypeCode())) {
						String accountKey = accountingLine.getChartOfAccountsCode() + accountingLine.getAccountNumber() + accountingLine.getSubAccountNumber() + accountingLine.getFinancialSubObjectCode() + accountingLine.getProjectCode() + accountingLine.getOrganizationReferenceId();
						if (expenseAccounts.containsKey(accountKey)) {
							KualiDecimal expense = expenseAccounts.get(accountKey).getAmount();
							KualiDecimal amount = accountingLine.getAmount();
							//Expense will be positive with Debit (D) flag. If the flag is C it will be treated as a negative.
							if(KFSConstants.GL_CREDIT_CODE.equalsIgnoreCase(accountingLine.getDebitCreditCode())){
								amount = amount.negated();
							}
							expenseAccounts.get(accountKey).setAmount(expense.add(amount));
							expenseAccounts.put(accountKey, expenseAccounts.get(accountKey));
						} else {
							KualiDecimal expense = accountingLine.getAmount();
							//Expense will be positive with Debit (D) flag. If the flag is C it will be treated as a negative.
							if(KFSConstants.GL_CREDIT_CODE.equalsIgnoreCase(accountingLine.getDebitCreditCode())){
								expense = expense.negated();
							}
							SourceAccountingLine expenseAcctLine = new SourceAccountingLine();
							expenseAcctLine.setChartOfAccountsCode(accountingLine.getChartOfAccountsCode());
							expenseAcctLine.setAccountNumber(accountingLine.getAccountNumber());
							expenseAcctLine.setSubAccountNumber(accountingLine.getSubAccountNumber());
							expenseAcctLine.setFinancialSubObjectCode(accountingLine.getFinancialSubObjectCode());
							expenseAcctLine.setProjectCode(accountingLine.getProjectCode());
							expenseAcctLine.setOrganizationReferenceId(accountingLine.getOrganizationReferenceId());
							expenseAcctLine.setAmount(expense);
							expenseAccounts.put(accountKey, expenseAcctLine);
						}

					} else if (incomeObjectCodeTypes.contains(accountingLine.getObjectCode().getFinancialObjectTypeCode()) && nominalActivityObjectTypeCodes.contains(accountingLine.getObjectCode().getFinancialObjectTypeCode())) {
						// get income
						String accountKey = accountingLine.getChartOfAccountsCode() + accountingLine.getAccountNumber() + accountingLine.getSubAccountNumber() + accountingLine.getFinancialSubObjectCode() + accountingLine.getProjectCode() + accountingLine.getOrganizationReferenceId();
						if (incomeAccounts.containsKey(accountKey)) {
							KualiDecimal income = incomeAccounts.get(accountKey).getAmount();
							KualiDecimal amount = accountingLine.getAmount();
							//Income will be positive with Credit (C) flag. If the flag is D it will be treated as a negative.
							if(KFSConstants.GL_DEBIT_CODE.equalsIgnoreCase(accountingLine.getDebitCreditCode())){
								amount = amount.negated();
							}
							incomeAccounts.get(accountKey).setAmount(income.add(amount));
							incomeAccounts.put(accountKey, incomeAccounts.get(accountKey));
						} else {
							KualiDecimal income = accountingLine.getAmount();
							//Income will be positive with Credit (C) flag. If the flag is D it will be treated as a negative.
							if(KFSConstants.GL_DEBIT_CODE.equalsIgnoreCase(accountingLine.getDebitCreditCode())){
								income = income.negated();
							}
							SourceAccountingLine incomeAcctLine = new SourceAccountingLine();
							incomeAcctLine.setChartOfAccountsCode(accountingLine.getChartOfAccountsCode());
							incomeAcctLine.setAccountNumber(accountingLine.getAccountNumber());
							incomeAcctLine.setSubAccountNumber(accountingLine.getSubAccountNumber());
							incomeAcctLine.setFinancialSubObjectCode(accountingLine.getFinancialSubObjectCode());
							incomeAcctLine.setProjectCode(accountingLine.getProjectCode());
							incomeAcctLine.setOrganizationReferenceId(accountingLine.getOrganizationReferenceId());
							incomeAcctLine.setAmount(income);
							incomeAccounts.put(accountKey, incomeAcctLine);
						}

					}

					// credit
					if (KFSConstants.GL_CREDIT_CODE.equalsIgnoreCase(accountingLine.getDebitCreditCode())) {
						String accountKey = accountingLine.getChartOfAccountsCode() + accountingLine.getAccountNumber() + accountingLine.getSubAccountNumber() + accountingLine.getFinancialSubObjectCode() + accountingLine.getProjectCode() + accountingLine.getOrganizationReferenceId();
						if (accountCredit.containsKey(accountKey)) {
							KualiDecimal credit = accountCredit.get(accountKey).getAmount();
							KualiDecimal amount = accountingLine.getAmount();
							accountCredit.get(accountKey).setAmount(credit.add(amount));
							accountCredit.put(accountKey, accountCredit.get(accountKey));
						} else {
							KualiDecimal credit = accountingLine.getAmount();
							SourceAccountingLine creditAcctLine = new SourceAccountingLine();
							creditAcctLine.setChartOfAccountsCode(accountingLine.getChartOfAccountsCode());
							creditAcctLine.setAccountNumber(accountingLine.getAccountNumber());
							creditAcctLine.setSubAccountNumber(accountingLine.getSubAccountNumber());
							creditAcctLine.setFinancialSubObjectCode(accountingLine.getFinancialSubObjectCode());
							creditAcctLine.setProjectCode(accountingLine.getProjectCode());
							creditAcctLine.setOrganizationReferenceId(accountingLine.getOrganizationReferenceId());
							creditAcctLine.setAmount(credit);
							accountCredit.put(accountKey, creditAcctLine);
						}

					}

					// debit
					if (KFSConstants.GL_DEBIT_CODE.equalsIgnoreCase(accountingLine.getDebitCreditCode())) {
						String accountKey = accountingLine.getChartOfAccountsCode() + accountingLine.getAccountNumber() + accountingLine.getSubAccountNumber() + accountingLine.getFinancialSubObjectCode() + accountingLine.getProjectCode() + accountingLine.getOrganizationReferenceId();
						if (accountDebit.containsKey(accountKey)) {
							KualiDecimal debit = accountDebit.get(accountKey).getAmount();
							KualiDecimal amount = accountingLine.getAmount();
							accountDebit.get(accountKey).setAmount(debit.add(amount));
							accountDebit.put(accountKey, accountDebit.get(accountKey));
						} else {
							KualiDecimal debit = accountingLine.getAmount();
							SourceAccountingLine debitAcctLine = new SourceAccountingLine();
							debitAcctLine.setChartOfAccountsCode(accountingLine.getChartOfAccountsCode());
							debitAcctLine.setAccountNumber(accountingLine.getAccountNumber());
							debitAcctLine.setSubAccountNumber(accountingLine.getSubAccountNumber());
							debitAcctLine.setFinancialSubObjectCode(accountingLine.getFinancialSubObjectCode());
							debitAcctLine.setProjectCode(accountingLine.getProjectCode());
							debitAcctLine.setOrganizationReferenceId(accountingLine.getOrganizationReferenceId());
							debitAcctLine.setAmount(debit);
							accountDebit.put(accountKey, debitAcctLine);
						}

					}
				}
			}

			// create gl entries
			// create NB 4490 income entry and 3190 offset

			List<GeneralLedgerPendingEntry> entries = createPendingEntries(document, accountingLine, sequenceHelper, closingCharts, fiscalYear, reversalDate);
			for (GeneralLedgerPendingEntry entry : entries) {
				document.addPendingEntry(entry);
			}

		}

		Set<String> incomeExpenseSet = new HashSet<String>();
		incomeExpenseSet.addAll(incomeAccounts.keySet());
		incomeExpenseSet.addAll(expenseAccounts.keySet());

		// create BB income - expense 3190 entry
		for (String accountKey : incomeExpenseSet) {
			KualiDecimal income = KualiDecimal.ZERO;
			KualiDecimal expense = KualiDecimal.ZERO;

			AccountingLine acctLine = ObjectUtils.isNull(incomeAccounts.get(accountKey)) ? expenseAccounts.get(accountKey) : incomeAccounts.get(accountKey);

			if (ObjectUtils.isNotNull(incomeAccounts.get(accountKey))) {
				income = incomeAccounts.get(accountKey).getAmount();
			}
			if (ObjectUtils.isNotNull(expenseAccounts.get(accountKey))) {
				expense = expenseAccounts.get(accountKey).getAmount();
			}
			
			KualiDecimal amount = income.subtract(expense);
			acctLine.setAmount(amount);
			if (ObjectUtils.isNotNull(amount) && amount.isNonZero()) {
				document.addPendingEntry(generateBBIncomeLessExpenseOffset(document, acctLine, sequenceHelper.getSequenceCounter(), fiscalYear, reversalDate));
				sequenceHelper.increment();
			}
		}

		Set<String> creditDebitSet = new HashSet<String>();
		creditDebitSet.addAll(accountCredit.keySet());
		creditDebitSet.addAll(accountDebit.keySet());
		
		// create BB AC entry for AS, LI,
		// create 1000 cash offset entry for remaining cash = credit - debit
		for (String accountKey : creditDebitSet) {
			KualiDecimal credit = KualiDecimal.ZERO;
			KualiDecimal debit = KualiDecimal.ZERO;

			AccountingLine acctLine = ObjectUtils.isNull(accountCredit.get(accountKey)) ? accountDebit.get(accountKey) : accountCredit.get(accountKey);

			if (ObjectUtils.isNotNull(accountCredit.get(accountKey))) {
				credit = accountCredit.get(accountKey).getAmount();
			}

			if (ObjectUtils.isNotNull(accountDebit.get(accountKey))) {
				debit = accountDebit.get(accountKey).getAmount();
			}

			KualiDecimal amount = credit.subtract(debit);
			acctLine.setAmount(amount);

			if (ObjectUtils.isNotNull(amount) && amount.isNonZero()) {
				document.addPendingEntry(generateBBCashOffset(document, acctLine, sequenceHelper.getSequenceCounter(), documentTypeCode, fiscalYear, reversalDate));
				sequenceHelper.increment();
			}

		}

		return true;
		
	}
	
	/**
	 * Generates the nominal close, nominal close offset and general forward entries for the given input accounting line
	 * 
	 * @param document
	 * @param postable
	 * @param sequenceHelper
	 * @param closingCharts
	 * @param fiscalYear
	 * @return the generated glpes
	 */
	protected List<GeneralLedgerPendingEntry> createPendingEntries(AccountingDocumentBase document, AccountingLine postable, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, Collection<String> closingCharts, Integer fiscalYear, Date reversalDate) {
		List<GeneralLedgerPendingEntry> glpes = new ArrayList<GeneralLedgerPendingEntry>();

		// generate NB

		if (ObjectUtils.isNull(closingCharts) || closingCharts.isEmpty()) {
			// do nothing

		} else {
			if (closingCharts.contains(postable.getChartOfAccountsCode())) {
				// generate List of nominal activity object type codes
				List<String> nominalActivityObjectTypeCodes = objectTypeService.getNominalActivityClosingAllowedObjectTypes(fiscalYear);

				if (nominalActivityObjectTypeCodes.contains(postable.getObjectCode().getFinancialObjectTypeCode()) && postable.getAmount().isNonZero()) {
					// create nominal activity entry

					GeneralLedgerPendingEntry nominalActivityEntry;

						nominalActivityEntry = generateNominalCloseEntry(document, postable, sequenceHelper.getSequenceCounter(), fiscalYear, reversalDate);
						glpes.add(nominalActivityEntry);
						sequenceHelper.increment();


					// create offset 3190
						GeneralLedgerPendingEntry nominalActivityOffset = generateNominalCloseOffset(document, postable, sequenceHelper.getSequenceCounter(), fiscalYear, reversalDate);
						glpes.add(nominalActivityOffset);
						sequenceHelper.increment();
				}
			}
		}
		
        // do the general forwards
     
        if (ObjectUtils.isNull(closingCharts) || closingCharts.isEmpty()) {
            //do nothing
        }
        if (closingCharts.contains(postable.getChartOfAccountsCode())){
            //ANNUAL_CLOSING_CHARTS parameter was detected and contained values
        	List<String> generalForwardBalanceObjectTypes = objectTypeService.getGeneralForwardBalanceObjectTypes(fiscalYear);
            Collection<String> generalBalanceForwardBalanceTypesArray = getParameterService().getParameterValuesAsString(BalanceForwardStep.class, GeneralLedgerConstants.BalanceForwardRule.BALANCE_TYPES_TO_ROLL_FORWARD_FOR_BALANCE_SHEET);
             
            if(generalBalanceForwardBalanceTypesArray.contains(postable.getBalanceTypeCode()) && generalForwardBalanceObjectTypes.contains(postable.getObjectCode().getFinancialObjectTypeCode())){
            	GeneralLedgerPendingEntry bbActivityEntry = generateGeneralForwardOriginEntry(document, postable, fiscalYear, sequenceHelper.getSequenceCounter(), reversalDate);
				glpes.add(bbActivityEntry);
				sequenceHelper.increment();
            }
        }

		return glpes;
	}
	
	/**
	 * Generates the nominal close entry for given accounting line.
	 * 
	 * @param document
	 * @param postable
	 * @param sequenceNumber
	 * @param fiscalYear
	 * @return the generated general ledger entry
	 */
	public GeneralLedgerPendingEntry generateNominalCloseEntry(AccountingDocumentBase document, AccountingLine postable, Integer sequenceNumber, Integer fiscalYear, Date reversalDate) {
		SystemOptions currentYearOptions = optionsService.getCurrentYearOptions();
		List<String> expenseObjectCodeTypes = objectTypeService.getExpenseObjectTypes(fiscalYear);
		String varNetExpenseObjectCode = getParameterService().getParameterValueAsString(NominalActivityClosingStep.class, "NET_EXPENSE_OBJECT_CODE");
		String varNetRevenueObjectCode = getParameterService().getParameterValueAsString(NominalActivityClosingStep.class, "NET_REVENUE_OBJECT_CODE");
		String currentDocumentTypeName = document.getFinancialSystemDocumentHeader().getWorkflowDocument().getDocumentTypeName();

		GeneralLedgerPendingEntry activityEntry = new GeneralLedgerPendingEntry();

		activityEntry.setUniversityFiscalYear(fiscalYear);
		activityEntry.setChartOfAccountsCode(postable.getChartOfAccountsCode());
		activityEntry.setAccountNumber(postable.getAccountNumber());
		activityEntry.setSubAccountNumber(StringUtils.isBlank(postable.getSubAccountNumber()) ?KFSConstants.getDashSubAccountNumber() : postable.getSubAccountNumber());

		if (expenseObjectCodeTypes.contains(postable.getObjectCode().getFinancialObjectTypeCode())) {
			activityEntry.setFinancialObjectCode(varNetExpenseObjectCode);
		} else {
			activityEntry.setFinancialObjectCode(varNetRevenueObjectCode);
		}

		activityEntry.setFinancialSubObjectCode(StringUtils.isBlank(postable.getFinancialSubObjectCode()) ? KFSConstants.getDashFinancialSubObjectCode() : postable.getFinancialSubObjectCode());
		activityEntry.setFinancialBalanceTypeCode(currentYearOptions.getNominalFinancialBalanceTypeCd());
		activityEntry.setFinancialObjectTypeCode(postable.getObjectCode().getFinancialObjectTypeCode());
		activityEntry.setUniversityFiscalPeriodCode(KFSConstants.MONTH13);
		activityEntry.setFinancialDocumentTypeCode(currentDocumentTypeName);
		activityEntry.setFinancialSystemOriginationCode(homeOriginationService.getHomeOrigination().getFinSystemHomeOriginationCode());
		activityEntry.setDocumentNumber(new StringBuffer(currentYearOptions.getActualFinancialBalanceTypeCd()).append(postable.getAccountNumber()).toString());
		activityEntry.setTransactionLedgerEntrySequenceNumber(sequenceNumber);

		if (expenseObjectCodeTypes.contains(postable.getObjectCode().getFinancialObjectTypeCode())) {
			activityEntry.setTransactionLedgerEntryDescription(createTransactionLedgerEntryDescription(configurationService.getPropertyValueAsString(KFSKeyConstants.MSG_CLOSE_ENTRY_TO_NOMINAL_EXPENSE), postable));
		} else {
			activityEntry.setTransactionLedgerEntryDescription(createTransactionLedgerEntryDescription(configurationService.getPropertyValueAsString(KFSKeyConstants.MSG_CLOSE_ENTRY_TO_NOMINAL_REVENUE), postable));
		}

		activityEntry.setTransactionLedgerEntryAmount(postable.getAmount());
		activityEntry.setFinancialObjectTypeCode(postable.getObjectCode().getFinancialObjectTypeCode());
		
		activityEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE.equalsIgnoreCase(postable.getDebitCreditCode()) ? KFSConstants.GL_CREDIT_CODE: KFSConstants.GL_DEBIT_CODE);

        Timestamp transactionTimestamp = new Timestamp(dateTimeService.getCurrentDate().getTime());
        activityEntry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        activityEntry.setTransactionEntryProcessedTs(transactionTimestamp);
		activityEntry.setOrganizationDocumentNumber(null);
		activityEntry.setProjectCode(StringUtils.isBlank(postable.getProjectCode()) ? KFSConstants.getDashProjectCode() : postable.getProjectCode());
		activityEntry.setOrganizationReferenceId(postable.getOrganizationReferenceId());
		activityEntry.setReferenceFinancialDocumentTypeCode(null);
		activityEntry.setReferenceFinancialSystemOriginationCode(null);
		activityEntry.setReferenceFinancialDocumentNumber(null);
		activityEntry.setFinancialDocumentReversalDate(reversalDate);
		activityEntry.setTransactionEncumbranceUpdateCode(null);

		if (postable.getAmount().isNegative()) {
			activityEntry.setTransactionLedgerEntryAmount(postable.getAmount().negated());
		}

		return activityEntry;

	}

	/**
	 * Generates the nominal close offset
	 * 
	 * @param document
	 * @param postable
	 * @param sequenceNumber
	 * @param fiscalYear
	 * @return the generated offset
	 */
	public GeneralLedgerPendingEntry generateNominalCloseOffset(AccountingDocumentBase document, AccountingLine postable,Integer sequenceNumber, Integer fiscalYear, Date reversalDate) {
		String varFundBalanceObjectCode = getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_FUND_BALANCE_OBJECT_CODE_PARM);
		String varFundBalanceObjectTypeCode = getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_FUND_BALANCE_OBJECT_TYPE_PARM);
		SystemOptions currentYearOptions = optionsService.getCurrentYearOptions();
		String currentDocumentTypeName = document.getFinancialSystemDocumentHeader().getWorkflowDocument().getDocumentTypeName();
		String debitCreditCode = postable.getDebitCreditCode();

		GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry();
		offsetEntry.setUniversityFiscalYear(fiscalYear);
		offsetEntry.setChartOfAccountsCode(postable.getChartOfAccountsCode());
		offsetEntry.setAccountNumber(postable.getAccountNumber());
		offsetEntry.setSubAccountNumber(StringUtils.isBlank(postable.getSubAccountNumber()) ? KFSConstants.getDashSubAccountNumber() : postable.getSubAccountNumber());//postable.getSubAccountNumber());
		offsetEntry.setFinancialObjectCode(varFundBalanceObjectCode);
		offsetEntry.setFinancialSubObjectCode(StringUtils.isBlank(postable.getFinancialSubObjectCode()) ? KFSConstants.getDashFinancialSubObjectCode() : postable.getFinancialSubObjectCode());
		offsetEntry.setFinancialBalanceTypeCode(currentYearOptions.getNominalFinancialBalanceTypeCd());
		offsetEntry.setFinancialObjectTypeCode(varFundBalanceObjectTypeCode);
		offsetEntry.setUniversityFiscalPeriodCode(KFSConstants.MONTH13);
		offsetEntry.setFinancialDocumentTypeCode(currentDocumentTypeName);
		offsetEntry.setFinancialSystemOriginationCode(homeOriginationService.getHomeOrigination().getFinSystemHomeOriginationCode());
		offsetEntry.setDocumentNumber(new StringBuffer(currentYearOptions.getActualFinancialBalanceTypeCd()).append(postable.getAccountNumber()).toString());
		offsetEntry.setTransactionLedgerEntrySequenceNumber(new Integer(sequenceNumber.intValue()));
		offsetEntry.setTransactionLedgerEntryDescription(createTransactionLedgerEntryDescription(configurationService.getPropertyValueAsString(KFSKeyConstants.MSG_CLOSE_ENTRY_TO_FUND_BALANCE), postable));
		offsetEntry.setTransactionLedgerEntryAmount(postable.getAmount());
		offsetEntry.setTransactionDebitCreditCode(debitCreditCode);
        Timestamp transactionTimestamp = new Timestamp(dateTimeService.getCurrentDate().getTime());
        offsetEntry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        offsetEntry.setTransactionEntryProcessedTs(transactionTimestamp);
		offsetEntry.setOrganizationDocumentNumber(null);
		offsetEntry.setProjectCode(StringUtils.isBlank(postable.getProjectCode()) ? KFSConstants.getDashProjectCode() : postable.getProjectCode());
		offsetEntry.setOrganizationReferenceId(postable.getOrganizationReferenceId());
		offsetEntry.setReferenceFinancialDocumentTypeCode(null);
		offsetEntry.setReferenceFinancialSystemOriginationCode(null);
		offsetEntry.setReferenceFinancialDocumentNumber(null);
		offsetEntry.setFinancialDocumentReversalDate(reversalDate);
		offsetEntry.setTransactionEncumbranceUpdateCode(null);

		return offsetEntry;
	}
	
    /**
     * Generates the general forward entry.
     * 
     * @param document
     * @param postable
     * @param closingFiscalYear
     * @param sequenceNumber
     * @return the generated glpe
     */
    public GeneralLedgerPendingEntry generateGeneralForwardOriginEntry(AccountingDocumentBase document, AccountingLine postable, Integer closingFiscalYear, Integer sequenceNumber, Date reversalDate) {
    	SystemOptions currentYearOptions = optionsService.getCurrentYearOptions();
    	String currentDocumentTypeName = document.getFinancialSystemDocumentHeader().getWorkflowDocument().getDocumentTypeName();

    	GeneralLedgerPendingEntry entry = new GeneralLedgerPendingEntry();
        entry.setUniversityFiscalYear(closingFiscalYear + 1);
        entry.setChartOfAccountsCode(postable.getChartOfAccountsCode());
        entry.setAccountNumber(postable.getAccountNumber());
        entry.setSubAccountNumber(StringUtils.isBlank(postable.getSubAccountNumber()) ? KFSConstants.getDashSubAccountNumber() : postable.getSubAccountNumber());
        entry.setFinancialObjectCode(postable.getFinancialObjectCode());
        entry.setFinancialSubObjectCode(StringUtils.isBlank(postable.getFinancialSubObjectCode()) ? KFSConstants.getDashFinancialSubObjectCode(): postable.getFinancialSubObjectCode());
        entry.setFinancialBalanceTypeCode(postable.getBalanceTypeCode());
        if (currentYearOptions.getFinObjTypeExpendNotExpCode().equals(postable.getObjectCode().getFinancialObjectTypeCode())) {
            entry.setFinancialObjectTypeCode(currentYearOptions.getFinancialObjectTypeAssetsCd());
        }
        else {
            entry.setFinancialObjectTypeCode(postable.getObjectCode().getFinancialObjectTypeCode());
        }
        entry.setUniversityFiscalPeriodCode(KFSConstants.PERIOD_CODE_BEGINNING_BALANCE);
        entry.setFinancialDocumentTypeCode(currentDocumentTypeName);
        entry.setFinancialSystemOriginationCode(homeOriginationService.getHomeOrigination().getFinSystemHomeOriginationCode());

        entry.setDocumentNumber(new StringBuffer(KFSConstants.BALANCE_TYPE_ACTUAL).append(postable.getAccountNumber())/* .append(balance.getChartOfAccountsCode()) */.toString());
        entry.setTransactionLedgerEntrySequenceNumber(sequenceNumber);
        entry.setTransactionLedgerEntryDescription(new StringBuffer("BEG BAL BROUGHT FORWARD FROM ").append(closingFiscalYear).toString());

        String transactionEncumbranceUpdateCode = null;

            transactionEncumbranceUpdateCode = KFSConstants.ENCUMB_UPDT_NO_ENCUMBRANCE_CD;

        entry.setTransactionEncumbranceUpdateCode(transactionEncumbranceUpdateCode);
        KualiDecimal transactionLedgerEntryAmount = KualiDecimal.ZERO;
        transactionLedgerEntryAmount = transactionLedgerEntryAmount.add(postable.getAmount());

        entry.setTransactionDebitCreditCode(postable.getDebitCreditCode());
        Timestamp transactionTimestamp = new Timestamp(dateTimeService.getCurrentDate().getTime());
        entry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        entry.setTransactionEntryProcessedTs(transactionTimestamp);
        entry.setOrganizationDocumentNumber(null);
        entry.setProjectCode(StringUtils.isBlank(postable.getProjectCode()) ? KFSConstants.getDashProjectCode() : postable.getProjectCode());
        entry.setOrganizationReferenceId(postable.getOrganizationReferenceId());
        entry.setReferenceFinancialDocumentTypeCode(null);
        entry.setReferenceFinancialSystemOriginationCode(null);
        entry.setReferenceFinancialDocumentNumber(null);
        entry.setFinancialDocumentReversalDate(reversalDate);
        if (KFSConstants.BALANCE_TYPE_AUDIT_TRAIL.equals(entry.getFinancialBalanceTypeCode())) {
            entry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
        }
        
        if (transactionLedgerEntryAmount.isNegative()) {
            if (KFSConstants.BALANCE_TYPE_ACTUAL.equals(entry.getFinancialBalanceTypeCode())) {
                transactionLedgerEntryAmount = transactionLedgerEntryAmount.negated();
            }
        }
        entry.setTransactionLedgerEntryAmount(transactionLedgerEntryAmount);
        return entry;
    }
    
	/**
	 * Generates the BB income less expense glpe.
	 * 
	 * @param document
	 * @param accountingLine
	 * @param sequenceNumber
	 * @param fiscalYear
	 * @return the generated glpe
	 */
	public GeneralLedgerPendingEntry generateBBIncomeLessExpenseOffset(AccountingDocumentBase document, AccountingLine accountingLine, Integer sequenceNumber, Integer fiscalYear, Date reversalDate) {
		String varFundBalanceObjectCode = getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_FUND_BALANCE_OBJECT_CODE_PARM);
		String varFundBalanceObjectTypeCode = getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_FUND_BALANCE_OBJECT_TYPE_PARM);
		Integer closingFiscalYear = new Integer(getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_FISCAL_YEAR_PARM));
		String currentDocumentTypeName = document.getFinancialSystemDocumentHeader().getWorkflowDocument().getDocumentTypeName();

		GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry();
		offsetEntry.setUniversityFiscalYear(fiscalYear + 1);
		offsetEntry.setChartOfAccountsCode(accountingLine.getChartOfAccountsCode());
		offsetEntry.setAccountNumber(accountingLine.getAccountNumber());
		offsetEntry.setSubAccountNumber(StringUtils.isBlank(accountingLine.getSubAccountNumber()) ? KFSConstants.getDashSubAccountNumber() : accountingLine.getSubAccountNumber());
		offsetEntry.setFinancialObjectCode(varFundBalanceObjectCode);
		offsetEntry.setFinancialSubObjectCode(StringUtils.isBlank(accountingLine.getFinancialSubObjectCode()) ? KFSConstants.getDashFinancialSubObjectCode() : accountingLine.getFinancialSubObjectCode());
		offsetEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
		offsetEntry.setFinancialObjectTypeCode(varFundBalanceObjectTypeCode);
		offsetEntry.setUniversityFiscalPeriodCode(KFSConstants.PERIOD_CODE_BEGINNING_BALANCE);
		offsetEntry.setFinancialDocumentTypeCode(currentDocumentTypeName);
		offsetEntry.setFinancialSystemOriginationCode(homeOriginationService.getHomeOrigination().getFinSystemHomeOriginationCode());
		offsetEntry.setDocumentNumber(document.getDocumentNumber());
		offsetEntry.setTransactionLedgerEntrySequenceNumber(new Integer(sequenceNumber.intValue()));
		offsetEntry.setTransactionLedgerEntryDescription(new StringBuffer("BEG BAL BROUGHT FORWARD FROM ").append(closingFiscalYear).toString());
		offsetEntry.setTransactionLedgerEntryAmount(accountingLine.getAmount());
        Timestamp transactionTimestamp = new Timestamp(dateTimeService.getCurrentDate().getTime());
        offsetEntry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        offsetEntry.setTransactionEntryProcessedTs(transactionTimestamp);
		offsetEntry.setOrganizationDocumentNumber(null);
		offsetEntry.setProjectCode(StringUtils.isBlank(accountingLine.getProjectCode()) ? KFSConstants.getDashProjectCode() : accountingLine.getProjectCode() );
		offsetEntry.setOrganizationReferenceId(accountingLine.getOrganizationReferenceId());
		offsetEntry.setReferenceFinancialDocumentTypeCode(null);
		offsetEntry.setReferenceFinancialSystemOriginationCode(null);
		offsetEntry.setReferenceFinancialDocumentNumber(null);
		offsetEntry.setFinancialDocumentReversalDate(reversalDate);
		offsetEntry.setTransactionEncumbranceUpdateCode(null);

		if (accountingLine.getAmount().isNegative()) {

			offsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
		} else {
			offsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
		}

		if (accountingLine.getAmount().isNegative()) {
			offsetEntry.setTransactionLedgerEntryAmount(accountingLine.getAmount().negated());
		}

		return offsetEntry;
	}
	
	/**
	 * Generates the BB cash offset.
	 * 
	 * @param document
	 * @param accountingLine
	 * @param sequenceNumber
	 * @param documentTypeCode
	 * @param fiscalYear
	 * @return the generated glpe
	 */
	public GeneralLedgerPendingEntry generateBBCashOffset(AccountingDocumentBase document, AccountingLine accountingLine, Integer sequenceNumber, String documentTypeCode, Integer fiscalYear, Date reversalDate){
		
		String varFundBalanceObjectTypeCode = getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_FUND_BALANCE_OBJECT_TYPE_PARM);
		Integer closingFiscalYear = new Integer(getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_FISCAL_YEAR_PARM));
		String currentDocumentTypeName = document.getFinancialSystemDocumentHeader().getWorkflowDocument().getDocumentTypeName();
		String cashOffsetObjectCode = offsetDefinitionService.getByPrimaryId(fiscalYear, accountingLine.getChartOfAccountsCode(), documentTypeCode, KFSConstants.BALANCE_TYPE_ACTUAL).getFinancialObjectCode();
		String debitCreditCode = null;


		GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry();
		offsetEntry.setUniversityFiscalYear(fiscalYear + 1);
		offsetEntry.setChartOfAccountsCode(accountingLine.getChartOfAccountsCode());
		offsetEntry.setAccountNumber(accountingLine.getAccountNumber());
		offsetEntry.setSubAccountNumber(StringUtils.isBlank(accountingLine.getSubAccountNumber()) ? KFSConstants.getDashSubAccountNumber() : accountingLine.getSubAccountNumber());
		offsetEntry.setFinancialObjectCode(cashOffsetObjectCode);
		offsetEntry.setFinancialSubObjectCode(StringUtils.isBlank(accountingLine.getFinancialSubObjectCode()) ? KFSConstants.getDashFinancialSubObjectCode() : accountingLine.getFinancialSubObjectCode());
		offsetEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
		offsetEntry.setFinancialObjectTypeCode(varFundBalanceObjectTypeCode);
		offsetEntry.setUniversityFiscalPeriodCode(KFSConstants.PERIOD_CODE_BEGINNING_BALANCE);
		offsetEntry.setFinancialDocumentTypeCode(currentDocumentTypeName);
		offsetEntry.setFinancialSystemOriginationCode(homeOriginationService.getHomeOrigination().getFinSystemHomeOriginationCode());
		offsetEntry.setDocumentNumber(document.getDocumentNumber());
		offsetEntry.setTransactionLedgerEntrySequenceNumber(new Integer(sequenceNumber.intValue()));
		offsetEntry.setTransactionLedgerEntryDescription("BEG BAL BROUGHT FORWARD FROM " + closingFiscalYear);
		offsetEntry.setTransactionLedgerEntryAmount(accountingLine.getAmount());
		offsetEntry.setTransactionDebitCreditCode(debitCreditCode);
        Timestamp transactionTimestamp = new Timestamp(dateTimeService.getCurrentDate().getTime());
        offsetEntry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        offsetEntry.setTransactionEntryProcessedTs(transactionTimestamp);
		offsetEntry.setOrganizationDocumentNumber(null);
		offsetEntry.setProjectCode(StringUtils.isBlank(accountingLine.getProjectCode()) ? KFSConstants.getDashProjectCode() : accountingLine.getProjectCode());
		offsetEntry.setOrganizationReferenceId(accountingLine.getOrganizationReferenceId());
		offsetEntry.setReferenceFinancialDocumentTypeCode(null);
		offsetEntry.setReferenceFinancialSystemOriginationCode(null);
		offsetEntry.setReferenceFinancialDocumentNumber(null);
		offsetEntry.setFinancialDocumentReversalDate(reversalDate);
		offsetEntry.setTransactionEncumbranceUpdateCode(null);
		
		if (accountingLine.getAmount().isNegative()) {

			offsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
		} else {
			offsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);

		}

		if (accountingLine.getAmount().isNegative()) {
			offsetEntry.setTransactionLedgerEntryAmount(accountingLine.getAmount().negated());
		}

		return offsetEntry;
	}

	/**
	 * Creates the glpe description
	 * 
	 * @param descriptorIntro
	 * @param postable
	 * @return
	 */
	private String createTransactionLedgerEntryDescription(String descriptorIntro, GeneralLedgerPendingEntrySourceDetail postable) {
		StringBuilder description = new StringBuilder();
		description.append(descriptorIntro.trim()).append(' ');
		return description.append(getSizedField(5, postable.getSubAccountNumber())).append("-").append(getSizedField(4, postable.getFinancialObjectCode())).append("-").append(getSizedField(3, postable.getFinancialSubObjectCode())).append("-").append(getSizedField(2, postable.getObjectCode().getFinancialObjectTypeCode())).toString();
	}

	/**
	 * Pads out a string so that it will be a certain length
	 *
	 * @param size
	 *            the size to pad to
	 * @param value
	 *            the String being padded
	 * @return the padded String
	 */
	private StringBuilder getSizedField(int size, String value) {
		StringBuilder fieldString = new StringBuilder();
		if (value != null) {
			fieldString.append(value);
			while (fieldString.length() < size) {
				fieldString.append(' ');
			}
		} else {
			while (fieldString.length() < size) {
				fieldString.append('-');
			}
		}
		return fieldString;
	}

	/**
	 * Gets the objectTypeService.
	 * 
	 * @return objectTypeService
	 */
	public ObjectTypeService getObjectTypeService() {
		return objectTypeService;
	}

	/**
	 * Sets the objectTypeService.
	 * 
	 * @param objectTypeService
	 */
	public void setObjectTypeService(ObjectTypeService objectTypeService) {
		this.objectTypeService = objectTypeService;
	}


	/**
	 * Gets the optionsService.
	 * 
	 * @return optionsService
	 */
	public OptionsService getOptionsService() {
		return optionsService;
	}

	/**
	 * Sets the optionsService.
	 * 
	 * @param optionsService
	 */
	public void setOptionsService(OptionsService optionsService) {
		this.optionsService = optionsService;
	}

	/**
	 * Gets the homeOriginationService.
	 * 
	 * @return homeOriginationService
	 */
	public HomeOriginationService getHomeOriginationService() {
		return homeOriginationService;
	}

	/**
	 * Sets the homeOriginationService.
	 * 
	 * @param homeOriginationService
	 */
	public void setHomeOriginationService(HomeOriginationService homeOriginationService) {
		this.homeOriginationService = homeOriginationService;
	}

	/**
	 * Gets the configurationService.
	 * 
	 * @return configurationService
	 */
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	/**
	 * Sets the configurationService.
	 * 
	 * @param configurationService
	 */
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	/**
	 * Gets the dateTimeService.
	 * 
	 * @return dateTimeService
	 */
	public DateTimeService getDateTimeService() {
		return dateTimeService;
	}

	/**
	 * Sets the dateTimeService.
	 * 
	 * @param dateTimeService
	 */
	public void setDateTimeService(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}

	/**
	 * Gets the offsetDefinitionService.
	 * 
	 * @return offsetDefinitionService
	 */
	public OffsetDefinitionService getOffsetDefinitionService() {
		return offsetDefinitionService;
	}

	/**
	 * Sets the offsetDefinitionService.
	 * 
	 * @param offsetDefinitionService
	 */
	public void setOffsetDefinitionService(OffsetDefinitionService offsetDefinitionService) {
		this.offsetDefinitionService = offsetDefinitionService;
	}

	/**
	 * Gets the parameterService.
	 * 
	 * @return parameterService
	 */
	public ParameterService getParameterService() {
		return parameterService;
	}

	/**
	 * Sets the parameterService.
	 * 
	 * @param parameterService
	 */
	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

}
