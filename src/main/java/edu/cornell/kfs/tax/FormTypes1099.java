package edu.cornell.kfs.tax;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public enum FormTypes1099 {
    MISC(CUTaxConstants.TAX_1099_MISC_FORM_TYPE, "1099-MISC"),
    NEC(CUTaxConstants.TAX_1099_NEC_FORM_TYPE, "1099-NEC"),
    UNKNOWN(CUTaxConstants.TAX_1099_UNKNOWN_FORM_TYPE, "Unknown");

    public final String formCode;
    public final String formDescription;

    private FormTypes1099(String formCode, String formDescription) {
        this.formCode = formCode;
        this.formDescription = formDescription;
    }

    public static Optional<FormTypes1099> findPotentialFormTypes1099FromFormCode(String formCode) {
        try {
            return Optional.of(findFormTypes1099FromFormCode(formCode));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static FormTypes1099 findFormTypes1099FromFormCode(String formCode) {
        for (FormTypes1099 type : FormTypes1099.values()) {
            if (StringUtils.equalsIgnoreCase(type.formCode, formCode)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unable to find a 1099 form type for code " + formCode);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }
}
