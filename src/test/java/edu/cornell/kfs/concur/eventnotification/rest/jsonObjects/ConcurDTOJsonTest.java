package edu.cornell.kfs.concur.eventnotification.rest.jsonObjects;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

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
import edu.cornell.kfs.sys.util.CUJsonUtils;

public class ConcurDTOJsonTest {
    private static final Logger LOG = LogManager.getLogger();
    
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
    }

    @AfterEach
    public void tearDown() throws Exception {
        objectMapper = null;
    }

    @Test
    public void testCreateConcurOauth2TokenResponseDTOFromJsonFile() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File(ConcurDTOJsonTestConstants.OAUTH2_TOKEN_RESPONSE_FILE_NAME); 
        ConcurOauth2TokenResponseDTO dto = objectMapper.readValue(jsonFile, ConcurOauth2TokenResponseDTO.class);
        validateConcurOauth2TokenResponseDTO(dto);
    }
    
    @Test
    public void testCreateConcurOauth2TokenResponseDTOFromJsonFileWithExtraValues() throws JsonParseException, JsonMappingException, IOException {
        File jsonFile = new File(ConcurDTOJsonTestConstants.OAUTH2_TOKEN_RESPONSE_WITH_EXTRA_VALUES_FILE_NAME); 
        ConcurOauth2TokenResponseDTO dto = objectMapper.readValue(jsonFile, ConcurOauth2TokenResponseDTO.class);
        validateConcurOauth2TokenResponseDTO(dto);
    }

    private void validateConcurOauth2TokenResponseDTO(ConcurOauth2TokenResponseDTO dto) {
        assertEquals(ConcurDTOJsonTestConstants.Oauth2TokenResponseValue.ACCESS_TOKEN, dto.getAccess_token());
        assertEquals(ConcurDTOJsonTestConstants.Oauth2TokenResponseValue.EXPIRES_IN, dto.getExpires_in());
        assertEquals(ConcurDTOJsonTestConstants.Oauth2TokenResponseValue.GEOLOCATION, dto.getGeolocation());
        assertEquals(ConcurDTOJsonTestConstants.Oauth2TokenResponseValue.ID_TOKEN, dto.getId_token());
        assertEquals(ConcurDTOJsonTestConstants.Oauth2TokenResponseValue.REFRESH_EXPIRES_IN, dto.getRefresh_expires_in());
        assertEquals(ConcurDTOJsonTestConstants.Oauth2TokenResponseValue.REFRESH_TOKEN, dto.getRefresh_token());
        assertEquals(ConcurDTOJsonTestConstants.Oauth2TokenResponseValue.SCOPE, dto.getScope());
        assertEquals(ConcurDTOJsonTestConstants.Oauth2TokenResponseValue.TOKEN_TYPE, dto.getToken_type());
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
        dto.setScope("scope");
        dto.setToken_type("type");
        
        String expectedJson = "{\"expires_in\":321,\"scope\":\"scope\",\"token_type\":\"type\",\"access_token\":\"access\",\"refresh_token\":\"refresh\",\"refresh_expires_in\":987654321,\"geolocation\":\"https://www.cornell.edu\",\"id_token\":\"id\"}";
        
        String actualJson = objectMapper.writeValueAsString(dto);
        LOG.info("testCreateJsonStringFromConcurOauth2TokenResponseDTO, generated JSON: " + actualJson);
        
        assertEquals(expectedJson, actualJson);
    }

}
