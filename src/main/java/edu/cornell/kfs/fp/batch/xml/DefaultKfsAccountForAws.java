package edu.cornell.kfs.fp.batch.xml;

import java.util.Date;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.sys.xmladapters.AwsStringToJavaDateTimeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "row", namespace = StringUtils.EMPTY)
public class DefaultKfsAccountForAws {

    @XmlElement(name = "aws_account", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String awsAccount;

    @XmlElement(name = "kfs_default_account", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String kfsDefaultAccount;

    @XmlElement(name = "updated_at", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(AwsStringToJavaDateTimeAdapter.class)
    protected Date updatedAt;

    public String getAwsAccount() {
        return awsAccount;
    }

    public void setAwsAccount(String awsAccount) {
        this.awsAccount = awsAccount;
    }

    public String getKfsDefaultAccount() {
        return kfsDefaultAccount;
    }

    public void setKfsDefaultAccount(String kfsDefaultAccount) {
        this.kfsDefaultAccount = kfsDefaultAccount;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object comparingDefaultKfsAccountForAws) {
        if (comparingDefaultKfsAccountForAws == null) {
            return false;
        }
        if (!(comparingDefaultKfsAccountForAws instanceof DefaultKfsAccountForAws)) {
            return false;
        }
        final DefaultKfsAccountForAws defaultKfsAccountForAws = (DefaultKfsAccountForAws) comparingDefaultKfsAccountForAws;

        return StringUtils.equals(defaultKfsAccountForAws.getKfsDefaultAccount(), kfsDefaultAccount) &&
                StringUtils.equals(defaultKfsAccountForAws.getAwsAccount(), awsAccount) &&
                ObjectUtils.equals(defaultKfsAccountForAws.getUpdatedAt(), updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(awsAccount, kfsDefaultAccount, updatedAt);
    }

}
