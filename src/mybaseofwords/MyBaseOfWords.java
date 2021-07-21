/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mybaseofwords;

import java.awt.EventQueue;
import javax.swing.JFrame;

/**
 *
 * @author TitarX
 */
public class MyBaseOfWords
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                MainForm mainForm=new MainForm();
                mainForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mainForm.setTitle("My base of words");
                mainForm.setVisible(true);
            }
        });
    }
}
