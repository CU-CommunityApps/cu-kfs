package edu.cornell.kfs.gl.batch.service.impl;

import java.math.BigDecimal;

import org.kuali.kfs.gl.batch.service.impl.PosterServiceImpl;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public class CuPosterServiceImpl extends PosterServiceImpl {

    /**
     * Calculates the percentage and rounds HALF_UP
     * 
     * @see org.kuali.kfs.gl.batch.service.impl.PosterServiceImpl#getPercentage(org.kuali.rice.core.api.util.type.KualiDecimal, java.math.BigDecimal)
     */
    @Override
    protected KualiDecimal getPercentage(KualiDecimal amount, BigDecimal percent) {
        BigDecimal result = amount.bigDecimalValue().multiply(percent).divide(BDONEHUNDRED, 2, BigDecimal.ROUND_HALF_UP);
        return new KualiDecimal(result);
    }
}
