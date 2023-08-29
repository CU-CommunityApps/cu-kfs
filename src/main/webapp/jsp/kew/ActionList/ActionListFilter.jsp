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
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<style type="text/css">
  .custom-filter th {
      padding-left: 24px;
      font-weight: 400;
      width: 20%;
  }
  .custom-filter td.subhead {
      font-size: 1.5rem;
      padding: 24px 0 16px 0;
  }
  .custom-filter td:not(:first-child) {
      padding: 8px 0;
  }
  .filter-input {
      width: 300px;
  }
</style>

<c:set var="KualiForm" value="${ActionListFilterForm}" scope="request"/>
<kul:page
    headerTitle="Action List Filter"
    lookup="false"
    headerMenuBar=""
    transactionalDocument="false"
    showDocumentInfo="false"
    htmlFormAction="ActionListFilter"
    docTitle="Action List Filter"
>

<html-el:hidden property="lookupableImplServiceName" />
<html-el:hidden property="lookupType" />
<html-el:hidden property="docTypeFullName" />
    <div id="workarea">
        <div class="tab-container" align="center">
            <table class="datatable-80 custom-filter" style="align:center" cellspacing="0" align="center">
                <tr>
                    <td class="subhead" colspan="2"><bean-el:message key="actionList.ActionListFilter.filter.label.parametersTitle"/></td>
                </tr>
                <c:if test="${! empty delegators}">
                <tr>
                    <th><div><span class="thnormal"><bean-el:message key="actionList.ActionListFilter.filter.label.secondaryDelegatorId"/></span></div></th>
                    <td class="datacell filter-input">
                        <html-el:select property="filter.delegatorId" onchange="if(document.forms[0]['filter.primaryDelegateId']){document.forms[0]['filter.primaryDelegateId'].value='${KRADConstants.PRIMARY_DELEGATION_DEFAULT}';}">
                            <html-el:option value="${KRADConstants.DELEGATION_DEFAULT}"><c:out value="${KRADConstants.DELEGATION_DEFAULT}" /></html-el:option>
                            <html-el:option value="${KRADConstants.ALL_CODE}"><c:out value="${KRADConstants.ALL_CODE}" /></html-el:option>
                            <c:forEach var="delegator" items="${delegators}">
                                <html-el:option value="${delegator.recipientId}"><c:out value="${delegator.displayName}" /></html-el:option>
                            </c:forEach>
                        </html-el:select>
                    </td>
                </tr>
                </c:if>
                <c:if test="${! empty primaryDelegates}">
                <tr>
                    <th><div><span class="thnormal"><bean-el:message key="actionList.ActionListFilter.filter.label.primaryDelegateId"/></span></div></th>
                    <td class="datacell filter-input">
                        <html-el:select property="filter.primaryDelegateId" onchange="if(document.forms[0]['filter.delegatorId']){document.forms[0]['filter.delegatorId'].value='${KRADConstants.DELEGATION_DEFAULT}';}">
                            <html-el:option value="${KRADConstants.PRIMARY_DELEGATION_DEFAULT}"><c:out value="${KRADConstants.PRIMARY_DELEGATION_DEFAULT}" /></html-el:option>
                            <html-el:option value="${KRADConstants.ALL_CODE}"><c:out value="${KRADConstants.ALL_CODE}" /></html-el:option>
                            <c:forEach var="delegatee" items="${primaryDelegates}">
                                <html-el:option value="${delegatee.recipientId}"><c:out value="${delegatee.displayName}" /></html-el:option>
                            </c:forEach>
                        </html-el:select>
                    </td>
                </tr>
                </c:if>
                <tr>
                    <th><span class="thnormal"><bean-el:message key="actionList.ActionListFilter.filter.label.documentTitle"/></span></th>
                    <td class="datacell filter-input">
                        <html-el:text property="filter.documentTitle"/>
                    </td>
                    <td>
                        <bean-el:message key="actionList.ActionListFilter.filter.label.exclude"/>
                        <html-el:checkbox property="filter.excludeDocumentTitle"/>
                    </td>
                </tr>
                <tr>
                    <th><span class="thnormal"><bean-el:message key="actionList.ActionListFilter.filter.label.documentRouteStatus"/></span></th>
                    <td class="datacell filter-input">
                        <html-el:select property="filter.docRouteStatus">
                            <html-el:option value="${KewApiConstants.ALL_CODE}">
                              <c:out value="${KewApiConstants.ALL_CODE}" />
                            </html-el:option>
                            <html-el:option value="${KewApiConstants.ROUTE_HEADER_DISAPPROVED_CD}">
                              <c:out value="${KewApiConstants.ROUTE_HEADER_DISAPPROVED_LABEL}" />
                            </html-el:option>
                            <html-el:option value="${KewApiConstants.ROUTE_HEADER_ENROUTE_CD}">
                              <c:out value="${KewApiConstants.ROUTE_HEADER_ENROUTE_LABEL}" />
                            </html-el:option>
                            <html-el:option value="${KewApiConstants.ROUTE_HEADER_EXCEPTION_CD}">
                              <c:out value="${KewApiConstants.ROUTE_HEADER_EXCEPTION_LABEL}" />
                            </html-el:option>
                            <html-el:option value="${KewApiConstants.ROUTE_HEADER_PROCESSED_CD}">
                              <c:out value="${KewApiConstants.ROUTE_HEADER_PROCESSED_LABEL}" />
                            </html-el:option>
                            <html-el:option value="${KewApiConstants.ROUTE_HEADER_SAVED_CD}">
                              <c:out value="${KewApiConstants.ROUTE_HEADER_SAVED_LABEL}" />
                            </html-el:option>
                        </html-el:select>
                    </td>
                    <td>
                        <bean-el:message key="actionList.ActionListFilter.filter.label.exclude"/>
                        <html-el:checkbox property="filter.excludeRouteStatus"/>
                    </td>
                </tr>
                <tr>
                    <th><span class="thnormal"><bean-el:message key="actionList.ActionListFilter.filter.label.actionRequested"/></span></th>
                    <td class="datacell filter-input">
                        <html-el:select property="filter.actionRequestCd">
                            <html-el:option value="${KewApiConstants.ALL_CODE}"><c:out value="${KewApiConstants.ALL_CODE}" /></html-el:option>
                            <html-el:option value="${KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ}"><c:out value="${KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ_LABEL}" /></html-el:option>
                            <html-el:option value="${KewApiConstants.ACTION_REQUEST_APPROVE_REQ}"><c:out value="${KewApiConstants.ACTION_REQUEST_APPROVE_REQ_LABEL}" /></html-el:option>
                            <html-el:option value="${KewApiConstants.ACTION_REQUEST_COMPLETE_REQ}"><c:out value="${KewApiConstants.ACTION_REQUEST_COMPLETE_REQ_LABEL}" /></html-el:option>
                            <html-el:option value="${KewApiConstants.ACTION_REQUEST_FYI_REQ}"><c:out value="${KewApiConstants.ACTION_REQUEST_FYI_REQ_LABEL}" /></html-el:option>
                        </html-el:select>
                    </td>
                    <td>
                        <bean-el:message key="actionList.ActionListFilter.filter.label.exclude"/>
                        <html-el:checkbox property="filter.excludeActionRequestCd"/>
                    </td>
                </tr>
                <tr>
                    <th><span class="thnormal"><bean-el:message key="actionList.ActionListFilter.filter.label.actionRequestGroup"/></span></th>
                    <td class="datacell filter-input">
                        <html-el:select name="ActionListFilterForm" property="filter.groupIdString">
                            <html-el:optionsCollection property="userWorkgroups" label="value" value="key" filter="false"/>
                        </html-el:select>
                    </td>
                    <td>
                        <bean-el:message key="actionList.ActionListFilter.filter.label.exclude"/>
                        <html-el:checkbox property="filter.excludeGroupId"/>
                    </td>
                </tr>
                <tr>
                    <th><span class="thnormal"><bean-el:message key="actionList.ActionListFilter.filter.label.documentType"/></span></th>
                    <td class="datacell">
                      <span id="docTypeElementId">
                        <c:out value="${ActionListFilterForm.docTypeFullName}" />
                      </span>
                      <kul:lookup boClassName="org.kuali.kfs.kew.doctype.bo.DocumentType" fieldConversions="name:docTypeFullName"/>
                    </td>
                    <td>
                        <bean-el:message key="actionList.ActionListFilter.filter.label.exclude"/>
                        <html-el:checkbox property="filter.excludeDocumentType"/>
                    </td>
                </tr>
                <tr>
                    <th><span class="thnormal"><bean-el:message key="actionList.ActionListFilter.filter.label.dateCreated"/></span></th>
                    <td class="datacell filter-input">
                        <table class="neutral" border="0" cellspacing="0" cellpadding="1">
                            <tr>
                                <td class="neutral">
                                    <table class="neutral" border="0" cellspacing="0" cellpadding="0">
                                        <tr>
                                            <td style="font-weight: 400; text-align:right" nowrap><bean-el:message key="actionList.ActionListFilter.filter.label.from"/>:</td>
                                            <td nowrap>
                                                <html-el:text property="createDateFrom" styleId="createDateFrom" size="10"/>
                                                <img src="static/images/cal.png" id="createDateFrom_trigger" alt="Click Here to pick up the from date created" width="24" border="0"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="neutral" style="font-weight: 400; text-align:right" nowrap><bean-el:message key="actionList.ActionListFilter.filter.label.to"/>:</td>
                                            <td class="neutral" nowrap>
                                                <html-el:text property="createDateTo" styleId="createDateTo" size="10"/>
                                                <img src="static/images/cal.png" id="createDateTo_trigger" alt="Click Here to pick up the to date created" width="24" border="0"/>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </td>
                    <td>
                        <bean-el:message key="actionList.ActionListFilter.filter.label.exclude"/>
                        <html-el:checkbox property="filter.excludeCreateDate"/>
                    </td>
                </tr>
                <tr>
                    <th><div><span class="thnormal"><bean-el:message key="actionList.ActionListFilter.filter.label.dateLastAssigned"/></span></div></th>
                    <td class="datacell filter-input">
                        <table class="neutral">
                            <tr>
                                <td class="neutral">
                                    <table class="neutral"  border="0" cellspacing="0" cellpadding="1">
                                        <tr>
                                            <td class="neutral" style="font-weight: 400; text-align:right" nowrap><bean-el:message key="actionList.ActionListFilter.filter.label.from"/>:</td>
                                            <td class="neutral"  nowrap>
                                                <html-el:text property="lastAssignedDateFrom" styleId="lastAssignedDateFrom" size="10" />
                                                <img src="static/images/cal.png" id="lastAssignedDateFrom_trigger" alt="Click Here to select the last assigned from date" width="24" border="0"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="neutral" style="font-weight: 400; text-align:right" nowrap><bean-el:message key="actionList.ActionListFilter.filter.label.to"/>:</td>
                                            <td class="neutral" nowrap>
                                                <html-el:text property="lastAssignedDateTo" styleId="lastAssignedDateTo" size="10" />
                                                <img src="static/images/cal.png" id="lastAssignedDateTo_trigger" alt="Click Here to select the last assigned to date" width="24" border="0"/>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </td>
                    <td>
                        <bean-el:message key="actionList.ActionListFilter.filter.label.exclude"/>
                        <html-el:checkbox property="filter.excludeLastAssignedDate"/>
                    </td>
                </tr>
                <!-- CU Customization: Added a preference for controlling the filtering of the action list last modified date column. -->
                <tr>
                    <th><div><span class="thnormal"><bean-el:message key="actionList.ActionListFilter.filter.label.dateLastModified"/></span></div></th>
                    <td class="datacell filter-input">
                        <table class="neutral">
                            <tr>
                                <td class="neutral">
                                    <table class="neutral"  border="0" cellspacing="0" cellpadding="1">
                                        <tr>
                                            <td class="neutral" style="font-weight: 400; text-align:right" nowrap><bean-el:message key="actionList.ActionListFilter.filter.label.from"/>:</td>
                                            <td class="neutral"  nowrap>
                                                <html-el:text property="lastModifiedDateFrom" styleId="lastModifiedDateFrom" size="10" />
                                                <img src="static/images/cal.png" id="lastModifiedDateFrom_trigger" alt="Click Here to select the last modified from date" width="24" border="0"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="neutral" style="font-weight: 400; text-align:right" nowrap><bean-el:message key="actionList.ActionListFilter.filter.label.to"/>:</td>
                                            <td class="neutral" nowrap>
                                                <html-el:text property="lastModifiedDateTo" styleId="lastModifiedDateTo" size="10" />
                                                <img src="static/images/cal.png" id="lastModifiedDateTo_trigger" alt="Click Here to select the last modified to date" width="24" border="0"/>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </td>
                    <td>
                        <bean-el:message key="actionList.ActionListFilter.filter.label.exclude"/>
                        <html-el:checkbox property="filter.excludeLastModifiedDate"/>
                    </td>
                </tr>
            </table>
        </div><!-- end div tabcontainer -->
        <div id="globalbuttons" class="globalbuttons">
            <html-el:hidden property="backLocation" />
            <html-el:submit property="methodToCall.filter" styleClass="btn btn-default">Filter</html-el:submit>
            <html-el:submit property="methodToCall.clear" styleClass="btn btn-default">Clear</html-el:submit>
            <a href="${KualiForm.backLocation}" class="btn btn-default">
                Cancel
            </a>
        </div>
    </div><!-- end div workarea -->
    <script type="text/javascript">
        (function () {
            Calendar.setup({
                inputField     :    "createDateFrom",     // id of the input field
                ifFormat       :    "%m/%d/%Y",     // format of the input field (even if hidden, this format will be honored)
                button         :    "createDateFrom_trigger", // the button or image that triggers this
                showsTime      :    false,            // will display a time selector
                daFormat       :    "%A, %B %d, %Y",// format of the displayed date
                singleClick    :    true,
                step           :    1
            });

            Calendar.setup({
                inputField     :    "createDateTo",     // id of the input field
                ifFormat       :    "%m/%d/%Y",     // format of the input field (even if hidden, this format will be honored)
                button         :    "createDateTo_trigger", // the button or image that triggers this
                showsTime      :    false,            // will display a time selector
                daFormat       :    "%A, %B %d, %Y",// format of the displayed date
                singleClick    :    true,
                step           :    1
            });

            Calendar.setup({
                inputField     :    "lastAssignedDateFrom",     // id of the input field
                ifFormat       :    "%m/%d/%Y",     // format of the input field (even if hidden, this format will be honored)
                button         :    "lastAssignedDateFrom_trigger", // the button or image that triggers this
                showsTime      :    false,            // will display a time selector
                daFormat       :    "%A, %B %d, %Y",// format of the displayed date
                singleClick    :    true,
                step           :    1
            });

            Calendar.setup({
                inputField     :    "lastAssignedDateTo",     // id of the input field
                ifFormat       :    "%m/%d/%Y",     // format of the input field (even if hidden, this format will be honored)
                button         :    "lastAssignedDateTo_trigger", // the button or image that triggers this
                showsTime      :    false,            // will display a time selector
                daFormat       :    "%A, %B %d, %Y",// format of the displayed date
                singleClick    :    true,
                step           :    1
            });
            
            Calendar.setup({
                inputField     :    "lastModifiedDateFrom",     // id of the input field
                ifFormat       :    "%m/%d/%Y",     // format of the input field (even if hidden, this format will be honored)
                button         :    "lastModifiedDateFrom_trigger", // the button or image that triggers this
                showsTime      :    false,            // will display a time selector
                daFormat       :    "%A, %B %d, %Y",// format of the displayed date
                singleClick    :    true,
                step           :    1
            });

            Calendar.setup({
                inputField     :    "lastModifiedDateTo",     // id of the input field
                ifFormat       :    "%m/%d/%Y",     // format of the input field (even if hidden, this format will be honored)
                button         :    "lastModifiedDateTo_trigger", // the button or image that triggers this
                showsTime      :    false,            // will display a time selector
                daFormat       :    "%A, %B %d, %Y",// format of the displayed date
                singleClick    :    true,
                step           :    1
            });
        })()
    </script>
</kul:page>
