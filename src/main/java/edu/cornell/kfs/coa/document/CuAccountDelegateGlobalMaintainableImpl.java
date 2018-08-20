/*
 * Copyright 2007 The Kuali Foundation
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
package edu.cornell.kfs.coa.document;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.coa.document.AccountDelegateGlobalMaintainableImpl;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable;

/**
 * This class overrides the base {@link FinancialSystemGlobalMaintainable} to generate the specific maintenance locks for Global delegates
 * and to help with using delegate models
 * 
 * @see OrganizationRoutingModelName
 */
public class CuAccountDelegateGlobalMaintainableImpl extends AccountDelegateGlobalMaintainableImpl {
    
    /**
     * This creates the particular locking representation for this global document.
     */
    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        // create locking rep for each combination of account and object code
        List<MaintenanceLock> maintenanceLocks = new ArrayList<>();
        
        return maintenanceLocks;
    }    
}
