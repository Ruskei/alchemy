package com.ixume.alchemy.gameobject.bending;

import org.bukkit.block.data.BlockData;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public record VisualBlockDisplay(Vector3f origin, Matrix4f transformationMatrix, BlockData displayData) {
}
