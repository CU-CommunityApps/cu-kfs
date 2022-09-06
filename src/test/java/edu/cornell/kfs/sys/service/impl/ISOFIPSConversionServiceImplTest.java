package edu.cornell.kfs.sys.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
    
    private TestISOFIPSConversionServiceImpl isoFipsConversionService;   

    @BeforeEach
    void setUp() throws Exception {
        isoFipsConversionService = new TestISOFIPSConversionServiceImpl();
        isoFipsConversionService.setCountryService(new MockCountryService());
        isoFipsConversionService.setIsoCountryService(new MockISOCountryService());
        isoFipsConversionService.setIsoFipsCountryMapService(new MockISOFIPSCountryMapService());
    }
    
    @AfterEach
    void tearDown() {
        isoFipsConversionService.setCountryService(null);
        isoFipsConversionService.setIsoCountryService(null);
        isoFipsConversionService.setIsoFipsCountryMapService(null);
        isoFipsConversionService = null;
    }
       
    private class MockCountryService extends CountryServiceImpl { 
        @Override
        public Country getByPrimaryId(String countryCode) {
            List<CountryFixture> countriesFound = CountryFixture.mockCountryTable().filter(row -> row.code.equalsIgnoreCase(countryCode)).collect(Collectors.toList());
            if (countriesFound.isEmpty()) {
                return null;
            } else {
                return new Country(countriesFound.get(0).name, countriesFound.get(0).code, countriesFound.get(0).alternateCode, countriesFound.get(0).active);
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
              List<ISOFIPSCountryMap> activeMapList = new ArrayList<ISOFIPSCountryMap>();
              activeMapList.add(new ISOFIPSCountryMap(activeMappingsFoundMatchingCode.get(0).isoCountryCode, 
                      activeMappingsFoundMatchingCode.get(0).fipsCountryCode, 
                      activeMappingsFoundMatchingCode.get(0).active, 
                      new Country(activeMappingsFoundMatchingCode.get(0).fipsCountryFixture.name,
                              activeMappingsFoundMatchingCode.get(0).fipsCountryFixture.code,
                              activeMappingsFoundMatchingCode.get(0).fipsCountryFixture.alternateCode,
                              activeMappingsFoundMatchingCode.get(0).fipsCountryFixture.active),
                      new ISOCountry(activeMappingsFoundMatchingCode.get(0).isoCountryFixture.name,
                              activeMappingsFoundMatchingCode.get(0).isoCountryFixture.code,
                              activeMappingsFoundMatchingCode.get(0).isoCountryFixture.alternateCode,
                              activeMappingsFoundMatchingCode.get(0).isoCountryFixture.active)));
              return activeMapList;
          }
        }
      
        @Override
        public List<ISOFIPSCountryMap> findActiveMapsByFIPSCountryId(String fipsCountryCode) {
          List<ISOFIPSCountryMapFixture> mappingsFoundMatchingCode = ISOFIPSCountryMapFixture.mockISOFIPSCountryMapTable().filter(row -> row.fipsCountryCode.equalsIgnoreCase(fipsCountryCode)).collect(Collectors.toList());
          List<ISOFIPSCountryMapFixture> activeMappingsFoundMatchingCode = mappingsFoundMatchingCode.stream().filter(row -> row.isActive()).collect(Collectors.toList());
          if (activeMappingsFoundMatchingCode.isEmpty()) {
              return null;
          } else {
              List<ISOFIPSCountryMap> activeMapList = new ArrayList<ISOFIPSCountryMap>();
              activeMapList.add(new ISOFIPSCountryMap(activeMappingsFoundMatchingCode.get(0).isoCountryCode, 
                      activeMappingsFoundMatchingCode.get(0).fipsCountryCode, 
                      activeMappingsFoundMatchingCode.get(0).active, 
                      new Country(activeMappingsFoundMatchingCode.get(0).fipsCountryFixture.name,
                              activeMappingsFoundMatchingCode.get(0).fipsCountryFixture.code,
                              activeMappingsFoundMatchingCode.get(0).fipsCountryFixture.alternateCode,
                              activeMappingsFoundMatchingCode.get(0).fipsCountryFixture.active),
                      new ISOCountry(activeMappingsFoundMatchingCode.get(0).isoCountryFixture.name,
                              activeMappingsFoundMatchingCode.get(0).isoCountryFixture.code,
                              activeMappingsFoundMatchingCode.get(0).isoCountryFixture.alternateCode,
                              activeMappingsFoundMatchingCode.get(0).isoCountryFixture.active)));
              return activeMapList;
          }
        } 
    }
       
    private static class TestISOFIPSConversionServiceImpl extends ISOFIPSConversionServiceImpl {

        private MockISOCountryService isoCountryService;
        private MockCountryService countryService;
        private MockISOFIPSCountryMapService isoFipsCountryMapService;
        
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
    }
    
    static Stream<Arguments> fipsToIsoMappingsForValidation() {
        return Stream.of(FIPStoISOCountryConversionFixture.ALL_ACTIVE_TEST,
                FIPStoISOCountryConversionFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE_TEST,
                FIPStoISOCountryConversionFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE_TEST,
                FIPStoISOCountryConversionFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_INACTIVE_TEST,
                FIPStoISOCountryConversionFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_INACTIVE_TEST)
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
                ISOtoFIPSCountryConversionFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE_TEST,
                ISOtoFIPSCountryConversionFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE_TEST,
                ISOtoFIPSCountryConversionFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_INACTIVE_TEST,
                ISOtoFIPSCountryConversionFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_INACTIVE_TEST)
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
