<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>

<c:set var="tabindexOverrideBase" value="30"/>
<c:set var="displayContractTab" value="${(not empty KualiForm.editingMode['displayContractTab'])}" scope="request"/>
<c:set var="contractIndicatorReadOnly" value="${(empty KualiForm.editingMode['editContractIndicator'])}" scope="request"/>
<c:set var="docEnroute" value="${KualiForm.docEnroute}"/>
<c:set var="canEditProcessingAssistantNetId" value="${(not empty KualiForm.editingMode['editPurcharingAssistantNetId'])}" scope="request"/>
<c:set var="purcharingAssistantNetIdReadOnly" value="${!fullEntryMode || !canEditProcessorNetId || !docEnroute}" scope="request"/>

<c:if test="${displayContractTab}">
    <kul:tab tabTitle="Contract" defaultOpen="true">
        <div class="tab-container">
            <table cellpadding="0" cellspacing="0" class="datatable" summary="Instructions">
                <tr>
                    <th class="right">
                        <kul:htmlAttributeHeaderCell attributeEntry="${documentAttributes.contractIndicator}"
                        	horizontal="true"
		                    rowspan="1"
		                    addClass="right top"
		                    width="25%"/>
                    </th>
                    <td rowspan="1" class="top" width="25%">
                        <kul:htmlControlAttribute
                            attributeEntry="${documentAttributes.contractIndicator}"
                            property="document.contractIndicator"
                            readOnly="${contractIndicatorReadOnly}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                    </td>
		            <kul:htmlAttributeHeaderCell
		                    labelFor="document.purcharingAssistantNetId"
		                    attributeEntry="${iWantDocAttributes.purcharingAssistantNetId}"
		                    horizontal="true"
		                    rowspan="1"
		                    addClass="right top"
		                    width="25%"/>
		            <td rowspan="1" class="top" width="25%">
		                <kul:user userIdFieldName="document.purcharingAssistantNetId"
		                          userId="${KualiForm.document.purcharingAssistantNetId}"
		                          universalIdFieldName=""
		                          universalId=""
		                          userNameFieldName="document.purcharingAssistantName"
		                          userName="${KualiForm.document.purcharingAssistantName}"
		                          readOnly="${purcharingAssistantNetIdReadOnly}"
		                          fieldConversions="principalName:document.purcharingAssistantNetId,name:document.purcharingAssistantName"
		                          hasErrors="${hasErrors}"
		                          onblur="loadProcessorInfo('document.purcharingAssistantNetId', 'document.purcharingAssistantName')"/>
		            </td>
                </tr>
            </table>
        </div>
    </kul:tab>
</c:if>