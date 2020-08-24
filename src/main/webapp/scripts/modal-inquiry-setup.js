$(document).ready(function() {
    const LEGACY_MODAL_SELECTOR = '#remodal';
    const REACT_MODAL_SELECTOR = '.ReactModalPortal';
    
    let asyncSubmitInProgress = false;
    let buttonsAlreadyBound = false;
    let breadcrumbContentCount = 0;
    let modalSelector = '';
    let updateModalAfterPost = null;
    let handleModalInquiryButtonClick = null;
    
    const forceAllowModalAsyncSubmit = function() {
        asyncSubmitInProgress = false;
    }
    
    const pageUsesLegacyModal = function() {
        return $(LEGACY_MODAL_SELECTOR).length;
    };
    
    const pageSupportsReactModal = function() {
        return $(REACT_MODAL_SELECTOR).length;
    };
    
    const getBreadcrumbDomNodeCount = function(modalElement) {
        const breadcrumbsNode = modalElement.find('#breadcrumbs');
        return breadcrumbsNode.contents().length;
    }
    
    const bindModalInquiryButtonHandlers = function() {
        const modalElement = $(modalSelector);
        const inquiryBody = modalElement.find('.inquirymodal.body');
        if (!inquiryBody.length) {
            buttonsAlreadyBound = false;
            return;
        }
        
        const newNodeCount = getBreadcrumbDomNodeCount(modalElement);
        if (buttonsAlreadyBound && breadcrumbContentCount === newNodeCount) {
            return;
        } else {
            breadcrumbContentCount = newNodeCount;
        }
        
        const inquiryButtons = inquiryBody.find('input[type="submit"]');
        if (!inquiryButtons.length) {
            return;
        }
        inquiryButtons.on('click', handleModalInquiryButtonClick);
        forceAllowModalAsyncSubmit();
        buttonsAlreadyBound = true;
    }

    const unbindModalInquiryButtonHandlers = function() {
        const modalElement = $(modalSelector);
        const inquiryBody = modalElement.find('.inquirymodal.body');
        const inquiryButtons = inquiryBody.find('input[type="submit"]');
        if (!inquiryButtons.length) {
            return;
        }
        inquiryButtons.unbind('click', handleModalInquiryButtonClick);
    }
    
    /*
     * React-component-retrieval code is based on that from the following post:
     * https://stackoverflow.com/questions/29321742/react-getting-a-component-from-a-dom-element-for-debugging
     */
    const getReactComponentFromNode = function(domNode) {
        const propertyKey = Object.keys(domNode).find(key => key.startsWith('__reactInternalInstance$'));
        const domFiber = propertyKey && domNode[propertyKey];
        if (!domFiber) {
            return null;
        }
        let parentFiber = domFiber.return;
        while (parentFiber && typeof parentFiber.type === 'string') {
            parentFiber = parentFiber.return;
        }
        return parentFiber && parentFiber.stateNode;
    };
    
    const updateLegacyModalAfterPost = function(htmlContent) {
        const modalElement = $('#remodal');
        const modalBody = modalElement.find('.remodal-content');
        if (!modalElement.length || !modalBody.length) {
            log.error('Could not access legacy modal for update!');
            return;
        }
        modalBody.html(htmlContent);
        modalElement.remodal();
    };
    
    const updateReactModalAfterPost = function(htmlContent) {
        const inquiryNodes = $('.inquirymodal.body').parent();
        const inquiryNode = inquiryNodes.length && inquiryNodes.get(0);
        const reactInquiry = inquiryNode && getReactComponentFromNode(inquiryNode);
        if (!reactInquiry || !reactInquiry.setState) {
            console.error('Could not access modal for update!');
            return;
        }
        reactInquiry.setState({ content: htmlContent });
    };
    
    const copyInquiryBreadcrumbs = function(oldContentElement, newContentElement) {
        const crumbsHtml = oldContentElement.find('#breadcrumbs').html();
        newContentElement.find('#breadcrumbs').html(crumbsHtml);
    }
    
    if (pageUsesLegacyModal()) {
        modalSelector = LEGACY_MODAL_SELECTOR;
        updateModalAfterPost = updateLegacyModalAfterPost;
    } else if (pageSupportsReactModal()) {
        modalSelector = REACT_MODAL_SELECTOR;
        updateModalAfterPost = updateReactModalAfterPost;
    } else {
        console.error('Page does not support modals!');
        return;
    }
    
    handleModalInquiryButtonClick = function(event) {
        event.preventDefault();
        if (asyncSubmitInProgress) {
            alert('Page already being processed by the server.');
            return false;
        }
        asyncSubmitInProgress = true;
        $('body').append('<div id="tempInquiryContentDiv" style="display:none;"></div>');
        
        const tempContentDiv = $('body #tempInquiryContentDiv');
        const modalElement = $(modalSelector);
        const inquiryBody = modalElement.find('.inquirymodal.body');
        const inquiryForm = inquiryBody.find('#kualiForm');
        const formUrl = inquiryForm.attr('action');
        
        const submitter = $(event.target);
        const submitterName = submitter.attr('name');
        const submitterValue = submitter.val() || '';
        const encodedSubmitterName = encodeURIComponent(submitterName);
        const encodedSubmitterValue = encodeURIComponent(submitterValue);
        
        const formDataToPost = {};
        const formDataArray = inquiryForm.serializeArray();
        for (let i = 0; i < formDataArray.length; i++) {
            let formDataItem = formDataArray[i];
            formDataToPost[formDataItem.name] = formDataItem.value;
        }
        formDataToPost[encodedSubmitterName] = encodedSubmitterValue;
        formDataToPost['mode']='modal';
        
        tempContentDiv.load(formUrl, formDataToPost, function(response, status, xhr) {
            let htmlContent = null;
            if ( status == "error" ) {
                // Copied the error content used by the financials modal.tag file.
                let msg = "Sorry but there was an error: ";
                let html = '<div class="fullwidth inquirymodal body"><main class="content">';
                    html += '<div class="modal-header"><div id="breadcrumbs"></div><button type="button" data-remodal-action="close" class="close remodal-close"><span aria-hidden="true">&times;</span></button></div>';
                    html += '<div id="view_div"><div class="inquiry"><div class="main-panel">';
                    html += '<div class="headerarea-small"><h2>Error</h2></div>';
                    html += '<div style="padding: 30px 0;">' + msg + xhr.status + " " + xhr.statusText + '</div>';
                    html += '</div></div></div>';
                    html += '</main></div>';
                htmlContent = html;
            } else {
                copyInquiryBreadcrumbs(inquiryBody, tempContentDiv);
                htmlContent = tempContentDiv.html();
            }
            unbindModalInquiryButtonHandlers();
            $('#tempInquiryContentDiv').remove();
            updateModalAfterPost(htmlContent);
            forceAllowModalAsyncSubmit();
            buttonsAlreadyBound = false;
        });
        
        return false;
    };
    
    setInterval(bindModalInquiryButtonHandlers, 500);
});
