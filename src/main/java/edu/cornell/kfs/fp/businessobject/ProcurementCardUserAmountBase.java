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

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

public class ProcurementCardUserAmountBase extends PersistableBusinessObjectBase
{
	private static final long serialVersionUID = 1L;
    private Integer pcardUserAmountId;
    protected KualiDecimal userAmount;

    public ProcurementCardUserAmountBase()
    {
        super();

        this.userAmount = new KualiDecimal(0);
    }

    public KualiDecimal getUserAmount()
    {
        return userAmount;
    }

    public void setUserAmount(KualiDecimal userAmount)
    {
        this.userAmount = userAmount;
    }

    public void setUserAmount(String userAmount)
    {
        if (StringUtils.isNotBlank(userAmount))
        {
            this.userAmount = new KualiDecimal(userAmount);
        }
        else
        {
            this.userAmount = KualiDecimal.ZERO;
        }
    }

    public Integer getPcardUserAmountId()
    {
        return pcardUserAmountId;
    }

    public void setPcardUserAmountId(Integer pcardUserAmountId)
    {
        this.pcardUserAmountId = pcardUserAmountId;
    }

}
