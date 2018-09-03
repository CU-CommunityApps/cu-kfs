package edu.cornell.kfs.fp.batch;

import edu.cornell.kfs.fp.businessobject.AchIncomeFile;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileGroup;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileGroupTrailer;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTrailer;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransaction;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionDateTime;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionNote;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionOpenItemReference;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionPayerOrPayeeName;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionPremiumPayersAdminsContact;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionPremiumReceiverName;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionReference;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionSet;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionSetTrailer;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionTrace;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.batch.AbstractFlatFileObjectSpecification;
import org.kuali.kfs.sys.batch.DelimitedFlatFilePropertySpecification;
import org.kuali.kfs.sys.batch.FlatFilePrefixObjectSpecification;
import org.kuali.kfs.sys.batch.FlatFilePropertySpecification;
import org.kuali.kfs.sys.businessobject.format.BatchDateFormatter;
import org.kuali.kfs.sys.businessobject.format.ExplicitKualiDecimalFormatter;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.web.format.Formatter;
import org.kuali.rice.core.web.format.IntegerFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AchIncomeDelimitedFlatFileSpecificationTest {

    private static final String ISA_LINE = "ISA*00*NV        *00*NV        *ZZ*NV             *ZZ*NV             *160223*2143*&*00400*000000000*0*P*?";
    private static final String GS_LINE = "GS*RA*NV*NV*20160223*2143*0*X*004020\\";
    private static final String ST_LINE = "ST*820*0001\\";
    private static final String BPR_LINE1 = "BPR*C*12761.79*C*ACH*CTX*01*011900254*DA*002037607421*1111541330**01*021302648*DA*01101000065**VEN\\";
    private static final String BPR_LINE2 = "BPR*C*3131.04*C*ACH*CTX*01*041036004*Z*8522*3041036004**01*021302648*DA*0111000065*20160223*VEN\\";
    private static final String TRN_LINE = "TRN*1*041036009100170\\";
    private static final String REF_LINE = "REF*GC*W911NF1420001\\";
    private static final String DTM_LINE = "DTM*097*20160222*163345\\";
    private static final String N1_LINE1 = "N1*PE*CORNELL UNIVERSITY, INC*93*4B578\\";
    private static final String N1_LINE2 = "N1*PR*NATIONAL SCIENCE FOUNDATION|";
    private static final String NM1_LINE = "NM1*?*?*PREMIUM RECEIVER NAME\\";
    private static final String PER_LINE = "PER*?*JOHN SMITH\\";
    private static final String NTE_LINE = "NTE*001*TRANSACTION NOTE\\";
    private static final String RMR_LINE = "RMR*IV*20*PI*3131.04*2424.65\\";
    private static final String SE_LINE = "SE*14*000000001\\";
    private static final String GE_LINE = "GE*1*092016170\\";
    private static final String IEA_LINE = "IEA*1*092016170\\";

    private static final String LINE_ENDING_BACKSLASH = "\\";
    private static final String LINE_ENDING_PIPE = "|";
    private static final String LINE_ENDING_TILDE = "~";
    private static final String DATE_FORMAT = "yyyyMMdd";

    private AchIncomeDelimitedFlatFileSpecification achIncomeDelimitedFlatFileSpecification;

    @Before
    public void setUp() {
        achIncomeDelimitedFlatFileSpecification = new AchIncomeDelimitedFlatFileSpecification();
        achIncomeDelimitedFlatFileSpecification.setDelimiter("*");
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFile() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 9, "fileDate");
        setFlatFileProperty(parseProperties, 10, "fileTime");
        setFlatFileProperty(parseProperties, 13, "interchangeControlNumber");
        setFlatFileProperty(parseProperties, 15, "productionOrTestIndicator");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFile achIncomeFile = new AchIncomeFile();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, ISA_LINE, achIncomeFile, 1);

        assertEquals("160223", achIncomeFile.getFileDate());
        assertEquals("2143", achIncomeFile.getFileTime());
        assertEquals("000000000", achIncomeFile.getInterchangeControlNumber());
        assertEquals("P", achIncomeFile.getProductionOrTestIndicator());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileGroup() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 6, "groupControlNumber");
        setFlatFileProperty(parseProperties, 1, "groupFunctionIdentifierCode");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileGroup achIncomeFileGroup = new AchIncomeFileGroup();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, GS_LINE, achIncomeFileGroup, 1);

        assertEquals("0", achIncomeFileGroup.getGroupControlNumber());
        assertEquals("RA", achIncomeFileGroup.getGroupFunctionIdentifierCode());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTransactionSet() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 2, "transactionSetControlNumber");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileTransactionSet achIncomeFileTransactionSet = new AchIncomeFileTransactionSet();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, ST_LINE, achIncomeFileTransactionSet, 1);

        assertEquals("0001", achIncomeFileTransactionSet.getTransactionSetControlNumber());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTransaction() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 2, "transactionAmount", ExplicitKualiDecimalFormatter.class);
        setFlatFileProperty(parseProperties, 3, "creditDebitIndicator");
        setFlatFileProperty(parseProperties, 4, "paymentMethodCode");
        setFlatFileProperty(parseProperties, 16, "effectiveDate");
        setFlatFileProperty(parseProperties, 10, "companyId");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileTransaction achIncomeFileTransaction = new AchIncomeFileTransaction();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, BPR_LINE1, achIncomeFileTransaction, 1);

        assertEquals(new KualiDecimal("12761.79"), achIncomeFileTransaction.getTransactionAmount());
        assertEquals("C", achIncomeFileTransaction.getCreditDebitIndicator());
        assertEquals("ACH", achIncomeFileTransaction.getPaymentMethodCode());
        assertEquals(null, achIncomeFileTransaction.getEffectiveDate());
        assertEquals("1111541330", achIncomeFileTransaction.getCompanyId());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTransactionWithDate() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 2, "transactionAmount", ExplicitKualiDecimalFormatter.class);
        setFlatFileProperty(parseProperties, 3, "creditDebitIndicator");
        setFlatFileProperty(parseProperties, 4, "paymentMethodCode");
        setFlatFileProperty(parseProperties, 16, "effectiveDate", BatchDateFormatter.class, DATE_FORMAT);
        setFlatFileProperty(parseProperties, 10, "companyId");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileTransaction achIncomeFileTransaction = new AchIncomeFileTransaction();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, BPR_LINE2, achIncomeFileTransaction, 1);

        Date expectedDate = new SimpleDateFormat(DATE_FORMAT).parse("20160223");

        assertEquals(new KualiDecimal("3131.04"), achIncomeFileTransaction.getTransactionAmount());
        assertEquals("C", achIncomeFileTransaction.getCreditDebitIndicator());
        assertEquals("ACH", achIncomeFileTransaction.getPaymentMethodCode());
        assertEquals(expectedDate, achIncomeFileTransaction.getEffectiveDate());
        assertEquals("3041036004", achIncomeFileTransaction.getCompanyId());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTransactionTrace() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 2, "traceNumber");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileTransactionTrace achIncomeFileTransactionTrace = new AchIncomeFileTransactionTrace();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, TRN_LINE, achIncomeFileTransactionTrace, 1);

        assertEquals("041036009100170", achIncomeFileTransactionTrace.getTraceNumber());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTransactionReference() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 1, "type");
        setFlatFileProperty(parseProperties, 2, "value");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileTransactionReference achIncomeFileTransactionReference = new AchIncomeFileTransactionReference();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, REF_LINE, achIncomeFileTransactionReference, 1);

        assertEquals("GC", achIncomeFileTransactionReference.getType());
        assertEquals("W911NF1420001", achIncomeFileTransactionReference.getValue());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTransactionDateTime() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 1, "type");
        setFlatFileProperty(parseProperties, 2, "dateTime");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileTransactionDateTime achIncomeFileTransactionDateTime = new AchIncomeFileTransactionDateTime();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, DTM_LINE, achIncomeFileTransactionDateTime, 1);

        assertEquals("097", achIncomeFileTransactionDateTime.getType());
        assertEquals("20160222", achIncomeFileTransactionDateTime.getDateTime());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTransactionPayeeName() throws Exception {
        parseLineIntoObjectAchIncomeFileTransactionPayerOrPayeeName(N1_LINE1, "PE", "CORNELL UNIVERSITY, INC", "93", "4B578");
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTransactionPayerName() throws Exception {
        parseLineIntoObjectAchIncomeFileTransactionPayerOrPayeeName(N1_LINE2, "PR", "NATIONAL SCIENCE FOUNDATION", null, null);
    }

    protected void parseLineIntoObjectAchIncomeFileTransactionPayerOrPayeeName(String lineToParse, String expectedType, String expectedValue, String expectedIdQualifier, String expectedIdCode) {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 1, "type");
        setFlatFileProperty(parseProperties, 2, "name");
        setFlatFileProperty(parseProperties, 3, "idQualifier");
        setFlatFileProperty(parseProperties, 4, "idCode");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileTransactionPayerOrPayeeName achIncomeFileTransactionPayerOrPayeeName = new AchIncomeFileTransactionPayerOrPayeeName();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, lineToParse, achIncomeFileTransactionPayerOrPayeeName, 1);

        assertEquals(expectedType, achIncomeFileTransactionPayerOrPayeeName.getType());
        assertEquals(expectedValue, achIncomeFileTransactionPayerOrPayeeName.getName());
        assertEquals(expectedIdQualifier, achIncomeFileTransactionPayerOrPayeeName.getIdQualifier());
        assertEquals(expectedIdCode, achIncomeFileTransactionPayerOrPayeeName.getIdCode());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTransactionPremiumReceiverName() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 3, "name");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileTransactionPremiumReceiverName achIncomeFileTransactionPremiumReceiverName = new AchIncomeFileTransactionPremiumReceiverName();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, NM1_LINE, achIncomeFileTransactionPremiumReceiverName, 1);

        assertEquals("PREMIUM RECEIVER NAME", achIncomeFileTransactionPremiumReceiverName.getName());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTransactionPremiumPayersAdminsContact() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 2, "name");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileTransactionPremiumPayersAdminsContact achIncomeFileTransactionPremiumPayersAdminsContact = new AchIncomeFileTransactionPremiumPayersAdminsContact();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, PER_LINE, achIncomeFileTransactionPremiumPayersAdminsContact, 1);

        assertEquals("JOHN SMITH", achIncomeFileTransactionPremiumPayersAdminsContact.getName());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTransactionNote() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 1, "type");
        setFlatFileProperty(parseProperties, 2, "value");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileTransactionNote achIncomeFileTransactionNote = new AchIncomeFileTransactionNote();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, NTE_LINE, achIncomeFileTransactionNote, 1);

        assertEquals("001", achIncomeFileTransactionNote.getType());
        assertEquals("TRANSACTION NOTE", achIncomeFileTransactionNote.getValue());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTransactionOpenItemReference() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 1, "type");
        setFlatFileProperty(parseProperties, 2, "invoiceNumber");
        setFlatFileProperty(parseProperties, 4, "netAmount", ExplicitKualiDecimalFormatter.class);
        setFlatFileProperty(parseProperties, 5, "invoiceAmount", ExplicitKualiDecimalFormatter.class);
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileTransactionOpenItemReference achIncomeFileTransactionOpenItemReference = new AchIncomeFileTransactionOpenItemReference();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, RMR_LINE, achIncomeFileTransactionOpenItemReference, 1);

        assertEquals("IV", achIncomeFileTransactionOpenItemReference.getType());
        assertEquals("20", achIncomeFileTransactionOpenItemReference.getInvoiceNumber());
        assertEquals(new KualiDecimal("3131.04"), achIncomeFileTransactionOpenItemReference.getNetAmount());
        assertEquals(new KualiDecimal("2424.65"), achIncomeFileTransactionOpenItemReference.getInvoiceAmount());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTransactionSetTrailer() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 2, "transactionSetControlNumber");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileTransactionSetTrailer achIncomeFileTransactionSetTrailer = new AchIncomeFileTransactionSetTrailer();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, SE_LINE, achIncomeFileTransactionSetTrailer, 1);

        assertEquals("000000001", achIncomeFileTransactionSetTrailer.getTransactionSetControlNumber());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileGroupTrailer() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 1, "totalTransactionSets", IntegerFormatter.class);
        setFlatFileProperty(parseProperties, 2, "groupControlNumber");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileGroupTrailer achIncomeFileGroupTrailer = new AchIncomeFileGroupTrailer();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, GE_LINE, achIncomeFileGroupTrailer, 1);

        assertEquals(1, achIncomeFileGroupTrailer.getTotalTransactionSets());
        assertEquals("092016170", achIncomeFileGroupTrailer.getGroupControlNumber());
    }

    @Test
    public void testParseLineIntoObjectAchIncomeFileTrailer() throws Exception {
        AbstractFlatFileObjectSpecification flatFileObjectSpecification = new FlatFilePrefixObjectSpecification();
        List<FlatFilePropertySpecification> parseProperties = new ArrayList<>();
        setFlatFileProperty(parseProperties, 1, "totalGroups", IntegerFormatter.class);
        setFlatFileProperty(parseProperties, 2, "interchangeControlNumber");
        flatFileObjectSpecification.setParseProperties(parseProperties);

        AchIncomeFileTrailer achIncomeFileTrailer = new AchIncomeFileTrailer();
        achIncomeDelimitedFlatFileSpecification.parseLineIntoObject(flatFileObjectSpecification, IEA_LINE, achIncomeFileTrailer, 1);

        assertEquals(1, achIncomeFileTrailer.getTotalGroups());
        assertEquals("092016170", achIncomeFileTrailer.getInterchangeControlNumber());
    }

    protected void setFlatFileProperty(List<FlatFilePropertySpecification> parseProperties, int lineSegmentIndex, String propertyName) {
        setFlatFileProperty(parseProperties, lineSegmentIndex, propertyName, Formatter.class);
    }

    protected void setFlatFileProperty(List<FlatFilePropertySpecification> parseProperties, int lineSegmentIndex, String propertyName, Class<? extends Formatter> formatterClass) {
        setFlatFileProperty(parseProperties, lineSegmentIndex, propertyName, formatterClass, null);
    }

    protected void setFlatFileProperty(List<FlatFilePropertySpecification> parseProperties, int lineSegmentIndex, String propertyName, Class<? extends Formatter> formatterClass, String dateFormat) {
        DelimitedFlatFilePropertySpecification delimitedFlatFilePropertySpecification = new MockDelimitedFlatFilePropertySpecification();
        delimitedFlatFilePropertySpecification.setFormatterClass(formatterClass);
        delimitedFlatFilePropertySpecification.setLineSegmentIndex(lineSegmentIndex);
        delimitedFlatFilePropertySpecification.setPropertyName(propertyName);
        delimitedFlatFilePropertySpecification.setDateFormat(dateFormat);
        parseProperties.add(delimitedFlatFilePropertySpecification);
    }

    @Test
    public void testRemoveEndOfLineCharacterBackslash() throws Exception {
        testRemoveEndOfLineCharacter(LINE_ENDING_BACKSLASH);
    }

    @Test
    public void testRemoveEndOfLineCharacterPipe() throws Exception {
        testRemoveEndOfLineCharacter(LINE_ENDING_PIPE);
    }

    @Test
    public void testRemoveEndOfLineCharacterTilde() throws Exception {
        testRemoveEndOfLineCharacter(LINE_ENDING_TILDE);
    }

    private void testRemoveEndOfLineCharacter(String lineEndingCharacter) throws Exception {
        String line = achIncomeDelimitedFlatFileSpecification.removeEndOfLineCharacter(ISA_LINE + lineEndingCharacter);
        assertEquals("Didn't remove end of line character '" + lineEndingCharacter + "' as expected.", ISA_LINE, line);
    }

    private class MockDelimitedFlatFilePropertySpecification extends DelimitedFlatFilePropertySpecification {

        protected Class<? extends Formatter> formatterClass;

        @Override
        protected Class<?> getFormatterClass(Object parsedObject) {
            return formatterClass;
        }

        @Override
        public void setFormatterClass(Class<? extends Formatter> formatterClass) {
            this.formatterClass = formatterClass;
        }

    }

}