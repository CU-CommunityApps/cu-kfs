<%--
    Custom variant of documentPage that supports showing the document content as a modal inquiry,
    if the document is set up to be used as an inquiry page in certain cases.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="modalInquiryMode" required="true" description="Boolean value of whether this page should be rendered as an inquiry within a modal popup box." %>
<%@ attribute name="documentTypeName" required="true" description="The name of the document type this document page is rendering." %>

<%@ attribute name="showDocumentInfo" required="false" description="Boolean value of whether to display the Document Type name and document type help on the page." %>
<%@ attribute name="headerMenuBar" required="false" description="HTML text for menu bar to display at the top of the page." %>
<%@ attribute name="headerTitle" required="false" description="The title of this page which will be displayed in the browser's header bar.  If left blank, docTitle will be used instead." %>
<%@ attribute name="htmlFormAction" required="false" description="The URL that the HTML form rendered on this page will be posted to." %>
<%@ attribute name="renderMultipart" required="false" description="Boolean value of whether the HTML form rendred on this page will be encoded to accept multipart - ie, uploaded attachment - input." %>
<%@ attribute name="showTabButtons" required="false" description="Whether to show the show/hide all tabs buttons." %>
<%@ attribute name="extraTopButtons" required="false" type="java.util.List" %>
<%@ attribute name="headerDispatch" required="false" description="A List of org.kuali.kfs.kns.web.ui.ExtraButton objects to display at the top of the page." %>
<%@ attribute name="headerTabActive" required="false" description="The name of the active header tab, if header navigation is used." %>
<%@ attribute name="docTitle" required="false" %>
<%@ attribute name="alternativeHelp" required="false"%>

<c:set var="currentDocumentEntry" value="${DataDictionary[documentTypeName]}"/>
<c:set var="currentDocumentTitle" value="${docTitle != null ? docTitle : currentDocumentEntry.label}"/>

<c:choose>
    <c:when test="${modalInquiryMode}">
        <c:set var="lookup" value="true"/>
        <kul:pageBody showDocumentInfo="${showDocumentInfo}"
                docTitle="${currentDocumentTitle}"
                htmlFormAction="${htmlFormAction}"
                transactionalDocument="${currentDocumentEntry.transactionalDocument}"
                renderMultipart="${renderMultipart}"
                showTabButtons="${showTabButtons}"
                defaultMethodToCall="${defaultMethodToCall}"
                lookup="${lookup}"
                headerMenuBar="${headerMenuBar}"
                alternativeHelp="${alternativeHelp}">
            <jsp:doBody/>
        </kul:pageBody>
    </c:when>
    <c:otherwise>
        <kul:documentPage documentTypeName="${documentTypeName}"
                showDocumentInfo="${showDocumentInfo}"
                headerMenuBar="${headerMenuBar}"
                headerTitle="${headerTitle}"
                htmlFormAction="${htmlFormAction}"
                renderMultipart="${renderMultipart}"
                showTabButtons="${showTabButtons}"
                extraTopButtons="${extraTopButtons}"
                headerDispatch="${headerDispatch}"
                headerTabActive="${headerTabActive}"
                docTitle="${currentDocumentTitle}"
                alternativeHelp="${alternativeHelp}">
            <jsp:doBody/>
        </kul:documentPage>
    </c:otherwise>
</c:choose>