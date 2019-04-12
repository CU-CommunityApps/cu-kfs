package edu.cornell.kfs.rass.batch.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.service.AgencyService;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.module.cg.CuCGPropertyConstants;
import edu.cornell.kfs.module.cg.businessobject.AgencyExtendedAttribute;

public class AgencyTranslationDefinition extends RassObjectTranslationDefinition<RassXmlAgencyEntry, Agency> {

    private AgencyService agencyService;

    public void setAgencyService(AgencyService agencyService) {
        this.agencyService = agencyService;
    }

    @Override
    public Class<RassXmlAgencyEntry> getXmlObjectClass() {
        return RassXmlAgencyEntry.class;
    }

    @Override
    public Class<Agency> getBusinessObjectClass() {
        return Agency.class;
    }

    @Override
    public String printPrimaryKeyValues(RassXmlAgencyEntry xmlAgency) {
        return xmlAgency.getNumber();
    }

    @Override
    public String printPrimaryKeyValues(Agency agency) {
        return agency.getAgencyNumber();
    }

    @Override
    public List<Pair<Class<?>, String>> getListOfObjectUpdatesToWaitFor(RassXmlAgencyEntry xmlAgency) {
        List<Pair<Class<?>, String>> objectsToWaitFor = new ArrayList<>();
        objectsToWaitFor.add(
                Pair.of(Agency.class, xmlAgency.getNumber()));
        if (StringUtils.isNotBlank(xmlAgency.getReportsToAgencyNumber())) {
            objectsToWaitFor.add(
                    Pair.of(Agency.class, xmlAgency.getReportsToAgencyNumber()));
        }
        return objectsToWaitFor;
    }

    @Override
    public Agency findExistingObject(RassXmlAgencyEntry xmlAgency) {
        if (StringUtils.isBlank(xmlAgency.getNumber())) {
            throw new RuntimeException("Attempted to search for an Agency with a blank Agency Number");
        }
        return agencyService.getByPrimaryId(xmlAgency.getNumber());
    }

    @Override
    public void processCustomTranslationForBusinessObjectEdit(
            RassXmlAgencyEntry xmlAgency, Agency oldAgency, Agency newAgency) {
        newAgency.refreshReferenceObject(KFSPropertyConstants.AGENCY_TYPE);
        newAgency.refreshReferenceObject(KFSPropertyConstants.REPORTS_TO_AGENCY);
        AgencyExtendedAttribute agencyExtension = (AgencyExtendedAttribute) newAgency.getExtension();
        agencyExtension.refreshReferenceObject(CuCGPropertyConstants.AGENCY_ORIGIN);
    }

}
