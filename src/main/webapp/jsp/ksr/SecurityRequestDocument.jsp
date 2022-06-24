  <%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<c:set var="readOnly" value="${!KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}" scope="request" />

<kul:documentPage
    showDocumentInfo="true"
    htmlFormAction="securityRequestDocument"
    documentTypeName="SecurityRequestDocument"
    renderMultipart="true"
    showTabButtons="true">

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