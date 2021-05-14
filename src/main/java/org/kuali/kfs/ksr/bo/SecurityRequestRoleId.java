package org.kuali.kfs.ksr.bo;

import javax.persistence.Column;
import javax.persistence.Id;

import org.kuali.rice.krad.data.jpa.IdClassBase;

/**
 * ====
 * CU Customization:
 * Added this request role ID class as part of
 * the KSR module's JPA conversion.
 * ====
 */
public class SecurityRequestRoleId extends IdClassBase {

    private static final long serialVersionUID = -8833710894738138328L;

    @Id
    @Column(name = "FDOC_NBR")
    private String documentNumber;

    @Id
    @Column(name = "RL_RQ_ID")
    private Long roleRequestId;

    public SecurityRequestRoleId() {
    }

    public SecurityRequestRoleId(String documentNumber, Long roleRequestId) {
        this.documentNumber = documentNumber;
        this.roleRequestId = roleRequestId;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public Long getRoleRequestId() {
        return roleRequestId;
    }

}
