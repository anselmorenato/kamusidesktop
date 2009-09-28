/**
 * Synchronizer.java
 * Created on Aug 6, 2009, 3:52:23 PM
 * @author arthur
 */
package org.kamusi;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
//import java.net.URLEncoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Synchronizer.java
 */
public class Synchronizer extends KamusiLogger
{

    /**
     * Database URL
     */
    private final String DATABASE = "jdbc:sqlite:kamusiproject.db";
    /**
     * Database username
     */
    private final String USERNAME = "";
    /**
     * Database password
     */
    private final String PASSWORD = "";
    /**
     * File that is used to log updates<BR>
     * IMPORTANT: Please DO NOT edit this file by hand
     */
    private final String editLog = "log/edit.log";
    /**
     * Holds the UNIX timestamp of when the file was last updated
     */
    private String lastUpdate = "";
    /**
     * The URL of the updates
     */
    private final String UPDATE_URL = "http://localhost:8084/kamusiproject/";
//    private final String UPDATE_URL = "http://pm.suuch.com:8080/kamusiproject/";
    /**
     * To denote whether we can synchronize
     */
    private boolean canSync = false;

    /**
     * Fetches the database updates
     * @param isEditorsVersion Boolean to check if its editors version
     */
    public void synchronize(boolean isEditorsVersion)
    {

//        TODO: Review this part. It may be failing on isEditorsVersion = false
        if (isEditorsVersion)
        {
            synchronizeDatabases();
        }

        if (updateLocalLog() && updateTimeStamp())
        {
            String message = "Databases synchronized successfully!";
            log(message);
            MainWindow.showInfo(message);
        }
        else
        {
            MainWindow.showError("An error has ocurred." +
                    "\n\nCheck your log files for further details regarding this error.");
        }
    }

    /**
     * Synchronizes the local database with the online one
     * Applies only to editor's version of the software
     */
    private void synchronizeDatabases()
    {
        // COMMIT LOCAL CHANGES TO REMOTE SERVER
        if (copyLocalToRemote())
        {
            // Download remote to local
            if (copyRemoteToLocal())
            {
                canSync = true;
            }
            else
            {
                canSync = false;
            }
        }
        else
        {
            canSync = false;
        }
    }

    /**
     * Gets how much of update we have since the last sync
     * @return The size of the updates
     */
    public long getSizeOfUpdate()
    {
        long sizeOfUpdate = 0;
        try
        {
            URL url = new URL(UPDATE_URL + "/fetchupdate.jsp?update=" + lastUpdate);
            URLConnection connection = url.openConnection();
            sizeOfUpdate = connection.getContentLength();
            connection.getInputStream().close();
            canSync = true;
            return sizeOfUpdate;
        }
        catch (java.net.UnknownHostException ex)
        {
            log(ex.toString());
            canSync = false;
            return sizeOfUpdate;
        }
        catch (MalformedURLException ex)
        {
            log(ex.toString());
            canSync = false;
            return sizeOfUpdate;
        }
        catch (IOException ex)
        {
            log(ex.toString());
            canSync = false;
            return sizeOfUpdate;
        }

    }

    /**
     * Loads the changes from the remote server and writes
     * them to a local database
     */
    private boolean updateLocalLog()
    {
        // NOW WE UPDATE THE CHANGES NOW TO LOCAL
        File file = new File(editLog);
        try
        {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                if (line.startsWith(";"))
                {
                    continue;
                }
                else if (line.trim().length() == 0)
                {
                    continue;
                }
                else if (line.startsWith("#"))
                {
                    continue;
                }
                else
                {
                    HexConverter converter = new HexConverter();
                    String value = converter.getAscii(line);
                    log(value);
                    String[] queryArray = value.split("\\|");
                    updateLocalDatabase(queryArray);
                }
            }
            // Close the reader in preparation for the next line
            fileReader.close();
            return true;
        }
        catch (Exception ex)
        {
            log(ex.toString());
            return false;
        }
    }

    /**
     * Sets the timestamp of the update log file to the current
     * UNIX timestamp. Useful when querying for new database updates
     */
    private boolean updateTimeStamp()
    {
        FileOutputStream fileOutputStream = null;
        try
        {
            File file = new File(editLog);
            fileOutputStream = new FileOutputStream(file);
            long timestamp = java.util.Calendar.getInstance().getTimeInMillis();
            fileOutputStream.write("# DO NOT EDIT THIS FILE BY HAND\n".getBytes());
            fileOutputStream.write((";" + String.valueOf(timestamp) + "\n").getBytes());
            return true;
        }
        catch (FileNotFoundException ex)
        {
            log(ex.toString());
            return false;
        }
        catch (IOException ex)
        {
            log(ex.toString());
            return false;
        }
        finally
        {
            try
            {
                fileOutputStream.close();
            }
            catch (IOException ex)
            {
                log(ex.toString());
            }
        }
    }

    /**
     * Copies remote updates to local server
     */
    private boolean copyRemoteToLocal()
    {
        try
        {
            URL url = new URL(UPDATE_URL + "/fetchupdate.jsp?update=" + lastUpdate);

            log("Fetching: " + url);

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
            return true;
        }
        catch (MalformedURLException ex)
        {
            log(ex.toString());
            return false;
        }
        catch (IOException ex)
        {
            log(ex.toString());
            return false;
        }
    }

    /**
     * Commits the editings to remote server
     */
    private boolean copyLocalToRemote()
    {
        try
        {
            File inputFile = new File(editLog);
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
                    HexConverter converter = new HexConverter();
                    String[] updateLog = converter.getAscii(line).split("\\|");
                    String column = updateLog[0];
                    String update = updateLog[1];
                    String row = updateLog[2];
                    String username = updateLog[3];
                    String oldValue = updateLog[4];

                    String data = URLEncoder.encode("column", "UTF-8") + "=" + URLEncoder.encode(column, "UTF-8");
                    data += "&" + URLEncoder.encode("update", "UTF-8") + "=" + URLEncoder.encode(update, "UTF-8");
                    data += "&" + URLEncoder.encode("row", "UTF-8") + "=" + URLEncoder.encode(row, "UTF-8");
                    data += "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
                    data += "&" + URLEncoder.encode("oldValue", "UTF-8") + "=" + URLEncoder.encode(oldValue, "UTF-8");

                    // Send data
                    URL url = new URL(UPDATE_URL);
                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter streamWriter = new OutputStreamWriter(conn.getOutputStream());

                    log("Posting: " + converter.getHex(data));

                    streamWriter.write(data);
                    streamWriter.flush();

                    // Get the response
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String responseLine;
                    while ((responseLine = bufferedReader.readLine()) != null)
                    {
                        // Process line...
                    }
                    streamWriter.close();
                    bufferedReader.close();

                    continue;
                }
            }
            fileReader.close();
            return true;
        }
        catch (MalformedURLException ex)
        {
            log(ex.toString());
            return false;
        }
        catch (IOException ex)
        {
            log(ex.toString());
            return false;
        }
    }

    /**
     * Initializes the class
     */
    public Synchronizer()
    {
        try
        {
            File inputFile = new File(editLog);
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
                if (line.startsWith("\\#"))
                {
                    continue;
                }
            }
        }
        catch (MalformedURLException ex)
        {
            log(ex.toString());
        }
        catch (IOException ex)
        {
            log(ex.toString());
        }
    }

    /**
     * Updates the local database with changes from the remote
     * @param queryArray
     */
    private void updateLocalDatabase(String[] queryArray)
    {

        String query = ("UPDATE DICT SET " + queryArray[0] +
                " = ? WHERE Id = ?");
        PreparedStatement statement = null;
        Connection connection = null;

        try
        {
            Class.forName("org.sqlite.JDBC").newInstance();
            connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
            statement = connection.prepareStatement(query);
            statement.setString(1, queryArray[1]);
            statement.setString(2, queryArray[2]);
            statement.executeUpdate();
        }
        catch (SQLException ex)
        {
            log(ex.toString());
        }
        catch (Exception ex)
        {
            log(ex.toString());
        }
        finally
        {
            try
            {
                statement.close();
                connection.close();
            }
            catch (Exception ex)
            {
                log(ex.toString());
            }
        }
    }

    /**
     * A test to check whether synchronization can take place.
     * @return boolean to show whether synchronization is possible
     */
    protected boolean canSync()
    {
        return canSync;
    }
}
