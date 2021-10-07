package edu.cornell.kfs.ksr.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class WrappedRoleQualificationDetail extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1104909772812511157L;

    private SecurityRequestRoleQualificationDetail roleQualificationDetail;
    private int detailIndex;
    private String sortCode;

    public WrappedRoleQualificationDetail() {}

    public WrappedRoleQualificationDetail(SecurityRequestRoleQualificationDetail roleQualificationDetail,
            int detailIndex, String sortCode) {
        this.roleQualificationDetail = roleQualificationDetail;
        this.detailIndex = detailIndex;
        this.sortCode = sortCode;
    }

    public SecurityRequestRoleQualificationDetail getRoleQualificationDetail() {
        return roleQualificationDetail;
    }

    public void setRoleQualificationDetail(SecurityRequestRoleQualificationDetail roleQualificationDetail) {
        this.roleQualificationDetail = roleQualificationDetail;
    }

    public int getDetailIndex() {
        return detailIndex;
    }

    public void setDetailIndex(int detailIndex) {
        this.detailIndex = detailIndex;
    }

    public String getSortCode() {
        return sortCode;
    }

    public void setSortCode(String sortCode) {
        this.sortCode = sortCode;
    }

}
