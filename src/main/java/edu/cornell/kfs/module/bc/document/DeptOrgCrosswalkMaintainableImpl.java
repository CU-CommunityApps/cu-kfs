package edu.cornell.kfs.module.bc.document;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kns.bo.DocumentHeader;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

import edu.cornell.kfs.module.bc.businessobject.DeptOrgCrosswalk;
import edu.cornell.kfs.module.bc.service.DeptOrgCrosswalkService;

public class DeptOrgCrosswalkMaintainableImpl extends FinancialSystemMaintainable {
	  public void doRouteStatusChange(DocumentHeader documentHeader) {
	        KualiWorkflowDocument workflowDocument = documentHeader.getWorkflowDocument();
	        DeptOrgCrosswalkService deptOrgCrosswalkService  = SpringContext.getBean(DeptOrgCrosswalkService.class);
	        DeptOrgCrosswalk deptOrgCrosswalk = (DeptOrgCrosswalk)this.getBusinessObject();
	        
	        if(workflowDocument.getRouteHeader().getDocRouteStatus().equalsIgnoreCase(KEWConstants.ROUTE_HEADER_FINAL_CD))
	        	deptOrgCrosswalkService.updatePositionDataExtension(deptOrgCrosswalk.getHrDepartment(), deptOrgCrosswalk.getOrgCode());
	  
	  }


}
