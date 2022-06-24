<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="securityRequestRoleIndex" required="true"
              description="Index of the security request role instance in the document collection to render." %>
<%@ attribute name="readOnly" required="false"
              description="Whether the Security Request Role data should be read-only" %>

<c:set var="genericAttributes" value="${DataDictionary.AttributeReferenceDummy.attributes}" />

<c:set var="securityRequestRole" value="${KualiForm.document.securityRequestRoles[securityRequestRoleIndex]}" />

<h3>${securityRequestRole.roleInfo.id} : ${securityRequestRole.roleInfo.namespaceCode} - ${securityRequestRole.roleInfo.name}</h3>
<table width="100%" border="0" cellpadding="0" cellspacing="0" class="datatable">
    <tr>     
        <th class="right">
            <kul:htmlAttributeLabel attributeEntry="${genericAttributes.activeIndicator}"/>
        </th>
        <td width="50%">
          <kul:htmlControlAttribute property="document.securityRequestRoles[${securityRequestRoleIndex}].active"
                attributeEntry="${genericAttributes.activeIndicator}" readOnly="${readOnly}"/> 
          <c:if test="${securityRequestRole.currentActive != securityRequestRole.active}">
              <kul:fieldShowChangedIcon />
          </c:if>
          <br/>
          <span class="current_qual">
            <c:if test="${securityRequestRole.currentActive}">
              Currently Active
            </c:if>
            <c:if test="${!securityRequestRole.currentActive}">
              Currently Inactive
            </c:if>
          </span>      
       </td>
    </tr>
    
    <c:if test="${securityRequestRole.qualifiedRole && ( (!empty securityRequestRole.requestRoleQualifications)
                || (!empty securityRequestRole.currentQualifications) || !readOnly )}">
      <tr>
          <kul:htmlAttributeHeaderCell literalLabel="Qualifications:" align="right" horizontal="true" addClass="right"/>
          <td style="padding: 5px;">
             <c:if test="${!empty securityRequestRole.requestRoleQualifications || !readOnly}">
               <ksr:securityRequestRoleQualifications securityRequestRoleIndex="${securityRequestRoleIndex}"
                      readOnly="${readOnly}" />
             </c:if>
             
             <c:if test="${!empty securityRequestRole.currentQualifications}">
               <span class="current_qual">Current Qualifications: ${securityRequestRole.currentQualifications}</span>
             </c:if>
          </td>
      </tr>
    </c:if>
</table>


