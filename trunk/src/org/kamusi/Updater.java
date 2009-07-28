package org.kamusi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author arthur
 */
public class Updater
{

    private static final String originaldb = "kamusiproject.db";
    private final String updatedb = "kamusiproject.db_bakup";
    private long totalSizeOfUpdate = 0;
    private DownloadProgressBar progress;
    private UpdaterThread update;

    ;
    /**
     * The update URL
     */
    public static final String UPDATE_URL =
                        "http://localhost/kamusidesktop/kamusiproject.db";
//            "http://pm.suuch.com:8080/kamusiproject/kamusiproject.db";
    private static URL url;

    public Updater()
    {
        progress = new DownloadProgressBar();
        update = new UpdaterThread();
    }

    public synchronized void update()
    {
        // Create and run the two threads
        update.start();
    }

    void cancelUpdate()
    {
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

                System.exit(0);

                break;

            case 1: //NO
                // Do nothing
                break;

            case -1: //Closed Window
                // Do nothing
                break;

            default:
                // Do nothing
                break;
        }
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
                // Get size of update
                totalSizeOfUpdate = getSizeOfUpdate();

                // Leave the original db intact
                // Save the new update as a temporary file

                url = new URL(UPDATE_URL);
                URLConnection connection = url.openConnection();
                BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                FileOutputStream ftpFileOutputStream = new FileOutputStream(updatedb);

                int i = 0;
                byte[] bytesIn = new byte[1024];

                while ((i = inputStream.read(bytesIn)) >= 0)
                {
                    ftpFileOutputStream.write(bytesIn, 0, i);
                    new Thread(progress).run();
                }

                ftpFileOutputStream.close();
                inputStream.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                // Restore the original file
                restoreOriginal();
                JOptionPane.showMessageDialog(null, "An error occurred while updating database. Reverted to original database.",
                        "Kamusi Desktop", JOptionPane.ERROR_MESSAGE);
            }

            //Delete the original
            File original = new File(originaldb);
            original.delete();

            //Rename the temp db appropriately
            File update = new File(updatedb);
            update.renameTo(new File(originaldb));
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
        long size = 0;

        try
        {
            url = new URL(UPDATE_URL);

            URLConnection connection = url.openConnection();
            size = connection.getContentLength();

            connection.getInputStream().close();
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            // Restore the original file
            restoreOriginal();
            JOptionPane.showMessageDialog(null, "An error occurred while updating database. Reverted to original database.",
                    "Kamusi Desktop", JOptionPane.ERROR_MESSAGE);
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            // Restore the original file
            restoreOriginal();
            JOptionPane.showMessageDialog(null, "An error occurred while trying to access the update file. Check your connection to the Internet.",
                    "Kamusi Desktop", JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException ex)
        {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            // Restore the original file
            restoreOriginal();
            JOptionPane.showMessageDialog(null, "An error occurred while updating database. Reverted to original database.",
                    "Kamusi Desktop", JOptionPane.ERROR_MESSAGE);
        }

        return size;
    }

    /**
     * Gets the size of the database file
     */
    public long getSizeOfDatabase()
    {
        long size = 0;

        File database = new File(updatedb);
        size = database.length();

        return size;
    }

    class DownloadProgressBar implements Runnable
    {

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
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run()
        {
            showDownloadProgress();
        }
    }
}
