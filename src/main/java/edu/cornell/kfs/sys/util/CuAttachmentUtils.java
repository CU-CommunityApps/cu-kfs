package edu.cornell.kfs.sys.util;

import java.net.URLConnection;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

public final class CuAttachmentUtils {

    private static final Logger LOG = LogManager.getLogger();

    private CuAttachmentUtils() {
        throw new UnsupportedOperationException("do not call");
    }

    /*
     * Modified copy of AccountingXmlDocumentDownloadAttachmentServiceImpl.findMimeType() method.
     * NOTE: If needed, create a user story for consolidating the other method's usage into this one.
     */
    public static String findMimeType(final String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return StringUtils.EMPTY;
        }
        String mimeType = URLConnection.guessContentTypeFromName(fileName);
        if (StringUtils.isBlank(mimeType)) {
            LOG.warn("findMimeType, could not determine mime type from file name '{}' using URLConnection object",
                    fileName);
            final String extensionSuffix = StringUtils.substringAfterLast(fileName, KFSConstants.DELIMITER); 
            if (StringUtils.isNotBlank(extensionSuffix)) {
                mimeType = extensionSuffix;
                LOG.info("findMimeType, determined mime type from file name's last extension value: {}", mimeType);
            } else {
                LOG.warn("findMimeType, could not parse the file name, setting mime type to empty string");
                mimeType = StringUtils.EMPTY;
            }
        }
        return StringUtils.lowerCase(mimeType, Locale.US);
    }

}
