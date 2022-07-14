<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2021 Kuali, Inc.

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
    div.infoline {
        border-top: 1px solid #E7E7E7;
        padding-top: 20px;
        width: 50%;
        margin: 26px auto;
    }
    div.apply-default {
        padding: 16px;
    }
    div.apply-default input {
        margin-left: 8px;
    }
    span.empty-message {
        padding-left: 24px;
        position: relative;
        top: 8px;
    }
</style>

<%-- Setup column labels based on ApplicationsResources --%>
<bean:define id="documentIdLabel">
    <bean-el:message key="actionList.ActionList.results.label.documentId" />
</bean:define>
<bean:define id="typeLabel">
    <bean-el:message key="actionList.ActionList.results.label.type" />
</bean:define>
<bean:define id="titleLabel">
    <bean-el:message key="actionList.ActionList.results.label.title" />
</bean:define>
<bean:define id="routeStatusLabel">
    <bean-el:message key="actionList.ActionList.results.label.routeStatus" />
</bean:define>
<bean:define id="actionRequestedLabel">
    <bean-el:message key="actionList.ActionList.results.label.actionRequested" />
</bean:define>
<bean:define id="initiatorLabel">
    <bean-el:message key="actionList.ActionList.results.label.initiator" />
</bean:define>
<bean:define id="delegatorLabel">
    <bean-el:message key="actionList.ActionList.results.label.delegator" />
</bean:define>
<bean:define id="dateCreatedLabel">
    <bean-el:message key="actionList.ActionList.results.label.dateCreated" />
</bean:define>
<bean:define id="dateApprovedLabel">
    <bean-el:message key="actionList.ActionList.results.label.dateApproved" />
</bean:define>
<%-- CU Customization: Load label related to custom Last Modified Date column. --%>
<bean:define id="lastModifiedDateLabel">
    <bean-el:message key="actionList.ActionList.results.label.dateLastModified" />
</bean:define>
<bean:define id="currentRouteNodesLabel">
    <bean-el:message key="actionList.ActionList.results.label.currentRouteNodes" />
</bean:define>
<bean:define id="workgroupRequestLabel">
    <bean-el:message key="actionList.ActionList.results.label.workgroupRequest" />
</bean:define>
<bean:define id="actionsLabel">
    <bean-el:message key="actionList.ActionList.results.label.actions" />
</bean:define>
<bean:define id="routeLogLabel">
    <bean-el:message key="actionList.ActionList.results.label.routeLog" />
</bean:define>
<bean:define id="outboxActionItemDelete">
    Delete Item
</bean:define>
<bean:define id="emptyActionListMessage">
    <bean-el:message key="actionList.ActionList.emptyList" />
</bean:define>
<bean:define id="emptyOutboxMessage">
    <bean-el:message key="actionList.Outbox.emptyList" />
</bean:define>
<%-- CU Customization: Load labels and messages related to custom Action List Notes column. --%>
<bean:define id="actionItemNotesLabel">
    <bean-el:message key="actionList.ActionList.results.label.notes" />
</bean:define>
<bean:define id="actionItemNotesMaxLength">
    <bean-el:message key="actionList.ActionList.results.notes.maxLength" />
</bean:define>
<bean:define id="actionNotesSuccessSaveMsg">
    <bean-el:message key="actionList.ActionList.results.saved.successfully" />
</bean:define>
<bean:define id="actionNotesTruncateMsg">
    <bean-el:message key="actionList.ActionList.results.saved.with.truncate" />
</bean:define>

<c:url var="actionListURI" value="ActionList.do">
    <c:param name="methodToCall" value="start"/>
    <c:param name="currentPage" value="${ActionListForm.currentPage}"/>
    <c:param name="currentSort" value="${ActionListForm.currentSort}"/>
    <c:param name="currentDir" value="${ActionListForm.currentDir}"/>
</c:url>

<kul:page headerTitle="Action List" lookup="true"
          transactionalDocument="false" showDocumentInfo="false"
          htmlFormAction="ActionList" docTitle="Action Lists">
    <kul:csrf />
    <script language="JavaScript" src="scripts/en-common.js"></script>
    <script language="JavaScript" src="scripts/kew/actionlist-common.js"></script>
    <%-- CU Customization: Add CU-specific scripts. --%>
    <script language="JavaScript" src="scripts/kew/cu-actionlist-common.js"></script>
    <c:if test="${!ActionListForm.viewOutbox}">
        <script language="JavaScript" src="dwr/interface/ActionListService.js"></script>
    </c:if>
    <%-- End custom CU scripts. --%>
    <style type="text/css">
        <!--
        tr.over { background-color:#CCFFFF; }
        tr.actionlist_anyRow:hover { background-color:#CCFFFF; }
        tr.actionlist_anyRow { visibility:visible; }
        -->
    </style>
    <%-- Since we are using the external paging and sorting features of the display tag now, if a new sortable column is added, remember to add it to the
       ActionItemComparator in the ActionListAction as well --%>
    <div class="headerarea-small" id="headerarea-small">
        <div><h1><c:out value="Action List" /></h1></div>
        <div class="lookupcreatenew">
                <html-el:submit property="methodToCall.viewPreferences" styleClass="btn btn-default" alt="preferences" title="preferences">Preferences</html-el:submit>
                <html-el:submit property="methodToCall.start" styleClass="btn btn-default" alt="refresh" title="refresh">Refresh</html-el:submit>
                <html-el:submit property="methodToCall.viewFilter" styleClass="btn btn-default" alt="filter" title="filter">Filter</html-el:submit>

            <!-- Delegator selection list -->

            <c:if test="${! empty ActionListForm.delegators}">
                <html-el:hidden property="oldDelegationId" value="${ActionListForm.delegationId}" />
                <div style="float:left; width:226px; position: relative; top: -.5em;">
                    <html-el:select property="delegationId" onchange="document.forms[0].methodToCall.value='start';if(document.forms[0].primaryDelegateId){document.forms[0].primaryDelegateId.value='${KewApiConstants.PRIMARY_DELEGATION_DEFAULT}';}document.forms[0].submit();">
                        <html-el:option value="${KewApiConstants.DELEGATION_DEFAULT}"><c:out value="${KewApiConstants.DELEGATION_DEFAULT}" /></html-el:option>
                        <html-el:option value="${KewApiConstants.ALL_CODE}"><c:out value="${KewApiConstants.ALL_SECONDARY_DELEGATIONS}" /></html-el:option>
                        <c:forEach var="delegator" items="${ActionListForm.delegators}">
                            <html-el:option value="${delegator.recipientId}"><c:out value="${delegator.displayName}" /></html-el:option>
                        </c:forEach>
                    </html-el:select>
                </div>
            </c:if>

            <!-- Primary Delegate selection list -->
            <c:if test="${! empty ActionListForm.primaryDelegates}">
                <html-el:hidden property="oldPrimaryDelegateId" value="${ActionListForm.primaryDelegateId}" />
                <html-el:select property="primaryDelegateId" onchange="document.forms[0].methodToCall.value='start';if(document.forms[0].delegationId){document.forms[0].delegationId.value='${KewApiConstants.DELEGATION_DEFAULT}';}document.forms[0].submit();">
                    <html-el:option value="${KewApiConstants.PRIMARY_DELEGATION_DEFAULT}"><c:out value="${KewApiConstants.PRIMARY_DELEGATION_DEFAULT}" /></html-el:option>
                    <html-el:option value="${KewApiConstants.ALL_CODE}"><c:out value="${KewApiConstants.ALL_PRIMARY_DELEGATES}" /></html-el:option>
                    <c:forEach var="primaryDelegate" items="${ActionListForm.primaryDelegates}">
                        <html-el:option value="${primaryDelegate.recipientId}"><c:out value="${primaryDelegate.displayName}" /></html-el:option>
                    </c:forEach>
                </html-el:select>
            </c:if>
            <c:if test="${userSession.objectMap[KewApiConstants.ACTION_LIST_FILTER_ATTR_NAME] != null && userSession.objectMap[KewApiConstants.ACTION_LIST_FILTER_ATTR_NAME].filterOn}">
                    <a class="btn btn-default" href='<c:out value="ActionList.do?methodToCall=clearFilter" />'  title="clearFilter" alt="Clear Filter">
                        Clear Filter
                    </a>
            </c:if>

            <c:if test="${helpDeskActionList != null}">
                <!--<p> Testing is this shows up on the screen </p> -->
                    <html-el:text property="helpDeskActionListUserName" size="12" />
                    <html-el:submit property="methodToCall.helpDeskActionListLogin" styleClass="btn btn-default">Help Desk</html-el:submit>
                <c:if test="${userSession.objectMap[KewApiConstants.HELP_DESK_ACTION_LIST_PERSON_ATTR_NAME] != null}">
                    <a href="
					<c:url value="ActionList.do">
						<c:param name="methodToCall" value="clearHelpDeskActionListUser" />
					</c:url>">Clear <c:out value="${userSession.objectMap[KewApiConstants.HELP_DESK_ACTION_LIST_PERSON_ATTR_NAME].name}"/>'s List</a>
                </c:if>
            </c:if>

        </div>
    </div>

    <div class="apply-default" align="right">
        <c:if
                test="${userSession.objectMap[KewApiConstants.HELP_DESK_ACTION_LIST_PERSON_ATTR_NAME] == null && ! empty actionList && ! empty ActionListForm.defaultActions}">
            <c:set var="defaultActions" value="${ActionListForm.defaultActions}" scope="request" />
            <html-el:select styleId='defaultAction' property="defaultActionToTake">
                <html-el:options collection="defaultActions" labelProperty="value" property="key" filter="false" />
                </html-el:select>
                <html-el:button property="false" styleClass="btn btn-default" onclick="setActions();">Apply Default</html-el:button>
        </c:if>
    </div>
    <c:if
            test="${!empty preferences.refreshRate && preferences.refreshRate != 0}">
        <c:if test="${!noRefresh}">
            <META HTTP-EQUIV="Refresh"
                  CONTENT="<c:out value="${preferences.refreshRate * 60}"/>; URL=ActionList.do">
        </c:if>
    </c:if>
    <html-el:form action="ActionList">
        <html-el:hidden property="methodToCall" value="" />
        <table width="100%">
            <tr>
                <td>
                    <table align="center" width="100%" border="0" cellpadding="0" cellspacing="0">
                        <tr>
                            <td>
                                <kul:errors errorTitle="Error loading action list : "/>
                                <kul:messages/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                            <table style="margin-left: 24px; width: 97%;" cellspacing="0" cellpadding="0">
                                <tr>
                                    <td>

                                        <c:choose>
                                            <c:when test="${ActionListForm.viewOutbox && ActionListForm.showOutbox}">
                                                <a href="<c:url value="ActionList.do?methodToCall=start&viewOutbox=false" />">
                                                    <bean-el:message key="actionList.ActionList.title" />
                                                </a>
                                                |
                                                <strong><bean-el:message key="actionList.Outbox.title" /></strong>
                                            </c:when>
                                            <c:otherwise>
                                                <strong>
                                                    <bean-el:message key="actionList.ActionList.title" />
                                                </strong>
                                                |
                                                <c:if test="${ActionListForm.showOutbox }">
                                                    <a href="<c:url value="ActionList.do?methodToCall=start&viewOutbox=true" />">
                                                        <bean-el:message key="actionList.Outbox.title" />
                                                    </a>
                                                </c:if>
                                            </c:otherwise>
                                        </c:choose>

                                    </td>
                                    <td>
                                        <div align="right">
                                            <c:if test="${ActionListForm.viewOutbox && ActionListForm.showOutbox && !ActionListForm.outBoxEmpty}">
                                                <html-el:submit
                                                        styleClass="btn btn-default"
                                                        property="methodToCall.removeOutboxItems" style="border-style:none;"
                                                >Delete Selected Items</html-el:submit>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        </tr>
                        <c:if test="${userSession.objectMap[KewApiConstants.ACTION_LIST_FILTER_ATTR_NAME].filterLegend != null && userSession.objectMap[KewApiConstants.ACTION_LIST_FILTER_ATTR_NAME].filterLegend != ''}">
                            <tr>
                                <td>
                                    <strong>
                                        <c:out value="${userSession.objectMap[KewApiConstants.ACTION_LIST_FILTER_ATTR_NAME].filterLegend}" />
                                    </strong>
                                </td>
                            </tr>
                        </c:if>
                        <tr>
                            <td>
                                <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                    <tr>
                                        <td>
                                            <div class="search-results">
                                                <display:table
                                                    class="datatable-100"
                                                    cellpadding="2"
                                                    cellspacing="0"
                                                    name="actionListPage"
                                                    pagesize="${preferences.pageSize}"
                                                    export="true"
                                                    id="result"
                                                    htmlId="row"
                                                    excludedParams="*"
                                                    requestURI="${actionListURI}"
                                                    style="padding=24px;"
                                                >
                                                    <display:setProperty name="export.banner" value="" />
                                                    <display:setProperty name="css.tr.even" value="even" />
                                                    <display:setProperty name="css.tr.odd" value="odd" />
                                                    <c:choose>
                                                        <c:when test="${ActionListForm.viewOutbox}">
                                                            <display:setProperty name="basic.msg.empty_list" value="<span class=\"empty-message\">${emptyOutboxMessage}</span>" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <display:setProperty name="basic.msg.empty_list" value="<span class=\"empty-message\">${emptyActionListMessage}</span>" />
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <display:column
                                                        sortable="true"
                                                        title="${documentIdLabel}"
                                                        sortProperty="documentId"
                                                    >
                                                        <c:choose>
                                                            <c:when test="${userSession.objectMap[KewApiConstants.HELP_DESK_ACTION_LIST_PERSON_ATTR_NAME] == null}">
                                                                <a
                                                                    href="<c:url value="${KewApiConstants.DOC_HANDLER_REDIRECT_PAGE}" >
                                                                    <c:param name="${KewApiConstants.DOCUMENT_ID_PARAMETER}" value="${result.documentId}"/>
                                                                    <c:param name="${KewApiConstants.COMMAND_PARAMETER}" value="${KewApiConstants.ACTIONLIST_COMMAND}" />
                                                                    </c:url>"
                                                                    class="showvisit"> <c:out value="${result.documentId}" />
                                                                </a>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <c:out value="${result.documentId}" />
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </display:column>
                                                    <c:if test="${preferences.showDocType == KewApiConstants.PREFERENCES_YES_VAL}">
                                                        <display:column property="docLabel" sortable="true" title="${typeLabel}" />
                                                    </c:if>
                                                    <c:if test="${preferences.showDocTitle == KewApiConstants.PREFERENCES_YES_VAL}">
                                                        <display:column sortProperty="docTitle" sortable="true" title="${titleLabel}" class="infocell">
                                                            <c:out value="${result.docTitle}" />&nbsp;
                                                        </display:column>
                                                    </c:if>
                                                    <c:if test="${preferences.showDocumentStatus == KewApiConstants.PREFERENCES_YES_VAL}">
                                                        <display:column property="routeHeader.combinedStatus"
                                                                        sortable="true" title="${routeStatusLabel}" class="infocell" />
                                                    </c:if>
                                                    <c:if test="${preferences.showActionRequested == KewApiConstants.PREFERENCES_YES_VAL}">
                                                        <display:column property="actionRequestLabel" sortable="true" title="${actionRequestedLabel}" class="infocell" />
                                                    </c:if>
                                                    <c:if test="${preferences.showInitiator == KewApiConstants.PREFERENCES_YES_VAL}">
                                                        <display:column sortable="true" title="${initiatorLabel}"
                                                                                        sortProperty="routeHeader.initiatorName" class="infocell">
                                                            <kul:inquiry boClassName="org.kuali.kfs.kim.impl.identity.PersonImpl"
                                                                         keyValues="principalId=${result.routeHeader.initiatorPrincipalId}"
                                                                         render="true">
                                                            <c:out value="${result.routeHeader.initiatorName}" />
                                                        </kul:inquiry>
                                                        </display:column>
                                                    </c:if>
                                                    <c:if test="${preferences.showDelegator == KewApiConstants.PREFERENCES_YES_VAL}">
                                                        <display:column sortable="true" title="${delegatorLabel}" sortProperty="delegatorName" class="infocell">
                                                            <c:choose>
                                                                <c:when test="${result.delegatorPrincipalId != null}">
                                                                <kul:inquiry boClassName="org.kuali.kfs.kim.impl.identity.PersonImpl"
                                                                             keyValues="principalId=${result.delegatorPrincipalId}"
                                                                             render="true">
                                                                    <c:out value="${result.delegatorName}" />
                                                                </kul:inquiry>
                                                                </c:when>
                                                                <c:when test="${result.delegatorGroupId != null}">
                                                                    <kul:inquiry boClassName="org.kuali.kfs.kim.impl.group.Group" keyValues="id=${result.delegatorGroupId}" render="true">
                                                                <c:out value="${result.delegatorName}" />
                                                                </kul:inquiry>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    &nbsp;
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </display:column>
                                                    </c:if>
                                                    <c:if test="${preferences.showDateCreated == KewApiConstants.PREFERENCES_YES_VAL}">
                                                        <display:column sortable="true" title="${dateCreatedLabel}"
                                                                                        sortProperty="routeHeader.createDate" class="infocell">
                                                            <fmt:formatDate value="${result.routeHeader.createDate}"
                                                                                    pattern="${KFSConstants.DEFAULT_DATE_FORMAT_PATTERN}" />&nbsp;
                                                        </display:column>
                                                    </c:if>
                                                    <c:if test="${preferences.showDateApproved == KewApiConstants.PREFERENCES_YES_VAL}">
                                                        <display:column sortable="true" title="${dateApprovedLabel}" sortProperty="lastApprovedDate" class="infocell">
                                                            <fmt:formatDate value="${result.lastApprovedDate}"
                                                                            pattern="${KFSConstants.DEFAULT_DATE_FORMAT_PATTERN}" />&nbsp;
                                                        </display:column>
                                                    </c:if>
                                                    <%-- CU Customization: Add column for Last Modified Date. --%>
                                                    <c:if test="${preferences.showLastModifiedDate == KewApiConstants.PREFERENCES_YES_VAL}">
                                                        <display:column sortable="true" title="${lastModifiedDateLabel}" 
                                                              sortProperty="lastModifiedDate" class="infocell">
                                                            <fmt:formatDate value="${result.lastModifiedDate}"
                                                                            pattern="${KFSConstants.DEFAULT_DATE_FORMAT_PATTERN}" />&nbsp;
                                                        </display:column>
                                                    </c:if>
                                                    <c:if test="${preferences.showWorkgroupRequest == KewApiConstants.PREFERENCES_YES_VAL}">
                                                        <display:column sortable="true" title="${workgroupRequestLabel}" sortProperty="groupName" class="infocell">
                                                            <c:choose>
                                                                <c:when test="${!empty result.groupId}">
                                                                    <kul:inquiry boClassName="org.kuali.kfs.kim.impl.group.Group" keyValues="id=${result.groupId}" render="true">
                                                                        <c:out value="${result.groupName}" />
                                                                    </kul:inquiry>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    &nbsp;
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </display:column>
                                                    </c:if>
                                                    <c:if test="${preferences.showCurrentNode == KewApiConstants.PREFERENCES_YES_VAL}">
                                                        <display:column
                                                            sortable="true"
                                                            title="${currentRouteNodesLabel}"
                                                            sortProperty="routeHeader.currentRouteLevelName" class="infocell">
                                                                <c:out value="${result.routeHeader.currentRouteLevelName}" />&nbsp;
                                                        </display:column>
                                                    </c:if>
                                                    <%-- CU Customization: Add column for Action List Notes. --%>
                                                    <c:if test="${preferences.showNotes == KewApiConstants.PREFERENCES_YES_VAL}">
                                                        <display:column sortable="true" title="${actionItemNotesLabel}"
                                                              sortProperty="extension.actionNoteForSorting" class="infocell">
                                                            <html-el:textarea cols="50" rows="2"
                                                                  disabled="${ActionListForm.viewOutbox}"
                                                                  property="actions[${result.actionListIndex}].actionNote"
                                                                  value="${ActionListForm.viewOutbox ? '' : result.extension.actionNote}"
                                                                  onchange="saveActionNoteChange(this,'${result.id}','${actionNotesSuccessSaveMsg}');"
                                                                  onkeyup="truncateNoteTextIfNecessary(this,${actionItemNotesMaxLength},'${actionNotesTruncateMsg}');"/>
                                                            <span id="actions[${result.actionListIndex}].actionNote.status">&nbsp;</span>
                                                        </display:column>
                                                    </c:if>
                                                    <c:if
                                                            test="${! ActionListForm.viewOutbox && userSession.objectMap[KewApiConstants.HELP_DESK_ACTION_LIST_PERSON_ATTR_NAME] == null && ActionListForm.hasCustomActions && (ActionListForm.customActionList || (preferences.showClearFyi == KewApiConstants.PREFERENCES_YES_VAL))}">
                                                        <display:column title="${actionsLabel}" class="infocell">
                                                            <c:if test="${! empty result.customActions}">
                                                                <c:set var="customActions" value="${result.customActions}"
                                                                       scope="request" />
                                                                <html-el:hidden
                                                                        property="actions[${result.actionListIndex}].actionItemId"
                                                                        value="${result.id}" />
                                                                <html-el:select
                                                                        property="actions[${result.actionListIndex}].actionTakenCd">
                                                                    <html-el:options collection="customActions"
                                                                                     labelProperty="value" property="key" filter="false" />
                                                                </html-el:select>
                                                                <c:set var="customActionsPresent" value="true" />
                                                            </c:if>&nbsp;
                                                        </display:column>
                                                    </c:if>
                                                    <c:if test="${ActionListForm.viewOutbox }">
                                                        <display:column title="${outboxActionItemDelete}" class="infocell">
                                                            <html-el:checkbox property="outboxItems" value="${result.id}" />
                                                        </display:column>
                                                    </c:if>

                                                    <display-e1:column title="Testing" class="infocell">
                                                        Testing
                                                    </display-e1:column>
                                                    <display:column title="${routeLogLabel}" class="infocell">
                                                    <div align="center">
                                                        <a href="<c:url value="RouteLog.do"><c:param name="documentId" value="${result.documentId}"/></c:url>&mode=modal" data-remodal-target="modal">
                                                            View
                                                        </a>
                                                    </div>
                                                    </display:column>
                                                </display:table>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        <c:if
            test="${userSession.objectMap[KewApiConstants.HELP_DESK_ACTION_LIST_PERSON_ATTR_NAME] == null && (! empty customActionsPresent) && (preferences.showClearFyi == KewApiConstants.PREFERENCES_YES_VAL || ActionListForm.customActionList)}">
            <div class="infoline">
                <div align="center">
                    <a class="btn btn-default" id="takeMassActions" href="javascript: setMethodToCallAndSubmit('takeMassActions')">
                        Take Action
                    </a>
                </div>
            </div>
        </c:if>
    </html-el:form>
</kul:page>