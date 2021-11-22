<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:page showDocumentInfo="false"
	headerTitle="Concur OAuth2 Refresh and Request Token Management"
	docTitle="Concur OAuth2 Refresh and Request Token Management"
	transactionalDocument="false"
	htmlFormAction="concur/manageRefreshToken" errorKey="*">
	
	<c:set var="refreshTokenFormAttributes" value="${DataDictionary.ConcurManageRefreshTokenForm.attributes}"/>

	<c:choose>
		<c:when test="${KualiForm.displayNonProdWarning}">
			<div class="main-panel">
				<div class="center" style="margin: 30px 0;">
					<div style="font-weight: bold">
						<c:out value="${KualiForm.nonProdWarning}" />
					</div>
				</div>
			</div>
		</c:when>
	</c:choose>

	<div class="main-panel">
		<div class="center" style="margin: 30px 0;">
			<div style="font-weight: bold">Manage Request Token</div>
			<c:choose>
				<c:when test="${KualiForm.displayUpdateRequestTokenMessage}">
					<div style="font-weight: bold">
						<c:out value="${KualiForm.updateRequestTokenMessage}" />
					</div>
				</c:when>
			</c:choose>
			<div style="font-weight: bold">
				Request Token last updated
				<c:out value="${KualiForm.requestTokenUpdateDate}" />
			</div>
			<div>
				<c:out value="${KualiForm.updateRequestTokenInstructions}" />
			</div>
			<div>
				<kul:htmlAttributeLabel
					attributeEntry="${refreshTokenFromAttributes.newRequestToken}"
					readOnly="true" />
			</div>
			<div>
				<kul:htmlAttributeLabel
					attributeEntry="${refreshTokenFormAttributes.newRequestToken}"
					readOnly="true" />
				<kul:htmlControlAttribute property="newRequestToken"
					attributeEntry="${refreshTokenFormAttributes.newRequestToken}"
					readOnly="false" />
			</div>
			<div>
				<html:submit property="methodToCall.replaceRequestToken"
					styleClass="btn btn-default" value="Save new Request Token" />
			</div>
		</div>
	</div>

	<div class="main-panel">
		<div class="center" style="margin: 30px 0;" >
			<div style="font-weight: bold">Manage Refresh Token</div>
			<c:choose>
				<c:when test="${KualiForm.displayUpdateRefreshTokenMessage}">
					<div style="font-weight: bold">
						<c:out value="${KualiForm.updateRefreshTokenMessage}" />
					</div>
				</c:when>
			</c:choose>
			<div style="font-weight: bold">
				Refresh Token last updated
				<c:out value="${KualiForm.refreshTokenUpdateDate}" />
			</div>
			<div>
				<html:submit property="methodToCall.replaceRefreshToken"
					styleClass="btn btn-default" value="Replace Refresh Token" />
			</div>
		</div>
	</div>

	<div class="main-panel">
		<div>
			<html:submit property="methodToCall.cancel"
				styleClass="btn btn-default" value="Cancel" />
		</div>
	</div>

</kul:page>
