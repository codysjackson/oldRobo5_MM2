package edu.gladstone.hts.microscope;

import mmcorej.CMMCore;
import mmcorej.CharVector;
//import org.micromanager.api.ScriptInterface;
import org.micromanager.Studio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StageLatchController {
   // private ScriptInterface gui;
    private CMMCore core_;
    private String port = "COM6";
    private String commandTerminator = "\r";
    private String answerTerminator = "\n";
    final Logger logger = LoggerFactory.getLogger(StageLatchController.class);
    private Studio gui;
    public StageLatchController(Studio studio) {
        gui = studio;
        core_ = gui.core();
    }

    private String tellStage(String command) {
        try {
            core_.setSerialPortCommand(port, command, commandTerminator);
            String answer = core_.getSerialPortAnswer(port, answerTerminator);
            logger.info("locked the stage");
            return answer;
        } catch (Exception e) {
            logger.error("Unable to send command: {}", command);
            logger.error(e.getMessage());
            return "Error";
        }
    }

    public void LockStage() {
    	String dutyCycle = "SECURE Y=100";
    	String dutyCycleAnswer = tellStage(dutyCycle);
    	core_.sleep(100);
    	logger.debug("Stage latch Duty Cycle set", dutyCycleAnswer);
        String command = "SECURE X=0"; //can be a number between 0 and 1
        logger.info("Closing stage latch");
        String answer = tellStage(command);
        core_.sleep(100);
        logger.info("Stage latch closed: {}", answer); // Is this true if we don't wait on device?
    }

    public void UnlockStage() {
    	String timeoutDisable = "SECURE F=0";
    	String timeoutDisableAnswer = tellStage(timeoutDisable);
    	logger.info("Disabled the auto-locking feature: " + timeoutDisableAnswer);
        String command = "SECURE X=1.0";
        logger.info("Opening stage latch");
        String answer = tellStage(command);
        core_.sleep(100);
        logger.info("Stage latch opened: {}", answer); // Is this true if we don't wait on device?
    }
}
