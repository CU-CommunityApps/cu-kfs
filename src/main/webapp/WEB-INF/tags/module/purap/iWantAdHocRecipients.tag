<%--
 Copyright 2005-2007 The Kuali Foundation

 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.opensource.org/licenses/ecl2.php

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp"%>


    <c:if test="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_ADD_ADHOC_REQUESTS] and not KualiForm.suppressAllButtons}">


        <div class="tab-container" align=center>

            <table cellpadding="0" cellspacing="0" class="datatable" summary="view/edit ad hoc recipients">
        <%-- first do the persons --%>
              <kul:displayIfErrors keyMatch="${Constants.AD_HOC_ROUTE_PERSON_ERRORS}">
          <tr>
              <th colspan=4>
                <kul:errors keyMatch="${Constants.AD_HOC_ROUTE_PERSON_ERRORS}" />
              </th>
            </tr>
        </kul:displayIfErrors>
         <tr>
			<td colspan="4" height=30  class="neutral" style="color: blue; font-family:Verdana, Verdana, serif; font-size: 12px;font-style: italic">
			<b>NOTE:</b> If someone needs to complete this form or approvals are required prior to submitting to your Service Center for processing, please list the individual's net id below and click the save button. If no additional approvals are needed please click the submit button to route to your service center.</td>
		</tr>

                <tr>
                    <th align=right valign=middle class="neutral" colspan="2"><div align="right">
                       <kul:htmlAttributeLabel attributeEntry="${DataDictionary.IWantDocument.attributes.currentRouteToNetId}" />
                     </div>
                     </th>
					
					<kul:checkErrors keyMatch="newAdHocRoutePerson.id" />
				
                    <td valign=middle class="neutral" colspan="2" ><div align=left>
                        <kul:user userIdFieldName="newAdHocRoutePerson.id"
                              userId="${KualiForm.newAdHocRoutePerson.id}"
                              universalIdFieldName=""
                              universalId=""
                              userNameFieldName="newAdHocRoutePerson.name"
                              userName="${KualiForm.newAdHocRoutePerson.name}"
                              readOnly="${displayReadOnly}"
                              renderOtherFields="true"
                              fieldConversions="principalName:newAdHocRoutePerson.id,name:newAdHocRoutePerson.name"
                              lookupParameters="newAdHocRoutePerson.id:principalName"
                              hasErrors="${hasErrors}" />
                    </td>
                </tr>
          </table>
          </div>
     
    </c:if>
