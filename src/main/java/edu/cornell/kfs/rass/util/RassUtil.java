package edu.cornell.kfs.rass.util;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.rass.batch.RassXmlObjectResult;
import edu.cornell.kfs.sys.CUKFSConstants;

public final class RassUtil {

    private RassUtil() {
        throw new UnsupportedOperationException("Instantiating static utility class is prohibited");
    }

    public static String buildClassAndKeyIdentifier(RassXmlObjectResult objectResult) {
        return buildClassAndKeyIdentifier(objectResult.getBusinessObjectClass(), objectResult.getPrimaryKey());
    }

    public static String buildClassAndKeyIdentifier(Class<?> clazz, String key) {
        return clazz.getName() + CUKFSConstants.COLON + key;
    }

    public static String getSimpleClassNameFromClassAndKeyIdentifier(String classAndKeyIdentifier) {
        String classNamePart = StringUtils.substringBefore(classAndKeyIdentifier, CUKFSConstants.COLON);
        return StringUtils.substringAfterLast(classNamePart, KFSConstants.DELIMITER);
    }

    public static String getKeyFromClassAndKeyIdentifier(String classAndKeyIdentifier) {
        return StringUtils.substringAfter(classAndKeyIdentifier, CUKFSConstants.COLON);
    }

}
