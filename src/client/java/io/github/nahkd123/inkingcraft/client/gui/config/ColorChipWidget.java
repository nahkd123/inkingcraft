package io.github.nahkd123.inkingcraft.client.gui.config;

import java.util.function.Consumer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class ColorChipWidget extends ClickableWidget {
	private int color;
	private Consumer<ColorChipWidget> callback;

	public ColorChipWidget(int x, int y, int width, int height, Text message, int color, Consumer<ColorChipWidget> callback) {
		super(x, y, width, height, message);
		this.color = color;
		this.callback = callback;
	}

	public int getColor() { return color; }

	public void setColor(int color) { this.color = color; }

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		boolean hovering = mouseX >= getX()
			&& mouseX <= getX() + width
			&& mouseY >= getY()
			&& mouseY <= getY() + height;

		context.drawBorder(getX(), getY(), width, height, hovering ? 0xFFFFFFFF : 0xFF5C5C5C);
		context.fill(getX() + 2, getY() + 2, getX() + width - 2, getY() + height - 2, color | (0xFF << 24));
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		if (callback != null) callback.accept(this);
	}
}
