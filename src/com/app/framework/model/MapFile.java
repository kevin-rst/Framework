package com.app.framework.model;

public class MapFile {
    private String nom;
    private String extension;
    private byte[] content;

    public MapFile() {}

    public MapFile(String nom, String extension, byte[] content) {
        setNom(nom);
        setExtension(extension);
        setContent(content);
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return this.nom;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return this.extension;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public byte[] getContent() {
        return this.content;
    }
}   