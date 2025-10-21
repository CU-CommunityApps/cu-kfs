<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2024 Kuali, Inc.

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

<%@ attribute name="documentAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields." %>
<%@ attribute name="displayRequisitionFields" required="false" description="Boolean to indicate if REQ specific fields should be displayed" %>
<%@ attribute name="displayPurchaseOrderFields" required="false" description="Boolean to indicate if PO specific fields should be displayed" %>
<%@ attribute name="displayPaymentRequestFields" required="false" description="Boolean to indicate if PREQ specific fields should be displayed" %>
<%@ attribute name="displayCreditMemoFields" required="false" description="Boolean to indicate if CM specific fields should be displayed" %>
<%@ attribute name="purchaseOrderAwarded" required="false" description="Boolean to indicate if this is a PO that has been awarded" %>

<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT] && (empty KualiForm.editingMode['restrictFullEntry'])}" />
<c:set var="canEditVendor" value="${KualiForm.documentActions[PurapAuthorizationConstants.CAN_EDIT_VENDOR]}" />
<c:set var="vendorReadOnly" value="${(not empty KualiForm.editingMode['lockVendorEntry'])}" />
<c:set var="amendmentEntry" value="${(not empty KualiForm.editingMode['amendmentEntry'])}" />
<c:set var="lockB2BEntry" value="${(not empty KualiForm.editingMode['lockB2BEntry'])}" />
<c:set var="editPreExtract"	value="${(not empty KualiForm.editingMode['editPreExtract'])}" />
<c:set var="currentUserCampusCode" value="${sessionScope['userSession'].person.campusCode}" />
<c:set var="restrictFullEntry" value="${(not empty KualiForm.editingMode['restrictFullEntry'])}" />
<c:set var="tabindexOverrideBase" value="30" />

<!--  this is a temporary workaround until release 3, where this is fixed more generally -->
<c:set var="fullDocEntryCompleted" value="${(not empty KualiForm.editingMode['fullDocumentEntryCompleted'])}" />
<c:set var="readOnlyForPREQ" value="${(displayPaymentRequestFields) and (fullDocEntryCompleted)}" />
<c:set var="achAccountInfoDisplayed" value="${(not empty KualiForm.editingMode['achAccountInfoDisplayed'])}" />

<c:set var="vendorEditable" value="${(fullEntryMode or amendmentEntry) and canEditVendor}" />

<c:choose> 
  <c:when test="${displayPurchaseOrderFields or displayPaymentRequestFields}" > 
    <c:set var="extraPrefix" value="document" />
  </c:when> 
  <c:when test="${displayRequisitionFields}" > 
    <c:if test="${not empty KualiForm.document.vendorContractGeneratedIdentifier}" >
        <c:set var="extraPrefix" value="document.vendorContract" />
    </c:if>
    <c:if test="${empty KualiForm.document.vendorContractGeneratedIdentifier}" >
        <c:set var="extraPrefix" value="document.vendorDetail" />
    </c:if>
  </c:when> 
  <c:otherwise> 
 	<c:set var="extraPrefix" value="document.vendorDetail" />
  </c:otherwise> 
</c:choose>  
<c:choose> 
  <c:when test="${displayPurchaseOrderFields}" > 
    <c:set var="extraPrefixShippingTitle" value="document" />
  </c:when> 
  <c:when test="${displayPaymentRequestFields}" > 
    <c:set var="extraPrefixShippingTitle" value="document.purchaseOrderDocument" />
  </c:when> 
  <c:when test="${displayRequisitionFields}" > 
    <c:if test="${not empty KualiForm.document.vendorContractGeneratedIdentifier}" >
        <c:set var="extraPrefixShippingTitle" value="document.vendorContract" />
    </c:if>
    <c:if test="${empty KualiForm.document.vendorContractGeneratedIdentifier}" >
        <c:set var="extraPrefixShippingTitle" value="document.vendorDetail" />
    </c:if>
  </c:when> 
  <c:otherwise> 
    <c:set var="extraPrefixShippingTitle" value="document.vendorDetail" />
  </c:otherwise> 
</c:choose>  

<kul:tab tabTitle="Vendor" defaultOpen="true" tabErrorKey="${PurapConstants.VENDOR_ERRORS}">
    <div class="tab-container" align=center>
		<h3>Vendor Address</h3>
        <table class="datatable standard" summary="Vendor Section">
            <tr>
                <th class="right top" width="25%">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorName}" />
                </th>
                <td class="datacell" width="25%">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorName}" property="document.vendorName" 
                    	readOnly="${not vendorEditable or vendorReadOnly or displayPaymentRequestFields or displayCreditMemoFields or purchaseOrderAwarded or lockB2BEntry}"/>
                    <c:if test="${vendorEditable and (displayRequisitionFields or displayPurchaseOrderFields) and !purchaseOrderAwarded and !lockB2BEntry}" >
                        <kul:lookup  boClassName="org.kuali.kfs.vnd.businessobject.VendorDetail" 
                        	lookupParameters="'Y':activeIndicator, 'PO':vendorHeader.vendorTypeCode"
                        	fieldConversions="vendorHeaderGeneratedIdentifier:document.vendorHeaderGeneratedIdentifier,vendorDetailAssignedIdentifier:document.vendorDetailAssignedIdentifier,defaultAddressLine1:document.vendorLine1Address,defaultAddressLine2:document.vendorLine2Address,defaultAddressCity:document.vendorCityName,defaultAddressPostalCode:document.vendorPostalCode,defaultAddressStateCode:document.vendorStateCode,defaultAddressInternationalProvince:document.vendorAddressInternationalProvinceName,defaultAddressCountryCode:document.vendorCountryCode"/>
                        <c:if test="${displayRequisitionFields}">
                            <html:submit
                                    property="methodToCall.clearVendor"
                                    alt="clear vendor"
                                    styleClass="btn btn-default small"
                                    value="Clear Vendor"/>
                        </c:if>
                    </c:if>
                </td>
                <th class="right" width="25%">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorCityName}" />
                </th>
                <td class="datacell" width="25%">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorCityName}" property="document.vendorCityName" 
                    	readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or displayCreditMemoFields or (lockB2BEntry and (displayRequisitionFields or displayPurchaseOrderFields))}" tabindexOverride="${tabindexOverrideBase + 3}"/>
                </td>
            </tr>

            <tr>
                <th class="right">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorNumber}" />
                </th>
                <td class="datacell">
	                <c:if test="${not empty KualiForm.document.vendorHeaderGeneratedIdentifier}">
		                <kul:inquiry boClassName="org.kuali.kfs.vnd.businessobject.VendorDetail" keyValues="vendorHeaderGeneratedIdentifier=${KualiForm.document.vendorHeaderGeneratedIdentifier}&vendorDetailAssignedIdentifier=${KualiForm.document.vendorDetailAssignedIdentifier}" render="true">
		                    <kul:htmlControlAttribute 
		                    	attributeEntry="${documentAttributes.vendorNumber}" property="document.vendorDetail.vendorNumber" 
		                    	readOnly="true" tabindexOverride="${tabindexOverrideBase + 0}"/>
		                </kul:inquiry>
	                </c:if>
                </td>
                <th class="right">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorStateCode}" />
                    	<c:if test="${displayPurchaseOrderFields}"><br> *required for US</c:if>
                </th>
                <td class="datacell">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorStateCode}" property="document.vendorStateCode" 
                    	readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or displayCreditMemoFields or (lockB2BEntry and (displayRequisitionFields or displayPurchaseOrderFields))}" tabindexOverride="${tabindexOverrideBase + 3}"/>
                </td>
            </tr>

            <tr>
                <th class="right">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorLine1Address}" />
                </th>
                <td class="datacell nowrap">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorLine1Address}" property="document.vendorLine1Address" 
                    	readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or displayCreditMemoFields or (lockB2BEntry and (displayRequisitionFields or displayPurchaseOrderFields))}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                    <c:if test="${(fullEntryMode or amendmentEntry) and vendorReadOnly and !lockB2BEntry}">
                        <kul:lookup  boClassName="org.kuali.kfs.vnd.businessobject.VendorAddress" 
                        	readOnlyFields="active, vendorHeaderGeneratedIdentifier,vendorDetailAssignedIdentifier" autoSearch="yes"
                        	lookupParameters="'Y':active,document.vendorHeaderGeneratedIdentifier:vendorHeaderGeneratedIdentifier,document.vendorDetailAssignedIdentifier:vendorDetailAssignedIdentifier" 
                        	fieldConversions="vendorAddressGeneratedIdentifier:document.vendorAddressGeneratedIdentifier"/>
                    </c:if>
                </td>
                <th class="right">
					<kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorAddressInternationalProvinceName}" />
                </th>
                <td class="datacell">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorAddressInternationalProvinceName}" property="document.vendorAddressInternationalProvinceName" 
                    	readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or displayCreditMemoFields or (lockB2BEntry and (displayRequisitionFields or displayPurchaseOrderFields))}" tabindexOverride="${tabindexOverrideBase + 3}"/>
                </td>
            </tr>

            <tr>
                <th class="right">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorLine2Address}" />
                </th>
                <td class="datacell">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorLine2Address}" property="document.vendorLine2Address" 
                    	readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or displayCreditMemoFields or (lockB2BEntry and (displayRequisitionFields or displayPurchaseOrderFields))}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <th class="right">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorPostalCode}" />
                    	<c:if test="${displayPurchaseOrderFields}"> <br> *required for US</c:if>
                </th>
				<td class="datacell">
					<kul:htmlControlAttribute 
						attributeEntry="${documentAttributes.vendorPostalCode}" property="document.vendorPostalCode" 
						readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or displayCreditMemoFields or (lockB2BEntry and (displayRequisitionFields or displayPurchaseOrderFields))}" tabindexOverride="${tabindexOverrideBase + 3}"/>
				</td>
            </tr>
            
            <tr>
                <th class="right">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorAttentionName}" />
                </th>
                <td class="datacell">
                   <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorAttentionName}" property="document.vendorAttentionName" 
                    	readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or displayCreditMemoFields or (lockB2BEntry and (displayRequisitionFields or displayPurchaseOrderFields))}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
            	<th class="right">
            		<kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorCountryCode}" />
            	</th>
            	<td class="datacell">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorCountryCode}" property="document.vendorCountryCode" extraReadOnlyProperty="document.vendorCountry.name" 
                    	readOnly="${(readOnlyForPREQ) or not (fullEntryMode or amendmentEntry) or displayCreditMemoFields or (lockB2BEntry and (displayRequisitionFields or displayPurchaseOrderFields))}" tabindexOverride="${tabindexOverrideBase + 3}"/>
            	</td>
            </tr>
            <c:if test="${(not empty KualiForm.document.vendorDetail) and KualiForm.document.vendorDetail.vendorHeader.vendorDebarredIndicator}">
                <tr>
                    <th class="right" >
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.justification}" />
                	</th>
                    <td class="datacell">
                        <kul:htmlControlAttribute
                            	attributeEntry="${documentAttributes.justification}" property="document.justification" tabindexOverride="${tabindexOverrideBase + 0}"/>
            	    </td>
                </tr>
            </c:if>
        </table>

        <h3>Vendor Info</h3>
        <table class="datatable standard" summary="Vendor Info Section">
            <c:if test="${displayPurchaseOrderFields}">
                <tr>
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.purchaseOrderVendorChoiceCode}" />
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.purchaseOrderVendorChoiceCode}" property="document.purchaseOrderVendorChoiceCode" 
                        	extraReadOnlyProperty="document.purchaseOrderVendorChoice.purchaseOrderVendorChoiceDescription"
                        	readOnly="${not (fullEntryMode or amendmentEntry) or lockB2BEntry}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                    <th class="right">&nbsp;</th>
                    <td class="datacell">&nbsp;</td>
                </tr>
            </c:if>

            <tr>
                <th class="right" width="25%">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorCustomerNumber}" />
                </th>
                <td class="datacell nowrap" width="25%">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorCustomerNumber}" property="document.vendorCustomerNumber" 
                    	readOnly="${not (fullEntryMode or amendmentEntry) or displayCreditMemoFields or lockB2BEntry}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    <c:if test="${(fullEntryMode or amendmentEntry) and vendorReadOnly and !lockB2BEntry}">
                        <kul:lookup  boClassName="org.kuali.kfs.vnd.businessobject.VendorCustomerNumber" 
                        	readOnlyFields="vendorHeaderGeneratedIdentifier,vendorDetailAssignedIdentifier" autoSearch="yes"
                        	lookupParameters="document.vendorHeaderGeneratedIdentifier:vendorHeaderGeneratedIdentifier,document.vendorDetailAssignedIdentifier:vendorDetailAssignedIdentifier" 
                        	fieldConversions="vendorCustomerNumber:document.vendorCustomerNumber"/>
                    </c:if>
                </td>

            	<c:choose>                <%-- KFSPTS-1646 --%>
                    <c:when test="${displayRequisitionFields or displayPurchaseOrderFields}">
                        <%-- KFSPTS-1458 --%>
                        <th class="right" width="25%">
                            <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorEmailAddress}" />
                        </th>
                        <td class="datacell nowrap" width="25%">
                            <kul:htmlControlAttribute
                                    attributeEntry="${documentAttributes.vendorEmailAddress}" property="document.vendorEmailAddress"
                                    readOnly="${not (fullEntryMode or amendmentEntry) or lockB2BEntry or not (displayRequisitionFields or displayPurchaseOrderFields)}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                        </td>
                    </c:when>
                    <c:otherwise>
                        <th class="right" width="25%">&nbsp;</th>
                        <td class="datacell" width="25%">&nbsp;</td>
                    </c:otherwise>
            	</c:choose>
            	
            </tr>

            <tr>
                <c:if test="${displayRequisitionFields or displayPurchaseOrderFields}">
                    <th class="right top" rowspan="2">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorNoteText}" />
                    </th>
                    <td class="datacell" rowspan="2">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.vendorNoteText}" property="document.vendorNoteText" 
                        	readOnly="${not (fullEntryMode or amendmentEntry) or lockB2BEntry}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                </c:if>                                                 
                <c:if test="${displayPaymentRequestFields or displayCreditMemoFields}">
                    <th class="right top" rowspan="3">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.noteLine1Text}" />
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.noteLine1Text}" property="document.noteLine1Text" 
                        	readOnly="${not (fullEntryMode or amendmentEntry or editPreExtract)}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                </c:if>
                <c:if test="${not displayCreditMemoFields}">                                                 
	                <th class="right">
	                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorPaymentTermsCode}" />
	                </th>
	                <td class="datacell">
	                    <kul:htmlControlAttribute 
	                    	attributeEntry="${documentAttributes.vendorPaymentTermsCode}" property="document.vendorPaymentTermsCode" 
	                    	extraReadOnlyProperty="${extraPrefix}.vendorPaymentTerms.vendorPaymentTermsDescription"
	                    	readOnly="${not (fullEntryMode or amendmentEntry) or displayRequisitionFields}" tabindexOverride="${tabindexOverrideBase + 6}"/>
	                </td>
				</c:if>	
				<c:if test="${displayCreditMemoFields}">
                    <th class="right">&nbsp;</th>
                    <td class="datacell">&nbsp;</td>
                </c:if>                    
            </tr> 

            <tr>
                <c:if test="${displayPaymentRequestFields or displayCreditMemoFields}">
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.noteLine2Text}" property="document.noteLine2Text" 
                        	readOnly="${not (fullEntryMode or amendmentEntry or editPreExtract)}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                </c:if>                                                 
                <c:if test="${not displayCreditMemoFields}">
	                <th class="right">
	                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorShippingTitleCode}" />
	                </th>	             
	                <td class="datacell">
	                    <kul:htmlControlAttribute 
	                    	attributeEntry="${documentAttributes.vendorShippingTitleCode}" property="document.vendorShippingTitleCode" 
	                    	extraReadOnlyProperty="${extraPrefixShippingTitle}.vendorShippingTitle.vendorShippingTitleDescription"
	                    	readOnly="${not (fullEntryMode or amendmentEntry) or not displayPurchaseOrderFields}" tabindexOverride="${tabindexOverrideBase + 6}"/>
	                </td>		            			            
                </c:if>
                <c:if test="${displayCreditMemoFields}">
                    <th class="right">&nbsp;</th>
                    <td class="datacell">&nbsp;</td>
                </c:if>    
            </tr> 

            <tr>
                <c:if test="${displayPaymentRequestFields or displayCreditMemoFields}">
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.noteLine3Text}" property="document.noteLine3Text" 
                        	readOnly="${not (fullEntryMode or amendmentEntry or editPreExtract)}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                </c:if> 
                <c:if test="${not displayCreditMemoFields}">                                                
	                <th class="right">
	                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorShippingPaymentTermsCode}" />
	                </th>
		            <td class="datacell">
		               <kul:htmlControlAttribute 
		               		attributeEntry="${documentAttributes.vendorShippingPaymentTermsCode}" property="document.vendorShippingPaymentTermsCode" 
							extraReadOnlyProperty="${extraPrefix}.vendorShippingPaymentTerms.vendorShippingPaymentTermsDescription"
		                    readOnly="${not (fullEntryMode or amendmentEntry) or not displayPurchaseOrderFields}" tabindexOverride="${tabindexOverrideBase + 6}"/>
		            </td>
				</c:if>
				<c:if test="${displayCreditMemoFields}">
                    <th class="right">&nbsp;</th>
                    <td class="datacell">&nbsp;</td>
                </c:if>    	                
            </tr> 

            <c:if test="${displayRequisitionFields or displayPurchaseOrderFields}">
                <tr>
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorContractName}" />
                    </th>
                    <td class="datacell nowrap">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.vendorContractName}" property="document.vendorContractName" 
                        	readOnly="true" tabindexOverride="${tabindexOverrideBase + 4}"/>
                        <c:if test="${(fullEntryMode or amendmentEntry) and !lockB2BEntry}">
                            <kul:lookup  boClassName="org.kuali.kfs.vnd.businessobject.VendorContract" 
                            	autoSearch="yes" readOnlyFields="vendorCampusCode" 
                            	lookupParameters="'${currentUserCampusCode}':vendorCampusCode" 
                            	fieldConversions="vendorContractGeneratedIdentifier:document.vendorContractGeneratedIdentifier" />
                        </c:if>
                    </td>
                </tr>            

                <tr>
                    <th class="right" rowspan="2">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.supplierDiversityLabel}" />
                    </th>
                    <td class="datacell" rowspan="2">
                          <c:if test="${not empty KualiForm.document.vendorDetail.vendorHeader.activeVendorSupplierDiversities}">
                              <c:forEach var="item" items="${KualiForm.document.vendorDetail.vendorHeader.activeVendorSupplierDiversities}" varStatus="status">
                                  <c:if test="${!(status.first)}"><br></c:if>${item.vendorSupplierDiversity.vendorSupplierDiversityDescription}
                              </c:forEach>
                          </c:if>
                    </td>
                </tr>
                <tr>
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorFaxNumber}" />
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.vendorFaxNumber}" property="document.vendorFaxNumber" 
                        	readOnly="${not (fullEntryMode or amendmentEntry) or lockB2BEntry}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                </tr>
            </c:if>

            <c:if test="${displayPaymentRequestFields}">
                <tr>
                    <th class="right top" rowspan="3">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.specialHandlingInstructionLine1Text}" />
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.specialHandlingInstructionLine1Text}" property="document.specialHandlingInstructionLine1Text" 
                        	readOnly="${not (fullEntryMode or editPreExtract)}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                    <c:choose>
	            		<c:when test="${achAccountInfoDisplayed}">
	                    	<th class="right">
                                <kul:htmlAttributeLabel
                                        attributeEntry="${documentAttributes.achSignUpStatusFlag}"/>
                            </th>
                    		<td class="datacell">
                                <kul:htmlControlAttribute
                                        property="document.achSignUpStatusFlag"
                                        attributeEntry="${documentAttributes.achSignUpStatusFlag}"
                                        readOnly="true" />
                            </td>
	                    </c:when>
						<c:otherwise>
							<th class="right">&nbsp;</th>
                    		<td class="datacell">&nbsp;</td>
						</c:otherwise>
					</c:choose>
                </tr> 
    
                <tr>
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.specialHandlingInstructionLine2Text}" property="document.specialHandlingInstructionLine2Text" 
                        	readOnly="${not (fullEntryMode or editPreExtract)}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                    <th class="right">&nbsp;</th>
                    <td class="datacell">&nbsp;</td>
                </tr> 
    
                <tr>
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.specialHandlingInstructionLine3Text}" property="document.specialHandlingInstructionLine3Text" 
                        	readOnly="${not (fullEntryMode or editPreExtract)}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                    <th class="right">&nbsp;</th>
                    <td class="datacell">&nbsp;</td>
                </tr> 
            </c:if>
            
			<c:choose> 				
                <c:when test="${displayPurchaseOrderFields}">
                    <c:if test="${(fullEntryMode or amendmentEntry) or ( (not(fullEntryMode or amendmentEntry)) and (not empty KualiForm.document.alternateVendorHeaderGeneratedIdentifier) )}">
                        <tr>
                            <th class="right">&nbsp;</th>
                            <td class="datacell">&nbsp;</td>
            
                            <th class="right">Alternate Vendor For Non-Primary Vendor Payment:</th>
                            <td class="datacell">
                                <c:if test="${fullEntryMode or amendmentEntry}">
                                    <div align="left">
                                        <b>Search for alternate vendor</b>
                                        <kul:lookup
                                                boClassName="org.kuali.kfs.vnd.businessobject.VendorDetail"
                                                fieldConversions="vendorHeaderGeneratedIdentifier:document.alternateVendorHeaderGeneratedIdentifier,vendorDetailAssignedIdentifier:document.alternateVendorDetailAssignedIdentifier"
                                                lookupParameters="'Y':activeIndicator, 'PO':vendorHeader.vendorTypeCode"
                                                fieldLabel="Search for alternate vendor"/>
                                        </div>
                                    <br/>
                                </c:if>

                                <div align="left">
                                    <b><kul:htmlAttributeLabel attributeEntry="${documentAttributes.alternateVendorName}" /></b>
                                    <kul:htmlControlAttribute
                                            attributeEntry="${documentAttributes.alternateVendorName}"
                                            property="document.alternateVendorName"
                                            readOnly="true"
                                            tabindexOverride="${tabindexOverrideBase + 8}"/>
                                </div>
                                <div align="left">
                                    <b><kul:htmlAttributeLabel attributeEntry="${documentAttributes.alternateVendorNumber}" /></b>
                                    <kul:htmlControlAttribute
                                            attributeEntry="${documentAttributes.alternateVendorNumber}"
                                            property="document.alternateVendorNumber"
                                            readOnly="true"
                                            tabindexOverride="${tabindexOverrideBase + 8}"/>
                                </div>
					
					            <c:if test="${fullEntryMode or amendmentEntry}">
					                <br/>
                                    <html:submit
                                            property="methodToCall.removeAlternateVendor"
                                            alt="Remove alternate vendor"
                                            title="Remove alternate vendor"
                                            styleClass="btn btn-default small"
                                            value="Remove Alternate Vendor"/>
					            </c:if>
                            </td>
                        </tr>
                    </c:if>
                </c:when>
			
                <c:when test="${displayPaymentRequestFields and (not empty KualiForm.document.alternateVendorHeaderGeneratedIdentifier)}">
                    <tr>
                        <th class="right">Alternate Vendor For Non-Primary Vendor Payment:</th>
                        <td class="datacell">
                            <c:choose>
                                <c:when test="${fullEntryMode}">
                                    <div align="left">
                                        <html:submit
                                                property="methodToCall.useAlternateVendor"
                                                alt="Use alternate vendor"
                                                title="Use alternate vendor"
                                                styleClass="btn btn-default small"
                                                value="Use Alternate Vendor"/>
                                    </div>
                                    <br/>
				                    <div align="left">
                                        <html:submit
                                                property="methodToCall.useOriginalVendor"
                                                alt="Use original vendor"
                                                title="Use original vendor"
                                                styleClass="btn btn-default small"
                                                value="Use Original Vendor"/>
				                    </div>
				                </c:when>
				                <c:otherwise>
					                &nbsp;
				                </c:otherwise>
				            </c:choose>
                        </td>
                    
                        <th class="right">Primary Vendor Name:</th>
                        <td class="datacell">
                            <kul:htmlControlAttribute
                                attributeEntry="${documentAttributes.primaryVendorName}"
                                property="document.primaryVendorName"
                                readOnly="true"
                                tabindexOverride="${tabindexOverrideBase + 8}"/>
                        </td>
                    </tr>
			    </c:when>
			</c:choose>
        </table>

        <c:if test="${displayRequisitionFields and !lockB2BEntry}">
            <h3>Additional Suggested Vendor Names</h3>
            <table class="datatable standard" summary="Additional Vendor Section">
                <tr>
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.alternate1VendorName}" />
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.alternate1VendorName}" property="document.alternate1VendorName" 
                        	readOnly="${not (fullEntryMode or amendmentEntry)}" tabindexOverride="${tabindexOverrideBase + 8}"/>
                    </td>
                </tr>
                <tr>
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.alternate2VendorName}" />
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.alternate2VendorName}" property="document.alternate2VendorName" 
                        	readOnly="${not (fullEntryMode or amendmentEntry)}" tabindexOverride="${tabindexOverrideBase + 8}"/>
                    </td>
                </tr>
                <tr>
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.alternate3VendorName}" />
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.alternate3VendorName}" property="document.alternate3VendorName" 
                        	readOnly="${not (fullEntryMode or amendmentEntry)}" tabindexOverride="${tabindexOverrideBase + 8}"/>
                    </td>
                </tr>
                <tr>    
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.alternate4VendorName}" />
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.alternate4VendorName}" property="document.alternate4VendorName" 
                        	readOnly="${not (fullEntryMode or amendmentEntry)}" tabindexOverride="${tabindexOverrideBase + 8}"/>
                    </td>
                </tr>
                <tr>    
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.alternate5VendorName}" />
                    </th>
                    <td class="datacell">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.alternate5VendorName}" property="document.alternate5VendorName" 
                        	readOnly="${not (fullEntryMode or amendmentEntry)}" tabindexOverride="${tabindexOverrideBase + 8}"/>
                    </td>                                                
                </tr>
            </table>
        </c:if>

    </div>
</kul:tab>

