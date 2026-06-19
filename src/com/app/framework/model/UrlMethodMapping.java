package com.app.framework.model;

public class UrlMethodMapping {
    private String url;
    private String className;
    private String methodName;

    public UrlMethodMapping() {}

    public UrlMethodMapping(String url, String className, String methodName) {
        setUrl(url);
        setClassName(className);
        setMethodName(methodName);
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
