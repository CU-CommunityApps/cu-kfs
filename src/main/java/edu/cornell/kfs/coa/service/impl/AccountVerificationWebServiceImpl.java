package edu.cornell.kfs.coa.service.impl;

import javax.jws.WebService;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.ProjectCodeService;
import org.kuali.kfs.coa.service.SubAccountService;
import org.kuali.kfs.coa.service.SubObjectCodeService;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.coa.service.AccountVerificationWebService;

/**
 *
 * <p>Title: AccountVerificationServiceImpl</p>
 * <p>Description: Implements the webservice for account verification</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Cornell University: Kuali Financial Systems</p>
 * @author Sandy Eccleston
 * @version 1.0
 */
@WebService(endpointInterface = "edu.cornell.kfs.coa.service.AccountVerificationWebService")
public class AccountVerificationWebServiceImpl implements AccountVerificationWebService {

    public boolean isValidAccountString(String chartOfAccountsCode, String accountNumber, String subAccountNumber, 
                                      String objectCode, String subObjectCode, String projectCode) throws Exception {

        boolean isValid = false;

        if (chartOfAccountsCode == null 
            || chartOfAccountsCode.isEmpty()
            || accountNumber == null 
            || accountNumber.isEmpty() 
            || objectCode == null 
            || objectCode.isEmpty()) {
            return false;
        }

        Account account = SpringContext.getBean(AccountService.class).getByPrimaryId(chartOfAccountsCode, accountNumber); 

        if (account == null || account.toString().isEmpty()) {
            isValid = false;
        } else {
//    isValid = true;
            if (!account.isActive() || account.isClosed() || account.isExpired()) {
                isValid = false;
            } else {
                isValid = true;
            }
            if (objectCode != null && !objectCode.isEmpty()) {
                isValid = isValid && isValidObjectCode(chartOfAccountsCode, objectCode);
            }

            if (subAccountNumber != null && !subAccountNumber.isEmpty()) {
                isValid = isValid && isValidSubAccount(chartOfAccountsCode, accountNumber, subAccountNumber);
            }

            if (subObjectCode != null && !subObjectCode.isEmpty()) {
                isValid = isValid && isValidSubObjectCode(chartOfAccountsCode, accountNumber, objectCode, subObjectCode);
            }

            if (projectCode != null && !projectCode.isEmpty()) {
                isValid = isValid && isValidProjectCode(projectCode);
            }
        }

        return isValid;
    }


    public boolean isValidSubAccount(String chartOfAccountsCode, String accountNumber, String subAccountNumber) throws Exception {
        boolean isValidSubAccount = false; 

        SubAccount subAccount = SpringContext.getBean(SubAccountService.class).getByPrimaryId(chartOfAccountsCode, accountNumber, subAccountNumber); 

        if (subAccount == null || subAccount.toString().isEmpty() || (!subAccount.isActive())) {
            isValidSubAccount = false;
        } else {
            isValidSubAccount = true;
        }

        return isValidSubAccount;
    }

    public boolean isValidObjectCode(String chartOfAccountsCode, String objectCodeParm) throws Exception {
        boolean isValidObjectCode = false;

        ObjectCode objectCode = SpringContext.getBean(ObjectCodeService.class).getByPrimaryIdForCurrentYear(chartOfAccountsCode, objectCodeParm);
        if (objectCode == null || objectCode.toString().isEmpty() || (!objectCode.isActive())) {
            isValidObjectCode = false;
        } else {
            isValidObjectCode = true;
        }
        return isValidObjectCode; 
    }

    public boolean isValidSubObjectCode(String chartOfAccountsCode, String accountNumber, String objectCode, String subObjectCodeParm) throws Exception {
        boolean isValidSubObjectCode = false;

        SubObjectCode subObjectCode = SpringContext.getBean(SubObjectCodeService.class)
                .getByPrimaryIdForCurrentYear(chartOfAccountsCode, accountNumber, objectCode, subObjectCodeParm);

        if (subObjectCode == null || subObjectCode.toString().isEmpty() || (!subObjectCode.isActive())) {
            isValidSubObjectCode = false;
        } else {
            isValidSubObjectCode = true;
        }

        return isValidSubObjectCode;
    }

    public boolean isValidProjectCode(String projectCodeParm) throws Exception {
        boolean isValidProjectCode = false;

        ProjectCode projectCode = SpringContext.getBean(ProjectCodeService.class).getByPrimaryId(projectCodeParm);

        if (projectCode == null || projectCode.toString().isEmpty() || (!projectCode.isActive())) {
            isValidProjectCode = false;
        } else {
            isValidProjectCode = true;
        }

        return isValidProjectCode;
    }


}