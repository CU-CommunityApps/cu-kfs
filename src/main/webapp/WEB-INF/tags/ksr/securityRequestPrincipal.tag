<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="readOnly" required="false"
              description="Whether the Security Request Principal data should be read-only" %>

<c:set var="securityRequestAttributes" value="${DataDictionary.SecurityRequestDocument.attributes}" />
 
<kul:tab tabTitle="Access Requested For: ${KualiForm.document.securityGroup.securityGroupName}" defaultOpen="true" tabErrorKey="document.principalId,document.requestPerson.*,document.primaryDepartmentCode,error.ksr.securityrequestdocument.primaryDeptCode.*">
    <div class="tab-container" align="center">
        <h3>Access Request for ${KualiForm.document.securityGroup.securityGroupName}</h3>
        
        <table width="100%" border="0" cellpadding="0" cellspacing="0" class="datatable">
            <tr>
            	<th class="right">
            		<kul:htmlAttributeLabel attributeEntry="${securityRequestAttributes['requestPerson.principalName']}"/>
        		</th>
                <td valign="middle" class="left" width="50%">
                    <kul:user userIdFieldName="document.requestPerson.principalName"
                          userId="${KualiForm.document.requestPerson.principalName}"
                          universalIdFieldName="document.principalId"
                          universalId="${KualiForm.document.principalId}"
                          userNameFieldName="document.requestPerson.name"
                          userName="${KualiForm.document.requestPerson.name}"
                          readOnly="true"
                          hasErrors="${hasErrors}"/>
                    <c:if test="${!readOnly}">
                        <kul:lookup boClassName="org.kuali.kfs.kim.impl.identity.Person"
                              fieldConversions="principalName:document.requestPerson.principalName,principalId:document.principalId,name:document.requestPerson.name"
                              lookupParameters="document.requestPerson.principalName:principalName,document.principalId:principalId,document.requestPerson.name:name"
                              fieldLabel="${securityRequestAttributes['requestPerson.principalName'].label}"
                              anchor="${currentTabIndex}"/>
                    </c:if>
                </td>
            </tr>
            <tr>
                <th class="right">
            		<kul:htmlAttributeLabel attributeEntry="${securityRequestAttributes.primaryDepartmentCode}"/>
        		</th>
                <td>
                    <kul:htmlControlAttribute property="document.primaryDepartmentCode"
                        attributeEntry="${securityRequestAttributes.primaryDepartmentCode}" readOnly="${readOnly}" />
                </td>
            </tr>
        </table>
    </div>
</kul:tab>
