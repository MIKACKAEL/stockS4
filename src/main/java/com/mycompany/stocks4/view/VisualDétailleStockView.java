package com.mycompany.stocks4.view;

import com.mycompany.stocks4.service.ServisMouvementsStock;
import com.mycompany.stocks4.service.ServisMouvementsStock.DetailStockRow;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class VisualDétailleStockView extends JFrame {
    private final int idArticle;
    private final String nomArticle;
    private final ServisMouvementsStock servisMouvementsStock;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel lblResume;

    public VisualDétailleStockView(int idArticle, String nomArticle) {
        this.idArticle = idArticle;
        this.nomArticle = nomArticle;
        this.servisMouvementsStock = new ServisMouvementsStock();
        this.tableModel = new DefaultTableModel(new Object[]{"Lot / Methode", "Quantite", "Prix unitaire", "Valeur"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(tableModel);
        this.lblResume = new JLabel("Stock actuel: 0");
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("Detail stock - " + nomArticle);
        setSize(900, 460);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(ModernTheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        // ── Article info panel ──────────────────────────────────────────
        JPanel top = ModernTheme.formPanel("Article");
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));

        JLabel lblArticle = ModernTheme.styledLabel("Article: " + idArticle + " - " + nomArticle);
        lblArticle.setFont(ModernTheme.FONT_SUBTITLE);
        lblArticle.setForeground(ModernTheme.TEXT_PRIMARY);

        lblResume.setFont(ModernTheme.FONT_SUBTITLE);
        lblResume.setForeground(ModernTheme.SUCCESS);

        JButton btnActualiser = ModernTheme.secondaryButton("\u21BB Actualiser");

        top.add(lblArticle);
        top.add(lblResume);
        top.add(btnActualiser);

        // ── Content wrapper ─────────────────────────────────────────────
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(ModernTheme.BG_DARK);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        contentPanel.add(ModernTheme.gradientHeader("\uD83D\uDD0D  Detail Stock - " + nomArticle), BorderLayout.NORTH);

        // Top (article info)
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(ModernTheme.BG_DARK);
        topWrapper.setBorder(new EmptyBorder(10, 0, 0, 0));
        topWrapper.add(top, BorderLayout.NORTH);

        // Table
        table.setAutoCreateRowSorter(true);
        ModernTheme.styleTable(table);
        JScrollPane scrollPane = ModernTheme.styledScrollPane(table);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(ModernTheme.BG_DARK);
        centerPanel.add(topWrapper, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // ── Sidebar ─────────────────────────────────────────────────────
        add(NavigationSidebar.create(this, NavigationSidebar.ViewKey.DETAIL, null), BorderLayout.WEST);

        // ── Events (logic unchanged) ────────────────────────────────────
        btnActualiser.addActionListener(e -> loadData());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<DetailStockRow> rows = servisMouvementsStock.getDetailStockArticle(idArticle);
            BigDecimal totalQte = BigDecimal.ZERO;
            BigDecimal totalValeur = BigDecimal.ZERO;

            for (DetailStockRow row : rows) {
                tableModel.addRow(new Object[]{
                    row.lotLabel(),
                    row.quantite(),
                    row.prixUnitaire(),
                    row.valeurLigne()
                });
                totalQte = totalQte.add(row.quantite());
                totalValeur = totalValeur.add(row.valeurLigne());
            }

            lblResume.setText("\u2139  Stock actuel: " + totalQte + "  |  Valeur Stock: " + totalValeur);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Chargement detail impossible: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void open(int idArticle, String nomArticle) {
        SwingUtilities.invokeLater(() -> new VisualDétailleStockView(idArticle, nomArticle).setVisible(true));
    }
}
