/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.pdp.batch.service.impl;

import java.util.Date;

import org.kuali.kfs.pdp.businessobject.PaymentProcess;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;

import edu.cornell.kfs.pdp.batch.service.impl.PaymentUrgency;

/**
 * ====
 * CU Customization: Added method for limiting extraction based on the payment's urgency.
 * ====
 * 
 * The state (i.e. context) of the extraction being performed, which can differ depending on the
 * {@link ExtractionType}.
 */
public interface ExtractTypeContext {

    boolean isExtractionType(ExtractionType extractionType);

    Date getDisbursementDate();

    PaymentStatus getExtractedStatus();

    PaymentProcess getPaymentProcess();

    default boolean isLimitedToPaymentsWithUrgency(PaymentUrgency urgency) {
        return false;
    }

}
