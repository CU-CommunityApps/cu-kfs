/*
 * Copyright 2007 The Kuali Foundation.
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
package com.rsmart.kuali.kfs.sec.businessobject.options;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.businessobject.OriginEntryGroup;
import org.kuali.kfs.gl.service.OriginEntryGroupService;
import org.kuali.kfs.gl.web.util.OriginEntryFileComparator;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.core.util.KeyLabelPair;

import com.rsmart.kuali.kfs.sec.SecConstants;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Returns list of inquiry namespaces
 */
public class SecurityInquiryNamespaceFinder extends KeyValuesBase {

    /**
     * @see org.kuali.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {
        List activeLabels = new ArrayList();

        activeLabels.add(new KeyLabelPair("",""));
        activeLabels.add(new KeyLabelPair(KFSConstants.ParameterNamespaces.GL, KFSConstants.ParameterNamespaces.GL));
        activeLabels.add(new KeyLabelPair(LaborConstants.LABOR_MODULE_CODE, LaborConstants.LABOR_MODULE_CODE));

        return activeLabels;
    }
}
