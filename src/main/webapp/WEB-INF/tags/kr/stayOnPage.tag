<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2020 Kuali, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp"%>

<%@ attribute name="active" required="true" description="The selector used to find the body element." %>

<c:if test="${active && (empty KualiForm.documentActions || !KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT])}">
    <c:set var="active" value="false"/>
</c:if>

<script type="text/javascript">
    var active = '${active}' === 'true';

    function isWebappLink(url) {
        const webappPath = '/webapp'
        // "new" documents handle their own loading modal
        var isNewDocument = window.documentData;
        return isNewDocument || (url.includes(webappPath) && window.location.pathname.includes(webappPath))
    }

    function goToPage(url) {
        window.location = url;
        $('.remodal-wrapper').hide()
        // Webapp links do not cause a page reload the same way that legacy links do, so do not show the loading modal
        if (!isWebappLink(url)) {
            showLoadingModal()
        }
    }

    function stayOnPage(event) {
        var anchor = $(event.target).closest('a');
        const newTab = (event.metaKey || event.ctrlKey) || anchor.attr('target') === '_blank'
        if (newTab) {
            return
        }
        var href = anchor.attr('href');
        var isLegacy = !isWebappLink(href);

        if (isLegacy) {
            if (active) {
                event.preventDefault();

                var myModal = $('#remodal');
                var modalBody = myModal.find('.remodal-content');
                var html = '<div class="confirm-dialog">';
                html += '<h3>Discard Changes?</h3>'
                html += '<div class="message">If you choose to continue, any unsaved changes to this document will be lost.</div>'
                html += '<div class="button-row">'
                html += '<button class="btn btn-default" data-remodal-action="close">Cancel</button>'
                html += '<button class="btn btn-primary" onclick="goToPage(\'' + href + '\')">Continue</button>'
                html += '</div>'
                html += '</div>';
                modalBody.html(html);
                myModal.remodal();
                $('.remodal-wrapper').show();
                setTimeout(function () {
                    $('.remodal-wrapper').find('button').last().focus();
                });
            } else {
                showLoadingModal()
            }
        }
    }

    if (active) {
        $(document).ready(function () {
            $(document).on('closed', '.remodal', function () {
                $('#remodal .remodal-content').html('');
                $('.remodal-wrapper').hide();
            });
        });
    }
</script>
<%-- CU Customization: Import script for handling certain POST requests on custom modal inquiries. --%>
<script src="${pageContext.request.contextPath}/scripts/modal-inquiry-setup.js"></script>
