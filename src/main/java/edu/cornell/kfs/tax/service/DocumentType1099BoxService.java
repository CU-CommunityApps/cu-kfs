package edu.cornell.kfs.tax.service;

/**
 * Convenience service for handling mappings from document types to 1099 tax boxes,
 * such as those configured by the "1099_DOCUMENT_TYPE_TO_TAX_BOX" parameter.
 */
public interface DocumentType1099BoxService {

    /**
     * Determines whether an explicit 1099 tax box mapping exists for the given document type.
     * 
     * @param documentTypeName The document type's name.
     * @return True if the document type maps to a 1099 tax box, false otherwise.
     */
    boolean isDocumentTypeMappedTo1099Box(String documentTypeName);

    /**
     * Returns the 1099 tax box associated with the given document type, if any.
     * 
     * @param documentTypeName The document type's name.
     * @return The 1099 tax box that should be used for transactions involving the given doc type, or null if no such mapping exists.
     */
    String getDocumentType1099Box(String documentTypeName);
}
