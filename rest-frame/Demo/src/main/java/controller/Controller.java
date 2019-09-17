package main.java.controller;

import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;

import main.java.model.TestModel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/main/*")
public class Controller extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
	@Override
	public void init() throws ServletException {}

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        responseConfig(response);
        
        //Path
        String pathInfo = request.getPathInfo();
        
        TestModel model = new TestModel();
        model.setName("Name");
        
        sendJson(response, model);
	}

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {}
	
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {}
	
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {}

	private void sendJson(HttpServletResponse response, Object model)
	        throws IOException {

	    Gson gson = new Gson();
        String json = gson.toJson(model);   
        
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
	}
	
	private void responseConfig(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
	}
}
