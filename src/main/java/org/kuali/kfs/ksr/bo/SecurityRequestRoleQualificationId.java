package org.kuali.kfs.ksr.bo;

import javax.persistence.Column;
import javax.persistence.Id;

import org.kuali.rice.krad.data.jpa.IdClassBase;

/**
 * ====
 * CU Customization:
 * Added this qualification ID class as part of
 * the KSR module's JPA conversion.
 * ====
 */
public class SecurityRequestRoleQualificationId extends IdClassBase {

    private static final long serialVersionUID = 1648838413194694021L;

    @Id
    @Column(name = "FDOC_NBR")
    private String documentNumber;

    @Id
    @Column(name = "RL_RQ_ID")
    private Long roleRequestId;

    @Id
    @Column(name = "QUAL_ID")
    private int qualificationId;

    public SecurityRequestRoleQualificationId() {
    }

    public SecurityRequestRoleQualificationId(String documentNumber, Long roleRequestId, int qualificationId) {
        this.documentNumber = documentNumber;
        this.roleRequestId = roleRequestId;
        this.qualificationId = qualificationId;
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

}
