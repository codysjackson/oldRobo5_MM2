package edu.gladstone.hts.parameters;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import edu.gladstone.hts.parameters.ExperimentDataBean;
import edu.gladstone.hts.parameters.MicroscopeDataBean;
public class ParameterToBeanHelper {
	final Logger logger = LoggerFactory.getLogger(ParameterToBeanHelper.class);
	public ParameterToBeanHelper(){
		
	}
	
    public ExperimentDataBean extractExperimentInfo(String lines){
    	StringReader expDataReader = new StringReader(lines);
    	HeaderColumnNameTranslateMappingStrategy<ExperimentDataBean> expBean = new HeaderColumnNameTranslateMappingStrategy<ExperimentDataBean>();
    	
    	Map <String,String> cmap = new HashMap<String,String>();

    	cmap.put("Experiment Name", "experimentName");
    	cmap.put("Author", "author");
    	cmap.put("Description", "description");
    	cmap.put("Plate", "plateType");
    	cmap.put("Microscope", "microscope");
    	cmap.put("Plate Barcode", "plateBarcode");
    	cmap.put("Well Count", "numberOfWells");
    	cmap.put("Incubator", "incubator");
    	cmap.put("Stack", "stack");
    	cmap.put("Shelf", "shelf");
    	cmap.put("Image Folder", "imageFolder");
		cmap.put("PFSPerTile","pfsPerTile");
    	
    	expBean.setType(ExperimentDataBean.class);
	    expBean.setColumnMapping(cmap);
	    
		CsvToBean<ExperimentDataBean> csv = new CsvToBean<ExperimentDataBean>();			
		List<ExperimentDataBean> list = csv.parse(expBean, expDataReader);	
		ExperimentDataBean bean = list.get(0);	
		logger.info("image folder : " + bean.getImageFolder());
	    return(bean);
    }
    
    public ArrayList<SchedulerData> extractSchedulerData(String lines){
    	StringReader sr = new StringReader(lines);
    	HeaderColumnNameTranslateMappingStrategy<SchedulerData> strat = new HeaderColumnNameTranslateMappingStrategy<SchedulerData>();
    	
    	Map<String, String> cmap = new HashMap<String, String>();
    	cmap.put("date", "date");
    	cmap.put("Time", "time");
    	cmap.put("Timepoint", "timepoint");
    	cmap.put("Hour", "hour");
    	cmap.put("Estimated Duration(hours)", "estimatedDuration");
    	
    	strat.setType(SchedulerData.class);
    	strat.setColumnMapping(cmap);
    	
    	CsvToBean<SchedulerData> csv = new CsvToBean<SchedulerData>();			
		List<SchedulerData> scheduleList = csv.parse(strat, sr);	
		
    	return (ArrayList<SchedulerData>) (scheduleList);
    }
    
    public ArrayList<WellDataBean> extractWellData(String lines){
    	StringReader wellDataReader = new StringReader(lines);
		HeaderColumnNameTranslateMappingStrategy<WellDataBean> strat = new HeaderColumnNameTranslateMappingStrategy<WellDataBean>();
					
		Map<String, String> cmap = new HashMap<String, String>();	
	    // these are included in both modalities :
		cmap.put("Well", "well");
	    cmap.put("isPositionFirst", "isPositionFirst"); //acquisition order XYZCt or CXYZt
	    cmap.put("Array", "arraySize");
	    cmap.put("PFS", "perfectFocusOffset");

	    // Channels
        cmap.put("Channels","channels");
	   	cmap.put("Excitation Intensity", "excitationIntensity");
	    cmap.put("Exposures", "exposures");

	    //Z-Stack
	    cmap.put("Z Height", "zHeight");
	   cmap.put("Z Step Size", "zStepSize");

		cmap.put("Objective", "objective");
		cmap.put("Overlap", "overlap");
		cmap.put("Timelapse", "timelapse");

	    strat.setType(WellDataBean.class);
	    strat.setColumnMapping(cmap);

		CsvToBean<WellDataBean> csv = new CsvToBean<WellDataBean>();
		List<WellDataBean> wellDataList = csv.parse(strat, wellDataReader);
		logger.info("Number of Well entries: " + wellDataList.size());

    	return (ArrayList<WellDataBean>) (wellDataList);
    }

}
