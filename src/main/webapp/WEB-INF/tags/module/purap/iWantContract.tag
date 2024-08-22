<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>

<c:set var="tabindexOverrideBase" value="30"/>
<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}"/>
<c:set var="displayContractTab" value="${(not empty KualiForm.editingMode['displayContractTab'])}" scope="request"/>
<c:set var="contractIndicatorReadOnly" value="${(empty KualiForm.editingMode['editContractIndicator'])}" scope="request"/>
<c:set var="docEnroute" value="${KualiForm.docEnroute}"/>
<c:set var="canEditProcurementAssistantNetId" value="${(not empty KualiForm.editingMode['editProcurementAssistantNetId'])}" scope="request"/>
<c:set var="procurementAssistantNetIdReadOnly" value="${!fullEntryMode || !canEditProcurementAssistantNetId || !docEnroute}" scope="request"/>

<c:if test="${displayContractTab}">
    <kul:tab tabTitle="Jaggaer Contract" defaultOpen="true">
        <div class="tab-container">
            <table class="standard side-margins"
           title="Jaggaer Contract"
           summary="Jaggaer Contract">
        <tr>
           <kul:htmlAttributeHeaderCell
                    labelFor="document.contractIndicator"
                    attributeEntry="${documentAttributes.contractIndicator}"
                    horizontal="true"
                    rowspan="1"
                    addClass="right top"
                    width="25%"/>
            <td rowspan="1" class="top" width="25%">
                        <kul:htmlControlAttribute
                            attributeEntry="${documentAttributes.contractIndicator}"
                            property="document.contractIndicator"
                            readOnly="${contractIndicatorReadOnly}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
            </td>
            <kul:htmlAttributeHeaderCell
                    labelFor="document.procurementAssistantNetId"
                    attributeEntry="${documentAttributes.procurementAssistantNetId}"
                    horizontal="true"
                    rowspan="1"
                    addClass="right top"
                    width="25%"/>
            <td rowspan="1" class="top" width="25%">
                <kul:user userIdFieldName="document.procurementAssistantNetId"
                          userId="${KualiForm.document.procurementAssistantNetId}"
                          universalIdFieldName=""
                          universalId=""
                          userNameFieldName="document.procurementAssistantName"
                          userName="${KualiForm.document.procurementAssistantName}"
                          readOnly="${procurementAssistantNetIdReadOnly}"
                          fieldConversions="principalName:document.procurementAssistantNetId,name:document.procurementAssistantName"
                          hasErrors="${hasErrors}"
                          onblur="loadProcessorInfo('document.procurementAssistantNetId', 'document.procurementAssistantName')"/>
            </td>
        </tr>
    </table>
        </div>
    </kul:tab>
</c:if>