<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ tag import="org.kuali.kfs.sys.util.Guid" %>
<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="itemAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>

<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}"/>
<c:set var="hasItems" value="${fn:length(KualiForm.document.items) > 0}"/>
<c:set var="hasLineItems" value="${fn:length(KualiForm.document.items) > 0}"/>
<c:set var="nbrOfItems" value="${fn:length(KualiForm.document.items)}"/>
<c:set var="accountsNbr" value="${fn:length(KualiForm.document.accounts)}"/>
<c:set var="tabindexOverrideBase" value="50"/>
<c:set var="mainColumnCount" value="9"/>
<c:set var="colSpanDescription" value="2"/>

<div class="tab-container" style="overflow:auto;">
    <table class="standard side-margins acct-lines" summary="Items Section">
        <c:if test="${fullEntryMode}">
            <tr class="title">
                <th style="visibility: hidden;"></th>
                <td colspan="${mainColumnCount - 1}">
                    <h2>
                        Add Item
                    </h2>
                </td>
            </tr>
            <tr class="header top">
                <th></th>
                <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemDescription}" colspan="${colSpanDescription}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemQuantity}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitOfMeasureCode}" useShortLabel="true"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemCatalogNumber}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitPrice}"/>
                <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.totalAmount}"/>
                <kul:htmlAttributeHeaderCell literalLabel="Action"/>
            </tr>
            <tr class="top new">
                <td class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.itemLineNumber}"
                            property="newIWantItemLine.itemLineNumber"
                            readOnly="true"/>
                </td>
                <td class="infoline relative wrap-break-word" colspan="${colSpanDescription}">
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.itemDescription}"
                            property="newIWantItemLine.itemDescription"
                            tabindexOverride="${tabindexOverrideBase + 0}"
                            styleClass="fullwidth"/>
                </td>
                <td class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.itemQuantity}"
                            property="newIWantItemLine.itemQuantity"
                            tabindexOverride="${tabindexOverrideBase + 0}"
                            readOnly="${not fullEntryMode}"
                            onchange="updateNewItemTotal()"/>
                </td>
                <td class="infoline nowrap">
                    <c:set var="itemUnitOfMeasureCodeField" value="newIWantItemLine.itemUnitOfMeasureCode"/>
                    <c:set var="itemUnitOfMeasureDescriptionField"
                           value="newIWantItemLine.itemUnitOfMeasure.itemUnitOfMeasureDescription"/>
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.itemUnitOfMeasureCode}"
                            property="${itemUnitOfMeasureCodeField}"
                            readOnly="${not fullEntryMode}"
                            onblur="loadItemUnitOfMeasureInfo( '${itemUnitOfMeasureCodeField}', '${itemUnitOfMeasureDescriptionField}' );${onblur}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                    <kul:lookup
                            boClassName="edu.cornell.kfs.module.purap.businessobject.IWantDocUnitOfMeasure"
                            fieldConversions="itemUnitOfMeasureCode:newIWantItemLine.itemUnitOfMeasureCode"
                            lookupParameters="'Y':active"
                            addClass="embed"/>
                    <div id="newIWantItemLine.itemUnitOfMeasure.itemUnitOfMeasureDescription.div" class="fineprint">
                        <html:hidden write="true" property="${itemUnitOfMeasureDescriptionField}"/>
                    </div>
                </td>
                <td class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.itemCatalogNumber}"
                            property="newIWantItemLine.itemCatalogNumber"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <td class="infoline right">
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.itemUnitPrice}"
                            property="newIWantItemLine.itemUnitPrice"
                            tabindexOverride="${tabindexOverrideBase + 0}"
                            readOnly="${not fullEntryMode}"
                            onchange="updateNewItemTotal()"/>
                </td>
                <td class="infoline right">
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.totalAmount}"
                            property="newIWantItemLine.totalAmount"
                            readOnly="true"/>
                </td>
                <td class="infoline">
                    <div class="actions">
                        <html:html-button
                                property="methodToCall.addItem"
                                alt="Add an Item"
                                title="Add an Item"
                                styleClass="btn btn-green skinny"
                                tabindex="${tabindexOverrideBase + 0}"
                                value="Add"
                                innerHTML="<span class=\"fa fa-plus\"></span>"/>
                    </div>
                </td>
            </tr>
        </c:if>
        <tr class="title">
            <th style="visibility: hidden;"></th>
            <td colspan="${mainColumnCount}" style="padding-top: 20px;">
                <h2>Current Items</h2>
            </td>
        </tr>
        <c:choose>
            <c:when test="${hasLineItems or hasItems}">
                <tr class="header">
                    <th></th>
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemDescription}" colspan="${colSpanDescription}"/>
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemQuantity}"/>
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitOfMeasureCode}"/>
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemCatalogNumber}"/>
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitPrice}"/>
                    <kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.totalAmount}"/>
                    <kul:htmlAttributeHeaderCell literalLabel="Actions"/>
                </tr>
            </c:when>
            <c:otherwise>
                <tr>
                    <th height="30" colspan="${mainColumnCount}" class="neutral">No items added</th>
                </tr>
            </c:otherwise>
        </c:choose>
        <logic:iterate indexId="ctr" name="KualiForm" property="document.items" id="itemLine">
            <c:choose>
                <c:when test="${itemLine.objectId == null}">
                    <c:set var="newObjectId" value="<%= (new Guid()).toString()%>"/>
                    <c:set var="tabKey" value="Item-${newObjectId}"/>
                </c:when>
                <c:otherwise>
                    <c:set var="tabKey" value="Item-${itemLine.objectId}"/>
                </c:otherwise>
            </c:choose>
            <c:set var="currentTab" value="${kfunc:getTabState(KualiForm, tabKey)}"/>
            <c:choose>
                <c:when test="${empty currentTab}">
                    <c:set var="isOpen" value="true"/>
                </c:when>
                <c:otherwise>
                    <c:set var="isOpen" value="${currentTab == 'OPEN'}"/>
                </c:otherwise>
            </c:choose>
            <c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
                <tbody style="display: none;" id="tab-${tabKey}-div">
            </c:if>
            <tr class="top line">
                <th class="infoline" nowrap="nowrap" style="position: relative;">
                    <bean:write name="KualiForm" property="document.item[${ctr}].itemLineNumber"/>
                </th>
                <td class="infoline relative wrap-break-word" colspan="2">
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.itemDescription}"
                            property="document.item[${ctr}].itemDescription"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"
                            styleClass="fullwidth"/>
                </td>
                <td class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.itemQuantity}"
                            property="document.item[${ctr}].itemQuantity"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"
                            onchange="updateItemsTotal('document.totalDollarAmount', 'document.accountingLinesTotal', '${ nbrOfItems}', '${accountsNbr }', 'document.item[${ctr}].totalAmount', '${ctr}')"/>
                </td>
                <td class="infoline nowrap">
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.itemUnitOfMeasureCode}"
                            property="document.item[${ctr}].itemUnitOfMeasureCode"
                            onblur="loadItemUnitOfMeasureInfo( 'document.item[${ctr}].itemUnitOfMeasureCode', 'document.item[${ctr}].itemUnitOfMeasure.itemUnitOfMeasureDescription' );${onblur}"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                    <div id="document.item[${ctr}].itemUnitOfMeasure.itemUnitOfMeasureDescription.div" class="fineprint">
                        <html:hidden write="true" property="document.item[${ctr}].itemUnitOfMeasure.itemUnitOfMeasureDescription"/>&nbsp;
                    </div>
                </td>
                <td class="infoline">
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.itemCatalogNumber}"
                            property="document.item[${ctr}].itemCatalogNumber"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <td class="infoline right">
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.itemUnitPrice}"
                            property="document.item[${ctr}].itemUnitPrice"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <td class="infoline right">
                    <kul:htmlControlAttribute
                            attributeEntry="${itemAttributes.totalAmount}"
                            property="document.item[${ctr}].totalAmount"
                            readOnly="${true}"/>
                </td>
                <td valign="center" class="neutral">
                    <div class="actions">
                        <c:choose>
                            <c:when test="${fullEntryMode}">
                                <html:html-button
                                        property="methodToCall.deleteItem.line${ctr}"
                                        alt="Delete Item ${ctr+1}"
                                        title="Delete Item ${ctr+1}"
                                        styleClass="btn clean"
                                        value="Delete"
                                        innerHTML="<span class=\"fa fa-trash\"></span>"/>
                            </c:when>
                            <c:otherwise>
                                <div align="center">&nbsp;</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </td>
            </tr>
            <c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
                </tbody>
            </c:if>
        </logic:iterate>
    </table>
</div>

<div class="tab-container">
    <h3>Totals</h3>
    <table class="standard" summary="Totals Section">
        <c:set var="colSpanTotalLabel" value="${mainColumnCount-2}"/>
        <tr>
            <th class="right" width="62%" colspan="${colSpanTotalLabel}" scope="row">
                <kul:htmlAttributeLabel attributeEntry="${DataDictionary.IWantDocument.attributes.totalDollarAmount}"/>
            </th>
            <td valign="middle" class="datacell right heavy" width="150px">
                <kul:htmlControlAttribute
                        attributeEntry="${DataDictionary.IWantDocument.attributes.totalDollarAmount}"
                        property="document.totalDollarAmount"
                        readOnly="true"/>
            </td>
            <td class="datacell">&nbsp;</td>
        </tr>
    </table>
</div>