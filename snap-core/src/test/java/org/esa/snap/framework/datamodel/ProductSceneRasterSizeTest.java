/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.snap.framework.datamodel;

import org.esa.snap.framework.dataio.ProductIO;
import org.esa.snap.framework.dataop.maptransf.Datum;
import org.junit.Ignore;
import org.junit.Test;

import javax.media.jai.operator.ConstantDescriptor;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.*;

public class ProductSceneRasterSizeTest {
    @Test
    public void testSizeChangeWithInitialSize() throws Exception {
        Product product = new Product("N", "T", 30, 20);
        assertEquals(new Dimension(30, 20), product.getSceneRasterSize());

        product.addBand(new Band("B0", ProductData.TYPE_FLOAT32, 5, 2));
        // todo - [multisize_products] or do we want to allow that a product can shrink after it has been initialized
        assertEquals(new Dimension(30, 20), product.getSceneRasterSize());

        product.addBand(new Band("B1", ProductData.TYPE_FLOAT32, 100, 200));
        assertEquals(new Dimension(100, 200), product.getSceneRasterSize());

        product.addBand(new Band("B2", ProductData.TYPE_FLOAT32, 110, 190));
        assertEquals(new Dimension(110, 200), product.getSceneRasterSize());

        product.addMask(Mask.BandMathsType.create("M1", "description1", 115, 210, "true", Color.BLACK, 0.0));
        assertEquals(new Dimension(115, 210), product.getSceneRasterSize());

        product.getMaskGroup().add(Mask.BandMathsType.create("M2", "description2", 120, 220, "true", Color.RED, 0.0));
        assertEquals(new Dimension(120, 220), product.getSceneRasterSize());

        product.addTiePointGrid(new TiePointGrid("TPG1", 500, 2, 0f, 0f, 1f, 1f, new float[1000]));
        assertEquals(new Dimension(500, 220), product.getSceneRasterSize());

        product.getTiePointGridGroup().add(new TiePointGrid("TPG2", 1000, 2, 0f, 0f, 1f, 1f, new float[2000]));
        assertEquals(new Dimension(1000, 220), product.getSceneRasterSize());
    }

    @Test
    public void testSizeChangeWithoutInitialSize() throws Exception {
        Product product = new Product("N", "T");
        assertNull(product.getSceneRasterSize());

        product.addBand(new Band("B1", ProductData.TYPE_FLOAT32, 100, 200));
        assertEquals(new Dimension(100, 200), product.getSceneRasterSize());

        product.addBand(new Band("B2", ProductData.TYPE_FLOAT32, 110, 190));
        assertEquals(new Dimension(110, 200), product.getSceneRasterSize());

        product.addMask(Mask.BandMathsType.create("M1", "description1", 115, 210, "true", Color.BLACK, 0.0));
        assertEquals(new Dimension(115, 210), product.getSceneRasterSize());

        product.getMaskGroup().add(Mask.BandMathsType.create("M2", "description2", 120, 220, "true", Color.RED, 0.0));
        assertEquals(new Dimension(120, 220), product.getSceneRasterSize());

        product.addTiePointGrid(new TiePointGrid("TPG1", 500, 2, 0f, 0f, 1f, 1f, new float[1000]));
        assertEquals(new Dimension(500, 220), product.getSceneRasterSize());

        product.getTiePointGridGroup().add(new TiePointGrid("TPG2", 1000, 2, 0f, 0f, 1f, 1f, new float[2000]));
        assertEquals(new Dimension(1000, 220), product.getSceneRasterSize());

    }

//    @Test
    @Ignore
    public void testDimap() throws Exception {

        Product product = new Product("N", "T");
        assertNull(product.getSceneRasterSize());

        Band b1 = new Band("B1", ProductData.TYPE_FLOAT32, 100, 200);
        b1.setSourceImage(ConstantDescriptor.create(100f, 200f, new Float[]{1f}, null));
        product.addBand(b1);

        Band b2 = new Band("B2", ProductData.TYPE_FLOAT32, 800, 190);
        b2.setSourceImage(ConstantDescriptor.create(800f, 190f, new Float[]{2f}, null));
        product.addBand(b2);

        product.addMask(Mask.BandMathsType.create("M1", "description1", 115, 210, "true", Color.BLACK, 0.0));
        product.getMaskGroup().add(Mask.BandMathsType.create("M2", "description2", 120, 220, "true", Color.RED, 0.0));

        product.addTiePointGrid(new TiePointGrid("TPG1", 500, 2, 0f, 0f, 1f, 1f, new float[1000]));
        product.getTiePointGridGroup().add(new TiePointGrid("TPG2", 1000, 2, 0f, 0f, 1f, 1f, new float[2000]));

        TiePointGrid latGrid = new TiePointGrid("latGrid", 2, 2, 0.5f, 0.5f, 500, 500, new float[]{54, 54, 56, 56});
        TiePointGrid lonGrid = new TiePointGrid("lonGrid", 2, 2, 0.5f, 0.5f, 500, 500, new float[]{9, 11, 9, 11});

        product.addTiePointGrid(latGrid);
        product.addTiePointGrid(lonGrid);

        product.setGeoCoding(new TiePointGeoCoding(latGrid, lonGrid, Datum.WGS_84));

        File file = new File("multisize_product.dim");
        try {
            ProductIO.writeProduct(product, file, "BEAM-DIMAP", false);
            Product product2 = ProductIO.readProduct(file);
            assertEquals(new Dimension(100, 200), product2.getBand("B1").getRasterSize());
            assertEquals(new Dimension(800, 190), product2.getBand("B2").getRasterSize());
            assertEquals(new Dimension(115, 210), product2.getMaskGroup().get("M1").getRasterSize());
            assertEquals(new Dimension(120, 220), product2.getMaskGroup().get("M2").getRasterSize());
            assertEquals(new Dimension(500, 2), product2.getTiePointGrid("TPG1").getRasterSize());
            assertEquals(new Dimension(1000, 2), product2.getTiePointGrid("TPG2").getRasterSize());
            assertEquals(new Dimension(1000, 220), product2.getSceneRasterSize());
        } finally {
            if(file.exists()) {
                Files.delete(file.toPath());
                File dataDir = new File(file.getAbsolutePath().replaceAll("dim", "data"));
                Files.walkFileTree(dataDir.toPath(), new PathTreeDeleter());
            }
        }
    }

    private static class PathTreeDeleter extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }
    }
}

