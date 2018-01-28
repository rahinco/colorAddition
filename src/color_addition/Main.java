package color_addition;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

public class Main extends Application {

    private enum AddColors implements BiFunction<Color, Color, Color> {
        HSB_01;

        @Override
        public Color apply(Color clrA, Color clrB) {
            switch (this) {
                case HSB_01:
                    double[] hsbA = new double[]{ clrA.getHue(),
                                    clrA.getSaturation(),
                                    clrA.getBrightness() },
                            hsbB = new double[]{ clrB.getHue(),
                                            clrB.getSaturation(),
                                            clrB.getBrightness() };
                    // making assumptions about colour composition ...
                    // 1) hue could be considered the mean perceived frequency
                    // 2) saturation determines the variance from the mean frequency
                    // 3) brightness is the area under the graph (the multiplier of
                       // the gaussian outputs)
                    /*
                    BiFunction<double[], Double, double[]> breakUp = (hsb, resMul) -> {
                        int res = (int) (360 * resMul);
                        double[] array = new double[res];
                        Arrays.fill(array, hsb[2] / res);
                        
                    }
                    */
            }
            return null;
        }

    }

    private AddColors addColors;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        initAsColorPropertiesHSB(primaryStage);
    }

    private static double[] readDoubleProperties(DoubleProperty[] doubleProperties) {
        return Arrays.stream(doubleProperties)
                .mapToDouble(DoubleExpression::doubleValue)
                .toArray();
    }

    private DoubleProperty[] addColorControl_hsb(Pane container, Color lineColor) {
        DoubleProperty[] hsb = new DoubleProperty[]{
                new SimpleDoubleProperty(),
                new SimpleDoubleProperty(),
                new SimpleDoubleProperty()
        };
        Rectangle rectangle = new Rectangle(32, 32);
        rectangle.setArcHeight(20);
        rectangle.setArcWidth(20);
        Label hueText = new Label("hue:");
        Label satText = new Label("sat:");
        Label brtText = new Label("brt:");
        Label hue = new Label("", hueText);
        Label sat = new Label("", satText);
        Label brt = new Label("", brtText);
        hue.setOnScroll(se -> {
            double hueVal = (hsb[0].doubleValue() + se.getDeltaY() * 0.05) % 360;
            if(hueVal < 0) hueVal += 360;
            hsb[0].setValue(hueVal);
        });
        sat.setOnScroll(se -> {
            double satVal = hsb[1].doubleValue() * ((se.getDeltaY() < 0) ? 0.92 : 1.08);
            if(satVal > 1) satVal = 1;
            hsb[1].setValue(satVal);
        });
        brt.setOnScroll(se -> {
            double brtVal = hsb[2].doubleValue() * ((se.getDeltaY() < 0) ? 0.92 : 1.08);
            if(brtVal > 1) brtVal = 1;
            hsb[2].setValue(brtVal);
        });
        GridPane gridPane = new GridPane();
        gridPane.setVgap(8);
        gridPane.setHgap(8);
        gridPane.setPadding(new Insets(4));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.addRow(0, rectangle, hue, sat, brt);
        container.getChildren().add(gridPane);

        InvalidationListener updateColor = observable -> {
            Color newColor = Color.hsb(
                    hsb[0].doubleValue(),
                    hsb[1].doubleValue(),
                    hsb[2].doubleValue());
            rectangle.setFill(newColor);
            gridPane.setBorder(new Border(
                    new BorderStroke(
                            newColor,
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(16),
                            BorderStroke.MEDIUM)));
            if(observable.equals(hsb[0])) {
                hue.setText("" + ((int) (hsb[0].doubleValue() * 10)) / 10.0);
            } else if(observable.equals(hsb[1])) {
                sat.setText("" + ((int) (hsb[1].doubleValue() * 100)) / 100.0);
            } else if(observable.equals(hsb[2])) {
                brt.setText("" + ((int) (hsb[2].doubleValue() * 100)) / 100.0);
            }
        };
        hsb[0].addListener(updateColor);
        hsb[1].addListener(updateColor);
        hsb[2].addListener(updateColor);
        return hsb;
    }

    private void initAsColorPropertiesHSB(Stage primaryStage) {
        BorderPane borderPane = new BorderPane();
        FlowPane flowPane = new FlowPane();
        flowPane.setVgap(8);
        flowPane.setHgap(8);
        flowPane.setPadding(new Insets(8));
        flowPane.setAlignment(Pos.CENTER);
        borderPane.setTop(flowPane);

        int graphLines = 2;
        double firstLineHue = Math.random() * 360;
        double hueStep = 360.0 / graphLines;
        double lineSaturation = 0.8;
        double lineBrightness = 0.33;
        DoubleProperty[][] lineColorData = new DoubleProperty[graphLines][3];
        for(int x=0; x<graphLines; x++) {
            double lineHue = (firstLineHue + hueStep * x) % 360;
            Color lineColor = Color.hsb(lineHue, lineSaturation, lineBrightness);
            DoubleProperty[] colorData = addColorControl_hsb(flowPane, lineColor);
            lineColorData[x] = colorData;
        }

        Canvas graph = new Canvas(860, 500);
        graph.setScaleY(-1);
        double graphMargin = 10;
        double graphWidth = graph.getWidth() - 2 * graphMargin;
        double graphHeight = graph.getHeight() - 2 * graphMargin;
        Consumer<double[]> graphFit = point -> {
                point[0] = point[0] * graphWidth + graphMargin;
                point[1] = point[1] * graphHeight + graphMargin;
        };

        InvalidationListener graphRedraw = observable -> {
            GraphicsContext gc = graph.getGraphicsContext2D();
            gc.setFill(new Color(1, 0.95, 0.9, 1));
            gc.fillRect(0, 0, graph.getWidth(), graph.getHeight());
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            double[][] points = new double[][]{{0, 0}, {1, 1}};
            graphFit.accept(points[0]);
            graphFit.accept(points[1]);
            gc.strokeLine(points[0][0], points[0][1], points[1][0], points[0][1]);
            gc.strokeLine(points[0][0], points[0][1], points[0][0], points[1][1]);
            Arrays.stream(lineColorData)
                    .forEach(colorData -> {
                        double[] hsb = readDoubleProperties(colorData);
                        points[0][0] = 1;
                        points[0][0] = 1;
                        graphFit.accept(points[0]);
                        double[] toDraw = colorBreakdown(hsb, (int) points[0][1]);
                        gc.setStroke(Color.hsb(hsb[0], hsb[1], hsb[2]));
                        for (int x = 0; x < toDraw.length - 1; x++) {
                            points[0][0] = ((double) x) / toDraw.length;
                            points[0][1] = toDraw[x];
                            points[1][0] = ((double) x + 1) / toDraw.length;
                            points[1][1] = toDraw[x + 1];
                            graphFit.accept(points[0]);
                            graphFit.accept(points[1]);
                            gc.strokeLine(
                                    points[0][0],
                                    points[0][1],
                                    points[1][0],
                                    points[1][1]);
                        }
                    });
        };
        Arrays.stream(lineColorData)
                .forEach(hsb -> {
                    hsb[0].setValue(Math.random() * 360);
                    hsb[1].setValue(Math.random());
                    hsb[2].setValue(Math.random());
                    hsb[0].addListener(graphRedraw);
                    hsb[1].addListener(graphRedraw);
                    hsb[2].addListener(graphRedraw);
                });
        graphRedraw.invalidated(null);
        borderPane.setCenter(graph);

        Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);
        primaryStage.show();
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

    private static double[] colorBreakdown(double[] hsb, int res) {
        double[] out = new double[res];
        final double stdDevAtSat0 = 0.75;
        final double stdDev = stdDevAtSat0 * (1 - hsb[1]);
        final double variance = stdDev * stdDev;
        final double mean = hsb[0] / 360;
        int start = (int) ((mean - 3 * stdDev) * res + 0.5);
        if(start > 0) start = 0;
        int end = (int) ((mean + 3 * stdDev) * res + 0.5);
        if(end < res) end = res;
        for(int i=start; i<end; i++) {
            double x = i * 1.0 / res;
            int at = i;
            while(at >= res) at -= res;
            while(at < 0) at += res;
            out[at] += MathX.gaussianAtX(x, mean, variance);
        }
        double adjust = 1 / out[(int) (mean * res + 0.5)];
        for(int x = 0; x<res; x++) {
            out[x] *= adjust;
            out[x] = Math.sqrt(out[x]);
            out[x] *= hsb[2];
        }
        return out;
    }

    /*
    private void initAsColorAddition(Stage primaryStage) {
        FlowPane formulaSelection = new FlowPane();
        MenuButton formula = new MenuButton();
        FlowPane colorAddition = new FlowPane();
        VBox vBox = new VBox();
        colorAddition.setPadding(new Insets(8));
        colorAddition.setHgap(8);
        colorAddition.setVgap(8);
        ColorPicker colorPickerA = new ColorPicker();
        ColorPicker colorPickerB = new ColorPicker();
        ColorPicker colorPickerC = new ColorPicker(Color.BLACK);
        Label aPlusB = new Label("+");
        Label bPlusC = new Label("+");
        Label equals = new Label("=");
        Rectangle colorResult = new Rectangle(48, 20);
        colorAddition.getChildren().addAll(
                colorPickerA, aPlusB, colorPickerB,
                bPlusC, colorPickerC, equals, colorResult);

        Scene scene = new Scene(colorAddition);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    */
}
