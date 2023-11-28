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
<%@ page import="org.kuali.kfs.coa.service.AccountService" %>
<%@ page import="org.kuali.kfs.sys.context.SpringContext" %>

<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>
<kul:documentPage showDocumentInfo="true" htmlFormAction="camsAssetTransfer" documentTypeName="AssetTransferDocument"
                  renderMultipart="true" showTabButtons="true">
    <script type='text/javascript' src="dwr/interface/AccountService.js"></script>
    <script language="JavaScript" type="text/javascript" src="scripts/module/cams/assetTransfer.js"></script>
    <c:set var="assetTransferAttributes" value="${DataDictionary.AssetTransferDocument.attributes}"/>
    <c:set var="assetAttributes" value="${DataDictionary.Asset.attributes}"/>
    <c:set var="assetOrgAttributes" value="${DataDictionary.AssetOrganization.attributes}"/>
    <c:set var="organizationAttributes" value="${DataDictionary.Organization.attributes}"/>
    <c:set var="readOnly" value="${!KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}"/>
    <c:set var="accountsCanCrossCharts" value="<%=SpringContext.getBean(AccountService.class).accountsCanCrossCharts()%>"/>
    <c:set var="readOnlyChartCode" value="${readOnly or !accountsCanCrossCharts}"/>

    <sys:documentOverview editingMode="${KualiForm.editingMode}"
                          viewYearEndAccountPeriod="${!empty KualiForm.documentActions[KFSConstants.YEAR_END_ACCOUNTING_PERIOD_VIEW_DOCUMENT_ACTION]}"/>
    <cams:viewAssetDetails defaultTabHide="false"/>

    <kul:tab tabTitle="Asset Transfer Information" defaultOpen="true"
             tabErrorKey="document.organizationOwnerAccountNumber,document.organizationOwnerChartOfAccountsCode,document.transferOfFundsFinancialDocumentNumber">
        <div class="tab-container">
            <table class="standard old-new">
                <tr>
                    <th></th>
                    <th class="old" width="25%">Old</th>
                    <th></th>
                    <th class="new" width="25%">New</th>
                </tr>
                <tr>
                    <td></td>
                    <td class="tab-subhead heavy" width="25%">Transfer from Originating Organization</td>
                    <td></td>
                    <td class="tab-subhead heavy" width="25%">Transfer to Receiving Organization</td>
                </tr>
                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel
                                attributeEntry="${assetTransferAttributes.oldOrganizationOwnerChartOfAccountsCode}"
                                readOnly="true"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute
                                property="document.oldOrganizationOwnerChartOfAccountsCode"
                                attributeEntry="${assetTransferAttributes.organizationOwnerChartOfAccountsCode}"
                                readOnly="true"/>
                    </td>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel
                                attributeEntry="${assetTransferAttributes.organizationOwnerChartOfAccountsCode}"/>
                    </th>
                    <td class="grid" width="25%">
                        <c:if test="${readOnlyChartCode}">
                            <span id="document.organizationOwnerChartOfAccountsCode">
                                <bean:write name="KualiForm" property="document.organizationOwnerChartOfAccountsCode"/>
                            </span>
                        </c:if>
                        <c:if test="${not readOnlyChartCode}">
                            <kul:htmlControlAttribute property="document.organizationOwnerChartOfAccountsCode"
                                                      attributeEntry="${assetTransferAttributes.organizationOwnerChartOfAccountsCode}"
                                                      readOnly="false"/>
                            &nbsp;
                            <kul:lookup boClassName="org.kuali.kfs.coa.businessobject.Chart"
                                        fieldConversions="chartOfAccountsCode:document.organizationOwnerChartOfAccountsCode"
                                        lookupParameters="document.organizationOwnerChartOfAccountsCode:chartOfAccountsCode"/>
                        </c:if>
                    </td>
                </tr>
                <tr>
                    <th class="grid right" width="25%"><kul:htmlAttributeLabel
                            attributeEntry="${assetTransferAttributes.oldOrganizationOwnerAccountNumber}"
                            readOnly="true"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute
                                property="document.oldOrganizationOwnerAccountNumber"
                                attributeEntry="${assetTransferAttributes.organizationOwnerAccountNumber}"
                                readOnly="true"/>
                    </td>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel
                                attributeEntry="${assetTransferAttributes.organizationOwnerAccountNumber}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute
                                property="document.organizationOwnerAccountNumber"
                                attributeEntry="${assetTransferAttributes.organizationOwnerAccountNumber}"
                                onblur="onblur_accountNumber(this, 'organizationOwnerChartOfAccountsCode');"
                                readOnly="${readOnly}"/>
                        <c:if test="${not readOnly}">
                            &nbsp;
                            <kul:lookup boClassName="org.kuali.kfs.coa.businessobject.Account"
                                        fieldConversions="accountNumber:document.organizationOwnerAccountNumber,chartOfAccountsCode:document.organizationOwnerChartOfAccountsCode"
                                        lookupParameters="document.organizationOwnerAccountNumber:accountNumber,document.organizationOwnerChartOfAccountsCode:chartOfAccountsCode"/>
                        </c:if>
                    </td>
                </tr>

                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel
                                attributeEntry="${organizationAttributes.organizationCode}" readOnly="true"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute
                                property="document.oldOrganizationOwnerAccount.organizationCode"
                                attributeEntry="${organizationAttributes.organizationCode}" readOnly="true"/>
                    </td>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel
                                attributeEntry="${organizationAttributes.organizationCode}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute
                                property="document.organizationOwnerAccount.organizationCode"
                                attributeEntry="${organizationAttributes.organizationCode}" readOnly="true"/>
                    </td>
                </tr>
            </table>

            <table class="standard">
                <tr>
                    <td class="tab-subhead" colspan="4" width="100%"><h3>Interdepartmental Sale</h3></td>
                </tr>
                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel
                                attributeEntry="${assetTransferAttributes.interdepartmentalSalesIndicator}"/>
                    </th>
                    <td class="grid" colspan="3">
                        <kul:htmlControlAttribute
                                property="document.interdepartmentalSalesIndicator"
                                attributeEntry="${assetTransferAttributes.interdepartmentalSalesIndicator}"
                                readOnly="${readOnly}"/>
                    </td>
                </tr>
                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel
                                attributeEntry="${assetTransferAttributes.transferOfFundsFinancialDocumentNumber}"/>
                    </th>
                    <td class="grid" colspan="3">
                        <kul:htmlControlAttribute
                                property="document.transferOfFundsFinancialDocumentNumber"
                                attributeEntry="${assetTransferAttributes.transferOfFundsFinancialDocumentNumber}"
                                readOnly="${readOnly}"/>
                        <c:if test="${not readOnly}">
                            &nbsp;
                            <input type="hidden" name="docType" value="TF"/>
                            <kul:lookup boClassName="org.kuali.kfs.kew.impl.document.search.DocumentSearchCriteriaBo"
                                        fieldConversions="documentId:document.transferOfFundsFinancialDocumentNumber"
                                        lookupParameters="docType:documentTypeName,document.transferOfFundsFinancialDocumentNumber:documentId"
                                        baseLookupUrl="${ConfigProperties.application.url}/DocumentSearch.do"/>
                        </c:if>
                    </td>
                </tr>
            </table>
        </div>
    </kul:tab>

    <kul:tab tabTitle="Asset Location" defaultOpen="true"
             tabErrorKey="document.locationTabKey,document.campusCode,document.buildingCode,document.buildingRoomNumber,document.offCampus*,assetLocationErrorSection">
        <div class="tab-container">
            <table class="standard side-margins">
                <tr>
                    <td></td>
                    <td class="tab-subhead heavy" width="25%">On Campus</td>
                    <td></td>
                    <td class="tab-subhead heavy" width="25%">Off Campus</td>
                </tr>
                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel
                                attributeEntry="${assetTransferAttributes.campusCode}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute property="document.campusCode"
                                                  attributeEntry="${assetTransferAttributes.campusCode}"
                                                  readOnly="${readOnly}"/>
                        <c:if test="${not readOnly}">
                            &nbsp;
                            <kul:lookup boClassName="org.kuali.kfs.sys.businessobject.Campus"
                                        fieldConversions="code:document.campusCode"
                                        lookupParameters="document.campusCode:code"/>
                        </c:if>
                    </td>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel
                                attributeEntry="${assetTransferAttributes.offCampusName}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute property="document.offCampusName"
                                                  attributeEntry="${assetTransferAttributes.offCampusName}"
                                                  readOnly="${readOnly}"/>
                    </td>
                </tr>
                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel
                                attributeEntry="${assetTransferAttributes.buildingCode}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute property="document.buildingCode"
                                                  attributeEntry="${assetTransferAttributes.buildingCode}"
                                                  readOnly="${readOnly}"/>
                        <c:if test="${not readOnly}">
                            &nbsp;
                            <kul:lookup boClassName="org.kuali.kfs.sys.businessobject.Building"
                                        fieldConversions="buildingCode:document.buildingCode,campusCode:document.campusCode"
                                        lookupParameters="document.buildingCode:buildingCode,document.campusCode:campusCode"/>
                        </c:if>
                    </td>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.offCampusAddress}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute property="document.offCampusAddress"
                                                  attributeEntry="${assetTransferAttributes.offCampusAddress}"
                                                  readOnly="${readOnly}"/>
                    </td>
                </tr>
                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.buildingRoomNumber}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute property="document.buildingRoomNumber"
                                                  attributeEntry="${assetTransferAttributes.buildingRoomNumber}"
                                                  readOnly="${readOnly}"/>
                        <c:if test="${not readOnly}">
                            &nbsp;
                            <kul:lookup boClassName="org.kuali.kfs.sys.businessobject.Room"
                                        fieldConversions="buildingRoomNumber:document.buildingRoomNumber,buildingCode:document.buildingCode,campusCode:document.campusCode"
                                        lookupParameters="document.buildingRoomNumber:buildingRoomNumber,document.buildingCode:buildingCode,document.campusCode:campusCode"/>
                        </c:if>
                    </td>
                    <th class="grid right" width="25%" ]>
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.offCampusCityName}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute property="document.offCampusCityName"
                                                  attributeEntry="${assetTransferAttributes.offCampusCityName}"
                                                  readOnly="${readOnly}"/>
                    </td>
                </tr>
                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.buildingSubRoomNumber}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute property="document.buildingSubRoomNumber"
                                                  attributeEntry="${assetTransferAttributes.buildingSubRoomNumber}"
                                                  readOnly="${readOnly}"/>
                    </td>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.offCampusStateCode}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute property="document.offCampusStateCode"
                                                  attributeEntry="${assetTransferAttributes.offCampusStateCode}"
                                                  readOnly="${readOnly}"/>
                        <c:if test="${not readOnly}">
                            &nbsp;
                            <kul:lookup boClassName="org.kuali.kfs.sys.businessobject.State"
                                        fieldConversions="code:document.offCampusStateCode"
                                        lookupParameters="document.offCampusCountryCode:postalCountryCode,document.offCampusStateCode:code,document.offCampusZipCode:postalCode"/>
                        </c:if>
                    </td>
                </tr>
                <tr>
                    <th class="grid right" width="25%" colspan="2"></th>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.offCampusZipCode}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute property="document.offCampusZipCode"
                                                  attributeEntry="${assetTransferAttributes.offCampusZipCode}"
                                                  readOnly="${readOnly}"/>
                        <c:if test="${not readOnly}">
                            &nbsp;
                            <kul:lookup boClassName="org.kuali.kfs.sys.businessobject.PostalCode"
                                        fieldConversions="code:document.offCampusZipCode"
                                        lookupParameters="document.offCampusCountryCode:countryCode,document.offCampusZipCode:code,document.offCampusStateCode:stateCode"/>
                        </c:if>
                    </td>
                </tr>
                <tr>
                    <th class="grid right" width="25%" colspan="2"></th>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.offCampusCountryCode}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute property="document.offCampusCountryCode"
                                                  attributeEntry="${assetTransferAttributes.offCampusCountryCode}"
                                                  readOnly="${readOnly}"/>
                        <c:if test="${not readOnly}">
                            &nbsp;
                            <kul:lookup boClassName="org.kuali.kfs.sys.businessobject.Country"
                                        fieldConversions="code:document.offCampusCountryCode"
                                        lookupParameters="document.offCampusCountryCode:code"/>
                        </c:if>
                    </td>
                </tr>
            </table>
        </div>
    </kul:tab>

    <kul:tab tabTitle="Organization Information" defaultOpen="true" tabErrorKey="document.assetRepresentative.principalName">
        <div class="tab-container">
            <table class="standard old-new side-margins">
                <tr>
                    <th></th>
                    <th class="old" width="25%">Current</th>
                    <th></th>
                    <th class="new" width="25%">New</th>
                </tr>
                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.organizationInventoryName}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute
                                property="document.asset.organizationInventoryName"
                                attributeEntry="${assetTransferAttributes.organizationInventoryName}" readOnly="true"/>
                    </td>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.organizationInventoryName}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute
                                property="document.organizationInventoryName"
                                attributeEntry="${assetTransferAttributes.organizationInventoryName}"
                                readOnly="${readOnly}"/>
                    </td>
                </tr>
                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.representativeUniversalIdentifier}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute
                                property="document.asset.assetRepresentative.name"
                                attributeEntry="${assetTransferAttributes.representativeUniversalIdentifier}"
                                readOnly="true"/>
                    </td>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.representativeUniversalIdentifier}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:checkErrors keyMatch="document.assetRepresentative.principalName"/>
                        <kul:user userIdFieldName="document.assetRepresentative.principalName"
                                  universalIdFieldName="document.representativeUniversalIdentifier"
                                  userNameFieldName="document.assetRepresentative.name" label="User"
                                  lookupParameters="document.assetRepresentative.principalName:principalName,document.representativeUniversalIdentifier:principalId,document.assetRepresentative.name:name"
                                  fieldConversions="principalName:document.assetRepresentative.principalName,principalId:document.representativeUniversalIdentifier,name:document.assetRepresentative.name"
                                  userId="${KualiForm.document.assetRepresentative.principalName}"
                                  universalId="${KualiForm.document.representativeUniversalIdentifier}"
                                  userName="${KualiForm.document.assetRepresentative.name}"
                                  hasErrors="${hasErrors}" readOnly="${readOnly}"/>
                    </td>
                </tr>
                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.organizationText}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute
                                property="document.asset.assetOrganization.organizationText"
                                attributeEntry="${assetTransferAttributes.organizationText}" readOnly="true"/>
                    </td>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.organizationText}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute
                                property="document.organizationText"
                                attributeEntry="${assetTransferAttributes.organizationText}"
                                readOnly="${readOnly}"/>
                    </td>
                </tr>
                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.organizationTagNumber}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute
                                property="document.asset.assetOrganization.organizationTagNumber"
                                attributeEntry="${assetTransferAttributes.organizationTagNumber}" readOnly="true"/>
                    </td>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetTransferAttributes.organizationTagNumber}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute
                                property="document.organizationTagNumber"
                                attributeEntry="${assetTransferAttributes.organizationTagNumber}"
                                readOnly="${readOnly}"/>
                    </td>
                </tr>
                <tr>
                    <th class="grid right" width="25%">
                        <kul:htmlAttributeLabel attributeEntry="${assetOrgAttributes.organizationAssetTypeIdentifier}"/>
                    </th>
                    <td class="grid" width="25%">
                        <kul:htmlControlAttribute
                                property="document.asset.assetOrganization.organizationAssetTypeIdentifier"
                                attributeEntry="${assetOrgAttributes.organizationAssetTypeIdentifier}"
                                readOnly="true"/>
                    </td>
                    <th class="grid right" width="25%">&nbsp;</th>
                    <td class="grid" width="25%">&nbsp;</td>
                </tr>
            </table>
        </div>
    </kul:tab>
    <cams:viewPayments defaultTabHide="true" assetPayments="${KualiForm.document.asset.assetPayments}"/>
    <cams:viewDepreciationInfo defaultTabHide="true"/>
    <gl:generalLedgerPendingEntries/>
    <kul:notes/>
    <kul:adHocRecipients/>
    <kul:routeLog/>
    <kul:superUserActions/>
    <sys:documentControls transactionalDocument="${documentEntry.transactionalDocument}"/>
</kul:documentPage>
