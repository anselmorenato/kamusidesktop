/**
 * MainWindow.java
 * Created on Aug 6, 2009, 3:52:23 PM
 * @author arthur
 */
package org.kamusi;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.net.UnknownHostException;
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
     * Loads system properties
     */
    private static KamusiProperties props = new KamusiProperties();
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
     * The languages
     */
    String language1 = System.getProperty("language1");
    String language2 = System.getProperty("language2");
    /**
     * Radio for language2 to language1 translation
     */
    private JRadioButton language2Tolanguage1;
    /**
     * Radio for language1 to language2 translation
     */
    private JRadioButton language1Tolanguage2;
    /**
     * Button group for the languages
     */
    private ButtonGroup language;
    /**
     * Button for resetting the window
     */
    private JButton resetButton;
    /**
     * For adding a new word
     */
    private JButton addNewWordButton;
    /**
     * For holding the button for ading a new word
     */
    JPanel tempPanel;
    /**
     * Button for cancelling the restore
     */
    private static JButton cancelUpdateButton;
    /**
     * Checkboxes for the fields to be displayed
     */
    private JCheckBox language2Word;
    private JCheckBox language1Word;
    private JCheckBox language1Example;
    private JCheckBox language2Example;
    private JCheckBox language1Plural;
    private JCheckBox language2Plural;
    /**
     * The MenuBar
     */
    private JMenuBar menuBar;
    /**
     * File Menu
     */
    private JMenu fileMenu;
    private static JMenuItem fileSynchronize;
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
    /**
     * For the editing of cells
     */
    private String oldWord = "";
    private Editor editor;
    /**
     * Differentiates the different versions of the software
     */
    private final boolean isEditorVersion = props.getEditor();
    private static final String APPLICATION_NAME = props.getName();
    private final String TITLE =
            (isEditorVersion)
            ? APPLICATION_NAME + " - Editor's Edition"
            : APPLICATION_NAME;

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
                    fileSynchronize.setEnabled(true);
                    helpDownloadOriginal.setEnabled(true);
                    // Reset the output display
                    statusPanel.removeAll();
                    statusPanel.add(statusLabel, BorderLayout.WEST);
                    statusPanel.add(staticLabel, BorderLayout.EAST);
                    updateStatusBar(MessageLocalizer.formatMessage("download_cancelled", null));
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
        fileSynchronize.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                String message;

                if (isEditorVersion)
                {
                    message =
                            MessageLocalizer.formatMessage("confirm_update", null)
                            + MessageLocalizer.formatMessage("further_details_editor_confirm_update",
                            new Object[]
                            {
                                APPLICATION_NAME
                            })
                            + MessageLocalizer.formatMessage("proceed", null);
                }
                else
                {
                    message = MessageLocalizer.formatMessage("confirm_update", null)
                            + MessageLocalizer.formatMessage("proceed", null);
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

                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                        progressBar.setIndeterminate(true);
                        progressBar.setString(MessageLocalizer.formatMessage("synchronizing", null));
                        JPanel updatePanel = new JPanel();
                        updatePanel.setLayout(new BorderLayout());
                        updatePanel.add(progressBar, BorderLayout.CENTER);
                        updatePanel.add(cancelUpdateButton, BorderLayout.EAST);
                        statusPanel.add(updatePanel, BorderLayout.CENTER);
                        pack();

                        try
                        {
                            synchronizeDatabases();
                        }
                        catch (UnknownHostException ex)
                        {
                            updating = false;
                            showError(ex);
                        }
                        catch (MalformedURLException ex)
                        {
                            updating = false;
                            showError(ex);
                        }
                        catch (IOException ex)
                        {
                            updating = false;
                            showError(ex);
                        }
                        finally
                        {
                            reset();
                        }
                        break;

                    case 1: //NO
                        updating = false;
                        reset();
                        break;

                    case -1: //Closed Window
                        updating = false;
                        reset();
                        break;

                    default:
                        updating = false;
                        reset();
                        break;
                }
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
         * Add a listener for the file-addword menu item
         */
        addNewWordButton.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                new WordAdder(wordField.getText(), language1Tolanguage2.isSelected()).setVisible(true);
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
                try
                {
                    downloadOriginalDatabase();
                }
                catch (IOException ex)
                {
                    updating = false;
                    showError(ex);
                    reset();
                }
            }
        });
        /**
         * Add a listener for the radio buttons
         */
        language2Tolanguage1.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                fetchTranslation();
            }
        });
        language1Tolanguage2.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                fetchTranslation();
            }
        });
        /**
         * Add a listener for the checkboxes
         */
        language1Example.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                fetchTranslation();
            }
        });
        language1Plural.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                fetchTranslation();
            }
        });
        language2Example.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                fetchTranslation();
            }
        });
        language2Plural.addActionListener(new ActionListener()
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

        if (language2Tolanguage1.isSelected())
        {
            languageToTranslateFrom = language2;
        }
        else if (language1Tolanguage2.isSelected())
        {
            languageToTranslateFrom = language1;
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

        wordLabel = new JLabel(MessageLocalizer.formatMessage("word_label", null), JLabel.LEFT);
        wordField = new JTextField(70);
        language = new ButtonGroup();

        Object[] languages =
        {
            language1,
            language2
        };

        language1Tolanguage2 = new JRadioButton(language1 + " to " + language2, true);
        language2Tolanguage1 = new JRadioButton(language2 + " to " + language1, false);
        language.add(language2Tolanguage1);
        language.add(language1Tolanguage2);
        languagePanel = new JPanel();
        languagePanel.setLayout(new GridLayout(0, 1));
        languagePanel.add(language1Tolanguage2);
        languagePanel.add(language2Tolanguage1);

        cancelUpdateButton = new JButton("x");
        cancelUpdateButton.setFont(new Font("Tahoma", Font.PLAIN, 8));
        cancelUpdateButton.setBorderPainted(true);
        cancelUpdateButton.setToolTipText(MessageLocalizer.formatMessage("cancel_update", null));
        resetButton = new JButton(MessageLocalizer.formatMessage("reset_button", null));

        language1Word = new JCheckBox(language1, true);
        language1Word.setEnabled(false);
        language2Word = new JCheckBox(language2, true);
        language2Word.setEnabled(false);
        language1Example = new JCheckBox(MessageLocalizer.formatMessage("language1_example", languages));
        language2Example = new JCheckBox(MessageLocalizer.formatMessage("language2_example", languages));
        language1Plural = new JCheckBox(MessageLocalizer.formatMessage("language1_plural", languages));
        language2Plural = new JCheckBox(MessageLocalizer.formatMessage("language2_plural", languages));

        fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new FlowLayout());
        fieldsPanel.add(new JLabel(MessageLocalizer.formatMessage("show_fields", null), JLabel.LEFT));
        fieldsPanel.add(language1Word);
        fieldsPanel.add(language2Word);
        fieldsPanel.add(language1Plural);
        fieldsPanel.add(language2Plural);
        fieldsPanel.add(language1Example);
        fieldsPanel.add(language2Example);

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

        statusLabel = new JLabel(props.getName(), JLabel.LEFT);
        staticLabel = new JLabel("The Kamusi Project", JLabel.LEFT);
        statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(staticLabel, BorderLayout.EAST);

        addNewWordButton = new JButton(
                MessageLocalizer.formatMessage("add_entry", null));

        tempPanel = new JPanel();
        tempPanel.setLayout(new BorderLayout());

        tempPanel.add(addNewWordButton, BorderLayout.WEST);
        statusPanel.add(tempPanel, BorderLayout.CENTER);
        addNewWordButton.setVisible(false);

        //Populate the menu
        fileSynchronize = new JMenuItem(MessageLocalizer.formatMessage("file_synchronize", null));
        filePrint = new JMenuItem(MessageLocalizer.formatMessage("file_print", null));
        fileQuit = new JMenuItem(MessageLocalizer.formatMessage("file_quit", null));
        helpAbout = new JMenuItem(MessageLocalizer.formatMessage("help_about", null));
        fileMenu = new JMenu(MessageLocalizer.formatMessage("file_menu", null));
        helpMenu = new JMenu(MessageLocalizer.formatMessage("help_menu", null));
        helpDownloadOriginal = new JMenuItem(MessageLocalizer.formatMessage("help_restore", null));
        menuBar = new JMenuBar();

        editor = new Editor();

        fileMenu.add(fileSynchronize);
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

        System.out.println(updating);

        if (!updating)
        {
            statusPanel.removeAll();
            statusPanel.add(statusLabel, BorderLayout.WEST);
            statusPanel.add(staticLabel, BorderLayout.EAST);
            statusPanel.add(tempPanel, BorderLayout.CENTER);
            addNewWordButton.setVisible(false);
        }
        else
        {
            updateStatusBar(props.getName());
        }

        pack();
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Gets a Vector representation of the selected displayable fields
     * @return The Vector representation
     */
    private Vector<String> getDisplayableFields()
    {
        Vector<String> fields = new Vector<String>();

        Object[] languages =
        {
            language1,
            language2
        };

        if (language1Plural.isSelected())
        {
            fields.add(MessageLocalizer.formatMessage("language1_plural", languages));
        }
        if (language2Plural.isSelected())
        {
            fields.add(MessageLocalizer.formatMessage("language2_plural", languages));
        }
        if (language1Example.isSelected())
        {
            fields.add(MessageLocalizer.formatMessage("language1_example", languages));
        }
        if (language2Example.isSelected())
        {
            fields.add(MessageLocalizer.formatMessage("language2_example", languages));
        }

        return fields;
    }

    /**
     * Displays a table containing the translations
     * @param word the word to be translated
     */
    private void displayTable(String word)
    {
        double startTimestamp = java.util.Calendar.getInstance().getTimeInMillis();

        final String searchKey = wordField.getText().trim();

        try
        {
            String languageToTranslateFrom = "";

            Vector<String> fields = getDisplayableFields();

            if (language2Tolanguage1.isSelected() || language1Tolanguage2.isSelected())
            {

                if (language2Tolanguage1.isSelected())
                {
                    languageToTranslateFrom = System.getProperty("language2");
                }
                else if (language1Tolanguage2.isSelected())
                {
                    languageToTranslateFrom = System.getProperty("language1");
                }

                outputPanel.removeAll();

                Translator translator =
                        new Translator(languageToTranslateFrom, word, fields);

                final JTable resultsTable = translator.getTable();

//                resultsTable.print();
//                final JTable resultsTable = new JTable(new Translator(languageToTranslateFrom, word, fields));

                resultsTable.setRowSelectionAllowed(true);
                resultsTable.setColumnSelectionAllowed(false);

                final JScrollPane scrollPane = new JScrollPane(resultsTable);

                scrollPane.setBackground(Color.WHITE);

                if (isEditorVersion)
                {
                    resultsTable.getModel().addTableModelListener(this);

                    resultsTable.addMouseListener(new MouseAdapter()
                    {

                        @Override
                        public void mouseClicked(MouseEvent e)
                        {
                            Point point = e.getPoint();
                            int column = resultsTable.columnAtPoint(point);
                            final int row = resultsTable.rowAtPoint(point);
                            final String columnName = resultsTable.getColumnName(column);
                            String cellValue = (String) resultsTable.getValueAt(row, column);
                            oldWord = (cellValue);

                            if (e.isMetaDown())
                            {
                                //Display a popup menu
                                JPopupMenu popupMenu = new JPopupMenu();
                                JMenuItem editEntry = new JMenuItem(MessageLocalizer.formatMessage("edit_entry", null));
                                JMenuItem deleteEntry = new JMenuItem(MessageLocalizer.formatMessage("delete_entry", null));
                                JMenuItem addNewEntry = new JMenuItem(MessageLocalizer.formatMessage("add_entry", null));
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
                                                MessageLocalizer.formatMessage("edit_entry", null), oldWord);

                                        if ((newWord != null))
                                        {
                                            String fromLanguage =
                                                    (language2Tolanguage1.isSelected())
                                                    ? language2 : language1;

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
                                                (language2Tolanguage1.isSelected())
                                                ? language2 : language1;
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
                                        new WordAdder(wordField.getText(), language1Tolanguage2.isSelected()).setVisible(true);
                                    }
                                });
                                popupMenu.show(resultsTable, point.x, point.y);
                            }
                        }
                    });
                }

                if (translator.getResultCount() > 0)
                {
                    outputPanel.add(scrollPane, BorderLayout.CENTER);
                }

                int count = translator.getResultCount();

                double finishTimestamp = java.util.Calendar.getInstance().getTimeInMillis();

                Object[] messageArguments =
                {
                    count,
                    ((finishTimestamp - startTimestamp) / 1000)
                };

                // Update the status bar


                if (count == 0)
                {
                    updateStatusBar(MessageLocalizer.formatMessage("found_rows", messageArguments));
                }
                else
                {
                    updateStatusBar(MessageLocalizer.formatMessage("fetched_rows", messageArguments));
                }

                if (count == 0 && !searchKey.contains("*"))
                {
                    addNewWordButton.setVisible(true);
                }
                else
                {
                    addNewWordButton.setVisible(false);
                }

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
                            JMenuItem addNewEntry = new JMenuItem(
                                    MessageLocalizer.formatMessage("add_entry", null));
                            popupMenu.add(addNewEntry);
                            addNewEntry.addActionListener(new ActionListener()
                            {

                                public void actionPerformed(ActionEvent e)
                                {
                                    new WordAdder(wordField.getText(), language1Tolanguage2.isSelected()).setVisible(true);
                                }
                            });
                            popupMenu.show(scrollPane, point.x, point.y);
                        }
                    }
                });
            }
            else
            {
                showWarning(MessageLocalizer.formatMessage("select_language", null));
            }
        }
        catch (Exception ex)
        {
            showError(new Exception(MessageLocalizer.formatMessage("general_error", null)));
        }
    }

    /**
     * Updates the status bar with the new status message
     * @param newMessage the new message to be displayed
     */
    private static void updateStatusBar(String newMessage)
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

        if (word.contains("*"))
        {

            Object[] options =
            {
                "Yes",
                "No"
            };

            int choice = 0;

//                    JOptionPane.showOptionDialog(null,
//                    MessageLocalizer.formatMessage("wildcard_search", null),
//                    props.getName(),
//                    JOptionPane.YES_NO_OPTION,
//                    JOptionPane.QUESTION_MESSAGE,
//                    null, //do not use a custom Icon
//                    options, //the titles of buttons
//                    options[1]); //default button title

            switch (choice)
            {
                case 0: //YES
                    break;

                default:
                    word = word.replaceAll("\\*", "");
                    wordField.setText(word);
                    break;
            }

        }

        int keyCode = e.getKeyCode();

        if (e.isActionKey() || keyCode == 16 || // Shift
                keyCode == 17 || // CTRL
                keyCode == 18 // ALT
                )
        {
        }
        else if (e.getKeyCode() == 8 && word.length() == 0) // Backspace
        {
            reset();
        }
        else
        {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            displayTable(word);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        resetButton.setText(MessageLocalizer.formatMessage("reset_button", null));
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
    private void synchronizeDatabases() throws UnknownHostException,
            MalformedURLException, IOException
    {

        Synchronizer synchronizer = new Synchronizer();

        String size = String.valueOf(synchronizer.getSizeOfUpdate());

        if (synchronizer.canSync())
        {
            Object[] messageArguments =
            {
                size
            };
            String message = MessageLocalizer.formatMessage("download_update_confirm", messageArguments);

            Object[] options =
            {
                MessageLocalizer.formatMessage("yes", null),
                MessageLocalizer.formatMessage("no", null)
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
                    wordField.setEnabled(false);
                    synchronizer.synchronize(isEditorVersion);
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
            updating = false;
            showError(new Exception(MessageLocalizer.formatMessage("synch_error", null)));
        }
        
        reset();
    }

    /**
     * Fetches the original database
     */
    private void downloadOriginalDatabase() throws
            IOException
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

            String message = props.getName() + " will now download "
                    + Double.valueOf(twoDForm.format(sizeInMBForm))
                    + " MB of database update. Proceed?";

            Object[] options =
            {
                MessageLocalizer.formatMessage("yes", null),
                MessageLocalizer.formatMessage("no", null)
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
                    fileSynchronize.setEnabled(false);
                    helpDownloadOriginal.setEnabled(false);
                    updateStatusBar(MessageLocalizer.formatMessage("downloading_update", null));
                    JPanel updatePanel = new JPanel();
                    updatePanel.setLayout(new BorderLayout());
                    updatePanel.add(progressBar, BorderLayout.CENTER);
                    updatePanel.add(cancelUpdateButton, BorderLayout.EAST);
                    statusPanel.add(updatePanel, BorderLayout.CENTER);
                    pack();
                    restorer.restore();
                    break;

                case 1: //NO
                    updateStatusBar(MessageLocalizer.formatMessage("update_cancelled", null));
                    updating = false;
                    break;

                case -1: //Closed Window
                    updateStatusBar(MessageLocalizer.formatMessage("update_cancelled", null));
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
        Object[] messageArguments =
        {
            downloadedInInt,
            totalInInt,
            percentage
        };

        progressBar.setString(MessageLocalizer.formatMessage("download_progress", messageArguments));
        progressBar.setValue(percentage);
        logger.logApplicationMessage(MessageLocalizer.formatMessage("download_progress", messageArguments));

        if (percentage >= 100)
        {
//            fileSynchronize.setEnabled(false);
            helpDownloadOriginal.setEnabled(false);
            cancelUpdateButton.setEnabled(false);
            updating = false;

            showInfo(MessageLocalizer.formatMessage("update_success", null));
        }
    }

    /**
     * Displays an error message
     * @param exception The exception itself
     */
    static protected void showError(Exception exception)
    {
        logger.logExceptionStackTrace(exception);

        Object[] options =
        {
            MessageLocalizer.formatMessage("send_bug_report", null),
            MessageLocalizer.formatMessage("close", null)
        };

        int choice = JOptionPane.showOptionDialog(null,
                exception.toString(),
                props.getName(),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, //do not use a custom Icon
                options, //the titles of buttons
                options[1]); //default button title

        switch (choice)
        {
            case 0: //YES
                DebugTool kamusiDebugger = new DebugTool();
                kamusiDebugger.sendErrorDetails();
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
     * Displays an information message
     * @param message The Information message
     */
    protected static void showInfo(String message)
    {
        logger.logApplicationMessage("[ INFO ] " + message);
        JOptionPane.showMessageDialog(null, message,
                APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays a warning message
     * @param message The warning message
     */
    protected static void showWarning(String message)
    {
        logger.logApplicationMessage("[ WARNING ] " + message);
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

        String fromLanguage = (language2Tolanguage1.isSelected()) ? language2 : language1;
        String searchKey = wordField.getText().trim();

        editor.edit(row, columnName, fromLanguage, oldWord, (String) newWord, searchKey);

        fetchTranslation();
    }
}
