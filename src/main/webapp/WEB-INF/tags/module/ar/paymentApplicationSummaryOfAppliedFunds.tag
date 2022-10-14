<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2022 Kuali, Inc.

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
<%@ attribute name="hasRelatedCashControlDocument" required="true"
	description="If has related cash control document"%>
<%@ attribute name="readOnly" required="true"
	description="If document is in read only mode"%>
<%@ attribute name="isCustomerSelected" required="true"
    description="Whether or not the customer is set" %>
<c:set var="docHeaderAttributes" value="${DataDictionary.DocumentHeader.attributes}" />
<c:set var="invoicePaidAppliedAttributes" value="${DataDictionary.InvoicePaidApplied.attributes}" />
<kul:tab tabTitle="Summary of Applied Funds"
	defaultOpen="${isCustomerSelected}"
	tabErrorKey="${KFSConstants.PAYMENT_APPLICATION_DOCUMENT_ERRORS}">
	<div class="tab-container" align="center">
		<c:choose>
			<c:when test="${!isCustomerSelected}">
		    		No Customer Selected
	    	</c:when>
			<c:otherwise>
			    <h3>Summary of Applied Funds</h3>
				<table class="standard side-margins" width="100%" cellpadding="0" cellspacing="0">
					<tr>
						<td style='vertical-align: top;' colspan='2'>
							<c:choose>
								<c:when test="${empty KualiForm.document.invoicePaidApplieds}">
								   		No applied payments.
							   	</c:when>
								<c:otherwise>
									<table width="100%" cellpadding="0" cellspacing="0" class="datatable" style="border-right: 1px solid #c3c3c3;">
										<tr>
											<td colspan='4' class='tab-subhead'>
												<h4>Applied Funds</h4>
											</td>
										</tr>
										<tr>
											<th>
												Invoice Nbr
											</th>
											<th>
												Item #
											</th>
											<th>
												Inv Item Desc
											</th>
											<th style="text-align: right">
												Applied Amount
											</th>
										</tr>
										<logic:iterate id="invoicePaidApplied" name="KualiForm"
											property="document.invoicePaidApplieds" indexId="ctr">
											<tr>
												<td>
													<a href="${ConfigProperties.application.url}/DocHandler.do?docId=${invoicePaidApplied.financialDocumentReferenceInvoiceNumber}&command=displayDocSearchView" target="blank">
														<c:out value="${invoicePaidApplied.financialDocumentReferenceInvoiceNumber}" />
													</a>
												</td>
												<td>
													<kul:htmlControlAttribute attributeEntry="${invoicePaidAppliedAttributes.invoiceItemNumber}" property="document.invoicePaidApplieds[${ctr}].invoiceItemNumber" readOnly="true" />
												</td>
												<td>
													<kul:htmlControlAttribute attributeEntry="${DataDictionary.CustomerInvoiceDetail.attributes.invoiceItemDescription}" property="document.invoicePaidApplieds[${ctr}].invoiceDetail.invoiceItemDescription" readOnly="true" />
												</td>
												<td style="text-align: right;">
													<kul:htmlControlAttribute attributeEntry="${invoicePaidAppliedAttributes.invoiceItemAppliedAmount}" property="document.invoicePaidApplieds[${ctr}].invoiceItemAppliedAmount" readOnly="true" />
												</td>
                                                <c:if test="${readOnly ne true}">
                                                    <td>
                                                        <html:html-button
                                                            property="methodToCall.deleteInvoicePaidApplied.line${ctr}"
                                                            alt="Delete Item ${ctr}"
                                                            title="Delete Item ${ctr}"
                                                            styleClass="btn clean"
                                                            value="Delete"
                                                            innerHTML="<span class=\"fa fa-trash\"></span>">
                                                        </html:html-button>
                                                    </td>
                                                </c:if>
											</tr>
										</logic:iterate>
									</table>
								</c:otherwise>
							</c:choose>
						</td>
						<td valign='top'>
                            <c:set var="showCCAndBtbA" value="${hasRelatedCashControlDocument}"/>
                            <table class='datatable'>
								<tr>
									<td colspan='3' class='tab-subhead'>
										<h4>Unapplied Funds</h4>
									</td>
								</tr>
								<tr>
									<c:if test="${!showCCAndBtbA}">
		                        	    <c:if test="${readOnly ne true}">
											<th class='tab-subhead' style="text-align: right">
												Total Unapplied Funds
											</th>
											<th class='tab-subhead' style="text-align: right">
												Open Amount
											</th>
										</c:if>
									</c:if>
									<c:if test="${showCCAndBtbA}">
										<th class='tab-subhead' style="text-align: right">
											Cash Control
										</th>
										<th class='tab-subhead' style="text-align: right">
											Open Amount
										</th>
									</c:if>
									<th class='tab-subhead' style="text-align: right">
										Applied Amount
                                    </th>
								</tr>
								<tr>
								    <c:if test="${!showCCAndBtbA}">
		                        	    <c:if test="${readOnly ne true}">
											<td style="text-align: right;">
												<kul:htmlControlAttribute attributeEntry="${docHeaderAttributes.financialDocumentTotalAmount}" property="totalFromControl" readOnly="true" />
											</td>
											<td style="text-align: right;">
												<kul:htmlControlAttribute attributeEntry="${docHeaderAttributes.financialDocumentTotalAmount}" property="unallocatedBalance" readOnly="true" />
											</td>
										</c:if>
									</c:if>
									<c:if test="${showCCAndBtbA}">
										<td style="text-align: right;">
											 <kul:htmlControlAttribute attributeEntry="${docHeaderAttributes.financialDocumentTotalAmount}" property="document.documentHeader.financialDocumentTotalAmount" readOnly="true" />
										</td>
										<td style="text-align: right;">
											<kul:htmlControlAttribute attributeEntry="${docHeaderAttributes.financialDocumentTotalAmount}" property="unallocatedBalance" readOnly="true" />
										</td>
									</c:if>
									<td style="text-align: right;">
										<kul:htmlControlAttribute attributeEntry="${docHeaderAttributes.financialDocumentTotalAmount}" property="totalApplied" readOnly="true" />
									</td>
								</tr>
							</table>
						<td>
					</tr>
				</table>
			</c:otherwise>
		</c:choose>
	</div>
</kul:tab>
