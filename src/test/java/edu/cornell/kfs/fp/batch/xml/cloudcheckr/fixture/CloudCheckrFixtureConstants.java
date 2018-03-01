package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.cornell.kfs.sys.CUKFSConstants;

public class CloudCheckrFixtureConstants {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(CUKFSConstants.DATE_FORMAT_mm_dd_yyyy_hh_mm_ss_am);
    
    public static final String DATE_FEB_1_2018_MIDNIGHT = "2/1/2018 12:00:00 AM";
    public static final String DATE_JAN_20_2017_MIDNIGHT= "1/20/2017 12:00:00 AM";
    
    public static final double DEPT1_COSTCENTER1_COST = 951.4736250193;
    public static final double DEPT1_COSTCENTER2_COST = 18.7591161721;
    public static final double DEPT1_COSTCENTER3_COST = 4.1275978282;
    public static final double DEPT1_COSTCENTER4_COST = 4.1193331424;
    public static final double DEPT1_COSTCENTER5_COST = 2.4449795375;
    public static final double DEPT2_COSTCENTER1_COST = 266.0180803809;
    public static final double DEPT3_COSTCENTER1_COST = 3855.0375293243;
    
    public static final String ACCOUNT_NONE = "None";
    public static final String ACCOUNT_U353901 = "U353901";
    public static final String ACCOUNT_U353803 = "U353803";
    public static final String ACCOUNT_U353805 = "U353805";
    public static final String ACCOUNT_1503307 = "1503307";
    
    public static final String DEPARTMENT_1_GROUP_VALUE = "AWS-abc (Cornell Dept1)";
    public static final String DEPARTMENT_1_FRIENDLY_NAME = "Cornell Dept1";
    public static final double DEPARTMENT_1_COST = 980.9246516995;
    
    public static final String DEPARTMENT_2_GROUP_VALUE = "AWS-def (Cornell dept2)";
    public static final String DEPARTMENT_2_FRIENDLY_NAME = "Cornell dept2";
    public static final double DEPARTMENT_2_COST = 266.0180803809;
    
    public static final String DEPARTMENT_3_GROUP_VALUE = "AWS-ghi (Cornell dept3)";
    public static final String DEPARTMENT_3_FRIENDLY_NAME = "Cornell dept3";
    public static final double DEPARTMENT_3_COST = 3855.0375293243;
    
    public static final double CORNELL_TEST_FILE_TOTAL = DEPARTMENT_1_COST + DEPARTMENT_2_COST + DEPARTMENT_3_COST;
    public static final double CORNELL_TEST_FILE_MAX = DEPARTMENT_3_COST;
    public static final double CORNELL_TEST_FILE_MIN = DEPARTMENT_2_COST;
    public static final double CORNELL_TEST_FILE_AVG = CORNELL_TEST_FILE_TOTAL/3;
    
    public static final double CLOUDCHECKR_EXAMPLE_TOTAL = 30.7983416200;
    public static final double CLOUDCHECKR_EXAMPLE_MAX = 30.7983416200;
    public static final double CLOUDCHECKR_EXAMPLE_MIN = 30.7983416200;
    public static final double CLOUDCHECKR_EXAMPLE_AVG = 30.798698086413034906518967198;
    
    public static final String CLOUDCHECKR_EXAMPLE_GROUP_NAME_SERVICE = "Service";
    public static final String CLOUDCHECKR_EXAMPLE_GROUP_NAME_CREATE_BY = "aws:createdBy";
    
    public static final String CLOUDCHECKR_EXAMPLE_GROUP_VALUE_S3 = "AmazonS3";
    public static final String CLOUDCHECKR_EXAMPLE_GROUP_FRIENDLY_NAME_S3 = "S3";
    public static final double CLOUDCHECKR_EXAMPLE_S3_COST = 0.1099985400;
    public static final String CLOUDCHECKR_EXAMPLE_GROUP_VALUE_IAM_MIKEB = "IAMUser:AIDAI5X7LBFKDHYABCDE:mikeb";
    public static final double CLOUDCHECKR_EXAMPLE_GROUP_MIKEB_COST = 0.1083478400;
    //public static final double CLOUDCHECK_EXAMPLE_GROUP_MIKEB_USAGE = 418.5352970600;
    
    public static final String CLOUDCHECKR_EXAMPLE_GROUP_VALUE_EC2 = "AmazonEC2";
    public static final String CLOUDCHECKR_EXAMPLE_GROUP_FRIENDLY_NAME_EC2 = "EC2";
    public static final double CLOUDCHECKR_EXAMPLE_EC2_COST = 22.6078574000;
    public static final String CLOUDCHECKR_EXAMPLE_GROUP_VALUE_IAM_VANW = "IAMUser:AIDAI4NZVQK6IW45ABCDE:paul.vanw";
    public static final double CLOUDCHECKR_EXAMPLE_GROUP_VANW_COST = 0.0750056100;
    //public static final double CLOUDCHECK_EXAMPLE_GROUP_VANW_USAGE = 24.0000619600;
}
