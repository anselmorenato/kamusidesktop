/**
 * Restorer.java
 * Created on Aug 6, 2009, 3:52:23 PM
 * @author arthur
 */
package org.kamusi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JOptionPane;

/**
 * Fetches the original database
 */
public class Restorer extends KamusiLogger
{

    /**
     * Holds the size of the current database
     */
    private long sizeOfDatabase = 0;
    /**
     * Holds the size of the update
     */
    private long sizeOfUpdate = 0;
    /**
     * The name of the app database
     */
    private static final String originaldb = "kamusiproject.db";
    /**
     * Temporary name to hold the update
     */
    private final String updatedb = "kamusiproject.db_bakup";
    /**
     * Thread to show the update progress
     */
    private RestoreProgress progress;
    /**
     * Thread that runs to perform the actual update
     */
    private RestorerThread restorer;
    /**
     * Denotes whether we can pick the poriginal database
     */
    private boolean canRestore;
    /**
     * Message to be displayed in case of an error
     */
    private String updateErrorMessage = "An error occurred while restoring database.\n" +
            "Check your connection to the Internet or try again later.";
    /**
     * The restore URL
     */
    public static final String UPDATE_URL =
            //            "http://localhost:8084/kamusiproject/kamusiproject.db";
            "http://pm.suuch.com:8080/kamusiproject/kamusiproject.db";
    private static URL url;
    /**
     * Loads system properties
     */
    private KamusiProperties props = new KamusiProperties();

    /**
     * Initializes the class
     */
    public Restorer()
    {
        canRestore = false;
    }

    /**
     * Start the restore process
     */
    public synchronized void restore()
    {
        // Create and run the two threads
        progress = new RestoreProgress();
        restorer = new RestorerThread();
        restorer.start();
        progress.start();
    }

    /**
     * Cancels the restoration
     * @return True is cancel was successful, false otherwise
     */
    protected boolean cancelUpdate()
    {
        boolean updateCancelled = false;

        String message = "Are you sure you want to cancel the database update?";

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
                options[0]); //default button title

        switch (choice)
        {
            case 0: //YES
                progress.running = false;
                progress.interrupt();
                restorer.interrupt();

                // Restore the original file
                restoreOriginal();
                updateCancelled = true;
                break;

            case 1: //NO
                // Do nothing
                updateCancelled = false;
                break;

            case -1: //Closed Window
                // Do nothing
                updateCancelled = false;
                break;

            default:
                // Do nothing
                updateCancelled = false;
                break;
        }

        return updateCancelled;
    }

    /**
     * The thread that does the restoration
     */
    class RestorerThread extends Thread
    {

        /**
         * Updates the words database
         */
        private void update()
        {
            try
            {
                // Leave the original db intact
                // Save the new restore as a temporary file

                url = new URL(UPDATE_URL);
                URLConnection connection = url.openConnection();

                BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                FileOutputStream ftpFileOutputStream = new FileOutputStream(updatedb);

                int i = 0;
                byte[] bytesIn = new byte[1024];

                while ((i = inputStream.read(bytesIn)) >= 0)
                {
                    ftpFileOutputStream.write(bytesIn, 0, i);
                }

                ftpFileOutputStream.close();
                inputStream.close();
                //Delete the original
                File original = new File(originaldb);
                original.delete();


                cleanUp();
            }
            catch (java.net.UnknownHostException ex)
            {
                log(ex.toString());
                // Restore the original file
                restoreOriginal();
                MainWindow.showError(updateErrorMessage);
            }
            catch (MalformedURLException ex)
            {
                log(ex.toString());
                // Restore the original file
                restoreOriginal();
                MainWindow.showError(updateErrorMessage);
            }
            catch (IOException ex)
            {
                log(ex.toString());
                // Restore the original file
                restoreOriginal();

                MainWindow.showError(updateErrorMessage +
                        "\n\nCheck your log files for further details regarding this error.");
            }
        }

        /**
         * Run the thread
         */
        @Override
        public void run()
        {
            yield();
            update();
        }
    }

    /**
     * Restores original db in case of an error
     */
    public void restoreOriginal()
    {
        File copy = new File(updatedb);
        copy.delete();
    }

    /**
     * Gets the size of the new restore
     * @return The size of restore to be downloaded
     */
    public long getSizeOfUpdate()
    {
        try
        {
            url = new URL(UPDATE_URL);

            URLConnection connection = url.openConnection();
            sizeOfUpdate = connection.getContentLength();

            connection.getInputStream().close();
            canRestore = true;
        }
        catch (java.net.UnknownHostException ex)
        {
            log(ex.toString());
            // Restore the original file
            restoreOriginal();
            MainWindow.showError(updateErrorMessage);
        }
        catch (MalformedURLException ex)
        {
            log(ex.toString());
            // Restore the original file
            restoreOriginal();
            MainWindow.showError(updateErrorMessage);
        }
        catch (IOException ex)
        {
            log(ex.toString());
            // Restore the original file
            restoreOriginal();
            MainWindow.showError(updateErrorMessage);
        }

        return sizeOfUpdate;
    }

    /**
     * Gets the size of the database file
     * @return The size of the database
     */
    public long getSizeOfDatabase()
    {
        File database = new File(updatedb);
        sizeOfDatabase = database.length();
        return sizeOfDatabase;
    }

    /**
     * Thread that shows the progress of the restoration
     */
    class RestoreProgress extends Thread
    {

        private boolean running = true;

        /**
         * Updates the progress bar with how much of the restore has been got
         */
        private void showDownloadProgress()
        {
            try
            {
                MainWindow.updateProgressBar();
            }
            catch (IOException ex)
            {
                log(ex.toString());
            }
        }

        /**
         * Run the thread
         */
        @Override
        public void run()
        {
            while (running)
            {
                showDownloadProgress();
            }
        }
    }

    /**
     * Gets if the db can be updated.
     * @return Boolean value if restore can be performed
     */
    public boolean canRestore()
    {
        return canRestore;
    }

    /**
     * Cleans up after an restore
     */
    private void cleanUp()
    {
        restorer.interrupt();
        progress.running = false;
        progress.interrupt();

        try
        {
            MainWindow.updateProgressBar();
        }
        catch (MalformedURLException ex)
        {
            log(ex.toString());
        }
        catch (IOException ex)
        {
            log(ex.toString());
        }
        //Rename the temp db appropriately
        File updateFile = new File(updatedb);
        updateFile.renameTo(new File(originaldb));
        log("Cleanup successful");
    }
}
