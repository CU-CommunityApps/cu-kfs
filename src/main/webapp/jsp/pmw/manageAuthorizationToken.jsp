<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:page showDocumentInfo="false"
	headerTitle="PaymentWorks Authorization Token Management"
	docTitle="PaymentWorks Authorization Token Management" transactionalDocument="false"
	htmlFormAction="pmw/manageAuthorizationToken" errorKey="*">

	<div class="main-panel">

    	<div class="center" style="margin: 30px 0;">
			<div>
				<c:if test="${!KualiForm.isProduction()}">
					<div class="alert alert-warning" style="width: 50%; margin-left: 25%;">
						<strong>Non-prod SQL needs to be updated with the new token value after a refresh.</strong>
					</div>
				</c:if>
				<html:submit property="methodToCall.refreshToken" styleClass="btn btn-default" value="Refresh and Replace Token" />
			</div>
		</div>

	</div>

</kul:page>
