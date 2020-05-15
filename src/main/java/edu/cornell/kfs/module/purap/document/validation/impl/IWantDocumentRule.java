package edu.cornell.kfs.module.purap.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.FinancialSystemWorkflowHelperService;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.kns.rules.DocumentRuleBase;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.CUPurapPropertyConstants;
import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.validation.AddIWantItemRule;

@SuppressWarnings("deprecation")
public class IWantDocumentRule extends DocumentRuleBase implements AddIWantItemRule {
    private static BusinessObjectDictionaryService businessObjectDictionaryService;

    @Override
    public boolean processAddIWantItemRules(IWantDocument document, IWantItem item, String errorPathPrefix) {
        boolean valid = true;
        if (StringUtils.isBlank(item.getItemDescription())) {
            valid = false;
            String attributeLabel = getBusinessObjectDictionaryService().getBusinessObjectEntry(IWantItem.class.getName()).getAttributeDefinition(CUPurapPropertyConstants.IWNT_ITEM_DESC).getLabel();
            GlobalVariables.getMessageMap().putError("newIWantItemLine.itemDescription", KFSKeyConstants.ERROR_REQUIRED, attributeLabel);
        }
        return valid;
    }

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(Document document) {
        boolean valid = super.processCustomRouteDocumentBusinessRules(document);
        IWantDocument iWantDocument = (IWantDocument) document;
        if (SpringContext.getBean(FinancialSystemWorkflowHelperService.class).isAdhocApprovalRequestedForPrincipal(
                document.getDocumentHeader().getWorkflowDocument(), GlobalVariables.getUserSession().getPrincipalId())) {
            //validate that Complete order option was selected
            if (StringUtils.isBlank(iWantDocument.getCompleteOption())) {
                GlobalVariables.getMessageMap().putError("document.completeOption", CUPurapKeyConstants.ERROR_IWNT_CONMPLETE_ORDER_OPTION_REQUIRED);
            }
        }
        
        return valid;
    }

    public BusinessObjectDictionaryService getBusinessObjectDictionaryService() {
        if (businessObjectDictionaryService == null) {
            businessObjectDictionaryService = SpringContext.getBean(BusinessObjectDictionaryService.class);
        }
        return businessObjectDictionaryService;
    }

}
