package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiEntityContactGenericUsageBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String usageRowId;
    private String usagePublic;
    private String usageTypeRowId;
    private String usageTypePrimary;
    private String usageType;
    private String useFor;
    private String useForTenanted;
    private String usageComments;

    public String getUsageRowId() {
        return usageRowId;
    }

    public void setUsageRowId(final String usageRowId) {
        this.usageRowId = usageRowId;
    }

    public String getUsagePublic() {
        return usagePublic;
    }

    public void setUsagePublic(final String usagePublic) {
        this.usagePublic = usagePublic;
    }

    public String getUsageTypeRowId() {
        return usageTypeRowId;
    }

    public void setUsageTypeRowId(final String usageTypeRowId) {
        this.usageTypeRowId = usageTypeRowId;
    }

    public String getUsageTypePrimary() {
        return usageTypePrimary;
    }

    public void setUsageTypePrimary(final String usageTypePrimary) {
        this.usageTypePrimary = usageTypePrimary;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(final String usageType) {
        this.usageType = usageType;
    }

    public String getUseFor() {
        return useFor;
    }

    public void setUseFor(final String useFor) {
        this.useFor = useFor;
    }

    public String getUseForTenanted() {
        return useForTenanted;
    }

    public void setUseForTenanted(final String useForTenanted) {
        this.useForTenanted = useForTenanted;
    }

    public String getUsageComments() {
        return usageComments;
    }

    public void setUsageComments(final String usageComments) {
        this.usageComments = usageComments;
    }

}
