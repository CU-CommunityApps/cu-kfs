package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;

import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;

public class CollegeLevelOrganizationValuesFinder extends KeyValuesBase {

    public List getKeyValues() {

        List<KeyLabelPair> keyValues = new ArrayList();

        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
        List<LevelOrganization> cLevelOrganizations = iWantDocumentService.getCLevelOrganizations();

        keyValues.add(new KeyLabelPair("", ""));

        if (cLevelOrganizations != null) {
            for (LevelOrganization cLevelOrganization : cLevelOrganizations) {
                keyValues
                        .add(new KeyLabelPair(cLevelOrganization.getCode(), cLevelOrganization.getCodeAndDescription()));
            }
        }

        return keyValues;
    }

}
