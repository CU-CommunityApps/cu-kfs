  <%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<c:forEach var="tab" items="${KualiForm.tabRoleIndexes}">
  <c:set var="tabErrorKey" value=""/>
  <c:forEach var="roleIndex" items="${tab.roleRequestIndexes}">
    <c:set var="tabErrorKey" value="${tabErrorKey}document.securityRequestRoles[${roleIndex}].*,"/>
  </c:forEach>
  
  <kul:tab tabTitle="Request Access to ${tab.tabName}" defaultOpen="true" tabErrorKey="${tabErrorKey}">
      <div class="tab-container" align="center">
         <h3>Roles</h3>
         
         <c:forEach var="roleIndex" items="${tab.roleRequestIndexes}">
           <ksr:securityRequestRole securityRequestRoleIndex="${roleIndex}" />
         </c:forEach>
      </div>
  </kul:tab>        
</c:forEach>
 