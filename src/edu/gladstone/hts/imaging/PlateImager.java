package edu.gladstone.hts.imaging;

import edu.gladstone.hts.MicroscopeControlPlugin;
import edu.gladstone.hts.microscope.Core;
import edu.gladstone.hts.microscope.MicroscopeStatus;
import edu.gladstone.hts.parameters.ExperimentDataBean;
import edu.gladstone.hts.parameters.FileData;
import edu.gladstone.hts.parameters.WellDataBean;
import org.micromanager.Studio;
//import org.micromanager.api.ScriptInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;
import java.io.File;

public class PlateImager implements Runnable {
    private final int LONG_DELAY = 40000;
    private final int NORMAL_DELAY = 11000;
    private final FileData fileData;
    private final WellDataBean[] wells;
    private Core htsCore;

    final Logger logger = LoggerFactory.getLogger(PlateImager.class);
    public Point2D.Double correction;
    private Point2D.Double centerA1;
    public ImageQC IQC;
    public static Metadata md;
    File f; // JSON metadata file

    public PlateImager(Studio studio, FileData experimentData) {
        htsCore = new Core(studio);
        this.fileData = experimentData;
        this.wells = experimentData.wells;
        //this.IQC = new ImageQC(htsCore);
        f= new File(wells[0].getSavepath()+"/"+wells[0].getBase().getName()+"_metadata.json");
        md = new Metadata();
    }

    public MicroscopeStatus ImagePlate() {

        // list all devices and properties

        // run experimental protocol
        try {
            htsCore.initializeExperimentalParameters();
        } catch (Exception e) {
            logger.error("Error initializing experiment");
            logger.error(e.getMessage());
            return MicroscopeStatus.Error;
        }
        try {
            alignAndGoToStartPoint();
        } catch (Exception e) {
            logger.error("Error finding fiduciary and going to initial position", e);
            return MicroscopeStatus.Error;
        }
        try {
            htsCore.prepareForImaging();
        } catch (Exception e) {
            logger.error("Error preparing for imaging", e);
            return MicroscopeStatus.Error;
        }
        try {
            imagePlate();            
        } catch (Exception e) {
            logger.error("Error imaging plate", e);
            return MicroscopeStatus.Error;
        }
        try{
            String timePoint = fileData.wells[0].getBase().getTimepoint();
            File fp = new File(fileData.wells[0].getSavepath() + File.separator + timePoint + "_done");
            fp.createNewFile();            
        }catch(Exception e){
        	logger.error("Error writing timepoint completion file", e);        	
        }
        try {
            htsCore.resetExperimentalParameters();
        } catch (RuntimeException e) {
            logger.error("Error reseting experimental parameters");
            logger.error(e.getMessage());
            return MicroscopeStatus.Error;
        }
        // read the experimental JSON file to memory
        logger.info("Opening the metadata file"+f.getAbsolutePath());
        md.readJSON(f);
        logger.info("Writing the metadata file"+f.getAbsolutePath());
        md.writeJSON(f);
        return MicroscopeStatus.Okay;

    }

    private void alignAndGoToStartPoint(){
        moveStageToHome();
        logger.info("Stage in home position: "+htsCore.getXYPosition());
        goToFiduciaryPoint();
        logger.info("Stage in fiduciary position: "+htsCore.getXYPosition());
        imageFiduciary();
        Fiduciary fd = new Fiduciary(this.fileData.fiduciary);
        this.correction = fd.findFiduciaryOffset();
        htsCore.moveRelativeXYAndWaitForStage(this.correction);
        logger.debug("Stage moved to fiduciary correction point:" + correction);
        setOriginFromFiduciary();
        goToStartPosition();
        logger.debug("Stage in starting position:" + htsCore.getXYPosition());
    }

    private void goToFiduciaryPoint() {
    	logger.info("Moving to the fiduciary point from" + htsCore.getXYPosition());
        htsCore.setTimeoutMs(LONG_DELAY);
        moveStageToFiduciaryPoint();
        htsCore.waitForStage();
        logger.info("Moved the Stage to the fiducairy point: " + htsCore.getXYPosition());
        htsCore.setTimeoutMs(NORMAL_DELAY);
    }

    private void moveStageToHome() {
    	htsCore.homeStage();
        // initialize the microscope while homing the stage
        htsCore.initializeExperimentalParameters();
        htsCore.waitForStage();
        logger.info("Home is: "+htsCore.getXYPosition());
    }

    private void moveStageToFiduciaryPoint() {
    	logger.info("Moving to fiduciary point from:" + htsCore.getXYPosition());
        Point2D.Double fiduciaryPoint = new Point2D.Double(this.fileData.plate.getX_DISPLACEMENT_HOME_TO_FIDUCIARY(),
                this.fileData.plate.getY_DISPLACEMENT_HOME_TO_FIDUCIARY());
        htsCore.moveRelativeXYAndWaitForStage(fiduciaryPoint);
    }

    public void imageFiduciary() {
        // Get initial position so you can return to it
    	Point2D.Double initialXYPosition = new Point2D.Double(this.fileData.plate.getX_DISPLACEMENT_HOME_TO_FIDUCIARY(),
    			this.fileData.plate.getY_DISPLACEMENT_HOME_TO_FIDUCIARY());
        logger.debug("Fiduciary imaging : initial position : (" + initialXYPosition.x + ", " + initialXYPosition.y + ")");
        WellImager fidImager = new WellImager(fileData.fiduciary, fileData, htsCore, IQC,true); //boolean for fiducial
        try {
            fidImager.AcquireFiducial();
        }catch(Exception e){
            logger.error("Error while imaging the fiducial: "+ e);
        }
        htsCore.moveXYAndWaitForStage(initialXYPosition);
    }

    public void setOriginFromFiduciary() {
    	logger.info("Stage is at position: "+htsCore.getXYPosition());
    	htsCore.moveRelativeXYAndWaitForStage(new Point2D.Double(this.fileData.plate.getX_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(),
    			this.fileData.plate.getY_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1()));
    	logger.info("Now at: " + htsCore.getXYPosition());
    	logger.info("Setting centerA1 from "+htsCore.getXYPosition());
    	centerA1=htsCore.getXYPosition(); //origin in absolute coordinates
    }

    private void goToStartPosition() {
    	logger.info("Moving to start position...");
    	//Point2D.Double centerOfWell = new Point2D.Double(wells[0].getCenter().getX() + centerA1.getX(), wells[0].getCenter().getY() + centerA1.getY());
        //htsCore.moveXYAndWaitForStage(centerOfWell);
        logger.info("Now at location : " + htsCore.getXYPosition()+","+htsCore.getZPosition());
    }

    private void imagePlate() {
        // Change to the imaging objective
        htsCore.setObjective(wells[0].getObjective());
        // Start the Image QC class before imaging the plate
        IQC.SetSavePathAndOpen(wells[0]);
        try {
            htsCore.innerCore.setProperty("Spectra", "State", "1"); //always allow for warmup of the light sources
        }catch(Exception e){
            logger.info("Couldn't turn on the SpectraX to warm up");
        }

        // Iterate over each well :
        for (WellDataBean well : wells) {
            logger.info("Center of well is located at: {}", well.getCenter());
            Point2D.Double center = new Point2D.Double((1)* well.getCenter().getX() + centerA1.getX(), (1)* well.getCenter().getY() + centerA1.getY()); // negative for Robo5
            htsCore.moveXYAndWaitForStage(center); // this should send the stage to the center of each well in the series
            logger.info("Current location: " + htsCore.getXYPosition());
            // home the zStage
            try {htsCore.homePiezoStage(); // always set piezo stage to 0 before imaging
            }catch(Exception e){ logger.info("No Zstage to change position with");}

            WellImager imager = new WellImager(well, fileData, htsCore, IQC);
            imager.ImageWell();

            //move back to the center of the well
            htsCore.moveXYAndWaitForStage(center);
        	logger.info("Now at: " + htsCore.getXYPosition());

            logger.info("Completed imaging well : " + well.getWell());

        }

        // send email to investigator
        // todo add the email class from Templatemaker

        IQC.close(); //close the CSV file
        htsCore.closeShutter();
        System.gc(); //run garbage collection at the end of a plate
    }

    @Override
    public void run() {
        MicroscopeControlPlugin.Status = ImagePlate();
    }
}
