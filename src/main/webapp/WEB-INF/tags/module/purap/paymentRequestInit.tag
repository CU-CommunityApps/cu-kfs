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

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>

<%@ attribute name="displayPaymentRequestInitFields" required="false"
              description="Boolean to indicate if PO specific fields should be displayed" %>

<kul:tabTop tabTitle="Payment Request Initiation" defaultOpen="true" tabErrorKey="*">
    <div class="tab-container" align=center>
        <table class="datatable standard" summary="Payment Request Initiation Section">
            <tr>
                <th class="right" width="25%">
                   <kul:htmlAttributeLabel attributeEntry="${documentAttributes.purchaseOrderIdentifier}" />
                </th>
                <td class="datacell" width="25%">
                   <kul:htmlControlAttribute
                   		attributeEntry="${documentAttributes.purchaseOrderIdentifier}"
                        property="document.purchaseOrderIdentifier"/>
                </td>
                <th class="right" width="25%">
                   <kul:htmlAttributeLabel attributeEntry="${documentAttributes.invoiceNumber}" />
                </th>
                <td class="datacell" width="25%">
                   <kul:htmlControlAttribute
                   		attributeEntry="${documentAttributes.invoiceNumber}" property="document.invoiceNumber" />
                </td>
            </tr>
            <tr>
                <th class="right" width="25%">
                   <kul:htmlAttributeLabel attributeEntry="${documentAttributes.invoiceDate}" />
                </th>
                <td class="datacell" width="25%">
                   <%-- CU Customization: Add onblur and onchange handlers to the Invoice Date field. --%>
                   <kul:htmlControlAttribute
                   		attributeEntry="${documentAttributes.invoiceDate}" property="document.invoiceDate" datePicker="true"
                        onblur="populateInvoiceReceivedDateIfNecessary(this)"
                        onchange="populateInvoiceReceivedDateIfNecessary(this)"/>
                </td>
                <th class="right" width="25%">
                   <kul:htmlAttributeLabel  attributeEntry="${documentAttributes.vendorInvoiceAmount}" />
                </th>
                <td class="datacell" width="25%">
                   <kul:htmlControlAttribute
                   		attributeEntry="${documentAttributes.vendorInvoiceAmount}" property="document.vendorInvoiceAmount" />
                </td>
            </tr>
            <tr>
                <th class="right" width="25%">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.invoiceReceivedDate}" />
                </th>
                <td class="datacell" width="25%">
                    <kul:htmlControlAttribute
                            attributeEntry="${documentAttributes.invoiceReceivedDate}" property="document.invoiceReceivedDate" datePicker="true"/>
                </td>
                <th class="right top" width="25%">
                   <kul:htmlAttributeLabel attributeEntry="${documentAttributes.specialHandlingInstructionLine1Text}" />
                </th>
                <td class="datacell" width="25%">
                   <kul:htmlControlAttribute
                   		attributeEntry="${documentAttributes.specialHandlingInstructionLine1Text}"
                        property="document.specialHandlingInstructionLine1Text"  />
                   <br/>
                   <kul:htmlControlAttribute
                   		attributeEntry="${documentAttributes.specialHandlingInstructionLine2Text}"
                        property="document.specialHandlingInstructionLine2Text" />
                   <br/>
                   <kul:htmlControlAttribute
                   		attributeEntry="${documentAttributes.specialHandlingInstructionLine3Text}"
                        property="document.specialHandlingInstructionLine3Text" />
                </td>
            </tr>
		</table>
    </div>

</kul:tabTop>
