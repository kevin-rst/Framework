package com.app.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import mg.itu.framework.annotation.Controller;
import com.app.framework.util.Util;
import com.app.framework.model.UrlMethodMapping;
import com.app.framework.model.UrlMethod;
import mg.itu.framework.annotation.UrlMapping;

public class FrontControllerServlet extends HttpServlet {
    List<String> listControllers = new ArrayList<>();
    Map<UrlMethod, UrlMethodMapping> urlMethodMappings = new HashMap<>();

    public void init() throws ServletException {
        List<String> packageNames = Util.splitString(this.getInitParameter("scanPackages"), ",");
        try {
            listControllers = Util.findClasses(packageNames, Controller.class);
            Util.findUrlMethodMappings(packageNames, urlMethodMappings, Controller.class, UrlMapping.class);
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
        
        out.println();

        out.println("Classes annotated by @Controller:");
        for (String controller : listControllers) {
            out.println(controller);
        }

        out.println();

        UrlMethod urlMethod = new UrlMethod(relativePath, request.getMethod().toUpperCase());
        UrlMethodMapping supported = urlMethodMappings.get(urlMethod);
        if (supported != null) {
            out.println("URL info: " + urlMethod.getHttpMethod()  + " " + urlMethod.getUrl() + " " + supported.getClazz().getName() + " -> " + supported.getMethod().getName());
        } else {
            out.println("Here are all supported URLs: ");

            for (UrlMethod urlMeth: urlMethodMappings.keySet()) {
                out.println("- " + urlMeth.getHttpMethod() + " " + urlMeth.getUrl());
            }
        }

    }

}
