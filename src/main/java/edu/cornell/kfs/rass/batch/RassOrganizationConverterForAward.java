package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.module.cg.businessobject.AwardOrganization;

import edu.cornell.kfs.rass.RassConstants;

public class RassOrganizationConverterForAward extends RassValueConverterBase {

    @Override
    public Object convert(Class<? extends PersistableBusinessObject> businessObjectClass, RassPropertyDefinition propertyMapping, Object propertyValue) {
        String organizationCode = (String) propertyValue;
        AwardOrganization awardOrganization = new AwardOrganization();
        awardOrganization.setChartOfAccountsCode(RassConstants.PROPOSAL_ORG_CHART);
        awardOrganization.setOrganizationCode(organizationCode);
        awardOrganization.setAwardPrimaryOrganizationIndicator(true);
        List<AwardOrganization> awardOrganizations = new ArrayList<>();
        awardOrganizations.add(awardOrganization);
        return awardOrganizations;
    }

}
