<%--
 Copyright 2005-2007 The Kuali Foundation

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
<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp"%>

<c:set var="readOnly" value="${!KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" scope="request" />

<kul:documentPage
    showDocumentInfo="true"
    htmlFormAction="securityRequestDocument"
    documentTypeName="SecurityRequestDocument"
    renderMultipart="true"
    showTabButtons="true"
    auditCount="0">

    <kul:hiddenDocumentFields />

    <kul:documentOverview editingMode="${KualiForm.editingMode}" />
    
    <ksr:securityRequestPrincipal />
    
    <c:if test="${!empty KualiForm.document.principalId}">
      <ksr:securityRequestTabs />
    </c:if>
    
    <kul:notes />
    <kul:adHocRecipients />
    <kul:routeLog />
    <kul:panelFooter />
    <kul:documentControls transactionalDocument="false" />

</kul:documentPage>    