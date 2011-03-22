/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Kinyarwanda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;

public final class Synchronizer extends EditLogger implements Runnable
{

    private Editor editor;
    private String editLog = getLogFileName();
    private String lastUpdate = "0";
    File inputFile = new File(editLog);

    public Synchronizer()
    {
        if (!inputFile.exists())
        {
            updateTimeStamp();
        }
    }

    private void updateLocalDB()
    {
        try
        {
            FileReader fileReader = new FileReader(inputFile);
            BufferedReader buff = new BufferedReader(fileReader);

            String line; // <<-- added

            while (((line = buff.readLine()) != null)) // <<-- modified
            {
                StatusDisplayer.getDefault().setStatusText("Updating " + Translator.DATABASE);

                line = line.substring(line.indexOf("|") + 1).trim();

                String[] updateExploded = line.split("@");

                String newWord = "";
                String paldoKey = "";
                String columnName = "";

                newWord = updateExploded[1];
                paldoKey = updateExploded[3];
                columnName = updateExploded[4];

                // Edit local file
                String query = "UPDATE DICT SET " + columnName + " = ? WHERE PaldoKey = ?";

                Class.forName("org.sqlite.JDBC").newInstance();

                Connection connection = DriverManager.getConnection(Translator.DATABASE);

                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, newWord);
                statement.setString(2, paldoKey);



                //TODO: Enable this flag
                //statement.executeUpdate();

                connection.close();

                continue;
            }

            fileReader.close();

        }
        catch (java.lang.ArrayIndexOutOfBoundsException ex)
        {
            StatusDisplayer.getDefault().setStatusText("Skipped");
        }
        catch (Exception ex)
        {
            StatusDisplayer.getDefault().setStatusText("Skipped");
        }
    }

    /**
     * Commits the editings to remote server
     */
    private void copyRemoteToLocal() throws Exception
    {
        FileReader fileReader = new FileReader(inputFile);
        BufferedReader buff = new BufferedReader(fileReader);

        String line; // <<-- added

        readFile:
        while (((line = buff.readLine()) != null)) // <<-- modified
        {
            if (line.startsWith(";"))
            {
                lastUpdate = line.replace(";", "").trim();
                break readFile;
            }
        }

        fileReader.close();

        //Fetch the updates
        StringBuilder requestURL = new StringBuilder();

        requestURL.append(SYNC_URL).
                append("?fetchupdate=").append(URLEncoder.encode(lastUpdate, "UTF-8"));

        URL url = new URL(requestURL.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; JVM)");
        conn.setRequestProperty("Pragma", "no-cache");
        conn.setDoOutput(true);
        conn.connect();

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        while ((line = rd.readLine()) != null)
        {
            if (line.trim().length() != 0)
            {
                logApplicationMessage(line);
                StatusDisplayer.getDefault().setStatusText(line);
            }

        }
    }

    /**
     * Commits the editings to remote server
     */
    private void copyLocalToRemote() throws Exception
    {
        FileReader fileReader = new FileReader(inputFile);
        BufferedReader buff = new BufferedReader(fileReader);

        String line; // <<-- added

        while (((line = buff.readLine()) != null)) // <<-- modified
        {
            if (line.startsWith(";"))
            {
                lastUpdate = line.replace(";", "").trim();

                continue;
            }
            if (line.startsWith("#"))
            {
                continue;
            }
            else
            {
                // Call the URL
                //Try sending it to PALDO

                line = line.substring(line.indexOf("|") + 1).trim();

                StringBuilder requestURL = new StringBuilder();

                requestURL.append(SYNC_URL).
                        append("?update=").append(URLEncoder.encode(line, "UTF-8"));

                URL url = new URL(requestURL.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; JVM)");
                conn.setRequestProperty("Pragma", "no-cache");
                conn.setDoOutput(true);
                conn.connect();

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StatusDisplayer.getDefault().setStatusText(line);

                continue;
            }
        }

        fileReader.close();
    }

    /**
     * Sets the timestamp of the update logApplicationMessage file to the current
     * UNIX timestamp. Useful when querying for new database updates
     */
    public void updateTimeStamp()
    {
        FileOutputStream fileOutputStream = null;

        try
        {
            File file = new File(editLog);
            fileOutputStream = new FileOutputStream(file);

            long timestamp = java.util.Calendar.getInstance().getTimeInMillis();
            fileOutputStream.write("# DO NOT EDIT THIS FILE BY HAND\n".getBytes());
            fileOutputStream.write((";" + String.valueOf(timestamp) + "\n").getBytes());
        }
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void run()
    {
        ProgressHandle progress = ProgressHandleFactory.createHandle(
                "Synchronizing databases...");

        progress.start();
        progress.switchToIndeterminate();

        //Read through the edit file
        //Get the timestamp if it exists)
        try
        {
            progress.progress("Pushing to PALDO");
            copyLocalToRemote();

            progress.progress("Fetching from PALDO");
            copyRemoteToLocal();

            progress.progress("Synching local from PALDO");
            updateLocalDB();

            progress.progress("Updating timestamps");
            updateTimeStamp();
        }
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }
        finally
        {
            progress.finish();
        }
    }
}
