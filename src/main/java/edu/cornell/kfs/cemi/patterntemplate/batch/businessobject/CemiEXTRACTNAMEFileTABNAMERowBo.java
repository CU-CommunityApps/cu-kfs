package edu.cornell.kfs.cemi.patterntemplate.batch.businessobject;

import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;

/*
 * Business object representing information for a specific row on a specific tab in a specific data extraction file.
 * It also contains any references to objects used to obtain the information contained in that row.
 *
 * The abstract class it is extended from contains common data attributes used across all business object of this kind.
 *
 * To create these business object(s) correctly:
 *     Start with the clean Huron mapping template spreadsheet.
 *     Create a business object for each tab in the spreadsheet where:
 *         (1) The name of the class follows the convention of
 *                  Cemi{EXTRACTNAME}File{TABNAME}RowBo
 *                  
 *         (2) It should extend abstract class edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase
 *             (a) Where that class defines the following attributes that need to be assigned values during processing: 
 *                      String jobRunDate
 *                      Long jobRunRowIndex
 *                      
 *         (3) All field names on a particular tab should become "private String" attributes in the business object.
 *         
 *         (4) Any parent data value reference keys should be part of the attributes defined in the business object.
 *         
 *         (5) Getters and setters should be generated for every attribute created.
 *
 * Examples of business object names for Huron mapping templates with single versus multiple tabs:
 * 
 *      Award Schedule data extract file would have this business object:
 *          CemiAwardScheduleFileAwardScheduleRowBo
 *
 *      Supplier data extract file would have these business objects:
 *          CemiSupplierFileSupplierRowBo
 *          CemiSupplierFileAddressesRowBo
 *          CemiSupplierFileEmailsRowBo
 *          CemiSupplierFilePhonesRowBo
 *          CemiSupplierFileBankAccountsRowBo
 *          CemiSupplierFileChildrenRowBo
 *
 * Each business object created will require:
 *  (1) A corresponding OJB table definition in file resources/edu/cornell/kfs/cemi/patterntemplate/batch/cu-ojb-cemi-patterntemplate.xml
 *  (2) A corresponding nonprod-sql database table SQL creation script. Details are in that example file.
 *  (3) Will be referenced in the Cemi{EXTRACTNAME}ExtractFileOutputDefinition.xml
 *  (4) Will be populated by a corresponding business object factory class. Detail are in that example file.
 *  
 */

public class CemiEXTRACTNAMEFileTABNAMERowBo extends CemiIndexedBusinessObjectBase {
    
    // Attributes jobRunRowIndex and jobRunDate are defined in super class. Uses its access methods
    
    private String anyKeyAttributes;                 // These items are business object keys needed to
    private String neededToTrackDataRowValues;      // get the data representing the tab row values below.
    
    private String eachDataField;
    private String fromTheMappingTemplateTab;
    private String thatShouldBe;
    private String populatedByThis;
    private String businessObject;
    
    
    public CemiEXTRACTNAMEFileTABNAMERowBo (CemiAwardSchedule cemiAwardScheduleDataRow, String proposalNumberForScheduleRow,
            LocalDateTime jobRunDate, CemiAwardScheduleBoSequence awardScheduleTabTableSequence) {
        
        // These values are to make the row that would appear in an extract spreadsheet seachable as well as
        // identifiable by the actual KFS data object keys used to create that row and date-time of the extract file.
        super.setJobRunRowIndex(jobRunRowIndex);
        super.setJobRunDate(jobRunDate);
        this.anyKeyAttributes = anyKeyAttributes;
        this.neededToTrackDataRowValues = neededToTrackDataRowValues;
        
        // These table data values should be the same as what would be in the extract file tabbed sheet columns.
        this.eachDataField = cemiAwardScheduleDataRow.getSpreadsheetKey();
        this.fromTheMappingTemplateTab = cemiAwardScheduleDataRow.getAddOnly();
        this.thatShouldBe = cemiAwardScheduleDataRow .getAwardSchedule();
        this.populatedByThis = cemiAwardScheduleDataRow.getAwardScheduleReferenceId();
        this.businessObject = cemiAwardScheduleDataRow.getAwardScheduleName();
    }

    public String getAnyKeyAttributes() {
        return anyKeyAttributes;
    }

    public void setAnyKeyAttributes(String anyKeyAttributes) {
        this.anyKeyAttributes = anyKeyAttributes;
    }

    public String getNeededToTrackDataRowValues() {
        return neededToTrackDataRowValues;
    }

    public void setNeededToTrackDataRowValues(String neededToTrackDataRowValues) {
        this.neededToTrackDataRowValues = neededToTrackDataRowValues;
    }

    public String getEachDataField() {
        return eachDataField;
    }

    public void setEachDataField(String eachDataField) {
        this.eachDataField = eachDataField;
    }

    public String getFromTheMappingTemplateTab() {
        return fromTheMappingTemplateTab;
    }

    public void setFromTheMappingTemplateTab(String fromTheMappingTemplateTab) {
        this.fromTheMappingTemplateTab = fromTheMappingTemplateTab;
    }

    public String getThatShouldBe() {
        return thatShouldBe;
    }

    public void setThatShouldBe(String thatShouldBe) {
        this.thatShouldBe = thatShouldBe;
    }


    public String getPopulatedByThis() {
        return populatedByThis;
    }

    public void setPopulatedByThis(String populatedByThis) {
        this.populatedByThis = populatedByThis;
    }

    public String getBusinessObject() {
        return businessObject;
    }

    public void setBusinessObject(String businessObject) {
        this.businessObject = businessObject;
    }

}
