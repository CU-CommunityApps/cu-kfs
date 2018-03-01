package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import java.util.List;

import edu.cornell.kfs.fp.batch.xml.cloudcheckr.CostsByTime;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentFixtureUtils;

public enum CostsByTimeFixture {
    DEPT1_GROUP1(costDateFixtureArrayBuilder(CostDateFixture.DEPT1_COSTDATE1), 
            CostsByTimeGroupingFixtureArrayBuilder(CostsByTimeGroupingFixture.GROUPING_DEPT1_ACCOUNT, 
                    CostsByTimeGroupingFixture.GROUPING_DEPT1_COSTCENTER1)),
    DEPT1_GROUP2(costDateFixtureArrayBuilder(CostDateFixture.DEPT1_COSTDATE2), 
            CostsByTimeGroupingFixtureArrayBuilder(CostsByTimeGroupingFixture.GROUPING_DEPT1_ACCOUNT, 
                    CostsByTimeGroupingFixture.GROUPING_DEPT1_COSTCENTER2)),
    DEPT1_GROUP3(costDateFixtureArrayBuilder(CostDateFixture.DEPT1_COSTDATE3), 
            CostsByTimeGroupingFixtureArrayBuilder(CostsByTimeGroupingFixture.GROUPING_DEPT1_ACCOUNT, 
                    CostsByTimeGroupingFixture.GROUPING_DEPT1_COSTCENTER3)),
    DEPT1_GROUP4(costDateFixtureArrayBuilder(CostDateFixture.DEPT1_COSTDATE4), 
            CostsByTimeGroupingFixtureArrayBuilder(CostsByTimeGroupingFixture.GROUPING_DEPT1_ACCOUNT, 
                    CostsByTimeGroupingFixture.GROUPING_DEPT1_COSTCENTER4)),
    DEPT1_GROUP5(costDateFixtureArrayBuilder(CostDateFixture.DEPT1_COSTDATE5), 
            CostsByTimeGroupingFixtureArrayBuilder(CostsByTimeGroupingFixture.GROUPING_DEPT1_ACCOUNT, 
                    CostsByTimeGroupingFixture.GROUPING_DEPT1_COSTCENTER5)),
    DEPT2_GROUP1(costDateFixtureArrayBuilder(CostDateFixture.DEPT2_COSTDATE1), 
            CostsByTimeGroupingFixtureArrayBuilder(CostsByTimeGroupingFixture.GROUPING_DEPT2_ACCOUNT, 
                    CostsByTimeGroupingFixture.GROUPING_DEPT2_COSTCENTER1)),
    DEPT3_GROUP1(costDateFixtureArrayBuilder(CostDateFixture.DEPT3_COSTDATE1), 
            CostsByTimeGroupingFixtureArrayBuilder(CostsByTimeGroupingFixture.GROUPING_DEPT3_ACCOUNT, 
                    CostsByTimeGroupingFixture.GROUPING_DEPT3_COSTCENTER1));
    
    
    public final List<CostDateFixture> costDates;
    public final List<CostsByTimeGroupingFixture> timeGroupings;
    
    private CostsByTimeFixture(CostDateFixture[] costDatesArray, CostsByTimeGroupingFixture[] timeGroupingsArray) {
        costDates = AccountingXmlDocumentFixtureUtils.toImmutableList(costDatesArray);
        timeGroupings = AccountingXmlDocumentFixtureUtils.toImmutableList(timeGroupingsArray);
    }
    
    public CostsByTime toCostsByTime() {
        CostsByTime costsByTime = new CostsByTime();
        timeGroupings.stream().forEach(group -> costsByTime.getGroupByTimeGroupings().add(group.toCostsByTimeGrouping()));
        costDates.stream().forEach(costDate -> costsByTime.getCostDates().add(costDate.toCostDate()));
        return costsByTime;
    }
    
    private static CostDateFixture[] costDateFixtureArrayBuilder(CostDateFixture...costDateFixtures) {
        return costDateFixtures;
    }
    
    private static CostsByTimeGroupingFixture[] CostsByTimeGroupingFixtureArrayBuilder(CostsByTimeGroupingFixture...costDateFixtures) {
        return costDateFixtures;
    }

}
