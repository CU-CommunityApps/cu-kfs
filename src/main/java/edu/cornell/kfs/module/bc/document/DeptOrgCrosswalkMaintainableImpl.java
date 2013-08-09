package edu.cornell.kfs.module.bc.document;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.krad.bo.DocumentHeader;

import edu.cornell.kfs.module.bc.businessobject.DeptOrgCrosswalk;
import edu.cornell.kfs.module.bc.service.DeptOrgCrosswalkService;

@SuppressWarnings("serial")
public class DeptOrgCrosswalkMaintainableImpl extends FinancialSystemMaintainable {
	  public void doRouteStatusChange(DocumentHeader documentHeader) {
	        WorkflowDocument workflowDocument = documentHeader.getWorkflowDocument();
	        DeptOrgCrosswalkService deptOrgCrosswalkService  = SpringContext.getBean(DeptOrgCrosswalkService.class);
	        DeptOrgCrosswalk deptOrgCrosswalk = (DeptOrgCrosswalk)this.getBusinessObject();
	        
	        if(workflowDocument.getDocument().getStatus().getCode().equalsIgnoreCase(KewApiConstants.ROUTE_HEADER_FINAL_CD))
	        	deptOrgCrosswalkService.updatePositionDataExtension(deptOrgCrosswalk.getHrDepartment(), deptOrgCrosswalk.getOrgCode());
	  
	  }


}
