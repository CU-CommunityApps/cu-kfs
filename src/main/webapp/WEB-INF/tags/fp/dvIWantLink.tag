<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>
<c:if test="${KualiForm.canViewIWantDoc}">
	${kfunc:registerEditableProperty(KualiForm, "methodToCall")}
   <div align="center">
        <a href='<c:out value="${KualiForm.iwantDocUrl}"/>' target="blank">
            <font color="red"><bean:message key="label.document.disbursementVoucher.openIWantDoc"/> : ${KualiForm.iwantDocID}</font>
        </a>
   </div>
   <br>
</c:if>
