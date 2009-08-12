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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.util.Calendar;
import javax.swing.JOptionPane;

/**
 * class Editor
 */
public class Editor
{

    /**
     * Logging facility
     */
    private LoggingUtil util = new LoggingUtil();
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
            util.log(String.valueOf(ex));
        }
        catch (InstantiationException ex)
        {
            util.log(String.valueOf(ex));
        }
        catch (IllegalAccessException ex)
        {
            util.log(String.valueOf(ex));
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
        util.log("Editing database entry from " + oldWord + " to " + newWord);

        // Get the ID at the specified row and column
        String id = getID(row, fromLanguage, searchKey);

        // We are now interested in the record of this id
        String updateQuery = "UPDATE DICT SET " + formatColumnName(columnName) +
                " = ? WHERE Id = ?";

        String updateLog = formatColumnName(columnName) + "|" + newWord +
                "|" + id;

        String message = "";

        if (newWord.trim().length() == 0)
        {
            message = "This will delete the entry \n" +
                    "\"" + oldWord + "\"\n" +
                    "Are you sure that you want to proceed?";
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
                query = "SELECT * FROM dict WHERE EnglishSortBy = ? " +
                        "ORDER BY EnglishSortBy ASC";
            }
            else if (fromLanguage.equalsIgnoreCase("SWAHILI"))
            {
                query = "SELECT Id FROM dict WHERE SwahiliSortBy = ? " +
                        "ORDER BY SwahiliSortBy ASC";
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
                }
                currentRow++;
            }
            resultSet.close();
            statement.close();
            connection.close();
        }
        catch (SQLException ex)
        {
            util.log(String.valueOf(ex));
            MainWindow.showError(String.valueOf(ex));
        }
        catch (Exception ex)
        {
            util.log(String.valueOf(ex));
            MainWindow.showError(String.valueOf(ex));
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
            return ("EnglishSortBy");
        }
        else if (columnName.equalsIgnoreCase("swahili"))
        {
            return ("SwahiliSortBy");
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
        util.log("Updating log with " + update);
        final String fileName = "log/edit.log";

        try
        {
            //Read the old file
            StringBuffer oldUpdates = new StringBuffer();
            File oldFile = new File(fileName);
            FileReader oldReader = new FileReader(oldFile);
            BufferedReader oldBuffer = new BufferedReader(oldReader);
            String oldLines;
            String oldTimeStamp = null;

            while ((oldLines = oldBuffer.readLine()) != null)
            {
                oldUpdates.append(oldLines + "\n");
            }

            oldReader.close();

            //Append the new data
            StringBuffer newUpdates = new StringBuffer();
            newUpdates.append(update);
            String newData = newUpdates.toString();

            //Write out the new update file
            BufferedWriter writer = new BufferedWriter(new FileWriter(oldFile, true));
            writer.write(newData);
            writer.newLine();
            writer.close();
        }
        catch (FileNotFoundException ex)
        {
            util.log(String.valueOf(ex));
            MainWindow.showError(String.valueOf(ex));
        }
        catch (IOException ex)
        {
            util.log(String.valueOf(ex));
            MainWindow.showError(String.valueOf(ex));
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
            statement.executeUpdate();
            statement.close();
            connection.close();
        }
        catch (SQLException ex)
        {
            util.log(String.valueOf(ex));
            MainWindow.showError(String.valueOf(ex));
        }
        catch (Exception ex)
        {
            util.log(String.valueOf(ex));
            MainWindow.showError(String.valueOf(ex));
        }
    }
}
