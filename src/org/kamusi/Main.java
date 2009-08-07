/**
 * Main.java
 * Created on Aug 6, 2009, 3:52:23 PM
 * @author arthur
 */
package org.kamusi;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Main
 */
public class Main
{
    /**
     * The starting point of the application
     * @param args Arguments to be passed to the application
     */
    public static void main(String[] args)
    {
        LoggingUtil util = new LoggingUtil();

        JFrame.setDefaultLookAndFeelDecorated(true);

        try
        {
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        }
        catch (UnsupportedLookAndFeelException ex)
        {
            util.log(ex.getMessage());
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
