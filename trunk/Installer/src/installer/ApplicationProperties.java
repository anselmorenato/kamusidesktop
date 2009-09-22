package installer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author arthur
 */
public class ApplicationProperties
{

    private final String propertiesFile = "build.properties";
    private FileInputStream applicationProperties;
    private Properties properties;
    private String nameOfAplication;
    private String requiredLibraries;
    private String logo;
    private String defaultLocation;
    private String appJar;
    private String targetOs;

    public String getMainApplicationName()
    {
        return nameOfAplication;
    }

    public String[] getRequiredLibraries()
    {
        return requiredLibraries.split("\\,");
    }

    public String getDefaultLocation()
    {
        return defaultLocation;
    }

    public String getLogo()
    {
        return logo;
    }

    public String getTargetOS()
    {
        return targetOs;
    }

    public String getAppJar()
    {
        return appJar;
    }

    public ApplicationProperties()
    {
        try
        {
            applicationProperties = new FileInputStream(propertiesFile);
//        properties = new Properties(System.getProperties());
            properties = new Properties(System.getProperties());
            properties.load(applicationProperties);

            // set the system properties
            System.setProperties(properties);
            // display new properties
//        System.getProperties().list(System.out);
            // TODO code application logic here
            nameOfAplication = System.getProperty("app.name").trim();
            requiredLibraries = System.getProperty("app.libraries").trim();
            logo = System.getProperty("app.logo").trim();
            defaultLocation = System.getProperty("user.home") + System.getProperty("file.separator") +
                    System.getProperty("app.name").trim();
            appJar = System.getProperty("app.jar").trim();
            targetOs = System.getProperty("target.os").trim();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }
}
