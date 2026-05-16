package com.mycompany.stocks4.model;

import java.sql.Timestamp;

public class Articles {
    private int idArticle;
    private String nomArticle;
    private ModeGestionStock modeGestion;
    private Timestamp dateCreation;

    // Constructors
    public Articles() {
    }

    public Articles(int idArticle, String nomArticle, ModeGestionStock modeGestion, Timestamp dateCreation) {
        this.idArticle = idArticle;
        this.nomArticle = nomArticle;
        this.modeGestion = modeGestion;
        this.dateCreation = dateCreation;
    }

    // Getters and Setters
    public int getIdArticle() {
        return idArticle;
    }

    public void setIdArticle(int idArticle) {
        this.idArticle = idArticle;
    }

    public String getNomArticle() {
        return nomArticle;
    }

    public void setNomArticle(String nomArticle) {
        this.nomArticle = nomArticle;
    }

    public ModeGestionStock getModeGestion() {
        return modeGestion;
    }

    public void setModeGestion(ModeGestionStock modeGestion) {
        this.modeGestion = modeGestion;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Articles orElseThrow(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'orElseThrow'");
    }
}
