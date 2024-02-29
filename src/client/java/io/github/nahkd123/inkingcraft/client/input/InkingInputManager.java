package io.github.nahkd123.inkingcraft.client.input;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.nahkd123.inking.api.tablet.Tablet;

public class InkingInputManager {
	private Map<String, TabletInput> pointers = new ConcurrentHashMap<>();

	public TabletInput get(Tablet tablet) {
		if (tablet == null) return null;
		TabletInput input = pointers.get(tablet.getTabletId());
		if (input == null) pointers.put(tablet.getTabletId(), input = new TabletInput());
		return input;
	}

	public TabletInput getFromId(String id) {
		return pointers.get(id);
	}

	public Map<String, TabletInput> getAll() { return Collections.unmodifiableMap(pointers); }
}
