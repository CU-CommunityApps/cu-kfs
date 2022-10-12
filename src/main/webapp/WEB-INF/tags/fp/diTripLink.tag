<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:if test="${KualiForm.canViewTrip}">
	${kfunc:registerEditableProperty(KualiForm, "methodToCall")}
    <div class="center">
        <font color="red"><bean:message key="label.document.distributionIncomeExpense.legacyTrip"/> : ${KualiForm.tripID}</font>
    </div>
    <br>
</c:if>
