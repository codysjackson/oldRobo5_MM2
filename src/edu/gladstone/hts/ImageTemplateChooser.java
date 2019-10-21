package edu.gladstone.hts;

import edu.gladstone.hts.microscope.MicroscopeCommandHandler;
import edu.gladstone.hts.microscope.Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class ImageTemplateChooser extends JPanel
        implements ActionListener {
    private Core htsCore;
	private static final long serialVersionUID = 2923605649649923170L;
	static private final String newline = "\n";
    JButton openButton;
    JButton startButton;
    JButton abortButton;
    JTextArea log;
    JFileChooser fc;
    MicroscopeCommandHandler ch;
    String currentFile = "";
    final Logger logger = LoggerFactory.getLogger(ImageTemplateChooser.class);

    public ImageTemplateChooser(MicroscopeCommandHandler commandHandler) {
        super(new BorderLayout());
        ch = commandHandler;
        //Create the log first, because the action listeners
        //need to refer to it.        
        log = new JTextArea(5, 20);
        log.setMargin(new Insets(5, 5, 5, 5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        //Create a file chooser
        fc = new JFileChooser();
        fc.setDialogTitle("HTS Plugin");

        openButton = new JButton("Open a File...");
        startButton = new JButton("Start Run with Selected Template...");
        abortButton = new JButton("ABORT!!!!");
        openButton.addActionListener(this);
        startButton.addActionListener(this);
        abortButton.addActionListener(this);
        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
        buttonPanel.add(startButton);
        buttonPanel.add(abortButton);

        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {

        //Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(ImageTemplateChooser.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would open the file.
                log.append("Selecting: " + file.getName() + "." + newline);
                currentFile = file.getName();
            } else {
                log.append("Image Template selection cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        } else if (e.getSource() == startButton) {        	
            String fakeData = currentFile; //fill in!
            /* using fake data here because real data is received from Green Button Go. Fake data here will help in testing without scheduler and GBG */
            fakeData = "Image "  + fc.getSelectedFile().getAbsolutePath() + 
            		 	", Barcode = 1.00E+00, TimePoint = 0, Hour = 0, Name = TestPlate, DesiredStart = 11/01/2014 1:33:00 PM";
            log.append(fakeData);
            // expects
            //Image C:/Users/Mike Ando/Desktop/Imaging Templates/Mike/Demo/Four well demo Two Channel Different Gain.csv, Barcode = 1.00E+00, TimePoint = 0, Hour = 0, Name = TestPlate, DesiredStart = 9/16/2013 1:33:00 PM
            ch.HandleExtendCommand();
            waitUntilStatusOk();
            ch.HandleRetractCommand();
            waitUntilStatusOk();
            logger.info("Running HandleImageCommand()");
            ch.HandleImageCommand(fakeData);
            waitUntilStatusOk();
            ch.HandleExtendCommand();
            waitUntilStatusOk();
        } else if (e.getSource() == abortButton){
            try {
                htsCore.innerCore.unloadAllDevices();
            }catch(Exception ex){logger.info("Could not unload all devices: "+ ex);}
        }
    }

    private void waitUntilStatusOk() {
        while (ch.HandleGetStatusCommand().equalsIgnoreCase("busy")) {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                String message = "Error waiting for status";
                JOptionPane.showMessageDialog(null, message);
                logger.error(message);
            }
        }
        if (ch.HandleGetStatusCommand().equalsIgnoreCase("error")) {
            String message = "Error status during manual run";
            JOptionPane.showMessageDialog(null, message);
            logger.error(message);
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    public void createAndShowGUI(){
    	//Create and set up the window.
        JFrame frame = new JFrame("HTS Plugin");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Add content to the window.
        frame.add(new ImageTemplateChooser(ch));

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        
    }
}