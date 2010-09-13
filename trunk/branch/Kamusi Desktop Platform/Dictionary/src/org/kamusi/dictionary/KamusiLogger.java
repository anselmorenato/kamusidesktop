/**
 * KamusiLogger.java
 * Created on Aug 18, 2010, 11:06:41 AM
 * @author arthur
 */
package org.kamusi.dictionary;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * class KamusiLogger
 */
public class KamusiLogger
{

    public final String SEPARATOR = "@";
    private String appName = NbBundle.getMessage(SearchTopComponent.class, "CTL_SearchTopComponent");
    private InputOutput io = IOProvider.getDefault().getIO(appName, false);

    /**
     * Initializes the class
     */
    public KamusiLogger()
    {
//        appName = NbBundle.getMessage(SearchTopComponent.class, "CTL_SearchTopComponent");
//        io = IOProvider.getDefault().getIO(appName, true);
        //Start by creating the logApplicationMessage folder if it does not exist
//        File logFolder = new File("log");
//        if (!logFolder.exists())
//        {
//            logFolder.mkdir();
//        }
    }

    /**
     * Logs system events to a file
     * @param message The message to be logged
     */
    public void logApplicationMessage(String message)
    {
        io.getOut().println(getTimeStamp() + " " + message);
        io.getOut().close();

        StatusDisplayer.getDefault().setStatusText(message);
    }

    /**
     * Logs the stack trace of an exception
     * @param exception The exception to log the stack trace of
     */
    public void logExceptionStackTrace(Exception exception)
    {
        Exceptions.printStackTrace(exception);
//        io.getErr().println(getTimeStamp() + exception.getMessage());
//        io.getErr().close();
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
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String date = f.format(new Date());
        String fileName = "log/" + date + ".log";
        return fileName;
    }
}
