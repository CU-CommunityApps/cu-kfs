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
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>
<style type="text/css">
  .custom-preferences th {
    padding-left: 24px;
    font-weight: 400;
    width: 20%;
  }
  .custom-preferences td.subhead {
    font-size: 1.5rem;
    padding: 24px 0 16px 0;
  }
  .custom-preferences td:not(:first-child) {
    padding: 8px 0;
  }
  .custom-preferences tr.document-type-notifications th {
    padding-left: 0;
  }
</style>
<c:set var="showSaveReminder" value="${requestScope.saveReminder}" />
<kul:page
  headerTitle="Workflow Preferences"
  lookup="false"
  headerMenuBar=""
  transactionalDocument="false"
  showDocumentInfo="false"
  htmlFormAction="Preferences"
  docTitle="Workflow Preferences"
  errorKey="*"
>

<html-el:hidden property="returnMapping"/>
<div id="workarea doc">
  <div class="tab-container" align="center">
    <table width="100%" class="datatable-80 custom-preferences" align="center" cellspacing="0">
      <tr>
        <td colspan="2" class="subhead">General</td>
      </tr>
      <tr>
        <th>Automatic Refresh Rate:</th>
        <td class="datacell">
          <html-el:text property="preferences.refreshRate" size="3" />
          <kul:checkErrors keyMatch="preferences.refreshRate" />
          <c:if test="${hasErrors}">
            <kul:fieldShowErrorIcon />
          </c:if>
          in whole minutes - 0 is no automatic refresh.</td>
      </tr>
      <tr>
        <th>Action List Page Size</th>
        <td class="datacell">
          <html-el:text property="preferences.pageSize" size="3" />
            <kul:checkErrors keyMatch="preferences.pageSize" />
            <c:if test="${hasErrors}">
              <kul:fieldShowErrorIcon />
            </c:if>
        </td>
      </tr>
      <tr>
        <th>Delegator Filter</th>
        <td class="datacell">
          <html-el:select property="preferences.delegatorFilter">
            <html-el:options collection="delegatorFilter" labelProperty="value" property="key"/>
          </html-el:select>
        </td>
      </tr>
      <tr>
        <th>Primary Delegate Filter</th>
        <td class="datacell">
          <html-el:select property="preferences.primaryDelegateFilter">
            <html-el:options collection="primaryDelegateFilter" labelProperty="value" property="key"/>
          </html-el:select>
        </td>
      </tr>
      <tr>
        <td colspan="2" class="subhead">Fields Displayed In Action List</td>
      </tr>

      <tr>
        <th>Document Type</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.showDocType" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>

      <tr>
        <th>Title</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.showDocTitle" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>

      <tr>
        <th>Action Requested</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.showActionRequested" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>

      <tr>
        <th>Initiator</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.showInitiator" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>

      <tr>
        <th>Delegator</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.showDelegator" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>

      <tr>
        <th>Date Created</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.showDateCreated" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>
      <tr>
        <th>Date Approved</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.showDateApproved" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>
      
      <%-- CU Customization: Add option for custom Last Modified Date column. --%>
      <tr>
        <th>Date Last Modified</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.showLastModifiedDate" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>
      <tr>
        <th>Current Route Node(s)</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.showCurrentNode" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>
      <tr>
        <th>WorkGroup Request</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.showWorkgroupRequest" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>

      <tr>
        <th>Document Route Status</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.showDocumentStatus" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>


      <tr>
        <th>Clear FYI</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.showClearFyi" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>

      <%-- CU Customization: Add option for custom Action List Notes column. --%>
      <tr>
        <th>Notes</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.showNotes" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>

      <c:if test="${KualiForm.showOutbox }">
      <tr>
        <th>Use Outbox</th>
        <td class="datacell">
          <html-el:checkbox styleClass="nobord" property="preferences.useOutbox" value="${KewApiConstants.PREFERENCES_YES_VAL }"/>
        </td>
      </tr>
      </c:if>

      <tr>
        <td colspan="2" class="subhead">Email Notification Preferences</td>
      </tr>
      <tr>
        <th>Receive Primary Delegate Emails</th>
        <td class="datacell"><html-el:checkbox styleClass="nobord" property="preferences.notifyPrimaryDelegation" value="${KewApiConstants.PREFERENCES_YES_VAL}"/></td>
      </tr>
      <tr>
        <th>Receive Secondary Delegate Emails</th>
        <td class="datacell"><html-el:checkbox styleClass="nobord" property="preferences.notifySecondaryDelegation" value="${KewApiConstants.PREFERENCES_YES_VAL}"/></td>
      </tr>
      <tr>
        <th>Default Email Notification</th>
        <td class="datacell">
          <html-el:select property="preferences.emailNotification">
            <html-el:option value="${KewApiConstants.EMAIL_RMNDR_NO_VAL}">None</html-el:option>
            <html-el:option value="${KewApiConstants.EMAIL_RMNDR_DAY_VAL}">Daily</html-el:option>
            <html-el:option value="${KewApiConstants.EMAIL_RMNDR_WEEK_VAL}">Weekly</html-el:option>
            <html-el:option value="${KewApiConstants.EMAIL_RMNDR_IMMEDIATE}">Immediate</html-el:option>
          </html-el:select>
        </td>
      </tr>
      <tr>
        <th>Document Type Notifications</th>
        <td class="datacell">
          <c:if test="${showSaveReminder}">
          <div style="font-weight: bold;"><bean:message key="docType.preference.save.reminder" /></div>
          </c:if>
          <table>
            <tr class="document-type-notifications">
              <th>Document Type</th>
              <th>Notification Preference</th>
              <th>Actions</th>
            </tr>
            <logic:iterate name="KualiForm" property="preferences.documentTypeNotificationPreferences" id="entry" indexId="status">
            <tr>
              <td>
                <c:set var="documentType"><bean:write name="entry" property="key" /></c:set>
                ${documentType}
                <html-el:hidden name="KualiForm" property="preferences.documentTypeNotificationPreference(${fn:replace(documentType, '.', KewApiConstants.DOCUMENT_TYPE_NOTIFICATION_DELIMITER)})" />
              </td>
              <td>
                <c:set var="preferenceValue"><bean:write name="entry" property="value" /></c:set>
                <c:choose>
                  <c:when test="${preferenceValue == KewApiConstants.EMAIL_RMNDR_NO_VAL}">None</c:when>
                  <c:when test="${preferenceValue == KewApiConstants.EMAIL_RMNDR_DAY_VAL}">Daily</c:when>
                  <c:when test="${preferenceValue == KewApiConstants.EMAIL_RMNDR_WEEK_VAL}">Weekly</c:when>
                  <c:when test="${preferenceValue == KewApiConstants.EMAIL_RMNDR_IMMEDIATE}">Immediate</c:when>
                </c:choose>
              </td>
              <td>
                <html:submit
                  property="methodToCall.deleteNotificationPreference.${documentType}"
                  styleClass="btn btn-default small"
                >
                  Delete
                </html:submit>
              </td>
            </tr>
            </logic:iterate>
            <tr>
              <td>
                <html-el:text name="KualiForm" property="documentTypePreferenceName" />
                <kul:checkErrors keyMatch="documentTypePreferenceName" />
                <c:if test="${hasErrors}">
                  <br/><kul:fieldShowErrorIcon />
                </c:if>
                <kul:lookup boClassName="org.kuali.kfs.kew.doctype.bo.DocumentType" fieldConversions="name:documentTypePreferenceName"/>
              </td>
              <td>
                <html-el:select name="KualiForm" property="documentTypePreferenceValue">
                  <html-el:option value="${KewApiConstants.EMAIL_RMNDR_NO_VAL}">None</html-el:option>
                  <html-el:option value="${KewApiConstants.EMAIL_RMNDR_DAY_VAL}">Daily</html-el:option>
                  <html-el:option value="${KewApiConstants.EMAIL_RMNDR_WEEK_VAL}">Weekly</html-el:option>
                  <html-el:option value="${KewApiConstants.EMAIL_RMNDR_IMMEDIATE}">Immediate</html-el:option>
                </html-el:select>
              </td>
              <td>
                <html:submit
                  property="methodToCall.addNotificationPreference"
                  styleClass="btn small btn-default"
                >
                Add
                </html:submit>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <tr>
        <th>Send Email Notifications For</th>
        <td class="datacell">
          <ul style="padding-left: 0;">
            <li style="list-style-type: none;"><html-el:checkbox styleClass="nobord" property="preferences.notifyComplete" value="${KewApiConstants.PREFERENCES_YES_VAL}"/> Complete</li>
            <li style="list-style-type: none;"><html-el:checkbox styleClass="nobord" property="preferences.notifyApprove" value="${KewApiConstants.PREFERENCES_YES_VAL}"/> Approve</li>
            <li style="list-style-type: none;"><html-el:checkbox styleClass="nobord" property="preferences.notifyAcknowledge" value="${KewApiConstants.PREFERENCES_YES_VAL}"/> Acknowledge</li>
            <li style="list-style-type: none;"><html-el:checkbox styleClass="nobord" property="preferences.notifyFYI" value="${KewApiConstants.PREFERENCES_YES_VAL}"/> FYI</li>
          </ul>
        </td>
      </tr>
    </table>
  </div><!-- End tab-container -->
  <div id="globalbuttons" class="globalbuttons">
    <html-el:hidden property="backLocation" />
    <html-el:submit styleClass="btn btn-default btn-small" property="methodToCall.save">Save</html-el:submit>
    <a href="javascript:document.forms[0].reset()" class="btn btn-default btn-small">
      Reset
    </a>
    <a href="${KualiForm.backLocation}" class="btn btn-default btn-small">
      Cancel
    </a>
  </div>
</div> <!-- End workarea -->
</kul:page>
