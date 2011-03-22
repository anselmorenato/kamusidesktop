/**
 * DictionaryDownloader.java
 * Created on Aug 19, 2010, 10:31:19 AM
 * @author arthur
 */
package org.kamusi.dictionary;

/**
 * class DictionaryDownloader
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Fetches the original database
 */
public class Downloader extends KamusiLogger implements Runnable
{

    /**
     * Holds the size of the current database
     */
    private long sizeOfDatabase = 0;
    /**
     * Holds the size of the update
     */
    private long sizeOfUpdate = 0;
    Translator translator = new Translator();
    /**
     * The name of the app database
     */
    private final File database = translator.getDictionaryFile();
    private String originaldb = translator.getDictionaryPath();
    /**
     * Temporary name to hold the update
     */
    private String updatedb = originaldb + ".backup";
    /**
     * Thread that runs to perform the actual update
     */
    private DownloaderThread restorer;
    public static final String RESTORE_URL = "http://173.203.102.17/TranslationCache/kamusiproject.db";
    private URL url;

    /**
     * Initializes the class
     */
    public Downloader()
    {
    }

    /**
     * Start the downloadDictionary process
     */
    public synchronized void downloadDictionary()
    {
        restorer = new DownloaderThread();
        restorer.start();
    }

    @Override
    public void run()
    {
        downloadDictionary();


//        Runnable fetcher = new Runnable()
//            {
//
//                public void run()
//                {
//                    ProgressHandle progress = ProgressHandleFactory.createHandle("Fetching translation");
//                    progress.start();
//                    progress.switchToIndeterminate();
//                    // do some work
////                    progress.progress("Step 1", 10);
//                    // do some more work
//                    progress.progress(100);
////                progress.finish();
//                }
//            };
//
//            Thread t = new Thread(fetcher);
//            t.start(); // start the task and progress visualization
    }

    boolean isRunning()
    {
        return DownloaderThread.interrupted();
    }

    /**
     * The thread that does the restoration
     */
    class DownloaderThread extends Thread
    {

        int totalInInt = (int) getSizeOfDictionary();

        /**
         * Updates the words database
         */
        private void download()
        {
            ProgressHandle p = ProgressHandleFactory.createHandle("Downloading database");
            p.start();

            try
            {

                p.switchToDeterminate(100);

                String urlString = RESTORE_URL;
                url = new URL(urlString);
                URLConnection connection = url.openConnection();

                BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());

                FileOutputStream ftpFileOutputStream = new FileOutputStream(updatedb);

                int i = 0;

                byte[] bytesIn = new byte[1024];

                while ((i = inputStream.read(bytesIn)) >= 0)
                {
                    ftpFileOutputStream.write(bytesIn, 0, i);

                    //updateProgress();

                    int percentage = (int) Math.ceil((getSizeOfDownloadedDictionary() * 100) / totalInInt);

                    String message = ("Downloaded " + getSizeOfDownloadedDictionary()
                            + " of " + sizeOfUpdate + " (" + percentage + "%)");

                    logApplicationMessage(message);

                    if (percentage == 100)
                    {
                        interrupt();
                    }

                    p.progress("Downloading database", percentage);
                }

                ftpFileOutputStream.close();

                inputStream.close();

                //Flush the existing database
                cleanUp();

            }
            catch (Exception ex)
            {
                Exceptions.printStackTrace(ex);
            }
            finally
            {
                p.finish();
            }
        }

        /**
         * Run the thread
         */
        @Override
        public void run()
        {
            //yield();
            download();
        }

        private void updateProgress()
        {

            int totalInInt = (int) getSizeOfDictionary();

            int percentage = (int) Math.ceil((getSizeOfDownloadedDictionary() * 100) / totalInInt);

            String message = ("Downloaded " + getSizeOfDownloadedDictionary()
                    + " of " + sizeOfUpdate + " (" + percentage + "%)");

            logApplicationMessage(message);

            if (percentage == 100)
            {
                interrupt();
            }
        }
    }

    /**
     * Gets the size of the new downloadDictionary
     * @return The size of downloadDictionary to be downloaded
     * @throws IOException
     */
    public long getSizeOfDictionary()
    {
        try
        {
            url = new URL(RESTORE_URL);
            URLConnection connection = url.openConnection();
            sizeOfUpdate = connection.getContentLength();
            connection.getInputStream().close();
            return sizeOfUpdate;
        }
        catch (Exception ex)
        {
            return 1; //Unity
        }
    }

    /**
     * Gets the size of the database file
     * @return The size of the database
     */
    public long getSizeOfDownloadedDictionary()
    {
        File database = new File(updatedb);
        sizeOfDatabase = database.length();
        return sizeOfDatabase;
    }

    /**
     * Cleans up after an downloadDictionary
     */
    private void cleanUp() throws
            IOException, MalformedURLException
    {
//        Rename the temp db appropriately
        File originalFile = new File(originaldb);
        originalFile.renameTo(new File(originaldb + ".old"));

        File updateFile = new File(updatedb);
        updateFile.renameTo(new File(originaldb));
        logApplicationMessage("Cleanup successful");

        JOptionPane.showMessageDialog(null, "Download Completed successfully.\n"
                + "Please restart "
                + NbBundle.getMessage(SearchTopComponent.class, "CTL_SearchTopComponent"),
                NbBundle.getMessage(SearchTopComponent.class, "CTL_SearchTopComponent"),
                JOptionPane.INFORMATION_MESSAGE);
    }
}
