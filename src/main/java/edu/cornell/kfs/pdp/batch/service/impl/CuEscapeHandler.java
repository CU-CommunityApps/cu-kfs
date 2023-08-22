package edu.cornell.kfs.pdp.batch.service.impl;

import org.kuali.kfs.krad.util.KRADConstants;

import com.prowidesoftware.swift.model.mx.DefaultEscapeHandler;

import edu.cornell.kfs.sys.CUKFSConstants;

public class CuEscapeHandler extends DefaultEscapeHandler {

    private static final String ESCAPED_APOSTROPHE = "&apos;";
    private static final String ESCAPED_QUOTE = "&quot;";

    @Override
    public String escape(char[] arr, boolean isAttribute) {
        StringBuilder result = new StringBuilder(super.escape(arr, isAttribute));
        replace(result, KRADConstants.SINGLE_QUOTE, ESCAPED_APOSTROPHE);
        replace(result, CUKFSConstants.DOUBLE_QUOTE, ESCAPED_QUOTE);
        return result.toString();
    }

    private void replace(StringBuilder source, String searchValue, String replacementValue) {
        for (int valIndex = source.indexOf(searchValue); valIndex >= 0; valIndex = source.indexOf(searchValue)) {
            source.replace(valIndex, valIndex + 1, replacementValue);
        }
    }

}
