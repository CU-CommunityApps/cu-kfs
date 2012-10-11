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
              
<script language="JavaScript" type="text/javascript" src="dwr/interface/IWantDocumentService.js"></script>
<script language="JavaScript" type="text/javascript" src="dwr/interface/PersonService.js"></script>
<script language="JavaScript" type="text/javascript" src="scripts/module/purap/iWantDoc.js"></script>  
      
<c:set var="fullEntryMode" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" />
<c:set var="currentUserCampusCode" value="${UserSession.person.campusCode}" />
<c:set var="tabindexOverrideBase" value="30" />

    <div class="tab-container" align=center>

            <table cellpadding="0" cellspacing="0" summary="Customer Data" border="0">
            
            <tr>
                <td colspan="4" class="subhead">Organization Information</td> 
            </tr>
            
            <tr>
                <td height=10 colspan="4" class="neutral">&nbsp;</td>
            </tr>
            
             <tr>
             <td colspan="4" class="neutral">
				<table cellpadding="0" cellspacing="0" border="0">
					<tr>
						<td height=30 colspan="2" class="neutral" style="color: blue; font-family:Verdana, Verdana, serif; font-size: 12px;font-style: italic">
							<b>NOTE:</b> Please select the college and department for which you are entering this request 
						</td>
					</tr>
					<tr>
						
                
                			<th align=right valign=middle width="20%" class="neutral" style="border-left-width: 1px;">
                    			<div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.collegeLevelOrganization}" /></div>
               				</th>
                			<td align=left valign=middle width="20%" class="neutral">
                    			<kul:htmlControlAttribute 
                        			attributeEntry="${documentAttributes.collegeLevelOrganization}" 
                        			property="document.collegeLevelOrganization" readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 0}"
                        			onchange="loadDepartments(this.form)"/>
                        		<html:hidden property="previousSelectedOrg" />
               				</td> 
                
                 			<th align=right valign=middle width="20%" class="neutral" style="border-left-width: 1px;">
                   				 <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.departmentLevelOrganization}" /></div>
                			</th>

                			<td align=left valign=middle width="20%" class="neutral">    
                      			<html:select property="document.departmentLevelOrganization" disabled="${not fullEntryMode}">
                        			<html:optionsCollection property="deptOrgKeyLabels" label="label" value="key" />
                      			</html:select>
                        	</td>
                        	<th align=right valign=middle width="10%" class="neutral" >
                   				 <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.useCollegeAndDepartmentAsDefault}" /></div>
                			</th>

                			<td align=left valign=middle width="10%" class="neutral">    
                      			<kul:htmlControlAttribute 
                        			attributeEntry="${documentAttributes.useCollegeAndDepartmentAsDefault}" 
                        			property="document.useCollegeAndDepartmentAsDefault" readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                        	</td>
                     </tr>
                  </table>
               </td>
                
             </tr>
             
            <tr>
                <td height=10 colspan="4" class="neutral">&nbsp;</td>
            </tr>
 
            <tr>
                <td colspan="2" class="subhead">Requestor</td> <td colspan="2" class="subhead">Deliver To</td>
            </tr>
            
             <tr>
                <th align=right valign=middle  width="25%" class="neutral">
                    &nbsp;
                </th>
                <td align=left valign=middle width="25%" class="neutral">
                	&nbsp;
                </td> 
                
                <th align=right valign=middle width="25%" class="neutral" style="border-left-width: 1px;">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.sameAsInitiator}" /></div>
                </th>
                <td align=left valign=middle width="25%" class="neutral">
                    <kul:htmlControlAttribute 
                        attributeEntry="${documentAttributes.sameAsInitiator}" 
                        property="document.sameAsInitiator" readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 0}" 
                        onclick="loadDeliverToInfoSameAsInitiator('document.sameAsInitiator','document.initiatorNetID', 'document.deliverToNetID', 'document.initiatorName', 'document.deliverToName', 'document.initiatorPhoneNumber', 'document.deliverToPhoneNumber', 'document.initiatorEmailAddress', 'document.deliverToEmailAddress', 'document.initiatorAddress', 'document.deliverToAddress')"/>&nbsp;
                </td> 
             </tr>
            
            <tr>
                <th align=right valign=middle  width="25%" class="neutral">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.initiatorNetID}" />
                </th>
                <td align=left valign=middle width="25%" class="neutral">
                 <kul:user userIdFieldName="document.initiatorNetID"
                              userId="${KualiForm.document.initiatorNetID}"
                              universalIdFieldName=""
                              universalId=""
                              userNameFieldName="document.initiatorName"
                              userName="${KualiForm.document.initiatorName}"
                              readOnly="${displayReadOnly}"
                              renderOtherFields="true"
                              fieldConversions="principalName:document.initiatorNetID,name:document.initiatorName,phoneNumber:document.initiatorPhoneNumber,emailAddress:document.initiatorEmailAddress"
                              lookupParameters="document.initiatorNetID:principalName"
                              hasErrors="${hasErrors}"
                              onblur="loadRequestorInfo('document.sameAsInitiator', 'document.initiatorNetID', 'document.initiatorName', 'document.initiatorPhoneNumber', 'document.initiatorEmailAddress', 'document.initiatorAddress')" />
                 
                   </td> 
                
                <th align=right valign=middle width="25%" class="neutral" style="border-left-width: 1px;">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliverToNetID}" /></div>
                </th>
                <td align=left valign=middle width="25%" class="neutral" >
                	 <kul:user userIdFieldName="document.deliverToNetID"
                              userId="${KualiForm.document.deliverToNetID}"
                              universalIdFieldName=""
                              universalId=""
                              userNameFieldName="document.deliverToName"
                              userName="${KualiForm.document.deliverToName}"
                              readOnly="${displayReadOnly}"
                              renderOtherFields="true"
                              fieldConversions="principalName:document.deliverToNetID,name:document.deliverToName,phoneNumber:document.deliverToPhoneNumber,emailAddress:document.deliverToEmailAddress"
                              lookupParameters="document.deliverToNetID:principalName"
                              hasErrors="${hasErrors}"
                              onblur="loadDeliverToInfo('document.sameAsInitiator', 'document.deliverToNetID', 'document.deliverToName', 'document.deliverToPhoneNumber', 'document.deliverToEmailAddress', 'document.deliverToAddress')" />
                 
                </td> 
             </tr>
            
            <tr>
                <th align=right valign=middle width="25%" class="neutral">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.initiatorPhoneNumber}"/></div>
                </th>
                <td align=left valign=middle width="25%" class="neutral">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.initiatorPhoneNumber}" property="document.initiatorPhoneNumber" 
                    	readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 5}"/>
                </td>
                
                 <th align=right valign=middle width="25%" class="neutral" style="border-left-width: 1px;">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliverToPhoneNumber}"/></div>
                </th>
                <td align=left valign=middle width="25%" class="neutral">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.deliverToPhoneNumber}" property="document.deliverToPhoneNumber" 
                    	readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 5}"/>
                </td>
                 
            </tr>
            
			<tr>     
                <th align=right valign=middle width="25%" class="neutral">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.initiatorEmailAddress}"/></div>
                </th>
                <td align=left valign=middle width="25%" class="neutral">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.initiatorEmailAddress}" property="document.initiatorEmailAddress" 
                    	readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 5}"/>
                </td>
                
                 <th align=right valign=middle width="25%" class="neutral" style="border-left-width: 1px;">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliverToEmailAddress}"/></div>
                </th>
                <td align=left valign=middle width="25%" class="neutral">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.deliverToEmailAddress}" property="document.deliverToEmailAddress" 
                    	readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 5}"/>
                </td>              
			</tr>
			
			<tr>  
			    <th align=right valign=middle width="25%" class="neutral">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.initiatorAddress}"/></div>
                </th>
                <td align=left valign=middle width="25%" class="neutral">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.initiatorAddress}" property="document.initiatorAddress" 
                    	readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 5}"/>
                </td>
                <th align=right valign=middle width="25%" class="neutral">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliverToAddress}"/></div>
                </th>
                <td align=left valign=middle width="25%" class="neutral">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.deliverToAddress}" property="document.deliverToAddress" 
                    	readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 5}"/>
                </td>
			</tr>
			
        </table>
        


    </div>
