<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>

<c:set var="tabindexOverrideBase" value="30"/>
<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}"/>
<c:set var="displayContractTab" value="${(not empty KualiForm.editingMode['displayContractTab'])}" scope="request"/>
<c:set var="contractIndicatorReadOnly" value="${(empty KualiForm.editingMode['editContractIndicator'])}" scope="request"/>
<c:set var="docEnroute" value="${KualiForm.docEnroute}"/>
<c:set var="canEditProcessingAssistantNetId" value="${(not empty KualiForm.editingMode['editPurchasingAssistantNetId'])}" scope="request"/>
<c:set var="purchasingAssistantNetIdReadOnly" value="${!fullEntryMode || !canEditProcessingAssistantNetId || !docEnroute}" scope="request"/>

<c:if test="${displayContractTab}">
    <kul:tab tabTitle="Contract" defaultOpen="true">
        <div class="tab-container">
            <table class="standard side-margins"
           title="Contract"
           summary="Contract">
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
                    labelFor="document.purchasingAssistantNetId"
                    attributeEntry="${documentAttributes.purchasingAssistantNetId}"
                    horizontal="true"
                    rowspan="1"
                    addClass="right top"
                    width="25%"/>
            <td rowspan="1" class="top" width="25%">
                <kul:user userIdFieldName="document.purchasingAssistantNetId"
                          userId="${KualiForm.document.purchasingAssistantNetId}"
                          universalIdFieldName=""
                          universalId=""
                          userNameFieldName="document.purchasingAssistantName"
                          userName="${KualiForm.document.purchasingAssistantName}"
                          readOnly="${purchasingAssistantNetIdReadOnly}"
                          fieldConversions="principalName:document.purchasingAssistantNetId,name:document.purchasingAssistantName"
                          hasErrors="${hasErrors}"
                          onblur="loadProcessorInfo('document.purchasingAssistantNetId', 'document.purchasingAssistantName')"/>
            </td>
        </tr>
    </table>
        </div>
    </kul:tab>
    <jsp:doBody/>
</c:if>