<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2024 Kuali, Inc.

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
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="docTitle" required="true" description="The title to display for the page." %>
<%@ attribute name="docTitleClass" required="false" description="The class added to the title of the page." %>
<%@ attribute name="transactionalDocument" required="true" description="The name of the document type this document page is rendering." %>
<%@ attribute name="showDocumentInfo" required="false" description="Boolean value of whether to display the Document Type name and document type help on the page." %>
<%@ attribute name="headerMenuBar" required="false" description="HTML text for menu bar to display at the top of the page." %>
<%@ attribute name="headerTitle" required="false" description="The title of this page which will be displayed in the browser's header bar.  If left blank, docTitle will be used instead." %>
<%@ attribute name="htmlFormAction" required="false" description="The URL that the HTML form rendered on this page will be posted to." %>
<%@ attribute name="renderMultipart" required="false" description="Boolean value of whether the HTML form rendred on this page will be encoded to accept multipart - ie, uploaded attachment - input." %>
<%@ attribute name="showTabButtons" required="false" description="Whether to show the show/hide all tabs buttons." %>
<%@ attribute name="extraTopButtons" required="false" type="java.util.List" description="A List of org.kuali.kfs.kns.web.ui.ExtraButton objects to display at the top of the page." %>
<%@ attribute name="headerDispatch" required="false" description="Overrides the header navigation tab buttons to go directly to the action given here." %>
<%@ attribute name="lookup" required="false" description="indicates whether the lookup page specific page should be shown"%>
<%@ attribute name="disableLegacyStyles" description="Boolean determining whether the legacy css should be loaded." %>

<%-- for non-lookup pages --%>
<%@ attribute name="headerTabActive" required="false" description="The name of the active header tab, if header navigation is used." %>
<%@ attribute name="defaultMethodToCall" required="false" description="The name of default methodToCall on the action for this page." %>
<%@ attribute name="errorKey" required="false" description="If present, this is the key which will be used to match errors that need to be rendered at the top of the page." %>
<%@ attribute name="additionalScriptFiles" required="false" type="java.util.List" description="A List of JavaScript file names to have included on the page." %>
<%@ attribute name="documentWebScope" required="false" description="The scope this page - which is hard coded to session, making this attribute somewhat useless." %>
<%@ attribute name="maintenanceDocument" required="false" description="Boolean value of whether this page is rendering a maintenance document." %>
<%@ attribute name="sessionDocument" required="false" description="Unused." %>
<%@ attribute name="alternativeHelp" required="false"%>
<%@ attribute name="renderInnerDiv" required="false"%>
<%@ attribute name="openNav" required="false"%>
<%@ attribute name="placeFocus" required="false" description="A way to prevent the body onLoad placeFocus() from executing" %>
<%@ attribute name="additionalBodyClass" required="false" description="Additional css class to add to the body tag."%>

<%-- Is the screen an inquiry? --%>
<c:set var="_isInquiry" value="${requestScope[KRADConstants.PARAM_MAINTENANCE_VIEW_MODE] eq KRADConstants.PARAM_MAINTENANCE_VIEW_MODE_INQUIRY}" />

<!DOCTYPE html>
<html:html>

	<c:if test="${empty headerTitle}">
		<c:set var="headerTitle" value="${docTitle}"/>
	</c:if>

	<head>
		<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon">
		<script type="text/javascript">var breadcrumbs = []</script>
		<c:if test="${not empty SESSION_TIMEOUT_WARNING_MILLISECONDS}">
			<script type="text/javascript">
				<!--
				setTimeout("alert('Your session will expire in ${SESSION_TIMEOUT_WARNING_MINUTES} minutes.')",'${SESSION_TIMEOUT_WARNING_MILLISECONDS}');
				// -->
			</script>
		</c:if>

		<script type="text/javascript">var jsContextPath = "${pageContext.request.contextPath}";</script>
		<title><bean:message key="app.title" /> :: <c:out value="${headerTitle}"/></title>
		<c:forEach items="${fn:split(ConfigProperties.kns.css.files, ',')}"
				   var="cssFile">
			<c:if test="${fn:length(fn:trim(cssFile)) > 0}">
				<link href="${pageContext.request.contextPath}/${cssFile}"
					  rel="stylesheet" type="text/css" />
			</c:if>
		</c:forEach>
		<c:forEach items="${fn:split(ConfigProperties.kns.javascript.files, ',')}"
				   var="javascriptFile">
			<c:if test="${fn:length(fn:trim(javascriptFile)) > 0}">
				<script language="JavaScript" type="text/javascript"
						src="${pageContext.request.contextPath}/${javascriptFile}"></script>
			</c:if>
		</c:forEach>

		<script type="text/javascript">
			var jq = jQuery.noConflict();
		</script>

		<c:choose>
			<c:when test="${lookup}" >
				<c:if test="${not empty KualiForm.headerNavigationTabs}">
					<link href="kr/css/${KualiForm.navigationCss}" rel="stylesheet" type="text/css" />
				</c:if>

				<script language="JavaScript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/lookup.js"></script>
			</c:when>
			<c:otherwise>
				<c:forEach items="${additionalScriptFiles}" var="scriptFile" >
					<script language="JavaScript" type="text/javascript" src="${scriptFile}"></script>
				</c:forEach>
			</c:otherwise>
		</c:choose>
		<c:if test="${not disableLegacyStyles}">
			<link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.min.css">
			<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
		</c:if>
		<link href='${pageContext.request.contextPath}/css/lookup.css?${cachingTimestamp}' rel='stylesheet' type='text/css'>
		<link href='${pageContext.request.contextPath}/css/newPortal.css?${cachingTimestamp}' rel='stylesheet' type='text/css'>
		<link href='https://fonts.googleapis.com/css?family=Roboto:300,400,500,700,900' rel='stylesheet'>
		<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet" type="text/css">
		<c:if test="${param.mode ne 'modal'}">
			<script src="${pageContext.request.contextPath}/scripts/jquery.min.js"></script>
			<script src="${pageContext.request.contextPath}/scripts/bootstrap.min.js"></script>
			<script src="${pageContext.request.contextPath}/scripts/remodal.min.js"></script>
			<link rel="stylesheet" href="${pageContext.request.contextPath}/css/remodal.min.css">
		</c:if>
		<kul:analytics />
		<script src="https://unpkg.com/@reduxjs/toolkit@1.9.3/dist/redux-toolkit.umd.min.js"></script>
		<script src="${pageContext.request.contextPath}/scripts/redux-shim/redux-shim.js"></script>
	</head>
	<c:choose>
		<c:when test="${lookup}" >
			<body onload="hideHeader();
				<c:if test='${placeFocus or empty placeFocus}'>
					<c:out value="placeFocus();"/>
				</c:if>

				<c:if test='<%= jspContext.findAttribute("KualiForm") != null %>'>
					<c:if test='<%= jspContext.findAttribute("KualiForm").getClass() == org.kuali.kfs.kns.web.struts.form.LookupForm.class %>'>
						<c:out value ="${KualiForm.lookupable.extraOnLoad}" />
					</c:if>
				</c:if>
			">
		</c:when>
		<c:otherwise>
			<%-- CU Customization: use setTimeout() to delay executing the restoreScrollPosition() function, to fix an IE browser issue. --%>
			<body onload="
				<c:if test='${placeFocus or empty placeFocus}'>
					<c:out value="placeFocus();"/>
				</c:if>
				setTimeout(restoreScrollPosition, 1);
			"
			onKeyPress="return isReturnKeyAllowed('${KRADConstants.DISPATCH_REQUEST_PARAMETER}.' , event);">
		</c:otherwise>
	</c:choose>

		<kul:stayOnPage active="${transactionalDocument || maintenanceDocument}"/>

		<kul:pageBody showDocumentInfo="${showDocumentInfo}" docTitle="${docTitle}" docTitleClass="${docTitleClass}"
				  htmlFormAction="${htmlFormAction}" transactionalDocument="${transactionalDocument}"
				  renderMultipart="${renderMultipart}" showTabButtons="${showTabButtons}" headerDispatch="${headerDispatch}"
				  defaultMethodToCall="${defaultMethodToCall}" lookup="${lookup}" extraTopButtons="${extraTopButtons}"
				  headerMenuBar="${headerMenuBar}" headerTabActive="${headerTabActive}" alternativeHelp="${alternativeHelp}"
				  errorKey="${errorKey}"
				  documentWebScope="${documentWebScope}" maintenanceDocument="${maintenanceDocument}"
				  renderInnerDiv="${renderInnerDiv}" cachingTimestamp="${cachingTimestamp}" openNav="${openNav}"
				  additionalBodyClass="${additionalBodyClass}">

			<div id="page-content">
                <jsp:doBody/>
            </div>
		</kul:pageBody>

		<kul:modal/>
	</body>
</html:html>
