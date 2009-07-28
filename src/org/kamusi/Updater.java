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
    private final String backupdb = "kamusiproject.db_bakup";
    private long totalSizeOfUpdate = 0;
    private DownloadProgressBar progress;
    private UpdaterThread update;

    ;
    /**
     * The update URL
     */
    public static final String UPDATE_URL =
//            "http://localhost/kamusidesktop/kamusiproject.db";
            "http://pm.suuch.com:8080/kamusiproject/kamusiproject.db";
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

                //Create a backup copy
                File original = new File(originaldb);
                original.renameTo(new File(backupdb));

                url = new URL(UPDATE_URL);
                URLConnection connection = url.openConnection();
                BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                FileOutputStream ftpFileOutputStream = new FileOutputStream(originaldb);

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

            //Delete the backup
            File copy = new File(backupdb);
            copy.delete();
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
        File copy = new File(backupdb);
        copy.renameTo(new File(originaldb));
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

        File database = new File(originaldb);
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
