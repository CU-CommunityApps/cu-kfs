package edu.cornell.kfs.module.purap.document.workflow;

import org.kuali.kfs.module.purap.document.workflow.PurchaseOrderActionListAttribute;
import org.kuali.kfs.kew.api.action.ActionSet;
import org.kuali.kfs.kew.actionitem.ActionItem;
import org.kuali.kfs.kew.api.KewApiConstants;
import java.util.ArrayList;
import java.util.List;

public class CuPurchaseOrderActionListAttribute extends PurchaseOrderActionListAttribute {
    @Override
    public ActionSet getLegalActions(final String principalId, final ActionItem actionItem) {
        final List<String> actionSetList = new ArrayList<>();
        actionSetList.add(KewApiConstants.ACTION_TAKEN_FYI_CD);        
        return new ActionSet(actionSetList);
    }

}
