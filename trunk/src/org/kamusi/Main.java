package org.kamusi;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Main
 * @author arthur
 */
public class Main
{
    /**
     * The starting point of the application
     * @param args Arguments to be passed to the application
     */
    public static void main(String[] args)
    {
        JFrame.setDefaultLookAndFeelDecorated(true);

        try
        {
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
            UIManager.setLookAndFeel(new MetalLookAndFeel());

        }
        catch (UnsupportedLookAndFeelException ex)
        {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Kamusi Desktop",
                    JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        }
    }
}
