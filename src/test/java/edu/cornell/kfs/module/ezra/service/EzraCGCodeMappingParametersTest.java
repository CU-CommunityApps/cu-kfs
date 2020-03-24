package edu.cornell.kfs.module.ezra.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.impl.datetime.DateTimeServiceImpl;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.ParameterServiceImpl;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.impl.DocumentServiceImpl;

import edu.cornell.kfs.module.cg.CuCGParameterConstants;
import edu.cornell.kfs.module.ezra.dataaccess.impl.EzraAwardProposalDaoOjb;
import edu.cornell.kfs.module.ezra.dataaccess.impl.SponsorDaoOjb;
import edu.cornell.kfs.module.ezra.service.impl.EzraServiceImpl;
import edu.cornell.kfs.sys.CUKFSConstants;

public class EzraCGCodeMappingParametersTest {

    private TestEzraService ezraService;

    @Before
    public void setUp() throws Exception {
        ezraService = new TestEzraService();
        ezraService.setBusinessObjectService(mock(BusinessObjectService.class));
        ezraService.setDateTimeService(mock(DateTimeServiceImpl.class));
        ezraService.setDocumentService(mock(DocumentServiceImpl.class));
        ezraService.setEzraAwardProposalDao(mock(EzraAwardProposalDaoOjb.class));
        ezraService.setSponsorDao(mock(SponsorDaoOjb.class));
    }



    @Test
    public void testCreationOfCGMappings() throws Exception {
        ParameterNameAndMultiValue agencyParm = createAgencyTypeParm(new KeyAndValue("1", "F"), new KeyAndValue("2", "S"), new KeyAndValue("3", "C"));
        ParameterNameAndMultiValue proposalStatusParm = createProposalStatusParm(new KeyAndValue("AMAF", "AF"), new KeyAndValue("AMNAC", "AN"));
        ParameterNameAndMultiValue grantDescriptionParm = createGrantDescriptionParm(new KeyAndValue("C", "CON"));
        ParameterNameAndMultiValue proposalPurposeParm = createProposalPurposeParm(new KeyAndValue("A", "A"), new KeyAndValue("G", "B"));
        
        setupMockParameterServiceWithMultiValueParms(agencyParm, proposalStatusParm, grantDescriptionParm, proposalPurposeParm);
        
        assertCorrectMapCreation(agencyParm.values, ezraService.getAgencyTypeMap());
        assertCorrectMapCreation(proposalStatusParm.values, ezraService.getProposalStatusMap());
        assertCorrectMapCreation(grantDescriptionParm.values, ezraService.getGrantDescriptionMap());
        assertCorrectMapCreation(proposalPurposeParm.values, ezraService.getProposalPurposeMap());
    }

    @Test
    public void testCreationOfEmptyCGMappings() throws Exception {
        List<KeyAndValue> emptyParmsList = Collections.emptyList();
        
        setupMockParameterServiceWithMultiValueParms(
                createAgencyTypeParm(), createProposalStatusParm(), createGrantDescriptionParm(), createProposalPurposeParm());
        
        assertCorrectMapCreation(emptyParmsList, ezraService.getAgencyTypeMap());
        assertCorrectMapCreation(emptyParmsList, ezraService.getProposalStatusMap());
        assertCorrectMapCreation(emptyParmsList, ezraService.getGrantDescriptionMap());
        assertCorrectMapCreation(emptyParmsList, ezraService.getProposalPurposeMap());
    }

    @Test
    public void testUnsuccessfulCreationOfCGMappingsForMalformedParameters() throws Exception {
        ParameterNameAndMultiValue agencyParm = createAgencyTypeParm(new KeyAndValue("1", "F"), new KeyAndValue("2", "S", false), new KeyAndValue("3", "C"));
        ParameterNameAndMultiValue proposalStatusParm = createProposalStatusParm(new KeyAndValue("AMAF", "AF", false), new KeyAndValue("AMNAC", "AN"));
        ParameterNameAndMultiValue grantDescriptionParm = createGrantDescriptionParm(new KeyAndValue("C", "CON", false));
        ParameterNameAndMultiValue proposalPurposeParm = createProposalPurposeParm(new KeyAndValue("A", "A"), new KeyAndValue("G", "B", false));
        
        setupMockParameterServiceWithMultiValueParms(agencyParm, proposalStatusParm, grantDescriptionParm, proposalPurposeParm);
        
        assertExceptionThrownOnMapCreationForIncorrectParameterSetup("getAgencyTypeMap");
        assertExceptionThrownOnMapCreationForIncorrectParameterSetup("getProposalStatusMap");
        assertExceptionThrownOnMapCreationForIncorrectParameterSetup("getGrantDescriptionMap");
        assertExceptionThrownOnMapCreationForIncorrectParameterSetup("getProposalPurposeMap");
    }

    protected void assertCorrectMapCreation(List<KeyAndValue> expectedValues, Map<String,String> actualMappings) throws Exception {
        Map<String,String> expectedMappings = convertToMap(expectedValues);
        assertEquals("Generated parameter map does not contain the correct mappings", expectedMappings, actualMappings);
    }

    protected void assertExceptionThrownOnMapCreationForIncorrectParameterSetup(String mapRetrievalMethodName) throws Exception {
        try {
            Method mapRetrievalMethod = TestEzraService.class.getMethod(mapRetrievalMethodName);
            mapRetrievalMethod.invoke(ezraService);
            fail("The method '" + mapRetrievalMethodName + "' should have thrown an exception due to incorrect parameter setup");
        } catch (InvocationTargetException e) {
            assertTrue("Attempted parameter setup should have thrown an instance of IndexOutOfBoundsException, but instead threw "
                    + e.getCause().getClass().getName(), e.getCause() instanceof IndexOutOfBoundsException);
        }
    }



    protected ParameterNameAndMultiValue createAgencyTypeParm(KeyAndValue... values) {
        return new ParameterNameAndMultiValue(CuCGParameterConstants.AGENCY_TYPE_MAPPINGS, createList(values));
    }

    protected ParameterNameAndMultiValue createProposalStatusParm(KeyAndValue... values) {
        return new ParameterNameAndMultiValue(CuCGParameterConstants.PROPOSAL_STATUS_MAPPINGS, createList(values));
    }

    protected ParameterNameAndMultiValue createGrantDescriptionParm(KeyAndValue... values) {
        return new ParameterNameAndMultiValue(CuCGParameterConstants.GRANT_DESCRIPTION_MAPPINGS, createList(values));
    }

    protected ParameterNameAndMultiValue createProposalPurposeParm(KeyAndValue... values) {
        return new ParameterNameAndMultiValue(CuCGParameterConstants.PROPOSAL_PURPOSE_MAPPINGS, createList(values));
    }

    protected List<KeyAndValue> createList(KeyAndValue... values) {
        return Arrays.asList(values);
    }


    protected void setupMockParameterServiceWithMultiValueParms(ParameterNameAndMultiValue... parameters) {
        ParameterService parameterService = mock(ParameterServiceImpl.class);

        for (ParameterNameAndMultiValue parameter : parameters) {
            List<String> expectedParameterValues = createParameterValueStrings(parameter.values);
            when(parameterService.getParameterValuesAsString(
                    CUKFSConstants.OptionalModuleNamespaces.CONTRACTS_AND_GRANTS, KfsParameterConstants.BATCH_COMPONENT, parameter.name
                )).thenReturn(expectedParameterValues);
        }
        
        ezraService.setParameterService(parameterService);
    }

    protected List<String> createParameterValueStrings(List<KeyAndValue> keysAndValues) {
        List<String> parameterValues = new ArrayList<>(keysAndValues.size());
        for (KeyAndValue keyAndValue : keysAndValues) {
            if (keyAndValue.useCorrectFormat) {
                parameterValues.add(keyAndValue.key + "=" + keyAndValue.value);
            } else {
                parameterValues.add(keyAndValue.key + keyAndValue.value);
            }
        }
        return parameterValues;
    }

    protected Map<String,String> convertToMap(List<KeyAndValue> keysAndValues) {
        Map<String,String> mappings = new HashMap<String,String>();
        for (KeyAndValue keyAndValue : keysAndValues) {
            mappings.put(keyAndValue.key, keyAndValue.value);
        }
        return mappings;
    }



    private static class KeyAndValue {
        public final String key;
        public final String value;
        public final boolean useCorrectFormat;
        
        public KeyAndValue(String key, String value) {
            this(key, value, true);
        }
        
        public KeyAndValue(String key, String value, boolean useCorrectFormat) {
            this.key = key;
            this.value = value;
            this.useCorrectFormat = useCorrectFormat;
        }
    }

    private static class ParameterNameAndMultiValue {
        public final String name;
        public final List<KeyAndValue> values;
        
        public ParameterNameAndMultiValue(String name, List<KeyAndValue> values) {
            this.name = name;
            this.values = Collections.unmodifiableList(new ArrayList<>(values));
        }
    }

    private static class TestEzraService extends EzraServiceImpl {
        @Override
        public Map<String,String> getAgencyTypeMap() {
            return super.getAgencyTypeMap();
        }
        
        @Override
        public Map<String,String> getProposalStatusMap() {
            return super.getProposalStatusMap();
        }
        
        @Override
        public Map<String,String> getGrantDescriptionMap() {
            return super.getGrantDescriptionMap();
        }
        
        @Override
        public Map<String,String> getProposalPurposeMap() {
            return super.getProposalPurposeMap();
        }
    }

}
