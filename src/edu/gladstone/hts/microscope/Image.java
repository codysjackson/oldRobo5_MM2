package edu.gladstone.hts.microscope;

import edu.gladstone.hts.imaging.PlateImager;
import edu.gladstone.hts.parameters.BaseName;
import edu.gladstone.hts.parameters.FileData;
import edu.gladstone.hts.parameters.WellDataBean;
import ij.ImagePlus;
import ij.process.ShortProcessor;
import mmcorej.CMMCore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
//import java.util.ArrayList;

public class Image {

    static CMMCore core_;
    static edu.gladstone.hts.imaging.Metadata md;
    static FileData fd;
    static WellDataBean wdb;
    private int PIXEL_WIDTH;
    private int PIXEL_HEIGHT;
    private boolean flip;
    final Logger logger = LoggerFactory.getLogger(Image.class);

    public Image(Core htsCore, FileData fData, WellDataBean wellData) {
        core_ = htsCore.innerCore;
        fd = fData;
        wdb = wellData;
        core_.getCameraDevice();
        //md = new edu.gladstone.hts.imaging.Metadata(); // file is already in memory no params needed
        md = PlateImager.md;
        PIXEL_WIDTH = (int)core_.getImageWidth();
        PIXEL_HEIGHT = (int)core_.getImageHeight();
        if (core_.getCameraDevice().contains("2")){
            flip=true;
        }else{
            flip=false;
        }
    }

    private void snapImage() {
        try {
            core_.snapImage();
        } catch (Exception e) {
            logger.error("Error taking an image");
            logger.error(e.getMessage());
            throw new RuntimeException("Error taking an image", e.getCause());
        }
    }

    private ImagePlus retrieveImage(long i) {

        try {
            Object img = core_.getImage(i); //need to specify which camera to take the image from
            ShortProcessor ip = new ShortProcessor(PIXEL_WIDTH, PIXEL_HEIGHT);
            ip.setPixels(img);
            if (i==1 || flip==true){
                ip.flipHorizontal();
            }
            return new ImagePlus("Temp", ip);
        } catch (Exception e) {
            logger.error("Error retrieving the image");
            logger.error(e.getMessage());
            throw new RuntimeException("Error retrieving the image",
                    e.getCause());
        }
    }

    public ImagePlus takeImage() {
        snapImage();
        try {
            //core_.popNextImage();
        }catch(Exception e){}
        return retrieveImage(0);

    }

    public ImagePlus[] takeMultiImage(){
        try {
            ImagePlus[] imageArray = new ImagePlus[2];
            snapImage();
            //core_.popNextImage();
            imageArray[0] = retrieveImage(0);
            imageArray[1] = retrieveImage(1);
            logger.debug("Snapped and retrieved multi-image");
            return(imageArray);
        }catch(Exception e){}
        return null;
    }

    public static void saveWithoutClosingImage(ImagePlus imp, String path, BaseName base,
                                               String imageWell, String imageNumber,String ChannelMode, String imageChannel,String interval, String burstFrame,String zIndex,String zStep) {
        String fullName = base.getBase() + "-" + burstFrame + "_" + imageWell + "_" + imageNumber
                + "_" +ChannelMode+"-"+imageChannel +"_"+interval+"_"+ zIndex + "_" + zStep+".tif";
        // Increment timepoint number if image already exists
        File newImage = new File(path, fullName);
        if (newImage.exists()) {
            String name = base.getName();
            int newTimepointNumber = base.getTimpointNumber() + 1;
            String hour = base.getHour();
            String date = base.getDate();
            BaseName newBase = new BaseName(date, name, newTimepointNumber, hour);
            saveWithoutClosingImage(imp, path, newBase, imageWell, imageNumber,
                    ChannelMode,imageChannel,interval,burstFrame,zIndex,zStep);
        } else {
            String fullPath = newImage.getAbsolutePath();
            if (!fullPath.contains("MONTAGE")) {
                //write the metadatafile
                md.addImageToJSON(fullName,fd,wdb,core_,imp);
            }
            fullPath = fullPath.replace("Transmission-Automated-DAPIFITC-","");
            fullPath = fullPath.replace("Transmission-Automated-CFPYFP-","");
            ij.IJ.save(imp, fullPath);
            System.out.println("saved: " + fullPath);
        }
    }

    public static void saveImage(ImagePlus imp, String path, BaseName base, String imageWell, String imageNumber,
                                 String ChannelMode, String imageChannel,String interval, String burstFrame,String zIndex,String zStep) {
        saveWithoutClosingImage(imp, path, base, imageWell, imageNumber,ChannelMode, imageChannel,interval,burstFrame,zIndex,zStep);
        destroyImage(imp);
    }

    public static void destroyImage(ImagePlus imp) {
        imp.close();
    }
}
