package com.app.framework.model;

import java.util.Map;
import java.util.HashMap;

public class SSession {
    private Map<String, Object> attributes;

    public SSession() {
        this.attributes = new HashMap<>();
    }

    public SSession(Map<String, Object> attributes) {
        setAttributes(attributes);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void invalidate() {
        attributes.clear();
    }
}