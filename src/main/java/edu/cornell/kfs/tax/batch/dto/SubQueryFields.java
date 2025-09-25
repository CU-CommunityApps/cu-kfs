package edu.cornell.kfs.tax.batch.dto;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.businessobject.UniversityDate;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

public final class SubQueryFields {

    public enum DvSubQueryField implements TaxDtoFieldEnum {
        // Fields from FP_DV_DOC_T (DisbursementVoucherDocument)
        documentNumber(DisbursementVoucherDocument.class),
        disbVchrCheckStubText(DisbursementVoucherDocument.class),
        disbVchrPaymentMethodCode(DisbursementVoucherDocument.class),
        extractDate(DisbursementVoucherDocument.class),
        paidDate(DisbursementVoucherDocument.class),
        // Fields from SH_UNIV_DATE_T (UniversityDate)
        universityDate(UniversityDate.class);

        private final Class<? extends BusinessObject> boClass;

        private DvSubQueryField(final Class<? extends BusinessObject> boClass) {
            this.boClass = boClass;
        }

        @Override
        public Class<? extends BusinessObject> getMappedBusinessObjectClass() {
            return boClass;
        }
    }

    public enum RouteHeaderSubQueryField implements TaxDtoFieldEnum {
        documentId,
        documentTypeId,
        finalizedDate;

        @Override
        public Class<? extends BusinessObject> getMappedBusinessObjectClass() {
            return DocumentRouteHeaderValue.class;
        }
    }

    public enum DocumentTypeSubQueryField implements TaxDtoFieldEnum {
        documentTypeId,
        name;

        @Override
        public Class<? extends BusinessObject> getMappedBusinessObjectClass() {
            return DocumentType.class;
        }
    }

}
