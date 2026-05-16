CREATE DATABASE db_stock;
\c db_stock;

-- 1) Types ENUM
CREATE TYPE mode_gestion_stock AS ENUM ('FIFO', 'LIFO', 'CUMP');
CREATE TYPE type_mouvement_stock AS ENUM ('ENTREE', 'SORTIE');

-- 2) Table articles
CREATE TABLE articles (
    id_article      SERIAL PRIMARY KEY,
    nom_article     VARCHAR(150) NOT NULL,
    mode_gestion    mode_gestion_stock NOT NULL,
    date_creation   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3) Table mouvements_stock
CREATE TABLE mouvements_stock (
    id_mouvement     SERIAL PRIMARY KEY,
    id_article       INTEGER NOT NULL REFERENCES articles(id_article) ON DELETE CASCADE,
    date_mouvement   DATE NOT NULL,
    type_mouvement   type_mouvement_stock NOT NULL,
    quantite         NUMERIC(12,2) NOT NULL CHECK (quantite > 0),
    prix_unitaire    NUMERIC(14,2) NOT NULL CHECK (prix_unitaire >= 0),
    valeur_total          NUMERIC(14,2) NOT NULL CHECK (valeur_total >= 0),
    stock_apres      NUMERIC(12,2) NOT NULL CHECK (stock_apres >= 0),
    cump_apres       NUMERIC(14,4) NOT NULL CHECK (cump_apres >= 0),
    valeur_stock DECIMAL(15,2),
    source INTEGER,
    CONSTRAINT fk_mouvement_source FOREIGN KEY (source) REFERENCES mouvements_stock(id_mouvement) ON DELETE SET NULL,
    date_creation    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_mvt_article_date ON mouvements_stock(id_article, date_mouvement, id_mouvement);
CREATE INDEX idx_mvt_articlesource ON mouvements_stock(source);

-- java -cp "target/classes;target/dependency/*" com.mycompany.stocks4.StockS4
-- mvn -q exec:java
-- mvn clean package
-- mvn dependency:copy-dependencies
-- mvn compile -q 2>&1



