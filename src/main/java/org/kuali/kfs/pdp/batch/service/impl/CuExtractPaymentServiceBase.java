package org.kuali.kfs.pdp.batch.service.impl;

import org.kuali.kfs.pdp.businessobject.PaymentStatus;

/**
 * Intermediate helper service class that's in the same package as Iso20022FormatExtractor,
 * so that the class's package-private extractAchs() method can be invoked by service impl classes
 * that reside in a different package. The extractor instance itself is also being stored in a new
 * protected variable, since ExtractPaymentServiceImpl stores it in a private one.
 * 
 * NOTE: If/When we end up overlaying Iso20022FormatExtractor in the future, we can remove
 * this helper class and just increase the visibility of the method on the extractor class.
 * (The variable storing the extractor can also be moved or adjusted accordingly.)
 */
public abstract class CuExtractPaymentServiceBase extends ExtractPaymentServiceImpl {

    protected final Iso20022FormatExtractor iso20022FormatExtractor;

    public CuExtractPaymentServiceBase(
            final Iso20022FormatExtractor iso20022FormatExtractor
    ) {
        super(iso20022FormatExtractor);
        this.iso20022FormatExtractor = iso20022FormatExtractor;
    }

    protected void extractAchsInIso20022Format(
            final PaymentStatus extractedStatus,
            final String directoryName
    ) {
        iso20022FormatExtractor.extractAchs(extractedStatus, directoryName);
    }

}
