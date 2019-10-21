package edu.gladstone.hts.microscope;

import edu.gladstone.hts.MicroscopeControlPlugin;
import mmcorej.CMMCore;
//import org.micromanager.api.ScriptInterface;
import org.micromanager.Studio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtendStageRunnable implements Runnable {
   //private ScriptInterface gui;
    private Studio gui;
    private CMMCore core_;
    private String stage;
    final Logger logger = LoggerFactory.getLogger(ExtendStageRunnable.class);

    public ExtendStageRunnable(Studio studio) {
        gui = studio;
        core_ = gui.core();
        stage = core_.getXYStageDevice();
    }

    @Override
    public void run() {
        MicroscopeStatus result = Extend();
        MicroscopeControlPlugin.Status = result;
    }

    private MicroscopeStatus Extend() {

        try {
            core_.setTimeoutMs(60000);
            int currentTimeout = core_.getTimeoutMs();
            logger.trace("Moving objective to bottom position");
            //gui.setPosition(0);
            core_.setPosition("TIZDrive",100); // avoid setting the stage to zero to avoid problems
            core_.waitForDevice("TIZDrive");
            logger.trace("Moving stage to front left position");
            core_.setXYPosition(stage, 300000, -300000); // this will move the stage to front left regardless of where (0,0) is set
            //core_.setXYPosition(stage,0,0);
            core_.waitForDevice(stage);
            logger.trace("Stage finished moving to front left position");
            core_.setTimeoutMs(currentTimeout);
        } catch (Exception e) {
            logger.error("Error Extending Stage");
            e.printStackTrace();
            return MicroscopeStatus.Error;
        }
        return MicroscopeStatus.Okay;
    }

}
