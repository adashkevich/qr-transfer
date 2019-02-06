package com.pe.adashkevich.codetransfer;

import com.sun.imageio.plugins.png.PNGMetadata;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class PNGImageTransfer {

    public static byte[] writeCustomData(BufferedImage buffImg, String key, String value) throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();

        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(TYPE_INT_RGB);

        //adding metadata
        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

        IIOMetadataNode textEntry = new IIOMetadataNode("tEXtEntry");
        textEntry.setAttribute("keyword", key);
        textEntry.setAttribute("value", value);

        IIOMetadataNode text = new IIOMetadataNode("tEXt");
        text.appendChild(textEntry);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_png_1.0");
        root.appendChild(text);

        metadata.mergeTree("javax_imageio_png_1.0", root);

        //writing the data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream stream = ImageIO.createImageOutputStream(baos);
        writer.setOutput(stream);
        writer.write(metadata, new IIOImage(buffImg, null, metadata), writeParam);
        stream.close();

        return baos.toByteArray();
    }

    public static String readCustomData(byte[] imageData, String key) throws IOException {
        ImageReader imageReader = ImageIO.getImageReadersByFormatName("png").next();

        imageReader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(imageData)), true);

        // read metadata of first image
        IIOMetadata metadata = imageReader.getImageMetadata(0);

        //this cast helps getting the contents
        PNGMetadata pngmeta = (PNGMetadata) metadata;
        NodeList childNodes = pngmeta.getStandardTextNode().getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            String keyword = node.getAttributes().getNamedItem("keyword").getNodeValue();
            String value = node.getAttributes().getNamedItem("value").getNodeValue();
            if(key.equals(keyword)){
                return value;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            BufferedImage img = ImageIO.read(Paths.get("C:\\Users\\Andrei Dashkevich\\Pictures\\me.png").toFile());


            Path resultImgPath = FileSystems.getDefault().getPath("./result.png");
            Path transferFilePath = Paths.get("C:\\Users\\Andrei Dashkevich\\IdeaProjects\\taxp-1-vat-compliance.zip");
            try (OutputStream fos = new FileOutputStream(resultImgPath.toFile())) {
                String textContent = new String(Files.readAllBytes(transferFilePath), CodeTransferCfg.QR_DATA_ENCODING);
                System.out.println(textContent.length());
                fos.write(writeCustomData(img, "test", textContent));
            }

            try (OutputStream fos = new FileOutputStream(new File("./taxp-1-vat-compliance.zip"))) {
                fos.write(readCustomData(Files.readAllBytes(resultImgPath), "test").getBytes(CodeTransferCfg.QR_DATA_ENCODING));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
