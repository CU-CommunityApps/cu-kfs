package edu.cornell.kfs.tax.dataaccess.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxFieldSource;
import edu.cornell.kfs.tax.batch.TaxOutputDefinition;
import edu.cornell.kfs.tax.batch.TaxOutputField;
import edu.cornell.kfs.tax.batch.TaxOutputSection;
import edu.cornell.kfs.tax.dataaccess.impl.TransactionRowProcessor.AlwaysBlankRecordPiece;
import edu.cornell.kfs.tax.dataaccess.impl.TransactionRowProcessor.RecordPiece;
import edu.cornell.kfs.tax.dataaccess.impl.TransactionRowProcessor.StaticStringRecordPiece;

/**
 * Helper class for building TransactionRowProcessor implementations.
 * Use the static createBuilder() method to obtain an instance.
 * 
 * <p>The buildNewProcessor() method is the one that does the actual
 * processor-instance building. It instantiates a new TransactionRowProcessor
 * subclass of a given type and then configures its output "pieces" and buffers
 * based on the given XML-parsed output definition object.</p>
 * 
 * <p>The given output definition is required to meet the following criteria:</p>
 * 
 * <ul>
 *   <li>It must have at least one output section.</li>
 *   <li>A section must have at least one output field.</li>
 *   <li>A section must define a length (maximum or exact).</li>
 *   <li>If a section is meant to place separators between its field outputs, it must define a separator character.</li>
 *   <li>A field must have a non-blank name.</li>
 *   <li>A field must have a non-blank type that equals one of the CUTaxBatchConstants.TaxFieldSource enum constants.</li>
 *   <li>If a field is not using the BLANK or STATIC type, its value must equal an alias or objectClass-and-propName combo for the given type.</li>
 * </ul>
 * 
 * <p>Also, this builder will use the following behavior when generating output "pieces" from the output field objects:</p>
 * 
 * <ul>
 *   <li>A new "piece" instance will be created for each BLANK or STATIC field.</li>
 *   <li>For all the other types, no more than one "piece" instance will be created for a given type-and-value combo.</li>
 *   <li>If more than one such field has the same type-and-value combo, then the same "piece" instance will be used for them.</li>
 *   <li>One auto-generated "piece" will be built for each "minimum" one that is defined by the processor but is not present in the parsed definition.</li>
 *   <li>Such auto-generated "pieces" will not be used for any output buffers; it's the processor's responsibility to reference them at creation time.</li>
 *   <li>In addition, all non-BLANK/non-STATIC "pieces" will be passed to the processor's setComplexPieces() method for final setup.</p>
 * </ul>
 */
final class TransactionRowProcessorBuilder {
	private static final Logger LOG = LogManager.getLogger(TransactionRowProcessorBuilder.class);

    private static final String AUTO_GEN_NAME_PREFIX = "autoGen";

    private TransactionRowProcessorBuilder() {
        // Do nothing.
    }

    static TransactionRowProcessorBuilder createBuilder() {
        return new TransactionRowProcessorBuilder();
    }



    /**
     * Helper method for building a new TransactionRowProcessor instance from the parsed XML input.
     * The processor's implementation class *must* have a default constructor.
     * Note that this method will not configure the PreparedStatement and Writer instances;
     * the calling code is responsible for that part of the processor setup.
     * 
     * @param processorClazz The TransactionRowProcessor implementation class to use.
     * @param outputDefinition The parsed XML definition for this processor's output formatting.
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return A TransactionRowProcessor implementation of the given type with the given formatting.
     */
    <T extends TransactionDetailSummary, V extends TransactionRowProcessor<T>> V buildNewProcessor(
            Class<V> processorClazz, TaxOutputDefinition outputDefinition, T summary) {
        V rowProcessor;
        Map<String,RecordPiece> complexPieces = new HashMap<String,RecordPiece>();
        Map<String,List<String>> complexPiecesNames = new HashMap<String,List<String>>();
        EnumMap<TaxFieldSource,Set<TaxTableField>> minimumPieces = new EnumMap<TaxFieldSource,Set<TaxTableField>>(TaxFieldSource.class);
        boolean foundDuplicate = false;
        int i = 0;
        
        // Check for at least one output section.
        if (outputDefinition.getSections().isEmpty()) {
            throw new IllegalArgumentException("outputDefinition has no sections!");
        }
        
        
        
        // Create a new processor using its default constructor.
        try {
            rowProcessor = processorClazz.newInstance();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        
        // Determine the minimum "piece" objects that need to be created for each type (excluding BLANK and STATIC).
        minimumPieces.put(TaxFieldSource.DETAIL, rowProcessor.getMinimumFields(TaxFieldSource.DETAIL, summary));
        minimumPieces.put(TaxFieldSource.VENDOR, rowProcessor.getMinimumFields(TaxFieldSource.VENDOR, summary));
        minimumPieces.put(TaxFieldSource.VENDOR_US_ADDRESS, rowProcessor.getMinimumFields(TaxFieldSource.VENDOR_US_ADDRESS, summary));
        minimumPieces.put(TaxFieldSource.VENDOR_ANY_ADDRESS, rowProcessor.getMinimumFields(TaxFieldSource.VENDOR_ANY_ADDRESS, summary));
        minimumPieces.put(TaxFieldSource.DERIVED, rowProcessor.getMinimumFields(TaxFieldSource.DERIVED, summary));
        
        
        
        // Create the "piece" objects for each section and add them to the processor.
        for (TaxOutputSection section : outputDefinition.getSections()) {
            if (section.getFields().isEmpty()) {
                throw new RuntimeException("Cannot have empty sections!");
            } else if (section.getLength() == null) {
                throw new RuntimeException("Cannot have section with unspecified max length!");
            } else if (section.isHasSeparators() && section.getSeparator() == null) {
                throw new RuntimeException("Cannot have a null separator for a section with separator-delimited fields!");
            }
            List<RecordPiece> pieces = new ArrayList<RecordPiece>(section.getFields().size());
            
            for (TaxOutputField field : section.getFields()) {
                if (StringUtils.isBlank(field.getName())) {
                    throw new RuntimeException("Cannot have field with blank name");
                } else if (StringUtils.isBlank(field.getType())) {
                    throw new RuntimeException("Cannot have field with blank type");
                } else if (field.getLength() == null) {
                    throw new RuntimeException("Cannot have field with null length");
                }
                
                TaxFieldSource fieldSource = TaxFieldSource.valueOf(field.getType());
                TaxTableField tableField;
                
                
                
                // Create a simple "piece" type or determine what complex "piece" type to create.
                switch (fieldSource) {
                    case BLANK :
                        // Use the AlwaysBlankRecordPiece implementation for blank "pieces".
                        pieces.add(new AlwaysBlankRecordPiece(field.getName(), field.getLength().intValue()));
                        tableField = null;
                        break;
                    
                    case STATIC :
                        // Use the StaticStringRecordPiece implementation for static-value "pieces".
                        pieces.add(new StaticStringRecordPiece(field.getName(), field.getLength().intValue(), field.getValue()));
                        tableField = null;
                        break;
                    
                    case DETAIL :
                        tableField = summary.transactionDetailRow.getField(field.getValue());
                        break;
                    
                    case PDP :
                        throw new IllegalStateException("Cannot create piece for PDP type");
                    
                    case DV :
                        throw new IllegalStateException("Cannot create piece for DV type");
                    
                    case VENDOR :
                        tableField = summary.vendorRow.getField(field.getValue());
                        break;
                    
                    case VENDOR_US_ADDRESS :
                    case VENDOR_ANY_ADDRESS :
                        tableField = summary.vendorAddressRow.getField(field.getValue());
                        break;
                    
                    case DOCUMENT_NOTE :
                        throw new IllegalStateException("Cannot create piece for DOCUMENT_NOTE type");
                    
                    case DERIVED :
                        tableField = summary.derivedValues.getField(field.getValue());
                        break;
                    
                    default :
                        throw new IllegalStateException("Unrecognized piece type for field");
                }
                
                
                
                // Create a more complex "piece" type if necessary.
                if (tableField != null) {
                    String pieceKey = tableField.propertyName;
                    
                    // Create a new "piece" or re-use an existing one for duplicates as needed.
                    RecordPiece currentPiece = complexPieces.get(pieceKey);
                    
                    if (currentPiece == null) {
                        // If not a duplicate, then create a new one.
                        currentPiece = rowProcessor.getPieceForField(fieldSource, tableField, field.getName(), field.getLength().intValue(), summary);
                        complexPiecesNames.put(pieceKey, new ArrayList<String>());
                        minimumPieces.get(fieldSource).remove(tableField);
                        // Add piece to cache.
                        complexPieces.put(pieceKey, currentPiece);
                    } else {
                        // If a duplicate, then use the originally-created piece instead, and warn about mismatched lengths.
                        foundDuplicate = true;
                        if (currentPiece.len != field.getLength().intValue()) {
                            LOG.warn("NOTE: Found multiple tax output pieces with key " + pieceKey + " that do not have the same max length!");
                        }
                    }
                    complexPiecesNames.get(pieceKey).add(field.getName());
                    pieces.add(currentPiece);
                }
            }
            
            
            
            // Setup the section's output buffer.
            rowProcessor.setupOutputBuffer(i, section.getLength(), pieces, section.isHasExactLength(), section.isHasSeparators(),
                    section.isHasSeparators() ? section.getSeparatorChar().charValue() : ' ');
            i++;
        }
        
        
        
        // If the processor has defined some minimum fields but they have not been created yet, then create them.
        i = 1;
        for (Map.Entry<TaxFieldSource,Set<TaxTableField>> minTypeSpecificPieces : minimumPieces.entrySet()) {
            if (minTypeSpecificPieces.getValue() != null) {
                for (TaxTableField minPiece : minTypeSpecificPieces.getValue()) {
                    complexPieces.put(minPiece.propertyName,
                            rowProcessor.getPieceForField(minTypeSpecificPieces.getKey(), minPiece, AUTO_GEN_NAME_PREFIX + i, 1, summary));
                    i++;
                }
            }
        }
        
        // Set the processor's complex "pieces" as needed.
        rowProcessor.setComplexPieces(complexPieces, summary);
        
        
        
        // Perform final logging as needed and return the processor.
        if (foundDuplicate && LOG.isDebugEnabled()) {
            LOG.debug("The following tax output fields appeared more than once under different names:");
            for (Map.Entry<String,List<String>> pieceNames : complexPiecesNames.entrySet()) {
                if (pieceNames.getValue().size() > 1) {
                    LOG.debug(pieceNames.getKey() + ": " + pieceNames.getValue().toString());
                }
            }
        }
        
        return rowProcessor;
    }

}
