package edu.gladstone.hts.imaging;

import edu.gladstone.hts.parameters.*;
import edu.gladstone.hts.microscope.Core;
import ij.ImagePlus;
import ij.measure.Measurements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * Created by elliot on 5/6/15.
 * Create and save the plots for each imaging session
 */

public class ImageQC {
    public FileWriter OutputFile;
    PrintWriter pw;
    final Logger logger = LoggerFactory.getLogger(Core.class);

    public ImageQC(Core htsCore) {

    }

    private String CameraTemp(Core core_) {
        String CCDTemp = "";
        try {
            CCDTemp = core_.innerCore.getProperty("iXon", "CCDTemperature");
        } catch (Exception e) {
            logger.trace("Could not retrieve camera temp " + e);
        }
        return CCDTemp;
    }

    private String ObjectiveHeight(Core core_) {
        logger.trace("getting the objective height");
        String zHeight = "";
        return String.valueOf(core_.getZPosition());
    }

    private String GetXPosition(Core core_) {
        logger.trace("Getting stage's X position");
        String Xpos = "";
        try {
            Xpos = String.valueOf(core_.innerCore.getXPosition(core_.innerCore.getXYStageDevice()));
        } catch (Exception e) {
            logger.error("Could not get Stage X position: " + e);
        }
        return Xpos;
    }

    private String GetYPosition(Core core_) {
        logger.trace("Getting stage's X position");
        String Ypos = "";
        try {
            Ypos = String.valueOf(core_.innerCore.getYPosition(core_.innerCore.getXYStageDevice()));
        } catch (Exception e) {
            logger.error("Could not get Stage X position: " + e);
        }
        return Ypos;
    }

    private String GetPFSOffset(Core core_) {
        String pfs = "";
        try {
            pfs = core_.innerCore.getProperty("TIPFSOffset", "Position");
        } catch (Exception e) {
            logger.error("Could not retrieve PFSOffset: " + e);
        }
        return pfs;
    }


    private String CurrentChannel(Core core_) {
        String Channel = "";
        try {
            Channel = core_.innerCore.getCurrentConfig("Channel");
        } catch (Exception e) {
            logger.trace("could not retrieve the Channel config  " + e);
        }
        return Channel;
    }


    private String imgMedian(ImagePlus img) {
        logger.trace("Getting the image histogram");
        long[] hist = img.getStatistics().getHistogram();
        long median = hist[hist.length / 2];
        return String.valueOf(median);
    }

    public void SetSavePathAndOpen(WellDataBean wd) {
        try {
            OutputFile = new FileWriter(wd.getSavepath() + "\\" + wd.getBase().getName() + "_QC.csv", true);
        } catch (IOException ioe) {
            logger.error("Not able to make this file" + ioe);
        }
        pw = new PrintWriter(OutputFile);

        if (wd.getBase().getTimpointNumber() == 0) {
            // if Timepoint == 0 and is a new file, write the header
            pw.append("Well,Timepoint,Channel,arrayIndex,Max,Min,Mean,Median,Mode,StdDev,SNR,ContrastScore,timeElapsed,Zheight,Camera,Date,Time,XPosition,YPosition,PFSOffset\n");
        }
    }

    public String[] printToday() {
        GregorianCalendar gc = new GregorianCalendar();
        gc.getTime();
        String[] dateTime = new String[2];
        dateTime[0] = String.valueOf(gc.get(Calendar.YEAR))
                + "/" + String.valueOf(gc.get(Calendar.MONTH) + 1)
                + "/" + String.valueOf(gc.get(Calendar.DAY_OF_MONTH));
        dateTime[1] = String.valueOf(gc.get(Calendar.HOUR_OF_DAY))
                + ":" + String.valueOf(gc.get(Calendar.MINUTE))
                + ":" + String.valueOf(gc.get(Calendar.SECOND));
        return dateTime;
    }

    public void QC(ImagePlus img, WellDataBean wd, String imgNum, double timeElapsed, Core core) {
        logger.info("inside the QC document writer");
        String[] Time = printToday();
        //take in new image parameters
        double max = img.getStatistics().max;
        double min = img.getStatistics().min;
        double mode = img.getStatistics().mode;
        double contrast = (max - min) / (max + min);
        logger.info("begin writing the file");
        pw.append(wd.getWell() + ",");
        pw.append(wd.getBase().getTimepoint() + ",");
        pw.append(CurrentChannel(core) + ",");
        pw.append(imgNum + ","); //array index
        pw.append(String.valueOf(max) + ","); // max
        pw.append(String.valueOf(min) + ","); //min
        pw.append(String.valueOf(img.getStatistics(Measurements.MEAN).mean) + ","); //mean
        pw.append(String.valueOf(img.getStatistics(Measurements.MEDIAN).median) + ","); //median
        pw.append(String.valueOf(img.getStatistics(Measurements.MODE).mode) + ","); //mode
        pw.append(String.valueOf(img.getStatistics(Measurements.STD_DEV).stdDev) + ","); //stdDev
        pw.append(String.valueOf(img.getStatistics().max / Math.sqrt(img.getStatistics(Measurements.MEDIAN).mode)) + ","); //SNR
        pw.append(String.valueOf(contrast) + ","); //contrast score
        pw.append(String.valueOf(timeElapsed) + ",");
        pw.append(ObjectiveHeight(core) + ",");
        pw.append(core.innerCore.getCameraDevice() + ",");
        pw.append(Time[0] + ","); // Date
        pw.append(Time[1] + ","); // time
        pw.append(GetXPosition(core) + ",");
        logger.debug("XPosition: " + GetXPosition(core));
        pw.append(GetYPosition(core) + ",");
        logger.debug("YPosition: " + GetYPosition(core));
        pw.append(GetPFSOffset(core) + ",");
        pw.append("\n");
        logger.info("Done with this line of the file");
    }

    public void close() {
        pw.flush();
        pw.close();
    }

}
