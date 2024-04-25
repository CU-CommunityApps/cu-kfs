<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>

<c:set var="fullEntryMode" value="${KualiForm.documentActions[KRADConstants.KUALI_ACTION_CAN_EDIT]}"/>
<c:set var="step" value="${KualiForm.step}"/>
<c:set var="isAdHocApprover" value="${KualiForm.editingMode['completeOrder']}"/>
<%-- TODO: We have to use a hard-coded 'A' since constants from KewApiConstants can't be accessed when KEW is not in LOCAL mode. Should we fix this? --%>
<c:set var="canAdHocRouteForApprove" value="${KualiForm.adHocActionRequestCodes['A']}"/>
<c:set var="isRegularStep" value="${ step eq 'regular' }"/>

<%-- Variable storing tab title message for use at various points. --%>
<c:set var="iwntTabTitle" value="IWNT"/>

<kul:documentPage showDocumentInfo="true"
                  documentTypeName="IWantDocument"
                  htmlFormAction="purapIWant" renderMultipart="true"
                  showTabButtons="${isRegularStep}">

    <SCRIPT type="text/javascript">
        var kualiForm = document.forms['KualiForm'];
        var kualiElements = kualiForm.elements;
    </SCRIPT>
    
    <c:set var="displayConfirmation" value="${KualiForm.editingMode['displayConfirmation']}" scope="request"/>
    
    <c:if test="${displayConfirmation}">
        <div align="center"><br><bean:message key="message.createReq.confirmationMessage"/></div>
        <br>
    </c:if>
    
    <c:if test="${not displayConfirmation}">

    <%-- Display "Document Overview" tab, if at the regular or customer data steps. --%>
    <c:if test="${isRegularStep or (step eq 'customerDataStep')}">
        <kul:tabTop tabTitle="Document Overview" defaultOpen="true" tabErrorKey="${KRADConstants.DOCUMENT_ERRORS}">
            <purap:iWantDocumentOverview readOnly="${not fullEntryMode}"/>
            <purap:iWantCustomerData documentAttributes="${DataDictionary.IWantDocument.attributes}"/>
        </kul:tabTop>
    </c:if>

    <%-- Display "Items" and "Accounting Info tabs, if at the regular or item+acct data steps. --%>
    <c:if test="${isRegularStep or (step eq 'itemAndAcctDataStep')}">
        <c:choose>
            <c:when test="${isRegularStep}">
                <kul:tab tabTitle="Items" defaultOpen="true" tabErrorKey="${CUKFSConstants.I_WANT_DOC_ITEM_TAB_ERRORS}">
                    <purap:iWantItems itemAttributes="${DataDictionary.IWantItem.attributes}"/>
                </kul:tab>
                <c:set var="iwntTabTitle" value="Accounting Info"/>
            </c:when>
            <c:otherwise>
                <kul:tabTop tabTitle="Items & Account Info" defaultOpen="true"
                            tabErrorKey="${CUKFSConstants.I_WANT_DOC_ITEM_TAB_ERRORS}">
                    <purap:iWantItems itemAttributes="${DataDictionary.IWantItem.attributes}"/>
                </kul:tabTop>
                <c:set var="iwntTabTitle" value="Account"/>
            </c:otherwise>
        </c:choose>

        <kul:tab tabTitle="${iwntTabTitle}" defaultOpen="true"
                 tabErrorKey="${CUKFSConstants.I_WANT_DOC_ACCOUNT_TAB_ERRORS}">
            <purap:iWantAccountInfo documentAttributes="${DataDictionary.IWantDocument.attributes}"
                                    wizard="${!isRegularStep}"/>
        </kul:tab>
    </c:if>

    <%-- Display vendor, misc info, and notes tabs, if at the regular or vendor steps. --%>
    <c:if test="${isRegularStep or (step eq 'vendorStep')}">
        <c:choose>
            <c:when test="${isRegularStep}">
                <kul:tab tabTitle="Vendor" defaultOpen="true"
                         tabErrorKey="${CUKFSConstants.I_WANT_DOC_VENDOR_TAB_ERRORS}">
                    <purap:iWantVendor documentAttributes="${DataDictionary.IWantDocument.attributes}"/>
                </kul:tab>
            </c:when>
            <c:otherwise>
                <kul:tabTop tabTitle="Vendor" defaultOpen="true"
                            tabErrorKey="${CUKFSConstants.I_WANT_DOC_VENDOR_TAB_ERRORS}">
                    <purap:iWantVendor documentAttributes="${DataDictionary.IWantDocument.attributes}" wizard="true"/>
                </kul:tabTop>
            </c:otherwise>
        </c:choose>
        
        <%-- Display contract tab based on user role/permissions and document route node--%>
        <purap:iWantContract documentAttributes="${DataDictionary.IWantDocument.attributes}"/>

        <purap:iWantMisc documentAttributes="${DataDictionary.IWantDocument.attributes}"/>

        <%-- Display related documents, if a req has been created from this doc. --%>
        <c:if test="${isRegularStep && !empty(KualiForm.document.reqsDocId)}">
            <purap:relatedDocuments />
        </c:if>

        <c:if test="${!empty(KualiForm.document.dvDocId)}">
            <purap:iWantRelatedDocuments />
        </c:if>

        <purap:iWantNotes attachmentTypesValuesFinder="${documentEntry.attachmentTypesValuesFinder}"
                          defaultOpen="true"/>
        
        <c:if test="${!isRegularStep}">
            <kul:superUserActions/>
        </c:if>
    </c:if>

    <%-- Display routing and submission tab, if at the regular or routing steps. --%>
    <c:choose>
        <c:when test="${isRegularStep}">
            <c:if test="${canAdHocRouteForApprove != null}">
                <kul:tab tabTitle="College/Unit Routing and Approval" defaultOpen="true">
                    <purap:iWantAdHocRecipients/>
                </kul:tab>
            </c:if>
        </c:when>
        <c:when test="${(step eq 'routingStep')}">
            <kul:tabTop tabTitle="College/Unit Routing and Approval" defaultOpen="true"
                        tabErrorKey="${PurapConstants.VENDOR_ERRORS}">
                <c:if test="${canAdHocRouteForApprove != null}">
                    <purap:iWantAdHocRecipients/>
                </c:if>
            </kul:tabTop>
        </c:when>
        <c:otherwise>
            <%-- Do not display this section if not at the proper step. --%>
        </c:otherwise>
    </c:choose>

    <%-- Display the route log and order completed sections, if at the regular step. --%>
    <c:if test="${isRegularStep}">
        <kul:routeLog/>

        <%-- Only ad hoc approvers should see this section. --%>
        <c:if test="${isAdHocApprover}">
            <kul:tab tabTitle="Order Completed (Required)" defaultOpen="true"
                     tabErrorKey="${CUKFSConstants.I_WANT_DOC_ORDER_COMPLETED_TAB_ERRORS}">
                <purap:iWantOrderCompleted documentAttributes="${DataDictionary.IWantDocument.attributes}" />
            </kul:tab>
        </c:if>
        
        <kul:superUserActions/>
    </c:if>
    </c:if>

    <c:set var="extraButtons" value="${KualiForm.extraButtons}"/>

    <sys:documentControls transactionalDocument="true" extraButtons="${extraButtons}"
                          suppressRoutingControls="${(!isRegularStep and KualiForm.editingMode['wizard']) or displayConfirmation}"/>

</kul:documentPage>
