package edu.cornell.kfs.cemi.patterntemplate;

public interface PatternTemplateREADME {
    
    // All of the items starting from this folder in the package structure tree pertain
    // to an EXAMPLE pattern to follow when creating a CEMI data extraction batch job.
    //
    // The generic folders and files in this area contain the framework to create a working data
    // extraction batch job when proper customization for a specific data extraction is added.
    //
    // DO NOT customize anything in this pattern template to be used by any runnable data 
    // extraction batch job. The ONLY changes that should be made to these files and folders
    // are those required to enhance or further refine the pattern base upon the furture patterns
    // that may be exposed during later data extraction development. 
    //
    // NONE of this tempate code in this part of the package structure should be executed as
    // a batch job as-is and in fact should fail if executed in its current and all future forms.
    //
    // Remove all patterntemplate exmaples and comments are you are coding including this instruction documentation.
    //
    //==============================================================================================================
    // Steps 1 & 2 to follow when using this patterntemplate to create the FIRST data extraction batch job
    // for a module area:
    //
    // (1) Copy the "patterntemplate" folder located at "java/edu/cornell/kfs/cemi" to the appropriate
    //     location under the "cemi" folder in the KFS package heirarchy where it should reside based 
    //     upon the information the job will be processing.
    //     Example: 
    //     If you are creating the FIRST data extraction associated with Assests, copy Java "patterntemplate" 
    //     folder hierarchy to "java/edu/cornell/kfs/cemi/module/" and rename the folder "patterntemplate" to "cam".
    //
    //
    // (2) Copy the "patterntemplate" folder located at "resources/edu/cornell/kfs/cemi" to the appropriate
    //     location under the "cemi" folder in the KFS package heirarchy where it should reside based 
    //     upon the information the job will be processing.
    //     Example: 
    //     If you are creating the FIRST data extraction associated with Assests, copy resources "patterntemplate" 
    //     folder hierarchy to "resources/edu/cornell/kfs/cemi/module/" and rename the folder "patterntemplate" to "cam". 
    //
    //==============================================================================================================
    // Steps 1 & 2 to follow when ADDITIONAL data extraction batch jobs AFTER the first one need to be created
    // with this pattentemplate so they co-exist with other extractions for the same module area.
    // NOTE: These steps are more tedious and you will need to ensure you are obtaining ALL the files.
    //
    // (1) Copy EVERY file from the "patterntemplate" folder located at "java/edu/cornell/kfs/cemi" to 
    //     the appropriate location under the "cemi" folder in the KFS package heirarchy where it should 
    //     reside based upon the information the job will be processing.
    //     Example: 
    //     If you are creating subsequent data extractions associated with Assests, copy EVERY file in the Java
    //     "patterntemplate" folder hierarchy to the corresponding folder in path "java/edu/cornell/kfs/cemi/module/cam".
    //
    //
    // (2) Copy EVERY file from the "patterntemplate" folder located at "resources/edu/cornell/kfs/cemi" to
    //     the appropriate location under the "cemi" folder in the KFS package heirarchy where it should
    //     reside based upon the information the job will be processing.
    //     Example: 
    //     If you are creating subsequent data extractions associated with Assests, copy EVERY file in the resources
    //     "patterntemplate" folder hierarchy to the corresponding folder in path "resources/edu/cornell/kfs/cemi/module/cam".
    //
    //==============================================================================================================
    // The remaining steps are common for both FIRST and ADDITIONAL data file extraction creation.
    //
    // (3) Obtain the CLEAN copy of the data conversion template you are creating the batch job for. 
    //     That file can be found on the Huron Sharepoint server in the appropriate "Templates to Populate" folder.
    //     
    //     Replace example file "resources/edu/cornell/kfs/cemi/APPROPRIATE-MODULE/PatternTemplate_Example.xlsx"
    //     with the Sharepoint server Excel spreadsheet.
    //
    //
    // (4) Rename copied file "CemiEXTRACTNAMEExtractFileOutputDefinition.xml" located in "resources/edu/cornell/kfs/cemi/..."
    //     based upon the name of the data extraction being created where EXTRACTNAME will uniquely represent the extract.
    //     Follow the coding instructions in that file.
    //
    //     NOTE: EXTRACTNAME will need to be used in numerous places so you need to keep it concise and unique.
    //     Examples:
    //          Put_Award_Schedule.xlsx ==> EXTRACTNAME = AwardSchedule ==> CemiAwardScheduleExtractFileOutputDefinition.xml
    //          Register_Asset.xlsx ==> EXTRACTNAME = RegisterAsset ==> CemiRegisterAssetExtractFileOutputDefinition.xml
    //          Update_Asset_Book_Configuration.xlsx ==>  EXTRACTNAME = AssetBookConfiguration ==> CemiAssetBookConfigExtractFileOutputDefinition.xml
    //
    
    
}
