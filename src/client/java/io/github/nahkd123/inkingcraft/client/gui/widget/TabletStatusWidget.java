package io.github.nahkd123.inkingcraft.client.gui.widget;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.nahkd123.inkingcraft.InkingCraft;
import io.github.nahkd123.inkingcraft.client.InkingCraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class TabletStatusWidget extends ClickableWidget {
	private static final Identifier TEXTURE_DISCONNECTED = InkingCraft.id("widget/status_disconnected");
	private static final Identifier TEXTURE_CONNECTED = InkingCraft.id("widget/status_connected");
	private Supplier<String> tabletId;
	private Consumer<TabletStatusWidget> callback;

	private String lastId = null;
	private long lastUpdated = 0L;
	private boolean lastConnectedState = false;

	public TabletStatusWidget(int x, int y, int width, int height, Text message, Supplier<String> tabletId, Consumer<TabletStatusWidget> callback) {
		super(x, y, width, height, message);
		this.tabletId = tabletId;
		this.callback = callback;
	}

	public TabletStatusWidget(int x, int y, int width, int height, Text message, Supplier<String> tabletId) {
		this(x, y, width, height, message, tabletId, null);
	}

	public boolean getCachedConnectState() {
		long now = System.currentTimeMillis();

		if (!tabletId.get().equals(lastId) || now - lastUpdated > 500L) {
			String tabletId = this.tabletId.get();
			lastUpdated = now;
			lastConnectedState = tabletId != null && InkingCraftClient.getDrivers().getConnectedTablets()
				.stream()
				.anyMatch(t -> t.getTabletId().equals(tabletId));
		}

		return lastConnectedState;
	}

	@SuppressWarnings("resource")
	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		String tabletId = this.tabletId.get();

		boolean connected = getCachedConnectState();
		boolean hovering = mouseX >= getX()
			&& mouseX <= getX() + width
			&& mouseY >= getY()
			&& mouseY <= getY() + height;
		String top = tabletId != null
			? InkingCraftClient.getSpecificationsStore().getAllSpecifications().get(tabletId).getTabletName()
			: "(no tablets connected)";
		Text bottom = connected
			? Text.literal("Connected").styled(s -> s.withColor(Formatting.GREEN))
			: Text.literal("Disconnected").styled(s -> s.withColor(Formatting.RED));
		Identifier texture = connected ? TEXTURE_CONNECTED : TEXTURE_DISCONNECTED;

		if (callback != null && hovering) context.drawBorder(getX(), getY(), width, height, 0xFFFFFFFF);
		context.drawText(textRenderer, top, getX(), getY() + 1, 0xFFFFFF, true);
		context.drawText(textRenderer, bottom, getX() + 16, getY() + 2 + textRenderer.fontHeight, 0xFFFFFF, true);
		context.drawGuiTexture(texture, getX(), getY() + 2 + textRenderer.fontHeight, 12, 8);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		// TODO
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		boolean hovering = mouseX >= getX()
			&& mouseX <= getX() + width
			&& mouseY >= getY()
			&& mouseY <= getY() + height;
		if (hovering && callback != null) callback.accept(this);
	}
}
