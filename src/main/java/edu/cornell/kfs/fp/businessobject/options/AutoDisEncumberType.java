package edu.cornell.kfs.fp.businessobject.options;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

import edu.cornell.kfs.sys.CUKFSConstants;

@SuppressWarnings("serial")
public class AutoDisEncumberType extends KeyValuesBase{
    /**
     * Creates a list of {@link Chart}s using their code as their key, and their code as the display value
     * 
     * @see org.kuali.kfs.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List<KeyValue> getKeyValues() {
        List<KeyValue> chartKeyLabels = new ArrayList<KeyValue>();
        chartKeyLabels.add(new ConcreteKeyValue( "", ""));
        chartKeyLabels.add(new ConcreteKeyValue( CUKFSConstants.PreEncumbranceDocumentConstants.BIWEEKLY, "Bi-Weekly" ));
        chartKeyLabels.add(new ConcreteKeyValue( CUKFSConstants.PreEncumbranceDocumentConstants.MONTHLY, "Monthly" ));
        chartKeyLabels.add(new ConcreteKeyValue( CUKFSConstants.PreEncumbranceDocumentConstants.SEMIMONTHLY, "Semi-Monthly" ));
        chartKeyLabels.add(new ConcreteKeyValue( CUKFSConstants.PreEncumbranceDocumentConstants.CUSTOM, "One Time"));
        return chartKeyLabels;
    }
        
}