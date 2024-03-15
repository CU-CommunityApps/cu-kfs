package edu.cornell.kfs.kim.businessobject.options;

import java.util.List;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.kim.CuKimConstants.AffiliationStatuses;

public class AffiliationStatusValuesFinder extends KeyValuesBase {
    private static final long serialVersionUID = 1L;

    @Override
    public List<KeyValue> getKeyValues() {
        return List.of(
                new ConcreteKeyValue(KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING),
                new ConcreteKeyValue(AffiliationStatuses.ACTIVE, "Active"),
                new ConcreteKeyValue(AffiliationStatuses.INACTIVE, "Inactive"),
                new ConcreteKeyValue(AffiliationStatuses.RETIRED, "Retired")
        );
    }

}
