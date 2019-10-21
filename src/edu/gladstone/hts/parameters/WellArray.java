package edu.gladstone.hts.parameters;

import edu.gladstone.hts.imaging.WellImager;


import java.awt.*;
import java.awt.geom.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WellArray {
    /*
    * Example array for a 4x3 montage with relative movement before imaging
    * wellArray = {(0,0), (1,0), (1,0), (1,0), (0,1), (-1,0), (-1, 0), (-1, 0), (0,1), (1,0), (1,0), (1,0)}
    */
    final Logger logger = LoggerFactory.getLogger(WellImager.class);
    // initialize the variables
    public Point2D.Double[] array;

    public WellArray() {
    }

    public Point2D.Double[] makeArray(int dim){
        //make the array
        logger.debug("making the array...");
        array = new Point2D.Double[dim*dim];
        // assign the first point
        logger.debug("assigning the first point...");
        int xcoord=0,ycoord=0;
        array[0]=new Point2D.Double(xcoord,ycoord);
        int i=0;
        logger.debug("making the rest of the array....");
        for (int y =0;y<dim;y++) {
            for (int x = 0; x < dim; x++) {
                if (i%dim==0){
                    xcoord = 0;
                    ycoord=1;
                }
                else{
                    xcoord = -1*(1-2*(y%2)); // -1 for correct direction of travel
                    ycoord=0;
                }
                if(i>=1){
                    array[i]= new Point2D.Double(xcoord,ycoord);
                    logger.debug("making array point"+ array[i].getX()+","+array[i].getY());
                }
                i++;
            }
        }
        logger.debug("done.");
        return array;
    }

    public Point2D.Double[] makeArray(int dimX, int dimY) {
        //make the array
        array = new Point2D.Double[dimX*dimY];
        // assign the first point
        int xcoord=0,ycoord=0;
        array[0]=new Point2D.Double(xcoord,ycoord);
        int i=0;
        for (int y=0;y<dimY;y++) {
            for (int x = 0; x < dimX; x++) {
                if (i%dimX==0){
                    xcoord = 0;
                    ycoord=1;
                }
                else{
                    xcoord = -1*(1-2*(y%2)); // -1 for correct direction of travel
                    ycoord=0;
                }
                if(i>=1){
                    array[i]= new Point2D.Double(xcoord,ycoord);
                }
                i++;
            }
        }
        return array;
    }



}
