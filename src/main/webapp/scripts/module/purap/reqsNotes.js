/**
 * Forcibly sets the "Copy Note to PO?" checkbox on the REQS note,
 * and also hides the checkbox and shows the text "Yes". Intended to be called
 * after an attachment file has been specified on the note addLine.
 */
function forceNewNoteCopyToPO() {
    var copyToPOElem = document.getElementById("newNote.extension.copyNoteIndicator");
    var forceCopyYesLabel = document.getElementById("newNoteForceCopyYesLabel");
    if (copyToPOElem) {
        copyToPOElem.checked = "checked";
        copyToPOElem.style.display = "none";
        if (forceCopyYesLabel) {
            forceCopyYesLabel.style.display = "inline";
        }
    }
}