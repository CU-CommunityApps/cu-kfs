/*
 * Copyright 2008-2009 The Kuali Foundation
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
package edu.cornell.kfs.pdp.batch.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.kuali.kfs.pdp.batch.service.AchAdviceNotificationService;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.sys.service.NonTransactional;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;

import com.rsmart.kuali.kfs.pdp.service.AchBundlerHelperService;

import edu.cornell.kfs.pdp.batch.PDPBadEmailRecord;
import edu.cornell.kfs.pdp.batch.service.CuAchAdviceNotificationWrrorReportService;
import edu.cornell.kfs.pdp.dataaccess.AchBundlerAdviceDao;
import edu.cornell.kfs.pdp.service.CuPdpEmailService;

public class CuAchAdviceNotificationServiceImpl implements AchAdviceNotificationService {
    private static final Logger LOG = LogManager.getLogger(CuAchAdviceNotificationServiceImpl.class);
    
    private CuPdpEmailService pdpEmailService;
    private PaymentGroupService paymentGroupService;

    private DateTimeService dateTimeService;
    private BusinessObjectService businessObjectService;
    private AchBundlerHelperService achBundlerHelperService;
    private AchBundlerAdviceDao achBundlerAdviceDao;
    private CuAchAdviceNotificationWrrorReportService cuAchAdviceNotificationWrrorReportService;

    @NonTransactional
    @Override
    public void sendAdviceNotifications() {
        LOG.info("sendAdviceNotifications, entering");
        List<PDPBadEmailRecord> badEmailRecords = new ArrayList<PDPBadEmailRecord>();

        if (achBundlerHelperService.shouldBundleAchPayments()) {
            HashSet<Integer> disbNbrs = achBundlerAdviceDao
                    .getDistinctDisbursementNumbersForAchPaymentsNeedingAdviceNotification();

            for (Iterator<Integer> disbIter = disbNbrs.iterator(); disbIter.hasNext();) {
                Integer disbursementNbr = disbIter.next();

                List<PaymentDetail> paymentDetails = achBundlerAdviceDao.getAchPaymentDetailsNeedingAdviceNotificationByDisbursementNumber(disbursementNbr);

                Iterator<PaymentDetail> paymentDetailsIter = paymentDetails.iterator();
                PaymentDetail payDetail = paymentDetailsIter.next();
                PaymentGroup payGroup = payDetail.getPaymentGroup();
                CustomerProfile customer = payGroup.getBatch().getCustomerProfile();
                try {
                    if (customer.getAdviceCreate()) {
                        cuAchAdviceNotificationWrrorReportService.validateEmailAddress(payGroup.getAdviceEmailAddress());
                        pdpEmailService.sendAchAdviceEmail(payGroup, paymentDetails, customer);
                    }

                    for (Iterator<PaymentDetail> paymentDetailsIter2 = paymentDetails.iterator(); paymentDetailsIter2.hasNext();) {
                        PaymentDetail pd = paymentDetailsIter2.next();
                        PaymentGroup pg = pd.getPaymentGroup();
                        pg.setAdviceEmailSentDate(dateTimeService.getCurrentTimestamp());
                        businessObjectService.save(pg);
                    }
                } catch (Exception e) {
                    LOG.error("sendAdviceNotifications, error processing bundled payments.", e);
                    addBadEmailRecord(badEmailRecords, payGroup);
                }

            }

        } else {
            List<PaymentGroup> paymentGroups = paymentGroupService.getAchPaymentsNeedingAdviceNotification();
            for (PaymentGroup paymentGroup : paymentGroups) {
                CustomerProfile customer = paymentGroup.getBatch().getCustomerProfile();
                List<PaymentDetail> paymentDetails = paymentGroup.getPaymentDetails();
                try {
                    if (customer.getAdviceCreate()) {
                        cuAchAdviceNotificationWrrorReportService.validateEmailAddress(paymentGroup.getAdviceEmailAddress());
                        pdpEmailService.sendAchAdviceEmail(paymentGroup, paymentDetails, customer);
                    }
                    paymentGroup.setAdviceEmailSentDate(dateTimeService.getCurrentTimestamp());
                    businessObjectService.save(paymentGroup);
                } catch (Exception e) {
                    LOG.error("sendAdviceNotifications, error processing bundled payments.", e);
                    addBadEmailRecord(badEmailRecords, paymentGroup);
                }
            }
        }

        createAndEmailErrorReport(badEmailRecords);
    }
    
    @NonTransactional
    private void addBadEmailRecord(List<PDPBadEmailRecord> badEmailRecords, PaymentGroup payGroup) {
        PDPBadEmailRecord badEmailRecord = new PDPBadEmailRecord(payGroup.getPayeeId(), payGroup.getId(), payGroup.getAdviceEmailAddress());
        badEmailRecord.logBadEmailRecord();
        badEmailRecords.add(badEmailRecord);
    }
    
    @NonTransactional
    private void createAndEmailErrorReport(List<PDPBadEmailRecord> badEmailRecords) {
        if (CollectionUtils.isNotEmpty(badEmailRecords)) {
            LOG.info("createBadEmailReport, there are " + badEmailRecords.size() + " bad email records to report");
            File reportFile = cuAchAdviceNotificationWrrorReportService.createBadEmailReport(badEmailRecords);
            cuAchAdviceNotificationWrrorReportService.emailBadEmailReport(reportFile);
            
        } else {
            LOG.info("createBadEmailReport, there were no bad email addresses to report.");
        }
    }
    
    @NonTransactional
    public void setPdpEmailService(CuPdpEmailService pdpEmailService) {
        this.pdpEmailService = pdpEmailService;
    }

    @NonTransactional
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    @NonTransactional
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    @NonTransactional
    public void setPaymentGroupService(PaymentGroupService paymentGroupService) {
        this.paymentGroupService = paymentGroupService;
    }
    
    @NonTransactional
    public void setAchBundlerHelperService(AchBundlerHelperService achBundlerHelperService) {
        this.achBundlerHelperService = achBundlerHelperService;
    }
    
    @NonTransactional
    public void setAchBundlerAdviceDao(AchBundlerAdviceDao achBundlerAdviceDao) {
        this.achBundlerAdviceDao = achBundlerAdviceDao;
    }

    public void setCuAchAdviceNotificationWrrorReportService(
            CuAchAdviceNotificationWrrorReportService cuAchAdviceNotificationWrrorReportService) {
        this.cuAchAdviceNotificationWrrorReportService = cuAchAdviceNotificationWrrorReportService;
    }
}
