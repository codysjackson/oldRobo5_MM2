package edu.gladstone.hts.plate;

/**
 * Created with IntelliJ IDEA.
 * User: Mike Ando
 * Date: 7/15/13
 * Time: 4:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class perkinelmer96CellCarrierConfigurer {
    static SBSPlate configurePlate(SBSPlate plate) {
        plate.togglePFS = false;
        plate.setWELL_SPACING(9000);
        plate.setPLATE_HEIGHT(6700);
        int centerA1X = 15778;
        int centerA1Y = -70875;
        int xFid  = 16471;
        int yFid  = -68711;
        plate.setX_DISPLACEMENT_HOME_TO_FIDUCIARY(xFid); 
        plate.setY_DISPLACEMENT_HOME_TO_FIDUCIARY(yFid);
        plate.setX_DISPLACEMENT_HOME_TO_CENTER_A1(centerA1X); //(xFid - 3000);
        plate.setY_DISPLACEMENT_HOME_TO_CENTER_A1(centerA1Y); //(yFid - 3000);
        plate.setX_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(centerA1X-xFid) ;//(-3000);
        plate.setY_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(centerA1Y-yFid) ;//(-3000);
        plate.setPFS(168);
        return plate;
    }
}