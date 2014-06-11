package org.brailleblaster.settings;

public class Table{
	String tableName, tableFile;
	Table(String tableName, String tableFile){
		this.tableName = tableName;
		this.tableFile = tableFile;
	}
	
	public String getTableName(){
		return tableName;
	}
	
	public String getTableFile(){
		return tableFile;
	}
}
