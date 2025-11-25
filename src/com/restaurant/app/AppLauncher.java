package com.restaurant.app;

import com.restaurant.app.ui.RestaurantApp;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Application entry point responsible for configuring the LAF
 * and launching the main Swing frame.
 */
public final class AppLauncher {

    private AppLauncher() {
        // no instances
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            installNimbusIfAvailable();
            RestaurantApp app = new RestaurantApp();
            app.setVisible(true);
        });
    }

    private static void installNimbusIfAvailable() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException
                 | InstantiationException
                 | IllegalAccessException
                 | UnsupportedLookAndFeelException ex) {
            System.err.println("Unable to set look and feel: " + ex.getMessage());
        }
    }
}


