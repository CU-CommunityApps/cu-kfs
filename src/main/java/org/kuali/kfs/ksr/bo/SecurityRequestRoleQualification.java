/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kuali.kfs.ksr.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.krad.bo.DataObjectBase;

/**
 * ====
 * CU Customization:
 * Remediated this class as needed for Rice 2.x and JPA compatibility.
 * ====
 * 
 * @author rSmart Development Team
 */
@IdClass(SecurityRequestRoleQualificationId.class)
@Entity
@Table(name = "KRSR_SEC_RQ_RL_QUAL_T")
public class SecurityRequestRoleQualification extends DataObjectBase {

    private static final long serialVersionUID = -1233869007626822287L;

    @Id
    @Column(name = "FDOC_NBR")
    private String documentNumber;

    @Id
    @Column(name = "RL_RQ_ID")
    private Long roleRequestId;

    @Id
    @Column(name = "QUAL_ID")
    private int qualificationId;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
        @JoinColumn(name = "FDOC_NBR", referencedColumnName = "FDOC_NBR"),
        @JoinColumn(name = "RL_RQ_ID", referencedColumnName = "RL_RQ_ID"),
        @JoinColumn(name = "QUAL_ID", referencedColumnName = "QUAL_ID")
    })
    private List<SecurityRequestRoleQualificationDetail> roleQualificationDetails;

    public SecurityRequestRoleQualification() {
        roleQualificationDetails = new ArrayList<>();
    }

    /**
     * Builds a Map from the qualification details
     * 
     * @return A Map containing attribute name/value pairs from the qualification details
     */
    public Map<String,String> buildQualificationMap() {
        Map<String,String> qualification = new HashMap<String,String>();

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
