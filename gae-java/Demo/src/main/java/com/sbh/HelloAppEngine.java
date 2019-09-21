package com.sbh;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet(
    name = "HelloAppEngine",
    urlPatterns = {"/position"}
)
public class HelloAppEngine extends HttpServlet {

	private static final Position[] POSITIONS = {
			new Position(40.914297, -73.123638, "SAC"),
			new Position(40.909657, -73.116138, "Stony Brook Hospital"),
			new Position(40.921307, -73.127839, "Stony Brook LIRR Station")
	};
	
	@Override
	public void init() throws ServletException {
	}	
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws IOException {
		// Process URI
		String pathInfo = request.getPathInfo();
		
		responseConfig(response);
		sendJson();
	}
	
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