<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<kul:page
        showDocumentInfo="false"
        headerTitle="Concur Access Token Management"
        docTitle="Concur Access Token Management"
        transactionalDocument="false"
        htmlFormAction="manageAccessToken"
        errorKey="*">

    <div class="main-panel">
        <div class="center" style="margin: 30px 0;">
            <html:submit
                    property="methodToCall.requestNewToken"
                    styleClass="btn btn-default"
                    value="Request New Token"/>
            <html:submit
                    property="methodToCall.refreshToken"
                    styleClass="btn btn-default"
                    value="Refresh Token"/>
            <html:submit
                    property="methodToCall.revokeToken"
                    styleClass="btn btn-default"
                    value="Revoke Token"/>
        </div>
    </div>

</kul:page>
