package com.ixume.alchemy;

import org.bukkit.util.Transformation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DisplayTransformation {
    public final Vector3f translation;
    public final Quaternionf leftRotation;
    public final Vector3f scale;
    public final Quaternionf rightRotation;
    private Matrix4f matrix;

    public Matrix4f getMatrix() {
        Matrix4f leftMatrix = new Matrix4f();
        leftRotation.get(leftMatrix);
        Matrix4f rightMatrix = new Matrix4f();
        rightRotation.get(rightMatrix);
        matrix.identity();
        matrix = matrix.translate(translation).mul(rightMatrix).mul(leftMatrix).scale(scale);
        return matrix;
    }

    public DisplayTransformation(Transformation transformation) {
        this.translation = new Vector3f(transformation.getTranslation());
        this.leftRotation = new Quaternionf(transformation.getLeftRotation());
        this.scale = new Vector3f(transformation.getScale());
        this.rightRotation = new Quaternionf(transformation.getRightRotation());
        this.matrix = new Matrix4f();
        Matrix4f leftMatrix = new Matrix4f();
        leftRotation.get(leftMatrix);
        Matrix4f rightMatrix = new Matrix4f();
        rightRotation.get(rightMatrix);
        matrix.identity();
        matrix = matrix.mul(rightMatrix).mul(leftMatrix).translate(translation).scale(scale);
    }

    public DisplayTransformation() {
        this.translation = new Vector3f(0);
        this.leftRotation = new Quaternionf().identity();
        this.scale = new Vector3f(1);
        this.rightRotation = new Quaternionf().identity();
        this.matrix = new Matrix4f();
        Matrix4f leftMatrix = new Matrix4f();
        leftRotation.get(leftMatrix);
        Matrix4f rightMatrix = new Matrix4f();
        rightRotation.get(rightMatrix);
        matrix = matrix.mul(rightMatrix).mul(leftMatrix).translate(translation).scale(scale);
    }
}
