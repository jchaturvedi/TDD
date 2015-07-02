package com.tooling.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.tooling.ApexClass;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class LoginLogic {
	
	static PartnerConnection connection ;
	ConnectorConfig toolingConfig = new ConnectorConfig();
	com.sforce.soap.tooling.SoapConnection toolingConnection = null ;
	String loginStatus ;
	com.sforce.soap.partner.LoginResult result = null;
	ConnectorConfig partnerConfig = new ConnectorConfig();
	private  WordprocessingMLPackage wordMLPackage;
	
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
			fetchApexClassMethod();
		}else{
			loginStatus = "error";
		}
		return loginStatus;				
	}
	
	private void fetchApexClassMethod(){
		QueryResult classList = null;
		try {
			classList = toolingConnection.query("SELECT Name, Status, LastModifiedDate, FullName FROM ApexClass");
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("QueryResult SIZE:--- "+ classList.getSize());
		
		List<ApexClass> apexClassFinalList = new ArrayList<ApexClass>();
		List<Map<String, String>> apexClassMapList = new ArrayList<Map<String, String>>();
		SObject[] apexClassList = classList.getRecords();
		for(int i = 0; i < apexClassList.length; ++i){
			ApexClass classRecord = (ApexClass) apexClassList[i];
			apexClassFinalList.add(classRecord);
		}
		
		for(ApexClass a : apexClassFinalList){
			Map<String,String> documentPlaceHolderApexClassMap = new HashMap<String, String>();
			documentPlaceHolderApexClassMap.put("FirstClass", a.getFullName());
			documentPlaceHolderApexClassMap.put("Method", a.getStatus());
			documentPlaceHolderApexClassMap.put("Description", a.getCreatedById());
			apexClassMapList.add(documentPlaceHolderApexClassMap);
		}
		
		try {
			wordMLPackage = WordprocessingMLPackage.load(new java.io.File("\\Jai\\Eclipse WS\\HerokuProj\\src\\com\\tooling\\custom\\Quality Initiative_TDD.docx"));
		} catch (Docx4JException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
		Map<String,String> repl2 = new HashMap<String, String>();
		repl2.put("FirstClass", "function2");
		repl2.put("Method", "desc2");
		repl2.put("Description", "period2");
 
 
		try {
			replaceTable(new String[]{"FirstClass","Method","Description"}, apexClassMapList, wordMLPackage);
			System.out.println("Done ");
		} catch (Docx4JException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
		System.out.println("In getAllElementFromObject method ");
		List<Object> result = new ArrayList<Object>();
		if (obj instanceof JAXBElement) obj = ((JAXBElement<?>) obj).getValue();
		if (obj.getClass().equals(toSearch)){
			result.add(obj);
		}else if (obj instanceof ContentAccessor) {
			List<?> children = ((ContentAccessor) obj).getContent();
			for (Object child : children) {
				result.addAll(getAllElementFromObject(child, toSearch));
			}
 
		}
		return result;
	}
	
	private void replaceTable(String[] placeholders, List<Map<String, String>> textToAdd,
		WordprocessingMLPackage template) throws Docx4JException, JAXBException {
		List<Object> tables = getAllElementFromObject(template.getMainDocumentPart(), Tbl.class);
 
		// 1. find the table
		Tbl tempTable = getTemplateTable(tables, placeholders[0]);
		List<Object> rows = getAllElementFromObject(tempTable, Tr.class);
		
		// first row is header, second row is content
		if (rows.size() == 2) {
			// this is our template row
			Tr templateRow = (Tr) rows.get(1);
 
			for (Map<String, String> replacements : textToAdd) {
				// 2 and 3 are done in this method
				addRowToTable(tempTable, templateRow, replacements);
			}
			
			// 4. remove the template row
		tempTable.getContent().remove(templateRow);
		wordMLPackage.save(new java.io.File("C:\\result.docx"));
		}
	}
	
	private Tbl getTemplateTable(List<Object> tables, String templateKey) throws Docx4JException, JAXBException {
		System.out.println("In getTemplateTable method "+templateKey);
		for (Iterator<Object> iterator = tables.iterator(); iterator.hasNext();) {
			Object tbl = iterator.next();
			List<?> textElements = getAllElementFromObject(tbl, Text.class);
			for (Object text : textElements) {
				Text textElement = (Text) text;
				if (textElement.getValue() != null && textElement.getValue().equals(templateKey)){
					return (Tbl) tbl;
				}
			}
		}
		return null;
	}
	
	private static void addRowToTable(Tbl reviewtable, Tr templateRow, Map<String, String> replacements) {
		System.out.println("***************addRowToTable ****************8");
		Tr workingRow = (Tr) XmlUtils.deepCopy(templateRow);
		List<?> textElements = getAllElementFromObject(workingRow, Text.class);
		for (Object object : textElements) {
			Text text = (Text) object;
			String replacementValue = (String) replacements.get(text.getValue());
			if (replacementValue != null){
				text.setValue(replacementValue);
			}
		}
		reviewtable.getContent().add(workingRow);
	}
	
}
