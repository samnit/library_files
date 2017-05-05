package com.automatics.packages.library.common;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestContext;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.screentaker.ViewportPastingStrategy;
import io.appium.java_client.*;
import io.appium.java_client.android.AndroidDriver;

@SuppressWarnings({"rawtypes"})
public class Android 
{
		
	private static Hashtable<String, String> hTestName_CurrentAndroidInstance = new Hashtable<String, String>();
	private static Hashtable<String, Hashtable<String, AppiumDriver>> h2TestName_AndroidDriver = new Hashtable<String, Hashtable<String, AppiumDriver>>();	
	private static Hashtable<String, Hashtable<String, String>> h2AndroidPrefs = new Hashtable<String, Hashtable<String, String>>();
	
	private static void putAndroidDriver(String sTestName, AppiumDriver driver, String currentAndroidInstance) {
		
		Hashtable <String, AppiumDriver > hdriver = new Hashtable<>();
		hdriver.put(currentAndroidInstance, driver);
		if (h2TestName_AndroidDriver.containsKey(sTestName))
			h2TestName_AndroidDriver.get(sTestName).put(currentAndroidInstance, driver);
		else 
			h2TestName_AndroidDriver.put(sTestName, hdriver);
	}
	
	public static AppiumDriver getAndroidDriver(String sTestName) {
	
		try {
			
			if (hTestName_CurrentAndroidInstance.containsKey(sTestName))
				return h2TestName_AndroidDriver.get(sTestName).get(hTestName_CurrentAndroidInstance.get(sTestName));
			
			else {
				Object myKey = h2TestName_AndroidDriver.get(sTestName).keySet().toArray()[0];
				return (AppiumDriver) h2TestName_AndroidDriver.get(sTestName).get(myKey);
			}
		} catch (Exception e) {
			throw e;
		}
}
	// Methods Exposed To User
	
		
	
	
public static class Web
	{
	
	/**
	 * @param sTestName
	 * @param sBrowserPref
	 * @return
	 * @throws Exception
	 */
	public static AppiumDriver awLaunchWeb(String sTestName, String sUrl, String sBrowserReference) throws Exception { 
		AppiumDriver driver = null;
		String sDesc = Logs.log(sTestName);
		
		try
		{
			driver = Helper.Launch_Web(sTestName,sBrowserReference);
			driver.manage().timeouts().implicitlyWait(Long.valueOf(Utils.hEnvParams.get("OBJ_TIMEOUT")), TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(Long.valueOf(Utils.hEnvParams.get("OBJ_TIMEOUT")), TimeUnit.SECONDS);
			
			driver.get(sUrl);
			putAndroidDriver(sTestName, driver, sBrowserReference);
			sBrowserReference = (sBrowserReference.equals("")) ? "Default" : sBrowserReference;
			Reporter.print(sTestName, sDesc + " -- "  + sUrl);
		
		}
		
		
		
		catch (Exception ex) {
			
			Reporter.printError(sTestName, ex, sDesc, "");
		}
		
		return driver;
	
	}
	public static AppiumDriver awSet_CurrentReference(String sTestName, String sBrowserReference) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = Logs.log(sTestName);
		
		try { 
		
			hTestName_CurrentAndroidInstance.put(sTestName, sBrowserReference);
			
			Reporter.print(sTestName, sDesc  + " to " + sBrowserReference + " -- Done");
	
		} catch (Exception ex) {
			Reporter.printError(sTestName, ex, sDesc, "");
		}
		return h2TestName_AndroidDriver.get(sTestName).get(sBrowserReference);
	}
	public static void awSet_AndroidPreference(String sTestName, String sPreferences, String sValue) throws HeadlessException, IOException, AWTException, InterruptedException { // TODO - Expose List of Prefs
		Hashtable <String, String > hPref = new Hashtable<>();
		hPref.put(sPreferences, sValue);
	
		String sDesc = Logs.log(sTestName);
	
		try { 
			
			if (h2AndroidPrefs.containsKey(sTestName))
				h2AndroidPrefs.get(sTestName).put(sPreferences, sValue);
			else {
				h2AndroidPrefs.put(sTestName,hPref);
			}
		
			Reporter.print(sTestName, sDesc + "\nAndroid Preference :" + sPreferences + " Set to : " + sValue + " -- Done");
		
	} catch (Exception ex) {
		
		Reporter.printError(sTestName, ex, sDesc, "");
	}
}
	public static boolean awVerify_Alert(String sTestName, String sExpAlertTxt) throws HeadlessException, IOException, AWTException, InterruptedException  {

		String sDesc, sActVal, sExpVal; boolean bStatus = false;
		sDesc = Logs.log(sTestName);
		
		
		try { 	
			sExpAlertTxt = Utils.Helper.validateUserInput(sTestName, sExpAlertTxt);
			sExpVal = Reporter.filterUserInput(sExpAlertTxt);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);

			sActVal = lDriver.switchTo().alert().getText();
			lDriver.switchTo().alert().accept();			
			
			bStatus = sExpVal.equals(sActVal);
			Reporter.print(sTestName, sExpAlertTxt, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(lDriver));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_Alert(sTestName, sExpAlertTxt); }
			Reporter.printError(sTestName, e, sDesc,Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}
	
	@SuppressWarnings("unused")
	public static boolean awVerify_AlertPresent(String sTestName, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException {
		
		String sDesc, sExpVal ; boolean bStatus = false;
		sDesc = Logs.log(sTestName);
		
		
		try { 
			
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			sExpVal = Reporter.filterUserInput(sVal);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);

			WebDriverWait wait = new WebDriverWait(lDriver, 20);
			
			try {
				if (wait.until(ExpectedConditions.alertIsPresent()) != null) { bStatus = true; }
			}
			catch (Exception e) {}
			
			Reporter.print(sTestName, sVal, sDesc, "NA", "NA", bStatus, Helper.takeScreenshot(lDriver));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_AlertPresent(sTestName,sVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}
	
	public static boolean awVerify_Attribute(String sTestName, WebElement oEle, String sObjStr, String sAttribName,String sExpVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {

		String sDesc, sActVal, sVal=sAttribName; boolean bStatus = false;
	
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		
		
		try { 	
			sAttribName = Utils.Helper.validateUserInput(sTestName, sAttribName);
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			sAttribName = Reporter.filterUserInput(sAttribName);
			
			Helper.checkReady(sTestName, oEle);
			

			sActVal= oEle.getAttribute(sAttribName);
			
			bStatus = sExpVal.equals(sActVal);
			Reporter.print(sTestName, sVal, sDesc + " :: Validate the atttibute - " + sAttribName, sExpVal, sActVal, bStatus, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_Attribute(sTestName, oEle, sObjStr,sVal, sExpVal); }
			Reporter.printError(sTestName, e, sDesc , Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean awVerify_Checked(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc; boolean bStatus = false;
	
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		
		try { 	
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			Reporter.filterUserInput(sVal);
			Helper.checkReady(sTestName, oEle);
			
			if (oEle.isSelected()) {
				bStatus = true;
			}
			Reporter.print(sTestName, sVal, sDesc , "NA", "NA", bStatus,Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_Checked(sTestName, oEle, sObjStr, sVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}
	
	public static boolean awVerify_Cookies(String sTestName,String sCookieName, String sExpVal) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc, sActVal,sVal=sCookieName; boolean bStatus = false;
		
		sDesc = Logs.log(sTestName);
		
	    
		try { 
			sCookieName = Utils.Helper.validateUserInput(sTestName, sCookieName);
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			sCookieName = Reporter.filterUserInput(sCookieName);
		    
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			sActVal = lDriver.manage().getCookieNamed(sCookieName).getValue();
			bStatus = sExpVal.equals(sActVal);
			Reporter.print(sTestName, sVal, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(lDriver));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_Cookies(sTestName, sVal,sExpVal); }
			Reporter.printError(sTestName, e, sDesc,Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean awVerify_Editable(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc; boolean bStatus = false; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
				
		try { 	
			
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			Reporter.filterUserInput(sVal);
			Helper.checkReady(sTestName, oEle);
			
			if (oEle.isEnabled()) { 
				bStatus = true;
			}
			Reporter.print(sTestName, sVal, sDesc, "NA", "NA", bStatus, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_Editable(sTestName, oEle, sObjStr, sVal); }
			Reporter.printError(sTestName, e, sDesc,Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean awVerify_Object_Dimension(String sTestName, WebElement oEle, String sObjStr, String sDimenType,String sDimenVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {

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
			Reporter.print(sTestName, sVal, sDesc + " :: Values - " + sDimenType, sDimenVal, sActVal, bStatus, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) {  awVerify_Object_Dimension(sTestName, oEle, sObjStr, sVal,sDimenVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}
	
	
	public static boolean awVerify_ElementPresent(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc; boolean bStatus = false; 
			
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
			
		try { 	
			
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			Reporter.filterUserInput(sVal);
			Helper.checkReady(sTestName, oEle);
			AppiumDriver driver = getAndroidDriver(sTestName);
			try {
				
				bStatus = Helper.waitForElement(driver,oEle, Integer.parseInt(Utils.hEnvParams.get("OBJ_TIMEOUT")));
				Reporter.print(sTestName, sVal, sDesc, "NA", "NA", bStatus, Helper.takeScreenshot(driver));
			}
			catch (NoSuchElementException e) {
				Reporter.print(sTestName, sVal, sDesc, "NA", "NA", false , Helper.takeScreenshot(driver));
			}
			
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_ElementPresent(sTestName, oEle, sObjStr, sVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}
	
	public static boolean awVerify_Text(String sTestName, WebElement oEle, String sObjStr, String sExpText) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc, sActVal = null, sExpVal; boolean bStatus = false;
				
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
	
		try { 
			
			sExpText = Utils.Helper.validateUserInput(sTestName, sExpText);
			sExpVal = Reporter.filterUserInput(sExpText);
			Helper.checkReady(sTestName, oEle);
			
			sActVal = oEle.getText();
			bStatus = sExpVal.equalsIgnoreCase(sActVal);
			
			Reporter.print(sTestName, sExpText, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_Text(sTestName, oEle, sObjStr, sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
		
	}

	public static boolean awEquals(String sTestName, String sExpVal,String sActVal) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = "",sVal=sExpVal; boolean bStatus = false;
	
		try { 
	    	
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			sActVal = Utils.Helper.validateUserInput(sTestName, sActVal);
			sDesc = Logs.log(sTestName) + " :: Values - " + sExpVal +" And "+ sActVal;
			sExpVal = Reporter.filterUserInput(sExpVal);
		    
		    bStatus = sExpVal.equals(sActVal);
			Reporter.print(sTestName, sVal, sDesc, sExpVal, sActVal, bStatus);
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awEquals(sTestName, sVal,sActVal); }
			Reporter.printError(sTestName, e, sDesc);
		}
		return bStatus;
	}

	public static boolean awVerifyIn_HTMLSource(String sTestName, String sExpText) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc, sExpVal; boolean bStatus = false;
		sDesc = Logs.log(sTestName) + " :: Values - " + sExpText;
		sExpVal = Reporter.filterUserInput(sExpText);
		
		try { 	
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			
			bStatus = lDriver.getPageSource().contains(sExpVal);
			Reporter.print(sTestName, sExpText, sDesc, "NA", "NA", bStatus, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerifyIn_HTMLSource(sTestName, sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}
	
	public static boolean awVerifyIn_URL(String sTestName, String sExpText) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc, sActVal = null, sExpVal; boolean bStatus = false;
		sDesc = Logs.log(sTestName);
		
		try { 	
			sExpText = Utils.Helper.validateUserInput(sTestName, sExpText);
			sExpVal = Reporter.filterUserInput(sExpText);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			
			sActVal = lDriver.getCurrentUrl();
			bStatus = sActVal.contains(sExpVal);
			Reporter.print(sTestName, sExpText, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(lDriver));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerifyIn_URL(sTestName, sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean awVerify_SelectOptions(String sTestName, WebElement oEle, String sObjStr, String sExpOptions) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {

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
				
				for (String sExpOption: sExpVal.split("\\|")) {
					
					if (!sActVal.contains(sExpOption))  {
						bStatus = false; break;
					} else 
						bStatus = true;
				}
				
				Reporter.print(sTestName, sExpOptions, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
			}
			else
				Reporter.print(sTestName, sDesc + "  :: Error : This Method Works only for Html : 'Select' tag");

		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_SelectOptions(sTestName, oEle, sObjStr, sExpOptions); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean awVerify_SelectedOptions(String sTestName, WebElement oEle, String sObjStr, String sExpOptions) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
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
				
				Reporter.print(sTestName, sExpOptions, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
			}
			else
				Reporter.print(sTestName, sDesc + "  :: Error : This Method Works only for Html : 'Select' tag");

		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_SelectedOptions(sTestName, oEle, sObjStr, sExpOptions); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}

	@SuppressWarnings("unchecked")
	public static boolean awVerify_TextPresent(String sTestName, String sExpText) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = "", sExpVal; boolean bStatus = false;
		
		try { 
			
			sExpText = Utils.Helper.validateUserInput(sTestName, sExpText);
			sDesc = Logs.log(sTestName) + " :: Validating text '" + sExpText + "'";
			sExpVal = Reporter.filterUserInput(sExpText);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			
			List<WebElement> oList = lDriver.findElements(By.xpath("//*[contains(text(),'" + sExpVal + "')]"));
			
			if (oList.size() > 0) bStatus = true;
			
			Reporter.print(sTestName, sExpText, sDesc, "NA", "NA", bStatus, Helper.takeScreenshot(lDriver));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_TextPresent(sTestName, sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean awVerifyIn_Title(String sTestName, String sExpTitle) throws HeadlessException, IOException, AWTException, InterruptedException , InterruptedException {
		
		String sDesc = "", sExpVal; boolean bStatus = false;
		
		try { 	
			
			sExpTitle = Utils.Helper.validateUserInput(sTestName, sExpTitle);
			sDesc = Logs.log(sTestName) + " :: Validating Title '" + sExpTitle + "'";
			sExpVal = Reporter.filterUserInput(sExpTitle);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			
			if (lDriver.getTitle().contains(sExpVal))
				bStatus = true;
			
			Reporter.print(sTestName, sExpTitle, sDesc, sExpVal, lDriver.getTitle(), bStatus, Helper.takeScreenshot(lDriver));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerifyIn_Title(sTestName, sExpTitle); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}

	public static boolean awVerify_Visible(String sTestName, WebElement oEle, String sObjStr, String sUserVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc; boolean bStatus = false;
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		
		try { 	
			
			sUserVal = Utils.Helper.validateUserInput(sTestName, sUserVal);
			Reporter.filterUserInput(sUserVal);
			Helper.checkReady(sTestName, oEle);
			
			if (oEle.isDisplayed())
				bStatus = true;
			
			Reporter.print(sTestName, sUserVal, sDesc + " :: Validating visible '" + sUserVal + "'", "NA", "NA", bStatus, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_Visible(sTestName, oEle, sObjStr, sUserVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}

	public static void awCheck_Uncheck(String sTestName, WebElement oEle, String sObjStr, String sUserVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc; 
			
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try { 	
			sUserVal = Utils.Helper.validateUserInput(sTestName, sUserVal);	
			AppiumDriver lDriver = getAndroidDriver(sTestName);

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
			
	    	if (Utils.handleIntermediateIssue()) { awCheck_Uncheck(sTestName, oEle, sObjStr, sUserVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static void awClick(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc = "";
		System.out.println(oEle.getClass());
				
		sDesc = Logs.log(sTestName) + " on Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		
		
		try { 	
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			System.out.println(lDriver.getContext());
			try {
				
				Helper.forceClick(lDriver, oEle);
			}
			catch (Exception e) {
				oEle.click();
			}
			Reporter.print(sTestName, sDesc + " :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awClick(sTestName, oEle, sObjStr); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static void awPrint(String sTestName, String sTxt) throws HeadlessException, IOException, AWTException, InterruptedException, InvalidInputException  {

		String sDesc = Logs.log(sTestName);
		sTxt = Utils.Helper.validateUserInput(sTestName, sTxt);	
		Reporter.print(sTestName, sDesc + " :: '" + sTxt + "'");
	}

	public static void awGoBack(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc; 
		sDesc = Logs.log(sTestName);
		
		try { 	
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			lDriver.navigate().back();

			Reporter.print(sTestName, sDesc + " :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awGoBack(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static void awOpenURL(String sTestName, String sURL) throws HeadlessException, IOException, AWTException, InterruptedException  {
		

		String sDesc = ""; 
		
		try {
		
			sURL = Utils.Helper.validateUserInput(sTestName, sURL);
		
			sDesc = Logs.log(sTestName) + ":  " + sURL;
		
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			lDriver.get(sURL);
			Reporter.print(sTestName, sDesc + "  :: Performed");
			
		} catch ( Exception e) {
		
			if (Utils.handleIntermediateIssue()) { awOpenURL(sTestName, sURL); }
			Reporter.printError(sTestName, e, sDesc);
		}
		 
	}

	public static void awRefresh(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc; 
		sDesc = Logs.log(sTestName);
		
		try { 	
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			lDriver.navigate().refresh();

			Reporter.print(sTestName, sDesc + "  :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awRefresh(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static void awMultiSelect(String sTestName, WebElement oEle, String sObjStr, String sOptions) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {

		String sDesc; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
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
			
	    	if (Utils.handleIntermediateIssue()) { awMultiSelect(sTestName, oEle, sObjStr, sOptions); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static void awSelect(String sTestName, WebElement oEle, String sObjStr, String sOption) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		
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
			
	    	if (Utils.handleIntermediateIssue()) { awSelect(sTestName, oEle, sObjStr, sOption); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static void awVerify_Frame(String sTestName, String sFrameInfo) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc, sExpVal; boolean bStatus = false;
		sDesc = Logs.log(sTestName);
		
		try { 	
			
			sFrameInfo = Utils.Helper.validateUserInput(sTestName, sFrameInfo);
			sExpVal  = Reporter.filterUserInput(sFrameInfo);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			@SuppressWarnings("unchecked")
			List<WebElement> oFrameList = lDriver.findElements(By.xpath("//iframe|//frame"));
			for (WebElement oFrame : oFrameList)  {
				if (oFrame.isDisplayed()) {
					
						JavascriptExecutor executor = (JavascriptExecutor) lDriver;
						String sOutAttribs =  executor.executeScript("var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index)" +
								"{ items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;", oFrame).toString();
						if (sOutAttribs.contains(sExpVal)) {
							 bStatus=true; break;
						}
				}
			}
			
			Reporter.print(sTestName, sFrameInfo, sDesc, "NA", "NA", bStatus, Helper.takeScreenshot(lDriver));
					
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_Frame(sTestName, sFrameInfo); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	public static void awSwitch_To_Frame(String sTestName, String sFrameInfo) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc; boolean bStatus = false;
		sDesc = Logs.log(sTestName);
		
		try { 	
			
			sFrameInfo = Utils.Helper.validateUserInput(sTestName, sFrameInfo);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			@SuppressWarnings("unchecked")
			List<WebElement> oFrameList = lDriver.findElements(By.xpath("//iframe|//frame"));
			for (WebElement oFrame : oFrameList)  {
				if (oFrame.isDisplayed()) {
					
					if (!sFrameInfo.isEmpty()) { 
						JavascriptExecutor executor = (JavascriptExecutor) lDriver;
						String sOutAttribs =  executor.executeScript("var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index)" +
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
			
	    	if (Utils.handleIntermediateIssue()) { awSwitch_To_Frame(sTestName, sFrameInfo); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static void awSwitch_To_Default(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc; 
		sDesc = Logs.log(sTestName);
		
		try { 	
			AppiumDriver lDriver = getAndroidDriver(sTestName);

			lDriver.switchTo().defaultContent();
			Reporter.print(sTestName, sDesc + "  :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awSwitch_To_Default(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static void awSwitch_To_Tab(String sTestName, String sWindowInfo) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc; 
		sDesc = Logs.log(sTestName);
		
		try { 	
			sWindowInfo = Utils.Helper.validateUserInput(sTestName, sWindowInfo);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);
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
			
	    	if (Utils.handleIntermediateIssue()) { awSwitch_To_Tab(sTestName, sWindowInfo); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static void awStore_Attribute(String sTestName, WebElement oEle, String sObjStr, String sComputedVal,String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc = "", sActVal; 
		
		
		try { 	
		
			sComputedVal = Utils.Helper.validateUserInput(sTestName, sComputedVal);
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
		
			sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
			
			if (sVarName.equals("")) {
				sVarName = "Temp";
			} 
			sActVal = oEle.getAttribute(sComputedVal);
			Utils.setScriptParams(sTestName, sVarName, sActVal);
				
			Reporter.print(sTestName, sDesc + " :: Store (Attribute-" + sComputedVal + ", Value-" + sActVal + ") in Local Variable '" + sVarName + "'");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awStore_Attribute(sTestName, oEle, sObjStr,sComputedVal,sVarName ); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

public static void awStore_URL(String sTestName, String sUserVal) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = "", sActVal, sVarName; 
		
		
		try { 
			sUserVal = Utils.Helper.validateUserInput(sTestName, sUserVal);
			
			sDesc = Logs.log(sTestName);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			
			sVarName = sUserVal;
			if (sUserVal.equals("")) {
				sVarName = "Temp";	
			}
			
			sActVal = lDriver.getCurrentUrl();
			Utils.setScriptParams(sTestName, sVarName, sActVal);
				
			Reporter.print(sTestName, sDesc + "  :: Store (Current URL -" + sActVal + ") in Local Variable '" + sVarName + "'");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awStore_URL(sTestName, sUserVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	
	}
	
	public static void awStore_SelectOptions(String sTestName, WebElement oEle, String sObjStr, String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc, sActVal = null; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		
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
			
	    	if (Utils.handleIntermediateIssue()) { awStore_SelectOptions(sTestName, oEle, sObjStr, sVarName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static void awStore_SelectedValue(String sTestName, WebElement oEle, String sObjStr, String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc, sActVal = null; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		
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
			
	    	if (Utils.handleIntermediateIssue()) { awStore_SelectedValue(sTestName, oEle, sObjStr, sVarName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static void awStore_Text(String sTestName, WebElement oEle, String sObjStr, String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc, sActVal = null; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try { 	
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			sActVal = oEle.getText(); 
			
			if (sVarName.equals("")) {
				sVarName = "Temp";	
			}
			
			Utils.setScriptParams(sTestName, sVarName, sActVal);
				
			Reporter.print(sTestName, sDesc + " :: Store '" + sActVal + "' of "+ sObjStr + " in Local Variable '" + sVarName + "'");
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awStore_Text(sTestName, oEle, sObjStr, sVarName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static void awStore_Value(String sTestName, WebElement oEle, String sObjStr, String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
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
			
	    	if (Utils.handleIntermediateIssue()) { awStore_Value(sTestName, oEle, sObjStr, sVarName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	
	@SuppressWarnings("unused")
	public static void awType(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {

		String sDesc = "" ; boolean flag = true; 
			
		try { 
			
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			sDesc = Logs.log(sTestName) + " in Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " ) , Value = " + sVal ;
			Helper.checkReady(sTestName, oEle);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			
			try {
				oEle.clear(); Thread.sleep(500L); 
			}
			catch (Exception e) {
				flag = false;
				oEle.sendKeys(sVal);
				Native.anNative_HideKeyboard(sTestName);
				
			}
			if(flag)
			oEle.sendKeys(sVal);
			Native.anNative_HideKeyboard(sTestName);
			Reporter.print(sTestName, sDesc + " :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awType(sTestName, oEle, sObjStr, sVal); }
	    	Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	@SuppressWarnings("unused")
	public static void awType_Advanced(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {

		String sDesc = "" ; boolean flag = true; 
			
		try { 
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			sDesc = Logs.log(sTestName) + " in Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " ) , Value = " + sVal ;
			Helper.checkReady(sTestName, oEle);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);	
			Helper.CheckPopUp_Object(sTestName, oEle, sObjStr);
				if (Helper.CheckExist(lDriver,oEle).contains("True")) {
					if (!oEle.getAttribute("value").equals("")){
						oEle.clear();Thread.sleep(2000L);
					} 
					oEle.sendKeys(sVal); 
				}
				awSwitch_To_Default(sTestName);
			
			
			
			Reporter.print(sTestName, sDesc + " :: Performed");
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awType_Advanced(sTestName, oEle, sObjStr, sVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static void awDeleteCookies(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException {
		
		String sDesc; 
		sDesc = Logs.log(sTestName)+" :: All Cookies Deleted";
		
		try { 	

			AppiumDriver lDriver = getAndroidDriver(sTestName);
	
			lDriver.manage().deleteAllCookies();
			
			Reporter.print(sTestName, sDesc);

		}  catch(Exception e) {
			
			if (Utils.handleIntermediateIssue()) { awDeleteCookies(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	
	public static void awDeleteCache(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException {
		
		String sDesc, sBrowser, sTitle = "", vbPath; 
		sDesc = Logs.log(sTestName) + " :: Cache Deleted";
		
		try { 	
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			sBrowser = Utils.h2TestName_TestParams.get(sTestName).get("Exe_Platform"); 
			if(sBrowser.equalsIgnoreCase("Firefox"))
				sTitle = lDriver.getTitle() +" - Mozilla Firefox";
			else if(sBrowser.equalsIgnoreCase("IE"))
				sTitle = lDriver.getTitle() +" - Internet Explorer";
			
				
			vbPath = "\""+System.getProperty("user.dir").replace("\\", "/") +"/Packages/delcache.vbs\"";
		
			Runtime.getRuntime().exec( "cscript " +vbPath+ " \""+sTitle+ "\"");
	
			Reporter.print(sTestName, sDesc);
			
		}  catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) {awDeleteCache(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	
	public static void awDoubleClick(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException   {
		
		String sDesc; 
		
		sDesc = Logs.log(sTestName)+ " :: Double Click on Element: " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try { 	
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			Actions action = new Actions(lDriver);
			action.moveToElement(oEle).doubleClick().build().perform();
			
			Reporter.print(sTestName, sDesc);
			
		}   catch (Exception e) {
		
			if (Utils.handleIntermediateIssue()) { awDoubleClick(sTestName,oEle,sObjStr); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	
	public static void awRunScript(String sTestCaseName,ITestContext testContext) throws HeadlessException, IOException, AWTException, InterruptedException   {
		
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
		
			if (Utils.handleIntermediateIssue()) { awRunScript(sTestCaseName, testContext); }
			Reporter.printError(testContext.getName(), e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(testContext.getName())));
		}
	}
	
	public static void awWait(String sTestName, String secs) throws NumberFormatException, InterruptedException, HeadlessException, IOException, AWTException, InterruptedException, InvalidInputException  {
		
		secs = Utils.Helper.validateUserInput(sTestName, secs);
		String sDesc = Logs.log(sTestName)+" :: Waiting for: ("+secs+")seconds";
		Thread.sleep(Long.valueOf(secs)*1000);
		Reporter.print(sTestName, sDesc);
	}
	
	public static void awWaitForElementPresent(String sTestName, WebElement oEle,  String sObjStr, String WaitTimeSec) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc = "" ;
		
		sDesc =  Logs.log(sTestName)+" :: Waiting for Element : " +sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
				
		try {
			
			WaitTimeSec = Utils.Helper.validateUserInput(sTestName, WaitTimeSec);
			long sec = Long.parseLong(WaitTimeSec);
			Helper.checkReady(sTestName, oEle);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			WebDriverWait wait = new WebDriverWait(lDriver, sec);
			wait.until(ExpectedConditions.visibilityOf(oEle));
			
			Reporter.print(sTestName, sDesc+ " to be Present: ("+WaitTimeSec+")seconds");
		
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { awWaitForElementPresent(sTestName,oEle,sObjStr, WaitTimeSec); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}

	public static boolean awVerify_SubStringInText(String sTestName, WebElement oEle, String sObjStr, String sExpText) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException {
		
		String sDesc, sActVal = null, sExpVal; boolean bStatus = false;
		
		sDesc = Logs.log(sTestName)+ " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try {
			
			sExpText = Utils.Helper.validateUserInput(sTestName, sExpText);
			sExpVal = Reporter.filterUserInput(sExpText);	
			Helper.checkReady(sTestName, oEle);
			
			sActVal = oEle.getText();
			sActVal = sActVal.replace("\n", " ");
			bStatus = (sActVal.contains(sExpVal));	
			
			Reporter.print(sTestName, sExpText, sDesc + " :: Check the contents '" + sExpText + "'", sExpVal, sActVal, bStatus, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { awVerify_SubStringInText(sTestName,oEle,sObjStr, sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
		return bStatus;
	}
	
	public static void awAcceptAlert(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = Logs.log(sTestName)+" :: Clicking on 'OK' of Popup";
		
		try {
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			Alert alert = lDriver.switchTo().alert();	
			alert.accept();
			
			Reporter.print(sTestName, sDesc);
		   
		}   catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { awAcceptAlert(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	
	public static void awCancelAlert(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = Logs.log(sTestName)+" :: Clicking on 'CANCEL' of Popup";
		
		try {
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			Alert alert = lDriver.switchTo().alert();							
		    alert.dismiss();
		    
		    Reporter.print(sTestName, sDesc);
				   
		}   catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { awCancelAlert(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	
	public static void awGoForward(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = Logs.log(sTestName)+" :: Moving Forward One Page";
		
		try{
			AppiumDriver lDriver = getAndroidDriver(sTestName);
		    lDriver.navigate().forward();
		    
		    Reporter.print(sTestName,sDesc);
	
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { awGoForward(sTestName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	
	public static boolean awVerify_ToolTip(String sTestName, WebElement oEle, String sObjStr, String sExpText) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
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
		
			if (Utils.handleIntermediateIssue()) { awVerify_ToolTip(sTestName, oEle, sObjStr, sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	    return bStatus;
	}
	
	public static boolean awVerifyIn_CSSValue(String sTestName, WebElement oEle, String sObjStr, String sAttribute,String sExpVal)throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException {
		
		String sDesc, sActVal = null, sVal=sAttribute; boolean bStatus = false;
		
		sDesc = Logs.log(sTestName)+ " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
			
		try {
			sAttribute = Utils.Helper.validateUserInput(sTestName, sAttribute);
			sExpVal = Utils.Helper.validateUserInput(sTestName, sExpVal);
			
			sAttribute = Reporter.filterUserInput(sAttribute);
			Helper.checkReady(sTestName, oEle);
			
			sActVal = oEle.getCssValue(sAttribute);
			bStatus = sActVal.equals(sExpVal);
			
			Reporter.print(sTestName, sVal, sDesc + " :: Verifying Value in CSS:'" + sVal + "'" , sExpVal, sActVal, bStatus, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
			
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { awVerifyIn_CSSValue(sTestName, oEle, sObjStr,sVal,sExpVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	    return bStatus;
	}	
	
	public static void awStore_CSSValue(String sTestName, WebElement oEle, String sObjStr,String sComputedVal,String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  { 
		
		String sDesc, sActVal; 
		sDesc = Logs.log(sTestName) + "GetCSS : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
			
		try {
			
			sComputedVal = Utils.Helper.validateUserInput(sTestName, sComputedVal);
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			if (sVarName.equals("")) {
				sVarName="Temp";
			} 
			sActVal = oEle.getCssValue(sComputedVal);
			Utils.setScriptParams(sTestName, sVarName, sActVal);
			
			Reporter.print(sTestName,sDesc + ", Type:("+sComputedVal+"), Got Value:("+sActVal+")");
		
		}	catch(Exception e) {
			
			if (Utils.handleIntermediateIssue()) { awStore_CSSValue(sTestName,oEle,sObjStr,sComputedVal,sVarName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
    }
	
	public static void awStore_Title(String sTestName, String sUserVal) throws HeadlessException, IOException, AWTException, InterruptedException  { 
		
		String sDesc, sActVal, sVarName; 
		sDesc = Logs.log(sTestName);
		
		try {	
			sUserVal = Utils.Helper.validateUserInput(sTestName, sUserVal);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);	
			sVarName = (sUserVal.equals("")) ? "Temp" : sUserVal;

			sActVal =lDriver.getTitle();
			Utils.setScriptParams(sTestName, sVarName, sActVal);
			
			Reporter.print(sTestName, sDesc + " :: Store (Current Title -" + sActVal + ") in Local Variable '" + sVarName + "'");
				
		} 	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { awStore_Title(sTestName,sUserVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}				
    }

	public static void awClick_OnText(String sTestName, String sText) throws HeadlessException, IOException, AWTException, InterruptedException 	{
		
		String sDesc = Logs.log(sTestName)+" :: Click on Text: "+ sText;
		boolean foundFlg = false;
		
		try {
			
			sText = Utils.Helper.validateUserInput(sTestName, sText);
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);	
			@SuppressWarnings("unchecked")
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
		
			if (Utils.handleIntermediateIssue()) { awClick_OnText(sTestName, sText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	public static void awEnter(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException {
		String sDesc = "" ;  
		
		try {
			
			sDesc = Logs.log(sTestName) + " in Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
			
			oEle.sendKeys(Keys.ENTER);
			Reporter.print(sTestName, sDesc + " :: Performed");
		
		} catch(Exception e) {
			
			if (Utils.handleIntermediateIssue()) { awEnter(sTestName, oEle, sObjStr); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	public static void awStore_FontColor(String sTestName, WebElement oEle, String sObjStr, String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc, sActVal; 
		
		sDesc = Logs.log(sTestName)+" :: Reterving Font color of Obj : "+sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try { 
			
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			
			sActVal = oEle.getCssValue("color");
			String[] numbers = sActVal.replace("rgba(", "").replace(")", "").split(",");
			int iR = Integer.parseInt(numbers[0].trim()); int iG = Integer.parseInt(numbers[1].trim()); int iB = Integer.parseInt(numbers[2].trim());
			sActVal = String.format("#%02x%02x%02x", iR, iG, iB).toUpperCase();
			
			sVarName = (sVarName.equals("")) ? "Temp" : sVarName;

			Utils.setScriptParams(sTestName, sVarName, sActVal);
			Reporter.print(sTestName, sDesc + " :: Store '" + sActVal + "' of "+ sObjStr + " in Local Variable '" + sVarName + "'");
		
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { awStore_FontColor(sTestName,oEle,sObjStr,sVarName); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	
	public static void awVerifyIn_Error(String sTestName, String sExpText) throws Exception {	
		
		String sDesc = "";
		
		try {
			
			sExpText = Utils.Helper.validateUserInput(sTestName, sExpText);
			sDesc = Logs.log(sTestName) + " :: Validating error message( " + sExpText + " )";
			
			AppiumDriver lDriver = getAndroidDriver(sTestName);
		    WebElement oEle = (WebElement) lDriver.findElement(By.xpath("//*[@id='errorDiv']"));
		    awVerify_SubStringInText(sTestName, oEle, "errObj", sExpText);
			    
			Reporter.print(sTestName, sDesc);		     
		 
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { awVerifyIn_Error(sTestName,sExpText); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	public static void awOpenIn_NewTab(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  { 
		
		String sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try{
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			Utils.setScriptParams(sTestName, "Parent_Brwsr", lDriver.getWindowHandle());

		    Actions newTab = new Actions(lDriver);
		    newTab.moveToElement(oEle); 
		    newTab.perform();
		    newTab.keyDown(Keys.CONTROL).click(oEle).build().perform();
			
			
			Reporter.print(sTestName,sDesc + " :: Performed");
			
		    for(String winHandle : lDriver.getWindowHandles()){
			    lDriver.switchTo().window(winHandle);			    
			}				
			Utils.setScriptParams(sTestName, "Child_Brwsr", lDriver.getWindowHandle());
			
		}	catch(Exception e) {
	
			if (Utils.handleIntermediateIssue()) { awOpenIn_NewTab(sTestName, oEle, sObjStr); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	
	public static void awFocus(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  { 
		
		String sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try{
			AppiumDriver lDriver = getAndroidDriver(sTestName);	
			if ("input".equals(oEle.getTagName())) {
				oEle.sendKeys("");
			} else {
				new Actions(lDriver).moveToElement(oEle).perform();
			}
			
			Reporter.print(sTestName,sDesc + " :: Focussed on Obj : " + sObjStr + ")");
		 
		}	catch(Exception e) {
		
			if (Utils.handleIntermediateIssue()) { awFocus(sTestName,oEle,sObjStr); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	}
	public static boolean awIs_Empty(String sTestName, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException  {
		
		String sDesc = "", sExpVal; boolean bStatus = false;
	
	    try { 	
	    	sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			sDesc = Logs.log(sTestName) + " :: Value - " + sVal;
			sExpVal = Reporter.filterUserInput(sVal);
			
		    bStatus = sExpVal.isEmpty();
			Reporter.print(sTestName, sVal, sDesc, "NA","NA", bStatus);
			
		}  catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awIs_Empty(sTestName, sVal); }
			Reporter.printError(sTestName, e, sDesc);
		}
		return bStatus;
	}
	
	public static void awMouseOver(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  { 
		
		String sDesc ; 
		
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try {
			AppiumDriver lDriver = getAndroidDriver(sTestName);
			Actions newTab = new Actions(lDriver);
			newTab.moveToElement(oEle); 
			newTab.perform();
			Reporter.print(sTestName,sDesc + " :: Performed");
		
		} catch(Exception e) {
			
			if (Utils.handleIntermediateIssue()) { awMouseOver(sTestName, oEle, sObjStr);; }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	 }
	
	public static void awMouseClick(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  { 

		String sDesc = "";
		
				
		sDesc = Logs.log(sTestName) + " on Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		try {
			AppiumDriver lDriver = getAndroidDriver(sTestName);
		
		
				Actions newTab = new Actions(lDriver);
				newTab.moveToElement(oEle); 
				newTab.perform();
				newTab.click(oEle).build().perform();
		
			Reporter.print(sTestName, sDesc + " :: Performed");
		}
		catch (Exception e) {
			oEle.click();
		}
		
	 }

	public static void awScreen_Capture(String sTestName , String sUsrVal) throws HeadlessException, IOException, AWTException, InterruptedException  { 
		
		String sDesc = "" ; 
		
		try {
			sUsrVal = Utils.Helper.validateUserInput(sTestName, sUsrVal);
			sDesc = Logs.log(sTestName) + " - " + sUsrVal ;
			Reporter.print(sTestName, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		
		} catch(Exception e) {
			
			if (Utils.handleIntermediateIssue()) { awScreen_Capture(sTestName, sUsrVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}
	 }
	public static void awGet_Cell_Data(String sTestName, WebElement oEle, String sObjStr, String sRowCol,String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException {
		
		String sDesc = "", sActVal ="", sColumn = "", sRow="" , flagTh = "false",sVal=sRowCol;  
		int sColumnNmbr, sRowNmbr,i=1;
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try { 	
			
			sRowCol = Utils.Helper.validateUserInput(sTestName, sRowCol);
			sVarName = Utils.Helper.validateUserInput(sTestName, sVarName);
			sRowCol = Reporter.filterUserInput(sRowCol);
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
			if(sColumn.equalsIgnoreCase("ALL")) 
				sColumnNmbr = -1;
			else
				sColumnNmbr = Integer.parseInt(sColumn);
			if(sRow.equalsIgnoreCase("ALL")) {
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
							for(int j=0;j<cols.size();j++)
								sActVal = sActVal + cols.get(j).getText() +"|";  	
					} 
				else {
						if(sColumnNmbr==-1 && sRowNmbr!=-1) {		//Row Number but All Columns
							if(i==sRowNmbr) {
								sActVal = sActVal + "Row No." + i + ":";
								for(int j=0;j<cols.size();j++)
									sActVal = sActVal + cols.get(j).getText() +"|";  
								break;
							}
						}
						else {
							if(sRowNmbr==-1 && sColumnNmbr!=-1) 	//All rows But Column Number
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
			
	    	if (Utils.handleIntermediateIssue()) {  awGet_Cell_Data(sTestName, oEle, sObjStr, sVal,sVarName);}
	    	Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		
		}
	}
	
	public static void awGet_RowWith_CellData(String sTestName, WebElement oEle, String sObjStr,String sCol,String sVarName) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException {
		
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
						if(sActVal.equalsIgnoreCase(sUsrVal)) {
							sResult = cols.get(sRequiredColumnNmbr-1).getText();
							break;
						}
					
					}
				
				Utils.setScriptParams(sTestName, sVarName, sResult);
				Reporter.print(sTestName, sDesc + " :: Store Value of Column: '"+ sRequiredColumnNmbr + "' where Column '" + sColumnNmbr + "' has Value: '" + sUsrVal + "' in Local Variable '" + sVarName + "' Data is: '" + sResult + "'");
				
			} catch(Exception e) {
				
			    	if (Utils.handleIntermediateIssue()) { awGet_RowWith_CellData(sTestName,  oEle, sObjStr, sVal,sVarName);}
			    	Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
					
			}
	
	}
	public static void awVerify_Cell_Data(String sTestName, WebElement oEle, String sObjStr,String sRowCol,String sExpVal) throws HeadlessException, IOException, AWTException, ClassNotFoundException, InterruptedException {
		
		String sDesc = null,sVal=sRowCol, sActVal ="", sColumn = null, sRow="", flagTh = "false"; boolean bStatus = false; int sColumnNmbr = 0, sRowNmbr,i=1;
		sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
		
		try { 	
			 
			sRowCol = Utils.Helper.validateUserInput(sTestName, sRowCol);
			sExpVal=Utils.Helper.validateUserInput(sTestName, sExpVal);
			sRowCol = Reporter.filterUserInput(sRowCol);
			
			Helper.checkReady(sTestName, oEle);
			sRow = sExpVal.split("::")[0];
			if (sRowCol.contains("::")) {
				sColumn = sRowCol.split("::")[1];
				sRow = sRowCol.split("::")[0];
			}
			else {
				Reporter.print(sTestName, sVal + "*EXIT_ON_FAIL*", sDesc +"Plese provide input correctly", "True", "False", false);
			}
			
			if(sColumn.equalsIgnoreCase("ALL")) 
				sColumnNmbr = -1;
			else
				sColumnNmbr = Integer.parseInt(sColumn);
			if(sRow.equalsIgnoreCase("ALL")) {
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
			Reporter.print(sTestName, sVal, sDesc, sExpVal, sActVal, bStatus, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));	
			
		} catch(Exception e) {
			
	    	if (Utils.handleIntermediateIssue()) { awVerify_Cell_Data(sTestName,oEle,sObjStr, sVal,sExpVal); }
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
			
		}
	}
	public static void awVerify_RowWith_CellData(String sTestName, WebElement oEle,  String sObjStr, String sCol,String sExpVal) throws HeadlessException, IOException, AWTException, ClassNotFoundException, InterruptedException {
		
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
			Reporter.print(sTestName, sVal, sDesc, sExpVal, sResult, bStatus, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
			
		} catch(Exception e) {
			
		    if (Utils.handleIntermediateIssue()) { awVerify_RowWith_CellData(sTestName, oEle, sObjStr, sVal,sExpVal);}
			Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
		}	
	
}	
	
	
	public static void awcloseApp(String sTestName) throws IOException, InterruptedException, HeadlessException, AWTException {
		String sDesc = Logs.log(sTestName);
		
		try {
  				Android.getAndroidDriver(sTestName).close();
  				Reporter.print(sTestName,sDesc + " :: CLOSE PERFORMED");
  		
  		} catch(Exception e)
		{
  			if (Utils.handleIntermediateIssue()) { awcloseApp(sTestName);}
			
			Reporter.printError(sTestName, e, sDesc + " :: PROBLEM IN CLOSING");
		}
  	}
	public static void awType_WithoutClear(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException {

		String sDesc = Logs.log(sTestName);
		try {

			sVal =  Utils.Helper.validateUserInput(sTestName, sVal);
					oEle.sendKeys(sVal);

			Reporter.print(sTestName, sDesc + " :: Performed");

		} 
		catch(Exception e) {

		if (Utils.handleIntermediateIssue()) { 
			awType_WithoutClear(sTestName, oEle, sObjStr, sVal); }
		Reporter.printError(sTestName, e, sDesc);
		}
	}
	
	public static void awSelect_By_Index(String sTestName, WebElement oEle, String sObjStr, String sOption) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {
		
		String sDesc = Logs.log(sTestName) + " Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )";
		
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
			
	    	if (Utils.handleIntermediateIssue()) { awSelect_By_Index(sTestName, oEle, sObjStr, sOption); }
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	}
	public static class Native
	{	
		public static AppiumDriver anLaunchNative(String sTestName, String AppReference) throws Exception { // TODO - Expose Test Params
			AppiumDriver driver = null;
			String sDesc = Logs.log(sTestName);
			
			try
			{
				driver = Helper.Launch_Native(sTestName,AppReference);
				driver.manage().timeouts().implicitlyWait(Long.valueOf(Utils.hEnvParams.get("OBJ_TIMEOUT")), TimeUnit.SECONDS);
				//driver.manage().timeouts().pageLoadTimeout(Long.valueOf(Utils.hEnvParams.get("OBJ_TIMEOUT")), TimeUnit.SECONDS);
				
				putAndroidDriver(sTestName, driver, AppReference);
				AppReference = (AppReference.equals("")) ? "Default" : AppReference;
				Reporter.print(sTestName, sDesc);
			}
			catch (Exception ex) {
				
				Reporter.printError(sTestName, ex, sDesc, "");
			}
			return driver;
		
		}	
		public static void anNative_Type(String sTestName, WebElement oEle, String sObjStr, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException  {

			String sDesc = "" ; boolean flag = true; 
				
			try { 
				
				sVal = Utils.Helper.validateUserInput(sTestName, sVal);
				sDesc = Logs.log(sTestName) + " in Object : " + sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " ) , Value = " + sVal ;
				
				AppiumDriver lDriver = getAndroidDriver(sTestName);
				
				try {
					oEle.clear(); Thread.sleep(500L); 
				}
				catch (Exception e) {
					flag = false;
					oEle.sendKeys(sVal);
					Native.anNative_HideKeyboard(sTestName);
					
				}
				if(flag)
				oEle.sendKeys(sVal);
				Native.anNative_HideKeyboard(sTestName);
				Reporter.print(sTestName, sDesc + " :: Performed");
				
			}  catch(Exception e) {
				
		    	if (Utils.handleIntermediateIssue()) { anNative_Type(sTestName, oEle, sObjStr, sVal); }
		    	Reporter.printError(sTestName, e, sDesc, Helper.takeScreenshot(Android.getAndroidDriver(sTestName)));
			}
		}
		
		  public static void anNative_ScrollDown(String sTestName, WebElement oEle, String sObjStr) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException {
				
				String sDesc = Logs.log(sTestName)+": Scroll to object : " +sObjStr + " -> ( " + Utils.Helper.getBy(sObjStr) + " )" ;
								
				try{
					AppiumDriver lDriver = getAndroidDriver(sTestName);	
					
					Dimension  size = lDriver.manage().window().getSize(); 
					int startx = (int) (size.width * 0.70); 
					int starty = size.height / 2; 
					int endy = (int) (size.height * 0.20);
					lDriver.swipe(startx, starty, startx, endy, 3000); 
					while(!Helper.waitForElement(lDriver, oEle, 2)) {
						lDriver.swipe(startx, starty, startx, endy, 3000); 
					}
					
				} catch(Exception e) {

					if (Utils.handleIntermediateIssue()) { anNative_ScrollDown(sTestName,oEle,sObjStr); }
					Reporter.printError(sTestName, e, sDesc);
				}
			}

		
				public static void anNative_Scroll_Ntimes(String sTestName, String sVal) throws HeadlessException, IOException, AWTException, InterruptedException, ClassNotFoundException, InvalidInputException {
				
				
				String sDesc = Logs.log(sTestName)+": Scroll : " +sVal + " times" ;
				
				sVal = Utils.Helper.validateUserInput(sTestName, sVal);
				int sValue= Integer.parseInt(sVal);
				
				try{
					
					AppiumDriver lDriver = getAndroidDriver(sTestName);
					Dimension dimensions = lDriver.manage().window().getSize();
				
					for(int i=0;i<sValue;i++) {
				
						Double screenHeightStart = dimensions.getHeight() * 0.5;
						Double screenHeightEnd = dimensions.getHeight() * 0.05;
						lDriver.swipe(0, screenHeightStart.intValue(), 0, screenHeightEnd.intValue(), 2000);
						
					}
				   	
					Reporter.print(sTestName, sDesc + " :: Performed");

				}	catch(Exception e) {
				
					if (Utils.handleIntermediateIssue()) { anNative_Scroll_Ntimes(sTestName, sVal);; }
					Reporter.printError(sTestName, e, sDesc);
				}
			}
			public static boolean anNative_IsKeyboardPresent(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
		       
				String checkKeyboardCommand = "adb shell dumpsys input_method";
		        String sDesc = Logs.log(sTestName) + " :: Verifying Keyboard is Displayed or Not" ;
		        boolean presentFlg = false;
		        
		        try {
		            Process process = Runtime.getRuntime().exec(checkKeyboardCommand);
		            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		            
		            int read; char[] buffer = new char[4096]; StringBuffer output = new StringBuffer();
		            while ((read = reader.read(buffer)) > 0) {
		                output.append(buffer, 0, read);
		            }
		            reader.close(); process.waitFor();
		            if (output.toString().contains("mInputShown=true"))
		            presentFlg = true;
		            Reporter.print(sTestName, "", sDesc, "True", String.valueOf(presentFlg), presentFlg);
			            
				}	catch(Exception e) {
				
					if (Utils.handleIntermediateIssue()) { anNative_IsKeyboardPresent(sTestName); }
					Reporter.printError(sTestName, e, sDesc);
				}
		        return presentFlg; 
			}
			public static boolean anNative_HideKeyboard(String sTestName) throws HeadlessException, IOException, AWTException, InterruptedException  {
			       
				
		        String sDesc = Logs.log(sTestName) + " :: Hide Keyboard in Native App" ;
		        boolean presentFlg = false;
		        
		        try {
		                   
		            presentFlg = anNative_IsKeyboardPresent(sTestName);
		            if(presentFlg) {
		            	AppiumDriver lDriver = getAndroidDriver(sTestName);
		            	String drivercontext=lDriver.getContext();
		            	lDriver.context("NATIVE_APP");
						lDriver.hideKeyboard();
						lDriver.context(drivercontext);
		            	
		            }
		            	
		            Reporter.print(sTestName, sDesc + ":: Performed");
			            
				}	catch(Exception e) {
				
					if (Utils.handleIntermediateIssue()) { anNative_HideKeyboard(sTestName); }
					Reporter.printError(sTestName, e, sDesc);
				}
		        return presentFlg; 
			}
			
			@SuppressWarnings("unchecked")
			public static void anSwitchContext(String sTestName, String Text) throws HeadlessException, IOException, AWTException, InterruptedException {
				 
				String sDesc = Logs.log(sTestName) + " :: Switch to Context : " + Text ;
			
				try {
					AppiumDriver lDriver = getAndroidDriver(sTestName);
					Set<String> contexts = lDriver.getContextHandles(); 
					for(String s:contexts) { 
						if(s.contains(Text)) 
							lDriver.context(s); 
					} 
				
				} catch(Exception e) {
					if (Utils.handleIntermediateIssue()) { anSwitchContext(sTestName, Text); }
					Reporter.printError(sTestName, e, sDesc);
				}
			}
			public static void anNative_SwipeInDropdown(String sTestName,WebElement oEle, String sObjStr,  String Text) throws HeadlessException, IOException, AWTException, InterruptedException { 
				 
				String sDesc =  Logs.log(sTestName);
		 
				try { 
		 
					Text = Utils.Helper.validateUserInput(sTestName, Text); 
					sDesc=Logs.log(sTestName); 
					AppiumDriver lDriver = getAndroidDriver(sTestName); 
		 
					Dimension  size = lDriver.manage().window().getSize(); 
					int startx = (int) (size.width * 0.70); 
					int starty = size.height / 2; 
					int endy = (int) (size.height * 0.20); 
		 
		 
					while (!Web.awVerify_ElementPresent(sTestName, oEle, sObjStr, "")) 
					{ 
					  if (Text.contains("VERTICALT2B")){ 
					  //Swipe from Top to Bottom. 
						  lDriver.swipe(startx, endy, startx, starty, 3000); 
						  Thread.sleep(2000); 
					  } 
					  else if (Text.contains("VERTICALB2T")) { 
						  //Swipe from Bottom to Top. 
						  lDriver.swipe(startx, starty, startx, endy, 3000); 
						  Thread.sleep(2000); 
					  } 
					
					  Reporter.print(sTestName, sDesc+" :: Performed"); 
					} 
				} catch(Exception e) { 
		 
					if (Utils.handleIntermediateIssue()) { anNative_SwipeInDropdown(sTestName,oEle,sObjStr, Text); } 
					Reporter.printError(sTestName, e, sDesc); 
				} 
			}
			
			
			/**
			 * @param sTestName
			 * @param sBrowserPref
			 * @return
			 * @throws AWTException 
			 * @throws HeadlessException 
			 * @throws Exception
			 */
			
			
			
			public static void anNative_closeApp(String sTestName) throws IOException, InterruptedException, HeadlessException, AWTException {
				try {
						Android.getAndroidDriver(sTestName).quit();
						Thread.sleep(1900);
						Helper.closeAppium();
						Reporter.print(sTestName, "CLOSE PERFORMED");
				} catch(Exception e)
				{
					Reporter.printError(sTestName, e, "PROBLEM IN CLOSING");
				}
			}
	}
	private static class Helper {

		 static boolean waitForElement(AppiumDriver driver, WebElement oEle,int Seconds) {
				
				try {
					
					WebDriverWait wait = new WebDriverWait(driver, Seconds);
					wait.until(ExpectedConditions.visibilityOf(oEle));
					return true;	
					
				} catch(Exception e) {
					return false;
				}
			}

		public static void closeAppium() throws IOException, InterruptedException {
			  	
			String sStopAppiumPath = System.getProperty("user.dir") + "\\Batch_Android\\StopAppium.bat";
			
			String cmds[] = {"cmd", "/c", "start "+ sStopAppiumPath};
			Process p=Runtime.getRuntime().exec(cmds);
			Thread.sleep(10000);
		}
		
		 private static AppiumDriver Launch_Native(String sTestName,String sBrowserPref) throws Exception {

			 // 	String sDesc = Logs.log(sTestName);
				AppiumDriver driver = null;
				try { 
				   
					String sPackageFldr = Utils.hSystemSettings.get("packageFolder"); 	
					
					DesiredCapabilities oCap = new DesiredCapabilities();
				    oCap.setCapability("deviceName", "Android"); 
					oCap.setCapability("platformName", "Android");
					oCap.setCapability("udid", Utils.h2TestName_TestParams.get(sTestName).get("Exe_Platform"));

					oCap.setCapability("deviceConnectIgnoreSession", true);
					oCap.setCapability("newCommandTimeout", Utils.hEnvParams.get("OBJ_TIMEOUT"));
					oCap.setCapability("deviceConnectUserName", Utils.hEnvParams.get("Dev_ConnUserID"));
					oCap.setCapability("deviceConnectApiKey", Utils.hEnvParams.get("Dev_ConnAPIKey"));
			
		
					if (h2AndroidPrefs.containsKey(sTestName))
					{	
													
						if (h2AndroidPrefs.get(sTestName).containsKey("App_WaitActivity") && h2AndroidPrefs.get(sTestName).containsKey("App_Package") && h2AndroidPrefs.get(sTestName).containsKey("App_Activity"))	{
							oCap.setCapability("appWaitActivity", h2AndroidPrefs.get(sTestName).get("App_WaitActivity"));
							oCap.setCapability("appPackage", h2AndroidPrefs.get(sTestName).get("App_Package")); 
							oCap.setCapability("appActivity", h2AndroidPrefs.get(sTestName).get("App_Activity")); 
							}
						else {
							throw new Exception(sTestName+": App Details are not complete");
						}
					}
					else {
						throw new Exception(sTestName+": You Need to fill App Details : App_WaitActivity , App_Package , App_Activity :");
					}
					oCap.setCapability("chromedriverExecutable", sPackageFldr+"chromedriver.exe");
					
					startAppium("");
					URL sURLNative = new URL("http://localhost:"+ Utils.h2TestName_TestParams.get(sTestName).get("Port") +"/wd/hub");
					driver = new AndroidDriver(sURLNative, oCap); 	
					
				}
			catch(Exception e)
				{
				throw e;
				}
			return driver;
		
		}
		
		  	public static void startAppium(String sTest_FullName) throws IOException, InterruptedException {
		  		
		  		String sStartAppiumPath = System.getProperty("user.dir")+"\\Batch_Android\\StartAppium_TC_Enhanced_Push_To_Talk__1__0715f7eafa052a38_4222__ANDROID_WEB";
		  		
				String cmds[] = {"cmd", "/c", "start "+ sStartAppiumPath};
				Process p=Runtime.getRuntime().exec(cmds); 
				Thread.sleep(2*10000);
		  	}
		 
		 
		private static AppiumDriver Launch_Web(String sTestName,String sBrowserPref) throws Exception {
			  
				
				AppiumDriver driver = null;
				try { 
				   
					
					DesiredCapabilities oCap = new DesiredCapabilities();
					oCap.setCapability("deviceName", "Android"); 
					oCap.setCapability("platformName", "Android");
					oCap.setCapability("udid", Utils.h2TestName_TestParams.get(sTestName).get("Exe_Platform"));
					oCap.setCapability("deviceConnectIgnoreSession", true);
					oCap.setCapability("deviceConnectUserName", Utils.hEnvParams.get("Dev_ConnUserID"));
					oCap.setCapability("deviceConnectApiKey", Utils.hEnvParams.get("Dev_ConnAPIKey"));
					oCap.setCapability("newCommandTimeout", Utils.hEnvParams.get("OBJ_TIMEOUT"));
			
					if (h2AndroidPrefs.containsKey(sTestName)) {
						
						if (h2AndroidPrefs.get(sTestName).containsKey("Browser_Name") && h2AndroidPrefs.get(sTestName).containsKey("App_Package") && h2AndroidPrefs.get(sTestName).containsKey("App_Activity"))	{
							oCap.setCapability("browserName", h2AndroidPrefs.get(sTestName).get("Browser_Name"));	
							oCap.setCapability("appPackage", h2AndroidPrefs.get(sTestName).get("App_Package")); 
							oCap.setCapability("appActivity", h2AndroidPrefs.get(sTestName).get("App_Activity")); 
						}
						else {
							oCap.setCapability("browserName", "Chrome");
							oCap.setCapability("appPackage", "com.android.chrome"); 
							oCap.setCapability("appActivity", "com.google.android.apps.chrome.Main"); 
							}	
					}
					else {
						oCap.setCapability("browserName", "Chrome");
						oCap.setCapability("appPackage", "com.android.chrome"); 
						oCap.setCapability("appActivity", "com.google.android.apps.chrome.Main");
						}

					URL sURL = new URL("http://"+Utils.h2TestName_TestParams.get(sTestName).get("Cloud_IP")+"/Appium");
					driver = new AndroidDriver(sURL, oCap); 
					
			}
					
				catch(Exception e)	{
					
					throw e;
				}
				return driver;

			}
		
		private static String takeScreenshot(AppiumDriver driver) throws IOException, HeadlessException, AWTException  { 
			try {

			BufferedImage screenshotBase64 = new AShot().shootingStrategy(new ViewportPastingStrategy(1000)).takeScreenshot(driver).getImage();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
		     ImageIO.write(screenshotBase64, "png", Base64.getEncoder().wrap(os));
		     return os.toString(StandardCharsets.ISO_8859_1.name());	
			
				
			} catch (Exception e) {
				return "";	
			}
			
		}	
	
	
	static String CheckExist(AppiumDriver lDriver, WebElement objDesc) throws Exception {
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
		AppiumDriver lDriver= Android.getAndroidDriver(sTestName);
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
	static void forceEnter(AppiumDriver driver, WebElement oEle, String val) {

		JavascriptExecutor executor = (JavascriptExecutor) driver;
		executor.executeScript("arguments[0].value="+val+";", oEle);
	}
	
	static void forceClick(AppiumDriver driver, WebElement oEle) {
		
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		executor.executeScript("arguments[0].click();", oEle);
	}
	
	@SuppressWarnings("unused")
	static boolean isDisplayed(WebElement oEle) {
		return oEle.isDisplayed();
	}
	
	 static void checkReady( String sTestName, WebElement oEle) throws InterruptedException, HeadlessException, IOException, AWTException	{
		
			Thread.sleep(1000);
		
			AppiumDriver lDriver = Android.getAndroidDriver(sTestName);
			WebDriverWait wait = new WebDriverWait(lDriver, Long.parseLong(Utils.hEnvParams.get("OBJ_TIMEOUT")));
			wait.until(ExpectedConditions.elementToBeClickable(oEle));
			Reporter.print(sTestName, "ELEMENT IS READY");

	}
		
	
}

		
}
	