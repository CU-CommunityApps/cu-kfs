package edu.cornell.kfs.tax.batch.dto;

import org.kuali.kfs.sys.businessobject.DocumentHeader;

import edu.cornell.kfs.tax.batch.annotation.TaxBusinessObjectMapping;
import edu.cornell.kfs.tax.batch.annotation.TaxDto;
import edu.cornell.kfs.tax.batch.annotation.TaxDtoField;

@TaxDto(mappedBusinessObjects = {
        @TaxBusinessObjectMapping(businessObjectClass = DocumentHeader.class)
})
public class DocumentHeaderLite {

    @TaxDtoField
    private String documentNumber;

    @TaxDtoField
    private String objectId;

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(final String objectId) {
        this.objectId = objectId;
    }

}
