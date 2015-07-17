/*
 * Copyright 2006-2007 The Kuali Foundation.
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

package edu.cornell.kfs.fp.businessobject;

/**
 * This class is used to represent a procurement card holder, or the individual whose name is on the card.
 */
public class CuProcurementCardHolder extends org.kuali.kfs.fp.businessobject.ProcurementCardHolder
{
  private static final long serialVersionUID = 1L;
  private ProcurementCardHolderDetail procurementCardHolderDetail;

  /**
   * @return Returns the procurementCardHolderDetail.
   */
  public ProcurementCardHolderDetail getProcurementCardHolderDetail()
  {
    return procurementCardHolderDetail;
  }

  /**
   * Sets the procurementCardHolderDetail attribute.
   *
   * @param procurementCardHolderDetail The procurementCardHolderDetail to set.
   * @deprecated
   */
  public void setProcurementCardHolderDetail(ProcurementCardHolderDetail procurementCardHolderDetail)
  {
    this.procurementCardHolderDetail = procurementCardHolderDetail;
  }

}
