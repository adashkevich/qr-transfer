/*
package com.pe.adashkevich.codetransfer;

import com.google.zxing.WriterException;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import java.awt.image.RenderedImage;

public class PNGCodeTransfer {

    private IIOMetadata createMetadata(RenderedImage image, ImageWriter imageWriter, ImageWriteParam writerParams, int resolution){
        ImageTypeSpecifier type;
        if (writerParams.getDestinationType() != null) {
            type=writerParams.getDestinationType();
        }
        else {
            type=ImageTypeSpecifier.createFromRenderedImage(image);
        }
        IIOMetadata meta=imageWriter.getDefaultImageMetadata(type,writerParams);
        return (addResolution(meta,resolution) ? meta : null);
    }

    public static void main(String[] args) {
        try {
            createMetadata(null, )
        } catch (Exception e) {
            System.out.println("Could not write png metadate :: " + e.getMessage());
        }
    }
}
*/
