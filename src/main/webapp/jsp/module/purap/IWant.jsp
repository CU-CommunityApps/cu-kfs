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
    <c:set var="canAdHocRouteForApprove" value="${KualiForm.adHocActionRequestCodes[KEWConstants.ACTION_REQUEST_APPROVE_REQ]}"/>	
    
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

  <script type='text/javascript' src="dwr/interface/IWantAmountUtil.js"></script>

	<kul:tabTop tabTitle="Document Overview" defaultOpen="true" tabErrorKey="${Constants.DOCUMENT_ERRORS}">

		<purap:iWantDocumentOverview editingMode="${KualiForm.editingMode}" readOnly="${not fullEntryMode}"> 
		</purap:iWantDocumentOverview>

		<purap:iWantCustomerData documentAttributes="${DataDictionary.IWantDocument.attributes}" />

	</kul:tabTop>

    
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
	
	<%-- Display related documents, if a req has been created from this doc. --%>
    <c:if test="${!empty(KualiForm.document.reqsDocId)}">
        <purap:relatedDocuments
                documentAttributes="${DataDictionary.RelatedDocuments.attributes}" />
    </c:if>	
	
    <purap:iWantNotes defaultOpen="true"/> 

    <c:if test="${canAdHocRouteForApprove != null}">
    <kul:tab tabTitle="Routing and Submission" defaultOpen="true">  
    
		<purap:iWantAdHocRecipients />
		
	</kul:tab>
	</c:if>

	<kul:routeLog />
	<c:if test="${isAdHocApprover}">
	<kul:tab tabTitle="Order Completed (Required)" defaultOpen="true" tabErrorKey="${KFSConstants.I_WANT_DOC_ORDER_COMPLETED_TAB_ERRORS}"> 	
		<div align=center class="tab-container" >
    	<table cellpadding="0" cellspacing="0" class="datatable" summary="Complete Information">
        <tr>
                <td class="subhead">Order Completed Information</td>
        </tr>
        <tr><td height="10" class="neutral"></td></tr>
		<tr align="center">
        <td align="center" class="neutral" >
        <div align="center" >
        <div align="center" >
				   <kul:htmlControlAttribute 
                        attributeEntry="${DataDictionary.IWantDocument.attributes.completeOption}" 
                        property="document.completeOption" readOnly="${not fullEntryMode}" />&nbsp;
        </div>
        </div>
        </td>
        </tr>
        <tr><td height="10" class="neutral"></td></tr>
        </table>
        </div>
       
              
	</kul:tab>
	
	</c:if>

	<kul:panelFooter />
	
	<c:set var="extraButtons" value="${KualiForm.extraButtons}"/>  	

	<purap:iWantDocumentControls transactionalDocument="true" extraButtons="${extraButtons}" />

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

  <script type='text/javascript' src="dwr/interface/IWantAmountUtil.js"></script>

	<c:if test="${(step eq 'customerDataStep')}">

		<kul:tabTop tabTitle="Document Overview" defaultOpen="true" tabErrorKey="${Constants.DOCUMENT_ERRORS}">

			<purap:iWantDocumentOverview editingMode="${KualiForm.editingMode}" readOnly="false"> 
			</purap:iWantDocumentOverview>

			<purap:iWantCustomerData documentAttributes="${DataDictionary.IWantDocument.attributes}" />

		</kul:tabTop>

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
	 <c:if test="${canAdHocRouteForApprove != null}">
	 	<purap:iWantAdHocRecipients />
	 </c:if>
	</kul:tabTop>
	</c:if>

	<kul:panelFooter />
	
	<c:set var="extraButtons" value="${KualiForm.extraButtons}"/>  	

	<sys:documentControls transactionalDocument="true" extraButtons="${extraButtons}" suppressRoutingControls="${KualiForm.editingMode['wizard']}"/>

</kul:page>

</c:otherwise>
</c:choose>
