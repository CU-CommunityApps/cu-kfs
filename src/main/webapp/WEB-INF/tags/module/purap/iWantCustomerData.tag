<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>

<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}"/>
<c:set var="tabindexOverrideBase" value="30"/>

<h3>Organization Information</h3>
<table class="datatable" summary="Customer Data">
    <tr>
        <td colspan="4" class="neutral">
            <table class="standard side-margins">
                <tr>
                    <td height="30" colspan="4" class="neutral"
                        style="color: blue; font-family:Verdana, Verdana, serif; font-size: 12px;font-style: italic">
                        <b>NOTE:</b> Please select the college and department for which you are entering this request.
                        <ul>
                            <li>Preset values are your saved default college and department.</li>
                            <li>Press "Reset Initiator Defaults" if you wish to have those defaults reset to your current Person data values.</li>
                            <li>Check "Set as Default" if you want your defaults to be set to the values presently displayed when you Submit.</li>
                        </ul>
                    </td>
                </tr>
                <tr>
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.collegeLevelOrganization}"/>
                    </th>
                    <td align="left" valign="middle" width="20%" class="neutral">
                        <kul:htmlControlAttribute
                                attributeEntry="${documentAttributes.collegeLevelOrganization}"
                                property="document.collegeLevelOrganization"
                                readOnly="${not fullEntryMode}"
                                tabindexOverride="${tabindexOverrideBase + 0}"
                                onchange="loadDepartments(this.form)"/>
                        <html:hidden property="previousSelectedOrg"/>
                    </td>
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.departmentLevelOrganization}"/>
                    </th>
                    <td align="left" valign="middle" width="20%" class="neutral">
                        <html:select property="document.departmentLevelOrganization" disabled="${not fullEntryMode}">
                            <html:optionsCollection property="deptOrgKeyLabels" label="value" value="key"/>
                        </html:select>
                    </td>
                </tr>
                <tr>
                    <td align="right" valign="middle" width="10%" class="neutral">
                        &nbsp;
                    </td>
                </tr>
                <tr>
                    <td align="left" valign="middle" class="neutral">
                        &nbsp;
                    </td>
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.useCollegeAndDepartmentAsDefault}"/>
                    </th>
                    <td align="left" valign="middle" class="neutral">
                        <kul:htmlControlAttribute
                                attributeEntry="${documentAttributes.useCollegeAndDepartmentAsDefault}"
                                property="document.useCollegeAndDepartmentAsDefault"
                                readOnly="${not fullEntryMode}"
                                tabindexOverride="${tabindexOverrideBase + 0}"/>
                    </td>
                    <td>
                        <html:submit property="methodToCall.resetInitiatorDefaultCollegeAndDepartment"
                                title="Reset Initator College-Department"
                                alt="Reset Initiator College-Department"
                                styleClass="btn btn-default"
                                disabled="${not fullEntryMode}"
                                value="Reset Initiator Defaults"/>
                    </td>
                    <th align="right" valign="middle" width="10%" class="neutral">
                        &nbsp;
                    </th>
                    <td align="left" valign="middle" width="10%" class="neutral">
                        &nbsp;
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <h3 style="margin: 20px 0px 15px 20px;">Requestor</h3>
        </td>
        <td colspan="2">
            <h3 style="margin: 20px 0px 15px 20px;">Deliver To</h3>
        </td>
    </tr>
    <tr>
        <th align="right" valign="middle" width="25%" class="neutral">
            &nbsp;
        </th>
        <td align="left" valign="middle" width="25%" class="neutral">
            &nbsp;
        </td>
        <th class="right">
            <kul:htmlAttributeLabel attributeEntry="${documentAttributes.sameAsInitiator}"/>
        </th>
        <td align="left" valign="middle" width="25%" class="neutral">
            <table cellpadding="0" cellspacing="0" border="0">
                <tr>
                    <td align="left" valign="middle" width="25%" class="neutral">
                        <kul:htmlControlAttribute
                                attributeEntry="${documentAttributes.sameAsInitiator}"
                                property="document.sameAsInitiator"
                                readOnly="${not fullEntryMode}"
                                tabindexOverride="${tabindexOverrideBase + 0}"
                                onclick="loadDeliverToInfoSameAsInitiator('document.sameAsInitiator','document.initiatorNetID', 'document.deliverToNetID', 'document.initiatorName', 'document.deliverToName', 'document.initiatorPhoneNumber', 'document.deliverToPhoneNumber', 'document.initiatorEmailAddress', 'document.deliverToEmailAddress', 'document.initiatorAddress', 'document.deliverToAddress')"/>&nbsp;
                    </td>
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.setDeliverToInfoAsDefault}"/>
                    </th>
                    <td align="left" valign="middle" width="25%" class="neutral">
                        <kul:htmlControlAttribute
                                attributeEntry="${documentAttributes.setDeliverToInfoAsDefault}"
                                property="document.setDeliverToInfoAsDefault"
                                readOnly="${not fullEntryMode}"
                                tabindexOverride="${tabindexOverrideBase + 0}" />
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <th class="right">
            <kul:htmlAttributeLabel attributeEntry="${documentAttributes.initiatorNetID}"/>
        </th>
        <td align="left" valign="middle" width="25%" class="neutral">
            <kul:user userIdFieldName="document.initiatorNetID"
                      userId="${KualiForm.document.initiatorNetID}"
                      universalIdFieldName=""
                      universalId=""
                      userNameFieldName="document.initiatorName"
                      userName="${KualiForm.document.initiatorName}"
                      readOnly="${not fullEntryMode}"
                      fieldConversions="principalName:document.initiatorNetID,name:document.initiatorName,phoneNumber:document.initiatorPhoneNumber,emailAddress:document.initiatorEmailAddress"
                      lookupParameters="document.initiatorNetIDForLookup:principalName"
                      hasErrors="${hasErrors}"
                      onblur="loadRequestorInfo('document.sameAsInitiator', 'document.initiatorNetID', 'document.initiatorName', 'document.initiatorPhoneNumber', 'document.initiatorEmailAddress', 'document.initiatorAddress')"/>
        </td>
        <th class="right">
            <kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliverToNetID}"/>
        </th>
        <td align="left" valign="middle" width="25%" class="neutral">
            <kul:user userIdFieldName="document.deliverToNetID"
                      userId="${KualiForm.document.deliverToNetID}"
                      universalIdFieldName=""
                      universalId=""
                      userNameFieldName="document.deliverToName"
                      userName="${KualiForm.document.deliverToName}"
                      readOnly="${not fullEntryMode}"
                      fieldConversions="principalName:document.deliverToNetID,name:document.deliverToName,phoneNumber:document.deliverToPhoneNumber,emailAddress:document.deliverToEmailAddress"
                      lookupParameters="document.deliverToNetIDForLookup:principalName"
                      hasErrors="${hasErrors}"
                      onblur="loadDeliverToInfo('document.sameAsInitiator', 'document.deliverToNetID', 'document.deliverToName', 'document.deliverToPhoneNumber', 'document.deliverToEmailAddress', 'document.deliverToAddress')"/>
        </td>
    </tr>
    <tr>
        <th class="right">
            <kul:htmlAttributeLabel attributeEntry="${documentAttributes.initiatorPhoneNumber}"/>
        </th>
        <td align="left" valign="middle" width="25%" class="neutral">
            <kul:htmlControlAttribute
                    attributeEntry="${documentAttributes.initiatorPhoneNumber}"
                    property="document.initiatorPhoneNumber"
                    readOnly="${not fullEntryMode}"
                    tabindexOverride="${tabindexOverrideBase + 5}"/>
        </td>
        <th class="right">
            <kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliverToPhoneNumber}"/>
        </th>
        <td align="left" valign="middle" width="25%" class="neutral">
            <kul:htmlControlAttribute
                    attributeEntry="${documentAttributes.deliverToPhoneNumber}"
                    property="document.deliverToPhoneNumber"
                    readOnly="${not fullEntryMode}"
                    tabindexOverride="${tabindexOverrideBase + 5}"/>
        </td>
    </tr>
    <tr>
        <th class="right">
            <kul:htmlAttributeLabel attributeEntry="${documentAttributes.initiatorEmailAddress}"/>
        </th>
        <td align="left" valign="middle" width="25%" class="neutral">
            <kul:htmlControlAttribute
                    attributeEntry="${documentAttributes.initiatorEmailAddress}"
                    property="document.initiatorEmailAddress"
                    readOnly="${not fullEntryMode}"
                    tabindexOverride="${tabindexOverrideBase + 5}"/>
        </td>
        <th class="right">
            <kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliverToEmailAddress}"/>
        </th>
        <td align="left" valign="middle" width="25%" class="neutral">
            <kul:htmlControlAttribute
                    attributeEntry="${documentAttributes.deliverToEmailAddress}"
                    property="document.deliverToEmailAddress"
                    readOnly="${not fullEntryMode}"
                    tabindexOverride="${tabindexOverrideBase + 5}"/>
        </td>
    </tr>
    <tr>
        <th class="right">
            <kul:htmlAttributeLabel attributeEntry="${documentAttributes.initiatorAddress}"/>
        </th>
        <td align="left" valign="middle" width="25%" class="neutral">
            <kul:htmlControlAttribute
                    attributeEntry="${documentAttributes.initiatorAddress}"
                    property="document.initiatorAddress"
                    readOnly="${not fullEntryMode}"
                    tabindexOverride="${tabindexOverrideBase + 5}"/>
        </td>
        <th class="right">
            <kul:htmlAttributeLabel attributeEntry="${documentAttributes.deliverToAddress}"/>
        </th>
        <td align="left" valign="middle" width="25%" class="neutral">
            <kul:htmlControlAttribute
                    attributeEntry="${documentAttributes.deliverToAddress}"
                    property="document.deliverToAddress"
                    readOnly="${not fullEntryMode}"
                    tabindexOverride="${tabindexOverrideBase + 5}"/>
        </td>
    </tr>
</table>