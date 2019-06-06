package edu.cornell.kfs.fp.batch.businessobject;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.sys.KFSConstants;

public class AmazonKfsAccountDTO {
    
    private String amazonAccoutNumber;
    private String costCenterOrKfsDefaultAccountForAWSAccount;
    private String kfsChart;
    private String kfsAccount;
    private String kfsSubAccount;
    private String kfsObject;
    private String kfsSubObject;
    private String kfsProject;
    private String kfsOrgRefId;
    
    private AmazonKfsAccountDTO() {
        this.amazonAccoutNumber = StringUtils.EMPTY;
        this.costCenterOrKfsDefaultAccountForAWSAccount = StringUtils.EMPTY;
        this.kfsChart = StringUtils.EMPTY;
        this.kfsAccount = StringUtils.EMPTY;
        this.kfsSubAccount = StringUtils.EMPTY;
        this.kfsObject = StringUtils.EMPTY;
        this.kfsSubObject = StringUtils.EMPTY;
        this.kfsProject = StringUtils.EMPTY;
        this.kfsOrgRefId = StringUtils.EMPTY;
    }
    
    public AmazonKfsAccountDTO(String amazonAccountNumber, String costCenterOrKfsDefaultAccountForAWSAccount, String defaultKfsChart, String defaultKfsObject) {
        this();
        this.amazonAccoutNumber = normalizeString(amazonAccountNumber);
        this.costCenterOrKfsDefaultAccountForAWSAccount = normalizeString(costCenterOrKfsDefaultAccountForAWSAccount);
        if (StringUtils.countMatches(costCenterOrKfsDefaultAccountForAWSAccount, KFSConstants.WILDCARD_CHARACTER) == 6) {
            String[] fullAccount = StringUtils.splitByWholeSeparatorPreserveAllTokens(costCenterOrKfsDefaultAccountForAWSAccount, KFSConstants.WILDCARD_CHARACTER);
            this.kfsChart = normalizeString(fullAccount[0]);
            this.kfsAccount = normalizeString(fullAccount[1]);
            this.kfsSubAccount = normalizeString(fullAccount[2]);
            this.kfsObject = normalizeString(fullAccount[3]);
            this.kfsSubObject = normalizeString(fullAccount[4]);
            this.kfsProject = normalizeString(fullAccount[5]);
            this.kfsOrgRefId = normalizeString(fullAccount[6]);
        } else if (StringUtils.countMatches(costCenterOrKfsDefaultAccountForAWSAccount, KFSConstants.DASH) == 1) {
            String[] accountSubAccount = StringUtils.splitByWholeSeparatorPreserveAllTokens(costCenterOrKfsDefaultAccountForAWSAccount, KFSConstants.DASH);
            this.kfsAccount = normalizeString(accountSubAccount[0]);
            this.kfsSubAccount = normalizeString(accountSubAccount[1]);
            setDefaultValues(defaultKfsChart, defaultKfsObject);
        } else {
            this.kfsAccount = normalizeString(costCenterOrKfsDefaultAccountForAWSAccount);
            setDefaultValues(defaultKfsChart, defaultKfsObject);
        }
    }
    
    private String normalizeString(String stringValue) {
        if (StringUtils.isNotBlank(stringValue)) {
            return StringUtils.trim(stringValue);
        } else {
            return StringUtils.EMPTY;
        }
    }
    
    private void setDefaultValues(String defaultKfsChart, String defaultKfsObject) {
        this.kfsChart = defaultKfsChart;
        this.kfsObject = defaultKfsObject;
    }

    public String getAmazonAccoutNumber() {
        return amazonAccoutNumber;
    }

    public String getCostCenterOrKfsDefaultAccountForAWSAccount() {
        return costCenterOrKfsDefaultAccountForAWSAccount;
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
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof AmazonKfsAccountDTO) {
            AmazonKfsAccountDTO other = (AmazonKfsAccountDTO) o;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(amazonAccoutNumber, other.getAmazonAccoutNumber());
            eb.append(costCenterOrKfsDefaultAccountForAWSAccount, other.getCostCenterOrKfsDefaultAccountForAWSAccount());
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
        return Objects.hash(amazonAccoutNumber, costCenterOrKfsDefaultAccountForAWSAccount, kfsChart, kfsAccount, kfsSubAccount, kfsObject, kfsSubObject, kfsProject, kfsOrgRefId);
    }

}
