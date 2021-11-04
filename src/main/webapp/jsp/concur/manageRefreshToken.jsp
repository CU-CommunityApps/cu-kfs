<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:page showDocumentInfo="false"
  headerTitle="Concur OAuth2 Refresh Token Management"
  docTitle="Concur OAuth2 Refresh Token Management" transactionalDocument="false"
  htmlFormAction="concur/manageRefreshToken" errorKey="*">

  <div class="main-panel">
    <div class="center" style="margin: 30px 0;"]>
      <c:choose>
        <c:when test="${KualiForm.dispayNonProdWarning}">
          <div style="font-weight: bold">
            <c:out value="${KualiForm.nonProdWarning}"/>
          </div>
        </c:when>
      </c:choose>
      <c:choose>
        <c:when test="${KualiForm.displayUpdateSuccessMessage}">
          <div style="font-weight: bold"> 
            <c:out value="${KualiForm.updateSuccessMessage}"/>
          </div>
        </c:when>
      </c:choose>
      <div style="font-weight: bold">
        <c:out value="${KualiForm.refreshDateMessage}"/>
      </div>
      <div></div>
      <div>
        <html:submit property="methodToCall.replaceRefreshToken" styleClass="btn btn-default" value="Replace Refresh Token" />
        <html:submit property="methodToCall.cancel" styleClass="btn btn-default" value="Cancel" />
      </div>
    </div>

  </div>

</kul:page>
