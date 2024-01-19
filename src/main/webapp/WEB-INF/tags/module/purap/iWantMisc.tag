<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>

<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}"/>
<c:set var="tabindexOverrideBase" value="30"/>

<kul:tab tabTitle="Additional Info" defaultOpen="true" tabErrorKey="${CUKFSConstants.I_WANT_DOC_MISC_ERRORS}">
    <div class="tab-container">
        <h3>Services</h3>
        <table cellpadding="0" cellspacing="0" class="datatable" summary="Instructions">
            <tr>
                <th class="right">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.servicePerformedOnCampus}"/>
                </th>
                <td align="left" valign="middle" class="neutral" width="70%">
                    <kul:htmlControlAttribute
                            attributeEntry="${documentAttributes.servicePerformedOnCampus}"
                            property="document.servicePerformedOnCampus"
                            readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
            </tr>
        </table>
        <h3>Miscellaneous</h3>
        <table cellpadding="0" cellspacing="0" class="datatable">
            <tr>
                <th class="right">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.sscProcessorNetId}"/>
                </th>
                <td align="left" valign="middle" width="70%" class="neutral">
                    <kul:user userIdFieldName="document.sscProcessorNetId"
                              userId="${KualiForm.document.sscProcessorNetId}"
                              universalIdFieldName=""
                              universalId=""
                              userNameFieldName="document.sscProcessorName"
                              userName="${KualiForm.document.sscProcessorName}"
                              readOnly="${not fullEntryMode}"
                              fieldConversions="principalName:document.sscProcessorNetId,name:document.sscProcessorName"
                              hasErrors="${hasErrors}"
                              onblur="loadSscProcessorInfo('document.sscProcessorNetId', 'document.sscProcessorName')"/>
                </td>
            </tr>
        </table>
        <table cellpadding="0" cellspacing="0" class="datatable" summary="Instructions">
            <tr>
                <th class="right">
                    <kul:htmlAttributeLabel attributeEntry="${documentAttributes.commentsAndSpecialInstructions}"/>
                </th>
                <td align="left" valign="middle" class="neutral" width="70%">
                    <kul:htmlControlAttribute
                            attributeEntry="${documentAttributes.commentsAndSpecialInstructions}"
                            property="document.commentsAndSpecialInstructions"
                            readOnly="${not (fullEntryMode)}"
                            tabindexOverride="${tabindexOverrideBase + 5}"/>
                    <div id="example" class="fineprint">
                        (i.e. date order is needed)
                    </div>
                </td>
            </tr>
        </table>
    </div>
</kul:tab>