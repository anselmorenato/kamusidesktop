/**
 * Editor.java
 * Created on Aug 5, 2009, 12:32:58 PM
 * @author arthur
 */
package org.kamusi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * class Editor
 */
public class Editor extends KamusiLogger
{

    /**
     * Logging facility
     */
    private KamusiLogger logger = new KamusiLogger();
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
     * The database connection
     */
    private Connection connection;
    /**
     * PreparedStatement object for the connections
     */
    private PreparedStatement statement;
    /**
     * ResultSet from the queries
     */
    private ResultSet resultSet;
    /**
     * To identify the editor
     */
    private final String usernameFile = "./log/username.txt";

    /**
     * Constructor
     */
    public Editor()
    {
        try
        {
            Class.forName("org.sqlite.JDBC").newInstance();
        }
        catch (ClassNotFoundException ex)
        {
            log(ex.toString());
        }
        catch (InstantiationException ex)
        {
            log(ex.toString());
        }
        catch (IllegalAccessException ex)
        {
            log(ex.toString());
        }
    }

    /**
     * Edits a given entry in the database
     * @param row The row that the entry is in in the database
     * @param columnName The name of the column that the entry is in
     * @param fromLanguage Language we are translating from
     * @param oldWord The old word that we need to edit from
     * @param newWord The new word to which we are setting
     * @param searchKey The search we made in order to find the word we want to edit
     */
    public void edit(int row, String columnName, String fromLanguage,
            String oldWord, String newWord, String searchKey)
    {
        log("Editing database entry from " + oldWord + " to " + newWord);

        // Get the ID at the specified row and column
        String id = getID(row, fromLanguage, searchKey);

        // We are now interested in the record of this id
        String updateQuery = "UPDATE DICT SET " + formatColumnName(columnName) +
                " = ? WHERE Id = ?";

        String updateLog = formatColumnName(columnName) + "|" + newWord +
                "|" + id + "|" + getUsername() + "|" + oldWord;

        String message = "";

        if (newWord.trim().length() == 0)
        {
            message = "This will delete the entry \n" +
                    "\"" + oldWord + "\"\n" +
                    "Are you sure that you want to proceed?";
        }
        else if (newWord.equals(oldWord))
        {
            log("No changes applied.");
            return;
        }
        else
        {
            message = "This will modify the entry from \n" +
                    "\"" + oldWord + "\" to \"" + newWord + "\"\n" +
                    "Are you sure that you want to proceed?";
        }

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
                options[1]); //default button title

        switch (choice)
        {
            case 0: //YES
                updateDatabase(updateQuery, id, newWord);
                // Log these changes to file
                logUpdate(updateLog);
                break;

            case 1: //NO
                break;

            case -1: //Closed Window
                break;

            default:
                break;
        }
    }

    /**
     * Gets the Primary Key ID for the word we are editing. Used to "lock on"
     * to the word
     * @param row The row in the JTable that the entry came in
     * @param fromLanguage The language we are translating from
     * @param word The search query we used
     * @return The ID of the row we are editing
     */
    private String getID(int row, String fromLanguage, String searchKey)
    {
        String id = "UNDEFINED";
        try
        {
            String query = "";

            if (fromLanguage.equalsIgnoreCase("ENGLISH"))
            {
                query = Translator.getQuery("english");
            }
            else if (fromLanguage.equalsIgnoreCase("SWAHILI"))
            {
                query = Translator.getQuery("swahili");
            }
            
            connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
            statement = connection.prepareStatement(query);
            statement.setString(1, searchKey);
            resultSet = statement.executeQuery();

            int currentRow = 0; // To hold the number of records

            while (resultSet.next())
            {
                if (currentRow == row)
                {
                    id = resultSet.getString("Id");
                    log("Editing ID: " + id);
                    break;
                }
                else
                {
                    currentRow++;
                }
            }
            resultSet.close();
            statement.close();
            connection.close();
        }
        catch (SQLException ex)
        {
            log(ex.toString());
            MainWindow.showError(ex.toString());
        }
        catch (Exception ex)
        {
            log(ex.toString());
            MainWindow.showError(ex.toString());
        }
        return id;
    }

    /**
     * Formats the human readable column names to match with what we have
     * in the database
     * @param columnName The human readable column name
     * @return The correct value of the column name as is in the database
     */
    private String formatColumnName(String columnName)
    {
        if (columnName.equalsIgnoreCase("english"))
        {
            return ("EnglishWord");
        }
        else if (columnName.equalsIgnoreCase("swahili"))
        {
            return ("SwahiliWord");
        }
        else if (columnName.equalsIgnoreCase("english plural"))
        {
            return ("EnglishPlural");
        }
        else if (columnName.equalsIgnoreCase("swahili plural"))
        {
            return ("SwahiliPlural");
        }
        else if (columnName.equalsIgnoreCase("english example"))
        {
            return ("EnglishExample");
        }
        else if (columnName.equalsIgnoreCase("swahili example"))
        {
            return ("SwahiliExample");
        }
        else
        {
            return null;
        }
    }

    /**
     * Logs the update we are doing into the file.
     * This update log is what is used in synchronizing with the online server.
     * Its therefore imperative that the log is correctly stored
     * @param update The update query that has been done
     */
    public void logUpdate(String update)
    {
        final String editLog = "log/edit.log";

        try
        {
            //Read the old file
            StringBuffer oldUpdates = new StringBuffer();
            File editFile = new File(editLog);

            //Append the new data

            //Write out the new update file
            BufferedWriter writer = new BufferedWriter(new FileWriter(editFile, true));

            HexConverter converter = new HexConverter();
            writer.write(converter.getHex(update));
            writer.newLine();
            writer.close();

            log("Updated edit log");
        }
        catch (FileNotFoundException ex)
        {
            log("Failed to update edit log: " + ex.toString());
            MainWindow.showError(ex.toString());
        }
        catch (IOException ex)
        {
            log("Failed to update edit log: " + ex.toString());
            MainWindow.showError(ex.toString());
        }
    }

    /**
     * Does the actual update of the database
     * @param updateQuery The query specified
     * @param id The id of the row
     * @param newWord The new word to have
     */
    private void updateDatabase(String updateQuery, String id, String newWord)
    {
        try
        {
            connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
            statement = connection.prepareStatement(updateQuery);
            statement.setString(1, newWord);
            statement.setString(2, id);
//            statement.executeUpdate();
            statement.close();
            connection.close();
        }
        catch (SQLException ex)
        {
            log(ex.toString());
            MainWindow.showError(ex.toString());
        }
        catch (Exception ex)
        {
            log(ex.toString());
            MainWindow.showError(ex.toString());
        }
    }

    /**
     * Deletes a given entry in the database
     *
     * @param row The row that the entry is in in the database
     * @param fromLanguage Language we are translating from
     * @param oldWord The word that we want to delete
     * @param searchKey The search we made in order to find the word we want to edit
     */
    public void deleteEntry(int row, String fromLanguage, String oldWord, String searchKey)
    {
        String exception =
                new UnsupportedOperationException("Deleting entries is not yet implemented").toString();
        MainWindow.showWarning(exception);
    }

//    TODO: Add authentication mechanism
    /**
     * Gets the username for kamusi edits
     * @return The username
     */
    public String getUsername()
    {
        String username = "";

        try
        {

            File inputFile = new File(usernameFile);
            FileReader fileReader = new FileReader(inputFile);
            BufferedReader buff = new BufferedReader(fileReader);

            String line; // <<-- added

            while (((line = buff.readLine()) != null)) // <<-- modified
            {
                if (line.startsWith(";") || line.startsWith("#"))
                {
                    continue;
                }
                else
                {
                    username = line;
                }
            }
        }
        catch (Exception ex)
        {
            log(ex.toString());
        }

        return new HexConverter().getAscii(username);
    }

    /**
     * Writes the username of the user to file
     * @param newUsername
     * @return True if writing to the file was successful, false otherwise
     */
    boolean setUsername(String newUsername)
    {
        //throw new UnsupportedOperationException("Not yet implemented");

        FileWriter fstream;
        BufferedWriter writer = null;
        try
        {
            fstream = new FileWriter(usernameFile, false);
            writer = new BufferedWriter(fstream);
            HexConverter converter = new HexConverter();
            writer.write("# DO NOT EDIT THIS FILE BY HAND");
            writer.newLine();
            writer.write(converter.getHex(newUsername));
            return true;
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
                writer.close();
            }
            catch (IOException ex)
            {
                log(ex.toString());
                return false;
            }
        }
    }
}
