<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:page showDocumentInfo="false"
	headerTitle="PaymentWorks Authorization Token Management"
	docTitle="PaymentWorks Authorization Token Management" transactionalDocument="false"
	htmlFormAction="pmw/manageAuthorizationToken" errorKey="*">

	<div class="main-panel">

    	<div class="center" style="margin: 30px 0;">
			<div>
				<html:submit property="methodToCall.refreshToken" styleClass="btn btn-default" value="Refresh and Replace Token" />
			</div>
		</div>

	</div>

</kul:page>
