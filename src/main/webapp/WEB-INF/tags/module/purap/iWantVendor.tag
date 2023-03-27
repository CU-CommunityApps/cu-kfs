<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>
<%@ attribute name="wizard" required="false" %>

<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}"/>
<c:set var="tabindexOverrideBase" value="30"/>

<div class="tab-container">
    <h3>Vendor Info</h3>
    <table class="standard side-margins">
        <tr>
            <td height="30" colspan="2" class="neutral"
                style="color: blue; font-family:Verdana, Verdana, serif; font-size: 12px;font-style: italic">
                <b>NOTE:</b> Please use Vendor Lookup to select. Or provide Vendor name, email address, and mailing address.
            </td>
        </tr>
    </table>
    <table cellpadding="0" cellspacing="0" class="datatable" summary="Vendor Section">
        <tr>
            <th class="right">
                <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorName}"/>
            </th>
            <td align="left" valign="middle" width="70%" class="neutral">
                <kul:htmlControlAttribute
                        attributeEntry="${documentAttributes.vendorName}"
                        property="document.vendorName"
                        readOnly="${not fullEntryMode}"
                        tabindexOverride="${tabindexOverrideBase + 0}"/>
                <c:if test="${fullEntryMode && (not empty KualiForm.editingMode['iwntUseLookups'])}">
                    <kul:lookup boClassName="edu.cornell.kfs.module.purap.businessobject.IWantDocVendorDetail"
                                lookupParameters="'Y':activeIndicator, 'PO':vendorHeader.vendorTypeCode"
                                fieldConversions="vendorName:document.vendorName,vendorHeaderGeneratedIdentifier:document.vendorHeaderGeneratedIdentifier,vendorDetailAssignedIdentifier:document.vendorDetailAssignedIdentifier,defaultAddressLine1:document.vendorLine1Address,defaultAddressLine2:document.vendorLine2Address,defaultAddressCity:document.vendorCityName,defaultAddressStateCode:document.vendorStateCode,defaultAddressPostalCode:document.vendorPostalCode,defaultAddressCountryCode:document.vendorCountryCode,defaultFaxNumber:document.vendorFaxNumber,vendorUrlAddress:document.vendorWebURL"/>
                </c:if>
            </td>
        </tr>
        <tr>
            <th class="right">
                <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorDescription}"/>
            </th>
            <td align="left" valign="middle" width="70%" class="neutral">
                <kul:htmlControlAttribute
                        attributeEntry="${documentAttributes.vendorDescription}"
                        property="document.vendorDescription"
                        readOnly="${not fullEntryMode}"
                        tabindexOverride="${tabindexOverrideBase + 0}"/>
            </td>
        </tr>
    </table>
</div>