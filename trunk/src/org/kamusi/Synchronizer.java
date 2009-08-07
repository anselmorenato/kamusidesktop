/**
 * Synchronizer.java
 * Created on Aug 6, 2009, 3:52:23 PM
 * @author arthur
 */
package org.kamusi;

import java.io.BufferedInputStream;
import java.sql.PreparedStatement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author arthur
 */
public class Synchronizer
{

    private final String DATABASE = "jdbc:sqlite:kamusiproject.db";
    private final String USERNAME = "";
    private final String PASSWORD = "";
    private final String editLog = "log/edit.log";
    private final String UPDATE_URL = "http://pm.suuch.com:8080/kamusiproject/";

    /**
     * @param isEditorsVersion Boolean to check if its editors version
     */
    public void fetchUpdate(boolean isEditorsVersion)
    {

        if (isEditorsVersion)
        {
            // SYNC LOCAL AND REMOTE DBs
            synchronizeDatabases();
        }

        // UPDATE THE CHANGES NOW TO LOCAL

        File oldFile = new File(editLog);

        try
        {
            FileReader oldReader = new FileReader(oldFile);
            BufferedReader oldBuffer = new BufferedReader(oldReader);
            String oldLines;

            while ((oldLines = oldBuffer.readLine()) != null)
            {
                System.out.println(oldLines);

                String[] queryArray = oldLines.split("\\|");

                String query = ("UPDATE DICT SET " + queryArray[0] +
                        "=? WHERE Id=?");
                try
                {
                    Class.forName("org.sqlite.JDBC").newInstance();
                    Connection connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, queryArray[1]);
                    statement.setString(2, queryArray[2]);
                    statement.executeUpdate();
                    statement.close();
                    connection.close();
                }
                catch (SQLException ex)
                {
                    ex.printStackTrace();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            oldReader.close();

            FileOutputStream clearLocalLog = new FileOutputStream(oldFile);
            clearLocalLog.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    /**
     * Synchronizes the local database with the online one
     * Applies only to editor's version of the software
     */
    private void synchronizeDatabases()
    {
        // COMMIT LOCAL CHANGES TO REMOTE SERVER

        // Read the log file. Then for each entry, put it to the URL
        final String editLogs = "log/edit.log";

        try
        {

            File inputFile = new File(editLogs);
            FileReader fileReader = new FileReader(inputFile);
            BufferedReader buff = new BufferedReader(fileReader);

            String line; // <<-- added
            while (((line = buff.readLine()) != null)) // <<-- modified
            {

                if (line.startsWith(";"))
                {
                    continue;
                }

                // Call the URL
                URL synchronize = new URL(UPDATE_URL + "?update=" +
                        line.replaceAll(" ", "_"));
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                        synchronize.openStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                    System.out.println(inputLine);
                }
                in.close();
            }
            fileReader.close();

            // Clear the sync logs and put a timestamp to the file
            FileOutputStream fos = new FileOutputStream(inputFile);
            fos.close();

            // DOWNLOAD ALL THE CHANGES THAT REMOTE HAS
            // AND COPY THEM TO THE LOCAL SYSTEM

            URL url = new URL(UPDATE_URL + "/edit.log");
            URLConnection connection = url.openConnection();


            BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
            FileOutputStream ftpFileOutputStream = new FileOutputStream("log/edit.log");

            int i = 0;
            byte[] bytesIn = new byte[1024];

            while ((i = inputStream.read(bytesIn)) >= 0)
            {
                ftpFileOutputStream.write(bytesIn, 0, i);
            }

            ftpFileOutputStream.close();
            inputStream.close();


        }
        catch (MalformedURLException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public long getSizeOfUpdate()
    {
        long sizeOfUpdate = 0;

        try
        {
            URL url = new URL(UPDATE_URL + "/edit.log");

            URLConnection connection = url.openConnection();
            sizeOfUpdate = connection.getContentLength();

            connection.getInputStream().close();
        }
        catch (java.net.UnknownHostException ex)
        {
            ex.printStackTrace();
        }
        catch (MalformedURLException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        return sizeOfUpdate;
    }
}
