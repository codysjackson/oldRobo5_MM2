/**
 * Created by robo5 on 1/25/2016.
 *
 * This code has been developed to control the Sutter VF-5 tunable filter system. As of 20160129 there has not been a
 * driver package made available, only a series of commands in Hex. Because of this, and because the filter is controlled
 * via the Sutter Lambda 10-3, a uManager User Defined Serial State Device has to be used to assign and call positions.
 */
package edu.gladstone.hts.microscope;
import mmcorej.CMMCore;
import ij.IJ;
import mmcorej.CharVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class VF5Contoller{
    private Core core_; // = new CMMCore();
    final Logger logger = LoggerFactory.getLogger(Core.class);
    // need to add extra "\" characters for adding the escape character
    private String baseCommand = "\\xda\\x01\\xbc\\xc2"; // Base string for 700nm filter
    private String[] baseCommandArray = {"0xda","0x01","0xbc","0xc1"}; //hex command storage for CharVector implementation
    private String comCommand;
    private char[] wlChar= new char[3]; //stores the wavelength as a char array
    ArrayList<String> configList;

    public VF5Contoller(Core htsCore) {
        core_ = htsCore;
    }

    public void SetWavelength(int wavelength){
        // check if the wavelength requested should be CFP-YFP cube or DAPI-FITC cube
        String chan = "Automated-DAPI-FITC";
        try {
            if (wavelength <= 400 ||
                    (wavelength >= 472 && wavelength <= 497) ||
                    (wavelength >= 547 && wavelength <= 575) ||
                    (wavelength >= 639 && wavelength <= 660)) {
                chan = "Automated-DAPI-FITC";
                //core_.innerCore.setProperty("TIFilterBlock","Label", "DAPI-FITC");
                //core_.waitForDevice("TIFilterBlock");

            } else if ((wavelength <= 450) ||
                    (wavelength >= 500 && wavelength <= 514) ||
                    (wavelength >= 579 && wavelength <= 600)) {
                chan = "Automated-CFP-YFP";
                //core_.innerCore.setProperty("TIFilterBlock","Label", "CFP-YFP");
                //core_.innerCore.waitForDevice("TIFilterBlock");
            } else {
                logger.info("Not advised to take images at this wavelength");
            }
        }catch(Exception e){
            logger.error("Could not change filterCube position: "+e);
        }
        // create the hex command string
        String tempString = CreateWavelengthString(wavelength);
        String [] str = baseCommandArray;
        CharVector cv = new CharVector();
        for (int i=0; i<str.length; i++){
            cv.add((char)(short)Short.decode(str[i]));
        }
        // send the string to the controller
        //SendCommand(tempString);
        SendCommand(cv);
        logger.debug("Changing the channel");
        // change to the "Automated" channel
        changeChannel(chan);;
    }

    public String CreateWavelengthString(int nm){
        comCommand="";
        String I = Integer.toHexString(nm);
        for (int j = 0; j < baseCommand.length(); j++) {
            if (j==10){
                comCommand+= String.valueOf(I.charAt(1));
                logger.debug("ComCommand:" + comCommand);
            }
            else if(j==11){
                comCommand+=String.valueOf(I.charAt(2));
                logger.debug("ComCommand:" + comCommand);
            }
            else if(j==14){
                comCommand+= "c";  // speed setting
                logger.debug("ComCommand:" + comCommand);
            }
            else if(j==15){
                comCommand+=String.valueOf(I.charAt(0));
                logger.debug("ComCommand:" + comCommand);
            }
            else{
                comCommand+=String.valueOf(baseCommand.charAt(j));
                logger.debug("ComCommand:" + comCommand);
            }
        }
        wlChar = I.toCharArray();
        baseCommandArray[2] = "0x"+wlChar[1]+wlChar[2];
        baseCommandArray[3] = "0x0"+wlChar[0]; // hardcoded with tilt speed 3
        return comCommand;
    }

    public ArrayList<String> CreateConfigArray(String Min, String Max, String Step){
        // expects params like "min:max:step"
        //String[] paramString = params.split(":");
        //parse the string in to correct values
        String minTemp = Min;
        String maxTemp = Max;
        String stepTemp = Step;
        logger.debug("CreateConfigArray params: "+minTemp+", "+maxTemp+", "+stepTemp);
        int min = Integer.parseInt(Min.replace(".0",""));
        int max = Integer.parseInt(Max.replace(".0",""));
        int step = Integer.parseInt(Step.replace(".0",""));
        //create the array for all the config strings
        configList = new ArrayList<String>();
        String nm = "";
        // populate the config list
        for(int i=min; i<=max; i+=step) {
            nm = String.valueOf(i);
            logger.debug("Currently Imaging Wavelength: "+nm);
            String chan = "Automated-DAPI-FITC";
            int wavelength = (int)Integer.parseInt(nm);
            if (wavelength<=400 ||
                    (wavelength>=472 && wavelength<=497) ||
                    (wavelength>=547 && wavelength<=575)||
                    (wavelength>=639 && wavelength<=660)){
                chan ="Automated-DAPIFITC";
            } else if((wavelength<=450) ||
                    (wavelength>=500 && wavelength<=514)||
                    (wavelength >=579 && wavelength<=600)){
                chan = "Automated-CFPYFP";
            }else{
                logger.info("Not advised to take images at this wavelength");
                continue;
            }
            configList.add(chan +"-"+ nm);
            logger.debug("Added Channel: " + chan + "-" + nm);

        }
        return configList;
    }

    public ArrayList<String> CreateConfigArray(String[] posList){
        String[] temp = posList;
        //create the array for all the config strings
        configList = new ArrayList<String>();
        String nm = "";
        // populate the config list
        for(String str : temp) {
            str = str.replace(".0","");
            nm = String.valueOf(str);
            logger.debug("Currently creating channels for wavelength: "+nm);
            String chan = "Automated-DAPI-FITC";
            int wavelength = (int)Integer.parseInt(nm);
            if (wavelength<=400 ||
                    (wavelength>=472 && wavelength<=497) ||
                    (wavelength>=547 && wavelength<=575)||
                    (wavelength>=639 && wavelength<=660)){
                chan ="Automated-DAPIFITC";
            } else if((wavelength<=450) ||
                    (wavelength>=495 && wavelength<=514)||
                    (wavelength >=579 && wavelength<=600)){
                chan = "Automated-CFPYFP";
            }else{
                logger.info("Not advised to take images at this wavelength: "+wavelength);
                continue;
            }
            configList.add(chan +"-"+ nm);
            logger.debug("Added Channel: " + chan + "-" + nm);

        }
        logger.debug("ConfigList: "+configList.toString());
        return configList;
    }
    
    public void SendCommand(String command) {
        try {
            core_.innerCore.setProperty("VF5", "SetPosition-command-0", command);
            core_.innerCore.waitForSystem();
            core_.sleep(500);
            core_.innerCore.getProperty("VF5", "SetPosition-command-0");
        } catch (Exception e) {
            logger.trace("could not assign command position: " + e);
        }
    }

    public CharVector checkWaveLength(){
        CharVector wl=null;
        String[] check = {"0xdb"};
        for (int i = 0;i<check.length;i++){
            wl.add((char)(short)Short.decode(check[i]));
        }
        SendCommand(wl);
        return readState();
    }

    public void SendCommand(CharVector cv){
        CharVector response;
        Boolean complete = false;
         try{
             core_.innerCore.writeToSerialPort("COM1",cv);
             while (complete==false){
                 logger.debug("waiting for VF5");
                 core_.sleep(250);
                 response = readState();
                 if(response.size()>=5){
                     complete = true; // 0x0d signifies \r and end of a busy flag
                 }
             }
         }catch(Exception e){
             logger.error("Caught Exception while sending wavlength: "+e.getMessage());
         }
    }

    public CharVector readState(){
        CharVector ans=new CharVector();
        try{
            ans = core_.innerCore.readFromSerialPort("COM1");
        }catch(Exception e){
            logger.error("Exception caught while returning response from VF5: "+e.getMessage());
        }
        return ans;
    }

    public void changeChannel(String channel){
        //Now change to that wavelength
        try {
            core_.innerCore.setConfig("Channels","channel");
            core_.innerCore.waitForConfig("Channels","channel");
            core_.sleep(500);
        }catch(Exception e){
            logger.trace("Could not change to desired wavelength: " + e);
        }
    }
}