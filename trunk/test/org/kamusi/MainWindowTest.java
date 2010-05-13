/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kamusi;

import javax.swing.event.TableModelEvent;
import junit.framework.TestCase;

/**
 *
 * @author arthur
 */
public class MainWindowTest extends TestCase
{

    public void testDisplayProgressBar()
    {
        Object[] messageArguments =
        {
            10,
            100,
            String.valueOf((10 * 100) / 100)
        };

        String result = MessageLocalizer.formatMessage("download_progress", messageArguments);
        String expResult = "Updating database. Downloaded 10 out of 100 bytes ( 10 % )";
        assertEquals(expResult, result);
    }
}
