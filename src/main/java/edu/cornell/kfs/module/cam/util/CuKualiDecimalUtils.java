package edu.cornell.kfs.module.cam.util;

import org.kuali.kfs.module.cam.util.KualiDecimalUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public class CuKualiDecimalUtils extends KualiDecimalUtils {

    public static boolean equals(KualiDecimal kualiDecimal1, KualiDecimal kualiDecimal2) {
        return kualiDecimal1 == null ? kualiDecimal2 == null : kualiDecimal1.compareTo(kualiDecimal2) == 0;
    }
}
