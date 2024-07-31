package com.ixume.alchemy.gameobject;

import java.util.HashMap;

public class TickersManager {
    private static TickersManager INSTANCE;
    private TickersManager() {
        tickers = new HashMap<>();
    }
    public static TickersManager getInstance() {
        if (INSTANCE == null) INSTANCE = new TickersManager();
        return INSTANCE;
    }

    public final HashMap<String, GameObjectTicker> tickers;
}
