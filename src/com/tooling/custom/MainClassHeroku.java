package com.tooling.custom;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MainClassHeroku extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		System.out.println("IN SERVLET ");
		
		String userName = request.getParameter("userid");
		String password = request.getParameter("pwd");	
		LoginLogic l = new LoginLogic();
		String result = l.initMethod(userName, password);
		System.out.println("IN SERVLET result " + result);
	}
}
