package com.mycompany.stocks4.service;

import com.mycompany.stocks4.model.Articles;
import com.mycompany.stocks4.model.ModeGestionStock;
import com.mycompany.stocks4.util.DBstock;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServisAticles {
    private final boolean strictModeChange;
    private final boolean strictDelete;

    public ServisAticles() {
        this(true, true);
    }

    public ServisAticles(boolean strictModeChange, boolean strictDelete) {
        this.strictModeChange = strictModeChange;
        this.strictDelete = strictDelete;
    }

    public Articles createArticle(String nomArticle, ModeGestionStock modeGestion) throws SQLException {
        validateNomArticle(nomArticle);
        validateModeGestion(modeGestion);

        String nomNormalise = nomArticle.trim();
        try (Connection connection = DBstock.getConnection()) {
            ensureConnection(connection);
            if (existsByNomArticle(connection, nomNormalise)) {
                throw new IllegalArgumentException("Un article avec ce nom existe deja.");
            }

            String sql = "INSERT INTO articles (nom_article, mode_gestion, date_creation) "
                    + "VALUES (?, ?::mode_gestion_stock, CURRENT_TIMESTAMP) RETURNING id_article, date_creation";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, nomNormalise);
                statement.setString(2, modeGestion.name());

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id_article");
                        Timestamp dateCreation = rs.getTimestamp("date_creation");
                        return new Articles(id, nomNormalise, modeGestion, dateCreation);
                    }
                }
            }
        }
        throw new SQLException("Creation de l'article impossible.");
    }

    public Articles updateArticle(int idArticle, String nouveauNom, ModeGestionStock nouveauModeGestion) throws SQLException {
        validateIdArticle(idArticle);
        validateNomArticle(nouveauNom);
        validateModeGestion(nouveauModeGestion);

        String nomNormalise = nouveauNom.trim();
        try (Connection connection = DBstock.getConnection()) {
            ensureConnection(connection);
            Articles articleExistant = getArticleById(connection, idArticle)
                    .orElseThrow(() -> new IllegalArgumentException("Article introuvable pour id=" + idArticle));

            if (!articleExistant.getNomArticle().equalsIgnoreCase(nomNormalise)
                    && existsByNomArticle(connection, nomNormalise)) {
                throw new IllegalArgumentException("Un article avec ce nom existe deja.");
            }

            boolean modeChanged = articleExistant.getModeGestion() != nouveauModeGestion;
            if (strictModeChange && modeChanged && hasMouvements(connection, idArticle)) {
                throw new IllegalStateException(
                        "Mode de gestion non modifiable: des mouvements existent deja pour cet article.");
            }

            String sql = "UPDATE articles SET nom_article = ?, mode_gestion = ?::mode_gestion_stock WHERE id_article = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, nomNormalise);
                statement.setString(2, nouveauModeGestion.name());
                statement.setInt(3, idArticle);
                int updated = statement.executeUpdate();
                if (updated == 0) {
                    throw new SQLException("Aucune ligne mise a jour.");
                }
            }

            return getArticleById(connection, idArticle)
                    .orElseThrow(() -> new SQLException("Article mis a jour mais introuvable."));
        }
    }

    public void deleteArticle(int idArticle) throws SQLException {
        validateIdArticle(idArticle);
        try (Connection connection = DBstock.getConnection()) {
            ensureConnection(connection);
            if (strictDelete && hasMouvements(connection, idArticle)) {
                throw new IllegalStateException(
                        "Suppression interdite: des mouvements existent deja pour cet article.");
            }

            String sql = "DELETE FROM articles WHERE id_article = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, idArticle);
                int deleted = statement.executeUpdate();
                if (deleted == 0) {
                    throw new IllegalArgumentException("Article introuvable pour id=" + idArticle);
                }
            }
        }
    }

    public Optional<Articles> getArticleById(int idArticle) throws SQLException {
        validateIdArticle(idArticle);
        try (Connection connection = DBstock.getConnection()) {
            ensureConnection(connection);
            return getArticleById(connection, idArticle);
        }
    }

    public List<Articles> getAllArticles() throws SQLException {
        String sql = "SELECT id_article, nom_article, mode_gestion, date_creation FROM articles ORDER BY id_article";
        List<Articles> articles = new ArrayList<>();
        try (Connection connection = DBstock.getConnection()) {
            ensureConnection(connection);
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)) {
                while (rs.next()) {
                    articles.add(mapArticle(rs));
                }
            }
        }
        return articles;
    }

    private Optional<Articles> getArticleById(Connection connection, int idArticle) throws SQLException {
        String sql = "SELECT id_article, nom_article, mode_gestion, date_creation FROM articles WHERE id_article = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idArticle);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapArticle(rs));
                }
            }
        }
        return Optional.empty();
    }

    private boolean existsByNomArticle(Connection connection, String nomArticle) throws SQLException {
        String sql = "SELECT 1 FROM articles WHERE LOWER(nom_article) = LOWER(?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nomArticle);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean hasMouvements(Connection connection, int idArticle) throws SQLException {
        String sql = "SELECT 1 FROM mouvements_stock WHERE id_article = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idArticle);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Articles mapArticle(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_article");
        String nomArticle = rs.getString("nom_article");
        ModeGestionStock modeGestion = ModeGestionStock.valueOf(rs.getString("mode_gestion"));
        Timestamp dateCreation = rs.getTimestamp("date_creation");
        return new Articles(id, nomArticle, modeGestion, dateCreation);
    }

    private void validateNomArticle(String nomArticle) {
        if (nomArticle == null || nomArticle.trim().isEmpty()) {
            throw new IllegalArgumentException("nom_article est obligatoire.");
        }
    }

    private void validateModeGestion(ModeGestionStock modeGestion) {
        if (modeGestion == null) {
            throw new IllegalArgumentException("mode_gestion est obligatoire.");
        }
    }

    private void validateIdArticle(int idArticle) {
        if (idArticle <= 0) {
            throw new IllegalArgumentException("id_article doit etre > 0.");
        }
    }

    private void ensureConnection(Connection connection) {
        if (connection == null) {
            throw new IllegalStateException("Connexion a la base indisponible.");
        }
    }

}
