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
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>
<%@ attribute name="wizard" required="false" %>
           

<c:set var="fullEntryMode" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" />
<c:set var="currentUserCampusCode" value="${UserSession.person.campusCode}" />
<c:set var="tabindexOverrideBase" value="30" />

<!--  this is a temporary workaround until release 3, where this is fixed more generally -->
<c:set var="fullDocEntryCompleted" value="${(not empty KualiForm.editingMode['fullDocumentEntryCompleted'])}" />


    <div class="tab-container" align=center>
		
        <table cellpadding="0" cellspacing="0" class="datatable" summary="Vendor Section">
            <tr>
                <td colspan="2" class="subhead">Vendor Info</td>
            </tr>
            
        <c:if test="${wizard}" >
		<tr>
			<td height=30 colspan="2" class="neutral" style="color: blue; font-family:Verdana, Verdana, serif; font-size: 12px;font-style: italic">
			<b>NOTE:</b> Please provide as much Vendor information as you can, if you do not know the Vendor please leave blank</td>
		</tr>
		</c:if>

            <tr>
                <th align=right valign=middle width="30%" class="neutral">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorName}" /></div>
                </th>
                <td align=left valign=middle width="70%" class="neutral" >
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorName}" property="document.vendorName" 
                    	readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                    <c:if test="${fullEntryMode}" >
                        <kul:lookup  boClassName="edu.cornell.kfs.module.purap.businessobject.IWantDocVendorDetail" 
                        	lookupParameters="'Y':activeIndicator, 'PO':vendorHeader.vendorTypeCode"
                        	fieldConversions="vendorName:document.vendorName,vendorHeaderGeneratedIdentifier:document.vendorHeaderGeneratedIdentifier,vendorDetailAssignedIdentifier:document.vendorDetailAssignedIdentifier,defaultAddressLine1:document.vendorLine1Address,defaultAddressLine2:document.vendorLine2Address,defaultAddressCity:document.vendorCityName,defaultAddressStateCode:document.vendorStateCode,defaultAddressPostalCode:document.vendorPostalCode,defaultAddressCountryCode:document.vendorCountryCode,defaultFaxNumber:document.vendorFaxNumber,vendorUrlAddress:document.vendorWebURL"/>
                        
                    </c:if> 
                </td>
           </tr>
                
          <%--      <th align=right valign=middle width="25%" class="neutral">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorAttentionName}" /></div>
                </th>
                <td align=left valign=middle width="25%" class="neutral">
                   <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorAttentionName}" property="document.vendorAttentionName" 
                    	readOnly="${not (fullEntryMode)}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
            </tr>

             <tr>
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorNumber}" /></div>
                </th>
                <td align=left valign=middle class="datacell">
	                <c:if test="${not empty KualiForm.document.vendorHeaderGeneratedIdentifier}">
		                <kul:inquiry boClassName="org.kuali.kfs.vnd.businessobject.VendorDetail" keyValues="vendorHeaderGeneratedIdentifier=${KualiForm.document.vendorHeaderGeneratedIdentifier}&vendorDetailAssignedIdentifier=${KualiForm.document.vendorDetailAssignedIdentifier}" render="true">
		                    <kul:htmlControlAttribute 
		                    	attributeEntry="${documentAttributes.vendorNumber}" property="document.vendorDetail.vendorNumber" 
		                    	readOnly="true" tabindexOverride="${tabindexOverrideBase + 0}"/>
		                </kul:inquiry>
	                </c:if>
                </td>
            </tr>
            
            
               <tr>
                	<th align=right valign=middle width="25%" class="neutral">
                    	<div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorLine1Address}" /></div>
                	</th>
                	<td align=left valign=middle width="25%" class="neutral">
                   		<kul:htmlControlAttribute 
                    		attributeEntry="${documentAttributes.vendorLine1Address}" property="document.vendorLine1Address" 
                    		readOnly="${not (fullEntryMode)}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                	</td>
                	
                    <th align=right valign=middle width="25%" class="neutral">
                        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorPhoneNumber}" /></div>
                    </th>
                    <td align=left valign=middle width="25%" class="neutral">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.vendorPhoneNumber}" property="document.vendorPhoneNumber" 
                        	readOnly="false" tabindexOverride="${tabindexOverrideBase + 4}"/>                    
                       
                    </td>
                    
                </tr>
                
                <tr>
               		<th align=right valign=middle width="25%" class="neutral">
                    	<div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorLine2Address}" /></div>
                	</th>
                	<td align=left valign=middle width="25%" class="neutral">
                   		<kul:htmlControlAttribute 
                    		attributeEntry="${documentAttributes.vendorLine2Address}" property="document.vendorLine2Address" 
                    		readOnly="${not (fullEntryMode)}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                	</td>
                    <th align=right valign=middle width="25%" class="neutral">
                        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorFaxNumber}" /></div>
                    </th>
                    <td align=left valign=middle width="25%" class="neutral">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.vendorFaxNumber}" property="document.vendorFaxNumber" 
                        	readOnly="${not (fullEntryMode or amendmentEntry) or lockB2BEntry}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                </tr>
                
                
                <tr>
                 	
                    <th align=right valign=middle width="25%" class="neutral">
                        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorCityName}" /></div>
                    </th>
                    <td align=left valign=middle width="25%" class="neutral">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.vendorCityName}" property="document.vendorCityName" 
                        	readOnly="${not (fullEntryMode or amendmentEntry) or lockB2BEntry}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                    
                    <th align=right valign=middle width="25%" class="neutral">
                        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorEmail}" /></div>
                    </th>
                    <td align=left valign=middle width="25%" class="neutral">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.vendorEmail}" property="document.vendorEmail" 
                        	readOnly="${not (fullEntryMode or amendmentEntry) or lockB2BEntry}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                </tr>
                <tr>
                 	<th align=right valign=middle width="25%" class="neutral">
                        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorStateCode}" /></div>
                    </th>
                    <td align=left valign=middle width="25%" class="neutral">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.vendorStateCode}" property="document.vendorStateCode" 
                        	readOnly="${not (fullEntryMode or amendmentEntry) or lockB2BEntry}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                    <th align=right valign=middle width="25%" class="neutral">
                        <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.verdorWebURL}" /></div>
                    </th>
                    <td align=left valign=middle width="25%" class="neutral">
                        <kul:htmlControlAttribute 
                        	attributeEntry="${documentAttributes.verdorWebURL}" property="document.verdorWebURL" 
                        	readOnly="${not (fullEntryMode or amendmentEntry) or lockB2BEntry}" tabindexOverride="${tabindexOverrideBase + 4}"/>
                    </td>
                </tr>
            
            <tr>
            	
                <th align=right valign=middle width="25%" class="neutral">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorPostalCode}" /></div>
                </th>
                <td align=left valign=middle width="25%" class="neutral">
                   <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorPostalCode}" property="document.vendorPostalCode" 
                    	readOnly="${not (fullEntryMode)}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <th align=right valign=middle width="25%" class="neutral">
                    &nbsp;
                </th>
                <td align=left valign=middle width="25%" class="neutral">
                   &nbsp;
                </td>
            </tr>
            
             <tr>
            	
                <th align=right valign=middle width="25%" class="neutral">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorCountryCode}" /></div>
                </th>
                <td align=left valign=middle width="25%" class="neutral">
                   <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorCountryCode}" property="document.vendorCountryCode" 
                    	readOnly="${not (fullEntryMode)}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                --%>
                 <tr>
                
                <th align=right valign=middle width="30%" class="neutral">
                    &nbsp;
                </th>
                <td align=left valign=middle width="70%" class="neutral">
                   &nbsp;
                </td>
            </tr>
            
             <tr>
                
               <th align=right valign=middle width="30%" class="neutral">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.vendorDescription}" /></div>
                </th>
                <td align=left valign=middle width="70%" class="neutral" >
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.vendorDescription}" property="document.vendorDescription" 
                    	readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                   
                </td>
            </tr>
            
                <tr>
                
                <th align=right valign=middle width="30%" class="neutral">
                    &nbsp;
                </th>
                <td align=left valign=middle width="70%" class="neutral">
                   &nbsp;
                </td>
            </tr>

        </table>

    </div>