//CU MOD: Back port of 2021-02-11 fix for FINP-7342 into pre-KEW 2021-01-14 version of file to obtain fix before we implement KEW-to-KFS.

/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.kns.web.ui;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.krad.datadictionary.mask.Mask;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.core.web.format.Formatter;
import org.kuali.rice.kew.api.KewApiConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Field (form field or read only)
 */
@Deprecated
public class Field implements java.io.Serializable, PropertyRenderingConfigElement {

    private static final long serialVersionUID = 6549897986355019202L;
    public static final int DEFAULT_MAXLENGTH = 30;
    public static final int DEFAULT_SIZE = 30;

    public static final String HIDDEN = "hidden";
    public static final String TEXT = "text";
    public static final String DROPDOWN = "dropdown";
    public static final String MULTIBOX = "multibox";
    public static final String MULTISELECT = "multiselect";
    public static final String RADIO = "radio";
    public static final String QUICKFINDER = "quickFinder";
    public static final String LOOKUP_RESULT_ONLY = "lookupresultonly";
    public static final String DROPDOWN_REFRESH = "dropdown_refresh";
    public static final String DROPDOWN_SCRIPT = "dropdown_script";
    public static final String CHECKBOX = "checkbox";
    public static final String CURRENCY = "currency";
    public static final String TEXT_AREA = "textarea";
    public static final String FILE = "file";
    public static final String IMAGE_SUBMIT = "imagesubmit";
    public static final String CONTAINER = "container";
    public static final String KUALIUSER = "kualiuser";
    public static final String READONLY = "readOnly";
    public static final String EDITABLE = "editable";
    public static final String LOOKUP_HIDDEN = "lookuphidden";
    public static final String LOOKUP_READONLY = "lookupreadonly";
    public static final String WORKFLOW_WORKGROUP = "workflowworkgroup";
    public static final String MASKED = "masked";
    public static final String PARTIALLY_MASKED = "partiallyMasked";

    public static final String SUB_SECTION_SEPARATOR = "subSectionSeparator";
    public static final String BLANK_SPACE = "blankSpace";
    public static final String BUTTON = "button";
    public static final String LINK = "link";
    public static final String TITLE_LINKED_TEXT = "titleLinkedText";

    //#START MOVED FROM DOC SEARCH RELATED
    public static final String DATEPICKER = "datePicker";

    public static final Set<String> SEARCH_RESULT_DISPLAYABLE_FIELD_TYPES;
    public static final Set<String> MULTI_VALUE_FIELD_TYPES = new HashSet<>();

    static {
        SEARCH_RESULT_DISPLAYABLE_FIELD_TYPES = new HashSet<>();
        SEARCH_RESULT_DISPLAYABLE_FIELD_TYPES.add(HIDDEN);
        SEARCH_RESULT_DISPLAYABLE_FIELD_TYPES.add(TEXT);
        SEARCH_RESULT_DISPLAYABLE_FIELD_TYPES.add(CURRENCY);
        SEARCH_RESULT_DISPLAYABLE_FIELD_TYPES.add(DROPDOWN);
        SEARCH_RESULT_DISPLAYABLE_FIELD_TYPES.add(RADIO);
        SEARCH_RESULT_DISPLAYABLE_FIELD_TYPES.add(DROPDOWN_REFRESH);
        SEARCH_RESULT_DISPLAYABLE_FIELD_TYPES.add(MULTIBOX);
        SEARCH_RESULT_DISPLAYABLE_FIELD_TYPES.add(MULTISELECT);

        MULTI_VALUE_FIELD_TYPES.add(MULTIBOX);
        MULTI_VALUE_FIELD_TYPES.add(MULTISELECT);
    }

    private boolean isIndexedForSearch = true;

    // following values used in ranged searches
    // the fieldLabel holds things like "From" and "Ending" and this field holds things like "Total Amount"
    private String mainFieldLabel;
    private Boolean rangeFieldInclusive;
    private boolean memberOfRange = false;
    private boolean allowInlineRange = false;

    // this field is currently a hack to allow us to indicate whether or not the column of data associated
    // with a particular field will be visible in the result set of a search or not
    private boolean isColumnVisible = true;

    //FIXME: this one definitely seems iffy, could be confused with regular fieldType, is there another better name or
    // can this go away?
    private String fieldDataType = KewApiConstants.SearchableAttributeConstants.DEFAULT_SEARCHABLE_ATTRIBUTE_TYPE_NAME;

    //used by multibox/select etc
    private String[] propertyValues;

    //extra field to skip blank option value (for route node)
    private boolean skipBlankValidValue = false;

    //#END DOC SEARCH RELATED

    private String fieldType;

    private String fieldLabel;
    private String fieldShortLabel;
    private String fieldHelpUrl;
    private String propertyName;
    private String propertyValue;

    private String alternateDisplayPropertyName;
    private String alternateDisplayPropertyValue;
    private String additionalDisplayPropertyName;
    private String additionalDisplayPropertyValue;

    private List<KeyValue> fieldValidValues;
    private String quickFinderClassNameImpl;
    private String baseLookupUrl;

    private boolean clear;
    private boolean dateField;
    private String fieldConversions;
    private boolean fieldRequired;

    private List fieldInactiveValidValues;
    private Formatter formatter;
    private boolean highlightField;
    private boolean isReadOnly;
    private String lookupParameters;
    private int maxLength;

    private HtmlData inquiryURL;
    private String propertyPrefix;
    private int size;
    private boolean upperCase;
    private int rows;
    private int cols;
    private List<Row> containerRows;
    private String fieldHelpSummary;
    private String businessObjectClassName;
    private String fieldHelpName;
    private String script;
    private String universalIdAttributeName;
    private String universalIdValue;
    private String userIdAttributeName;
    private String personNameAttributeName;
    private String personNameValue;
    private String defaultValue = KRADConstants.EMPTY_STRING;
    private boolean keyField;
    private String displayEditMode;
    private Mask displayMask;
    private String displayMaskValue;
    private String encryptedValue;
    private boolean secure;
    private String webOnBlurHandler;
    private String webOnBlurHandlerCallback;
    protected List<String> webUILeaveFieldFunctionParameters = new ArrayList<>();
    private String styleClass;
    private int formattedMaxLength;
    private String containerName;
    private String containerElementName;
    private List<Field> containerDisplayFields;
    private boolean isDatePicker;
    private boolean ranged;

    private boolean expandedTextArea;
    private String referencesToRefresh;
    private int numberOfColumnsForCollection;
    public String cellAlign;
    private String inquiryParameters;
    private boolean fieldDirectInquiryEnabled;

    public boolean fieldLevelHelpEnabled;

    public boolean fieldLevelHelpDisabled;
    public String fieldLevelHelpUrl;

    private String imageSrc;
    private String target;
    private String hrefText;

    private boolean triggerOnChange;

    private boolean newLookup;

    protected List<String> readOnlyLookupFields;

    /**
     * For container fields (i.e. fieldType.equals(CONTAINER)) with MV lookups enabled, the DD defined objectLabel of
     * the class on which a multiple value lookup is performed. The user friendly name.
     */
    private String multipleValueLookupClassLabel;
    /**
     * For container fields (i.e. fieldType.equals(CONTAINER)) with MV lookups enabled, this is the class to perform
     * a lookup upon.
     */
    private String multipleValueLookupClassName;
    /**
     * For container fields (i.e. fieldType.equals(CONTAINER)) with MV lookups enabled, this is the name of the
     * collection on the doc on which the MV lookup is performed.
     */
    private String multipleValueLookedUpCollectionName;

    public Field() {
        this.fieldLevelHelpEnabled = false;
        this.triggerOnChange = false;
    }

    /**
     * Constructor that creates an instance of this class to support inquirable
     *
     * @param propertyName property attribute of the bean
     * @param fieldLabel   label of the display field
     */
    public Field(String propertyName, String fieldLabel) {
        this.propertyName = propertyName;
        this.fieldLabel = fieldLabel;
        this.isReadOnly = false;
        this.upperCase = false;
        this.keyField = false;
        this.secure = false;
        this.fieldLevelHelpEnabled = false;
        this.triggerOnChange = false;
    }

    /**
     * Constructor that creates an instance of this class.
     *
     * @param fieldLabel               label of the search criteria field
     * @param fieldHelpUrl             url of a help link to help instructions
     * @param fieldType                type of input field for this search criteria
     * @param clear                    clear action flag
     * @param propertyName             name of the bean attribute for this search criteria
     * @param propertyValue            value of the bean attribute
     * @param fieldRequired            flag to denote if field is required
     * @param dateField                flag to denote if field should be validated as a date object
     * @param fieldValidValues         used for drop down list
     * @param quickFinderClassNameImpl class name to transfer control to quick finder
     */
    public Field(String fieldLabel, String fieldHelpUrl, String fieldType, boolean clear, String propertyName,
            String propertyValue, boolean fieldRequired, boolean dateField, List<KeyValue> fieldValidValues,
            String quickFinderClassNameImpl) {
        this.dateField = dateField;
        this.fieldLabel = fieldLabel;
        this.fieldHelpUrl = fieldHelpUrl;
        this.fieldType = fieldType;
        this.fieldRequired = fieldRequired;
        this.clear = clear;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.fieldValidValues = fieldValidValues;
        this.quickFinderClassNameImpl = quickFinderClassNameImpl;
        this.size = DEFAULT_SIZE;
        this.maxLength = DEFAULT_MAXLENGTH;
        this.isReadOnly = false;
        this.upperCase = false;
        this.keyField = false;
        this.fieldLevelHelpEnabled = false;
        this.triggerOnChange = false;
    }

    /**
     * Constructor that creates an instance of this class.
     *
     * @param fieldLabel               label of the search criteria field
     * @param fieldHelpUrl             url of a help link to help instructions
     * @param fieldType                type of input field for this search criteria
     * @param clear                    clear action flag
     * @param propertyName             name of the bean attribute for this search criteria
     * @param propertyValue            value of the bean attribute
     * @param fieldRequired            flag to denote if field is required
     * @param dateField                flag to denote if field should be validated as a date object
     * @param fieldValidValues         used for drop down list
     * @param quickFinderClassNameImpl class name to transfer control to quick finder
     * @param size                     size of the input field
     * @param maxLength                maxLength of the input field
     */
    public Field(String fieldLabel, String fieldHelpUrl, String fieldType, boolean clear, String propertyName,
            String propertyValue, boolean fieldRequired, boolean dateField, List<KeyValue> fieldValidValues,
            String quickFinderClassNameImpl, int size, int maxLength) {
        this.dateField = dateField;
        this.fieldLabel = fieldLabel;
        this.fieldHelpUrl = fieldHelpUrl;
        this.fieldType = fieldType;
        this.fieldRequired = fieldRequired;
        this.clear = clear;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.fieldValidValues = fieldValidValues;
        this.upperCase = false;
        this.quickFinderClassNameImpl = quickFinderClassNameImpl;
        if (size <= 0) {
            this.size = DEFAULT_SIZE;
        } else {
            this.size = size;
        }
        if (size <= 0) {
            this.size = DEFAULT_MAXLENGTH;
        } else {
            this.maxLength = maxLength;
        }
        this.isReadOnly = false;
        this.keyField = false;
        this.fieldLevelHelpEnabled = false;
        this.triggerOnChange = false;
    }

    /**
     * Helper method to determine if this is an INPUT type field
     *
     * @param fieldType
     */
    public static boolean isInputField(String fieldType) {
        if (StringUtils.isBlank(fieldType)) {
            return false;
        }
        // JJH: Would it be good to create a InputField Set and test to see if the fieldType exists in the set?
        return fieldType.equals(Field.DROPDOWN) || fieldType.equals(Field.DROPDOWN_REFRESH)
                || fieldType.equals(Field.TEXT) || fieldType.equals(Field.RADIO) || fieldType.equals(Field.CURRENCY)
                || fieldType.equals(Field.KUALIUSER) || fieldType.equals(Field.DROPDOWN_SCRIPT)
                || fieldType.equals(LOOKUP_READONLY) || fieldType.equals(TEXT_AREA);
    }

    public boolean isNewLookup() {
        return newLookup;
    }

    public void setNewLookup(boolean newLookup) {
        this.newLookup = newLookup;
    }

    public List<String> getReadOnlyLookupFields() {
        return readOnlyLookupFields;
    }

    public void setReadOnlyLookupFields(List<String> readOnlyLookupFields) {
        this.readOnlyLookupFields = readOnlyLookupFields;
    }

    public String getImageSrc() {
        return this.imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }

    public String getTarget() {
        return this.target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getHrefText() {
        return this.hrefText;
    }

    public void setHrefText(String hrefText) {
        this.hrefText = hrefText;
    }

    public HtmlData getInquiryURL() {
        return inquiryURL;
    }

    public void setInquiryURL(HtmlData propertyURL) {
        this.inquiryURL = propertyURL;
    }

    public int getNumberOfColumnsForCollection() {
        return numberOfColumnsForCollection;
    }

    public void setNumberOfColumnsForCollection(int numberOfColumnsForCollection) {
        this.numberOfColumnsForCollection = numberOfColumnsForCollection;
    }

    public boolean isDatePicker() {
        return isDatePicker;
    }

    public void setDatePicker(boolean isDatePicker) {
        this.isDatePicker = isDatePicker;
    }

    public boolean isRanged() {
        return this.ranged;
    }

    public void setRanged(boolean ranged) {
        this.ranged = ranged;
    }

    public boolean isExpandedTextArea() {
        return expandedTextArea;
    }

    public void setExpandedTextArea(boolean expandedTextArea) {
        this.expandedTextArea = expandedTextArea;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean containsBOData() {
        return StringUtils.isNotBlank(this.propertyName);
    }

    public String getCHECKBOX() {
        return CHECKBOX;
    }

    public String getCONTAINER() {
        return CONTAINER;
    }

    public String getDROPDOWN() {
        return DROPDOWN;
    }

    public String getTEXT_AREA() {
        return TEXT_AREA;
    }

    public String getDROPDOWN_REFRESH() {
        return DROPDOWN_REFRESH;
    }

    public String getDROPDOWN_SCRIPT() {
        return DROPDOWN_SCRIPT;
    }

    public String getMULTISELECT() {
        return MULTISELECT;
    }

    public String getKUALIUSER() {
        return KUALIUSER;
    }

    public String getFILE() {
        return FILE;
    }

    public String getSUB_SECTION_SEPARATOR() {
        return SUB_SECTION_SEPARATOR;
    }

    public String getBLANK_SPACE() {
        return BLANK_SPACE;
    }

    public String getBUTTON() {
        return BUTTON;
    }

    public String getLINK() {
        return LINK;
    }

    public String getFieldConversions() {
        return fieldConversions;
    }

    public Map<String, String> getFieldConversionMap() {
        Map<String, String> fieldConversionMap = new HashMap<>();
        if (!StringUtils.isBlank(fieldConversions)) {
            String[] splitFieldConversions = fieldConversions.split(KRADConstants.FIELD_CONVERSIONS_SEPARATOR);
            for (String fieldConversion : splitFieldConversions) {
                if (!StringUtils.isBlank(fieldConversion)) {
                    String[] splitFieldConversion = fieldConversion.split(KRADConstants.FIELD_CONVERSION_PAIR_SEPARATOR,
                            2);
                    String originalFieldName = splitFieldConversion[0];
                    String convertedFieldName = "";
                    if (splitFieldConversion.length > 1) {
                        convertedFieldName = splitFieldConversion[1];
                    }
                    fieldConversionMap.put(originalFieldName, convertedFieldName);
                }
            }
        }
        return fieldConversionMap;
    }

    public String getFieldHelpUrl() {
        return fieldHelpUrl;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getFieldShortLabel() {
        return fieldShortLabel;
    }

    public void setFieldShortLabel(String fieldShortLabel) {
        this.fieldShortLabel = fieldShortLabel;
    }

    public List<KeyValue> getFieldValidValues() {
        return fieldValidValues;
    }

    @Override
    public Formatter getFormatter() {
        return formatter;
    }

    public String getHIDDEN() {
        return HIDDEN;
    }

    public String getLookupParameters() {
        return lookupParameters;
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public String getPropertyValue() {
        if (propertyValue == null) {
            propertyValue = KRADConstants.EMPTY_STRING;
        }

        return propertyValue;
    }

    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    public String getCleanPropertyValue() {
        //CU Mod: FINP-7342 back port fix.
        //return Jsoup.clean(getPropertyValue(), Whitelist.basic());
        return Jsoup.clean(getPropertyValue(), Whitelist.basic()).replace("&amp;", "&");
    }

    public void setPropertyPrefix(String propertyPrefix) {
        this.propertyPrefix = propertyPrefix;
    }

    public String getQUICKFINDER() {
        return QUICKFINDER;
    }

    public String getQuickFinderClassNameImpl() {
        return quickFinderClassNameImpl;
    }

    public String getRADIO() {
        return RADIO;
    }

    public int getSize() {
        return size;
    }

    public String getTEXT() {
        return TEXT;
    }

    public String getTITLE_LINKED_TEXT() {
        return TITLE_LINKED_TEXT;
    }

    public String getCURRENCY() {
        return CURRENCY;
    }

    public String getIMAGE_SUBMIT() {
        return IMAGE_SUBMIT;
    }

    public String getLOOKUP_HIDDEN() {
        return LOOKUP_HIDDEN;
    }

    public String getLOOKUP_READONLY() {
        return LOOKUP_READONLY;
    }

    public String getWORKFLOW_WORKGROUP() {
        return WORKFLOW_WORKGROUP;
    }

    public boolean isClear() {
        return clear;
    }

    public boolean isDateField() {
        return dateField;
    }

    public boolean isFieldRequired() {
        return fieldRequired;
    }

    public boolean isHighlightField() {
        return highlightField;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public boolean isUpperCase() {
        return upperCase;
    }

    public void setClear(boolean clear) {
        this.clear = clear;
    }

    public void setDateField(boolean dateField) {
        this.dateField = dateField;
    }

    public void setFieldConversions(Map<String, String> fieldConversionsMap) {
        List<String> keyValuePairStrings = new ArrayList<>();
        for (String key : fieldConversionsMap.keySet()) {
            String mappedField = fieldConversionsMap.get(key);
            keyValuePairStrings.add(key + ":" + mappedField);
        }
        String commaDelimitedConversions = StringUtils.join(keyValuePairStrings, ",");
        setFieldConversions(commaDelimitedConversions);
    }

    public void setFieldConversions(String fieldConversions) {
        this.fieldConversions = fieldConversions;
    }

    public void appendFieldConversions(String fieldConversions) {
        if (StringUtils.isNotBlank(fieldConversions)) {
            this.fieldConversions = this.fieldConversions + "," + fieldConversions;
        }
    }

    public void setFieldHelpUrl(String fieldHelpUrl) {
        this.fieldHelpUrl = fieldHelpUrl;
    }

    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    public void setFieldRequired(boolean fieldRequired) {
        this.fieldRequired = fieldRequired;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public void setFieldValidValues(List<KeyValue> fieldValidValues) {
        this.fieldValidValues = fieldValidValues;
    }

    public boolean getHasBlankValidValue() {
        if (fieldValidValues == null) {
            throw new IllegalStateException("Valid values are undefined");
        }
        for (KeyValue keyLabel : fieldValidValues) {
            if (keyLabel.getKey().equals("")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public void setHighlightField(boolean highlightField) {
        this.highlightField = highlightField;
    }

    public void setLookupParameters(Map lookupParametersMap) {
        String lookupParameterString = "";
        for (Iterator iter = lookupParametersMap.keySet().iterator(); iter.hasNext(); ) {
            String field = (String) iter.next();
            String mappedField = (String) lookupParametersMap.get(field);
            lookupParameterString += field + ":" + mappedField;
            if (iter.hasNext()) {
                lookupParameterString += ",";
            }
        }
        setLookupParameters(lookupParameterString);
    }

    public void setLookupParameters(String lookupParameters) {
        this.lookupParameters = lookupParameters;
    }

    /**
     * This method appends the passed-in lookupParameters to the existing
     *
     * @param lookupParameters
     */
    public void appendLookupParameters(String lookupParameters) {
        if (StringUtils.isNotBlank(lookupParameters)) {
            if (StringUtils.isBlank(this.lookupParameters)) {
                this.lookupParameters = lookupParameters;
            } else {
                this.lookupParameters = this.lookupParameters + "," + lookupParameters;
            }
        }
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void setPropertyName(String propertyName) {
        String newPropertyName = KRADConstants.EMPTY_STRING;
        if (propertyName != null) {
            newPropertyName = propertyName;
        }
        this.propertyName = newPropertyName;
    }

    public void setPropertyValue(Object propertyValue) {
        String newPropertyValue = ObjectUtils.formatPropertyValue(propertyValue);

        if (isUpperCase()) {
            newPropertyValue = newPropertyValue.toUpperCase(Locale.US);
        }

        this.propertyValue = newPropertyValue;
    }

    @Override
    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public void setQuickFinderClassNameImpl(String quickFinderClassNameImpl) {
        this.quickFinderClassNameImpl = quickFinderClassNameImpl;
    }

    public void setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setUpperCase(boolean upperCase) {
        this.upperCase = upperCase;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public List<Row> getContainerRows() {
        return containerRows;
    }

    public void setContainerRows(List<Row> containerRows) {
        this.containerRows = containerRows;
    }

    public String getBusinessObjectClassName() {
        return businessObjectClassName;
    }

    public void setBusinessObjectClassName(String businessObjectClassName) {
        this.businessObjectClassName = businessObjectClassName;
    }

    public String getFieldHelpSummary() {
        return fieldHelpSummary;
    }

    public void setFieldHelpSummary(String fieldHelpSummary) {
        this.fieldHelpSummary = fieldHelpSummary;
    }

    public String getFieldHelpName() {
        return fieldHelpName;
    }

    public void setFieldHelpName(String fieldHelpName) {
        this.fieldHelpName = fieldHelpName;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getPersonNameAttributeName() {
        return personNameAttributeName;
    }

    public void setPersonNameAttributeName(String personNameAttributeName) {
        this.personNameAttributeName = personNameAttributeName;
    }

    public String getUniversalIdAttributeName() {
        return universalIdAttributeName;
    }

    public void setUniversalIdAttributeName(String universalIdAttributeName) {
        this.universalIdAttributeName = universalIdAttributeName;
    }

    public String getUserIdAttributeName() {
        return userIdAttributeName;
    }

    public void setUserIdAttributeName(String userIdAttributeName) {
        this.userIdAttributeName = userIdAttributeName;
    }

    public boolean isKeyField() {
        return keyField;
    }

    public void setKeyField(boolean keyField) {
        this.keyField = keyField;
    }

    public String getDisplayEditMode() {
        return displayEditMode;
    }

    public void setDisplayEditMode(String displayEditMode) {
        this.displayEditMode = displayEditMode;
    }

    public Mask getDisplayMask() {
        return displayMask;
    }

    public void setDisplayMask(Mask displayMask) {
        this.displayMask = displayMask;
    }

    public String getDisplayMaskValue() {
        return displayMaskValue;
    }

    public void setDisplayMaskValue(String displayMaskValue) {
        this.displayMaskValue = displayMaskValue;
    }

    public String getEncryptedValue() {
        return encryptedValue;
    }

    public void setEncryptedValue(String encryptedValue) {
        this.encryptedValue = encryptedValue;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * @return the method name of a function present in the page which should be called when the user tabs away from
     *         the field.
     */
    public String getWebOnBlurHandler() {
        return webOnBlurHandler;
    }

    public void setWebOnBlurHandler(String webOnBlurHandler) {
        this.webOnBlurHandler = webOnBlurHandler;
    }

    /**
     * @return the method name of a function present in the page which should be called after an AJAX call from the
     *         onblur handler.
     */
    public String getWebOnBlurHandlerCallback() {
        return webOnBlurHandlerCallback;
    }

    public void setWebOnBlurHandlerCallback(String webOnBlurHandlerCallback) {
        this.webOnBlurHandlerCallback = webOnBlurHandlerCallback;
    }

    @Override
    public String toString() {
        return "[" + getFieldType() + "] " + getPropertyName() + " = '" + getPropertyValue() + "'";
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public int getFormattedMaxLength() {
        return formattedMaxLength;
    }

    public void setFormattedMaxLength(int formattedMaxLength) {
        this.formattedMaxLength = formattedMaxLength;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getContainerElementName() {
        return containerElementName;
    }

    public void setContainerElementName(String containerElementName) {
        this.containerElementName = containerElementName;
    }

    public List<Field> getContainerDisplayFields() {
        return containerDisplayFields;
    }

    public void setContainerDisplayFields(List<Field> containerDisplayFields) {
        this.containerDisplayFields = containerDisplayFields;
    }

    public String getReferencesToRefresh() {
        return referencesToRefresh;
    }

    public void setReferencesToRefresh(String referencesToRefresh) {
        this.referencesToRefresh = referencesToRefresh;
    }

    /**
     * @return The DD defined objectLabel of the class on which a multiple value lookup is performed
     */
    public String getMultipleValueLookupClassLabel() {
        return multipleValueLookupClassLabel;
    }

    /**
     * @param multipleValueLookupClassLabel The DD defined objectLabel of the class on which a multiple value lookup
     *                                      is performed
     */
    public void setMultipleValueLookupClassLabel(String multipleValueLookupClassLabel) {
        this.multipleValueLookupClassLabel = multipleValueLookupClassLabel;
    }

    /**
     * @return container fields (i.e. fieldType.equals(CONTAINER)) with MV lookups enabled, this is the name of the
     *         collection on the doc on which the MV lookup is performed
     */
    public String getMultipleValueLookedUpCollectionName() {
        return multipleValueLookedUpCollectionName;
    }

    /**
     * @param multipleValueLookedUpCollectionName for container fields (i.e. fieldType.equals(CONTAINER)) with MV
     *                                            lookups enabled, this is the name of the collection on the doc on
     *                                            which the MV lookup is performed
     */
    public void setMultipleValueLookedUpCollectionName(String multipleValueLookedUpCollectionName) {
        this.multipleValueLookedUpCollectionName = multipleValueLookedUpCollectionName;
    }

    /**
     * @return for container fields (i.e. fieldType.equals(CONTAINER)) with MV lookups enabled, this is the class to
     *         perform a lookup upon
     */
    public String getMultipleValueLookupClassName() {
        return multipleValueLookupClassName;
    }

    /**
     * @param multipleValueLookupClassName for container fields (i.e. fieldType.equals(CONTAINER)) with MV lookups
     *                                     enabled, this is the class to perform a lookup upon
     */
    public void setMultipleValueLookupClassName(String multipleValueLookupClassName) {
        this.multipleValueLookupClassName = multipleValueLookupClassName;
    }

    /**
     * @return the td alignment to use for the Field.
     */
    public String getCellAlign() {
        return cellAlign;
    }

    /**
     * @param cellAlign the td alignment to use for the Field.
     */
    public void setCellAlign(String cellAlign) {
        this.cellAlign = cellAlign;
    }

    public String getInquiryParameters() {
        return this.inquiryParameters;
    }

    public void setInquiryParameters(String inquiryParameters) {
        this.inquiryParameters = inquiryParameters;
    }

    /**
     * Returns whether field level help is enabled for this field.  If this value is true, then the field level help
     * will be enabled. If false, then whether a field is enabled is determined by the value returned by
     * {@link #isFieldLevelHelpDisabled()} and the system-wide parameter setting.  Note that if a field is read-only,
     * that may cause field-level help to not be rendered.
     *
     * @return true if field level help is enabled, false if the value of this method should NOT be used to determine
     *         whether this method's return value affects the enablement of field level help
     */
    public boolean isFieldLevelHelpEnabled() {
        return this.fieldLevelHelpEnabled;
    }

    public void setFieldLevelHelpEnabled(boolean fieldLevelHelpEnabled) {
        this.fieldLevelHelpEnabled = fieldLevelHelpEnabled;
    }

    /**
     * Returns whether field level help is disabled for this field.  If this value is true and
     * {@link #isFieldLevelHelpEnabled()} returns false, then the field level help will not be rendered. If both this
     * and {@link #isFieldLevelHelpEnabled()} return false, then the system-wide setting will determine whether field
     * level help is enabled.  Note that if a field is read-only, that may cause field-level help to not be rendered.
     *
     * @return true if field level help is disabled, false if the value of this method should NOT be used to determine
     *         whether this method's return value affects the enablement of field level help
     */
    public boolean isFieldLevelHelpDisabled() {
        return this.fieldLevelHelpDisabled;
    }

    public void setFieldLevelHelpDisabled(boolean fieldLevelHelpDisabled) {
        this.fieldLevelHelpDisabled = fieldLevelHelpDisabled;
    }

    public boolean isFieldDirectInquiryEnabled() {
        return this.fieldDirectInquiryEnabled;
    }

    public void setFieldDirectInquiryEnabled(boolean fieldDirectInquiryEnabled) {
        this.fieldDirectInquiryEnabled = fieldDirectInquiryEnabled;
    }

    public List getFieldInactiveValidValues() {
        return this.fieldInactiveValidValues;
    }

    public void setFieldInactiveValidValues(List fieldInactiveValidValues) {
        this.fieldInactiveValidValues = fieldInactiveValidValues;
    }

    public boolean isTriggerOnChange() {
        return this.triggerOnChange;
    }

    public void setTriggerOnChange(boolean triggerOnChange) {
        this.triggerOnChange = triggerOnChange;
    }

    public boolean getHasLookupable() {
        return !StringUtils.isBlank(quickFinderClassNameImpl);
    }

    @Override
    public String getAlternateDisplayPropertyName() {
        return this.alternateDisplayPropertyName;
    }

    @Override
    public void setAlternateDisplayPropertyName(String alternateDisplayPropertyName) {
        this.alternateDisplayPropertyName = alternateDisplayPropertyName;
    }

    public String getAlternateDisplayPropertyValue() {
        return this.alternateDisplayPropertyValue;
    }

    public void setAlternateDisplayPropertyValue(Object alternateDisplayPropertyValue) {
        this.alternateDisplayPropertyValue = ObjectUtils.formatPropertyValue(alternateDisplayPropertyValue);
    }

    @Override
    public String getAdditionalDisplayPropertyName() {
        return this.additionalDisplayPropertyName;
    }

    @Override
    public void setAdditionalDisplayPropertyName(String additionalDisplayPropertyName) {
        this.additionalDisplayPropertyName = additionalDisplayPropertyName;
    }

    public String getAdditionalDisplayPropertyValue() {
        return this.additionalDisplayPropertyValue;
    }

    public void setAdditionalDisplayPropertyValue(Object additionalDisplayPropertyValue) {
        this.additionalDisplayPropertyValue = ObjectUtils.formatPropertyValue(additionalDisplayPropertyValue);
    }

    public boolean isIndexedForSearch() {
        return this.isIndexedForSearch;
    }

    public void setIndexedForSearch(boolean indexedForSearch) {
        this.isIndexedForSearch = indexedForSearch;
    }

    public String getMainFieldLabel() {
        return this.mainFieldLabel;
    }

    public Boolean getRangeFieldInclusive() {
        return this.rangeFieldInclusive;
    }

    public boolean isMemberOfRange() {
        return this.memberOfRange;
    }

    public void setMainFieldLabel(String mainFieldLabel) {
        this.mainFieldLabel = mainFieldLabel;
    }

    public void setRangeFieldInclusive(Boolean rangeFieldInclusive) {
        this.rangeFieldInclusive = rangeFieldInclusive;
    }

    public void setMemberOfRange(boolean memberOfRange) {
        this.memberOfRange = memberOfRange;
    }

    public boolean isInclusive() {
        return (rangeFieldInclusive == null) ? true : rangeFieldInclusive;
    }

    public String getFieldDataType() {
        return this.fieldDataType;
    }

    public void setFieldDataType(String fieldDataType) {
        this.fieldDataType = fieldDataType;
    }

    public boolean isColumnVisible() {
        return this.isColumnVisible;
    }

    public void setColumnVisible(boolean isColumnVisible) {
        this.isColumnVisible = isColumnVisible;
    }

    public String[] getPropertyValues() {
        return this.propertyValues;
    }

    public void setPropertyValues(String[] propertyValues) {
        this.propertyValues = propertyValues;
    }

    public boolean isSkipBlankValidValue() {
        return this.skipBlankValidValue;
    }

    public void setSkipBlankValidValue(boolean skipBlankValidValue) {
        this.skipBlankValidValue = skipBlankValidValue;
    }

    public boolean isAllowInlineRange() {
        return this.allowInlineRange;
    }

    public void setAllowInlineRange(boolean allowInlineRange) {
        this.allowInlineRange = allowInlineRange;
    }

    public String getUniversalIdValue() {
        return this.universalIdValue;
    }

    public void setUniversalIdValue(String universalIdValue) {
        this.universalIdValue = universalIdValue;
    }

    public String getPersonNameValue() {
        return this.personNameValue;
    }

    public void setPersonNameValue(String personNameValue) {
        this.personNameValue = personNameValue;
    }

    public String getBaseLookupUrl() {
        return this.baseLookupUrl;
    }

    public void setBaseLookupUrl(String baseLookupURL) {
        this.baseLookupUrl = baseLookupURL;
    }

    public String getFieldLevelHelpUrl() {
        return fieldLevelHelpUrl;
    }

    public void setFieldLevelHelpUrl(String fieldLevelHelpUrl) {
        this.fieldLevelHelpUrl = fieldLevelHelpUrl;
    }

    public List<String> getWebUILeaveFieldFunctionParameters() {
        return this.webUILeaveFieldFunctionParameters;
    }

    public void setWebUILeaveFieldFunctionParameters(
        List<String> webUILeaveFieldFunctionParameters) {
        this.webUILeaveFieldFunctionParameters = webUILeaveFieldFunctionParameters;
    }

    public String getWebUILeaveFieldFunctionParametersString() {
        return KRADUtils.joinWithQuotes(getWebUILeaveFieldFunctionParameters());
    }

}
