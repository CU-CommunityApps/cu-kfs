package edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture;

import java.util.List;

import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupingByTime;
import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;

public enum GroupingByTimeFixture {
    DEPT1_GROUP1(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT1_COSTDATE1), 
            groupingFixtureArrayBuilder(GroupingFixture.DEPT1_ACCOUNT, 
                    GroupingFixture.DEPT1_COSTCENTER1)),
    DEPT1_GROUP2(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT1_COSTDATE2), 
            groupingFixtureArrayBuilder(GroupingFixture.DEPT1_ACCOUNT, 
                    GroupingFixture.DEPT1_COSTCENTER2)),
    DEPT1_GROUP3(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT1_COSTDATE3), 
            groupingFixtureArrayBuilder(GroupingFixture.DEPT1_ACCOUNT, 
                    GroupingFixture.DEPT1_COSTCENTER3)),
    DEPT1_GROUP4(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT1_COSTDATE4), 
            groupingFixtureArrayBuilder(GroupingFixture.DEPT1_ACCOUNT, 
                    GroupingFixture.DEPT1_COSTCENTER4)),
    DEPT1_GROUP5(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT1_COSTDATE5), 
            groupingFixtureArrayBuilder(GroupingFixture.DEPT1_ACCOUNT, 
                    GroupingFixture.DEPT1_COSTCENTER5)),
    DEPT2_GROUP1(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT2_COSTDATE1), 
            groupingFixtureArrayBuilder(GroupingFixture.DEPT2_ACCOUNT, 
                    GroupingFixture.DEPT2_COSTCENTER1)),
    DEPT3_GROUP1(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.DEPT3_COSTDATE1), 
            groupingFixtureArrayBuilder(GroupingFixture.DEPT3_ACCOUNT, 
                    GroupingFixture.DEPT3_COSTCENTER1)),
    CLOUDCHECKR_GROUP1(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.CLOUDCHECKR_COSTDATE1), 
            groupingFixtureArrayBuilder(GroupingFixture.CLOUDCHECKR_SERVICE_EC2, 
                    GroupingFixture.CLOUDCHECKR_SERVICE_CREATED_BY_VANW)),
    CLOUDCHECKR_GROUP2(groupCostDateFixtureArrayBuilder(GroupCostDateFixture.CLOUDCHECKR_COSTDATE2), 
            groupingFixtureArrayBuilder(GroupingFixture.CLOUDCHECKR_SERVICE_S3, 
                    GroupingFixture.CLOUDCHECKR_SERVICE_CREATED_BY_MIKEB));
    
    
    public final List<GroupCostDateFixture> groupCostDateFixtures;
    public final List<GroupingFixture> groupingFixtures;
    
    private GroupingByTimeFixture(GroupCostDateFixture[] groupCostDateFixtureArray, GroupingFixture[] groupingFixtureArray) {
        groupCostDateFixtures = XmlDocumentFixtureUtils.toImmutableList(groupCostDateFixtureArray);
        groupingFixtures = XmlDocumentFixtureUtils.toImmutableList(groupingFixtureArray);
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
