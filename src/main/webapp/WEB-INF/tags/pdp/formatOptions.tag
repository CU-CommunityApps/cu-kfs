<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2019 Kuali, Inc.

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

<%@ attribute name="paymentGroupAttributes" required="true"
	type="java.util.Map"
	description="The DataDictionary entry containing attributes for disbursement number range."%>

<kul:tab tabTitle="Format Options" defaultOpen="true" tabErrorKey="ranges*">
	<div id="formatOptions" class="tab-container" align=center>
        <table class="standard" summary="Format Options">
			<tr>
				<td colspan="3" class="subhead">
					Enter a Pay Date and any other selection criteria required
				</td>
			</tr>
			<tr>
			
			<tr>
                <th class="right" width="50%">
                    <kul:htmlAttributeLabel attributeEntry="${paymentGroupAttributes.paymentDate}" labelFor="paymentDate" />
                    <br><font size="1"> Ex. 11/26/2004</font>
                </th>
                <td class="datacell" align="left">
                    <kul:dateInput attributeEntry="${paymentGroupAttributes.paymentDate}" property="paymentDate"/>
                </td>
            </tr>
            <tr>
                <th class="right">Only Disbursements Flagged as Immediate:</th>
                <td><html:checkbox property="paymentTypes" value="immediate"/></td>
            </tr>
            <tr>
                <th class="right">All Payment Types:</th>
                <td><html:radio property="paymentTypes" value="all"/></td>
            </tr>
            <tr>
                <th class="right">Only Disbursements with Attachments:</th>
                <td><html:radio property="paymentTypes" value="pymtAttachment"/></td>
            </tr>
            <tr>
                <th class="right" nowrap="nowrap">Only Disbursements with No Attachments:</th>
                <td><html:radio property="paymentTypes" value="pymtAttachmentFalse"/></td>
            </tr>
            <tr>
                <th class="right">Only Disbursements with Special Handling:</th>
                <td><html:radio property="paymentTypes" value="pymtSpecialHandling"/></td>
            </tr>
            <tr>
                <th class="right">Only Disbursements with No Special Handling:</th>
                <td><html:radio property="paymentTypes" value="pymtSpecialHandlingFalse"/></td>
            </tr>
            <tr>
                <td colspan="3" class="subhead">
                    Select which payment methods you want to format
                </td>
            </tr>
            <tr>
                <th class="right">All Payments:</th>
                <td><html:radio property="paymentDistribution" value="all"/></td>
            </tr>
            <tr>
                <th class="right">ACH Payments Only:</th>
                <td><html:radio property="paymentDistribution" value="achOnly"/></td>
            </tr>
            <tr>
                <th class="right">Check Payments Only:</th>
                <td><html:radio property="paymentDistribution" value="checkOnly"/></td>
            </tr>
		</table>
	</div>
</kul:tab>
    
