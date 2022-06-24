<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="readOnly" required="false"
              description="Whether the Security Request Tab data should be read-only" %>

<c:forEach var="tab" items="${KualiForm.tabRoleIndexes}">
  <c:set var="tabErrorKey" value=""/>
  <c:forEach var="roleIndex" items="${tab.roleRequestIndexes}">
    <c:set var="tabErrorKey" value="${tabErrorKey}document.securityRequestRoles[${roleIndex}].*,"/>
  </c:forEach>
  
  <kul:tab tabTitle="Request Access to ${tab.tabName}" defaultOpen="true" tabErrorKey="${tabErrorKey}">
      <div class="tab-container" align="center">
         <c:forEach var="roleIndex" items="${tab.roleRequestIndexes}">
           <ksr:securityRequestRole securityRequestRoleIndex="${roleIndex}" readOnly="${readOnly}" />
         </c:forEach>
      </div>
  </kul:tab>
</c:forEach>
 