package com.ixume.alchemy;

public class EntityIDManager {
    private static EntityIDManager INSTANCE;
    private EntityIDManager() {
        id = -1;
    }
    public static EntityIDManager getInstance() {
        if (INSTANCE == null) INSTANCE = new EntityIDManager();
        return INSTANCE;
    }

    private int id;

    public int getID() { return --id;}
}
