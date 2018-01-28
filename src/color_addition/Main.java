package color_addition;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Main extends Application {

    private Colors colors = new Colors();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        int numColors = 2;
        List<DoubleProperty[]> hsbList = colors.getHsbColorList();
        for (int x = 0; x < numColors; x++) {
            hsbList.add(new DoubleProperty[]{
                    new SimpleDoubleProperty(),
                    new SimpleDoubleProperty(),
                    new SimpleDoubleProperty()
            });
        }
        init(primaryStage);
        hsbList.forEach(hsb -> {
            hsb[0].setValue(Math.random() * 360);
            hsb[1].setValue(Math.random());
            hsb[2].setValue(Math.random());
        });
    }

    private void addColorControl_hsb(DoubleProperty[] hsb, Pane container) {
        Rectangle rectangle = new Rectangle(20, 20);
        rectangle.setArcHeight(14);
        rectangle.setArcWidth(14);
        Tooltip colorButtonTooltip = new Tooltip("randomize color");
        Button colorButton = new Button();
        colorButton.setGraphic(rectangle);
        colorButton.setPadding(new Insets(4));
        colorButton.setTooltip(colorButtonTooltip);
        colorButton.setOnAction(actionEvent -> {
            hsb[0].setValue(Math.random() * 360);
            hsb[1].setValue(Math.random());
            hsb[2].setValue(Math.random());
        });

        Label hueText = new Label("hue");
        Label satText = new Label("sat");
        Label brtText = new Label("brt");
        Label hue = new Label("", hueText);
        Label sat = new Label("", satText);
        Label brt = new Label("", brtText);
        Tooltip hueTooltip = new Tooltip("scroll hue");
        Tooltip satTooltip = new Tooltip("scroll saturation");
        Tooltip brtTooltip = new Tooltip("scroll brightness");
        hue.setTooltip(hueTooltip);
        sat.setTooltip(satTooltip);
        brt.setTooltip(brtTooltip);
        hue.setOnScroll(se -> {
            double hueVal = (hsb[0].doubleValue() + se.getDeltaY() * 0.05) % 360;
            if (hueVal < 0) hueVal += 360;
            hsb[0].setValue(hueVal);
        });
        sat.setOnScroll(se -> {
            double satVal = hsb[1].doubleValue() * ((se.getDeltaY() < 0) ? 0.92 : 1.08);
            if (satVal > 1) satVal = 1;
            hsb[1].setValue(satVal);
        });
        brt.setOnScroll(se -> {
            double brtVal = hsb[2].doubleValue() * ((se.getDeltaY() < 0) ? 0.92 : 1.08);
            if (brtVal > 1) brtVal = 1;
            hsb[2].setValue(brtVal);
        });

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setPadding(new Insets(8));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.addRow(0, colorButton, hue, sat, brt);
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
            if (observable == null) {
                System.err.println("Null source updating color control?!");
            } else if (observable.equals(hsb[0])) {
                hue.setText("" + ((int) (hsb[0].doubleValue() * 10)) / 10.0);
            } else if (observable.equals(hsb[1])) {
                sat.setText("" + ((int) (hsb[1].doubleValue() * 100)) / 100.0);
            } else if (observable.equals(hsb[2])) {
                brt.setText("" + ((int) (hsb[2].doubleValue() * 100)) / 100.0);
            }
        };

        hsb[0].addListener(updateColor);
        hsb[1].addListener(updateColor);
        hsb[2].addListener(updateColor);
    }

    private Parent getGraph(List<DoubleProperty[]> colorList,
                            int sizeWidth,
                            int sizeHeight,
                            int sizeMargin,
                            Color backGround) {

        int graphWidth = sizeWidth - 2 * sizeMargin;
        int graphHeight = sizeHeight - 2 * sizeMargin;

        Consumer<double[]> graphFit = point -> {
            point[0] = point[0] * graphWidth + sizeMargin;
            point[1] = point[1] * graphHeight + sizeMargin;
        };

        Consumer<GraphicsContext> drawBackground = gc -> {
            gc.setFill(backGround);
            gc.fillRect(0, 0, sizeWidth, sizeHeight);
        };

        BiConsumer<GraphicsContext, Pair<double[], Color>> drawLine = (gc, coloredLine) -> {
            gc.setStroke(coloredLine.getValue());
            gc.setLineDashes();
            gc.setLineWidth(2);
            double[] toDraw = coloredLine.getKey();
            double[][] points = new double[2][2];
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
        };

        Consumer<GraphicsContext> drawAxises = gc -> {
            gc.setStroke(Color.BLACK);
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setLineDashes(4, 6);
            gc.setLineWidth(2);
            double[][] points = new double[2][2];
            points[0][0] = 0;
            points[0][1] = 0;
            points[1][0] = 1;
            points[1][1] = 1;
            graphFit.accept(points[0]);
            graphFit.accept(points[1]);
            gc.strokeLine(points[0][0], points[0][1], points[1][0], points[0][1]);
            gc.strokeLine(points[0][0], points[0][1], points[0][0], points[1][1]);
        };

        Consumer<GraphicsContext> drawAll = gc -> {
            drawBackground.accept(gc);
            colors.getHsbColorList()
                    .forEach(colorData -> {
                        double[] point = new double[]{1, 0};
                        graphFit.accept(point);
                        drawLine.accept(gc, new Pair<>(
                                colors.colorBreakdown(colorData, (int) point[0]),
                                colors.getColor(colorData)
                        ));
                    });
            drawAxises.accept(gc);
        };

        Canvas graph = new Canvas(sizeWidth, sizeHeight);
        graph.setScaleY(-1);
        InvalidationListener graphRedraw =
                observable -> drawAll.accept(graph.getGraphicsContext2D());
        colors.getHsbColorList()
                .forEach(hsb -> {
                    hsb[0].addListener(graphRedraw);
                    hsb[1].addListener(graphRedraw);
                    hsb[2].addListener(graphRedraw);
                });
        Platform.runLater(() -> graphRedraw.invalidated(null));
        GridPane container = new GridPane();
        container.addRow(0, graph);
        return container;
    }

    private void init(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(8));
        FlowPane topPane = new FlowPane();
        topPane.setVgap(8);
        topPane.setHgap(8);
        topPane.setAlignment(Pos.CENTER);
        root.setTop(topPane);

        List<DoubleProperty[]> hsbList = colors.getHsbColorList();
        hsbList.forEach(hsb -> addColorControl_hsb(hsb, topPane));
        Parent graph = getGraph(hsbList,
                880,
                520,
                10,
                new Color(1, 0.95, 0.90, 1));

        root.setCenter(graph);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
