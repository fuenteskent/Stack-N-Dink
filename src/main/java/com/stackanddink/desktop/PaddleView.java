package com.stackanddink.desktop;

import com.stackanddink.core.model.Player;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;

/**
 * Desktop-only rendering of a digital paddle.
 * This does not own player location or queue state.
 */
public final class PaddleView {

    private static final String HEAD =
        "M 23 6 C 12 6 7 14 7 25 L 7 51 " +
        "C 7 63 14 70 24 77 L 26 81 L 42 81 L 44 77 " +
        "C 54 70 61 63 61 51 L 61 25 C 61 14 56 6 45 6 Z";

    private static final String[] BASE = {
        "#e9b8a5", "#373c3a", "#a5b08b", "#454c52",
        "#dca193", "#434441", "#b8c19f", "#bb785b"
    };

    private static final String[] ACCENT = {
        "#f7e7d5", "#aeb59c", "#f0ead7", "#939a87",
        "#f1d4c2", "#b9b4a4", "#f7f0de", "#f4d3b0"
    };

    private PaddleView() {
    }

    public static VBox playerTile(Player player, int index) {
        Label label = new Label(player.name().toUpperCase());
        label.getStyleClass().add("paddle-name");
        label.setMaxWidth(100);
        label.setTooltip(new Tooltip(player.name()));

        Pane paddle = paddleArt(index, false);
        VBox tile = new VBox(4, label, paddle);
        tile.getStyleClass().add("player-tile");
        tile.setAlignment(Pos.TOP_CENTER);
        tile.setMinWidth(94);
        tile.setPrefWidth(94);
        tile.setMaxWidth(94);
        Tooltip.install(tile, new Tooltip("Player: " + player.name() + "\nDrag-and-drop will be added next."));
        return tile;
    }

    public static Pane emptySlotArt() {
        Pane shape = paddleArt(0, true);
        shape.setScaleX(0.80);
        shape.setScaleY(0.80);
        shape.setMouseTransparent(true);
        return shape;
    }

    /** Vector artwork; no internet or image files required. */
    private static Pane paddleArt(int index, boolean placeholder) {
        Pane canvas = new Pane();
        canvas.setMinSize(68, 110);
        canvas.setPrefSize(68, 110);
        canvas.setMaxSize(68, 110);

        Rectangle grip = new Rectangle(28, 77, 12, 29);
        grip.setArcWidth(5);
        grip.setArcHeight(5);

        SVGPath head = new SVGPath();
        head.setContent(HEAD);
        head.setStrokeWidth(1.6);

        if (placeholder) {
            grip.setFill(Color.TRANSPARENT);
            grip.setStroke(Color.web("#7e916d"));
            grip.getStrokeDashArray().addAll(5.0, 5.0);
            head.setFill(Color.TRANSPARENT);
            head.setStroke(Color.web("#7e916d"));
            head.getStrokeDashArray().addAll(6.0, 5.0);
            canvas.getChildren().addAll(grip, head);
            return canvas;
        }

        Color base = Color.web(BASE[Math.floorMod(index, BASE.length)]);
        Color accent = Color.web(ACCENT[Math.floorMod(index, ACCENT.length)]);
        grip.setFill(index % 3 == 1 ? Color.web("#303632") : Color.web("#e1d2bc"));
        grip.setStroke(Color.web("#4e493f"));
        grip.setStrokeWidth(1.3);

        Rectangle gripBand1 = new Rectangle(28, 89, 12, 2);
        Rectangle gripBand2 = new Rectangle(28, 96, 12, 2);
        gripBand1.setFill(Color.web("#9b8b77", 0.70));
        gripBand2.setFill(Color.web("#9b8b77", 0.70));

        head.setFill(new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, base.brighter()), new Stop(1, base.darker())
        ));
        head.setStroke(Color.web("#3d4038"));
        head.setEffect(new DropShadow(6, Color.web("#382d25", 0.24)));

        // Decorative diagonal stripes are clipped to the shape of the paddle.
        Pane decoration = new Pane();
        decoration.setPrefSize(68, 87);
        for (int offset = 0; offset < 2; offset++) {
            Line stripe = new Line(-3, 58 + offset * 13, 72, 9 + offset * 13);
            stripe.setStroke(accent);
            stripe.setStrokeWidth(index % 2 == 0 ? 8 : 5);
            stripe.setStrokeLineCap(StrokeLineCap.ROUND);
            stripe.setOpacity(index % 2 == 0 ? 0.90 : 0.65);
            decoration.getChildren().add(stripe);
        }
        SVGPath clippingShape = new SVGPath();
        clippingShape.setContent(HEAD);
        decoration.setClip(clippingShape);

        canvas.getChildren().addAll(grip, gripBand1, gripBand2, head, decoration);
        return canvas;
    }
}
