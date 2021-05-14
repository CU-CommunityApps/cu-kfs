package org.kuali.kfs.ksr.uif.field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.ksr.bo.SecurityRequestRoleQualification;
import org.kuali.kfs.ksr.bo.SecurityRequestRoleQualificationDetail;
import org.kuali.kfs.ksr.service.KSRServiceLocator;
import org.kuali.kfs.ksr.service.SecurityRequestDocumentService;
import org.kuali.rice.core.api.uif.RemotableAbstractWidget;
import org.kuali.rice.core.api.uif.RemotableAttributeField;
import org.kuali.rice.core.api.uif.RemotableQuickFinder;
import org.kuali.rice.core.api.uif.RemotableTextInput;
import org.kuali.rice.kim.api.common.attribute.KimAttribute;
import org.kuali.rice.kim.api.type.KimAttributeField;
import org.kuali.rice.kim.api.type.KimTypeAttribute;

/**
 * Helper class for creating RemotableAttributeField instances that will be converted into
 * request role qualification detail fields.
 */
public final class KsrRemoteFieldBuilder {

    protected static final String PROPERTY_NAME_FORMAT = "roleQualificationDetails[%s].attributeValue";

    public List<RemotableAttributeField> buildQualificationFields(SecurityRequestRoleQualification roleQualification,
            List<KimTypeAttribute> orderedTypeAttributes) {
        Map<String,Integer> attributeToDetailIndexMap = buildAttributeIdToDetailIndexMap(roleQualification);
        List<RemotableAttributeField> attributeFields = new ArrayList<>();
        List<SecurityRequestRoleQualificationDetail> qualificationDetails = roleQualification.getRoleQualificationDetails();
        
        for (KimTypeAttribute kimTypeAttribute : orderedTypeAttributes) {
            KimAttribute kimAttribute = kimTypeAttribute.getKimAttribute();
            Integer detailIndex = attributeToDetailIndexMap.get(kimAttribute.getId());
            if (detailIndex == null) {
                throw new IllegalStateException("Qualifier row does not contain the KIM attribute " + kimAttribute.getAttributeLabel());
            }
            
            SecurityRequestRoleQualificationDetail qualificationDetail = qualificationDetails.get(detailIndex.intValue());
            KimAttributeField kimAttributeField = qualificationDetail.getAttributeDefinition();
            if (kimAttributeField == null) {
                kimAttributeField = createDefaultKimAttributeField(qualificationDetail);
            }
            
            RemotableAttributeField attributeField = kimAttributeField.getAttributeField();
            RemotableAttributeField.Builder newAttributeField = RemotableAttributeField.Builder.create(attributeField);
            String newPropertyName = String.format(PROPERTY_NAME_FORMAT, detailIndex);
            
            newAttributeField.setName(newPropertyName);
            newAttributeField.setRequired(false);
            updateQuickFinderIfNecessary(newAttributeField, qualificationDetail);
            
            attributeFields.add(newAttributeField.build());
        }
        
        return attributeFields;
    }

    protected Map<String,Integer> buildAttributeIdToDetailIndexMap(SecurityRequestRoleQualification roleQualification) {
        Map<String,Integer> attributeToDetailIndexMap = new HashMap<>();
        int i = 0;
        
        for (SecurityRequestRoleQualificationDetail qualificationDetail : roleQualification.getRoleQualificationDetails()) {
            attributeToDetailIndexMap.put(qualificationDetail.getAttributeId(), Integer.valueOf(i));
            i++;
        }
        
        return attributeToDetailIndexMap;
    }

    protected KimAttributeField createDefaultKimAttributeField(SecurityRequestRoleQualificationDetail qualificationDetail) {
        KimTypeAttribute kimTypeAttribute = qualificationDetail.getKimTypeAttribute();
        KimAttribute kimAttribute = kimTypeAttribute.getKimAttribute();
        final int DEFAULT_SIZE = 15;
        
        String label = kimAttribute.getAttributeLabel();
        if (StringUtils.isBlank(label)) {
            label = kimAttribute.getAttributeName();
        }
        
        RemotableTextInput.Builder textControl = RemotableTextInput.Builder.create();
        textControl.setSize(Integer.valueOf(DEFAULT_SIZE));
        
        RemotableAttributeField.Builder attributeField = RemotableAttributeField.Builder.create(kimAttribute.getAttributeName());
        attributeField.setControl(textControl);
        attributeField.setShortLabel(label);
        attributeField.setLongLabel(label);
        
        KimAttributeField.Builder kimAttributeField = KimAttributeField.Builder.create(attributeField, kimAttribute.getId());
        return kimAttributeField.build();
    }

    protected void updateQuickFinderIfNecessary(RemotableAttributeField.Builder attributeField,
            SecurityRequestRoleQualificationDetail qualificationDetail) {
        Optional<RemotableAbstractWidget.Builder> filteredQuickFinder = attributeField.getWidgets()
                .stream()
                .filter((widget) -> widget instanceof RemotableQuickFinder.Builder)
                .findFirst();
        
        if (filteredQuickFinder.isPresent()) {
            RemotableQuickFinder.Builder quickFinder = (RemotableQuickFinder.Builder) filteredQuickFinder.get();
            updateFieldConversions(quickFinder, qualificationDetail.getAttributeName(), attributeField.getName());
            updateLookupParameters(quickFinder, qualificationDetail.getAttributeName(), attributeField.getName());
        }
    }

    protected void updateFieldConversions(RemotableQuickFinder.Builder quickFinder, String attributeName, String newPropertyName) {
        Map<String,String> oldFieldConversions = quickFinder.getFieldConversions();
        Map<String,String> newFieldConversions = new HashMap<>();
        
        oldFieldConversions.forEach((key, value) -> {
            if (StringUtils.equals(attributeName, key) || StringUtils.equals(attributeName, value)) {
                newFieldConversions.put(key, newPropertyName);
            }
        });
        
        quickFinder.setFieldConversions(newFieldConversions);
    }

    protected void updateLookupParameters(RemotableQuickFinder.Builder quickFinder, String attributeName, String newPropertyName) {
        Map<String,String> oldLookupParameters = quickFinder.getLookupParameters();
        Map<String,String> newLookupParameters = new HashMap<>();
        
        oldLookupParameters.forEach((key, value) -> {
            if (StringUtils.equals(attributeName, key)) {
                newLookupParameters.put(newPropertyName, value);
            }
        });
        
        quickFinder.setLookupParameters(newLookupParameters);
    }

    protected SecurityRequestDocumentService getSecurityRequestDocumentService() {
        return KSRServiceLocator.getSecurityRequestDocumentService();
    }

}
