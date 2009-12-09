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
    private String englishWord, swahiliWord, englishExample, swahiliExample, swahiliPlural,
            englishPlural;
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

            if (fromLanguage.equalsIgnoreCase("ENGLISH"))
            {
                query = getQuery("english", wildCardSearch);

                headers.addElement("English");
                headers.addElement("Swahili");
            }
            else if (fromLanguage.equalsIgnoreCase("SWAHILI"))
            {
                query = getQuery("swahili", wildCardSearch);

                headers.addElement("Swahili");
                headers.addElement("English");
            }
            else
            {
                throw new Exception("Undefined language!");
            }

            Enumeration<String> availableFields = fields.elements();

            while (availableFields.hasMoreElements())
            {
                headers.addElement(availableFields.nextElement());
            }

            statement = connection.prepareStatement(query);

            String parameter = wildCardSearch ? word.replaceAll("\\*", "%") : word;

            statement.setString(1, parameter);
            statement.setString(2, parameter);

            resultSet = statement.executeQuery();

            row = 0; // To hold the number of records

            data = new Vector<Vector<String>>();

            while (resultSet.next())
            {
                row++;

                englishWord = resultSet.getString("EnglishWord");
                swahiliWord = resultSet.getString("SwahiliWord");
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
                }
                else if (fromLanguage.equalsIgnoreCase("SWAHILI"))
                {
                    rows.addElement(swahiliWord);
                    rows.addElement(englishWord);
                }
                if (fields.contains("English Plural"))
                {
                    rows.addElement(englishPlural);
                }
                if (fields.contains("Swahili Plural"))
                {
                    rows.addElement(swahiliPlural);
                }
                if (fields.contains("English Example"))
                {
                    rows.addElement(englishExample);
                }
                if (fields.contains("Swahili Example"))
                {
                    rows.addElement(swahiliExample);
                }

                //add to model
                data.addElement(rows);
            }
            if (row > 0)
            {
                logger.log(row + " results matched for \"" + word + "\" from " + fromLanguage);
                table = new JTable(data, headers);
                table.getTableHeader().setDefaultRenderer(new MyHeaderRenderer());
                table.setDefaultRenderer(Object.class, new MyCellRenderer());
                table.setIntercellSpacing(new Dimension(1, 1));
                table.setShowHorizontalLines(false);
                table.setShowVerticalLines(true);
                table.setGridColor(Color.lightGray);

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
            logger.log(ex.toString());

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
                logger.log(ex.toString());
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

//        String query = (wildcardSearch) ? "SELECT DISTINCT di.*, wg.GroupNum, wg.InGroupPos " +
//                "FROM dict AS di " +
//                "LEFT JOIN word_grouping AS wg " +
//                "WHERE wg.WordId = di.Id AND (" + language + "SortBy LIKE ? OR " +
//                language + "Plural LIKE ?) " +
//                "ORDER BY di." + language + "Word ASC"
//                : "SELECT DISTINCT di.*, wg.GroupNum, wg.InGroupPos " +
//                "FROM dict AS di " +
//                "LEFT JOIN word_grouping AS wg " +
//                "WHERE wg.WordId = di.Id AND (" + language + "SortBy = ? OR " +
//                language + "Plural = ?) " +
//                "ORDER BY di." + language + "Word ASC";
        String query = (wildcardSearch) ? "SELECT * FROM dict where " + language + "SortBy LIKE ? OR " +
                language + "Plural LIKE ? " +
                "ORDER BY " + language + "Word ASC"
                : "SELECT * FROM dict where " + language + "SortBy = ? OR " +
                language + "Plural = ? " +
                "ORDER BY " + language + "Word ASC";

        return query;
    }
}
