function populateInvoiceReceivedDateIfNecessary(invoiceDateField) {
    const datePattern = /^\d{1,2}\D\d{1,2}\D\d{2}(\d{2})?$/;
    if (invoiceDateField && invoiceDateField.value && datePattern.test(invoiceDateField.value)) {
        const invoiceReceivedDateField = document.getElementById("document.invoiceReceivedDate");
        const invoiceReceivedDate = invoiceReceivedDateField && invoiceReceivedDateField.value;
        if (invoiceReceivedDateField && (!invoiceReceivedDate || invoiceReceivedDate.trim().length == 0)) {
            invoiceReceivedDateField.value = invoiceDateField.value;
        }
    }
}
