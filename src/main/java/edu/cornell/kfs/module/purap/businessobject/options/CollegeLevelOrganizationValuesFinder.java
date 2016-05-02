package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;

import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;

public class CollegeLevelOrganizationValuesFinder extends KeyValuesBase {

	private static final long serialVersionUID = 1L;

	public List<KeyValue> getKeyValues() {

        List<KeyValue> keyValues = new ArrayList<KeyValue>();

        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
        List<LevelOrganization> cLevelOrganizations = iWantDocumentService.getCLevelOrganizations();

        keyValues.add(new ConcreteKeyValue("", ""));

        if (cLevelOrganizations != null) {
            for (LevelOrganization cLevelOrganization : cLevelOrganizations) {
                keyValues
                        .add(new ConcreteKeyValue(cLevelOrganization.getCode(), cLevelOrganization.getCodeAndDescription()));
            }
        }

        return keyValues;
    }

}
