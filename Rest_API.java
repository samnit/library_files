package com.automatics.packages.library.common;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.WebElement;



import static io.restassured.RestAssured.*;
import  static io.restassured.parsing.Parser.JSON;
import static org.hamcrest.Matchers.*;
public class Rest_API {
	
	public static HashMap<String ,HashMap<String,String>> h2Responses=new HashMap<String,HashMap<String,String>>();
	public static String temp_response=null;
	
	public static void get_request(String sTestName,String Req_URL,String get_name) throws InterruptedException, HeadlessException, IOException, AWTException
	{	int code=0;
		JSONArray jsonarr=null;
		JSONObject jsonobj =null;
		String response=null;
		HashMap<String,String> localParam=new HashMap<String,String>();
		String sDesc=null;
		sDesc = Logs.log(sTestName);
	
		try {
			response=given().accept(ContentType.JSON).when().get(Req_URL).thenReturn().body().asString();
			if(response.charAt(0)=='[') {
				jsonarr=  (JSONArray) new JSONParser().parse(response);
				temp_response=jsonarr.toJSONString();
				localParam.put(get_name, jsonarr.toJSONString());
			}
			else {
				jsonobj= (JSONObject) new JSONParser().parse(response);
				temp_response=jsonobj.toJSONString();
				localParam.put(get_name, jsonobj.toJSONString());
			}
			code=given().accept(ContentType.JSON).when().get(Req_URL).thenReturn().statusCode();

			
			if(h2Responses.containsKey(sTestName)) {
				
				h2Responses.get(sTestName).putAll(localParam);
			}
			else{
				
				h2Responses.put(sTestName,localParam);
			}
			if(code==200)
				Logs.log(sTestName, "GET REQUEST SUCCESSFULL, Response is : \n" + temp_response +"\n");
	    	else
	    		Logs.log(sTestName, "GET REQUEST FAILED");
			
			Reporter.print(sTestName, sDesc + "  :: Performed");
		}
		catch(Exception e) {
			
			if (Utils.handleIntermediateIssue()) { get_request(sTestName,Req_URL,get_name); }
			Reporter.printError(sTestName, e, sDesc);
		}
		
	}
		
	public static void post_request(String sTestName,String Req_URL,String json_Path) throws InterruptedException, HeadlessException, IOException, AWTException
	{
		JSONObject jsonobj =null;
		JSONArray jsonarr=null;
		String sDesc=null;
		int status_code=0;
		sDesc = Logs.log(sTestName);
		//json file parsing to be posted
	   	 try
	   	 {	try {
	   		 	jsonobj = (JSONObject)  new JSONParser().parse(new InputStreamReader(new FileInputStream(json_Path)));
	   		 	status_code=io.restassured.RestAssured.given().body(jsonobj.toJSONString()).contentType("application/json; charset=UTF-8").when().post(Req_URL).andReturn().statusCode();
	   	 		}
	   	 	catch(java.lang.ClassCastException e) {
	   	 		jsonarr=  (JSONArray) new JSONParser().parse(new InputStreamReader(new FileInputStream(json_Path)));
	   	 		status_code=io.restassured.RestAssured.given().body(jsonarr.toJSONString()).contentType("application/json; charset=UTF-8").when().post(Req_URL).andReturn().statusCode();
	   	 	}
		 
		   		if(status_code==200)
		   			Logs.log(sTestName, "POST REQUEST SUCCESSFULL");
		    	else
		    		Logs.log(sTestName, "NOT POSTED SUCCESSFULLY");
		   	 
		   	Reporter.print(sTestName, sDesc + "  :: Performed");
	   	}
	   	 catch(Exception e)
	   	 {	Logs.log(sTestName, " ERROR IN READING JSON :: "+e);
	   		if (Utils.handleIntermediateIssue()) { post_request(sTestName,Req_URL,json_Path); }
	   		Reporter.printError(sTestName, e, sDesc);
	   	 }
	   	
	   	
	}
		
	public static void delete_request(String sTestName,String Req_URL) throws InterruptedException, HeadlessException, IOException, AWTException
	{	String sDesc=null;
		sDesc = Logs.log(sTestName);
		
		try {
		   	 int del_status_code=given().when().delete(Req_URL).thenReturn().statusCode();
			 
		   	 if(del_status_code==200)
		   		Logs.log(sTestName, "DELETED SUCCESSFULLY");
		   	 else
		   		Logs.log(sTestName, "NOT DELETED SUCCESSFULLY");
		   	
		   	 Reporter.print(sTestName, sDesc + "  :: Performed");
	   	
		}
		catch(Exception e)
		{
			Logs.log(sTestName, "ERROR IN DELETE REQUEST  ::  "+e);
			if (Utils.handleIntermediateIssue()) { delete_request(sTestName,Req_URL); }
			Reporter.printError(sTestName, e, sDesc);
		}
	}
		
	public static void put_request(String sTestName,String Req_URL,String json_Path) throws InterruptedException, HeadlessException, IOException, AWTException
	{	
		String sDesc=null;
		JSONObject jsonobj =null;
		JSONArray jsonarr=null;
		int status_code=0;
		sDesc = Logs.log(sTestName);
		//json file parsing to be posted
		 try
	   	 {
	   		
	   		try {
	   		 	jsonobj = (JSONObject)  new JSONParser().parse(new InputStreamReader(new FileInputStream(json_Path)));
	   		 	status_code=io.restassured.RestAssured.given().body(jsonobj.toJSONString()).contentType("application/json; charset=UTF-8").when().put(Req_URL).andReturn().statusCode();
	   	 		}
	   	 	catch(java.lang.ClassCastException e) {
	   	 		jsonarr=  (JSONArray) new JSONParser().parse(new InputStreamReader(new FileInputStream(json_Path)));
	   	 		status_code=io.restassured.RestAssured.given().body(jsonarr.toJSONString()).contentType("application/json; charset=UTF-8").when().put(Req_URL).andReturn().statusCode();
	   	 		}
		
		   		if(status_code==200)
		   			Logs.log(sTestName, "PUT REQUEST SUCCESSFULLY EXECUTED");
		    	else
		    		Logs.log(sTestName, "PUT REQUEST FAILED");
		   		
		   	 Reporter.print(sTestName, sDesc + "  :: Performed");	
	   	}
	   	 catch(Exception e)
	   	 {	Logs.log(sTestName, " ERROR IN READING JSON :: "+e);
	   		if (Utils.handleIntermediateIssue()) { put_request(sTestName,Req_URL,json_Path); }
	   		Reporter.printError(sTestName, e, sDesc);
	   	 }
	}
	
	public static void Show_ResponseAPI(String sTestName,String resp_name,String Criteria_path) throws FileNotFoundException, IOException, ParseException, HeadlessException, AWTException
	{	
		String sDesc=null;
		JSONObject jsonobj =null;
		JsonPath jsonPath =null;
		JSONArray jsonarr=null;
		try {
			resp_name=	Utils.Helper.validateUserInput(sTestName,resp_name);
			sDesc = Logs.log(sTestName);
			
			if(resp_name.equals(""))
			{
				try {
					if(temp_response.charAt(0)=='[') {
						jsonarr=(JSONArray) new JSONParser().parse(temp_response);
						jsonPath = new JsonPath(jsonarr.toJSONString());
					}
					else {
						jsonobj = (JSONObject)  new JSONParser().parse(temp_response);
						jsonPath = new JsonPath(jsonobj.toJSONString());
					}
					
					Reporter.print(sTestName, sDesc + "\n*** THE VALUE FOR GIVEN " + Criteria_path + " IS :  " + jsonPath.get(Criteria_path) + "*** :: Performed");
					
				}
				catch(Exception e) {
					Reporter.print(sTestName,  sDesc + "\n*** RESPONSE HAS SOME ERRORS CANNOT PARSE IT  ::  "+ e + "*** :: Performed");
				}
			}
			else
			{	if(h2Responses.get(sTestName).get(resp_name)!=null) {
					try {
						
						
						if(h2Responses.get(sTestName).get(resp_name).charAt(0)=='[') {
							jsonarr=(JSONArray) new JSONParser().parse(h2Responses.get(sTestName).get(resp_name));
							jsonPath = new JsonPath(jsonarr.toJSONString());
						}
						else {
							jsonobj = (JSONObject)  new JSONParser().parse(h2Responses.get(sTestName).get(resp_name));
							jsonPath = new JsonPath(jsonobj.toJSONString());
						}
						
						Reporter.print(sTestName, sDesc + "\n*** THE VALUE FOR GIVEN " + Criteria_path+" IS :  "+jsonPath.get(Criteria_path) + "*** :: Performed");
					}
					catch(Exception e) {
						Reporter.print(sTestName,sDesc + "\n*** RESPONSE HAS SOME ERRORS CANNOT PARSE IT  ::  "+e + "*** :: Performed");
					}
				}
				else
				{
					Reporter.print(sTestName, sDesc + "\n*** NO SUCH RESPONSE STORED"+ "*** :: Performed");
				}
			}
			}
		catch(Exception e)
		{
			if (Utils.handleIntermediateIssue()) { Show_ResponseAPI(sTestName,resp_name,Criteria_path); }
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	
	public static void Store_jsonResp(String sTestName,WebElement oELe,String sObjStr,String var_name) throws ClassNotFoundException, InterruptedException, HeadlessException, IOException, AWTException
	{
		String sDesc, sActVal = null; 
		HashMap<String,String> localParam=new HashMap<String,String>();
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		sActVal = oELe.getText();
		
		try { 
			if(var_name.equals(""))	{
				
				localParam.put(sTestName+"_Temp", sActVal);
				temp_response=sActVal;
			
				if(h2Responses.containsKey(sTestName)) 
					h2Responses.get(sTestName).putAll(localParam);
				else				
					h2Responses.put(sTestName,localParam);
				
			}
			else
			{
				localParam.put(var_name, sActVal);
				temp_response=sActVal;
			
				if(h2Responses.containsKey(sTestName)) 
					h2Responses.get(sTestName).putAll(localParam);
				else				
					h2Responses.put(sTestName,localParam);
			}
			Reporter.print(sTestName, sDesc + " :: STORE THE RESPONSE - " +  sActVal);
		
		}
		catch(Exception e)
		{
			if (Utils.handleIntermediateIssue()) { Store_jsonResp(sTestName, oELe, sObjStr, var_name); }
			Reporter.printError(sTestName, e, sDesc);
		}
	}
}
