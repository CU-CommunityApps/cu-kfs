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

<%@ attribute name="notesBo" required="false" type="java.util.List" %>
<%@ attribute name="noteType" required="false" type="java.lang.Enum" %>
<%@ attribute name="displayTopicFieldInNotes" required="false" %>
<%@ attribute name="attachmentTypesValuesFinder" required="false" %>
<%@ attribute name="transparentBackground" required="false" %>
<%@ attribute name="defaultOpen" required="false" %>

<c:if test="${empty noteType}">
  <%-- default to document header notes this default should probably be set somewhere else --%>
  <c:set var="noteType" value="${Constants.NoteTypeEnum.DOCUMENT_HEADER_NOTE_TYPE}"/>
  <c:set var="notesBo" value="${KualiForm.document.notes}" />
</c:if>

<c:set var="sendToVendorValuesFinder" value="edu.cornell.kfs.module.purap.businessobject.options.RequisitionAttachmentTypeValuesFinder" />
<c:set var="documentTypeName" value="${KualiForm.docTypeName}" />
<c:set var="documentEntry" value="${DataDictionary[documentTypeName]}" />
<c:set var="allowsNoteAttachments" value="${documentEntry.allowsNoteAttachments}" />
<c:set var="allowsNoteFYI" value="${documentEntry.allowsNoteFYI}" />
<c:set var="tabTitle" value="Notes and Attachments" />
<c:if test="${allowsNoteAttachments eq false}">
  <c:set var="tabTitle" value="Notes" />
</c:if>

<c:if test="${empty displayTopicFieldInNotes}">
  <c:set var="displayTopicFieldInNotes" value="${documentEntry.displayTopicFieldInNotes}" />
</c:if>

<kul:tab tabTitle="${tabTitle}" defaultOpen="${!empty notesBo or (not empty defaultOpen and defaultOpen)}" tabErrorKey="${Constants.DOCUMENT_NOTES_ERRORS}" tabItemCount="${fn:length(notesBo)}" transparentBackground="${transparentBackground}" >
    <c:set var="notesAttributes" value="${DataDictionary.Note.attributes}" />
    <div class="tab-container" align=center id="G4">
    <jsp:doBody/>
        <table class="datatable items standard" summary="view/add notes">
            <tbody>
                <c:if test="${ ((not empty attachmentTypesValuesFinder) and (allowsNoteAttachments eq true)) || kfunc:canAddNoteAttachment(KualiForm.document)}" >
                    <tr>
                        <td class="infoline">&nbsp;</td>
                        <td class="infoline">&nbsp;</td>
                        <td class="infoline">&nbsp;</td>
                        <c:if test="${displayTopicFieldInNotes eq true}">
                            <td class="infoline">
                                <kul:htmlAttributeLabel attributeEntry="${notesAttributes.noteTopicText}" forceRequired="true" />
                                <br/>
                                <kul:htmlControlAttribute attributeEntry="${notesAttributes.noteTopicText}" property="newNote.noteTopicText" forceRequired="true" />
                            </td>
                        </c:if>
                        <td class="infoline">
                            <kul:htmlAttributeLabel attributeEntry="${notesAttributes.noteText}" forceRequired="${notesAttributes.noteText.required}" />
                            <br/>
                            <kul:htmlControlAttribute attributeEntry="${notesAttributes.noteText}" property="newNote.noteText" forceRequired="${notesAttributes.noteText.required}" />
                        </td>

                        <c:set var="noteAddButtonOnClick" value="" />
                        <c:if test="${allowsNoteAttachments eq true}">
                            <c:set var="noteAddButtonOnClick" value="this.form.encoding='multipart/form-data'; return true;" />
                            <td class="infoline">
                                <kul:htmlAttributeLabel attributeEntry="${notesAttributes.attachment}" />
                                <br/>
                                <html:file property="attachmentFile" size="30" styleId="attachmentFile" value="" />
                                <br/>
                                <html:submit property="methodToCall.cancelBOAttachment" title="Cancel Attachment" alt="Remove Attachment" styleClass="tinybutton btn btn-default small" value="Remove Attachment"/>
                            </td>
                        </c:if>

                        <c:if test="${(not empty attachmentTypesValuesFinder) and (allowsNoteAttachments eq true)}">
                            <c:set var="finderClass" value="${fn:replace(attachmentTypesValuesFinder,'.','|')}"/>
                            <td class="infoline">
                                Attachment Type
                                <br/>
                                <html:select property="newNote.attachment.attachmentTypeCode">
                                    <html:optionsCollection property="actionFormUtilMap.getOptionsMap${Constants.ACTION_FORM_UTIL_MAP_METHOD_PARM_DELIMITER}${finderClass}" label="value" value="key"/>
                                </html:select>
                            </td>
                            <c:set var="finderClass1" value="${fn:replace(sendToVendorValuesFinder,'.','|')}"/>
                            <td class="infoline">
                                Send to Vendor?
                                <br/>
                                <c:choose>
                                    <c:when test="${KualiForm.docFinal}">
                                        No
                                    </c:when>
                                    <c:otherwise>
                                        <html:select property="newNote.noteTopicText">
                                            <html:optionsCollection
                                                    property="actionFormUtilMap.getOptionsMap${Constants.ACTION_FORM_UTIL_MAP_METHOD_PARM_DELIMITER}${finderClass1}"
                                                    label="value" value="key"/>
                                        </html:select>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </c:if>

                        <td class="infoline">
                            <html:submit property="methodToCall.insertBONote" alt="Add a Note" title="Add a Note" styleClass="tinybutton btn btn-green" value="Add"/>
                        </td>

                        <c:if test="${allowsNoteFYI}" >
                            <td>&nbsp;</td>
                        </c:if>
                    </tr>
			    </c:if>

                <c:if test="${not empty notesBo}">
                    <tr class="header">
                        <kul:htmlAttributeHeaderCell literalLabel="&nbsp;" scope="col" align="left"/>
                        <kul:htmlAttributeHeaderCell attributeEntry="${notesAttributes.notePostedTimestamp}" hideRequiredAsterisk="true" scope="col" align="left"/>
                        <kul:htmlAttributeHeaderCell attributeEntry="${notesAttributes.authorUniversalIdentifier}" hideRequiredAsterisk="true" scope="col" align="left"/>

                        <c:if test="${displayTopicFieldInNotes eq true}">
                            <kul:htmlAttributeHeaderCell attributeEntry="${notesAttributes.noteTopicText}" labelFor="newNote.noteTopicText" hideRequiredAsterisk="${true}" scope="col" align="left"/>
                        </c:if>

                        <kul:htmlAttributeHeaderCell attributeEntry="${notesAttributes.noteText}" labelFor="newNote.noteText" hideRequiredAsterisk="${true}" scope="col" align="left"/>

                        <c:if test="${allowsNoteAttachments eq true}">
                            <kul:htmlAttributeHeaderCell attributeEntry="${notesAttributes.attachment}" labelFor="attachmentFile" scope="col" align="left"/>
                        </c:if>

                        <c:if test="${(not empty attachmentTypesValuesFinder) and (allowsNoteAttachments eq true)}">
                            <kul:htmlAttributeHeaderCell literalLabel="Attachment Type" scope="col" align="left"/>
                            <kul:htmlAttributeHeaderCell literalLabel="Send to Vendor?" scope="col" align="left"/>
                        </c:if>

                        <c:if test="${allowsNoteFYI}">
                            <kul:htmlAttributeHeaderCell literalLabel="Notification Recipient" scope="col"/>
                        </c:if>

                        <kul:htmlAttributeHeaderCell literalLabel="Actions" scope="col"/>
                    </tr>
                </c:if>

                <html:hidden property="newNote.noteTypeCode" value="${noteType.code}"/>

                <c:forEach var="note" items="${notesBo}" varStatus="status">
    	            <c:set var="authorUniversalIdentifier" value = "${note.authorUniversalIdentifier}" />
	                <c:if test="${kfunc:canViewNoteAttachment(KualiForm.document, null)}" >
                        <tr class="${status.index % 2 == 0 ? "highlight" : ""}">
                            <td>${status.index + 1}</td>
                            <td class="datacell">
                                <bean:write name="KualiForm" property="document.notes[${status.index}].notePostedTimestamp"/>
                                &nbsp;
                            </td>

                            <td class="datacell">
                                <bean:write name="KualiForm" property="document.notes[${status.index}].authorUniversal.name"/>
                            </td>

                            <c:if test="${displayTopicFieldInNotes eq true}">
                                <td class="datacell">
                                    <bean:write name="KualiForm" property="document.notes[${status.index}].noteTopicText"/>
                                </td>
                            </c:if>

                            <td class="datacell">
                                <bean:write name="KualiForm" property="document.notes[${status.index}].noteText"/>
                            </td>

                            <c:choose>
                                <c:when test="${(!empty note.attachment) and (note.attachment.complete)}">
                                    <td class="datacell">
                                        <c:if test="${allowsNoteAttachments eq true}">
                                            <c:if test="${(!empty note.attachment)}">
										        <c:set var="attachmentTypeCode" value ="${note.attachment.attachmentTypeCode}" />
										        <c:set var="mimeTypeCode" value="${note.attachment.attachmentMimeTypeCode}"/> 
									        </c:if>
                                            <span>
                                                <c:if test="${kfunc:canViewNoteAttachment(KualiForm.document, attachmentTypeCode)}" >
                                                	<html:image property="methodToCall.downloadBOAttachment.attachment[${status.index}]" 
                                                		src="${ConfigProperties.kr.externalizable.images.url}${kfunc:getAttachmentImageForUrl(mimeTypeCode)}"  
                                                		title="download attachment" alt="download attachment" style="padding:5px;margin-bottom:-10px;"
                                                		onclick="excludeSubmitRestriction=true"/>
                                                </c:if>
                                                <bean:write name="KualiForm" property="document.notes[${status.index}].attachment.attachmentFileName"/>
                                                &nbsp;
                                                <kul:fileSize byteSize="${note.attachment.attachmentFileSize}">
                                                    (<c:out value="${fileSize} ${fileSizeUnits}" />,  <bean:write name="KualiForm" property="document.notes[${status.index}].attachment.attachmentMimeTypeCode"/>)
                                                </kul:fileSize>
                                            </span>
                                        </c:if>
                                    </td>

                                    <c:if test="${(not empty attachmentTypesValuesFinder) and (allowsNoteAttachments eq true)}">
                                        <td class="datacell">
                                            &nbsp;
									        <c:set var="mapKey" value = "getOptionsMap${Constants.ACTION_FORM_UTIL_MAP_METHOD_PARM_DELIMITER}${finderClass}" />
									        <c:set var="attachmentTypeFinderMap" value="${KualiForm.actionFormUtilMap[mapKey]}"  />
                                            <c:forEach items="${attachmentTypeFinderMap}" var="type">
                                                <c:if test="${type.key eq note.attachment.attachmentTypeCode}">${type.value}</c:if>
                                            </c:forEach>
                                        </td>
                                        <td class="datacell">
                                            <c:choose>
                                                <c:when test="${KualiForm.docFinal}">
                                                    <c:if test="${'sendToVendor' eq note.noteTopicText}">Yes</c:if>
                                                    <c:if test="${'dontSendToVendor' eq note.noteTopicText}">No</c:if>
                                                    <c:if test="${empty note.noteTopicText}">No</c:if>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:set var="finderClass1" value="${fn:replace(sendToVendorValuesFinder,'.','|')}"/>
                                                    <html:select property="document.notes[${status.index}].noteTopicText">
                                                        <html:optionsCollection
                                                                property="actionFormUtilMap.getOptionsMap${Constants.ACTION_FORM_UTIL_MAP_METHOD_PARM_DELIMITER}${finderClass1}"
                                                                label="value" value="key"/>
                                                    </html:select>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <td class="datacell">&nbsp;</td>
                                    <c:if test="${(not empty attachmentTypesValuesFinder) and (allowsNoteAttachments eq true)}">
                                        <td class="datacell">&nbsp;</td>
                                        <td class="datacell">
                                            <c:if test="${'sendToVendor' eq note.noteTopicText}">Yes</c:if>
                                            <c:if test="${'dontSendToVendor' eq note.noteTopicText}">No</c:if>
                                            <c:if test="${empty note.noteTopicText}">No</c:if>
                                        </td>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>

                            <c:if test="${allowsNoteFYI}" >
                                <td class="infoline">
                                    <c:if test="${!empty KualiForm.documentActions[Constants.KUALI_ACTION_CAN_SEND_NOTE_FYI]}">
                                        <kul:user userIdFieldName="document.notes[${status.index}].adHocRouteRecipient.id"
                                                  userId="${note.adHocRouteRecipient.id}"
                                                  universalIdFieldName=""
                                                  universalId=""
                                                  userNameFieldName="document.notes[${status.index}].adHocRouteRecipient.name"
                                                  userName="${note.adHocRouteRecipient.name}"
                                                  readOnly="false"
                                                  fieldConversions="principalName:document.notes[${status.index}].adHocRouteRecipient.id,name:document.notes[${status.index}].adHocRouteRecipient.name"
                                                  lookupParameters="document.notes[${status.index}].adHocRouteRecipient.id:principalName" />
                                    </c:if>
                                    <c:if test="${empty KualiForm.documentActions[Constants.KUALI_ACTION_CAN_SEND_NOTE_FYI]}">
                                        &nbsp;
                                    </c:if>
                                </td>
                           </c:if>
                           
                            <td class="datacell">
                                <c:if test="${kfunc:canDeleteNoteAttachment(KualiForm.document, attachmentTypeCode, authorUniversalIdentifier)}">
                                    <html:submit property="methodToCall.deleteBONote.line${status.index}"
                                                 title="Delete a Note" alt="Delete a Note"
                                                 styleClass="tinybutton btn btn-red" value="Delete"/>
                                </c:if> &nbsp;
                                <c:if test="${allowsNoteFYI && !empty KualiForm.documentActions[Constants.KUALI_ACTION_CAN_SEND_NOTE_FYI]}" >
                                    <html:submit property="methodToCall.sendNoteWorkflowNotification.line${status.index}"
                                                 title="Send FYI" alt="Send FYI" styleClass="tinybutton btn btn-default"
                                                 value="Send"/>
                                </c:if>
                            </td>
                        </tr>
	                </c:if>
                </c:forEach>
                <c:if test="${not KualiForm.docFinal}" >
                    <tr>
                        <th rowspan="1" colspan="2" scope="col" align="left">Reason to change 'Send to Vendor' requirement</th>
                        <td colspan="7" class="datacell">
                            <kul:htmlControlAttribute attributeEntry="${notesAttributes.noteText}" property="reasonToChange" />
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</kul:tab>
