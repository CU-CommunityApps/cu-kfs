/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.kuali.kfs.ksr.bo;

import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.api.type.KimAttributeField;
import org.kuali.rice.kim.api.type.KimType;
import org.kuali.rice.kim.api.type.KimTypeAttribute;
import org.kuali.rice.kim.api.type.KimTypeInfoService;
import org.kuali.rice.kim.framework.services.KimFrameworkServiceLocator;
import org.kuali.rice.kim.framework.type.KimTypeService;
import org.kuali.rice.krad.bo.DataObjectBase;

/**
 * ====
 * CU Customization:
 * Remediated this class as needed for Rice 2.x and JPA compatibility.
 * ====
 * 
 * @author rSmart Development Team
 */
@IdClass(SecurityRequestRoleQualificationDetailId.class)
@Entity
@Table(name = "KRSR_SEC_RQ_RL_QUAL_DTL_T")
public class SecurityRequestRoleQualificationDetail extends DataObjectBase {

    private static final long serialVersionUID = -7855669137103025726L;

    @Id
    @Column(name = "FDOC_NBR")
    private String documentNumber;

    @Id
    @Column(name = "RL_RQ_ID")
    private Long roleRequestId;

    @Id
    @Column(name = "QUAL_ID")
    private int qualificationId;

    @Id
    @Column(name = "ATTR_ID")
    private String attributeId;

    @Column(name = "RL_TYP_ID")
    private String roleTypeId;

    @Column(name = "ATTR_VAL")
    private String attributeValue;

    @Transient
    private transient KimType kimType;

    @Transient
    private transient List<KimAttributeField> definitions;

    public List<KimAttributeField> getDefinitions() {
        if (CollectionUtils.isEmpty(definitions)) {
            KimTypeService kimTypeService = getKimTypeService(getKimType());
            if (kimTypeService != null) {
                this.definitions = kimTypeService.getAttributeDefinitions(roleTypeId);
            }
        }

        if (definitions == null) {
            definitions = Collections.emptyList();
        }

        return definitions;
    }

    public KimAttributeField getAttributeDefinition() {
        for (KimAttributeField definition : getDefinitions()) {
            if (StringUtils.equals(definition.getAttributeField().getName(), getAttributeName())) {
                return definition;
            }
        }
        return null;
    }

    public String getAttributeName() {
        KimTypeAttribute attributeInfo = getKimType().getAttributeDefinitionById(attributeId);
        if (attributeInfo != null) {
            return attributeInfo.getKimAttribute().getAttributeName();
        }
        return StringUtils.EMPTY;
    }

    public KimTypeAttribute getKimTypeAttribute() {
        return getKimType().getAttributeDefinitionById(attributeId);
    }

    public KimType getKimType() {
        if (kimType == null || !StringUtils.equals(roleTypeId, kimType.getId())) {
            kimType = getKimTypeInfoService().getKimType(roleTypeId);
        }
        if (kimType == null) {
            throw new IllegalStateException("Could not find KIM type with ID: " + roleTypeId);
        }
        return kimType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Long getRoleRequestId() {
        return roleRequestId;
    }

    public void setRoleRequestId(Long roleRequestId) {
        this.roleRequestId = roleRequestId;
    }

    public int getQualificationId() {
        return qualificationId;
    }

    public void setQualificationId(int qualificationId) {
        this.qualificationId = qualificationId;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getRoleTypeId() {
        return roleTypeId;
    }

    public void setRoleTypeId(String roleTypeId) {
        this.roleTypeId = roleTypeId;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    protected KimTypeInfoService getKimTypeInfoService() {
        return KimApiServiceLocator.getKimTypeInfoService();
    }

    protected KimTypeService getKimTypeService(KimType kimType) {
        return KimFrameworkServiceLocator.getKimTypeService(kimType);
    }

}
