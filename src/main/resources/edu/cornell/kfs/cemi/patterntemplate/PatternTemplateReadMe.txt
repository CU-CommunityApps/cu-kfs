    ==============================================================================================================
     All of the items starting at and within the Java and resources package structure tree
     edu.cornell.kfs.cemi.patterntemplate pertain to an EXAMPLE pattern to follow when creating a CEMI data
     extraction batch job.
    
     The generic folders and files in this area contain the framework to create a working data
     extraction batch job when proper customization for a specific data extraction is added.
    
     DO NOT customize anything in this pattern template to be used by any runnable data 
     extraction batch job. The ONLY changes that should be made to these pattern template files
     and folders are those changes required to enhance or further refine the pattern base upon
     future examples that may be exposed during later data extraction development.
    
     NONE of this template code in this part of the package structure should be executed as
     a batch job as-is and in fact should fail if executed in its current and all future forms.
    
     Remove all patterntemplate examples and comments as you are coding, including this instruction documentation.
     
     When customizing this pattern template for a specific data extraction, keep all of your changes within
     this copied pattern structure. Do not change any code in any other package structures or abstract base classes
     without first consulting with the rest of the team.
    
    ==============================================================================================================
     Steps 1 & 2 to follow when using this patterntemplate to create the FIRST data extraction batch job
     for a module area:
    
     (1) Copy the "patterntemplate" folder located at "java/edu/cornell/kfs/cemi" to the appropriate
         location under the "cemi" folder in the KFS package hierarchy where it should reside based 
         upon the information the job will be processing.
         Example: 
         If you are creating the FIRST data extraction associated with Assets, copy Java "patterntemplate" 
         folder hierarchy to "java/edu/cornell/kfs/cemi/module/" and rename the folder "patterntemplate" to "cam".
    
    
     (2) Copy the "patterntemplate" folder located at "resources/edu/cornell/kfs/cemi" to the appropriate
         location under the "cemi" folder in the KFS package hierarchy where it should reside based 
         upon the information the job will be processing.
         Example: 
         If you are creating the FIRST data extraction associated with Assets, copy resources "patterntemplate" 
         folder hierarchy to "resources/edu/cornell/kfs/cemi/module/" and rename the folder "patterntemplate" to "cam". 
    
    ==============================================================================================================
     Steps 1 & 2 to follow when ADDITIONAL data extraction batch jobs AFTER the first one need to be created
     with this pattentemplate so they co-exist with other extractions for the same module area.
     NOTE: These steps are more tedious and you will need to ensure you are obtaining ALL the files.
    
     (1) Copy EVERY file from the "patterntemplate" folder located at "java/edu/cornell/kfs/cemi" to 
         the appropriate location under the "cemi" folder in the KFS package hierarchy where it should 
         reside based upon the information the job will be processing.
         Example: 
         If you are creating subsequent data extractions associated with Assets, copy EVERY file in the Java
         "patterntemplate" folder hierarchy to the corresponding folder in path "java/edu/cornell/kfs/cemi/module/cam".
    
    
     (2) Copy EVERY file from the "patterntemplate" folder located at "resources/edu/cornell/kfs/cemi" to
         the appropriate location under the "cemi" folder in the KFS package hierarchy where it should
         reside based upon the information the job will be processing.
         Example: 
         If you are creating subsequent data extractions associated with Assets, copy EVERY file in the resources
         "patterntemplate" folder hierarchy to the corresponding folder in path "resources/edu/cornell/kfs/cemi/module/cam".

    ==============================================================================================================
     The remaining steps are common for both FIRST and ADDITIONAL data file extraction creation.
    
     (3) Obtain the CLEAN copy of the data conversion template you are creating the batch job for. 
         That file can be found on the Huron Sharepoint server in the appropriate "Templates to Populate" folder.
         
         Put that actual file obtained from the Sharepoint server in the appropriate spot of the source code file tree
         using the placement of example file "resources/edu/cornell/kfs/cemi/patterntemplate/batch/PatternTemplate_Example.xlsx"
         as a reference.
    
    
     (4) Rename copied file "Cemi{EXTRACTNAME}ExtractFileOutputDefinition.xml" located in "resources/edu/cornell/kfs/cemi/..."
         based upon the name of the data extraction being created where EXTRACTNAME will uniquely represent the extract.
         Follow the coding instructions in that file.
    
         NOTE: EXTRACTNAME will need to be used in numerous places so you need to keep it concise and unique.
         Examples:
              Put_Award_Schedule.xlsx ==> EXTRACTNAME = AwardSchedule ==> CemiAwardScheduleExtractFileOutputDefinition.xml
              Register_Asset.xlsx ==> EXTRACTNAME = RegisterAsset ==> CemiRegisterAssetExtractFileOutputDefinition.xml
              Update_Asset_Book_Configuration.xlsx ==>  EXTRACTNAME = AssetBookConfiguration ==> CemiAssetBookConfigExtractFileOutputDefinition.xml
    
     (5) Rename copied file "cu-ojb-cemi-patterntemplate.xml" to pertain to the extract being developed.
         Follow the instuctions in that file.
         !!!!!VERY IMPORTANT!!!!!
            If there are any encrypted fields, add update statements which will scrub those fields to nonprod-sql 
            repository file :    manual / kfs/ KFSPTS-38305-cemiManualScrub.sql
    
     (6) In a separate GitHub branch checked out from repository nonprod-sql, copy the three SQL example files at
         location "resources/edu/cornell/kfs/cemi/module/patterntemplate/examplesql" to the "kfs" folder for nonprod-sql.
         
         These three Oracle SQL files will be used to create the database artifacts required to support the processing
         for the EXTRACTNAME data extraction. The contents of the "create-tables" and "create-views" files will be very
         specific to extraction under development. The "create-parameters" file contains the two system parameters that
         must exist for each data extraction batch job to function correctly.
         
         References to any database artifacts (tables, views, sequences, ...) in these SQL scripts must include the
         schema those artifacts are located in.
         
         Rename all three files in the following manner:
            (a) Replace XXX in the name with the next three digit numeric value specific to the extract being created.
            
            (b) Replace the EXTRACT-NAME with the appropriate value.
                Note: The XXX value will be used in the name of every SQL script file for a given data extraction to
                      group the EXTRACTNAME SQL scripts together. The step-YYY value in the SQL script file name
                      enforces the appropriate execution order.
                Example:
                    cemi-001-suppliers-step-001-create-tables.sql
                    cemi-001-suppliers-step-002-create-views.sql
                    cemi-001-suppliers-step-003-create-parameters.sql
                    cemi-002-award-schedule-step-001-create-tables.sql
                    cemi-002-award-schedule-step-002-create-views.sql
                    cemi-002-award-schedule-step-003-create-parameters.sql
                            ...
                    cemi-005-order-from-step-001-create-tables.sql
                    cemi-005-order-from-step-002-create-views.sql
                    cemi-005-order-from-step-003-create-parameters.sql
                    
            (c) Keep comments in the SQL files to an absolute minimum. Preferably, there should not be any comments in
                the SQL files with all existing comments there from these template instructions being fully removed. 
            
            (d) At a minimum the "create-tables" script file needs to hold the table definitions for:
                    (1) Every sheet tab defined in the spreadsheet where the table names follow the pattern:
                            CU_CEMI_EXTR_{EXTRACT_NAME}_TAB_{TAB_NAME}_T
                            
                    (2) Any data scope tables or helper tables needed for obtaining the information would follow the pattern:
                            CU_CEMI_{EXTRACT_NAME}_EXTR_{DATA_OBJECT}_T
                            
                    (3) If associations for legacyDataIdentifiers-to-WorkdayDataIdentifiers-to-extractionRunDate
                        cannot be tracked as information in table CU_CEMI_EXTR_{EXTRACT_NAME}_TAB_{TAB_NAME}_T then
                        another table and associated JDBC service methods need to be created to load it. The interaction
                        with this table should not be included in the business object factory utilized to create the  
                        data extraction file. JDBC service methods which invoke a CU customization need to be created to 
                        provide this functionality. An actual EXAMPLE of how to implement has been provided in this
                        pattern template.
                        
                            (a) The table names for that data would follow the pattern:
                                    CU_CEMI_MAPPING_{EXTRACT_NAME}_EXTR_FILE_T
                                    
                            (b) This SQL file would need to contain the script to create that table:
                                    cemi-XXX-EXTRACT-NAME-step-001-create-tables.sql
                                    
                            (c) This data access service method, interface definition, and JDBC implementation would
                                need to be created in these three files. An actual example of how this would be used by
                                the Award Schedule data extract is present in this pattern template even though that
                                data extract does not utilize this functionality.
                                    Method Name         ==> storeSpreadsheetRowItemKeyLegacyObjectKeyExtractRunDateMapping
                                    Interface File      ==> CemiEXTRACTNAMEExtractDao.java
                                    Implementation Name ==> CemiEXTRACTNAMEExtractDaoJdbcImpl.java
                                    
                            (d) The call to that data access service method would be placed in this service method. 
                                An actual example of that service call is present in this pattern template.
                                    Method Name                 ==> createAndStoreAwardScheduleFileAwardScheduleTabRows
                                    Service Implementation Name ==> CemiEXTRACTNAMEFileExtractDataBuilderDefaultImpl

            (e) At a minimum the "create-views" script file needs to hold the definition that creates the view used to
                obtain the keys defining the scope of objects to used for the data extraction.
                
            (f) The "create-parameters" file contains definitions for the base parameters required by all batch jobs.
                Adjust those definitions to make the batch job step names specific to the extraction being developed.
                The parameter names are defined in constants class edu.cornell.kfs.cemi.sys.CemiBaseParameterConstants
                
                
     (7) Rename copied class Cemi{EXTRACTNAME}File{TABENAME}TabRowBo to the appropriate {EXTRACTNAME} and {TABNAME}
         for the data conversion being created. Follow the instructions in that file to properly create the business 
         object class(es) that will represent each tab/sheet in the spreadsheet using the corresponding 
         Cemi{EXTRACTNAME}File{TABNAME}TabRowBoFactory.
         Note: 
         The concrete business object we are creating extends abstract class CemiIndexBusinessObjectBase. Do NOT 
         modify any common code in that abstract class as you will affect the processing of other data extractions.
     
     
     (8) Rename copied interface Cemi{EXTRACTNAME}ExtractService and its implementation class 
         Cemi{EXTRACTNAME}ExtractServiceImpl appropriately. The set of services in the interface defines minimum
         processing necessary for a SPECIFIC data conversion extraction file. Those services would normally be called
         from the batch job's step class.
         
         Data access objects (DAOs) will most likely be utilized via service calls to Cemi{EXTRACTNAME}ExtractDao 
         routines in the Cemi{EXTRACTNAME}ExtractService "captureInScopeBusinessObjectKeysToProcessingTable" and 
         "resetState" methods while service calls to Cemi{EXTRACTNAME}ExtractOrmDao will most likely be used in methods 
         "generateIntermediateExtractData" and "generateDataConversionExtractFile". 
         
         
         The pattern template file Cemi{EXTRACTNAME}ExtractServiceImpl also extends class CemiDataExtractServiceBase.
         That abstract class contains the default common processing that should be used by all data conversion batch 
         jobs as well as a number of items requiring configuration or specification by the unique data conversion batch 
         job coding. This assortment of Spring beans and their setup is described below.
         
             (a) Spring setup of concrete Cemi{EXTRACTNAME}ExtractService class deals with configuring the specific
                 data values required by abstract class CemiDataExtractServiceBase in a manner that makes those values
                 unique to the data extraction being coded. 
                 These items require that kind of Spring configuration:
                      (i) dataFileCreationDirectory
                     (ii) dataFileOutboundDirectory
                     
             (b) The Spring definition of the concrete class needs to include beans defining the service attributes
                 from the abstract class.
                 These items require that kind of Spring configuration:
                       (i) environment
                      (ii) cemiFileAppenderService
                     (iii) cemiOutputDefinitionFileType
                      (iv) parameterService
                    
             (c) The services specific to the actual data extraction being coded in the concrete class will also require
                 Spring bean definitions.
                     
             This is an actual Spring setup example for a concrete class associated with a working data extraction
             containing all three of those bean setup types:
             
                 Example:
                     <bean id="cemiAwardScheduleExtractService" parent="cemiAwardScheduleExtractService-parentBean"/>
                     <bean id="cemiAwardScheduleExtractService-parentBean"
                           abstract="true"
                           class="edu.cornell.kfs.cemi.module.cg.batch.service.impl.CemiAwardScheduleExtractServiceImpl"
                           c:environment-ref="environment"
                           p:dataFileCreationDirectory="${staging.directory}/cemi/module/cg/cemiAwardScheduleExtract"
                           p:dataFileOutboundDirectory="${staging.directory}/cemi/conversions/outbound"
                           p:cemiFileAppenderService-ref="cemiFileAppenderService"
                           p:cemiOutputDefinitionFileType-ref="cemiOutputDefinitionFileType"
                           p:parameterService-ref="parameterService"
                           p:cemiAwardScheduleOrmDao-ref="cemiAwardScheduleOrmDao"
                           p:cemiAwardScheduleDao-ref="cemiAwardScheduleDao"
                           p:businessObjectService-ref="businessObjectService"
                           p:dateTimeService-ref="dateTimeService"/>
     
  ==============================================================================================================
  ==============================================================================================================
