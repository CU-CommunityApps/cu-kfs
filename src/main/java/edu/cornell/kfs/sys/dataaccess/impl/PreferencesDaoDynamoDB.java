package edu.cornell.kfs.sys.dataaccess.impl;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.dataaccess.PreferencesDao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PreferencesDaoDynamoDB implements PreferencesDao {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PreferencesDaoDynamoDB.class);

    public static final String INSTITUTION_PREFERENCES = "InstitutionPreferences";
    public static final String INSTITUTION_ID_KEY = "institutionId";
    public static final String INSTITUTION_PREFERENCES_CACHE = "InstitutionPreferencesCache";
    public static final String INSTITUTION_PREFERENCES_CACHE_LENGTH = "InstitutionPreferencesCacheLength";
    public static final String CACHE_LENGTH = "cacheLength";

    public static final String USER_PREFERENCES = "UserPreferences";
    public static final String PRINCIPAL_NAME_KEY = "principalName";
    public static final String DATE_KEY = "createdAt";
    public static final String CACHED_KEY = "cached";
    public static final String DEFAULT_INSTITUTION_ID = "1232413535";

    private DynamoDBClient documentStoreClient;
    private String tableNamePrefix;

    @Override
    public Map<String, Object> findInstitutionPreferences() {
        LOG.debug("findInstitutionPreferences() started");

        Table table = retrieveTable(INSTITUTION_PREFERENCES);

        Item item = table.getItem(INSTITUTION_ID_KEY, DEFAULT_INSTITUTION_ID);
        return item == null ? null : item.asMap();
    }

    @Override
    public void saveInstitutionPreferences(String institutionId, Map<String, Object> preferences) {
        LOG.debug("saveInstitutionPreferences started");

        ObjectMapper mapper = new ObjectMapper();
        try {
            saveInstitutionPreferences(institutionId, mapper.writeValueAsString(preferences));
        } catch (JsonProcessingException e) {
            LOG.error("saveInstitutionPreferences() Error processing json", e);
            throw new RuntimeException("Error processing json");
        }
    }

    private void saveInstitutionPreferences(String institutionId, String preferences) {
        Item item = Item.fromJSON(preferences);
        item = item.withString(INSTITUTION_ID_KEY, institutionId);

        Table table = retrieveTable(INSTITUTION_PREFERENCES);

        PutItemOutcome outcome = table.putItem(item);
        LOG.debug("Saved institution preferences: " + outcome);
    }

    @Override
    public Map<String, Object> findInstitutionPreferencesCache(String principalName) {
        LOG.debug("findInstitutionPreferencesCache() started");

        Table table = retrieveTable(INSTITUTION_PREFERENCES_CACHE);

        Item item = table.getItem(PRINCIPAL_NAME_KEY, principalName);

        if (item == null) {
            return null;
        }

        long cachedDate = ((BigDecimal)item.asMap().get(DATE_KEY)).longValue();
        if (expireCache(cachedDate)) {
            table.deleteItem(PRINCIPAL_NAME_KEY, principalName);
            item = null;
        }

        return item == null ? null : item.asMap();
    }

    private boolean expireCache(long cachedDate) {
        int cacheLength = getInstitutionPreferencesCacheLength();
        if (cacheLength == 0) {
            return false;
        }

        long now = new Date().getTime();
        return cachedDate + (cacheLength * 1000) < now;
    }

    @Override
    public void cacheInstitutionPreferences(String principalName, Map<String, Object> institutionPreferences) {
        LOG.debug("cacheInstitutionPreferences() started");

        institutionPreferences.put(PRINCIPAL_NAME_KEY, principalName);
        institutionPreferences.put(DATE_KEY, new Date());
        institutionPreferences.put(CACHED_KEY,true);
        institutionPreferences.remove("_id");

        Table table = retrieveTable(INSTITUTION_PREFERENCES_CACHE);

        String preferences;

        ObjectMapper mapper = new ObjectMapper();
        try {
            preferences = mapper.writeValueAsString(institutionPreferences);
        } catch (JsonProcessingException e) {
            LOG.error("cacheInstitutionPreferences() Error processing json", e);
            throw new RuntimeException("Error processing json");
        }

        Item item = Item.fromJSON(preferences);
        item = item.withString(PRINCIPAL_NAME_KEY, principalName);

        PutItemOutcome outcome = table.putItem(item);
        LOG.debug("Cached institution preferences: " + outcome);
    }

    @Override
    public void setInstitutionPreferencesCacheLength(int seconds) {
        LOG.debug("setInstitutionPreferencesCacheLength started");

        Map<String, Object> cacheLength = new HashMap<>();
        cacheLength.put(CACHE_LENGTH, seconds);

        ObjectMapper mapper = new ObjectMapper();
        try {
            saveInstitutionPreferencesCacheLength(DEFAULT_INSTITUTION_ID, mapper.writeValueAsString(cacheLength));
        } catch (JsonProcessingException e) {
            LOG.error("saveInstitutionPreferences() Error processing json", e);
            throw new RuntimeException("Error processing json");
        }
    }

    private void saveInstitutionPreferencesCacheLength(String institutionId, String cacheLength) {
        Item item = Item.fromJSON(cacheLength);
        item = item.withString(INSTITUTION_ID_KEY, institutionId);

        Table table = retrieveTable(INSTITUTION_PREFERENCES_CACHE_LENGTH);

        PutItemOutcome outcome = table.putItem(item);
        LOG.debug("Saved institution preferences cache length: " + outcome);
    }

    @Override
    public int getInstitutionPreferencesCacheLength() {
        LOG.debug("getInstitutionPreferencesCacheLength started");

        Table table = retrieveTable(INSTITUTION_PREFERENCES_CACHE_LENGTH);

        if (table == null) {
            return 0;
        }

        Item item = table.getItem(INSTITUTION_ID_KEY, DEFAULT_INSTITUTION_ID);
        return item == null ? 0 : ((BigDecimal)item.asMap().get(CACHE_LENGTH)).intValue();
    }

    @Override
    public Map<String, Object> getUserPreferences(String principalName) {
        Table table = retrieveTable(USER_PREFERENCES);

        Item item = table.getItem(PRINCIPAL_NAME_KEY, principalName);
        return item == null ? null : item.asMap();
    }

    @Override
    public void saveUserPreferences(String principalName, String preferences) {
        Item item = Item.fromJSON(preferences);
        item = item.withString(PRINCIPAL_NAME_KEY, principalName);

        Table table = retrieveTable(USER_PREFERENCES);
        PutItemOutcome outcome = table.putItem(item);
        LOG.debug("Saved user preferences: " + outcome);
    }

    @Override
    public void saveUserPreferences(String principalName, Map<String, Object> preferences) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            saveUserPreferences(principalName, mapper.writeValueAsString(preferences));
        } catch (JsonProcessingException e) {
            LOG.error("saveUserPreferences() Error processing json",e);
            throw new RuntimeException("Error processing json");
        }
    }

    protected Table retrieveTable(String tableName) {
        DynamoDB dynamoDB = new DynamoDB(documentStoreClient.getClient());
        if (StringUtils.isNotBlank(tableNamePrefix)) {
            tableName = tableNamePrefix + tableName;
        }
        return dynamoDB.getTable(tableName);
    }

    public void setDocumentStoreClient(DynamoDBClient documentStoreClient) {
        this.documentStoreClient = documentStoreClient;
    }

    public String getTableNamePrefix() {
        return tableNamePrefix;
    }

    public void setTableNamePrefix(String tableNamePrefix) {
        this.tableNamePrefix = tableNamePrefix;
    }
}
