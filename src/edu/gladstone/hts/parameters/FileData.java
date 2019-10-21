package edu.gladstone.hts.parameters;

import edu.gladstone.hts.microscope.ExtendStageRunnable;
import edu.gladstone.hts.plate.PlateFactory;
import edu.gladstone.hts.plate.SBSPlate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import java.nio.file.Files;

/**
 * Created with IntelliJ IDEA.
 * User: mikeando
 * Date: 4/21/13
 * Time: 9:59 PM
 * To change this template use File | Settings | File Templates.
 */

public class FileData {
    private ParameterParser pp;
    private BaseName base;
    private String savepath;

    public WellDataBean[] wells;
    public WellDataBean fiduciary;
    public SBSPlate plate;

    final Logger logger = LoggerFactory.getLogger(FileData.class);

    /**
     * This class parses a string to determine the basename for the experiment and find the Image Template file.
     * It also sets the save path, but that is currently hardcoded in.
     *
     * @param data The string passed from the scheduling program to Micro-Manager which details the experiment
     *             parameters.
     * @throws FileNotFoundException
     */

    public FileData(String data) throws Exception {
        logger.info("Experiment data is: {}", data);
        ImageTemplateParser ImageTemplateData = new ImageTemplateParser(data);
        String filepath = ImageTemplateData.getFilepath();
        logger.info("Image template : {}", filepath );
        FileReader dataFile = new FileReader(filepath);
        pp = new ParameterParser(dataFile);

        String experimentName = pp.getExperimentDataBean().getExperimentName();//ImageTemplateData.getExperimentName();

        int timepointNumber = ImageTemplateData.getTimepointNumber();
        String hour = ImageTemplateData.getHour();
        String date = getDate();
        base = new BaseName(date, experimentName, timepointNumber, hour);
        logger.info("Base filename is: {}", base.getBase());


        String dataFolder = "C:/Images"; // assume default exists ??
        dataFolder = pp.getImageFolder();
        plate = new PlateFactory(pp.getPlate()).createPlate();
        dataFile.close(); // this can throw IOException
 
        File saveFolder = new File(dataFolder, base.getName().trim());
        if (!saveFolder.exists()) {
            saveFolder.mkdir();
        }
        savepath = saveFolder.getAbsolutePath();

        logger.info("Save path is: {}", savepath);
        logger.info("filepath : {} ", filepath);

        //set all the data for the fiducial montage
        fiduciary = new WellDataBean();
        fiduciary.setBase(base);
        fiduciary.setSavepath(savepath);
        fiduciary.setWell("A1");

        // copy imaging template to save folder        
    	File src = new File(filepath);
    	File dest = new File(savepath + File.separator + src.getName());
    	File destPath = new File(savepath);
    	try{
    		if (!dest.canRead() && destPath.exists()){
    			Files.copy(src.toPath(), dest.toPath());
    		}
        }catch(Exception e){
        	logger.error("Error copying imaging template to image folder");
        	logger.error("src: " + src.getAbsolutePath() + ", dest: " +dest.getAbsolutePath());
        	logger.error(e.getMessage(), e);
        }
        // write important information from the ExperimentDataBean to the wellDataBean
        ArrayList<WellDataBean> wellData = pp.getWellData();
        Point center;
        wells = new WellDataBean[pp.getWellCount()];
        int i =0;
        for(WellDataBean wdb: wellData){
            center = getCenterWellPosition( wdb.getWellParser() );
            logger.debug("Savepath put in to WDB: " + savepath);
            wdb.setSavepath(savepath);
            logger.debug("Basename put in to WDB: "+ base.getBase());
            wdb.setBase(base);
            wdb.setCenter(center);
            wells[i] = wdb;
            i++;
        }
     }

    public String getRobot(){
        return pp.experimentDataBean.getMicroscope();
    }

    public String getPFSPerTile(){return pp.experimentDataBean.getPFSPerTile();}

    public Point getCenterWellPosition(WellParser well) {
        // Center well position relative to the center for the first well (as set by the fiduciary alignment)
    	logger.info("Well Spacing is: "+plate.getWELL_SPACING());
        logger.debug("Well Name is : "+well.getName());
        int xPosition = (well.getColumn() - 1) * plate.getWELL_SPACING();
        int yPosition = (well.getRow() - 1) * plate.getWELL_SPACING();
        logger.info(well.getName() + " is located at row " + well.getRow() +" and column " + well.getColumn());
        logger.info("Center of "+well.getName()+" located at: "+xPosition+"," + yPosition);
        return new Point(xPosition, yPosition);
    }

    private static String getDate() {
        int YEAR = 1;
        int MONTH = 2;
        int DAY = 5;

        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar
                .getInstance();
        String year = ((Integer) cal.get(YEAR)).toString();
        Integer monthStartZero = (Integer) cal.get(MONTH);
        Integer monthStartOne = monthStartZero + 1;
        String month = monthStartOne.toString();
        String day = ((Integer) cal.get(DAY)).toString();
        // Pad Month and Year if necessary
        if (month.length() == 1) {
            month = "0" + month;
        }
        if (day.length() == 1) {
            day = "0" + day;
        }
        // Assemble date
        String date = year + month + day;
        return date;
    }
}
