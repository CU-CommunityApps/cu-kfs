package org.kuali.kfs.datadictionary.legacy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.datadictionary.legacy.model.CollectionDefinition;
import org.kuali.kfs.datadictionary.legacy.model.SectionDefinition;
import org.kuali.kfs.kns.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.kns.datadictionary.DocumentEntry;
import org.kuali.kfs.kns.datadictionary.FieldDefinition;
import org.kuali.kfs.kns.datadictionary.InquiryCollectionDefinition;
import org.kuali.kfs.kns.datadictionary.InquiryDefinition;
import org.kuali.kfs.kns.datadictionary.InquirySectionDefinition;
import org.kuali.kfs.kns.datadictionary.LookupDefinition;
import org.kuali.kfs.kns.datadictionary.MaintainableCollectionDefinition;
import org.kuali.kfs.kns.datadictionary.MaintainableFieldDefinition;
import org.kuali.kfs.kns.datadictionary.MaintainableItemDefinition;
import org.kuali.kfs.kns.datadictionary.MaintainableSectionDefinition;
import org.kuali.kfs.kns.datadictionary.MaintenanceDocumentEntry;
import org.kuali.kfs.kns.datadictionary.control.KualiUserControlDefinition;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.DataDictionaryEntry;
import org.kuali.kfs.krad.datadictionary.PrimitiveAttributeDefinition;
import org.kuali.kfs.krad.datadictionary.RelationshipDefinition;
import org.kuali.kfs.krad.datadictionary.SortDefinition;
import org.kuali.kfs.krad.datadictionary.SupportAttributeDefinition;

import edu.cornell.kfs.kim.CuKimPropertyConstants;
import edu.cornell.kfs.krad.datadictionary.MaskedPersonAttributeDefinition;

/*
 * NOTE: This custom class needs to be under the "org.kuali.kfs.*" package location,
 * so that it can access the appropriate package-private DataDictionaryService methods.
 */
@SuppressWarnings("deprecation")
public class CuDataDictionaryService extends DataDictionaryService {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void parseDataDictionaryConfigurationFiles() {
        super.parseDataDictionaryConfigurationFiles();
        adjustPersonAttributeReferencesForMasking();
    }

    private void adjustPersonAttributeReferencesForMasking() {
        LOG.info("adjustPersonAttributeReferencesForMasking, Starting DD adjustments for potential "
                + "Person data masking");
        for (BusinessObjectEntry boEntry : getDataDictionary().getBusinessObjectEntries().values()) {
            adjustBusinessObjectEntryForMasking(boEntry);
        }
        for (DocumentEntry docEntry : getDataDictionary().getDocumentEntries().values()) {
            adjustDocumentEntryForMasking(docEntry);
        }
        LOG.info("adjustPersonAttributeReferencesForMasking, Completed DD adjustments for potential "
                + "Person data masking");
    }

    private boolean adjustBusinessObjectEntryForMasking(BusinessObjectEntry boEntry) {
        Set<String> attributesToMask = this.getPersonAttributesToMask(boEntry);
        if (attributesToMask.isEmpty()) {
            return false;
        }
        boolean madeAdjustments = false;
        // If "attributes" is modified, its setter needs to be called again to reinitialize an internal map.
        if (adjustAttributes(boEntry.getAttributes(), attributesToMask)) {
            boEntry.setAttributes(boEntry.getAttributes());
            madeAdjustments = true;
        }
        madeAdjustments |= adjustDefinitions(boEntry.getRelationships(), attributesToMask,
                this::adjustRelationshipDefinition);
        madeAdjustments |= adjustLookupDefinition(boEntry.getLookupDefinition(), attributesToMask);
        madeAdjustments |= adjustInquiryDefinition(boEntry.getInquiryDefinition(), attributesToMask);

        if (madeAdjustments) {
            LOG.info("adjustBusinessObjectEntryForMasking, Adjusted DD Person-related attributes for object {}",
                    boEntry.getBusinessObjectClass().getSimpleName());
        }
        return madeAdjustments;
    }

    private boolean adjustDocumentEntryForMasking(DocumentEntry docEntry) {
        boolean madeAdjustments = false;
        Set<String> attributesToMask = getPersonAttributesToMask(docEntry);
        if (!attributesToMask.isEmpty()) {
            // If "attributes" is modified, its setter needs to be called again to reinitialize an internal map.
            if (adjustAttributes(docEntry.getAttributes(), attributesToMask)) {
                docEntry.setAttributes(docEntry.getAttributes());
                madeAdjustments = true;
            }
        }
        if (docEntry instanceof MaintenanceDocumentEntry) {
            madeAdjustments |= adjustChildObjectDefinition(
                    (MaintenanceDocumentEntry) docEntry, MaintenanceDocumentEntry::getBusinessObjectClass,
                    this::adjustMaintenanceDocumentEntry);
        }

        if (madeAdjustments) {
            LOG.info("adjustDocumentEntryForMasking, Adjusted DD Person-related attributes for document {}",
                    docEntry.getDocumentTypeName());
        }
        return madeAdjustments;
    }

    private boolean adjustMaintenanceDocumentEntry(MaintenanceDocumentEntry maintEntry, Set<String> attributesToMask) {
        boolean madeAdjustments = false;
        // If "maintainableSections" is modified, its setter needs to be called again to reinitialize an internal map.
        if (adjustDefinitions(maintEntry.getMaintainableSections(), attributesToMask,
                this::adjustMaintainableSection)) {
            maintEntry.setMaintainableSections(maintEntry.getMaintainableSections());
            madeAdjustments = true;
        }
        return madeAdjustments;
    }

    private boolean adjustMaintainableSection(MaintainableSectionDefinition maintainableSection,
            Set<String> attributesToMask) {
        return adjustDefinitions(maintainableSection.getMaintainableItems(), attributesToMask,
                this::adjustMaintainableItem);
    }

    private boolean adjustMaintainableItem(MaintainableItemDefinition maintainableItem, Set<String> attributesToMask) {
        if (maintainableItem instanceof MaintainableFieldDefinition) {
            return adjustMaintainableField((MaintainableFieldDefinition) maintainableItem, attributesToMask);
        } else if (maintainableItem instanceof MaintainableCollectionDefinition) {
            return adjustChildObjectDefinition((MaintainableCollectionDefinition) maintainableItem,
                    MaintainableCollectionDefinition::getBusinessObjectClass, this::adjustMaintainableCollection);
        } else {
            return false;
        }
    }

    private boolean adjustMaintainableFields(List<MaintainableFieldDefinition> maintainableFields,
            Set<String> attributesToMask) {
        return adjustDefinitions(maintainableFields, attributesToMask, this::adjustMaintainableField);
    }

    private boolean adjustMaintainableField(MaintainableFieldDefinition maintainableField,
            Set<String> attributesToMask) {
        boolean madeAdjustments = false;
        String attributeName = StringUtils.defaultString(maintainableField.getName());
        if (attributesToMask.contains(attributeName)) {
            maintainableField.setName(attributeName + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
            madeAdjustments = true;
        }
        attributeName = StringUtils.defaultString(maintainableField.getAlternateDisplayAttributeName());
        if (attributesToMask.contains(attributeName)) {
            maintainableField.setAlternateDisplayAttributeName(
                    attributeName + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
            madeAdjustments = true;
        }
        attributeName = StringUtils.defaultString(maintainableField.getAdditionalDisplayAttributeName());
        if (attributesToMask.contains(attributeName)) {
            maintainableField.setAdditionalDisplayAttributeName(
                    attributeName + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
            madeAdjustments = true;
        }
        return madeAdjustments;
    }

    @SuppressWarnings("unchecked")
    private boolean adjustMaintainableCollection(MaintainableCollectionDefinition maintainableCollection,
            Set<String> attributesToMask) {
        boolean madeAdjustments = false;
        String attributeName = StringUtils.defaultString(
                maintainableCollection.getAttributeToHighlightOnDuplicateKey());
        if (attributesToMask.contains(attributeName)) {
            maintainableCollection.setAttributeToHighlightOnDuplicateKey(
                    maintainableCollection.getAttributeToHighlightOnDuplicateKey()
                            + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
            madeAdjustments = true;
        }
        /*
         * Due to behind-the-scenes attribute map initialization, modifications to the maintainable attributes
         * will require calling the list setters again to reinitialize the maps.
         */
        if (adjustMaintainableFields(maintainableCollection.getMaintainableFields(), attributesToMask)) {
            maintainableCollection.setMaintainableFields(maintainableCollection.getMaintainableFields());
            madeAdjustments = true;
        }
        if (adjustMaintainableFields((List<MaintainableFieldDefinition>) maintainableCollection.getSummaryFields(),
                attributesToMask)) {
            maintainableCollection.setSummaryFields(
                    (List<MaintainableFieldDefinition>) maintainableCollection.getSummaryFields());
            madeAdjustments = true;
        }
        if (adjustMaintainableFields(maintainableCollection.getDuplicateIdentificationFields(), attributesToMask)) {
            maintainableCollection.setDuplicateIdentificationFields(
                    maintainableCollection.getDuplicateIdentificationFields());
            madeAdjustments = true;
        }
        if (adjustChildObjectDefinitions(maintainableCollection.getMaintainableCollections(),
                MaintainableCollectionDefinition::getBusinessObjectClass, this::adjustMaintainableCollection)) {
            maintainableCollection.setMaintainableCollections(maintainableCollection.getMaintainableCollections());
            madeAdjustments = true;
        }
        return madeAdjustments;
    }

    private boolean adjustAttributes(List<AttributeDefinition> attributes, Set<String> attributesToMask) {
        return adjustDefinitions(attributes, attributesToMask, this::adjustAttribute);
    }

    private boolean adjustAttribute(AttributeDefinition attribute, Set<String> attributesToMask) {
        boolean madeAdjustments = false;
        String attributeName = StringUtils.defaultString(attribute.getName());
        if (attributesToMask.contains(attributeName)) {
            attribute.setName(attributeName + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
            madeAdjustments = true;
        }
        if (attribute.getControl() instanceof KualiUserControlDefinition) {
            madeAdjustments |= adjustKualiUserControl(
                    (KualiUserControlDefinition) attribute.getControl(), attributesToMask);
        }
        return madeAdjustments;
    }

    private boolean adjustKualiUserControl(KualiUserControlDefinition control, Set<String> attributesToMask) {
        boolean madeAdjustments = false;
        String attributeName = StringUtils.defaultString(control.getUniversalIdAttributeName());
        if (attributesToMask.contains(attributeName)) {
            control.setUniversalIdAttributeName(attributeName + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
            madeAdjustments = true;
        }
        attributeName = StringUtils.defaultString(control.getUserIdAttributeName());
        if (attributesToMask.contains(attributeName)) {
            control.setUserIdAttributeName(attributeName + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
            madeAdjustments = true;
        }
        attributeName = StringUtils.defaultString(control.getPersonNameAttributeName());
        if (attributesToMask.contains(attributeName)) {
            control.setPersonNameAttributeName(attributeName + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
            madeAdjustments = true;
        }
        return madeAdjustments;
    }

    private boolean adjustRelationshipDefinition(RelationshipDefinition relationship, Set<String> attributesToMask) {
        boolean madeAdjustments = false;
        if (CollectionUtils.isNotEmpty(relationship.getPrimitiveAttributes())) {
            for (PrimitiveAttributeDefinition attribute : relationship.getPrimitiveAttributes()) {
                String sourceName = StringUtils.defaultString(attribute.getSourceName());
                if (attributesToMask.contains(sourceName)) {
                    attribute.setSourceName(sourceName + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
                    attribute.setTargetName(
                            attribute.getTargetName() + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
                    madeAdjustments = true;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(relationship.getSupportAttributes())) {
            for (SupportAttributeDefinition attribute : relationship.getSupportAttributes()) {
                String sourceName = StringUtils.defaultString(attribute.getSourceName());
                if (attributesToMask.contains(sourceName)) {
                    attribute.setSourceName(sourceName + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
                    attribute.setTargetName(
                            attribute.getTargetName() + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
                    madeAdjustments = true;
                }
            }
        }
        return madeAdjustments;
    }

    private boolean adjustLookupDefinition(LookupDefinition lookupDefinition, Set<String> attributesToMask) {
        if (lookupDefinition == null) {
            return false;
        }
        boolean madeAdjustments = adjustSortDefinition(lookupDefinition.getDefaultSort(), attributesToMask);
        /*
         * Due to behind-the-scenes attribute map initialization, modifications to the lookup attributes
         * will require calling the list setters again to reinitialize the maps.
         */
        if (adjustAttributes(lookupDefinition.getFormAttributeDefinitions(), attributesToMask)) {
            lookupDefinition.setFormAttributeDefinitions(lookupDefinition.getFormAttributeDefinitions());
            madeAdjustments = true;
        }
        if (adjustAttributes(lookupDefinition.getDisplayAttributeDefinitions(), attributesToMask)) {
            lookupDefinition.setDisplayAttributeDefinitions(lookupDefinition.getDisplayAttributeDefinitions());
            madeAdjustments = true;
        }
        if (adjustLegacyFieldDefinitions(lookupDefinition.getLookupFields(), attributesToMask)) {
            lookupDefinition.setLookupFields(lookupDefinition.getLookupFields());
            madeAdjustments = true;
        }
        if (adjustLegacyFieldDefinitions(lookupDefinition.getResultFields(), attributesToMask)) {
            lookupDefinition.setResultFields(lookupDefinition.getResultFields());
            madeAdjustments = true;
        }
        return madeAdjustments;
    }

    private boolean adjustSortDefinition(SortDefinition sortDefinition, Set<String> attributesToMask) {
        if (sortDefinition == null || CollectionUtils.isEmpty(sortDefinition.getAttributeNames())) {
            return false;
        }
        boolean madeAdjustments = false;
        List<String> attributeNames = sortDefinition.getAttributeNames();
        if (attributeNames.stream()
                .filter(StringUtils::isNotBlank)
                .anyMatch(attributesToMask::contains)) {
            List<String> adjustedAttributeNames = attributeNames.stream()
                    .map(attributeName -> adjustAttributeNameIfNecessary(attributeName, attributesToMask))
                    .collect(Collectors.toCollection(ArrayList::new));
            sortDefinition.setAttributeNames(adjustedAttributeNames);
            madeAdjustments = true;
        }
        return madeAdjustments;
    }

    private String adjustAttributeNameIfNecessary(String attributeName, Set<String> attributesToMask) {
        return StringUtils.isNotBlank(attributeName) && attributesToMask.contains(attributeName)
                ? attributeName + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX
                : attributeName;
    }

    private boolean adjustInquiryDefinition(InquiryDefinition inquiryDefinition, Set<String> attributesToMask) {
        if (inquiryDefinition == null) {
            return false;
        }
        boolean madeAdjustments = adjustDefinitions(inquiryDefinition.getSections(), attributesToMask,
                this::adjustInquirySection);
        madeAdjustments |= adjustDefinitions(inquiryDefinition.getInquirySections(), attributesToMask,
                this::adjustLegacyInquirySection);
        return madeAdjustments;
    }

    private boolean adjustInquirySection(SectionDefinition inquirySection, Set<String> attributesToMask) {
        boolean madeAdjustments = false;
        madeAdjustments |= adjustAttributes(inquirySection.getFields(), attributesToMask);
        madeAdjustments |= adjustChildObjectDefinitions(inquirySection.getCollectionDefinitions(),
                CollectionDefinition::getBusinessObjectClass, this::adjustInquiryCollection);
        return madeAdjustments;
    }

    private boolean adjustInquiryCollection(CollectionDefinition inquiryCollection, Set<String> attributesToMask) {
        boolean madeAdjustments = false;
        madeAdjustments |= adjustAttributes(inquiryCollection.getKeyAttributes(), attributesToMask);
        madeAdjustments |= adjustAttributes(inquiryCollection.getFields(), attributesToMask);
        return madeAdjustments;
    }

    @SuppressWarnings("unchecked")
    private boolean adjustLegacyInquirySection(InquirySectionDefinition inquirySection, Set<String> attributesToMask) {
        boolean madeAdjustments = false;
        // If "inquiryFields" is modified, its setter needs to be called again to reinitialize an internal map.
        if (adjustLegacyFieldDefinitions(inquirySection.getInquiryFields(), attributesToMask)) {
            inquirySection.setInquiryFields(inquirySection.getInquiryFields());
            madeAdjustments = true;
        }
        Map<?, ?> inquiryCollections = inquirySection.getInquiryCollections();
        if (inquiryCollections != null && !inquiryCollections.isEmpty()) {
            adjustChildObjectDefinitions((Collection<InquiryCollectionDefinition>) inquiryCollections.values(),
                    InquiryCollectionDefinition::getBusinessObjectClass,
                    this::adjustLegacyInquiryCollectionDefinition);
        }
        return madeAdjustments;
    }

    private boolean adjustLegacyInquiryCollectionDefinition(
            InquiryCollectionDefinition inquiryCollection, Set<String> attributesToMask) {
        boolean madeAdjustments = false;
        /*
         * Due to behind-the-scenes attribute map initialization, modifications to the inquiry collection attributes
         * will require calling the list setters again to reinitialize the maps.
         */
        if (adjustLegacyFieldDefinitions(inquiryCollection.getSummaryFields(), attributesToMask)) {
            inquiryCollection.setSummaryFields(inquiryCollection.getSummaryFields());
            madeAdjustments = true;
        }
        if (adjustLegacyFieldDefinitions(inquiryCollection.getInquiryFields(), attributesToMask)) {
            inquiryCollection.setInquiryFields(inquiryCollection.getInquiryFields());
            madeAdjustments = true;
        }
        if (adjustChildObjectDefinitions(inquiryCollection.getInquiryCollections(),
                InquiryCollectionDefinition::getBusinessObjectClass, this::adjustLegacyInquiryCollectionDefinition)) {
            inquiryCollection.setInquiryCollections(inquiryCollection.getInquiryCollections());
            madeAdjustments = true;
        }
        return madeAdjustments;
    }

    private boolean adjustLegacyFieldDefinitions(List<FieldDefinition> fieldDefinitions,
            Set<String> attributesToMask) {
        return adjustDefinitions(fieldDefinitions, attributesToMask, this::adjustLegacyFieldDefinition);
    }

    private boolean adjustLegacyFieldDefinition(FieldDefinition field, Set<String> attributesToMask) {
        if (field instanceof InquiryCollectionDefinition) {
            return adjustChildObjectDefinition(
                    (InquiryCollectionDefinition) field, InquiryCollectionDefinition::getBusinessObjectClass,
                    this::adjustLegacyInquiryCollectionDefinition);
        }
        String attributeName = StringUtils.defaultString(field.getAttributeName());
        if (attributesToMask.contains(attributeName)) {
            field.setAttributeName(attributeName + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
            return true;
        }
        return false;
    }

    private <T> boolean adjustDefinitions(Collection<T> definitions, Set<String> attributesToMask,
            BiPredicate<T, Set<String>> definitionMasker) {
        if (CollectionUtils.isEmpty(definitions)) {
            return false;
        }
        boolean madeAdjustments = false;
        for (T definition : definitions) {
            madeAdjustments |= definitionMasker.test(definition, attributesToMask);
        }
        return madeAdjustments;
    }

    private <T> boolean adjustChildObjectDefinitions(Collection<T> definitions,
            Function<T, Class<? extends BusinessObject>> boClassGetter, BiPredicate<T, Set<String>> definitionMasker) {
        if (CollectionUtils.isEmpty(definitions)) {
            return false;
        }
        boolean madeAdjustments = false;
        for (T definition : definitions) {
            madeAdjustments |= adjustChildObjectDefinition(definition, boClassGetter, definitionMasker);
        }
        return madeAdjustments;
    }

    private <T> boolean adjustChildObjectDefinition(T definition,
            Function<T, Class<? extends BusinessObject>> boClassGetter, BiPredicate<T, Set<String>> definitionMasker) {
        Set<String> attributesToMask = getPersonAttributesToMask(boClassGetter.apply(definition));
        return definitionMasker.test(definition, attributesToMask);
    }

    private Set<String> getPersonAttributesToMask(Class<? extends BusinessObject> businessObjectClass) {
        if (businessObjectClass == null) {
            return Set.of();
        }
        BusinessObjectEntry boEntry = getDataDictionary().getBusinessObjectEntry(businessObjectClass.getName());
        return (boEntry != null) ? getPersonAttributesToMask(boEntry) : Set.of();
    }

    private Set<String> getPersonAttributesToMask(DataDictionaryEntry ddEntry) {
        if (CollectionUtils.isEmpty(ddEntry.getAttributes())) {
            return Set.of();
        }
        return ddEntry.getAttributes().stream()
                .filter(attribute -> attribute instanceof MaskedPersonAttributeDefinition)
                .map(MaskedPersonAttributeDefinition.class::cast)
                .filter(MaskedPersonAttributeDefinition::attributeWasConvertedToPotentiallyMaskedEquivalent)
                .map(MaskedPersonAttributeDefinition::getOriginalAttributeName)
                .collect(Collectors.toUnmodifiableSet());
    }

}
