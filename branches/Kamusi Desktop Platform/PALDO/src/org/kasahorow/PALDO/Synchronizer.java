/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kasahorow.PALDO;

import credentials.UserManagementPanel;
import drupaljava.DrupalViewNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import org.apache.commons.io.IOUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

public final class Synchronizer extends EditLogger implements Runnable
{

    private String editLog = getEditFileName();
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
            //Fetch from online database all approved updates
            //at or after the last update

            //Get the nodes to update
            String updateURL = "http://173.203.102.17:8080/kpdsync/fetchupdate.jsp?t=" + lastUpdate;
            InputStream in = new URL(updateURL).openStream();
            String nodes = IOUtils.toString(in).trim();
            IOUtils.closeQuietly(in);

            nodes = nodes.replaceAll("\\[", "").replaceAll("\\]", "").trim();

//            JOptionPane.showMessageDialog(null, "Nodes|" + nodes + "|");

            String[] nodeReferences = nodes.split(",");

            for (String nodeRef : nodeReferences)
            {
                //Fetch the node from Drupal
                DrupalViewNode viewNode = new DrupalViewNode();

                //TODO: Put these credentials interface a common conf file
                String nodeContent = viewNode.getNode("http://kamusi.org",
                        "6a52873678803be88b9aa0bf5d0460ff",
                        "http://173.203.102.17/?q=services/xmlrpc",
                        NbPreferences.forModule(UserManagementPanel.class).get("username", ""),
                        NbPreferences.forModule(UserManagementPanel.class).get("password", ""),
                        Integer.parseInt(nodeRef.trim()));

                //Apply this update immediately
                String newContent = NodeParser.getKey("body", nodeContent);

                String columnName = newContent.substring(
                        newContent.indexOf("Editing <b>", newContent.indexOf("words.fienipa.com")) + "Editing <b>".length(),
                        newContent.indexOf("</b>", newContent.indexOf("Editing <b>",
                        newContent.indexOf("words.fienipa.com")))).trim();

                String newWord =
                        newContent.substring(newContent.indexOf("New Word:") + "New Word:".length()).trim();

                String paldoKey =
                        newContent.substring(newContent.indexOf("http://words.fienipa.com/node/")
                        + "http://words.fienipa.com/node/".length(),
                        newContent.indexOf("Editing", newContent.indexOf("http://words.fienipa.com/node/")));

                StatusDisplayer.getDefault().setStatusText("Updating " + paldoKey);

                // Edit local file
                String query = "UPDATE dict SET " + columnName + " = ? WHERE PaldoKey = ?";

                Class.forName("org.sqlite.JDBC").newInstance();
                Connection connection = DriverManager.getConnection(Translator.DATABASE);
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, newWord);
                statement.setString(2, paldoKey);

                statement.executeUpdate();

                connection.close();

                logApplicationMessage(query.replaceFirst("\\?", newWord).replaceFirst("\\?", paldoKey));
            }
        }
        catch (Exception ex)
        {
            StatusDisplayer.getDefault().setStatusText("ERROR: " + ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Copys the editings from remote server
     */
    private void setLastUpdateTimestamp() throws Exception
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
                //TODO: Send updates to PALDO

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
            //TODO: Fix this
            progress.progress("Pushing to PALDO");
            //copyLocalToRemote();

            progress.progress("Fetching timestamps");
            setLastUpdateTimestamp();

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
