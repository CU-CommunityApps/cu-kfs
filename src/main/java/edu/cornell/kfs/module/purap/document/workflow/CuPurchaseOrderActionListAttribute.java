package edu.cornell.kfs.module.purap.document.workflow;

import org.kuali.kfs.module.purap.document.workflow.PurchaseOrderActionListAttribute;
import org.kuali.rice.kew.api.action.ActionSet;

public class CuPurchaseOrderActionListAttribute extends PurchaseOrderActionListAttribute {
    public ActionSet getLegalActions(String principalId, org.kuali.rice.kew.api.action.ActionItem actionItem) throws Exception {
        ActionSet actionSet = super.getLegalActions(principalId, actionItem);
        actionSet.addFyi();
        return actionSet;
    }

}
