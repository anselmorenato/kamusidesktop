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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Fetches the original database
 */
public class Restorer
{

    private long sizeOfDatabase = 0;
    private long sizeOfUpdate = 0;
    private static final String originaldb = "kamusiproject.db";
    private final String updatedb = "kamusiproject.db_bakup";
    private long totalSizeOfUpdate = 0;
    private RestorerProgress progress;
    private RestorerThread restorer;
    private boolean canRestore;
    private LoggingUtil util;
    /**
     * The update URL
     */
    public static final String UPDATE_URL =
//            "http://localhost/kamusi/kamusiproject.db";
            "http://pm.suuch.com:8080/kamusiproject/kamusiproject.db";
    private static URL url;

    public Restorer()
    {
        canRestore = false;
        util = new LoggingUtil();
    }

    public synchronized void update()
    {
        // Create and run the two threads
        progress = new RestorerProgress();
        restorer = new RestorerThread();
        restorer.start();
        progress.start();
    }

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
                "Kamusi Desktop",
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
                // Save the new update as a temporary file

                url = new URL(UPDATE_URL);
                URLConnection connection = url.openConnection();

                // Get size of update
                totalSizeOfUpdate = getSizeOfUpdate();

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
                util.log(ex.getMessage());
                // Restore the original file
                restoreOriginal();
                MainWindow.showError("An error occurred while connecting to the update server.");
            }
            catch (MalformedURLException ex)
            {
                util.log(ex.getMessage());
                // Restore the original file
                restoreOriginal();
                MainWindow.showError("An error occurred while updating database.");
            }
            catch (IOException ex)
            {
                util.log(ex.getMessage());
                // Restore the original file
                restoreOriginal();

                if (ex.getMessage().contains("Permission denied"))
                {
                    MainWindow.showError("An error occurred while updating database.\n" +
                            "Do you have permissions to write to the Kamusi Desktop installation directory?");
                }
                else
                {
                    MainWindow.showError("An error occurred while updating database.");
                }
            }
        }

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
     * Gets the size of the new update
     * @return The size of update to be downloaded
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
            util.log(ex.getMessage());
            // Restore the original file
            restoreOriginal();
            MainWindow.showError("An error occurred while connecting to the update server.");
        }
        catch (MalformedURLException ex)
        {
            util.log(ex.getMessage());
            // Restore the original file
            restoreOriginal();
            MainWindow.showError("An error occurred while updating database.");
        }
        catch (IOException ex)
        {
            util.log(ex.getMessage());
            // Restore the original file
            restoreOriginal();
            MainWindow.showError("An error occurred while updating database.");
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

    class RestorerProgress extends Thread
    {

        private boolean running = true;

        /**
         * Updates the progress bar with how much of the update has been got
         */
        private void showDownloadProgress()
        {
            try
            {
                MainWindow.updateProgressBar();
            }
            catch (IOException ex)
            {
                util.log(ex.getMessage());
            }
        }

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
     * @return Boolean value if update can be performed
     */
    public boolean canRestore()
    {
        return canRestore;
    }

    /**
     * Cleans up after an update
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
            Logger.getLogger(Restorer.class.getName()).log(Level.SEVERE, null, ex);
        }        catch (IOException ex)
        {
            Logger.getLogger(Restorer.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Rename the temp db appropriately
        File updateFile = new File(updatedb);
        updateFile.renameTo(new File(originaldb));
        util.log("Cleanup successful");
    }
}
