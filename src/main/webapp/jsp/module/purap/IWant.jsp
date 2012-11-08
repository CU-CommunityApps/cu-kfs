<%--
 Copyright 2006 The Kuali Foundation
 
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

<c:set var="headerTitle" value="${KualiForm.headerTitle}" />

    <c:set var="fullEntryMode" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" />
    <c:set var="step" value="${KualiForm.step}" />
    <c:set var="isAdHocApprover" value="${KualiForm.editingMode['completeOrder']}"/>
	
    
<c:choose>
<c:when test="${ step eq 'regular' }">
<kul:documentPage showDocumentInfo="true"
    headerTitle="${headerTitle}"
	documentTypeName="IWantDocument"
	htmlFormAction="purapIWant" renderMultipart="true" multipartHack="true"
	showTabButtons="true" >
  
  <SCRIPT type="text/javascript">
    var kualiForm = document.forms['KualiForm'];
    var kualiElements = kualiForm.elements;
  </SCRIPT>
	

	<purap:iWantDocumentOverview editingMode="${KualiForm.editingMode}" readOnly="${not fullEntryMode}"> 
	</purap:iWantDocumentOverview>
	

   <%--   <purap:delivery
        documentAttributes="${DataDictionary.RequisitionDocument.attributes}" 
        showDefaultBuildingOption="true"  /> --%>
        
       
    <purap:iWantCustomerData
        documentAttributes="${DataDictionary.IWantDocument.attributes}" />

    
    <kul:tab tabTitle="Items" defaultOpen="true" tabErrorKey="${KFSConstants.I_WANT_DOC_ITEM_TAB_ERRORS}">
    
    	<purap:iWantItems itemAttributes="${DataDictionary.IWantItem.attributes}" /> 
    
    </kul:tab>
    
    <kul:tab tabTitle="Accounting Info" defaultOpen="true" tabErrorKey="${KFSConstants.I_WANT_DOC_ACCOUNT_TAB_ERRORS}">	
    
    	<purap:iWantAccountInfo documentAttributes="${DataDictionary.IWantDocument.attributes}" /> 
    	
    </kul:tab>
   
   <kul:tab tabTitle="Vendor" defaultOpen="true" tabErrorKey="${KFSConstants.I_WANT_DOC_VENDOR_TAB_ERRORS}"> 	
   
   		<purap:iWantVendor documentAttributes="${DataDictionary.IWantDocument.attributes}" />
    
   </kul:tab>
        
    <purap:iWantMisc
        documentAttributes="${DataDictionary.IWantDocument.attributes}" />	
	            
    <purap:iWantNotes defaultOpen="true"/> 

    <kul:tab tabTitle="Routing and Submission" defaultOpen="true">  
    
		<purap:iWantAdHocRecipients />
		
	</kul:tab>

	<kul:routeLog />
	<c:if test="${isAdHocApprover}">
	<kul:tab tabTitle="Order Completed" defaultOpen="true" tabErrorKey="${KFSConstants.I_WANT_DOC_VENDOR_TAB_ERRORS}"> 	
		<div class="tab-container" align=center>
    	<table cellpadding="0" cellspacing="0" class="datatable" summary="Complete Information">
        <tr>
                <td class="subhead">Order Completed Information</td>
        </tr>
		<tr align="center">
        <td align="center" class="neutral" >
				   <kul:htmlControlAttribute 
                        attributeEntry="${DataDictionary.IWantDocument.attributes.complete}" 
                        property="document.complete" readOnly="${not fullEntryMode}" />&nbsp;
        </td>
        </tr>
        </table>
        </div>
       
              
	</kul:tab>
	
	</c:if>

	<kul:panelFooter />
	
	<c:set var="extraButtons" value="${KualiForm.extraButtons}"/>  	

	<sys:documentControls transactionalDocument="true" extraButtons="${extraButtons}" />

</kul:documentPage>
</c:when>
<c:otherwise>

<kul:page showDocumentInfo="true"
	htmlFormAction="purapIWant" renderMultipart="true"
	multipartHack="true"
	docTitle="${headerTitle}"
    transactionalDocument="true">
    
  <SCRIPT type="text/javascript">
    var kualiForm = document.forms['KualiForm'];
    var kualiElements = kualiForm.elements;
  </SCRIPT>

	<c:if test="${(step eq 'customerDataStep')}">
	
	<purap:iWantDocumentOverview editingMode="${KualiForm.editingMode}" readOnly="false"> 
	</purap:iWantDocumentOverview>
       
    <purap:iWantCustomerData
        documentAttributes="${DataDictionary.IWantDocument.attributes}" />
    </c:if>

    <c:if test="${(step eq 'itemAndAcctDataStep')}">
    <kul:tabTop tabTitle="Items & Account Info" defaultOpen="true" tabErrorKey="${KFSConstants.I_WANT_DOC_ITEM_TAB_ERRORS}">
    		 <purap:iWantItems itemAttributes="${DataDictionary.IWantItem.attributes}" 
    	/> 
    		
   		</kul:tabTop>

       <kul:tab tabTitle="Account" defaultOpen="true" tabErrorKey="${KFSConstants.I_WANT_DOC_ACCOUNT_TAB_ERRORS}" > 
	     <purap:iWantAccountInfo documentAttributes="${DataDictionary.IWantDocument.attributes}" wizard="true" /> 
    	</kul:tab>
	    
   </c:if>
   <c:if test="${(step eq 'vendorStep')}">
  	<kul:tabTop tabTitle="Vendor" defaultOpen="true" tabErrorKey="${KFSConstants.I_WANT_DOC_VENDOR_TAB_ERRORS}"> 	
    		<purap:iWantVendor
        		documentAttributes="${DataDictionary.IWantDocument.attributes}" wizard="true"/>
   		</kul:tabTop>
   		
   		<purap:iWantMisc documentAttributes="${DataDictionary.IWantDocument.attributes}" />
   		
   		 <purap:iWantNotes defaultOpen="true"/> 

   </c:if>
   
    <c:if test="${(step eq 'routingStep')}">   
    
    <kul:tabTop tabTitle="Routing and Submission" defaultOpen="true" tabErrorKey="${PurapConstants.VENDOR_ERRORS}">  
	<purap:iWantAdHocRecipients />
	</kul:tabTop>
	</c:if>

	<kul:panelFooter />
	
	<c:set var="extraButtons" value="${KualiForm.extraButtons}"/>  	

	<sys:documentControls transactionalDocument="true" extraButtons="${extraButtons}" suppressRoutingControls="${KualiForm.editingMode['wizard']}"/>

</kul:page>

</c:otherwise>
</c:choose>
