package com.app.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import com.app.framework.model.UrlMethodMapping;
import com.app.framework.model.UrlMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Constructor;
import com.app.framework.model.ModelAndView;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import mg.itu.framework.annotation.WebAPI;
import com.app.framework.util.Util;

public class FrontControllerServlet extends HttpServlet {
    List<String> listControllers;
    Map<UrlMethod, UrlMethodMapping> urlMethodMappings;
    String prefix;
    String suffix;
    ObjectMapper mapper;

    public void init() throws ServletException {
        try {
            prefix = (String) getServletContext().getAttribute("prefix");
            suffix = (String) getServletContext().getAttribute("suffix");
            listControllers = (List<String>) getServletContext().getAttribute("listControllers");
            urlMethodMappings = (Map<UrlMethod, UrlMethodMapping>) getServletContext().getAttribute("urlMethodMappings");
            mapper = (ObjectMapper) getServletContext().getAttribute("mapper");
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
        StringWriter buffer = new StringWriter();
        PrintWriter out = new PrintWriter(buffer);

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

            Object result = null;

            try {
                Constructor<?> constructor = supported.getClazz().getDeclaredConstructor();
                Object controllerInstance = constructor.newInstance();

                boolean hasParam = false;
                for (Class<?> paramType: supported.getMethod().getParameterTypes()) {
                    if (paramType.equals(WebApplicationContext.class)) {

                        WebApplicationContext ctx =
                                WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());

                        result = supported.getMethod().invoke(controllerInstance, ctx);

                        hasParam = true;
                        break;
                    }
                }

                if (!hasParam) {
                    result = supported.getMethod().invoke(controllerInstance);
                }
                
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

                        if (supported.getMethod().isAnnotationPresent(WebAPI.class)) {
                            WebAPI webAPI = supported.getMethod().getAnnotation(WebAPI.class);

                            response.setContentType("application/json");

                            Object output = result;

                            if (webAPI.serialize()) {
                                output = Util.toJson(mapper, result);
                            }

                            response.getWriter().println(output);
                        } else {
                            out.println("Output: " + result);

                            response.setContentType("text/plain");
                            response.getWriter().println(buffer.toString());
                        }

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

            response.setContentType("text/plain");
            response.getWriter().println(buffer.toString());
        }

    }

}
