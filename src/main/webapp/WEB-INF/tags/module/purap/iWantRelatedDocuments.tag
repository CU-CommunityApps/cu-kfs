<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<kul:tab tabTitle="View Related Documents" defaultOpen="false" tabErrorKey="${PurapConstants.RELATED_DOCS_TAB_ERRORS}">
    <div class="tab-container">
        <h3>Related Documents</h3>
        <h3>
            ${KualiForm.document.dvDocumentLabel} -
            <a href="<c:out value='${KualiForm.document.dvUrl}' />" target="_BLANK">
                <c:out value="${KualiForm.document.dvDocId}"/>
            </a>
        </h3>
    </div>
</kul:tab>
