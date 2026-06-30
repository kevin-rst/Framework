package com.app.framework.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import mg.itu.framework.annotation.Controller;
import mg.itu.framework.annotation.UrlMapping;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.app.framework.model.UrlMethodMapping;
import com.app.framework.model.UrlMethod;
import com.app.framework.util.Util;

public class AppInitializer implements ServletContextListener {
    private List<String> listControllers = new ArrayList<>();
    private Map<UrlMethod, UrlMethodMapping> urlMethodMappings = new HashMap<>();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        List<String> packageNames = Util.splitString(servletContext.getInitParameter("scanPackages"), ",");

        try {
            listControllers = Util.findClasses(packageNames, Controller.class);
            Util.findUrlMethodMappings(packageNames, urlMethodMappings, Controller.class, UrlMapping.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing application", e);
        }

        servletContext.setAttribute("listControllers", listControllers);
        servletContext.setAttribute("urlMethodMappings", urlMethodMappings);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
       
    }
    
}
