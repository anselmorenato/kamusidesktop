/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package englistswahiliitglossary;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

/**
 *
 * @author arthur
 */
public class MainWindow extends JFrame
{

    private JTextField wordField;
    private JLabel wordLabel;
    private JPanel inputPanel;
    private JPanel outputPanel;
    private JPanel statuspanel;
    private JPanel languagePanel;
    private JRadioButton swahiliToEnglish;
    private JRadioButton englishToSwahili;
    private ButtonGroup language;
    private JTable table;
    private ResultTable resultTable;
    private JScrollPane scroller;
    private JButton clearButton;

    public MainWindow()
    {
        super("Kamusi Project Desktop");
        initComponents();
        addActionListeners();
    }

    private void addActionListeners()
    {
        clearButton.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                clear();
            }
        });

        wordField.addKeyListener(new KeyAdapter()
        {

            public void keyReleased(KeyEvent e)
            {
                String word = wordField.getText().trim();

                if (e.isActionKey())
                {
                }
                else if (e.getKeyCode() == 8) // Backspace
                {
                    if (word.length() == 0) // Backspace
                    {
                        clear();
                    }
                    else
                    {
                        displayTable(word);
                    }
                }
                else
                {
                    displayTable(word);
                }
            }
        });
    }

    private void initComponents()
    {
        wordLabel = new JLabel("Word", JLabel.LEFT);
        wordField = new JTextField(20);
        language = new ButtonGroup();
        swahiliToEnglish = new JRadioButton("Swa to Eng", false);
        englishToSwahili = new JRadioButton("Eng to Swa", true);
        language.add(swahiliToEnglish);
        language.add(englishToSwahili);
        languagePanel = new JPanel();
        languagePanel.setLayout(new GridLayout(0, 1));
        languagePanel.add(englishToSwahili);
        languagePanel.add(swahiliToEnglish);
        clearButton = new JButton("CLEAR");

        inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        inputPanel.add(languagePanel);
        inputPanel.add(wordLabel);
        inputPanel.add(wordField);
        inputPanel.add(clearButton);


        outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());

        setLayout(new BorderLayout());

        add(inputPanel, BorderLayout.NORTH);
        add(outputPanel, BorderLayout.CENTER);

        setSize(500, 300);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        setVisible(true);
        wordField.requestFocusInWindow();

    }

    private void clear()
    {
        wordField.setText("");
        outputPanel.removeAll();
        pack();
    }

    private void displayTable(String word)
    {
        String languageToTranslateFrom = "";

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

                resultTable = new ResultTable(languageToTranslateFrom, word);
                TableModel model = resultTable.getTableModel();

                JTable newTable = new JTable(model);

                JScrollPane scrollPane = new JScrollPane(newTable);

                outputPanel.add(scrollPane, BorderLayout.CENTER);

//                scroller.setVisible(true);
                pack();
            }
        }
        else
        {
            JOptionPane.showMessageDialog(null, "Please select the desired language first");
        }

    }
}