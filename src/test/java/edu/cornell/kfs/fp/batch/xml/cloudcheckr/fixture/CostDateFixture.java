package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.xml.cloudcheckr.CostDate;

public enum CostDateFixture {
    DEPT1_COSTDATE1("2/1/2018 12:00:00 AM", 951.4736250193),
    DEPT1_COSTDATE2("2/1/2018 12:00:00 AM", 18.7591161721),
    DEPT1_COSTDATE3("2/1/2018 12:00:00 AM", 4.1275978282),
    DEPT1_COSTDATE4("2/1/2018 12:00:00 AM", 4.1193331424),
    DEPT1_COSTDATE5("2/1/2018 12:00:00 AM", 2.4449795375),
    DEPT2_COSTDATE1("2/1/2018 12:00:00 AM", 266.0180803809),
    DEPT3_COSTDATE1("2/1/2018 12:00:00 AM", 3855.0375293243);
    
    public final Date date;
    public final KualiDecimal cost;
    public final Long usageQuantity;
    
    CostDateFixture(String dateString, double cost, long usageQuantity) {
        this.cost = new KualiDecimal(cost);
        this.usageQuantity = new Long(usageQuantity);
        if (StringUtils.isNotBlank(dateString)) {
            this.date = DateTime.parse(dateString, CloudCheckrFixtureConstants.DATE_FORMATTER).toDate();
        } else {
            this.date = null;
        }
    }
    
    CostDateFixture(String dateString, double cost) {
        this(dateString, cost, 0);
    }
    
    public CostDate toCostDate() {
        CostDate costDate = new CostDate();
        costDate.setCost(cost);
        costDate.setDate(date);
        costDate.setUsageQuantity(usageQuantity);
        return costDate;
    }
}
