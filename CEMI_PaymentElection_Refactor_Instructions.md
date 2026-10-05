# Refactoring the CEMI Payment Election Extract to the Pattern Template

## 1. Goal

`CreateCemiPaymentElectionExtractStep` was written before the CEMI pattern template existed. It writes its data to CSV files and has its own copy of the code that builds, masks and copies the spreadsheet.

Newer extracts follow the pattern in `edu.cornell.kfs.cemi.patterntemplate`. The Award Schedule extract (`CreateCemiAwardScheduleExtractStep`) is the simplest working example. They store each spreadsheet row in a database table and reuse shared base classes for everything else.

Your job is to move Payment Election onto that pattern **without changing the content of the spreadsheet it produces**.

**Keep all code in the existing `edu.cornell.kfs.cemi.pdp` package.** Do not move it to `cemi/module/...`.

---

## 2. Read these first

Before writing any code, read these files in this order:

1. `src/main/resources/edu/cornell/kfs/cemi/patterntemplate/PatternTemplateReadMe.txt`: the official process.
2. Every Java file under `src/main/java/edu/cornell/kfs/cemi/patterntemplate/`. Each one has comments explaining its role.
3. The Award Schedule extract, which is a real implementation of the template:
   - `cemi/module/cg/batch/CreateCemiAwardScheduleExtractStep.java`
   - `cemi/module/cg/batch/service/impl/CemiAwardScheduleExtractServiceImpl.java`
   - `cemi/module/cg/batch/service/impl/CemiAwardScheduleFileExtractDataBuilderDefaultImpl.java`
   - `cemi/module/cg/batch/factory/CemiAwardScheduleFileAwardScheduleTabRowBoFactory.java`
   - `cemi/module/cg/batch/businessobject/CemiAwardScheduleFileAwardScheduleTabRowBo.java`
   - `cemi/module/cg/dataaccess/...` (both DAOs)
   - `resources/.../cemi/module/cg/cu-ojb-cemi-award-schedule.xml`
   - The "Award Schedule" section of `resources/.../cemi/cu-spring-cemi.xml`
4. The shared base classes. **Do not modify these.**
   - `cemi/sys/batch/service/impl/CemiDataExtractServiceBase.java`
   - `cemi/sys/batch/service/impl/CemiOrmDataBuilderBase.java`
   - `cemi/sys/batch/businessobject/CemiIndexedBusinessObjectBase.java`
   - `cemi/sys/dataaccess/impl/CemiOrmDaoOjbImplBase.java`
   - `cemi/sys/batch/service/impl/CemiFileAppenderServiceImpl.java` and `cemi/sys/dataaccess/impl/CemiFileAppenderOrmDaoOjbImpl.java`

---

## 3. The pattern

### 3.1 The three phases

Every extract step runs the same four service calls:

```java
service.resetState();                                      // Phase 1: truncate the "in-scope keys" table
service.captureInScopeBusinessObjectKeysToProcessingTable(); // Phase 1: copy keys from the scope VIEW into that table
service.generateIntermediateExtractData(jobRunDate);       // Phase 2: one DB row per spreadsheet row
service.generateDataConversionExtractFile(jobRunDate);     // Phase 3: DB rows -> .xlsx (base class does this)
```

### 3.2 What each class does

| Role | Pattern name | Payment Election name |
|---|---|---|
| Batch step | `CreateCemi{X}ExtractStep` | `CreateCemiPaymentElectionExtractStep` |
| Service interface and implementation (extends `CemiDataExtractServiceBase`) | `Cemi{X}ExtractService(Impl)` | `CemiPaymentElectionExtractService(Impl)` |
| Plain JDBC DAO (truncate, insert keys, mapping rows) | `Cemi{X}ExtractDao` / `...DaoJdbcImpl` | `CemiPaymentElectionDao` / `CemiPaymentElectionDaoJdbcImpl` |
| ORM DAO (streams the legacy business objects) | `Cemi{X}ExtractOrmDao` / `...OrmDaoOjbImpl` | `CemiPaymentElectionOrmDao` / `CemiPaymentElectionOrmDaoOjbImpl` |
| Data builder: loops over legacy objects, calls the factory, saves rows. **No conversion logic.** | `Cemi{X}FileExtractDataBuilder(DefaultImpl)` | `CemiPaymentElectionFileExtractDataBuilder(DefaultImpl)` |
| Factory: converts **one** legacy object into **one** row. **All conversion logic goes here.** | `Cemi{X}File{TAB}TabRowBoFactory` | `CemiPaymentElectionFileGroupTwoTabRowBoFactory` |
| Row business object for one spreadsheet tab (extends `CemiIndexedBusinessObjectBase`) | `Cemi{X}File{TAB}TabRowBo` | `CemiPaymentElectionFileGroupTwoTabRowBo` |
| OJB mapping (one file per extract) | `cu-ojb-cemi-{x}.xml` | `pdp/cu-ojb-cemi-payment-election.xml` |
| Output definition (maps tab columns to row-BO properties) | `Cemi{X}ExtractFileOutputDefinition.xml` | already exists; needs `business-object-class` added |
| Constants | `Cemi{X}Constants` | `CemiPaymentElectionConstants` |

### 3.3 How the data flows

```
CU_CEMI_PYMNT_ELCTN_EXTR_V (view)
   --Dao.queryAndStore...--> CU_CEMI_PYMNT_ELCTN_EXTR_ACH_ACCT_T (in-scope keys)
   --OrmDao stream--> PayeeACHAccount objects
   --DataBuilder loop + Factory--> CemiPaymentElectionFileGroupTwoTabRowBo
   --storeSheetRow()--> CU_CEMI_EXTR_PYMNT_ELCTN_TAB_GRP_TWO_T  (+ mapping row in CU_CEMI_MAPPING_PYMNT_ELCTN_EXTR_FILE_T)
   --CemiDataExtractServiceBase.generateFileForDataExtract--> Payment_Election_ITH_<date>.xlsx
```

### 3.4 What the base classes already handle

You do not write any of this. You only configure it:

- `storeSheetRow()` sets `jobRunDateString` and `jobRunRowIndex` on each row and saves it. Don't use a DB sequence.
- Masking: `shouldMaskCemiSensitiveData()` reads parameter `CEMI_SENSITIVE_DATA_MASKING_SETTING` and checks whether this is the CEMI environment.
- Building the xlsx from the template, writing the rows, and copying the file to the outbound folder (controlled by parameter `COPY_CEMI_FILE_TO_OUTBOUND_FOLDER`).
- The concrete service only overrides three methods: `getComponentClassForDataExtractParameter()`, `getOutputDefinitionFilePathSuffix()` and `getTemplateWorkbookFilePathSuffix()`.

### 3.5 What changes and what stays the same

| Stays the same | Changes |
|---|---|
| Scope view, scope table, mapping table | Intermediate storage moves from CSV files to a DB table |
| Every value in every column of the spreadsheet | Old `CemiGroupTwo` DTO + `CemiPaymentElectionGroupTwoBo` are replaced by one row BO and a factory |
| The masking rules | The custom service code is replaced by `CemiDataExtractServiceBase` |
| Job and bean names | The copy-to-outbound parameter is renamed to the shared name |

---

## 4. Before you start: capture a baseline

You need a known-good output to compare against after every step.

1. Make sure parameter `CEMI_SENSITIVE_DATA_MASKING_SETTING` for `KFS-CEMI` / `CreateCemiPaymentElectionExtractStep` is set to `MASK`.
2. Run `createCemiPaymentElectionExtractJob` locally on the **current, unchanged** code.
3. Save the generated `Payment_Election_ITH_<timestamp>.xlsx` from `${staging.directory}/cemi/pdp/cemiPaymentElectionExtract` somewhere outside the repo. This is your **baseline**.
4. Note how many rows the job wrote to `CEMI.CU_CEMI_MAPPING_PYMNT_ELCTN_EXTR_FILE_T` for that run date.

**"Verify" in each step below means:** build, start the app, run the job, and check:
- The job succeeds.
- The new xlsx has the same row count as the baseline, and the same values in the same order. Compare the first, last and a few middle rows of every column.
- A new set of mapping rows was written, with the same count as the baseline.

---

## 5. The work, split into steps

Each step is a separate commit or PR. **After every step the code compiles and the job still produces a correct Payment Election extract.** Do not start a step until the previous one is verified.

Steps 1–4 are refactors that don't change behaviour. Step 5 is the only switch-over. Step 6 removes dead code.

### Step 0: Database changes (nonprod-sql repository, separate branch)

These changes only add things, so the current code keeps working.

1. **Create the new tab table** `CEMI.CU_CEMI_EXTR_PYMNT_ELCTN_TAB_GRP_TWO_T`. Copy the column types and sizes from the existing `CU_CEMI_EXTR_GRP_TWO_TAB_PYMNT_ELCTN_T` DDL, with these differences:
   - Primary key is `(JOB_RUN_ROW_INDEX NUMBER(14,0) NOT NULL, EXTR_FILE_RUNDATE VARCHAR2(20) NOT NULL)`.
   - Drop `EXTR_TBL_ROW_ID`. A sequence is no longer needed.
   - Keep `ACH_ACCT_GNRTD_ID`, `EMPL_ID`, and all the `..._2` / `..._2_1` columns.
   - Rename `BNK_RTNG_NBR` to `BNK_RTNG_NBR_2_1` and `DISTRIB_BAL_2_1` to `DISTRB_BAL_2_1` so they match the naming of the other columns. *(Confirm with your lead; if they'd rather keep the old names, just use the old names in the OJB file in Step 3.)*
2. **Add the shared parameter** `COPY_CEMI_FILE_TO_OUTBOUND_FOLDER` for `KFS-CEMI` / `CreateCemiPaymentElectionExtractStep`. Use the same value as the existing `COPY_CEMI_PAYMENT_ELECTION_FILE_TO_OUTBOUND_FOLDER`. Follow `patterntemplate/examplesql/cemi-XXX-EXTRACT-NAME-step-003-create-parameters.sql`. **This parameter must exist before Step 5.** The base class unboxes it to a `boolean`, so the job fails if it's missing.
3. `ACCT_NBR_2_1` is encrypted. Per README step 5, add scrub SQL for the new table's `ACCT_NBR_2_1` to `manual/kfs/KFSPTS-38305-cemiManualScrub.sql`.
4. Per README step 9, add cleanup statements for the new table to `manual/kfs/KFSPTS-38161-deleteCemiTestRunData.sql`.

Do not drop the old table, sequence or parameter yet. That happens in Step 6.

**Verify:** run the scripts locally, then run the job on unchanged code. The output should still match the baseline.

---

### Step 1: Rename the DAO methods to the pattern names

Only the DAOs, plus the callers that have to change with them.

1. `CemiPaymentElectionDao` / `CemiPaymentElectionDaoJdbcImpl`:
   - `clearExistingListOfExtractablePayeeAchAccountGeneratedIds()` → `clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution()`
   - `queryAndStorePayeeAchAccountGeneratedIdsForPaymentElectionExtract()` → `queryAndStoreInScopeBusinessObjectKeysForDataExtract()`
   - `storeEmployeeIdAchAccountGeneratedIdPaymentElectionExtractRunDate(String, KualiInteger, LocalDateTime)` → `storeSpreadsheetRowItemKeyLegacyObjectKeyExtractRunDateMapping(String employeeId, KualiInteger achAccountGeneratedIdentifier, String jobRunDateString)`. It now takes the **already-formatted** run-date string, so the DAO no longer needs `DateTimeService` or `CemiUtils`.
   - Remove the `dateTimeService` field and its getter/setter from the JDBC implementation.
2. `CemiPaymentElectionOrmDao` / `CemiPaymentElectionOrmDaoOjbImpl`:
   - Rename `getPayeeAchAccountIdsForCemiPaymentElectionExtractAsCloseableStream()` → `getPayeeAchAccountsForCemiPaymentElectionExtractAsCloseableStream()`. It returns accounts, not IDs.
   - Change `extends PlatformAwareDaoBaseOjb` to `extends CemiOrmDaoOjbImplBase`. Delete the two private static helper methods and use the inherited `shouldUseLessDataDuringCemiDevelopment()`.
   - Order by `PdpPropertyConstants.ACH_ACCOUNT_GENERATED_IDENTIFIER` from base KFS instead of the custom constant.
3. `cu-spring-cemi.xml`:
   - `cemiPaymentElectionDao-parentBean`: remove `p:dateTimeService-ref`.
   - `cemiPaymentElectionOrmDao-parentBean`: add `p:configurationService-ref="configurationService"`. The base class requires it.
4. Update the callers in the old code so it still compiles:
   - `CemiPaymentElectionExtractServiceImpl`: use the new DAO method names.
   - `CemiPaymentElectionDataBuilderBase.recordPaymentElectionIdentifiersInLegacyAssociationTable`: pass `CemiUtils.generateBatchJobRunDateAsString(jobRunDate)` to the renamed method.
5. Delete `CemiPaymentElectionPropertyConstants.java`. Nothing uses it any more.

**Verify:** the output matches the baseline.

---

### Step 2: Rename the service methods to the pattern names

1. `CemiPaymentElectionExtractService` (and its implementation):
   - `populateListOfInScopeEmployeePaymentElections()` → `captureInScopeBusinessObjectKeysToProcessingTable()`
   - `generateIntermediatePaymentElectionExtractData(...)` → `generateIntermediateExtractData(...)`
   - `generatePaymentElectionExtractFile(...)` → `generateDataConversionExtractFile(...)`
2. Update `CreateCemiPaymentElectionExtractStep` to call the new names. Compare it with `CreateCemiAwardScheduleExtractStep`; the two should now look the same.

The method bodies don't change. This step only renames.

**Verify:** the output matches the baseline.

---

### Step 3: Add the row business object and its persistence (not used yet)

1. Create `pdp/batch/businessobject/CemiPaymentElectionFileGroupTwoTabRowBo.java`:
   - `extends CemiIndexedBusinessObjectBase`. Do **not** declare `jobRunDateString` or `jobRunRowIndex`; they're inherited.
   - Key attribute: `KualiInteger achAccountGeneratedIdentifierUsedForDataRow`.
   - One `private String` per spreadsheet column, using exactly the same property names as the old `CemiPaymentElectionGroupTwoBo` (`employeeId`, `paymentElectionGroupRule_2`, ... `distributionBalance_2_1`).
   - Getters and setters only. No constructor logic.
2. Create `resources/edu/cornell/kfs/cemi/pdp/cu-ojb-cemi-payment-election.xml`, modelled on `cu-ojb-cemi-award-schedule.xml`:
   - `class-descriptor` for the new BO, `table="CU_CEMI_EXTR_PYMNT_ELCTN_TAB_GRP_TWO_T"`, `schema="CEMI"`.
   - The first two fields are `jobRunRowIndex` / `JOB_RUN_ROW_INDEX` (BIGINT) and `jobRunDateString` / `EXTR_FILE_RUNDATE` (VARCHAR). Both get `primarykey="true" index="true"`.
   - `achAccountGeneratedIdentifierUsedForDataRow` / `ACH_ACCT_GNRTD_ID`, BIGINT, with `conversion="org.kuali.kfs.core.framework.persistence.ojb.conversion.OjbKualiIntegerFieldConversion"`.
   - The rest are VARCHAR, using the column names from Step 0.
   - **`accountNumber_2_1` MUST keep `conversion="org.kuali.kfs.core.framework.persistence.ojb.conversion.OjbKualiEncryptDecryptFieldConversion"`.**
3. Register the file in `cu-spring-cemi.xml` → `cemiModuleConfiguration-base-parentBean` → `databaseRepositoryFilePaths`. Add `edu/cornell/kfs/cemi/pdp/cu-ojb-cemi-payment-election.xml`. Leave `cu-ojb-cemi.xml` in place for now; the old code still uses it.
4. In `CemiPaymentElectionExtractFileOutputDefinition.xml`, add this attribute to the `Group_TWO` `<sheet>`:
   `business-object-class="edu.cornell.kfs.cemi.pdp.batch.businessobject.CemiPaymentElectionFileGroupTwoTabRowBo"`.
   The old CSV code ignores the attribute; the new appender in Step 5 needs it.

**Verify:** the app starts without OJB errors, and the output matches the baseline. Nothing writes to the new table yet.

---

### Step 4: Add the factory and data builder (not used yet)

1. Create `pdp/batch/service/impl/CemiPaymentElectionFileGroupTwoTabRowBoFactory.java`, modelled on `CemiAwardScheduleFileAwardScheduleTabRowBoFactory`:
   - Constructor arguments: `PayeeACHAccount`, `String achBankName`, `String jobRunDateString`, `boolean maskSensitiveData`.
   - `createCemiPaymentElectionFileGroupTwoTabRowBo()` validates its inputs, builds the row and sets every column.
   - **Move the conversion logic over from the old `CemiGroupTwo` constructor unchanged**: the constant values, `determineBankAccountNumber` (masking), `determineBankAccountType` (KFS→Workday map, defaulting to checking with a warning), the routing number, and the employee ID (`payeeIdNumber`).
   - Also set `achAccountGeneratedIdentifierUsedForDataRow` from the account.
2. Create `pdp/batch/service/CemiPaymentElectionFileExtractDataBuilder.java` (interface) with one method:
   `void writePaymentElectionFileGroupTwoTabExtractDataToIntermediateStorage(Iterator<PayeeACHAccount> payeeAchAccounts);`
3. Create `pdp/batch/service/impl/CemiPaymentElectionFileExtractDataBuilderDefaultImpl.java`, modelled on `CemiAwardScheduleFileExtractDataBuilderDefaultImpl`:
   - `extends CemiOrmDataBuilderBase`, and calls `super(businessObjectService, jobRunDateString, CemiPaymentElectionFileGroupTwoTabRowBo.class)`.
   - Constructor also takes `AchBankService`, both DAOs and `maskSensitiveData`, and checks them with `Validate.notNull`.
   - The loop logs progress every 1000 rows. For each account it looks up the ACH bank name (move `determineAchBankName` from the old `CemiPaymentElectionDataBuilderBase`), builds the row with the factory, and calls `storeSheetRow(row)`.
   - After `storeSheetRow`, it writes the mapping row: `cemiPaymentElectionDao.storeSpreadsheetRowItemKeyLegacyObjectKeyExtractRunDateMapping(row.getEmployeeId(), row.getAchAccountGeneratedIdentifierUsedForDataRow(), row.getJobRunDateString())`.

*Optional but recommended:* write a unit test for the factory that covers masked vs. unmasked account numbers, each bank account type, and a missing account type.

**Verify:** compile, run the unit test if you wrote one, and check the output matches the baseline. The new classes aren't wired up yet.

---

### Step 5: Switch over to the new pattern

This is the only step that changes how the extract runs. Keep the PR small and review it carefully.

1. `CemiPaymentElectionConstants`:
   - Replace `PAYMENT_ELECTION_OUTPUT_DEFINITION_FILE_PATH` and `PAYMENT_ELECTION_TEMPLATE_FILE_PATH` (full `classpath:` paths) with path suffixes that start **after** `edu/cornell/kfs/cemi/`:
     - `PAYMENT_ELECTION_OUTPUT_DEFINITION_PATH_SUFFIX = "pdp/batch/CemiPaymentElectionExtractFileOutputDefinition.xml"`
     - `PAYMENT_ELECTION_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX = "pdp/batch/Payment_Election.xlsx"`
   - Remove `CU_CEMI_EXTR_GRP_TWO_TAB_PYMNT_ELCTN_SEQ`.
   - Make the class `final`.
2. Rewrite `CemiPaymentElectionExtractServiceImpl` to match `CemiAwardScheduleExtractServiceImpl`:
   - `extends CemiDataExtractServiceBase implements CemiPaymentElectionExtractService`, with a constructor that calls `super(environment)`.
   - Fields: the two DAOs, `AchBankService` and `BusinessObjectService`, with setters only.
   - `generateIntermediateExtractData` opens the ORM stream in try-with-resources, creates `CemiPaymentElectionFileExtractDataBuilderDefaultImpl` with `shouldMaskCemiSensitiveData()`, and calls the write method.
   - `generateDataConversionExtractFile` calls `generateFileForDataExtract(jobRunDate, PLAIN_FILENAME, FILENAME_PREFIX)`.
   - Implement the three required overrides.
   - **Delete** all the old private code: the file creation, the copy, the output-definition parsing, the masking helpers, and the `environment`, directory, parameter and dateTime fields. The base class provides all of these.
3. `cu-spring-cemi.xml` → `cemiPaymentElectionExtractService-parentBean`:
   - `p:paymentElectionFileCreationDirectory` → `p:dataFileCreationDirectory`
   - `p:paymentElectionFileOutboundDirectory` → `p:dataFileOutboundDirectory`
   - Add `p:cemiFileAppenderService-ref="cemiFileAppenderService"`.
   - Remove `p:dateTimeService-ref`.
   - Keep `environment` (constructor), `cemiOutputDefinitionFileType`, `parameterService`, both DAOs, `achBankService` and `businessObjectService`.

**Verify:** this is the key check.
- The output matches the baseline row for row.
- `CEMI.CU_CEMI_EXTR_PYMNT_ELCTN_TAB_GRP_TWO_T` has rows for the run date, with `JOB_RUN_ROW_INDEX` running 1..N.
- `ACCT_NBR_2_1` is stored encrypted in the table and shows as `XXXXXXXXX` in the xlsx (masked).
- The mapping table got N new rows.
- Set `COPY_CEMI_FILE_TO_OUTBOUND_FOLDER` to `Y`, run again, and confirm `Payment_Election.xlsx` shows up in `${staging.directory}/cemi/conversions/outbound`.

---

### Step 6: Remove the old code

All of the following is now unused. Delete it:

- `pdp/batch/dto/CemiGroupTwo.java`
- `pdp/batch/businessobject/CemiPaymentElectionGroupTwoBo.java`
- `pdp/batch/businessobject/CemiPaymentElectionGroupTwoBoSequence.java`
- `pdp/batch/service/CemiPaymentElectionDataBuilder.java`
- `pdp/batch/service/CemiPaymentElectionFileAppender.java`
- `pdp/batch/service/impl/CemiPaymentElectionDataBuilderBase.java`
- `pdp/batch/service/impl/CemiPaymentElectionDataBuilderCsvImpl.java`
- `pdp/batch/service/impl/CemiPaymentElectionFileAppenderBase.java`
- `pdp/batch/service/impl/CemiPaymentElectionFileAppenderCsvImpl.java`
- `pdp/CemiPaymentElectionParameterConstants.java`
- `resources/edu/cornell/kfs/cemi/cu-ojb-cemi.xml`, plus its entry in `databaseRepositoryFilePaths`. It only held the old Payment Election mapping.

Before deleting anything, search the whole repository for each class name to confirm nothing else uses it.

In nonprod-sql, a separate follow-up handles the old database objects. Once Step 5 is deployed everywhere, drop the old table `CU_CEMI_EXTR_GRP_TWO_TAB_PYMNT_ELCTN_T`, the sequence `CU_CEMI_EXTR_GRP_TWO_TAB_PYMNT_ELCTN_SEQ`, and the parameter `COPY_CEMI_PAYMENT_ELECTION_FILE_TO_OUTBOUND_FOLDER`. Also remove their scrub and cleanup entries.

**Verify:** the output matches the baseline one last time.

---

## 6. Common mistakes

- **Putting conversion logic in the data builder.** The builder only loops, looks things up and saves. Every value transformation belongs in the factory.
- **Declaring `jobRunDateString` / `jobRunRowIndex` in the row BO,** or setting them yourself. `storeSheetRow()` sets them.
- **Forgetting the encryption conversion on `accountNumber_2_1`.** Bank account numbers would then be stored in plain text.
- **Using full `classpath:` paths for the two suffix constants.** The base class adds the `classpath:edu/cornell/kfs/cemi/` prefix itself.
- **A missing `COPY_CEMI_FILE_TO_OUTBOUND_FOLDER` parameter.** The job fails in Step 5.
- **A JDBC DAO bean without `p:dataSource-ref="cemiDataSource"`.** The `TRUNCATE` fails because the KFS user can't truncate CEMI tables.
- **Missing `business-object-class` on the `<sheet>` element.** The file appender can't find the table to read.
- **Editing `CemiDataExtractServiceBase`, `CemiOrmDataBuilderBase` or anything under `patterntemplate`.** If you think you need to, talk to the team first.

---

## 7. Using Claude Code for this work

- Give it **one step at a time**. Paste that step's section from this document and tell it which pattern files to model the code on, for example: *"Follow `CemiAwardScheduleFileExtractDataBuilderDefaultImpl`."*
- Tell it to keep everything in `edu.cornell.kfs.cemi.pdp` and not to modify any base class.
- Review every diff before accepting it. Use the Source Control view in VS Code and click each file to see a side-by-side diff. Check it against this document, not just against whether it compiles.
- Do the **Verify** check yourself after each step, before moving on.
