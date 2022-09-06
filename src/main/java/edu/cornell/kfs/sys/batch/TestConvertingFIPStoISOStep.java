package edu.cornell.kfs.sys.batch;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.businessobject.Country;

import edu.cornell.kfs.sys.exception.ManyFIPStoISOMappingException;
import edu.cornell.kfs.sys.exception.NoFIPStoISOMappingException;
import edu.cornell.kfs.sys.service.CountryService;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class TestConvertingFIPStoISOStep extends AbstractStep {

    private static final Logger LOG = LogManager.getLogger(TestConvertingFIPStoISOStep.class);
    
    private BusinessObjectService businessObjectService;
    private CountryService countryService;
    private ISOFIPSConversionService isoFipsConversionService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) {
        
        LOG.info("TestConvertingFIPStoISOStep: **** START TEST OF CONVERTING ALL FIPS COUNTRY CODES TO CORRESPONDING ISO COUNTRY CODE ****" + jobRunDate.toString());
        Collection<Country> allFipsCountriesCollection = this.getBusinessObjectService().findAll(Country.class);
        List<Country> allFipsCountriesFound = new ArrayList<Country>(allFipsCountriesCollection);
        
        for (Country eachFipsCountry : allFipsCountriesFound) {
            try {
                LOG.info("FIPS COUNTRY CODE BEING CONVERTED = " + eachFipsCountry.getCode());
                String activeIsoCountryCode = this.getIsoFipsConversionService().convertFIPSCountryCodeToActiveISOCountryCode(eachFipsCountry.getCode());
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

    public CountryService getCountryService() {
        return countryService;
    }

    public void setCountryService(CountryService countryService) {
        this.countryService = countryService;
    }

    public ISOFIPSConversionService getIsoFipsConversionService() {
        return isoFipsConversionService;
    }

    public void setIsoFipsConversionService(ISOFIPSConversionService isoFipsConversionService) {
        this.isoFipsConversionService = isoFipsConversionService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}