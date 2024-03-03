package io.github.nahkd123.inkingcraft.client.bridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;

public interface ClientMouseBridge {
	public static ClientMouseBridge from(Mouse mouse) {
		return (ClientMouseBridge) mouse;
	}

	public static ClientMouseBridge from(MinecraftClient mc) {
		return from(mc.mouse);
	}

	public static ClientMouseBridge getCurrent() { return from(MinecraftClient.getInstance()); }

	public void inkingOnMousePosition(long window, double x, double y);

	public void inkingOnMouseButton(long window, int button, int action, int mods);
}
