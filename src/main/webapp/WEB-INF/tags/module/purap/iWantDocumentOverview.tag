<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp" %>

<%@ attribute name="readOnly" required="true" %>

<c:set var="docHeaderAttributes" value="${DataDictionary.DocumentHeader.attributes}"/>
<c:set var="iWantDocAttributes" value="${DataDictionary.IWantDocument.attributes}"/>

<div class="tab-container" align="center">
    <html:hidden property="document.documentHeader.documentNumber"/>
    <h3>Document Overview</h3>
    <table class="standard side-margins"
           title="view/edit document overview information"
           summary="view/edit document overview information">
        <tr>
            <kul:htmlAttributeHeaderCell
                    labelFor="document.documentHeader.documentDescription"
                    attributeEntry="${docHeaderAttributes.documentDescription}"
                    horizontal="true"
                    addClass="right top"
                    width="25%"/>
            <td class="top" width="25%">
                <kul:htmlControlAttribute
                        property="document.documentHeader.documentDescription"
                        attributeEntry="${docHeaderAttributes.documentDescription}"
                        readOnly="${!KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT_DOCUMENT_OVERVIEW]}"/>
            </td>
            <kul:htmlAttributeHeaderCell
                    labelFor="document.documentHeader.explanation"
                    attributeEntry="${iWantDocAttributes.explanation}"
                    horizontal="true"
                    rowspan="2"
                    addClass="right top"
                    width="25%"/>
            <td rowspan="2" class="top" width="25%">
                <kul:htmlControlAttribute
                        property="document.documentHeader.explanation"
                        attributeEntry="${iWantDocAttributes.explanation}"
                        readOnly="${readOnly}"
                        readOnlyAlternateDisplay="${KualiForm.document.documentHeader.explanation}" />
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td colspan="4"
                style="color: blue; font-family:Verdana, Verdana, serif; font-size: 12px;font-style: italic">
                <b>NOTE:</b> The Business Purpose should tie the purchase to the account being charged; what is being
                purchased, why it is being purchased, how it will be used.
                <ul>
                    <li><b>Not</b> an adequate Business Purpose = "Lab Supplies"</li>
                    <li>Adequate Business Purpose = "Electronic components for sample analyzer used in Prof. Smith's XYZ
                        project (or OSP 12345)."
                    </li>
                </ul>
            </td>
        </tr>
        <c:if test="${!empty(KualiForm.document.documentHeader.organizationDocumentNumber)}">
            <tr>
                <kul:htmlAttributeHeaderCell
                        labelFor="document.documentHeader.organizationDocumentNumber"
                        attributeEntry="${docHeaderAttributes.organizationDocumentNumber}"
                        horizontal="true"
                        addClass="right" />
                <td>
                    <kul:htmlControlAttribute
                            property="document.documentHeader.organizationDocumentNumber"
                            attributeEntry="${docHeaderAttributes.organizationDocumentNumber}"
                            readOnly="${readOnly}"/>
                </td>
            </tr>
        </c:if>
    </table>
    <jsp:doBody/>
</div>