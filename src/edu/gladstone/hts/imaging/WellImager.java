package edu.gladstone.hts.imaging;
/**
 * Created by elliot on 1/19/16.
 * This class is designed to simplify the old WellImager class and increase the number of accessible imaging parameters
 **/

import edu.gladstone.hts.microscope.*;
import edu.gladstone.hts.microscope.Image;
import edu.gladstone.hts.parameters.*;

import ij.IJ;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import ij.ImagePlus;
import ij.ImageStack;

import java.awt.geom.Point2D;
import java.lang.Object;
import java.util.ArrayList;
import java.util.GregorianCalendar;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.RuntimeErrorException;

public class WellImager {
    //Declare the variables of interest
    private final WellDataBean wDB;
    private final FileData fd;
    private final Core htsCore;
    private Image image;
    private ImageStack[] imgP;
    private ImageQC IQC;
    private Metadata md;
    final Logger logger = LoggerFactory.getLogger(WellImager.class);

    //well Parameters
    private Point2D.Double[] monArray; // montage array
    private Point2D.Double[] moveArray; // stage translation array
    private String[] arraySize;
    private String[] zStep, zThickness;
    private ArrayList<String> zStepArray;
    private ArrayList<String> zThicknessArray;
    private double[] zStack;
    private String[] pfs;
    private ArrayList<String> pfsArray;
    private String pfsPerTile;
    private ArrayList<String> configChannels;
    private String[] exposures;
    private ArrayList<String> expArray;
    private String[] timeLapse;
    private ArrayList<String> timelapseArray;
    private double interval; // wait time between images
    private int nImg; // number of images to burst
    private String intensities;
    private long[] camPx;
    private double umPx;
    private String objective;
    private Double overlap;
    private GregorianCalendar today = new GregorianCalendar();

    public WellImager(WellDataBean wellDataBean, FileData fd,  Core htsCore, ImageQC imageQC){
        // initialize the WellDataBean
        this.wDB = wellDataBean;
        // experimental data bean
        this.fd = fd;
        logger.debug(fd.plate.toString());
        // initialize the core
        this.htsCore = htsCore;
        // initialize the ImageQC class
        this.IQC = imageQC;
        // array dimensions
        arraySize = wDB.getArraySize().split("x");
        //initialize automated channel arrays
        pfsArray = new ArrayList<String>();
        expArray = new ArrayList<String>();
        zStepArray = new ArrayList<String>();
        zThicknessArray = new ArrayList<String>();
        timelapseArray = new ArrayList<String>();
        // pfs offsets
        pfs = wDB.getPerfectFocusOffset().split(";");
        // pfs on or off between tiles
        pfsPerTile = fd.getPFSPerTile();
        // get stack info
        zStep = wDB.getzStepSize().split(";");
        zThickness = wDB.getzHeight().split(";");
        //objective
        objective = wDB.getObjective();
        //overlap
        overlap = Double.parseDouble(wDB.getOverlap());
        //set the objective and get the pixel config
        htsCore.setObjective(objective);
        htsCore.searchForPFSHeight(fd.plate.getPLATE_HEIGHT());
        // get pixel sizes
        umPx = htsCore.getPixelConfig();
        // finish parsing config data
        configChannels = htsCore.CreateConfigList(wDB.getChannels());
        // exposures
        exposures = wDB.getExposures().split(";");
        for(int i=0;i<exposures.length;i++) {
            logger.debug("Extracted exposures: " + exposures[i]);
        }
        // bursting parameters
        timeLapse = wDB.getTimelapse().split(";");
        // set well excitation intensities
        intensities = wDB.getExcitationIntensity(); // this is a string which contains all excitation intensities regardless of channel
        htsCore.setExcitationIntensity(intensities , fd.getRobot());
        //add needed extra pfs, timelapse, and z-stack information, as needed
        int chanIndex = 0;
        for(int pfsIndex=0;pfsIndex<pfs.length;pfsIndex++) {
            logger.debug("Adding pfs, exposure, zStep, zThickness, and timelispe information from pfsIndex: "+pfsIndex);
            pfsArray.add(pfs[pfsIndex]);
            logger.debug("Added pfs: "+pfs[pfsIndex]);
            expArray.add(exposures[pfsIndex]);   // each pfs will need an exposure
            logger.debug("Added exposure: "+ exposures[pfsIndex]);
            zStepArray.add(zStep[pfsIndex]);
            logger.debug("Added zStep: "+ zStep[pfsIndex]);
            zThicknessArray.add(zThickness[pfsIndex]);
            logger.debug("Added zThickness: "+zThickness[pfsIndex]);
            timelapseArray.add(timeLapse[pfsIndex]);
            logger.debug("Added timelapse: "+timeLapse[pfsIndex]);
            if (chanIndex==configChannels.size()-1) {
                logger.debug("Reached end of configuration list");
                break;
            }
            else if(configChannels.get(chanIndex+1).contains("Automated") && configChannels.get(chanIndex).contains("Automated")){
                logger.debug("added automated channel to the list");
                pfsIndex--;
            }
            chanIndex++;
        }
        logger.info("Using PFSOffsets: "+pfsArray);
        logger.info("Using exposures: "+expArray);
        logger.info("Using zSteps: "+zStepArray);
        logger.info("Using zThickness: "+zThicknessArray);
        logger.info("Using timelapse: "+timelapseArray);
        // camera parameters
        camPx = htsCore.getCameraPixels();
    }

    public WellImager(WellDataBean wellDataBean, FileData fd, Core htsCore, ImageQC imageQC, boolean fid) {
        // initialize the WellDataBean
        this.wDB = wellDataBean;
        // experimental data
        this.fd = fd;
        // initialize the core
        this.htsCore = htsCore;
        // initialize the ImageQC class
        this.IQC = imageQC;
    }

    private void SetImagingParameters(int i) {
        // enable AutoShutter
        htsCore.enableAutoShutter();
        // make sure PFS is enabled
        htsCore.setPFSOffset(Double.parseDouble(pfsArray.get(i)));
        if(!htsCore.isPFSLocked()) {
            htsCore.enablePFS();
        }
        //get the imaging array
        if (wDB.getIsPositionFirst().equals("TRUE")) {
            TranslationArray(wDB.getArray());
        }
        // set the channel configuration
        logger.debug("Setting the channel " + configChannels.get(i));
        htsCore.setChannelConfig(configChannels.get(i));
        //setExposures
        logger.debug("Setting the exposure");
        htsCore.setExposure(Double.parseDouble(expArray.get(i)));
        // get Z-stack information and create the array
        logger.debug("making the z-Stack");
        // create the z-steps
        zStack = htsCore.createZStack(Double.parseDouble(zStepArray.get(i)), Double.parseDouble(zThicknessArray.get(i)));
        // burst parameters
        logger.debug("Parse the burst information");
        String[] temp = timelapseArray.get(i).split(":");
        nImg = Integer.parseInt(temp[0]);
        interval = Double.parseDouble(temp[1]);
        htsCore.updateSystemStateCache();
    }

    private boolean isStackBurst() {
        // if creating Z Stacks or quick timelapse kill PFS and autoshutter
        logger.debug("Zstack:" + zStack.length);
        logger.debug("nImages: "+ nImg);
        logger.debug("Interval: "+ interval);
        if (zStack.length > 1) {
            return true;
        }else if((nImg > 1 && interval < 100)){
            return true;
        }else {
            return false;
        }
    }

    private void disableShutterFocus(){
        htsCore.disablePFS();
        htsCore.disableAutoShutter();
        htsCore.openShutter();
    }

    private void enableShutterFocus(){
        htsCore.enablePFS();
        htsCore.enableAutoShutter();
        htsCore.closeShutter();
    }

    public void ImageWell() {
        logger.debug("Starting to image the well...");
        // assign devices needing to be synchronized for imaging
        htsCore.setImageSynchro();
        // get well array size
        //monArray = wDB.getMontageOffsets(arraySize, camPx, false);
        logger.info("Montage offsets: {}", new Object[]{moveArray});
        //todo create an imagePlus to apply the montage
        // engage PFS if not already locked
        htsCore.enablePFS();
        if (wDB.getIsPositionFirst().contains("FALSE")) {
            TranslationArray(wDB.getArray());
            logger.debug("imaging channel first...");
            for (int arrayIndex=0 ; arrayIndex < moveArray.length; arrayIndex++) {
                logger.debug("Moving the stage");
                htsCore.moveRelativeXYAndWaitForStage(moveArray[arrayIndex]);
                logger.debug("imaging ArrayIndex: "+ arrayIndex);
                for (int configIndex = 0; configIndex < configChannels.size(); configIndex++) {
                    //htsCore.setChannelConfig(configChannels.get(configIndex));
                    logger.debug("Setting the channel configuration...");
                    SetImagingParameters(configIndex);
                    image = new Image(htsCore,fd,wDB);
                    if (pfsPerTile.equals("TRUE") && isStackBurst()){disableShutterFocus(); logger.info("Disabled PFS and autoShutter");}// disable only if the zStack is larger than 1 and user asked to disable
                    String[] simultaneousChannels = configChannels.get(configIndex).split("_"); // subtract 1 for Epi or Confocal label
                    logger.debug("simultaneous Channels: "+ (simultaneousChannels.length-1));
                    if (simultaneousChannels.length==1){ // fix for brightfield not having EPI_Brightfield
                        imgP = new ImageStack[simultaneousChannels.length];
                    }else {
                        imgP = new ImageStack[simultaneousChannels.length - 1];
                    }
                    for(int i=0;i<imgP.length;i++){
                        imgP[i] = new ImageStack((int)camPx[0],(int)camPx[1]);
                    }
                    for (int zIndex = 0; zIndex < zStack.length; zIndex++) {
                        htsCore.setPiezoHeight(zStack[zIndex]);
                        for (int frameIndex = 0; frameIndex < nImg; frameIndex++) {
                            //image = new Image(htsCore, fd, wDB);
                            htsCore.waitForImageSynchro();
                            if (simultaneousChannels.length-1>1){
                                ImagePlus[] imgPArray = image.takeMultiImage();
                                Image.saveImage(imgPArray[1], wDB.getSavepath(), wDB.getBase(), wDB.getWell(), String.valueOf(arrayIndex+1),
                                        simultaneousChannels[0],simultaneousChannels[1],String.valueOf(interval),String.valueOf(frameIndex),
                                        String.valueOf(zIndex), zStep[configIndex]);
                                Image.saveImage(imgPArray[0], wDB.getSavepath(), wDB.getBase(), wDB.getWell(), String.valueOf(arrayIndex+1),
                                        simultaneousChannels[0],simultaneousChannels[2], String.valueOf(interval),String.valueOf(frameIndex),
                                        String.valueOf(zIndex), zStep[configIndex]);
                                //add lines to the QC file
//                                IQC.QC(imgPArray[0],wDB,String.valueOf(arrayIndex),System.nanoTime(),htsCore);
//                                IQC.QC(imgPArray[1],wDB,String.valueOf(arrayIndex),System.nanoTime(),htsCore);
                            }else if (simultaneousChannels.length==1) {
                                // snap image
                                ImagePlus img = image.takeImage();
                                //save image
                                image.saveImage(img, wDB.getSavepath(), wDB.getBase(), wDB.getWell(), String.valueOf(arrayIndex+1),
                                        "Transmission",simultaneousChannels[0],String.valueOf(interval),String.valueOf(frameIndex),
                                        String.valueOf(zIndex), zStep[configIndex]);
                                // add line to the QC file
//                                IQC.QC(img,wDB,String.valueOf(arrayIndex),System.nanoTime(),htsCore);
                            }else{
                                // snap the image
                                ImagePlus img = image.takeImage();
                                //save image
                                image.saveImage(img, wDB.getSavepath(), wDB.getBase(), wDB.getWell(), String.valueOf(arrayIndex+1),
                                        simultaneousChannels[0],simultaneousChannels[1],String.valueOf(interval),String.valueOf(frameIndex),
                                        String.valueOf(zIndex), zStep[configIndex]);
//                                IQC.QC(img,wDB,String.valueOf(arrayIndex),System.nanoTime(),htsCore);
                            }
                            htsCore.sleep((int) interval);
                        }
                        htsCore.stopAcquisition();
                    }
                    if (pfsPerTile.equals("TRUE") && isStackBurst()){ enableShutterFocus(); logger.debug("Enabled PFS");} // re-enable PFS if the user has asked for PFS between wells of Z-stacked wells
                }
            }
        }
        // Positions first
        else if(wDB.getIsPositionFirst().contains("TRUE")){
            logger.debug("Imaging position first");
            Point2D.Double initPos = htsCore.getXYPosition();
            for (int configIndex = 0; configIndex < configChannels.size(); configIndex++) {
                // move to the initial position
                htsCore.moveXYAndWaitForStage(initPos);
                SetImagingParameters(configIndex);
                image = new Image(htsCore,fd,wDB);
                String[] simultaneousChannels = configChannels.get(configIndex).split("_"); // subtract 1 for Epi or Confocal label
                logger.debug("simultaneous Channels: "+ (simultaneousChannels.length-1));
                for (int arrayIndex = 0; arrayIndex < moveArray.length; arrayIndex++) {
                    if (pfsPerTile.equals("TRUE") && isStackBurst()){disableShutterFocus(); logger.info("PFS disabled");} // disable only if the zStack is larger than 1 and user asked to disable
                    if (simultaneousChannels.length==1){ // fix for brightfield not having EPI_Brightfield
                        imgP = new ImageStack[simultaneousChannels.length];
                    }else {
                        imgP = new ImageStack[simultaneousChannels.length - 1];
                    }
                    for(int i=0;i<imgP.length;i++){
                        imgP[i] = new ImageStack((int)camPx[0],(int)camPx[1]);
                    }
                    logger.debug("Moving the stage");
                    htsCore.moveRelativeXYAndWaitForStage(moveArray[arrayIndex]);
                    logger.info("imaging ArrayIndex: " + arrayIndex);
                    for (int zIndex = 0; zIndex < zStack.length; zIndex++) {
                        htsCore.setPiezoHeight(zStack[zIndex]);
                        for (int frameIndex = 0; frameIndex < nImg; frameIndex++) {
                            //image = new Image(htsCore,fd,wDB);
                            htsCore.waitForImageSynchro();
                            if (simultaneousChannels.length-1>1){
                                ImagePlus[] imgPArray = image.takeMultiImage();
                                Image.saveImage(imgPArray[1], wDB.getSavepath(), wDB.getBase(), wDB.getWell(), String.valueOf(arrayIndex+1),
                                        simultaneousChannels[0],simultaneousChannels[1],String.valueOf(interval),String.valueOf(frameIndex),
                                        String.valueOf(zIndex), zStep[configIndex]);
                                Image.saveImage(imgPArray[0], wDB.getSavepath(), wDB.getBase(), wDB.getWell(), String.valueOf(arrayIndex+1),
                                        simultaneousChannels[0],simultaneousChannels[2], String.valueOf(interval),String.valueOf(frameIndex),
                                        String.valueOf(zIndex), zStep[configIndex]);
                                //add lines to the QC file
//                                IQC.QC(imgPArray[0],wDB,String.valueOf(arrayIndex),System.nanoTime(),htsCore);
//                                IQC.QC(imgPArray[1],wDB,String.valueOf(arrayIndex),System.nanoTime(),htsCore);
                            }else if (simultaneousChannels.length==1) {
                                ImagePlus img = image.takeImage();
                                image.saveImage(img, wDB.getSavepath(), wDB.getBase(), wDB.getWell(), String.valueOf(arrayIndex+1),
                                        "Transmission",simultaneousChannels[0],String.valueOf(interval),String.valueOf(frameIndex),
                                        String.valueOf(zIndex), zStep[configIndex]);
                                // add line to the QC file
//                                IQC.QC(img,wDB,String.valueOf(arrayIndex),System.nanoTime(),htsCore);
                            }else{
                                ImagePlus img = image.takeImage();
                                Image.saveImage(img, wDB.getSavepath(), wDB.getBase(), wDB.getWell(), String.valueOf(arrayIndex+1),
                                        simultaneousChannels[0],simultaneousChannels[1],String.valueOf(interval),String.valueOf(frameIndex),
                                        String.valueOf(zIndex), zStep[configIndex]);
//                                IQC.QC(img,wDB,String.valueOf(arrayIndex),System.nanoTime(),htsCore);
                            }
                            htsCore.sleep((int) interval);
                        }
                    }
                    if (pfsPerTile.equals("TRUE") && isStackBurst()){enableShutterFocus(); logger.debug("Enabled PFS");}// re-enable PFS if the user has asked for PFS between wells of Z-stacked wells
                }
                htsCore.stopAcquisition();
            }
        }
        // optional pre-processing
        // montage ?
        // todo arrange images in a montage post processing so this does not need to be done in bulk
        // background subtract ?
        // todo make a background image to subtract or call IJ background subtraction plugin
        // flatfield correction ?
        // todo create a "dark" image to perform flatfield correction with per camera
        // deconvolution ?
        // todo check if deconvolution can happen on the fly and how to implement
        // save
        // todo call the save function for all the images created/acquired
    }

    public void AcquireFiducial(){
        // Assumes that starting in correct initial position
        // set the channel to Brightfield
        htsCore.setChannelConfig("Brightfield");
        try {
            htsCore.innerCore.waitForConfig("Channels","Brightfield");
        }catch(Exception E){logger.info("did not wait for config");}
        htsCore.setExcitationIntensity("5:0:5:0:5:5:0:0:0:0:0:0", fd.getRobot());
        // Create the base image to place all the tile on
        camPx = htsCore.getCameraPixels();
        logger.debug("camPx: " + camPx[0]+","+camPx[1]);
        // create a blank byte array the size of a  2x2 montage
        ShortProcessor montageIp = new ShortProcessor(2* (int)camPx[1], 2 * (int)camPx[0]);
        //set the exposure
        htsCore.setExposure(5);
        // set the objective and pixelconfiguration
        htsCore.setObjective("10X");
        //get the microns per pixel
        umPx = htsCore.getPixelConfig();
        logger.debug("umPx: " +umPx);
        //set the correct offset
        htsCore.setPFSOffset(fd.plate.getPFS());
        // search for plate height and engage PFS
        htsCore.searchForPFSHeight(fd.plate.getPLATE_HEIGHT());
        htsCore.sleep(1000); //enable system to settle
        htsCore.enablePFS();
        htsCore.sleep(1000); // wait for system to lock in focus
        //create the montage
        Point2D.Double[] montageOffsets = wDB.getMontageOffsets(2, camPx, true);
        TranslationArray(2,this.umPx);
        logger.debug("Montage offsets: {}", new Object[]{montageOffsets});
        logger.debug("Move Array offsets: {}", new Object[]{moveArray});
        //Acquire the fiduciary
        for (int j = 0; j < moveArray.length; j++) {
                Point2D.Double point = moveArray[j];
                logger.debug("moving to: "+point);
                htsCore.moveRelativeXY(point);
                htsCore.waitForStage();
                image = new Image(htsCore,fd,wDB);
                ImagePlus imp = image.takeImage();
                ImageProcessor currentIp = imp.getProcessor();
                Point2D.Double offset = montageOffsets[j]; // well index number is the same as J
            montageIp.insert(currentIp, (int)offset.x, (int)offset.y);
        }
        montageIp.resize(10240,10240,true);// 10x10 fiducial size on the Andor iXon888
        ImagePlus montage = new ImagePlus("Montage", montageIp);
        logger.debug("Basename: " + fd.fiduciary.getBase().getBase());
        logger.debug("SavePath: "+ fd.fiduciary.getSavepath());
        logger.debug("Well: "+fd.fiduciary.getWell());
        edu.gladstone.hts.microscope.Image.saveImage(montage,
                fd.fiduciary.getSavepath(),
                fd.fiduciary.getBase(),
                fd.fiduciary.getWell(),
                "Fiducial",
                "MONTAGE",
                "Brightfield","0","0","0","0");
    }

    public void TranslationArray(Point2D.Double[] array){
        Point2D.Double topCorner;
        this.moveArray = array;
        // travel to top corner of the array
        if (arraySize.length>1) {
            topCorner = new Point2D.Double((-1) * (double) (Integer.parseInt(arraySize[0]) - 1) * umPx * camPx[0] / 2.0, (-1) * (double) (Integer.parseInt(arraySize[1]) - 1) * umPx * camPx[0] / 2.0);
        }else{
            topCorner = new Point2D.Double((-1) * (double) (Integer.parseInt(arraySize[0]) - 1) * umPx * camPx[0] / 2.0, (-1) * (double) (Integer.parseInt(arraySize[0]) - 1) * umPx * camPx[0] / 2.0);
        }
        htsCore.moveRelativeXYAndWaitForStage(topCorner);
        for (int i =0; i< array.length; i++){
            logger.debug("making position: "+i);
            moveArray[i].x *= (-1)*umPx*camPx[0]*(1-overlap);
            moveArray[i].y *= (1)*umPx*camPx[1]*(1-overlap);
        }
    }

    // this array is used for the fiducial and therefore does not need a return to top corner
    public Point2D.Double[] TranslationArray(int size, double umPx){
        this.moveArray = new WellArray().makeArray(size);
        logger.debug("MovementArray: {}",new Object[]{this.moveArray});
        for (Point2D.Double i: this.moveArray){
            i.x *= (-1)*umPx*(int)camPx[0];
            i.y *= (1)*umPx*(int)camPx[1];
        }
        return this.moveArray;
    }
}
