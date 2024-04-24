package edu.cornell.kfs.kim.businessobject.options;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.kim.impl.identity.affiliation.EntityAffiliationType;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.KeyValuesService;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.kim.CuKimConstants.KfsAffiliations;

public class AffiliationTypeForListItemValuesFinder extends KeyValuesBase {

    private static final long serialVersionUID = 1L;

    private KeyValuesService keyValuesService;

    @Override
    public List<KeyValue> getKeyValues() {
        Collection<EntityAffiliationType> affiliationTypes = keyValuesService.findAll(EntityAffiliationType.class);
        Stream<KeyValue> emptyKeyValue = Stream.of(
                new ConcreteKeyValue(KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING));
        Stream<KeyValue> affilKeyValues = affiliationTypes.stream()
                .filter(affilType -> !StringUtils.equals(affilType.getCode(), KfsAffiliations.NONE))
                .map(affilType -> new ConcreteKeyValue(affilType.getCode(), affilType.getName()));
        return Stream.concat(emptyKeyValue, affilKeyValues)
                .collect(Collectors.toUnmodifiableList());
    }

    public void setKeyValuesService(KeyValuesService keyValuesService) {
        this.keyValuesService = keyValuesService;
    }

}
