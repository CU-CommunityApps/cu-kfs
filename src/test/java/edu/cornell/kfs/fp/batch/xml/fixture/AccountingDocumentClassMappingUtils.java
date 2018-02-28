package edu.cornell.kfs.fp.batch.xml.fixture;

import org.kuali.kfs.fp.document.InternalBillingDocument;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;

import edu.cornell.kfs.fp.document.CuDistributionOfIncomeAndExpenseDocument;
import edu.cornell.kfs.sys.businessobject.TestSourceAccountingLine;
import edu.cornell.kfs.sys.businessobject.TestTargetAccountingLine;

@SuppressWarnings("deprecation")
public class AccountingDocumentClassMappingUtils {

    public static Class<? extends AccountingDocument> getDocumentClassByDocumentType(String documentTypeName) {
        switch (documentTypeName) {
            case KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE :
                return CuDistributionOfIncomeAndExpenseDocument.class;
            case KFSConstants.FinancialDocumentTypeCodes.INTERNAL_BILLING :
                return InternalBillingDocument.class;
            default :
                throw new IllegalArgumentException("Could not find document class for document type: " + documentTypeName);
        }
    }

    public static Class<? extends SourceAccountingLine> getSourceAccountingLineClassByDocumentClass(Class<? extends Document> documentClass) {
        if (CuDistributionOfIncomeAndExpenseDocument.class.isAssignableFrom(documentClass)) {
            return getSourceAccountingLineClassByDocumentType(KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE);
        } else if (InternalBillingDocument.class.isAssignableFrom(documentClass)) {
            return getSourceAccountingLineClassByDocumentType(KFSConstants.FinancialDocumentTypeCodes.INTERNAL_BILLING);
        } else {
            throw new IllegalArgumentException("Could not find source acct line class for document class: " + documentClass.getName());
        }
    }

    public static Class<? extends SourceAccountingLine> getSourceAccountingLineClassByDocumentType(String documentTypeName) {
        switch (documentTypeName) {
            case KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE :
            case KFSConstants.FinancialDocumentTypeCodes.INTERNAL_BILLING :
                return TestSourceAccountingLine.class;
            default :
                throw new IllegalArgumentException("Could not find source acct line class for document type: " + documentTypeName);
        }
    }

    public static Class<? extends TargetAccountingLine> getTargetAccountingLineClassByDocumentClass(Class<? extends Document> documentClass) {
        if (CuDistributionOfIncomeAndExpenseDocument.class.isAssignableFrom(documentClass)) {
            return getTargetAccountingLineClassByDocumentType(KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE);
        } else if (InternalBillingDocument.class.isAssignableFrom(documentClass)) {
            return getTargetAccountingLineClassByDocumentType(KFSConstants.FinancialDocumentTypeCodes.INTERNAL_BILLING);
        } else {
            throw new IllegalArgumentException("Could not find target acct line class for document class: " + documentClass.getName());
        }
    }

    public static Class<? extends TargetAccountingLine> getTargetAccountingLineClassByDocumentType(String documentTypeName) {
        switch (documentTypeName) {
            case KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE :
            case KFSConstants.FinancialDocumentTypeCodes.INTERNAL_BILLING :
                return TestTargetAccountingLine.class;
            default :
                throw new IllegalArgumentException("Could not find target acct line class for document type: " + documentTypeName);
        }
    }

}
