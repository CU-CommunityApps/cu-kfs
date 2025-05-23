function populateInvoiceReceivedDateIfNecessary(invoiceDateField) {
    const invoiceDate = invoiceDateField && invoiceDateField.value;
    if (invoiceDate && invoiceDate.trim().length > 0) {
        const invoiceReceivedDateField = document.getElementById("document.invoiceReceivedDate");
        const invoiceReceivedDate = invoiceReceivedDateField && invoiceReceivedDateField.value;
        if (invoiceReceivedDateField && (!invoiceReceivedDate || invoiceReceivedDate.trim().length == 0)) {
            invoiceReceivedDateField.value = invoiceDate;
        }
    }
}
