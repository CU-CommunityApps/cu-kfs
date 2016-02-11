/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2015 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.gl.batch.service.impl;

import java.text.MessageFormat;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.gl.batch.service.impl.IndirectCostRecoveryAccountDistributionMetadata;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.gl.exception.IndirectCostRecoveryMetadataSearchException;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuIndirectCostRecoveryAccountDistributionMetadata extends IndirectCostRecoveryAccountDistributionMetadata {

    /**
     * @param icrAccount
     * @throws Exception
     */
    public CuIndirectCostRecoveryAccountDistributionMetadata(IndirectCostRecoveryAccount icrAccount) throws IndirectCostRecoveryMetadataSearchException {
      super(icrAccount);

      // Do what super does if the ICRA is null or not closed
      if(ObjectUtils.isNotNull(icrAccount) && 
         ObjectUtils.isNotNull(icrAccount.getIndirectCostRecoveryAccount()) && 
         icrAccount.getIndirectCostRecoveryAccount().isClosed()) {
        Account replacementICRA = icrAccount.getIndirectCostRecoveryAccount();
        int i = 0;

        // Search 10 levels or less to find an open Account, as we do in other processes
        while((i < 10) && (ObjectUtils.isNotNull(replacementICRA) && replacementICRA.isClosed())) {
          replacementICRA = replacementICRA.getContinuationAccount();
          i++;
        }
  
        // If we've found a replacement that isn't what we've already got...
        if(ObjectUtils.isNotNull(replacementICRA) && (replacementICRA != icrAccount.getIndirectCostRecoveryAccount())) {
          if(replacementICRA.isClosed()) {
            // If the ICR Account is still closed after searching this far down, raise an exception.
            String errorText = SpringContext.getBean(ConfigurationService.class)
                                            .getPropertyValueAsString(CUKFSKeyConstants.ERROR_ICRACCOUNT_CONTINUATION_ACCOUNT_CLOSED);
            Object[] args = {
                icrAccount.getIndirectCostRecoveryFinCoaCode(),
                icrAccount.getIndirectCostRecoveryAccountNumber()
              };
            errorText = MessageFormat.format(errorText, args);
  
            throw new IndirectCostRecoveryMetadataSearchException(errorText);
          } else {
            // Otherwise, change the ICR Account settings to the one at the level we've drilled down to.
            this.setIndirectCostRecoveryFinCoaCode(replacementICRA.getChartOfAccountsCode());
            this.setIndirectCostRecoveryAccountNumber(replacementICRA.getAccountNumber());
          }
        }
      }
    }

}
