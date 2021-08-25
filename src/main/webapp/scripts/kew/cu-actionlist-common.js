// Modified version of CONTRIB-73 by MSU - Add a Note to Your Action List Item.

function setNoteStatusMessage(messageElementId, messageText, messageColor) {
    const messageElement = document.getElementById(messageElementId);
    if (!messageElement) {
        console.error('ERROR: Could not find element: ' + messageElementId);
        return;
    }
    const actualMessageText = (messageText && messageText.length) ? messageText : '&nbsp;';
    const escapeHtml = actualMessageText !== '&nbsp;';
    messageElement.style.color = messageColor;
    dwr.util.setValue(messageElementId, actualMessageText, { escapeHtml });
}

function saveActionNoteChange(noteTextarea, actionItemId, successMessage) {
    const messageElementId = noteTextarea.name + '.status';
    setNoteStatusMessage(messageElementId, 'saving...', 'black');

    const callback = data => {
        if ( data == successMessage) {
            setNoteStatusMessage(messageElementId, data, 'green');
        } else if (data && data.length > 0) {
            setNoteStatusMessage(messageElementId, data, 'red');
        } else {
            setNoteStatusMessage(messageElementId, '', 'black');
        }
    };
    const errorHandler = errorMessage => {
        setNoteStatusMessage(messageElementId, 'Notes not saved properly', 'red');
    };
    const dwrReply = { callback, errorHandler };
    ActionListService.saveActionItemNoteForActionItemId(noteTextarea.value, actionItemId, dwrReply);
}

function truncateNoteTextIfNecessary(noteTextarea, maxlen, truncateMessage) {
    const fieldValue = noteTextarea.value;
    if (fieldValue && fieldValue.length > maxlen) {
        const messageElementId = noteTextarea.name + '.status';
        noteTextarea.value = noteTextarea.value.substr(0, maxlen);
        setNoteStatusMessage(messageElementId, truncateMessage, 'red');
    }
}
