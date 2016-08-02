<%--

    Copyright 2005-2015 The Kuali Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.opensource.org/licenses/ecl2.php

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%--
    Modified by Cornell University 2013-2016 to add custom error message.
--%>
<%@ page import="org.kuali.kfs.krad.exception.AuthorizationException"%>
<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp" %>

<c:set var="parameters"
       value="<%=request.getAttribute(\"org.kuali.kfs.kns.web.struts.action.AuthorizationExceptionAction\")%>" />

<c:if test="${not empty parameters}">
	<c:set var="message" value="${parameters.message}" />
  <c:if test="${empty message}">
    <c:set var="exception" value='<%=request.getAttribute("org.apache.struts.action.EXCEPTION")%>'/>
    <c:set var="message" value="${exception['class'].name}" />
  </c:if>
</c:if>

<kul:page showDocumentInfo="false"
	      headerTitle="Authorization Exception"
	      docTitle="Authorization Exception Report"
	      transactionalDocument="false"
	      htmlFormAction="authorizationExceptionReport"
	      defaultMethodToCall="notify"
	      errorKey="*">

	<html:hidden property="message" write="false" value="${message}" />

	<div class="center">
		<font color="blue" size="3">
		We're sorry, you are currently not authorized to perform this KFS function.
		</font>
		<br/><br/>
		<font color="blue" size="2">
		If you were performing a lookup, be sure you click on "return value" to make your selection.
		<br/>
		If you feel you should have access to this function, please contact your Financial Transaction Center or Business Service Center.
		</font>
	</div>
	<div class="center">
		<strong>Error Message:</strong>
			${message}
	</div>
</kul:page>
