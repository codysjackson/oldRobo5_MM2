package edu.gladstone.hts.parameters;

import edu.gladstone.hts.plate.SupportedPlate;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class ParameterParser {

    final Logger logger = LoggerFactory.getLogger(ParameterParser.class);
    private final String ccb = "CCB";
	private final String ccbGlass = "CCBGlass";
	private final String ccbSlice = "CCBSlice";
	private final String mea48 = "MEA48Well";
	private final String tpp = "TPP";
	private final String ccbExtStg = "CCBExtStg";
    private final String ELWD10X = "10X";
    private final String ELWD20X = "20X";
    private final String ELWD20XPh1 = "20XPh1";
    private final String ELWD40X = "40X";
    private final String ELWD40XPh2 = "40XPh2";
    private final String ELWD60X = "60X";
    private final String andorZyla = "Zyla";
    private String imageFolder;
	private SupportedPlate supportedPlate;
    private int actualWellCount;
    private List<String> params ;
    ExperimentDataBean experimentDataBean;
    ArrayList<WellDataBean> wellData;
	private ArrayList<SchedulerData> schedulerData;
	public Hashtable<String, ArrayList<WellDataBean>> wellLists;
	
    public ParameterParser(FileReader dataFile) throws Exception {    	
        params = new ArrayList<String>();
		String line;
		BufferedReader br;
		StringBuffer sb;
		String mode;
		String[] allModes;

		int i;
        try {
    		br = new BufferedReader(dataFile);    		
    		while ( (line = br.readLine())!= null ){
    			//read line by line, while also eliminating empty lines
    			if (this.keep(line)){    			
    				params.add(line);    				
    			}
    		}
    		br.close();
        } catch (Exception e) {
            logger.error("Error opening csv file");
            logger.error(e.getMessage(), e);
            // since we are unable to open the imaging template, exit the plugin
            ij.IJ.log("Exiting plugin - Unable to read imaging template");            
            throw(e);
        }

        // now that we have read in the whole file, let's pull out the parameters
		// lines 1 & 2 are the experiment parameters
        ParameterToBeanHelper parameterHelper = new ParameterToBeanHelper();
		experimentDataBean = parameterHelper.extractExperimentInfo(params.get(0)+"\n"+params.get(1));
		this.imageFolder = experimentDataBean.getImageFolder();
		
		// remove parsed contents 
		params.remove(0);
		params.remove(0);
		
		// Scheduler related parameters  - this also parses out the complex imaging parameters 
		i = 0;
		line = params.get(i);
		sb = new StringBuffer();
		while( (! line.startsWith("Well")) && (i<params.size()) ){
			sb.append(line).append("\n");
			i++;
			line = params.get(i);
		}
		
		this.schedulerData = parameterHelper.extractSchedulerData(sb.toString());
		// retrieve the list of unique imaging modes in this experiment
		sb = new StringBuffer();
		for (SchedulerData sd: this.schedulerData){
			mode = sd.getMode(); // this may be a ";" separated string
			if (mode!=null && !mode.isEmpty()){
				sb.append(mode);				
			}else{
				sb.append("long"); // default longitudinal imaging
			}
			sb.append(";");
		}
		
		params = params.subList(i, params.size());
		// "params" now contains only the Well data
		//TODO reminder that pixel/micron is taken care of in the pixel calibrations
        this.getPlate(experimentDataBean.getPlateType() , experimentDataBean.getNumberOfWells());

		// convert the remaining items in the "params" array to the appropriate string format for parsing by CsvToBean() 
		sb = new StringBuffer();
		for(i=0; i<params.size(); i++){
			sb.append(params.get(i)).append("\n");
		}		
		wellData = parameterHelper.extractWellData(sb.toString()); 
		this.actualWellCount = wellData.size();

    }
   
    public String getImageFolder() {
		return imageFolder;
	}

    //Actual well count may be different than the number of wells in cases where want to image only part of the plate
    public int getWellCount(){
    	return(this.actualWellCount);
    }
    
    public ExperimentDataBean getExperimentDataBean() {
		return experimentDataBean;
	}

	public ArrayList<WellDataBean> getWellData() {
		return wellData;
	}

	private boolean keep(String line){
    	// Check whether there are any non-empty string in line by removing all the commas
    	int len = line.replaceAll(",", "").length();
    	return ( len >0 );
    }

	//Remove this method :
    public SupportedPlate getPlate(){
    	return(this.supportedPlate);
    }
    
    private void getPlate(String plateType, int wellCount) throws IllegalArgumentException {
		logger.info("Wells: "+wellCount);
		logger.info("plate: "+plateType);
        if ( plateType.equalsIgnoreCase(ccb) && (wellCount==96) ) {
			supportedPlate = SupportedPlate.perkinelmer96CellCarrier;
		}else if ( plateType.equalsIgnoreCase(mea48) && (wellCount==48) ) {
			supportedPlate = SupportedPlate.axionMEA48Well;
		} else if ( plateType.equalsIgnoreCase(ccbExtStg) && (wellCount==96) ) {
			supportedPlate = SupportedPlate.perkinelmer96CellCarrierExtStageR5;
		} else if ( plateType.equalsIgnoreCase(ccbGlass) && (wellCount==96) ) {
				supportedPlate = SupportedPlate.perkinelmer96CellCarrierGlass;
        }else if ( plateType.equalsIgnoreCase(ccbSlice) && (wellCount==96) ) {
			supportedPlate = SupportedPlate.perkinElmer96WellSliceCulture;
		} else if (plateType.equalsIgnoreCase(ccb) && (wellCount==384) ) {
			supportedPlate = SupportedPlate.perkinelmer384CellCarrier;
		}else if (plateType.equalsIgnoreCase(tpp) && (wellCount==96)) {
			supportedPlate = SupportedPlate.tpp96Well;
        } else {        
            String errorMessage = "Un-supported plate type : " + plateType + " " + wellCount;
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
	logger.debug("Plate: "+supportedPlate);
    }

}