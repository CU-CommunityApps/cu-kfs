package edu.cornell.kfs.fp.batch.xml;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;

public class AccountingXmlDocumentUnmarshalListener extends Unmarshaller.Listener implements ValidationEventHandler {

    private AccountingXmlDocumentEntry currentEntry;

    @Override
    public void beforeUnmarshal(Object target, Object parent) {
        super.beforeUnmarshal(target, parent);
        if (target instanceof AccountingXmlDocumentEntry) {
            if (currentEntry != null) {
                throw new IllegalStateException("Unexpected nesting of XML accounting documents");
            }
            currentEntry = (AccountingXmlDocumentEntry) target;
        }
    }

    @Override
    public void afterUnmarshal(Object target, Object parent) {
        super.afterUnmarshal(target, parent);
        if (target instanceof AccountingXmlDocumentEntry) {
            if (currentEntry == null) {
                throw new IllegalStateException("Unexpected orphaned ending of XML accounting document section");
            }
            currentEntry = null;
        }
    }

    @Override
    public boolean handleEvent(ValidationEvent event) {
        switch (event.getSeverity()) {
            case ValidationEvent.FATAL_ERROR:
                return false;
            
            case ValidationEvent.ERROR:
                if (currentEntry != null) {
                    currentEntry.addValidationError(event);
                }
                return true;
                
            default:
                return true;
        }
    }

}
