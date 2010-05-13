/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kamusi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import junit.framework.TestCase;

/**
 *
 * @author arthur
 */
public class SynchronizerTest extends TestCase
{

    public SynchronizerTest(String testName)
    {
        super(testName);
    }

    /**
     * Test of synchronize method, of class Synchronizer.
     * @throws IOException
     */
    public void testFetchUpdate() throws IOException
    {
        System.out.println("fetchUpdate");
        boolean isEditorsVersion = false;
        Synchronizer instance = new Synchronizer();
        instance.synchronize(isEditorsVersion);
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSizeOfUpdate method, of class Synchronizer.
     * @throws UnknownHostException
     * @throws MalformedURLException
     * @throws IOException 
     */
    public void testGetSizeOfUpdate() throws UnknownHostException, MalformedURLException, IOException
    {
        System.out.println("getSizeOfUpdate");
        Synchronizer instance = new Synchronizer();
        long expResult = 0L;
        long result = instance.getSizeOfUpdate();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    /**
     * Test of canSync method, of class Synchronizer.
     */
    public void testCanSync()
    {
        System.out.println("canSync");
        Synchronizer instance = new Synchronizer();
        boolean expResult = false;
        boolean result = instance.canSync();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }
}
