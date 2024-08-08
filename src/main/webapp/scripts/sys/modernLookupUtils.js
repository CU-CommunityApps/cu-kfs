/*
 * This function is similar to hasFormAlreadyBeenSubmitted() in core.js, except that it doesn't display
 * an alert() box when the form is ineligible for submit, and it has been named to accurately reflect
 * the true/false logic (unlike the base code method).
 */
function canSubmitFormProgrammatically() {
    try {
        saveScrollPosition();
    } catch (ex) {
        // Ignore
    }

    if (!document.getElementById('formComplete') || (formHasAlreadyBeenSubmitted && !excludeSubmitRestriction)) {
        return false;
    } else {
        formHasAlreadyBeenSubmitted = true;
        return true;
    }
}

function submitFormIfNotAlreadySubmitted(elementToFocus) {
    if (canSubmitFormProgrammatically()) {
        setFieldToFocusAndSubmit(elementToFocus);
    }
}

function forceSubmitOnChangeOrWhenFieldIsUpdatedByModernLookup(dataFieldName) {
    if (!dataFieldName) {
        throw new Error("dataFieldName must be specified");
    }

    let listenerIntervalId = null;
    let oldFieldValue = null;
    const formField = document.querySelector(`[name="${dataFieldName}"]`);
    const lookupButton = document.querySelector(`[data-lookup-type="single"][data-field-name="${dataFieldName}"]`);

    if (formField && lookupButton) {
        const forceSubmit = () => {
            submitFormIfNotAlreadySubmitted(formField);
        };

        const forceSubmitIfReturningFromLookup = () => {
            const currentFieldValue = formField.value;
            if (currentFieldValue && currentFieldValue !== oldFieldValue) {
                clearInterval(listenerIntervalId);
                submitFormIfNotAlreadySubmitted(formField);
            }
        };

        const startListeningForReturnFromLookup = () => {
            if (listenerIntervalId) {
                clearInterval(listenerIntervalId);
            }
            oldFieldValue = formField.value;
            listenerIntervalId = setInterval(forceSubmitIfReturningFromLookup, 250);
        };

        const stopListeningForReturnFromLookup = () => {
            if (listenerIntervalId) {
                clearInterval(listenerIntervalId);
                listenerIntervalId = null;
            }
        }

        formField.addEventListener('change', forceSubmit);
        formField.addEventListener('keydown', stopListeningForReturnFromLookup);
        formField.addEventListener('mousedown', stopListeningForReturnFromLookup);
        lookupButton.addEventListener('click', startListeningForReturnFromLookup);
    }
}
