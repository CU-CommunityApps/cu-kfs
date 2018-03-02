package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import java.util.List;

import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupingByTime;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentFixtureUtils;

public enum GroupingByTimeFixture {
    DEPT1_GROUP1(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT1_COSTDATE1), 
            groupingFixtureArrayBuilder(GroupingFixture.GROUPING_DEPT1_ACCOUNT, 
                    GroupingFixture.GROUPING_DEPT1_COSTCENTER1)),
    DEPT1_GROUP2(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT1_COSTDATE2), 
            groupingFixtureArrayBuilder(GroupingFixture.GROUPING_DEPT1_ACCOUNT, 
                    GroupingFixture.GROUPING_DEPT1_COSTCENTER2)),
    DEPT1_GROUP3(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT1_COSTDATE3), 
            groupingFixtureArrayBuilder(GroupingFixture.GROUPING_DEPT1_ACCOUNT, 
                    GroupingFixture.GROUPING_DEPT1_COSTCENTER3)),
    DEPT1_GROUP4(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT1_COSTDATE4), 
            groupingFixtureArrayBuilder(GroupingFixture.GROUPING_DEPT1_ACCOUNT, 
                    GroupingFixture.GROUPING_DEPT1_COSTCENTER4)),
    DEPT1_GROUP5(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT1_COSTDATE5), 
            groupingFixtureArrayBuilder(GroupingFixture.GROUPING_DEPT1_ACCOUNT, 
                    GroupingFixture.GROUPING_DEPT1_COSTCENTER5)),
    DEPT2_GROUP1(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT2_COSTDATE1), 
            groupingFixtureArrayBuilder(GroupingFixture.GROUPING_DEPT2_ACCOUNT, 
                    GroupingFixture.GROUPING_DEPT2_COSTCENTER1)),
    DEPT3_GROUP1(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT3_COSTDATE1), 
            groupingFixtureArrayBuilder(GroupingFixture.GROUPING_DEPT3_ACCOUNT, 
                    GroupingFixture.GROUPING_DEPT3_COSTCENTER1));
    
    
    public final List<GroupCostDateFixture> groupCostDateFixtures;
    public final List<GroupingFixture> groupingFixtures;
    
    private GroupingByTimeFixture(GroupCostDateFixture[] groupCostDateFixtureArray, GroupingFixture[] groupingFixtureArray) {
        groupCostDateFixtures = AccountingXmlDocumentFixtureUtils.toImmutableList(groupCostDateFixtureArray);
        groupingFixtures = AccountingXmlDocumentFixtureUtils.toImmutableList(groupingFixtureArray);
    }
    
    public GroupingByTime toGroupingByTime() {
        GroupingByTime costsByTime = new GroupingByTime();
        groupingFixtures.stream().forEach(group -> costsByTime.getGroupings().add(group.toGrouping()));
        groupCostDateFixtures.stream().forEach(costDate -> costsByTime.getGroupCostDates().add(costDate.toGroupCostDate()));
        return costsByTime;
    }
    
    private static GroupCostDateFixture[] groupCostDateFixtureArrayBuilder(GroupCostDateFixture...fixtures) {
        return fixtures;
    }
    
    private static GroupingFixture[] groupingFixtureArrayBuilder(GroupingFixture...fixtures) {
        return fixtures;
    }

}
