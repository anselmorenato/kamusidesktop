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
        //Start by creating the logApplicationMessage folder if it does not exist
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
    public void logApplicationMessage(String message)
    {
        FileWriter fstream;
        BufferedWriter writer = null;
        try
        {
            fstream = new FileWriter(getLogFileName(), true);
            writer = new BufferedWriter(fstream);
            writer.write(getTimeStamp() + " " + message);
            writer.newLine();
        }
        catch (IOException ex)
        {
            MainWindow.showError(ex);
        }
        finally
        {
            try
            {
                writer.close();
            }
            catch (IOException ex)
            {
                MainWindow.showError(ex);
            }
        }
    }

    /**
     * Logs the stack trace of an exception
     * @param exception The exception to log the stack trace of
     */
    public void logExceptionStackTrace(Exception exception)
    {
        KamusiProperties props = new KamusiProperties();
        
        System.out.println("props.printStackTrace() -> " + props.printStackTrace());

        if (props.printStackTrace())
        {
            exception.printStackTrace();
        }

        String exceptionStacktrace = "";

        StackTraceElement[] ex = exception.getStackTrace();

        for (StackTraceElement e : ex)
        {
            exceptionStacktrace += ("at " + e.getClassName() + ".");
            exceptionStacktrace += (e.getMethodName());
            exceptionStacktrace += ("(");
            exceptionStacktrace += (e.getFileName());
            exceptionStacktrace += (":");
            exceptionStacktrace += (e.getLineNumber());
            exceptionStacktrace += (")");
            exceptionStacktrace += ("\n");
        }

        logApplicationMessage("[ System Error ]" + "\r\n"
                + exception.toString() + "\r\n" + exceptionStacktrace);
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
     * Gets the name of the logApplicationMessage file
     * @return The logApplicationMessage file in the form yyyy-MM-dd.logApplicationMessage
     */
    public String getLogFileName()
    {
        KamusiProperties props = new KamusiProperties();
        String logFormat = props.getLogFormat();
        SimpleDateFormat f = new SimpleDateFormat(logFormat.replace(".log", ""));
        String date = f.format(new Date());
        String fileName = "log/" + date + ".log";
        return fileName;
    }
}
