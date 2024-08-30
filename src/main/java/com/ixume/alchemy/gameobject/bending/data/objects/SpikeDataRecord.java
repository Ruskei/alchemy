package com.ixume.alchemy.gameobject.bending.data.objects;

import org.joml.Vector3f;

public record SpikeDataRecord(Vector3f origin, Vector3f target, int life, int speed, int initWidth, int endWidth) {
}
