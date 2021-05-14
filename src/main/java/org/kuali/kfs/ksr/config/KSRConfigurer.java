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
package org.kuali.kfs.ksr.config;

import java.util.Collections;

import org.kuali.rice.core.api.config.module.RunMode;
import org.kuali.rice.core.framework.config.module.ModuleConfigurer;

/**
 * ====
 * CU Customization:
 * This file has been remediated for Rice 2.x compatibility by the Cynergy team.
 * ====
 * 
 * Configurer for the Kuali Security Request Module
 * 
 * @author rSmart Development Team
 */
public class KSRConfigurer extends ModuleConfigurer {
    
    public KSRConfigurer() {
        super("KSR");
        
        //setModuleName( "KSR" );
        //setHasWebInterface( true );
        
        // Since we currently have the KSR module specific to the Cynergy server,
        // we are only allowing LOCAL run mode.
        setValidRunModes(Collections.singletonList(RunMode.LOCAL));
    }

}
