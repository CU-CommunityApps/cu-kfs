package edu.cornell.kfs.coa.service;

import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * <p>Title: AccountVerificationWebService</p>
 * <p>Description: Describes the functions that need to be implemented in the account verification web service</p>
 * <p>Copyright: Copyright (c) 2010</p>
 * <p>Company: Cornell University: Kuali Financial Systems</p>
 * @author Sandy Eccleston
 * @version 1.0
 */

@WebService 
public interface AccountVerificationWebService {

  /**
   * A valid account is one that has a status of active in the chart of Accounts Code, and Account number is active and valid for this Chart
   * @param chartOfAccountsCode String 
   * @param accountNumber String 
   * @return Boolean
   */
  public boolean isValidAccountString(
          @WebParam(name = "chartOfAccountsCode")String chartOfAccountsCode, 
          @WebParam(name = "accountNumber")String accountNumber, 
          @WebParam(name = "subAccountNumber")String subAccountNumber, 
          @WebParam(name = "objectCode")String objectCode,
          @WebParam(name = "subObjectCode") String subObjectCode,
          @WebParam(name = "projectCode") String projectCode) throws Exception;

  /**
   * A valid subAccount is one that is currently active and is valid for this Chart and this Account Number
   * @param chartOfAccountsCode String 
   * @param accountNumber String  
   * @param subAccountNumber String  
   * @return Boolean
   */
  public boolean isValidSubAccount(
          @WebParam(name = "chartOfAccountsCode")String chartOfAccountsCode, 
          @WebParam(name = "accountNumber")String accountNumber, 
          @WebParam(name = "subAccountNumber")String subAccountNumber) throws Exception;


  /**
   * A valid objectCode is currently active and is valid for this Chart and Fiscal Year
   * @param chartOfAccountsCode String 
   * @param objectCode String 
   * @return Boolean
   */
  public boolean isValidObjectCode(
          @WebParam(name = "chartOfAccountsCode")String chartOfAccountsCode, 
          @WebParam(name = "objectCode")String objectCode) throws Exception;


  /**
   * A valid subObjectCode is currently active and is valid for this Chart, Account, Object Code, and Fiscal Year
   * @param chartOfAccountsCode String 
   * @param accountNumber String
   * @param objectCode String 
   * @param subObjectCode String 
   * @return Boolean
   */
  public boolean isValidSubObjectCode(
          @WebParam(name = "chartOfAccountsCode")String chartOfAccountsCode, 
          @WebParam(name = "accountNumber")String accountNumber, 
          @WebParam(name = "objectCode")String objectCode,
          @WebParam(name = "subObjectCode") String subObjectCode) throws Exception;

  /**
   * A valid Project Code is valid 
   * @param projectCode String 
   * @return Boolean
   */
  public boolean isValidProjectCode(@WebParam(name = "projectCode") String projectCode) throws Exception;

}