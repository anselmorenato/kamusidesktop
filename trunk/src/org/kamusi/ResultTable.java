package org.kamusi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class ResultTable extends DefaultTableModel
{
    
    /**
     * The translation SQLite3 database
     */
    private String database = "jdbc:sqlite:kamusiproject.db";
    /**
     * Translation database username. Defaults to blank
     */
    private String username = "";
    /**
     * Translation database password. Defaults to blank
     */
    private String password = "";
    /**
     * Table to display the output
     */
    private JTable table;
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
    public ResultTable(String fromLanguage, String word, Vector fields)
    {
        Vector heading = new Vector();
        Vector data = new Vector();

        String fieldsString = "";
        Enumeration availableFieldsString = fields.elements();
        while (availableFieldsString.hasMoreElements())
        {
            fieldsString += ((String) availableFieldsString.nextElement());
        }

        try
        {
            Class.forName("org.sqlite.JDBC").newInstance();
            connection = DriverManager.getConnection(database, username, password);

            String query = "";

            if (fromLanguage.equalsIgnoreCase("ENGLISH"))
            {
                query = "select * from dict where EnglishSortBy like ? " +
                        "Order by EnglishSortBy asc";
                heading.addElement("English");
                heading.addElement("Swahili");

                Enumeration availableFields = fields.elements();
                while (availableFields.hasMoreElements())
                {
                    heading.addElement((String) availableFields.nextElement());
                }
            }
            else if (fromLanguage.equalsIgnoreCase("SWAHILI"))
            {
                query = "select * from dict where SwahiliSortBy like ? " +
                        "Order by SwahiliSortBy asc";
                heading.addElement("Swahili");
                heading.addElement("English");

                Enumeration availableFields = fields.elements();
                while (availableFields.hasMoreElements())
                {
                    heading.addElement((String) availableFields.nextElement());
                }
            }
            else
            {
                throw new Exception("Undefined language!");
            }

            statement = connection.prepareStatement(query);

            statement.setString(1, "%" + word + "%");

            resultSet = statement.executeQuery();

            row = 0; // To hold the number of records

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
                Vector fieldToDisplay = new Vector();

                if (fromLanguage.equalsIgnoreCase("ENGLISH"))
                {
                    fieldToDisplay.addElement(englishWord);
                    fieldToDisplay.addElement(swahiliWord);

                    if (fieldsString.contains("EnglishPlural"))
                    {
                        fieldToDisplay.addElement(englishPlural);
                    }
                    if (fieldsString.contains("SwahiliPlural"))
                    {
                        fieldToDisplay.addElement(swahiliPlural);
                    }
                    if (fieldsString.contains("EnglishExample"))
                    {
                        fieldToDisplay.addElement(englishExample);
                    }
                    if (fieldsString.contains("SwahiliExample"))
                    {
                        fieldToDisplay.addElement(swahiliExample);
                    }
                }
                else if (fromLanguage.equalsIgnoreCase("SWAHILI"))
                {
                    fieldToDisplay.addElement(swahiliWord);
                    fieldToDisplay.addElement(englishWord);

                    if (fieldsString.contains("EnglishPlural"))
                    {
                        fieldToDisplay.addElement(englishPlural);
                    }
                    if (fieldsString.contains("SwahiliPlural"))
                    {
                        fieldToDisplay.addElement(swahiliPlural);
                    }
                    if (fieldsString.contains("EnglishExample"))
                    {
                        fieldToDisplay.addElement(englishExample);
                    }
                    if (fieldsString.contains("SwahiliExample"))
                    {
                        fieldToDisplay.addElement(swahiliExample);
                    }
                }

                //add to model
                data.addElement(fieldToDisplay);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Logger.getLogger(ResultTable.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Kamusi Desktop",
                JOptionPane.ERROR_MESSAGE);
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
                Logger.getLogger(ResultTable.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Kamusi Desktop",
                JOptionPane.ERROR_MESSAGE);
            }
        }

        this.table = new JTable(data, heading);

        TableModel model = this.table.getModel();
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
     * Returns a table model
     * @return the table model
     */
    public TableModel getTableModel()
    {
        TableModel model = this.table.getModel();

        return model;
    }

    /**
     * Gets how many records have been fetched
     * @return the number of records
     */
    public int getResultCount()
    {
        return this.row;
    }
}
