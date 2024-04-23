package edu.cornell.kfs.krad.datadictionary;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.kim.CuKimPropertyConstants;

public class MaskedPersonAttributeDefinition extends AttributeDefinition {

    private static final long serialVersionUID = 1L;

    private String originalAttributeName;
    private String personAttributeName;

    @Override
    public void afterPropertiesSet() {
        originalAttributeName = getName();
        if (shouldConvertAttributeToPotentiallyMaskedEquivalent()) {
            setName(getName() + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
        }
        super.afterPropertiesSet();
    }

    private boolean shouldConvertAttributeToPotentiallyMaskedEquivalent() {
        return StringUtils.endsWith(getName(), KFSConstants.DELIMITER + personAttributeName);
    }

    public boolean attributeWasConvertedToPotentiallyMaskedEquivalent() {
        return !StringUtils.equals(originalAttributeName, getName());
    }

    public String getOriginalAttributeName() {
        return originalAttributeName;
    }

    public void setOriginalAttributeName(String originalAttributeName) {
        this.originalAttributeName = originalAttributeName;
    }

    public String getPersonAttributeName() {
        return personAttributeName;
    }

    public void setPersonAttributeName(String personAttributeName) {
        this.personAttributeName = personAttributeName;
    }

}
