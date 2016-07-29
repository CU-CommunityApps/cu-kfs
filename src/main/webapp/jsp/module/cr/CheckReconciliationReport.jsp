<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:page showDocumentInfo="false" docTitle="Check Reconciliation Report" renderMultipart="false"
	transactionalDocument="false" htmlFormAction="crCheckReconciliationReport" showTabButtons="true">

	<kul:tabTop tabTitle="Run Report" defaultOpen="true" tabErrorKey="*">
	    <div class="tab-container" align="center">
			<h3>Report Parameters</h3>
	      	<table class="standard side-margins">
			<tr>
				<th class="right" width="50%">Start Date:</th>
		      	<td class="left" width="50%">
					<kul:dateInputNoAttributeEntry property="startDate" maxLength="10" size="10" />
				</td>
			</tr>
			<tr>
				<th class="right" width="50%">End Date:</th>
		      	<td class="left" width="50%">
					<kul:dateInputNoAttributeEntry property="endDate" maxLength="10" size="10" />
				</td>
			</tr>
			<tr>
				<th class="right" width="50%">Format:</th>
		      	<td class="left" width="50%">
					<input type="radio" name="format" value="pdf" checked>PDF
					<input type="radio" name="format" value="excel">Excel
				</td>
			</tr>
			</table>
		</div>
	</kul:tabTop>
    <div id="globalbuttons">
        <html:submit
                property="methodToCall.performReport"
                value="Submit"
                styleClass="btn btn-default"
                alt="Submit"
                title="Submit"
                onclick="excludeSubmitRestriction=true" />
        <html:submit
                property="methodToCall.returnToIndex"
                value="Close"
                styleClass="btn btn-default"
                alt="Close"
                title="Close" />
    </div>
</kul:page>