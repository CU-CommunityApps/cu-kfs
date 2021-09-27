  <%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="requestQualifications" required="true" type="java.util.List"%>
<%@ attribute name="pathPrefix" required="true"%>
<%@ attribute name="quickFinder" required="true" type="org.kuali.kfs.kim.api.type.QuickFinder"%>

<c:set var="fieldConversion" value="" />
<c:set var="params" value="" />

<c:forEach var="qualification" items="${requestQualifications}" varStatus="defidx">
    <c:set var="searchStr" value="${qualification.attributeName}" />
    <c:forEach items="${quickFinder.fieldConversions}" var="lookupReturn" varStatus="lookupIdx">
        <c:if test="${lookupReturn.key == searchStr}">
            <c:set var="fieldConversion" value="${fieldConversion},${searchStr}:${pathPrefix}.roleQualificationDetails[${defidx.index}].attributeValue" />
        </c:if>
        <c:if test="${lookupReturn.key != searchStr and lookupReturn.value == searchStr}">
            <c:set var="fieldConversion" value="${fieldConversion},${lookupReturn.key}:${pathPrefix}.roleQualificationDetails[${defidx.index}].attributeValue" />
        </c:if>
    </c:forEach>

    <c:forEach items="${quickFinder.lookupParameters}" var="lookupInput" varStatus="lookupIdx">
        <c:if test="${lookupInput.key == searchStr}">
            <c:set var="params" value="${params},${pathPrefix}.roleQualificationDetails[${defidx.index}].attributeValue:${lookupInput.value}" />
        </c:if>
    </c:forEach>
</c:forEach>

<c:set var="fieldConversion" value="${fn:substringAfter(fieldConversion, ',')}" />
<c:set var="params" value="${fn:substringAfter(params, ',')}" />

<kul:lookup boClassName="${quickFinder.dataObjectClass}" fieldConversions="${fieldConversion}" lookupParameters="${params}" />
