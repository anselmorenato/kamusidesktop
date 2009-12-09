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
import javax.swing.plaf.metal.OceanTheme;

/**
 * Main
 */
public class Main
{

    private static String LOOKANDFEEL;
    private static String THEME;
    private static KamusiLogger logger = new KamusiLogger();
    private static KamusiProperties props = new KamusiProperties();

    /**
     * The starting point of the application
     * @param args Arguments to be passed to the application
     */
    public static void main(String[] args)
    {
        String language;
        String country;

        if (args.length != 2)
        {
            language = new String("en");
            country = new String("US");
        }
        else
        {
            language = new String(args[0]);
            country = new String(args[1]);
        }

        logSystemProperties();
        initLookAndFeel();
        initDatabaseDriver();

        if (props.getEditor())
        {
            if (askForUsername())
            {
                runApplication();
            }
        }
        else
        {
            runApplication();
        }
    }

    /**
     * Starts up the main application
     */
    private static void runApplication()
    {
        MainWindow mainWindow = new MainWindow();
        mainWindow.setResizable(false);
        mainWindow.setVisible(true);
    }

    /**
     * Prompts for the kamusi.org username for the sake of editing
     * @return True for successful setting of username, false otherwise
     */
    private static boolean askForUsername()
    {
        boolean canEdit = false;

        Editor editor = new Editor();

        String username = editor.getUsername();

        if ((username == null) || (username.trim().length() == 0))
        {

            String newUsername = JOptionPane.showInputDialog(null,
                    props.getName() + " needs your kamusi.org username" +
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
                        props.getName(),
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

    /**
     * Sets the application look and feel
     */
    private static void initLookAndFeel()
    {
        LOOKANDFEEL = props.getLookAndFeel();
        THEME = props.getTheme();

        String lookAndFeel = null;

        if (props.isDecorated())
        {
            JFrame.setDefaultLookAndFeelDecorated(true);
        }

        if (LOOKANDFEEL != null)
        {
            if (LOOKANDFEEL.equals("Metal"))
            {
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            }
            else if (LOOKANDFEEL.equals("System"))
            {
                lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            }
            else if (LOOKANDFEEL.equals("Motif"))
            {
                lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            }
            else if (LOOKANDFEEL.equals("GTK"))
            {
                lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            }
            else if (LOOKANDFEEL.equals("Nimbus"))
            {
                lookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
            }
            else if (LOOKANDFEEL.equals("Windows"))
            {
                lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
            }
            else
            {
                logger.log("Unexpected value of LOOKANDFEEL specified: " + LOOKANDFEEL);
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            }

            try
            {
                UIManager.setLookAndFeel(lookAndFeel);

                if (LOOKANDFEEL.equals("Metal"))
                {
                    if (THEME.equals("DefaultMetal"))
                    {
                        MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
                    }
                    else if (THEME.equals("Ocean"))
                    {
                        MetalLookAndFeel.setCurrentTheme(new OceanTheme());
                    }
                    UIManager.setLookAndFeel(new MetalLookAndFeel());
                }
            }
            catch (ClassNotFoundException e)
            {
                logger.log("Couldn't find class for specified look and feel:" + lookAndFeel);
                logger.log("Did you include the L&F library in the class path?");
                logger.log("Using the default look and feel.");
            }
            catch (UnsupportedLookAndFeelException e)
            {
                logger.log("Can't use the specified look and feel (" + lookAndFeel + ") on this platform.");
                logger.log("Using the default look and feel.");
            }
            catch (Exception e)
            {
                logger.log("Couldn't get specified look and feel (" + lookAndFeel + "), for some reason.");
                logger.log("Using the default look and feel.");
                logger.log(e.toString());
            }
        }
    }

    /**
     * Logs system properties to file
     * This is useful especially when dealing with errors
     */
    private static void logSystemProperties()
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
    }

    /**
     * Initializes the database driver
     */
    private static void initDatabaseDriver()
    {
        try
        {
            Class.forName("org.sqlite.JDBC").newInstance();
        }
        catch (Exception ex)
        {
            logger.log(ex.toString());
        }
    }
}
