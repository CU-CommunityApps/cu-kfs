package edu.cornell.kfs.sys.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.sys.businessobject.Country;

import edu.cornell.kfs.sys.businessobject.ISOCountry;
import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;
import edu.cornell.kfs.sys.exception.NoFIPStoISOMappingException;
import edu.cornell.kfs.sys.exception.NoISOtoFIPSMappingException;
import edu.cornell.kfs.sys.service.impl.fixture.CountryFixture;
import edu.cornell.kfs.sys.service.impl.fixture.FIPStoISOCountryConversionFixture;
import edu.cornell.kfs.sys.service.impl.fixture.ISOFIPSCountryMapFixture;
import edu.cornell.kfs.sys.service.impl.fixture.ISOtoFIPSCountryConversionFixture;
import edu.cornell.kfs.sys.service.impl.fixture.ISOCountryFixture;

public class ISOFIPSConversionServiceImplTest {
    private static final Logger LOG = LogManager.getLogger(ISOFIPSConversionServiceImplTest.class);
    
    private MockConfigurationService mockConfigurationService;
    private MockCountryService mockCountryService;
    private MockISOCountryService mockISOCountryService;
    private MockISOFIPSCountryMapService mockISOFIPSCountryMapService;
    private TestISOFIPSConversionServiceImpl isoFipsConversionService;

    @BeforeEach
    void setUp() throws Exception {
        mockConfigurationService = new MockConfigurationService();
        mockCountryService = new MockCountryService();
        mockCountryService.setConfigurationService(mockConfigurationService);
        mockISOCountryService = new MockISOCountryService();
        mockISOCountryService.setConfigurationService(mockConfigurationService);
        mockISOFIPSCountryMapService = new MockISOFIPSCountryMapService();
        isoFipsConversionService = new TestISOFIPSConversionServiceImpl();
        isoFipsConversionService.setCountryService(mockCountryService);
        isoFipsConversionService.setIsoCountryService(mockISOCountryService);
        isoFipsConversionService.setIsoFipsCountryMapService(mockISOFIPSCountryMapService);
        isoFipsConversionService.setConfigurationService(mockConfigurationService);
    }
    
    @AfterEach
    void tearDown() {
        mockConfigurationService = null;
        mockCountryService.setConfigurationService(null);
        mockCountryService = null;
        mockISOCountryService.setConfigurationService(null);
        mockISOCountryService = null;
        mockISOFIPSCountryMapService = null;
        isoFipsConversionService.setCountryService(null);
        isoFipsConversionService.setIsoCountryService(null);
        isoFipsConversionService.setIsoFipsCountryMapService(null);
        isoFipsConversionService.setConfigurationService(null);
        isoFipsConversionService = null;
    }
       
    private class MockCountryService extends CountryServiceImpl { 
        @Override
        public Country getByPrimaryId(String countryCode) {
            List<CountryFixture> countriesFound = CountryFixture.mockCountryTable().filter(row -> row.code.equalsIgnoreCase(countryCode)).collect(Collectors.toList());
            if (countriesFound.isEmpty()) {
                return null;
            } else {
                Country countryFound = new Country();
                countryFound.setName(countriesFound.get(0).name);
                countryFound.setCode(countriesFound.get(0).code);
                countryFound.setAlternateCode(countriesFound.get(0).alternateCode);
                countryFound.setActive(countriesFound.get(0).active);
                return countryFound;
            }
        }
    }
    
    private static class MockISOCountryService extends ISOCountryServiceImpl {
        @Override
        public ISOCountry getByPrimaryId(String isoCountryCode) {
            List<ISOCountryFixture> countriesFound = ISOCountryFixture.mockISOCountryTable().filter(row -> row.code.equalsIgnoreCase(isoCountryCode)).collect(Collectors.toList());
            if (countriesFound.isEmpty()) {
                return null;
            } else {
                return new ISOCountry(countriesFound.get(0).name, countriesFound.get(0).code, countriesFound.get(0).alternateCode, countriesFound.get(0).active);
            }
        }
    }
    
    private static class MockISOFIPSCountryMapService extends ISOFIPSCountryMapServiceImpl {
        @Override
        public List<ISOFIPSCountryMap> findActiveMapsByISOCountryId(String isoCountryCode) {

          List<ISOFIPSCountryMapFixture> mappingsFoundMatchingCode = ISOFIPSCountryMapFixture.mockISOFIPSCountryMapTable().filter(row -> row.isoCountryCode.equalsIgnoreCase(isoCountryCode)).collect(Collectors.toList());
          List<ISOFIPSCountryMapFixture> activeMappingsFoundMatchingCode = mappingsFoundMatchingCode.stream().filter(row -> row.isActive()).collect(Collectors.toList());
          if (activeMappingsFoundMatchingCode.isEmpty()) {
              return null;
          } else {
              return createISOFIPSCountryMapObjectFromFixture(activeMappingsFoundMatchingCode);
          }
        }
      
        @Override
        public List<ISOFIPSCountryMap> findActiveMapsByFIPSCountryId(String fipsCountryCode) {
          List<ISOFIPSCountryMapFixture> mappingsFoundMatchingCode = ISOFIPSCountryMapFixture.mockISOFIPSCountryMapTable().filter(row -> row.fipsCountryCode.equalsIgnoreCase(fipsCountryCode)).collect(Collectors.toList());
          List<ISOFIPSCountryMapFixture> activeMappingsFoundMatchingCode = mappingsFoundMatchingCode.stream().filter(mapping -> mapping.isActive()).collect(Collectors.toList());

          if (activeMappingsFoundMatchingCode.isEmpty()) {
              return null;
          } else {
              return createISOFIPSCountryMapObjectFromFixture(activeMappingsFoundMatchingCode);
          }
        }
        
        private List<ISOFIPSCountryMap> createISOFIPSCountryMapObjectFromFixture(List<ISOFIPSCountryMapFixture> activeMappingsFoundMatchingCode) {
            List<ISOFIPSCountryMap> activeMapList = new ArrayList<ISOFIPSCountryMap>();
            for (ISOFIPSCountryMapFixture matchingFixtureFound : activeMappingsFoundMatchingCode) {
                Country countryFound = new Country();
                countryFound.setName(matchingFixtureFound.fipsCountryFixture.name);
                countryFound.setCode(matchingFixtureFound.fipsCountryFixture.code);
                countryFound.setAlternateCode(matchingFixtureFound.fipsCountryFixture.alternateCode);
                countryFound.setActive(matchingFixtureFound.fipsCountryFixture.active);
                activeMapList.add(new ISOFIPSCountryMap(matchingFixtureFound.isoCountryCode,
                        matchingFixtureFound.fipsCountryCode,
                        matchingFixtureFound.active,
                        countryFound,
                        new ISOCountry(matchingFixtureFound.isoCountryFixture.name,
                                matchingFixtureFound.isoCountryFixture.code,
                                matchingFixtureFound.isoCountryFixture.alternateCode,
                                matchingFixtureFound.isoCountryFixture.active)));
            }
            return activeMapList;
        }
    }

    protected static class MockConfigurationService implements ConfigurationService {
        private HashMap<String, String> applicationResourcePropertiesMessages;

        public MockConfigurationService() {
            applicationResourcePropertiesMessages = new HashMap<String, String>();
            applicationResourcePropertiesMessages.put("error.no.country.found.for.code", "No Country found for code : {0}");
            applicationResourcePropertiesMessages.put("message.country.code.indicator", "Country code : {0} has status of {1}");
            applicationResourcePropertiesMessages.put("error.no.iso.country.found.for.code", "No ISOCountry found for code : {0}");
            applicationResourcePropertiesMessages.put("message.iso.country.code.indicator", "ISOCountry code : {0} has active status of {1}");
            applicationResourcePropertiesMessages.put("error.no.iso.to.fips.mappings", "No Active ISO-to-FIPS Country generic mapping found for ISO Country code : {0}");
            applicationResourcePropertiesMessages.put("error.many.iso.to.fips.mappings", "More than one Active ISO-to-FIPS Country generic mapping found for ISO Country code : {0}");
            applicationResourcePropertiesMessages.put("message.one.to.one.iso.to.fips.mappping", "One Active ISO-to-FIPS Country generic mapping found: ISO Country code : {0} mapped to FIPS Country code : {1}");
            applicationResourcePropertiesMessages.put("error.no.fips.to.iso.mappings", "No Active FIPS-to-ISO Country generic mapping found for FIPS Country code : {0}");
            applicationResourcePropertiesMessages.put("error.many.fips.to.iso.mappings", "More than one Active FIPS-to-ISO Country generic mapping found for FIPS Country code : {0}");
            applicationResourcePropertiesMessages.put("message.one.to.one.fips.to.iso.mapping", "One Active FIPS-to-ISO Country generic mapping found FIPS Country code : {0} mapped to ISO Country code : {1}");
        }

        @Override
        public String getPropertyValueAsString(String key) {
            return applicationResourcePropertiesMessages.get(key);
        }

        @Override
        public Map<String, String> getAllProperties() {
            return null;
        }

        @Override
        public boolean getPropertyValueAsBoolean(String key) {
            return false;
        }
        @Override
        public boolean getPropertyValueAsBoolean(String key, boolean defaultValue) {
            return false;
        }
    }
       
    private static class TestISOFIPSConversionServiceImpl extends ISOFIPSConversionServiceImpl {

        private MockISOCountryService isoCountryService;
        private MockCountryService countryService;
        private MockISOFIPSCountryMapService isoFipsCountryMapService;
        private MockConfigurationService configurationService;
        
        public MockISOCountryService getIsoCountryService() {
            return isoCountryService;
        }

        public void setIsoCountryService(MockISOCountryService isoCountryService) {
            this.isoCountryService = isoCountryService;
        }

        public MockCountryService getCountryService() {
            return countryService;
        }

        public void setCountryService(MockCountryService countryService) {
            this.countryService = countryService;
        }

        public MockISOFIPSCountryMapService getIsoFipsCountryMapService() {
            return isoFipsCountryMapService;
        }

        public void setIsoFipsCountryMapService(MockISOFIPSCountryMapService isoFipsCountryMapService) {
            this.isoFipsCountryMapService = isoFipsCountryMapService;
        }

        public MockConfigurationService getConfigurationService() {
            return configurationService;
        }

        public void setConfigurationService(MockConfigurationService configurationService) {
            this.configurationService = configurationService;
        }
    }
    
    static Stream<Arguments> fipsToIsoMappingsForValidation() {
        return Stream.of(FIPStoISOCountryConversionFixture.ALL_ACTIVE_TEST,
                FIPStoISOCountryConversionFixture.ALL_INACTIVE_TEST,
                FIPStoISOCountryConversionFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE_TEST,
                FIPStoISOCountryConversionFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_INACTIVE_TEST,
                FIPStoISOCountryConversionFixture.FIPS_INACTIVE_ISO_INACTIVE_MAP_ACTIVE_TEST,
                FIPStoISOCountryConversionFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_INACTIVE_TEST,
                FIPStoISOCountryConversionFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_ACTIVE_TEST)
                .map(Arguments::of);
    }
    
    @ParameterizedTest
    @MethodSource("fipsToIsoMappingsForValidation") 
    void testConvertFIPSCountryToISOCountry (FIPStoISOCountryConversionFixture testFixture) {
        if (StringUtils.equalsIgnoreCase(testFixture.testShouldPass, "PASS")) {
            String foundISOCountryCode = isoFipsConversionService.convertFIPSCountryCodeToActiveISOCountryCode(testFixture.fipsCountryCodeToSearchFor);
            assertEquals(testFixture.isoFipsCountryMapFixture.isoCountryCode, foundISOCountryCode, "FIPS Country Code did not map to expected ISO Country Code.");
        } else if (StringUtils.equalsIgnoreCase(testFixture.testShouldPass, "FAIL")){
            String foundISOCountryCode = isoFipsConversionService.convertFIPSCountryCodeToActiveISOCountryCode(testFixture.fipsCountryCodeToSearchFor);
            assertNotEquals(testFixture.isoFipsCountryMapFixture.isoCountryCode, foundISOCountryCode, "FIPS Country Code was not expected to map to this ISO Country Code.");
        } else {
            assertThrows(NoFIPStoISOMappingException.class, 
                    () -> isoFipsConversionService.convertFIPSCountryCodeToActiveISOCountryCode(testFixture.fipsCountryCodeToSearchFor),
                    "NoFIPStoISOMappingException should have been thrown due to FIPS Country Code being Inactive.");
        }
    }
    
    static Stream<Arguments> isoToFipsMappingsForValidation() {
        return Stream.of(ISOtoFIPSCountryConversionFixture.ALL_ACTIVE_TEST,
                ISOtoFIPSCountryConversionFixture.ALL_INACTIVE_TEST,
                ISOtoFIPSCountryConversionFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE_TEST,
                ISOtoFIPSCountryConversionFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_INACTIVE_TEST,
                ISOtoFIPSCountryConversionFixture.FIPS_INACTIVE_ISO_INACTIVE_MAP_ACTIVE_TEST,
                ISOtoFIPSCountryConversionFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_INACTIVE_TEST,
                ISOtoFIPSCountryConversionFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_ACTIVE_TEST)
                .map(Arguments::of);
    }
    
    @ParameterizedTest
    @MethodSource("isoToFipsMappingsForValidation") 
    void testConvertISOCountryToFIPSCountry (ISOtoFIPSCountryConversionFixture testFixture) {
        if (StringUtils.equalsIgnoreCase(testFixture.testShouldPass, "PASS")) {
            String foundFIPSCountryCode = isoFipsConversionService.convertISOCountryCodeToActiveFIPSCountryCode(testFixture.isoCountryCodeToSearchFor);
            assertEquals(testFixture.isoFipsCountryMapFixture.fipsCountryCode, foundFIPSCountryCode, "ISO Country Code did not map to expected FIPS Country Code.");
        } else if (StringUtils.equalsIgnoreCase(testFixture.testShouldPass, "FAIL")){
            String foundFIPSCountryCode = isoFipsConversionService.convertISOCountryCodeToActiveFIPSCountryCode(testFixture.isoCountryCodeToSearchFor);
            assertNotEquals(testFixture.isoFipsCountryMapFixture.isoCountryCode, foundFIPSCountryCode, "ISO Country Code was not expected to map to this FIPS Country Code.");
        } else {
            assertThrows(NoISOtoFIPSMappingException.class, 
                    () -> isoFipsConversionService.convertISOCountryCodeToActiveFIPSCountryCode(testFixture.isoCountryCodeToSearchFor),
                    "NoFIPStoISOMappingException should have been thrown due to ISO Country Code being Inactive.");
        }
    }
    
}
