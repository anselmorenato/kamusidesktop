/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kamusi;

import java.io.IOException;
import junit.framework.TestCase;

/**
 *
 * @author arthur
 */
public class RestorerTest extends TestCase
{

    public RestorerTest(String testName)
    {
        super(testName);
    }

    /**
     * Test of restore method, of class Restorer.
     */
    public void testRestore()
    {
        System.out.println("restore");
        Restorer instance = new Restorer();
        instance.restore();
        fail("The test case is a prototype.");
    }

    /**
     * Test of cancelUpdate method, of class Restorer.
     */
    public void testCancelUpdate()
    {
        System.out.println("cancelUpdate");
        Restorer instance = new Restorer();
        boolean expResult = false;
        boolean result = instance.cancelUpdate();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    /**
     * Test of restoreOriginal method, of class Restorer.
     */
    public void testRestoreOriginal()
    {
        System.out.println("restoreOriginal");
        Restorer instance = new Restorer();
        instance.restoreOriginal();
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSizeOfUpdate method, of class Restorer.
     * @throws IOException
     */
    public void testGetSizeOfUpdate() throws IOException
    {
        System.out.println("getSizeOfUpdate");
        Restorer instance = new Restorer();
        long expResult = 0L;
        long result = instance.getSizeOfUpdate();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSizeOfDatabase method, of class Restorer.
     */
    public void testGetSizeOfDatabase()
    {
        System.out.println("getSizeOfDatabase");
        Restorer instance = new Restorer();
        long expResult = 0L;
        long result = instance.getSizeOfDatabase();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    /**
     * Test of canRestore method, of class Restorer.
     */
    public void testCanRestore()
    {
        System.out.println("canRestore");
        Restorer instance = new Restorer();
        boolean expResult = false;
        boolean result = instance.canRestore();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }
}
