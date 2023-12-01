package edu.cornell.kfs.module.cg.businessobject.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.web.struts.form.LookupForm;
import org.kuali.kfs.kns.web.ui.Column;
import org.kuali.kfs.kns.web.ui.ResultRow;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.cg.CGConstants;
import org.kuali.kfs.module.cg.businessobject.lookup.AwardLookupableHelperServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.krad.bo.BusinessObject;

import edu.cornell.kfs.sys.CUKFSConstants;

@SuppressWarnings("deprecation")
public class CuAwardLookupableHelperServiceImpl extends AwardLookupableHelperServiceImpl {
    private static final long serialVersionUID = 1372707190594999024L;
    private static final Logger LOG = LogManager.getLogger(CuAwardLookupableHelperServiceImpl.class);
    
    private static final String VIEW_INVOICES_LINK_VALUE = "View Invoices";
    private static final String INVOICE_LINK_COLUMN_NAME = "invoiceLink";
    
    protected DocumentHelperService documentHelperService;
    protected PermissionService permissionService;
    
    @Override
    public List<HtmlData> getCustomActionUrls(final BusinessObject businessObject, final List pkNames) {
        LOG.debug("getCustomActionUrls, entering");
        final List<HtmlData> anchorHtmlDataList = new ArrayList<>();
        
        if (canInitAward() && allowsMaintenanceEditAction(businessObject)) {
            anchorHtmlDataList.add(getUrlData(businessObject, KRADConstants.MAINTENANCE_EDIT_METHOD_TO_CALL, pkNames));
            if (allowsMaintenanceNewOrCopyAction()) {
                anchorHtmlDataList.add(getUrlData(businessObject, KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL, pkNames));
            }
        }

        if (canViewInvoiceLink() && getAccountsReceivableModuleBillingService().isContractsGrantsBillingEnhancementActive()) {
            final AnchorHtmlData invoiceUrl = getInvoicesLookupUrl(businessObject);
            anchorHtmlDataList.add(invoiceUrl);
        }

        return anchorHtmlDataList;
    }
    
    @Override
    public List<Column> getColumns() {
        final List<Column> columns = super.getColumns();

        if (canInitAward() || !canViewInvoiceLink() || !getAccountsReceivableModuleBillingService().isContractsGrantsBillingEnhancementActive()) {
            LOG.debug("getColumns, remove invoices column");
            for (final Iterator<Column> it = columns.iterator(); it.hasNext(); ) {
                final Column column = it.next();
                if (StringUtils.equalsIgnoreCase(column.getPropertyName(), INVOICE_LINK_COLUMN_NAME)) {
                    it.remove();
                }
            }
        }

        return columns;
    }
    
    @Override
    public Collection<? extends BusinessObject> performLookup(
            final LookupForm lookupForm, Collection<ResultRow> resultTable, final boolean bounded) {
        LOG.debug("performLookup, entering");
        final Collection<? extends BusinessObject> results = super.performLookup(lookupForm, resultTable, bounded);
        if (canViewInvoiceLink() && !canInitAward()) {
            resultTable.stream().forEach(this::findAndResetInvoiceColumn);
        }
        return results;
    }
    
    protected void findAndResetInvoiceColumn(final ResultRow row) {
        row.getColumns().stream()
            .filter(col -> StringUtils.equalsIgnoreCase(col.getPropertyName(), INVOICE_LINK_COLUMN_NAME))
            .findFirst()
            .ifPresent(col -> resetInvoiceColumn(row, col));
    }

    protected void resetInvoiceColumn(final ResultRow row, final Column invoiceColumn) {
        final String hrefLink = parseHrefLinkFromAnchorTag(row.getActionUrls());
        if (StringUtils.isNotBlank(hrefLink)) {
            final AnchorHtmlData anchorHtmlData = new AnchorHtmlData(hrefLink, KFSConstants.SEARCH_METHOD, VIEW_INVOICES_LINK_VALUE);
            anchorHtmlData.setTarget(KFSConstants.NEW_WINDOW_URL_TARGET);
            invoiceColumn.setColumnAnchor(anchorHtmlData);
            invoiceColumn.setPropertyValue(VIEW_INVOICES_LINK_VALUE);
        }
    }
    
    protected String parseHrefLinkFromAnchorTag(final String anchorTag) {
        if (StringUtils.isNotBlank(anchorTag)) {
            final String link = StringUtils.substringBetween(anchorTag, "href=\"", CUKFSConstants.DOUBLE_QUOTE);
            return link;
        } else {
            return StringUtils.EMPTY;
        }
    }

    private boolean canViewInvoiceLink() {
        final Map<String, String> permissionDetails = new HashMap<String, String>();
        permissionDetails.put(KFSPropertyConstants.DOCUMENT_TYPE_NAME, ArConstants.ArDocumentTypeCodes.CONTRACTS_GRANTS_INVOICE);
        final boolean canOpenInvoices = permissionService.hasPermissionByTemplate(GlobalVariables.getUserSession().getPrincipalId(), KFSConstants.CoreModuleNamespaces.KFS, 
                KimConstants.PermissionTemplateNames.OPEN_DOCUMENT, permissionDetails);
        LOG.debug("canViewInvoiceLink: " + canOpenInvoices);
        return canOpenInvoices;
    }
    
    private boolean canInitAward() {
        final boolean canInitAward = documentHelperService.getDocumentAuthorizer(CGConstants.AWARD).canInitiate(CGConstants.AWARD, GlobalVariables.getUserSession().getPerson());
        LOG.debug("canInitAward: " + canInitAward);
        return canInitAward;
    }

    public void setDocumentHelperService(final DocumentHelperService documentHelperService) {
        this.documentHelperService = documentHelperService;
    }

    public void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }
    
}
