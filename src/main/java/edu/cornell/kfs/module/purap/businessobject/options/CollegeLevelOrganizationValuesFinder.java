package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;

public class CollegeLevelOrganizationValuesFinder extends KeyValuesBase {
	private static final long serialVersionUID = 1L;
	protected IWantDocumentService iWantDocumentService;

	public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
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

    public void setiWantDocumentService(IWantDocumentService iWantDocumentService) {
        this.iWantDocumentService = iWantDocumentService;
    }

}
