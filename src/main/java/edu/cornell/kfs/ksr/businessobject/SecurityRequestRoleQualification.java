package edu.cornell.kfs.ksr.businessobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.impl.type.KimTypeAttribute;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.ksr.util.KSRUtil;

public class SecurityRequestRoleQualification extends PersistableBusinessObjectBase {
    private static final long serialVersionUID = -8781959491437530198L;

    private String documentNumber;
    private Long roleRequestId;
    private int qualificationId;

    private List<SecurityRequestRoleQualificationDetail> roleQualificationDetails;

    public SecurityRequestRoleQualification() {
        super();

        roleQualificationDetails = new ArrayList<SecurityRequestRoleQualificationDetail>();
    }

    public Map<String, String> buildQualificationAttributeSet() {
        Map<String, String> qualification = new HashMap<String, String>();

        for (SecurityRequestRoleQualificationDetail qualificationDetail : getRoleQualificationDetails()) {
            if (StringUtils.isNotBlank(qualificationDetail.getAttributeValue())) {
                qualification.put(qualificationDetail.getAttributeName(), qualificationDetail.getAttributeValue());
            }
        }

        return qualification;
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

    public List<SecurityRequestRoleQualificationDetail> getRoleQualificationDetails() {
        return this.roleQualificationDetails;
    }

    public void setRoleQualificationDetails(List<SecurityRequestRoleQualificationDetail> roleQualificationDetails) {
        this.roleQualificationDetails = roleQualificationDetails;
    }

    public List<WrappedRoleQualificationDetail> getWrappedRoleQualificationDetails() {
        KimType kimType = getKimTypeFromQualificationDetails();
        if (ObjectUtils.isNull(kimType)) {
            return Collections.emptyList();
        }
        
        Map<String, KimTypeAttribute> typeAttributes = KSRUtil.getTypeAttributesMappedByAttributeId(kimType);
        Stream.Builder<WrappedRoleQualificationDetail> wrappedDetails = Stream.builder();
        int i = 0;
        for (SecurityRequestRoleQualificationDetail roleQualificationDetail : roleQualificationDetails) {
            KimTypeAttribute typeAttribute = typeAttributes.get(roleQualificationDetail.getAttributeId());
            if (ObjectUtils.isNull(typeAttribute)) {
                throw new IllegalStateException("Could not find attribute " + roleQualificationDetail.getAttributeId()
                        + " for KIM type " + kimType.getNamespaceCode() + " " + kimType.getName());
            }
            wrappedDetails.add(new WrappedRoleQualificationDetail(
                    roleQualificationDetail, i, typeAttribute.getSortCode()));
            i++;
        }
        
        return wrappedDetails.build()
                .sorted(Comparator.comparing(WrappedRoleQualificationDetail::getSortCode))
                .collect(Collectors.toUnmodifiableList());
    }

    private KimType getKimTypeFromQualificationDetails() {
        if (CollectionUtils.isNotEmpty(roleQualificationDetails)) {
            return roleQualificationDetails.get(0).getKimType();
        }
        return null;
    }

}
