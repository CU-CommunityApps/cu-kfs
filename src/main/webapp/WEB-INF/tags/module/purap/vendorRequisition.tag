<%--
 Copyright 2006-2009 The Kuali Foundation
 
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl2.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>


<c:set var="fullEntryMode"
       value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT] && (empty KualiForm.editingMode['restrictFiscalEntry'])}"/>
<c:set var="vendorReadOnly" value="${(not empty KualiForm.editingMode['lockVendorEntry'])}"/>
<c:set var="amendmentEntry" value="${(not empty KualiForm.editingMode['amendmentEntry'])}"/>
<c:set var="lockB2BEntry" value="${(not empty KualiForm.editingMode['lockB2BEntry'])}"/>
<c:set var="editPreExtract" value="${(not empty KualiForm.editingMode['editPreExtract'])}"/>
<c:set var="currentUserCampusCode" value="${UserSession.person.campusCode}"/>
<c:set var="tabindexOverrideBase" value="30"/>

<!-- this is a temporary workaround until release 3, where this is fixed more generally -->
<c:set var="fullDocEntryCompleted" value="${(not empty KualiForm.editingMode['fullDocumentEntryCompleted'])}"/>


<c:if test="${not empty KualiForm.document.vendorContractGeneratedIdentifier}">
    <c:set var="extraPrefix" value="document.vendorContract"/>
</c:if>
<c:if test="${empty KualiForm.document.vendorContractGeneratedIdentifier}">
    <c:set var="extraPrefix" value="document.vendorDetail"/>
</c:if>


<c:if test="${not empty KualiForm.document.vendorContractGeneratedIdentifier}">
    <c:set var="extraPrefixShippingTitle" value="document.vendorContract"/>
</c:if>
<c:if test="${empty KualiForm.document.vendorContractGeneratedIdentifier}">
    <c:set var="extraPrefixShippingTitle" value="document.vendorDetail"/>
</c:if>


<kul:tab tabTitle="Vendor" defaultOpen="true" tabErrorKey="${PurapConstants.VENDOR_ERRORS}">
<div class="tab-container" align=center>

<table cellpadding="0" cellspacing="0" class="datatable" summary="Vendor Section">
<tr>
    <td colspan="4" class="subhead">Vendor Address</td>
</tr>

<tr>
    <th align=right valign=middle class="bord-l-b" width="25%">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorName}"/></div>
    </th>
    <td align=left valign=middle class="datacell" width="25%">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorName}" property="document.vendorName"
                readOnly="${not (fullEntryMode or amendmentEntry) or vendorReadOnly or lockB2BEntry}"
                tabindexOverride="${tabindexOverrideBase + 0}"/>
        <c:if test="${(fullEntryMode or amendmentEntry) and !lockB2BEntry}">
            <kul:lookup boClassName="org.kuali.kfs.vnd.businessobject.VendorDetail"
                        lookupParameters="'Y':activeIndicator, 'PO':vendorHeader.vendorTypeCode"
                        fieldConversions="vendorHeaderGeneratedIdentifier:document.vendorHeaderGeneratedIdentifier,vendorDetailAssignedIdentifier:document.vendorDetailAssignedIdentifier,defaultAddressLine1:document.vendorLine1Address,defaultAddressLine2:document.vendorLine2Address,defaultAddressCity:document.vendorCityName,defaultAddressPostalCode:document.vendorPostalCode,defaultAddressStateCode:document.vendorStateCode,defaultAddressInternationalProvince:document.vendorAddressInternationalProvinceName,defaultAddressCountryCode:document.vendorCountryCode"/>

            &nbsp;<html:image property="methodToCall.clearVendor"
                              src="${ConfigProperties.externalizable.images.url}tinybutton-clearvendor.gif"
                              alt="clear vendor" styleClass="tinybutton"/>

        </c:if>
        <c:if test="${KualiForm.document.hasB2BVendor}">
            &nbsp;&nbsp;<portal:portalLink displayTitle="true" title="Shop Catalogs"
                                           url="b2b.do?methodToCall=shopCatalogs"/>
        </c:if>
    </td>
    <th align=right valign=middle class="bord-l-b" width="25%">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorCityName}"/></div>
    </th>
    <td align=left valign=middle class="datacell" width="25%">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorCityName}" property="document.vendorCityName"
                readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or lockB2BEntry}"
                tabindexOverride="${tabindexOverrideBase + 3}"/>
    </td>
</tr>

<tr>
    <th align=right valign=middle class="bord-l-b">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorNumber}"/></div>
    </th>
    <td align=left valign=middle class="datacell">
        <c:if test="${not empty KualiForm.document.vendorHeaderGeneratedIdentifier}">
            <kul:inquiry boClassName="org.kuali.kfs.vnd.businessobject.VendorDetail"
                         keyValues="vendorHeaderGeneratedIdentifier=${KualiForm.document.vendorHeaderGeneratedIdentifier}&vendorDetailAssignedIdentifier=${KualiForm.document.vendorDetailAssignedIdentifier}"
                         render="true">
                <kul:htmlControlAttribute
                        attributeEntry="${documentAttributes.vendorNumber}"
                        property="document.vendorDetail.vendorNumber"
                        readOnly="true" tabindexOverride="${tabindexOverrideBase + 0}"/>
            </kul:inquiry>
        </c:if>
    </td>
    <th align=right valign=middle class="bord-l-b">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorStateCode}"/></div>
    </th>
    <td align=left valign=middle class="datacell">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorStateCode}" property="document.vendorStateCode"
                readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or lockB2BEntry}"
                tabindexOverride="${tabindexOverrideBase + 3}"/>
    </td>
</tr>

<tr>
    <th align=right valign=middle class="bord-l-b">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorLine1Address}"/></div>
    </th>
    <td align=left valign=middle class="datacell">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorLine1Address}" property="document.vendorLine1Address"
                readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry)  or lockB2BEntry}"
                tabindexOverride="${tabindexOverrideBase + 0}"/>
        <c:if test="${(fullEntryMode or amendmentEntry) and vendorReadOnly and !lockB2BEntry}">
            <kul:lookup boClassName="org.kuali.kfs.vnd.businessobject.VendorAddress"
                        readOnlyFields="active, vendorHeaderGeneratedIdentifier,vendorDetailAssignedIdentifier"
                        autoSearch="yes"
                        lookupParameters="'Y':active,document.vendorHeaderGeneratedIdentifier:vendorHeaderGeneratedIdentifier,document.vendorDetailAssignedIdentifier:vendorDetailAssignedIdentifier"
                        fieldConversions="vendorAddressGeneratedIdentifier:document.vendorAddressGeneratedIdentifier"/>
        </c:if>
    </td>
    <th align=right valign=middle class="bord-l-b">
        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorAddressInternationalProvinceName}"/>
    </th>
    <td align=left valign=middle class="datacell">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorAddressInternationalProvinceName}"
                property="document.vendorAddressInternationalProvinceName"
                readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or lockB2BEntry}"
                tabindexOverride="${tabindexOverrideBase + 3}"/>
    </td>
</tr>

<tr>

    <th align=right valign=middle class="bord-l-b">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorLine2Address}"/></div>
    </th>
    <td align=left valign=middle class="datacell">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorLine2Address}" property="document.vendorLine2Address"
                readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or lockB2BEntry}"
                tabindexOverride="${tabindexOverrideBase + 0}"/>
    </td>

    <th align=right valign=middle class="bord-l-b">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorPostalCode}"/></div>
    </th>
    <td align=left valign=middle class="datacell">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorPostalCode}" property="document.vendorPostalCode"
                readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or lockB2BEntry}"
                tabindexOverride="${tabindexOverrideBase + 3}"/>
    </td>
</tr>

<tr>
    <th align=right valign=middle class="bord-l-b">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorAttentionName}"/></div>
    </th>
    <td align=left valign=middle class="datacell">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorAttentionName}" property="document.vendorAttentionName"
                readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or lockB2BEntry }"
                tabindexOverride="${tabindexOverrideBase + 0}"/>
    </td>

    <th align=right valign=middle class="bord-l-b">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorCountryCode}"/></div>
    </th>
    <td align=left valign=middle class="datacell">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorCountryCode}" property="document.vendorCountryCode"
                extraReadOnlyProperty="document.vendorCountry.postalCountryName"
                readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or lockB2BEntry }"
                tabindexOverride="${tabindexOverrideBase + 3}"/>
    </td>
</tr>

<tr>
    <td colspan="4" class="subhead">Vendor Info</td>
</tr>


<tr>
    <th align=right valign=middle class="bord-l-b" rowspan="4">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorCustomerNumber}"/></div>
    </th>

    <td align=left valign=middle class="datacell" rowspan="4">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorCustomerNumber}" property="document.vendorCustomerNumber"
                readOnly="${not (fullEntryMode or amendmentEntry) or lockB2BEntry}"
                tabindexOverride="${tabindexOverrideBase + 4}"/>
        <c:if test="${(fullEntryMode or amendmentEntry) and vendorReadOnly and !lockB2BEntry}">
            <kul:lookup boClassName="org.kuali.kfs.vnd.businessobject.VendorCustomerNumber"
                        readOnlyFields="vendorHeaderGeneratedIdentifier,vendorDetailAssignedIdentifier" autoSearch="yes"
                        lookupParameters="document.vendorHeaderGeneratedIdentifier:vendorHeaderGeneratedIdentifier,document.vendorDetailAssignedIdentifier:vendorDetailAssignedIdentifier"
                        fieldConversions="vendorCustomerNumber:document.vendorCustomerNumber"/>
        </c:if>
    </td>


</tr>

<tr>


    <th align=right valign=middle class="bord-l-b">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorPaymentTermsCode}"/></div>
    </th>
    <td align=left valign=middle class="datacell">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorPaymentTermsCode}" property="document.vendorPaymentTermsCode"
                extraReadOnlyProperty="${extraPrefix}.vendorPaymentTerms.vendorPaymentTermsDescription"
	            readOnly="${not (fullEntryMode or amendmentEntry) or displayRequisitionFields}" tabindexOverride="${tabindexOverrideBase + 6}"/>
    </td>


</tr>

<tr>


    <th align=right valign=middle class="bord-l-b">
        <div align="right"><kul:htmlAttributeLabel
                attributeEntry="${documentAttributes.vendorShippingTitleCode}"/></div>
    </th>
    <td align=left valign=middle class="datacell">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorShippingTitleCode}"
                property="document.vendorShippingTitleCode"
                extraReadOnlyProperty="${extraPrefixShippingTitle}.vendorShippingTitle.vendorShippingTitleDescription"
	            readOnly="${not (fullEntryMode or amendmentEntry) or not displayPurchaseOrderFields}" tabindexOverride="${tabindexOverrideBase + 6}"/>
    </td>


</tr>

<tr>


    <th align=right valign=middle class="bord-l-b">
        <div align="right"><kul:htmlAttributeLabel
                attributeEntry="${documentAttributes.vendorShippingPaymentTermsCode}"/></div>
    </th>
    <td align=left valign=middle class="datacell">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorShippingPaymentTermsCode}"
                property="document.vendorShippingPaymentTermsCode"
                extraReadOnlyProperty="${extraPrefix}.vendorShippingPaymentTerms.vendorShippingPaymentTermsDescription"
		        readOnly="${not (fullEntryMode or amendmentEntry) or not displayPurchaseOrderFields}" tabindexOverride="${tabindexOverrideBase + 6}"/>
    </td>


</tr>


<tr>
    <th align=right valign=middle class="bord-l-b">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorContractName}"/></div>
    </th>
    <td align=left valign=middle class="datacell">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorContractName}" property="document.vendorContractName"
                readOnly="true" tabindexOverride="${tabindexOverrideBase + 4}"/>
        <c:if test="${(fullEntryMode or amendmentEntry) and !lockB2BEntry}">
            <kul:lookup boClassName="org.kuali.kfs.vnd.businessobject.VendorContract"
                        autoSearch="yes" readOnlyFields="vendorCampusCode"
                        lookupParameters="'${currentUserCampusCode}':vendorCampusCode"
                        fieldConversions="vendorContractGeneratedIdentifier:document.vendorContractGeneratedIdentifier"/>
        </c:if>
    </td>
    <th align=right valign=middle class="bord-l-b" rowspan="1">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorContactsLabel}"/></div>
    </th>
    <td align=left valign=middle class="datacell" rowspan="1">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorContactsLabel}" property="document.vendorContactsLabel"
                readOnly="true" tabindexOverride="${tabindexOverrideBase + 6}"/>
        <c:if test="${vendorReadOnly}">
            <kul:lookup boClassName="org.kuali.kfs.vnd.businessobject.VendorContact"
                        readOnlyFields="vendorHeaderGeneratedIdentifier,vendorDetailAssignedIdentifier" autoSearch="yes"
                        lookupParameters="document.vendorHeaderGeneratedIdentifier:vendorHeaderGeneratedIdentifier,document.vendorDetailAssignedIdentifier:vendorDetailAssignedIdentifier"
                        hideReturnLink="true"
                        extraButtonSource="${ConfigProperties.externalizable.images.url}buttonsmall_return.gif"/>
        </c:if>
    </td>
</tr>

<tr>
    <th align=right valign=middle class="bord-l-b">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorPhoneNumber}"/></div>
    </th>
    <td align=left valign=middle class="datacell">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorPhoneNumber}" property="document.vendorPhoneNumber"
                readOnly="true" tabindexOverride="${tabindexOverrideBase + 4}"/>
        <c:if test="${vendorReadOnly}">
            <kul:lookup boClassName="org.kuali.kfs.vnd.businessobject.VendorPhoneNumber"
                        readOnlyFields="vendorHeaderGeneratedIdentifier,vendorDetailAssignedIdentifier" autoSearch="yes"
                        lookupParameters="document.vendorHeaderGeneratedIdentifier:vendorHeaderGeneratedIdentifier,document.vendorDetailAssignedIdentifier:vendorDetailAssignedIdentifier"
                        hideReturnLink="true"
                        extraButtonSource="${ConfigProperties.externalizable.images.url}buttonsmall_return.gif"/>
        </c:if>
    </td>
    <th align=right valign=middle class="bord-l-b" rowspan="2">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.supplierDiversityLabel}"/></div>
    </th>
    <td align=left valign=middle class="datacell" rowspan="2">
        <c:if test="${not empty KualiForm.document.vendorDetail.vendorHeader.vendorSupplierDiversities}">
            <c:forEach var="item" items="${KualiForm.document.vendorDetail.vendorHeader.vendorSupplierDiversities}"
                       varStatus="status">
                <c:if test="${!(status.first)}"><br></c:if>${item.vendorSupplierDiversity.vendorSupplierDiversityDescription}
            </c:forEach>
        </c:if>&nbsp;
    </td>
</tr>
<tr>
    <th align=right valign=middle class="bord-l-b">
        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorFaxNumber}"/></div>
    </th>
    <td align=left valign=middle class="datacell">
        <kul:htmlControlAttribute
                attributeEntry="${documentAttributes.vendorFaxNumber}" property="document.vendorFaxNumber"
                readOnly="${not (fullEntryMode or amendmentEntry) or lockB2BEntry}"
                tabindexOverride="${tabindexOverrideBase + 4}"/>
    </td>
</tr>


</table>

<c:if test="${!lockB2BEntry}">
    <h3>Additional Suggested Vendor Names</h3>

    <table cellpadding="0" cellspacing="0" class="datatable" summary="Additional Vendor Section">
        <tr>
            <th align=right valign=middle class="bord-l-b" width="25%">
                <div align="right"><kul:htmlAttributeLabel
                        attributeEntry="${documentAttributes.alternate1VendorName}"/></div>
            </th>
            <td align=left valign=middle class="datacell">
                <kul:htmlControlAttribute
                        attributeEntry="${documentAttributes.alternate1VendorName}"
                        property="document.alternate1VendorName"
                        readOnly="${not (fullEntryMode or amendmentEntry)}"
                        tabindexOverride="${tabindexOverrideBase + 8}"/>
            </td>
        </tr>
        <tr>
            <th align=right valign=middle class="bord-l-b">
                <div align="right"><kul:htmlAttributeLabel
                        attributeEntry="${documentAttributes.alternate2VendorName}"/></div>
            </th>
            <td align=left valign=middle class="datacell">
                <kul:htmlControlAttribute
                        attributeEntry="${documentAttributes.alternate2VendorName}"
                        property="document.alternate2VendorName"
                        readOnly="${not (fullEntryMode or amendmentEntry)}"
                        tabindexOverride="${tabindexOverrideBase + 8}"/>
            </td>
        </tr>
        <tr>
            <th align=right valign=middle class="bord-l-b">
                <div align="right"><kul:htmlAttributeLabel
                        attributeEntry="${documentAttributes.alternate3VendorName}"/></div>
            </th>
            <td align=left valign=middle class="datacell">
                <kul:htmlControlAttribute
                        attributeEntry="${documentAttributes.alternate3VendorName}"
                        property="document.alternate3VendorName"
                        readOnly="${not (fullEntryMode or amendmentEntry)}"
                        tabindexOverride="${tabindexOverrideBase + 8}"/>
            </td>
        </tr>
        <tr>
            <th align=right valign=middle class="bord-l-b">
                <div align="right"><kul:htmlAttributeLabel
                        attributeEntry="${documentAttributes.alternate4VendorName}"/></div>
            </th>
            <td align=left valign=middle class="datacell">
                <kul:htmlControlAttribute
                        attributeEntry="${documentAttributes.alternate4VendorName}"
                        property="document.alternate4VendorName"
                        readOnly="${not (fullEntryMode or amendmentEntry)}"
                        tabindexOverride="${tabindexOverrideBase + 8}"/>
            </td>
        </tr>
        <tr>
            <th align=right valign=middle class="bord-l-b">
                <div align="right"><kul:htmlAttributeLabel
                        attributeEntry="${documentAttributes.alternate5VendorName}"/></div>
            </th>
            <td align=left valign=middle class="datacell">
                <kul:htmlControlAttribute
                        attributeEntry="${documentAttributes.alternate5VendorName}"
                        property="document.alternate5VendorName"
                        readOnly="${not (fullEntryMode or amendmentEntry)}"
                        tabindexOverride="${tabindexOverrideBase + 8}"/>
            </td>
        </tr>
    </table>
</c:if>

</div>
</kul:tab>

