package com.mycompany.stocks4.view;

import com.mycompany.stocks4.service.ServisMouvementsStock;
import com.mycompany.stocks4.service.ServisMouvementsStock.GlobalStockRow;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class VisualGlobalStockView extends JFrame {
    private final ServisMouvementsStock servisMouvementsStock;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel lblResume;

    public VisualGlobalStockView() {
        this.servisMouvementsStock = new ServisMouvementsStock();
        this.tableModel = new DefaultTableModel(new Object[]{"ID Article", "Article", "Mode", "Quantite disponible", "CUMP actuel"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(tableModel);
        this.lblResume = new JLabel("Articles en stock: 0");
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("Etat global du stock");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(ModernTheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        // ── Resume Panel ────────────────────────────────────────────────
        JPanel top = ModernTheme.formPanel("Resume");
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));

        lblResume.setFont(ModernTheme.FONT_SUBTITLE);
        lblResume.setForeground(ModernTheme.TEXT_PRIMARY);

        JButton btnActualiser = ModernTheme.secondaryButton("\u21BB Actualiser");
        JButton btnDetail = ModernTheme.primaryButton("\uD83D\uDD0D Voir detail article");

        top.add(lblResume);
        top.add(btnActualiser);
        top.add(btnDetail);

        // ── Content wrapper ─────────────────────────────────────────────
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(ModernTheme.BG_DARK);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        contentPanel.add(ModernTheme.gradientHeader("\uD83D\uDCCA  Etat Global du Stock"), BorderLayout.NORTH);

        // Top (resume)
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(ModernTheme.BG_DARK);
        topWrapper.setBorder(new EmptyBorder(10, 0, 0, 0));
        topWrapper.add(top, BorderLayout.NORTH);

        // Table
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        add(NavigationSidebar.create(this, NavigationSidebar.ViewKey.GLOBAL, this::openDetailFromSelection), BorderLayout.WEST);

        // ── Events (logic unchanged) ────────────────────────────────────
        btnActualiser.addActionListener(e -> loadData());
        btnDetail.addActionListener(e -> openDetailFromSelection());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openDetailFromSelection();
                }
            }
        });
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<GlobalStockRow> rows = servisMouvementsStock.getEtatGlobalStock();
            BigDecimal totalQte = BigDecimal.ZERO;
            for (GlobalStockRow row : rows) {
                tableModel.addRow(new Object[]{
                    row.idArticle(),
                    row.nomArticle(),
                    row.modeGestion(),
                    row.quantiteDisponible(),
                    row.cumpActuel()
                });
                totalQte = totalQte.add(row.quantiteDisponible());
            }
            lblResume.setText("\u2139  Articles en stock: " + rows.size() + "  |  Quantite totale: " + totalQte);
        } catch (Exception ex) {
            showError("Chargement global impossible: " + ex.getMessage());
        }
    }

    private void openDetailFromSelection() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            showError("Selectionnez un article dans la liste.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int idArticle = Integer.parseInt(String.valueOf(tableModel.getValueAt(modelRow, 0)));
        String nomArticle = String.valueOf(tableModel.getValueAt(modelRow, 1));
        new VisualDétailleStockView(idArticle, nomArticle).setVisible(true);
        dispose();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new VisualGlobalStockView().setVisible(true));
    }
}
