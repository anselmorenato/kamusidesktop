/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package installer;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 *
 * @author arthur
 */
public class Main
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception
    {
        JFrame.setDefaultLookAndFeelDecorated(true);
        MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        InstallerWindow installer = new InstallerWindow();
        installer.setLocationRelativeTo(null);
        installer.setVisible(true);
    }
}
