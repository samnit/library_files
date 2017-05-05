package com.automatics.packages.library.common;


import java.awt.*;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import org.apache.commons.lang3.StringUtils;
import com.automatics.packages.library.common.Utils;
import com.jcraft.jsch.*;

public class SSH {
	
	 static Hashtable<String, Channel> hSSHName_To_Channel = new Hashtable<String, Channel>();
	 static Hashtable<String, String> hSSHName_PWD = new Hashtable<String,String>();
	 static Hashtable<String, String> hSSHName_LASTResponse = new Hashtable<String,String>();
	 static Hashtable<String, String> hSSHName_Prompt = new Hashtable<String,String>();
	 static Hashtable<String, String> hSSHName_Status = new Hashtable<String,String>();
	 static Hashtable<String, String> hTestCase_SSHName = new Hashtable<String,String>();
	
	 public static void sshConnect(String sTestName, String sConnection) throws IOException, HeadlessException, AWTException  {
			
		String sDesc = "", command = "",sUsrVal, sUrl, userId, sPassword, sshResponse, promptLastLine, sVarName =""; 
			
		try {
				if (sConnection.contains("{")&&sConnection.contains("}")) {
					sVarName = sConnection.split("\\{")[1].replace("}", "");
					sUsrVal = Utils.Helper.validateUserInput(sTestName, sConnection);
				
				} else {
					sVarName = sTestName.split("\\|")[1];
					sTestName = sTestName.split("\\|")[0];
					sUsrVal = sConnection; 
				}
			sUrl = sUsrVal.split("\\|")[0];
			userId = sUsrVal.split("\\|")[1];
			sPassword = sUsrVal.split("\\|")[2];
			if(StringUtils.countMatches(sUsrVal, "|")>3)
				command = sUsrVal.split("\\|",4)[3];
			hSSHName_PWD.put("Password", sPassword);
			sDesc = Logs.log(sTestName) + "Connect To SSH " + sVarName + ": (" + sUrl + ", " + userId + ", " + sPassword + ")";
						
			JSch jSch = new JSch(); Session session = jSch.getSession(userId, sUrl); 
			UserInfo ui=new RSA.MyUserInfo(); session.setUserInfo(ui);
			session.connect(); 
			Channel channel = session.openChannel("shell"); 
			Thread.sleep(2000);
	       	
	        Expect expect = new ExpectBuilder().withOutput(channel.getOutputStream()).withInputs(channel.getInputStream(),
	        			channel.getExtInputStream()).withEchoInput(System.out).withEchoOutput(System.err).withTimeout(30, TimeUnit.SECONDS).withExceptionOnFailure().build();		
			
			final ByteArrayOutputStream baos = new ByteArrayOutputStream(); channel.setOutputStream(baos);
			
			channel.connect();
			Thread.sleep(5000);
			 
		    String lastOut_Line = baos.toString().substring(baos.toString().lastIndexOf("\n"));
			if (StringUtils.containsIgnoreCase(lastOut_Line, "assword")) 
				expect.send(SSH_Helper.GetPassword(false)+"\n");
			Thread.sleep(3000);
			sshResponse = baos.toString();
			promptLastLine = sshResponse.substring(sshResponse.lastIndexOf("\n"));
			
			
			if (sTestName.equalsIgnoreCase("PreRequisite")) 
				Logs.log("PreRequisite", sDesc);
			else 
				Reporter.print(sTestName, sDesc);
				
			Logs.log(sTestName, "CONNECTED SSH SESSION (" + sUrl + ", " + userId + ")");
			Logs.log(sTestName, "\n********************************************RESPONSE********************************************\n" + sshResponse + "\n********************************************RESPONSE END********************************************\n");
			if (!sTestName.equalsIgnoreCase("PreRequisite")) {
				sVarName= sVarName + "__" + sTestName;
			}
			hSSHName_To_Channel.put(sVarName, channel);
			
			if (!command.isEmpty()) {
				sshResponse = SSH_Helper.connect_SSH(sTestName, channel, command);
				promptLastLine = sshResponse.substring(sshResponse.lastIndexOf("\n"));
			}
			
			
			
				hSSHName_LASTResponse.put(sVarName, sshResponse.replaceAll("\u0007", ""));
				hSSHName_Prompt.put(sVarName, promptLastLine);
				hSSHName_Status.put(sVarName, "FREE");
			
		
		} catch(Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
	}
		
	public static void sshSet(String sTestName, String connectToChannel) throws IOException, HeadlessException, AWTException  {
		
		String sDesc ="";
	
		try {
			
			if(!connectToChannel.isEmpty()) {
				connectToChannel = connectToChannel.split("\\{")[1].replace("}", "");
				sDesc = Logs.log(sTestName) + " : Connect To Channel :" + connectToChannel;	
				if (hSSHName_Status.containsKey(connectToChannel)) {
					if (!hSSHName_Status.get(connectToChannel).equalsIgnoreCase("FREE")) {
						SSH_Helper.waitForProcessToFree(sTestName, connectToChannel);
					}
				}
				else if (hSSHName_Status.containsKey(connectToChannel + "__" +sTestName)){
					connectToChannel = connectToChannel+ "__" +sTestName;
					if (!hSSHName_Status.get(connectToChannel).equalsIgnoreCase("FREE")) {
						SSH_Helper.waitForProcessToFree(sTestName, connectToChannel);
					}
				} else {
					Reporter.print(sTestName, "*EXIT_ON_FAIL*", sDesc + "Error: You have not connected to any such channel", "True", "False", false);
					
				}
			}
			else {
				sDesc = Logs.log(sTestName) + " : Connect To any free Channel ";	
				connectToChannel = SSH_Helper.waitForProcessToFree(sTestName, connectToChannel);
			}
			hSSHName_Status.put(connectToChannel, sTestName);
			hTestCase_SSHName.put(sTestName, connectToChannel);	
			Reporter.print(sTestName, sDesc +" :: Performed");
			
		} catch(Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	
	public static String sshExecuteCmd(String sTestName, String cmd,String expCond) throws HeadlessException, AWTException, IOException {
		
		Channel tcChannel = null; String sshResponse ="",promptLastLine, sDesc = "", lastOut;
		int timeOut, i=0;
		
		try {
			cmd = Utils.Helper.validateUserInput(sTestName, cmd);
			expCond = Utils.Helper.validateUserInput(sTestName, expCond);
			
			sDesc = Logs.log(sTestName)+ " : Execute Command (" + cmd + ")\n";
			
			if (!hTestCase_SSHName.containsKey(sTestName)) 
				sshSet(sTestName, "");
			
			tcChannel = hSSHName_To_Channel.get(hTestCase_SSHName.get(sTestName)); 
			lastOut = hSSHName_LASTResponse.get(hTestCase_SSHName.get(sTestName));
			promptLastLine = hSSHName_Prompt.get(hTestCase_SSHName.get(sTestName));
		
			Reporter.print(sTestName, promptLastLine);

			if (Utils.hEnvParams.containsKey("SSH_CommandTimeout"))
				timeOut = Integer.parseInt(Utils.hEnvParams.get("SSH_CommandTimeout"))*60;
			else
				timeOut = 1800;  //10 minutes
			
			if (expCond.isEmpty()||lastOut.contains(expCond)) {
				
				Expect expect = new ExpectBuilder().withOutput(tcChannel.getOutputStream()).withInputs(tcChannel.getInputStream(), tcChannel.getExtInputStream())
						.withEchoInput(System.out).withEchoOutput(System.err).withTimeout(30, TimeUnit.SECONDS).withExceptionOnFailure().build();		
				
				final ByteArrayOutputStream baos = new ByteArrayOutputStream(); tcChannel.setOutputStream(baos);
				expect.sendLine(cmd.trim());
		
				Thread.sleep(2000);
				sshResponse = baos.toString();
				String lastOut_Line = baos.toString().substring(baos.toString().lastIndexOf("\n"));
				
				if (StringUtils.containsIgnoreCase(lastOut_Line, "assword")) {
				
					expect.send(SSH_Helper.GetPassword(false)+"\n");
					Thread.sleep(5000);
					sshResponse = baos.toString();
					
					lastOut_Line = sshResponse.substring(sshResponse.lastIndexOf("\n"));
				}
				
				while (!baos.toString().substring(baos.toString().lastIndexOf("\n")).contains(promptLastLine.substring(0, promptLastLine.length() - 1)) && (i<timeOut)) { 
					Thread.sleep(1000); i++; 
				}

				sshResponse = baos.toString();
				SSH.hSSHName_LASTResponse.put(hTestCase_SSHName.get(sTestName), sshResponse.replaceAll("\u0007", ""));
			}
			else {
				sshResponse = " : Expected Condition Did not met";
			}
			Reporter.print(sTestName, sDesc + "\n********************************************RESPONSE********************************************\n"+ sshResponse + "\n********************************************RESPONSE END********************************************\n");
				    
		} catch (Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
	    return sshResponse;
	}

	public static void sshExecuteCmd_ExportResult(String sTestName, String cmd,String expCond) throws IOException, HeadlessException, AWTException  {
	
		String sshResponse ="", sDesc = "";
			
		try {
			cmd = Utils.Helper.validateUserInput(sTestName, cmd);
			expCond = Utils.Helper.validateUserInput(sTestName, expCond);
			
			sDesc = Logs.log(sTestName);	
			sshResponse = sshExecuteCmd(sTestName, cmd,expCond);
			Logs.log(sTestName +"_SSH_Output","\n********************************************RESPONSE********************************************\n" + sshResponse + "\n********************************************RESPONSE END********************************************\n");
			Reporter.print(sTestName, sDesc + "\nSaved SSH response at location:" + Reporter.sExecutionLogFldr + "/" + sTestName + "_SSH_Output.log" );
			
		} catch(Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
	}
	
	public static void sshExecuteCmd_VerifyResult(String sTestName, String cmd,String sVal) throws IOException, HeadlessException, AWTException  {
		
		String sshResponse ="", sDesc = "", sValue=cmd, sExpVal,expCond=""; boolean bStatus = false;
		
		try {
			cmd = Utils.Helper.validateUserInput(sTestName, cmd);
			sVal = Utils.Helper.validateUserInput(sTestName, sVal);
			cmd = Reporter.filterUserInput(cmd);
			
			if(sVal.contains("|")) {
				expCond=sVal.split("\\||")[0];
				sExpVal=sVal.split("\\||")[1];
			}
			else {
				sExpVal=sVal;
			}
			sDesc = Logs.log(sTestName)+ " : Execute Command (" + cmd + ") , export result to ("+sTestName +"_SSH_Output.log) file and Verify whether result contains( " +sExpVal +" )";		
			
			sshResponse = sshExecuteCmd(sTestName, cmd,expCond);
			Logs.log(sTestName +"_SSH_Output",  "\n********************************************RESPONSE********************************************\n" + sshResponse + "\n********************************************RESPONSE END********************************************\n");
			
			
			if(sshResponse.contains(sExpVal))
				bStatus = true;
			Reporter.print(sTestName, sValue, sDesc, "True", String.valueOf(bStatus), bStatus);
			Logs.log(sTestName, "\nSaved SSH response at location:" + Reporter.sExecutionLogFldr + "/" + sTestName + "_SSH_Output.log" );
							
			} catch(Exception e) { 
				Reporter.printError(sTestName, e, sDesc);
			}
	}
	public static void sshVerify_InResult(String sTestName, String sUserVal) throws IOException, HeadlessException, AWTException  {
		
		String sshResponse ="", sDesc = ""; boolean bStatus = false;
		
		try {
			sUserVal = Utils.Helper.validateUserInput(sTestName, sUserVal);
			sDesc = Logs.log(sTestName)+ " : Verify whether result of last Command Executed contains ( " +sUserVal +" )";	
			sUserVal = Reporter.filterUserInput(sUserVal);
			
			if (hTestCase_SSHName.containsKey(sTestName)) {
				sshResponse = hSSHName_LASTResponse.get(hTestCase_SSHName.get(sTestName));
			}
			else {
				sshResponse = hSSHName_LASTResponse.get(hSSHName_LASTResponse.keySet().toArray()[0]);
			}
			if(sshResponse.contains(sUserVal))
				bStatus = true;
			Reporter.print(sTestName, sUserVal, sDesc, "True", String.valueOf(bStatus), bStatus);
							
			} catch(Exception e) { 
				Reporter.printError(sTestName, e, sDesc);
			}
	}

	public static void sshFree(String sTestName) throws IOException, HeadlessException, AWTException  {
		
		String sDesc = ""; 
			
		try {
			sDesc = Logs.log(sTestName);	
			hTestCase_SSHName.put(sTestName, "FREE");
			hSSHName_Status.put(hTestCase_SSHName.get(sTestName), "FREE");
			Reporter.print(sTestName, sDesc +" :: Performed");
		} catch(Exception e) {
			Reporter.printError(sTestName, e, sDesc);
		}
		}
}	

	class SSH_Helper {
		
		static String GetPassword(boolean flag) throws InterruptedException, UnsupportedFlavorException, IOException {
			String info = "";
			try{
				info = SSH.hSSHName_PWD.get("Password");
				//if (info.equals("RSA")) {
					//info = RSA.GetToken(flag); } 
			} catch (Exception e) {System.out.println("Error");}
			return info;
		}
		
		static String waitForProcessToFree(String sTestName, String connectToChannel) throws InterruptedException, HeadlessException, IOException, AWTException {

			int timeOut = 20, i = 1, foundSession=0;  String sessionName ="";
			if (Utils.hEnvParams.containsKey("SSH_SessionTimeout"))
				timeOut = Integer.parseInt(Utils.hEnvParams.get("SSH_SessionTimeout"));
			if(connectToChannel.isEmpty()) {
				while (i<=timeOut) {
					for (HashMap.Entry< String, String> checkFunc  : SSH.hSSHName_Status.entrySet()) {
				   		if(checkFunc.getValue().equalsIgnoreCase("FREE")) {
				   			foundSession++;
				   			sessionName = checkFunc.getKey();
				   			break;
				   		}
				   		
				   	}
					if(foundSession==0) {
					Thread.sleep(60000);
					i++;
					if(i>timeOut) 
						Reporter.print(sTestName, "*EXIT_ON_FAIL*", "TestCase could not acquire any session for more than "+ timeOut + " minutes", "NA", "NA", false);
					}
					else 
						break;
				}
			}
			else {
				while (i<=timeOut) {
					if(!SSH.hSSHName_Status.get(connectToChannel).equalsIgnoreCase("FREE")) {
						Thread.sleep(60000);
						i++;
						if(i>timeOut) 
							Reporter.print(sTestName, "*EXIT_ON_FAIL*", "TestCase could not acquire session "+connectToChannel+ " for more than "+ timeOut + " minutes", "NA", "NA", false);
					}
					else 
						break;
					}			
			}
			return sessionName;
		}

		static String connect_SSH(String sTestName, Channel tcChannel, String command) throws HeadlessException, IOException, AWTException {
			
			String sDesc ="" ; String[] cmd = {""};
			String sshResponse ="", lastOut_Line = null;
				
			try {
			
				sDesc = Logs.log(sTestName);
				if (command.contains("|")) {
					cmd = command.split("\\|"); }
				else
					cmd[0] = command;
				
				Expect expect = new ExpectBuilder().withOutput(tcChannel.getOutputStream()).withInputs(tcChannel.getInputStream(),
						tcChannel.getExtInputStream()).withEchoInput(System.out).withEchoOutput(System.err).withTimeout(30, TimeUnit.SECONDS).withExceptionOnFailure().build();		
			
				final ByteArrayOutputStream baos = new ByteArrayOutputStream(); tcChannel.setOutputStream(baos);
				
				for (String commandExecute : cmd) {
					expect.sendLine(commandExecute.trim());
					Thread.sleep(5000);
					sshResponse = baos.toString();
					lastOut_Line = sshResponse.substring(sshResponse.lastIndexOf("\n"));
					if (StringUtils.containsIgnoreCase(lastOut_Line, "assword")) {
						expect.send(SSH_Helper.GetPassword(false)+"\n");
						Thread.sleep(5000);
						sshResponse = baos.toString();
						lastOut_Line = sshResponse.substring(sshResponse.lastIndexOf("\n"));
					}
					Logs.log(sTestName, sDesc + ":: Executed Command : " +commandExecute +  "\n********************************************RESPONSE********************************************\n" + sshResponse + "\n********************************************RESPONSE END********************************************\n");
				}	
			
			} catch(Exception e) {
				Reporter.printError(sTestName, e, sDesc, "");
		}
		return sshResponse;
		}
	
 }
	
	class RSA {
		static class MyUserInfo implements UserInfo, UIKeyboardInteractive{
			
			public String getPassword(){ return null; }
		    
		    public boolean promptYesNo(String str){ return true; }

		    public String getPassphrase(){ return null; }
		    
		    public boolean promptPassphrase(String message) { return false; }
		    
		    public boolean promptPassword(String message){ return true; }
		    public void showMessage(String message){}

		    public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo){

		      String pwd = null;
		      try { pwd = SSH_Helper.GetPassword(true);	} 
		      catch (InterruptedException e1) { e1.printStackTrace(); } 
		      catch (UnsupportedFlavorException e1) { e1.printStackTrace();	} 
		      catch (IOException e1) { e1.printStackTrace();}
		      
		      String[] response=new String[prompt.length];
		      for(int i=0; i<prompt.length; i++){ response[i] = pwd; }
		      return response;
		    }
		}
	}
           