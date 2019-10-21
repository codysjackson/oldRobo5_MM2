package edu.gladstone.hts.microscope;

import mmcorej.CMMCore;
//import org.micromanager.api.ScriptInterface;
import org.micromanager.Studio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StageController {

   // private ScriptInterface gui;
    private CMMCore core_;
    private String stage;
    private StageLatchController latch;
    final Logger logger = LoggerFactory.getLogger(StageController.class);
    private Studio gui;


    public StageController(Studio studio) {
        gui = studio;
        core_ = gui.core();
        stage = core_.getXYStageDevice();
        latch = new StageLatchController(studio);
    }

    public void ExtendStage() {
        latch.UnlockStage();
        ExtendStageRunnable Extender = new ExtendStageRunnable(gui);
        Thread thread = new Thread(Extender);
        thread.start();
    }

    public MicroscopeStatus RetractStage() {
        latch.LockStage();
        try {
            core_.setTimeoutMs(60000);
            int currentTimeout = core_.getTimeoutMs();
            //gui.setStagePosition(0);
            core_.setPosition("TIZDrive",100); // avoid setting the stage to zero to avoid problem
            core_.waitForDevice("TIZDrive");
            core_.home(stage);
            core_.waitForDevice(stage);
            core_.setTimeoutMs(currentTimeout);
        } catch (Exception e) {
            logger.error("Error Retracting Stage");
            e.printStackTrace();
            return MicroscopeStatus.Error;
        }
        return MicroscopeStatus.Okay;
    }
}
