package edu.cornell.kfs.tax.batch.dto;

import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.businessobject.DocumentHeader;

import edu.cornell.kfs.tax.batch.annotation.HasNestedEnumWithDtoFieldListing;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

@HasNestedEnumWithDtoFieldListing
public class DocumentHeaderLite {

    private String documentNumber;
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



    public enum DocumentHeaderField implements TaxDtoFieldEnum {
        documentNumber,
        objectId;

        @Override
        public Class<? extends BusinessObject> getMappedBusinessObjectClass() {
            return DocumentHeader.class;
        }
    }

}
