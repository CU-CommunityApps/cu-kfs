<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2023 Kuali, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%--
    Modified by Cornell University 2013-2016 to add custom error message.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<c:set var="parameters"
       value="<%=request.getAttribute(\"org.kuali.kfs.kns.web.struts.action.AuthorizationExceptionAction\")%>"/>

<c:if test="${not empty parameters}">
    <c:set var="message" value="${parameters.message}"/>
    <c:if test="${empty message}">
        <c:set var="exception" value='<%=request.getAttribute("org.apache.struts.action.EXCEPTION")%>'/>
        <c:set var="message" value="${exception['class'].name}"/>
    </c:if>
</c:if>

<kul:page showDocumentInfo="false"
          headerTitle="Authorization Exception"
          docTitle="Authorization Exception Report"
          transactionalDocument="false"
          htmlFormAction="authorizationExceptionReport"
          defaultMethodToCall="notify"
          errorKey="*">

    <html:hidden property="message" write="false" value="${message}"/>

    <div class="center">
        <font color="blue" size="3">
            We're sorry, you are currently not authorized to perform this KFS function.
        </font>
        <br/><br/>
        <font color="blue" size="2">
            If you were performing a lookup, be sure you click on "return value" to make your selection.
            <br/>
            If you feel you should have access to this function, please contact your Financial Transaction Center or
            Business Service Center.
        </font>
    </div>
    <div class="center">
        <strong>Error Message:</strong>
            ${message}
    </div>
</kul:page>
