/**
 * KamusiLogger.java
 * Created on Aug 4, 2009, 10:27:51 AM
 * @author arthur
 */
package org.kamusi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * class KamusiLogger
 */
public class KamusiLogger
{

    /**
     * Initializes the class
     */
    public KamusiLogger()
    {
        //Start by creating the log folder if it does not exist
        File logFolder = new File("log");
        if (!logFolder.exists())
        {
            logFolder.mkdir();
        }
    }

    /**
     * Logs system events to a file
     * @param message The message to be logged
     */
    public void log(String message)
    {
        FileWriter fstream;
        BufferedWriter writer = null;
        try
        {
            fstream = new FileWriter(getFileName(), true);
            writer = new BufferedWriter(fstream);
            writer.write(getTimeStamp() + " " + message);
            writer.newLine();
        }
        catch (IOException ex)
        {
            MainWindow.showError(ex.toString());
        }
        finally
        {
            try
            {
                writer.close();
            }
            catch (IOException ex)
            {
                MainWindow.showError(ex.toString());
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

    /**
     * Gets the name of the log file
     * @return The log file in the form yyyy-MM-dd.log
     */
    private String getFileName()
    {
        KamusiProperties props = new KamusiProperties();
        String logFormat = props.getLogFormat();
        SimpleDateFormat f = new SimpleDateFormat(logFormat.replace(".log", ""));
        String date = f.format(new Date());
        String fileName = "log/" + date + ".log";
        return fileName;
    }
}
