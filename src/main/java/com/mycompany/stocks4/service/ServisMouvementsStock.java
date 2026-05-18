package com.mycompany.stocks4.service;

import com.mycompany.stocks4.model.ModeGestionStock;
import com.mycompany.stocks4.model.MouvementsStock;
import com.mycompany.stocks4.model.TypeMouvementStock;
import com.mycompany.stocks4.util.DBstock;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ServisMouvementsStock {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE_QTE = 2;
    private static final int SCALE_MONEY = 2;
    private static final int SCALE_CUMP = 4;

    public MouvementsStock createMouvement(int idArticle,
                                           Date dateMouvement,
                                           TypeMouvementStock typeMouvement,
                                           BigDecimal quantite,
                                           BigDecimal prixUnitaireEntree,
                                           Integer source) throws SQLException {
        validateCommonInputs(idArticle, dateMouvement, typeMouvement, quantite);

        try (Connection connection = DBstock.getConnection()) {
            ensureConnection(connection);
            connection.setAutoCommit(false);
            try {
                ModeGestionStock modeGestion = getModeGestionArticle(connection, idArticle)
                        .orElseThrow(() -> new IllegalArgumentException("Article introuvable pour id=" + idArticle));

                if (source != null) {
                    validateSource(connection, source);
                }

                EtatStock precedent = getEtatPrecedent(connection, idArticle);
                CalculMouvement calc;
                if (typeMouvement == TypeMouvementStock.ENTREE) {
                    calc = calculerEntree(precedent, quantite, prixUnitaireEntree);
                } else {
                    calc = calculerSortie(connection, idArticle, modeGestion, precedent, quantite);
                }

                BigDecimal quantiteNormalisee = scaleQte(quantite);
                MouvementsStock mouvement = insertMouvement(connection, idArticle, dateMouvement, typeMouvement, quantiteNormalisee, calc, source);
                connection.commit();
                return mouvement;
            } catch (Exception ex) {
                connection.rollback();
                if (ex instanceof SQLException sqlEx) {
                    throw sqlEx;
                }
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<MouvementsStock> getMouvementsByArticle(int idArticle) throws SQLException {
        if (idArticle <= 0) {
            throw new IllegalArgumentException("id_article doit etre > 0.");
        }
        String sql = "SELECT id_mouvement, id_article, date_mouvement, type_mouvement, quantite, prix_unitaire, "
                + "valeur_total, stock_apres, cump_apres, valeur_stock, source, date_creation "
                + "FROM mouvements_stock WHERE id_article = ? ORDER BY date_mouvement, id_mouvement";

        try (Connection connection = DBstock.getConnection()) {
            ensureConnection(connection);
            List<MouvementsStock> mouvements = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, idArticle);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        mouvements.add(mapMouvement(rs));
                    }
                }
            }
            return mouvements;
        }
    }

    public List<GlobalStockRow> getEtatGlobalStock() throws SQLException {
        String sql = "SELECT a.id_article, a.nom_article, a.mode_gestion, "
                + "COALESCE(m.stock_apres, 0) AS stock_apres, "
                + "COALESCE(m.cump_apres, 0) AS cump_apres "
                + "FROM articles a "
                + "LEFT JOIN LATERAL ( "
                + "    SELECT stock_apres, cump_apres "
                + "    FROM mouvements_stock ms "
                + "    WHERE ms.id_article = a.id_article "
                + "    ORDER BY ms.date_mouvement DESC, ms.id_mouvement DESC "
                + "    LIMIT 1 "
                + ") m ON true "
                + "ORDER BY a.nom_article";

        try (Connection connection = DBstock.getConnection()) {
            ensureConnection(connection);
            List<GlobalStockRow> rows = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    BigDecimal stockApres = scaleQte(rs.getBigDecimal("stock_apres"));
                    if (stockApres.compareTo(ZERO) > 0) {
                        rows.add(new GlobalStockRow(
                                rs.getInt("id_article"),
                                rs.getString("nom_article"),
                                ModeGestionStock.valueOf(rs.getString("mode_gestion")),
                                stockApres,
                                scaleCump(rs.getBigDecimal("cump_apres"))
                        ));
                    }
                }
            }
            return rows;
        }
    }

    public List<DetailStockRow> getDetailStockArticle(int idArticle) throws SQLException {
        if (idArticle <= 0) {
            throw new IllegalArgumentException("id_article doit etre > 0.");
        }
        try (Connection connection = DBstock.getConnection()) {
            ensureConnection(connection);
            ModeGestionStock modeGestion = getModeGestionArticle(connection, idArticle)
                    .orElseThrow(() -> new IllegalArgumentException("Article introuvable pour id=" + idArticle));
            EtatStock etat = getEtatPrecedent(connection, idArticle);
            if (etat.stockApres().compareTo(ZERO) <= 0) {
                return new ArrayList<>();
            }

            if (modeGestion == ModeGestionStock.CUMP) {
                List<DetailStockRow> rows = new ArrayList<>();
                rows.add(new DetailStockRow("MOYENNE CUMP", etat.stockApres(), etat.cumpApres(),
                        scaleMoney(etat.stockApres().multiply(etat.cumpApres()))));
                return rows;
            }

            List<LotVirtuel> lots = buildLotsRestants(connection, idArticle, modeGestion);
            List<DetailStockRow> rows = new ArrayList<>();
            int compteur = 1;
            for (LotVirtuel lot : lots) {
                if (lot.quantiteRestante.compareTo(ZERO) > 0) {
                    rows.add(new DetailStockRow(
                            "Lot " + compteur,
                            scaleQte(lot.quantiteRestante),
                            scaleMoney(lot.prixUnitaire),
                            scaleMoney(lot.quantiteRestante.multiply(lot.prixUnitaire))
                    ));
                    compteur++;
                }
            }
            return rows;
        }
    }

    private CalculMouvement calculerEntree(EtatStock precedent, BigDecimal quantite, BigDecimal prixUnitaireEntree) {
        if (prixUnitaireEntree == null || prixUnitaireEntree.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("prix_unitaire est obligatoire et > 0 pour une ENTREE.");
        }

        BigDecimal qte = scaleQte(quantite);
        BigDecimal pu = scaleMoney(prixUnitaireEntree);
        BigDecimal stockApres = scaleQte(precedent.stockApres().add(qte));
        BigDecimal valeurTotal = scaleMoney(qte.multiply(pu));

        BigDecimal valeurStockAvant = precedent.stockApres().multiply(precedent.cumpApres());
        BigDecimal cumpApres = valeurStockAvant.add(valeurTotal)
                .divide(stockApres, SCALE_CUMP, RoundingMode.HALF_UP);

        BigDecimal valeurStock = scaleMoney(stockApres.multiply(cumpApres));
        return new CalculMouvement(pu, valeurTotal, stockApres, scaleCump(cumpApres), valeurStock);
    }

    private CalculMouvement calculerSortie(Connection connection,
                                           int idArticle,
                                           ModeGestionStock modeGestion,
                                           EtatStock precedent,
                                           BigDecimal quantite) throws SQLException {
        BigDecimal qte = scaleQte(quantite);
        if (precedent.stockApres().compareTo(qte) < 0) {
            throw new IllegalStateException("Stock insuffisant. Stock actuel=" + precedent.stockApres() + ", demande=" + qte);
        }

        BigDecimal stockApres = scaleQte(precedent.stockApres().subtract(qte));
        BigDecimal prixUnitaire;
        BigDecimal valeurTotal;
        BigDecimal valeurStock;
        BigDecimal cumpApres;

        if (modeGestion == ModeGestionStock.CUMP) {
            prixUnitaire = scaleMoney(precedent.cumpApres());
            valeurTotal = scaleMoney(qte.multiply(prixUnitaire));
            cumpApres = precedent.cumpApres();
            valeurStock = scaleMoney(stockApres.multiply(cumpApres));
        } else {
            ValorisationSortie valorisation = valoriserSortieParLots(connection, idArticle, modeGestion, qte);
            valeurTotal = valorisation.valeurTotaleSortie();
            prixUnitaire = scaleMoney(valeurTotal.divide(qte, SCALE_MONEY, RoundingMode.HALF_UP));
            valeurStock = valorisation.valeurStockRestant();
            if (stockApres.compareTo(ZERO) > 0) {
                cumpApres = valeurStock.divide(stockApres, SCALE_CUMP, RoundingMode.HALF_UP);
            } else {
                cumpApres = ZERO;
            }
        }

        return new CalculMouvement(prixUnitaire, valeurTotal, stockApres, scaleCump(cumpApres), valeurStock);
    }

    private ValorisationSortie valoriserSortieParLots(Connection connection,
                                                      int idArticle,
                                                      ModeGestionStock modeGestion,
                                                      BigDecimal quantiteSortie) throws SQLException {
        List<LotVirtuel> lots = buildLotsRestants(connection, idArticle, modeGestion);
        BigDecimal restant = quantiteSortie;
        BigDecimal total = ZERO;

        int index = modeGestion == ModeGestionStock.FIFO ? 0 : lots.size() - 1;
        int step = modeGestion == ModeGestionStock.FIFO ? 1 : -1;

        while (index >= 0 && index < lots.size() && restant.compareTo(ZERO) > 0) {
            LotVirtuel lot = lots.get(index);
            if (lot.quantiteRestante.compareTo(ZERO) > 0) {
                BigDecimal consommee = lot.quantiteRestante.min(restant);
                total = total.add(consommee.multiply(lot.prixUnitaire));
                lot.quantiteRestante = lot.quantiteRestante.subtract(consommee);
                restant = restant.subtract(consommee);
            }
            index += step;
        }

        if (restant.compareTo(ZERO) > 0) {
            throw new IllegalStateException("Impossible de valoriser la sortie (lots insuffisants).");
        }

        BigDecimal valeurStockRestant = ZERO;
        for (LotVirtuel lot : lots) {
            if (lot.quantiteRestante.compareTo(ZERO) > 0) {
                valeurStockRestant = valeurStockRestant.add(lot.quantiteRestante.multiply(lot.prixUnitaire));
            }
        }

        return new ValorisationSortie(scaleMoney(total), scaleMoney(valeurStockRestant));
    }

    private List<LotVirtuel> buildLotsRestants(Connection connection, int idArticle, ModeGestionStock modeGestion) throws SQLException {
        String sql = "SELECT type_mouvement, quantite, prix_unitaire FROM mouvements_stock "
                + "WHERE id_article = ? ORDER BY date_mouvement, id_mouvement";

        List<LotVirtuel> lots = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idArticle);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    TypeMouvementStock type = TypeMouvementStock.valueOf(rs.getString("type_mouvement"));
                    BigDecimal quantite = scaleQte(rs.getBigDecimal("quantite"));
                    BigDecimal prixUnitaire = scaleMoney(rs.getBigDecimal("prix_unitaire"));

                    if (type == TypeMouvementStock.ENTREE) {
                        lots.add(new LotVirtuel(quantite, prixUnitaire));
                    } else {
                        consommerLots(lots, quantite, modeGestion);
                    }
                }
            }
        }
        if (modeGestion == ModeGestionStock.LIFO) {
            Collections.reverse(lots);
        }
        return lots;
    }

    private void consommerLots(List<LotVirtuel> lots, BigDecimal quantiteASortir, ModeGestionStock modeGestion) {
        BigDecimal restant = quantiteASortir;
        if (modeGestion == ModeGestionStock.LIFO) {
            for (int i = lots.size() - 1; i >= 0 && restant.compareTo(ZERO) > 0; i--) {
                LotVirtuel lot = lots.get(i);
                if (lot.quantiteRestante.compareTo(ZERO) <= 0) {
                    continue;
                }
                BigDecimal consommee = lot.quantiteRestante.min(restant);
                lot.quantiteRestante = lot.quantiteRestante.subtract(consommee);
                restant = restant.subtract(consommee);
            }
        } else {
            for (LotVirtuel lot : lots) {
                if (restant.compareTo(ZERO) <= 0) {
                    break;
                }
                if (lot.quantiteRestante.compareTo(ZERO) <= 0) {
                    continue;
                }
                BigDecimal consommee = lot.quantiteRestante.min(restant);
                lot.quantiteRestante = lot.quantiteRestante.subtract(consommee);
                restant = restant.subtract(consommee);
            }
        }
        if (restant.compareTo(ZERO) > 0) {
            throw new IllegalStateException("Historique incoherent: sorties > entrees.");
        }
    }

    private MouvementsStock insertMouvement(Connection connection,
                                            int idArticle,
                                            Date dateMouvement,
                                            TypeMouvementStock typeMouvement,
                                            BigDecimal quantite,
                                            CalculMouvement calcul,
                                            Integer source) throws SQLException {
        String sql = "INSERT INTO mouvements_stock (id_article, date_mouvement, type_mouvement, quantite, prix_unitaire, "
                + "valeur_total, stock_apres, cump_apres, valeur_stock, source, date_creation) "
                + "VALUES (?, ?, ?::type_mouvement_stock, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) "
                + "RETURNING id_mouvement, date_creation";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idArticle);
            statement.setDate(2, dateMouvement);
            statement.setString(3, typeMouvement.name());
            statement.setBigDecimal(4, quantite);
            statement.setBigDecimal(5, calcul.prixUnitaire());
            statement.setBigDecimal(6, calcul.valeurTotal());
            statement.setBigDecimal(7, calcul.stockApres());
            statement.setBigDecimal(8, calcul.cumpApres());
            statement.setBigDecimal(9, calcul.valeurStock());
            if (source == null) {
                statement.setNull(10, java.sql.Types.INTEGER);
            } else {
                statement.setInt(10, source);
            }

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Insertion mouvement impossible.");
                }
                MouvementsStock mouvement = new MouvementsStock();
                mouvement.setIdMouvement(rs.getInt("id_mouvement"));
                mouvement.setIdArticle(idArticle);
                mouvement.setDateMouvement(dateMouvement);
                mouvement.setTypeMouvement(typeMouvement);
                mouvement.setQuantite(quantite);
                mouvement.setPrixUnitaire(calcul.prixUnitaire());
                mouvement.setValeurTotal(calcul.valeurTotal());
                mouvement.setStockApres(calcul.stockApres());
                mouvement.setCumpApres(calcul.cumpApres());
                mouvement.setValeurStock(calcul.valeurStock());
                mouvement.setSource(source);
                mouvement.setDateCreation(rs.getTimestamp("date_creation"));
                return mouvement;
            }
        }
    }

    private EtatStock getEtatPrecedent(Connection connection, int idArticle) throws SQLException {
        String sql = "SELECT stock_apres, cump_apres FROM mouvements_stock "
                + "WHERE id_article = ? ORDER BY date_mouvement DESC, id_mouvement DESC LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idArticle);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new EtatStock(scaleQte(rs.getBigDecimal("stock_apres")), scaleCump(rs.getBigDecimal("cump_apres")));
                }
            }
        }
        return new EtatStock(scaleQte(ZERO), scaleCump(ZERO));
    }

    private Optional<ModeGestionStock> getModeGestionArticle(Connection connection, int idArticle) throws SQLException {
        String sql = "SELECT mode_gestion FROM articles WHERE id_article = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idArticle);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(ModeGestionStock.valueOf(rs.getString("mode_gestion")));
                }
            }
        }
        return Optional.empty();
    }

    private void validateSource(Connection connection, int source) throws SQLException {
        if (source <= 0) {
            throw new IllegalArgumentException("source doit etre > 0.");
        }
        String sql = "SELECT 1 FROM mouvements_stock WHERE id_mouvement = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, source);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("source introuvable: id_mouvement=" + source);
                }
            }
        }
    }

    private MouvementsStock mapMouvement(ResultSet rs) throws SQLException {
        return new MouvementsStock(
                rs.getInt("id_mouvement"),
                rs.getInt("id_article"),
                rs.getDate("date_mouvement"),
                TypeMouvementStock.valueOf(rs.getString("type_mouvement")),
                scaleQte(rs.getBigDecimal("quantite")),
                scaleMoney(rs.getBigDecimal("prix_unitaire")),
                scaleMoney(rs.getBigDecimal("valeur_total")),
                scaleQte(rs.getBigDecimal("stock_apres")),
                scaleCump(rs.getBigDecimal("cump_apres")),
                scaleMoney(rs.getBigDecimal("valeur_stock")),
                (Integer) rs.getObject("source"),
                rs.getTimestamp("date_creation")
        );
    }

    private void validateCommonInputs(int idArticle, Date dateMouvement, TypeMouvementStock typeMouvement, BigDecimal quantite) {
        if (idArticle <= 0) {
            throw new IllegalArgumentException("id_article doit etre > 0.");
        }
        if (dateMouvement == null) {
            throw new IllegalArgumentException("date_mouvement est obligatoire.");
        }
        if (typeMouvement == null) {
            throw new IllegalArgumentException("type_mouvement est obligatoire.");
        }
        if (quantite == null || quantite.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("quantite doit etre > 0.");
        }
    }

    private void ensureConnection(Connection connection) {
        if (connection == null) {
            throw new IllegalStateException("Connexion a la base indisponible.");
        }
    }

    private BigDecimal scaleQte(BigDecimal value) {
        return (value == null ? ZERO : value).setScale(SCALE_QTE, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        return (value == null ? ZERO : value).setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleCump(BigDecimal value) {
        return (value == null ? ZERO : value).setScale(SCALE_CUMP, RoundingMode.HALF_UP);
    }

    private record EtatStock(BigDecimal stockApres, BigDecimal cumpApres) {
    }

    private record CalculMouvement(BigDecimal prixUnitaire,
                                   BigDecimal valeurTotal,
                                   BigDecimal stockApres,
                                   BigDecimal cumpApres,
                                   BigDecimal valeurStock) {
    }

    private record ValorisationSortie(BigDecimal valeurTotaleSortie,
                                      BigDecimal valeurStockRestant) {
    }

    private static final class LotVirtuel {
        private BigDecimal quantiteRestante;
        private final BigDecimal prixUnitaire;

        private LotVirtuel(BigDecimal quantiteRestante, BigDecimal prixUnitaire) {
            this.quantiteRestante = quantiteRestante;
            this.prixUnitaire = prixUnitaire;
        }
    }

    public record GlobalStockRow(int idArticle,
                                 String nomArticle,
                                 ModeGestionStock modeGestion,
                                 BigDecimal quantiteDisponible,
                                 BigDecimal cumpActuel) {
    }

    public record DetailStockRow(String lotLabel,
                                 BigDecimal quantite,
                                 BigDecimal prixUnitaire,
                                 BigDecimal valeurLigne) {
    }
}
