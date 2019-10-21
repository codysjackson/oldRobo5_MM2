package edu.gladstone.hts.plate;

/**
 * Created by robo5 on 8/12/2016.
 */
public class perkinelmer96CellCarrierConfigurerExtStageR5 {
    static SBSPlate configurePlate(SBSPlate plate) {
        plate.togglePFS = false;
        plate.setWELL_SPACING(9000);
        plate.setPLATE_HEIGHT(6700);
        int centerA1X = 66500;
        int centerA1Y = -84000;
        int xFid  = 68551;
        int yFid  = -82543;
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
