<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp" %>

<c:if test="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_ADD_ADHOC_REQUESTS] and not KualiForm.suppressAllButtons}">
    <div class="tab-container">
        <table class="standard side-margins">
            <kul:displayIfErrors keyMatch="${Constants.AD_HOC_ROUTE_PERSON_ERRORS}">
                <tr>
                    <th colspan="4">
                        <kul:errors keyMatch="${Constants.AD_HOC_ROUTE_PERSON_ERRORS}"/>
                    </th>
                </tr>
            </kul:displayIfErrors>
            <tr>
                <td class="neutral" colspan="4" height="30" 
                    style="color: blue;font-family: Verdana , Verdana , serif;font-size: 12.0px;font-style: italic;">
                    <p>
                        <strong>NOTE:</strong>
                    </p>
                    <ul>
                        <li>Do not enter your Net ID in the box below.</li>
                        <li>If additional approvals are required prior to submitting to your Service Center for processing, please list the individual's Net ID below.</li>
                        <li>Please click the <strong>Submit</strong> button to route to the named approver or your Service Center.</li>
                    </ul>
                </td>
            </tr>
        </table>
        <table cellpadding="0" cellspacing="0" class="datatable" summary="view/edit ad hoc recipients">
            <tr>
                <th class="right" width="50%">
                    <kul:htmlAttributeLabel attributeEntry="${DataDictionary.IWantDocument.attributes.currentRouteToNetId}"/>
                </th>
                <kul:checkErrors keyMatch="newAdHocRoutePerson.id"/>
                <td valign="middle" class="left" width="50%">
                    <kul:user userIdFieldName="newAdHocRoutePerson.id"
                              userId="${KualiForm.newAdHocRoutePerson.id}"
                              universalIdFieldName=""
                              universalId=""
                              userNameFieldName="newAdHocRoutePerson.name"
                              userName="${KualiForm.newAdHocRoutePerson.name}"
                              readOnly="${displayReadOnly}"
                              fieldConversions="principalName:newAdHocRoutePerson.id,name:newAdHocRoutePerson.name"
                              lookupParameters="newAdHocRoutePersonIdForLookup:principalName"
                              hasErrors="${hasErrors}"/>
                </td>
            </tr>
        </table>
    </div>
</c:if>