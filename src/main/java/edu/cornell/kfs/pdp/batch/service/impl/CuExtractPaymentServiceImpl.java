package edu.cornell.kfs.pdp.batch.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.batch.service.impl.ExtractPaymentServiceImpl;
import org.kuali.kfs.pdp.batch.service.impl.Iso20022FormatExtractor;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.pdp.businessobject.PaymentProcess;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.sys.KFSConstants;

import com.rsmart.kuali.kfs.pdp.service.AchBundlerHelperService;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.pdp.batch.service.CuPayeeAddressService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class CuExtractPaymentServiceImpl extends ExtractPaymentServiceImpl {
    private static final Logger LOG = LogManager.getLogger();
    
    protected AchBundlerHelperService achBundlerHelperService;
    protected CuPayeeAddressService cuPayeeAddressService;
    protected Iso20022FormatExtractor iso20022FormatExtractor;

    public CuExtractPaymentServiceImpl() {
    	super(null);
    }

    public CuExtractPaymentServiceImpl(
            final Iso20022FormatExtractor iso20022FormatExtractor
    ) {
    	super(iso20022FormatExtractor);
    	this.iso20022FormatExtractor = iso20022FormatExtractor;
    }

    /** MOD: Overridden to make filename unique by adding milliseconds to filename **/
    @Override
    protected String getOutputFile(final String fileprefix, final Date runDate) {
        //add a step to check for directory paths
        prepareDirectories(getRequiredDirectoryNames());

        String filename = directoryName + "/" + fileprefix + "_";
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
        filename = filename + sdf.format(runDate);
        filename = filename + ".xml";

        return filename;
    }
    
    /**
    * MOD: Overridden to detect if the Bundle ACH Payments system parameter is on and if so, to 
    * call the new extraction bundler method
    */
    @Override
    public void extractAchPayments() {
        LOG.debug("MOD - extractAchPayments() - Enter");

        PaymentStatus extractedStatus = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class,
                PdpConstants.PaymentStatusCodes.EXTRACTED);

        iso20022FormatExtractor.extractAchs(extractedStatus, directoryName);

    }

    
    
    protected String updateNoteLine(String noteLine) {
        // Had to add this code to check for and remove the colons (::) that were added in
        // DisbursementVoucherExtractServiceImpl.java line 506 v4229 if they exist.  If not
        // then just return what was sent.  This was placed in a method as it is used in
        // two locations in this class

        if (noteLine.length() >= 2 && noteLine.substring(0,2).contains(CuDisbursementVoucherConstants.DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER)) {
            noteLine = noteLine.substring(2);     
        }

        return noteLine;
    }
    
    protected boolean renameFile(final String fromFile, final String toFile) {
        boolean bResult = false;
        try {
            final File f = new File(fromFile);
            f.renameTo(new File(toFile));
        } catch (final Exception ex) {
            LOG.error("renameFile Exception: " + ex.getMessage());
            LOG.error("fromFile: " + fromFile + ", toFile: " + toFile);
        }
        return bResult;
    }
    
    public AchBundlerHelperService getAchBundlerHelperService() {
        return achBundlerHelperService;
    }

    public void setAchBundlerHelperService(final AchBundlerHelperService achBundlerHelperService) {
        this.achBundlerHelperService = achBundlerHelperService;
    }

    public void setCuPayeeAddressService(final CuPayeeAddressService cuPayeeAddressService) {
        this.cuPayeeAddressService = cuPayeeAddressService;
    }
}
