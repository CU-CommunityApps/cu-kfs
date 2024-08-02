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
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<SCRIPT type="text/javascript">
    function hideImport() {
        document.getElementById("showLink").style.display="inline";
        document.getElementById("uploadDiv").style.display="none";
    }
    function showImport() {
        document.getElementById("showLink").style.display="none";
        document.getElementById("uploadDiv").style.display="inline";
    }
</SCRIPT>

<%@ attribute name="documentAttributes" required="true"
              type="java.util.Map"
              description="The DataDictionary entry containing attributes for cash control document fields."%>
<%@ attribute name="cashControlDetailAttributes" required="true"
              type="java.util.Map"
              description="The DataDictionary entry containing attributes for cash control detail fields."%>
<%@ attribute name="readOnly" required="true"
              description="If document is in read only mode"%>
<%@ attribute name="editDetails" required="true"
              description="If document details are in edit mode"%>
<%@ attribute name="editPaymentAppDoc" required="true"
              description="If payment application document number should be a link"%>

<kul:tab tabTitle="Cash Control Details" defaultOpen="true" tabErrorKey="${KFSConstants.CASH_CONTROL_DETAILS_ERRORS}"
         helpUrl="${KualiForm.detailsImportInstructionsUrl}" helpLabel="Import Templates">
    <div id="cashControlDetails" class="tab-container">
        <table class="datatable standard">
            <tbody>
            <tr>
                <td class="tab-subhead-import" nowrap="nowrap" style="border-right: none; text-align: right">
                    <input id="showLink" title="Import Details From File" class="btn btn-default uppercase"
                           type="button" alt="Import Cash Control Detail" value="Import"
                           onclick="showImport(); return false;" />
                    <div id="uploadDiv" style="display:none; float:right;" >
                        <html:file size="30" property="detailImportFile" />
                        <html:submit styleClass="btn btn-green" property="methodToCall.importDetails"
                                     alt="Add Imported Items" title="Add Imported Items" value="Add"/>
                        <input title="Cancel Import" class="btn btn-default uppercase" type="submit"
                               alt="Cancel Import" value="Cancel Import" onclick="hideImport(); return false;" />
                    </div>
                </td>
            </tr>
            </tbody>
        </table>

        <table cellpadding="0" cellspacing="0" class="datatable standard" summary="Cash control Details">
            <tr class="header">
                <kul:htmlAttributeHeaderCell literalLabel="&nbsp;" />
                <kul:htmlAttributeHeaderCell attributeEntry="${cashControlDetailAttributes.documentNumber}" />
                <kul:htmlAttributeHeaderCell attributeEntry="${cashControlDetailAttributes.status}" />
                <kul:htmlAttributeHeaderCell attributeEntry="${cashControlDetailAttributes.customerNumber}" />
                <kul:htmlAttributeHeaderCell attributeEntry="${cashControlDetailAttributes.customerPaymentMediumIdentifier}" />
                <kul:htmlAttributeHeaderCell attributeEntry="${cashControlDetailAttributes.customerPaymentDate}" />
                <kul:htmlAttributeHeaderCell attributeEntry="${cashControlDetailAttributes.financialDocumentLineAmount}" />
                <c:if test="${editDetails}">
                    <kul:htmlAttributeHeaderCell literalLabel="Actions" />
                </c:if>
            </tr>
            <c:if test="${editDetails}">
                <ar:cashControlDetail propertyName="newCashControlDetail"
                                      cashControlDetailAttributes="${cashControlDetailAttributes}"
                                      addLine="true" readOnly="${readOnly}" rowHeading="&nbsp;"
                                      editPaymentAppDoc="${editPaymentAppDoc}"
                                      rowClass="highlight"
                                      cssClass="infoline"
                                      actionMethod="addCashControlDetail"
                                      actionAlt="Add Cash Control Detail"
                                      actionText="Add"
                                      actionButtonClass="btn-green"/>
            </c:if>
            <logic:iterate id="cashControlDetail" name="KualiForm" property="document.cashControlDetails" indexId="ctr">
                <c:set var="rowClass" value=""/>
                <c:choose>
                    <c:when test="${editDetails}">
                        <c:if test="${(ctr + 1) % 2 == 0}">
                            <c:set var="rowClass" value="highlight"/>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${ctr % 2 == 0}">
                            <c:set var="rowClass" value="highlight"/>
                        </c:if>
                    </c:otherwise>
                </c:choose>
                <c:set var="referenceFinancialDocumentStatus"
                       value="${cashControlDetail.referenceFinancialDocument.documentHeader.workflowDocument.document.status}"
                />
                <c:set var="readonlyDocumentStatus"
                       value="${'FINAL' eq referenceFinancialDocumentStatus
							  || 'CANCELED' eq referenceFinancialDocumentStatus
							  || 'DISAPPROVED' eq referenceFinancialDocumentStatus
								|| 'ENROUTE' eq referenceFinancialDocumentStatus}"
                />
                <ar:cashControlDetail
                        propertyName="document.cashControlDetail[${ctr}]"
                        cashControlDetailAttributes="${cashControlDetailAttributes}"
                        addLine="false"
                        readOnly="${readonlyDocumentStatus || !editDetails}"
                        rowHeading="${ctr+1}"
                        editPaymentAppDoc="${editPaymentAppDoc}"
                        rowClass="${rowClass}"
                        cssClass="datacell"
                        actionMethod="cancelCashControlDetail.line${ctr}"
                        actionAlt="Cancel Cash Control Detail"
                        actionText="Cancel"
                        actionButtonClass="btn-red" />
            </logic:iterate>
            <tr>
                <td class="total-line" colspan="6">
                    &nbsp;
                </td>
                <td class="total-line">
                    <strong>Total:
                            ${KualiForm.document.currencyFormattedTotalCashControlAmount}</strong>
                </td>
                <c:if test="${!readOnly}">
                    <td class="total-line">
                        &nbsp;
                    </td>
                </c:if>
            </tr>

        </table>
    </div>
</kul:tab>