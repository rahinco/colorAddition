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

}
