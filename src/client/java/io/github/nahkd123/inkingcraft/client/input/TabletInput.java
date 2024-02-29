package io.github.nahkd123.inkingcraft.client.input;

import io.github.nahkd123.inking.api.tablet.ButtonType;
import io.github.nahkd123.inking.api.tablet.Packet;
import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inking.api.util.Vector2;
import io.github.nahkd123.inkingcraft.client.InkingCraftClient;
import io.github.nahkd123.inkingcraft.client.config.InkingConfiguration;
import io.github.nahkd123.inkingcraft.client.config.TabletConfiguration;
import io.github.nahkd123.inkingcraft.client.utils.PenRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class TabletInput {
	private volatile Packet lastRawPacket;
	private volatile FilteredPacketData lastFilteredPacket;

	public Packet getLastRawPacket() { return lastRawPacket; }

	public void setRawPacket(Packet lastRawPacket) { this.lastRawPacket = lastRawPacket; }

	public FilteredPacketData getLastFilteredPacket() { return lastFilteredPacket; }

	public void setFilteredPacket(FilteredPacketData lastFilteredPacket) {
		this.lastFilteredPacket = lastFilteredPacket;
	}

	public long nanoSinceLastPacket() {
		return System.nanoTime() - (lastRawPacket != null ? lastRawPacket.getTimestamp() : 0L);
	}

	@SuppressWarnings("resource")
	public void renderPointer(DrawContext ctx, TabletSpec spec, TabletConfiguration config) {
		if (spec == null || lastRawPacket == null || nanoSinceLastPacket() > 100_000000) return;
		float windowScale = (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
		int pointerColor = config != null ? config.getPointerColor() : 0xFFFFFF;
		InkingConfiguration global = InkingCraftClient.getGlobalConfig();
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		ctx.getMatrices().push();
		ctx.getMatrices().translate(
			lastFilteredPacket.screenX() / windowScale,
			lastFilteredPacket.screenY() / windowScale,
			0);

		if (global.isShowPointers()) ctx.fill(-1, -1, 1, 1, pointerColor | (0xFF << 24));

		if (global.isShowIndicators()) {
			Vector2 tilt = lastRawPacket.getTilt();
			PenRenderer.renderPenAtZero(ctx, lastFilteredPacket.pressure(), tilt.x(), tilt.y(), pointerColor);
			boolean buttonDown = false;
			String buttons = "";

			for (int i = 0; i < spec.getButtonsCount(ButtonType.PEN); i++) {
				if (lastRawPacket.isButtonDown(ButtonType.PEN, i)) {
					buttonDown = true;
					buttons += (buttons.isEmpty() ? "" : ", ") + (i + 1);
				}
			}

			if (buttonDown) {
				ctx.drawBorder(-5, -5, 10, 10, pointerColor | (0xFF << 24));
				ctx.drawText(textRenderer, buttons, 5, 5, pointerColor, false);
			}
		}

		ctx.getMatrices().pop();
	}
}
