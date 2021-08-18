// Modified version of CONTRIB-73 by MSU - Add a Note to Your Action List Item.

function onblur_saveActionNoteChange(noteText, actionItemId, successMsg) {
    const divName = noteText.name + ".div";
    const noteDiv = document.getElementById(divName);
    const callback = data => {
        if ( data == successMsg) {
            noteDiv.style.color = "green";
            setRecipientValue(divName, data, true);
        } else if (data && data.length > 0) {
            noteDiv.style.color = "red";
            setRecipientValue(divName, data, true);
        }
    };
    const errorHandler = errorMessage => {
        noteDiv.style.color = "red";
        setRecipientValue(divName, wrapError("Notes not saved properly"), true);
    };
    const dwrReply = { callback, errorHandler };
    ActionListService.saveActionItemNoteForActionItemId(noteText.value, actionItemId, dwrReply);
}

function textLimitWithErrMsg(noteText, maxlen, truncateMsg) {
    const fieldValue = noteText.value;
    if (fieldValue.length > maxlen) {
        const divName = noteText.name + ".div";
        const noteDiv = document.getElementById(divName);
        noteDiv.style.color = "red";
        noteText.value = noteText.value.substr(0, maxlen); 
        setRecipientValue(divName, truncateMsg, true);
    }
}
