package edu.cornell.kfs.kim.impl.responsibility;

import java.util.Set;

import org.kuali.kfs.kim.impl.responsibility.ResponsibilityInternalServiceImpl;
import org.kuali.kfs.kim.impl.role.RoleMember;

/**
 * ====
 * CU Customization (KFSPTS-535) (Updated for compatibility with the migration of KIM to KFS):
 * 
 * This is an optimized implementation of the ResponsibilityInternalService from Eric Westfall.
 * 
 *   Per Eric, "As far as functional impact, this essentially means that when you modify role membership 
 *   and there are existing documents enroute against members of that role, that [they wouldn't] get automagically 
 *   re-resolved (though you could still do it manually from document operations using the 
 *   "queue document requeuer" button)."
 * ====
 */
public class CuResponsibilityInternalServiceImpl extends ResponsibilityInternalServiceImpl {

    /**
     * Overridden to only perform the member-saving portion from the superclass and not the action request updates.
     */
    @Override
    public RoleMember saveRoleMember(RoleMember roleMember) {
        return businessObjectService.save(roleMember);
    }

    /**
     * Overridden to only perform the inactivation portion from the superclass and not the action request updates.
     */
    @Override
    public void removeRoleMember(RoleMember roleMember) {
        roleMember.setActiveToDateValue(new java.sql.Timestamp(System.currentTimeMillis()));
        businessObjectService.save(roleMember);
    }

    /**
     * Overridden to make this a no-op method.
     */
    @Override
    public void updateActionRequestsForResponsibilityChange(Set<String> responsibilityIds) {
        // Do nothing.
    }

}
