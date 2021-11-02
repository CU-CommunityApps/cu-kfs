<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:page showDocumentInfo="false"
  headerTitle="Concur OAuth2 Refresh Token Management"
  docTitle="Concur OAuth2 Refresh Token Management" transactionalDocument="false"
  htmlFormAction="concur/manageRefreshToken" errorKey="*">

  <div class="main-panel">
    <div class="center" style="margin: 30px 0;"]>
      <div style="font-weight: bold">Replace the Concur OAuth2 refresh token.</div>
      <div style="font-weight: bold">if you ware doing this action in a non-production environment, be sure to update .</div>
      <div>
        <html:submit property="methodToCall.replaceRefreshToken" styleClass="btn btn-default" value="Replace Refresh Token" />
      </div>
    </div>

  </div>

</kul:page>
