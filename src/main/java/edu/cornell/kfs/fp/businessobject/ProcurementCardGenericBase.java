/*
 * Copyright 2012 The Kuali Foundation.
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
package edu.cornell.kfs.fp.businessobject;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

public class ProcurementCardGenericBase extends PersistableBusinessObjectBase
{

	private static final long serialVersionUID = 1L;
    private Integer genericId;
    protected String genericAddendumData;

    public ProcurementCardGenericBase()
    {
        super();
    }

    public String getGenericAddendumData()
    {
        return genericAddendumData;
    }

    public void setGenericAddendumData(String genericAddendumData)
    {
        this.genericAddendumData = genericAddendumData;
    }

    public Integer getGenericId()
    {
        return genericId;
    }

    public void setGenericId(Integer genericId)
    {
        this.genericId = genericId;
    }

}
