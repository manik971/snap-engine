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

package org.esa.snap.gpf.operators.standard;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.framework.dataio.ProductIO;
import org.esa.snap.framework.dataio.ProductReader;
import org.esa.snap.framework.datamodel.Band;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.datamodel.ProductData;
import org.esa.snap.framework.gpf.Operator;
import org.esa.snap.framework.gpf.OperatorException;
import org.esa.snap.framework.gpf.OperatorSpi;
import org.esa.snap.framework.gpf.Tile;
import org.esa.snap.framework.gpf.annotations.OperatorMetadata;
import org.esa.snap.framework.gpf.annotations.Parameter;
import org.esa.snap.framework.gpf.annotations.TargetProduct;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

/**
 * Reads the specified file as product. This operator may serve as a source node in processing graphs,
 * especially if multiple data products need to be read in.
 * <p>
 * Here is a sample of how the <code>Read</code> operator can be integrated as a node within a processing graph:
 * <pre>
 *    &lt;node id="readNode"&gt;
 *        &lt;operator&gt;Read&lt;/operator&gt;
 *        &lt;parameters&gt;
 *            &lt;file&gt;/eodata/SST.nc&lt;/file&gt;
 *        &lt;/parameters&gt;
 *    &lt;/node&gt;
 * </pre>
 *
 * @author Norman Fomferra
 * @author Marco Zuehlke
 * @since BEAM 4.2
 */
@OperatorMetadata(alias = "Read",
                  category = "Input-Output",
                  version = "1.1",
                  authors = "Marco Zuehlke, Norman Fomferra",
                  copyright = "(c) 2010 by Brockmann Consult",
                  description = "Reads a product from disk.")
public class ReadOp extends Operator {

    // (A) Make this a Path object
    @Parameter(description = "The file from which the data product is read.", notNull = true, notEmpty = true)
    private File file;
    @TargetProduct
    private Product targetProduct;

    private transient ProductReader productReader;

    @Override
    public void initialize() throws OperatorException {
        if (file == null) {
            throw new OperatorException("'file' parameter must be set");
        }
        try {
            final ProductReader productReader = ProductIO.getProductReaderForInput(file);
            if (productReader == null) {
                throw new OperatorException("No product reader found for file " + file);
            }
            // (B) If (A) is done:
            // targetProduct.setSourceLocation(file.toURI());
            // targetProduct.setFileLocation(file.toFile());
            targetProduct = productReader.readProductNodes(file, null);
            targetProduct.setFileLocation(file);
            this.productReader = productReader;
        } catch (IOException e) {
            throw new OperatorException(e);
        }
    }

    @Override
    public void computeTile(Band band, Tile targetTile, ProgressMonitor pm) throws OperatorException {

        ProductData dataBuffer = targetTile.getRawSamples();
        Rectangle rectangle = targetTile.getRectangle();
        try {
            productReader.readBandRasterData(band, rectangle.x, rectangle.y, rectangle.width,
                                             rectangle.height, dataBuffer, pm);
            targetTile.setRawSamples(dataBuffer);
        } catch (IOException e) {
            throw new OperatorException(e);
        }
    }

    public static class Spi extends OperatorSpi {
        public Spi() {
            super(ReadOp.class);
        }
    }
}
