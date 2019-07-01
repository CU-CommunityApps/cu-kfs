/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.document.web.renderers;

import edu.cornell.kfs.sys.businessobject.options.FavoriteAccountValuesFinder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.taglib.html.HiddenTag;
import org.kuali.kfs.kns.web.taglib.html.KNSFileTag;
import org.kuali.kfs.kns.web.taglib.html.KNSSubmitTag;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocumentBase;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.datadictionary.AccountingLineGroupDefinition;
import org.kuali.kfs.sys.document.datadictionary.AccountingLineViewActionDefinition;
import org.kuali.kfs.sys.document.web.AccountingLineViewAction;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.KeyValue;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders the standard group header/import line.
 */
public class GroupTitleLineRenderer implements Renderer, CellCountCurious {

    private int titleCellSpan = 4;
    private int cellCount = 1;
    private AccountingLineGroupDefinition accountingLineGroupDefinition;
    private AccountingDocument accountingDocument;
    private String lineCollectionProperty;
    private KNSFileTag scriptFileTag = new KNSFileTag();
    private KNSFileTag noscriptFileTag = new KNSFileTag();
    private KNSSubmitTag uploadButtonTag = new KNSSubmitTag();
    private KNSSubmitTag showHideTag = new KNSSubmitTag();
    private HiddenTag hideStateTag = new HiddenTag();
    private KNSSubmitTag cancelButtonTag = new KNSSubmitTag();
    private boolean shouldUpload = true;
    private boolean canEdit = false;

    private boolean groupActionsRendered = false;

    private boolean hideDetails;
    // KFSPTS-985
    private String riceImageBase;

    /**
     * Constructs a ImportLineRenderer, setting defaults on the tags that will always exist.
     */
    public GroupTitleLineRenderer() {
        scriptFileTag.setSize("30");
        noscriptFileTag.setSize("30");
        uploadButtonTag.setStyleClass("btn btn-green");
        uploadButtonTag.setValue("Add");
        cancelButtonTag.setProperty("methodToCall.cancel");
        cancelButtonTag.setStyleClass("btn btn-default");
        cancelButtonTag.setValue("Cancel");

        showHideTag.setStyleClass("btn btn-default uppercase");
        hideStateTag.setName("KualiForm");
        hideStateTag.setProperty("hideDetails");
    }

    public void clear() {
        cellCount = 1;
        accountingLineGroupDefinition = null;
        titleCellSpan = 4;
        lineCollectionProperty = null;
        accountingDocument = null;
        shouldUpload = true;
        canEdit = false;

        // clean script file tag
        scriptFileTag.setPageContext(null);
        scriptFileTag.setParent(null);
        scriptFileTag.setProperty(null);

        // clean noscript file tag
        noscriptFileTag.setPageContext(null);
        noscriptFileTag.setParent(null);
        noscriptFileTag.setProperty(null);

        // clean upload button tag
        uploadButtonTag.setPageContext(null);
        uploadButtonTag.setParent(null);
        uploadButtonTag.setProperty(null);
        uploadButtonTag.setAlt(null);
        uploadButtonTag.setTitle(null);

        // clean cancel import tag
        cancelButtonTag.setPageContext(null);
        cancelButtonTag.setParent(null);
        cancelButtonTag.setAlt(null);
        cancelButtonTag.setTitle(null);
        cancelButtonTag.setOnclick(null);

        showHideTag.setPageContext(null);
        showHideTag.setParent(null);
        showHideTag.setProperty(null);
        showHideTag.setAlt(null);
        showHideTag.setTitle(null);
        showHideTag.setValue(null);

        hideStateTag.setPageContext(null);
        hideStateTag.setParent(null);

        hideDetails = false;
    }

    public void render(PageContext pageContext, Tag parentTag) throws JspException {
        try {
            //KFSPTS-985 : add favorite account selection
            // setdistribution does not have accountPrefix
            String accountPrefix = (String)pageContext.getAttribute("accountPrefix");
            FavoriteAccountValuesFinder accounts = new FavoriteAccountValuesFinder();
            if (canEdit && isDocumentIntegratedFavoriteAccount() && CollectionUtils.isNotEmpty(accounts.getKeyValues()) && accounts.getKeyValues().size() > 1) {
                pageContext.getOut().write(buildFavoriteAccounts(accountPrefix));
            }

            String rowClass = null;
            if (lineCollectionProperty.equals(KFSConstants.ACCOUNT_DISTRIBUTION_SRC_LINES)) {
                rowClass = "distribution";
            }
            pageContext.getOut().write(buildRowBeginning(rowClass));

            pageContext.getOut().write(buildBlankCell());
            pageContext.getOut().write(buildTitleCell());
            this.renderGroupLevelActions(pageContext, parentTag);

            pageContext.getOut().write(buildRowEnding());
        }
        catch (IOException ioe) {
            throw new JspException("Difficulty in rendering import/group header line", ioe);
        }
    }

    /**
     * Builds a tag for the row beginning.
     *
     * @return the String with the HTML for the row opening
     */
    protected String buildRowBeginning(String additionalClass) {
        StringBuilder rowBeginning = new StringBuilder();
        rowBeginning.append("<tr class=\"title");
        if (StringUtils.isNotBlank(additionalClass)) {
            rowBeginning.append(" ");
            rowBeginning.append(additionalClass);
        }
        rowBeginning.append("\">");
        return rowBeginning.toString();
    }

    /**
     * Builds the tag for the row beginning.
     *
     * @return the String with the HTML for the row beginning
     */
    protected String buildRowEnding() {
        return "</tr>";
    }

    protected void renderGroupLevelActions(PageContext pageContext, Tag parentTag) throws JspException {
        JspWriter out = pageContext.getOut();

        try {
            out.write(this.buildGroupActionsBeginning());

            this.renderGroupActions(pageContext, parentTag);

            this.renderHideDetails(pageContext, parentTag);

            this.renderUploadCell(pageContext, parentTag);

            out.write(this.buildGroupActionsColumnEnding());
        }
        catch (IOException ioe) {
            throw new JspException("Difficulty rendering group level actions", ioe);
        }
    }

    /**
     * Builds a tag for the row beginning.
     *
     * @return the String with the HTML for the row opening
     */
    protected String buildGroupActionsBeginning() {
        StringBuilder groupActionsBeginning = new StringBuilder();
        final int width = cellCount - titleCellSpan;

        groupActionsBeginning.append("<td ");
        groupActionsBeginning.append("colspan=\"");
        groupActionsBeginning.append(Integer.toString(width));
        groupActionsBeginning.append("\" ");

        groupActionsBeginning.append("class=\"tab-subhead-import\" ");
        groupActionsBeginning.append("align=\"right\" ");
        groupActionsBeginning.append("nowrap=\"nowrap\" ");
        groupActionsBeginning.append("style=\"border-right: none;\"");
        groupActionsBeginning.append(">");

        return groupActionsBeginning.toString();
    }

    /**
     * Builds the tag for the row beginning.
     *
     * @return the String with the HTML for the row beginning
     */
    protected String buildGroupActionsColumnEnding() {
        return this.canUpload() || this.isGroupActionsRendered() ? "</td>" : StringUtils.EMPTY;
    }

    /**
     * Builds the tags for the title cell of the import line.
     *
     * @return the String with the HTML for the title cell
     */
    protected String buildTitleCell() throws JspException{
        StringBuilder titleCell = new StringBuilder();
        int colSpan = titleCellSpan;

        // subtract one for the blank cell before the title
        if (colSpan > 0) {
            colSpan--;
        }

        titleCell.append("<td ");

        titleCell.append("colspan=\"");
        titleCell.append(colSpan);
        titleCell.append("\" ");

        titleCell.append("class=\"tab-subhead\" ");

        titleCell.append("style=\"border-right: none;\"");

        titleCell.append(">");

        titleCell.append("<h2>");

        titleCell.append(buildGroupAnchor());

        titleCell.append(accountingLineGroupDefinition.getGroupLabel());

        titleCell.append("</h2>");

        titleCell.append("</td>");

        return titleCell.toString();
    }

    protected String buildBlankCell() throws JspException{
        StringBuilder titleCell = new StringBuilder();
        titleCell.append("<th style=\"visibility:hidden;\"></th>");
        return titleCell.toString();
    }

    /**
     * @return the unique anchor for this group.
     */
    protected String buildGroupAnchor() {
        return "<a name=\"accounting" + getGroupInfix() + "Anchor\"></a>";
    }

    protected void renderGroupActions(PageContext pageContext, Tag parentTag) throws JspException {
        List<? extends AccountingLineViewActionDefinition> accountingLineGroupActions = accountingLineGroupDefinition.
                getAccountingLineGroupActions();
        if (!this.isGroupActionsRendered() || accountingLineGroupActions == null || accountingLineGroupActions.
                isEmpty()) {
            return;
        }

        List<AccountingLineViewAction> viewActions = new ArrayList<>();
        for (AccountingLineViewActionDefinition action : accountingLineGroupActions) {
            String actionMethod = action.getActionMethod();
            String actionLabel = action.getActionLabel();

            AccountingLineViewAction viewAction = new AccountingLineViewAction(actionMethod, actionLabel, 
                    action.getButtonStyle(), action.getButtonLabel(), action.getButtonIcon());
            viewActions.add(viewAction);
        }

        if (!viewActions.isEmpty()) {
            ActionsRenderer actionsRenderer = new ActionsRenderer();
            actionsRenderer.setActions(viewActions);
            actionsRenderer.render(pageContext, parentTag);
            actionsRenderer.clear();
        }
    }

    /**
     * A dumb way to get the group infix that tries to figure out if it's dealing with a source or target line.
     *
     * @return the String "source" or "target" to populate the buildGroupAnchor
     */
    protected String getGroupInfix() {
        Class accountingLineClass = accountingLineGroupDefinition.getAccountingLineClass();
        return (accountingLineClass.isAssignableFrom(SourceAccountingLine.class) ? "source" : "target");
    }

    /**
     * Renders the show/hide button
     *
     * @param pageContext the page context to render to
     * @param parentTag the tag requesting all this rendering
     * @throws JspException thrown under terrible circumstances when the rendering failed and had to be left behind like 
     *                      so much refuse
     */
    protected void renderHideDetails(PageContext pageContext, Tag parentTag) throws JspException {
        hideStateTag.setPageContext(pageContext);
        hideStateTag.setParent(parentTag);

        hideStateTag.doStartTag();
        hideStateTag.doEndTag();

        String toggle = hideDetails ? "show" : "hide";
        String displayToggle = hideDetails ? "Show" : "Hide";

        showHideTag.setPageContext(pageContext);
        showHideTag.setParent(parentTag);
        showHideTag.setProperty("methodToCall."+toggle+"Details");
        showHideTag.setStyleClass("btn btn-default uppercase");
        showHideTag.setAlt(toggle+" transaction details");
        showHideTag.setTitle(toggle+" transaction details");
        showHideTag.setValue(displayToggle + " Details");

        showHideTag.doStartTag();
        showHideTag.doEndTag();
    }

    /**
     * Oy, the big one...this one actually renders instead of returning the HTML in a String. This is because it's kind 
     * of complex(and a likely target for future refactoring)
     *
     * @param pageContext the page context to render to
     * @param parentTag the tag that is requesting all the rendering
     * @throws JspException thrown if something goes wrong
     */
    protected void renderUploadCell(PageContext pageContext, Tag parentTag) throws JspException {
        JspWriter out = pageContext.getOut();

        if (canUpload()) {
            try {
                String hideImport = getHideImportName();
                String showImport = getShowImportName();
                String showLink = getShowLinkName();
                String uploadDiv = getUploadDivName();

                out.write("\n<SCRIPT type=\"text/javascript\">\n");
                out.write("<!--\n");
                out.write("\tfunction " + hideImport + "(showLinkId, uploadDivId) {\n");
                out.write("\t\tdocument.getElementById(showLinkId).style.display=\"inline\";\n");
                out.write("\t\tdocument.getElementById(uploadDivId).style.display=\"none\";\n");
                out.write("\t}\n");
                out.write("\tfunction " + showImport + "(showLinkId, uploadDivId) {\n");
                out.write("\t\tdocument.getElementById(showLinkId).style.display=\"none\";\n");
                out.write("\t\tdocument.getElementById(uploadDivId).style.display=\"inline\";\n");
                out.write("\t}\n");
                out.write("\tdocument.write(\n");
                out.write(
                        "\t\t'<a class=\"btn btn-default uppercase\" id=\"" + showLink + "\" href=\"#\" onclick=\""
                                + showImport + "(\\\'" + showLink + "\\\',\\\'" + uploadDiv
                                + "\\\');return false;\">Import Lines</a>'+\n");
                out.write("\t\t'<div class=\"uploadDiv\" id=\"" + uploadDiv + "\" style=\"display:none;\" >' +\n");

                out.write("\t\t'");

                scriptFileTag.setPageContext(pageContext);
                scriptFileTag.setParent(parentTag);
                String index = StringUtils.substringBetween(getLineCollectionProperty(), "[", "]");
                if (StringUtils.isNotBlank(index) && getLineCollectionProperty().contains("transactionEntries")) {
                    scriptFileTag.setProperty(StringUtils
                            .substringBeforeLast(getLineCollectionProperty(), ".") + "." + accountingLineGroupDefinition
                            .getImportedLinePropertyPrefix() + "File");
                }
                else {
                    scriptFileTag.setProperty(accountingLineGroupDefinition.getImportedLinePropertyPrefix() + "File");
                }
                scriptFileTag.doStartTag();
                scriptFileTag.doEndTag();

                out.write("' +\n");
                out.write("\t\t'");

                uploadButtonTag.setPageContext(pageContext);
                uploadButtonTag.setParent(parentTag);
                uploadButtonTag.setProperty("methodToCall.upload" + StringUtils.capitalize(accountingLineGroupDefinition
                        .getImportedLinePropertyPrefix()) + "Lines" + "." + getLineCollectionProperty());
                uploadButtonTag.setAlt("insert " + accountingLineGroupDefinition.getGroupLabel() + " accounting lines");
                uploadButtonTag.setTitle(
                        "insert " + accountingLineGroupDefinition.getGroupLabel() + " accounting lines");
                uploadButtonTag.doStartTag();
                uploadButtonTag.doEndTag();

                out.write("' +\n");

                out.write("\t\t'");

                cancelButtonTag.setPageContext(pageContext);
                cancelButtonTag.setParent(parentTag);
                cancelButtonTag.setAlt(
                        "Cancel import of " + accountingLineGroupDefinition.getGroupLabel() + " accounting lines");
                cancelButtonTag.setTitle(
                        "Cancel import of " + accountingLineGroupDefinition.getGroupLabel() + " accounting lines");
                cancelButtonTag.setOnclick(
                        getHideImportName() + "(\\\'" + showLink + "\\\',\\\'" + uploadDiv + "\\\');return false;");
                cancelButtonTag.doStartTag();
                cancelButtonTag.doEndTag();

                out.write("' +\n");

                out.write("\t'</div>');\n");
                out.write("\t//-->\n");
                out.write("</SCRIPT>\n");
                out.write("<NOSCRIPT>\n");
                out.write("\tImport " + accountingLineGroupDefinition.getGroupLabel() + " lines\n");

                noscriptFileTag.setPageContext(pageContext);
                noscriptFileTag.setParent(parentTag);
                noscriptFileTag.setProperty(accountingLineGroupDefinition.getImportedLinePropertyPrefix() + "File");
                noscriptFileTag.doStartTag();
                noscriptFileTag.doEndTag();

                uploadButtonTag.doStartTag();
                uploadButtonTag.doEndTag();

                out.write("</NOSCRIPT>\n");
            }
            catch (IOException ioe) {
                throw new JspException("Difficulty rendering accounting lines import upload", ioe);
            }
        }
    }

    /**
     * @return the name of the line collection property, but in a form that is okay for javascript variable/function 
     * naming
     */
    protected String getVariableFriendlyLineCollectionProperty() {
        return lineCollectionProperty.replaceAll("[^A-Za-z0-9]", "_");
    }

    /**
     * @return the name of the hide import function.
     */
    protected String getHideImportName() {
        return "hide" + getVariableFriendlyLineCollectionProperty() + "Import";
    }

    /**
     * @return the name of the show import function.
     */
    protected String getShowImportName() {
        return "show" + getVariableFriendlyLineCollectionProperty() + "Import";
    }

    /**
     * @return the name of the show link element.
     */
    protected String getShowLinkName() {
        return lineCollectionProperty + "ShowLink";
    }

    /**
     * @return the name of the upload div.
     */
    protected String getUploadDivName() {
        return "upload" + lineCollectionProperty + "Div";
    }

    /**
     * @return true if upload can proceed for the accounting line group, false otherwise.
     */
    protected boolean canUpload() {
        return (canEdit && accountingDocument.getAccountingLineParser() != null && shouldUpload);
    }

    /**
     * Allows overriding of whether something can be uploaded - though this serves only to turn uploading more off, 
     * never more on
     *
     * @param allowUpload should we be allowed to upload?
     */
    public void overrideCanUpload(boolean allowUpload) {
        this.shouldUpload = allowUpload;
    }

    /**
     * @return the cellCount attribute.
     */
    public int getCellCount() {
        return cellCount;
    }

    /**
     * @param cellCount The cellCount value to set.
     */
    public void setCellCount(int cellCount) {
        this.cellCount = cellCount;
    }

    /**
     * @return the accountingDocument attribute.
     */
    public AccountingDocument getAccountingDocument() {
        return accountingDocument;
    }

    /**
     * @param accountingDocument The accountingDocument to set.
     */
    public void setAccountingDocument(AccountingDocument accountingDocument) {
        this.accountingDocument = accountingDocument;
    }

    /**
     * @return the accountingLineGroupDefinition attribute.
     */
    public AccountingLineGroupDefinition getAccountingLineGroupDefinition() {
        return accountingLineGroupDefinition;
    }

    /**
     * @param accountingLineGroupDefinition The accountingLineGroupDefinition to set.
     */
    public void setAccountingLineGroupDefinition(AccountingLineGroupDefinition accountingLineGroupDefinition) {
        this.accountingLineGroupDefinition = accountingLineGroupDefinition;
    }

    /**
     * @return the titleCellSpan attribute.
     */
    public int getTitleCellSpan() {
        return titleCellSpan;
    }

    /**
     * @param titleCellSpan The titleCellSpan value to set.
     */
    public void setTitleCellSpan(int titleCellSpan) {
        this.titleCellSpan = titleCellSpan;
    }

    /**
     * @return the lineCollectionProperty attribute.
     */
    public String getLineCollectionProperty() {
        return lineCollectionProperty;
    }

    /**
     * @param lineCollectionProperty The lineCollectionProperty value to set.
     */
    public void setLineCollectionProperty(String lineCollectionProperty) {
        this.lineCollectionProperty = lineCollectionProperty;
    }

    /**
     * @return the groupActionsRendered attribute.
     */
    public boolean isGroupActionsRendered() {
        return groupActionsRendered;
    }

    /**
     * @param groupActionsRenderred The groupActionsRendered value to set.
     */
    public void setGroupActionsRendered(boolean groupActionsRenderred) {
        this.groupActionsRendered = groupActionsRenderred;
    }

    /**
     * @param canEdit The canEdit value to set.
     */
    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    /**
     * @return the hideDetails attribute.
     */
    public boolean getHideDetails() {
        return hideDetails;
    }

    /**
     * @param hideDetails The hideDetails value to set.
     */
    public void setHideDetails(boolean hideDetails) {
        this.hideDetails = hideDetails;
    }

    // Begin KFSPTS-985, KFSUPGRADE-75 customization
    public void setRiceImageBase(String riceImageBase) {
        this.riceImageBase = riceImageBase;
    }

    private boolean isDocumentIntegratedFavoriteAccount() {
        return 	getAccountingDocument() instanceof RequisitionDocument || getAccountingDocument() instanceof PurchaseOrderAmendmentDocument || getAccountingDocument() instanceof PurchaseOrderDocument;
    }

    private String buildFavoriteAccounts(String accountPrefix) {
        int itemIdx = getItemIdx(accountPrefix);
        Integer accountLineId = null;
        if (isDocumentIntegratedFavoriteAccount()) {
            if (itemIdx >= 0) {
                accountLineId = ((PurchasingItemBase)((PurchasingDocumentBase)this.getAccountingDocument()).getItem(itemIdx)).getFavoriteAccountLineIdentifier();
            } else {
                accountLineId = ((PurchasingDocumentBase)this.getAccountingDocument()).getFavoriteAccountLineIdentifier();
            }
        }
        StringBuffer favoriteAccountLine = new StringBuffer();
        favoriteAccountLine.append("<tr>");

        favoriteAccountLine.append("<th colspan=\"2\" align=\"right\" valign=\"middle\" class=\"bord-l-b\">");
        favoriteAccountLine.append("<div  align=\"right\">");
        favoriteAccountLine.append("Favorite Account");
        favoriteAccountLine.append("</div>");
        favoriteAccountLine.append("</th>");

        if (StringUtils.isBlank(accountPrefix)) {
            accountPrefix = "document.";
        }
        favoriteAccountLine.append("<td colspan=\"7\" class=\"infoline\">");
        favoriteAccountLine.append("<select name=\"").append(accountPrefix).append("favoriteAccountLineIdentifier\" id=\"").append(accountPrefix).append("favoriteAccountLineIdentifier\" title=\"* Favorite Account\">");
        FavoriteAccountValuesFinder accounts = new FavoriteAccountValuesFinder();
        for (KeyValue keyValue : (List<KeyValue>)accounts.getKeyValues()) {
            favoriteAccountLine.append("<option value=\"").append(keyValue.getKey());
            if (checkToAddError(accountPrefix + "favoriteAccountLineIdentifier")) {
                favoriteAccountLine.append(getSelected(accountLineId, keyValue.getKey()));
            } else {
                favoriteAccountLine.append("\" >");
            }
            favoriteAccountLine.append(keyValue.getValue());
            favoriteAccountLine.append("</option>\n");
        }
        favoriteAccountLine.append("</select>");
        if (checkToAddError(accountPrefix + "favoriteAccountLineIdentifier")) {
            favoriteAccountLine.append(getErrorIconImageTag());
        }
        favoriteAccountLine.append("</td>");

        favoriteAccountLine.append("<td valign=\"top\" class=\"infoline\">");
        favoriteAccountLine.append("<div style=\"text-align: center;\">");
        favoriteAccountLine.append("<input type=\"image\" name=\"methodToCall.addFavoriteAccount.line").append(itemIdx).append(".anchorFavoriteAnchor\" src=\"kr/static/images/tinybutton-add1.gif\" tabindex=\"0\" class=\"tinybutton\"");
        favoriteAccountLine.append(" title=\"Add Favorite  Accounting Line\" alt=\"Add Favorite Accounting Line\">");
        favoriteAccountLine.append("</input>");
        favoriteAccountLine.append("<br></div></td>");

        favoriteAccountLine.append("</tr>");

        return favoriteAccountLine.toString();
    }

    private String getSelected(Object propertyValue, Object keyValue) {
        if (propertyValue != null) {
            if (propertyValue.toString().equalsIgnoreCase(keyValue.toString())) {
                return "\" selected=\"selected\" >";
            }
        }
        return "\" >";

    }

    private boolean checkToAddError(String errorKey) {
        if (CollectionUtils.isNotEmpty(GlobalVariables.getMessageMap().getErrorMessagesForProperty(errorKey))) {
            return true;
        }
        return false;
    }

    protected String getErrorIconImageTag() {
        return "<img src=\""+getErrorIconImageSrc()+"\" alt=\"error\" />";
    }

    private String getErrorIconImageSrc() {
        return getRiceImageBase()+"errormark.gif";
    }

    private String getRiceImageBase() {
        if (riceImageBase == null) {
            riceImageBase = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KRADConstants.EXTERNALIZABLE_IMAGES_URL_KEY);
        }
        return riceImageBase;
    }

    private int getItemIdx(String accountPrefix) {
        if (StringUtils.isNotBlank(accountPrefix)) {
            int startIdx = 	accountPrefix.indexOf("[");
            return Integer.parseInt(accountPrefix.substring(accountPrefix.indexOf("[")+1,accountPrefix.indexOf("]")));
        } else {
            // set distribution.
            return -2;
        }
    }
    // End KFSPTS-985, KFSUPGRADE-75 customization

}
