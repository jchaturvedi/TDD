package com.tooling.custom;

import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class LoginClass {
	
	static PartnerConnection connection ;
	ConnectorConfig toolingConfig = new ConnectorConfig();
	com.sforce.soap.tooling.SoapConnection toolingConnection = null ;
	String loginStatus ;
	com.sforce.soap.partner.LoginResult result = null;
	ConnectorConfig partnerConfig = new ConnectorConfig();
	
	public  String initMethod(String userName, String password){		
		partnerConfig.setManualLogin(true);
		System.out.println("SECOND STEP ");
		try {
			connection = Connector.newConnection(partnerConfig);
				System.out.println("****** STEP");
				result = connection.login(userName, password);
				loginStatus =  "Please enter proper data";
			System.out.println("3 STEP ");
			toolingConfig.setSessionId(result.getSessionId());
			toolingConfig.setServiceEndpoint(result.getServerUrl().replace('u', 'T'));
			System.out.println("4 STEP ");
			toolingConnection = com.sforce.soap.tooling.Connector.newConnection(toolingConfig);
			System.out.println("5 STEP ");
		} catch (ConnectionException e) {
			loginStatus =  e.getMessage();
		}
		System.out.println("toolingConnection:--- "+ toolingConnection.getSessionHeader().getSessionId());
		String loginStatus;
		if( toolingConnection.getSessionHeader().getSessionId() != null){
			loginStatus = "success";
			//fetchApexClassMethod();
		}else{
			loginStatus = "error";
		}
		return loginStatus;				
	}
	
}
