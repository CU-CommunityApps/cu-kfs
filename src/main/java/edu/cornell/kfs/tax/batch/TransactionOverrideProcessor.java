package edu.cornell.kfs.tax.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.FlatFileDataHandler;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.exception.FileStorageException;
import org.kuali.rice.core.api.criteria.CriteriaLookupService;
import org.kuali.rice.core.api.criteria.PredicateFactory;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.businessobject.TransactionOverride;
import edu.cornell.kfs.tax.businessobject.TransactionOverrideHeader;

/**
 * Handler for performing bulk additions/updates of TransactionOverride BOs.
 */
public class TransactionOverrideProcessor implements FlatFileDataHandler {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TransactionOverrideProcessor.class);

    private static final String TAX_TYPE_FIELD = "taxType";
    private static final String NULL_STRING = "null";
    private static final String NONE_STRING = "None";

    @Override
    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifier) {
        return null;
    }

    @Override
    public boolean validate(Object parsedFileContents) {
        if (!(parsedFileContents instanceof List)) {
            return false;
        }
        
        TransactionOverrideHeader header = null;
        List<String> invalidLines = new ArrayList<String>();
        
        // Validate and remove rows as needed.
        for (Iterator<?> contentsIter = ((List<?>) parsedFileContents).iterator(); contentsIter.hasNext();) {
            Object parsedItem = contentsIter.next();
            
            if (parsedItem instanceof TransactionOverride) {
                // If a TransactionOverride instance, validate it.
                TransactionOverride transOverride = (TransactionOverride) parsedItem;
                /*
                 * Verifies the following for each row:
                 * 
                 * - University date and document line number are non-null.
                 * - Document number is non-blank.
                 * - The 1099 and 1042S box numbers are not both blank, and are not both non-blank either.
                 * - The 1099 box number, if non-blank, is not longer than the allowed maximum length (unless it equals the string "null").
                 * 
                 * NOTE: At this point, 1099 box number is in tax type property, and 1042S box number is in box number property.
                 */
                if (transOverride.getUniversityDate() == null || StringUtils.isBlank(transOverride.getDocumentNumber())
                        || transOverride.getFinancialDocumentLineNumber() == null
                        || (StringUtils.isBlank(transOverride.getTaxType()) ? StringUtils.isBlank(transOverride.getBoxNumber())
                                : (StringUtils.isNotBlank(transOverride.getBoxNumber())
                                        || (transOverride.getTaxType().length() > CUTaxConstants.TAX_1099_MAX_BUCKET_LENGTH
                                                && !NULL_STRING.equalsIgnoreCase(transOverride.getTaxType()))))) {
                    // Line is invalid; remove from list of those to be processed, and add the file line to the list of invalid ones.
                    invalidLines.add(transOverride.getObjectId());
                    contentsIter.remove();
                }
                
            } else if (parsedItem instanceof TransactionOverrideHeader) {
                // If a TransactionOverrideHeader instance, save its reference for later.
                if (header != null) {
                    // Header row already exists; add the duplicate header as an error row and remove it from the processing.
                    invalidLines.add(((TransactionOverrideHeader) parsedItem).getHeader());
                    contentsIter.remove();
                } else {
                    header = (TransactionOverrideHeader) parsedItem;
                }
                
            }
        }
        
        // Setup invalid line references as needed.
        if (header != null) {
            header.setInvalidLines(invalidLines);
        }
        
        // Let validation succeed as long as a header row was specified.
        return header != null;
    }



    @Override
    public void process(String fileName, Object parsedFileContents) {
        CriteriaLookupService criteriaLookupService = SpringContext.getBean(CriteriaLookupService.class);
        BusinessObjectService businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        LOG.info("==== Begin processing of bulk transaction override upload ====");
    
        for (Object parsedItem : (List<?>) parsedFileContents) {
            // Process each validated item.
            if (parsedItem instanceof TransactionOverride) {
                // If a TransactionOverride instance, add it or update an existing matching one.
                TransactionOverride transOverride = (TransactionOverride) parsedItem;
                String oldBoxOverride = NONE_STRING;
                // Modify data accordingly.
                if (StringUtils.isNotBlank(transOverride.getTaxType())) {
                    // 1099 box number embedded in tax type property; update to a 1099 transaction override.
                    transOverride.setBoxNumber(transOverride.getTaxType());
                    transOverride.setTaxType(CUTaxConstants.TAX_TYPE_1099);
                    if (NULL_STRING.equalsIgnoreCase(transOverride.getBoxNumber())) {
                        transOverride.setBoxNumber(CUTaxConstants.TAX_1099_UNKNOWN_BOX_KEY);
                    }
                } else {
                    // 1042S box number stored in box number property; update to a 1042S transaction override.
                    transOverride.setTaxType(CUTaxConstants.TAX_TYPE_1042S);
                    if (NULL_STRING.equalsIgnoreCase(transOverride.getBoxNumber())) {
                        transOverride.setBoxNumber(CUTaxConstants.TAX_1042S_UNKNOWN_BOX_KEY);
                    }
                }
                transOverride.setObjectId(null);
                transOverride.setActive(true);
                // Check if override already exists.
                List<TransactionOverride> existingOverride = criteriaLookupService.lookup(TransactionOverride.class, QueryByCriteria.Builder.fromPredicates(
                        PredicateFactory.equal(KFSPropertyConstants.UNIVERSITY_DATE, transOverride.getUniversityDate()),
                        PredicateFactory.equal(KFSPropertyConstants.DOCUMENT_NUMBER, transOverride.getDocumentNumber()),
                        PredicateFactory.equal(KFSPropertyConstants.FINANCIAL_DOCUMENT_LINE_NUMBER, transOverride.getFinancialDocumentLineNumber()),
                        PredicateFactory.equal(TAX_TYPE_FIELD, transOverride.getTaxType())
                )).getResults();
                
                if (!existingOverride.isEmpty()) {
                    // If override already exists, copy object ID and version number to the updated object.
                    transOverride.setObjectId(existingOverride.get(0).getObjectId());
                    transOverride.setVersionNumber(existingOverride.get(0).getVersionNumber());
                    oldBoxOverride = existingOverride.get(0).getBoxNumber();
                }
                // Save the override.
                transOverride = businessObjectService.save(transOverride);
                LOG.info("Saved override for date " + transOverride.getUniversityDate() + ", doc " + transOverride.getDocumentNumber()
                        + ", line " + transOverride.getFinancialDocumentLineNumber() + ", and tax type " + transOverride.getTaxType()
                        + " --- Old Box Override: " + oldBoxOverride + ", New Box Override: " + transOverride.getBoxNumber());
                
            } else if (parsedItem instanceof TransactionOverrideHeader) {
                // If a TransactionOverrideHeader instance, create a validation error file if invalid rows were found earlier.
                TransactionOverrideHeader header = (TransactionOverrideHeader) parsedItem;
                if (CollectionUtils.isNotEmpty(header.getInvalidLines())) {
                    writeValidationErrorFile(fileName, header);
                }
            }
        }
        
        LOG.info("==== End processing of bulk transaction override upload ====");
    }

    /**
     * Prints the header line and the problem lines to a separate file;
     * should only be called if one or more override lines contained errors.
     * 
     * @param fileName The filename and path to use; typically the name that would have been used if the parser's shouldSave() method had returned true.
     * @param header The object encapsulating the header line and the error lines.
     */
    protected void writeValidationErrorFile(String fileName, TransactionOverrideHeader header) {
        fileName = fileName.substring(0, fileName.lastIndexOf('.')) + "_error" + fileName.substring(fileName.lastIndexOf('.'));
        File errorFile = new File(fileName);
        if (errorFile.exists()) {
            throw new FileStorageException("Transaction override error file already exists: " + fileName);
        }
        
        BufferedWriter writer = null;
        try {
            // Write the header line and the individual error lines to the file.
            writer = new BufferedWriter(new PrintWriter(errorFile));
            writer.write(header.getHeader());
            writer.write('\n');
            for (String invalidLine : header.getInvalidLines()) {
                writer.write(invalidLine);
                writer.write('\n');
            }
            writer.flush();
        } catch (IOException e) {
            throw new FileStorageException("Encountered error while writing transaction error output file", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // Ignore.
                }
            }
        }
        
        LOG.info("Found " + Integer.toString(header.getInvalidLines().size()) + " error lines, these have been written to a new error file.");
        GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, CUTaxConstants.CUTaxKeyConstants.ERROR_BATCH_UPLOAD_INVALID_TRANSACTION_OVERRIDES,
                Integer.toString(header.getInvalidLines().size()));
    }

}
