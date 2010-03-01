/**
 * Translator.java
 * Created on Aug 6, 2009, 3:52:23 PM
 * @author arthur
 */
package org.kamusi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
    private String language1 = System.getProperty("language1");
    private String language2 = System.getProperty("language2");
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
                headers.addElement(availableFields.nextElement()
                        .replaceFirst("language1", System.getProperty("language1"))
                        .replaceFirst("language2", System.getProperty("language2")));
            }

            statement = connection.prepareStatement(query);

            String parameter = wildCardSearch ? word.replaceAll("\\*", "%") : word;

            statement.setString(1, parameter);
            statement.setString(2, parameter);
            statement.setString(3, parameter);
            statement.setString(4, parameter);

            resultSet = statement.executeQuery();

            row = 0; // To hold the number of records

            data = new Vector<Vector<String>>();

            while (resultSet.next())
            {
                row++;

                language1Word = resultSet.getString(language1 + "Word");
                language2Word = resultSet.getString(language2 + "Word");
                language1Plural = resultSet.getString(language1 + "Plural");
                language2Plural = resultSet.getString(language2 + "Plural");
                language1Example = resultSet.getString(language1 + "Example");
                language2Example = resultSet.getString(language2 + "Example");

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
                if (fields.contains(language1 + " Plural"))
                {
                    rows.addElement(language1Plural);
                }
                if (fields.contains(language2 + " Plural"))
                {
                    rows.addElement(language2Plural);
                }
                if (fields.contains(language1 + " Example"))
                {
                    rows.addElement(language1Example);
                }
                if (fields.contains(language2 + " Example"))
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

//                table.print();

//                for (int i = 0; i < table.getRowCount(); i++)
//                {
//
//                    int rowToSelect = i;
//
//                    if ((rowToSelect % 2) == 0)
//                    {
//                        table.setRowSelectionInterval(rowToSelect, rowToSelect);
//                    }
//                }
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
//        return MessageLocalizer.getQuery(language);

        language = language.trim();

        String order = language.equals("Swahili")?"English":
            language.equals("English")?"Swahili":"";

        System.out.println("Order -> " + order);

        String query = (wildcardSearch) ?

            "SELECT DISTINCT di.*, wg.GroupNum FROM dict AS di " +
            "LEFT JOIN word_grouping AS wg ON ( di.Id = wg.WordId ) " +
            "WHERE" + language + "Word like ? " +
            "OR " + language + "Plural like ? " +
            "OR" + language.substring(0, 3) + "Alt like ? " +
            "OR" + language.substring(0, 3) + "PluralAlt like ? " +
            "Group By di.Id " +
            "ORDER BY wg.GroupNum ASC, wg.InGroupPos ASC, " +
            "LOWER(di." + order + "Word) DESC, di.Id ASC"

                :

            "SELECT DISTINCT di.*, wg.GroupNum,wg.InGroupPos FROM dict AS di " +
            "LEFT JOIN word_grouping AS wg ON ( di.Id = wg.WordId ) " +
            "WHERE di." + language + "SortBy = ? " +
            "OR di." + language + "Plural = ? " +
            "OR di." + language.substring(0, 3) + "Alt = ? " +
            "OR di." + language.substring(0, 3) + "PluralAlt = ? " +
            "Group By di.Id " +
            "ORDER BY wg.GroupNum ASC, wg.InGroupPos ASC, " +
            "LOWER(di." + order + "Word) DESC, di.Id ASC";

//        String query = (wildcardSearch) ? "SELECT * FROM dict where " + language + "SortBy LIKE ? OR " +
//                language + "Plural LIKE ? " +
//                "ORDER BY " + language + "Word ASC"
//                : "SELECT * FROM dict where " + language + "SortBy = ? OR " +
//                language + "Plural = ? " +
//                "ORDER BY " + language + "Word ASC";

        return query;
    }
}
