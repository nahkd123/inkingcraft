package io.github.nahkd123.inkingcraft.client.input;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.lwjgl.glfw.GLFW;

import io.github.nahkd123.inking.api.tablet.ButtonType;
import io.github.nahkd123.inking.api.tablet.Tablet;
import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inking.api.util.Vector2;
import io.github.nahkd123.inkingcraft.client.bridge.ClientMouseBridge;
import io.github.nahkd123.inkingcraft.client.config.TabletConfiguration;
import io.github.nahkd123.inkingcraft.client.config.binding.MouseEmulation;
import io.github.nahkd123.inkingcraft.client.config.binding.trigger.BindingTrigger;
import io.github.nahkd123.inkingcraft.client.config.binding.trigger.Trigger;
import net.minecraft.client.MinecraftClient;

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

	public void handleFilteredPacket(Tablet tablet, TabletConfiguration config, TabletSpec spec, FilteredPacketData filtered) {
		MinecraftClient client = MinecraftClient.getInstance();
		TabletInput input = get(tablet);

		boolean penPrevPressed = input.getLastFilteredPacket() != null && input.getLastFilteredPacket().pressure() > 0;
		FilteredPacketData lastPacket = input.getLastFilteredPacket();

		Vector2 deltaXY = input.updateFilteredPacket(filtered);
		boolean penPressed = filtered.pressure() > 0;
		int hoveringDist = filtered.raw().getRawHoverDistance();
		int maxHoveringDist = config.getBinding().getMouse().getMaximumHoverDistance();

		if (config.getBinding().getMouse().isEnabled() && hoveringDist <= maxHoveringDist) {
			MouseEmulation mouseEmu = config.getBinding().getMouse();
			ClientMouseBridge mouseBridge = ClientMouseBridge.from(client);
			long handle = client.getWindow().getHandle();

			if (mouseEmu.isAlwaysRelative() || client.mouse.isCursorLocked())
				mouseBridge.inkingOnMousePosition(
					handle,
					client.mouse.getX() + deltaXY.x(),
					client.mouse.getY() + deltaXY.y());
			else
				mouseBridge.inkingOnMousePosition(handle, filtered.screenX(), filtered.screenY());

			if (lastPacket != null
				&& client.currentScreen != null
				&& config.getBinding().getMouse().isEmulateClicks()) {
				if (penPrevPressed ^ penPressed)
					mouseBridge.inkingOnMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT, penPressed
						? GLFW.GLFW_PRESS
						: GLFW.GLFW_RELEASE, 0);

				boolean prevRight = lastPacket.raw().isButtonDown(ButtonType.PEN, 0)
					|| lastPacket.raw().isButtonDown(ButtonType.AUXILIARY, 0);
				boolean currRight = filtered.raw().isButtonDown(ButtonType.PEN, 0)
					|| filtered.raw().isButtonDown(ButtonType.AUXILIARY, 0);
				if (prevRight ^ currRight)
					mouseBridge.inkingOnMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT, currRight
						? GLFW.GLFW_PRESS
						: GLFW.GLFW_RELEASE, 0);

				boolean prevMid = lastPacket.raw().isButtonDown(ButtonType.PEN, 1)
					|| lastPacket.raw().isButtonDown(ButtonType.AUXILIARY, 1);
				boolean currMid = filtered.raw().isButtonDown(ButtonType.PEN, 1)
					|| filtered.raw().isButtonDown(ButtonType.AUXILIARY, 1);
				if (prevMid ^ currMid)
					mouseBridge.inkingOnMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, currRight
						? GLFW.GLFW_PRESS
						: GLFW.GLFW_RELEASE, 0);
			}
		}

		if (client.currentScreen == null && penPrevPressed ^ penPressed) {
			Trigger trigger = input.getLastRawPacket().isEraser()
				? config.getBinding().getPenEraserTrigger()
				: config.getBinding().getPenTipTrigger();
			if (trigger != null) trigger.onTrigger(penPressed);
		}

		if (lastPacket != null) for (ButtonType type : ButtonType.values()) {
			ButtonsComparator.compareDifferences(
				spec.getButtonsCount(type),
				i -> lastPacket.raw().isButtonDown(type, i),
				i -> filtered.raw().isButtonDown(type, i),
				i -> {
					BindingTrigger trigger = config.getBinding().getButtonTrigger(type, i);
					if (trigger != null) trigger.onTrigger(filtered.raw().isButtonDown(type, i));
				});
		}
	}
}
