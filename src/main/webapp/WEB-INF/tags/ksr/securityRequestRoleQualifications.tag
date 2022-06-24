<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="securityRequestRoleIndex" required="true"
              description="Index of the security request role instance in the document collection to render qualifications for." %>
<%@ attribute name="readOnly" required="false"
              description="Whether the Security Request Role Qualification data should be read-only" %>

<c:set var="securityRequestRole" value="${KualiForm.document.securityRequestRoles[securityRequestRoleIndex]}" />

<c:set var="securityRequestRoleQualificationHdr" value="${securityRequestRole.newRequestRoleQualification}" />
<c:if test="${readOnly}">
  <c:set var="securityRequestRoleQualificationHdr" value="${securityRequestRole.requestRoleQualifications[0]}" />
</c:if>

<table border="0" cellpadding="0" cellspacing="0" class="celltable">
    <%-- Header Row --%>
    <tr>
        <th width="10" scope="col">&nbsp;</th>
        <c:forEach var="wrappedQualificationDetail" items="${securityRequestRoleQualificationHdr.wrappedRoleQualificationDetails}">
          <th scope="col">
             <c:set var="qualificationDetail" value="${wrappedQualificationDetail.roleQualificationDetail}"/>
             <c:set var="attributeEntry" value="${qualificationDetail.attributeEntry}" />
             <kul:htmlAttributeLabel attributeEntry="${attributeEntry}" useShortLabel="false" noColon="true"/>
          </th>
        </c:forEach>

        <c:if test="${!readOnly}">
            <th scope="col">Actions</th>
        </c:if>
    </tr>

    <%-- Add Line --%>
    <c:if test="${!readOnly}">
        <tr>
            <th scope="row"><div align="right">add:</div>
            </th>
            
            <c:forEach var="wrappedQualificationDetail" items="${securityRequestRoleQualificationHdr.wrappedRoleQualificationDetails}" varStatus="status">
              <td>
                <c:set var="qualificationDetail" value="${wrappedQualificationDetail.roleQualificationDetail}"/>
                <c:set var="attributeEntry" value="${qualificationDetail.attributeEntry}" />
                <kul:htmlControlAttribute attributeEntry="${attributeEntry}"
                    property="document.securityRequestRoles[${securityRequestRoleIndex}].newRequestRoleQualification.roleQualificationDetails[${status.count-1}].attributeValue" readOnly="${readOnly}" />
                
                <c:set var="attributeDefinition" value="${qualificationDetail.attributeDefinition}" />    
                <c:if test="${(!empty attributeDefinition.quickFinder) && !readOnly}"> 
                	<c:set var="quickFinder" value="${qualificationDetail.attributeDefinition.quickFinder}" />
                   <ksr:securityRequestQualifierLookup requestQualifications="${securityRequestRoleQualificationHdr.roleQualificationDetails}" pathPrefix="document.securityRequestRoles[${securityRequestRoleIndex}].newRequestRoleQualification" quickFinder="${quickFinder}" />
                </c:if>    
              </td>
            </c:forEach>
            
            <td>
               <html:html-button property="methodToCall.addQualificationLine.roleRequestIndex${securityRequestRoleIndex}" 
                	alt="Add Qualification" title="Add Qualification" styleClass="btn btn-green skinny" value="Add" innerHTML="<span class=\"fa fa-plus\"></span>"/>
            </td>
        </tr>
    </c:if>

    <%-- Existing Lines --%>
    <c:forEach var="securityRequestRoleQualification" items="${securityRequestRole.requestRoleQualifications}" varStatus="qualStatus">
        <tr>
            <th scope="row"><div align="right">${qualStatus.count}:</div>
            </th>
            
            <c:forEach var="wrappedQualificationDetail" items="${securityRequestRoleQualification.wrappedRoleQualificationDetails}" varStatus="dtlStatus">
              <td>
                <c:set var="qualificationDetail" value="${wrappedQualificationDetail.roleQualificationDetail}"/>
                <c:set var="detailIndex" value="${wrappedQualificationDetail.detailIndex}"/>
                <c:set var="attributeEntry" value="${qualificationDetail.attributeEntry}" />
                <kul:htmlControlAttribute attributeEntry="${attributeEntry}" 
                    property="document.securityRequestRoles[${securityRequestRoleIndex}].requestRoleQualifications[${qualStatus.count-1}].roleQualificationDetails[${detailIndex}].attributeValue" readOnly="${readOnly}" />
            
                <c:set var="attributeDefinition" value="${qualificationDetail.attributeDefinition}" />  
                 
                <c:if test="${(!empty attributeDefinition.quickFinder) && !readOnly}"> 
                <c:set var="quickFinder" value="${qualificationDetail.attributeDefinition.quickFinder}" />
                   <ksr:securityRequestQualifierLookup requestQualifications="${securityRequestRoleQualificationHdr.roleQualificationDetails}" pathPrefix="document.securityRequestRoles[${securityRequestRoleIndex}].requestRoleQualifications[${detailIndex}]" quickFinder="${quickFinder}" />
                </c:if>              
              </td>
            </c:forEach>
            
            <c:if test="${!readOnly}">
              <td>
                 <html:html-button property="methodToCall.deleteQualificationLine.roleRequestIndex${securityRequestRoleIndex}.qualificationIndex${qualStatus.count-1}" 
                	alt="Delete Qualification" title="Delete Qualification" styleClass="btn btn-red skinny" value="Delete" innerHTML="<span class=\"fa fa-trash\"></span>"/>
              </td>
            </c:if>
        </tr>
    </c:forEach>    
</table>