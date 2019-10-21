package edu.gladstone.hts;

import edu.gladstone.hts.parameters.FileData;
import edu.gladstone.hts.imaging.PlateImager;
import edu.gladstone.hts.microscope.MicroscopeCommandHandler;
import edu.gladstone.hts.microscope.MicroscopeStatus;
import edu.gladstone.hts.microscope.StageController;
import edu.gladstone.hts.server.CommandServer;

//import org.micromanager.api.MMPlugin;
//import org.micromanager.api.ScriptInterface;

import org.micromanager.LogManager;
import org.micromanager.MenuPlugin;
import org.micromanager.Studio;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.FileNotFoundException;

@Plugin(type=MenuPlugin.class)
public class MicroscopeControlPlugin implements MenuPlugin, SciJavaPlugin,
        MicroscopeCommandHandler {

    private edu.gladstone.hts.microscope.StageController StageController;
    private edu.gladstone.hts.server.CommandServer CommandServer;
    //private ScriptInterface app;
    private Studio studio_;
    public static String menuName = "HTS Plugin robo5";
    public static String tooltipDescription = "Plugin for longitudinal automated microscopy";
    private Thread thread;

    private LogManager logManager;
    public static MicroscopeStatus Status;

    final Logger logger = LoggerFactory.getLogger(MicroscopeControlPlugin.class);

    //@Override
    //public void configurationChanged() {
    // TODO Auto-generated method stub
//
    //}
/*
    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }
*/
    @Override
    public void setContext(Studio studio) {
        studio_ = studio;
    }

    @Override
    public void onPluginSelected() {
        int defaultPort = 5242; // selecting a different default port than Robo 3 to avoid any potential conflict
        Status = MicroscopeStatus.Okay;
        logger.info("Starting Command Server");


        try {
            CommandServer = new CommandServer();
            CommandServer.Launch(defaultPort, this);
            String message = "Command Server launched on port "
                    + Integer.toString(defaultPort);
            logger.info(message);
            //app.showMessage(message);
            //logManager.showMessage(message);
        } catch (Exception exception) {
            String message = "Error - Could not launch Command Server: "
                    + exception.getMessage();
            logger.error(message);
            // app.showMessage(message);
            // logManager.showMessage(message);
        }
        logger.trace("Creating stage controller");
        // StageController = new StageController(app);
        StageController = new StageController(studio_);
        logger.info("Moving Stage into default postion (Loading position)");
        StageController.ExtendStage();
        final ImageTemplateChooser itc = new ImageTemplateChooser(this);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                itc.createAndShowGUI();
            }
        });
    }


    @Override
    public String getName() {
        return menuName;
    }

    @Override
    public String getHelpText() {
        return tooltipDescription;
    }

    @Override
    public String getVersion() {
        return "Beta";
    }
/*
    @Override
    public void show() {
        @SuppressWarnings("unused")
        String ig = "HTS Plugin";

    }

    @Override
    public void setApp(ScriptInterface app) {    	
        this.app = app;
        int defaultPort = 5242; // selecting a different default port than Robo 3 to avoid any potential conflict 
        Status = MicroscopeStatus.Okay;
        logger.info("Starting Command Server");

        try {
            CommandServer = new CommandServer();
            CommandServer.Launch(defaultPort, this);
            String message = "Command Server launched on port "
                    + Integer.toString(defaultPort);
            logger.info(message);
            app.showMessage(message);
        } catch (Exception exception) {
            String message = "Error - Could not launch Command Server: "
                    + exception.getMessage();
            logger.error(message);
            app.showMessage(message);
        }
        logger.trace("Creating stage controller");
        StageController = new StageController(app);
        logger.info("Moving Stage into default postion (Loading position)");
        StageController.ExtendStage();
        final ImageTemplateChooser itc = new ImageTemplateChooser(this);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);                
                itc.createAndShowGUI();
            }
        });
    }
*/

    @Override
    public void HandleRetractCommand() {
        Status = MicroscopeStatus.Busy;
        Status = StageController.RetractStage();
    }

    @Override
    public void HandleExtendCommand() {
        Status = MicroscopeStatus.Busy;
        logger.info("Command to Extend Stage");
        StageController.ExtendStage();
    }

    @Override
    public void HandleImageCommand(String schedulerData) {
        // Get parameters from experiment via info passed in from GBG
        FileData experimentData;
        try {
            // SetUpExperiment surround with try/catch for no imaging template found  or parameters off
            experimentData = new FileData(schedulerData);
        } catch (FileNotFoundException e) {
            logger.error("Unable to read Image Template file");
            logger.error(e.getMessage());
            Status = MicroscopeStatus.Error;
            return;
        } catch (IllegalArgumentException e) {
            logger.error("Unable to properly set parameters from Image Template file");
            logger.error(e.getMessage());
            e.printStackTrace(System.out);
            Status = MicroscopeStatus.Error;
            return;
        } catch (Exception e) {
            logger.error("Unexpected error when setting up imaging parameters");
            logger.error(e.getMessage(), e);
            Status = MicroscopeStatus.Error;
            return;
        }

        Status = MicroscopeStatus.Busy;
        // Pass plate, objective/objectivecamera, data, app into imaging instance
        logger.info("Filename:" + schedulerData);
        PlateImager ImageInstance = new PlateImager(studio_, experimentData);
        // Start thread
        Thread thread = new Thread(ImageInstance);
        thread.setPriority(java.lang.Thread.MAX_PRIORITY);
        thread.start();
    }

    @Override
    public String HandleGetStatusCommand() {
        logger.trace("returning status " + Status.toString());
        return Status.toString();

    }

    @Override
    public void HandleAbortCommand() {
        logger.trace("Aborting current run" + Status.toString());
        thread.interrupt();
        Status = MicroscopeStatus.Error; // make the status move to "Okay" to abort imaging
        return;
    }
    @Override
    public String getCopyright() {
        return "Gladstone, 2012. Author: Mike Ando";
    }


    @Override
    public String getSubMenu() {
        return "";
    }

}