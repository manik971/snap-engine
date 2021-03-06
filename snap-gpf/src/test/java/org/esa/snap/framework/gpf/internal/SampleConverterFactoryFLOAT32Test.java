/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package org.esa.snap.framework.gpf.internal;

import org.esa.snap.framework.datamodel.Band;
import org.esa.snap.framework.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class SampleConverterFactoryFLOAT32Test {

    private static Band band;
    private double maxRawValue;
    private double minRawValue;

    @Before
    public void beforeTest() {
        band = new Band("n", ProductData.TYPE_FLOAT32, 1, 1);
        band.setRasterData(band.createCompatibleRasterData());
        maxRawValue = Float.MAX_VALUE;
        minRawValue = -Float.MAX_VALUE;
    }


    @Test
    public void testToGeoPhysical_Unscaled() {
        SampleConverterFactory.SampleConverter sampleConverter = SampleConverterFactory.createConverter(band);
        assertNotNull(sampleConverter);

        assertEquals(maxRawValue, sampleConverter.toGeoPhysical(maxRawValue), 1.0e-6);
        assertEquals(-356, sampleConverter.toGeoPhysical(-356), 1.0e-6);
        assertEquals(0, sampleConverter.toGeoPhysical(0), 1.0e-6);
        assertEquals(16.0, sampleConverter.toGeoPhysical(16.0), 1.0e-6);
        assertEquals(minRawValue, sampleConverter.toGeoPhysical(minRawValue), 1.0e-6);
    }

    @Test
    public void testToGeoPhysical_Scaled() {
        double scalingFactor = 1.1;
        band.setScalingFactor(scalingFactor);

        SampleConverterFactory.SampleConverter sampleConverter = SampleConverterFactory.createConverter(band);
        assertNotNull(sampleConverter);

        assertEquals(maxRawValue * scalingFactor, sampleConverter.toGeoPhysical(maxRawValue), 1.0e-6);
        assertEquals(-356 * scalingFactor, sampleConverter.toGeoPhysical(-356), 1.0e-6);
        assertEquals(0 * scalingFactor, sampleConverter.toGeoPhysical(0), 1.0e-6);
        assertEquals(16.0 * scalingFactor, sampleConverter.toGeoPhysical(16.0), 1.0e-6);
        assertEquals(minRawValue * scalingFactor, sampleConverter.toGeoPhysical(minRawValue), 1.0e-6);
    }


    @Test
    public void testToRaw_Unscaled() {
        SampleConverterFactory.SampleConverter sampleConverter = SampleConverterFactory.createConverter(band);
        assertNotNull(sampleConverter);

        assertEquals(maxRawValue, sampleConverter.toRaw(maxRawValue), 1.0e-6);
        assertEquals(-356, sampleConverter.toRaw(-356), 1.0e-6);
        assertEquals(0, sampleConverter.toRaw(0), 1.0e-6);
        assertEquals(10.8, sampleConverter.toRaw(10.8), 1.0e-6);
        assertEquals(minRawValue, sampleConverter.toRaw(minRawValue), 1.0e-6);
    }

    @Test
    public void testToRaw_Scaled() {
        double scalingFactor = 1.1;
        band.setScalingFactor(scalingFactor);

        SampleConverterFactory.SampleConverter sampleConverter = SampleConverterFactory.createConverter(band);
        assertNotNull(sampleConverter);

        assertEquals(maxRawValue / 1.1, sampleConverter.toRaw(maxRawValue), 1.0e-6);
        assertEquals(-356 / 1.1, sampleConverter.toRaw(-356), 1.0e-6);
        assertEquals(0 / 1.1, sampleConverter.toRaw(0), 1.0e-6);
        assertEquals(10.8 / 1.1, sampleConverter.toRaw(10.8), 1.0e-6);
        assertEquals(minRawValue / 1.1, sampleConverter.toRaw(minRawValue), 1.0e-6);
    }

}
