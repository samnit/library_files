package com.automatics.packages.library.common;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.support.ui.*;
import org.testng.ITestContext;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.screentaker.ViewportPastingStrategy;

import com.google.gson.JsonObject;


@SuppressWarnings({"rawtypes", "unchecked"})
public class Web 
{
	
	private static Hashtable<String, String> hTestName_CurrentWebInstance = new Hashtable<String, String>();
	private static Hashtable<String, Hashtable<String, WebDriver>> h2TestName_WebDriver = new Hashtable();	
	private static Hashtable<String, Hashtable<String, String>> h2FirefoxPrefs = new Hashtable<String, Hashtable<String, String>>();
	private static Hashtable<String, Hashtable<String, String>> h2IEPrefs = new Hashtable<String, Hashtable<String, String>>();
	private static Hashtable<String, Hashtable<String, String>> h2ChromePrefs = new Hashtable<String, Hashtable<String, String>>();
	
	private static void putWebDriver(String sTestName, WebDriver driver, String currentWebInstance) {
		
		Hashtable <String, WebDriver > hdriver = new Hashtable<>();
		hdriver.put(currentWebInstance, driver);
		if (h2TestName_WebDriver.containsKey(sTestName))
			h2TestName_WebDriver.get(sTestName).put(currentWebInstance, driver);
		else 
			h2TestName_WebDriver.put(sTestName, hdriver);
	}
	
	private static WebDriver getWebDriver(String sTestName) {
	
		try {
			
			if (hTestName_CurrentWebInstance.containsKey(sTestName))
				return h2TestName_WebDriver.get(sTestName).get(hTestName_CurrentWebInstance.get(sTestName));
			
			else {
				Object myKey = h2TestName_WebDriver.get(sTestName).keySet().toArray()[0];
				return (WebDriver) h2TestName_WebDriver.get(sTestName).get(myKey);
			}
		} catch (Exception e) {
			throw e;
		}
}
	// Methods Exposed To User
	
	public static void wbSet_BrowserPreference(String sTestName, String sPreferences, String sValue) throws HeadlessException, IOException, AWTException, InterruptedException { // TODO - Expose List of Prefs
			Hashtable <String, String > hPref = new Hashtable<>();
			hPref.put(sPreferences, sValue);
		
			String sDesc = Logs.log(sTestName);
		
			try { 
	
				if(Utils.h2TestName_TestParams.get(sTestName).get("Exe_Platform").equalsIgnoreCase("Firefox")) {
					if (h2FirefoxPrefs.containsKey(sTestName))
						h2FirefoxPrefs.get(sTestName).put(sPreferences, sValue);
					else {
						h2FirefoxPrefs.put(sTestName,hPref);
					}
				}		
				
				else if (Utils.h2TestName_TestParams.get(sTestName).get("Exe_Platform").equalsIgnoreCase("Chrome")) {
					if (h2ChromePrefs.containsKey(sTestName))
						h2ChromePrefs.get(sTestName).put(sPreferences, sValue);
					else
						h2ChromePrefs.put(sTestName, hPref);
				}			
				
				else {
				
					if (h2IEPrefs.containsKey(sTestName))
						h2IEPrefs.get(sTestName).put(sPreferences, sValue);
					else
						h2IEPrefs.put(sTestName, hPref);
				}
			
				Reporter.print(sTestName, sDesc + "\nBrowser Preference :" + sPreferences + " Set to : " + sValue + " -- Done");
			
		} catch (Exception ex) {
			
			Reporter.printError(sTestName, ex, sDesc, "");
		}
	}
	
	public static WebDriver wbLaunchBrowser(String sTestName, String sUrl, String sBrowserReference) throws Exception { // TODO - Expose Test Params
	
		WebDriver driver = null;
		String sDesc = Logs.log(sTestName);
		
		try { 
		    String sBrowser = Utils.h2TestName_TestParams.get(sTestName).get("Exe_Platform");
		    
		    if (sBrowser.equalsIgnoreCase("IE"))
		    	driver = Helper.launchIE(sTestName);
	    	else if (sBrowser.equalsIgnoreCase("Firefox"))
	    		driver = Helper.launchFirefox(sTestName);
	    	else if (sBrowser.equalsIgnoreCase("Chrome")) 
				driver = Helper.launchChrome(sTestName);
			
			driver.manage().timeouts().implicitlyWait(Long.valueOf(Utils.hEnvParams.get("OBJ_TIMEOUT")), TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(Long.valueOf(Utils.hEnvParams.get("OBJ_TIMEOUT")), TimeUnit.SECONDS);
			driver.manage().window().maximize();
			sBrowserReference = Utils.Helper.validateUserInput(sTestName, sBrowserReference);
			sBrowserReference = (sBrowserReference.equals("")) ? "Default" : sBrowserReference;

			putWebDriver(sTestName, driver, sBrowserReference);
			sUrl = Utils.Helper.validateUserInput(sTestName, sUrl);
			
			driver.get(sUrl);
			
			Reporter.print(sTestName, sDesc + " -- "  + sUrl);
		
		} catch (Exception ex) {
		
			Reporter.printError(sTestName, ex, sDesc, "");
		}
		return driver;
	}
	
	public static WebDriver wbSet_CurrentReference(String sTestName, String sBrowserReference) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = Logs.log(sTestName);
		sBrowserReference = Utils.Helper.validateUserInput(sTestName, sBrowserReference);
		
		try { 
		
			hTestName_CurrentWebInstance.put(sTestName, sBrowserReference);
			
			Reporter.print(sTestName, sDesc  + " to " + sBrowserReference + " -- Done");
	
		} catch (Exception ex) {
			Reporter.printError(sTestName, ex, sDesc, "");
		}
		return h2TestName_WebDriver.get(sTestName).get(sBrowserReference);
	}
	
	
	public static boolean wbVerify_Alert(String sTestName, String sExpAlertTxt) throws HeadlessException, IOException, AWTException, InterruptedException  {

		String sDesc, sActVal, sExpVal; boolean bStatus = false;
		sDesc = Logs.log(sTestName);
		
		
		try { 	
			sExpAlertTxt = Utils.Helper.validateUserInput(sTestName, sExpAlertTxt);
			sExpVal = Reporter.filterUserInput(sExpAlertTxt);
			Helper.checkReady(sTestName, null);
			
			WebDriver lDriver = getWebDriver(sTestName);

			sActVal = lDriver.switchTo().alert().getText();
			lDriver.switchTo().alert().accept();			
			
			bStatus = sExpVal.equals(sActVal);
			Reporter.print(sTestName, sExpAlertTxt, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(lDriver));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_Alert(sTestName, sExpAlertTxt); }
			Reporter.printError(sTestName, e, sDesc,Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}
	
	@SuppressWarnings("unused")
	public static boolean wbVerify_AlertPresent(String sTestName, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException {
		
		String sDesc, sExpVal ; boolean bStatus = false;
		sDesc = Logs.log(sTestName);
		
		
		try { 
			
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			sExpVal = Reporter.filterUserInput(sVal);
			Helper.checkReady(sTestName, null);
			
			WebDriver lDriver = getWebDriver(sTestName);

			WebDriverWait wait = new WebDriverWait(lDriver, 20);
			
			try {
				if (wait.until(ExpectedConditions.alertIsPresent()) != null) { bStatus = true; }
			}
			catch (Exception e) {}
			
			Reporter.print(sTestName, sVal, sDesc, "NA", "NA", bStatus, Helper.takeScreenshot(lDriver));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_AlertPresent(sTestName,sVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}
	
	public static boolean wbVerify_Attribute(String sTestName, WebElement oEle, String sObjStr, String sAttribName,String sExpVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {

		String sDesc, sActVal,sVal=sAttribName; boolean bStatus = false;
	
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		
		
		try { 	
			sAttribName = Utils.Helper.validateUserInput(sTestName, sAttribName);
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			sAttribName = Reporter.filterUserInput(sAttribName);
			Helper.checkReady(sTestName, oEle);
			
			sActVal= oEle.getAttribute(sAttribName);
			
			bStatus = sExpVal.equals(sActVal);
			Reporter.print(sTestName, sVal, sDesc + " :: Validate the atttibute - " + sAttribName, sExpVal, sActVal, bStatus, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_Attribute(sTestName, oEle, sObjStr,sVal, sExpVal); }
			Reporter.printError(sTestName, e, sDesc , Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean wbVerify_Checked(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc; boolean bStatus = false;
	
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		
		try { 	
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			Reporter.filterUserInput(sVal);
			Helper.checkReady(sTestName, oEle);
			
			if (oEle.isSelected()) {
				bStatus = true;
			}
			Reporter.print(sTestName, sVal, sDesc , "NA", "NA", bStatus,Helper.takeScreenshot(Web.getWebDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_Checked(sTestName, oEle, sObjStr, sVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}
	
	public static boolean wbVerify_Cookies(String sTestName,String sCookieName, String sExpVal) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc, sActVal,sVal=sCookieName; boolean bStatus = false;
		
		sDesc = Logs.log(sTestName);
		
	    
		try { 
			
			sCookieName = Utils.Helper.validateUserInput(sTestName, sCookieName);
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			sCookieName = Reporter.filterUserInput(sCookieName);

		    Helper.checkReady(sTestName, null);
		    
			WebDriver lDriver = getWebDriver(sTestName);
			sActVal = lDriver.manage().getCookieNamed(sCookieName).getValue();
			bStatus = sExpVal.equals(sActVal);
			Reporter.print(sTestName, sVal, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(lDriver));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_Cookies(sTestName, sVal,sExpVal); }
			Reporter.printError(sTestName, e, sDesc,Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean wbVerify_Editable(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc; boolean bStatus = false; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
				
		try { 	
			
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			Reporter.filterUserInput(sVal);
			Helper.checkReady(sTestName, oEle);
			
			if (oEle.isEnabled()) { 
				bStatus = true;
			}
			Reporter.print(sTestName, sVal, sDesc, "NA", "NA", bStatus, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_Editable(sTestName, oEle, sObjStr, sVal); }
			Reporter.printError(sTestName, e, sDesc,Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean wbVerify_Object_Dimension(String sTestName, WebElement oEle, String sObjStr, String sDimenType,String sDimenVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {

		String sDesc, sActVal = null,sVal=sDimenType; boolean bStatus = false;
			
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
	    
		try { 
			sDimenType = Utils.Helper.validateUserInput(sTestName, sDimenType);
			sDimenVal = Utils.Helper.validateUserInput(sTestName, sDimenVal);
			sDimenType = Reporter.filterUserInput(sDimenType);

		    Helper.checkReady(sTestName, oEle);
						
		    if (sDimenType.equalsIgnoreCase("Height")) {
		    	sActVal = String.valueOf(oEle.getSize().getHeight());
		    	
		    } else if (sDimenType.equalsIgnoreCase("Width")) {
		    	sActVal = String.valueOf(oEle.getSize().getWidth());
		    	
		    } else if (sDimenType.equalsIgnoreCase("Both")) {
		    	
		    	sActVal = String.valueOf(oEle.getSize().getHeight()) + "|" + String.valueOf(oEle.getSize().getWidth());
		    }
		    
		    bStatus = sDimenVal.equals(sActVal);
			Reporter.print(sTestName, sVal, sDesc + " :: Values - " + sDimenType, sDimenVal, sActVal, bStatus, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_Object_Dimension(sTestName, oEle, sObjStr, sVal,sDimenVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}
	
	
	public static boolean wbVerify_ElementPresent(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc; boolean bStatus = false; 
			
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
			
		try { 	
			
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			Reporter.filterUserInput(sVal);
			Helper.checkReady(sTestName, oEle);
			WebDriver driver = getWebDriver(sTestName);
			try {
				
				bStatus = Helper.waitForElement(driver,oEle, Integer.parseInt(Utils.hEnvParams.get("OBJ_TIMEOUT")));
				Reporter.print(sTestName, sVal, sDesc, "NA", "NA", bStatus, Helper.takeScreenshot(driver));
			}
			catch (NoSuchElementException e) {
				Reporter.print(sTestName, sVal, sDesc, "NA", "NA", false , Helper.takeScreenshot(driver));
			}
			
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_ElementPresent(sTestName, oEle, sObjStr, sVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}
	
	public static boolean wbVerify_Text(String sTestName, WebElement oEle, String sObjStr, String sExpText) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc, sActVal = null, sExpVal; boolean bStatus = false;
				
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
	
		try { 
			
			sExpText = Utils.Helper.validateUserInput(sTestName, sExpText);
			sExpVal = Reporter.filterUserInput(sExpText);
			Helper.checkReady(sTestName, oEle);
			
			sActVal = oEle.getText();
			bStatus = sExpVal.equalsIgnoreCase(sActVal);
			
			Reporter.print(sTestName, sExpText, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_Text(sTestName, oEle, sObjStr, sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
		
	}

	public static boolean wbEquals(String sTestName, String sExpVal,String sActVal) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = "",sVal=sExpVal; boolean bStatus = false;
	
		try { 
	    	
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			sActVal = Utils.Helper.validateUserInput(sTestName, sActVal);
			sDesc = Logs.log(sTestName) + " :: Values - " + sExpVal +" And "+ sActVal;
			sExpVal = Reporter.filterUserInput(sExpVal);

		    
		    bStatus = sExpVal.equals(sActVal);
			Reporter.print(sTestName, sVal, sDesc, sExpVal, sActVal, bStatus);
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbEquals(sTestName, sVal,sActVal); }
			Reporter.printError(sTestName, e, sDesc);
		}
		return bStatus;
	}

	public static boolean wbVerifyIn_HTMLSource(String sTestName, String sExpText) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc, sExpVal; boolean bStatus = false;
		sDesc = Logs.log(sTestName) + " :: Values - " + sExpText;
		sExpVal = Reporter.filterUserInput(sExpText);
		Helper.checkReady(sTestName, null);
		
		try { 	
			WebDriver lDriver = getWebDriver(sTestName);
			
			bStatus = lDriver.getPageSource().contains(sExpVal);
			Reporter.print(sTestName, sExpText, sDesc, "NA", "NA", bStatus, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerifyIn_HTMLSource(sTestName, sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}
	
	public static boolean wbVerifyIn_URL(String sTestName, String sExpText) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc, sActVal = null, sExpVal; boolean bStatus = false;
		sDesc = Logs.log(sTestName);
		
		try { 	
			sExpText = Utils.Helper.validateUserInput(sTestName, sExpText);
			sExpVal = Reporter.filterUserInput(sExpText);
						
			WebDriver lDriver = getWebDriver(sTestName);
			
			sActVal = lDriver.getCurrentUrl();
			bStatus = sActVal.contains(sExpVal);
			
			Reporter.print(sTestName, sExpText, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(lDriver));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerifyIn_URL(sTestName, sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean wbVerify_SelectOptions(String sTestName, WebElement oEle, String sObjStr, String sExpOptions) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {

		String sDesc, sActVal = null, sExpVal; boolean bStatus = false;
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		
		
		try { 	
			
			sExpOptions = Utils.Helper.validateUserInput(sTestName, sExpOptions);
			sExpVal = Reporter.filterUserInput(sExpOptions);
			Helper.checkReady(sTestName, oEle);
			
			if(oEle.getTagName().toString().equalsIgnoreCase("select")){
				
				Select selList = new Select(oEle); List<WebElement> oSize = selList.getOptions();
				for(int i =0; i<oSize.size(); i++) { 
					sActVal = sActVal + "|" + selList.getOptions().get(i).getText(); 
				}		
				if (sExpVal.contains("\\|")) {
					
					for (String sExpOption: sExpVal.split("\\|")) {
					
						if (!sActVal.contains(sExpOption))  {
							bStatus = false; break;
						} else 
							bStatus = true;
					}
				}
				else if (sActVal.contains(sExpVal))  
						bStatus = true;
				
				
				Reporter.print(sTestName, sExpOptions, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
			}
			else
				Reporter.print(sTestName, sDesc + "  :: Error : This Method Works only for Html : 'Select' tag");

		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_SelectOptions(sTestName, oEle, sObjStr, sExpOptions); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean wbVerify_SelectedOptions(String sTestName, WebElement oEle, String sObjStr, String sExpOptions) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc, sActVal = null, sExpVal; boolean bStatus = false;
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		
		try {
			
			sExpOptions = Utils.Helper.validateUserInput(sTestName, sExpOptions);
			sExpVal = Reporter.filterUserInput(sExpOptions);
			Helper.checkReady(sTestName, oEle);
			
			if(oEle.getTagName().toString().equalsIgnoreCase("select")){
				
				Select selList = new Select(oEle); List<WebElement> oSize = selList.getAllSelectedOptions();
				for(int i =0; i<oSize.size(); i++) { 
					sActVal = sActVal + "|" + selList.getAllSelectedOptions().get(i).getText(); 
				}		
				
				for (String sExpOption: sExpVal.split("\\|")) {
					
					if (!sActVal.contains(sExpOption))  {
						bStatus = false; break;
					} else 
						bStatus = true;
				}
				
				Reporter.print(sTestName, sExpOptions, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
			}
			else
				Reporter.print(sTestName, sDesc + "  :: Error : This Method Works only for Html : 'Select' tag");

		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_SelectedOptions(sTestName, oEle, sObjStr, sExpOptions); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean wbVerify_TextPresent(String sTestName, String sExpText) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = "", sExpVal; boolean bStatus = false;
		
		try { 
			
			sExpText = Utils.Helper.validateUserInput(sTestName, sExpText);
			sDesc = Logs.log(sTestName) + " :: Validating text '" + sExpText + "'";
			sExpVal = Reporter.filterUserInput(sExpText);
			Helper.checkReady(sTestName, null);
			
			WebDriver lDriver = getWebDriver(sTestName);
			
			List<WebElement> oList = lDriver.findElements(By.xpath("//*[contains(text(),'" + sExpVal + "')]"));
			
			if (oList.size() > 0) bStatus = true;
			
			Reporter.print(sTestName, sExpText, sDesc, "NA", "NA", bStatus, Helper.takeScreenshot(lDriver));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_TextPresent(sTestName, sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean wbVerifyIn_Title(String sTestName, String sExpTitle) throws HeadlessException, IOException, AWTException, InterruptedException , InterruptedException {
		
		String sDesc = "", sExpVal; boolean bStatus = false;
		
		try { 	
			
			sExpTitle = Utils.Helper.validateUserInput(sTestName, sExpTitle);
			sDesc = Logs.log(sTestName) + " :: Validating Title '" + sExpTitle + "'";
			sExpVal = Reporter.filterUserInput(sExpTitle);
			Helper.checkReady(sTestName,null);
			
			WebDriver lDriver = getWebDriver(sTestName);
			
			if (lDriver.getTitle().contains(sExpVal))
				bStatus = true;
			
			Reporter.print(sTestName, sExpTitle, sDesc, sExpVal, lDriver.getTitle(), bStatus, Helper.takeScreenshot(lDriver));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerifyIn_Title(sTestName, sExpTitle); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean wbVerify_Visible(String sTestName, WebElement oEle, String sObjStr, String sUserVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc; boolean bStatus = false;
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		
		try { 	
			
			sUserVal = Utils.Helper.validateUserInput(sTestName, sUserVal);
			Reporter.filterUserInput(sUserVal);
			Helper.checkReady(sTestName, oEle);
			
			if (oEle.isDisplayed())
				bStatus = true;
			
			Reporter.print(sTestName, sUserVal, sDesc + " :: Validating visible '" + sUserVal + "'", "NA", "NA", bStatus, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_Visible(sTestName, oEle, sObjStr, sUserVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}

	public static void wbCheck_Uncheck(String sTestName, WebElement oEle, String sObjStr, String sUserVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc; 
			
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		Helper.checkReady(sTestName, oEle);
		
		try { 	
			sUserVal = Utils.Helper.validateUserInput(sTestName, sUserVal);	
			WebDriver lDriver = getWebDriver(sTestName);

			if (sUserVal.equalsIgnoreCase("true")) {
				
				if (!oEle.isSelected()) {
					Helper.forceClick(lDriver, oEle);
				}
			} else if (sUserVal.equalsIgnoreCase("false")) {
				
				if (oEle.isSelected()) {
					Helper.forceClick(lDriver, oEle);
				}
			}
				
			Reporter.print(sTestName, sDesc + " :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbCheck_Uncheck(sTestName, oEle, sObjStr, sUserVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}

	public static void wbClick(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc = "";
						
		sDesc = Logs.log(sTestName) + " on Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		
				
		try { 	
			WebDriver lDriver = getWebDriver(sTestName);
			
			try {
				Helper.forceClick(lDriver, oEle);
			}
			catch (Exception e) {
				oEle.click();
			}
			Reporter.print(sTestName, sDesc + " :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbClick(sTestName, oEle, sObjStr); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	
	
	public static void wbPrint(String sTestName, String sTxt) throws HeadlessException, IOException, AWTException, InterruptedException, InvalidInputException  {

		String sDesc = Logs.log(sTestName);
		sTxt = Utils.Helper.validateUserInput(sTestName, sTxt);	
		Reporter.print(sTestName, sDesc + " :: '" + sTxt + "'");
	}

	public static void wbGoBack(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc; 
		sDesc = Logs.log(sTestName);
		Helper.checkReady(sTestName, null);
		
		try { 	
			WebDriver lDriver = getWebDriver(sTestName);
			lDriver.navigate().back();

			Reporter.print(sTestName, sDesc + " :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbGoBack(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}

	public static void wbOpenURL(String sTestName, String sURL) throws HeadlessException, IOException, AWTException, InterruptedException  {
		

		String sDesc = ""; 
		
		try {
		
			sURL = Utils.Helper.validateUserInput(sTestName, sURL);
		
			sDesc = Logs.log(sTestName) + ":  " + sURL;
		
			WebDriver lDriver = getWebDriver(sTestName);
			lDriver.get(sURL);
			Reporter.print(sTestName, sDesc + "  :: Performed");
			
		} catch ( Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbOpenURL(sTestName, sURL); }
			Reporter.printError(sTestName, e, sDesc);
		}
		 
	}

	public static void wbRefresh(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc; 
		sDesc = Logs.log(sTestName);
		Helper.checkReady(sTestName, null);
		
		try { 	
			WebDriver lDriver = getWebDriver(sTestName);
			lDriver.navigate().refresh();

			Reporter.print(sTestName, sDesc + "  :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbRefresh(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}

	public static void wbMultiSelect(String sTestName, WebElement oEle, String sObjStr, String sOptions) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {

		String sDesc; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		Helper.checkReady(sTestName, oEle);
		
		try {
			
			sOptions = Utils.Helper.validateUserInput(sTestName, sOptions);
			
			if(oEle.getTagName().toString().equalsIgnoreCase("select")){
				Select oSelect = new Select(oEle);
				
				for (String sValToBeSelected : sOptions.split("\\|")) {
					oSelect.selectByVisibleText(sValToBeSelected);
					oEle.sendKeys(Keys.CONTROL);
				}
				Reporter.print(sTestName, sDesc + " :: Performed");
			}
			else
				Reporter.print(sTestName, sDesc + "  :: Error : This Method Works only for Html : 'Select' tag");
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbMultiSelect(sTestName, oEle, sObjStr, sOptions); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}

	public static void wbSelect(String sTestName, WebElement oEle, String sObjStr, String sOption) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		Helper.checkReady(sTestName, oEle);
		
		try { 
			sOption = Utils.Helper.validateUserInput(sTestName, sOption);
			
			if(oEle.getTagName().toString().equalsIgnoreCase("select")){
				Select oSelect = new Select(oEle);
		        oSelect.selectByVisibleText(sOption);            
	
				Reporter.print(sTestName, sDesc + " :: Performed");
			}
			else
				Reporter.print(sTestName, sDesc + " :: Error : This Method Works only for Html : 'Select' tag");
	
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbSelect(sTestName, oEle, sObjStr, sOption); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}

	public static void wbVerify_Frame(String sTestName, String sFrameInfo) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc, sExpVal; boolean bStatus = false;
		sDesc = Logs.log(sTestName);
		Helper.checkReady(sTestName, null);
		
		try { 	
			
			sFrameInfo = Utils.Helper.validateUserInput(sTestName, sFrameInfo);
			sExpVal  = Reporter.filterUserInput(sFrameInfo);
			
			WebDriver lDriver = getWebDriver(sTestName);
			List<WebElement> oFrameList = lDriver.findElements(By.xpath("//iframe|//frame"));
			for (WebElement oFrame : oFrameList)  {
				if (oFrame.isDisplayed()) {
					
						JavascriptExecutor executor = (JavascriptExecutor) lDriver;
						String sOutAttribs = (String) executor.executeScript("var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index)" +
								"{ items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;", oFrame).toString();
						if (sOutAttribs.contains(sExpVal)) {
							 bStatus=true; break;
						}
				}
			}
			
			Reporter.print(sTestName, sFrameInfo, sDesc, "NA", "NA", bStatus, Helper.takeScreenshot(lDriver));
					
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_Frame(sTestName, sFrameInfo); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	public static void wbWindow_Resize(String sTestName,String wSize) throws HeadlessException, InvalidInputException, IOException, AWTException, InterruptedException
	{	String sDesc="";
		sDesc = Logs.log(sTestName);
		try 
		{
		wSize = Utils.Helper.validateUserInput(sTestName,wSize);
		String[] output=wSize.split(",");  
		Dimension dimension = new Dimension(Integer.parseInt(output[0]),Integer.parseInt(output[1]));
		WebDriver lDriver = getWebDriver(sTestName);
		lDriver.manage().window().setSize(dimension);
		Reporter.print(sTestName, sDesc + " ( WIDTH : "+output[0]+" HEIGHT : "+output[1]+") size  :: Performed");
		}
			catch (Exception e) {
				
				if (Utils.handleIntermediateIssue()) { wbWindow_Resize(sTestName,wSize); }
				Reporter.printError(sTestName, e, sDesc);
			}
	}
	public static void wbSwitch_To_Frame(String sTestName, String sFrameInfo) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc; boolean bStatus = false;
		sDesc = Logs.log(sTestName);
		Helper.checkReady(sTestName, null);
		
		try { 	
			
			sFrameInfo = Utils.Helper.validateUserInput(sTestName, sFrameInfo);
			
			WebDriver lDriver = getWebDriver(sTestName);
			List<WebElement> oFrameList = lDriver.findElements(By.xpath("//iframe|//frame"));
			for (WebElement oFrame : oFrameList)  {
				if (oFrame.isDisplayed()) {
					
					if (!sFrameInfo.isEmpty()) { 
						JavascriptExecutor executor = (JavascriptExecutor) lDriver;
						String sOutAttribs = (String) executor.executeScript("var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index)" +
								"{ items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;", oFrame).toString();
						if (sOutAttribs.contains(sFrameInfo)) {
							lDriver.switchTo().frame(oFrame); bStatus=true; break;
						}
					} else {
						lDriver.switchTo().frame(oFrame); bStatus=true; break;
					}
				}
			}
			
			if (!bStatus)
				Reporter.print(sTestName, sDesc + "  :: Frame '" + sFrameInfo + "' not found");
			else
				Reporter.print(sTestName, sDesc + "  :: Performed - '" + sFrameInfo + "'");
					
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbSwitch_To_Frame(sTestName, sFrameInfo); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	
	public static void wbSwitch_To_Default(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc; 
		sDesc = Logs.log(sTestName);
		Helper.checkReady(sTestName, null);
		
		try { 	
			WebDriver lDriver = getWebDriver(sTestName);

			lDriver.switchTo().defaultContent();
			Reporter.print(sTestName, sDesc + "  :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbSwitch_To_Default(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}

	public static void wbSwitch_To_Window(String sTestName, String sWindowInfo) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = Logs.log(sTestName);
		Helper.checkReady(sTestName, null);
		
		try { 	
			sWindowInfo = Utils.Helper.validateUserInput(sTestName, sWindowInfo);
			
			WebDriver lDriver = getWebDriver(sTestName);
			if (sWindowInfo.isEmpty()) {
				
				Utils.setScriptParams(sTestName, "Parent_Brwsr", lDriver.getWindowHandle());
				for (String sWinHandle : lDriver.getWindowHandles())  {
				    lDriver.switchTo().window(sWinHandle);
				  }
				Utils.setScriptParams(sTestName, "Child_Brwsr", lDriver.getWindowHandle());

			} else if (sWindowInfo.equalsIgnoreCase("Parent")) {
			
				if (!Utils.h2TestName_ScriptParams.get(sTestName).get("Parent_Brwsr").isEmpty()) {   
					lDriver.switchTo().window(Utils.h2TestName_ScriptParams.get(sTestName).get("Parent_Brwsr"));
				} else {	
					for (String sWinHandle : lDriver.getWindowHandles())  {
					    lDriver.switchTo().window(sWinHandle);
					}
					Utils.setScriptParams(sTestName, "Parent_Brwsr", lDriver.getWindowHandle());
				}
				
			} else if (sWindowInfo.equalsIgnoreCase("Child")) {
				if (!Utils.h2TestName_ScriptParams.get(sTestName).get("Child_Brwsr").isEmpty()) {   
					lDriver.switchTo().window(Utils.h2TestName_ScriptParams.get(sTestName).get("Child_Brwsr"));
				} else {	
					for (String sWinHandle : lDriver.getWindowHandles())  {
					    lDriver.switchTo().window(sWinHandle);
					}
					Utils.setScriptParams(sTestName, "Child_Brwsr", lDriver.getWindowHandle());
				}
			}
			
			Reporter.print(sTestName, sDesc + "  :: Performed - '" + sWindowInfo + "'");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbSwitch_To_Window(sTestName, sWindowInfo); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}

	public static void wbStore_Attribute(String sTestName, WebElement oEle, String sObjStr, String sComputedVal,String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc = "", sActVal; 
		
		try { 			
			sComputedVal = Utils.Helper.validateUserInput(sTestName, sComputedVal);
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
		
			sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
			Helper.checkReady(sTestName, oEle);
			
			if (sVarName.equals("")) {
				sVarName = "Temp";
			} 
			sActVal = oEle.getAttribute(sComputedVal);
			Utils.setScriptParams(sTestName, sVarName, sActVal);
				
			Reporter.print(sTestName, sDesc + " :: Store (Attribute-" + sComputedVal + ", Value-" + sActVal + ") in Local Variable '" + sVarName + "'");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbStore_Attribute(sTestName, oEle, sObjStr,sComputedVal,sVarName ); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}

public static void wbStore_URL(String sTestName, String sUserVal) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = "", sActVal, sVarName; 
		
		
		try { 
			sUserVal = Utils.Helper.validateUserInput(sTestName, sUserVal);
			
			sDesc = Logs.log(sTestName);
			Helper.checkReady(sTestName, null);
			
			WebDriver lDriver = getWebDriver(sTestName);
			
			sVarName = sUserVal;
			if (sUserVal.equals("")) {
				sVarName = "Temp";	
			}
			
			sActVal = lDriver.getCurrentUrl();
			Utils.setScriptParams(sTestName, sVarName, sActVal);
				
			Reporter.print(sTestName, sDesc + "  :: Store (Current URL -" + sActVal + ") in Local Variable '" + sVarName + "'");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbStore_URL(sTestName, sUserVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	
	}
	
	public static void wbStore_SelectOptions(String sTestName, WebElement oEle, String sObjStr, String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc, sActVal = null; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		Helper.checkReady(sTestName, oEle);
		
		try { 	
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			if(oEle.getTagName().toString().equalsIgnoreCase("select")){
				
				Select selList = new Select(oEle); List<WebElement> oSize = selList.getOptions();
				for(int i =0; i<oSize.size(); i++) { 
					sActVal = sActVal + "|" + selList.getOptions().get(i).getText(); 
				}		
				
				if (sVarName.equals("")) {
					sVarName = "Temp";	
				}
				
				sActVal = sActVal.substring(2);
				Utils.setScriptParams(sTestName, sVarName, sActVal);
					
				Reporter.print(sTestName, sDesc + " :: Store options '" + sActVal + "' of "+ sObjStr + " in Local Variable '" + sVarName + "'");
			}
			else
				Reporter.print(sTestName, sDesc + "  :: Error : This Method Works only for Html : 'Select' tag");

		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbStore_SelectOptions(sTestName, oEle, sObjStr, sVarName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}

	public static void wbStore_SelectedValue(String sTestName, WebElement oEle, String sObjStr, String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc, sActVal = null; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		Helper.checkReady(sTestName, oEle);
		
		try { 	
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			if(oEle.getTagName().toString().equalsIgnoreCase("select")){
				
				Select selList = new Select(oEle); List<WebElement> oSize = selList.getAllSelectedOptions();
				for(int i =0; i<oSize.size(); i++) { 
					sActVal = sActVal + "|" + selList.getAllSelectedOptions().get(i).getText(); 
				}		
				
				if (sVarName.equals("")) {
					sVarName = "Temp";	
				}
				
				sActVal = sActVal.substring(2);
				Utils.setScriptParams(sTestName, sVarName, sActVal);
					
				Reporter.print(sTestName, sDesc + " :: Store selected options '" + sActVal + "' of "+ sObjStr + " in Local Variable '" + sVarName + "'");
			}
			else
				Reporter.print(sTestName, sDesc + "  :: Error : This Method Works only for Html : 'Select' tag");
	
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbStore_SelectedValue(sTestName, oEle, sObjStr, sVarName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}

	public static void wbStore_Text(String sTestName, WebElement oEle, String sObjStr, String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc, sActVal = null; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		Helper.checkReady(sTestName, oEle);
		
		try { 	
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			sActVal = oEle.getText(); 
			
			if (sVarName.equals("")) {
				sVarName = "Temp";	
			}
			
			Utils.setScriptParams(sTestName, sVarName, sActVal);
				
			Reporter.print(sTestName, sDesc + " :: Store '" + sActVal + "' of "+ sObjStr + " in Local Variable '" + sVarName + "'");
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbStore_Text(sTestName, oEle, sObjStr, sVarName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}

	public static void wbStore_Value(String sTestName, WebElement oEle, String sObjStr, String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc, sActVal = null; 
		
		sDesc = Logs.log(sTestName) + " of Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		Helper.checkReady(sTestName, oEle);
		
		try { 	
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			sActVal = oEle.getText(); 
			
			if (sVarName.equals("")) {
				sVarName = "Temp";	
			}
			
			Utils.setScriptParams(sTestName, sVarName, sActVal);
				
			Reporter.print(sTestName, sDesc + " :: Store '" + sActVal +  " in Local Variable '" + sVarName + "'");
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbStore_Value(sTestName, oEle, sObjStr, sVarName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	
public static void wbRunScript(String sTestCaseName,ITestContext testContext) throws HeadlessException, IOException, AWTException   {
		
		String sDesc = ""; 
		
		try { 	
			sTestCaseName = Utils.Helper.validateUserInput(testContext.getName(), sTestCaseName);
			sDesc = Logs.log(testContext.getName())+" :: Running  script: ("+sTestCaseName+")";
			
				Reporter.print(testContext.getName(), sDesc + " :: Script : "+ sTestCaseName + " Called"); 
				Class<?> c = Class.forName("com.automatics.packages.testScripts."+sTestCaseName);
				Class<?>[] paramTypes = {ITestContext.class};
				
				Method method = c.getDeclaredMethod("test", paramTypes);
				
				Object ret = method.invoke(c.newInstance(), testContext);
				Reporter.print(testContext.getName(), sDesc + " :: Script executed");			
					
			
		}	catch (Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbRunScript(sTestCaseName, testContext); }
			Reporter.printError(testContext.getName(), e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(testContext.getName())));
		}
	}
	
	public static void wbType(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {

		String sDesc = "" ; boolean flag = true; 
			
		try { 
			
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			sDesc = Logs.log(sTestName) + " in Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " ) , Value = " + sVal ;
			Helper.checkReady(sTestName, oEle);
			
				
			try {
				oEle.clear(); Thread.sleep(500L); 
			}
			catch (Exception e) {
				flag = false;
				oEle.sendKeys(sVal);
			}
			if(flag)
			oEle.sendKeys(sVal);
			
			Reporter.print(sTestName, sDesc + " :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbType(sTestName, oEle, sObjStr, sVal); }
	    	Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	@SuppressWarnings("unused")
	public static void wbType_Advanced(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {

		String sDesc = "" ; boolean flag = true; 
			
		try { 
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			sDesc = Logs.log(sTestName) + " in Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " ) , Value = " + sVal ;
			Helper.checkReady(sTestName, oEle);
			
			WebDriver lDriver = getWebDriver(sTestName);	
			Helper.CheckPopUp_Object(sTestName, oEle, sObjStr);
				if (Helper.CheckExist(lDriver,oEle).contains("True")) {
					if (!oEle.getAttribute("value").equals("")){
						oEle.clear();Thread.sleep(2000L);
					} 
					oEle.sendKeys(sVal); 
				}
				wbSwitch_To_Default(sTestName);
			
			
			
			Reporter.print(sTestName, sDesc + " :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbType_Advanced(sTestName, oEle, sObjStr, sVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	//Anukriti	
	public static void wbKeyPress(String sTestName, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException  { 
		
		String sDesc = "", sExpVal,KeyType,KeyAction; 
		
		try { 	
	    	sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			sExpVal = Reporter.filterUserInput(sVal);
			sDesc = Logs.log(sTestName) + " :: Pressing Keys'" + sExpVal + "'";
			KeyType = sExpVal.split("\\+")[0];
		    KeyAction = sExpVal.split("\\+")[1];
		    Helper.checkReady(sTestName, null);
		    
				WebDriver lDriver = getWebDriver(sTestName);
				KeyType.toUpperCase().toString();
				KeyAction.toUpperCase().toString();
				Actions action = new Actions(lDriver);
				new WebDriverWait(lDriver,30);
		
				if(KeyType.equals("CTRL")){switch(KeyAction){
					case "DOWN": action.keyDown(Keys.CONTROL).sendKeys(Keys.ARROW_DOWN).keyUp(Keys.CONTROL).perform();break;
					case "UP": 	action.keyDown(Keys.CONTROL).sendKeys(Keys.ARROW_UP).keyUp(Keys.CONTROL).perform();break;
					case "LEFT":action.keyDown(Keys.CONTROL).sendKeys(Keys.ARROW_LEFT).keyUp(Keys.CONTROL).perform();break;
					case "RIGHT": action.keyDown(Keys.CONTROL).sendKeys(Keys.ARROW_RIGHT).keyUp(Keys.CONTROL).perform();break;
					default:Reporter.print(sTestName, "Info:Wrong values of Keys entered"); }
				}
				else{
					if(KeyType=="SHIFT"){switch(KeyAction)	{
						case "DOWN": action.keyDown(Keys.SHIFT).sendKeys(Keys.ARROW_DOWN).keyUp(Keys.SHIFT).perform();break;
						case "UP": 	action.keyDown(Keys.SHIFT).sendKeys(Keys.ARROW_UP).keyUp(Keys.SHIFT).perform();break;
						case "LEFT":action.keyDown(Keys.SHIFT).sendKeys(Keys.ARROW_LEFT).keyUp(Keys.SHIFT).perform();break;
						case "RIGHT": action.keyDown(Keys.SHIFT).sendKeys(Keys.ARROW_RIGHT).keyUp(Keys.SHIFT).perform();break;
						default:Reporter.print(sTestName, "Info:Wrong values of Keys entered"); }
					}
					else{
						if(KeyType=="ALT")	{switch(KeyAction){
							case "DOWN": action.keyDown(Keys.ALT).sendKeys(Keys.ARROW_DOWN).keyUp(Keys.ALT).perform();break;
							case "UP": 	action.keyDown(Keys.ALT).sendKeys(Keys.ARROW_UP).keyUp(Keys.ALT).perform();break;
							case "LEFT":action.keyDown(Keys.ALT).sendKeys(Keys.ARROW_LEFT).keyUp(Keys.ALT).perform();break;
							case "RIGHT": action.keyDown(Keys.ALT).sendKeys(Keys.ARROW_RIGHT).keyUp(Keys.ALT).perform();break;
							default:Reporter.print(sTestName, "Info:Wrong values of Keys entered"); }
						}
					}
				}
				
				Reporter.print(sTestName, sVal, sDesc, "NA", "NA", true, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
				
			}  catch(Exception e) {
					
				if (Utils.handleIntermediateIssue()) { wbKeyPress(sTestName, sVal); }
				Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
			}
	}
	
	public static void wbDeleteCookies(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException {
		
		String sDesc; 
		sDesc = Logs.log(sTestName)+" :: All Cookies Deleted";
		Helper.checkReady(sTestName, null);
		
		try { 	

			WebDriver lDriver = getWebDriver(sTestName);
			lDriver.manage().deleteAllCookies();
			Reporter.print(sTestName, sDesc);

		}  catch(Exception e) {
			
			if (Utils.handleIntermediateIssue()) { wbDeleteCookies(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
public static void wbSelect_By_Index(String sTestName, WebElement oEle, String sObjStr, String sOption) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		Helper.checkReady(sTestName, oEle);
		
		try { 
			sOption = Utils.Helper.validateUserInput(sTestName, sOption);
			
			if(oEle.getTagName().toString().equalsIgnoreCase("select")){
				Select oSelect = new Select(oEle);
		        oSelect.selectByIndex(Integer.parseInt(sOption)-1);            
	
				Reporter.print(sTestName, sDesc + " :: Performed");
			}
			else
				Reporter.print(sTestName, sDesc + " :: Error : This Method Works only for Html : 'Select' tag");
	
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbSelect_By_Index(sTestName, oEle, sObjStr, sOption); }
			Reporter.printError(sTestName, e, sDesc);
		}
	}
public static void wbType_WithoutClear(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException {

	String sDesc = "" ; 
	sDesc = Logs.log(sTestName);
	try {

		sVal =  Utils.Helper.validateUserInput(sTestName, sVal);
		Helper.checkReady(sTestName, oEle);
				oEle.sendKeys(sVal);

		Reporter.print(sTestName, sDesc + " :: Performed");

	} 
	catch(Exception e) {

	if (Utils.handleIntermediateIssue()) { 
		wbType_WithoutClear(sTestName, oEle, sObjStr, sVal); }
	Reporter.printError(sTestName, e, sDesc);
	}
}
	public static void wbDeleteCache(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException {
		
		String sDesc, sBrowser, sTitle = "", vbPath; 
		sDesc = Logs.log(sTestName) + " :: Cache Deleted";
		Helper.checkReady(sTestName, null);
		
		try { 	
			WebDriver lDriver = getWebDriver(sTestName);
			sBrowser = Utils.h2TestName_TestParams.get(sTestName).get("Platform"); 
			if(sBrowser.equalsIgnoreCase("Firefox"))
				sTitle = lDriver.getTitle() +" - Mozilla Firefox";
			else if(sBrowser.equalsIgnoreCase("IE"))
				sTitle = lDriver.getTitle() +" - Internet Explorer";
			
				
			vbPath = "\""+Utils.hSystemSettings.get("packageFolder").replace("\\", "/") +"/delcache.vbs\"";
		
			Runtime.getRuntime().exec( "cscript " +vbPath+ " \""+sTitle+ "\"");
	
			Reporter.print(sTestName, sDesc);
			
		}  catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) {wbDeleteCache(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	@SuppressWarnings("null")
	public static void wbReadMail_ClickLink(String sTestName) throws IOException, InterruptedException, HeadlessException, AWTException {
		 String sDesc="", vbPath, arg; 
		 sDesc = Logs.log(sTestName) ;
		
		 
		 try{
		
			 	arg = System.getProperty("user.dir").replace("\\", "/") +"/Packages";
			 	File file = new File(arg+"/temp.txt");
			 	
			 	file.delete();

			 	Thread.sleep(30000);
			 
			 	vbPath = "\""+System.getProperty("user.dir").replace("\\", "/") +"/Packages/openExcelVB.vbs\"";
			 	Runtime.getRuntime().exec( "cscript " +vbPath+ " \""+arg+ "\"");
			 
			 	Thread.sleep(3000);
			 	int i=0;
			 	while(i<30)
			 		try {
			 			FileReader inputFile = new FileReader(arg+"/temp.txt");

			 			BufferedReader bufferReader = new BufferedReader(inputFile);
			 			String line ="";
			 			while ((line = bufferReader.readLine()) != null)   {
			 				line+=line;;
			 			}
			 			bufferReader.close();
			 			if(line.contains("Success"))
			 				Reporter.print(sTestName, sDesc +" :: Performed");
			 			else
			 				Reporter.print(sTestName, sDesc + " ::Problem reading email");
			 		} catch (FileNotFoundException e)	{
			 				Thread.sleep(1000);
			 				i++;
			 		}
		 	} catch(Exception e) {
		 		if (Utils.handleIntermediateIssue()) {wbReadMail_ClickLink(sTestName); }
				Reporter.printError(sTestName, e, sDesc);
			
		 	}
	 }
	public static void wbDoubleClick(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException   {
		
		String sDesc; 
		
		sDesc = Logs.log(sTestName)+ " :: Double Click on Element: " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		Helper.checkReady(sTestName, oEle);
		
		try { 	
			String sBrowser = Utils.h2TestName_TestParams.get(sTestName).get("Exe_Platform");
			WebDriver lDriver = getWebDriver(sTestName);
			
			if(sBrowser.equalsIgnoreCase("IE")||sBrowser.equalsIgnoreCase("CHROME"))	{
				Actions action = new Actions(lDriver);
				action.moveToElement(oEle).doubleClick().build().perform();
			}
			else {
				String doubleClickJS = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('dblclick',"+"true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject)"+"{ arguments[0].fireEvent('ondblclick');}";
					JavascriptExecutor js = (JavascriptExecutor) lDriver;
					js.executeScript(doubleClickJS, oEle);
					sDesc = sDesc + "\n::Note: Special Handling using javascript Executor::\n";
			
			}
			Reporter.print(sTestName, sDesc);
			
		}   catch (Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbDoubleClick(sTestName,oEle,sObjStr); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	
	
	
	public static void wbWait(String sTestName, String secs) throws NumberFormatException, InterruptedException, HeadlessException, IOException, AWTException, InterruptedException, InvalidInputException  {
		
		secs = Utils.Helper.validateUserInput(sTestName, secs);
		String sDesc = Logs.log(sTestName)+" :: Waiting for: ("+secs+")seconds";
		Thread.sleep(Long.valueOf(secs)*1000);
		Reporter.print(sTestName, sDesc);
	}
	
	public static void wbWaitForPageToLoad(String sTestName, String WaitTimeSec) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = "";	
		
		try {
			
			WaitTimeSec = Utils.Helper.validateUserInput(sTestName, WaitTimeSec);
			sDesc = Logs.log(sTestName)+" :: Waiting for page To Load: ("+WaitTimeSec+")seconds";
			long sec = Long.parseLong(WaitTimeSec);
			
			WebDriver lDriver = getWebDriver(sTestName);
			
			WebDriverWait wait = new WebDriverWait(lDriver, sec);
					wait.until(new ExpectedCondition<Boolean>() {
						public Boolean apply(WebDriver lDriver) {
							return ((JavascriptExecutor) lDriver).executeScript("return document.readyState").equals("complete");
						}
					});
						
			Reporter.print(sTestName, sDesc);
				
		} 	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbWaitForPageToLoad(sTestName, WaitTimeSec); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	
	
	public static void wbWaitForElementPresent(String sTestName, WebElement oEle,  String sObjStr, String WaitTimeSec) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc = "" ;
		
		sDesc =  Logs.log(sTestName)+" :: Waiting for Element : " +sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
				
		try {
			
			WaitTimeSec = Utils.Helper.validateUserInput(sTestName, WaitTimeSec);
			long sec = Long.parseLong(WaitTimeSec);
			Helper.checkReady(sTestName, oEle);
			
			WebDriver lDriver = getWebDriver(sTestName);
			WebDriverWait wait = new WebDriverWait(lDriver, sec);
			wait.until(ExpectedConditions.visibilityOf(oEle));
			
			Reporter.print(sTestName, sDesc+ " to be Present: ("+WaitTimeSec+")seconds");
		
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbWaitForElementPresent(sTestName,oEle,sObjStr, WaitTimeSec); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}

	public static boolean wbVerify_SubStringInText(String sTestName, WebElement oEle, String sObjStr, String sExpText) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException {
		
		String sDesc, sActVal = null, sExpVal; boolean bStatus = false;
		
		sDesc = Logs.log(sTestName)+ " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try {
			
			sExpText = Utils.Helper.validateUserInput(sTestName, sExpText);
			sExpVal = Reporter.filterUserInput(sExpText);	
			Helper.checkReady(sTestName, oEle);
			
			sActVal = oEle.getText();
			sActVal = sActVal.replace("\n", " ");
			bStatus = (sActVal.contains(sExpVal));	
			
			Reporter.print(sTestName, sExpText, sDesc + " :: Check the contents '" + sExpText + "'", sExpVal, sActVal, bStatus, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbVerify_SubStringInText(sTestName,oEle,sObjStr, sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
		return bStatus;
	}
	
	public static void wbAcceptAlert(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = Logs.log(sTestName)+" :: Clicking on 'OK' of Popup";
			
		try {
			WebDriver lDriver = getWebDriver(sTestName);
			Alert alert = lDriver.switchTo().alert();	
			alert.accept();
			
			Reporter.print(sTestName, sDesc);
		   
		}   catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbAcceptAlert(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	
	public static void wbCancelAlert(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = Logs.log(sTestName)+" :: Clicking on 'CANCEL' of Popup";
		Helper.checkReady(sTestName, null);
		
		try {
			WebDriver lDriver = getWebDriver(sTestName);
			Alert alert = lDriver.switchTo().alert();							
		    alert.dismiss();
		    
		    Reporter.print(sTestName, sDesc);
				   
		}   catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbCancelAlert(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	
	public static void wbGoForward(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = Logs.log(sTestName)+" :: Moving Forward One Page";
		Helper.checkReady(sTestName, null);
		
		try{
			WebDriver lDriver = getWebDriver(sTestName);
		    lDriver.navigate().forward();
		    
		    Reporter.print(sTestName,sDesc);
	
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbGoForward(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	
	public static boolean wbVerify_ToolTip(String sTestName, WebElement oEle, String sObjStr, String sExpText) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc, sActVal = null, sExpVal; boolean bStatus = false;
		
		sDesc = Logs.log(sTestName)+ " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try {
			
			sExpText = Utils.Helper.validateUserInput(sTestName, sExpText);
			sExpVal = Reporter.filterUserInput(sExpText);
			Helper.checkReady(sTestName, oEle);
			
			sActVal = oEle.getAttribute("title").replace("\n", " "); 
			bStatus = sExpVal.equals(sActVal);
		
		    Reporter.print(sTestName, sExpText, sDesc + " :: Verifying ToolTip Text:'" + sExpText + "'", sExpVal, sActVal, bStatus);
			
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbVerify_ToolTip(sTestName, oEle, sObjStr, sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	    return bStatus;
	}
	
	public static boolean wbVerifyIn_CSSValue(String sTestName, WebElement oEle, String sObjStr, String sAttribute,String sExpVal)throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException {
		
		String sDesc, sActVal = null, sVal=sAttribute; boolean bStatus = false;
		
		sDesc = Logs.log(sTestName)+ " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
			
		try {
			sAttribute = Utils.Helper.validateUserInput(sTestName, sAttribute);
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			
			sAttribute = Reporter.filterUserInput(sAttribute);
			Helper.checkReady(sTestName, oEle);

			sActVal = oEle.getCssValue(sAttribute);
			bStatus = sActVal.equals(sExpVal);
			
			Reporter.print(sTestName, sVal, sDesc + " :: Verifying Value in CSS:'" + sVal + "'" , sExpVal, sActVal, bStatus, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
			
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbVerifyIn_CSSValue(sTestName, oEle, sObjStr, sVal,sExpVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	    return bStatus;
	}	
	
	public static void wbStore_CSSValue(String sTestName, WebElement oEle, String sObjStr, String sComputedVal,String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  { 
		
		String sDesc, sActVal; 
		sDesc = Logs.log(sTestName) + "GetCSS : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
			
		try {
			
			sComputedVal = Utils.Helper.validateUserInput(sTestName, sComputedVal);
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);

			Helper.checkReady(sTestName, oEle);
			
			if (sVarName.equals("")) {
				sVarName="Temp";
			} 
			
			sActVal = oEle.getCssValue(sComputedVal);
			Utils.setScriptParams(sTestName, sVarName, sActVal);
			
			Reporter.print(sTestName,sDesc + ", Type:("+sComputedVal+"), Got Value:("+sActVal+")");
		
		}	catch(Exception e) {
			
			if (Utils.handleIntermediateIssue()) { wbStore_CSSValue(sTestName,oEle,sObjStr,sComputedVal,sVarName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
    }
	
	public static void wbStore_Title(String sTestName, String sUserVal) throws HeadlessException, IOException, AWTException, InterruptedException  { 
		
		String sDesc, sActVal, sVarName; 
		sDesc = Logs.log(sTestName);
		Helper.checkReady(sTestName, null);
		
		try {	
			sUserVal = Utils.Helper.validateUserInput(sTestName, sUserVal);
			
			WebDriver lDriver = getWebDriver(sTestName);	
			sVarName = (sUserVal.equals("")) ? "Temp" : sUserVal;

			sActVal =lDriver.getTitle();
			Utils.setScriptParams(sTestName, sVarName, sActVal);
			
			Reporter.print(sTestName, sDesc + " :: Store (Current Title -" + sActVal + ") in Local Variable '" + sVarName + "'");
				
		} 	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbStore_Title(sTestName,sUserVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}				
    }

	public static void wbClick_OnText(String sTestName, String sText) throws HeadlessException, IOException, AWTException, InterruptedException 	{
		
		String sDesc = Logs.log(sTestName)+" :: Click on Text: "+ sText;
		boolean foundFlg = false;
		Helper.checkReady(sTestName, null);
		
		try {
			
			sText = Utils.Helper.validateUserInput(sTestName, sText);
			
			WebDriver lDriver = getWebDriver(sTestName);	
			List<WebElement> childEle = lDriver.findElements(By.tagName("*"));
			for(WebElement oEle : childEle){	
				if(oEle.isDisplayed()) {
					if (oEle.getAttribute("innerHTML").contains(sText)) {
						oEle.click(); foundFlg = true; break;
				    }
			    }
			}
			sDesc=Logs.log(sTestName)+" :: Click on Text: "+ sText + ", found " + String.valueOf(foundFlg);
			
		} catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbClick_OnText(sTestName, sText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	public static void wbEnter(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException {
		String sDesc = "" ;  
		
		try {
			
			sDesc = Logs.log(sTestName) + " in Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
			Helper.checkReady(sTestName, oEle);
			
			oEle.sendKeys(Keys.ENTER);
			Reporter.print(sTestName, sDesc + " :: Performed");
		
		} catch(Exception e) {
			
			if (Utils.handleIntermediateIssue()) { wbEnter(sTestName, oEle, sObjStr); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	public static void wbStore_FontColor(String sTestName, WebElement oEle, String sObjStr, String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc, sActVal; 
		
		sDesc = Logs.log(sTestName)+" :: Reterving Font color of Obj : "+sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		Helper.checkReady(sTestName, oEle);
		
		try { 
			
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			sActVal = oEle.getCssValue("color");
			String[] numbers = sActVal.replace("rgb(", "").replace(")", "").split(",");
			int iR = Integer.parseInt(numbers[0].trim()); int iG = Integer.parseInt(numbers[1].trim()); int iB = Integer.parseInt(numbers[2].trim());
			sActVal = String.format("#%02x%02x%02x", iR, iG, iB).toUpperCase();
			
			sVarName = (sVarName.equals("")) ? "Temp" : sVarName;

			Utils.setScriptParams(sTestName, sVarName, sActVal);
			Reporter.print(sTestName, sDesc + " :: Store '" + sActVal + "' of "+ sObjStr + " in Local Variable '" + sVarName + "'");
		
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbStore_FontColor(sTestName,oEle,sObjStr,sVarName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	
	public static void wbVerifyIn_Error(String sTestName, String sExpText) throws Exception {	
		
		String sDesc = "";
		
		try {
			
			sExpText = Utils.Helper.validateUserInput(sTestName, sExpText);
			sDesc = Logs.log(sTestName) + " :: Validating error message( " + sExpText + " )";
			Helper.checkReady(sTestName, null);
			
			WebDriver lDriver = getWebDriver(sTestName);
		    WebElement oEle = (WebElement) lDriver.findElement(By.xpath("//*[@id='errorDiv']"));
		    wbVerify_SubStringInText(sTestName, oEle, "errObj", sExpText);
			    
			Reporter.print(sTestName, sDesc);		     
		 
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbVerifyIn_Error(sTestName,sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	public static void wbOpenIn_NewWindow(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  { 
		
		String sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		Helper.checkReady(sTestName, oEle);
		
		try{
			WebDriver lDriver = getWebDriver(sTestName);
			Utils.setScriptParams(sTestName, "Parent_Brwsr", lDriver.getWindowHandle());
			String sBrowser = Utils.h2TestName_TestParams.get(sTestName).get("Exe_Platform");
			if(sBrowser.equalsIgnoreCase("IE")||sBrowser.equalsIgnoreCase("CHROME"))
			{
			    Actions newTab = new Actions(lDriver);
			    newTab.moveToElement(oEle); 
			    newTab.perform();
			    newTab.keyDown(Keys.SHIFT).click(oEle).build().perform();
			}
			else
			{
				new Robot().keyPress(KeyEvent.VK_SHIFT);
				Thread.sleep(10000); 
				oEle.click();
				new Robot().keyRelease(KeyEvent.VK_SHIFT);
			}
			Reporter.print(sTestName,sDesc + " :: Performed");
			
		    for(String winHandle : lDriver.getWindowHandles()){
			    lDriver.switchTo().window(winHandle);			    
			}				
			Utils.setScriptParams(sTestName, "Child_Brwsr", lDriver.getWindowHandle());
			
		}	catch(Exception e) {
	
			if (Utils.handleIntermediateIssue()) { wbOpenIn_NewWindow(sTestName, oEle, sObjStr); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	
	public static void wbFocus(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  { 
		
		String sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		Helper.checkReady(sTestName, oEle);
		
		try{
			WebDriver lDriver = getWebDriver(sTestName);
		
			String sBrowser = Utils.h2TestName_TestParams.get(sTestName).get("Exe_Platform");
			
			if ("input".equals(oEle.getTagName())) {
				oEle.sendKeys("");
			}
			else if(sBrowser.equalsIgnoreCase("IE")||sBrowser.equalsIgnoreCase("CHROME")){
				Actions newTab = new Actions(lDriver);
				newTab.moveToElement(oEle); 
				newTab.perform();
			}
			else	{
				String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
				((JavascriptExecutor) lDriver).executeScript(mouseOverScript, oEle);	
				sDesc = sDesc + "\n::Note: Special Handling using javascript Executor::\n";
				
			} 
				
			
			
			Reporter.print(sTestName,sDesc + " :: Focussed on Obj : " + sObjStr + ")");
		 
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { wbFocus(sTestName,oEle,sObjStr); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	}
	public static boolean wbIs_Empty(String sTestName, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = "", sExpVal; boolean bStatus = false;
	
	    try { 	
	    	sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			sDesc = Logs.log(sTestName) + " :: Value - " + sVal;
			sExpVal = Reporter.filterUserInput(sVal);
			
		    bStatus = sExpVal.isEmpty();
			Reporter.print(sTestName, sVal, sDesc, "NA","NA", bStatus);
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbIs_Empty(sTestName, sVal); }
			Reporter.printError(sTestName, e, sDesc);
		}
		return bStatus;
	}
public static void wbClose(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc; 
		
		sDesc = Logs.log(sTestName) ;
		String sBrowser = Utils.h2TestName_TestParams.get(sTestName).get("Exe_Platform");
			
		try { 	
			
			WebDriver lDriver = getWebDriver(sTestName);
			if(sBrowser.equalsIgnoreCase("IE")||sBrowser.equalsIgnoreCase("CHROME"))
				lDriver.close();
			else
				lDriver.quit();

			Reporter.print(sTestName, sDesc + " :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbClose(sTestName); }
			Reporter.printError(sTestName, e, sDesc);
		}
	}


public static void wbClose(String sTestName,String sUsrVal) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc, sActVal = null; 
		sDesc = Logs.log(sTestName);
		int cnt=0;
		String sBrowser = Utils.h2TestName_TestParams.get(sTestName).get("Exe_Platform");
		
		try { 	
				sUsrVal = Utils.Helper.validateUserInput(sTestName, sUsrVal);
								
				if (sUsrVal.isEmpty())
					
					Web.wbClose(sTestName);
				
				else {
					Map<String,WebDriver> test_drs= h2TestName_WebDriver.get(sTestName);
				
					for (WebDriver lDriver : test_drs.values())	{
						
						for (String sWinHandle : lDriver.getWindowHandles())  {
							
							lDriver.switchTo().window(sWinHandle);	
							sActVal= lDriver.getTitle();
							if (sActVal.contains(sUsrVal)) {
								if(sBrowser.equalsIgnoreCase("IE")||sBrowser.equalsIgnoreCase("CHROME"))
									lDriver.close();
								else
									lDriver.quit();
								cnt++;	break;
							}
						}
						if (cnt>0) break;
					}
					if (cnt>0)
						Reporter.print(sTestName, sDesc + ":: Title : "+sUsrVal+" :: Performed");
					else
						Reporter.print(sTestName, sDesc + " :: No such Title Found !!!");
				}
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbClose(sTestName, sUsrVal); }
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	
	
	
	public static void wbMouseOver(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  { 
		
		String sDesc ; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		Helper.checkReady(sTestName, oEle);
		
		try {
			String sBrowser = Utils.h2TestName_TestParams.get(sTestName).get("Exe_Platform");
			WebDriver lDriver = getWebDriver(sTestName);
			
			if (sBrowser.equalsIgnoreCase("IE")||sBrowser.equalsIgnoreCase("CHROME")) {
				Actions newTab = new Actions(lDriver);
				newTab.moveToElement(oEle); 
				newTab.perform();
			}
			else {
				String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
				((JavascriptExecutor) lDriver).executeScript(mouseOverScript, oEle);	
				sDesc = sDesc + "\n::Note: Special Handling using javascript Executor::\n";
				
			
			}
			
				
		    Reporter.print(sTestName,sDesc + " :: Performed");		
		
		} catch(Exception e) {
			
			if (Utils.handleIntermediateIssue()) { wbMouseOver(sTestName, oEle, sObjStr);; }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	 }
	
	public static void wbMouseClick(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  { 
		String sDesc = "";
				
		sDesc = Logs.log(sTestName) + " on Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		try{
		
			String sBrowser = Utils.h2TestName_TestParams.get(sTestName).get("Exe_Platform");
			WebDriver lDriver = getWebDriver(sTestName);
		
			if (sBrowser.equalsIgnoreCase("IE")||sBrowser.equalsIgnoreCase("CHROME")) {
				try {
					Actions newTab = new Actions(lDriver);
					newTab.moveToElement(oEle); 
					newTab.perform();
					newTab.click(oEle).build().perform();
				} catch (Exception e) {
					oEle.click();
				}
			}
			else
				oEle.click();
		
			Reporter.print(sTestName, sDesc + " :: Performed");
		} catch(Exception e) {
			
			if (Utils.handleIntermediateIssue()) { wbMouseClick(sTestName, oEle, sObjStr); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	 }

	public static void wbScreen_Capture(String sTestName , String sUsrVal) throws HeadlessException, IOException, AWTException, InterruptedException  { 
		
		String sDesc = "" ; 
		
		try {
			sUsrVal = Utils.Helper.validateUserInput(sTestName, sUsrVal);
			sDesc = Logs.log(sTestName) + " - " + sUsrVal ;
			Helper.checkReady(sTestName, null);
			Reporter.print(sTestName, sDesc, Helper.takeFullScreenshot(Web.getWebDriver(sTestName)));
		
		} catch(Exception e) {
			
			if (Utils.handleIntermediateIssue()) { wbScreen_Capture(sTestName, sUsrVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}
	 }
	public static void wbGet_Cell_Data(String sTestName, WebElement oEle, String sObjStr, String sRowCol,String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException {
		
		String sDesc = "", sActVal ="", sColumn = "", sRow="" , flagTh = "false",sVal=sRowCol;  
		int sColumnNmbr, sRowNmbr,i=1;
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try { 	
			
			sRowCol = Utils.Helper.validateUserInput(sTestName, sRowCol);
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			sRowCol = Reporter.filterUserInput(sRowCol);
			Helper.checkReady(sTestName, oEle);
			
			if (sRowCol.contains("::")) {
				sColumn = sRowCol.split("::")[1];
				sRow = sRowCol.split("::")[0];
			}
			else {
				Reporter.print(sTestName, sVal + "*EXIT_ON_FAIL*", sDesc +"  : Please provide input correctly", "True", "False", false);
			}
			if(sVarName.equals(""))
			{
				sVarName="Temp";
			}
			
			if (sColumn.equalsIgnoreCase("ALL")) 
				sColumnNmbr = -1;
			else
				sColumnNmbr = Integer.parseInt(sColumn);
			if (sRow.equalsIgnoreCase("ALL")) {
				sRowNmbr = -1 ;
			} else 
				sRowNmbr = Integer.parseInt(sRow);
				
			List<WebElement> rows = oEle.findElements(By.tagName("tr"));
			
			for(WebElement row : rows) {
				List<WebElement> cols = null;
				String htmlSource = row.getAttribute("innerHTML");
				if (htmlSource.contains("<th") ) {
					if (flagTh.equalsIgnoreCase("true")) {
						continue;
					}
					else {
						cols = row.findElements(By.tagName("th"));
						flagTh = "true";
					}
					
				}
				else {
					cols = row.findElements(By.tagName("td"));
				}
				if (sColumnNmbr==-1 && sRowNmbr==-1) { 			//All rows All Columns
						sActVal = sActVal + "\nRow No." + i + ":";
							for (int j=0;j<cols.size();j++)
								sActVal = sActVal + cols.get(j).getText() +"|";  	
					} 
				else {
						if (sColumnNmbr==-1 && sRowNmbr!=-1) {		//Row Number but All Columns
							if (i==sRowNmbr) {
								sActVal = sActVal + "Row No." + i + ":";
								for(int j=0;j<cols.size();j++)
									sActVal = sActVal + cols.get(j).getText() +"|";  
								break;
							}
						}
						else {
							if (sRowNmbr==-1 && sColumnNmbr!=-1) 	//All rows But Column Number
								sActVal = sActVal + cols.get(sColumnNmbr-1).getText() +"|";
							else if(i==sRowNmbr) {					//Row Number Column Number
								sActVal = cols.get(sColumnNmbr-1).getText();
								break;
							}
						}
				}
					i++;
			}
			Utils.setScriptParams(sTestName, sVarName, sActVal);
			Reporter.print(sTestName, sDesc + " :: Store Cell Data of Row: '" + sRow + "' and Column: '" + sColumn + "' in Local Variable '" + sVarName + "', Data is: '" + sActVal + "'");
			
		} catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) {  wbGet_Cell_Data(sTestName, oEle, sObjStr, sVal,sVarName);}
	    	Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		
		}
	}
	
	public static void wbGet_RowWith_CellData(String sTestName, WebElement oEle, String sObjStr, String sCol,String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException {
		
		String sDesc, sActVal ="", sUsrVal = "", sResult = "", flagTh = "false",sVal=sCol; int sRequiredColumnNmbr = 0, sColumnNmbr = 0;
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try {
			sCol = Utils.Helper.validateUserInput(sTestName, sCol);
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			sCol = Reporter.filterUserInput(sCol);
			
			if (sCol.contains("=")) {
				sColumnNmbr = Integer.parseInt(sCol.split("=")[0]);
				sUsrVal = sCol.split("=")[1];
				sRequiredColumnNmbr = Integer.parseInt(sVarName.split("=")[0]);
				sVarName=sVarName.split("=")[1];
			}	
			else {
				Reporter.print(sTestName, sVal + "*EXIT_ON_FAIL*", sDesc +"Plese provide input correctly", "True", "False", false);
			}
				List<WebElement> rows = oEle.findElements(By.tagName("tr"));
				
				for (WebElement row : rows) {
					List<WebElement> cols = null;
					String htmlSource = row.getAttribute("innerHTML");
					if (htmlSource.contains("<th") ) {
						if (flagTh.equalsIgnoreCase("true")) {
							continue;
						}
						else {
							cols = row.findElements(By.tagName("th"));
							flagTh = "true";
						}
						
					}
					else {
						cols = row.findElements(By.tagName("td"));
					}
						sActVal = cols.get(sColumnNmbr-1).getText();
						if(sActVal.equalsIgnoreCase(sUsrVal)) {
							sResult = cols.get(sRequiredColumnNmbr-1).getText();
							break;
						}
					
					}
				
				Utils.setScriptParams(sTestName, sVarName, sResult);
				Reporter.print(sTestName, sDesc + " :: Store Value of Column: '"+ sRequiredColumnNmbr + "' where Column '" + sColumnNmbr + "' has Value: '" + sUsrVal + "' in Local Variable '" + sVarName + "' Data is: '" + sResult + "'");
				
			} catch(Exception e) {
				
			    	if (Utils.handleIntermediateIssue()) { wbGet_RowWith_CellData(sTestName, oEle, sObjStr, sVal,sVarName);}
			    	Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
					
			}
	
	}
	public static void wbVerify_Cell_Data(String sTestName, WebElement oEle, String sObjStr,String sRowCol,String sExpVal) throws HeadlessException, IOException, AWTException, ClassNotFoundException, InterruptedException {
		
		String sDesc = null,sVal=sRowCol, sActVal ="", sColumn = null, sRow="", flagTh = "false"; boolean bStatus = false; int sColumnNmbr = 0, sRowNmbr,i=1;
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try { 	
			 
		
			sRowCol = Utils.Helper.validateUserInput(sTestName, sRowCol);
			sExpVal=Utils.Helper.validateUserInput(sTestName, sExpVal);
			sRowCol = Reporter.filterUserInput(sRowCol);
			
			Helper.checkReady(sTestName, oEle);
			

			if (sRowCol.contains("::")) {
				sColumn = sRowCol.split("::")[1];
				sRow = sRowCol.split("::")[0];
			}
			else {
				Reporter.print(sTestName, sVal + "*EXIT_ON_FAIL*", sDesc +"Plese provide input correctly", "True", "False", false);
			}
			
			if (sColumn.equalsIgnoreCase("ALL")) 
				sColumnNmbr = -1;
			else
				sColumnNmbr = Integer.parseInt(sColumn);
			if (sRow.equalsIgnoreCase("ALL")) {
				sRowNmbr = -1 ;
			} else 
				sRowNmbr = Integer.parseInt(sRow);
			
			List<WebElement> rows = oEle.findElements(By.tagName("tr"));
			for (WebElement row : rows) {
				List<WebElement> cols = null;
				String htmlSource = row.getAttribute("innerHTML");
				if (htmlSource.contains("<th") ) {
					if (flagTh.equalsIgnoreCase("true")) {
						continue;
					}
					else {
						cols = row.findElements(By.tagName("th"));
						flagTh = "true";
					}
					
				}
				else {
					cols = row.findElements(By.tagName("td"));
				}
					if (sColumnNmbr==-1 && sRowNmbr==-1) {
						for(int j=0;j<cols.size();j++) {
							sActVal =  sActVal +cols.get(j).getText() + "|";
							if(sActVal.contains(sExpVal)) {
								bStatus = true;
								break;
							}
						}
					} 
					else {
						if(sColumnNmbr==-1 && sRowNmbr!=-1) {
							if(i==sRowNmbr) {
								for(int j=0;j<cols.size();j++) {
									sActVal =  sActVal +cols.get(j).getText()+ "|";
									if(sActVal.contains(sExpVal)) {
										bStatus = true;
										break;
									}
								}
							}
						}
						else {
							if(sRowNmbr==-1 && sColumnNmbr!=-1) { 
								sActVal =  sActVal + cols.get(sColumnNmbr-1).getText()+ "|";
								if(sActVal.contains(sExpVal)) {
									bStatus = true;
									break;
								}
							}
							else if(i==sRowNmbr) {
								sActVal = cols.get(sColumnNmbr-1).getText();
								if(sActVal.equalsIgnoreCase(sExpVal)) {
									bStatus = true;
									break;
								}
							}
						}
					}
					i++;
				
			}
			Reporter.print(sTestName, sVal, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(Web.getWebDriver(sTestName)));	
			
		} catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { wbVerify_Cell_Data(sTestName, oEle, sObjStr,sVal,sExpVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
			
		}
	}
	public static void wbVerify_RowWith_CellData(String sTestName, WebElement oEle, String sObjStr, String sCol,String sExpVal) throws HeadlessException, IOException, AWTException, ClassNotFoundException, InterruptedException {
		
		String sDesc = "", sActVal ="",sVal=sCol, sResult = "", sColumnVal = "", flagTh = "false"; boolean bStatus = false; int sRequiredColumnNmbr = 0, sColumnNmbr = 0;
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
	
	try {
		
		sCol = Utils.Helper.validateUserInput(sTestName, sCol);
		sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
		sCol = Reporter.filterUserInput(sCol);
		
		if (sCol.contains("=")) {
			sColumnNmbr = Integer.parseInt(sCol.split("=")[0]);
			sColumnVal = sCol.split("=")[1];
			sRequiredColumnNmbr = Integer.parseInt(sExpVal.split("=")[0]);
			sExpVal = sExpVal.split("=")[1];
		}	
		else {
			Reporter.print(sTestName, sVal + "*EXIT_ON_FAIL*", sDesc +"Plese provide input correctly", "True", "False", false);
		}
		
			List<WebElement> rows = oEle.findElements(By.tagName("tr"));
			
			for(WebElement row : rows) {
				List<WebElement> cols = null;
				String htmlSource = row.getAttribute("innerHTML");
				if (htmlSource.contains("<th") ) {
					if (flagTh.equalsIgnoreCase("true")) {
						continue;
					}
					else {
					cols = row.findElements(By.tagName("th"));
					flagTh = "true";
					}
					
				}
				else {
					cols = row.findElements(By.tagName("td"));
				}
					sActVal = cols.get(sColumnNmbr-1).getText();
					if(sActVal.equalsIgnoreCase(sColumnVal)) {
						
						sResult = cols.get(sRequiredColumnNmbr-1).getText();
						if(sResult.equalsIgnoreCase(sExpVal))
							bStatus = true;
							break;
					}
			}
			Reporter.print(sTestName, sVal, sDesc, sExpVal, sResult, bStatus, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
			
		} catch(Exception e) {
			
		    if (Utils.handleIntermediateIssue()) { wbVerify_RowWith_CellData(sTestName, oEle, sObjStr, sVal,sExpVal);}
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}	
	
}	
	public static void wbRightClick_SelectOption(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, ClassNotFoundException, InterruptedException {
		
		String sDesc = ""; int sOption = 0;
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " ) , Select Option No. " + sVal ;
	
		try {
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			
			sOption = Integer.parseInt(sVal);
			WebDriver lDriver = getWebDriver(sTestName);
			Actions action = new Actions(lDriver);
			
			for (int i=0 ; i<sOption ; i++) 
				action.contextClick(oEle).sendKeys(Keys.ARROW_DOWN).build().perform();
		
			action.contextClick(oEle).sendKeys(Keys.ENTER).build().perform();
	
			
			Reporter.print(sTestName, sDesc + " :: Performed");
		
		} catch(Exception e) {
			
		    if (Utils.handleIntermediateIssue()) { wbRightClick_SelectOption(sTestName, oEle, sObjStr, sVal);}
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Web.getWebDriver(sTestName)));
		}	
			
	}
	
	
	
	private static class Helper {
		private static String takeScreenshot(WebDriver lDriver) throws IOException, HeadlessException, AWTException  { 
			try {
				lDriver = new Augmenter().augment(lDriver);
				String screenshotBase64 = ((TakesScreenshot)lDriver).getScreenshotAs(OutputType.BASE64);
				return screenshotBase64;
				
			} catch (Exception e) {
				throw e;	
			}
			
		}
		
		private static String takeFullScreenshot(WebDriver driver) throws IOException, HeadlessException, AWTException  { 
			
			try {
				Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
				
				String sBrowser = cap.getBrowserName();
				
				if(sBrowser.equalsIgnoreCase("IE")||sBrowser.equalsIgnoreCase("internet explorer")) {
					String screenshotBase64 = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BASE64);
					return screenshotBase64;
				}
				else {
					
						BufferedImage screenshotBase64 = new AShot().shootingStrategy(new ViewportPastingStrategy(1000)).takeScreenshot(driver).getImage();
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						
						ImageIO.write(screenshotBase64, "png", Base64.getEncoder().wrap(os));	
						return os.toString(StandardCharsets.ISO_8859_1.name());	
				}
				
			} catch (Exception e) {
				throw e;
			}
		}
			
	@SuppressWarnings("deprecation")
	private static  WebDriver launchFirefox(String sTestName) throws Exception {
		if (!Utils.hEnvParams.containsKey("FIREFOX_PATH"))
			throw new InvalidInputException("You have Not Provided FIREFOX_PATH in Configuration Parameters");
		WebDriver driver = null; String errMsg = "";
	
		try { 
			if (!SeleniumGrid.isWindow64())
				errMsg += "Selenium 3 only Supports 64- bit Windows For Firefox Browser";
			
			if (!errMsg.isEmpty()) 
				throw new Exception(errMsg);  
		
			errMsg += checkFirefoxVersion();
			if (!errMsg.isEmpty()) 
				throw new Exception(errMsg);  
			
			DesiredCapabilities cap = DesiredCapabilities.firefox();
			FirefoxProfile prof = new FirefoxProfile();
			
			if (h2FirefoxPrefs.containsKey(sTestName)) {
				if (h2FirefoxPrefs.get(sTestName).containsKey("Popup_Allow")) 
						
					prof.setPreference("dom.disable_open_during_load", false);
				
				
				if (h2FirefoxPrefs.get(sTestName).containsKey("Clean_Session")) { 
					
					prof.setPreference("browser.cache.disk.enable", false);
					prof.setPreference("browser.cache.memory.enable", false);
					prof.setPreference("browser.cache.offline.enable", false);
					prof.setPreference("network.http.use-cache", false);
				}
				
				if (h2FirefoxPrefs.get(sTestName).containsKey("Add_On")) 
					
					prof.addExtension(new File(h2FirefoxPrefs.get(sTestName).get("Add_On")));
			    
				if (h2FirefoxPrefs.get(sTestName).containsKey("Accept_SSL")) {
					
					prof.setAcceptUntrustedCertificates(true);
					prof.setAssumeUntrustedCertificateIssuer(false);
				}
				
				if (h2FirefoxPrefs.get(sTestName).containsKey("Alert_Handle")) {
					
					if (h2FirefoxPrefs.get(sTestName).get("Alert_Handle").equalsIgnoreCase("Accept")) 
						cap.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR ,UnexpectedAlertBehaviour.ACCEPT);	
					
					else if (h2FirefoxPrefs.get(sTestName).get("Alert_Handle").equalsIgnoreCase("Dismiss"))
						cap.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR ,UnexpectedAlertBehaviour.DISMISS);
					
					else 
						cap.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR ,UnexpectedAlertBehaviour.IGNORE);
				}
			}					
			
			cap.setCapability(FirefoxDriver.PROFILE, prof);
			
			if( h2FirefoxPrefs.containsKey(sTestName) && h2FirefoxPrefs.get(sTestName).containsKey("FireFox_Proxy")) {
					
				String aHost = h2FirefoxPrefs.get(sTestName).get("FireFox_Proxy").split(":")[0];
				Number aPort = Integer.parseInt(h2FirefoxPrefs.get(sTestName).get("FireFox_Proxy").split(":")[1]);
				GeckoDriverService service = null;
				FirefoxBinary firefoxBinary = new FirefoxBinary(new File( Utils.hEnvParams.get("FIREFOX_PATH")));
				service = new GeckoDriverService.Builder(firefoxBinary)
				          .usingDriverExecutable(new File(Utils.hSystemSettings.get("packageFolder") + "geckodriver.exe"))
				          .usingAnyFreePort().usingAnyFreePort().build();
				service.start();      
						
				DesiredCapabilities required = new DesiredCapabilities();

				JsonObject json = new JsonObject();
				json.addProperty("proxyType", "MANUAL"); json.addProperty("httpProxy", aHost);
				json.addProperty("httpProxyPort",aPort);json.addProperty("sslProxy", aHost);
				json.addProperty("sslProxyPort", aPort);
				required.setCapability("proxy", json);
			
			//    driver = new FirefoxDriver(service, cap, required);
				driver = new FirefoxDriver(cap, required);
			}
			
			else {
				
				System.setProperty("webdriver.gecko.driver", Utils.hSystemSettings.get("packageFolder") + "geckodriver.exe");
				cap.setBrowserName("firefox");
				URL sURL= null;
				
				if (Utils.h2TestName_TestParams.get(sTestName).get("Run_on").toString().equalsIgnoreCase("localhost")) {
					cap.setCapability("firefox_binary",  Utils.hEnvParams.get("FIREFOX_PATH"));   // tdo - set pref
					sURL = new URL("http://localhost:5555/wd/hub"); 
				}	
				else {
					
					cap.setCapability("firefox_binary",  Utils.hRemoteIps.get(Utils.h2TestName_TestParams.get(sTestName).get("Run_on")));   // tdo - set pref
					sURL = new URL("http://"+Utils.h2TestName_TestParams.get(sTestName).get("Run_on")+":5555/wd/hub");
				}
				driver = new RemoteWebDriver(sURL, cap);
			}

		} catch (Exception e) {
			throw e;
		}
		return driver;
	
	}
	
	  private static  WebDriver launchIE(String sTestName) throws Exception {
		
		WebDriver driver = null; String errMsg = "";
		
		try { 
			DesiredCapabilities cap = DesiredCapabilities.internetExplorer();
			cap.setBrowserName("internet explorer");
			
			if (h2IEPrefs.containsKey(sTestName)) {
				
				if (h2IEPrefs.get(sTestName).containsKey("Popup_Allow"))  
				
					cap.setCapability("browserstack.ie.enablePopups", "true");
				
				if (h2IEPrefs.get(sTestName).containsKey("Ignore_Zoom")) 
					
					cap.setCapability("ignoreZoomSetting", true);
				
				if (h2IEPrefs.get(sTestName).containsKey("Element_Cache_Clean"))  
					
					cap.setCapability(InternetExplorerDriver.ENABLE_ELEMENT_CACHE_CLEANUP, true);
				
				if (h2IEPrefs.get(sTestName).containsKey("Browser_Attach_Timeout"))  
					
					cap.setCapability(InternetExplorerDriver.BROWSER_ATTACH_TIMEOUT, Integer.parseInt(h2IEPrefs.get(sTestName).get("Browser_Attach_Timeout")));
				
				if (h2IEPrefs.get(sTestName).containsKey("Clean_Session"))  
					
					cap.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
				
				if (h2IEPrefs.get(sTestName).containsKey("Ignore_ProtectedMode"))
					
					cap.setCapability("ignoreProtectedModeSettings" , true);
					cap.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
							
				if (h2IEPrefs.get(sTestName).containsKey("Accept_SSL")) 
						
					cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
					
					/* if above code doesnt work try this after opening the driver
					 * driver.navigate().to("javascript:document.getElementById('overridelink').click()");
					 */	
				
				if (h2IEPrefs.get(sTestName).containsKey("Alert_Handle")) {
					
					if (h2IEPrefs.get(sTestName).get("Alert_Handle").equalsIgnoreCase("Accept")) 
						cap.setCapability("unexpectedAlertBehaviour", "accept");	
						
					else if (h2IEPrefs.get(sTestName).get("Alert_Handle").equalsIgnoreCase("Dismiss"))
						cap.setCapability("unexpectedAlertBehaviour", "dismiss");
					
					else 
						cap.setCapability("unexpectedAlertBehaviour", "ignore");
				}
						
				
				//When a proxy is specified using the proxy capability, this capability sets the proxy settings on a per-process basis when set to true.
				if (h2IEPrefs.get(sTestName).containsKey("Process_Proxy"))  
					
					cap.setCapability(InternetExplorerDriver.IE_USE_PRE_PROCESS_PROXY, true);
				
				if (h2IEPrefs.get(sTestName).containsKey("IE_Proxy")) {
					
					Proxy proxy = new Proxy();
					proxy.setHttpProxy(h2IEPrefs.get(sTestName).get("IE_Proxy"));
			        proxy.setFtpProxy(h2IEPrefs.get(sTestName).get("IE_Proxy"));
			        proxy.setSslProxy(h2IEPrefs.get(sTestName).get("IE_Proxy"));
	
					cap.setCapability(CapabilityType.PROXY, proxy);
							
				}
			}
			URL sURL = null;
			if (Utils.h2TestName_TestParams.get(sTestName).get("Run_on").toString().equalsIgnoreCase("localhost"))
				 sURL = new URL("http://localhost:5558/wd/hub");
			else
				 sURL = new URL("http://"+Utils.h2TestName_TestParams.get(sTestName).get("Run_on")+":5558/wd/hub");
			driver = new RemoteWebDriver(sURL, cap);
			errMsg += checkIEVersion(driver);
			if (!errMsg.isEmpty()) 
				throw new Exception(errMsg); 
		
		} catch (Exception e) {
			throw e;
		}
		return driver;
	
	}
	 
	  private static WebDriver launchChrome(String sTestName) throws Exception {
		
		WebDriver driver = null; String errMsg = "";
		
		try { 
			
			ChromeOptions options = new ChromeOptions();
			DesiredCapabilities cap = DesiredCapabilities.chrome();
			cap.setBrowserName("chrome");
			
			if (h2ChromePrefs.containsKey(sTestName)) {
				
				if (h2ChromePrefs.get(sTestName).containsKey("Clean_Session"))  
					
					cap.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
				
				if (h2ChromePrefs.get(sTestName).containsKey("Add_On")) 
					
					options.addExtensions(new File(h2ChromePrefs.get(sTestName).get("Add_On")));
			    
				if (h2ChromePrefs.get(sTestName).containsKey("Maximize")) 
					
					options.addArguments("start-maximized");
				
				if (h2ChromePrefs.get(sTestName).containsKey("Accept_SSL")) 				
					
					cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
					
				if (h2ChromePrefs.get(sTestName).containsKey("Chrome_Binary")) 
							
					options.setBinary(h2ChromePrefs.get(sTestName).get("Chrome_Binary"));
				
				if (h2ChromePrefs.get(sTestName).containsKey("Alert_Handle")) {
					
					if (h2ChromePrefs.get(sTestName).get("Alert_Handle").equalsIgnoreCase("Accept")) 
						cap.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR ,UnexpectedAlertBehaviour.ACCEPT);	
					
					else if (h2ChromePrefs.get(sTestName).get("Alert_Handle").equalsIgnoreCase("Dismiss"))
						cap.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR ,UnexpectedAlertBehaviour.DISMISS);
					
					else 
						cap.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR ,UnexpectedAlertBehaviour.IGNORE);
				}
				
				if (h2ChromePrefs.get(sTestName).containsKey("Popup_Allow")) { 
					
					options.addArguments("test-type");
					options.addArguments("disable-popup-blocking");
		
				}
				
				if (h2ChromePrefs.get(sTestName).containsKey("Chrome_Proxy")) {
					
					Proxy proxy = new Proxy();
					proxy.setHttpProxy(h2ChromePrefs.get(sTestName).get("Chrome_Proxy"));
					proxy.setFtpProxy(h2ChromePrefs.get(sTestName).get("Chrome_Proxy"));
				    proxy.setSslProxy(h2ChromePrefs.get(sTestName).get("Chrome_Proxy"));
	
					cap.setCapability("proxy", proxy);
				/* if above code doesnt work try this
				 * options.addArguments("--proxy-server=proxy.com:8080");
				 */
				}
	
			}
			
			cap.setCapability(ChromeOptions.CAPABILITY, options);	
			URL sURL = null;
			options.addArguments("start-maximized");
			if (Utils.h2TestName_TestParams.get(sTestName).get("Run_on").toString().equalsIgnoreCase("localhost"))
				 sURL = new URL("http://localhost:5559/wd/hub"); 
			else
				 sURL = new URL("http://"+Utils.h2TestName_TestParams.get(sTestName).get("Run_on")+":5559/wd/hub");
			
			
			driver = new RemoteWebDriver(sURL, cap);
			errMsg += checkChromeVersion(driver);
			
			if (!errMsg.isEmpty()) 
				throw new Exception(errMsg); 
		
		} catch (Exception e) {
			throw e;
		}
		return driver;
	
	}
	
	
	  private static String checkFirefoxVersion() throws IOException, JSONException, ParseException, HeadlessException, AWTException {
	  		
	  		String errMsg = "";
	  		try {
	  			List<String> cmd= Arrays.asList("cmd.exe", "/k","\"" + Utils.hEnvParams.get("FIREFOX_PATH") + "\" -v|more");
			  	String inputRecieved = Utils.Helper.executeRunCommand(cmd, false);
	  			
	  			if (inputRecieved.contains("cannot find the path specified"))
	  				errMsg += "Your Firefox Path is not Correct";
	  			else {
		  			int version = Integer.parseInt(inputRecieved.replace(" ","").split("Firefox")[1].split("\\.")[0]);
					if (!(version>=50))
						errMsg += "FireFox version less than 48 is not supported";
	  			}
	  		} catch (Exception e) {
	
	  			System.out.println("Could Not Check Firefox Version."); 
	  		}
			return errMsg;
		}
	  	
	  	private static String checkChromeVersion(WebDriver driver) throws IOException, JSONException, ParseException, HeadlessException, AWTException {
	        
	  		String errMsg = "";
	  		try {
	        
	        	Capabilities Cap= ((RemoteWebDriver) driver).getCapabilities();
	        	System.out.println("Chrome Version : "+Cap.getVersion().toString());
	            
	        	if(Cap.getVersion().toString().isEmpty()) {
	            	String message = Cap.getCapability("message").toString();
		            errMsg += message.replace("unknown error:", "Error: ");
	            } else {
	            	int version = Integer.parseInt(Cap.getVersion().split("\\.")[0]);
	            	if (!(version>=54))
	            		errMsg += "Chrome version less than 52 is not supported";
	            }
	    
	        } catch (Exception e) {
	        	
	        	errMsg += "ERROR:-" + org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e); 
	        }
	  		return errMsg;
	  	}
	  	
	  	private static String checkIEVersion(WebDriver driver) throws IOException, JSONException, ParseException, InterruptedException, HeadlessException, AWTException {
	  		
	  		String errMsg = "";
	  		try {
	  			Capabilities Cap= ((RemoteWebDriver) driver).getCapabilities();
	        	System.out.println("IE Version : "+Cap.getVersion().toString());  
	        	
	  			if (Cap.getVersion().toString().isEmpty()) {
	                String message = Cap.getCapability("message").toString();
	                errMsg += message.replace("unknown error:", "Error: ");
	  			} else {
	                int version = Integer.parseInt(Cap.getVersion());
	  				if (!(version>=11))
	  					errMsg += "IE version less than 11 is not supported";
	  			}
	  		} catch (Exception e) {
	  			
	  			errMsg += "ERROR:-" + org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e); 
	  		}
			return errMsg;
	  	}
	
	
	static String CheckExist(WebDriver lDriver, WebElement objDesc) throws Exception {
		String sRes="False";
		try{
			try {
				((JavascriptExecutor) lDriver).executeScript("arguments[0].scrollIntoView(true);", objDesc);
				if (objDesc.getTagName() != null)
					sRes = "True";
			} catch (Exception ex) {
				((JavascriptExecutor) lDriver).executeScript("arguments[0].scrollIntoView(true);", objDesc);
				if (objDesc.getTagName() != null)
					sRes = "True";
			}
		}catch(Exception e) {
			throw e;
		}
		return sRes;
	}
	
	static void CheckPopUp_Object(String sTestName,WebElement oEle,String sObjStr) throws Exception
	{    
		WebDriver lDriver= Web.getWebDriver(sTestName);
		try
		{
			if (lDriver.findElement(By.tagName("iframe")).getTagName()!=null )
		    { 
				List<WebElement> iframes=lDriver.findElements(By.tagName("iframe"));
				for(int i=0;i<iframes.size();i++)
				{
						if (iframes.get(i).isDisplayed()) {
							String FrameName = iframes.get(i).getAttribute("name");
							if(iframes.get(i).getAttribute("name").equals("")){
								FrameName = iframes.get(i).getAttribute("id");
								if(iframes.get(i).getAttribute("id").equals("")){
									FrameName = iframes.get(i).getAttribute("class");}
								
								}
							lDriver.switchTo().frame(FrameName);
							if (Helper.CheckExist(lDriver,oEle).contains("True")) {break;}	
							lDriver.switchTo().defaultContent();			
						}		
				}			
		    }
		} catch(Exception e) {
			throw e;
		}
	}
	

	@SuppressWarnings("unused")
	static void forceEnter(WebDriver driver, WebElement oEle, String val) {

		JavascriptExecutor executor = (JavascriptExecutor) driver;
		executor.executeScript("arguments[0].value="+val+";", oEle);
	}
	
	static void forceClick(WebDriver driver, WebElement oEle) {
		
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		executor.executeScript("arguments[0].click();", oEle);
	}
	
	@SuppressWarnings("unused")
	static boolean isDisplayed(WebElement oEle) {
		return oEle.isDisplayed();
	}
	
	static void checkReady( String sTestName, WebElement oEle) throws InterruptedException, HeadlessException, IOException, AWTException	{
		
			Thread.sleep(1000);
		
			WebDriver lDriver = Web.getWebDriver(sTestName);
			WebDriverWait wait = new WebDriverWait(lDriver, Long.parseLong(Utils.hEnvParams.get("OBJ_TIMEOUT")));
			wait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver lDriver) {
					return ((JavascriptExecutor) lDriver).executeScript("return document.readyState").equals("complete");
				}
			});
	}
	
	
	
	public static boolean waitForElement(WebDriver driver, WebElement oEle,int Seconds) {
		// TODO Auto-generated method stub

		try {
			
			WebDriverWait wait = new WebDriverWait(driver, Seconds);
			wait.until(ExpectedConditions.visibilityOf(oEle));
			return true;	
			
		} catch(Exception e) {
			return false;
		}
	}
}

}




	 
