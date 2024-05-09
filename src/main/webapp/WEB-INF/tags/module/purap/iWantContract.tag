<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>

<c:set var="tabindexOverrideBase" value="30"/>
<c:set var="displayContractTab" value="${(not empty KualiForm.editingMode['displayContractTab'])}" scope="request"/>
<c:set var="contractIndicatorReadOnly" value="${(empty KualiForm.editingMode['editContractIndicator'])}" scope="request"/>

<c:if test="${displayContractTab}">
    <kul:tab tabTitle="Contract" defaultOpen="true">
        <div class="tab-container">
            <table cellpadding="0" cellspacing="0" class="datatable" summary="Instructions">
                <tr>
                    <th class="right">
                        <kul:htmlAttributeLabel attributeEntry="${documentAttributes.contractIndicator}"/>
                    </th>
                    <td align="left" valign="middle" class="neutral" width="70%">
                        <kul:htmlControlAttribute
                            attributeEntry="${documentAttributes.contractIndicator}"
                            property="document.contractIndicator"
                            readOnly="${contractIndicatorReadOnly}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                    </td>
                </tr>
            </table>
        </div>
    </kul:tab>
</c:if>