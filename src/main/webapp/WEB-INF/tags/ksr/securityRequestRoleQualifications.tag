<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="securityRequestRoleIndex" required="true"
              description="Index of the security request role instance in the document collection to render qualifications for." %>

<c:set var="securityRequestRole" value="${KualiForm.document.securityRequestRoles[securityRequestRoleIndex]}" />

<c:set var="securityRequestRoleQualificationHdr" value="${securityRequestRole.newRequestRoleQualification}" />
<c:if test="${readOnly}">
  <c:set var="securityRequestRoleQualificationHdr" value="${securityRequestRole.requestRoleQualifications[0]}" />
</c:if>

<table border="0" cellpadding="0" cellspacing="0" class="celltable">
    <%-- Header Row --%>
    <tr>
        <th width="10" scope="col">&nbsp;</th>
        <c:forEach var="qualificationDetail" items="${securityRequestRoleQualificationHdr.roleQualificationDetails}">
          <th scope="col">
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
            
            <c:forEach var="qualificationDetail" items="${securityRequestRoleQualificationHdr.roleQualificationDetails}" varStatus="status">
              <td>
                <c:set var="attributeEntry" value="${qualificationDetail.attributeEntry}" />
                <kul:htmlControlAttribute attributeEntry="${attributeEntry}"
                    property="document.securityRequestRoles[${securityRequestRoleIndex}].newRequestRoleQualification.roleQualificationDetails[${status.count-1}].attributeValue" readOnly="${readOnly}" />
                
                <c:set var="attributeDefinition" value="${qualificationDetail.attributeDefinition}" />    
                <c:if test="${(!empty attributeDefinition.quickFinder) && !readOnly}"> 
                	<c:set var="quickFinderAttributeDefinition" value="${qualificationDetail.attributeDefinition.quickFinder}" />
                   <ksr:securityRequestQualifierLookup requestQualifications="${securityRequestRoleQualificationHdr.roleQualificationDetails}" pathPrefix="document.securityRequestRoles[${securityRequestRoleIndex}].newRequestRoleQualification" attributeDefinition="${quickFinderAttributeDefinition}" />
                </c:if>    
              </td>
            </c:forEach>
            
            <td>
               <html:image src="${ConfigProperties.externalizable.images.url}tinybutton-add1.gif" styleClass="tinybutton"
                  property="methodToCall.addQualificationLine.roleRequestIndex${securityRequestRoleIndex}" alt="Add Qualification" title="Add Qualification" />
            </td>
        </tr>
    </c:if>

    <%-- Existing Lines --%>
    <c:forEach var="securityRequestRoleQualification" items="${securityRequestRole.requestRoleQualifications}" varStatus="qualStatus">
        <tr>
            <th scope="row"><div align="right">${qualStatus.count}:</div>
            </th>
            
            <c:forEach var="qualificationDetail" items="${securityRequestRoleQualification.roleQualificationDetails}" varStatus="dtlStatus">
              <td>
                <c:set var="attributeEntry" value="${qualificationDetail.attributeEntry}" />
                <kul:htmlControlAttribute attributeEntry="${attributeEntry}" 
                    property="document.securityRequestRoles[${securityRequestRoleIndex}].requestRoleQualifications[${qualStatus.count-1}].roleQualificationDetails[${dtlStatus.count-1}].attributeValue" readOnly="${readOnly}" />
            
                <c:set var="attributeDefinition" value="${qualificationDetail.attributeDefinition}" />  
                 
                <c:if test="${(!empty attributeDefinition.quickFinder) && !readOnly}"> 
                <c:set var="quickFinderAttributeDefinition" value="${qualificationDetail.attributeDefinition.quickFinder}" />
                   <ksr:securityRequestQualifierLookup requestQualifications="${securityRequestRoleQualificationHdr.roleQualificationDetails}" pathPrefix="document.securityRequestRoles[${securityRequestRoleIndex}].requestRoleQualifications[${qualStatus.count-1}]" attributeDefinition="${quickFinderAttributeDefinition}" />
                </c:if>              
              </td>
            </c:forEach>
            
            <c:if test="${!readOnly}">
              <td>
                 <html:image src="${ConfigProperties.externalizable.images.url}tinybutton-delete1.gif" styleClass="tinybutton"
                    property="methodToCall.deleteQualificationLine.roleRequestIndex${securityRequestRoleIndex}.qualificationIndex${qualStatus.count-1}" alt="Delete Qualification" title="Delete Qualification" />
              </td>
            </c:if>
        </tr>
    </c:forEach>    
</table>