package com.mycompany.stocks4.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Centralized modern theme constants and utilities for the application UI.
 */
public final class ModernTheme {

    // ── Color Palette ───────────────────────────────────────────────────
    public static final Color BG_DARK       = new Color(30, 33, 40);
    public static final Color BG_PANEL      = new Color(38, 42, 52);
    public static final Color BG_CARD       = new Color(46, 51, 63);
    public static final Color BG_INPUT      = new Color(55, 60, 75);
    public static final Color BG_HEADER     = new Color(35, 39, 48);
    public static final Color ACCENT        = new Color(99, 140, 255);
    public static final Color ACCENT_HOVER  = new Color(130, 165, 255);
    public static final Color SUCCESS       = new Color(80, 200, 120);
    public static final Color DANGER        = new Color(235, 87, 87);
    public static final Color WARNING       = new Color(242, 183, 5);
    public static final Color TEXT_PRIMARY   = new Color(230, 233, 240);
    public static final Color TEXT_SECONDARY = new Color(160, 170, 190);
    public static final Color BORDER_COLOR  = new Color(60, 65, 80);
    public static final Color TABLE_ROW_ALT = new Color(42, 46, 58);
    public static final Color TABLE_SEL_BG  = new Color(99, 140, 255, 60);
    public static final Color SIDEBAR_BG    = new Color(25, 28, 35);

    // ── Fonts ───────────────────────────────────────────────────────────
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_TABLE     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_TABLE_HDR = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BUTTON    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_NAV       = new Font("Segoe UI", Font.BOLD, 13);

    // ── Sizes ───────────────────────────────────────────────────────────
    public static final int SIDEBAR_WIDTH   = 180;
    public static final int BUTTON_HEIGHT   = 36;
    public static final int INPUT_HEIGHT    = 34;
    public static final int BORDER_RADIUS   = 8;

    private ModernTheme() {}

    // ── Global FlatLaf Overrides ────────────────────────────────────────
    public static void applyGlobalDefaults() {
        UIManager.put("TitledBorder.titleColor", TEXT_PRIMARY);
        UIManager.put("TitledBorder.border", BorderFactory.createLineBorder(BORDER_COLOR));
        UIManager.put("OptionPane.background", BG_PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Panel.background", BG_PANEL);
        UIManager.put("OptionPane.buttonFont", FONT_BUTTON);
    }

    // ── Styled Components ───────────────────────────────────────────────

    /** Create a styled primary button (accent color). */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, ACCENT, Color.WHITE);
        return btn;
    }

    /** Create a styled success button (green). */
    public static JButton successButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, SUCCESS, Color.WHITE);
        return btn;
    }

    /** Create a styled danger button (red). */
    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, DANGER, Color.WHITE);
        return btn;
    }

    /** Create a styled secondary / neutral button. */
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, BG_INPUT, TEXT_PRIMARY);
        return btn;
    }

    /** Create a styled navigation button for the sidebar. */
    public static JButton navButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_NAV);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(SIDEBAR_WIDTH - 20, 40));
        btn.setMaximumSize(new Dimension(SIDEBAR_WIDTH - 20, 40));
        if (active) {
            btn.setBackground(ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setEnabled(false);
        } else {
            btn.setBackground(SIDEBAR_BG);
            btn.setForeground(TEXT_SECONDARY);
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    btn.setBackground(BG_INPUT);
                    btn.setForeground(TEXT_PRIMARY);
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.setBackground(SIDEBAR_BG);
                    btn.setForeground(TEXT_SECONDARY);
                }
            });
        }
        return btn;
    }

    private static void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, BUTTON_HEIGHT));
        btn.putClientProperty("JButton.buttonType", "roundRect");

        Color hoverBg = bg.brighter();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(hoverBg);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(bg);
            }
        });
    }

    /** Style a text field with modern look. */
    public static void styleTextField(JTextField tf) {
        tf.setFont(FONT_BODY);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT);
        tf.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, INPUT_HEIGHT));
    }

    /** Style a combo box with modern look. */
    public static <T> void styleComboBox(JComboBox<T> cb) {
        cb.setFont(FONT_BODY);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRIMARY);
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, INPUT_HEIGHT));
    }

    /** Create a styled label. */
    public static JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    /** Create a title label. */
    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    /** Create a modern titled border. */
    public static Border modernTitledBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                "  " + title + "  ");
        tb.setTitleFont(FONT_SUBTITLE);
        tb.setTitleColor(ACCENT);
        return new CompoundBorder(tb, new EmptyBorder(10, 10, 10, 10));
    }

    // ── Table Styling ───────────────────────────────────────────────────

    /** Style a JTable with modern dark theme. */
    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(TABLE_SEL_BG);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(36);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? BG_CARD : TABLE_ROW_ALT);
                }
                c.setForeground(isSelected ? TEXT_PRIMARY : TEXT_PRIMARY);
                ((JComponent) c).setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HDR);
        header.setBackground(BG_HEADER);
        header.setForeground(ACCENT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                c.setFont(FONT_TABLE_HDR);
                c.setBackground(BG_HEADER);
                c.setForeground(ACCENT);
                ((JComponent) c).setBorder(new CompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT),
                        new EmptyBorder(0, 10, 0, 10)
                ));
                return c;
            }
        });
    }

    /** Style a scroll pane wrapping a table. */
    public static JScrollPane styledScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        sp.getViewport().setBackground(BG_CARD);
        return sp;
    }

    // ── Panel Utilities ─────────────────────────────────────────────────

    /** Create a form panel with modern border and background. */
    public static JPanel formPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(BG_PANEL);
        panel.setBorder(modernTitledBorder(title));
        return panel;
    }

    /** Apply dark background to a panel. */
    public static void darkPanel(JPanel panel) {
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    // ── Gradient Header Panel ───────────────────────────────────────────

    /** Create a gradient header panel with a title. */
    @SuppressWarnings("serial")
    public static JPanel gradientHeader(String title) {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(40, 55, 95),
                        getWidth(), 0, new Color(25, 30, 45));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 50));
        header.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 12));
        JLabel lbl = titleLabel(title);
        header.add(lbl);
        return header;
    }
}
