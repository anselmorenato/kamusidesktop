/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kamusi;

import javax.swing.JTable;
import junit.framework.TestCase;

/**
 *
 * @author arthur
 */
public class TranslatorTest extends TestCase
{

    public TranslatorTest(String testName)
    {
        super(testName);
    }

    /**
     * Test of getTable method, of class Translator.
     */
    public void testGetTable()
    {
        System.out.println("getTable");
        Translator instance = null;
        JTable expResult = null;
        JTable result = instance.getTable();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    /**
     * Test of getResultCount method, of class Translator.
     */
    public void testGetResultCount()
    {
        System.out.println("getResultCount");
        Translator instance = null;
        int expResult = 0;
        int result = instance.getResultCount();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }
}
