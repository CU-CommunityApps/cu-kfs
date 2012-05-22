/*
 * Copyright 2012 The Kuali Foundation.
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
package org.kuali.kfs.gl.batch.service.impl;


import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.kuali.kfs.gl.batch.service.ReversionUnitOfWorkService;
import org.kuali.kfs.gl.businessobject.ReversionUnitOfWork;
import org.kuali.kfs.gl.businessobject.ReversionUnitOfWorkCategoryAmount;
import org.kuali.rice.kns.service.BusinessObjectService;

public abstract class ReversionUnitOfWorkServiceImpl implements ReversionUnitOfWorkService {

    static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ReversionUnitOfWorkServiceImpl.class);

    
    BusinessObjectService businessObjectService;

    
    public ReversionUnitOfWorkServiceImpl() {
        super();
    }

    public ReversionUnitOfWork loadCategories(ReversionUnitOfWork orgRevUnitOfWork) {
        Collection categoryAmounts = businessObjectService.findMatching(ReversionUnitOfWorkCategoryAmount.class, orgRevUnitOfWork.toStringMapper());
        Map<String, ReversionUnitOfWorkCategoryAmount> categories = orgRevUnitOfWork.getCategoryAmounts();
        Iterator iter = categoryAmounts.iterator();
        while (iter.hasNext()) {
            ReversionUnitOfWorkCategoryAmount catAmount = (ReversionUnitOfWorkCategoryAmount) iter.next();
            categories.put(catAmount.getCategoryCode(), catAmount);
        }
        return orgRevUnitOfWork;
    }

    public <T extends ReversionUnitOfWork> void save(T orgRevUnitOfWork) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving org reversion summary for " + orgRevUnitOfWork.toString() + "; its category keys are: " + orgRevUnitOfWork.getCategoryAmounts().keySet());
        }
        getBusinessObjectService().save(orgRevUnitOfWork);
        for (String category: orgRevUnitOfWork.getCategoryAmounts().keySet()) {
            final ReversionUnitOfWorkCategoryAmount categoryAmount = orgRevUnitOfWork.getCategoryAmounts().get(category);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Saving category amount for " + categoryAmount.toString());
            }
            getBusinessObjectService().save(categoryAmount);
        }
    }
    
    /**
     * Gets the businessObjectService attribute.
     * 
     * @return Returns the businessObjectService.
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Sets the businessObjectService attribute value.
     * 
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    
    public abstract void destroyAllUnitOfWorkSummaries();


}