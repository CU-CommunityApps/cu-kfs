/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.kns.kim.type;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.uif.AttributeError;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.TypeUtils;
import org.kuali.kfs.core.web.format.Formatter;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.type.KimAttributeField;
import org.kuali.kfs.kim.api.type.KimTypeInfoService;
import org.kuali.kfs.kim.api.type.QuickFinder;
import org.kuali.kfs.kim.bo.impl.KimAttributes;
import org.kuali.kfs.kim.framework.type.KimTypeService;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.impl.type.KimTypeAttribute;
import org.kuali.kfs.kns.lookup.LookupUtils;
import org.kuali.kfs.kns.service.DictionaryValidationService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.util.FieldUtils;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.comparator.StringValueComparator;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.PrimitiveAttributeDefinition;
import org.kuali.kfs.krad.datadictionary.RelationshipDefinition;
import org.kuali.kfs.krad.datadictionary.validation.ValidationUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.springframework.util.AutoPopulatingList;

import java.beans.PropertyDescriptor;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
 * Cornell Modification: Changes for KFSPTS-27121 
 */

/**
 * A base class for {@code KimTypeService} implementations which read attribute-related information from the Data
 * Dictionary. This implementation is currently written against the KNS apis for Data Dictionary. Additionally, it
 * supports the ability to read non-Data Dictionary attribute information from the {@link KimTypeInfoService}.
 */
public class DataDictionaryTypeServiceBase implements KimTypeService {

    private static final Logger LOG = LogManager.getLogger();
    private static final String ANY_CHAR_PATTERN_S = ".*";
    private static final Pattern ANY_CHAR_PATTERN = Pattern.compile(ANY_CHAR_PATTERN_S);
    private static final String ERROR_CANT_BE_MODIFIED = "error.unmodifiable.attribute";
    private static final String ERROR_INCLUSIVE_MIN = "error.inclusiveMin";
    protected static final String COMMA_SEPARATOR = ", ";

    protected BusinessObjectService businessObjectService;
    protected DataDictionaryService dataDictionaryService;
    protected DictionaryValidationService dictionaryValidationService;
    protected DocumentTypeService documentTypeService;
    protected KimTypeInfoService kimTypeInfoService;

    @Override
    public List<String> getWorkflowRoutingAttributes(final String routeLevel) {
        if (StringUtils.isBlank(routeLevel)) {
            throw new IllegalArgumentException("routeLevel was blank or null");
        }

        return Collections.emptyList();
    }

    @Override
    public List<KimAttributeField> getAttributeDefinitions(final String kimTypeId) {
        final List<String> uniqueAttributes = getUniqueAttributes(kimTypeId);

        //using map.entry as a 2-item tuple
        final List<Map.Entry<String, KimAttributeField>> definitions = new ArrayList<>();
        final KimType kimType = kimTypeInfoService.getKimType(kimTypeId);
        final String nsCode = kimType.getNamespaceCode();

        for (final KimTypeAttribute typeAttribute : kimType.getAttributeDefinitions()) {
            final KimAttributeField definition;
            if (typeAttribute.getKimAttribute().getComponentName() == null) {
                definition = getNonDataDictionaryAttributeDefinition(nsCode, kimTypeId, typeAttribute,
                        uniqueAttributes);
            } else {
                definition = getDataDictionaryAttributeDefinition(nsCode, kimTypeId, typeAttribute, uniqueAttributes);
            }

            if (definition != null) {
                definitions.add(new AbstractMap.SimpleEntry<>(typeAttribute.getSortCode() != null ?
                        typeAttribute.getSortCode() : "", definition));
            }
        }

        definitions.sort(Comparator.comparing(Map.Entry<String, KimAttributeField>::getKey));

        //transform removing sortCode
        return Collections.unmodifiableList(definitions.stream().map(Map.Entry::getValue).collect(Collectors.toList()));
    }

    /**
     * This is the default implementation.  It calls into the service for each attribute to validate it there. No
     * combination validation is done.  That should be done by overriding this method.
     */
    @Override
    public List<AttributeError> validateAttributes(final String kimTypeId, final Map<String, String> attributes) {
        if (StringUtils.isBlank(kimTypeId)) {
            throw new IllegalArgumentException("kimTypeId was null or blank");
        }

        if (attributes == null) {
            throw new IllegalArgumentException("attributes was null or blank");
        }

        final List<AttributeError> validationErrors = new ArrayList<>();
        final KimType kimType = kimTypeInfoService.getKimType(kimTypeId);

        for (final Map.Entry<String, String> entry : attributes.entrySet()) {
            final KimTypeAttribute attr = kimType.getAttributeDefinitionByName(entry.getKey());
            final List<AttributeError> attributeErrors;
            if (attr.getKimAttribute().getComponentName() == null) {
                attributeErrors = validateNonDataDictionaryAttribute(attr, entry.getKey(), entry.getValue());
            } else {
                attributeErrors = validateDataDictionaryAttribute(attr, entry.getKey(), entry.getValue());
            }

            if (attributeErrors != null) {
                validationErrors.addAll(attributeErrors);
            }
        }

        final List<AttributeError> referenceCheckErrors = validateReferencesExistAndActive(kimType,
                attributes, validationErrors);
        validationErrors.addAll(referenceCheckErrors);

        return Collections.unmodifiableList(validationErrors);
    }

    @Override
    public List<AttributeError> validateAttributesAgainstExisting(
            final String kimTypeId,
            final Map<String, String> newAttributes, final Map<String, String> oldAttributes) {
        if (StringUtils.isBlank(kimTypeId)) {
            throw new IllegalArgumentException("kimTypeId was null or blank");
        }

        if (newAttributes == null) {
            throw new IllegalArgumentException("newAttributes was null or blank");
        }

        if (oldAttributes == null) {
            throw new IllegalArgumentException("oldAttributes was null or blank");
        }
        return Collections.emptyList();
    }

    /**
     * This method matches input attribute set entries and standard attribute set entries using literal string match.
     */
    protected boolean performMatch(final Map<String, String> inputAttributes, final Map<String, String> storedAttributes) {
        if (storedAttributes == null || inputAttributes == null) {
            return true;
        }
        for (final Map.Entry<String, String> entry : storedAttributes.entrySet()) {
            if (inputAttributes.containsKey(entry.getKey())
                    && !StringUtils.equals(inputAttributes.get(entry.getKey()), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    protected Map<String, String> translateInputAttributes(final Map<String, String> qualification) {
        return qualification;
    }

    protected List<AttributeError> validateReferencesExistAndActive(
            final KimType kimType,
                                                                    final Map<String, String> attributes, final List<AttributeError> previousValidationErrors) {
        final Map<String, BusinessObject> componentClassInstances = new HashMap<>();
        final List<AttributeError> errors = new ArrayList<>();

        for (final String attributeName : attributes.keySet()) {
            final KimTypeAttribute attr = kimType.getAttributeDefinitionByName(attributeName);

            if (StringUtils.isNotBlank(attr.getKimAttribute().getComponentName())) {
                if (!componentClassInstances.containsKey(attr.getKimAttribute().getComponentName())) {
                    try {
                        final Class<?> componentClass = Class.forName(attr.getKimAttribute().getComponentName());
                        if (!BusinessObject.class.isAssignableFrom(componentClass)) {
                            LOG.warn(
                                    "Class {} does not implement BusinessObject.  Unable to perform reference "
                                    + "existence and active validation",
                                    componentClass::getName
                            );
                            continue;
                        }
                        final BusinessObject componentInstance = (BusinessObject) componentClass.newInstance();
                        componentClassInstances.put(attr.getKimAttribute().getComponentName(), componentInstance);
                    } catch (final Exception e) {
                        LOG.error("Unable to instantiate class for attribute: {}", attributeName, e);
                    }
                }
            }
        }

        // now that we have instances for each component class, try to populate them with any attribute we can,
        // assuming there were no other validation errors associated with it
        for (final Map.Entry<String, String> entry : attributes.entrySet()) {
            if (!AttributeError.containsAttribute(entry.getKey(), previousValidationErrors)) {
                for (final Object componentInstance : componentClassInstances.values()) {
                    try {
                        ObjectUtils.setObjectProperty(componentInstance, entry.getKey(), entry.getValue());
                    } catch (final NoSuchMethodException e) {
                        // this is expected since not all attributes will be in all components
                    } catch (final Exception e) {
                        LOG.error("Unable to set object property class: {} property: {}",
                                () -> componentInstance.getClass().getName(),
                                entry::getKey,
                                () -> e
                        );
                    }
                }
            }
        }

        for (final Map.Entry<String, BusinessObject> entry : componentClassInstances.entrySet()) {
            final List<RelationshipDefinition> relationships = getBusinessObjectDictionaryService()
                    .getBusinessObjectEntry(entry.getKey()).getRelationships();
            if (relationships == null) {
                continue;
            }

            for (final RelationshipDefinition relationshipDefinition : relationships) {
                final List<PrimitiveAttributeDefinition> primitiveAttributes = relationshipDefinition.getPrimitiveAttributes();

                // this code assumes that the last defined primitiveAttribute is the attributeToHighlightOnFail
                final String attributeToHighlightOnFail = primitiveAttributes.get(primitiveAttributes.size() - 1)
                        .getSourceName();

                // TODO: will this work for user ID attributes?

                if (!attributes.containsKey(attributeToHighlightOnFail)) {
                    // if the attribute to highlight wasn't passed in, don't bother validating
                    continue;
                }

                final KimTypeAttribute attr = kimType.getAttributeDefinitionByName(attributeToHighlightOnFail);
                if (attr != null) {
                    final String attributeDisplayLabel;
                    if (StringUtils.isNotBlank(attr.getKimAttribute().getComponentName())) {
                        attributeDisplayLabel = getDataDictionaryService().getAttributeLabel(
                                attr.getKimAttribute().getComponentName(), attributeToHighlightOnFail);
                    } else {
                        attributeDisplayLabel = attr.getKimAttribute().getAttributeLabel();
                    }

                    getDictionaryValidationService().validateReferenceExistsAndIsActive(entry.getValue(),
                            relationshipDefinition.getObjectAttributeName(),
                        attributeToHighlightOnFail, attributeDisplayLabel);
                }
                final List<String> extractedErrors = extractErrorsFromGlobalVariablesErrorMap(attributeToHighlightOnFail);
                if (CollectionUtils.isNotEmpty(extractedErrors)) {
                    errors.add(AttributeError.Builder.create(attributeToHighlightOnFail,
                            extractedErrors).build());
                }
            }
        }
        return errors;
    }

    protected List<AttributeError> validateAttributeRequired(
            final String kimTypeId, final String objectClassName,
            final String attributeName, final Object attributeValue, final String errorKey) {
        final List<AttributeError> errors = new ArrayList<>();
        // check if field is a required field for the business object
        if (attributeValue == null || attributeValue instanceof String && StringUtils.isBlank((String) attributeValue)) {
            final List<KimAttributeField> map = getAttributeDefinitions(kimTypeId);
            final KimAttributeField definition = DataDictionaryTypeServiceHelper.findAttributeField(attributeName, map);

            final boolean required = definition.getAttributeField().isRequired();
            if (required) {
                // get label of attribute for message
                final String errorLabel = DataDictionaryTypeServiceHelper.getAttributeErrorLabel(definition);
                errors.add(AttributeError.Builder.create(errorKey, DataDictionaryTypeServiceHelper
                        .createErrorString(KFSKeyConstants.ERROR_REQUIRED, errorLabel)).build());
            }
        }
        return errors;
    }

    protected List<AttributeError> validateDataDictionaryAttribute(
            final String kimTypeId, final String entryName,
            final Object object, final PropertyDescriptor propertyDescriptor) {
        return validatePrimitiveFromDescriptor(kimTypeId, entryName, object, propertyDescriptor);
    }

    protected List<AttributeError> validateDataDictionaryAttribute(
            final KimTypeAttribute attr, final String key,
                                                                   final String value) {
        try {
            // create an object of the proper type per the component
            final Object componentObject = Class.forName(attr.getKimAttribute().getComponentName()).newInstance();

            if (attr.getKimAttribute().getAttributeName() != null) {
                // get the bean utils descriptor for accessing the attribute on that object
                final PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(componentObject,
                        attr.getKimAttribute().getAttributeName());
                if (propertyDescriptor != null) {
                    // set the value on the object so that it can be checked
                    Object attributeValue = KRADUtils.hydrateAttributeValue(propertyDescriptor.getPropertyType(), value);
                    if (attributeValue == null) {
                        // not a super-awesome fallback strategy, but...
                        attributeValue = value;
                    }
                    if (!ValidationUtils.isNullOrEmpty(attributeValue)) {
                        propertyDescriptor.getWriteMethod().invoke(componentObject, attributeValue);
                    }
                    return validateDataDictionaryAttribute(attr.getKimTypeId(),
                            attr.getKimAttribute().getComponentName(), componentObject, propertyDescriptor);
                }
            }
        } catch (final Exception e) {
            throw new KimTypeAttributeValidationException(e);
        }
        return Collections.emptyList();
    }

    protected List<AttributeError> validatePrimitiveFromDescriptor(
            final String kimTypeId, final String entryName,
            final Object object, final PropertyDescriptor propertyDescriptor) {
        final List<AttributeError> errors = new ArrayList<>();
        // validate the primitive attributes if defined in the dictionary
        if (null != propertyDescriptor && getDataDictionaryService().isAttributeDefined(entryName,
                propertyDescriptor.getName())) {
            final Object value = ObjectUtils.getPropertyValue(object, propertyDescriptor.getName());
            final Class<?> propertyType = propertyDescriptor.getPropertyType();

            if (TypeUtils.isStringClass(propertyType) || TypeUtils.isIntegralClass(propertyType)
                    || TypeUtils.isDecimalClass(propertyType) || TypeUtils.isTemporalClass(propertyType)) {
                // check value format against dictionary
                if (value != null && StringUtils.isNotBlank(value.toString())) {
                    if (!TypeUtils.isTemporalClass(propertyType)) {
                        errors.addAll(validateAttributeFormat(kimTypeId, entryName, propertyDescriptor.getName(),
                                value.toString(), propertyDescriptor.getName()));
                    }
                } else {
                    // if it's blank, then we check whether the attribute should be required
                    errors.addAll(validateAttributeRequired(kimTypeId, entryName, propertyDescriptor.getName(), value,
                            propertyDescriptor.getName()));
                }
            }
        }
        return errors;
    }

    protected Pattern getAttributeValidatingExpression(final KimAttributeField definition) {
        return ANY_CHAR_PATTERN;
    }

    protected Formatter getAttributeFormatter(final KimAttributeField definition) {
        if (definition.getAttributeField().getDataType() == null) {
            return null;
        }

        return Formatter.getFormatter(definition.getAttributeField().getDataType().getType());
    }

    protected Double getAttributeMinValue(final KimAttributeField definition) {
        if (definition == null || definition.getAttributeField() == null) {
            return null;
        }

        final String exclusiveMin = definition.getAttributeField().getExclusiveMin();
        if (exclusiveMin != null) {
            try {
                return Double.valueOf(exclusiveMin);
            } catch (final NumberFormatException e) {
                // quash; this indicates that the DD contained a min for a non-numeric attribute
            }
        }

        return null;
    }

    protected Double getAttributeMaxValue(final KimAttributeField definition) {
        if (definition == null || definition.getAttributeField() == null) {
            return null;
        }

        final String inclusiveMax = definition.getAttributeField().getInclusiveMax();
        if (inclusiveMax != null) {
            try {
                return Double.valueOf(inclusiveMax);
            } catch (final NumberFormatException e) {
                // quash; this indicates that the DD contained a min for a non-numeric attribute
            }
        }

        return null;
    }

    protected List<AttributeError> validateAttributeFormat(
            final String kimTypeId, final String objectClassName,
            final String attributeName, final String attributeValue, final String errorKey) {
        final List<AttributeError> errors = new ArrayList<>();

        final List<KimAttributeField> attributeDefinitions = getAttributeDefinitions(kimTypeId);
        final KimAttributeField definition = DataDictionaryTypeServiceHelper.findAttributeField(attributeName,
            attributeDefinitions);

        final String errorLabel = DataDictionaryTypeServiceHelper.getAttributeErrorLabel(definition);

        LOG.debug("(bo, attributeName, attributeValue) = ({},{},{})",
                objectClassName,
                attributeName,
                attributeValue
        );

        if (StringUtils.isNotBlank(attributeValue)) {
            final Integer maxLength = definition.getAttributeField().getMaxLength();
            if (maxLength != null && maxLength < attributeValue.length()) {
                errors.add(AttributeError.Builder.create(errorKey, DataDictionaryTypeServiceHelper
                    .createErrorString(KFSKeyConstants.ERROR_MAX_LENGTH, errorLabel, maxLength.toString())).build());
                return errors;
            }
            final Double min = getAttributeMinValue(definition);
            if (min != null) {
                try {
                    if (Double.parseDouble(attributeValue) < min) {
                        errors.add(AttributeError.Builder.create(errorKey, DataDictionaryTypeServiceHelper
                            .createErrorString(ERROR_INCLUSIVE_MIN, errorLabel, min.toString())).build());
                        return errors;
                    }
                } catch (final NumberFormatException e) {
                    // quash; this indicates that the DD contained a min for a non-numeric attribute
                }
            }
            final Double max = getAttributeMaxValue(definition);
            if (max != null) {
                try {

                    if (Double.parseDouble(attributeValue) > max) {
                        errors.add(AttributeError.Builder.create(errorKey, DataDictionaryTypeServiceHelper
                            .createErrorString(KFSKeyConstants.ERROR_INCLUSIVE_MAX, errorLabel, max.toString())).build());
                        return errors;
                    }
                } catch (final NumberFormatException e) {
                    // quash; this indicates that the DD contained a max for a non-numeric attribute
                }
            }
            //Cornell Modification: KFSPTS-27121 Start
            //Role qualifiers have their attributes defined to be of type String. The transactional system is presuming
            //the fromAmount and toAmount role qualifier values entered by the users for money amount comparisons have
            //been validated by the system and only contain numerals or a period that can be converted to a KualiDecimal
            //value during downstream processing. This check will present an error on the Person document when the user
            //attempts to "Add" an invalid from or to amount role qualifier.
            if (errors.isEmpty() && StringUtils.isNotBlank(attributeName) && StringUtils.isNotBlank(attributeValue)
                    && StringUtils.equalsAny(attributeName, KimAttributes.FROM_AMOUNT, KimAttributes.TO_AMOUNT)) {
                try {
                    KualiDecimal moneyAmount = new KualiDecimal(attributeValue);
                } catch (NumberFormatException nfe) {
                    errors.add(AttributeError.Builder.create(errorKey, DataDictionaryTypeServiceHelper
                            .createErrorString(KFSKeyConstants.ERROR_CURRENCY, errorLabel)).build());
                }
            }
            //Cornell Modification: KFSPTS-27121 End
        }
        return errors;
    }

    /*
     * will create a list of errors in the following format:
     *
     *
     * error_key:param1;param2;param3;
     */
    protected List<String> extractErrorsFromGlobalVariablesErrorMap(final String attributeName) {
        final AutoPopulatingList<ErrorMessage> errorList = GlobalVariables.getMessageMap()
                .getErrorMessagesForProperty(attributeName);
        final List<String> errors = new ArrayList<>();
        if (errorList != null) {
            for (final ErrorMessage errorMessage : errorList) {
                errors.add(DataDictionaryTypeServiceHelper.createErrorString(errorMessage.getErrorKey(),
                    errorMessage.getMessageParameters()));
            }
        }
        GlobalVariables.getMessageMap().removeAllErrorMessagesForProperty(attributeName);
        return errors;
    }

    protected List<AttributeError> validateNonDataDictionaryAttribute(
            final KimTypeAttribute attr, final String key,
                                                                      final String value) {
        return Collections.emptyList();
    }

    /**
     * @param namespaceCode
     * @param typeAttribute
     * @return an AttributeDefinition for the given KimTypeAttribute, or null no base AttributeDefinition
     * matches the typeAttribute parameter's attributeName.
     */
    protected KimAttributeField getDataDictionaryAttributeDefinition(
            final String namespaceCode, final String kimTypeId,
            final KimTypeAttribute typeAttribute, final List<String> uniqueAttributes) {
        final String componentClassName = typeAttribute.getKimAttribute().getComponentName();
        final String attributeName = typeAttribute.getKimAttribute().getAttributeName();
        final Class<? extends BusinessObject> componentClass;
        final AttributeDefinition baseDefinition;

        // try to resolve the component name - if not possible - try to pull the definition from the app mediation
        // service
        try {
            if (StringUtils.isNotBlank(componentClassName)) {
                componentClass = (Class<? extends BusinessObject>) Class.forName(componentClassName);
                baseDefinition = getBusinessObjectDictionaryService().getBusinessObjectEntry(componentClassName)
                        .getAttributeDefinition(attributeName);
            } else {
                baseDefinition = null;
                componentClass = null;
            }
        } catch (final ClassNotFoundException ex) {
            throw new KimTypeAttributeException(ex);
        }

        if (baseDefinition == null) {
            return null;
        }

        final QuickFinder qf = createQuickFinder(componentClass, attributeName);

        final KimAttributeField.Builder kimField = KimAttributeField.Builder.create(baseDefinition, qf,
                typeAttribute.getKimAttribute().getId());

        if (uniqueAttributes != null && uniqueAttributes.contains(baseDefinition.getName())) {
            kimField.setUnique(true);
        }

        return kimField.build();
    }

    private QuickFinder createQuickFinder(
            final Class<? extends BusinessObject> componentClass,
            final String attributeName) {
        Field field = FieldUtils.getPropertyField(componentClass, attributeName, false);
        final BusinessObject sampleComponent;
        try {
            sampleComponent = componentClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new KimTypeAttributeException(e);
        }

        field = LookupUtils.setFieldQuickfinder(sampleComponent, attributeName, field,
                Collections.singletonList(attributeName));
        if (StringUtils.isNotBlank(field.getQuickFinderClassNameImpl())) {
            final Class<? extends BusinessObject> lookupClass;
            try {
                lookupClass = (Class<? extends BusinessObject>) Class.forName(field.getQuickFinderClassNameImpl());
            } catch (final ClassNotFoundException e) {
                throw new KimTypeAttributeException(e);
            }

            final String baseLookupUrl = LookupUtils.getBaseLookupUrl(false) + "?methodToCall=start";
            final QuickFinder quickFinder = new QuickFinder(baseLookupUrl, lookupClass.getName());
            quickFinder.setLookupParameters(toMap(field.getLookupParameters()));
            quickFinder.setFieldConversions(toMap(field.getFieldConversions()));
            return quickFinder;
        }
        return null;
    }

    private static Map<String, String> toMap(final String s) {
        if (StringUtils.isBlank(s)) {
            return Collections.emptyMap();
        }
        final Map<String, String> map = new HashMap<>();
        for (final String string : s.split(",")) {
            final String[] keyVal = string.split(":");
            map.put(keyVal[0], keyVal[1]);
        }
        return Collections.unmodifiableMap(map);
    }

    protected KimAttributeField getNonDataDictionaryAttributeDefinition(
            final String namespaceCode, final String kimTypeId,
                                                                        final KimTypeAttribute typeAttribute, final List<String> uniqueAttributes) {
        final AttributeDefinition field = new AttributeDefinition();
        field.setName(typeAttribute.getKimAttribute().getAttributeName());
        field.setLabel(typeAttribute.getKimAttribute().getAttributeLabel());

        //KULRICE-9143 shortLabel must be set for KIM to render attribute
        field.setShortLabel(typeAttribute.getKimAttribute().getAttributeLabel());

        final KimAttributeField.Builder definition = KimAttributeField.Builder.create(field,
                typeAttribute.getKimAttribute().getId());

        if (uniqueAttributes != null && uniqueAttributes.contains(typeAttribute.getKimAttribute().getAttributeName())) {
            definition.setUnique(true);
        }
        return definition.build();
    }

    protected void validateRequiredAttributesAgainstReceived(final Map<String, String> receivedAttributes) {
        // abort if type does not want the qualifiers to be checked
        if (!isCheckRequiredAttributes()) {
            return;
        }
        // abort if the list is empty, no attributes need to be checked
        if (getRequiredAttributes() == null || getRequiredAttributes().isEmpty()) {
            return;
        }
        final List<String> missingAttributes = new ArrayList<>();
        // if attributes are null or empty, they're all missing
        if (receivedAttributes == null || receivedAttributes.isEmpty()) {
            return;
        } else {
            for (final String requiredAttribute : getRequiredAttributes()) {
                if (!receivedAttributes.containsKey(requiredAttribute)) {
                    missingAttributes.add(requiredAttribute);
                }
            }
        }
        if (!missingAttributes.isEmpty()) {
            final StringBuilder errorMessage = new StringBuilder();
            final Iterator<String> attribIter = missingAttributes.iterator();
            while (attribIter.hasNext()) {
                errorMessage.append(attribIter.next());
                if (attribIter.hasNext()) {
                    errorMessage.append(COMMA_SEPARATOR);
                }
            }
            errorMessage.append(" not found in required attributes for this type.");
            throw new KimTypeAttributeValidationException(errorMessage.toString());
        }
    }

    @Override
    public List<AttributeError> validateUniqueAttributes(
            final String kimTypeId, final Map<String, String> newAttributes,
            final Map<String, String> oldAttributes) {
        if (StringUtils.isBlank(kimTypeId)) {
            throw new IllegalArgumentException("kimTypeId was null or blank");
        }

        if (newAttributes == null) {
            throw new IllegalArgumentException("newAttributes was null or blank");
        }

        if (oldAttributes == null) {
            throw new IllegalArgumentException("oldAttributes was null or blank");
        }
        final List<String> uniqueAttributes = getUniqueAttributes(kimTypeId);
        if (uniqueAttributes == null || uniqueAttributes.isEmpty()) {
            return Collections.emptyList();
        } else {
            final List<AttributeError> m = new ArrayList<>();
            if (areAttributesEqual(uniqueAttributes, newAttributes, oldAttributes)) {
                //add all unique attrs to error map
                for (final String a : uniqueAttributes) {
                    m.add(AttributeError.Builder.create(a, KFSKeyConstants.ERROR_DUPLICATE_ENTRY).build());
                }

                return m;
            }
        }
        return Collections.emptyList();
    }

    protected boolean areAttributesEqual(
            final List<String> uniqueAttributeNames, final Map<String, String> aSet1,
            final Map<String, String> aSet2) {
        final StringValueComparator comparator = StringValueComparator.getInstance();
        for (final String uniqueAttributeName : uniqueAttributeNames) {
            final String attrVal1 = getAttributeValue(aSet1, uniqueAttributeName);
            final String attrVal2 = getAttributeValue(aSet2, uniqueAttributeName);
            if (comparator.compare(attrVal1, attrVal2) != 0) {
                return false;
            }
        }
        return true;
    }

    protected String getAttributeValue(final Map<String, String> aSet, final String attributeName) {
        if (StringUtils.isEmpty(attributeName)) {
            return null;
        }
        for (final Map.Entry<String, String> entry : aSet.entrySet()) {
            if (attributeName.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    protected List<String> getUniqueAttributes(final String kimTypeId) {
        final KimType kimType = kimTypeInfoService.getKimType(kimTypeId);
        final List<String> uniqueAttributes = new ArrayList<>();
        if (kimType != null) {
            for (final KimTypeAttribute attributeDefinition : kimType.getAttributeDefinitions()) {
                uniqueAttributes.add(attributeDefinition.getKimAttribute().getAttributeName());
            }
        } else {
            LOG.error("Unable to retrieve a KimTypeInfo for a null kimTypeId in getUniqueAttributes()");
        }
        return Collections.unmodifiableList(uniqueAttributes);
    }

    @Override
    public List<AttributeError> validateUnmodifiableAttributes(
            final String kimTypeId,
            final Map<String, String> originalAttributes, final Map<String, String> newAttributes) {
        if (StringUtils.isBlank(kimTypeId)) {
            throw new IllegalArgumentException("kimTypeId was null or blank");
        }

        if (newAttributes == null) {
            throw new IllegalArgumentException("newAttributes was null or blank");
        }

        if (originalAttributes == null) {
            throw new IllegalArgumentException("oldAttributes was null or blank");
        }
        final List<AttributeError> validationErrors = new ArrayList<>();
        final KimType kimType = kimTypeInfoService.getKimType(kimTypeId);
        final List<String> uniqueAttributes = getUniqueAttributes(kimTypeId);
        for (final String attributeNameKey : uniqueAttributes) {
            final KimTypeAttribute attr = kimType.getAttributeDefinitionByName(attributeNameKey);
            final String mainAttributeValue = getAttributeValue(originalAttributes, attributeNameKey);
            final String delegationAttributeValue = getAttributeValue(newAttributes, attributeNameKey);

            if (!StringUtils.equals(mainAttributeValue, delegationAttributeValue)) {
                validationErrors.add(AttributeError.Builder.create(attributeNameKey,
                        DataDictionaryTypeServiceHelper.createErrorString(ERROR_CANT_BE_MODIFIED,
                                dataDictionaryService.getAttributeLabel(attr.getKimAttribute().getComponentName(),
                                        attributeNameKey))).build());
            }
        }
        return validationErrors;
    }

    protected List<String> getRequiredAttributes() {
        return Collections.emptyList();
    }

    protected boolean isCheckRequiredAttributes() {
        return false;
    }

    protected String getClosestParentDocumentTypeName(
        final DocumentType documentType,
        final Set<String> potentialParentDocumentTypeNames) {
        if (potentialParentDocumentTypeNames == null || documentType == null) {
            return null;
        }
        if (potentialParentDocumentTypeNames.contains(documentType.getName())) {
            return documentType.getName();
        }
        if (documentType.getParentId() == null || documentType.getParentId().equals(documentType.getId())) {
            return null;
        }
        return getClosestParentDocumentTypeName(getDocumentTypeService().getDocumentTypeById(
                documentType.getParentId()), potentialParentDocumentTypeNames);
    }

    private BusinessObjectDictionaryService getBusinessObjectDictionaryService() {
        return KNSServiceLocator.getBusinessObjectDictionaryService();
    }

    protected BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    protected DataDictionaryService getDataDictionaryService() {
        if (dataDictionaryService == null) {
            dataDictionaryService = KNSServiceLocator.getDataDictionaryService();
        }
        return dataDictionaryService;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    protected DictionaryValidationService getDictionaryValidationService() {
        if (dictionaryValidationService == null) {
            dictionaryValidationService = KNSServiceLocator.getKNSDictionaryValidationService();
        }
        return dictionaryValidationService;
    }

    public void setDictionaryValidationService(final DictionaryValidationService dictionaryValidationService) {
        this.dictionaryValidationService = dictionaryValidationService;
    }

    protected DocumentTypeService getDocumentTypeService() {
        if (documentTypeService == null) {
            documentTypeService = KEWServiceLocator.getDocumentTypeService();
        }
        return documentTypeService;
    }

    public void setDocumentTypeService(final DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    public void setKimTypeInfoService(final KimTypeInfoService kimTypeInfoService) {
        this.kimTypeInfoService = kimTypeInfoService;
    }

    protected static class KimTypeAttributeValidationException extends RuntimeException {
        private static final long serialVersionUID = 8220618846321607801L;

        protected KimTypeAttributeValidationException(final String message) {
            super(message);
        }

        protected KimTypeAttributeValidationException(final Throwable cause) {
            super(cause);
        }
    }

    protected static class KimTypeAttributeException extends RuntimeException {
        private static final long serialVersionUID = 8220618846321607801L;

        protected KimTypeAttributeException(final String message) {
            super(message);
        }

        protected KimTypeAttributeException(final Throwable cause) {
            super(cause);
        }
    }
}
