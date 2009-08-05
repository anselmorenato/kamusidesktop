/**
 * Editor.java
 * Created on Aug 5, 2009, 12:32:58 PM
 * @author arthur
 */
package org.kamusi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * class Editor
 */
public class Editor
{

    private LoggingUtil util = new LoggingUtil();
    private final String DATABASE = "jdbc:sqlite:kamusiproject.db";
    private final String USERNAME = "";
    private final String PASSWORD = "";

    public void edit(int row, String columnName, String fromLanguage,
            String oldWord, String newWord, String searchKey)
    {
        // Get the ID at the specified row and column
        String id = getID(row, fromLanguage, searchKey);

        // We are now interested in the record of this id
        String updateQuery = "UPDATE DICT SET " + getSetColumnName(columnName) +
                " = ? WHERE Id = ?";

        String updateLog = getSetColumnName(columnName) + "\t" + newWord + "\t" + id;

        try
        {
            Class.forName("org.sqlite.JDBC").newInstance();
            Connection connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
            PreparedStatement statement = connection.prepareStatement(updateQuery);
            statement.setString(1, newWord);
            statement.setString(2, id);
//            statement.executeUpdate();
            statement.close();
            connection.close();
            // Log these changes to file
            logUpdate(updateLog);
        }
        catch (SQLException ex)
        {
            util.log(ex.getMessage());
            MainWindow.showError(ex.getMessage());
        }
        catch (Exception ex)
        {
            util.log(ex.getMessage());
            MainWindow.showError(ex.getMessage());
        }
    }

    private String getID(int row, String fromLanguage, String word)
    {
        String id = "UNDEFINED";
        try
        {
            Class.forName("org.sqlite.JDBC").newInstance();
            Connection connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
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
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, word);
            ResultSet resultSet = statement.executeQuery();
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
            util.log(ex.getMessage());
            MainWindow.showError(ex.getMessage());
        }
        catch (Exception ex)
        {
            util.log(ex.getMessage());
            MainWindow.showError(ex.getMessage());
        }
        return id;
    }

    private String getSetColumnName(String columnName)
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

    public void logUpdate(String update)
    {
        final String fileName = "logs/edit.log";
        FileWriter fstream;
        BufferedWriter out = null;
        try
        {
            fstream = new FileWriter(fileName, true);
            out = new BufferedWriter(fstream);
            out.write(update + "\n");
        }
        catch (IOException ex)
        {
            MainWindow.showError(ex.getMessage());
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (IOException ex)
            {
                MainWindow.showError(ex.getMessage());
            }
        }
    }
}
