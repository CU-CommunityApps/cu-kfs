package edu.cornell.kfs.coa.document.validation.impl;

import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.ObjectCodeGlobal;
import org.kuali.kfs.coa.document.validation.impl.ObjectCodeGlobalRule;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetail;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.coa.businessobject.ObjectCodeGlobalDetail;

import edu.cornell.kfs.coa.businessobject.CUObjectCodeGlobal;
import edu.cornell.kfs.coa.businessobject.ContractGrantReportingCode;
import edu.cornell.kfs.sys.CUKFSKeyConstants;


@SuppressWarnings("deprecation")
public class ObjectCodeGlobalExtensionRule extends ObjectCodeGlobalRule {

  /*
   * (non-Javadoc)
   * @see org.kuali.kfs.coa.document.validation.impl.ObjectCodeGlobalRule#processCustomRouteDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
   */
  @Override
  protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
      boolean success = super.processCustomRouteDocumentBusinessRules(document);
      Object maintainableObject = document.getNewMaintainableObject().getBusinessObject();
      success &= checkContractGrantReportingCode((CUObjectCodeGlobal) maintainableObject);
      return success;
  }

  /*
   * Verify value has been entered for required attribute cgReportingCode
   */
  protected boolean checkContractGrantReportingCode(CUObjectCodeGlobal ocg) {
    boolean success = true;

    String cgReportingCode = ocg.getCgReportingCode();
    
    for(GlobalBusinessObjectDetail ocgd : ocg.getAllDetailObjects()) {
      String chartOfAccountsCode = ((ObjectCodeGlobalDetail)ocgd).getChartOfAccountsCode();
      
      if ((!StringUtils.isBlank(cgReportingCode)) && (!StringUtils.isBlank(cgReportingCode))) {
        // have values for both table primary keys
        HashMap<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put("chartOfAccountsCode", chartOfAccountsCode);
        fieldValues.put("code", cgReportingCode);  // prompt table has attribute defined as "code" and we need to use it for the lookup
        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
        Collection<ContractGrantReportingCode> retVals = bos.findMatching(ContractGrantReportingCode.class, fieldValues);

        if (retVals.isEmpty()) {
          putFieldError("cgReportingCode", CUKFSKeyConstants.ERROR_DOCUMENT_OBJCDMAINT_CG_RPT_CAT_CODE_NOT_EXIST, new String[] {chartOfAccountsCode, cgReportingCode});
          success = false;
        } else { //verify the value to be assigned is active
          for (ContractGrantReportingCode sfp : retVals) {
            if (!sfp.isActive()) {
              putFieldError("cgReportingCode", KFSKeyConstants.ERROR_INACTIVE, getFieldLabel(CUObjectCodeGlobal.class, "cgReportingCode"));
              success = false;
            }
          }
        }
      } //implied else coa or cgReportingCode or both are blank, caught by maintenance doc having these fields defined as "required", else coding to report this causes double error messages
    }
    
    return success;
  }

}