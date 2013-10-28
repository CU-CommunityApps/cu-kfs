package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.validation.event.AttributedAddPurchasingAccountsPayableItemEvent;
import org.kuali.kfs.module.purap.document.web.struts.PurchasingFormBase;
import org.kuali.kfs.module.purap.document.web.struts.RequisitionAction;
import org.kuali.kfs.module.purap.document.web.struts.RequisitionForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorCommodityCode;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.service.KualiRuleService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.service.UserFavoriteAccountService;

public class CuRequisitionAction extends RequisitionAction {

    @Override
    public ActionForward addItem(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurApItem item = purchasingForm.getNewPurchasingItemLine();
        RequisitionItem requisitionItem = (RequisitionItem)item;
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();
        
        if (StringUtils.isBlank(requisitionItem.getPurchasingCommodityCode())) {
            boolean commCodeParam = SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(RequisitionDocument.class, PurapParameterConstants.ENABLE_DEFAULT_VENDOR_COMMODITY_CODE_IND);

            if (commCodeParam) {
                if (purchasingForm instanceof RequisitionForm) {
                    RequisitionDocument reqs =(RequisitionDocument)purchasingForm.getDocument();
                    VendorDetail dtl = reqs.getVendorDetail();
                    if (ObjectUtils.isNotNull(dtl)) {
                        List<VendorCommodityCode> vcc = dtl.getVendorCommodities();
                        String defaultCommodityCode = "";
                        Iterator<VendorCommodityCode> it = vcc.iterator();
                        while (it.hasNext()) {
                            VendorCommodityCode commodity = it.next();
                            if (commodity.isCommodityDefaultIndicator()) {
                                defaultCommodityCode = commodity.getPurchasingCommodityCode();
                                requisitionItem.setPurchasingCommodityCode(defaultCommodityCode);
                            }
                        }
                    }
                }
            }
        }
        
        boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(new AttributedAddPurchasingAccountsPayableItemEvent("", purDocument, item));

        if (rulePassed) {
            item = purchasingForm.getAndResetNewPurchasingItemLine();
            purDocument.addItem(item);
            // KFSPTS-985
            if (isDocumentIntegratedFavoriteAccount(purDocument)) {
                populatePrimaryFavoriteAccount(item.getSourceAccountingLines(), purDocument instanceof RequisitionDocument);
            }
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    protected boolean isDocumentIntegratedFavoriteAccount(PurchasingDocument document) {
        return 	document instanceof RequisitionDocument || document instanceof PurchaseOrderAmendmentDocument || document instanceof PurchaseOrderDocument;
    }
    protected void populatePrimaryFavoriteAccount(List<PurApAccountingLine> sourceAccountinglines, boolean isRequisition) {
    	FavoriteAccount account =  SpringContext.getBean(UserFavoriteAccountService.class).getFavoriteAccount(GlobalVariables.getUserSession().getPrincipalId());
    	if (ObjectUtils.isNotNull(account)) {
    		sourceAccountinglines.add(SpringContext.getBean(UserFavoriteAccountService.class).getPopulatedNewAccount(account, isRequisition));
    	}
    }

}
