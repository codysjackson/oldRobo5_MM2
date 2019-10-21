package edu.gladstone.hts.plate;

/**
 * Created with IntelliJ IDEA.
 * User: Mike Ando
 * Date: 7/15/13
 * Time: 4:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class perkinelmer384CellCarrierConfigurer {

    static SBSPlate configurePlate(SBSPlate plate) {
        plate.togglePFS = false;
        plate.setWELL_SPACING(4500);//From Specs
        plate.setPLATE_HEIGHT(4000);
        //int xfid  = 8278;//+203;//-109882;
        //int yfid  = -73924;//+25;//9415;
        //with ASI stage latch
        int xCenter  = 13231;//8278;//+203;//-109882;
        int yCenter  = -72777;//-73924;//+25;//9415;
        int xfid = 14785;
        int yfid = -71462;
        plate.setX_DISPLACEMENT_HOME_TO_FIDUCIARY(xfid); 
        plate.setY_DISPLACEMENT_HOME_TO_FIDUCIARY(yfid);
        plate.setX_DISPLACEMENT_HOME_TO_CENTER_A1(xfid - 1375);
        plate.setY_DISPLACEMENT_HOME_TO_CENTER_A1(yfid - 1375);
        plate.setX_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(-1375);
        plate.setY_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(-1375);
        plate.setPFS(160);
        return plate;
    }

}
