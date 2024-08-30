package com.ixume.alchemy.gameobject.bending.data;

import com.ixume.alchemy.gameobject.bending.data.objects.SpikeDataRecord;

public class SpikeObjectsFactory implements BendingObjectObjectsFactory {
    @Override
    public BendingObjectData getDamageHitbox(Record dataObject) {
        if (!(dataObject instanceof SpikeDataRecord spikeDataObject)) throw new RuntimeException("wrong data type for bending display");

        return null;
    }
}
