package edu.cornell.kfs.kim;

public final class CuKimConstants {

    public static final String PHONE_NUMBER_PATTERN_TYPE_NAME = "cuKimPhoneNumber";

    public static final class PersonInquirySections {
        public static final String NAMES = "Names";
        public static final String ADDRESSES = "Addresses";
        public static final String PHONE_NUMBERS = "Phone Numbers";
        public static final String EMAIL_ADDRESSES = "Email Addresses";
    }

    private CuKimConstants() {
        throw new UnsupportedOperationException("do not call");
    }

}
