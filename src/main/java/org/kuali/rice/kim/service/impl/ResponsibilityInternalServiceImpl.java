/*
 * Copyright 2007-2009 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kim.service.impl;

import java.util.Set;

import org.kuali.rice.kim.bo.role.impl.RoleMemberImpl;
import org.kuali.rice.kim.service.ResponsibilityInternalService;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.KNSServiceLocator;

/**
 * This is an optimized implementatino of the ResponsibilityInternalService from Eric Westfall.
 * 
 *   Per Eric, "As far as functional impact, this essentially means that when you modify role membership 
 *   and there are existing documents enroute against members of that role, that [wouldn't] get automagically 
 *   re-resolved (though you could still do it manually from document operations using the 
 *   "queue document requeuer" button)."
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class ResponsibilityInternalServiceImpl implements ResponsibilityInternalService {

	private BusinessObjectService businessObjectService;

	public void saveRoleMember(RoleMemberImpl roleMember){
    	getBusinessObjectService().save( roleMember );
	}

	public void removeRoleMember(RoleMemberImpl roleMember){
    	getBusinessObjectService().delete( roleMember );
	}

	public void updateActionRequestsForRoleChange(String roleId) {
    	// this method has been evil to us, we're disabling it with extreme prejudice
	}

	public void updateActionRequestsForResponsibilityChange(Set<String> responsibilityIds) {
        // this method has *also* been evil to us, we shall disable it as well
	}

	protected BusinessObjectService getBusinessObjectService() {
		if ( businessObjectService == null ) {
			businessObjectService = KNSServiceLocator.getBusinessObjectService();
		}
		return businessObjectService;
	}

}
