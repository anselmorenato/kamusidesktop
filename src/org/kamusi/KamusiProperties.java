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
    private String stackTrace;
    private String editor;
    private String restore_url;
    private String sync_url;

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

    public String getRestoreURL()
    {
        return restore_url;
    }

    public String getSyncURL()
    {
        return sync_url;
    }

    public boolean getEditor()
    {
        return editor.equals("0")? false: true;
    }

    public boolean printStackTrace()
    {
        return stackTrace.equals("1")? true: false;
    }

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

            name = "Kamusi Desktop";
//            version = System.getProperty("app.version").trim();
            version = "2.6_Alpha";
            decorated = false;
            lookAndFeel = System.getProperty("app.lookAndFeel").trim();
            theme = System.getProperty("app.theme").trim();

            stackTrace = System.getProperty("app.print_stack_trace").trim();

            logFormat = System.getProperty("log.format").trim();

            editor = System.getProperty("app.editor").trim();
            restore_url = System.getProperty("restore_url").trim();
            sync_url = System.getProperty("sync_url").trim();
        }
        catch (Exception ex)
        {
            MainWindow.showError(ex);
            log(ex.toString());
        }
    }
}
