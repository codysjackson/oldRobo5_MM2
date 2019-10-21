package edu.gladstone.hts.plate;

/**
 * Created by Nikon-Demo on 11/22/2016.
 */
public class perkinelmer96WellSliceCultureConfigurer {
    static SBSPlate configurePlate(SBSPlate plate) {
        plate.togglePFS = false;
        plate.setWELL_SPACING(9000);
        plate.setPLATE_HEIGHT(5200);
        int centerA1X = 14328;
        int centerA1Y = -71444;
        int xFid = 18307;
        int yFid = -68901;
        plate.setX_DISPLACEMENT_HOME_TO_FIDUCIARY(xFid);
        plate.setY_DISPLACEMENT_HOME_TO_FIDUCIARY(yFid);
        plate.setX_DISPLACEMENT_HOME_TO_CENTER_A1(centerA1X); //(xFid - 3000);
        plate.setY_DISPLACEMENT_HOME_TO_CENTER_A1(centerA1Y); //(yFid - 3000);
        plate.setX_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(centerA1X - xFid);//(-3000);
        plate.setY_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(centerA1Y - yFid);//(-3000);
        plate.setPFS(124);
        return plate;
    }
}
