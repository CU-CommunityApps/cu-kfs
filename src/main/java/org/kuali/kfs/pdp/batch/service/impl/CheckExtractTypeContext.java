/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
import java.util.Objects;

import org.kuali.kfs.pdp.businessobject.PaymentProcess;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;

import edu.cornell.kfs.pdp.batch.service.impl.PaymentUrgency;

/**
 * ====
 * CU Customization: Added field for configuring whether the context
 * should process regular payments or immediate payments.
 * ====
 * 
 * Implementation specific to {@link ExtractionType#CHECK}.
 */
public class CheckExtractTypeContext extends AbstractExtractTypeContext {

    private final PaymentUrgency urgency;

    public CheckExtractTypeContext(
            final Date extractBeginDate,
            final PaymentStatus extractedStatus,
            final PaymentProcess paymentProcess,
            final PaymentUrgency urgency
    ) {
        super(extractBeginDate, extractedStatus, paymentProcess);
        Objects.requireNonNull(urgency, "urgency cannot be null");
        this.urgency = urgency;
    }

    @Override
    public boolean isExtractionType(final ExtractionType extractionType) {
        return ExtractionType.CHECK == extractionType;
    }

    @Override
    public boolean isLimitedToPaymentsWithUrgency(PaymentUrgency urgency) {
        return this.urgency == urgency;
    }

}
