    ==============================================================================================================
     All of the items starting at and within this folder in the package structure tree pertain
     to an EXAMPLE pattern to follow when creating a CEMI data extraction batch job.
    
     The generic folders and files in this area contain the framework to create a working data
     extraction batch job when proper customization for a specific data extraction is added.
    
     DO NOT customize anything in this pattern template to be used by any runnable data 
     extraction batch job. The ONLY changes that should be made to these files and folders
     are those required to enhance or further refine the pattern base upon the future patterns
     that may be exposed during later data extraction development. 
    
     NONE of this template code in this part of the package structure should be executed as
     a batch job as-is and in fact should fail if executed in its current and all future forms.
    
     Remove all patterntemplate exmaples and comments as you are coding, including this instruction documentation.
    
    ==============================================================================================================
     Steps 1 & 2 to follow when using this patterntemplate to create the FIRST data extraction batch job
     for a module area:
    
     (1) Copy the "patterntemplate" folder located at "java/edu/cornell/kfs/cemi" to the appropriate
         location under the "cemi" folder in the KFS package heirarchy where it should reside based 
         upon the information the job will be processing.
         Example: 
         If you are creating the FIRST data extraction associated with Assets, copy Java "patterntemplate" 
         folder hierarchy to "java/edu/cornell/kfs/cemi/module/" and rename the folder "patterntemplate" to "cam".
    
    
     (2) Copy the "patterntemplate" folder located at "resources/edu/cornell/kfs/cemi" to the appropriate
         location under the "cemi" folder in the KFS package heirarchy where it should reside based 
         upon the information the job will be processing.
         Example: 
         If you are creating the FIRST data extraction associated with Assets, copy resources "patterntemplate" 
         folder hierarchy to "resources/edu/cornell/kfs/cemi/module/" and rename the folder "patterntemplate" to "cam". 
    
    ==============================================================================================================
     Steps 1 & 2 to follow when ADDITIONAL data extraction batch jobs AFTER the first one need to be created
     with this pattentemplate so they co-exist with other extractions for the same module area.
     NOTE: These steps are more tedious and you will need to ensure you are obtaining ALL the files.
    
     (1) Copy EVERY file from the "patterntemplate" folder located at "java/edu/cornell/kfs/cemi" to 
         the appropriate location under the "cemi" folder in the KFS package heirarchy where it should 
         reside based upon the information the job will be processing.
         Example: 
         If you are creating subsequent data extractions associated with Assets, copy EVERY file in the Java
         "patterntemplate" folder hierarchy to the corresponding folder in path "java/edu/cornell/kfs/cemi/module/cam".
    
    
     (2) Copy EVERY file from the "patterntemplate" folder located at "resources/edu/cornell/kfs/cemi" to
         the appropriate location under the "cemi" folder in the KFS package heirarchy where it should
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
    
    
     (5) In a separate GitHub branch checked out from repository nonprod-sql, copy the three SQL example files at
         location "resources/edu/cornell/kfs/cemi/module/patterntemplate/examplesql" to the "kfs" folder for nonprod-sql.
         These three Oracle SQL files will be used to create the database artifacts required to support the processing
         for the EXTRACTNAME data extraction. The contents of the "create-tables" and "create-views" files will be very
         specific to extraction under development. The "create-parameters" file contains the two system parameters that
         must exist for each data extraction batch job to function correctly.
         
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
                    cemi-002-award-schedule-step-003-create-parameters.sq
                            ...
                    cemi-005-order-from-step-001-create-tables.sql
                    cemi-005-order-from-step-002-create-views.sql
                    cemi-005-order-from-step-003-create-parameters.sql
                    
            (c) Keep comments in the SQL files to a minimum. 
            
            (d) At a minimum the "create-tables" script file needs to hold the table definitions for:
                    (1) Every sheet tab defined in the spreadsheet.
                    (2) An association table that links the legacy object keys to the new Workday object id in the file.
                    (3) Any helper table needed to obtaining the information.

            (e) At a minimum the "create-views" script file needs to hold the definition that creates the view used to
                obtain the keys defining the scope of objects to used for the data extraction.
                
            (f) The "create-parameters" file contains definitions for the base parameters required by the batch job.
                Adjust those definitons to make them specific to the extraction being developed.
    ==============================================================================================================
