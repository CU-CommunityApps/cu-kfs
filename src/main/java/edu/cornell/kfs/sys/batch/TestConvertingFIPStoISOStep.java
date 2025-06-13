package edu.cornell.kfs.sys.batch;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.businessobject.Country;

import edu.cornell.kfs.sys.exception.ManyFIPStoISOMappingException;
import edu.cornell.kfs.sys.exception.NoFIPStoISOMappingException;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;

import java.time.LocalDateTime;
import java.util.Collection;

public class TestConvertingFIPStoISOStep extends AbstractStep {

    private static final Logger LOG = LogManager.getLogger(TestConvertingFIPStoISOStep.class);
    
    private BusinessObjectService businessObjectService;
    private ISOFIPSConversionService isoFipsConversionService;

    @Override
    public boolean execute(String jobName, LocalDateTime jobRunDate) {
        
        LOG.info("TestConvertingFIPStoISOStep: **** START TEST OF CONVERTING ALL FIPS COUNTRY CODES TO CORRESPONDING ISO COUNTRY CODE ****" + jobRunDate.toString());
        Collection<Country> allFipsCountriesFound = businessObjectService.findAll(Country.class);
        
        for (Country eachFipsCountry : allFipsCountriesFound) {
            try {
                LOG.info("FIPS COUNTRY CODE BEING CONVERTED = " + eachFipsCountry.getCode());
                String activeIsoCountryCode = isoFipsConversionService.convertFIPSCountryCodeToActiveISOCountryCode(eachFipsCountry.getCode());
                LOG.info("ISO COUNTRY CODE FOUND IN MAPPING = " + activeIsoCountryCode);
                LOG.info("");
            } catch (NoFIPStoISOMappingException FTIE) {
                //noop: error should have printed during conversion request
            } catch (ManyFIPStoISOMappingException FTIE) {
                //noop: error should have printed during conversion request
            }
        }
        LOG.info("TestConvertingFIPStoISOStep: **** END TEST OF CONVERTING ALL FIPS COUNTRY CODES TO CORRESPONDING ISO COUNTRY CODE ****" + jobRunDate.toString());
        return true;
    }

    public void setIsoFipsConversionService(ISOFIPSConversionService isoFipsConversionService) {
        this.isoFipsConversionService = isoFipsConversionService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
