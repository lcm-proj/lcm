package lcm.util;

import java.awt.*;

/** Converts scalar values to RGB colors by interpolating from a user-provided look-up table. **/
public class ColorMapper
{
    /** Minimum/maximum value for mapped range (will be drawn opaquely). **/
    double minval;
    double maxval;
    int[] colors;

    /** If these bounds are exceeded, use transparent color **/
    double opaqueMax = Double.MAX_VALUE, opaqueMin=-Double.MAX_VALUE;

    public ColorMapper(int[] colors, double minval, double maxval)
    {
        this.colors=colors;
        this.minval=minval;
        this.maxval=maxval;
    }

    public void setMinMax(double minval, double maxval)
    {
        this.minval = minval;
        this.maxval = maxval;
    }

    public void setOpaqueMax(double opaqueMax)
    {
        this.opaqueMax = opaqueMax;
    }

    public void setOpaqueMin(double opaqueMin)
    {
        this.opaqueMin = opaqueMin;
    }

    public boolean isVisible(double v)
    {
        if (v > opaqueMax || v < opaqueMin)
            return false;
        return true;
    }

    public int map(double v)
    {
        if (!isVisible(v))
            return 0x00000000; // transparent

        double normval = (colors.length)*(v-minval)/(maxval-minval);

        int a = (int) Math.floor(normval);
        if (a<0)
            a=0;
        if (a>=colors.length)
            a=colors.length-1;

        int b = a + 1;
        if (b>=colors.length)
            b=colors.length-1;

        double frac = normval - a;
        if (frac<0)
            frac=0;
        if (frac>1)
            frac=1;

        int c=0;
        for (int i=0;i<4;i++)
	    {
            int r =i*8;
            int comp = (int) (((colors[a]>>r)&0xff)*(1-frac) + ((colors[b]>>r)&0xff)*frac);
            comp = comp & 0xff;
            c |= (comp<<r);
	    }

        // force opacity
        return c | 0xff000000;
    }
}
