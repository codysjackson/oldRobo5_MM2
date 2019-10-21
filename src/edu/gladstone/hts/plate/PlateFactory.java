package edu.gladstone.hts.plate;

/**
 * Created with IntelliJ IDEA.
 * User: mikeando
 * Date: 4/21/13
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlateFactory {

    private final SupportedPlate plateType;

    public PlateFactory(SupportedPlate plateType) {
        this.plateType = plateType;
    }

    public SBSPlate createPlate() {
        SBSPlate plate = new SBSPlate();
        if (plateType.equals(SupportedPlate.tpp96Well)) {
            plate = tpp96WellConfigurer.configurePlate(plate);
        } else if (plateType.equals(SupportedPlate.axionMEA48Well)) {
            plate = axionMEA48WellConfigurer.configurePlate(plate);
        } else if (plateType.equals(SupportedPlate.perkinelmer96CellCarrier)) {
            plate = perkinelmer96CellCarrierConfigurer.configurePlate(plate);
        }else if (plateType.equals(SupportedPlate.perkinelmer96CellCarrierExtStageR5)) {
            plate = perkinelmer96CellCarrierConfigurerExtStageR5.configurePlate(plate);
        } else if(plateType.equals(SupportedPlate.perkinelmer96CellCarrierGlass)){
            plate = perkinelmer96CellCarrirerGlassConfigurer.configurePlate(plate);
        } else if(plateType.equals(SupportedPlate.perkinElmer96WellSliceCulture)){
            plate = perkinelmer96WellSliceCultureConfigurer.configurePlate(plate);
        }else if (plateType.equals(SupportedPlate.perkinelmer384CellCarrier)) {
            plate = perkinelmer384CellCarrierConfigurer.configurePlate(plate);
        }else if (plateType.equals(SupportedPlate.perkinElmer384CellCarrierExtStageR5)){
            plate = perkinElmer384CellCarrierConfigurerExtStageR5.configurePlate(plate);
        }
        return plate;
    }
}
