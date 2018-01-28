package color_addition;

public class MathX {

    public static double gaussianAtX(double x,
                                     double mean,
                                     double variance) {
        return Math.pow(
                Math.E, -(x - mean) * (x - mean)
                        / (2 * variance * variance)
        ) / Math.sqrt(2 * Math.PI * variance);
    }

    /*
    private static double[] gaussianCurveTest(int res) {
        double[] curve = new double[res];
        double x;
        for(int i=0; i<res; i++) {
            x = i * 1.0 / res;
            curve[i] = MathX.gaussianAtX(x, 0.5, 0.18);
        }
        return curve;
    }
    */

}
