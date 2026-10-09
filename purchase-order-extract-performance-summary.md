# CEMI Purchase Order Extract: Performance Improvement

## Problem

The Purchase Order extract job (`CreateCemiPurchaseOrderExtractStep`) ran very slowly. Almost all of the time went into
loading each PO and gathering its data, not into finding which POs were in scope.

Phase 2 (`generateIntermediateExtractData`) used to load each PO with
`DocumentService.getDocumentsByListOfDocumentHeaderIds`, 50 at a time. For every PO, KFS then:

- loaded the workflow document;
- ran `processAfterRetrieve()`, which calls `refreshNonUpdateableReferences()` on the PO **and on every accounting line
  of every item**, re-querying chart, account, object code and other references;
- loaded the PO's notes, which the extract never uses.

The extract then made more database calls for each PO:

- two `personService.findPeople` searches, for the requestor and the delivery recipient;
- a `getPersonByPrincipalName` lookup for the default buyer whenever the requestor had no match;
- a `getSupplierIdForVendor` SQL query;
- `refreshReferenceObject("vendorPaymentTerms")`;
- a `LocationService` lookup for the delivery country name.

Altogether this came to dozens of small database queries for each PO.

## Solution

Phase 2 now gets its data from **database views** and no longer loads KFS documents. No new tables are created, and
nothing needs to be loaded before the job runs. The three-phase step from the pattern template is unchanged:

| Phase | Method | Change |
|---|---|---|
| 1 | `resetState`, `captureInScopeBusinessObjectKeysToProcessingTable` | None. The in-scope table is still filled from `CU_CEMI_EXTR_PURCHASE_ORDER_OPEN_PO_DOCS_V`. |
| 2 | `generateIntermediateExtractData` | **One** streaming JDBC query over the new views replaces the document loads and the per-PO lookups. |
| 3 | `generateDataConversionExtractFile` | None. |

The query returns one row per PO, open item and open accounting line, sorted by PO number, then line, then account.
`CemiPurchaseOrderIterator` groups consecutive rows into one PO at a time, so memory use stays flat however many POs
are in scope.

Person lookups stay in Java, because the name parsing and the "exactly one match" rule are hard to reproduce reliably
in SQL. Their results are now **cached for the job run**, keyed on name and email, so a requestor who appears on many
POs is looked up only once. The default buyer is also looked up only once per run.

### What replaced each per-PO call

| Previous source | New source |
|---|---|
| `DocumentService` load (with workflow, reference refresh and notes) | `CU_CEMI_EXTR_PURCHASE_ORDER_HDR_V`, `..._OPEN_ITM_V`, `..._OPEN_ACCT_V` |
| `WorkflowDocument` status and approval date | `KREW_DOC_HDR_T.DOC_HDR_STAT_CD`, `APRV_DT` (header view) |
| `refreshReferenceObject("vendorPaymentTerms")` | Join to `PUR_PMT_TERM_TYP_T` (header view) |
| `getDeliveryCountryName()` via `LocationService` | Join to `SH_CNTRY_T` (header view) |
| `getTotalDollarAmount()` and item `getTotalAmount()` | Calculated in `CU_CEMI_EXTR_PURCHASE_ORDER_ITM_V` |
| Freight item search in Java | `FRHT_OSTND_ENC_AMT` column (header view) |
| `getSupplierIdForVendor` query for each PO | Supplier table joined into the extract query, filtered by the supplier run-date parameter |
| Open item and open account filtering in Java | `..._OPEN_ITM_V` and `..._OPEN_ACCT_V` |
| `personService` searches for each PO | `CemiPurchaseOrderEmployeeIdLookup`, cached for the run |

## Database changes (nonprod-sql)

File: `kfs/cemi-009-purchase-order-step-002-create-views.sql`

The existing view `CU_CEMI_EXTR_PURCHASE_ORDER_OPEN_PO_DOCS_V` is unchanged. Four views were added, each joined to
the in-scope table `CU_CEMI_EXTR_PURCHASE_ORDER_IN_SCOPE_PO_DOCS_T`:

| View | Contents |
|---|---|
| `CEMI.CU_CEMI_EXTR_PURCHASE_ORDER_ITM_V` | Every item on the in-scope POs, with `ITM_TOT_AMT` calculated the same way as `PurApItemBase.getTotalAmount()`: unit price × quantity for quantity-based item types, otherwise the unit price, plus either the sales tax or the summed use tax, depending on `USE_TAX_IND` |
| `CEMI.CU_CEMI_EXTR_PURCHASE_ORDER_HDR_V` | PO header fields, workflow status and approval date, payment terms description, country name, PO total and remaining freight amount |
| `CEMI.CU_CEMI_EXTR_PURCHASE_ORDER_OPEN_ITM_V` | Active items with an outstanding encumbered amount greater than 0 |
| `CEMI.CU_CEMI_EXTR_PURCHASE_ORDER_OPEN_ACCT_V` | Accounting lines with an outstanding encumbrance greater than 0 |

## Java changes (cu-kfs)

Package: `edu.cornell.kfs.cemi.module.purap`

**Added**
- `batch/businessobject/CemiLegacyPurchaseOrder`, `CemiLegacyPurchaseOrderItem`, `CemiLegacyPurchaseOrderAccount`:
  simple data objects that replace `PurchaseOrderDocument`, `PurchaseOrderItem` and `PurchaseOrderAccount`.
- `batch/businessobject/CemiPurchaseOrderExtractRow`: one row of the extract query.
- `batch/service/impl/CemiPurchaseOrderEmployeeIdLookup`: the person search moved out of the header factory, with
  caching added.

**Modified**
- `dataaccess/CemiPurchaseOrderExtractDao` and `CemiPurchaseOrderExtractDaoJdbcImpl`:
  - added `getPurchaseOrderExtractRowsAsCloseableStream(supplierJobRunDateString)`, which uses
    `JdbcTemplate.queryForStream` with a fetch size of 500;
  - removed `getSupplierIdForVendor`;
  - the development smaller-data-set filter moved here from the ORM DAO.
- `batch/service/impl/CemiPurchaseOrderIterator`: rewritten to group sorted rows into POs, replacing the 50-document
  batch loader.
- `batch/service/impl/CemiPurchaseOrderExtractServiceImpl`: uses the new stream and iterator; the `DocumentService`,
  `DataDictionaryService` and ORM DAO dependencies were removed.
- `batch/service/CemiPurchaseOrderFileExtractDataBuilder` and `CemiPurchaseOrderFileExtractDataBuilderDefaultImpl`:
  work with the new data objects; Java now only filters items by type.
- `batch/service/impl/factory/CemiPurchaseOrderHeaderBoFactory`, `...GoodsLineBoFactory`, `...ServiceLineBoFactory`
  and `...LineSplitBoFactory`: work with the new data objects. **The output logic is unchanged.**
- `util/CemiPurchaseOrderUtils`: removed the account-filtering helpers, which the views now handle.
- `CemiPurchaseOrderConstants`: removed `MAX_PURCHASE_ORDER_PRELOAD_BATCH_SIZE`.
- `cu-spring-cemi.xml`:
  - removed the `cemiPurchaseOrderExtractOrmDao` bean;
  - removed the `documentService` and `dataDictionaryService` properties from the service;
  - added `configurationService` to the JDBC DAO.
- `cu-ojb-cemi-purchase-order.xml`: removed the `CemiPurchaseOrderIdBo` mapping.

**Deleted**
- `dataaccess/CemiPurchaseOrderExtractOrmDao` and `dataaccess/impl/CemiPurchaseOrderExtractOrmDaoOjbImpl`
- `batch/businessobject/CemiPurchaseOrderIdBo`

## Behaviour differences

1. **Blank requestor or delivery name:** previously this could cause a NullPointerException during name parsing. Now
   the existing "insufficient information" warning is logged and the employee ID is left blank.
2. **Duplicate supplier rows:** if the supplier table has more than one row for the same vendor and run date, the
   query takes the lowest `SUPPLIER_ID`. The old query used whichever row came back first.

Everything else is intended to produce the same output as before.

## Before deploying

1. **Grants:** the CEMI schema needs direct `SELECT` grants (not grants through a role) on these KFS tables, which the
   new views read: `PUR_PO_ITM_T`, `PUR_PO_ACCT_T`, `PUR_AP_ITM_TYP_T`, `PUR_PO_ITM_USE_TAX_T`, `PUR_PMT_TERM_TYP_T`
   and `SH_CNTRY_T`.
2. **Compile and test:** the changes have not yet been compiled or run.
3. **Compare output:** run the old and new code on the same development subset (`cu.cemi.development.use.smaller.data.set=true`)
   and diff `CEMI.CU_CEMI_EXTR_PURCHASE_ORDER_TAB_SUBMIT_PURCHASE_ORDER_T`. Pay close attention to:
   - the "Original PO Amount" and "Original PO Line Amount" memo fields, whose totals are now calculated in SQL;
   - the document date, freight amount, payment terms and ship-to country.
4. **Measure run time:** time a full run of Phase 2 before and after the change to confirm the improvement.
