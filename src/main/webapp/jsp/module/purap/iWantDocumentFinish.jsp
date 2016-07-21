<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:page showDocumentInfo="false"
          headerTitle="I Want Document"
          docTitle="I Want Document Submitted"
          transactionalDocument="false"
          htmlFormAction="purapIWant"
          errorKey="*">
    <br/>
	<div class="center" style="font-size: 18px">
		Your information was submitted. To see your orders click <a href="${ConfigProperties.procurementgateway.url}" target="_blank">here</a>
	</div>
</kul:page>