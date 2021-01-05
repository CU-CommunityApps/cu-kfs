/*
 * Copyright 2008 The Kuali Foundation
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
package edu.cornell.kfs.fp.document.service;

import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.kim.api.identity.Person;

import edu.cornell.kfs.fp.businessobject.CuDisbursementPayee;

import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;

/**
 * define a set of service methods related to disbursement payee
 */
public interface CuDisbursementVoucherPayeeService extends org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService {

    /* determine whether the given payee is an student
    * 
    * @param dvPayeeDetail the given payee
    * @return true if the given payee is an student; otherwise, false
    */
   public boolean isStudent(CuDisbursementVoucherPayeeDetail dvPayeeDetail);

   /**
    * determine whether the given payee is an student
    * 
    * @param payee the given payee
    * @return true if the given payee is an student; otherwise, false
    */
   public boolean isStudent(CuDisbursementPayee payee);

   /**
    * determine whether the given payee is an alumni
    * 
    * @param dvPayeeDetail the given payee
    * @return true if the given payee is an alumni; otherwise, false
    */
   public boolean isAlumni(CuDisbursementVoucherPayeeDetail dvPayeeDetail);

   /**
    * determine whether the given payee is an alumni
    * 
    * @param payee the given payee
    * @return true if the given payee is an alumni; otherwise, false
    */
   public boolean isAlumni(CuDisbursementPayee payee);

   
   //public CuDisbursementPayee getPayeeFromPerson(Person personDetail, String payeeTypeCode);
   
   public CuDisbursementPayee getPayeeFromVendor(VendorDetail vendorDetail);

public DisbursementPayee getPayeeFromPerson(Person personDetail,
        String payeeTypeCode);

    public String getPayeeTypeCodeForVendorType(String vendorTypeCode);

}
