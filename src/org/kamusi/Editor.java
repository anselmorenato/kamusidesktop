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
import javax.swing.JOptionPane;

/**
 * class Editor
 */
public class Editor extends KamusiLogger
{

    /**
     * Database URL
     */
    private final String DATABASE = "jdbc:sqlite:" + System.getProperty("database");
    /**
     * Database username
     */
    private final String USERNAME = System.getProperty("database_username");
    /**
     * Database password
     */
    private final String PASSWORD = System.getProperty("database_password");
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
     * Loads system properties
     */
    private KamusiProperties props = new KamusiProperties();
    /**
     * The languages
     */
    String language1 = System.getProperty("language1");
    String language2 = System.getProperty("language2");

    /**
     * Constructor
     */
    public Editor()
    {
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
        logApplicationMessage("Editing database entry from " + oldWord + " to " + newWord);

        String username = getUsername();

        if (username == null || username.trim().length() == 0)
        {
            Object[] messageParams =
            {
                props.getName()
            };

            String message = MessageLocalizer.formatMessage("cannot_edit", messageParams);

            Object[] options =
            {
                "Yes",
                "No"
            };

            int choice = JOptionPane.showOptionDialog(null,
                    message,
                    props.getName(),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, //do not use a custom Icon
                    options, //the titles of buttons
                    options[0]); //default button title

            switch (choice)
            {
                case 0: //YES
                    if (askForUsername())
                    {
                        edit(row, columnName, fromLanguage, oldWord, newWord, searchKey);
                    }
                    break;

                case 1: //NO
                    break;

                case -1: //Closed Window
                    break;

                default:
                    break;
            }
        }
        else
        {
            // Get the ID at the specified row and column
            String id = getID(row, fromLanguage, searchKey);

            // We are now interested in the record of this id
            String query = "UPDATE DICT SET " + formatColumnName(columnName)
                    + " = ? WHERE Id = ?";

            //TODO: Perhaps logApplicationMessage the whole query?

            String message = "";

            if (newWord.trim().length() == 0)
            {
                // TODO: Possible bug if a required field is deleted
                message = MessageLocalizer.formatMessage("confirm_delete", null);
            }
            else if (newWord.equals(oldWord))
            {
                logApplicationMessage("No changes applied.");
                return;
            }
            else
            {
                Object[] messageParams =
                {
                    oldWord,
                    newWord
                };

                message = MessageLocalizer.formatMessage("confirm_edit", messageParams);
            }

            Object[] options =
            {
                "Yes",
                "No"
            };

            int choice = JOptionPane.showOptionDialog(null,
                    message,
                    props.getName(),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, //do not use a custom Icon
                    options, //the titles of buttons
                    options[1]); //default button title

            switch (choice)
            {
                case 0: //YES
                    try
                    {
                        connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);

//            throw new Exception("Just a test exception");

                        statement = connection.prepareStatement(query);
                        statement.setString(1, newWord);
                        statement.setString(2, id);

                        statement.executeUpdate();

                        logUpdate(query + "|" + newWord + "|" + id + "|" + getUsername());

                        System.out.println(query + "|" + newWord + "|" + id + "|" + getUsername());
                    }
                    catch (SQLException ex)
                    {
                        logApplicationMessage(ex.toString());
                        MainWindow.showError(ex);
                    }
                    catch (Exception ex)
                    {
                        logApplicationMessage(ex.toString());
                        MainWindow.showError(ex);
                    }
                    finally
                    {
                        try
                        {
                            statement.close();
                            connection.close();
                        }
                        catch (SQLException ex)
                        {
                            logApplicationMessage(ex.toString());
                            MainWindow.showError(ex);
                        }
                    }

                    break;

                case 1: //NO
                    break;

                case -1: //Closed Window
                    break;

                default:
                    break;
            }
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

            if (fromLanguage.equalsIgnoreCase(language1))
            {
                query = Translator.getQuery(language1, searchKey.contains("\\*"));
            }
            else if (fromLanguage.equalsIgnoreCase(language2))
            {
                query = Translator.getQuery(language2, searchKey.contains("\\*"));
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
                    logApplicationMessage("Editing ID: " + id);
                    break;
                }
                else
                {
                    currentRow++;
                }
            }
        }
        catch (SQLException ex)
        {
            logApplicationMessage(ex.toString());
            MainWindow.showError(ex);
        }
        catch (Exception ex)
        {
            logApplicationMessage(ex.toString());
            MainWindow.showError(ex);
        }
        finally
        {
            try
            {
                statement.close();
                connection.close();
            }
            catch (SQLException ex)
            {
                logApplicationMessage(ex.toString());
                MainWindow.showError(ex);
            }
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
        if (columnName.equalsIgnoreCase(language1))
        {
            return (language1 + "Word");
        }
        else if (columnName.equalsIgnoreCase(language2))
        {
            return (language2 + "Word");
        }
        else if (columnName.equalsIgnoreCase(language1 + " plural"))
        {
            return (language1 + "Plural");
        }
        else if (columnName.equalsIgnoreCase(language2 + " plural"))
        {
            return (language2 + "Plural");
        }
        else if (columnName.equalsIgnoreCase(language1 + " example"))
        {
            return (language1 + "Example");
        }
        else if (columnName.equalsIgnoreCase(language2 + " example"))
        {
            return (language2 + "Example");
        }
        else
        {
            return null;
        }
    }

    /**
     * Logs the update we are doing into the file.
     * This update logApplicationMessage is what is used in synchronizing with the online server.
     * Its therefore imperative that the logApplicationMessage is correctly stored
     * @param update The update query that has been done
     */
    public void logUpdate(String update)
    {
        final String editLog = "log/edit.log";

        try
        {
            File editFile = new File(editLog);

            if (!editFile.exists())
            {
                throw new FileNotFoundException("Edit file does not exist!");
            }

            //Append the new data
            //Write out the new update file
            BufferedWriter writer = new BufferedWriter(new FileWriter(editFile, true));
            HexConverter converter = new HexConverter();
            writer.write(converter.getHex(update));
            writer.newLine();
            writer.close();

            logApplicationMessage("Updated edit log");
        }
        catch (FileNotFoundException ex)
        {
            logApplicationMessage(ex.toString() + "Attempting to create one");

            FileWriter fstream;
            BufferedWriter writer = null;
            try
            {
                fstream = new FileWriter(editLog, false);
                writer = new BufferedWriter(fstream);
                HexConverter converter = new HexConverter();
                writer.write("# DO NOT EDIT THIS FILE BY HAND");
                writer.newLine();
            }
            catch (IOException exe)
            {
                logApplicationMessage(exe.toString());
            }
            finally
            {
                try
                {
                    writer.close();
                }
                catch (IOException exe)
                {
                    logApplicationMessage(exe.toString());
                }
                // Now do the update
                logUpdate(update);
            }
        }
        catch (IOException ex)
        {
            logApplicationMessage("Failed to update edit log: " + ex.toString());
            MainWindow.showError(ex);
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
        String message = MessageLocalizer.formatMessage("confirm_delete", null);

        Object[] options =
        {
            "Yes",
            "No"
        };

        int choice = JOptionPane.showOptionDialog(null,
                message,
                props.getName(),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, //do not use a custom Icon
                options, //the titles of buttons
                options[0]); //default button title

        switch (choice)
        {
            case 0: //YES

                String id = getID(row, fromLanguage, searchKey);
                String query = "DELETE FROM dict WHERE id = ?";

                try
                {
                    connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
                    statement = connection.prepareStatement(query);
                    statement.setString(1, id);

                    statement.executeUpdate();
                    logUpdate(query + "|" + id + "|" + getUsername());
                }
                catch (Exception ex)
                {
                    logApplicationMessage(ex.toString());
                    MainWindow.showError(ex);
                }
                finally
                {
                    try
                    {
                        statement.close();
                        connection.close();
                    }
                    catch (SQLException ex)
                    {
                        logApplicationMessage(ex.toString());
                        MainWindow.showError(ex);
                    }
                }
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
     * Gets the username for kamusi edits
     * @return The username
     */
    public String getUsername()
    {
        String username = "";

        try
        {
            File userFile = new File(usernameFile);

            if (!userFile.exists())
            {
                throw new FileNotFoundException("Edit file does not exist!");
            }

            FileReader fileReader = new FileReader(userFile);
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
        catch (FileNotFoundException ex)
        {
            logApplicationMessage(ex.toString());
        }
        catch (Exception ex)
        {
            logApplicationMessage(ex.toString());
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
            logApplicationMessage(ex.toString());
            MainWindow.showError(ex);
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
                logApplicationMessage(ex.toString());
                MainWindow.showError(ex);
                return false;
            }
        }
    }

    /**
     * Prompts for a kamusiproject username
     * @return True for a successful setting of username, false otherwise
     */
    private boolean askForUsername()
    {
        String newUsername = JOptionPane.showInputDialog(null,
                props.getName() + " needs your kamusi.org username"
                + "\nin order to process your edits. You will not be asked for this again."
                + "\n\nPlease enter your kamusi.org username");

        if ((newUsername == null) || (newUsername.trim().length() == 0))
        {
            String message = "You will not be able to edit entries.\n\n"
                    + "Proceed?";
            Object[] options =
            {
                "Yes",
                "No"
            };

            int choice = JOptionPane.showOptionDialog(null,
                    message,
                    props.getName(),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, //do not use a custom Icon
                    options, //the titles of buttons
                    options[1]); //default button title

            switch (choice)
            {
                case 0: //YES
                    break;

                case 1: //NO
                    askForUsername();
                    break;

                case -1: //Closed Window
                    break;

                default:
                    break;
            }
        }
        else
        {
            if (setUsername(newUsername))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a word to the database
     * @param EnglishWord 
     * @param SwahiliWord 
     * @param EnglishSortBy 
     * @param SwahiliSortBy 
     * @param PartOfSpeech 
     * @param Class 
     * @param SwahiliPlural 
     * @param EnglishPlural 
     * @param EngAlt 
     * @param SwaAlt 
     * @param EngPluralAlt 
     * @param SwaPluralAlt
     * @param EnglishDef
     * @param SwahiliDefinition
     * @param Derived
     * @param DerivedLang
     * @param RelatedWords
     * @param DialectNote
     * @param Taxonomy
     * @param EnglishExample
     * @param SwahiliExample
     * @param Dialect
     * @param Terminology 
     * @return true if word was added successfully, false otherwise
     */
    public boolean addWord(String EnglishWord, String SwahiliWord, String EnglishSortBy,
            String SwahiliSortBy, String PartOfSpeech, String Class, String SwahiliPlural,
            String EnglishPlural, String EngAlt, String SwaAlt, String EngPluralAlt,
            String SwaPluralAlt, String EnglishDef, String SwahiliDefinition, String Derived,
            String DerivedLang, String RelatedWords, String DialectNote, String Taxonomy,
            String EnglishExample, String SwahiliExample, String Dialect, String Terminology)
    {

        // Confirm minimum requirements
        if (SwahiliWord == null || SwahiliWord.trim().length() == 0
                || EnglishWord == null || EnglishWord.trim().length() == 0
                || SwahiliSortBy == null || SwahiliSortBy.trim().length() == 0
                || EnglishSortBy == null || EnglishSortBy.trim().length() == 0
                || PartOfSpeech == null || PartOfSpeech.trim().length() == 0
                || Class == null || Class.trim().length() == 0
                || PartOfSpeech.trim().equalsIgnoreCase("Please Select")
                || Class.trim().equalsIgnoreCase("Please Select"))
        {
            MainWindow.showWarning("Mandatory field(s) missing values");
        }
        else
        {
            try
            {
                String query = "INSERT INTO dict (PartOfSpeech, Class, SwahiliSortBy, EnglishSortBy, "
                        + "SwahiliWord, EnglishWord, SwahiliPlural, EnglishPlural, SwahiliDefinition, "
                        + "SwahiliExample, EnglishExample, Derived, DialectNote, Dialect, Terminology, "
                        + "EnglishDef, DerivedLang, Taxonomy, RelatedWords, EngAlt, SwaAlt, EngPluralAlt, "
                        + "SwaPluralAlt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                connection = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);

                statement = connection.prepareStatement(query);

                statement.setString(1, PartOfSpeech);
                statement.setString(2, Class);
                statement.setString(3, SwahiliSortBy);
                statement.setString(4, EnglishSortBy);
                statement.setString(5, SwahiliWord);
                statement.setString(6, EnglishWord);
                statement.setString(7, SwahiliPlural);
                statement.setString(8, EnglishPlural);
                statement.setString(9, SwahiliDefinition);
                statement.setString(10, SwahiliExample);
                statement.setString(11, EnglishExample);
                statement.setString(12, Derived);
                statement.setString(13, DialectNote);
                statement.setString(14, Dialect);
                statement.setString(15, Terminology);
                statement.setString(16, EnglishDef);
                statement.setString(17, DerivedLang);
                statement.setString(18, Taxonomy);
                statement.setString(19, RelatedWords);
                statement.setString(20, EngAlt);
                statement.setString(21, SwaAlt);
                statement.setString(22, EngPluralAlt);
                statement.setString(23, SwaPluralAlt);

                statement.executeUpdate();

                String updateLog = query + "|";
                updateLog += PartOfSpeech + "|";
                updateLog += Class + "|";
                updateLog += SwahiliSortBy + "|";
                updateLog += EnglishSortBy + "|";
                updateLog += SwahiliWord + "|";
                updateLog += EnglishWord + "|";
                updateLog += SwahiliPlural + "|";
                updateLog += EnglishPlural + "|";
                updateLog += SwahiliDefinition + "|";
                updateLog += SwahiliExample + "|";
                updateLog += EnglishExample + "|";
                updateLog += Derived + "|";
                updateLog += DialectNote + "|";
                updateLog += Dialect + "|";
                updateLog += Terminology + "|";
                updateLog += EnglishDef + "|";
                updateLog += DerivedLang + "|";
                updateLog += Taxonomy + "|";
                updateLog += RelatedWords + "|";
                updateLog += EngAlt + "|";
                updateLog += SwaAlt + "|";
                updateLog += EngPluralAlt + "|";
                updateLog += SwaPluralAlt;

                logUpdate(updateLog + "|" + getUsername());

                return true;
            }
            catch (Exception ex)
            {
                MainWindow.showError(ex);
                return false;
            }
            finally
            {
                try
                {
                    statement.close();
                    connection.close();
                }
                catch (SQLException ex)
                {
                    MainWindow.showError(ex);
                }
            }
            /*CREATE TABLE "dict" (
            "Id" integer NOT NULL primary key autoincrement,
            "PartOfSpeech" integer unsigned NOT NULL default '0',
            "Class" integer unsigned NOT NULL default '0',
            "SwahiliSortBy" varchar(16) NOT NULL default '',
            "EnglishSortBy" varchar(17) NOT NULL default '',
            "SwahiliWord" varchar(53) NOT NULL default '',
            "EnglishWord" varchar(175) NOT NULL default '',
            "SwahiliPlural" varchar(255) NOT NULL default '',
            "EnglishPlural" varchar(255) NOT NULL default '',
            "SwahiliDefinition" varchar(174) NOT NULL default '',
            "SwahiliExample" text,
            "EnglishExample" text,
            "Derived" varchar(82) NOT NULL default '',
            "DialectNote" varchar(255) NOT NULL default '',
            "Dialect" text NOT NULL,
            "Terminology" text NOT NULL,
            "EnglishDef" text,
            "DerivedLang" integer unsigned default NULL,
            "Taxonomy" text NOT NULL,
            "RelatedWords" text NOT NULL,
            "EngAlt" varchar(255) NOT NULL default '',
            "SwaAlt" varchar(255) NOT NULL default '',
            "EngPluralAlt" varchar(255) NOT NULL default '',
            "SwaPluralAlt" varchar(255) NOT NULL default ''
             */
        }

        return false;

    }
}
