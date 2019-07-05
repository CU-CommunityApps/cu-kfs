/*
 * Copyright 2011 The Kuali Foundation.
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
package com.rsmart.kuali.kfs.module.ld.businessobject.defaultvalue;

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.valuefinder.DefaultValueFinder;
import org.kuali.kfs.module.ld.batch.LaborEnterpriseFeedStep;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;

import com.rsmart.kuali.kfs.module.ld.LdConstants;

public class FiscalYearFinder implements DefaultValueFinder {

    private ParameterService parameterService;
    
    /**
     * @see org.kuali.kfs.kns.lookup.valueFinder.ValueFinder#getValue()
     */
    @Override
    public String getDefaultValue() {
        String offsetParmValue = getParameterService().getParameterValueAsString(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_CALCULATION_OFFSET);
        
        if(offsetParmValue.equalsIgnoreCase("n")) {
            return "";
        }
        return SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear().toString();
    }

    /**
     * Gets the parameterService attribute. 
     * @return Returns the parameterService.
     */
    public ParameterService getParameterService() {
        if(parameterService == null){
            parameterService = (ParameterService)GlobalResourceLoader.getService( "parameterService" );
        }
        return parameterService;
    }
    
}
