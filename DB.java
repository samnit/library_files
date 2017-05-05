package com.automatics.packages.library.common;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.ResultSetDynaClass;

import com.automatics.packages.library.common.Reporter;
import com.automatics.packages.library.common.Utils;


@SuppressWarnings("rawtypes")
public class DB {
	
	private static HashMap<String, Connection> hDBConnection_Params = new HashMap<String, Connection>();
	private static HashMap<String, Statement> hTestName_DBStatement = new HashMap<String, Statement>();
	private static HashMap<String, ResultSet> hTestName_DBResultSet = new HashMap<>();
	
	public static void dbConnect(String sTestName, String Connection) throws SQLException, HeadlessException, IOException, AWTException, InterruptedException, InvalidInputException  {
		
		String sDesc = "", sHost, sSvrName , userId , sPassword , sVarName = null ; Connection dbConn = null;
		
		
		try {
			if (Connection.contains("{")&&Connection.contains("}"))
				sVarName = Connection.split("\\{")[1].replace("}", "");
			else {
				sVarName = sTestName.split("\\|")[1];
				sTestName = sTestName.split("\\|")[0];
			
			}
			Connection = Utils.Helper.validateUserInput(sTestName, Connection);
			sHost = Connection.split("\\|")[0];	sSvrName = Connection.split("\\|")[1];
			userId = Connection.split("\\|")[2]; sPassword = Connection.split("\\|")[3];
			sDesc = Logs.log(sTestName) + " (" + sHost + ", " + sSvrName + ", " + userId + ", " + sPassword + ")";
			
			Class.forName("oracle.jdbc.OracleDriver");
			dbConn = DriverManager.getConnection("jdbc:oracle:thin:" + userId + "/" + sPassword + "@//" + sHost + ":1521/" + sSvrName);
			
					       	
			hDBConnection_Params.put(sVarName , dbConn);
			if (sTestName.equalsIgnoreCase("PreRequisite")) 
				Logs.log("PreRequisite", sDesc + " :: Connected To Db ");
			else
				Reporter.print(sTestName, sDesc + " :: Connected To Db ");
			
		} catch (ClassNotFoundException e) {
			
			Reporter.printError(sTestName, e, sDesc);
	    
		} catch (SQLException e) {
			
			Reporter.printError(sTestName, e, sDesc);
	    }
	}

	public static void dbSetConnection(String sTestName, String connectionToDb) throws SQLException, HeadlessException, IOException, AWTException, InterruptedException{
		
		String sDesc;
		sDesc = Logs.log(sTestName);
		
		try {
			
			Connection dbConn = hDBConnection_Params.get(connectionToDb.split("\\{")[1].replace("}", ""));
		    Statement dbStmt = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		    hTestName_DBStatement.put(sTestName, dbStmt);
		    Reporter.print(sTestName, sDesc +" :: Connection Set");
		
		} catch(Exception e){
			
			Reporter.printError(sTestName, e, sDesc);
		}
		
	}
	
	

	public static void dbExecute_Query(String sTestName, String userQuery) throws HeadlessException, IOException, AWTException, SQLException, InterruptedException  {
		
		String sDesc ; ResultSet dbResultSet=null; int numberOfRecords = 0;
		sDesc = Logs.log(sTestName) + "Execute DB Query";
		
		
		try {
			userQuery = Utils.Helper.validateUserInput(sTestName, userQuery);
			Logs.log(sTestName, "Query-> " + userQuery );
			Statement dbStmt = hTestName_DBStatement.get(sTestName);       
			dbResultSet = dbStmt.executeQuery(userQuery);
			
			boolean checkLast = dbResultSet.last();
			
			if (checkLast)
			    numberOfRecords = dbResultSet.getRow();
			
			if (numberOfRecords>500)
				Reporter.print(sTestName, userQuery+"*EXIT_ON_FAIL*", sDesc + "Error: Not Supported Result Set With More Than 500 Rows", "True", "False", false);
			
			dbResultSet.beforeFirst();
			Reporter.print(sTestName, sDesc + " :: Executed");
			hTestName_DBResultSet.put(sTestName, dbResultSet);
			dbPrintResultSet(sTestName);
        
		} catch(Exception e){
			
			Reporter.printError(sTestName, e, sDesc);
		}
		
	}
	

	public static void dbPrintResultSet(String sTestName) throws SQLException, HeadlessException, IOException, AWTException
	{
		try {
			 ResultSet dbResultSet = hTestName_DBResultSet.get(sTestName);
			 ResultSetMetaData rsmd = dbResultSet.getMetaData();
			 int columnsNumber = rsmd.getColumnCount();
			 while (dbResultSet.next()) {
				 for (int i = 1; i <= columnsNumber; i++) {
					 if (i > 1) System.out.print(",  ");
					 String columnValue = dbResultSet.getString(i);
					 System.out.print(columnValue + " " + rsmd.getColumnName(i));
				 }
				 System.out.println("");
			 }
			 dbResultSet.beforeFirst();
			 
		} catch(Exception e) {
			
			Reporter.printError(sTestName, e, " Error Printing Result Set");
		}
	}
	
	public static void dbColumn_StoreFirstCell(String sTestName, String sColumnName,String sVarName) throws SQLException, HeadlessException, IOException, AWTException, InterruptedException {	
		
		String sDesc = null , sActVal = null,sVal=sColumnName; ResultSet dbResultSet ;
		
		
		try {
			sColumnName = Utils.Helper.validateUserInput(sTestName, sColumnName);
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			sDesc = Logs.log(sTestName) + " of Column: " + sVal;
			if (sVarName.equals("")){
				sVarName = "Temp";
			}
						
			dbResultSet = hTestName_DBResultSet.get(sTestName);
			ResultSetDynaClass rsdc = new ResultSetDynaClass(dbResultSet);
			Iterator itr = rsdc.iterator();
			
			while (itr.hasNext())  {
				DynaBean bean = (DynaBean)itr.next();
				sActVal =  bean.get(sColumnName.toLowerCase()).toString();
				
				
				Utils.setScriptParams(sTestName, sVarName, sActVal);
				break;
			}
			dbResultSet.beforeFirst();
			Reporter.print(sTestName, sDesc +" :: Value Stored : "+ sActVal);
				
		} catch(Exception e) {
				
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	
	public static void dbColumn_StoreLastCell(String sTestName, String sColumnName,String sVarName) throws SQLException, HeadlessException, IOException, AWTException, InterruptedException {	
			
		String  sDesc = null , sActVal = null,sVal=sColumnName; ResultSet dbResultSet ;
		
		try {
			sColumnName = Utils.Helper.validateUserInput(sTestName, sColumnName);
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			sDesc = Logs.log(sTestName) + " of Column: " + sVal;
			if (sVarName.equals("")){
				sVarName = "Temp";
			}
						
			dbResultSet = hTestName_DBResultSet.get(sTestName);
			dbResultSet.last();
			
			ResultSetDynaClass rsdc = new ResultSetDynaClass(dbResultSet);
			Iterator itr = rsdc.iterator();
			DynaBean bean  = (DynaBean)itr;
			sActVal =  bean.get(sColumnName.toLowerCase()).toString();
							
			dbResultSet.beforeFirst();
			
			Utils.setScriptParams(sTestName, sVarName, sActVal);
			
			Reporter.print(sTestName, sDesc +" :: Value Stored : "+ sActVal);
				
		} catch(Exception e) {
				
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	
	public static void dbColumn_StoreAllCell(String sTestName, String sColumnName,String sVarName) throws SQLException, HeadlessException, IOException, AWTException, InterruptedException {	
		
		String  sDesc = null ,sVal=sColumnName, sActVal = ""; ResultSet dbResultSet ;
		
		try {
			sColumnName = Utils.Helper.validateUserInput(sTestName, sColumnName);
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			sDesc = Logs.log(sTestName) + " of Column: " + sVal;
			if (sVarName.equals("")){
				sVarName = "Temp";
			}
						
			dbResultSet = hTestName_DBResultSet.get(sTestName);
			ResultSetDynaClass rsdc = new ResultSetDynaClass(dbResultSet);
			Iterator itr = rsdc.iterator();
			DynaBean bean = null;
			
			while (itr.hasNext())  {
				bean = (DynaBean)itr.next();
				sActVal += bean.get(sColumnName.toLowerCase()).toString() + "++";
			}
			
			dbResultSet.beforeFirst();
			
			Utils.setScriptParams(sTestName, sVarName, sActVal.substring(0,sActVal.length() - 2));
			Reporter.print(sTestName, sDesc +" :: Values Stored : "+sActVal);
		
		} catch(Exception e) {
				
			Reporter.printError(sTestName, e, sDesc);
		}
	}

	
	public static void dbColumn_VerifyFirstCell(String sTestName, String sColumnName,String sExpVal) throws SQLException, HeadlessException, IOException, AWTException, InterruptedException {	
		   
		
		String sDesc ="" , sActVal = "" , sVal=sColumnName ; boolean bStatus = false; ResultSet dbResultSet ;
		
		try {
			sColumnName = Utils.Helper.validateUserInput(sTestName, sColumnName);
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			sColumnName = Reporter.filterUserInput(sColumnName);	
			
			sDesc = Logs.log(sTestName) + " :: Check whether DB column (" +sColumnName + ") has value (" + sExpVal + ") in the First row";
			
			dbResultSet = hTestName_DBResultSet.get(sTestName);
			ResultSetDynaClass rsdc = new ResultSetDynaClass(dbResultSet);
			Iterator itr = rsdc.iterator();
			while(itr.hasNext()) {
				DynaBean bean = (DynaBean)itr.next();
				if (bean.get(sColumnName.toLowerCase())!= null) {
					sActVal = bean.get(sColumnName.toLowerCase()).toString();
					if(sActVal.equalsIgnoreCase(sExpVal))		 
						bStatus =true;
					break;
				}
			 }
			
			Reporter.print(sTestName, sVal, sDesc, sExpVal, sActVal, bStatus);
			dbResultSet.beforeFirst();
				
		} catch(Exception e) {
		
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	
	
	public static void dbColumn_VerifyLastCell(String sTestName, String sColumnName,String sExpVal) throws SQLException, HeadlessException, IOException, AWTException, InterruptedException {	
			   
			
		String sDesc = "", sActVal = null ,  sVal=sColumnName ; boolean bStatus = false; ResultSet dbResultSet ;
						
		try {
			sColumnName = Utils.Helper.validateUserInput(sTestName, sColumnName);
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			sColumnName = Reporter.filterUserInput(sColumnName);	
			
			sDesc = Logs.log(sTestName) + " :: Check whether DB column (" +sColumnName + ") has value (" + sExpVal + ") in the Last row";
			
			dbResultSet = hTestName_DBResultSet.get(sTestName);
			dbResultSet.last();
			
			ResultSetDynaClass rsdc = new ResultSetDynaClass(dbResultSet);
			Iterator itr = rsdc.iterator();
			DynaBean bean = (DynaBean)itr;
			
			if (bean.get(sColumnName.toLowerCase())!= null) {
				sActVal = bean.get(sColumnName.toLowerCase()).toString();
				if(sActVal.equalsIgnoreCase(sExpVal))		 
					bStatus =true;
			}
			Reporter.print(sTestName, sVal, sDesc, sExpVal, sActVal, bStatus);
			dbResultSet.beforeFirst();
				
		} catch(Exception e) {
		
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	
	public static void dbColumn_VerifyInAllCells(String sTestName, String sColumnName,String sExpVal) throws SQLException, HeadlessException, IOException, AWTException {	
		   
		
		String sDesc = "", sActVal = "" , sVal=sColumnName ; boolean bStatus = false; ResultSet dbResultSet ;
		
		try {
			sColumnName = Utils.Helper.validateUserInput(sTestName, sColumnName);
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			sColumnName = Reporter.filterUserInput(sColumnName);	
			
			sDesc = Logs.log(sTestName) + " :: Check whether DB column (" +sColumnName + ") has value (" + sExpVal + ") in any of the rows";
			
			dbResultSet = hTestName_DBResultSet.get(sTestName);
						
			ResultSetDynaClass rsdc = new ResultSetDynaClass(dbResultSet);
			Iterator itr = rsdc.iterator();
			while(itr.hasNext()) {
				DynaBean bean = (DynaBean)itr.next();
				if (bean.get(sColumnName.toLowerCase())!= null) {
					sActVal = bean.get(sColumnName.toLowerCase()).toString();
					if(sActVal.equalsIgnoreCase(sExpVal)) {		 
						bStatus =true;
						break;
					}
				}
				
			 }
							
			Reporter.print(sTestName, sVal, sDesc, sExpVal, sActVal, bStatus);
			dbResultSet.beforeFirst();
		
		} catch(Exception e) {
		
			Reporter.printError(sTestName, e, sDesc);
		}
}
	
	
public static void dbColumn_VerifyByRow(String sTestName,String sRowColN, String sExpVal) throws SQLException, HeadlessException, IOException, AWTException {	
		   
		
		String sDesc = "", sActVal = "" , sColumnName , sVal=sExpVal; boolean bStatus = false; ResultSet dbResultSet ;
		int sRowNumber,i=1;
		try {			
			sRowColN = Utils.Helper.validateUserInput(sTestName, sRowColN);
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			sRowColN = Reporter.filterUserInput(sRowColN);
			sRowNumber=Integer.parseInt(sRowColN.split("\\||")[0]);
			sColumnName = sRowColN.split("\\||")[1];
		
			sDesc = Logs.log(sTestName) + " :: Check whether DB column (" +sColumnName + ") has value (" + sExpVal + ") in any Row number: "+ sRowNumber;
			
			dbResultSet = hTestName_DBResultSet.get(sTestName);
						
			ResultSetDynaClass rsdc = new ResultSetDynaClass(dbResultSet);
			Iterator itr = rsdc.iterator();
			while(itr.hasNext()) {
				DynaBean bean = (DynaBean)itr.next();
				if ((bean.get(sColumnName.toLowerCase())!= null)&&(i==sRowNumber)) {
					sActVal = bean.get(sColumnName.toLowerCase()).toString();
					if(sActVal.equalsIgnoreCase(sExpVal)) {		 
						bStatus =true;
						break;
					}
				}
				i++;
			 }
							
			Reporter.print(sTestName, sVal, sDesc, sExpVal, sActVal, bStatus);
			dbResultSet.beforeFirst();
		
		} catch(Exception e) {
		
			Reporter.printError(sTestName, e, sDesc);
		}
}
	
public static void dbColumn_StoreByRow(String sTestName, String sRowColN,String sVarName) throws SQLException, HeadlessException, IOException, AWTException {	
	   
	
	String sDesc = "", sActVal = "" , sColumnName ;ResultSet dbResultSet ;
	int sRowNumber,i=1;
	try {
		sRowColN = Utils.Helper.validateUserInput(sTestName, sRowColN);
		sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
		sRowNumber=Integer.parseInt(sRowColN.split("\\||")[0]);
		sColumnName = sRowColN.split("\\||")[1];
		
		
		sDesc = Logs.log(sTestName) + " :: Store Cell value of column (" +sColumnName + ") and Row number: "+ sRowNumber;
		
		dbResultSet = hTestName_DBResultSet.get(sTestName);
					
		ResultSetDynaClass rsdc = new ResultSetDynaClass(dbResultSet);
		Iterator itr = rsdc.iterator();
		while(itr.hasNext()) {
			DynaBean bean = (DynaBean)itr.next();
			if ((bean.get(sColumnName.toLowerCase())!= null)&&(i==sRowNumber)) {
				sActVal = bean.get(sColumnName.toLowerCase()).toString();
				break;
			}
			i++;
		 }
						
		dbResultSet.beforeFirst();
		
		Utils.setScriptParams(sTestName, sVarName, sActVal);
		
		Reporter.print(sTestName, sDesc +" :: Value Stored : "+ sActVal);
		
	
	} catch(Exception e) {
	
		Reporter.printError(sTestName, e, sDesc);
	}
}
	
	public static void dbStoreCellValue_FromCondition(String sTestName,String sCol, String sVal ) throws SQLException, HeadlessException, IOException, AWTException, InterruptedException {
		
		String sVarName = "", sDesc = null , sColumnName = "", sValueForCompare = "", sRequiredColumnName = "", sRequiredValue = "", sActVal = ""; ResultSet dbResultSet ;
		
		sDesc = Logs.log(sTestName);
		
		try {
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			sCol = Utils.Helper.validateUserInput(sTestName, sCol);
			
			if(sCol.contains("=")) {
				sColumnName = sVal.split("=")[0];
				sValueForCompare = sVal.split("=")[1];
		
				if (sVal.contains("=")) {
					sRequiredColumnName = sVal.split("=")[0];
					sVarName = sVal.split("=")[1];
				} else {
					sRequiredColumnName = sVal;
					sVarName = "Temp";
				}
			} else {
				
				Reporter.print(sTestName, sCol + "*EXIT_ON_FAIL*", sDesc, "True", "False", false);
			}
			dbResultSet = hTestName_DBResultSet.get(sTestName);
			ResultSetDynaClass rsdc = new ResultSetDynaClass(dbResultSet);
			Iterator itr = rsdc.iterator();
			 
			while (itr.hasNext())  {
				DynaBean bean = (DynaBean)itr.next();
				sActVal =  bean.get(sColumnName.toLowerCase()).toString();
				if (sActVal.equalsIgnoreCase(sValueForCompare)) {
					
					sRequiredValue = bean.get(sRequiredColumnName.toLowerCase()).toString();
					break;
				}
			}
			
			dbResultSet.beforeFirst();
			Utils.setScriptParams(sTestName, sVarName, sRequiredValue);
			Reporter.print(sTestName, sDesc + " :: Value Stored");
								
			} catch(Exception e){
			
					Reporter.printError(sTestName, e, sDesc);
			}
	}
	
	public static void dbVerifyCellValue_FromCondition(String sTestName, String sCol,String sVal ) throws SQLException, HeadlessException, IOException, AWTException, InterruptedException {
			
		String sExpVal = "", sDesc = null , sColumnName = "", sValueForCompare = "", sRequiredColumnName = "", colValue = "", sActVal = ""; ResultSet dbResultSet ; Boolean bStatus = false;
		
		sDesc = Logs.log(sTestName);
		
		try {
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			sCol = Utils.Helper.validateUserInput(sTestName, sCol);
			sCol = Reporter.filterUserInput(sCol);	
			
			if (sCol.contains("=")&&sVal.contains("=")) {
				
				sColumnName = sCol.split("=")[0];
				sValueForCompare = sCol.split("=")[1];
				
				sRequiredColumnName = sVal.split("=")[0];
				sExpVal = sVal.split("=")[1];
				
			} else {
				
				Reporter.print(sTestName, sCol + "*EXIT_ON_FAIL*", sDesc, "True", "False", false);
			}
			dbResultSet = hTestName_DBResultSet.get(sTestName);
			ResultSetDynaClass rsdc = new ResultSetDynaClass(dbResultSet);
			Iterator itr = rsdc.iterator();
			 
			while (itr.hasNext())  {
				DynaBean bean = (DynaBean)itr.next();
				colValue =  bean.get(sColumnName.toLowerCase()).toString();
				if(colValue.equalsIgnoreCase(sValueForCompare)) {
					
					sActVal = bean.get(sRequiredColumnName.toLowerCase()).toString();
					if(sActVal.equalsIgnoreCase(sExpVal)) {
						bStatus = true;
						break;
					}
				}
			}
			Reporter.print(sTestName, sCol, sDesc, sExpVal, sActVal, bStatus);	
			dbResultSet.beforeFirst();
		} catch(Exception e){
			
			Reporter.printError(sTestName, e, sDesc);
		}
	}
}