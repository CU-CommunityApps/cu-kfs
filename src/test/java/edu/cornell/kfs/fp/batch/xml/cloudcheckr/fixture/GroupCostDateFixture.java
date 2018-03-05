package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupCostDate;

public enum GroupCostDateFixture {
    DEPT1_COSTDATE1(CloudCheckrFixtureConstants.DATE_FEB_1_2018_MIDNIGHT, CloudCheckrFixtureConstants.DEPT1_COSTCENTER1_COST),
    DEPT1_COSTDATE2(CloudCheckrFixtureConstants.DATE_FEB_1_2018_MIDNIGHT, CloudCheckrFixtureConstants.DEPT1_COSTCENTER2_COST),
    DEPT1_COSTDATE3(CloudCheckrFixtureConstants.DATE_FEB_1_2018_MIDNIGHT, CloudCheckrFixtureConstants.DEPT1_COSTCENTER3_COST),
    DEPT1_COSTDATE4(CloudCheckrFixtureConstants.DATE_FEB_1_2018_MIDNIGHT, CloudCheckrFixtureConstants.DEPT1_COSTCENTER4_COST),
    DEPT1_COSTDATE5(CloudCheckrFixtureConstants.DATE_FEB_1_2018_MIDNIGHT, CloudCheckrFixtureConstants.DEPT1_COSTCENTER5_COST),
    DEPT2_COSTDATE1(CloudCheckrFixtureConstants.DATE_FEB_1_2018_MIDNIGHT, CloudCheckrFixtureConstants.DEPT2_COSTCENTER1_COST),
    DEPT3_COSTDATE1(CloudCheckrFixtureConstants.DATE_FEB_1_2018_MIDNIGHT, CloudCheckrFixtureConstants.DEPT3_COSTCENTER1_COST),
    CLOUDCHECKR_COSTDATE1(CloudCheckrFixtureConstants.DATE_JAN_20_2017_MIDNIGHT, CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_VANW_COST,
            24.0000619600),
    CLOUDCHECKR_COSTDATE2(CloudCheckrFixtureConstants.DATE_JAN_20_2017_MIDNIGHT, CloudCheckrFixtureConstants.CLOUDCHECKR_EXAMPLE_GROUP_MIKEB_COST,
            418.5352970600);
    
    public final Date date;
    public final KualiDecimal cost;
    public final Double usageQuantity;
    
    GroupCostDateFixture(String dateString, double cost, double usageQuantity) {
        this.cost = new KualiDecimal(cost);
        this.usageQuantity = new Double(usageQuantity);
        if (StringUtils.isNotBlank(dateString)) {
            this.date = DateTime.parse(dateString, CloudCheckrFixtureConstants.DATE_FORMATTER).toDate();
        } else {
            this.date = null;
        }
    }
    
    GroupCostDateFixture(String dateString, double cost) {
        this(dateString, cost, 0);
    }
    
    public GroupCostDate toGroupCostDate() {
        GroupCostDate costDate = new GroupCostDate();
        costDate.setCost(cost);
        costDate.setDate(date);
        costDate.setUsageQuantity(usageQuantity);
        return costDate;
    }
}
