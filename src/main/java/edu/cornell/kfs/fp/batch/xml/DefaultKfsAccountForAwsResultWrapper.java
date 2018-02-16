package edu.cornell.kfs.fp.batch.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "result", namespace = StringUtils.EMPTY)
public class DefaultKfsAccountForAwsResultWrapper {

    @XmlElement(name = "row", namespace = StringUtils.EMPTY, required = true)
    protected List<DefaultKfsAccountForAws> defaultKfsAccountForAws;

    public DefaultKfsAccountForAwsResultWrapper() {
        defaultKfsAccountForAws = new ArrayList<>();
    }

    public List<DefaultKfsAccountForAws> getDefaultKfsAccountForAws() {
        return defaultKfsAccountForAws;
    }

    public void setDefaultKfsAccountForAws(List<DefaultKfsAccountForAws> defaultKfsAccountForAws) {
        this.defaultKfsAccountForAws = defaultKfsAccountForAws;
    }

}
