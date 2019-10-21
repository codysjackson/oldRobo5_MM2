package edu.gladstone.hts.plate;

/**
 * Created by Nikon-Demo on 12/23/2016.
 */
public class axionMEA48WellConfigurer {
    static SBSPlate configurePlate(SBSPlate plate) {
        plate.togglePFS = false;
        plate.setWELL_SPACING(13080);
        plate.setPLATE_HEIGHT(5900);
        int centerA1X = 19042;//19083;
        int centerA1Y = -71816;//-72355;
        int xFid  = 20283;
        int yFid  = -71280;
        plate.setX_DISPLACEMENT_HOME_TO_FIDUCIARY(xFid);
        plate.setY_DISPLACEMENT_HOME_TO_FIDUCIARY(yFid);
        plate.setX_DISPLACEMENT_HOME_TO_CENTER_A1(centerA1X); //(xFid - 3000);
        plate.setY_DISPLACEMENT_HOME_TO_CENTER_A1(centerA1Y); //(yFid - 3000);
        plate.setX_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(centerA1X-xFid) ;//(-3000);
        plate.setY_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(centerA1Y-yFid) ;//(-3000);
        plate.setPFS(160);
        return plate;
    }
}
