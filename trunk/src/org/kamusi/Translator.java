/**
 * Translator.java
 * Created on Aug 6, 2009, 3:52:23 PM
 * @author arthur
 */
package org.kamusi;

import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Translator.java
 * Fetches translations from the database and renders them into a JTable
 */
public class Translator extends DefaultTableModel
{

    /**
     * The translation SQLite3 database
     */
    private final String DATABASE = "jdbc:sqlite:" + System.getProperty("database");
    /**
     * Translation database username. Defaults to blank
     */
    private final String USERNAME = System.getProperty("database_username");
    /**
     * Translation database password. Defaults to blank
     */
    private final String PASSWORD = System.getProperty("database_password");
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
    private static String language1 = System.getProperty("language1");
    private static String language2 = System.getProperty("language2");
    private String language1Word, language2Word, language1Example, language2Example, language2Plural,
            language1Plural;
    private Vector<String> headers;
    private Vector<Vector<String>> data;
    private Vector<String> rows;
    private KamusiLogger logger = new KamusiLogger();
    /**
     * Holds the number of records fetched
     */
    private int row = 0;
    /**
     * Loads system properties
     */
    private KamusiProperties props = new KamusiProperties();

    /**
     * Initializes the class
     * @param fromLanguage the language from which we are translating
     * @param word the word to translate
     * @param fields the fields which we are interested in
     */
    public Translator(String fromLanguage, String word, Vector<String> fields)
    {
        boolean wildCardSearch = word.contains("*");

        try
        {
            connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);

            String query = "";

            headers = new Vector<String>();

            if (fromLanguage.equalsIgnoreCase(language1))
            {
                query = getQuery(language1, wildCardSearch);

                headers.addElement(System.getProperty("language1"));
                headers.addElement(System.getProperty("language2"));
            }
            else if (fromLanguage.equalsIgnoreCase(language2))
            {
                query = getQuery(language2, wildCardSearch);

                headers.addElement(System.getProperty("language2"));
                headers.addElement(System.getProperty("language1"));
            }
            else
            {
                throw new Exception("Undefined language!");
            }

            Enumeration<String> availableFields = fields.elements();

            while (availableFields.hasMoreElements())
            {
                String element = availableFields.nextElement();

                headers.addElement(element);
            }

            statement = connection.prepareStatement(query);

            String parameter1 = fromLanguage.trim().equalsIgnoreCase("English") ? "en"
                    : fromLanguage.trim().equalsIgnoreCase("Swahili") ? "sw" : "";

            String parameter2 = fromLanguage.trim().equalsIgnoreCase("English") ? "sw"
                    : fromLanguage.trim().equalsIgnoreCase("Swahili") ? "en" : "";

            String parameter3 = wildCardSearch ? word.replaceAll("\\*", "%") : word;

            statement.setString(1, parameter1);
            statement.setString(2, parameter2);
            statement.setString(3, parameter3);

            resultSet = statement.executeQuery();

            row = 0; // To hold the number of records

            data = new Vector<Vector<String>>();

            while (resultSet.next())
            {
                row++;

                language1Word = resultSet.getString("root").
                        equalsIgnoreCase("null") ? "" : resultSet.getString("root");
                language2Word = resultSet.getString("oroot").
                        equalsIgnoreCase("null") ? "" : resultSet.getString("oroot");
                language1Plural = resultSet.getString("title").
                        equalsIgnoreCase("null") ? "" : resultSet.getString("title");
                language2Plural = resultSet.getString("otitle").
                        equalsIgnoreCase("null") ? "" : resultSet.getString("otitle");
                language1Example = resultSet.getString("defn").
                        equalsIgnoreCase("null") ? "" : resultSet.getString("defn");
                language2Example = resultSet.getString("odefn").
                        equalsIgnoreCase("null") ? "" : resultSet.getString("odefn");

                //create a row
                rows = new Vector<String>();

                if (fromLanguage.equalsIgnoreCase(language1))
                {
                    rows.addElement(language1Word);
                    rows.addElement(language2Word);
                }
                else if (fromLanguage.equalsIgnoreCase(language2))
                {
                    rows.addElement(language2Word);
                    rows.addElement(language1Word);
                }

                Object[] languages = new Object[]
                {
                    language1, language2
                };

                if (fields.contains(MessageLocalizer.formatMessage("language1_plural", languages)))
                {
                    rows.addElement(language1Plural);
                }
                if (fields.contains(MessageLocalizer.formatMessage("language2_plural", languages)))
                {
                    rows.addElement(language2Plural);
                }
                if (fields.contains(MessageLocalizer.formatMessage("language1_example", languages)))
                {
                    rows.addElement(language1Example);
                }
                if (fields.contains(MessageLocalizer.formatMessage("language2_example", languages)))
                {
                    rows.addElement(language2Example);
                }

                //add to model
                data.addElement(rows);
            }
            if (row > 0)
            {
                logger.logApplicationMessage(row + " results matched for \"" + word + "\" from " + fromLanguage);
                table = new JTable(data, headers);
                table.getTableHeader().setDefaultRenderer(new MyHeaderRenderer());
                table.setDefaultRenderer(Object.class, new MyCellRenderer());
                table.setGridColor(new Color(205, 213, 226));
            }
        }
        catch (Exception ex)
        {
            logger.logApplicationMessage(ex.toString());

//                MainWindow.showError(props.getName() + " Could not find database or " +
//                        "your database may be corrupt.\nYou may " +
//                        "select Help -> Restore in order to fetch a new database.");
            MainWindow.showError(ex);
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
                logger.logApplicationMessage(ex.toString());
                MainWindow.showError(ex);
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

    /**
     * The cell renderer.
     */
    private class MyCellRenderer extends DefaultTableCellRenderer
    {

        private Color whiteColor = Color.WHITE;
        private Color alternateColor = new Color(237, 243, 254);
        private Color selectedColor = new Color(153, 153, 204);

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean selected, boolean focused,
                int row, int column)
        {
            super.getTableCellRendererComponent(table, value,
                    selected, focused, row, column);

// Set the background color
            Color bg;
            if (!selected)
            {
                bg = (row % 2 == 0 ? alternateColor : whiteColor);
            }
            else
            {
                bg = selectedColor;
            }
            setBackground(bg);

// Set the foreground to white when selected
            Color fg;
            if (selected)
            {
                fg = Color.white;
            }
            else
            {
                fg = Color.black;
            }
            setForeground(fg);

            return this;
        }
    }

    /**
     * The header renderer. All this does is make the text left aligned.
     */
    public class MyHeaderRenderer extends DefaultTableCellRenderer
    {

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean selected, boolean focused,
                int row, int column)
        {
            super.getTableCellRendererComponent(table, value,
                    selected, focused, row, column);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            return this;
        }
    }

    /**
     * The query used to perform serches in the database
     * @param language The language from which we are translating
     * @param wildcardSearch Is the search a wild card search?
     * @return The appropriate SQL query
     */
    public static String getQuery(String language, boolean wildcardSearch)
    {
        language = language.trim();

        System.out.println("Wildcard: -> " + wildcardSearch);

        String query = "";

        if (language.equalsIgnoreCase(language1))
        {
            query = (wildcardSearch)
                    ? "SELECT * FROM paldo WHERE tpl=? AND otpl=? AND root LIKE ?"
                    : "SELECT * FROM paldo WHERE tpl=? AND otpl=? AND root=?";
        }
        else if (language.equalsIgnoreCase(language2))
        {
            query = (wildcardSearch)
                    ? "SELECT * FROM paldo WHERE otpl=? AND tpl=? AND oroot LIKE ?"
                    : "SELECT * FROM paldo WHERE otpl=? AND tpl=? AND oroot=?";
        }

        return query;
    }
}
