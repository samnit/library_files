package com.automatics.packages.library.common;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.automatics.packages.library.common.Logs;
import com.automatics.packages.library.common.Reporter;

import com.automatics.packages.library.common.Utils;

public class SOAP {
	
	private static HashMap<String, HashMap<String, String>> h2XmlTestReferences = new HashMap<>();
	private static HashMap<String, String> hXmlTestCurrentRefer = new HashMap<String, String>();
	
	public static void xmlOpen_File(String sTestName, String sUserVal) throws HeadlessException, IOException, AWTException {

		String sDesc = "", inputFilePath = "", variableName,tcName,fileName; HashMap<String, String> hTestReferences = new HashMap<>();
		
		try {
	
			tcName = sTestName;
			fileName = sUserVal.split("/")[1];
			
			if (sUserVal.contains("{")&sUserVal.contains("}")) {
				//variableName = sUserVal.substring(sUserVal.indexOf("{") + 1, sUserVal.indexOf("}"));
				variableName = tcName + "_"+ fileName;
				inputFilePath = Utils.Helper.validateUserInput(sTestName, sUserVal);
				
			} else {
				inputFilePath = sUserVal;
				variableName = sTestName;
			}
			
			sDesc = Logs.log(sTestName) + ": Set Referrence to File : '" + inputFilePath + "'\n";
			
			hTestReferences.put(variableName, inputFilePath);
			h2XmlTestReferences.put(sTestName,hTestReferences);
			hXmlTestCurrentRefer.put(sTestName, variableName);
			Reporter.print(sTestName , sDesc); 
		    	
		} catch(Exception e) {
			
			Reporter.printError(sTestName, e, sDesc);
		}
	}
		
	public static void xmlSOAP_Request(String sTestName,  String sURL,String sUserPwd) throws IOException, HeadlessException, AWTException {
		
		String sDesc = "", requestMessageFile, variableName, requestData="",line = "", userAuthorization;
		StringWriter writer; Source sourceContent;  SOAPConnection soapConnection; SOAPMessage responseMessage, requestMessage;
		Transformer transformer; StreamResult result; HashMap<String, String> hTestReferences = new HashMap<>();
				
		try {
			
			sURL = Utils.Helper.validateUserInput(sTestName, sURL);
			sUserPwd = Utils.Helper.validateUserInput(sTestName, sUserPwd);
			
			variableName = hXmlTestCurrentRefer.get(sTestName);
			requestMessageFile = h2XmlTestReferences.get(sTestName).get(variableName);
			
						
			sDesc = Logs.log(sTestName) + ": Sending Soap Request for URL: '" + sURL + "' with request meassage from file : '" + requestMessageFile + "' and saving the response in Logs Folder";
			
			URL url = new URL(sURL);			 
			File file = new File(requestMessageFile);
			BufferedReader reader = new BufferedReader(new FileReader(file));
		    while ((line = reader.readLine()) != null) {
		    	  requestData += line + "\r\n";
		    } 
		    reader.close();
		    Logs.log(variableName + "_SOAP_INPUT", requestData); 
		    variableName = variableName + "_SOAP_OUTPUT" ;
			
		    requestMessage = getSoapMessageFromFile(requestMessageFile);
		    MimeHeaders header = requestMessage.getMimeHeaders();
				
		    if (!sUserPwd.equalsIgnoreCase("NA"))	{	        
		    	userAuthorization = new sun.misc.BASE64Encoder().encode((sUserPwd).getBytes());
		    	header.addHeader("Authorization", "Basic " + userAuthorization);	
		    }
		  
			soapConnection = SOAPConnectionFactory.newInstance().createConnection();		
			responseMessage = soapConnection.call(requestMessage, url);		
			transformer = TransformerFactory.newInstance().newTransformer();		
			sourceContent = responseMessage.getSOAPPart().getContent();
			writer = new StringWriter();
			result=new StreamResult(writer);
			transformer.transform(sourceContent, result);
					
			Logs.log(variableName , writer.toString()); 
			Reporter.print(sTestName,  sDesc +"\nSaved XML response at location:" + Reporter.sExecutionLogFldr + "/" + variableName + ".log" );
			hTestReferences.put(variableName, Reporter.sExecutionLogFldr + "/" + variableName +".log");
			h2XmlTestReferences.put(sTestName,hTestReferences);
			
		} catch(Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
		
	}
	
	
	public static void xmlStore_Tag_FirstValue(String sTestName, String sTag,String sVarName) throws HeadlessException, IOException, AWTException {
		 
		String sDesc = "", tagData = null, xmlFile; HashMap<String, String> hTestReferences = new HashMap<>();
			
		try {
			
			sTag = Utils.Helper.validateUserInput(sTestName, sTag);
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			hTestReferences = h2XmlTestReferences.get(sTestName);
			xmlFile = hTestReferences.values().toArray()[0].toString();
			
			sDesc = Logs.log(sTestName) + ": Get First Value of XML Tag = '" + sTag + "' from file '" + xmlFile + "' and store in '"+ sVarName+"')\n";
			           
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFile));
			doc.getDocumentElement().normalize();

			tagData = doc.getElementsByTagName(sTag).item(0).getTextContent();

            Utils.setScriptParams(sTestName, sVarName, tagData);
            Reporter.print(sTestName, sDesc + "Received Value: " + tagData);
	      	      
		} catch(Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
	}

	public static void xmlStore_Tag_AllValues(String sTestName, String sTag,String sVarName) throws HeadlessException, IOException, AWTException {
		
		String sDesc = "", tagData = "", xmlFile; HashMap<String, String> hTestReferences = new HashMap<>();
		
		try {
			
			sTag = Utils.Helper.validateUserInput(sTestName, sTag);
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			hTestReferences = h2XmlTestReferences.get(sTestName);
			xmlFile = hTestReferences.values().toArray()[0].toString();
			
			sDesc = Logs.log(sTestName) + ": Get All Values of XML Tag = '" + sTag + "' from file '" + xmlFile + "' and store in '"+ sVarName+"')\n";
		
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFile));
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(sTag);

			for (int i = 0; i < nList.getLength(); i++) 
				tagData+=nList.item(i).getTextContent().toString() + "++" ;
		
            Utils.setScriptParams(sTestName, sVarName, tagData);
            Reporter.print(sTestName, sDesc + "Received Value: " + tagData);
            	
		} catch(Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	
	public static void xmlStore_Tag_LastValue(String sTestName, String sTag,String sVarName) throws HeadlessException, IOException, AWTException {
		
		String sDesc = "", tagData = "", xmlFile; HashMap<String, String> hTestReferences = new HashMap<>();
		
		try {
			sTag = Utils.Helper.validateUserInput(sTestName, sTag);
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			
			hTestReferences = h2XmlTestReferences.get(sTestName);
			xmlFile = hTestReferences.values().toArray()[0].toString();
			
			sDesc = Logs.log(sTestName) + ": Get Last Value of XML Tag = '" + sTag + "' from file '" + xmlFile + "' and store in '"+ sVarName+"')\n";
		
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFile));
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(sTag);

			for (int i = 0; i < nList.getLength(); i++) 
				tagData = nList.item(i).getTextContent().toString();
		 			
            Utils.setScriptParams(sTestName, sVarName, tagData);
            Reporter.print(sTestName, sDesc + "Received Value: " + tagData);
		} catch(Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	
	public static void xmlStore_CorrespondingTag_Value(String sTestName, String sTagVal,String sTagVar) throws HeadlessException, IOException, AWTException {
		
		String sDesc = "", tagData = "", sTag, xmlFile, sCompareTag, sCompareValue, variableName; HashMap<String, String> hTestReferences = new HashMap<>();
		
		try {
			sTagVal = Utils.Helper.validateUserInput(sTestName, sTagVal);
			sTagVar = Utils.Helper.validateUserInput(sTestName, sTagVar);
			
			sCompareTag = sTagVal.split("\\=")[0]; 
			sCompareValue = sTagVal.split("\\=")[1]; 
			sTag = sTagVar.split("\\=")[0];
			variableName = sTagVar.split("\\=")[1];
						
			hTestReferences = h2XmlTestReferences.get(sTestName);
			xmlFile = hTestReferences.values().toArray()[0].toString();
			
			sDesc = Logs.log(sTestName) + " : Get Value of XML Tag = '" + sTag + "' from file '" + xmlFile + "' where Tag : '" + sCompareTag + "'has value: '" + sCompareValue + "' and store in '"+ variableName+"')\n";
		
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFile));
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(sCompareTag);
			NodeList nGetList = doc.getElementsByTagName(sTag);

			for (int i = 0; i < nList.getLength(); i++)  {
				tagData = nList.item(i).getTextContent().toString();
				if (tagData.equals(sCompareValue)) {
					tagData = nGetList.item(i).getTextContent().toString();
					break;
				}			
			}
            Utils.setScriptParams(sTestName, variableName, tagData);
            Reporter.print(sTestName, sDesc + "Received Value: " + tagData);
	
		} catch(Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	public static void xmlVerify_Tag_Present(String sTestName, String sUserVal) throws HeadlessException, IOException, AWTException {
		
		String sDesc = "", xmlFile, sTag, sExpVal; boolean bStatus = false;; HashMap<String, String> hTestReferences = new HashMap<>();
		
		try {
			
			sExpVal = Utils.Helper.validateUserInput(sTestName, sUserVal);
			sExpVal = Reporter.filterUserInput(sExpVal);
			sTag = sExpVal; 
			
			hTestReferences = h2XmlTestReferences.get(sTestName);
			xmlFile = hTestReferences.values().toArray()[0].toString();
			
			sDesc = Logs.log(sTestName) + " :: Validate wether XML Tag = '" + sTag + "' is present in File: '" + xmlFile +"'";
		
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFile));
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(sTag);
			
	        if(!(nList.getLength() == 0))
	        	bStatus =true;
	        
	       Reporter.print(sTestName, sUserVal, sDesc, "NA", "NA", bStatus); 
		
		} catch(Exception e) {
			
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	
	public static void xmlVerify_Tag_FirstValue(String sTestName, String sTag,String sExpVal) throws HeadlessException, IOException, AWTException {
		
		String sDesc = "", sVal=sTag, xmlFile, sActVal =""; boolean bStatus = false; HashMap<String, String> hTestReferences = new HashMap<>();
		
		try {
			
			sTag = Utils.Helper.validateUserInput(sTestName, sTag);
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			sTag = Reporter.filterUserInput(sTag);
			
			
			hTestReferences = h2XmlTestReferences.get(sTestName);
			xmlFile = hTestReferences.values().toArray()[0].toString();
			
			sDesc = Logs.log(sTestName) + " : Verify whther XML Tag = '" + sTag + "' has value: '" + sExpVal + "'  in file '" + xmlFile + "'\n";
		
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFile));
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(sTag);
			sActVal = nList.item(0).getTextContent().toString();
			if (sActVal.equals(sExpVal)) 
				bStatus = true;
			          
			Reporter.print(sTestName, sVal, sDesc, sExpVal, sActVal, bStatus); 
		
		} catch(Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	public static void xmlVerify_Tag_LastValue(String sTestName, String sTag,String sExpVal) throws HeadlessException, IOException, AWTException {
		
		String sDesc = "", sVal=sTag, xmlFile,sActVal =""; boolean bStatus = false; HashMap<String, String> hTestReferences = new HashMap<>();
		
		try {
			
			sTag = Utils.Helper.validateUserInput(sTestName, sTag);
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			sTag = Reporter.filterUserInput(sTag);
			
			hTestReferences = h2XmlTestReferences.get(sTestName);
			xmlFile = hTestReferences.values().toArray()[0].toString();
			
			sDesc = Logs.log(sTestName) + ": Verify whther XML Tag = '" + sTag + "' has value: '" + sExpVal + "'  in file '" + xmlFile + "'\n";
		
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFile));
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(sTag);
			sActVal = nList.item(nList.getLength()-1).getTextContent().toString();
			if (sActVal.equals(sExpVal)) 
				bStatus = true;
			          
			 Reporter.print(sTestName, sVal, sDesc, sExpVal, sActVal, bStatus); 
		
		} catch(Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	public static void xmlVerify_Tag_InAllValues(String sTestName,  String sTag,String sExpVal) throws HeadlessException, IOException, AWTException {
	
	String sDesc = "", tagData = "", sVal=sTag, xmlFile, sActVal =""; boolean bStatus = false; HashMap<String, String> hTestReferences = new HashMap<>();
	
		try {
			
			sTag = Utils.Helper.validateUserInput(sTestName, sTag);
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			sTag = Reporter.filterUserInput(sTag);
			
			hTestReferences = h2XmlTestReferences.get(sTestName);
			xmlFile = hTestReferences.values().toArray()[0].toString();
			
			sDesc = Logs.log(sTestName) + ": Verify whether XML Tag = '" + sTag + "' has value: '" + sExpVal + "'  in file '" + xmlFile + "'\n";
		
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFile));
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(sTag);
			
			for (int i = 0; i < nList.getLength(); i++)  {
				tagData = nList.item(i).getTextContent().toString();
				if (tagData.equals(sExpVal)) {
					bStatus = true;
					sActVal = tagData;
					break;
				}
			}
	       
			 Reporter.print(sTestName, sVal, sDesc, sExpVal, sActVal, bStatus); 
		
		} catch(Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	public static void xmlUpdate_Tag_Value(String sTestName, String sTag,String sValue) throws HeadlessException, IOException, AWTException, ParserConfigurationException, SAXException, TransformerException {
		
		String sDesc = "", xmlFile; HashMap<String, String> hTestReferences = new HashMap<>();
		
		try {
			sTag = Utils.Helper.validateUserInput(sTestName, sTag);
			sValue = Utils.Helper.validateUserInput(sTestName, sValue);
			sTag = Reporter.filterUserInput(sTag);
			
			hTestReferences = h2XmlTestReferences.get(sTestName);
			xmlFile = hTestReferences.values().toArray()[0].toString();
			
			sDesc = Logs.log(sTestName) + ": Update XML Tag = '" + sTag + "' with value: '" + sValue + "'  in file '" + xmlFile + "'\n";
		
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFile));
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(sTag);
			
			for (int i = 0; i < nList.getLength(); i++)  {
				nList.item(i).setTextContent(sValue);
			}
			
    		Transformer transformer = TransformerFactory.newInstance().newTransformer();
    		transformer.transform( new DOMSource(doc), new StreamResult(new File(xmlFile).toURI().getPath()));
    		
			Reporter.print(sTestName, sDesc + ":: Updated");
		
		} catch (Exception e) {
			
			Reporter.printError(sTestName, e, sDesc);
		} 
	}	
	
	public static void xmlUpdate_CorrespondingTag_Value(String sTestName, String sCTagVal,String sTagVal) throws HeadlessException, IOException, AWTException, ParserConfigurationException, SAXException, TransformerException {
		
		String sDesc = "", tagData = "", sTag, xmlFile, sCompareTag, sCompareValue, sActVal; HashMap<String, String> hTestReferences = new HashMap<>();
		
		try {
			sCTagVal = Utils.Helper.validateUserInput(sTestName, sCTagVal);
			sTagVal = Utils.Helper.validateUserInput(sTestName, sTagVal);
			sCTagVal = Reporter.filterUserInput(sCTagVal);
			
			sCompareTag = sCTagVal.split("\\=")[0]; 
			sCompareValue = sCTagVal.split("\\=")[1]; 
			sTag = sTagVal.split("\\=")[0];
			sActVal = sTagVal.split("\\=")[1];
						
			hTestReferences = h2XmlTestReferences.get(sTestName);
			xmlFile = hTestReferences.values().toArray()[0].toString();
			
			sDesc = Logs.log(sTestName) + ": Update Value of XML Tag = '" + sTag + "' with value : '"+ sActVal + "' in file '" + xmlFile + "' where Tag : '" + sCompareTag + "'has value: '" + sCompareValue + "'\n";
		
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlFile));
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName(sCompareTag);
			NodeList nGetList = doc.getElementsByTagName(sTag);

			for (int i = 0; i < nList.getLength(); i++)  {
				tagData = nList.item(i).getTextContent().toString();
				if (tagData.equals(sCompareValue)) {
					nGetList.item(i).setTextContent(sActVal);
					break;
				}			
			}
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
    		transformer.transform( new DOMSource(doc), new StreamResult(new File(xmlFile).toURI().getPath()));
    		
            Reporter.print(sTestName, sDesc + ":: Updated");
	
		} catch(Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	
	private static SOAPMessage getSoapMessageFromFile(String filename) throws Exception
	{
		File file=new File(filename);
		BufferedReader reader=new BufferedReader(new FileReader(file));
		String xml="",temp;
		while((temp=reader.readLine())!=null) {
			xml=xml+temp;
		}
		reader.close();
		MessageFactory factory=MessageFactory.newInstance();
		SOAPMessage message=factory.createMessage(new MimeHeaders(),new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
		return message;
	}	
}