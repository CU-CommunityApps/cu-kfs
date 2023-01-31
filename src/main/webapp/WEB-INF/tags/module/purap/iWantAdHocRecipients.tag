<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<c:if test="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_ADD_ADHOC_REQUESTS] and not KualiForm.suppressAllButtons}">
    <div class="tab-container">
        <table class="standard side-margins">
            <kul:displayIfErrors keyMatch="${KRADConstants.AD_HOC_ROUTE_PERSON_ERRORS}">
                <tr>
                    <th colspan="4">
                        <kul:errors keyMatch="${KRADConstants.AD_HOC_ROUTE_PERSON_ERRORS}"/>
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
                        <li>Do <strong>not</strong> enter your Net ID in the Approver's box below.</li>
                        <li>If you did not enter an <strong>account number</strong> in Step 2/Account Information, enter the Net ID of the person in your department who can provide that information. </li>
                        <li>If additional approvals are required, enter the Net ID of the person in your department who can authorize the expenditure <strong>or</strong> attach the approval in Step 3/Notes and Attachments.</li>
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