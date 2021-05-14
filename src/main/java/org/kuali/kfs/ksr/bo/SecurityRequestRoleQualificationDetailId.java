package org.kuali.kfs.ksr.bo;

import javax.persistence.Column;
import javax.persistence.Id;

import org.kuali.rice.krad.data.jpa.IdClassBase;

/**
 * ====
 * CU Customization:
 * Added this qualification detail ID class as part of
 * the KSR module's JPA conversion.
 * ====
 */
public class SecurityRequestRoleQualificationDetailId extends IdClassBase {

    private static final long serialVersionUID = -8728199211694918724L;

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

    public SecurityRequestRoleQualificationDetailId() {
    }

    public SecurityRequestRoleQualificationDetailId(String documentNumber, Long roleRequestId, int qualificationId, String attributeId) {
        this.documentNumber = documentNumber;
        this.roleRequestId = roleRequestId;
        this.qualificationId = qualificationId;
        this.attributeId = attributeId;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public Long getRoleRequestId() {
        return roleRequestId;
    }

    public int getQualificationId() {
        return qualificationId;
    }

    public String getAttributeId() {
        return attributeId;
    }

}
