package com.mycompany.stocks4.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public final class NavigationSidebar {
    public enum ViewKey {
        ARTICLES,
        MOUVEMENTS,
        GLOBAL,
        DETAIL
    }

    private NavigationSidebar() {
    }

    public static JPanel create(JFrame currentFrame, ViewKey currentView, Runnable openDetailAction) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(ModernTheme.SIDEBAR_BG);
        wrapper.setPreferredSize(new Dimension(ModernTheme.SIDEBAR_WIDTH, 0));
        wrapper.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ModernTheme.BORDER_COLOR));

        // ── Title section ───────────────────────────────────────────────
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(ModernTheme.SIDEBAR_BG);
        titlePanel.setBorder(new EmptyBorder(20, 15, 20, 15));
        JLabel appTitle = new JLabel("\u2693 StockS4");
        appTitle.setFont(ModernTheme.FONT_TITLE);
        appTitle.setForeground(ModernTheme.ACCENT);
        appTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.add(appTitle);

        JLabel subtitle = new JLabel("Gestion de stock");
        subtitle.setFont(ModernTheme.FONT_SMALL);
        subtitle.setForeground(ModernTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);
        wrapper.add(titlePanel, BorderLayout.NORTH);

        // ── Navigation buttons ──────────────────────────────────────────
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setBackground(ModernTheme.SIDEBAR_BG);
        buttons.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnArticles = ModernTheme.navButton("\uD83D\uDCE6  Articles", currentView == ViewKey.ARTICLES);
        JButton btnMouvements = ModernTheme.navButton("\uD83D\uDD04  Mouvements", currentView == ViewKey.MOUVEMENTS);
        JButton btnGlobal = ModernTheme.navButton("\uD83D\uDCCA  Stock Global", currentView == ViewKey.GLOBAL);
        JButton btnDetail = ModernTheme.navButton("\uD83D\uDD0D  Detail Stock", currentView == ViewKey.DETAIL);

        btnArticles.setEnabled(currentView != ViewKey.ARTICLES);
        btnMouvements.setEnabled(currentView != ViewKey.MOUVEMENTS);
        btnGlobal.setEnabled(currentView != ViewKey.GLOBAL);
        btnDetail.setEnabled(currentView != ViewKey.DETAIL && openDetailAction != null);

        btnArticles.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnMouvements.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGlobal.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDetail.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnArticles.addActionListener(e -> {
            new Articleview().setVisible(true);
            currentFrame.dispose();
        });
        btnMouvements.addActionListener(e -> {
            new MouvementsStockView().setVisible(true);
            currentFrame.dispose();
        });
        btnGlobal.addActionListener(e -> {
            new VisualGlobalStockView().setVisible(true);
            currentFrame.dispose();
        });
        if (openDetailAction != null) {
            btnDetail.addActionListener(e -> openDetailAction.run());
        }

        buttons.add(btnArticles);
        buttons.add(Box.createVerticalStrut(6));
        buttons.add(btnMouvements);
        buttons.add(Box.createVerticalStrut(6));
        buttons.add(btnGlobal);
        buttons.add(Box.createVerticalStrut(6));
        buttons.add(btnDetail);
        buttons.add(Box.createVerticalGlue());

        wrapper.add(buttons, BorderLayout.CENTER);
        return wrapper;
    }
}
