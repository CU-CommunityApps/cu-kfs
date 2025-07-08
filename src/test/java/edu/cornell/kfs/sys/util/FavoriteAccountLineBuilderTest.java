package edu.cornell.kfs.sys.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.sys.service.impl.TestUserFavoriteAccountServiceImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLineBase;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocumentBase;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.document.LedgerPostingDocumentBase;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.mockito.Mockito;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.document.CuRequisitionDocument;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.util.PurApFavoriteAccountLineBuilderForIWantDocument;
import edu.cornell.kfs.module.purap.util.PurchasingFavoriteAccountLineBuilderForDistribution;
import edu.cornell.kfs.module.purap.util.PurchasingFavoriteAccountLineBuilderForLineItem;
import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.service.UserFavoriteAccountService;
import edu.cornell.kfs.sys.service.UserProcurementProfileValidationService;
import edu.cornell.kfs.sys.service.impl.UserProcurementProfileValidationServiceImpl;

public class FavoriteAccountLineBuilderTest {
    private static UserProcurementProfileValidationService userProcurementProfileValidationService;
    private static UserFavoriteAccountService userFavoriteAccountService;
    private static FavoriteAccount testFavoriteAccount;
    private static FavoriteAccount testAltFavoriteAccount;
    private static RequisitionItem reqsItem;
    private static PurchaseOrderItem poItem;
    private static List<PurApAccountingLine> reqsAccounts;
    private static List<PurApAccountingLine> poAccounts;

    private static CuRequisitionDocument reqsDoc;
    private static PurchaseOrderDocument poDoc;
    private static IWantDocument iwntDoc;

    private static final Integer TEST_FAVORITE_ACCOUNT_LINE_ID = Integer.valueOf(1);
    private static final Integer TEST_USER_PROFILE_ID = Integer.valueOf(3);
    private static final String TEST_CHART_CODE = "IT";
    private static final String TEST_ACCOUNT_NUMBER = "G254700";
    private static final String TEST_OBJECT_CODE = "6500";

    private static final Integer TEST_ALT_FAVORITE_ACCOUNT_LINE_ID = Integer.valueOf(2);
    private static final Integer TEST_ALT_USER_PROFILE_ID = Integer.valueOf(4);
    private static final String TEST_ALT_CHART_CODE = TEST_CHART_CODE;
    private static final String TEST_ALT_ACCOUNT_NUMBER = "1653311";
    private static final String TEST_ALT_SUB_ACCOUNT_NUMBER = "58800";
    private static final String TEST_ALT_OBJECT_CODE = "6540";
    private static final String TEST_ALT_SUB_OBJECT_CODE = "100";
    private static final String TEST_ALT_PROJECT_CODE = "D-USA";
    private static final String TEST_ALT_ORG_REF_ID = "601";

    // Test subclasses to avoid constructor issues with Mockito
    private static class TestCuRequisitionDocument extends CuRequisitionDocument {
        // Empty subclass to avoid constructor issues
    }
    
    private static class TestPurchaseOrderDocument extends PurchaseOrderDocument {
        // Empty subclass to avoid constructor issues
    }
    
    private static class TestIWantDocument extends IWantDocument {
        // Empty subclass to avoid constructor issues
    }

    @BeforeClass
    public static void setUp() throws Exception {
        userProcurementProfileValidationService = new UserProcurementProfileValidationServiceImpl();
        userFavoriteAccountService = new ExtendedTestUserFavoriteAccountService();
        
        reqsAccounts = new ArrayList<PurApAccountingLine>();
        poAccounts = new ArrayList<PurApAccountingLine>();
        reqsItem = new RequisitionItem();
        poItem = new PurchaseOrderItem();
        reqsDoc = buildMockCuRequisitionDocument();
        poDoc = buildMockPurchaseOrderDocument();
        iwntDoc = buildMockIWantDocument();
        
        iwntDoc.setAccounts(new ArrayList<IWantAccount>());
        
        // Favorite Account with only some fields populated.
        testFavoriteAccount = new FavoriteAccount();
        testFavoriteAccount.setAccountLineIdentifier(TEST_FAVORITE_ACCOUNT_LINE_ID);
        testFavoriteAccount.setUserProfileId(TEST_USER_PROFILE_ID);
        testFavoriteAccount.setChartOfAccountsCode(TEST_CHART_CODE);
        testFavoriteAccount.setAccountNumber(TEST_ACCOUNT_NUMBER);
        testFavoriteAccount.setFinancialObjectCode(TEST_OBJECT_CODE);
        
        // Favorite Account with all fields populated (except reference objects or object ID or version number).
        testAltFavoriteAccount = new FavoriteAccount();
        testAltFavoriteAccount.setAccountLineIdentifier(TEST_ALT_FAVORITE_ACCOUNT_LINE_ID);
        testAltFavoriteAccount.setUserProfileId(TEST_ALT_USER_PROFILE_ID);
        testAltFavoriteAccount.setChartOfAccountsCode(TEST_ALT_CHART_CODE);
        testAltFavoriteAccount.setAccountNumber(TEST_ALT_ACCOUNT_NUMBER);
        testAltFavoriteAccount.setSubAccountNumber(TEST_ALT_SUB_ACCOUNT_NUMBER);
        testAltFavoriteAccount.setFinancialObjectCode(TEST_ALT_OBJECT_CODE);
        testAltFavoriteAccount.setFinancialSubObjectCode(TEST_ALT_SUB_OBJECT_CODE);
        testAltFavoriteAccount.setProjectCode(TEST_ALT_PROJECT_CODE);
        testAltFavoriteAccount.setOrganizationReferenceId(TEST_ALT_ORG_REF_ID);
        
        GlobalVariables.setMessageMap(new MessageMap());
    }

    private static CuRequisitionDocument buildMockCuRequisitionDocument() {
        CuRequisitionDocument document = Mockito.spy(new TestCuRequisitionDocument());
        return document;
    }
    
    private static PurchaseOrderDocument buildMockPurchaseOrderDocument() {
        PurchaseOrderDocument document = Mockito.spy(new TestPurchaseOrderDocument());
        return document;
    }
    
    private static IWantDocument buildMockIWantDocument() {
        IWantDocument document = Mockito.spy(new TestIWantDocument());
        return document; 
    }

    @Before
    public void clearListsAndMessageMapErrors() {
        clearMessageMapErrors();
        reqsItem.getSourceAccountingLines().clear();
        poItem.getSourceAccountingLines().clear();
        reqsAccounts.clear();
        poAccounts.clear();
        iwntDoc.getAccounts().clear();
    }

    public void clearMessageMapErrors() {
        GlobalVariables.getMessageMap().clearErrorMessages();
    }

    

    /*
     * Test creating favorite accounting lines for use with line items.
     */
    @Test
    public void testCreateAndAddFavoriteAccountLinesForLineItems() throws Exception {
        reqsItem.setFavoriteAccountLineIdentifier(TEST_FAVORITE_ACCOUNT_LINE_ID);
        poItem.setFavoriteAccountLineIdentifier(TEST_ALT_FAVORITE_ACCOUNT_LINE_ID);
        
        PurchasingFavoriteAccountLineBuilderForLineItem<RequisitionAccount> reqsBuilder
                = createBuilderForLineItem(reqsItem, 0, new RequisitionAccount());
        PurchasingFavoriteAccountLineBuilderForLineItem<PurchaseOrderAccount> poBuilder
                = createBuilderForLineItem(poItem, 0, new PurchaseOrderAccount());
        
        assertAccountLineCreation(reqsBuilder, testFavoriteAccount, RequisitionAccount.class);
        assertAccountLineCreation(poBuilder, testAltFavoriteAccount, PurchaseOrderAccount.class);
        assertAccountLineAdditionToList(reqsBuilder, testFavoriteAccount, RequisitionAccount.class);
        assertAccountLineAdditionToList(poBuilder, testAltFavoriteAccount, PurchaseOrderAccount.class);
    }

    /*
     * Test creating favorite accounting lines for use with the account distribution section.
     */
    @Test
    public void testCreateAndAddFavoriteAccountLinesForDistribution() throws Exception {
        reqsDoc.setFavoriteAccountLineIdentifier(TEST_ALT_FAVORITE_ACCOUNT_LINE_ID);
        poDoc.setFavoriteAccountLineIdentifier(TEST_FAVORITE_ACCOUNT_LINE_ID);
        
        PurchasingFavoriteAccountLineBuilderForDistribution<RequisitionAccount> reqsBuilder
                = createBuilderForDistribution(reqsDoc, reqsAccounts, new RequisitionAccount());
        PurchasingFavoriteAccountLineBuilderForDistribution<PurchaseOrderAccount> poBuilder
                = createBuilderForDistribution(poDoc, poAccounts, new PurchaseOrderAccount());
        
        assertAccountLineCreation(reqsBuilder, testAltFavoriteAccount, RequisitionAccount.class);
        assertAccountLineCreation(poBuilder, testFavoriteAccount, PurchaseOrderAccount.class);
        assertAccountLineAdditionToList(reqsBuilder, testAltFavoriteAccount, RequisitionAccount.class);
        assertAccountLineAdditionToList(poBuilder, testFavoriteAccount, PurchaseOrderAccount.class);
    }

    /*
     * Test creating favorite accounting lines for use with the IWantDocument.
     */
    @Test
    public void testCreateAndAddFavoriteAccountLinesForIWant() throws Exception {
        iwntDoc.setFavoriteAccountLineIdentifier(TEST_FAVORITE_ACCOUNT_LINE_ID);
        
        PurApFavoriteAccountLineBuilderForIWantDocument iwntBuilder
                = createBuilderForIWant(iwntDoc);
        
        assertAccountLineCreation(iwntBuilder, testFavoriteAccount, IWantAccount.class);
        assertAccountLineAdditionToList(iwntBuilder, testFavoriteAccount, IWantAccount.class);
    }

    /*
     * Test creating and adding multiple accounting lines for distinct favorite accounts.
     */
    @Test
    public void testCreateAndAddMultipleAccountLines() throws Exception {
        reqsItem.setFavoriteAccountLineIdentifier(TEST_FAVORITE_ACCOUNT_LINE_ID);
        poDoc.setFavoriteAccountLineIdentifier(TEST_FAVORITE_ACCOUNT_LINE_ID);
        iwntDoc.setFavoriteAccountLineIdentifier(TEST_FAVORITE_ACCOUNT_LINE_ID);
        
        PurchasingFavoriteAccountLineBuilderForLineItem<RequisitionAccount> reqsBuilder
                = createBuilderForLineItem(reqsItem, 0, new RequisitionAccount());
        PurchasingFavoriteAccountLineBuilderForDistribution<PurchaseOrderAccount> poBuilder
                = createBuilderForDistribution(poDoc, poAccounts, new PurchaseOrderAccount());
        PurApFavoriteAccountLineBuilderForIWantDocument iwntBuilder
                = createBuilderForIWant(iwntDoc);
        
        // Initial addition should succeed.
        assertAccountLineAdditionToList(reqsBuilder, testFavoriteAccount, RequisitionAccount.class);
        assertAccountLineAdditionToList(poBuilder, testFavoriteAccount, PurchaseOrderAccount.class);
        assertAccountLineAdditionToList(iwntBuilder, testFavoriteAccount, IWantAccount.class);
        
        // Subsequent addition for a different favorite account should also succeed.
        reqsItem.setFavoriteAccountLineIdentifier(TEST_ALT_FAVORITE_ACCOUNT_LINE_ID);
        poDoc.setFavoriteAccountLineIdentifier(TEST_ALT_FAVORITE_ACCOUNT_LINE_ID);
        iwntDoc.setFavoriteAccountLineIdentifier(TEST_ALT_FAVORITE_ACCOUNT_LINE_ID);
        assertAccountLineAdditionToList(reqsBuilder, testAltFavoriteAccount, RequisitionAccount.class);
        assertAccountLineAdditionToList(poBuilder, testAltFavoriteAccount, PurchaseOrderAccount.class);
        assertAccountLineAdditionToList(iwntBuilder, testAltFavoriteAccount, IWantAccount.class);
    }

    /*
     * Test that a null line ID will cause line creation to fail.
     */
    @Test
    public void testLineBuilderFailureForNullLineID() throws Exception {
        reqsItem.setFavoriteAccountLineIdentifier(null);
        poDoc.setFavoriteAccountLineIdentifier(null);
        iwntDoc.setFavoriteAccountLineIdentifier(null);
        
        PurchasingFavoriteAccountLineBuilderForLineItem<RequisitionAccount> reqsBuilder
                = createBuilderForLineItem(reqsItem, 0, new RequisitionAccount());
        PurchasingFavoriteAccountLineBuilderForDistribution<PurchaseOrderAccount> poBuilder
                = createBuilderForDistribution(poDoc, poAccounts, new PurchaseOrderAccount());
        PurApFavoriteAccountLineBuilderForIWantDocument iwntBuilder
                = createBuilderForIWant(iwntDoc);
        
        assertUnsuccessfulAccountLineCreation(reqsBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineCreation(poBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineCreation(iwntBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineAdditionToList(reqsBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineAdditionToList(poBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineAdditionToList(iwntBuilder);
    }

    /*
     * Test that line creation will fail if no favorite account exists for the given ID.
     */
    @Test
    public void testLineBuilderFailureForNonexistentLine() throws Exception {
        Integer badId = Integer.valueOf(-1);
        
        reqsItem.setFavoriteAccountLineIdentifier(badId);
        poDoc.setFavoriteAccountLineIdentifier(badId);
        iwntDoc.setFavoriteAccountLineIdentifier(badId);
        
        PurchasingFavoriteAccountLineBuilderForLineItem<RequisitionAccount> reqsBuilder
                = createBuilderForLineItem(reqsItem, 0, new RequisitionAccount());
        PurchasingFavoriteAccountLineBuilderForDistribution<PurchaseOrderAccount> poBuilder
                = createBuilderForDistribution(poDoc, poAccounts, new PurchaseOrderAccount());
        PurApFavoriteAccountLineBuilderForIWantDocument iwntBuilder
                = createBuilderForIWant(iwntDoc);
        
        assertUnsuccessfulAccountLineCreation(reqsBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineCreation(poBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineCreation(iwntBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineAdditionToList(reqsBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineAdditionToList(poBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineAdditionToList(iwntBuilder);
    }

    /*
     * Test that line creation will fail if a matching line already exists.
     */
    @Test
    public void testLineBuilderFailureForPreexistingLine() throws Exception {
        reqsItem.setFavoriteAccountLineIdentifier(TEST_FAVORITE_ACCOUNT_LINE_ID);
        poDoc.setFavoriteAccountLineIdentifier(TEST_FAVORITE_ACCOUNT_LINE_ID);
        iwntDoc.setFavoriteAccountLineIdentifier(TEST_FAVORITE_ACCOUNT_LINE_ID);
        
        PurchasingFavoriteAccountLineBuilderForLineItem<RequisitionAccount> reqsBuilder
                = createBuilderForLineItem(reqsItem, 0, new RequisitionAccount());
        PurchasingFavoriteAccountLineBuilderForDistribution<PurchaseOrderAccount> poBuilder
                = createBuilderForDistribution(poDoc, poAccounts, new PurchaseOrderAccount());
        PurApFavoriteAccountLineBuilderForIWantDocument iwntBuilder
                = createBuilderForIWant(iwntDoc);
        
        // First additions should succeed.
        assertAccountLineAdditionToList(reqsBuilder, testFavoriteAccount, RequisitionAccount.class);
        assertAccountLineAdditionToList(poBuilder, testFavoriteAccount, PurchaseOrderAccount.class);
        assertAccountLineAdditionToList(iwntBuilder, testFavoriteAccount, IWantAccount.class);
        
        // Subsequent creations or additions for the same ID should fail.
        assertUnsuccessfulAccountLineCreation(reqsBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineCreation(poBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineCreation(iwntBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineAdditionToList(reqsBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineAdditionToList(poBuilder);
        clearMessageMapErrors();
        assertUnsuccessfulAccountLineAdditionToList(iwntBuilder);
    }



    private <T extends PurApAccountingLine> PurchasingFavoriteAccountLineBuilderForLineItem<T>
            createBuilderForLineItem(PurchasingItemBase lineItem, int lineIndex, T sampleAccountingLine) {
        PurchasingFavoriteAccountLineBuilderForLineItem<T> builder = new PurchasingFavoriteAccountLineBuilderForLineItem<>(
                lineItem, lineIndex, sampleAccountingLine);
        configureServicesOnBuilder(builder);
        return builder;
    }

    private <T extends PurApAccountingLine> PurchasingFavoriteAccountLineBuilderForDistribution<T>
            createBuilderForDistribution(PurchasingDocumentBase document, List<PurApAccountingLine> lines, T sampleAccountingLine) {
        PurchasingFavoriteAccountLineBuilderForDistribution<T> builder = new PurchasingFavoriteAccountLineBuilderForDistribution<>(
                document, lines, sampleAccountingLine);
        configureServicesOnBuilder(builder);
        return builder;
    }

    private PurApFavoriteAccountLineBuilderForIWantDocument createBuilderForIWant(IWantDocument document) {
        PurApFavoriteAccountLineBuilderForIWantDocument builder = new PurApFavoriteAccountLineBuilderForIWantDocument(document);
        configureServicesOnBuilder(builder);
        return builder;
    }

    private <E extends GeneralLedgerPendingEntrySourceDetail,T extends E>
            void configureServicesOnBuilder(FavoriteAccountLineBuilderBase<E,T> lineBuilder) {
        lineBuilder.setUserProcurementProfileValidationService(userProcurementProfileValidationService);
        lineBuilder.setUserFavoriteAccountService(userFavoriteAccountService);
    }

    /*
     * Test the successful addition a new line to the list.
     */
    private <E extends GeneralLedgerPendingEntrySourceDetail,T extends E>
            void assertAccountLineAdditionToList(FavoriteAccountLineBuilderBase<E,T> builder,
                    FavoriteAccount expectedAccount, Class<T> expectedLineClass) throws Exception {
        int oldSize = builder.getAccountingLines().size();
        assertFalse("Message map should have been empty", GlobalVariables.getMessageMap().hasErrors());
        
        builder.addNewFavoriteAccountLineToListIfPossible();
        
        assertFalse("Message map should have been empty", GlobalVariables.getMessageMap().hasErrors());
        assertEquals("Accounting line list has wrong size after line addition", oldSize + 1, builder.getAccountingLines().size());
        assertAccountingLineHasCorrectConfiguration(expectedAccount, expectedLineClass,
                builder.getAccountingLines().get(builder.getAccountingLines().size() - 1));
    }

    /*
     * Test the unsuccessful addition a new line to the list.
     */
    private <E extends GeneralLedgerPendingEntrySourceDetail,T extends E>
            void assertUnsuccessfulAccountLineAdditionToList(FavoriteAccountLineBuilderBase<E,T> builder) throws Exception {
        int oldSize = builder.getAccountingLines().size();
        assertFalse("Message map should have been empty", GlobalVariables.getMessageMap().hasErrors());
        
        builder.addNewFavoriteAccountLineToListIfPossible();
        
        assertTrue("Message map should have been non-empty", GlobalVariables.getMessageMap().hasErrors());
        assertEquals("Accounting line list should not have changed size after failed line addition", oldSize, builder.getAccountingLines().size());
    }

    /*
     * Test the successful creation of a new line.
     */
    private <E extends GeneralLedgerPendingEntrySourceDetail,T extends E>
            void assertAccountLineCreation(FavoriteAccountLineBuilderBase<E,T> builder,
                    FavoriteAccount expectedAccount, Class<T> expectedLineClass) throws Exception {
        assertFalse("Message map should have been empty", GlobalVariables.getMessageMap().hasErrors());
        
        T acctLine = builder.createNewFavoriteAccountLineIfPossible();
        assertNotNull("New accounting line should have been non-null", acctLine);
        assertFalse("Message map should have been empty", GlobalVariables.getMessageMap().hasErrors());
        assertAccountingLineHasCorrectConfiguration(expectedAccount, expectedLineClass, acctLine);
    }

    /*
     * Test the unsuccessful creation of a new line.
     */
    private <E extends GeneralLedgerPendingEntrySourceDetail,T extends E>
            void assertUnsuccessfulAccountLineCreation(FavoriteAccountLineBuilderBase<E,T> builder) throws Exception {
        assertFalse("Message map should have been empty", GlobalVariables.getMessageMap().hasErrors());

        T acctLine = builder.createNewFavoriteAccountLineIfPossible();
        assertNull("New accounting line should have been null", acctLine);
        assertTrue("Message map should have been non-empty", GlobalVariables.getMessageMap().hasErrors());
        
    }

    /*
     * Convenience method for validating that the generated accounting line has the correct data and is of the correct type.
     */
    private void assertAccountingLineHasCorrectConfiguration(FavoriteAccount expectedAccount,
            Class<? extends GeneralLedgerPendingEntrySourceDetail> expectedLineClass, GeneralLedgerPendingEntrySourceDetail acctLine) throws Exception {
        if (!expectedLineClass.isAssignableFrom(acctLine.getClass())) {
            fail("Expected line type is " + expectedLineClass.getName() + " but the generated line is of type " + acctLine.getClass().getName()
                    + " which is not an instance of the expected one");
        }
        assertEquals("Generated line has wrong chart code", expectedAccount.getChartOfAccountsCode(), acctLine.getChartOfAccountsCode());
        assertEquals("Generated line has wrong account number", expectedAccount.getAccountNumber(), acctLine.getAccountNumber());
        assertEquals("Generated line has wrong sub-account number", expectedAccount.getSubAccountNumber(), acctLine.getSubAccountNumber());
        assertEquals("Generated line has wrong object code", expectedAccount.getFinancialObjectCode(), acctLine.getFinancialObjectCode());
        assertEquals("Generated line has wrong sub-object code", expectedAccount.getFinancialSubObjectCode(), acctLine.getFinancialSubObjectCode());
        assertEquals("Generated line has wrong project code", expectedAccount.getProjectCode(), acctLine.getProjectCode());
        assertEquals("Generated line has wrong org ref ID", expectedAccount.getOrganizationReferenceId(), acctLine.getOrganizationReferenceId());
        
        // Validate pre-initialized percentage. The retrieval means will vary depending on line type and whether we need hacks to avoid Spring calls.
        if (acctLine instanceof RequisitionAccount) {
            assertEquals("Generated line has wrong percentage (comparison against 100% should have been zero)",
                    0, getRequisitionAccountLinePercent(((RequisitionAccount) acctLine)).compareTo(new BigDecimal(100)));
        } else if (acctLine instanceof PurApAccountingLine) {
            assertEquals("Generated line has wrong percentage (comparison against 100% should have been zero)",
                    0, ((PurApAccountingLine) acctLine).getAccountLinePercent().compareTo(new BigDecimal(100)));
        } else if (acctLine instanceof IWantAccount) {
            assertEquals("Generated line has wrong amount-or-percent indicator", CUPurapConstants.PERCENT, ((IWantAccount) acctLine).getUseAmountOrPercent());
            assertEquals("Generated line has wrong percentage (comparison against 100% should have been zero)",
                    0, ((IWantAccount) acctLine).getAmountOrPercent().compareTo(new KualiDecimal(100)));
        }
    }

    /*
     * Convenience method for using reflection to retrieve a REQS account line's percentage,
     * to avoid Spring calls that would normally occur in its getter method.
     * 
     * NOTE: This will prevent certain auto-scaling or auto-construction work from occurring
     * since the getter normally handles that; however, for the use cases of this unit test class,
     * we expect the value to be pre-initialized already with an acceptable scale.
     */
    private BigDecimal getRequisitionAccountLinePercent(RequisitionAccount acctLine) {
        try {
            Field percentField = PurApAccountingLineBase.class.getDeclaredField("accountLinePercent");
            percentField.setAccessible(true);
            return (BigDecimal) percentField.get(acctLine);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    /*
     * Test-only UserFavoriteAccountService implementation.
     */
    private static class ExtendedTestUserFavoriteAccountService extends TestUserFavoriteAccountServiceImpl {
        @Override
        public FavoriteAccount getSelectedFavoriteAccount(Integer accountLineIdentifier) {
            FavoriteAccount favAccount = null;
            if (TEST_FAVORITE_ACCOUNT_LINE_ID.equals(accountLineIdentifier)) {
                favAccount = testFavoriteAccount;
            } else if (TEST_ALT_FAVORITE_ACCOUNT_LINE_ID.equals(accountLineIdentifier)) {
                favAccount = testAltFavoriteAccount;
            }
            return favAccount;
        }
    }

}
