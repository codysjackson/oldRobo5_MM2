package edu.gladstone.hts.imaging;


import edu.gladstone.hts.parameters.FileData;
import edu.gladstone.hts.parameters.WellDataBean;

import ij.ImagePlus;

import java.io.*;
import java.util.Iterator;

import ij.measure.Measurements;
import mmcorej.CMMCore;
import mmcorej.PropertySetting;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.Configuration;

/**
 * Created by elliot on 6/15/17.
 *
 * This is designed to follow the same metadata JSON structure that has been discussed by the NeuroLINCS group
 * additional to the NeuroLINCS data, we will add other sections for all lab speciic biology and investigator
 * information
 */
public class Metadata {
    static Writer w;
    static File f;
    static JSONObject jo;
    public JSONObject tempObj = new JSONObject();

    static PrintWriter pw;

    static CMMCore core_; // innerCore
    static WellDataBean wdb; // well data
    static FileData fd; // file data
    static String fp; // full path name from Image class
    String JSONString="";
    static String imgName="";


    static ImagePlus imgP;
    static mmcorej.Configuration config;
    static Logger logger = LoggerFactory.getLogger(Metadata.class);

    public Metadata(){

    }

    public Metadata(String ImgName, FileData fData,WellDataBean wData, CMMCore c,ImagePlus img) {
        core_ = c;
        wdb = wData;
        fd=fData;
        imgP=img;
        imgName = ImgName;
        f = new File(wdb.getSavepath()+wdb.getBase().getName()+"_metadata.json");
        readJSON(f);
    }


    public void readJSON(File f){
        try {
            if (f.exists()==false) {
                logger.info("Did not find previous metadata file");
                //JSONString = "{}";
                jo = new JSONObject();
            } else {
                logger.info("Found existing metadata file");
                BufferedReader br = new BufferedReader(new FileReader(f));
                JSONString = br.readLine();
                if (!JSONString.startsWith("{")) {
                    String temp = "{";
                    temp+=JSONString;
                    JSONString=temp;
                }
                jo = new JSONObject(JSONString);
            }


        }catch(IOException ioe){
            System.out.println("IO Exception encountered: " + ioe);
        }catch(JSONException je){
            System.out.println("JSON Exception encountered: " + je);
        }

    }

    public void writeJSON(File f){
        try{
            logger.info("TempOBject has length: " + tempObj.length());
            Iterator i = tempObj.keys();
            while (i.hasNext()){
                String k = (String)i.next();
                //logger.info("Key: " + k);
                //logger.info("Keys of K"+tempObj.getJSONObject(k).toString());
                jo.put(k, tempObj.getJSONObject(k));
            }
            w = new FileWriter(f);
            jo.write(w);
            w.flush();
            w.close();
            JSONString=null;
            tempObj=null;
            jo=null;
        }catch(IOException ioe){
            System.out.println("Error reading file: "+ioe);
        }catch(JSONException je){
            System.out.println("JSONString error"+ je);
        }catch(NullPointerException npe){
            logger.debug("NullPointerException caught:"+npe);
            if(tempObj.length()>0){
                logger.info("Trying again....");
                writeJSON(f);
            }else {
                return;
            }
        }

    }

    public void addImageToJSON(String img,FileData fd,WellDataBean wdb,CMMCore core_,ImagePlus imp){
        imgName = img;
        imgP = imp;
        try {
            // create an image specific object under Image parent JSONObject
            tempObj.put(imgName, new JSONObject());

            //Create System sub-section
            tempObj.getJSONObject(imgName).put("Microscope",new JSONObject());
            logger.info("gathering microscope MD");
            long now = System.nanoTime();
            config = core_.getSystemStateCache();
            for(int i=0;i<config.size();i++) {
                PropertySetting keyValPair = config.getSetting(i);
                //logger.debug("key:"+keyValPair.getPropertyName());
                //logger.debug("value:"+keyValPair.getPropertyValue());
                //logger.debug("key:"+keyValPair.getPropertyName());
               //logger.debug("value:"+keyValPair.getPropertyValue());
                tempObj.getJSONObject(imgName).getJSONObject("Microscope").put(keyValPair.getDeviceLabel()+"-"+keyValPair.getKey(),keyValPair.getPropertyValue());
            }

            logger.debug("Collecting System state took: "+(System.nanoTime()-now)/1000000+"ms");
            tempObj.getJSONObject(imgName).getJSONObject("Microscope").put("X Position", core_.getXPosition(core_.getXYStageDevice()));
            tempObj.getJSONObject(imgName).getJSONObject("Microscope").put("Y Position",core_.getYPosition(core_.getXYStageDevice()));
            tempObj.getJSONObject(imgName).getJSONObject("Microscope").put("TI-Z Position",core_.getPosition("TIZDrive"));
            tempObj.getJSONObject(imgName).getJSONObject("Microscope").put("Z Position",core_.getPosition("ZStage"));

            // Create ImageStats JSONObject
            now = System.nanoTime();
            tempObj.getJSONObject(imgName).put("Image",new JSONObject());
            logger.debug("Gathering Image Stats");
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("DesiredDateAcqired",wdb.getBase().getDate());
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("ActualTimeAcqired",System.currentTimeMillis());
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("TimePoint",wdb.getBase().getTimepoint());
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("row",wdb.getWell().charAt(0));
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("column",wdb.getWell().charAt(1));
            //tempObj.getJSONObject(imgName).getJSONObject("Image").put("Array",wdb.getArray());
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("Channel",wdb.getChannels());
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("Overlap",wdb.getOverlap());
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("IsPositionFirst",wdb.getIsPositionFirst());
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("PFSPerTile",fd.getPFSPerTile());
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("umPerPx",wdb.umPerPx);
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("Well",wdb.getWell());
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("PFSOffset",wdb.getPerfectFocusOffset());
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("min",imgP.getStatistics().min);
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("max", imgP.getStatistics().max);
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("mean",imgP.getStatistics(Measurements.MEAN).mean);
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("mode", imgP.getStatistics(Measurements.MODE).mode);
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("median",imgP.getStatistics(Measurements.MEDIAN).median);
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("Standard Deviation",imgP.getStatistics(Measurements.STD_DEV).stdDev);
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("Kurtosis",imgP.getStatistics(Measurements.KURTOSIS).kurtosis);
            tempObj.getJSONObject(imgName).getJSONObject("Image").put("Skewness",imgP.getStatistics(Measurements.SKEWNESS).skewness);
            logger.debug("Collecting Image stats took: "+(System.nanoTime()-now)/1000000+"ms");

        }catch(JSONException jsonEx){
            logger.error("Error writing JSON file"+ jsonEx);
        }catch (IOException ioe){
            logger.error("IO Error when flushing and closing the file: "+ioe);
        }catch(Exception e){
            logger.error("Error retrieving metadata key-value pair: "+e.getStackTrace().toString());
        }

    }
}
