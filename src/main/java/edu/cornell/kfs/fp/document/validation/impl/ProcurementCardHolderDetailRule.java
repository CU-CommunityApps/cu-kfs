/*
 * Copyright 2006-2007 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.fp.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.ProcurementCardHolderDetail;

/**
 * This class represents business rules for the procurement cardholder maintenance document
 */
@SuppressWarnings("deprecation")
public class ProcurementCardHolderDetailRule extends MaintenanceDocumentRuleBase
{
  private ProcurementCardHolderDetail newProcurementCardHolderDetail;

  /**
   * Sets up a ProcurementCardHolderDetail convenience objects to make sure all possible sub-objects are populated
   *
   * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#setupConvenienceObjects()
   */
  public void setupConvenienceObjects()
  {
    newProcurementCardHolderDetail = (ProcurementCardHolderDetail) super.getNewBo();
  }

  /**
   * Return true if rules for processing a save for the procurement cardholder maintenance document are are valid.
   *
   * @param document maintenance document
   * @return true chart/account/organization is valid
   * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomSaveDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
   */
  protected boolean processCustomSaveDocumentBusinessRules(MaintenanceDocument document)
  {
    // default to success
    boolean success = true;
    setupConvenienceObjects();

    // check chart/account/organization is valid
    success &= checkAccountValidity();

    return success;
  }

  /**
   * Returns value from processCustomRouteDocumentBusinessRules(document)
   *
   * @param document maintenance document
   * @return value from processCustomRouteDocumentBusinessRules(document)
   * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomApproveDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
   */
  protected boolean processCustomApproveDocumentBusinessRules(MaintenanceDocument document)
  {
    return processCustomRouteDocumentBusinessRules(document);
  }

  /**
   * Returns true procurement cardholder maintenance document is routed successfully
   *
   * @param document submitted procurement cardholder maintenance document
   * @return true if chart/account/organization is valid
   * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
   */
  protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document)
  {
    // default to success
    boolean success = true;
    setupConvenienceObjects();

    // check chart/account/organization is valid
    success &= checkAccountValidity();

    return success;
  }

  /**
   * Returns true if chart/account/organization is valid
   *
   * @return true if chart/account/organization is valid
   */
  protected boolean checkAccountValidity()
  {
    boolean result = false;

    // check that an org has been entered
    if (StringUtils.isNotBlank(newProcurementCardHolderDetail.getOrganizationCode()))
    {

      AccountService acctService = SpringContext.getBean(AccountService.class);
      if (StringUtils.isNotBlank(newProcurementCardHolderDetail.getChartOfAccountsCode()))
      {
        Account defAccount =
          acctService.getByPrimaryId(newProcurementCardHolderDetail.getChartOfAccountsCode(), newProcurementCardHolderDetail.getAccountNumber());

        // if the object doesn't exist, then we can't continue, so exit
        if (ObjectUtils.isNull(defAccount))
        {
          return result;
        }

        if (newProcurementCardHolderDetail.getOrganizationCode().equals(defAccount.getOrganizationCode()))
        {
          result = true;
        }

        if ( ! result)
        {
          putFieldError( "organizationCode",
                  KFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ACCOUNT_INVALID_ORG,
                  new String[] {
                    newProcurementCardHolderDetail.getAccountNumber(),
                    newProcurementCardHolderDetail.getOrganizationCode()
                  });
        }
      }
    }

    return result;
  }

}
