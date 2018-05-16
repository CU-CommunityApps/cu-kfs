package edu.cornell.kfs.module.cg.businessobject.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.web.struts.form.LookupForm;
import org.kuali.kfs.kns.web.ui.Column;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.ResultRow;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.GlobalVariables;
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
import org.kuali.rice.kim.api.permission.Permission;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.module.cg.businessobject.CuAward;

@SuppressWarnings("deprecation")
public class CuAwardLookupableHelperServiceImpl extends AwardLookupableHelperServiceImpl {
    private static final long serialVersionUID = 1372707190594999024L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAwardLookupableHelperServiceImpl.class);
    
    protected DocumentHelperService documentHelperService;
    protected PermissionService permissionService;
    
    @Override
    public List<HtmlData> getCustomActionUrls(BusinessObject businessObject, List pkNames) {
        LOG.info("getCustomActionUrls, entering");
        List<HtmlData> anchorHtmlDataList = new ArrayList<HtmlData>();
        
        if (canInitAward()) {
            anchorHtmlDataList.add(getUrlData(businessObject, KRADConstants.MAINTENANCE_EDIT_METHOD_TO_CALL, pkNames));
            if (allowsMaintenanceNewOrCopyAction()) {
                anchorHtmlDataList.add(getUrlData(businessObject, KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL, pkNames));
            }
        }

        if (canViewInvoiceLink() && getAccountsReceivableModuleBillingService().isContractsGrantsBillingEnhancementActive()) {
            AnchorHtmlData invoiceUrl = getInvoicesLookupUrl(businessObject);
            anchorHtmlDataList.add(invoiceUrl);
        }

        return anchorHtmlDataList;
    }
    
    public Set<String> getConditionallyHiddenPropertyNames() {
        LOG.info("getConditionallyHiddenPropertyNames, entering");
        Set<String> properties = super.getConditionallyHiddenPropertyNames();
        return properties;
    }
    
    @Override
    public Collection<? extends BusinessObject> performLookup(LookupForm lookupForm, Collection<ResultRow> resultTable, boolean bounded) {
        LOG.info("performLookup, entering");
        Collection<? extends BusinessObject> results = super.performLookup(lookupForm, resultTable, bounded);
        if (canViewInvoiceLink() && !canInitAward()) {
            resultTable.stream().forEach(this::resetInvoiceColumn);
        }
        return results;
    }
    
    private void resetInvoiceColumn(ResultRow row) {
        Column invoiceColumn = row.getColumns().get(0);
        String link = parseHrefLinkFromAnchorTag(row.getActionUrls());
        AnchorHtmlData anchorHtmlData = new AnchorHtmlData(link, KFSConstants.SEARCH_METHOD, "View Invoices");
        anchorHtmlData.setTarget(KFSConstants.NEW_WINDOW_URL_TARGET);
        invoiceColumn.setColumnAnchor(anchorHtmlData);
        invoiceColumn.setPropertyValue("View Invoices");
    }
    
    protected String parseHrefLinkFromAnchorTag(String anchorTag) {
        String link = StringUtils.substringBetween(anchorTag, "href=\"", "\"");
        return link;
    }

    private boolean canViewInvoiceLink() {
        Document document = null;
        Map<String, String> permissionDetails = new HashMap<String, String>();
        permissionDetails.put("documentTypeName", "CINV");
        boolean canOpenInvoices = permissionService.hasPermissionByTemplate(GlobalVariables.getUserSession().getPrincipalId(), "KR-NS", "Open Document", permissionDetails);
        LOG.info("canViewInvoiceLink: " + canOpenInvoices);
        return canOpenInvoices;
    }
    
    private boolean canInitAward() {
        boolean canInitAward = documentHelperService.getDocumentAuthorizer("AWRD").canInitiate("AWRD", GlobalVariables.getUserSession().getPerson());
        LOG.info("canInitAward: " + canInitAward);
        return canInitAward;
    }

    public void setDocumentHelperService(DocumentHelperService documentHelperService) {
        this.documentHelperService = documentHelperService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
    
}
