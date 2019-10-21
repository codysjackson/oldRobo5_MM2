package edu.gladstone.hts.microscope;


import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import mmcorej.CMMCore;
import mmcorej.StrVector;
import org.micromanager.Studio;
//import org.micromanager.api.ScriptInterface;
//import org.micromanager.utils.MMScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.awt.*;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.lang.Exception;
import java.util.ArrayList;

public class Core {
   // public ScriptInterface gui;
    public Studio gui;
    public CMMCore innerCore;
    private String stage;
    final Logger logger = LoggerFactory.getLogger(Core.class);
    private  VF5Contoller vf5;

    public Core(Studio studio) {
        gui = studio;
        innerCore = studio.core();
        stage = innerCore.getXYStageDevice();
        innerCore.setTimeoutMs(120000); // 120 second timeout to make sure that the stage has sufficient time to travel
        vf5 = new VF5Contoller(this);
    }

    public void initializeExperimentalParameters() {
    	try{
            // cycle through several parameter sets to make sure everything is correctly initialized
            logger.debug("changing to Epi_DAPI");
            innerCore.setConfig("Channels","Epi_DAPI");
            innerCore.waitForConfig("Channels","Epi_DAPI");
            logger.debug("Changing to Brightfield");
            innerCore.setConfig("Channels","Brightfield");
            innerCore.waitForConfig("Channels","Brightfield");
            logger.debug("Changing to Confocal_GFP_Cy5");
            innerCore.setConfig("Channels","Confocal_GFP_Cy5");
            innerCore.waitForConfig("Channels","Confocal_GFP_Cy5");
    	}catch(Exception e){
    		logger.error("Unable to initialize micro-manager settings");
    		logger.error(e.getMessage());
    	}
        //resetExperimentalParameters(); // reset all baseMode parameters
    }

    public void resetExperimentalParameters() {
        try {
            innerCore.setConfig("BaseMode", "reset");
            innerCore.waitForConfig("BaseMode", "reset");
            innerCore.setConfig("BaseMode", "init");
            innerCore.waitForConfig("BaseMode", "init");
            logger.info("Setting autoshutter");
            innerCore.setAutoShutter(true);
            setImageSynchro();
        }catch (Exception e){
            logger.error("Unable to reset and initialize : "+ e);
        }
    }
    
    public void prepareForImaging() {
        resetExperimentalParameters();
    	try{
            innerCore.setAutoShutter(true);
    		//innerCore.setProperty("CSUW1-Shutter", "State", "Open");
    	}catch(Exception e){
    		logger.error("Error in prepareForImaging");
    		logger.error(e.getMessage(), e);
    	}
    }

    public long[] getCameraPixels() {
        logger.debug("Getting camera pixels");
        long[] pixels = new long[2];
        innerCore.getCameraDevice();
        logger.debug("Camera being used: " + innerCore.getCameraDevice());
        try {
            pixels[0] = innerCore.getImageWidth();
            pixels[1] = innerCore.getImageHeight();
            logger.debug("reporting back that sensor size is:" + pixels[0] + ","+ pixels[1]);
        } catch (Exception e) {
            logger.trace("could not get image height and width: " + e);
        }
        return pixels;
    }

    public void setImageSynchro(){
        try {
            //innerCore.assignImageSynchro(innerCore.getCameraDevice());
            innerCore.assignImageSynchro(innerCore.getXYStageDevice());
            innerCore.assignImageSynchro(innerCore.getAutoFocusDevice());
            innerCore.assignImageSynchro(innerCore.getDeviceName("ZStage"));
        }catch(Exception e){logger.trace("Error while setting up Image synchro: "+ e );}
    }

    public void waitForImageSynchro(){
        try{
            innerCore.waitForImageSynchro();
        }catch(Exception e){
            logger.error("Exception caught while waiting for image synchro: "+e.getMessage());
        }
    }

    public mmcorej.Configuration getSystemStateCache(){
        mmcorej.Configuration conf=null;
        try{
            conf = innerCore.getSystemStateCache();
        }catch(Exception e){
            logger.error("Exception caught while retrieving system state cache: "+e);
        }
        return conf;
    }

    public void updateSystemStateCache(){
        try{
            innerCore.updateSystemStateCache();
        }catch(Exception e){
            logger.error("Exception caught while updating system state cache: "+e.getMessage());
        }
    }

    public ArrayList<String> CreateConfigList(String inputConfig){
        ArrayList<String> channelsList = new ArrayList<String>();
        for (String config: inputConfig.split(";") ) {
            if (config.contains(":")) {
                String[] temp = config.split(":");
                // determine if the config being called is for the VF5
                if (temp.length == 3 && Integer.parseInt(temp[2]) < 250) { // if the step size is larger than 390 this is not a valid linear step size
                    String min = temp[0];
                    String max = temp[1];
                    String step = temp[2];
                    //VF5Contoller vf5 = new VF5Contoller();
                    channelsList.addAll(vf5.CreateConfigArray(min, max, step));
                }else{
                    channelsList.addAll(vf5.CreateConfigArray(temp));
                }
            }else if(config.contains("Epi")|| config.contains("Confocal"))  {
                channelsList.add(config);
            }
        }
        return channelsList;
    }

    public void setChannelConfig(String config){
        // set the Imaging Channel configuration
        //check for Robo5 automated filters
        if(config.contains("Automated")){
            String[] temp = config.split("-");
            try {
                innerCore.setConfig("Channels",temp[0]+"-"+temp[1]);
                innerCore.setProperty("Filter-0", "VF-5-Wavelength", Integer.parseInt(temp[2]));
                //vf5.SetWavelength(Integer.parseInt(temp[2]));
            }catch(Exception e){
                logger.error("Could not change the automated filter: " +e);
                throw new RuntimeException("Invalid wavelength recieved");
            }
        }
        else {
            try {
                innerCore.setConfig("Channels", config);
                innerCore.waitForConfig("Channels", config);
            } catch (Exception e) {
                logger.error("Could not switch to configuration: " + e);
                throw new RuntimeException("Invalid channel configuration requested:" + e);

            }
        }
    }

    public void setObjective(String objective) {
        try {
            innerCore.setConfig("Objective", objective);
            innerCore.waitForDevice("TINosePiece");
        } catch (Exception e) {
            logger.error("Could not set objective to : " + objective + ":" + e);
            //throw new RuntimeException("unable to set objective");
        }
    }

    public double getPixelConfig(){
        double umPx=0;
        try{
            innerCore.getCurrentPixelSizeConfig();
            umPx = innerCore.getPixelSizeUm();
            logger.info("Set umPx to: "+ umPx);
        }catch(Exception e){
            logger.error("unable to grab pixel size: "+ e);
        }
        return umPx;
    }

    public void setExposure(double exp){
        try{
            innerCore.setExposure(exp);
            String cam = innerCore.getCameraDevice();
            if (cam.contains("Multi")){
                for (int i=1; i<=4;i++) {
                    if (cam.contains("Zyla")) {
                        String mc = innerCore.getProperty("Multi Camera", "Physical Camera " + i);
                        innerCore.setProperty(mc, "FrameRate", 1000);
                    }
                }
            }else if (cam.contains("Zyla")){
                innerCore.setProperty(cam,"FrameRate",1000);
            }else{
                logger.debug("EMCCD does not ask for frame rate.");
            }

        }catch(Exception e){
            logger.error("Could not set Camera Exposure+ "  + e);
        }
        //startAcquisition(exp);
    }

    public void startAcquisition(Double exp){
        try{
            innerCore.startContinuousSequenceAcquisition(exp);
        }catch(Exception e){}
    }

    public void stopAcquisition(){
        try {
            innerCore.stopSequenceAcquisition();
        }catch(Exception e){}
    }

    public boolean flipImg(){
        if( innerCore.getCameraDevice().contains("Zyla2")){
        return true;
        }else{
            return false;
        }
    }

    public void setExcitationIntensity(String intensity, String Scope)  {
        // create an array of the source intensities
        String[] percentIntensity = intensity.split(":");
        // set the intensities
        if (Scope.equals("Robo3")) {
            logger.debug("Hello, Robo3.");
            // LambdaXL does not currently allow changing intensities controlled by uManager
        }
        if (Scope.equals("Robo5")) {
            logger.debug("Hello, Robo5.");
            try {

                StrVector devices = innerCore.getLoadedDevices();
                System.out.println("Device status:");
                for (int i=0; i<devices.size(); i++) {
                    System.out.println(devices.get(i));
// list device properties
                    StrVector properties = innerCore.getDevicePropertyNames(devices.get(i));
                    for (int j = 0; j < properties.size(); j++) {
                        System.out.println(" " + properties.get(j) + " = "
                                + innerCore.getProperty(devices.get(i), properties.get(j)));
                        StrVector values = innerCore.getAllowedPropertyValues(devices.get(i),
                                properties.get(j));
                        for (int k = 0; k < values.size(); k++) {
                            System.out.println(" " + values.get(k));
                        }
                    }
                }
                innerCore.setProperty("Spectra", "White_Level", 100);
                innerCore.setProperty("Spectra", "White_Enable", 1);
            } catch (Exception e) {
                logger.error("Could not set excitation intensities: " + e);
            }
        }
        if (Scope.equals("Robo4")) {
            logger.debug("Hello, Robo4.");
            try {
                // spectraX
                innerCore.setProperty("Spectra", "Violet_Level", percentIntensity[0]);
                logger.info("Setting Violet to:" + percentIntensity[0]);
                innerCore.setProperty("Spectra", "Blue_Level", percentIntensity[1]);
                logger.info("Setting Blue to:" + percentIntensity[1]);
                innerCore.setProperty("Spectra", "Cyan_Level", percentIntensity[2]);
                logger.info("Setting Cyan to:" + percentIntensity[2]);
                innerCore.setProperty("Spectra", "Teal_Level", percentIntensity[3]);
                logger.info("Setting Teal to:" + percentIntensity[3]);
                innerCore.setProperty("Spectra", "Green_Level", percentIntensity[4]);
                logger.info("Setting Green to:" + percentIntensity[4]);
                innerCore.setProperty("Spectra", "Red_Level", percentIntensity[5]);
                logger.info("Setting Red to:" + percentIntensity[5]);
                if(Scope.equals("Robo4")) {
                    // LMM5
                    innerCore.setProperty("LMM5-Hub", "Transmission (%) 405nm-5", percentIntensity[6]);
                    innerCore.setProperty("LMM5-Hub", "Transmission (%) 447nm-6", percentIntensity[7]);
                    innerCore.setProperty("LMM5-Hub", "Transmission (%) 488nm-7", percentIntensity[8]);
                    innerCore.setProperty("LMM5-Hub", "Transmission (%) 516nm-2", percentIntensity[9]);
                    innerCore.setProperty("LMM5-Hub", "Transmission (%) 561nm-4", percentIntensity[10]);
                    innerCore.setProperty("LMM5-Hub", "Transmission (%) 642nm-3", percentIntensity[11]);
                }
            } catch (Exception e) {
                logger.error("Could not set excitation intensities: " + e);
            }
        }
    }

    public void moveRelativeXY(Point2D.Double coordinates) {
        try {
            innerCore.setRelativeXYPosition(stage, coordinates.getX(), coordinates.getY());
        } catch (Exception e) {
            String output = String.format(
                    "Error moving to relative position %f, %f", coordinates.getX(), coordinates.getY());
            logger.error(output);
            logger.error(e.getMessage());
        }
    }

    public void moveRelativeXYAndWaitForStage(Point2D.Double position) {
        double x = position.getX();
        double y = position.getY();
        try {
            innerCore.setRelativeXYPosition(stage, x, y);
            innerCore.waitForSystem();
        } catch (Exception e) {
            String output = String.format("Error moving to position %f, %f", x, y);
            logger.error(output, e);
        }
    }

    public void moveXYAndWaitForStage(Point2D.Double position) {
        double x = position.getX();
        double y = position.getY();
        try {
        	stage = innerCore.getXYStageDevice();
            innerCore.setXYPosition(stage, x, y);
            innerCore.waitForDevice(stage);
        } catch (Exception e) {
            String output = String.format("Error moving to position %f, %f", x,
                    y);
            logger.error(output);
            logger.error(e.getMessage());
        }
    }

    public Point2D.Double getXYPosition() {
        Point2D.Double position = new Point2D.Double(); // Initially set to 0,0
        try {
            double x = innerCore.getXPosition(stage);
            double y = innerCore.getYPosition(stage);
            position.setLocation(x, y);
        } catch (Exception e) {
            logger.error("Error retrieving current position");
            logger.error(e.getMessage(), e);
        }        
        return position;
    }

    public void waitForStage() {
        try {
            innerCore.waitForDevice(stage);
        } catch (Exception e) {
            logger.error("Error waiting for stage");
            logger.error(e.getMessage(), e);
        }
    }

    public void setTimeoutMs(int timeout) {
        innerCore.setTimeoutMs(timeout);
    }

    public void homeStage() {        
        try {
            innerCore.home(stage);
        } catch (Exception e) {
            logger.error("Error moving stage home");
            logger.error(e.getMessage(), e);
        }
    }
    
    public void disableAutoShutter(){
    	innerCore.setAutoShutter(false);
    }

    public void enableAutoShutter(){
        innerCore.setAutoShutter(true);
    }
    
    public void openShutter(){
    	try{innerCore.setShutterOpen(true);}
    	catch(Exception e){logger.error("could not open the shutter");}
    	logger.info("Opened the shutter");
    }
    
    public void closeShutter(){
    	try{innerCore.setShutterOpen(false);}
    	catch(Exception e){logger.error("could not close the shutter");}
    	logger.info("Closed the shutter");
    }
    
    public void waitForDevice(String device) {
    	try {
            innerCore.waitForDevice(device);
        } catch (Exception e) {
            logger.error("Error waiting for " + device + " to set");
            logger.error(e.getMessage());
        }
    }

    public void setAutoFocusDevice(String device) {
        try {
            innerCore.setAutoFocusDevice(device);
        } catch (Exception e) {
            String output = "Error setting shutter " + device;
            logger.error(output);
            logger.error(e.getMessage());
            throw new RuntimeException(output, e.getCause());
        }
    }

    public void setPFSOffset(double offset) {
        String offsetString = Double.toString(offset);
        try {
            innerCore.setProperty("TIPFSOffset", "Position", offsetString);
            innerCore.waitForDevice("TIPFSOffset");
        } catch (Exception e) {
            String output = "Error setting PFS Offset " + offsetString;
            logger.error(output);
            logger.error(e.getMessage());
            throw new RuntimeException(output, e.getCause());
        }

    }

    public void enablePFS() {
        try {
            homePiezoStage();
            waitForDevice("ZStage");
            innerCore.enableContinuousFocus(true);
            waitForPFS();
        } catch (Exception e) {
            String output = "Error engaging PFS";
            logger.error(output);
            logger.error(e.getMessage());
        }
    }

    public void disablePFS() {
        try {
            innerCore.enableContinuousFocus(false);
        } catch (Exception e) {
            String output = "Error disengaging PFS";
            logger.error(output);
            logger.error(e.getMessage());
        }
    }

    public void waitForPFS(){
        sleep(100);
        boolean waiting = true;
        String status = "";
        double tiZHeight=0.0;
        int checkLimit = 0;
        while (waiting == true || checkLimit<50) {
            logger.debug("waiting for pfs");
            checkLimit+=1;
            try {
                status = innerCore.getProperty("TIPFSStatus", "Status");
                tiZHeight = innerCore.getPosition("TIZDrive");
                logger.info("TIPFSStatus: " + status);
                sleep(300);
                if (checkLimit%20==0 && status.contains("Within")) {
                    // if check limit is reached and the system is "within range"
                    // we will just perturb the system by turning enabling the focus
                    innerCore.enableContinuousFocus(true);
                    //wait for the system to Lock in focus
                    sleep(300);
                }
                if (status.equalsIgnoreCase("Locked in focus")){
                    waiting = false;
                    logger.info("PFS WaitingStatus: "+waiting);
                    break;
                }else if (status.equals(null) || status.equals(" ")){
                    throw new RuntimeException("Invalid PFS status given");
                }else if (status.contains("failed")){
                    //if the focus lock fails escape the loop and move to next well
                    break;
                }
                else if (tiZHeight>8500){
                    //todo this will need to change to reflect the different heights of plates
                    logger.error("Skipping well due to invalid plate height");
                    //sreduce height of the stage and stop waiting
                    waiting=false;
                    innerCore.setRelativePosition("TIZDrive",-1000);
                }
            } catch (Exception e) {
                logger.info("Did not wait for PFS");
                throw new RuntimeException("PFS did not lock ");
            }
        }
        try{innerCore.waitForSystem();}
        catch(Exception e){logger.error("Did not properly wait for system: "+ e);}
    }

    public boolean isPFSLocked(){
        boolean locked;
        try {
            locked = innerCore.isContinuousFocusLocked();
        }catch(Exception e){
            logger.error("could not check if PFS is locked: "+e);
            locked = false;
        }
        return locked;
    }
    
    public double searchForPFSHeight(int plateHeight){
    	//Moves the objective vertically until PFSStatus reports focus range is found
        double height = plateHeight; // this should be lower than most plates and slides
        logger.info("Searching for bottom of plate at height: "+ plateHeight);
        try {
            String pfsStatus = innerCore.getProperty("TIPFSStatus", "Status");
            while(pfsStatus.contains("Out") && height < 8200){
                innerCore.setPosition("TIZDrive",height);
                innerCore.waitForDevice("TIZDrive");
                height+=15;
                sleep(20); // sleep and wait for the system to settle
                pfsStatus = innerCore.getProperty("TIPFSStatus","Status");
                if (pfsStatus.equals(null) || pfsStatus.equals(" ")){
                    throw new RuntimeException("Invalid PFS status detected");
                }
            }
        }catch(Exception e){
            logger.error("Error while serching for PFS height: "+ e);
            throw new RuntimeException("Error while searching for plate height: "+ e);
        }
        return height;
    }

    public void opticalSearchForFocus(int plateHeight) {
        double height = plateHeight;
        //set the initial height of the objective
        setZPosition(height);
        logger.info("Searching for bottom of plate at height: "+ plateHeight);
        double stepSize=100;
        double tempStdDev=0;
        int checkCounts=0;
        try {
            //get the camera's size
            innerCore.getCameraDevice();
            int PIXEL_WIDTH = (int)innerCore.getImageWidth();
            int PIXEL_HEIGHT = (int)innerCore.getImageHeight();
            // create a place to fill the image
            ShortProcessor sp = new ShortProcessor(PIXEL_HEIGHT,PIXEL_WIDTH);
            // start a continuous acquisition sequence
            innerCore.startContinuousSequenceAcquisition(0);
            while(stepSize>0.5 && height < 8200){
                //snap and image and gather the stats
                sp.setPixels(innerCore.popNextImage());
                double stdDev = sp.getStatistics().stdDev;
                if (stdDev>tempStdDev) {
                    tempStdDev = stdDev;
                    setRelativeZPosition(stepSize);
                    // if stdDev increases, reset the checks
                    checkCounts=0;
                }else if(tempStdDev==stdDev){
                    checkCounts+=1;
                    setRelativeZPosition(stepSize);
                }else{
                    // if stdDev drops, change direction and reduce stepSize
                    stepSize*=(-1)*0.5;
                    setRelativeZPosition(stepSize);
                    // if changing direction, reset the checks
                    checkCounts=0;
                }
                if (checkCounts>=10){
                    // if the number of checks is larger than 10, change direction and move half the checked distance for focus
                    setRelativeZPosition((-1)*checkCounts/2.0*stepSize);
                }
                height = getZPosition();
            }
        }catch(Exception e){
            logger.error("Error while serching for PFS height: "+ e);
            throw new RuntimeException("Error while searching for plate height: "+ e);
        }

    }

    public double[] createZStack(double zStep,double zThick){
        // assumes symetric about PFS plane
        // this will shorten certain valued thicknesses e.g. 5um Stack x 1um steps {-2.0, -1.0, 0.0, 1.0, 2.0}
        // this can be worked around by using other zStep values
        ArrayList<Double> tempStack = new ArrayList<Double>();
        double[] zStack;
        double zBase = 0.0;
        //collect steps to fill the array
        for (double z=zBase; z<=zThick/2.0;z+=zStep){
            tempStack.add(z);
        }
        // create the array of doubles twice the size of temp array minus the PFS offset position
        zStack = new double[2*tempStack.size()-1];
        //fill the array from max and min value to zero at center
        //use int j and k to fill from both sides of array
        int j = 0;
        int k = zStack.length-1;
        for(int i=tempStack.size()-1; i>=0; i--){
            zStack[j] = -tempStack.get(i);
            zStack[k] = tempStack.get(i);
            j++;
            k--;
        }
        return zStack;
    }

    public double getZPosition() {
        try {
            return innerCore.getPosition("TIZDrive");
        } catch (Exception e) {
            String output = "Error getting height information";
            logger.error(output);
            logger.error(e.getMessage());
            throw new RuntimeException(output, e.getCause());
        }

    }

    public void setZPosition(double height) {
        try {
            innerCore.setPosition("TIZDrive",height);
        } catch (Exception e) {
            String output = String.format("Error moving to height %f", height);
            logger.info(output);
            logger.info(e.getMessage());
            throw new RuntimeException(output, e.getCause());
        }
    }


    public void setRelativeZPosition(double stepSize){
        try{
            innerCore.setRelativePosition("TIZDrive", stepSize);
            innerCore.waitForDevice("TIZDrive");
        }catch(Exception e){
            logger.error("Could not set new ZPosition: "+e);
        }
    }

	public void homePiezoStage(){
        try{ setPiezoHeight(0);}
        catch(Exception e){ logger.info("No Piezo stage to home");}
	}
	
	public void setPiezoHeight(double h){
		try{
			innerCore.setPosition("ZStage", h);
			innerCore.waitForDevice("ZStage");
		}catch(Exception e){
			logger.info("Unable to set Piezo stage to " + h, e);
		}		
	}
	
	public double getPiezoHeight(){
		double h = -100000;
		try{
			h = innerCore.getPosition("ZStage");
		}catch(Exception e){
			logger.info("Unable to get Piezo stage height", e);
		}		
		return(h);
	}

	public void movePiezoStageRelative(double step) {
		try{
			innerCore.setRelativePosition("ZStage", step);
			innerCore.waitForDevice("ZStage");
			//logger.info("z : " + innerCore.getPosition("ZStage"));
		}catch(Exception e){
			logger.info("Unable to move piezo stage by " + step, e);
		}
		
	}

	public void startAcquisitionSequence(){
	    try{
	        innerCore.startContinuousSequenceAcquisition(0); //start the acquisition with no gaps between frames
        }catch(Exception e){
	        logger.error("Unable to initialize the sequence: "+ e);
	        throw new RuntimeException("unable to start sequence: "+ e);
        }
    }

    public void stopAcquisitionSequence(){
	    try{
	        innerCore.stopSequenceAcquisition();
        }catch(Exception e){
	        logger.error("Unable to stop the sequence acquisition");
	        throw new RuntimeException("Unable to stop the sequence acquisition" +e);
        }
    }

	public void sleep(int i){ //added to make an item sleep
		innerCore.sleep(i);
	}

}
