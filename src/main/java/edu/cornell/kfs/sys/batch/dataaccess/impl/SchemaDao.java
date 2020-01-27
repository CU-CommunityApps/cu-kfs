package edu.cornell.kfs.sys.batch.dataaccess.impl;

public abstract class SchemaDao {
    
    private String schemaName;
    private Object[] fullyQualifiedTableNameArguments;
    
    //This method should store the schema name and then populate the 
    //fullyQualifiedTableArguments array. 
    abstract void initialize(String schemaName);
    
    /**
     * A convenience method for creating a multi-item Object[] array and 
     * making the array available for logging (if an SQL exception occurs).
     */
    Object[] getMArgs(Object... args) {
        return args;
    }

    public String getSchemaName() {
        return schemaName;
    }

    protected void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public Object[] getFullyQualifiedTableNameArguments() {
        return fullyQualifiedTableNameArguments;
    }

    public void setFullyQualifiedTableNameArguments(Object[] fullyQualifiedTableNameArguments) {
        this.fullyQualifiedTableNameArguments = fullyQualifiedTableNameArguments;
    }

}
