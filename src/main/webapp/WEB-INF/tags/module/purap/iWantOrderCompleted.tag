<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>

<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}"/>

<div class="tab-container">
    <h3>Order Completed Information</h3>
    <table class="datatable" summary="Complete Information">
        <tr>
            <td class="right">
                <kul:htmlControlAttribute
                        attributeEntry="${documentAttributes.completeOption}"
                        property="document.completeOption"
                        readOnly="${not fullEntryMode}"/>
            </td>
        </tr>
    </table>
</div>