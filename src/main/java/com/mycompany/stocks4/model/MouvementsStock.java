package com.mycompany.stocks4.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.math.BigDecimal;

public class MouvementsStock {
    private int idMouvement;
    private int idArticle;
    private Date dateMouvement;
    private TypeMouvementStock typeMouvement;
    private BigDecimal quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal valeurTotal;
    private BigDecimal stockApres;
    private BigDecimal cumpApres;
    private BigDecimal valeurStock;
    private Integer source;
    private Timestamp dateCreation;

    public MouvementsStock() {
    }

    public MouvementsStock(int idMouvement, int idArticle, Date dateMouvement, TypeMouvementStock typeMouvement, BigDecimal quantite, BigDecimal prixUnitaire, BigDecimal valeurTotal, BigDecimal stockApres, BigDecimal cumpApres, BigDecimal valeurStock, Integer source, Timestamp dateCreation) {
        this.idMouvement = idMouvement;
        this.idArticle = idArticle;
        this.dateMouvement = dateMouvement;
        this.typeMouvement = typeMouvement;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.valeurTotal = valeurTotal;
        this.stockApres = stockApres;
        this.cumpApres = cumpApres;
        this.valeurStock = valeurStock;
        this.source = source;
        this.dateCreation = dateCreation;
    }

    // Getters and Setters
    public int getIdMouvement() {
        return idMouvement;
    }

    public void setIdMouvement(int idMouvement) {
        this.idMouvement = idMouvement;
    }

    public int getIdArticle() {
        return idArticle;
    }

    public void setIdArticle(int idArticle) {
        this.idArticle = idArticle;
    }

    public Date getDateMouvement() {
        return dateMouvement;
    }

    public void setDateMouvement(Date dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public TypeMouvementStock getTypeMouvement() {
        return typeMouvement;
    }

    public void setTypeMouvement(TypeMouvementStock typeMouvement) {
        this.typeMouvement = typeMouvement;
    }

    public BigDecimal getQuantite() {
        return quantite;
    }

    public void setQuantite(BigDecimal quantite) {
        this.quantite = quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public BigDecimal getValeurTotal() {
        return valeurTotal;
    }

    public void setValeurTotal(BigDecimal valeurTotal) {
        this.valeurTotal = valeurTotal;
    }

    public BigDecimal getStockApres() {
        return stockApres;
    }

    public void setStockApres(BigDecimal stockApres) {
        this.stockApres = stockApres;
    }

    public BigDecimal getCumpApres() {
        return cumpApres;
    }

    public void setCumpApres(BigDecimal cumpApres) {
        this.cumpApres = cumpApres;
    }

    public BigDecimal getValeurStock() {
        return valeurStock;
    }

    public void setValeurStock(BigDecimal valeurStock) {
        this.valeurStock = valeurStock;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }
}