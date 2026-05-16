/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.stocks4;

import com.formdev.flatlaf.FlatDarkLaf;
import com.mycompany.stocks4.view.Articleview;
import com.mycompany.stocks4.view.ModernTheme;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author TUF
 */
public class StockS4 {

    public static void main(String[] args) {
        // Install modern FlatLaf dark theme
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to set FlatLaf look and feel: " + ex.getMessage());
        }
        ModernTheme.applyGlobalDefaults();
        Articleview.open();
    }
}
