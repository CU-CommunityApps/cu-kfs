package edu.cornell.kfs.concur.eventnotification.rest.jsonObjects;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.concur.rest.jsonObjects.ConcurOauth2TokenResponseDTO;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.CUJsonUtils;

public class ConcurDTOJsonTest {
    private static final String TOKEN_TYPE = "token_type";
    private static final String SCOPE = "scope";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String REFRESH_EXPIRES_IN = "refresh_expires_in";
    private static final String ID_TOKEN = "id_token";
    private static final String GEOLOCATION = "geolocation";
    private static final String EXPIRES_IN = "expires_in";
    private static final String ACCESS_TOKEN = "access_token";
    private static final Logger LOG = LogManager.getLogger();
    public static final String OAUTH2_TOKEN_RESPONSE_FILE_NAME = "src/test/resources/edu/cornell/kfs/concur/rest/jsonObjects/fixture/ConcurOauth2TokenResponse.json";
    public static final String OAUTH2_TOKEN_RESPONSE_WITH_EXTRA_VALUES_FILE_NAME = "src/test/resources/edu/cornell/kfs/concur/rest/jsonObjects/fixture/ConcurOauth2TokenResponseWithExtraValues.json";
    public static final String EXPECTED_TOKEN_VALUES_FILE_NAME = "src/test/resources/edu/cornell/kfs/concur/rest/jsonObjects/fixture/jsonValues.txt";
    
    private ObjectMapper objectMapper;
    private Map<String, Object> exptectedJsonValues;

    @BeforeEach
    public void setUp() throws Exception {
        objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        buildExptectedJsonValues();
    }
    
    private void buildExptectedJsonValues() {
        exptectedJsonValues = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(EXPECTED_TOKEN_VALUES_FILE_NAME));) {
            String line = br.readLine();
            while (line != null) {
                String[] lineValues = StringUtils.split(line, CUKFSConstants.EQUALS_SIGN);
                String key = lineValues[0];
                Object value;
                if (StringUtils.contains(key, EXPIRES_IN)) {
                    value = Integer.valueOf(lineValues[1]);
                } else {
                    value = lineValues[1];
                }
                exptectedJsonValues.put(key, value);
                line = br.readLine();
            }
        } catch (Exception e) {
            LOG.error("buildExptectedJsonValues, had an error reading the json expected values file", e);
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        objectMapper = null;
        exptectedJsonValues = null;
    }

    @Test
    public void testCreateConcurOauth2TokenResponseDTOFromJsonFile() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File(OAUTH2_TOKEN_RESPONSE_FILE_NAME); 
        ConcurOauth2TokenResponseDTO dto = objectMapper.readValue(jsonFile, ConcurOauth2TokenResponseDTO.class);
        validateConcurOauth2TokenResponseDTO(dto);
    }
    
    @Test
    public void testCreateConcurOauth2TokenResponseDTOFromJsonFileWithExtraValues() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File(OAUTH2_TOKEN_RESPONSE_WITH_EXTRA_VALUES_FILE_NAME); 
        ConcurOauth2TokenResponseDTO dto = objectMapper.readValue(jsonFile, ConcurOauth2TokenResponseDTO.class);
        validateConcurOauth2TokenResponseDTO(dto);
    }

    private void validateConcurOauth2TokenResponseDTO(ConcurOauth2TokenResponseDTO dto) {
        assertEquals(exptectedJsonValues.get(ACCESS_TOKEN), dto.getAccess_token());
        assertEquals(exptectedJsonValues.get(EXPIRES_IN), dto.getExpires_in());
        assertEquals(exptectedJsonValues.get(GEOLOCATION), dto.getGeolocation());
        assertEquals(exptectedJsonValues.get(ID_TOKEN), dto.getId_token());
        assertEquals(exptectedJsonValues.get(REFRESH_EXPIRES_IN), dto.getRefresh_expires_in());
        assertEquals(exptectedJsonValues.get(REFRESH_TOKEN), dto.getRefresh_token());
        assertEquals(exptectedJsonValues.get(SCOPE), dto.getScope());
        assertEquals(exptectedJsonValues.get(TOKEN_TYPE), dto.getToken_type());
    }
    
    @Test
    public void testCreateJsonStringFromConcurOauth2TokenResponseDTO() throws JsonProcessingException {
        ConcurOauth2TokenResponseDTO dto = new ConcurOauth2TokenResponseDTO();
        dto.setAccess_token("access");
        dto.setExpires_in(321);
        dto.setGeolocation("https://www.cornell.edu");
        dto.setId_token("id");
        dto.setRefresh_expires_in(987654321);
        dto.setRefresh_token("refresh");
        dto.setScope(SCOPE);
        dto.setToken_type("type");
        
        String expectedJson = "{\"expires_in\":321,\"scope\":\"scope\",\"token_type\":\"type\",\"access_token\":\"access\",\"refresh_token\":\"refresh\",\"refresh_expires_in\":987654321,\"geolocation\":\"https://www.cornell.edu\",\"id_token\":\"id\"}";
        
        String actualJson = objectMapper.writeValueAsString(dto);
        LOG.info("testCreateJsonStringFromConcurOauth2TokenResponseDTO, generated JSON: " + actualJson);
        
        assertEquals(expectedJson, actualJson);
    }

}
