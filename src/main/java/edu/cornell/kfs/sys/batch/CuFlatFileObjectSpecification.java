package edu.cornell.kfs.sys.batch;

import java.util.List;



import org.apache.commons.beanutils.PropertyUtils;
import org.kuali.kfs.sys.batch.FlatFileObjectSpecification;
import org.kuali.kfs.sys.batch.FlatFilePropertySpecification;

 

/**

 * The specification for a business object which should be parsed into during the parsing of a flat file

 */

public class CuFlatFileObjectSpecification implements FlatFileObjectSpecification {

 

        protected String linePrefix;

   protected Class<?> businessObjectClass;

   protected List <FlatFilePropertySpecification> parseProperties;

   protected Class<?> parentBusinessObjectClass;

   protected String parentTargetProperty;

   

   /**

     * @return the prefix of the line which determines if the given line should be associated with this object specification

     */

   public String getLinePrefix() {

                return linePrefix;

        }

 

   /**

     * Sets the prefix that configures which lines this object specification will be associated with

     * @param linePrefix the prefix

     */

        public void setLinePrefix(String linePrefix) {

                this.linePrefix = linePrefix;

        }

 

        /**

         * @return the class of the object lines associated with this prefix should be parsed into

         */

        public Class<?> getBusinessObjectClass() {

       return businessObjectClass;

   }

    

        /**

         * Sets the class of the object lines which will be parsed by this object specification should be parsed into

         * @param businessObjectClass the class of the object to parse into

         */

   public void setBusinessObjectClass(Class<?> businessObjectClass) {

       this.businessObjectClass = businessObjectClass;

   }

   

   /**

     * @return the List of FlatFilePropertySpecification which determine which properties on the parsed into business object each substring of the line will be populated into

     */

   public List<FlatFilePropertySpecification> getParseProperties() {

       return parseProperties;

   }

   

   /**

     * Sets the List of FlatFilePropertySpecification objects

     * @param parseProperties the List of FlatFilePropertySpecification used to instruct the parsing of lines associated with this object specification

     */

   public void setParseProperties(List<FlatFilePropertySpecification> parseProperties) {

       this.parseProperties = parseProperties;

   }

 

   /**

     * @return the class of the object that will hold objects parsed by this object specification

     */

        public Class<?> getParentBusinessObjectClass() {

                return parentBusinessObjectClass;

        }

 

        /**

         * Sets the class of the business object which holds instances of objects of the businessObjectClass; if null, then this class represents a root

         * @param parentBusinessObjectClass the class of the parent business object

         */

        public void setParentBusinessObjectClass(Class<?> parentBusinessObjectClass) {

                this.parentBusinessObjectClass = parentBusinessObjectClass;

        }

 

        /**

         * @return the property of the parent business object which should populated by objects parsed by this object specification 

         */

        public String getParentTargetProperty() {

                return parentTargetProperty;

        }

 

        /**

         * Sets the name on the parent where objects of this class should be placed

         * @param parentTargetProperty the property of the parent class to add objects of this class

         */

        public void setParentTargetProperty(String parentProperty) {

                this.parentTargetProperty = parentProperty;

        }

}