package edu.cornell.kfs.fp.batch.businessobject;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kuali.kfs.sys.KFSConstants;

public class AmazonKfsAccountDTO {
    
    private String amazonAccoutNumber;
    private String costCenter;
    private String kfsChart;
    private String kfsAccount;
    private String kfsSubAccount;
    private String kfsObject;
    private String kfsSubObject;
    private String kfsProject;
    private String kfsOrgRefId;
    
    public AmazonKfsAccountDTO(String amazonAccountNumber, String costCenter, String defaultKfsChart, String defaultKfsObject) {
        this.amazonAccoutNumber = amazonAccountNumber;
        this.costCenter = costCenter;
        if (StringUtils.countMatches(costCenter, KFSConstants.WILDCARD_CHARACTER) == 6) {
            String[] fullAccount = StringUtils.splitByWholeSeparatorPreserveAllTokens(costCenter, KFSConstants.WILDCARD_CHARACTER);
            this.kfsChart = fullAccount[0];
            this.kfsAccount = fullAccount[1];
            this.kfsSubAccount = fullAccount[2];
            this.kfsObject = fullAccount[3];
            this.kfsSubObject = fullAccount[4];
            this.kfsProject = fullAccount[5];
            this.kfsOrgRefId = fullAccount[6];
        } else if (StringUtils.countMatches(costCenter, KFSConstants.DASH) == 1) {
            String[] accountSubAccount = StringUtils.splitByWholeSeparatorPreserveAllTokens(costCenter, KFSConstants.DASH);
            this.kfsAccount = accountSubAccount[0];
            this.kfsSubAccount = accountSubAccount[1];
            setDefaultChartObject(defaultKfsChart, defaultKfsObject);
        } else {
            this.kfsAccount = costCenter;
            setDefaultChartObject(defaultKfsChart, defaultKfsObject);
        }
    }
    
    private void setDefaultChartObject(String defaultKfsChart, String defaultKfsObject) {
        this.kfsChart = defaultKfsChart;
        this.kfsObject = defaultKfsObject;
    }

    public String getAmazonAccoutNumber() {
        return amazonAccoutNumber;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public String getKfsChart() {
        return kfsChart;
    }

    public String getKfsAccount() {
        return kfsAccount;
    }

    public String getKfsSubAccount() {
        return kfsSubAccount;
    }

    public String getKfsObject() {
        return kfsObject;
    }

    public String getKfsSubObject() {
        return kfsSubObject;
    }

    public String getKfsProject() {
        return kfsProject;
    }

    public String getKfsOrgRefId() {
        return kfsOrgRefId;
    }
    
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof AmazonKfsAccountDTO) {
            AmazonKfsAccountDTO other = (AmazonKfsAccountDTO)o;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(amazonAccoutNumber, other.getAmazonAccoutNumber());
            eb.append(costCenter, other.getCostCenter());
            eb.append(kfsChart, other.getKfsChart());
            eb.append(kfsAccount, other.getKfsAccount());
            eb.append(kfsSubAccount, other.getKfsSubAccount());
            eb.append(kfsObject, other.getKfsObject());
            eb.append(kfsSubObject, other.getKfsSubObject());
            eb.append(kfsProject, other.getKfsProject());
            eb.append(kfsOrgRefId, other.getKfsOrgRefId());
            return eb.isEquals();
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amazonAccoutNumber, costCenter, kfsChart, kfsAccount, kfsSubAccount, kfsObject, kfsSubObject, kfsProject, kfsOrgRefId);
    }

}
