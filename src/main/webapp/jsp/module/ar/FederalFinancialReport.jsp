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
<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%--
    CU Customization: Backported the FINP-7147 changes into this file.
    This overlay can be removed when we upgrade to the 2023-06-28 financials patch.
--%>

<c:set var="award" value="${DataDictionary.Award.attributes}"/>
<c:set var="agency" value="${DataDictionary.Agency.attributes}"/>
<c:set var="organization" value="${DataDictionary.OrganizationAccountingDefault.attributes}"/>

<kul:page showDocumentInfo="false"
          headerTitle="Federal Financial Report Generation"
          docTitle="Federal Financial Report Generation" renderMultipart="true"
          transactionalDocument="false" htmlFormAction="arFederalFinancialReport"
          errorKey="foo">
    <script>
    function hasFormAlreadyBeenSubmitted() {
        // don't block the resubmission of the form
        return true;
    }
    </script>
    <div id="lookup">
        <div class="main-panel">
            <div class="headerarea-small"></div>
            <table class="standard" style="margin: 20px auto 0 auto;" summary="Federal Financial Report Generation"
            ">
            <tr>
                <th class="right" width="50%">
                    <label for="federalForm">Federal Form:</label>
                </th>
                <td width="50%">
                    <html-el:select property="federalForm">
                        <html-el:option value=""></html-el:option>
                        <html-el:option value="425">SF425</html-el:option>
                        <html-el:option value="425A">SF425A</html-el:option>
                    </html-el:select>
                </td>
            </tr>

            <tr>
                <th>
                    <label>
                        <kul:htmlAttributeLabel attributeEntry="${award.proposalNumber}" readOnly="true"/>
                    </label>
                </th>
                <td>
                    <kul:htmlControlAttribute attributeEntry="${award.proposalNumber}" property="proposalNumber"/>
                    <kul:lookup boClassName="org.kuali.kfs.module.cg.businessobject.Award"/>
                </td>
            </tr>
            <tr>
                <th>
                    <label>
                        <kul:htmlAttributeLabel attributeEntry="${agency.agencyNumber}" readOnly="true"/>
                    </label>
                </th>
                <td>
                    <kul:htmlControlAttribute attributeEntry="${agency.agencyNumber}" property="agencyNumber"/>
                    <kul:lookup boClassName="org.kuali.kfs.module.cg.businessobject.Agency"/>
                </td>
            </tr>
            <tr>
                <th>
                    <label for="fiscalYear">Print invoices for Calendar Year:</label>
                </th>
                <td>
                    <kul:htmlControlAttribute attributeEntry="${organization.universityFiscalYear}" property="fiscalYear"/>
                </td>
            </tr>
            <tr>
                <th>
                    <label for="reportingPeriod">Reporting Period:</label>
                </th>
                <td>
                    <html-el:select property="reportingPeriod">
                        <html-el:option value=""></html-el:option>
                        <html-el:option value="q1">Quarter 1</html-el:option>
                        <html-el:option value="q2">Quarter 2</html-el:option>
                        <html-el:option value="q3">Quarter 3</html-el:option>
                        <html-el:option value="q4">Quarter 4</html-el:option>
                        <html-el:option value="sa">Semi Annually</html-el:option>
                        <html-el:option value="an">Annually</html-el:option>
                        <html-el:option value="f">Final</html-el:option>
                    </html-el:select>
                </td>
            </tr>
            <tr align="center">
                <td height="30" colspan="4" class="infoline">
                    <c:set var="extraButtons" value="${KualiForm.extraButtons}"/>
                    <c:if test="${!empty extraButtons}">
                        <c:forEach items="${extraButtons}" var="extraButton">
                            <html:submit styleClass="tinybutton btn btn-default" property="${extraButton.extraButtonProperty}" title="${extraButton.extraButtonAltText}" alt="${extraButton.extraButtonAltText}" value="${extraButton.extraButtonAltText}"/>
                        </c:forEach>
                    </c:if>
                </td>
            </tr>
            </table>
        </div>
    </div>

    <div>
        <c:if test="${!empty KualiForm.error }">
            ${KualiForm.error}
        </c:if>
    </div>
</kul:page>
