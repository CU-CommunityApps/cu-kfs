/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2018 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.pdp.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kuali.kfs.pdp.service.PaymentFileService;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.XmlBatchInputFileTypeBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.core.io.Resource;

/**
 * This step will call the {@link PaymentService} to pick up incoming PDP payment files and process.
 */
public class LoadPaymentsStep extends AbstractStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(LoadPaymentsStep.class);

    private PaymentFileService paymentFileService;
    private BatchInputFileType paymentInputFileType;

    /**
     * Picks up the required path from the batchInputFIleType as well as from the payment file service.
     */
    @Override
    public List<String> getRequiredDirectoryNames() {
        List<String> requiredDirectoryList = new ArrayList<String>();
        requiredDirectoryList.add(paymentInputFileType.getDirectoryPath());
        requiredDirectoryList.addAll(paymentFileService.getRequiredDirectoryNames());

        return requiredDirectoryList;
    }

    /**
     * CU Customization: Updated this method to retrieve the schema resource
     * using SpringContext.getResource() instead of constructing a UrlResource instance.
     */
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.debug("execute() started");
        //check if payment.xsd exists. If not terminate at this point.
        if (paymentInputFileType instanceof XmlBatchInputFileTypeBase) {
            Resource schemaResource = SpringContext.getResource(
                    ((XmlBatchInputFileTypeBase) paymentInputFileType).getSchemaLocation());
            if (!schemaResource.exists()) {
                LOG.error(schemaResource.getFilename() + " file does not exist");
                throw new RuntimeException("error getting schema stream from url: " + schemaResource
                        .getFilename() + " file does not exist ");
            }
        }

        paymentFileService.processPaymentFiles(paymentInputFileType);

        return true;
    }

    /**
     * @param paymentFileService The paymentFileService to set.
     */
    public void setPaymentFileService(PaymentFileService paymentFileService) {
        this.paymentFileService = paymentFileService;
    }

    /**
     * @param paymentInputFileType The paymentInputFileType to set.
     */
    public void setPaymentInputFileType(BatchInputFileType paymentInputFileType) {
        this.paymentInputFileType = paymentInputFileType;
    }

}
