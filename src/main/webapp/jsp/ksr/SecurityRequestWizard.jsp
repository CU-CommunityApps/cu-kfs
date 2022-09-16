<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>
<c:set var="securityGroupAttributes" value="${DataDictionary.SecurityGroup.attributes}" />

<kul:page 
	docTitle="Security Request Wizard" 
	headerMenuBar="" 
	headerTitle="" 
	transactionalDocument="" 
	htmlFormAction="securityRequestWizard" 
	renderMultipart="true" 
	lookup="false">
    <br />
	<br />
    
	<table cellspacing="0" cellpadding="2" class="datatable" style="text-align: left; margin-left: auto; margin-right: auto; padding-left: 5em;">
		<thead>
			<tr>
				<td style="border-right: medium none;" class="tab-subhead" colspan="4"><h3>Select System</h3></td>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${KualiForm.securityGroups}" var="securityGroup" varStatus="status">
				<tr>
					<td class="sideBars">&nbsp;</td>
					<td>
						<html:radio property="securityGroup.securityGroupId" value="${securityGroup.securityGroupId}" 
							title="${securityGroup.securityGroupName}" styleId="securityGroup.securityGroupName[${status.index}]" />
					</td>
					<th>
						<label for="securityGroup.securityGroupName[${status.index}]">
							<c:out value="${securityGroup.securityGroupName}" />
						</label>
					</th>
					<td class="sideBars">&nbsp;</td>
				</tr>
			</c:forEach>
		</tbody>
		<tfoot>
			<tr>
				<td colspan="4" align="center">
					<div>
						<html:submit property="methodToCall.processWizard" styleClass="btn btn-default" value="Continue" />
					</div>
				</div>
               </td>
			</tr>
		</tfoot>
	</table>
</kul:page>