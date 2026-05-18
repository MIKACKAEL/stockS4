# GESTION DE STOCK

## Conception

### 3 manières de gestion de stock

| Source | Date  | Quantité | PU      | Valeur   | Stock | CUMP    | Valeur Stock |
|:------:|:-----:|---------:|--------:|---------:|------:|--------:|------------:|
| 1      | 1 Mai | 10       | 100     | 1000     | 10    | 100     | 1000        |
| 2      | 2 Mai | 5        | 120     | 600      | 15    | 106,66  | 1500        |
| 3      | 3 Mai | 3        | 150     | 450      | 18    | 113,88  | 2050        |
| 4      | 4 Mai | -13      | 113,88  | 1480,44  | 5     | ....    | 568,45      |
| 5      | 5 Mai | 10       | 110     | 1100     | 15    | 111,23  |             |

---

## Règles de sortie (Sortie de stock)

- **Lorsqu'on fait une sortie**, la **PU utilisée** devient la **dernière CUMP**.
  - Exemple : au **3 Mai**, **CUMP = 113,88**  
    → lors de la **sortie du 4 Mai**, la **PU = 113,88**.

- **Les règles ci-dessous s’appliquent uniquement à la sortie :**
  - **Méthode LIFO** (*Last In, First Out*) : la sortie se fait sur le stock entré **le plus récemment**.
  - **Méthode FIFO** (*First In, First Out*) : la sortie se fait sur le stock entré **le plus anciennement**.

---

## CUMP (Coût Unitaire Moyen Pondéré)

- Le **CUMP** est la **moyenne pondérée** du prix de l’article.
- Il **change uniquement lorsqu’il y a une entrée de stock**.
- Le calcul du **CUMP se fait par article**.

### Formule (principe)

\[
\text{CUMP} = \frac{\sum (\text{Quantité entrée} \times \text{PU})}{\text{Quantité totale en stock après entrée}}
\]

### Exemples

- **En 1 Mai :**
  - CUMP = 1000 / 10 = **100**

- **En 2 Mai :**
  - CUMP = \[\(10 × 100\) + \(5 × 120\)\] / 15  
  - CUMP = \[1000 + 600\] / 15 = **106,66**

- **En 3 Mai :**
  - CUMP = \[\(10 × 100\) + \(5 × 120\) + \(3 × 150\)\] / 18  
  - CUMP = \[1000 + 600 + 450\] / 18 = **113,88**

---

## Valeur du stock

- **Formule :**  
  \[
  \text{Valeur du Stock} = \text{Quantité en stock} \times \text{CUMP}
  \]

### Exemples

- **Valeur du stock actuel (en 3 Mai) :**
  - Valeur = 18 × 113,88 = **2050** *(selon le tableau)*

- **Valeur du stock (en 4 Mai) après sortie :**
  - Stock = 5  
  - Valeur = 5 × 113,88 = **569,45** *(valeur arrondie)*

---

## Entrée du 5 Mai

- En **5 Mai**, on a fait une **entrée** de **10 biscuits** à **110 Ar**.
- Le **CUMP est recalculé** car il y a une **entrée**.

**CUMP (au 5 Mai) :** *(à compléter selon votre méthode exacte d’arrondi et les valeurs précédentes)*



LIFO (Last In, First Out)
Méthode de sortie de stock où les derniers articles entrés sont les premiers à sortir.
Exemple : tu achètes 10 unités puis plus tard 5 unités → si tu sors 3 unités, on considère que ça vient d’abord du lot des 5 unités (le plus récent).

FIFO (First In, First Out)
Méthode de sortie de stock où les premiers articles entrés sont les premiers à sortir.
Exemple : tu achètes 10 unités puis plus tard 5 unités → si tu sors 3 unités, on considère que ça vient d’abord du lot des 10 unités (le plus ancien).

PU (Prix Unitaire)
C’est le prix d’une seule unité de l’article.
Exemple : si 10 biscuits coûtent 1 100 Ar, alors PU = 110 Ar par biscuit.

CUMP (Coût Unitaire Moyen Pondéré)
C’est le coût moyen d’une unité en stock, calculé en tenant compte des quantités achetées à différents prix (moyenne “pondérée”).

Il est recalculé à chaque entrée (achat/approvisionnement).
Formule (après une nouvelle entrée) :
CUMP = (Valeur du stock actuel + Valeur de la nouvelle entrée) / (Quantité stock actuelle + Quantité entrée)


### lancement 


-- mvn clean package
-- mvn dependency:copy-dependencies
-- mvn compile -q 2>&1
-- java -cp "target/classes;target/dependency/*" com.mycompany.stocks4.StockS4
-- mvn -q exec:java

<!-- Règles de sortie aplique cette exemple 
- **Lorsqu'on fait une sortie**, la **PU utilisée** devient la **dernière CUMP**.
  - Exemple : au **3 Mai**, **CUMP = 113,88**  
    → lors de la **sortie du 4 Mai**, la **PU = 113,88**.
    corrige le cump pour une resulta correcte et aplique ces Règles de sortie pour chaque gestion de stock  -->