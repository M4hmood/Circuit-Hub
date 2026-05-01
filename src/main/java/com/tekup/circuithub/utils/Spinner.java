package com.tekup.circuithub.utils;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.util.Duration;

/**
 * Rotating circular progress indicator. Reusable in any async context —
 * drop Spinner.create() into your scene graph and call start()/stop().
 */
public class Spinner extends Group {

    private final RotateTransition rotate;

    public Spinner(double radius) {
        Arc arc = new Arc(0, 0, radius, radius, 0, 270);
        arc.setType(ArcType.OPEN);
        arc.setStroke(Color.web("#00FF9C"));
        arc.setStrokeWidth(3);
        arc.setFill(Color.TRANSPARENT);
        arc.setEffect(new DropShadow(10, Color.web("#00FF9C")));
        getChildren().add(arc);

        rotate = new RotateTransition(Duration.seconds(1.1), this);
        rotate.setByAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
    }

    public static Spinner create() { return new Spinner(18); }

    public void start() { rotate.play(); }
    public void stop()  { rotate.stop(); }
}
