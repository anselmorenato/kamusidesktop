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
        if (askForUsername())
        {

            KamusiLogger logger = new KamusiLogger();

//            JFrame.setDefaultLookAndFeelDecorated(true);

            try
            {
//                MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
//                UIManager.setLookAndFeel(new MetalLookAndFeel());
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (Exception ex)
            {
                logger.log(ex.toString());
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Kamusi Desktop",
                        JOptionPane.ERROR_MESSAGE);
            }
            finally
            {
                StringBuffer systemProperties = new StringBuffer();
                systemProperties.append("[ ");
                systemProperties.append(String.valueOf(new java.util.Date()));
                systemProperties.append(" " + System.getProperty("os.name"));
                systemProperties.append(" " + System.getProperty("os.version"));
                systemProperties.append(" " + System.getProperty("java.version"));
                systemProperties.append(" " + System.getProperty("java.vendor"));
                systemProperties.append(" ]");
                logger.log(systemProperties.toString());
                MainWindow mainWindow = new MainWindow();
                mainWindow.setVisible(true);
            }
        }
    }

    /**
     * Prompts for the kamusi.org username for the sake of editing
     */
    private static boolean askForUsername()
    {
        boolean canEdit = false;

        Editor editor = new Editor();

        String username = editor.getUsername();

        if ((username == null) || (username.trim().length() == 0))
        {

            String newUsername = JOptionPane.showInputDialog(null,
                    "Kamusi Desktop needs your kamusi.org username" +
                    "\nin order to process your edits. You will not be asked for this again." +
                    "\n\nPlease enter your kamusi.org username");

            if ((newUsername == null) || (newUsername.trim().length() == 0))
            {
                String message = "You will not be able to edit entries.\n\n" +
                        "Proceed?";
                Object[] options =
                {
                    "Yes",
                    "No"
                };

                int choice = JOptionPane.showOptionDialog(null,
                        message,
                        "Kamusi Desktop",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, //do not use a custom Icon
                        options, //the titles of buttons
                        options[1]); //default button title

                switch (choice)
                {
                    case 0: //YES
                        canEdit = true;
                        break;

                    case 1: //NO
                        System.gc();
                        main(null);
                        break;

                    case -1: //Closed Window
                        canEdit = true;
                        break;

                    default:
                        System.exit(0);
                        break;
                }
            }
            else
            {
                if (new Editor().setUsername(newUsername))
                {
                    canEdit = true;
                }
            }
        }
        else
        {
            canEdit = true;
        }
        return canEdit;
    }
}
