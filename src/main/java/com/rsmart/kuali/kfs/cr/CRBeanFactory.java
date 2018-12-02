/*
 * Copyright 2008 The Kuali Foundation.
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
package com.rsmart.kuali.kfs.cr;

import java.util.Vector;


import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliationReport;

public class CRBeanFactory {

    public static Vector createBeanCollection() {
        java.util.Vector coll = new java.util.Vector();
        
        CheckReconciliationReport yourBean = new CheckReconciliationReport();
        yourBean.setStatus(CRConstants.CANCELLED);
        yourBean.setBankAccountNumber("1010000");
        yourBean.setCheckDate("11/11/2008");
        yourBean.setSubTotal(12.07);
        yourBean.setCheckNumber("10001");
        yourBean.setCheckMonth("2008/11");
        
        coll.add(yourBean);
        
        CheckReconciliationReport myBean = new CheckReconciliationReport();
        myBean.setStatus(CRConstants.CLEARED);
        myBean.setBankAccountNumber("1010000");
        myBean.setCheckDate("12/12/2008");
        myBean.setSubTotal(100.00);
        myBean.setCheckNumber("10000");
        myBean.setCheckMonth("2008/12");
        
        coll.add(myBean);
        
        CheckReconciliationReport hisBean = new CheckReconciliationReport();
        hisBean.setStatus(CRConstants.ISSUED);
        hisBean.setBankAccountNumber("1010000");
        hisBean.setCheckDate("01/01/2009");
        hisBean.setSubTotal(33.00);
        hisBean.setCheckNumber("10002");
        hisBean.setCheckMonth("2009/01");
        
        coll.add(hisBean);
        
        CheckReconciliationReport herBean = new CheckReconciliationReport();
        herBean.setStatus(CRConstants.CLEARED);
        herBean.setBankAccountNumber("1010000");
        herBean.setCheckDate("01/02/2009");
        herBean.setSubTotal(1000.05);
        herBean.setCheckNumber("10003");
        herBean.setCheckMonth("2009/01");
        
        coll.add(herBean);
        
        CheckReconciliationReport aBean = new CheckReconciliationReport();
        aBean.setStatus(CRConstants.CLEARED);
        aBean.setBankAccountNumber("1111111");
        aBean.setCheckDate("01/02/2009");
        aBean.setSubTotal(99.99);
        aBean.setCheckNumber("10004");
        aBean.setCheckMonth("2009/01");
        
        coll.add(aBean);
        
        return coll;
    }
}
