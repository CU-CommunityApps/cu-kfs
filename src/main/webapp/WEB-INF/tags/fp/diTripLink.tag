<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:if test="${KualiForm.canViewTrip}">
	${kfunc:registerEditableProperty(KualiForm, "methodToCall")}
    <div class="center">
        <a href='<c:out value="${KualiForm.tripUrl}"/>' target="blank">
            <font color="red"><bean:message key="label.document.distributionIncomeExpense.openTrip"/> : ${KualiForm.tripID}</font>
        </a>
    </div>
    <br>
</c:if>
