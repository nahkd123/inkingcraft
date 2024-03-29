package io.github.nahkd123.inkingcraft.client.input;

import io.github.nahkd123.inking.api.tablet.ButtonType;
import io.github.nahkd123.inking.api.tablet.Packet;
import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inking.api.util.ConstantVector2;
import io.github.nahkd123.inking.api.util.Vector2;
import io.github.nahkd123.inkingcraft.client.InkingCraftClient;
import io.github.nahkd123.inkingcraft.client.config.InkingConfiguration;
import io.github.nahkd123.inkingcraft.client.config.TabletConfiguration;
import io.github.nahkd123.inkingcraft.client.utils.PenRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

// TODO Make packets timeout configurable
public class TabletInput {
	private volatile Packet lastRawPacket;
	private volatile FilteredPacketData lastFilteredPacket;

	public Packet getLastRawPacket() { return lastRawPacket; }

	public void setRawPacket(Packet lastRawPacket) { this.lastRawPacket = lastRawPacket; }

	public FilteredPacketData getLastFilteredPacket() { return lastFilteredPacket; }

	public Vector2 updateFilteredPacket(FilteredPacketData data) {
		// @formatter:off
		Vector2 deltaXY = lastFilteredPacket != null
			&& (System.nanoTime() - lastFilteredPacket.raw().getTimestamp()) < 100_000000L
			? new ConstantVector2(
				data.screenX() - lastFilteredPacket.screenX(),
				data.screenY() - lastFilteredPacket.screenY())
			: ConstantVector2.ZERO;
		// @formatter:on

		lastFilteredPacket = data;
		return deltaXY;
	}

	public long nanoSinceLastPacket() {
		return System.nanoTime() - (lastRawPacket != null ? lastRawPacket.getTimestamp() : 0L);
	}

	@SuppressWarnings("resource")
	public void renderPointer(DrawContext ctx, TabletSpec spec, TabletConfiguration config) {
		if (spec == null
			|| lastRawPacket == null
			|| lastFilteredPacket == null
			|| nanoSinceLastPacket() > 100_000000L) return;

		float windowScale = (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
		int pointerColor = config != null ? config.getPointerColor() : 0xFFFFFF;
		int maxHoverDist = config.getBinding().getMouse().getMaximumHoverDistance();
		int pointerAlpha = lastRawPacket.getRawHoverDistance() <= maxHoverDist ? 0xFF : 0x5F;
		InkingConfiguration global = InkingCraftClient.getGlobalConfig();
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		ctx.getMatrices().push();
		ctx.getMatrices().translate(
			lastFilteredPacket.screenX() / windowScale,
			lastFilteredPacket.screenY() / windowScale,
			0);

		if (global.isShowPointers()) ctx.fill(-1, -1, 1, 1, pointerColor | (pointerAlpha << 24));

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
