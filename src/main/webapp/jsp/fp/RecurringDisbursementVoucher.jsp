<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<fp:disbursementVoucherBase formAction="financialRecurringDisbursementVoucher" documentTypeName="RCDV"
                            dvAttributesType="${DataDictionary.RecurringDisbursementVoucherDocument.attributes}"
                            displayGLPE="${false }" displayRecurringDetail="${true }"/>
