package edu.cornell.kfs.fp.businessobject.options;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;
import org.kuali.rice.kns.service.KeyValuesService;
import edu.cornell.kfs.sys.CUKFSConstants;
public class AutoDisEncumberType extends KeyValuesBase{
    /**
     * Creates a list of {@link Chart}s using their code as their key, and their code as the display value
     * 
     * @see org.kuali.rice.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {
        List chartKeyLabels = new ArrayList();
        chartKeyLabels.add(new KeyLabelPair( "", ""));
        chartKeyLabels.add(new KeyLabelPair( CUKFSConstants.PreEncumbranceDocumentConstants.BIWEEKLY, "Bi-Weekly" ));
        chartKeyLabels.add(new KeyLabelPair( CUKFSConstants.PreEncumbranceDocumentConstants.MONTHLY, "Monthly" ));
        chartKeyLabels.add(new KeyLabelPair( CUKFSConstants.PreEncumbranceDocumentConstants.SEMIMONTHLY, "Semi-Monthly" ));
        chartKeyLabels.add(new KeyLabelPair( CUKFSConstants.PreEncumbranceDocumentConstants.CUSTOM, "One Time"));
        return chartKeyLabels;
    }
        
}