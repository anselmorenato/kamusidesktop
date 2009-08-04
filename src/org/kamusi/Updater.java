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
 * Fetched an update of the database
 * @author arthur
 */
public class Updater
{

    private long sizeOfDatabase = 0;
    private long sizeOfUpdate = 0;
    private static final String originaldb = "kamusiproject.db";
    private final String updatedb = "kamusiproject.db_bakup";
    private long totalSizeOfUpdate = 0;
    private DownloadProgressBar progress;
    private UpdaterThread update;
    private boolean canUpdate;
    private LoggingUtil util;
    /**
     * The update URL
     */
    public static final String UPDATE_URL =
            //"http://localhost/kamusi/kamusiproject.db";
            "http://pm.suuch.com:8080/kamusiproject/kamusiproject.db";
    private static URL url;

    public Updater()
    {
        canUpdate = false;
        util = new LoggingUtil();
    }

    public synchronized void update()
    {
        // Create and run the two threads
        progress = new DownloadProgressBar();
        update = new UpdaterThread();
        update.start();
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
                update.interrupt();
                MainWindow.showInfo("Database update download has been cancelled.");

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

    class UpdaterThread extends Thread
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

                //Rename the temp db appropriately
                File update = new File(updatedb);
                update.renameTo(new File(originaldb));

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
     */
    public long getSizeOfUpdate()
    {
        try
        {
            url = new URL(UPDATE_URL);

            URLConnection connection = url.openConnection();
            sizeOfUpdate = connection.getContentLength();

            connection.getInputStream().close();
            canUpdate = true;
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
     */
    public long getSizeOfDatabase()
    {
        File database = new File(updatedb);
        sizeOfDatabase = database.length();
        return sizeOfDatabase;
    }

    class DownloadProgressBar extends Thread
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
     */
    public boolean canUpdate()
    {
        return canUpdate;
    }

    /**
     * Cleans up after an update
     */
    private void cleanUp()
    {
        progress.running = false;
        progress.interrupt();
        util.log("Cleanup successful");
        MainWindow.showInfo("Database updated successfully.");
    }
}
