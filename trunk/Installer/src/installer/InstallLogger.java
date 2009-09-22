/**
 * Logger.java
 * Created on Sep 6, 2009, 3:56:09 PM
 * @author arthur
 */
package installer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * class Logger
 */
public class InstallLogger
{
    public InstallLogger()
    {
        
    }
    /**
     * Logs system events to a file
     * @param message The message to be logged
     */
    public void log(String message)
    {
//        System.out.println(message);
        
        FileWriter fstream;
        BufferedWriter writer = null;
        try
        {
            fstream = new FileWriter("install.log", true);
            writer = new BufferedWriter(fstream);
            writer.write("[" + getTimeStamp() + "] " + message);
            writer.newLine();
        }
        catch (IOException ex)
        {
            log(ex.toString());
        }
        finally
        {
            try
            {
                writer.close();
            }
            catch (IOException ex)
            {
                log(ex.toString());
            }
        }
    }

    /**
     * Gets the current system time
     * @return The time in the form yyyy-MM-dd HH:mm:ss
     */
    private String getTimeStamp()
    {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return f.format(new Date());
    }
}