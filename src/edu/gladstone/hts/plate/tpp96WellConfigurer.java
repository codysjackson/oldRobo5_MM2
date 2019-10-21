package edu.gladstone.hts.plate;

/**
 * Created with IntelliJ IDEA.
 * User: mikeando
 * Date: 4/21/13
 * Time: 6:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class tpp96WellConfigurer {

    static SBSPlate configurePlate(SBSPlate plate) {
        plate.togglePFS = false;
        plate.setWELL_SPACING(9000);
        plate.setPLATE_HEIGHT(6000);
        int centerA1X = 15778;
        int centerA1Y = -70875;
        int xFid  = 17778;
        int yFid  = -68875;
        plate.setX_DISPLACEMENT_HOME_TO_FIDUCIARY(xFid);
        plate.setY_DISPLACEMENT_HOME_TO_FIDUCIARY(yFid);
        plate.setX_DISPLACEMENT_HOME_TO_CENTER_A1(centerA1X); //(xFid - 3000);
        plate.setY_DISPLACEMENT_HOME_TO_CENTER_A1(centerA1Y); //(yFid - 3000);
        plate.setX_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(centerA1X-xFid) ;//(-3000);
        plate.setY_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(centerA1Y-yFid) ;//(-3000);
        plate.setPFS(555);
        return plate;
    }
}
