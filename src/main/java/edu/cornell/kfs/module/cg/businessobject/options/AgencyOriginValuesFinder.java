package edu.cornell.kfs.module.cg.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.KeyValuesService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;

import edu.cornell.kfs.module.cg.businessobject.AgencyOrigin;

public class AgencyOriginValuesFinder extends KeyValuesBase {

    private static final long serialVersionUID = 1L;

    @Override
    public List<KeyValue> getKeyValues() {
        Collection<AgencyOrigin> agencyOrigins = getKeyValuesService().findAllOrderBy(
                AgencyOrigin.class, KFSPropertyConstants.CODE, false);
        List<KeyValue> keyValues = new ArrayList<>();
        keyValues.add(new ConcreteKeyValue(KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING));
        agencyOrigins.stream()
                .map(agencyOrigin -> new ConcreteKeyValue(agencyOrigin.getCode(), agencyOrigin.getName()))
                .forEach(keyValues::add);
        return keyValues;
    }

    protected KeyValuesService getKeyValuesService() {
        return SpringContext.getBean(KeyValuesService.class);
    }

}
