package edu.cornell.kfs.coa.document.validation.impl;

import edu.cornell.kfs.sys.CUKFSConstants;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.A21IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.document.validation.impl.SubAccountPreRules;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentRestrictions;
import org.kuali.kfs.kns.service.BusinessObjectAuthorizationService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuSubAccountPreRules extends SubAccountPreRules {

  @SuppressWarnings("deprecation")
  @Override
  protected boolean doCustomPreRules(MaintenanceDocument document) {
    boolean preRulesOK = super.doCustomPreRules(document);
    preRulesOK &= checkOffCampus(document);
    return preRulesOK;
  }

    @SuppressWarnings("deprecation")
    protected void copyICRFromAccount(MaintenanceDocument document) {
        Person user = GlobalVariables.getUserSession().getPerson();

        // get a new instance of MaintenanceDocumentAuthorizations for this context
        MaintenanceDocumentRestrictions auths = SpringContext.getBean(BusinessObjectAuthorizationService.class)
                .getMaintenanceDocumentRestrictions(document, user);

        // don't need to copy if the user does not have the authority to edit the fields
        if (!auths.getFieldRestriction("a21SubAccount.financialIcrSeriesIdentifier").isReadOnly()) {
            // only need to do this of the account sub type is EX
            A21SubAccount a21SubAccount = newSubAccount.getA21SubAccount();
            Account account = newSubAccount.getAccount();
            if (KFSConstants.SubAccountType.EXPENSE.equals(a21SubAccount.getSubAccountTypeCode())) {
                if (ObjectUtils.isNull(account) || StringUtils.isBlank(account.getAccountNumber())) {
					account = getAccountService().getByPrimaryId(newSubAccount.getChartOfAccountsCode(),
							newSubAccount.getAccountNumber());
                }
                if (ObjectUtils.isNotNull(account)) {
                    if (a21SubAccount.getA21ActiveIndirectCostRecoveryAccounts().isEmpty()) {
                        for (IndirectCostRecoveryAccount icrAccount : account.getActiveIndirectCostRecoveryAccounts()){
							A21IndirectCostRecoveryAccount copyAccount = A21IndirectCostRecoveryAccount
									.copyICRAccount(icrAccount);
							copyAccount.setNewCollectionRecord(true);
							a21SubAccount.getA21IndirectCostRecoveryAccounts().add(copyAccount);
                        }
                    }
                    if (StringUtils.isBlank(a21SubAccount.getFinancialIcrSeriesIdentifier())) {
                        a21SubAccount.setFinancialIcrSeriesIdentifier(account.getFinancialIcrSeriesIdentifier());
                    }
                    if (StringUtils.isBlank(a21SubAccount.getIndirectCostRecoveryTypeCode())) {
                        a21SubAccount.setIndirectCostRecoveryTypeCode(account.getAcctIndirectCostRcvyTypeCd());
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    protected boolean checkOffCampus(MaintenanceDocument saccDoc) {
      boolean continueRules = true;
      boolean saccOffCampus = newSubAccount.getA21SubAccount().getOffCampusCode();

      if (saccOffCampus) {
        String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.QUESTION_A21SUBACCOUNT_OFF_CAMPUS_INDICATOR);

        boolean leaveAsIs = super.askOrAnalyzeYesNoQuestion(CUKFSConstants.A21SubAccountDocumentConstants.OFF_CAMPUS_INDICATOR_QUESTION_ID, questionText);

        if (!leaveAsIs) {
          // return to document if the user doesn't want to clear the indicator
          super.event.setActionForwardName(KFSConstants.MAPPING_BASIC);
          continueRules = false;
        }
      }

      return continueRules;
    }
}
