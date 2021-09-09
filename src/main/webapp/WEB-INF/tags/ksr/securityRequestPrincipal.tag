 <%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<c:set var="securityRequestAttributes" value="${DataDictionary.SecurityRequestDocument.attributes}" />
<%-- CU Customization (CYNERGY-2425): Modified the tabErrorKey to include more primary department code error messages. --%>
 
<kul:tab tabTitle="Access Requested For: ${KualiForm.document.securityGroup.securityGroupName}" defaultOpen="true" tabErrorKey="document.principalId,document.requestPerson.*,document.primaryDepartmentCode,error.ksr.securityrequestdocument.primaryDeptCode.*">
    <div class="tab-container" align="center">
        <h3>Access Request for ${KualiForm.document.securityGroup.securityGroupName}</h3>
        
        <table width="100%" border="0" cellpadding="0" cellspacing="0" class="datatable">
            <tr>
                <kul:htmlAttributeHeaderCell labelFor="document.requestPerson.principalName" attributeEntry="${securityRequestAttributes['requestPerson.principalName']}"
                    align="right" horizontal="true"/>
                <td width="50%">
                    <html:hidden property="document.principalId" /> <kul:htmlControlAttribute property="document.requestPerson.principalName"
                        attributeEntry="${securityRequestAttributes['requestPerson.principalName']}" readOnly="true"/> 
                        
                    <c:if test="${!readOnly}">

                      <kul:user userIdFieldName="document.requestPerson.principalName"
                      userId="${KualiForm.document.requestPerson.principalName}"
                      universalIdFieldName="document.principalId"
                      universalId="${KualiForm.document.principalId}"
                      userNameFieldName="document.requestPerson.name"
                      userName="${KualiForm.document.requestPerson.name}"
                      readOnly="${readOnly}"
                      fieldConversions="principalName:document.requestPerson.principalName,principalId:document.principalId,name:document.requestPerson.name"
                      lookupParameters="document.requestPerson.principalName:principalName,document.principalId:principalId,document.requestPerson.name:name"
                      hasErrors="${hasErrors}"
                      />
                    </c:if>    
                </td>
            </tr>
            <tr>
                <kul:htmlAttributeHeaderCell labelFor="document.primaryDepartmentCode"
                    attributeEntry="${securityRequestAttributes.primaryDepartmentCode}" align="right" horizontal="true"/>
                <td>
                    <kul:htmlControlAttribute property="document.primaryDepartmentCode"
                        attributeEntry="${securityRequestAttributes.primaryDepartmentCode}" readOnly="${readOnly}" />
                </td>
            </tr>
        </table>
    </div>
</kul:tab>
