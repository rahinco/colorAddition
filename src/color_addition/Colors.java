package color_addition;

import javafx.beans.property.DoubleProperty;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Colors {

    private List<DoubleProperty[]> hsbColorList = new ArrayList<>();

    public List<DoubleProperty[]> getHsbColorList() {
        return hsbColorList;
    }

    private double[] colorBreakdown(double hue, double sat, double brt, int res) {
        double[] out = new double[res];
        final double stdDevAtSat0 = 0.70;
        final double stdDev = stdDevAtSat0 * (1 - sat);
        final double variance = stdDev * stdDev;
        final double mean = hue / 360;
        int start = (int) ((mean - 3 * stdDev) * res + 0.5);
        if (start > 0) start = 0;
        int end = (int) ((mean + 3 * stdDev) * res + 0.5);
        if (end < res) end = res;
        for (int i = start; i < end; i++) {
            double x = i * 1.0 / res;
            int at = i;
            while (at >= res) at -= res;
            while (at < 0) at += res;
            out[at] += MathX.gaussianAtX(x, mean, variance);
        }
        double adjust = 1 / out[(int) (mean * res + 0.5)];
        for (int x = 0; x < res; x++) {
            out[x] *= adjust;
            out[x] = Math.sqrt(out[x]);
            out[x] *= brt;
        }
        return out;
    }

    /*
    public double[] colorBreakdown(double[] hsb, int res) {
        return colorBreakdown(hsb[0], hsb[1], hsb[2], res);
    }
    */

    public double[] colorBreakdown(DoubleProperty[] hsb, int res) {
        return colorBreakdown(
                hsb[0].doubleValue(),
                hsb[1].doubleValue(),
                hsb[2].doubleValue(),
                res);
    }

    /*
    public Color getColor(double hue, double sat, double brt) {
        return Color.hsb(hue, sat, brt);
    }

    public Color getColor(double[] hsb) {
        return Color.hsb(hsb[0], hsb[1], hsb[2]);
    }
    */

    public Color getColor(DoubleProperty[] hsb) {
        return Color.hsb(
                hsb[0].doubleValue(),
                hsb[1].doubleValue(),
                hsb[2].doubleValue());
    }

}
