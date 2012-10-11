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
           

<c:set var="fullEntryMode" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT] && (empty KualiForm.editingMode['restrictFiscalEntry'])}" />
<c:set var="currentUserCampusCode" value="${UserSession.person.campusCode}" />
<c:set var="tabindexOverrideBase" value="30" />

<!--  this is a temporary workaround until release 3, where this is fixed more generally -->
<c:set var="fullDocEntryCompleted" value="${(not empty KualiForm.editingMode['fullDocumentEntryCompleted'])}" />

<kul:tab tabTitle="Delivery" defaultOpen="true" tabErrorKey="${PurapConstants.DELIVERY_ERRORS}">
    <div class="tab-container" align=center>

            <table cellpadding="0" cellspacing="0" class="datatable" summary="Delivery">
            <tr>
                <td colspan="2" class="subhead">Delivery</td>
            </tr>
            <tr>
                <th align=right valign=middle class="bord-l-b" width="25%">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.sameAsInitiator}" /></div>
                </th>
                <td align=left valign=middle class="datacell" width="25%">
                    <kul:htmlControlAttribute 
                        attributeEntry="${documentAttributes.sameAsInitiator}" 
                        property="document.sameAsInitiator" readOnly="false" tabindexOverride="${tabindexOverrideBase + 0}"/>&nbsp;
                </td> 
                
             </tr>
            <tr>
                <th align=right valign=middle class="bord-l-b" width="25%">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliverToNetID}" /></div>
                </th>
                <td align=left valign=middle class="datacell" width="25%">
                    <kul:htmlControlAttribute 
                        attributeEntry="${documentAttributes.deliverToNetID}" 
                        property="document.initiatorNetID" readOnly="false" tabindexOverride="${tabindexOverrideBase + 0}"/>&nbsp;
                </td> 
                
             </tr>
             <tr>              
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliverToName}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.deliverToName}" property="document.deliverToName" 
                    	readOnly="${not (fullEntryMode or amendmentEntry) or deliveryReadOnly}" tabindexOverride="${tabindexOverrideBase + 5}"/>
                    <c:if test="${fullEntryMode && !deliveryReadOnly}">
                        <kul:lookup boClassName="org.kuali.rice.kim.bo.Person" 
                        	fieldConversions="name:document.initiatorName,emailAddress:document.initiatorEmailAddress,phoneNumber:document.initiatorPhoneNumber"/>
                    </c:if>
                </td>
            </tr>
            <tr>
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliverToPhoneNumber}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.deliverToPhoneNumber}" property="document.deliverToPhoneNumber" 
                    	readOnly="${not (fullEntryMode or amendmentEntry) or deliveryReadOnly}" tabindexOverride="${tabindexOverrideBase + 5}"/>
                </td>
            </tr>
			<tr>     
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliverToEmailAddress}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.deliverToEmailAddress}" property="document.deliverToEmailAddress" 
                    	readOnly="${not (fullEntryMode )}" tabindexOverride="${tabindexOverrideBase + 5}"/>
                </td>
			</tr>
			<tr>     
                <th align=right valign=middle class="bord-l-b">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliverToAddress}"/></div>
                </th>
                <td align=left valign=middle class="datacell">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.deliverToAddress}" property="document.deliverToAddress" 
                    	readOnly="${not (fullEntryMode ) }" tabindexOverride="${tabindexOverrideBase + 5}"/>
                </td>
			</tr>
			
        </table>


    </div>
</kul:tab>