package edu.gladstone.hts.imaging;

import edu.gladstone.hts.microscope.Image;
import edu.gladstone.hts.parameters.WellDataBean;
import edu.gladstone.hts.microscope.Core;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ImageCalculator;
import ij.process.ImageProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class Fiduciary {
    private final WellDataBean fiduciaryData;
    private final int xBin = 5;
    private final int yBin = 5;
    private Core core_;

    final Logger logger = LoggerFactory.getLogger(Fiduciary.class);

    public Fiduciary(WellDataBean fiduciary) {

        this.fiduciaryData = fiduciary;
    }

    public Point2D.Double findFiduciaryOffset() {
        if (this.fiduciaryData.getBase().getTimepoint().equalsIgnoreCase("T0")) {
            logger.info("First Timepoint");
            return new Point2D.Double(0, 0);
        } else {
            ImagePlus firstFiduciary = openFiduciary("T0");
            ImagePlus currentFiduciary = openFiduciary(this.fiduciaryData.getBase().getTimepoint());
            ImagePlus fiduciaryStack = makeStack(firstFiduciary,
                    currentFiduciary);
            //ImagePlus enhancedFiduciaryStack = enhance(fiduciaryStack);
            //ImagePlus alignedFiduciaryStack = LinearSIFTalignment(fiduciaryStack);
            ImagePlus alignedFiduciaryStack = StackRegAlignment(fiduciaryStack);
            Point2D.Double offset = findOffset(alignedFiduciaryStack);
            Image.saveImage(alignedFiduciaryStack, this.fiduciaryData.getSavepath(), this.fiduciaryData.getBase(), "FIDUCIARY", "STACK", "Transmission","Brightfield","0","0","0","0");
            return offset;
        }
    }

    private ImagePlus openFiduciary(String timepoint) {
        File f = new File(this.fiduciaryData.getSavepath());
        String[] totalFiles = f.list();
        ImagePlus imp = findFiduciary(totalFiles, timepoint);
        imp.show();
        return imp;
    }

    private ImagePlus findFiduciary(String[] totalFiles, String timepoint) {
        String imageName = null;
        ImagePlus imp = null;
        for (String item : totalFiles) {
            String suffix = "MONTAGE-Brightfield";
            String nameAndTimepoint = this.fiduciaryData.getBase().getName() + "_" + timepoint;
            logger.info("Looking for file: "+ nameAndTimepoint);
            if (item.contains(suffix) && item.contains(nameAndTimepoint)) {
                logger.info("Found: "+ nameAndTimepoint);
                imageName = item;
            }
        }

        try {
            File fiduciary = new File(this.fiduciaryData.getSavepath(), imageName); // Will throw a null exception if no imageName exists
            imp = new ij.ImagePlus(fiduciary.getAbsolutePath());
        } catch (Exception e) {
        	logger.error("Unable to open fiduciary image. Path : " + this.fiduciaryData.getSavepath() +  "\tName: " +  imageName);
            throw new RuntimeException("Error opening fidicuary image", e.getCause());
        }
        return imp;
    }

    private ImagePlus makeStack(ImagePlus firstImp, ImagePlus secondImp) {
        ImageProcessor firstProcessor = firstImp.getProcessor();
        ImageProcessor secondProcessor = secondImp.getProcessor();
        int width = firstProcessor.getWidth();
        int height = firstProcessor.getHeight();
        ImageStack alignmentStack = new ij.ImageStack(width, height);
        alignmentStack.addSlice(firstProcessor);
        alignmentStack.addSlice(secondProcessor);
        ImagePlus alignmentImp = new ImagePlus("Alignment_Stack.tif",
                alignmentStack);
        firstImp.close();
        secondImp.close();
        alignmentImp.show();
        return alignmentImp;
    }

    public ImagePlus enhance(ImagePlus originalFiduciaryStack) {
        ImagePlus small = originalFiduciaryStack.duplicate();
        small.setTitle("Small.tif");
        ImagePlus large = originalFiduciaryStack.duplicate();
        large.setTitle("Large.tif");
        ij.IJ.run(small, "Gaussian Blur...", "sigma=50 stack");
        ij.IJ.run(large, "Gaussian Blur...", "sigma=150 stack");
        ImageCalculator calc = new ij.plugin.ImageCalculator();
        ImagePlus filteredStack = calc.run("Subtract create stack", small, large);
        filteredStack.show();
        ij.IJ.run(filteredStack, "Min...", "value=1 stack"); //Offset needs the image to be non-zero
        filteredStack.changes = false;
        originalFiduciaryStack.close();
        small.changes = false;
        large.changes = false;
        small.close();
        large.close();
        return filteredStack;

    }

    // Having troubles with SIFT alignment on CCB plates
    @SuppressWarnings("unused")
	private ImagePlus LinearSIFTalignment(ImagePlus originalFiduciaryStack) {
        ij.IJ.run(originalFiduciaryStack,
                "Linear Stack Alignment with SIFT",
                "initial_gaussian_blur=1.60 "
                        + "steps_per_scale_octave=3 minimum_image_size=64 maximum_image_size=1024 "
                        + "feature_descriptor_size=4 feature_descriptor_orientation_bins=8 "
                        + "closest/next_closest_ratio=0.92 maximal_alignment_error=25 "
                        + "inlier_ratio=0.05 expected_transformation=Translation interpolate");
        ImagePlus alignedFiduciary = ij.WindowManager
                .getImage("Aligned 2 of 2");
        originalFiduciaryStack.close();
        return alignedFiduciary;
    }

    private ImagePlus StackRegAlignment(ImagePlus originalFiduciaryStack) {
        originalFiduciaryStack.setSlice(1); //Needs to be on first so second slice is aligned to it. Otherwise slice 1 is aligned to 2.
        ij.IJ.run(originalFiduciaryStack, "Bin...", "x=" + Integer.toString(xBin) + " y=" + Integer.toString(yBin) + " z=1 bin=Average");
        ij.IJ.run(originalFiduciaryStack, "StackReg", "transformation=Translation");
        ij.IJ.run("Duplicate...", "title=[Aligned 2 of 2] duplicate range=1-2");
        ImagePlus alignedFiduciary = ij.WindowManager
                .getImage("Aligned 2 of 2");
        originalFiduciaryStack.close();
        return alignedFiduciary;
    }

    private Point2D.Double findOffset(ImagePlus alignedStack) {
        ImageStack stack = alignedStack.getStack();
        ImageProcessor ip = stack.getProcessor(2);
        int height = ip.getHeight();
        int width = ip.getWidth();
        ArrayList<Integer> xZeros = new ArrayList<Integer>();
        for (int i = 1; i < height; i++) {
            double[] line = ip.getLine(0, i, width, i);
            ArrayList<Double> lineList = new ArrayList<Double>();
            for (double entry : line) {
                lineList.add(entry);
            }
            int zeros = Collections.frequency(lineList, 0.0);
            xZeros.add(zeros);
        }
        ArrayList<Integer> yZeros = new ArrayList<Integer>();
        for (int j = 1; j < width; j++) {
            double[] line = ip.getLine(j, 0, j, height);
            ArrayList<Double> lineList = new ArrayList<Double>();
            for (double entry : line) {
                lineList.add(entry);
            }
            int zeros = Collections.frequency(lineList, 0.0);
            yZeros.add(zeros);
        }
        Collections.sort(xZeros);
        Integer xMedian = xZeros.get(height / 2);
        Collections.sort(yZeros);
        Integer yMedian = yZeros.get(height / 2);
        double[] xLine = ip.getLine(0, 0, 0, height);
        double[] yLine = ip.getLine(0, 0, width, 0);
        ArrayList<Double> xList = new ArrayList<Double>();
        for (double entry : xLine) {
            xList.add(entry);
        }
        ArrayList<Double> yList = new ArrayList<Double>();
        for (Double entry : yLine) {
            yList.add(entry);
        }
        int xSign = -1; 
        int ySign = 1; 
/*        if (Collections.max(xList) > 0) {
            xSign = -1;
        }
        if (Collections.max(yList) > 0) {
            ySign = 1;
        }*/
        //double xOffset = xBin * xMedian * xSign * this.fiduciaryData.getObjectiveCamera().getMICRON_PER_PIXEL();
        //double yOffset = yBin * yMedian * ySign * this.fiduciaryData.getObjectiveCamera().getMICRON_PER_PIXEL();
        //double xOffset = xBin * xMedian * xSign * this.fiduciaryData.camera.getPixelsX();
        //double yOffset = yBin * yMedian * ySign * this.fiduciaryData.camera.getPixelsY();
        double xOffset = xBin * xMedian * xSign * 0.648; //core_.innerCore.getPixelSizeUm();
        double yOffset = yBin * yMedian * ySign * 0.648;//core_.innerCore.getImageHeight();

        Point2D.Double offset = new Point2D.Double( xOffset, yOffset);
        logger.info("Offset calculated by SIFT");
        logger.info("X Offset: {}", Integer.toString((int) xOffset));
        logger.info("Y Offset: {}", Integer.toString((int) yOffset));

        return offset;
    }

}
