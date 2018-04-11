package edu.cornell.kfs.fp.batch.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "result", namespace = StringUtils.EMPTY)
public class DefaultKfsAccountForAwsResultWrapper {

    @XmlElement(name = "row", namespace = StringUtils.EMPTY, required = true)
    protected List<DefaultKfsAccountForAws> defaultKfsAccountsForAws;

    public DefaultKfsAccountForAwsResultWrapper() {
        defaultKfsAccountsForAws = new ArrayList<>();
    }

    public List<DefaultKfsAccountForAws> getDefaultKfsAccountsForAws() {
        return defaultKfsAccountsForAws;
    }

    public void setDefaultKfsAccountsForAws(List<DefaultKfsAccountForAws> defaultKfsAccountsForAws) {
        this.defaultKfsAccountsForAws = defaultKfsAccountsForAws;
    }

    @Override
    public boolean equals(Object comparingDefaultKfsAccountForAwsWrapper) {
        if (comparingDefaultKfsAccountForAwsWrapper == null) {
            return false;
        }
        if (!(comparingDefaultKfsAccountForAwsWrapper instanceof DefaultKfsAccountForAwsResultWrapper)) {
            return false;
        }
        final DefaultKfsAccountForAwsResultWrapper defaultKfsAccountForAwsWrapper = (DefaultKfsAccountForAwsResultWrapper) comparingDefaultKfsAccountForAwsWrapper;

        return CollectionUtils.isEqualCollection(defaultKfsAccountForAwsWrapper.getDefaultKfsAccountsForAws(), defaultKfsAccountsForAws);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultKfsAccountsForAws);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DefaultKfsAccountForAwsResultWrapper {");
        String joinedStringOutput = defaultKfsAccountsForAws.stream()
                .map(acct -> acct.toString())
                .collect(Collectors.joining("] [", "[", "]"));
        sb.append(joinedStringOutput).append("}");
        return sb.toString();
    }

}
