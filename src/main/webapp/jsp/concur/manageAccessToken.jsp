<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:page showDocumentInfo="false"
	headerTitle="Concur Access Token Management"
	docTitle="Concur Access Token Management" transactionalDocument="false"
	htmlFormAction="concur/manageAccessToken" errorKey="*">

	<div class="main-panel">
		<div class="center" style="margin: 30px 0;"]>
			<div style="font-weight: bold">Access Token Expiration Date</div>
			<div>${KualiForm.accessTokenExpirationDate }</div>
		</div>
		<div class="center" style="margin: 30px 0;"]>
			<div style="font-weight: bold">${KualiForm.replaceTokenExplanation }</div>
			<div>
				<html:submit property="methodToCall.replaceToken" styleClass="btn btn-default" value="Replace Token" />
			</div>
		</div>

		<div class="center" style="margin: 30px 0;">
			<div style="font-weight: bold">${KualiForm.refreshTokenExplanation }</div>
			<div>
				<html:submit property="methodToCall.refreshToken" styleClass="btn btn-default" value="Refresh Token" />
			</div>
		</div>
	</div>

</kul:page>
