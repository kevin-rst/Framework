package com.app.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import com.app.framework.model.UrlMethodMapping;
import com.app.framework.model.UrlMethod;
import java.lang.reflect.Constructor;
import com.app.framework.model.ModelAndView;

public class FrontControllerServlet extends HttpServlet {
    List<String> listControllers;
    Map<UrlMethod, UrlMethodMapping> urlMethodMappings;
    String prefix;
    String suffix;

    public void init() throws ServletException {
        try {
            prefix = (String) getServletContext().getAttribute("prefix");
            suffix = (String) getServletContext().getAttribute("suffix");
            listControllers = (List<String>) getServletContext().getAttribute("listControllers");
            urlMethodMappings = (Map<UrlMethod, UrlMethodMapping>) getServletContext().getAttribute("urlMethodMappings");
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

            try {
                Constructor<?> constructor = supported.getClazz().getDeclaredConstructor();
                Object controllerInstance = constructor.newInstance();

                Object result = supported.getMethod().invoke(controllerInstance);
                
                if (result != null) {
                    if (result instanceof ModelAndView) {
                        ModelAndView model = (ModelAndView) result;

                        String view = model.getView();
                        Map<String, Object> modelData = model.getModel();

                        for (Map.Entry<String, Object> entry : modelData.entrySet()) {
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            
                            request.setAttribute(key, value);
                        }

                        request.getRequestDispatcher(prefix + view + suffix).forward(request, response);
                    } else {
                        out.println("Output: " + result);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace(out);
            }
        } else {
            out.println("Here are all supported URLs: ");

            for (UrlMethod urlMeth: urlMethodMappings.keySet()) {
                out.println("- " + urlMeth.getHttpMethod() + " " + urlMeth.getUrl());
            }
        }

    }

}
