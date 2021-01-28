<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:page showDocumentInfo="false"
	headerTitle="Concur Access Token Management"
	docTitle="Concur Access Token Management" transactionalDocument="false"
	htmlFormAction="concur/manageAccessToken" errorKey="*">

	<div class="main-panel">
		<div class="center" style="margin: 30px 0;"]>
			<div style="font-weight: bold">Access Token Expiration Date</div>
			<div>${KualiForm.accessTokenExpirationDate}</div>
		</div>
		<div class="center" style="margin: 30px 0;"]>
			<div style="font-weight: bold">Revoke the existing access token and retrieve new access and refresh tokens.</div>
			<div>
				<html:submit property="methodToCall.replaceToken" styleClass="btn btn-default" value="Replace Token" />
			</div>
		</div>

		<c:choose>
    		<c:when test="${KualiForm.showRevokeAndRefreshButtons}">
    			<div class="center" style="margin: 30px 0;">
					<div style="font-weight: bold">Refresh the existing access token.</div>
					<div>
						<html:submit property="methodToCall.refreshToken" styleClass="btn btn-default" value="Refresh Token" />
					</div>
				</div>
				<div class="center" style="margin: 30px 0;">
					<div style="font-weight: bold">Revoke the current token</div>
					<div>
						<html:submit property="methodToCall.revokeToken" styleClass="btn btn-default" value="Revoke Token" />
					</div>
				</div>
			</c:when>
		</c:choose>

		<c:choose>
			<c:when test="${KualiForm.showResetTokenToEmptyStringButton}">
				<div class="center" style="margin: 30px 0;">
					<div style="font-weight: bold">Reset existing token to empty string in the back-end storage.</div>
					<div>(Do this in non-prod systems to test revoke access token.)</div>
					<div>
						<html:submit property="methodToCall.resetTokenToEmptyString" styleClass="btn btn-default" value="Reset Token To Empty String" />
					</div>
				</div>
			</c:when>
		</c:choose>

	</div>

</kul:page>
