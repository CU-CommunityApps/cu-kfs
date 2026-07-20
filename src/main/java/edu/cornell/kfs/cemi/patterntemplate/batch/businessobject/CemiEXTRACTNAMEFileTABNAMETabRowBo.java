package edu.cornell.kfs.cemi.patterntemplate.batch.businessobject;

import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;

/*
 * Business object representing information for a specific row on a specific tab in a specific data extraction spreadsheet file.
 * It also contains any references to legacgy objects used to obtain the information contained in that row.
 *
 * Abstract class CemiIndexedBusinessObjectBase this concrete class is extended from contains common data attributes 
 * used across all business objects of this kind.
 *
 * To create these concrete business object(s) correctly:
 *     Start with the clean Huron mapping template spreadsheet.
 *     Create a business object for each tab in the spreadsheet where:
 *         (1) The name of the class follows the convention of
 *                  Cemi{EXTRACTNAME}File{TABNAME}TabRowBo
 *                  
 *         (2) It should extend abstract class edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase
 *             (a) Where the abstract class defines the following attributes which need to be assigned values during processing: 
 *                      String jobRunDateString
 *                      Long jobRunRowIndex
 *                      
 *         (3) Any legeacy data object reference keys should be part of the attributes defined in the concrete business object.
 *                      
 *         (4) All field names on a particular tab should become "private String" attributes in the concrete business object.
 *         
 *         (5) Getters and setters should be generated for every attribute created in the concreate business object.
 *
 * Example of business object names for Huron mapping templates with a single spreadsheet tab  
 *      Award Schedule data extract file would have this business object:
 *          CemiAwardScheduleFileAwardScheduleTabRowBo
 * 
 * Examples of business object names for Huron mapping templates with multiple tabs (items are not yet in the pattern):
 *      Supplier data extract file would have these business objects:
 *          CemiSupplierFileSupplierRowBo
 *          CemiSupplierFileAddressesRowBo
 *          CemiSupplierFileEmailsRowBo
 *          CemiSupplierFilePhonesRowBo
 *          CemiSupplierFileBankAccountsRowBo
 *          CemiSupplierFileChildrenRowBo
 *
 * Each concreate business object created will:
 *  (1) Require a corresponding OJB table definition in file resources/edu/cornell/kfs/cemi/patterntemplate/batch/cu-ojb-cemi-patterntemplate.xml
 *  (2) Require a corresponding nonprod-sql database table SQL creation script. Details are in that example file.
 *  (3) Be referenced in a Cemi{EXTRACTNAME}ExtractFileOutputDefinition.xml
 *  (4) Be populated by a corresponding business object factory class Cemi{EXTRACTNAME}File{TABNAME}TabRowBoFactory. Detail are in that example file.
 */

public class CemiEXTRACTNAMEFileTABNAMETabRowBo extends CemiIndexedBusinessObjectBase {
    
    // These attributes are defined in the abstract class. Uses its accessor methods
    // private String jobRunDateString;
    // private Long jobRunRowIndex;
    
    // These items would be legacy system business object keys needed to obtain the data representing 
    // the tab row's values held in the remaining attributes in the section below.
    private String anyKeyAttributes;
    private String neededToTrackDataRowValues;
    
    // The attributes listed in this section represent each data field from the
    // mapping template tab that should be populated by this business object's factory.
    private String eachDataField;
    private String fromTheMappingTemplateTab;
    private String thatShouldBe;
    private String populatedByThis;
    private String businessObject;
    
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
