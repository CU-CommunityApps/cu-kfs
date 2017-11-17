package edu.cornell.kfs.fp.batch.xml.fixture;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;

import edu.cornell.kfs.fp.document.CuDistributionOfIncomeAndExpenseDocument;

@SuppressWarnings("deprecation")
public class AccountingDocumentClassMappingUtils {

    public static Class<? extends AccountingDocument> getDocumentClassByDocumentType(String documentTypeName) {
        switch (documentTypeName) {
            case KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE :
                return CuDistributionOfIncomeAndExpenseDocument.class;
            default :
                throw new IllegalArgumentException("Could not find document class for document type: " + documentTypeName);
        }
    }

    public static Class<? extends SourceAccountingLine> getSourceAccountingLineClassByDocumentType(String documentTypeName) {
        switch (documentTypeName) {
            case KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE :
                return SourceAccountingLine.class;
            default :
                throw new IllegalArgumentException("Could not find source acct line class for document type: " + documentTypeName);
        }
    }

    public static Class<? extends TargetAccountingLine> getTargetAccountingLineClassByDocumentType(String documentTypeName) {
        switch (documentTypeName) {
            case KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE :
                return TargetAccountingLine.class;
            default :
                throw new IllegalArgumentException("Could not find target acct line class for document type: " + documentTypeName);
        }
    }

}
