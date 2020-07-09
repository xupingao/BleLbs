package com.ustc.location.drawtool;

import java.util.EventListener;

public interface DrawEventListener extends EventListener {

	void handleDrawEvent(DrawEvent event);
}
