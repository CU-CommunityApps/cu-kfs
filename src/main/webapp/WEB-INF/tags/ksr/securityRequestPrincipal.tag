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
                      <kul:lookup boClassName="org.kuali.rice.kim.bo.impl.PersonImpl"
                           fieldConversions="principalId:principalId,principalName:requestPerson.principalName" />
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
