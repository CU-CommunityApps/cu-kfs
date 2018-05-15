package edu.cornell.kfs.module.cg.businessobject.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.kfs.kns.web.struts.form.LookupForm;
import org.kuali.kfs.kns.web.ui.Column;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.ResultRow;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.module.cg.CGPropertyConstants;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.lookup.AwardLookupableHelperServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.module.cg.businessobject.CuAward;

public class CuAwardLookupableHelperServiceImpl extends AwardLookupableHelperServiceImpl {
    private static final long serialVersionUID = 1372707190594999024L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAwardLookupableHelperServiceImpl.class);
    
    @Override
    public List<HtmlData> getCustomActionUrls(BusinessObject businessObject, List pkNames) {
        LOG.info("getCustomActionUrls, entering");
        List<HtmlData> anchorHtmlDataList = new ArrayList<HtmlData>();
        
        if (canEditAward()) {
            anchorHtmlDataList.add(getUrlData(businessObject, KRADConstants.MAINTENANCE_EDIT_METHOD_TO_CALL, pkNames));
            if (allowsMaintenanceNewOrCopyAction()) {
                anchorHtmlDataList.add(getUrlData(businessObject, KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL, pkNames));
            }
        }

        // only display invoice lookup URL if CGB is enabled
        if (canViewInvoiceLink() && getAccountsReceivableModuleBillingService().isContractsGrantsBillingEnhancementActive()) {
            AnchorHtmlData invoiceUrl = getInvoicesLookupUrl(businessObject);
            anchorHtmlDataList.add(invoiceUrl);
        }

        return anchorHtmlDataList;
    }
    
    @Override
    public Collection<? extends BusinessObject> performLookup(LookupForm lookupForm, Collection<ResultRow> resultTable, boolean bounded) {
        LOG.info("performLookup, entering");
        Collection<? extends BusinessObject> results = super.performLookup(lookupForm, resultTable, bounded);
        if (canViewInvoiceLink()) {
            results.stream().map(obj -> (CuAward) obj).forEach(this::setInvoiceUrlOnAward);
        }
        return results;
    }
    
    private void setInvoiceUrlOnAward(CuAward award) {
        String invoiceLink = getInvoicesLookupUrl(award).getHref();
        LOG.info("setInvoiceUrlOnAward, award: " + award.getProposalNumber() + " invoice URL: " + invoiceLink);
        award.setInvoiceLink(invoiceLink);
    }
    
    /**
     * @todo implement this
     * @return
     */
    private boolean canViewInvoiceLink() {
        return true;
    }
    
    /**
     * @todo implement this
     */
    private boolean canEditAward() {
        return true;
    }
}
