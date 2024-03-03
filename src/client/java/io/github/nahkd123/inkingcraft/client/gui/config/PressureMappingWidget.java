package io.github.nahkd123.inkingcraft.client.gui.config;

import java.util.function.Supplier;

import org.joml.Matrix4f;

import io.github.nahkd123.inking.api.tablet.Packet;
import io.github.nahkd123.inking.api.tablet.Tablet;
import io.github.nahkd123.inkingcraft.client.config.PressureMapping;
import io.github.nahkd123.inkingcraft.client.config.PressureMappingPoint;
import io.github.nahkd123.inkingcraft.client.gui.widget.TabletElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Text;

public class PressureMappingWidget extends ClickableWidget implements TabletElement {
	private Supplier<PressureMapping> mapping;
	private Supplier<Integer> maxPressure;
	private Runnable onChanges;
	private PressureMappingPoint dragging = null;
	private PressureMappingPoint selected = null;

	// Preview
	private int sourcePressure;
	private double mappedPressure;

	public PressureMappingWidget(int x, int y, int width, int height, Supplier<PressureMapping> mapping, Supplier<Integer> maxPressure, Runnable onChanges) {
		super(x, y, width, height, Text.literal("Pressure Mapping"));
		this.mapping = mapping;
		this.maxPressure = maxPressure;
		this.onChanges = onChanges;
	}

	@SuppressWarnings("resource")
	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		PressureMapping mapping = this.mapping.get();
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		context.fill(getX(), getY(), getX() + width, getY() + height, 0x7F000000);
		context.drawBorder(getX(), getY(), width, height, 0xFF2F2F2F);

		if (mapping == null) {
			context.drawCenteredTextWithShadow(
				textRenderer,
				Text.literal("Please select a tablet").styled(s -> s.withItalic(true)),
				getX() + width / 2, getY() + (height - textRenderer.fontHeight) / 2, 0x7F7F7F);
			return;
		}

		context.enableScissor(getX(), getY(), getX() + width, getY() + height);
		context.getMatrices().push();
		context.getMatrices().translate(getX() + 5, getY() + 5, 0);

		double width = this.width - 10;
		double height = this.height - 10;
		int maxPressure = this.maxPressure.get();
		context.drawText(textRenderer, "Max Pressure: " + maxPressure + " units", 0, 0, 0xAFAFAF, false);

		// Draw axes
		int fHeight = textRenderer.fontHeight;
		context.drawText(textRenderer, "Input", 8, (int) height - fHeight - 8, 0xAFAFAF, false);
		context.drawText(textRenderer, "Output", 8, (int) height - fHeight * 3 - 8, 0xAFAFAF, false);
		context.fill(36, (int) height - 11, (int) width / 4, (int) height - 10, 0xFFAFAFAF);
		context.fill(8, (int) height - fHeight * 3 - 12, 9, fHeight * 2, 0xFFAFAFAF);

		PressureMappingPoint lastPoint = new PressureMappingPoint(0, 0);
		for (PressureMappingPoint point : mapping.getPoints()) {
			float prevPointX = (float) (lastPoint.sourcePressure() * width / maxPressure);
			float prevPointY = (float) (height - lastPoint.targetPressure() * height / maxPressure);
			float thisPointX = (float) (point.sourcePressure() * width / maxPressure);
			float thisPointY = (float) (height - point.targetPressure() * height / maxPressure);

			VertexConsumer vc = context.getVertexConsumers().getBuffer(RenderLayer.getGui());
			Matrix4f mat = context.getMatrices().peek().getPositionMatrix();

			vc.vertex(mat, prevPointX, prevPointY, 0).color(0x7FFFFFFF).next();
			vc.vertex(mat, prevPointX, (float) height, 0).color(0x7FFFFFFF).next();
			vc.vertex(mat, thisPointX, (float) height, 0).color(0x7FFFFFFF).next();
			vc.vertex(mat, thisPointX, thisPointY, 0).color(0x7FFFFFFF).next();

			if (sourcePressure >= lastPoint.sourcePressure()) {
				float prog = (sourcePressure - lastPoint.sourcePressure())
					/ (float) (point.sourcePressure() - lastPoint.sourcePressure());
				if (prog > 1) prog = 1;

				// @formatter:off
				vc.vertex(mat, prevPointX, prevPointY, 0).color(0xFFFFFFFF).next();
				vc.vertex(mat, prevPointX, (float) height, 0).color(0xFFFFFFFF).next();
				vc.vertex(mat, prevPointX + (thisPointX - prevPointX) * prog, (float) height, 0).color(0xFFFFFFFF).next();
				vc.vertex(mat, prevPointX + (thisPointX - prevPointX) * prog, prevPointY + (thisPointY - prevPointY) * prog, 0).color(0xFFFFFFFF).next();
				// @formatter:on
			}

			boolean hovering = mouseX >= getX() + thisPointX - 3
				&& mouseX <= getX() + 5 + thisPointX + 3
				&& mouseY >= getY() + 5 + thisPointY - 3
				&& mouseY <= getY() + 5 + thisPointY + 3;
			boolean selected = this.selected == point;

			context.getMatrices().push();
			context.getMatrices().translate(thisPointX, thisPointY, 0);
			context.fill(-3, -3, 3, 3, hovering || selected ? 0xFFFFFFFF : 0x7FFFFFFF);
			context.getMatrices().pop();

			lastPoint = point;
		}

		// Mapped pressure line
		double mappedPressureLineY = height - height * mappedPressure;
		context.fill(0, (int) mappedPressureLineY, (int) width, (int) (mappedPressureLineY + 1), 0xFFFFFFFF);

		context.getMatrices().pop();
		context.disableScissor();
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		// TODO Auto-generated method stub
	}

	@Override
	public void tabletInputted(Tablet tablet, Packet raw, double penX, double penY, double pressure) {
		sourcePressure = raw.getRawPressure();
		mappedPressure = pressure;
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		PressureMapping mapping = this.mapping.get();
		if (mapping == null) return;
		double width = this.width - 10;
		double height = this.height - 10;
		int maxPressure = this.maxPressure.get();

		for (PressureMappingPoint point : mapping.getPoints()) {
			float pointX = (float) (point.sourcePressure() * width / maxPressure);
			float pointY = (float) (height - point.targetPressure() * height / maxPressure);
			boolean hovering = mouseX >= getX() + pointX - 3
				&& mouseX <= getX() + 5 + pointX + 3
				&& mouseY >= getY() + 5 + pointY - 3
				&& mouseY <= getY() + 5 + pointY + 3;

			if (hovering) {
				dragging = selected = point;
				onChanges.run();
				return;
			}
		}

		int source = (int) ((mouseX - getX() - 5) * maxPressure / width);
		int target = maxPressure - (int) ((mouseY - getY() - 5) * maxPressure / height);
		source = clamp(source, 0, maxPressure);
		target = clamp(target, 0, maxPressure);
		mapping.add(dragging = selected = new PressureMappingPoint(source, target));
		onChanges.run();
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
		if (dragging == null) return;
		PressureMapping mapping = this.mapping.get();
		double width = this.width - 10;
		double height = this.height - 10;
		int sourceDelta = (int) (deltaX * maxPressure.get() / width);
		int targetDelta = (int) -(deltaY * maxPressure.get() / height);

		// @formatter:off
		PressureMappingPoint newPoint = new PressureMappingPoint(
			clamp(dragging.sourcePressure() + sourceDelta, 0, maxPressure.get()),
			clamp(dragging.targetPressure() + targetDelta, 0, maxPressure.get()));
		// @formatter:on

		mapping.replace(dragging, newPoint);
		dragging = selected = newPoint;
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		if (dragging != null) {
			dragging = null;
			onChanges.run();
		}
	}

	private static int clamp(int v, int min, int max) {
		return Math.max(Math.min(v, max), min);
	}

	public PressureMappingPoint getSelectedPoint() { return selected; }

	public void setSelectedPoint(PressureMappingPoint selected) { this.selected = selected; }
}
