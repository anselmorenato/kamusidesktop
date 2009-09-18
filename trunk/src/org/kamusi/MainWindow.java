/**
 * MainWindow.java
 * Created on Aug 6, 2009, 3:52:23 PM
 * @author arthur
 */
package org.kamusi;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * This is the main window of the application
 */
public class MainWindow extends JFrame implements TableModelListener
{

    /**
     * The input field for the word to be translated
     */
    private static JTextField wordField;
    /**
     * Label to direct for the input
     */
    private JLabel wordLabel;
    /**
     * Panel to hold the input field for the word
     */
    private JPanel wordInputPanel;
    /**
     * Panel to hold all the input objects
     */
    private JPanel inputPanel;
    /**
     * Panel to hold the checkboxes for the fields to be displayed
     */
    private JPanel fieldsPanel;
    /**
     * Panel for displaying the results
     */
    private JPanel outputPanel;
    /**
     * Panel for the status bar
     */
    private JPanel statusPanel;
    /**
     * The status label
     */
    private static JLabel statusLabel, staticLabel;
    /**
     * Panel to hold the Radio Buttons for the available languages
     */
    private JPanel languagePanel;
    /**
     * Radio for Swahili to English translation
     */
    private JRadioButton swahiliToEnglish;
    /**
     * Radio for English to Swahili translation
     */
    private JRadioButton englishToSwahili;
    /**
     * Button group for the languages
     */
    private ButtonGroup language;
    /**
     * Button for resetting the window
     */
    private JButton resetButton;
    /**
     * Button for cancelling the restore
     */
    private static JButton cancelUpdateButton;
    /**
     * Checkboxes for the fields to be displayed
     */
    private JCheckBox swahiliWord;
    private JCheckBox englishWord;
    private JCheckBox englishExample;
    private JCheckBox swahiliExample;
    private JCheckBox englishPlural;
    private JCheckBox swahiliPlural;
    /**
     * The MenuBar
     */
    private JMenuBar menuBar;
    /**
     * File Menu
     */
    private JMenu fileMenu;
    private static JMenuItem fileUpdate;
    private JMenuItem filePrint;
    private JMenuItem fileQuit;
    private JMenu helpMenu;
    private JMenuItem helpAbout;
    private static JMenuItem helpDownloadOriginal;
    /**
     * The updater
     */
    private static Restorer restorer;
    /**
     * For restore progress indication
     */
    private static JProgressBar progressBar;
    /**
     * For restore indication
     */
    private static boolean updating = false;
    /**
     * For logging system messages
     */
    private static KamusiLogger logger;
    /**
     * Used in the restore progress
     */
    public static long downloadedSize = 0;
    public static long totalDownloadSize = 0;
    private static final String APPLICATION_NAME = "Kamusi Desktop";
    /**
     * For the editing of cells
     */
    private String oldWord = "";
    private Editor editor;
    /**
     * Differentiates the different versions of the software
     */
    private final boolean isEditorVersion = true;
    private final String TITLE =
            (isEditorVersion)
            ? "Kamusi Project Desktop - Editor's Edition"
            : "Kamusi Project Desktop";

    /**
     * Initializes the display
     */
    public MainWindow()
    {
        super();
        setTitle(TITLE);
        initComponents();
        addActionListeners();
    }

    /**
     * Sets the action listeners for the objects in the window
     */
    private void addActionListeners()
    {
        /**
         * Adds action listener for the "cancel restore" button
         */
        cancelUpdateButton.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                if (restorer.cancelUpdate())
                {
                    fileUpdate.setEnabled(true);
                    helpDownloadOriginal.setEnabled(true);
                    // Reset the output display
                    statusPanel.removeAll();
                    statusPanel.add(statusLabel, BorderLayout.WEST);
                    statusPanel.add(staticLabel, BorderLayout.EAST);
                    updateStatusBar("Database update has been cancelled");
                    pack();
                }
            }
        });
        /**
         * Adds action listener for the "clear" button
         */
        resetButton.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                reset(); // Reset the input/output display
            }
        });

        /**
         * Adds a key listener for looking up a word in real-time typing
         */
        wordField.addKeyListener(new KeyAdapter()
        {

            /**
             * What happens when a key is released?
             */
            @Override
            public void keyReleased(KeyEvent e)
            {
                fetchTranslation(e);
            }
        });

        /**
         * Add a listener for the file-restore menu item
         */
        fileUpdate.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {

                progressBar.setIndeterminate(true);
                progressBar.setString("Synchronizing databases...");
                JPanel updatePanel = new JPanel();
                updatePanel.setLayout(new BorderLayout());
                updatePanel.add(progressBar, BorderLayout.CENTER);
                updatePanel.add(cancelUpdateButton, BorderLayout.EAST);
                statusPanel.add(updatePanel, BorderLayout.CENTER);
                pack();

                String message;

                if (isEditorVersion)
                {
                    message = "You will not be able to use the application " +
                            "until all the updates have been fetched.\nThis might take" +
                            "some time depending on your internet connection.\n\n" +
                            "Further to this, any editings that you may have done will be " +
                            "committed to the server and\n" +
                            "be made available for download " +
                            "to other " + APPLICATION_NAME + " users.\n\n" +
                            "Are you sure that you want to proceed?";
                }
                else
                {
                    message = "You will not be able to use the application " +
                            "until all the updates have been fetched.\nThis might take" +
                            "some time depending on your internet connection.\n\n" +
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
                        synchronizeDatabases();
                        break;

                    case 1: //NO
                        break;

                    case -1: //Closed Window
                        break;

                    default:
                        break;
                }
            }
        });

        /**
         * Add a listener for the file-quit menu item
         */
        fileQuit.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });

        /**
         * Add a listener for the file-print menu item
         */
        filePrint.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                print();
            }
        });

        /**
         * Add a listener for the help-about menu item
         */
        helpAbout.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                final AboutBox dialog = new AboutBox(null, true);
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
        /**
         * Add a listener for the help-downloadoriginal menu item
         */
        helpDownloadOriginal.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                downloadOriginalDatabase();
            }
        });
        /**
         * Add a listener for the radio buttons
         */
        swahiliToEnglish.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                fetchTranslation();
            }
        });
        englishToSwahili.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                fetchTranslation();
            }
        });
        /**
         * Add a listener for the checkboxes
         */
        englishExample.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                fetchTranslation();
            }
        });
        englishPlural.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                fetchTranslation();
            }
        });
        swahiliExample.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                fetchTranslation();
            }
        });
        swahiliPlural.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                fetchTranslation();
            }
        });
    }

    /**
     * Prints what's currently on display in the JFrame
     */
    private void print()
    {
        String word = wordField.getText().trim();

        String languageToTranslateFrom = "";

        Vector<String> fields = getDisplayableFields();

        if (swahiliToEnglish.isSelected())
        {
            languageToTranslateFrom = "SWAHILI";
        }
        else if (englishToSwahili.isSelected())
        {
            languageToTranslateFrom = "ENGLISH";
        }

        TableModel model = new Translator(languageToTranslateFrom, word, fields);

        JTable newTable = new JTable(model);

        try
        {
            newTable.print();
        }
        catch (PrinterException ex)
        {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the components
     */
    private void initComponents()
    {
        logger = new KamusiLogger();

        restorer = new Restorer();
        progressBar = new JProgressBar();
        progressBar.setString("0%");
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        wordLabel = new JLabel("Word", JLabel.LEFT);
        wordField = new JTextField(70);
        language = new ButtonGroup();
        swahiliToEnglish = new JRadioButton("Swa to Eng", false);
        englishToSwahili = new JRadioButton("Eng to Swa", true);
        language.add(swahiliToEnglish);
        language.add(englishToSwahili);
        languagePanel = new JPanel();
        languagePanel.setLayout(new GridLayout(0, 1));
        languagePanel.add(englishToSwahili);
        languagePanel.add(swahiliToEnglish);

        cancelUpdateButton = new JButton("x");
        cancelUpdateButton.setFont(new Font("Tahoma", Font.PLAIN, 8));
        cancelUpdateButton.setBorderPainted(true);
        cancelUpdateButton.setToolTipText("Cancel Database Update");
        resetButton = new JButton("RESET");

        englishWord = new JCheckBox("English", true);
        englishWord.setEnabled(false);
        swahiliWord = new JCheckBox("Swahili", true);
        swahiliWord.setEnabled(false);
        englishExample = new JCheckBox("English Example");
        swahiliExample = new JCheckBox("Swahili Example");
        englishPlural = new JCheckBox("English Plural");
        swahiliPlural = new JCheckBox("Swahili Plural");

        fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new FlowLayout());
        fieldsPanel.add(new JLabel("Show Fields: ", JLabel.LEFT));
        fieldsPanel.add(englishWord);
        fieldsPanel.add(swahiliWord);
        fieldsPanel.add(englishPlural);
        fieldsPanel.add(swahiliPlural);
        fieldsPanel.add(englishExample);
        fieldsPanel.add(swahiliExample);

        wordInputPanel = new JPanel();
        wordInputPanel.setLayout(new FlowLayout());
        wordInputPanel.add(languagePanel);
        wordInputPanel.add(wordLabel);
        wordInputPanel.add(wordField);
        wordInputPanel.add(resetButton);

        inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(0, 1));
        inputPanel.add(wordInputPanel);
        inputPanel.add(fieldsPanel);

        outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());

        statusLabel = new JLabel("The Kamusi Project", JLabel.LEFT);
        staticLabel = new JLabel("The Kamusi Project", JLabel.LEFT);
        statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(staticLabel, BorderLayout.EAST);

        //Populate the menu
        fileUpdate = new JMenuItem("Synchronize");
        filePrint = new JMenuItem("Print");
        fileQuit = new JMenuItem("Quit");
        helpAbout = new JMenuItem("About");
        fileMenu = new JMenu("File");
        helpMenu = new JMenu("Help");
        helpDownloadOriginal = new JMenuItem("Download Original Database");
        menuBar = new JMenuBar();

        editor = new Editor();

        fileMenu.add(fileUpdate);
//        fileMenu.add(filePrint);
        fileMenu.addSeparator();
        fileMenu.add(fileQuit);

        helpMenu.add(helpDownloadOriginal);
        helpMenu.add(helpAbout);
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        //Lay out everything on the window
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(outputPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        setSize(500, 300);

        setIconImage(new ImageIcon(getClass().
                getResource("/org/kamusi/resources/favicon.png")).getImage());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        JFrame.setDefaultLookAndFeelDecorated(false);

        wordField.requestFocusInWindow(); // Makes the cursor go to this field on startup
    }

    /**
     * Resets the window to its default state
     */
    private void reset()
    {
        wordField.setText("");
        outputPanel.removeAll();
        System.gc();

        if (!updating)
        {
            statusPanel.removeAll();
            statusPanel.add(statusLabel, BorderLayout.WEST);
            statusPanel.add(staticLabel, BorderLayout.EAST);
        }
        else
        {
            updateStatusBar("The Kamusi Project");
        }

        pack();
    }

    /**
     * Gets a Vector representation of the selected displayable fields
     * @return The Vector representation
     */
    private Vector<String> getDisplayableFields()
    {
        Vector<String> fields = new Vector<String>();

        if (englishPlural.isSelected())
        {
            fields.add("English Plural");
        }
        if (swahiliPlural.isSelected())
        {
            fields.add("Swahili Plural");
        }
        if (englishExample.isSelected())
        {
            fields.add("English Example");
        }
        if (swahiliExample.isSelected())
        {
            fields.add("Swahili Example");
        }

        return fields;
    }

    /**
     * Displays a table containing the translations
     * @param word the word to be translated
     */
    private void displayTable(String word)
    {
        String languageToTranslateFrom = "";

        Vector<String> fields = getDisplayableFields();

        if (swahiliToEnglish.isSelected() || englishToSwahili.isSelected())
        {

            if (swahiliToEnglish.isSelected())
            {
                languageToTranslateFrom = "SWAHILI";
            }
            else if (englishToSwahili.isSelected())
            {
                languageToTranslateFrom = "ENGLISH";
            }

            outputPanel.removeAll();

            Translator resultTable =
                    new Translator(languageToTranslateFrom, word, fields);

            final JTable newTable = resultTable.getTable();

            newTable.setRowSelectionAllowed(true);
            newTable.setColumnSelectionAllowed(false);

            final JScrollPane scrollPane = new JScrollPane(newTable);

            if (isEditorVersion)
            {
                newTable.getModel().addTableModelListener(this);

                newTable.addMouseListener(new MouseAdapter()
                {

                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        Point point = e.getPoint();
                        int column = newTable.columnAtPoint(point);
                        final int row = newTable.rowAtPoint(point);
                        final String columnName = newTable.getColumnName(column);
                        String cellValue = (String) newTable.getValueAt(row, column);
                        oldWord = (cellValue);

                        if (e.isMetaDown())
                        {
                            //Display a popup menu
                            JPopupMenu popupMenu = new JPopupMenu();
                            JMenuItem editEntry = new JMenuItem("Edit Value");
                            JMenuItem deleteEntry = new JMenuItem("Delete Word");
                            JMenuItem addNewEntry = new JMenuItem("Add New Word");
                            popupMenu.add(editEntry);
                            popupMenu.add(deleteEntry);
                            popupMenu.addSeparator();
                            popupMenu.add(addNewEntry);
                            editEntry.addActionListener(new ActionListener()
                            {

                                public void actionPerformed(ActionEvent e)
                                {
                                    String newWord =
                                            JOptionPane.showInputDialog(null,
                                            "Please enter the New Word", oldWord);

                                    if ((newWord != null))
                                    {
                                        String fromLanguage =
                                                (swahiliToEnglish.isSelected())
                                                ? "Swahili" : "English";
                                        String searchKey = wordField.getText().trim();
                                        
                                        editor.edit(row, columnName, fromLanguage,
                                                oldWord, newWord, searchKey);
                                        fetchTranslation();
                                        return;
                                    }
                                }
                            });
                            deleteEntry.addActionListener(new ActionListener()
                            {

                                public void actionPerformed(ActionEvent e)
                                {
                                    String fromLanguage =
                                            (swahiliToEnglish.isSelected())
                                            ? "Swahili" : "English";
                                    String searchKey = wordField.getText().trim();
                                    
                                    editor.deleteEntry(row, fromLanguage,
                                            oldWord, searchKey);
                                    fetchTranslation();
                                    return;
                                }
                            });
                            addNewEntry.addActionListener(new ActionListener()
                            {

                                public void actionPerformed(ActionEvent e)
                                {
                                    String exception = String.valueOf(
                                            new UnsupportedOperationException(
                                            "Adding new entries is not yet implemented"));
                                    MainWindow.showWarning(exception);
                                }
                            });
                            popupMenu.show(newTable, point.x, point.y);
                        }
                    }
                });
            }

            outputPanel.add(scrollPane, BorderLayout.CENTER);

            // Update the status bar
            updateStatusBar(resultTable.getResultCount() + " Rows fetched");

            pack();

            scrollPane.addMouseListener(new MouseAdapter()
            {

                @Override
                public void mouseClicked(MouseEvent e)
                {
                    Point point = e.getPoint();

                    if (e.isMetaDown())
                    {
                        //Display a popup menu
                        JPopupMenu popupMenu = new JPopupMenu();
                        JMenuItem addNewEntry = new JMenuItem("Add New Word");
                        popupMenu.add(addNewEntry);
                        addNewEntry.addActionListener(new ActionListener()
                        {

                            public void actionPerformed(ActionEvent e)
                            {
                                String exception = String.valueOf(
                                        new UnsupportedOperationException(
                                        "Adding new entries is not yet implemented"));
                                MainWindow.showWarning(exception);
                            }
                        });
                        popupMenu.show(scrollPane, point.x, point.y);
                    }
                }
            });
        }
        else
        {
            showWarning("Please select the desired language first");
        }
    }

    /**
     * Updates the status bar with the new status message
     * @param newMessage the new message to be displayed
     */
    private void updateStatusBar(String newMessage)
    {
        statusLabel.setText(newMessage);
    }

    /**
     * Fetches and displays the translation depending on the key pressed
     * on the field bearing the word to translate
     * @param e The key to listen for
     */
    private void fetchTranslation(KeyEvent e)
    {
        String word = wordField.getText().trim();

        if (e.isActionKey())
        {
        }
        else if (e.getKeyCode() == 8) // Backspace
        {
            if (word.length() == 0) // Backspace
            {
                reset();
            }
            else
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                displayTable(word);
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
        else
        {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            displayTable(word);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Refreshes the display
     * on the field bearing the word to translate
     * @param e The key to listen for
     */
    private void fetchTranslation()
    {
        String word = wordField.getText().trim();
        if (word.trim().length() == 0)
        {
        }
        else
        {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            displayTable(word);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Fetches an updates for the database
     */
    private void synchronizeDatabases()
    {
        Synchronizer synchronizer = new Synchronizer();
        String size = String.valueOf(synchronizer.getSizeOfUpdate());

        if (synchronizer.canSync())
        {
            String message = "We will now fetch " + size + " bytes of updates.\n\n" +
                    "Proceed?";
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
//                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    wordField.setEnabled(false);
                    synchronizer.synchronize(isEditorVersion);
//                    synchronizer.run();
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    wordField.setEnabled(true);
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
            MainWindow.showError("An error occurred while synchronizing.\n" +
                    "Check your connection to the Internet or try again later.");
        }
        reset();
    }

    /**
     * Fetches the original database
     */
    private void downloadOriginalDatabase()
    {
        updating = true;

        updateStatusBar("Downloading database update...");

        progressBar.setIndeterminate(true);
        progressBar.setString("Initializing. Please wait...");

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        long size = restorer.getSizeOfUpdate();

        if (restorer.canRestore())
        {
            double sizeInMBForm = (double) size / 1000 / 1000;
            DecimalFormat twoDForm = new DecimalFormat("#.##");

            String message = "Kamusi Desktop will now download " +
                    Double.valueOf(twoDForm.format(sizeInMBForm)) +
                    " MB of database update. Proceed?";

            Object[] options =
            {
                "Yes",
                "No"
            };

            int choice = JOptionPane.showOptionDialog(null,
                    message,
                    APPLICATION_NAME,
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, //do not use a custom Icon
                    options, //the titles of buttons
                    options[1]); //default button title

            switch (choice)
            {
                case 0: //YES
                    fileUpdate.setEnabled(false);
                    helpDownloadOriginal.setEnabled(false);
                    updateStatusBar("Downloading update");
                    JPanel updatePanel = new JPanel();
                    updatePanel.setLayout(new BorderLayout());
                    updatePanel.add(progressBar, BorderLayout.CENTER);
                    updatePanel.add(cancelUpdateButton, BorderLayout.EAST);
                    statusPanel.add(updatePanel, BorderLayout.CENTER);
                    pack();
                    restorer.restore();
                    break;

                case 1: //NO
                    updateStatusBar("Database update cancelled.");
                    updating = false;
                    break;

                case -1: //Closed Window
                    updateStatusBar("Database update cancelled.");
                    updating = false;
                    break;

                default:
                    updating = false;
                    break;
            }
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Updates the progress bar with how much of the restore has been got
     * @throws MalformedURLException
     * @throws IOException
     */
    public static void updateProgressBar() throws MalformedURLException, IOException
    {
        progressBar.setIndeterminate(false);
        downloadedSize = restorer.getSizeOfDatabase();
        int downloadedInInt = (int) downloadedSize;
        totalDownloadSize = restorer.getSizeOfUpdate();
        int totalInInt = (int) totalDownloadSize;
        int percentage = (int) Math.ceil((downloadedInInt * 100) / totalInInt);
//        int percentage = (downloadedInInt * 100) / totalInInt;
        progressBar.setString("Updating database. Downloaded " + downloadedInInt +
                " out of " + totalInInt + " bytes (" + percentage + "% )");
        progressBar.setValue(percentage);
        logger.log("Downloaded update: " + downloadedInInt + " out of " + totalInInt);
        if (percentage >= 100)
        {
            fileUpdate.setEnabled(false);
            helpDownloadOriginal.setEnabled(false);
            cancelUpdateButton.setEnabled(false);
            updating = false;

            showInfo("Database has been updated successfully.");
        }
    }

    /**
     * Displays an error message
     * @param message The error message
     */
    protected static void showError(String message)
    {
        logger.log(message);
        JOptionPane.showMessageDialog(null, message,
                APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays an information message
     * @param message The Information message
     */
    protected static void showInfo(String message)
    {
        logger.log(message);
        JOptionPane.showMessageDialog(null, message,
                APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays a warning message
     * @param message The warning message
     */
    protected static void showWarning(String message)
    {
        logger.log(message);
        JOptionPane.showMessageDialog(null, message,
                APPLICATION_NAME, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * What happens when a cell value is changed
     * @param e The event that fired the table change
     */
    public void tableChanged(TableModelEvent e)
    {
        int row = e.getFirstRow();
        int column = e.getColumn();

        TableModel model = (TableModel) e.getSource();
        String columnName = model.getColumnName(column);

        Object newWord = model.getValueAt(row, column);

        String fromLanguage = (swahiliToEnglish.isSelected()) ? "Swahili" : "English";
        String searchKey = wordField.getText().trim();

        editor.edit(row, columnName, fromLanguage, oldWord, (String) newWord, searchKey);

        fetchTranslation();
    }
}
