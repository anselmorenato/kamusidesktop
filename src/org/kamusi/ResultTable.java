/**
 * ResultTable.java
 * Created on Aug 6, 2009, 3:52:23 PM
 * @author arthur
 */
package org.kamusi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * ResultTable.java
 * Fetches translations from the database and renders them into a JTable
 */
public class ResultTable extends DefaultTableModel
{

    /**
     * The translation SQLite3 database
     */
    private final String DATABASE = "jdbc:sqlite:kamusiproject.db";
    /**
     * Translation database username. Defaults to blank
     */
    private final String USERNAME = "";
    /**
     * Translation database password. Defaults to blank
     */
    private final String PASSWORD = "";
    /**
     * Table to display the output
     */
    private JTable table = new JTable();
    /**
     * Connection to the database
     */
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    /**
     * The fields available to be fetched
     */
    private String englishWord, swahiliWord, englishExample, swahiliExample, swahiliPlural,
            englishPlural;
    private String fieldsString = "";
    private Vector<String> headers;
    private Vector<Vector> data;
    private Vector<String> rows;
    private LoggingUtil util = new LoggingUtil();
    /**
     * Holds the number of records fetched
     */
    private int row = 0;

    /**
     * Initializes the class
     * @param fromLanguage the language from which we are translating
     * @param word the word to translate
     * @param fields the fields which we are interested in
     */
    public ResultTable(String fromLanguage, String word, Vector <String> fields)
    {

        try
        {
            Class.forName("org.sqlite.JDBC").newInstance();

            StringBuffer fieldStringBuffer = new StringBuffer();

            Enumeration <String> availableFieldsString = fields.elements();

            while (availableFieldsString.hasMoreElements())
            {
                fieldStringBuffer.append(availableFieldsString.nextElement());
            }

            fieldsString = fieldStringBuffer.toString();
            connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);

            String query = "";

            headers = new Vector<String>();

            if (fromLanguage.equalsIgnoreCase("ENGLISH"))
            {
                query = "SELECT * FROM dict WHERE EnglishSortBy = ? " +
                        "ORDER BY EnglishSortBy ASC";
                headers.addElement("English");
                headers.addElement("Swahili");

                Enumeration <String> availableFields = fields.elements();

                while (availableFields.hasMoreElements())
                {
                    headers.addElement((String) availableFields.nextElement());
                }
            }
            else if (fromLanguage.equalsIgnoreCase("SWAHILI"))
            {
                query = "SELECT * FROM dict WHERE SwahiliSortBy = ? " +
                        "ORDER BY SwahiliSortBy ASC";
                headers.addElement("Swahili");
                headers.addElement("English");

                Enumeration <String> availableFields = fields.elements();

                while (availableFields.hasMoreElements())
                {
                    headers.addElement((String) availableFields.nextElement());
                }
            }
            else
            {
                throw new Exception("Undefined language!");
            }

            statement = connection.prepareStatement(query);

//            statement.setString(1, "%" + word + "%");
            statement.setString(1, word);

            resultSet = statement.executeQuery();

            row = 0; // To hold the number of records

            data = new Vector<Vector>();

            while (resultSet.next())
            {
                row++;

                englishWord = resultSet.getString("EnglishSortBy");
                swahiliWord = resultSet.getString("SwahiliSortBy");
                englishPlural = resultSet.getString("EnglishPlural");
                swahiliPlural = resultSet.getString("SwahiliPlural");
                englishExample = resultSet.getString("EnglishExample");
                swahiliExample = resultSet.getString("SwahiliExample");

                //create a row
                rows = new Vector<String>();

                if (fromLanguage.equalsIgnoreCase("ENGLISH"))
                {
                    rows.addElement(englishWord);
                    rows.addElement(swahiliWord);

                    if (fieldsString.contains("English Plural"))
                    {
                        rows.addElement(englishPlural);
                    }
                    if (fieldsString.contains("Swahili Plural"))
                    {
                        rows.addElement(swahiliPlural);
                    }
                    if (fieldsString.contains("English Example"))
                    {
                        rows.addElement(englishExample);
                    }
                    if (fieldsString.contains("Swahili Example"))
                    {
                        rows.addElement(swahiliExample);
                    }
                }
                else if (fromLanguage.equalsIgnoreCase("SWAHILI"))
                {
                    rows.addElement(swahiliWord);
                    rows.addElement(englishWord);

                    if (fieldsString.contains("English Plural"))
                    {
                        rows.addElement(englishPlural);
                    }
                    if (fieldsString.contains("Swahili Plural"))
                    {
                        rows.addElement(swahiliPlural);
                    }
                    if (fieldsString.contains("English Example"))
                    {
                        rows.addElement(englishExample);
                    }
                    if (fieldsString.contains("Swahili Example"))
                    {
                        rows.addElement(swahiliExample);
                    }
                }

                //add to model
                data.addElement(rows);
            }
            if (row > 0)
            {
                util.log(row + " results matched for \"" + word + "\" from " + fromLanguage);
                table.setModel(new JTable(data, headers).getModel());
            }
        }
        catch (SQLException ex)
        {
            util.log(String.valueOf(ex));

            if (ex.getMessage().equalsIgnoreCase("no such table: dict"))
            {
                MainWindow.showError("Kamusi Desktop Could not find database or " +
                        "your database may be corrupted.\nCheck your working directory or\n" +
                        "select file -> Update in order to fetch a new database.");
            }
            else
            {
                MainWindow.showError(String.valueOf(ex));
            }
        }
        catch (Exception ex)
        {
            util.log(String.valueOf(ex));
            MainWindow.showError(String.valueOf(ex));
        }
        finally
        {
            try
            {
                resultSet.close();
                statement.close();
                connection.close();
            }
            catch (SQLException ex)
            {
                util.log(String.valueOf(ex));
                MainWindow.showError(String.valueOf(ex));
            }
        }
    }

    /**
     * Returns a JTable created from #getTableModel
     * @return the JTable Object
     */
    public JTable getTable()
    {
        return this.table;
    }

    /**
     * Gets how many records have been fetched
     * @return the number of records
     */
    public int getResultCount()
    {
        return row;
    }
}