package edu.gladstone.hts.plate;

/**
 * Created by robo5 on 10/5/2017.
 */
public class perkinElmer384CellCarrierConfigurerExtStageR5 {
    static SBSPlate configurePlate(SBSPlate plate) {
        plate.togglePFS = false;
        plate.setWELL_SPACING(4500);//From Specs
        plate.setPLATE_HEIGHT(4000);
        //int xfid  = 8278;//+203;//-109882;
        //int yfid  = -73924;//+25;//9415;
        //with ASI stage latch
        int xCenter  = 64034;//8278;//+203;//-109882;
        int yCenter  = -86800;//-73924;//+25;//9415;
        int xfid = 65534;
        int yfid = -85317;
        plate.setX_DISPLACEMENT_HOME_TO_FIDUCIARY(xfid);
        plate.setY_DISPLACEMENT_HOME_TO_FIDUCIARY(yfid);
        plate.setX_DISPLACEMENT_HOME_TO_CENTER_A1(xCenter);
        plate.setY_DISPLACEMENT_HOME_TO_CENTER_A1(yCenter);
        plate.setX_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(xCenter-xfid);
        plate.setY_POSITION_FIDUCIARY_RELATIVE_TO_CENTER_A1(yCenter-yfid);
        plate.setPFS(142);
        return plate;
    }
}
