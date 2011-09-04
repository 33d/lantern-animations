/* Creates a fire effect.  Convert to an animated gif like this:
     $ convert -delay 10 fire-*.png fire.gif
*/

import java.nio.*
import java.awt.image.*
import java.awt.*
import javax.imageio.*

width = 36
height = 35
fuel = 2
border = 4
bwidth = width + (border*2)
frames = 512

colormap = (/fire.gpl/ as File).newReader() \
    .findAll { it =~ /^[\s0-9]{3}\s/ }.collect {
        it.trim().split(/\s+/)[0..2].collect { 
            it as int 
        }
    }.flatten() as byte[]

i = new BufferedImage(bwidth, height+fuel, 
    BufferedImage.TYPE_BYTE_GRAY)
g = i.graphics
r = new Random()
op = new ConvolveOp(new Kernel(3, 5, [
// Don't add up to 1, so the flame gets slightly darker as it rises
   0, 0.39,   0,
   0, 0.30,   0,
 0.1, 0.05, 0.1,
   0,    0,   0,
   0,    0,   0
] as float[]))

coloredImage = new BufferedImage(width, height, 
    BufferedImage.TYPE_BYTE_INDEXED, 
    new IndexColorModel(8, 256, colormap, 0, false))

frames.times { frameno ->
    // the fire goes out in the last few frames
    if (frameno < frames - height)
        (height..<height+fuel).each { y ->
            (0..<bwidth).each { x ->
                g.color = r.nextInt(256).with {
                    new Color(it, it, it)
                }
                g.fillRect(x, y, 1, 1)
            }
        }
    g.drawImage(i, op, 0, 0)

    // Copy the raw pixel data, so Java2D doesn't try to preserve the colours
    coloredImage.raster.setPixels(0, 0, width, height,
        i.raster.getPixels(border, 0, width, height, (int[]) null))
    ImageIO.write(coloredImage,
        "png", String.format("fire-%04d.png", frameno) as File)
}

