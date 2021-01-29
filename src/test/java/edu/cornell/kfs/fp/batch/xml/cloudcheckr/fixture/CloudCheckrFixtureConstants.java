package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import java.util.Locale;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.cornell.kfs.sys.CUKFSConstants;

public class CloudCheckrFixtureConstants {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(CUKFSConstants.DATE_FORMAT_mm_dd_yyyy_hh_mm_ss_am).withLocale(Locale.US);
    
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
    public static final String ACCOUNT_165833X = "165833X";
    public static final String ACCOUNT_STRING_R583805_70170 = "R583805-70170";
    public static final String ACCOUNT_STRING_R589966_70170 = "R589966-70170";
    public static final String ACCOUNT_STRING_R583805_533X = "R583805-533X";
    public static final String ACCOUNT_STRING_IT_R589966_NONCA_1000 = "IT*R589966*NONCA*1000**EB-PLGIFT*AEH56";
    public static final String ACCOUNT_STRING_IT_1023715_97601_4020 = "IT*1023715*97601*4020*109**AEH56";
    public static final String ACCOUNT_STRING_IT_1023715_97601_4020_10X = "IT*1023715*97601*4020*10X**AEH56";
    public static final String ACCOUNT_STRING_IT_R589966_NONCX_1000 = "IT*R589966*NONCX*1000**EB-PLGIFX*AEH56";
    public static final String ACCOUNT_STRING_CS_J801000 = "CS*J801000";
    public static final String ACCOUNT_STRING_CS_J801000_SHAN_6600 = "CS*J801000*SHAN*6600***";
    public static final String ACCOUNT_J80100X = "J80100X";
    public static final String ACCOUNT_STRING_IT_1023715_97601_4020_109 = "IT*1023715*97601*4020*109**AEH56*foo";
    
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
    
    public static final String CLOUDCHECKR_EXAMPLE_GROUP_VALUE_EC2 = "AmazonEC2";
    public static final String CLOUDCHECKR_EXAMPLE_GROUP_FRIENDLY_NAME_EC2 = "EC2";
    public static final double CLOUDCHECKR_EXAMPLE_EC2_COST = 22.6078574000;
    public static final String CLOUDCHECKR_EXAMPLE_GROUP_VALUE_IAM_VANW = "IAMUser:AIDAI4NZVQK6IW45ABCDE:paul.vanw";
    public static final double CLOUDCHECKR_EXAMPLE_GROUP_VANW_COST = 0.0750056100;
}
