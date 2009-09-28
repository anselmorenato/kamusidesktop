/**
 * KamusiProperties.java
 * Created on Sep 22, 2009, 8:22:45 PM
 * @author arthur
 */
package org.kamusi;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * class KamusiProperties
 */
public class KamusiProperties extends KamusiLogger
{

    private final String propertiesFile = "conf/app.properties";
    private FileInputStream applicationProperties;
    private Properties properties;
    private String name;
    private String version;
    private boolean decorated;
    private String theme;
    private String lookAndFeel;
    private String fileMenu;
    private String fileSynchronize;
    private String filePrint;
    private String editor;

    public boolean isDecorated()
    {
        if (System.getProperty("app.decorated").trim().equals("0"))
        {
            decorated = false;
        }
        else
        {
            decorated = true;
        }
        return decorated;
    }

    public String getEditMenu()
    {
        return editMenu;
    }

    public String getFileMenu()
    {
        return fileMenu;
    }

    public String getFilePrint()
    {
        return filePrint;
    }

    public String getFileQuit()
    {
        return fileQuit;
    }

    public String getFileSynchronize()
    {
        return fileSynchronize;
    }

    public String getHelpAbout()
    {
        return helpAbout;
    }

    public String getHelpMenu()
    {
        return helpMenu;
    }

    public String getHelpRestore()
    {
        return helpRestore;
    }

    public String getLogFormat()
    {
        return logFormat;
    }

    public String getLookAndFeel()
    {
        return lookAndFeel;
    }

    public String getName()
    {
        return name + " " + getVersion();
    }

    public String getVersion()
    {
        return version;
    }

    public String getTheme()
    {
        return theme;
    }

    public boolean getEditor()
    {
        return editor.equals("0")? false: true;
    }

    private String fileQuit;
    private String editMenu;
    private String helpMenu;
    private String helpRestore;
    private String helpAbout;
    private String logFormat;

    public KamusiProperties()
    {
        try
        {
            applicationProperties = new FileInputStream(propertiesFile);
            properties = new Properties(System.getProperties());
            properties.load(applicationProperties);

            // set the system properties
            System.setProperties(properties);


            name = System.getProperty("app.name").trim();
//            version = System.getProperty("app.version").trim();
            version = "2.0.1_Beta";
            decorated = false;
            lookAndFeel = System.getProperty("app.lookAndFeel").trim();
            theme = System.getProperty("app.theme").trim();

            fileMenu = System.getProperty("file.menu").trim();
            fileSynchronize = System.getProperty("file.synchronize").trim();
            filePrint = System.getProperty("file.print").trim();
            fileQuit = System.getProperty("file.quit").trim();

            editMenu = System.getProperty("edit.menu").trim();

            helpMenu = System.getProperty("help.menu").trim();
            helpRestore = System.getProperty("help.restore").trim();
            helpAbout = System.getProperty("help.about").trim();

            logFormat = System.getProperty("log.format").trim();

            editor = System.getProperty("app.editor").trim();
        }
        catch (Exception ex)
        {
            log(ex.toString());
        }
    }
}
