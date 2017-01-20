/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.paymentworks.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.batch.report.AchUpdateSummary;
import edu.cornell.kfs.paymentworks.batch.report.AchUpdateSummaryLine;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksAchService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksKfsService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUtilityService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksWebService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksUpdateVendorStatus;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;
import edu.cornell.kfs.sys.service.ReportWriterService;

@Transactional
public class PaymentWorksRetrieveAchUpdatesStep extends AbstractStep {
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksRetrieveAchUpdatesStep.class);

	protected PaymentWorksWebService paymentWorksWebService;
	protected PaymentWorksAchService paymentWorksAchService;

	@Override
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
		boolean routed = false;
		List<PaymentWorksVendorUpdatesDTO> pendingACHUpdates = getPaymentWorksWebService().getPendingAchUpdatesFromPaymentWorks();
		LOG.info("execute, number of ACH Updates retrieved: " + pendingACHUpdates.size());
		routed = getPaymentWorksAchService().processACHUpdates(pendingACHUpdates);
		LOG.debug("execute, were all the changes routed:  + routed");
		return routed;
	}

	public PaymentWorksWebService getPaymentWorksWebService() {
		return paymentWorksWebService;
	}

	public void setPaymentWorksWebService(PaymentWorksWebService paymentWorksWebService) {
		this.paymentWorksWebService = paymentWorksWebService;
	}

	public PaymentWorksAchService getPaymentWorksAchService() {
		return paymentWorksAchService;
	}

	public void setPaymentWorksAchService(PaymentWorksAchService paymentWorksAchService) {
		this.paymentWorksAchService = paymentWorksAchService;
	}

}
