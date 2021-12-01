package edu.cornell.kfs.kim.impl.group;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.group.GroupMember;
import org.kuali.kfs.kim.impl.group.GroupServiceImpl;

public class CuGroupServiceImpl extends GroupServiceImpl {

    /**
     * FINP-7432 changes from KualiCo patch release 2021-03-11 backported onto original
     * KEW-to-KFS KualiCo patch release 2021-01-28 version of the method.
     */
    @Override
    protected List<Group> getDirectParentGroups(String groupId) {
        if (groupId == null) {
            return Collections.emptyList();
        }
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KIMPropertyConstants.GroupMember.MEMBER_ID, groupId);
        criteria.put(KIMPropertyConstants.GroupMember.MEMBER_TYPE_CODE,
                KimConstants.KimGroupMemberTypes.GROUP_MEMBER_TYPE.getCode());

        List<GroupMember> groupMembers = (List<GroupMember>) businessObjectService.findMatching(
                GroupMember.class, criteria);
        Set<String> matchingGroupIds = new HashSet<>();
        // filter to active groups
        for (GroupMember gm : groupMembers) {
            if (gm.isActive(new Timestamp(System.currentTimeMillis()))) {
                matchingGroupIds.add(gm.getGroupId());
            }
        }
        if (!matchingGroupIds.isEmpty()) {
            return getGroups(matchingGroupIds);
        }
        return Collections.emptyList();
    }

}
