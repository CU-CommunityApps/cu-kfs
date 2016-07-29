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
                <td colspan="4" height="30" class="neutral"
                    style="color: blue; font-family:Verdana, Verdana, serif; font-size: 12px;font-style: italic">
                    <b>NOTE:</b> If someone needs to complete this form or approvals are required prior to submitting to
                    your Service Center for processing, please list the individual's net id below and click the save
                    button. If no additional approvals are needed please click the submit button to route to your
                    service center.
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