package edu.cornell.kfs.sec.businessobject.defaultvalue;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.valuefinder.DefaultValueFinder;
import org.kuali.kfs.sec.businessobject.SecurityModelDefinition;

import edu.cornell.kfs.sec.CUSecConstants;

/**
 * Sequence-based values finder that returns the next PK to use for security model definition BOs.
 */
// TODO: SequenceValueFinder no longer exists in KFS. Determine if we still need this customization!
public class CuNextSecurityModelDefinitionIdFinder /*extends SequenceValueFinder*/ implements DefaultValueFinder {

    @Override
    public String getDefaultValue() {
        // TODO: Implement this method or re-define SequenceValueFinder appropriately!
        return null;
    }

    //@Override
    public Class<? extends PersistableBusinessObject> getAssociatedClass() {
        return SecurityModelDefinition.class;
    }

    //@Override
    public String getSequenceName() {
        return CUSecConstants.SECURITY_MODEL_DEFINITION_ID_SEQUENCE_NAME;
    }

}
