package edu.cornell.kfs.kim;

public final class CuKimConstants {

    public static final String PHONE_NUMBER_PATTERN_TYPE_NAME = "cuKimPhoneNumber";
    public static final String CORNELL_IT_CAMPUS = "IT";
    public static final String DEPARTMENT_CODE_PREFIX = "IT-";
    public static final int MAX_ADDRESS_LINE_LENGTH = 40;

    public static final class EdwAffiliations {
        public static final String ACADEMIC = "A";
        public static final String AFFILIATE = "I";
        public static final String ALUMNI = "L";
        public static final String EXCEPTION = "X";
        public static final String FACULTY = "F";
        public static final String STAFF = "E";
        public static final String STUDENT = "S";
    }

    public static final class KfsAffiliations {
        public static final String ACADEMIC = "ACADEM";
        public static final String AFFILIATE = "AFLT";
        public static final String ALUMNI = "ALUMNI";
        public static final String EXCEPTION = "EXCPTN";
        public static final String FACULTY = "FCLTY";
        public static final String STAFF = "STAFF";
        public static final String STUDENT = "STDNT";
        public static final String NONE = "NONE";
    }

    public static final class AffiliationStatuses {
        public static final String ACTIVE = "A";
        public static final String INACTIVE = "I";
        public static final String RETIRED = "R";
        public static final String NONEXISTENT = "N";
    }

    public static final class AddressTypes {
        public static final String HOME = "HM";
        public static final String CAMPUS = "CMP";
    }

    private CuKimConstants() {
        throw new UnsupportedOperationException("do not call");
    }

}
