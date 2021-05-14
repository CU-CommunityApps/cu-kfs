/*
 * Copyright 2007 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.ksr.bo.options;

import org.kuali.kfs.ksr.KsrConstants;
import org.kuali.kfs.ksr.service.KSRServiceLocator;
import org.kuali.rice.krad.data.platform.MaxValueIncrementerFactory;
import org.kuali.rice.krad.valuefinder.ValueFinder;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

/**
 * ====
 * CU Customization:
 * Copied over the version of this class from a more up-to-date rSmart KSR repository,
 * since it contains some fixes and features that are not present in the older KSR repository.
 * 
 * Also remediated this class as needed for Rice 2.x compatibility.
 * ====
 * 
 * Get the current sequence number of Security Group
 * 
 * @author rSmart Development Team
 */
public class SecurityGroupIDFinder implements ValueFinder {

    @Override
    public String getValue() {
        DataFieldMaxValueIncrementer incrementer = MaxValueIncrementerFactory.getIncrementer(
                KSRServiceLocator.getDataSource(), KsrConstants.SECURITY_GROUP_SEQ_NAME);
        return incrementer.nextStringValue();
    }

}
