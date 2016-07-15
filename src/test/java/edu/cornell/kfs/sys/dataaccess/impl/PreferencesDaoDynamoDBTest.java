package edu.cornell.kfs.sys.dataaccess.impl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PreferencesDaoDynamoDBTest {

    public static final String PRINCIPAL_NAME = "bh79";
    public static final String PRINCIPAL_NAME_EXPIRED = "ccs1";
    public static final String PREFERENCES_KEY = "preferences";

    private PreferencesDaoDynamoDB preferencesDaoDynamoDB;

    @Before
    public void setUp() throws Exception {
        preferencesDaoDynamoDB = new MockPreferencesDaoDynamodDB();
    }

    @After
    public void tearDown() throws Exception {
        preferencesDaoDynamoDB = null;
    }

    @Test
    public void findInstitutionPreferences() throws Exception {
        Map<String, Object> institutionalPreferences = preferencesDaoDynamoDB.findInstitutionPreferences();
        validateInstitutionalPreferences(institutionalPreferences, PreferencesDaoDynamoDB.INSTITUTION_PREFERENCES, PreferencesDaoDynamoDB.INSTITUTION_ID_KEY, PreferencesDaoDynamoDB.DEFAULT_INSTITUTION_ID, PREFERENCES_KEY);
    }

    @Test
    public void saveInstitutionPreferences() throws Exception {
        Map<String, Object> institutionalPreferences = new HashMap<>();
        institutionalPreferences.put(PREFERENCES_KEY, "new preferences");
        preferencesDaoDynamoDB.saveInstitutionPreferences(PreferencesDaoDynamoDB.DEFAULT_INSTITUTION_ID, institutionalPreferences);
        Map<String, Object> savedInstitutionalPreferences = preferencesDaoDynamoDB.findInstitutionPreferences();
        validateInstitutionalPreferences(savedInstitutionalPreferences, PreferencesDaoDynamoDB.INSTITUTION_PREFERENCES, PreferencesDaoDynamoDB.INSTITUTION_ID_KEY, PreferencesDaoDynamoDB.DEFAULT_INSTITUTION_ID, "new preferences");
    }

    @Test
    public void findInstitutionPreferencesCacheNoCache() throws Exception {
        Map<String, Object> institutionalPreferences = preferencesDaoDynamoDB.findInstitutionPreferencesCache(PRINCIPAL_NAME);
        assertNull("InstitutionalPreferencesCache should be null", institutionalPreferences);
    }

    @Test
    public void findInstitutionPreferencesCacheExpiredCache() throws Exception {
        Map<String, Object> cachedInstitutionalPreferences = preferencesDaoDynamoDB.findInstitutionPreferencesCache(PRINCIPAL_NAME_EXPIRED);
        validateInstitutionalPreferences(cachedInstitutionalPreferences, PreferencesDaoDynamoDB.INSTITUTION_PREFERENCES_CACHE, PreferencesDaoDynamoDB.PRINCIPAL_NAME_KEY, PRINCIPAL_NAME_EXPIRED, "Cathy's preferences");

        preferencesDaoDynamoDB.setInstitutionPreferencesCacheLength(1);

        cachedInstitutionalPreferences = preferencesDaoDynamoDB.findInstitutionPreferencesCache(PRINCIPAL_NAME_EXPIRED);
        assertNull("InstitutionalPreferencesCache should be null", cachedInstitutionalPreferences);
    }

    @Test
    public void cacheInstitutionPreferences() throws Exception {
        Map<String, Object> institutionalPreferences = new HashMap<>();
        institutionalPreferences.put(PREFERENCES_KEY, "Bryan's preferences");
        preferencesDaoDynamoDB.cacheInstitutionPreferences(PRINCIPAL_NAME, institutionalPreferences);
        Map<String, Object> cachedInstitutionalPreferences = preferencesDaoDynamoDB.findInstitutionPreferencesCache(PRINCIPAL_NAME);
        validateInstitutionalPreferences(cachedInstitutionalPreferences, PreferencesDaoDynamoDB.INSTITUTION_PREFERENCES_CACHE, PreferencesDaoDynamoDB.PRINCIPAL_NAME_KEY, PRINCIPAL_NAME, "Bryan's preferences");
    }

    private void validateInstitutionalPreferences(Map<String, Object> institutionalPreferences, String tableName, String keyName, String expectedKeyValue, String expectedPreferences) {
        assertNotNull(tableName + "should not be null", institutionalPreferences);
        assertEquals(keyName + "is not what we expected", expectedKeyValue, institutionalPreferences.get(keyName));
        assertEquals("Preferences are not what we expected", expectedPreferences, institutionalPreferences.get(PREFERENCES_KEY));
    }

    @Test
    public void setInstitutionPreferencesCacheLength() throws Exception {
        preferencesDaoDynamoDB.setInstitutionPreferencesCacheLength(100);
        assertEquals(100, preferencesDaoDynamoDB.getInstitutionPreferencesCacheLength());
    }

    @Test
    public void getInstitutionPreferencesCacheLengthNoneSaved() throws Exception {
        assertEquals(0, preferencesDaoDynamoDB.getInstitutionPreferencesCacheLength());
    }

    @Test
    public void getUserPreferencesNoPreferences() throws Exception {
        Map<String, Object> userPreferences = preferencesDaoDynamoDB.getUserPreferences(PRINCIPAL_NAME);
        assertNull("UserPreferences should be null", userPreferences);
    }

    @Test
    public void saveUserPreferencesString() throws Exception {
        String userPreferences = "{\"user preferences\": \"Bryan's user preferences\"}";
        preferencesDaoDynamoDB.saveUserPreferences(PRINCIPAL_NAME, userPreferences);
        validateUserPreferences("Bryan's user preferences");
    }

    @Test
    public void saveUserPreferencesMap() throws Exception {
        Map<String, Object> userPreferences = new HashMap<>();
        userPreferences.put("user preferences", "Bryan's new user preferences");
        preferencesDaoDynamoDB.saveUserPreferences(PRINCIPAL_NAME, userPreferences);
        validateUserPreferences("Bryan's new user preferences");
    }

    private void validateUserPreferences(String expectedUserPreferences) {
        Map<String, Object> savedUserPreferences = preferencesDaoDynamoDB.getUserPreferences(PRINCIPAL_NAME);
        assertNotNull("UserPreferences should not be null", savedUserPreferences);
        assertEquals("User Preferences are not what we expected", expectedUserPreferences, savedUserPreferences.get("user preferences"));
    }

    private class MockPreferencesDaoDynamodDB extends PreferencesDaoDynamoDB {

        private Map<String, Table> tables = new HashMap<>();

        public MockPreferencesDaoDynamodDB() throws JsonProcessingException {
            setupTable(INSTITUTION_PREFERENCES, INSTITUTION_ID_KEY, buildPreferencesItem());
            setupTable(INSTITUTION_PREFERENCES_CACHE, PRINCIPAL_NAME_KEY, buildExpiredCacheItem());
            setupTable(INSTITUTION_PREFERENCES_CACHE_LENGTH, INSTITUTION_ID_KEY);
            setupTable(USER_PREFERENCES, PRINCIPAL_NAME_KEY);
        }

        private void setupTable(String tableName, String tableKey, Item item) {
            setupTable(tableName, tableKey);
            tables.get(tableName).putItem(item);
        }

        private void setupTable(String tableName, String tableKey) {
            MockTable table = new MockTable(new AmazonDynamoDBClient(), tableName);
            table.setKey(tableKey);
            tables.put(tableName, table);
        }

        private Item buildPreferencesItem() {
            String preferences = "{\"preferences\": \"preferences\"}";
            Item item = Item.fromJSON(preferences);
            item = item.withString(INSTITUTION_ID_KEY, DEFAULT_INSTITUTION_ID);
            return item;
        }

        private Item buildExpiredCacheItem() throws JsonProcessingException {
            Date date = new Date();
            date.setTime(date.getTime() - 1000);

            Map<String, Object> expiredCachedPreferences = new HashMap<>();
            expiredCachedPreferences.put(PRINCIPAL_NAME_KEY, PRINCIPAL_NAME_EXPIRED);
            expiredCachedPreferences.put(DATE_KEY, date);
            expiredCachedPreferences.put(CACHED_KEY,true);
            expiredCachedPreferences.put(PREFERENCES_KEY, "Cathy's preferences");

            ObjectMapper mapper = new ObjectMapper();
            String cachedPreferences = mapper.writeValueAsString(expiredCachedPreferences);

            Item expiredCacheItem = Item.fromJSON(cachedPreferences);
            expiredCacheItem = expiredCacheItem.withString(PRINCIPAL_NAME_KEY, PRINCIPAL_NAME_EXPIRED);
            return expiredCacheItem;
        }

        protected Table retrieveTable(String tableName) {
            return tables.get(tableName);
        }

    }

    private class MockTable extends Table {

        private Map<String, Item> items = new HashMap<>();
        private String key;

        public MockTable(AmazonDynamoDB client, String tableName) {
            super(client, tableName, null);
        }

        public Item getItem(String hashKeyName, Object hashKeyValue) {
            return items.get(hashKeyName + "-" + hashKeyValue);
        }

        public PutItemOutcome putItem(Item item) {
            if (item.asMap().containsKey(getKey())) {
                String itemKey = getKey() + "-" + item.asMap().get(getKey());
                items.put(itemKey, item);
            }
            return null;
        }

        public DeleteItemOutcome deleteItem(String hashKeyName, Object hashKeyValue) {
            items.remove(hashKeyName + "-" + hashKeyValue);
            return null;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}