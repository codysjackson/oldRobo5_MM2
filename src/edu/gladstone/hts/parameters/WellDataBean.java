package edu.gladstone.hts.parameters;
import java.awt.Point;
import java.awt.geom.Point2D;
import edu.gladstone.hts.imaging.WellImager;

//enable logging
import loci.common.FileHandle;
//import org.micromanager.api.ScriptInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WellDataBean {
	private String well;

	private String isPositionFirst;
	private String arraySize;
	private String excitationIntensity;
	private String channels;
	private String exposures;
	private String perfectFocusOffset;
	private String zHeight;
	private String zStepSize;
	private Point center;
	private BaseName base;
    private String savepath;
    private String objective;
    private String overlap;
    private String timelapse;
	private String flatfieldBackground;
	public double umPerPx;
	public double[] pixelsXY = new double[2];

	// moving data from WellData to this class for simplification
	private WellArray wellArray = new WellArray();
	private WellParser wellParser;
	public Point2D.Double[] array;
	
    final Logger logger = LoggerFactory.getLogger(WellImager.class);

	public WellDataBean(){}

	public String getWell() {
		return well;
	}

	public void setWell(String well){
		this.well = well;
		this.setWellParser( new WellParser(this.well));
	}

	public void setWellParser(WellParser wellParser){
		this.wellParser = wellParser;
	}

	public WellParser getWellParser(){
		return this.wellParser;
	}

	public void setCenter(Point center){
			this.center = center;
	}

	public Point getCenter(){
		return this.center;
	}

	public String getIsPositionFirst() {
		return isPositionFirst;
	}

	public void setIsPositionFirst(String isPositionFirst){
		this.isPositionFirst = isPositionFirst;
	}

	public String getArraySize() {
		return arraySize;
	}

	public void setArraySize(String arraySize){
		this.arraySize=arraySize;
	}

	public Point2D.Double[] getArray(){
		String[] tempArray = arraySize.split("x");
		if(tempArray.length == 1){
			//int size = Integer.parseInt(tempArray[0]);
			array = wellArray.makeArray(Integer.parseInt(tempArray[0]));
		}
		else if(tempArray.length == 2){
			array = wellArray.makeArray(Integer.parseInt(tempArray[0]),Integer.parseInt(tempArray[1]));
		}
		else{
			throw new RuntimeException("invalid array size given");
		}
		//array  = wellArray.makeArray(size);
		return array;
	}

	public String getPerfectFocusOffset(){
		return perfectFocusOffset;
	}

	public void setPerfectFocusOffset(String perfectFocusOffset){
		this.perfectFocusOffset = perfectFocusOffset;
	}
	
	public String getChannels(){
		return channels;
	}

	public void setChannels(String channels){
		this.channels = channels;
	}

	public String getExposures() {
		return exposures;
	}

	public void setExposures(String exposures){
		logger.debug("Setting Exposures: " + exposures);
		this.exposures = exposures;
	}

	public BaseName getBase() {
		return base;
	}

	public void setBase(BaseName bn){
		this.base = bn;
	}

	public String getSavepath() {
		return savepath;
	}

	public void setSavepath(String savepath){
		this.savepath = savepath;
	}

	public String getzHeight() {
		return zHeight;
	}

	public void setzHeight(String zHeight){
		logger.debug("zHeight Going in to WDB: "+zHeight);
		this.zHeight = zHeight;
	}

	public String getzStepSize() {
		return zStepSize;
	}

	public void setzStepSize(String zStepSize){
		logger.debug("zStepSize In WDB: " + zStepSize);
		this.zStepSize = zStepSize;
	}

	public String getObjective() {
		return objective;
	}

	public void setObjective(String objective){
		logger.debug("Setting objective in WDB: " + objective);
		this.objective = objective;
	}

    public String getTimelapse(){
    	return this.timelapse;
    	}

	public void setTimelapse(String timelapse){
		logger.debug("Setting Time lapse in WDB: " + timelapse);
		this.timelapse = timelapse;
	}

	public String getOverlap() {
		return overlap;
	}

	public void setOverlap(String overlap){
		this.overlap =overlap;
	}

	public String getExcitationIntensity() {
		return excitationIntensity;
	}

	public void setExcitationIntensity(String excitationIntensity){
		logger.debug("intensities: "+ excitationIntensity);
		this.excitationIntensity = excitationIntensity;
	}

	public double getUmPerPx(){
		double objFOV = getObjectiveFOV();
		//int camPx = (int)core_.getCameraPixels()[0];
		//umPerPx = objFOV / camPx;
		return umPerPx;
	}

	public double getObjectiveFOV(){
		String obj = getObjective();
		switch (obj){
			case("10X"): return 1320;
			case("20X"): return 660;
			case("40X"): return 330;
			case("60X"): return 180;
			default: return 1320; //default to 10X;

		}
	}

	public Point2D.Double[] getMontageOffsets(int MontageSize, long[] camPx, boolean fid){
		//set the initial direction of the snake path
		int dir=-1;
		// set the fiducial overlap to 0
		if(fid){setOverlap("0");}
		logger.debug("Overlap set to : "+ getOverlap());
		//set camera dimensions accounting for overlap
		int dimX = (int)(camPx[0]*(1 - Double.parseDouble(getOverlap())));
		int dimY = (int)(camPx[1]*(1 - Double.parseDouble(getOverlap())));
		// get the array size
		int numPositions = MontageSize*MontageSize;
		//set initial variables
		int y=0;
		// set the initial x location
		int x = dimX*(MontageSize - 1); //counting from 0,0
		int index = 0;
		Point2D.Double[] point = new Point2D.Double[numPositions];
		// make a list of all the positions in order of being visited.
		while(index<numPositions){
			point[index]= new Point2D.Double(x,y);
			logger.debug("put an image at2: " + x + "," + y);
			logger.debug("put point at: "+point[index]);
			if ((index>0 && index < numPositions - 1) && (x==0 || x == dimX*(MontageSize-1))){
				index++;
				dir*=-1;
				y+=dimY;
				point[index] = new Point2D.Double(x,y);
				logger.debug("put point at: "+point[index]);
				logger.debug("put an image at1: "+x+","+y);
			}
			x+=dir*dimX;
			index++;
		}
		return point;
	}
}

