package edu.gladstone.hts.plate;

public class SBSPlate {
    private int WELL_SPACING;
    private int PLATE_HEIGHT;
    private int PFS;
    private int Y_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1 = 0;
    private int X_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1 = 0;
    private int Y_DISPLACEMENT_HOME_TO_FIDUCIARY = 0;
    private int X_DISPLACEMENT_HOME_TO_FIDUCIARY = 0;
    private int Y_DISPLACEMENT_HOME_TO_CENTER_A1;
    private int X_DISPLACEMENT_HOME_TO_CENTER_A1;
    public final int FIDUCIARY_EXPOSURE = 10;
    public final int FIDUCIARY_GAIN = 20;
    public final String FIDUCIARY_CHANNEL = "Brightfield";
    public boolean togglePFS = false;

    public void setWELL_SPACING(int spacing) {
        this.WELL_SPACING = spacing;
    }

    public int getWELL_SPACING() {
        return this.WELL_SPACING;
    }

    public void setPLATE_HEIGHT(int height){
        this.PLATE_HEIGHT = height;
    }

    public int getPLATE_HEIGHT(){
        return this.PLATE_HEIGHT;
    }

    public void setPFS(int pfs){
        PFS = pfs;
    }

    public int getPFS(){
        return this.PFS;
    }

    public void setY_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(int distance) {
        this.Y_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1 = distance;
    }

    public int getY_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1() {
        return this.Y_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1;
    }

    public void setX_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(int distance) {
        this.X_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1 = distance;
    }

    public int getX_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1() {
        return this.X_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1;
    }

    public void setY_DISPLACEMENT_HOME_TO_FIDUCIARY(int distance) {
        this.Y_DISPLACEMENT_HOME_TO_FIDUCIARY = distance;
    }

    public int getY_DISPLACEMENT_HOME_TO_FIDUCIARY() {
        return this.Y_DISPLACEMENT_HOME_TO_FIDUCIARY;
    }

    public void setX_DISPLACEMENT_HOME_TO_FIDUCIARY(int distance) {
        this.X_DISPLACEMENT_HOME_TO_FIDUCIARY = distance;
    }

    public int getX_DISPLACEMENT_HOME_TO_FIDUCIARY() {
        return this.X_DISPLACEMENT_HOME_TO_FIDUCIARY;
    }

    public void setY_DISPLACEMENT_HOME_TO_CENTER_A1(int distance) {
        this.Y_DISPLACEMENT_HOME_TO_CENTER_A1 = distance;
    }

    public int getY_DISPLACEMENT_HOME_TO_CENTER_A1() {
        return this.Y_DISPLACEMENT_HOME_TO_CENTER_A1;
    }

    public void setX_DISPLACEMENT_HOME_TO_CENTER_A1(int distance) {
        this.X_DISPLACEMENT_HOME_TO_CENTER_A1 = distance;
    }

    public int getX_DISPLACEMENT_HOME_TO_CENTER_A1() {
        return this.X_DISPLACEMENT_HOME_TO_CENTER_A1;
    }
}
