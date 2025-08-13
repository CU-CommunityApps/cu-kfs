package edu.cornell.kfs.tax.batch.dto;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.UniversityDate;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

public final class SubQueryFields {

    public enum DvSubQueryField implements TaxDtoFieldEnum {
        // Fields from FP_DV_DOC_T (DisbursementVoucherDocument)
        dvDocumentNumber(DisbursementVoucherDocument.class, KFSPropertyConstants.DOCUMENT_NUMBER),
        disbVchrCheckStubText(DisbursementVoucherDocument.class),
        documentDisbVchrPaymentMethodCode(DisbursementVoucherDocument.class,
                KFSPropertyConstants.DISB_VCHR_PAYMENT_METHOD_CODE),
        extractDate(DisbursementVoucherDocument.class),
        paidDate(DisbursementVoucherDocument.class),
        // Fields from SH_UNIV_DATE_T (UniversityDate)
        universityDate(UniversityDate.class);

        private final Class<? extends BusinessObject> boClass;
        private final String boFieldName;

        private DvSubQueryField(final Class<? extends BusinessObject> boClass) {
            this(boClass, null);
        }

        private DvSubQueryField(final Class<? extends BusinessObject> boClass, final String boFieldName) {
            this.boClass = boClass;
            this.boFieldName = StringUtils.defaultIfBlank(boFieldName, name());
        }

        @Override
        public Class<? extends BusinessObject> getMappedBusinessObjectClass() {
            return boClass;
        }

        @Override
        public String getBusinessObjectFieldName() {
            return boFieldName;
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
