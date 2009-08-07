/**
 * LoggingUtil.java
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * class LoggingUtil
 */
public class LoggingUtil
{

    public LoggingUtil()
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
     * @param level The level of the message
     * @param message The message to be logged
     */
    public void log(String message)
    {
        FileWriter fstream;
        BufferedWriter out = null;
        try
        {
            fstream = new FileWriter(getFileName(), true);
            out = new BufferedWriter(fstream);
            out.write(getTimeStamp() + " " + message + "\n");
        }
        catch (IOException ex)
        {
            Logger.getLogger(LoggingUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(LoggingUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String getTimeStamp()
    {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return f.format(new Date());
    }

    private String getFileName()
    {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String date = f.format(new Date());
        String fileName = "log/" + date + ".log";
        return fileName;
    }
}
