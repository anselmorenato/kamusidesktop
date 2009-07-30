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
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

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
    private String fieldsString = "";
    private Vector heading;
    private Vector data;
    private Vector fieldToDisplay;
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
        try
        {
            Class.forName("org.sqlite.JDBC").newInstance();

            StringBuffer fieldStringBuffer = new StringBuffer();

            Enumeration availableFieldsString = fields.elements();

            while (availableFieldsString.hasMoreElements())
            {
                fieldStringBuffer.append((String) availableFieldsString.nextElement());
            }

            fieldsString = fieldStringBuffer.toString();
            connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);

            String query = "";

            heading = new Vector();

            if (fromLanguage.equalsIgnoreCase("ENGLISH"))
            {
                query = "SELECT * FROM dict WHERE EnglishSortBy = ? " +
                        "ORDER BY EnglishSortBy ASC";
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
                query = "SELECT * FROM dict WHERE SwahiliSortBy = ? " +
                        "ORDER BY SwahiliSortBy ASC";
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

//            statement.setString(1, "%" + word + "%");
            statement.setString(1, word);

            resultSet = statement.executeQuery();

            row = 0; // To hold the number of records

            data = new Vector();


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
                fieldToDisplay = new Vector();

                if (fromLanguage.equalsIgnoreCase("ENGLISH"))
                {
                    fieldToDisplay.addElement(englishWord);
                    fieldToDisplay.addElement(swahiliWord);

                    if (fieldsString.contains("English Plural"))
                    {
                        fieldToDisplay.addElement(englishPlural);
                    }
                    if (fieldsString.contains("Swahili Plural"))
                    {
                        fieldToDisplay.addElement(swahiliPlural);
                    }
                    if (fieldsString.contains("English Example"))
                    {
                        fieldToDisplay.addElement(englishExample);
                    }
                    if (fieldsString.contains("Swahili Example"))
                    {
                        fieldToDisplay.addElement(swahiliExample);
                    }
                }
                else if (fromLanguage.equalsIgnoreCase("SWAHILI"))
                {
                    fieldToDisplay.addElement(swahiliWord);
                    fieldToDisplay.addElement(englishWord);

                    if (fieldsString.contains("English Plural"))
                    {
                        fieldToDisplay.addElement(englishPlural);
                    }
                    if (fieldsString.contains("Swahili Plural"))
                    {
                        fieldToDisplay.addElement(swahiliPlural);
                    }
                    if (fieldsString.contains("English Example"))
                    {
                        fieldToDisplay.addElement(englishExample);
                    }
                    if (fieldsString.contains("Swahili Example"))
                    {
                        fieldToDisplay.addElement(swahiliExample);
                    }
                }

                //add to model
                data.addElement(fieldToDisplay);

            }
        }
        catch (SQLException ex)
        {
            if (ex.getMessage().equalsIgnoreCase("no such table: dict"))
            {
                JOptionPane.showMessageDialog(null, "Kamusi Desktop Could not find databse or " +
                        "your database may be corrupted.\nCheck your working directory or\n" +
                        "Click file -> Update in order to fetch a new database.", "Kamusi Desktop",
                        JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Kamusi Desktop",
                        JOptionPane.ERROR_MESSAGE);
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
     * Class Instance for printing
     * @param fromLanguage the language from which we are translating
     * @param word the word to translate
     * @param fields the fields which we are interested in
     */
    public void print(String fromLanguage, String word, Vector fields)
    {
        StringBuffer printables = new StringBuffer();

        try
        {
            Class.forName("org.sqlite.JDBC").newInstance();

            StringBuffer fieldStringBuffer = new StringBuffer();

            Enumeration availableFieldsString = fields.elements();

            while (availableFieldsString.hasMoreElements())
            {
                fieldStringBuffer.append((String) availableFieldsString.nextElement());
            }

            fieldsString = fieldStringBuffer.toString();
            connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);

            String query = "";


            if (fromLanguage.equalsIgnoreCase("ENGLISH"))
            {
                query = "SELECT * FROM dict WHERE EnglishSortBy LIKE ? " +
                        "ORDER BY EnglishSortBy ASC";
                printables.append("English|");
                printables.append("Swahili|");

                Enumeration availableFields = fields.elements();
                while (availableFields.hasMoreElements())
                {
                    printables.append((String) availableFields.nextElement() + "|");
                }
            }
            else if (fromLanguage.equalsIgnoreCase("SWAHILI"))
            {
                query = "SELECT * FROM dict WHERE SwahiliSortBy LIKE ? " +
                        "ORDER BY SwahiliSortBy ASC";
                printables.append("Swahili|");
                printables.append("English|");

                Enumeration availableFields = fields.elements();
                while (availableFields.hasMoreElements())
                {
                    printables.append((String) availableFields.nextElement() + "|");
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


                printables.append("\n");

                if (fromLanguage.equalsIgnoreCase("ENGLISH"))
                {
                    printables.append(englishWord + "|");
                    printables.append(swahiliWord + "|");

                    if (fieldsString.contains("English Plural"))
                    {
                        printables.append(englishPlural + "|");
                    }
                    if (fieldsString.contains("Swahili Plural"))
                    {
                        printables.append(swahiliPlural + "|");
                    }
                    if (fieldsString.contains("English Example"))
                    {
                        printables.append(englishExample + "|");
                    }
                    if (fieldsString.contains("Swahili Example"))
                    {
                        printables.append(swahiliExample + "|");
                    }
                }
                else if (fromLanguage.equalsIgnoreCase("SWAHILI"))
                {
                    printables.append(swahiliWord + "|");
                    printables.append(englishWord + "|");

                    if (fieldsString.contains("English Plural"))
                    {
                        printables.append(englishPlural + "|");
                    }
                    if (fieldsString.contains("Swahili Plural"))
                    {
                        printables.append(swahiliPlural + "|");
                    }
                    if (fieldsString.contains("English Example"))
                    {
                        printables.append(englishExample + "|");
                    }
                    if (fieldsString.contains("Swahili Example"))
                    {
                        printables.append(swahiliExample + "|");
                    }
                }
            }

            JTextPane pane = new JTextPane();
            String text = printables.toString();
            pane.setText(text);
            new PrintMe().print(pane);
            System.gc();
        }
        catch (SQLException ex)
        {
            if (ex.getMessage().equalsIgnoreCase("no such table: dict"))
            {
                JOptionPane.showMessageDialog(null, "Kamusi Desktop Could not find databse or " +
                        "your database may be corrupted.\nCheck your working directory or\n" +
                        "Click file -> Update in order to fetch a new database.", "Kamusi Desktop",
                        JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Kamusi Desktop",
                        JOptionPane.ERROR_MESSAGE);
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
