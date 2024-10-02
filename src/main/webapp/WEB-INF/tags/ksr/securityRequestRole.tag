<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="securityRequestRoleIndex" required="true"
              description="Index of the security request role instance in the document collection to render." %>
<%@ attribute name="readOnly" required="false"
              description="Whether the Security Request Role data should be read-only" %>

<c:set var="genericAttributes" value="${DataDictionary.AttributeReferenceDummy.attributes}" />
<c:set var="securityRequestRole" value="${KualiForm.document.securityRequestRoles[securityRequestRoleIndex]}" />
<c:set var="roleTitle" value="${securityRequestRole.roleInfo.id} : ${securityRequestRole.roleInfo.namespaceCode} - ${securityRequestRole.roleInfo.name}"/>
<c:set var="roleInquiryUrl" value="${ConfigProperties.application.url}/inquiry.do?methodToCall=start&businessObjectClassName=org.kuali.kfs.kim.impl.role.Role&id=${securityRequestRole.roleInfo.id}&mode=standalone"/>

<h3>&nbsp;</h3>
<table width="100%" border="0" cellpadding="0" cellspacing="0" class="datatable">
  <tr>
    <th colspan="2" width="100%">
      <b>
        <c:out value="${roleTitle}"/>
        <a href="<c:out value='${roleInquiryUrl}'/>" target="_blank" title="Open in new tab" class="new-window" onclick="event.stopPropagation();">
          <span class="glyphicon glyphicon-new-window"></span>
        </a>
      </b>
    </th>
  </tr>
    <tr>     
        <th width="20%" class="right">
            <kul:htmlAttributeLabel attributeEntry="${genericAttributes.activeIndicator}"/>
        </th>
        <td width="80%">
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
          <kul:htmlAttributeHeaderCell literalLabel="Qualifications:" align="right" horizontal="true" width="20%" addClass="right"/>
          <td style="padding: 5px; border-top-width: 1px; border-top-style: dashed;" width="80%">
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


