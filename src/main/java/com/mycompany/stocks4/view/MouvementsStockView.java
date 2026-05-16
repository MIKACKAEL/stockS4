package com.mycompany.stocks4.view;

import com.mycompany.stocks4.model.Articles;
import com.mycompany.stocks4.model.MouvementsStock;
import com.mycompany.stocks4.model.TypeMouvementStock;
import com.mycompany.stocks4.service.ServisAticles;
import com.mycompany.stocks4.service.ServisMouvementsStock;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class MouvementsStockView extends JFrame {
    private final ServisAticles servisAticles;
    private final ServisMouvementsStock servisMouvementsStock;

    private final JComboBox<ArticleItem> cmbArticle;
    private final JComboBox<TypeMouvementStock> cmbType;
    private final JTextField txtQuantite;
    private final JTextField txtDate;
    private final JTextField txtPrixUnitaire;
    private final JTextField txtSource;

    private final DefaultTableModel tableModel;
    private final JTable tableMouvements;

    public MouvementsStockView() {
        this.servisAticles = new ServisAticles();
        this.servisMouvementsStock = new ServisMouvementsStock();
        this.cmbArticle = new JComboBox<>();
        this.cmbType = new JComboBox<>(TypeMouvementStock.values());
        this.txtQuantite = new JTextField(10);
        this.txtDate = new JTextField(LocalDate.now().toString(), 10);
        this.txtPrixUnitaire = new JTextField(10);
        this.txtSource = new JTextField(8);
        this.tableModel = new DefaultTableModel(
                new Object[]{"ID", "Article", "Date", "Type", "Quantite", "PU", "Valeur", "Stock Apres", "CUMP", "Source"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.tableMouvements = new JTable(tableModel);
        initUI();
        loadArticles();
    }

    private void initUI() {
        setTitle("Saisie des mouvements de stock");
        setSize(1000, 550);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(ModernTheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        // ── Form Panel ──────────────────────────────────────────────────
        JPanel panelForm = ModernTheme.formPanel("Nouveau mouvement");
        panelForm.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Style inputs
        ModernTheme.styleTextField(txtQuantite);
        ModernTheme.styleTextField(txtDate);
        ModernTheme.styleTextField(txtPrixUnitaire);
        ModernTheme.styleTextField(txtSource);
        ModernTheme.styleComboBox(cmbArticle);
        ModernTheme.styleComboBox(cmbType);

        // Row 0
        gbc.gridy = 0;
        gbc.gridx = 0; panelForm.add(ModernTheme.styledLabel("Article :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; panelForm.add(cmbArticle, gbc); gbc.weightx = 0;
        gbc.gridx = 2; panelForm.add(ModernTheme.styledLabel("Type :"), gbc);
        gbc.gridx = 3; panelForm.add(cmbType, gbc);
        gbc.gridx = 4; panelForm.add(ModernTheme.styledLabel("Quantite :"), gbc);
        gbc.gridx = 5; panelForm.add(txtQuantite, gbc);

        // Row 1
        gbc.gridy = 1;
        gbc.gridx = 0; panelForm.add(ModernTheme.styledLabel("Date (yyyy-mm-dd) :"), gbc);
        gbc.gridx = 1; panelForm.add(txtDate, gbc);
        gbc.gridx = 2; panelForm.add(ModernTheme.styledLabel("Prix unitaire :"), gbc);
        gbc.gridx = 3; panelForm.add(txtPrixUnitaire, gbc);
        gbc.gridx = 4; panelForm.add(ModernTheme.styledLabel("Source (opt.) :"), gbc);
        gbc.gridx = 5; panelForm.add(txtSource, gbc);

        // Row 2 — Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(ModernTheme.BG_PANEL);

        JButton btnEnregistrer = ModernTheme.successButton("\u2713 Enregistrer");
        JButton btnActualiser = ModernTheme.secondaryButton("\u21BB Actualiser");
        JButton btnGlobal = ModernTheme.primaryButton("\uD83D\uDCCA Voir Etat Global");
        btnPanel.add(btnEnregistrer);
        btnPanel.add(btnActualiser);
        btnPanel.add(btnGlobal);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 6;
        gbc.insets = new Insets(12, 8, 6, 8);
        panelForm.add(btnPanel, gbc);

        // ── Content wrapper ─────────────────────────────────────────────
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(ModernTheme.BG_DARK);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        contentPanel.add(ModernTheme.gradientHeader("\uD83D\uDD04  Mouvements de Stock"), BorderLayout.NORTH);

        // Form
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(ModernTheme.BG_DARK);
        formWrapper.setBorder(new EmptyBorder(10, 0, 0, 0));
        formWrapper.add(panelForm, BorderLayout.NORTH);

        // Table
        tableMouvements.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMouvements.setAutoCreateRowSorter(true);
        ModernTheme.styleTable(tableMouvements);
        JScrollPane scrollPane = ModernTheme.styledScrollPane(tableMouvements);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(ModernTheme.BG_DARK);
        centerPanel.add(formWrapper, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // ── Sidebar ─────────────────────────────────────────────────────
        add(NavigationSidebar.create(this, NavigationSidebar.ViewKey.MOUVEMENTS, null), BorderLayout.WEST);

        // ── Events (logic unchanged) ────────────────────────────────────
        cmbType.addActionListener(e -> updatePrixUnitaireVisibility());
        cmbArticle.addActionListener(e -> loadMouvementsForSelectedArticle());
        btnEnregistrer.addActionListener(e -> enregistrerMouvement());
        btnActualiser.addActionListener(e -> {
            loadArticles();
            loadMouvementsForSelectedArticle();
        });
        btnGlobal.addActionListener(e -> new VisualGlobalStockView().setVisible(true));

        updatePrixUnitaireVisibility();
    }

    private void updatePrixUnitaireVisibility() {
        TypeMouvementStock type = (TypeMouvementStock) cmbType.getSelectedItem();
        boolean isEntree = type == TypeMouvementStock.ENTREE;
        txtPrixUnitaire.setEnabled(isEntree);
        if (!isEntree) {
            txtPrixUnitaire.setText("");
        }
    }

    private void loadArticles() {
        try {
            List<Articles> articles = servisAticles.getAllArticles();
            DefaultComboBoxModel<ArticleItem> model = new DefaultComboBoxModel<>();
            for (Articles article : articles) {
                model.addElement(new ArticleItem(article.getIdArticle(), article.getNomArticle()));
            }
            cmbArticle.setModel(model);
            if (model.getSize() > 0) {
                cmbArticle.setSelectedIndex(0);
            }
            loadMouvementsForSelectedArticle();
        } catch (Exception ex) {
            showError("Chargement des articles impossible: " + ex.getMessage());
        }
    }

    private void loadMouvementsForSelectedArticle() {
        tableModel.setRowCount(0);
        ArticleItem selected = (ArticleItem) cmbArticle.getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            List<MouvementsStock> mouvements = servisMouvementsStock.getMouvementsByArticle(selected.id());
            for (MouvementsStock m : mouvements) {
                tableModel.addRow(new Object[]{
                    m.getIdMouvement(),
                    selected.nom(),
                    m.getDateMouvement(),
                    m.getTypeMouvement(),
                    m.getQuantite(),
                    m.getPrixUnitaire(),
                    m.getValeurTotal(),
                    m.getStockApres(),
                    m.getCumpApres(),
                    m.getSource()
                });
            }
        } catch (Exception ex) {
            showError("Chargement des mouvements impossible: " + ex.getMessage());
        }
    }

    private void enregistrerMouvement() {
        try {
            ArticleItem article = (ArticleItem) cmbArticle.getSelectedItem();
            if (article == null) {
                throw new IllegalArgumentException("Selectionnez un article.");
            }

            TypeMouvementStock type = (TypeMouvementStock) cmbType.getSelectedItem();
            BigDecimal quantite = new BigDecimal(txtQuantite.getText().trim());
            Date dateMouvement = Date.valueOf(LocalDate.parse(txtDate.getText().trim()));

            BigDecimal puEntree = null;
            if (type == TypeMouvementStock.ENTREE) {
                puEntree = new BigDecimal(txtPrixUnitaire.getText().trim());
            }

            Integer source = null;
            if (!txtSource.getText().trim().isBlank()) {
                source = Integer.valueOf(txtSource.getText().trim());
            }

            MouvementsStock mouvement = servisMouvementsStock.createMouvement(
                    article.id(), dateMouvement, type, quantite, puEntree, source);

            showInfo("Mouvement enregistre. PU applique: " + mouvement.getPrixUnitaire()
                    + " | Valeur totale: " + mouvement.getValeurTotal());
            txtQuantite.setText("");
            txtSource.setText("");
            if (type == TypeMouvementStock.ENTREE) {
                txtPrixUnitaire.setText("");
            }
            loadMouvementsForSelectedArticle();
        } catch (Exception ex) {
            showError("Enregistrement impossible: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new MouvementsStockView().setVisible(true));
    }

    private record ArticleItem(int id, String nom) {
        @Override
        public String toString() {
            return id + " - " + nom;
        }
    }
}
