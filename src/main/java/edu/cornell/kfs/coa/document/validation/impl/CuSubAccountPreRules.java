package edu.cornell.kfs.coa.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.A21IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.document.validation.impl.SubAccountPreRules;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.document.authorization.MaintenanceDocumentRestrictions;
import org.kuali.rice.kns.service.BusinessObjectAuthorizationService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuSubAccountPreRules extends SubAccountPreRules {
    protected void copyICRFromAccount(MaintenanceDocument document) {
        Person user = GlobalVariables.getUserSession().getPerson();

        // get a new instance of MaintenanceDocumentAuthorizations for this context
        MaintenanceDocumentRestrictions auths = SpringContext.getBean(BusinessObjectAuthorizationService.class).getMaintenanceDocumentRestrictions(document, user);

        // don't need to copy if the user does not have the authority to edit the fields
        if (!auths.getFieldRestriction("a21SubAccount.financialIcrSeriesIdentifier").isReadOnly()) {
            // only need to do this of the account sub type is EX
            A21SubAccount a21SubAccount = newSubAccount.getA21SubAccount();
            Account account = newSubAccount.getAccount();
            if (KFSConstants.SubAccountType.EXPENSE.equals(a21SubAccount.getSubAccountTypeCode())) {
                if (ObjectUtils.isNull(account) || StringUtils.isBlank(account.getAccountNumber())) {
                    account = getAccountService().getByPrimaryId(newSubAccount.getChartOfAccountsCode(), newSubAccount.getAccountNumber());
                }
                if (ObjectUtils.isNotNull(account)) {
                    if (a21SubAccount.getA21ActiveIndirectCostRecoveryAccounts().isEmpty()) {
                        for (IndirectCostRecoveryAccount icrAccount : account.getActiveIndirectCostRecoveryAccounts()){
                            a21SubAccount.getA21IndirectCostRecoveryAccounts().add(A21IndirectCostRecoveryAccount.copyICRAccount(icrAccount));
                        }
                    }
                    if (StringUtils.isBlank(a21SubAccount.getFinancialIcrSeriesIdentifier())) {
                        a21SubAccount.setFinancialIcrSeriesIdentifier(account.getFinancialIcrSeriesIdentifier());
                        a21SubAccount.setOffCampusCode(account.isAccountOffCampusIndicator());
                    }
                    if (StringUtils.isBlank(a21SubAccount.getIndirectCostRecoveryTypeCode())) {
                        a21SubAccount.setIndirectCostRecoveryTypeCode(account.getAcctIndirectCostRcvyTypeCd());
                    }
                }
            }
        }
    }

}
