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
import com.fasterxml.jackson.databind.ObjectMapper;

public class AppInitializer implements ServletContextListener {
    private List<String> listControllers = new ArrayList<>();
    private List<String> listRequestPackages = new ArrayList<>();
    private Map<UrlMethod, UrlMethodMapping> urlMethodMappings = new HashMap<>();
    private String prefix;
    private String suffix;
    private ObjectMapper mapper;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        List<String> packageNames = Util.splitString(servletContext.getInitParameter("scanPackages"), ",");
        listRequestPackages = Util.splitString(servletContext.getInitParameter("requestPackages"), ",");
        prefix = servletContext.getInitParameter("prefix");
        suffix = servletContext.getInitParameter("suffix");
        mapper = new ObjectMapper();

        try {
            listControllers = Util.findClasses(packageNames, Controller.class);
            Util.findUrlMethodMappings(packageNames, urlMethodMappings, Controller.class, UrlMapping.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing application", e);
        }

        servletContext.setAttribute("listControllers", listControllers);
        servletContext.setAttribute("listRequestPackages", listRequestPackages);
        servletContext.setAttribute("urlMethodMappings", urlMethodMappings);
        servletContext.setAttribute("prefix", prefix);
        servletContext.setAttribute("suffix", suffix);
        servletContext.setAttribute("mapper", mapper);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
       
    }
    
}
