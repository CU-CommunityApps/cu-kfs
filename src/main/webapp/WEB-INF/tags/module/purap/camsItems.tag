<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2023 Kuali, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>
<%@ attribute name="itemAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="camsItemAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="camsSystemAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="camsAssetAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="camsLocationAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="isRequisition" required="false" description="Determines if this is a requisition document"%>
<%@ attribute name="isPurchaseOrder" required="false" description="Determines if this is a requisition document"%>
<!--  KFSPTS-1792 : allow FO to edit REQ capital asset tab add 'enableCa' in purCams.tag-->

<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT] && (empty KualiForm.editingMode['restrictFullEntry'])}" />
<c:set var="tabindexOverrideBase" value="60" />
<c:set var="availabilityOnce" value="${PurapConstants.CapitalAssetAvailability.ONCE}"/>
<c:set var="colSpan" value="10"/>

<h3>Capital Asset Items</h3>
<table class="standard side-margins acct-lines" summary="Capital Asset Items">
	<tr class="header">
		<th></th>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemTypeCode}" hideRequiredAsterisk="true"/>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemQuantity}"/>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitOfMeasureCode}" />
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemCatalogNumber}" />
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.purchasingCommodityCode}" nowrap="true" />
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemDescription}"/>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitPrice}" nowrap="true" addClass="right"/>
		<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.extendedPrice}" nowrap="true" addClass="right" />
		<c:if test="${isRequisition}">
			<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemRestrictedIndicator}" nowrap="true" addClass="center" />
			<c:set var="colSpan" value="${colSpan + 1}"/>
		</c:if>
		<th>Actions</th>
	</tr>

<logic:iterate indexId="ctr" name="KualiForm" property="document.purchasingCapitalAssetItems" id="itemLine">
	<tr class="line ${ctr == 0 ? 'first' : ''}">
        <th class="infoline" rowspan="2" valign="middle" align="middle">
        	${KualiForm.document.purchasingCapitalAssetItems[ctr].purchasingItem.itemLineNumber}
        </th>
		<td class="infoline">
			${KualiForm.document.purchasingCapitalAssetItems[ctr].purchasingItem.itemType.itemTypeDescription}
	    </td>
		<td class="infoline">
		    ${KualiForm.document.purchasingCapitalAssetItems[ctr].purchasingItem.itemQuantity}
	    </td>
		<td class="infoline">
		    ${KualiForm.document.purchasingCapitalAssetItems[ctr].purchasingItem.itemUnitOfMeasureCode}
	    </td>
		<td class="infoline">
		    ${KualiForm.document.purchasingCapitalAssetItems[ctr].purchasingItem.itemCatalogNumber}
	    </td>
        <td class="infoline">
            ${KualiForm.document.purchasingCapitalAssetItems[ctr].purchasingItem.commodityCode.commodityDescription}
		</td>
		<td class="infoline">
		   ${KualiForm.document.purchasingCapitalAssetItems[ctr].purchasingItem.itemDescription}
	    </td>
		<td class="infoline">
		    <div align="right">
		        ${KualiForm.document.purchasingCapitalAssetItems[ctr].purchasingItem.itemUnitPrice}
			</div>
		</td>
		<td class="infoline">
			<div align="right">
				${KualiForm.document.purchasingCapitalAssetItems[ctr].purchasingItem.extendedPrice}
			</div>
		</td>
		<c:if test="${isRequisition}">
            <td class="infoline">
                <div align="center">
                    <kul:htmlControlAttribute attributeEntry="${itemAttributes.itemRestrictedIndicator}" property="document.purchasingCapitalAssetItems[${ctr}].purchasingItem.itemRestrictedIndicator" readOnly="true" />
                </div>
            </td>
		</c:if>

        <!-- Cams Tab -->
        <c:set var="currentTabIndex" value="${KualiForm.currentTabIndex}" scope="request" />
        <c:set var="topLevelTabIndex" value="${KualiForm.currentTabIndex}" scope="request" />
        <c:set var="tabTitle" value="CamsLines-${currentTabIndex}" />
        <c:set var="tabKey" value="${kfunc:generateTabKey(tabTitle)}"/>
        <!--  hit form method to increment tab index -->
        <c:set var="dummyIncrementer" value="${kfunc:incrementTabIndex(KualiForm, tabKey)}" />
        <c:set var="currentTab" value="${kfunc:getTabState(KualiForm, tabKey)}"/>

        <%-- default to closed --%>
        <c:choose>
            <c:when test="${empty currentTab}">
                <c:set var="isOpen" value="true" />
            </c:when>
            <c:when test="${!empty currentTab}">
                <c:set var="isOpen" value="${currentTab == 'OPEN'}" />
            </c:when>
        </c:choose>

		<th>
			<div>
				<c:if test="${isOpen == 'true' || isOpen == 'TRUE'}">
					<html:submit
							property="methodToCall.toggleTab.tab${tabKey}"
							alt="hide"
							title="Hide Capital Asset"
							styleClass="btn btn-default small"
							styleId="tab-${tabKey}-imageToggle"
							onclick="javascript: return toggleTab(document, 'kualiFormModal', '${tabKey}');"
							value="Hide Capital Asset"/>
				</c:if>
				<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
					<html:submit
							property="methodToCall.toggleTab.tab${tabKey}"
							alt="Show"
							title="Show Capital Asset"
							styleClass="btn btn-default small"
							styleId="tab-${tabKey}-imageToggle"
							onclick="javascript: return toggleTab(document, 'kualiFormModal', '${tabKey}');"
							value="Show Capital Asset"/>
				</c:if>
			</div>
		</th>
	</tr>

    <c:if test="${not isPurchaseOrder}">
    	<c:set var="itemActive" value="true"/>
    </c:if>

	<c:if test="${isPurchaseOrder}">
    	<c:set var="itemActive" value="${KualiForm.document.purchasingCapitalAssetItems[ctr].purchasingItem.itemActiveIndicator}"/>
    </c:if>

	<tr style="border-bottom:1px solid #BBBBBB;">
	<td class="infoline" valign="middle" colspan="10">
		<table border="0" cellspacing="0" cellpadding="0" width="100%">
		<tr>

	    </tr>

		<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
			<tr style="display: none;"  id="tab-${tabKey}-div">
		</c:if>
			<th colspan="${colSpan}">
				<table class="standard acct-lines">
			    	<tr>
						<th class="right">
						   <kul:htmlAttributeLabel attributeEntry="${camsItemAttributes.capitalAssetTransactionTypeCode}" />
		                </th>
				        <td class="datacell">
				            <c:choose>
							    <c:when test="${!empty KualiForm.document.purchasingCapitalAssetItems and ( (KualiForm.purchasingItemCapitalAssetAvailability eq availabilityOnce) or (KualiForm.purchasingCapitalAssetSystemCommentsAvailability eq availabilityOnce) or (KualiForm.purchasingCapitalAssetSystemDescriptionAvailability eq availabilityOnce) or (KualiForm.purchasingCapitalAssetSystemAvailability eq availabilityOnce) )}">
								    <kul:htmlControlAttribute attributeEntry="${camsItemAttributes.capitalAssetTransactionTypeCode}"
									    property="document.purchasingCapitalAssetItems[${ctr}].capitalAssetTransactionTypeCode"
									    extraReadOnlyProperty="document.purchasingCapitalAssetItems[${ctr}].capitalAssetTransactionType.capitalAssetTransactionTypeDescription"
									    readOnly="${!itemActive or !(fullEntryMode or amendmentEntry or enableCa)}"
									    tabindexOverride="${tabindexOverrideBase + 9}"/>
							    </c:when>
							    <c:otherwise>
								    <kul:htmlControlAttribute attributeEntry="${camsItemAttributes.capitalAssetTransactionTypeCode}"
                                        property="document.purchasingCapitalAssetItems[${ctr}].capitalAssetTransactionTypeCode"
                                        extraReadOnlyProperty="document.purchasingCapitalAssetItems[${ctr}].capitalAssetTransactionType.capitalAssetTransactionTypeDescription"
                                        readOnly="${!itemActive or !(fullEntryMode or amendmentEntry or enableCa)}"
                                        tabindexOverride="${tabindexOverrideBase + 0}"/>
                                </c:otherwise>
                            </c:choose>
    					</td>
	    			</tr>
				    <purap:camsDetail ctr="${ctr}" camsItemIndex="${ctr}" camsSystemAttributes="${camsSystemAttributes}" camsAssetAttributes="${camsAssetAttributes}" camsLocationAttributes="${camsLocationAttributes}" camsAssetSystemProperty="document.purchasingCapitalAssetItems[${ctr}].purchasingCapitalAssetSystem" availability="${PurapConstants.CapitalAssetAvailability.EACH}" isRequisition="${isRequisition}" isPurchaseOrder="${isPurchaseOrder}" poItemInactive="${not itemActive}"/>
				</table>
	        </th>
		<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
		    </tr>
		</c:if>

		</table>
	</td>
	</tr>
</logic:iterate>
</table>
