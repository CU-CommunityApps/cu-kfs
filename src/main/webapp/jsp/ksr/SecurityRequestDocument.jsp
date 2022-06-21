  <%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<c:set var="readOnly" value="${!KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}" scope="request" />

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
    <kul:superUserActions />
    <kul:panelFooter />
    <kul:documentControls transactionalDocument="true" />
    <kul:modernLookupSupport />

</kul:documentPage>    