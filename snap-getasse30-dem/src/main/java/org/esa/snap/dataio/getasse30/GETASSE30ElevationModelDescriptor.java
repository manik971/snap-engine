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
package org.esa.snap.dataio.getasse30;

import org.esa.snap.framework.datamodel.GeoPos;
import org.esa.snap.framework.dataop.dem.AbstractElevationModelDescriptor;
import org.esa.snap.framework.dataop.dem.ElevationModel;
import org.esa.snap.framework.dataop.maptransf.Datum;
import org.esa.snap.framework.dataop.resamp.Resampling;

import java.io.IOException;
import java.net.URL;

public class GETASSE30ElevationModelDescriptor extends AbstractElevationModelDescriptor {

    private static final String NAME = "GETASSE30";
    private static final String DB_FILE_SUFFIX = ".GETASSE30";
    private static final int NUM_X_TILES = 24;
    private static final int NUM_Y_TILES = 12;
    private static final int DEGREE_RES = 15;
    private static final int PIXEL_RES = 1800;
    public static final int NO_DATA_VALUE = -9999;
    private static final GeoPos RASTER_ORIGIN = new GeoPos(90.0f, 180.0f);
    private static final int RASTER_WIDTH = NUM_X_TILES * PIXEL_RES;
    private static final int RASTER_HEIGHT = NUM_Y_TILES * PIXEL_RES;

    private static final Datum DATUM = Datum.WGS_84;

    public GETASSE30ElevationModelDescriptor() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Datum getDatum() {
        return DATUM;
    }

    @Override
    public int getNumXTiles() {
        return NUM_X_TILES;
    }

    @Override
    public int getNumYTiles() {
        return NUM_Y_TILES;
    }

    @Override
    public float getNoDataValue() {
        return NO_DATA_VALUE;
    }

    @Override
    public int getRasterWidth() {
        return RASTER_WIDTH;
    }

    @Override
    public int getRasterHeight() {
        return RASTER_HEIGHT;
    }

    @Override
    public GeoPos getRasterOrigin() {
        return RASTER_ORIGIN;
    }

    @Override
    public int getDegreeRes() {
        return DEGREE_RES;
    }

    @Override
    public int getPixelRes() {
        return PIXEL_RES;
    }

    @Override
    public boolean isDemInstalled() {
        return true;
    }

    @Override
    public URL getDemArchiveUrl() {
        return null;
    }

    @Override
    public ElevationModel createDem(Resampling resampling) {
        try {
            return new GETASSE30ElevationModel(this, resampling);
        } catch (IOException e) {
            return null;
        }
    }

    public String createTileFilename(int minLat, int minLon) {
        String latString = minLat < 0 ? Math.abs(minLat) + "S" : minLat + "N";
        while (latString.length() < 3) {
            latString = "0" + latString;
        }
        String lonString = minLon < 0 ? Math.abs(minLon) + "W" : minLon + "E";
        while (lonString.length() < 4) {
            lonString = "0" + lonString;
        }
        return latString + lonString + DB_FILE_SUFFIX;
    }

}
