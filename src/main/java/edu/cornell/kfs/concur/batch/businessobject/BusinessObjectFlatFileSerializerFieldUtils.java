package edu.cornell.kfs.concur.batch.businessobject;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.util.ObjectPropertyUtils;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils;
import org.kuali.kfs.krad.bo.BusinessObject;

/**
 * Base utility class for serializing specific objects into single-line fixed-length Strings,
 * so that they can be written to a flat file.
 * 
 * KFS already has utility classes that compute the metadata necessary for parsing
 * flat file lines into BOs. This serializer uses instances of such classes to get
 * that same metadata, but uses them for serialization purposes instead.
 * 
 * It is necessary to create one serializer instance per line type for a particular file.
 * If the flat file can use differently-formatted lines to generate the same type of BO,
 * then extra serializer instances are needed for each variation.
 * 
 * The serialization process iterates over the properties defined by the field util object's
 * getOrderedProperties() method, in the order given by its returned array. Each property value
 * is then converted to a String, and subsequently truncated or right-padded as needed
 * to match the property's max length (as given by the util object's getFieldLengthMap() mapping).
 * Lastly, any control characters in the generated line are converted into spaces.
 * 
 * By default, each property value is converted using the defaultConversionToString() method,
 * which returns either the property's toString() value if the property is non-null,
 * or an empty String otherwise. (It is assumed that the property's toString() value is non-null.)
 * Subclasses can override the serialization behavior on a per-property basis by implementing
 * the getCustomSerializerFunctions() method, and having it return a Map from property names
 * to Function implementations (or an empty Map to use the default behavior for all properties).
 * Each serializer Function must accept a potentially-null Object and return a non-null String.
 */
@SuppressWarnings("deprecation")
public abstract class BusinessObjectFlatFileSerializerFieldUtils {

    protected static final String CONTROL_CHARS_PATTERN_STRING = "\\p{Cntrl}";

    protected BusinessObjectStringParserFieldUtils parserFieldUtils;
    protected Map<String,Function<Object,String>> fieldSerializerFunctions;
    protected Pattern controlCharacterPattern;
    protected int lineLength;

    public void setParserFieldUtils(BusinessObjectStringParserFieldUtils parserFieldUtils) {
        this.parserFieldUtils = parserFieldUtils;
    }

    /**
     * This method MUST be invoked prior to serializing any objects.
     * The serializeBusinessObject() method will automatically call this method if necessary.
     * 
     * The initialization will also call the getCustomSerializerFunctions() method,
     * so that it can cache its returned Map.
     * 
     * @throws NullPointerException if the getCustomSerializerFunctions() method returns a null Map.
     */
    public void initialize() {
        controlCharacterPattern = Pattern.compile(CONTROL_CHARS_PATTERN_STRING);
        lineLength = calculateLineLength();
        fieldSerializerFunctions = getCustomSerializerFunctions();
        
        if (fieldSerializerFunctions == null) {
            throw new NullPointerException("getCustomSerializerFunctions() call returned a null Map");
        }
    }

    /**
     * Takes a business object that is compatible with the configured BO parsing utility object,
     * and serializes it to a fixed-length String based on the utility's metadata and any
     * custom property-serialization Functions that have been configured. Property values
     * will be truncated or right-padded as needed to match the max lengths from the metadata.
     * 
     * If the initialize() method has not already been invoked, this method will call it
     * prior to performing the object serialization.
     * 
     * @param businessObject The business object to serialize; cannot be null.
     * @return A single-line fixed-width String representation of the given object, intended for being written to an output file.
     * @throws IllegalArgumentException if businessObject is null or is not an instance of the expected type.
     * @throws NullPointerException if thrown by initialize() or if a property-serialization Function converts a property value to a null String.
     */
    public String serializeBusinessObject(BusinessObject businessObject) {
        if (ObjectUtils.isNull(businessObject)) {
            throw new IllegalArgumentException("businessObject cannot be null");
        } else if (!parserFieldUtils.getBusinessObjectClass().isAssignableFrom(businessObject.getClass())) {
            throw new IllegalArgumentException("businessObject is not of the expected type");
        } else if (fieldSerializerFunctions == null) {
            initialize();
        }
        
        Map<String,Integer> fieldLengthMap = parserFieldUtils.getFieldLengthMap();
        StringBuilder outputLine = new StringBuilder(lineLength);
        
        for (String propertyName : parserFieldUtils.getOrderedProperties()) {
            Object propertyValue = ObjectPropertyUtils.getPropertyValue(businessObject, propertyName);
            int propertyMaxLength = fieldLengthMap.get(propertyName).intValue();
            Function<Object,String> serializerFunction = fieldSerializerFunctions.getOrDefault(
                    propertyName, this::defaultConversionToString);
            
            String stringValue = serializerFunction.apply(propertyValue);
            if (stringValue == null) {
                throw new NullPointerException("Property was serialized to a null string: " + propertyName);
            }
            stringValue = formatForOutput(stringValue, propertyMaxLength);
            outputLine.append(stringValue);
        }
        
        String outputString = outputLine.toString();
        return controlCharacterPattern.matcher(outputString).replaceAll(KRADConstants.BLANK_SPACE);
    }

    protected String formatForOutput(String value, int maxLength) {
        if (value.length() == maxLength) {
            return value;
        } else if (value.length() < maxLength) {
            return StringUtils.rightPad(value, maxLength);
        } else {
            return StringUtils.left(value, maxLength);
        }
    }

    protected int calculateLineLength() {
        String[] orderedProperties = parserFieldUtils.getOrderedProperties();
        Map<String,Integer> fieldBeginningPositionMap = parserFieldUtils.getFieldBeginningPositionMap();
        Map<String,Integer> fieldLengthMap = parserFieldUtils.getFieldLengthMap();
        
        String lastPropertyName = orderedProperties[orderedProperties.length - 1];
        int lastPropertyStart = fieldBeginningPositionMap.get(lastPropertyName).intValue();
        int lastPropertyLength = fieldLengthMap.get(lastPropertyName).intValue();
        
        return lastPropertyStart + lastPropertyLength;
    }

    /**
     * This is the conversion method that will be used if the getCustomSerializerFunctions() Map
     * does not contain an entry for a specific property name.
     * 
     * @param value The property value to be converted; may be null.
     * @return The property's toString() value if the property is non-null, or an empty String otherwise.
     */
    protected String defaultConversionToString(Object value) {
        return (value != null) ? value.toString() : StringUtils.EMPTY;
    }

    /**
     * Subclasses must implement this method to specify any special serialization handling
     * for specific object properties. If no special handling is needed for any of the properties,
     * then an empty Map should be returned.
     * 
     * @return A Map from property names to value conversion Functions; cannot be null.
     */
    protected abstract Map<String,Function<Object,String>> getCustomSerializerFunctions();

}
