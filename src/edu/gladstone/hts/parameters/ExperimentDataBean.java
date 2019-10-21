package edu.gladstone.hts.parameters;

public class ExperimentDataBean {
	private String experimentName;
	private BaseName baseName;
	private String author;
	private String description;
	private String plateType;
	private String microscope;
	private int plateBarcode;
	private int numberOfWells;
	private int incubator;
	private int stack;
	private int shelf;
	private String imageFolder;
	private String pfsPerTile;
	
	public ExperimentDataBean(){		
	}

	public String getExperimentName() {
		return experimentName;
	}

	public void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPlateType() {
		return plateType;
	}

	public void setPlateType(String plateType) {
		this.plateType = plateType;
	}

	public String getMicroscope() {
		return microscope;
	}

	public void setMicroscope(String microscope) {
		this.microscope = microscope;
	}

	public int getPlateBarcode() {
		return plateBarcode;
	}

	public void setPlateBarcode(int plateBarcode) {
		this.plateBarcode = plateBarcode;
	}

	public int getNumberOfWells() {
		return numberOfWells;
	}

	public void setNumberOfWells(int numberOfWells) {
		this.numberOfWells = numberOfWells;
	}

	public int getIncubator() {
		return incubator;
	}

	public void setIncubator(int incubator) {
		this.incubator = incubator;
	}

	public int getStack() {
		return stack;
	}

	public void setStack(int stack) {
		this.stack = stack;
	}

	public int getShelf() {
		return shelf;
	}

	public void setShelf(int shelf) {
		this.shelf = shelf;
	}

	public String getImageFolder() {
		return imageFolder;
	}

	public void setImageFolder(String imageFolder) {
		this.imageFolder = imageFolder;
	}

	public BaseName getBaseName(){
		return baseName;
	}

	public void setBaseName(BaseName bN){
		this.baseName = bN;
	}

	public void setPFSPerTile(String pfsPerTile){
		this.pfsPerTile = pfsPerTile;
	}

	public String getPFSPerTile(){
		return pfsPerTile;
	}
	
}