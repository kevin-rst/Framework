package com.app.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import mg.itu.framework.annotation.Controller;
import com.app.framework.util.Util;

public class FrontControllerServlet extends HttpServlet {
    List<String> listControllers = new ArrayList<>();

    public void init() throws ServletException {
        String packageName = this.getInitParameter("scanPackage");
        try {
            listControllers = Util.findClasses(packageName, Controller.class);
        } catch (Exception e) {
            throw new ServletException("Error initializing FrontControllerServlet", e);
        }
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String path = request.getRequestURI();
        String context = request.getContextPath();

        String relativePath = path.substring(context.length());
        out.println("Requested Path: " + relativePath);

        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            out.println(key + "=" + String.join(",", values));
        }

        out.println("List of Controllers:");
        for (String controller : listControllers) {
            out.println(controller);
        }
    }

}
