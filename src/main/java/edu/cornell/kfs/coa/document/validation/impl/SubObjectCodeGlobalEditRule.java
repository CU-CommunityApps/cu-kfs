package edu.cornell.kfs.coa.document.validation.impl;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.document.validation.impl.GlobalDocumentRuleBase;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.SubObjectCodeGlobalEdit;
import edu.cornell.kfs.coa.businessobject.SubObjectCodeGlobalEditDetail;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class SubObjectCodeGlobalEditRule extends GlobalDocumentRuleBase {
	
	protected SubObjectCodeGlobalEdit subObjectCodeGlobalEdit;
	
    public void setupConvenienceObjects() {

    	subObjectCodeGlobalEdit = (SubObjectCodeGlobalEdit) super.getNewBo();

        // forces refreshes on all the sub-objects in the lists
        for (SubObjectCodeGlobalEditDetail subObjectCodeGlobalEditDetail : subObjectCodeGlobalEdit.getSubObjCdGlobalEditDetails()) {
        	subObjectCodeGlobalEditDetail.refreshNonUpdateableReferences();
        }
    }
    
    @Override
    protected boolean processCustomApproveDocumentBusinessRules(MaintenanceDocument document) {
        boolean success = true;
        setupConvenienceObjects();
        
        // check if there are any accounts
        if (ObjectUtils.isNull(subObjectCodeGlobalEdit.getSubObjCdGlobalEditDetails()) || subObjectCodeGlobalEdit.getSubObjCdGlobalEditDetails().size() == 0) {
            putGlobalError(CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_SUBPOBJ_CD_INACTIVATION_NO_SUB_OBJ_CDS);
            success = false;
        }

        return success;
    }


    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean success = true;
        setupConvenienceObjects();
        
        // check if there are any accounts
        if (ObjectUtils.isNull(subObjectCodeGlobalEdit.getSubObjCdGlobalEditDetails()) || subObjectCodeGlobalEdit.getSubObjCdGlobalEditDetails().size() == 0) {
            putGlobalError(CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_SUBPOBJ_CD_INACTIVATION_NO_SUB_OBJ_CDS);
            success = false;
        }

        return success;
    }

}
