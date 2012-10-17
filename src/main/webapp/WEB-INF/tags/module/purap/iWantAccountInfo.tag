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

<%@ attribute name="documentAttributes" required="true"
	type="java.util.Map"
	description="The DataDictionary entry containing attributes for cash control document fields."%>
	
<%@ attribute name="wizard" required="false" %>
<script language="JavaScript" type="text/javascript" src="dwr/interface/AccountService.js"></script>
<script language="JavaScript" type="text/javascript" src="scripts/module/purap/iWantDoc.js"></script> 

<c:set var="hasAccounts" value="${fn:length(KualiForm.document.accounts) > 0}" />
<c:set var="accountAttributes" value="${DataDictionary.IWantAccount.attributes}" />
<c:set var="fullEntryMode" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" />
<c:set var="accountsNbr" value="${fn:length(KualiForm.document.accounts)}" />

    
     <div class="tab-container" align=center>
    <table cellpadding="0" cellspacing="0" class="datatable" summary="Account Information">
        <tr>
                <td colspan="11" class="subhead">Account Information</td>
        </tr>
        
		 <tr>
			<td height=30 colspan="11" class="neutral" style="color: blue; font-family:Verdana, Verdana, serif; font-size: 12px;font-style: italic"><b>NOTE:</b> If you do not know the specific account number to be charged, please provide information in the <b>account description box</b> below (i.e. NSF research account, salary recovery account).</td>
		</tr>
		  <tr><td colspan="11" class="neutral"></td></tr>
         <tr> 
         <td colspan="11" class="neutral">
         	<table border="0">  
         		<tr>    
                <th align=right valign=middle class="neutral" width="50%">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.accountDescriptionTxt}" />
                </th>
                <td align=left valign=middle class="neutral" width="50%">
                    <kul:htmlControlAttribute 
                        attributeEntry="${documentAttributes.accountDescriptionTxt}" 
                        property="document.accountDescriptionTxt" readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td > 
               
                </tr>
                </table>
                </td>
                
             </tr>
		
        <tr>
		
        <tr>
			<th height=30 colspan="11" class="neutral" valign="middle">&nbsp;</th>
		</tr>
        <tr>
			<th height=30 colspan="11" class="neutral" valign="middle">or</th>
		</tr>
		<tr>
			<th height=30 colspan="11" class="neutral" valign="middle">&nbsp;</th>
		</tr>
		
		<c:if test="${fullEntryMode}">
        <tr>
       			<th align=left valign=middle class="neutral">
                    <div align="left">&nbsp;</div>
                </th>
         		<th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.chartOfAccountsCode}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.accountNumber}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.subAccountNumber}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.financialObjectCode}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.financialSubObjectCode}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.projectCode}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.organizationReferenceId}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.useAmountOrPercent}" /></div>
                </th>
                 <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.amountOrPercent}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left">Action</div>
                </th>
                
        </tr>
        <tr>
               <td align=left valign=top class="neutral">
                    &nbsp;
                </td> 
                <td align=left valign=top class="neutral">
                    <kul:htmlControlAttribute 
                        attributeEntry="${accountAttributes.chartOfAccountsCode}" 
                        property="newSourceLine.chartOfAccountsCode" readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 1}"/>&nbsp;
                </td> 
                <td align=left valign=top class="neutral">

                        
                <kul:htmlControlAttribute attributeEntry="${accountAttributes.accountNumber}" property="newSourceLine.accountNumber"
	                	onblur="loadAccountName('newSourceLine.accountNumber', 'newSourceLine.chartOfAccountsCode', 'document.newSourceLine.accountNumber.name.div');" readOnly="${not fullEntryMode}" />&nbsp;
	                	<c:if test="${ fullEntryMode}">
	                	<kul:lookup boClassName="org.kuali.kfs.coa.businessobject.Account"
					                fieldConversions="accountNumber:newSourceLine.accountNumber,chartOfAccountsCode:newSourceLine.chartOfAccountsCode"
					                lookupParameters="newSourceLine.accountNumber:accountNumber,newSourceLine.chartOfAccountsCode:chartOfAccountsCode"/>
					    
				    <br/>
					<div id="document.newSourceLine.accountNumber.name.div" class="fineprint">
            			<kul:htmlControlAttribute attributeEntry="${accountAttributes.accountNumber}" property="newSourceLine.account.accountName" readOnly="true" />
            		</div>
            		</c:if>
         
                </td> 
                <td align=left valign=top class="neutral">
                    <kul:htmlControlAttribute 
                        attributeEntry="${accountAttributes.subAccountNumber}" 
                        property="newSourceLine.subAccountNumber" readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 0}"/>&nbsp;
                </td>
                <td align=left valign=top class="neutral">
                    <kul:htmlControlAttribute 
                        attributeEntry="${accountAttributes.financialObjectCode}" 
                        property="newSourceLine.financialObjectCode" readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 0}"/>&nbsp;
                </td>
                <td align=left valign=top class="neutral">
                    <kul:htmlControlAttribute 
                        attributeEntry="${accountAttributes.financialSubObjectCode}" 
                        property="newSourceLine.financialSubObjectCode" readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 0}"/>&nbsp;
                </td>
                <td align=left valign=top class="neutral">
                    <kul:htmlControlAttribute 
                        attributeEntry="${accountAttributes.projectCode}" 
                        property="newSourceLine.projectCode" readOnly="false" tabindexOverride="${tabindexOverrideBase + 0}"/>&nbsp;
                </td>
                <td align=left valign=top class="neutral">
                    <kul:htmlControlAttribute 
                        attributeEntry="${accountAttributes.organizationReferenceId}" 
                        property="newSourceLine.organizationReferenceId" readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 0}"/>&nbsp;
                </td>
                <td align=left valign=top class="neutral">
                    <kul:htmlControlAttribute 
                        attributeEntry="${accountAttributes.useAmountOrPercent}" 
                        property="newSourceLine.useAmountOrPercent" readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 0}"
                       />&nbsp;
                </td>   
                <td align=left valign=top class="neutral">
                    <kul:htmlControlAttribute 
                        attributeEntry="${accountAttributes.amountOrPercent}" 
                        property="newSourceLine.amountOrPercent" readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 0}" 
                        />&nbsp;
                </td> 
                <td align=left valign=top class="neutral">
                    
				        <html:image property="methodToCall.addAccountingLine" src="${ConfigProperties.externalizable.images.url}tinybutton-save.gif" alt="Add an Account" title="Add an Account" styleClass="tinybutton" tabindex="${tabindexOverrideBase + 0}"/>
				   
                </td>     
                
         </tr>
         </c:if>
         <tr>
			<td colspan="11" class="subhead">
			    <span class="subhead-left">Current Accounts</span>
			</td>
		</tr>
		
        <c:if test="${ hasAccounts}">
			  <tr>
       			<th align=left valign=middle class="neutral">
                    <div align="left">&nbsp;</div>
                </th>
         		<th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.chartOfAccountsCode}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.accountNumber}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.subAccountNumber}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.financialObjectCode}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.financialSubObjectCode}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.projectCode}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.organizationReferenceId}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.useAmountOrPercent}" /></div>
                </th>
                <th align=left valign=middle class="neutral">
                    <div align="left"><kul:htmlAttributeLabel attributeEntry="${accountAttributes.amountOrPercent}" /></div>
                </th>
                <c:if test="${fullEntryMode }">
                <th align=left valign=middle class="neutral">
                    <div align="left">Action</div>
                </th>
                </c:if>
                <c:if test="${not fullEntryMode }">
                <th align=left valign=middle class="neutral">
                    <div align="left">&nbsp;</div>
                </th>
                </c:if>
                
        </tr>
		</c:if>

		<c:if test="${ !hasAccounts}">
			<tr>
				<th height=30 colspan="11" class="neutral">No accounts added</th>
			</tr>
		</c:if>

		<logic:iterate indexId="ctr" name="KualiForm" property="document.accounts" id="accountLine">
			


				<!-- table class="datatable" style="width: 100%;" -->

				<tr>
					<td valign="top" class="neutral" align="left">
					<td valign="top" class="neutral" align="left">
					<div align="top">
					    <kul:htmlControlAttribute
						    attributeEntry="${accountAttributes.chartOfAccountsCode}"
						    property="document.account[${ctr}].chartOfAccountsCode"
						    readOnly="${not fullEntryMode}"
						    tabindexOverride="${tabindexOverrideBase + 0}"/>
						    </div>
					</td>
					<td valign="top" class="neutral" align="left">
					<div align="left">
						 <kul:htmlControlAttribute
						    attributeEntry="${accountAttributes.accountNumber}"
						    property="document.account[${ctr}].accountNumber"
						    readOnly="${not fullEntryMode}"
						    tabindexOverride="${tabindexOverrideBase + 0}"/>
						    
						    <c:if test="${ fullEntryMode}">
	                	<kul:lookup boClassName="org.kuali.kfs.coa.businessobject.Account"
					                fieldConversions="accountNumber:newSourceLine.accountNumber,chartOfAccountsCode:newSourceLine.chartOfAccountsCode"
					                lookupParameters="newSourceLine.accountNumber:accountNumber,newSourceLine.chartOfAccountsCode:chartOfAccountsCode"/>
					    </div>
				    <br/>
					<div id="document.account[${ctr}].accountNumber.name.div" class="fineprint">
            			<kul:htmlControlAttribute attributeEntry="${accountAttributes.accountNumber}" property="document.account[${ctr}].accountNumber" readOnly="true" />
            		</div>
            		</c:if>
					</td>	
					<td valign="top" class="neutral" align="left">
					    <kul:htmlControlAttribute
						    attributeEntry="${accountAttributes.subAccountNumber}"
						    property="document.account[${ctr}].subAccountNumber"
						    readOnly="${not fullEntryMode}"
						    tabindexOverride="${tabindexOverrideBase + 0}"/>
					</td>
                    <td valign="top" class="neutral" align="left">
                        <kul:htmlControlAttribute 
                            attributeEntry="${accountAttributes.financialObjectCode}" 
                            property="document.account[${ctr}].financialObjectCode"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                                        
                    </td>				    
					<td valign="top" class="neutral" align="left">					
					    <kul:htmlControlAttribute
						    attributeEntry="${accountAttributes.financialSubObjectCode}"
						    property="document.account[${ctr}].financialSubObjectCode"
						    readOnly="${not fullEntryMode}"
						    tabindexOverride="${tabindexOverrideBase + 0}"/>
				    </td>				
                    	    
									
					<td valign="top" class="neutral">
					    <div align="left">
					        <kul:htmlControlAttribute
						        attributeEntry="${accountAttributes.projectCode}"
						        property="document.account[${ctr}].projectCode"
						        readOnly="${not fullEntryMode}"
						        tabindexOverride="${tabindexOverrideBase + 0}"/>
						</div>
					</td>
					
				    <td valign="top" class="neutral">
					    <div align="left">
					        <kul:htmlControlAttribute
						        attributeEntry="${accountAttributes.organizationReferenceId}"
						        property="document.account[${ctr}].organizationReferenceId"
						        readOnly="${not fullEntryMode}"
						        tabindexOverride="${tabindexOverrideBase + 0}"/>
						</div>
					</td>
					
			        <td valign="top" class="neutral">
					    <div align="left">
					        <kul:htmlControlAttribute
						        attributeEntry="${accountAttributes.useAmountOrPercent}"
						        property="document.account[${ctr}].useAmountOrPercent"
						        readOnly="${not fullEntryMode}"
						        tabindexOverride="${tabindexOverrideBase + 0}"
						        onchange="updateAccountsTotal('document.totalDollarAmount', 'document.accountingLinesTotal', '${accountsNbr}' )"
						       />
						</div>
					</td>
					
										
			        <td valign="top" class="neutral">
					    <div align="left">
					        <kul:htmlControlAttribute
						        attributeEntry="${accountAttributes.amountOrPercent}"
						        property="document.account[${ctr}].amountOrPercent"
						        readOnly="${not fullEntryMode}"
						        tabindexOverride="${tabindexOverrideBase + 0}" 
						        onchange="updateAccountsTotal('document.totalDollarAmount', 'document.accountingLinesTotal', '${accountsNbr}' )"
						        />
						</div>
					</td>
								
					
					<td valign="top" class="neutral" >
					    <div align="left">				
					    	<c:choose>		    	
					    	<c:when test="${fullEntryMode}">
					        	<html:image
						        	property="methodToCall.deleteAccount.line${ctr}"
						        	src="${ConfigProperties.kr.externalizable.images.url}tinybutton-delete1.gif"
						        	alt="Delete Account ${ctr+1}" title="Delete Account ${ctr+1}"
						        	styleClass="tinybutton" /><br><br>
						    </c:when>
						    						    
						    <c:otherwise>							    	
					    		<div align="center">&nbsp;</div>
						    </c:otherwise>
						    </c:choose>						    
						</div>
					</td>
				
				</tr>
				

		</logic:iterate>
		

<!-- BEGIN TOTAL SECTION -->
		<tr>
			<th height=30 colspan="11" class="neutral">&nbsp;</th>
		</tr>

		<tr>
			<td colspan="11" class="subhead">
                <span class="subhead-left">Totals</span>
                <span class="subhead-right">&nbsp;</span>
            </td>
		</tr>	

		<tr>
			<th align=right colspan="9" scope="row" class="neutral">
			    <div align="right">
			        <kul:htmlAttributeLabel attributeEntry="${DataDictionary.IWantDocument.attributes.totalDollarAmount}" />
			    </div>
			</th>
			<td valign=middle class="neutral" colspan="2" >
			    <div align="right">
			        <b>
                        <html:text name="KualiForm" property="document.accountingLinesTotal" readonly="true" style="border: none; font-weight: bold"/>
                    </b>
                </div>
			</td>
			
		</tr>

		<!-- END TOTAL SECTION -->
		
		
		</table>
		</div>