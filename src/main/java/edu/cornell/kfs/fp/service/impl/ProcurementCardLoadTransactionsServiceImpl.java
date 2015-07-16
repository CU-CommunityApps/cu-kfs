/*
 * Copyright 2006 The Kuali Foundation
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
package edu.cornell.kfs.fp.service.impl;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;
import org.kuali.rice.krad.service.SequenceAccessorService;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionExtension;

/**
 * This is the default implementation of the ProcurementCardLoadTransactionsService interface. Handles loading, parsing, and storing
 * of incoming procurement card batch files.
 *
 * @see org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService
 */
public class ProcurementCardLoadTransactionsServiceImpl extends org.kuali.kfs.fp.batch.service.impl.ProcurementCardLoadTransactionsServiceImpl
{
	private static Logger LOG = Logger.getLogger(ProcurementCardLoadTransactionsServiceImpl.class);

	protected SequenceAccessorService sequenceAccessorService;

	protected static String PCARD_TRN_SEQ_NAME = "FP_PRCRMNT_CARD_TRN_MT_SEQ";

	/**
	 * Loads all the parsed XML transactions into the temp transaction table. This method is overridden to generate the
	 * transactionSequenceRowNumber and set it on the ProcurementCardTransaction and ProcurementCardTransactionExtension objects.
	 *
	 * @param transactions List of ProcurementCardTransactions to load.
	 */
	@Override
	protected void loadTransactions(List transactions)
	{
		List<ProcurementCardTransaction> pcardTransactions = transactions;
		for (ProcurementCardTransaction trans : pcardTransactions)
		{
			Integer sequenceNumber = sequenceAccessorService.getNextAvailableSequenceNumber(PCARD_TRN_SEQ_NAME).intValue();
			trans.setTransactionSequenceRowNumber(sequenceNumber);
			ProcurementCardTransactionExtension extension = (ProcurementCardTransactionExtension) trans.getExtension();
			if (ObjectUtils.isNotNull(extension))
			{
				extension.setTransactionSequenceRowNumber(trans.getTransactionSequenceRowNumber());
			}
		}

		for (ProcurementCardTransaction t : (List<ProcurementCardTransaction>) transactions)
		{
			businessObjectService.save(t);
			LOG.info("Saved transaction: " + t.getTransactionSequenceRowNumber() + "ref number:" + t.getTransactionReferenceNumber());
		}
	}

	@Override
	public void cleanTransactionsTable()
	{
		// KFS-1512 - change this from deleteMatching in order to delete the ProcurementCardTransactionExtension and it's tables
		List<ProcurementCardTransaction> transactions = (List<ProcurementCardTransaction>) businessObjectService.findMatching(ProcurementCardTransaction.class, new HashMap());
		businessObjectService.delete(transactions);
	}

	public SequenceAccessorService getSequenceAccessorService()
	{
		return sequenceAccessorService;
	}

	public void setSequenceAccessorService(SequenceAccessorService sequenceAccessorService)
	{
		this.sequenceAccessorService = sequenceAccessorService;
	}

}
