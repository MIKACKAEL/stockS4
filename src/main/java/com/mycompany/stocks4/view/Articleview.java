package com.mycompany.stocks4.view;

import com.mycompany.stocks4.model.Articles;
import com.mycompany.stocks4.model.ModeGestionStock;
import com.mycompany.stocks4.service.ServisAticles;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class Articleview extends JFrame {

    private final ServisAticles servisAticles;
    private final JTextField txtNom;
    private final JComboBox<ModeGestionStock> cmbMode;
    private final JTable tableArticles;
    private final DefaultTableModel tableModel;

    public Articleview() {
        this.servisAticles = new ServisAticles();
        this.txtNom = new JTextField(20);
        this.cmbMode = new JComboBox<>(ModeGestionStock.values());
        this.tableModel = new DefaultTableModel(new Object[]{"ID", "Nom Article", "Mode Gestion", "Date Creation"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.tableArticles = new JTable(tableModel);
        initUI();
        loadArticles();
    }

    private void initUI() {
        setTitle("Gestion des Articles");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(ModernTheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        // ── Form Panel ──────────────────────────────────────────────────
        JPanel panelForm = ModernTheme.formPanel("Formulaire Article");
        panelForm.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Style inputs
        ModernTheme.styleTextField(txtNom);
        ModernTheme.styleComboBox(cmbMode);

        // Row 0 — Labels + Inputs
        gbc.gridy = 0;
        gbc.gridx = 0; panelForm.add(ModernTheme.styledLabel("Nom :"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panelForm.add(txtNom, gbc);
        gbc.weightx = 0;
        gbc.gridx = 2; panelForm.add(ModernTheme.styledLabel("Mode :"), gbc);
        gbc.gridx = 3; panelForm.add(cmbMode, gbc);

        // Row 1 — Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(ModernTheme.BG_PANEL);

        JButton btnAjouter = ModernTheme.successButton("+ Ajouter");
        JButton btnModifier = ModernTheme.primaryButton("Modifier");
        JButton btnSupprimer = ModernTheme.dangerButton("Supprimer");
        JButton btnActualiser = ModernTheme.secondaryButton("Actualiser");
        JButton btnVider = ModernTheme.secondaryButton("Vider");

        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);
        btnPanel.add(btnActualiser);
        btnPanel.add(btnVider);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(12, 8, 6, 8);
        panelForm.add(btnPanel, gbc);

        // ── Content wrapper ─────────────────────────────────────────────
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(ModernTheme.BG_DARK);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        contentPanel.add(ModernTheme.gradientHeader("Gestion des Articles"), BorderLayout.NORTH);

        // Form
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(ModernTheme.BG_DARK);
        formWrapper.setBorder(new EmptyBorder(10, 0, 0, 0));
        formWrapper.add(panelForm, BorderLayout.NORTH);

        // Table
        tableArticles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ModernTheme.styleTable(tableArticles);
        JScrollPane scrollPane = ModernTheme.styledScrollPane(tableArticles);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(ModernTheme.BG_DARK);
        centerPanel.add(formWrapper, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // ── Sidebar ─────────────────────────────────────────────────────
        add(NavigationSidebar.create(this, NavigationSidebar.ViewKey.ARTICLES, null), BorderLayout.WEST);

        // ── Events (logic unchanged) ────────────────────────────────────
        tableArticles.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });

        btnAjouter.addActionListener(e -> addArticle());
        btnModifier.addActionListener(e -> updateArticle());
        btnSupprimer.addActionListener(e -> deleteArticle());
        btnActualiser.addActionListener(e -> loadArticles());
        btnVider.addActionListener(e -> clearForm());
    }

    private void loadArticles() {
        try {
            List<Articles> articles = servisAticles.getAllArticles();
            tableModel.setRowCount(0);
            for (Articles article : articles) {
                tableModel.addRow(new Object[]{
                    article.getIdArticle(),
                    article.getNomArticle(),
                    article.getModeGestion(),
                    article.getDateCreation()
                });
            }
        } catch (SQLException ex) {
            showError("Erreur lors du chargement des articles: " + ex.getMessage());
        }
    }

    private void fillFormFromSelection() {
        int selectedRow = tableArticles.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        txtNom.setText(String.valueOf(tableModel.getValueAt(selectedRow, 1)));
        cmbMode.setSelectedItem(ModeGestionStock.valueOf(String.valueOf(tableModel.getValueAt(selectedRow, 2))));
    }

    private void addArticle() {
        try {
            String nom = txtNom.getText();
            ModeGestionStock mode = (ModeGestionStock) cmbMode.getSelectedItem();
            servisAticles.createArticle(nom, mode);
            loadArticles();
            clearForm();
            showInfo("Article ajoute avec succes.");
        } catch (IllegalArgumentException | SQLException ex) {
            showError("Ajout impossible: " + ex.getMessage());
        }
    }

    private void updateArticle() {
        try {
            int selectedRow = tableArticles.getSelectedRow();
            if (selectedRow < 0) {
                showError("Selectionnez un article a modifier.");
                return;
            }
            int id = Integer.parseInt(String.valueOf(tableModel.getValueAt(selectedRow, 0)));
            String nom = txtNom.getText();
            ModeGestionStock mode = (ModeGestionStock) cmbMode.getSelectedItem();
            servisAticles.updateArticle(id, nom, mode);
            loadArticles();
            clearForm();
            showInfo("Article modifie avec succes.");
        } catch (IllegalArgumentException | IllegalStateException | SQLException ex) {
            showError("Modification impossible: " + ex.getMessage());
        }
    }

    private void deleteArticle() {
        try {
            int selectedRow = tableArticles.getSelectedRow();
            if (selectedRow < 0) {
                showError("Selectionnez un article a supprimer.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Voulez-vous vraiment supprimer cet article ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            int id = Integer.parseInt(String.valueOf(tableModel.getValueAt(selectedRow, 0)));
            servisAticles.deleteArticle(id);
            loadArticles();
            clearForm();
            showInfo("Article supprime avec succes.");
        } catch (IllegalArgumentException | IllegalStateException | SQLException ex) {
            showError("Suppression impossible: " + ex.getMessage());
        }
    }

    private void clearForm() {
        txtNom.setText("");
        cmbMode.setSelectedIndex(0);
        tableArticles.clearSelection();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new Articleview().setVisible(true));
    }

}
