package edu.cornell.kfs.sec.businessobject.defaultvalue;

import org.kuali.kfs.sec.businessobject.SecurityModelDefinition;
import org.kuali.kfs.sys.businessobject.defaultvalue.SequenceValueFinder;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;

import edu.cornell.kfs.sec.CUSecConstants;

/**
 * Sequence-based values finder that returns the next PK to use for security model definition BOs.
 */
public class CuNextSecurityModelDefinitionIdFinder extends SequenceValueFinder {

    @Override
    public Class<? extends PersistableBusinessObject> getAssociatedClass() {
        return SecurityModelDefinition.class;
    }

    @Override
    public String getSequenceName() {
        return CUSecConstants.SECURITY_MODEL_DEFINITION_ID_SEQUENCE_NAME;
    }

}
