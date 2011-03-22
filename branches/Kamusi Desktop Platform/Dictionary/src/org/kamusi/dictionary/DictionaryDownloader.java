/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kamusi.dictionary;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class DictionaryDownloader implements ActionListener
{

    public void actionPerformed(ActionEvent e)
    {
        Downloader d = new Downloader();
        d.run();
    }
}
