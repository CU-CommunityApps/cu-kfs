package edu.cornell.kfs.ksr.businessobject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

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

}
