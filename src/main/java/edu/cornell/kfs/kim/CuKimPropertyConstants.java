package edu.cornell.kfs.kim;

public final class CuKimPropertyConstants {

    public static final class SharedProperties {
        public static final String COUNTRY_CODE = "countryCode";
    }

    public static final class EntityName {
        public static final String NAME_PREFIX = "namePrefix";
        public static final String FIRST_NAME = "firstName";
        public static final String MIDDLE_NAME = "middleName";
        public static final String LAST_NAME = "lastName";
        public static final String NAME_SUFFIX = "nameSuffix";
    }

    public static final class EntityAddress {
        public static final String LINE_1 = "line1";
        public static final String LINE_2 = "line2";
        public static final String LINE_3 = "line3";
        public static final String CITY = "city";
        public static final String STATE_PROVINCE_CODE = "stateProvinceCode";
        public static final String POSTAL_CODE = "postalCode";
    }

    public static final class EntityPhone {
        public static final String PHONE_NUMBER = "phoneNumber";
        public static final String EXTENSION_NUMBER = "extensionNumber";
    }

    public static final class EntityEmail {
        public static final String EMAIL_ADDRESS = "emailAddress";
    }

    private CuKimPropertyConstants() {
        throw new UnsupportedOperationException("do not call");
    }

}
