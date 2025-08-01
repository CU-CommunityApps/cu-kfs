package edu.cornell.kfs.sys.batch;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.sys.businessobject.ISOCountry;
import edu.cornell.kfs.sys.exception.ManyISOtoFIPSMappingException;
import edu.cornell.kfs.sys.exception.NoISOtoFIPSMappingException;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;

import java.time.LocalDateTime;
import java.util.Collection;

public class TestConvertingISOtoFIPSStep extends AbstractStep {

    private static final Logger LOG = LogManager.getLogger(TestConvertingISOtoFIPSStep.class);
    
    private BusinessObjectService businessObjectService;
    private ISOFIPSConversionService isoFipsConversionService;

    @Override
    public boolean execute(String jobName, LocalDateTime jobRunDate) {
        
        LOG.info("TestISOFIPSConversionMappingsStep: **** START TEST OF CONVERTING ALL ISO COUNTRY CODES TO CORRESPONDING FIPS COUNTRY CODE ****" + jobRunDate.toString());
        Collection<ISOCountry> allISOCountriesFound = businessObjectService.findAll(ISOCountry.class);
        
        for (ISOCountry eachISOCountry : allISOCountriesFound) {
            try {
                LOG.info("ISO COUNTRY CODE BEING CONVERTED = " + eachISOCountry.getCode());
                String activeFipsCountryCode = isoFipsConversionService.convertISOCountryCodeToActiveFIPSCountryCode(eachISOCountry.getCode());
                LOG.info("FIPS COUNTRY CODE FOUND IN MAPPING = " + activeFipsCountryCode);
                LOG.info("");
            } catch (NoISOtoFIPSMappingException ITFE) {
                //noop: error should have printed during conversion request
            } catch (ManyISOtoFIPSMappingException MITFE) {
                //noop: error should have printed during conversion request
            }
        }
        LOG.info("TestISOFIPSConversionMappingsStep: **** END TEST OF CONVERTING ALL ISO COUNTRY CODES TO CORRESPONDING FIPS COUNTRY CODE ****" + jobRunDate.toString());

        return true;
    }

    public void setIsoFipsConversionService(ISOFIPSConversionService isoFipsConversionService) {
        this.isoFipsConversionService = isoFipsConversionService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
