package edu.cornell.kfs.fp.batch.xml.fixture;

import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;

public class AccountingDocumentClassMappingUtils {

    public static Class<? extends AccountingDocument> getDocumentClassByDocumentType(String documentTypeName) {
        return AccountingDocumentMapping.getMappingByDocumentType(documentTypeName)
                .map((mapping) -> mapping.documentClass)
                .orElseThrow(() -> new IllegalArgumentException("Could not find document class for document type: " + documentTypeName));
    }

    public static String getDocumentTypeByDocumentClass(Class<? extends Document> documentClass) {
        return AccountingDocumentMapping.getMappingByDocumentClass(documentClass)
                .map((mapping) -> mapping.documentTypeName)
                .orElseThrow(() -> new IllegalArgumentException("Could not find document type for document class: " + documentClass.getName()));
    }

    public static Class<? extends SourceAccountingLine> getSourceAccountingLineClassByDocumentClass(Class<? extends Document> documentClass) {
        return AccountingDocumentMapping.getMappingByDocumentClass(documentClass)
                .map((mapping) -> mapping.sourceAccountingLineClass)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find source acct line class for document class: " + documentClass.getName()));
    }

    public static Class<? extends SourceAccountingLine> getSourceAccountingLineClassByDocumentType(String documentTypeName) {
        return AccountingDocumentMapping.getMappingByDocumentType(documentTypeName)
                .map((mapping) -> mapping.sourceAccountingLineClass)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find source acct line class for document type: " + documentTypeName));
    }

    public static Class<? extends TargetAccountingLine> getTargetAccountingLineClassByDocumentClass(Class<? extends Document> documentClass) {
        return AccountingDocumentMapping.getMappingByDocumentClass(documentClass)
                .map((mapping) -> mapping.targetAccountingLineClass)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find target acct line class for document class: " + documentClass.getName()));
    }

    public static Class<? extends TargetAccountingLine> getTargetAccountingLineClassByDocumentType(String documentTypeName) {
        return AccountingDocumentMapping.getMappingByDocumentType(documentTypeName)
                .map((mapping) -> mapping.targetAccountingLineClass)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find target acct line class for document type: " + documentTypeName));
    }

}
