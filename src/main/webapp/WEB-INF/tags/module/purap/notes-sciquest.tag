<%--
 Copyright 2005-2007 The Kuali Foundation.

 Licensed under the Educational Community License, Version 1.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.opensource.org/licenses/ecl1.php

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp" %>

<%@ attribute name="notesBo" required="false" type="java.util.List" %>
<%@ attribute name="noteType" required="false" type="java.lang.Enum" %>
<%@ attribute name="displayTopicFieldInNotes" required="false" %>
<%@ attribute name="allowsNoteDelete" required="false" %>
<%@ attribute name="allowsNoteAttachments" required="false" %>
<%@ attribute name="attachmentTypesValuesFinderClass" required="false" %>
<%@ attribute name="transparentBackground" required="false" %>
<%@ attribute name="defaultOpen" required="false" %>
<%@ attribute name="allowsNoteFYI" required="false"
              description="Indicator for determing whether to render the ad hoc fyi recipient box and send fyi button" %>

<c:set var="noteColSpan" value="6"/>

<c:if test="${empty noteType}">
    <%-- default to document header notes this default should probably be set somewhere else --%>
    <c:set var="noteType" value="${Constants.NoteTypeEnum.DOCUMENT_HEADER_NOTE_TYPE}"/>
    <c:set var="notesBo" value="${KualiForm.document.notes}"/>
</c:if>

<c:set var="documentTypeName" value="${KualiForm.document['class'].name}"/>
<c:set var="documentEntry" value="${DataDictionary[documentTypeName]}"/>
<c:set var="allowsNoteAttachments" value="${documentEntry.allowsNoteAttachments}"/>
<c:set var="tabTitle" value="Notes and Attachments"/>
<c:if test="${allowsNoteAttachments eq false}">
    <c:set var="tabTitle" value="Notes"/>
</c:if>

<c:set var="propPrefix" value="${noteType.fullPath}."/>

<c:if test="${not empty attachmentTypesValuesFinderClass}">
    <c:set var="noteColSpan" value="${noteColSpan + 1}"/>
</c:if>

<c:if test="${empty displayTopicFieldInNotes}">
    <c:set var="displayTopicFieldInNotes" value="${documentEntry.displayTopicFieldInNotes}"/>
</c:if>

<c:if test="${displayTopicFieldInNotes eq true}">
    <c:set var="noteColSpan" value="${noteColSpan + 1}"/>
</c:if>

<kul:tab tabTitle="${tabTitle}" defaultOpen="${!empty notesBo or (not empty defaultOpen and defaultOpen)}"
         tabErrorKey="${Constants.DOCUMENT_NOTES_ERRORS}" tabItemCount="${fn:length(notesBo)}"
         transparentBackground="${transparentBackground}">
<c:set var="notesAttributes" value="${DataDictionary.Note.attributes}"/>
<div class="tab-container" align=center id="G4">
<p align=left><jsp:doBody/>

<h3>${tabTitle}</h3>
<table cellpadding="0" cellspacing="0" class="datatable" summary="view/add notes">
<tbody>

<tr>
    <kul:htmlAttributeHeaderCell literalLabel="&nbsp;" scope="col" align="left"/>
    <kul:htmlAttributeHeaderCell attributeEntry="${notesAttributes.notePostedTimestamp}" hideRequiredAsterisk="true"
                                 scope="col" align="left"/>
    <kul:htmlAttributeHeaderCell attributeEntry="${notesAttributes.authorUniversalIdentifier}"
                                 hideRequiredAsterisk="true" scope="col" align="left"/>

    <c:if test="${displayTopicFieldInNotes eq true}">
        <kul:htmlAttributeHeaderCell attributeEntry="${notesAttributes.noteTopicText}" forceRequired="true"
                                     labelFor="newNote.noteTopicText" scope="col" align="left"/>
    </c:if>
    <kul:htmlAttributeHeaderCell attributeEntry="${notesAttributes.noteText}" labelFor="newNote.noteText" scope="col"
                                 align="left"/>
    <c:if test="${allowsNoteAttachments eq true}">
        <kul:htmlAttributeHeaderCell attributeEntry="${notesAttributes.attachment}" labelFor="attachmentFile"
                                     scope="col" align="left"/>
    </c:if>


        <%--mjmc ************************************************************** attachments to sciquest **********************************************************************************************************    --%>
    <c:if test="${(not empty attachmentTypesValuesFinderClass) and (allowsNoteAttachments eq true)}">
        <kul:htmlAttributeHeaderCell literalLabel="Send to Vendor?" scope="col" align="left"/>
    </c:if>
        <%--mjmc ************************************************************** --%>

    <c:if test="${allowsNoteFYI}">
        <kul:htmlAttributeHeaderCell literalLabel="Notification Recipient" scope="col"/>
    </c:if>


    <kul:htmlAttributeHeaderCell literalLabel="Actions" scope="col"/>
</tr>
<html:hidden property="newNote.noteTypeCode" value="${noteType.code}"/>
<c:if test="${ ((not empty attachmentTypesValuesFinderClass) and (allowsNoteAttachments eq true)) || kfunc:canAddNoteAttachment(KualiForm.document)}">
    <tr>
        <kul:htmlAttributeHeaderCell literalLabel="add:" scope="row"/>
        <td class="infoline">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td class="infoline">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <c:if test="${displayTopicFieldInNotes eq true}">
            <td class="infoline"><kul:htmlControlAttribute attributeEntry="${notesAttributes.noteTopicText}"
                                                           property="newNote.noteTopicText" forceRequired="true"/></td>
        </c:if>
        <td class="infoline"><kul:htmlControlAttribute attributeEntry="${notesAttributes.noteText}"
                                                       property="newNote.noteText"
                                                       forceRequired="${notesAttributes.noteText.required}"/></td>
        <c:if test="${allowsNoteAttachments eq true}">
            <td class="infoline">
                <div align="center"><br/>
                    <html:file property="attachmentFile" size="30" styleId="attachmentFile" value=""/><br/><br/>
                    <html:image property="methodToCall.cancelBOAttachment"
                                src="${ConfigProperties.kr.externalizable.images.url}tinygrey-cancel.gif"
                                title="Cancel Attachment" alt="Cancel Attachment" styleClass="tinybutton"/>
                </div>
            </td>
        </c:if>
        <c:if test="${(not empty attachmentTypesValuesFinderClass) and (allowsNoteAttachments eq true)}">


            <%--mjmc 20091016 ---------------  this sets the dropdown list ------------------------------------------------------  --%>
            <c:set var="finderClass"
                   value="${fn:replace(attachmentTypesValuesFinderClass,'.','|')}"/>
            <td class="infoline">
                <div align="center">
                  <c:choose>
                    <c:when test="${KualiForm.docFinal  or KualiForm.docCanceledOrDisapproved}">
                           No
                    </c:when>
                    <c:otherwise>
                    <html:select property="newNote.noteTopicText">
                        <html:optionsCollection
                                property="actionFormUtilMap.getOptionsMap${Constants.ACTION_FORM_UTIL_MAP_METHOD_PARM_DELIMITER}${finderClass}"
                                label="value" value="key"/>
                    </html:select>
                    </c:otherwise>
                  </c:choose>
                </div>
            </td>


        </c:if>
        <c:if test="${allowsNoteFYI}">
            <td>&nbsp;</td>
        </c:if>


        <td class="infoline">
        <input type="hidden" id="finalFlag" value="${KualiForm.docFinal  or KualiForm.docCanceledOrDisapproved}">

            <div align="center"><html:image property="methodToCall.insertBONote"
                                            src="${ConfigProperties.kr.externalizable.images.url}tinybutton-add1.gif"
                                            alt="Add a Note" title="Add a Note" styleClass="tinybutton"
                                            onclick="javascript:if (document.getElementById('finalFlag').value =='true') {
                                            alert('Caution: The addition of a note or note with attachment will NOT cause this document to route. Attachments will NOT be sent to the vendor, nor will a Contract Manager view your note/attachment.');}"/>
                                            </div>
    </tr>
</c:if>


<c:forEach var="note" items="${notesBo}" varStatus="status">

    <c:set var="authorUniversalIdentifier" value="${note.authorUniversalIdentifier}"/>
    <c:if test="${kfunc:canViewNoteAttachment(KualiForm.document, null)}">

    </c:if>


    <tr>
        <kul:htmlAttributeHeaderCell literalLabel="${status.index + 1}" scope="row"/>
        <td class="datacell center">
            <bean:write name="KualiForm" property="document.notes[${status.index}].notePostedTimestamp"/>
            &nbsp;</td>

        <td class="datacell center">
                <bean:write name="KualiForm" property="document.notes[${status.index}].authorUniversal.name"/>
                <%-- NEED TO ADD THIS TOPIC FIELD TO DATABASE --%>
            <c:if test="${displayTopicFieldInNotes eq true}">
        <td class="datacell center">
            <bean:write name="KualiForm" property="document.notes[${status.index}].noteTopicText"/></td>
        </c:if>

        <td class="datacell center"><bean:write name="KualiForm"
                                                property="document.notes[${status.index}].noteText"/></td>

        <c:choose>
            <c:when test="${(!empty note.attachment) and (note.attachment.complete)}">
                <td class="datacell center">

                    <c:if test="${allowsNoteAttachments eq true}">
                        <c:if test="${(!empty note.attachment)}">
                            <c:set var="attachmentTypeCode" value="${note.attachment.attachmentTypeCode}"/>
                        </c:if>
                        <c:if test="${kfunc:canViewNoteAttachment(KualiForm.document, attachmentTypeCode)}">
                            <html:image property="methodToCall.downloadBOAttachment.attachment[${status.index}]"
                                        src="${ConfigProperties.kr.externalizable.images.url}clip.gif"
                                        title="download attachment" alt="download attachment" style="padding:5px"
                                        onclick="excludeSubmitRestriction=true"/>
                        </c:if>
                        <bean:write name="KualiForm"
                                    property="document.notes[${status.index}].attachment.attachmentFileName"/>
                        &nbsp;
                                    <span style="white-space: nowrap">
                                      <kul:fileSize byteSize="${note.attachment.attachmentFileSize}">
                                          (<c:out value="${fileSize} ${fileSizeUnits}"/>, <bean:write name="KualiForm"
                                                                                                      property="document.notes[${status.index}].attachment.attachmentMimeTypeCode"/>)
                                      </kul:fileSize>
                                    </span>
                    </c:if>
                </td>


                <c:if test="${(not empty attachmentTypesValuesFinderClass) and (allowsNoteAttachments eq true)}">
                    <c:set var="finderClass" value="${fn:replace(attachmentTypesValuesFinderClass,'.','|')}"/>

                    <%--mjmc ****************    drop down list set value (yes or no) ********************************************** attachments to sciquest ********************************    --%>
                    <%--mjmc ****************************************************************************************************************************************************************    --%>
                    <td class="datacell center">
                        <div align="center">
                  <c:choose>
                    <c:when test="${KualiForm.docFinal or KualiForm.docCanceledOrDisapproved}">
                           <c:if test="${'sendToVendor' eq note.noteTopicText}">Yes</c:if>
                            <c:if test="${'dontSendToVendor' eq note.noteTopicText}">No</c:if>
                            <c:if test="${empty note.noteTopicText}">No</c:if> 
                    </c:when>
                    <c:otherwise>
                    <html:select property="document.notes[${status.index}].noteTopicText">
                        <html:optionsCollection
                                property="actionFormUtilMap.getOptionsMap${Constants.ACTION_FORM_UTIL_MAP_METHOD_PARM_DELIMITER}${finderClass}"
                                label="value" value="key"/>
                    </html:select>
                    </c:otherwise>
                  </c:choose>
                    
                        </div>
                    </td>

                </c:if>
            </c:when>
            <c:otherwise>
                <td class="datacell center">&nbsp;</td>
                <c:if test="${(not empty attachmentTypesValuesFinderClass) and (allowsNoteAttachments eq true)}">

                    <td class="datacell center">
                        <div align="center">
                        
                            <c:if test="${'sendToVendor' eq note.noteTopicText}">Yes</c:if>
                            <c:if test="${'dontSendToVendor' eq note.noteTopicText}">No</c:if>
                            <c:if test="${empty note.noteTopicText}">No</c:if>
                        </div>
                    </td>


                </c:if>
            </c:otherwise>
        </c:choose>

        <c:if test="${allowsNoteFYI}">
            <td class="infoline">
                <div align="center">
                    <c:if test="${!empty KualiForm.documentActions[Constants.KUALI_ACTION_CAN_SEND_NOTE_FYI]}">
                        <kul:user userIdFieldName="document.notes[${status.index}].adHocRouteRecipient.id"
                                  userId="${note.adHocRouteRecipient.id}"
                                  universalIdFieldName=""
                                  universalId=""
                                  userNameFieldName="document.notes[${status.index}].adHocRouteRecipient.name"
                                  userName="${note.adHocRouteRecipient.name}"
                                  readOnly="false"
                                  fieldConversions="principalName:document.notes[${status.index}].adHocRouteRecipient.id,name:document.notes[${status.index}].adHocRouteRecipient.name"
                                  lookupParameters="document.notes[${status.index}].adHocRouteRecipient.id:principalName"/> 
                    </c:if>
                    <c:if test="${empty KualiForm.documentActions[Constants.KUALI_ACTION_CAN_SEND_NOTE_FYI]}">
                        &nbsp;
                    </c:if>
                </div>
            </td>
        </c:if>


        <td class="datacell center">
            <div align="center">
                <c:if test="${kfunc:canDeleteNoteAttachment(KualiForm.document, attachmentTypeCode, authorUniversalIdentifier)}">
                    <html:image property="methodToCall.deleteBONote.line${status.index}"
                                src="${ConfigProperties.kr.externalizable.images.url}tinybutton-delete1.gif"
                                title="Delete a Note" alt="Delete a Note" styleClass="tinybutton"/>
                </c:if> &nbsp;
                <c:if test="${allowsNoteFYI && !empty KualiForm.documentActions[Constants.KUALI_ACTION_CAN_SEND_NOTE_FYI]}">
                    <html:image property="methodToCall.sendNoteWorkflowNotification.line${status.index}"
                                src="${ConfigProperties.kr.externalizable.images.url}tinybutton-send.gif"
                                title="Send FYI" alt="Send FYI" styleClass="tinybutton"/>
                </c:if>


            </div>
        </td>
    </tr>

</c:forEach>
  <c:if test="${KualiForm.docEnroute}" >
   <tr>
     <th rowspan="1" colspan="2" scope="col" align="left">Reason to change 'Send to Vendor' requirement
     </th>             
     <td colspan="6" class="datacell">
                <kul:htmlControlAttribute attributeEntry="${notesAttributes.noteText}" property="reasonToChange" />
     </td> 
       
   </tr>
   </c:if>


</tbody>
</table>
</div>
</kul:tab>