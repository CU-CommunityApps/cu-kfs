package edu.cornell.kfs.ksr.businessobject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.api.type.KimAttributeField;
import org.kuali.kfs.kim.api.type.KimTypeInfoService;
import org.kuali.kfs.kim.framework.type.KimTypeService;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.impl.type.KimTypeAttribute;
import org.kuali.kfs.kim.service.UiDocumentService;
import org.kuali.kfs.kns.datadictionary.control.TextControlDefinition;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.datadictionary.exporter.StringMap;
import org.kuali.kfs.sys.context.SpringContext;

public class SecurityRequestRoleQualificationDetail extends PersistableBusinessObjectBase {
    private static final long serialVersionUID = -1391866128346133799L;

    private String documentNumber;
    private Long roleRequestId;
    private int qualificationId;
    private String attributeId;
    private String roleTypeId;
    private String attributeValue;

    private transient KimType kimType;
    private transient List<KimAttributeField> definitions;
    private transient Map<String, Object> attributeEntryMap;

    public SecurityRequestRoleQualificationDetail() {
        super();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAttributeEntry() {
        Map<String, Object> attributeEntry = null;

        if (attributeEntryMap == null || attributeEntryMap.isEmpty()) {
            this.attributeEntryMap = SpringContext.getBean(UiDocumentService.class).getAttributeEntries(getDefinitions());
        }

        if ((attributeEntryMap != null) && (attributeEntryMap.containsKey(getAttributeName()))) {
            attributeEntry = (Map<String, Object>) attributeEntryMap.get(getAttributeName());
        } else {
            attributeEntry = getGenericAttributeEntry();
        }

        return attributeEntry;
    }

    protected Map<String, Object> getGenericAttributeEntry() {
        Map<String, Object> attributeEntry = new HashMap<String, Object>();

        KimTypeAttribute attributeInfo = getKimType().getAttributeDefinitionById(attributeId);

        Map<String, Object> controlMap = new StringMap();
        TextControlDefinition textControl = new TextControlDefinition();
        textControl.setSize(15);

        controlMap.put("text", "true");
        controlMap.put("size", textControl.getSize().toString());
        controlMap.put("datePicker", Boolean.valueOf(textControl.isDatePicker()).toString());
        controlMap.put("ranged", Boolean.valueOf(textControl.isRanged()).toString());

        attributeEntry.put("control", controlMap);
        attributeEntry.put("name", attributeInfo.getKimAttribute().getAttributeName());

        String label = attributeInfo.getKimAttribute().getAttributeLabel();
        if (StringUtils.isBlank(label)) {
            label = attributeInfo.getKimAttribute().getAttributeName();
        }

        attributeEntry.put("label", label);
        attributeEntry.put("shortLabel", label);

        return attributeEntry;
    }

    public List<KimAttributeField> getDefinitions() {
        if (definitions == null || definitions.isEmpty()) {
            KimTypeService kimTypeService = SpringContext.getBean(KimTypeService.class);
            if (kimTypeService != null) {
                this.definitions = kimTypeService.getAttributeDefinitions(roleTypeId);
            }
        }

        return this.definitions;
    }

    public KimAttributeField getAttributeDefinition() {
        List<KimAttributeField> definitions = getDefinitions();
        if (definitions != null) {
            for (KimAttributeField definition : definitions) {
                if (StringUtils.equals(definition.getAttributeField().getName(), getAttributeName())) {
                    return definition;
                }
            }
        }

        return null;
    }

    public String getAttributeName() {
        KimTypeAttribute attributeInfo = getKimType().getAttributeDefinitionById(attributeId);
        if (attributeInfo != null) {
            return attributeInfo.getKimAttribute().getAttributeName();
        }

        return "";
    }

    public KimType getKimType() {
        if ((kimType == null) || (!StringUtils.equals(roleTypeId, kimType.getId()))) {
            kimType = SpringContext.getBean(KimTypeInfoService.class).getKimType(roleTypeId);
        }

        return kimType;
    }

    public String getDocumentNumber() {
        return this.documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Long getRoleRequestId() {
        return this.roleRequestId;
    }

    public void setRoleRequestId(Long roleRequestId) {
        this.roleRequestId = roleRequestId;
    }

    public int getQualificationId() {
        return this.qualificationId;
    }

    public void setQualificationId(int qualificationId) {
        this.qualificationId = qualificationId;
    }

    public String getAttributeId() {
        return this.attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getRoleTypeId() {
        return this.roleTypeId;
    }

    public void setRoleTypeId(String roleTypeId) {
        this.roleTypeId = roleTypeId;
    }

    public String getAttributeValue() {
        return this.attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

}
