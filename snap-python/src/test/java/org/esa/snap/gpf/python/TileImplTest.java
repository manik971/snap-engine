package org.esa.snap.gpf.python;

import org.esa.snap.framework.datamodel.Band;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.gpf.internal.TileImpl;
import org.junit.Test;

import javax.media.jai.ImageLayout;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;

/**
 * @author Norman Fomferra
 */
public class TileImplTest {

    @Test
    public void testTileSetSamples() throws Exception {

        ImageLayout value = new ImageLayout();
        value.setTileWidth(200);
        value.setTileHeight(200);

        Product p = new Product("N", "T", 1121, 705);
        p.setPreferredTileSize(200, 200);
        Band b = p.addBand("b", "0");

        RenderedImage image = b.getSourceImage().getImage(0);



        Raster raster = image.getTile(3, 0);
        TileImpl tile = new TileImpl(b, raster);

        tile.setSamples(new float[200*200]);

    }
}
