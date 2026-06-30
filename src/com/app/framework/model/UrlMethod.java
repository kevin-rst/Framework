package com.app.framework.model;

import java.util.Objects;

public class UrlMethod {
    private String url;
    private String httpMethod;

    public UrlMethod() {}

    public UrlMethod(String url, String httpMethod) {
        setUrl(url);
        setHttpMethod(httpMethod);
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, httpMethod);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UrlMethod)) return false;
        
        UrlMethod other = (UrlMethod) obj;
        return Objects.equals(url, other.url) && Objects.equals(httpMethod, other.httpMethod);
    }
}