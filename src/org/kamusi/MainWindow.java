package org.kamusi;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableModel;

/**
 * This is the main window of the application
 * @author arthur
 */
public class MainWindow extends JFrame
{

    /**
     * The input field for the word to be translated
     */
    private JTextField wordField;
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
    private JLabel statusLabel, staticLabel;
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
     * JTable for displaying output
     */
    private ResultTable resultTable;
    /**
     * Button for resetting the window
     */
    private JButton resetButton;
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
    private JMenuItem fileUpdate;

    /**
     * Initializes the display
     */
    public MainWindow()
    {
        super("Kamusi Project Desktop");
        initComponents();
        addActionListeners();
    }

    /**
     * Sets the action listeners for the objects in the window
     */
    private void addActionListeners()
    {
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
         * Add a listener for the file-update menu item
         */
        fileUpdate.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                updateDatabase();
            }
        });
    }

    /**
     * Initializes the components
     */
    private void initComponents()
    {
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

        fileUpdate = new JMenuItem("Update Database");
        fileMenu = new JMenu("File");
        menuBar = new JMenuBar();

        fileMenu.add(fileUpdate);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        //Lay out everything on the window
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(outputPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        setSize(500, 300);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        setVisible(true);

        try
        {
            // Set the look and feel to what the user's system looks like
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException ex)
        {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Kamusi Desktop",
                JOptionPane.ERROR_MESSAGE);
        }
        catch (InstantiationException ex)
        {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Kamusi Desktop",
                JOptionPane.ERROR_MESSAGE);
        }
        catch (IllegalAccessException ex)
        {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Kamusi Desktop",
                JOptionPane.ERROR_MESSAGE);
        }
        catch (UnsupportedLookAndFeelException ex)
        {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Kamusi Desktop",
                JOptionPane.ERROR_MESSAGE);
        }

        wordField.requestFocusInWindow(); // Makes the cursor go to this field on startup
    }

    /**
     * Resets the window to its default state
     */
    private void reset()
    {
        wordField.setText("");
        englishWord.setSelected(false);
        englishExample.setSelected(false);
        swahiliExample.setSelected(false);
        englishPlural.setSelected(false);
        swahiliPlural.setSelected(false);
        outputPanel.removeAll();
        updateStatusBar("The Kamusi Project");
        pack();
    }

    /**
     * Gets a Vector representation of the selected displayable fields
     * @return The Vector representation
     */
    private Vector getDisplayableFields()
    {
        Vector fields = new Vector();

        if (englishPlural.isSelected())
        {
            fields.add("EnglishPlural");
        }
        if (swahiliPlural.isSelected())
        {
            fields.add("SwahiliPlural");
        }
        if (englishExample.isSelected())
        {
            fields.add("EnglishExample");
        }
        if (swahiliExample.isSelected())
        {
            fields.add("SwahiliExample");
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
        Vector fields = getDisplayableFields();

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

            if (word.trim().length() == 0)
            {
                JOptionPane.showMessageDialog(null, "Please input a word to translate first.");
            }
            else
            {
                outputPanel.removeAll();

                resultTable = new ResultTable(languageToTranslateFrom, word, fields);
                TableModel model = resultTable.getTableModel();

                JTable newTable = new JTable(model);

                JScrollPane scrollPane = new JScrollPane(newTable);

                outputPanel.add(scrollPane, BorderLayout.CENTER);

                // Update the status bar
                updateStatusBar(resultTable.getResultCount() + " Rows fetched");

                pack();
            }
        }
        else
        {
            JOptionPane.showMessageDialog(null, "Please select the desired language first");
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
    //TODO: Implement database update mechanism

    /**
     * Updates the words database
     */

    private void updateDatabase()
    {
        JOptionPane.showMessageDialog(null, "Not yet Implemented!", "Kamusi Desktop",
                JOptionPane.WARNING_MESSAGE);
    }
}
