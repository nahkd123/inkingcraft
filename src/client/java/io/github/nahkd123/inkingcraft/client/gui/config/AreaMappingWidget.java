package io.github.nahkd123.inkingcraft.client.gui.config;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import io.github.nahkd123.inking.api.tablet.Packet;
import io.github.nahkd123.inking.api.tablet.Tablet;
import io.github.nahkd123.inking.api.util.Vector2;
import io.github.nahkd123.inkingcraft.client.config.AreaMapping;
import io.github.nahkd123.inkingcraft.client.gui.widget.TabletElement;
import io.github.nahkd123.inkingcraft.client.utils.LetterboxingUtils;
import io.github.nahkd123.inkingcraft.client.utils.PenRenderer;
import io.github.nahkd123.inkingcraft.client.utils.Rectangle;
import io.github.nahkd123.inkingcraft.client.utils.XYConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class AreaMappingWidget extends ClickableWidget implements TabletElement {
	private static final DecimalFormat FORMATTER = new DecimalFormat("#,##0.##");
	private Supplier<AreaMapping> mapping;
	private Supplier<Vector2> physicalSize;
	private Supplier<Vector2> inputSize;
	private Supplier<Vector2> screenSize;
	private Runnable onChanges;

	// Clickable corners coords (computed at render time)
	private int[] topLeft = new int[2];
	private int[] topRight = new int[2];
	private int[] bottomLeft = new int[2];
	private int[] bottomRight = new int[2];
	private int[] activeArea = new int[4];
	private XYConsumer widgetDragDelta;
	private Runnable widgetRelease;

	// Tablet preview
	private long lastPointerNano;
	private double penX, penY, tiltX, tiltY, pressure;

	// Avoid funny and weird handles dragging glitch
	private Rectangle currentMappingArea = null;

	private List<Map.Entry<int[], RectangleCornerResizer>> resizers = Arrays.asList(
		Map.entry(topLeft, Rectangle::withTopLeftCornerResize),
		Map.entry(topRight, Rectangle::withTopRightCornerResize),
		Map.entry(bottomLeft, Rectangle::withBottomLeftCornerResize),
		Map.entry(bottomRight, Rectangle::withBottomRightCornerResize));

	@FunctionalInterface
	private static interface RectangleCornerResizer {
		Rectangle resize(Rectangle rect, double dx, double dy);
	}

	public AreaMappingWidget(int x, int y, int width, int height, Supplier<AreaMapping> mapping, Supplier<Vector2> physicalSize, Supplier<Vector2> inputSize, Supplier<Vector2> screenSize, Runnable onChanges) {
		super(x, y, width, height, Text.literal("Area Mapping"));
		this.mapping = mapping;
		this.physicalSize = physicalSize;
		this.inputSize = inputSize;
		this.screenSize = screenSize;
		this.onChanges = onChanges;
	}

	@SuppressWarnings("resource")
	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		AreaMapping mapping = this.mapping.get();
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		context.fill(getX(), getY(), getX() + width, getY() + height, 0xFF000000);
		context.drawBorder(getX(), getY(), width, height, 0xFF2F2F2F);

		if (mapping == null) {
			context.drawCenteredTextWithShadow(
				textRenderer,
				Text.literal("Please select a tablet").styled(s -> s.withItalic(true)),
				getX() + width / 2, getY() + (height - textRenderer.fontHeight) / 2, 0x7F7F7F);
			return;
		}

		context.enableScissor(getX(), getY(), getX() + width, getY() + height);
		Vector2 physical = physicalSize.get();
		Vector2 fullInput = inputSize.get();

		double x = getX() + 5;
		double y = getY() + 5;
		double width = this.width - 10;
		double height = this.height - 10;

		// Full area
		double[] tabletDrawSize = LetterboxingUtils.apply(physical.x() / physical.y(), width, height);
		double tabletDrawX = x + (width - tabletDrawSize[0]) / 2d;
		double tabletDrawY = y + (height - tabletDrawSize[1]) / 2d;
		context.getMatrices().push();
		context.getMatrices().translate(tabletDrawX, tabletDrawY, 0);
		context.fill(0, 0, (int) tabletDrawSize[0], (int) tabletDrawSize[1],
			0xFF2F2F2F);
		context.drawBorder(0, 0, (int) tabletDrawSize[0], (int) tabletDrawSize[1], 0xFF7F7F7F);
		context.drawText(
			textRenderer,
			"Physical: " + FORMATTER.format(physical.x()) + "mm * " + FORMATTER.format(physical.y()) + "mm",
			3, 3,
			0xAFAFAF, false);
		context.drawText(
			textRenderer,
			"Input: " + FORMATTER.format(fullInput.x()) + " * " + FORMATTER.format(fullInput.y()),
			3, 3 + textRenderer.fontHeight,
			0xAFAFAF, false);

		// Draw current pointer
		if (System.nanoTime() - lastPointerNano < 100_000_000) {
			double penPixelX = penX * tabletDrawSize[0] / fullInput.x();
			double penPixelY = penY * tabletDrawSize[1] / fullInput.y();
			context.getMatrices().push();
			context.getMatrices().translate(penPixelX, penPixelY, 0);
			context.fill(-1, -1, 1, 1, 0xFFFFFFFF); // TODO pointer color
			PenRenderer.renderPenAtZero(context, pressure, tiltX, tiltY, 0xFFFFFF);
			context.getMatrices().pop();
		}

		// Mapped active area
		Rectangle tabletArea = currentMappingArea != null ? currentMappingArea : mapping.getTabletArea();
		double mappedX = tabletArea.x() / fullInput.x() * tabletDrawSize[0];
		double mappedY = tabletArea.y() / fullInput.y() * tabletDrawSize[1];
		double mappedW = tabletArea.width() / fullInput.x() * tabletDrawSize[0];
		double mappedH = tabletArea.height() / fullInput.y() * tabletDrawSize[1];

		topLeft[0] = bottomLeft[0] = (int) (tabletDrawX + mappedX);
		topLeft[1] = topRight[1] = (int) (tabletDrawY + mappedY);
		topRight[0] = bottomRight[0] = (int) (tabletDrawX + mappedX + mappedW);
		bottomLeft[1] = bottomRight[1] = (int) (tabletDrawY + mappedY + mappedH);
		activeArea[0] = topLeft[0];
		activeArea[1] = topLeft[1];
		activeArea[2] = bottomRight[0];
		activeArea[3] = bottomRight[1];

		context.getMatrices().translate(mappedX, mappedY, 0);
		context.fill(0, 0, (int) mappedW, (int) mappedH, hoveringArea(mouseX, mouseY) ? 0x5CFFFFFF : 0x1FFFFFFF);
		context.drawBorder(0, 0, (int) mappedW, (int) mappedH, hoveringArea(mouseX, mouseY) ? 0xFFFFFFFF : 0x7FFFFFFF);
		context.drawText(
			textRenderer,
			"Active Area: " +
				FORMATTER.format(tabletArea.width() / fullInput.x() * physical.x()) + "mm * " +
				FORMATTER.format(tabletArea.height() / fullInput.y() * physical.y()) + "mm",
			3, (int) mappedH - 1 - textRenderer.fontHeight,
			0xFFFFFF, false);

		context.fill(-5, -5, 5, 5,
			hoveringCorner(topLeft, mouseX, mouseY) ? 0xFFFFFFFF : 0x7FFFFFFF);
		context.fill((int) mappedW - 5, -5, (int) mappedW + 5, 5,
			hoveringCorner(topRight, mouseX, mouseY) ? 0xFFFFFFFF : 0x7FFFFFFF);
		context.fill(-5, (int) mappedH - 5, 5, (int) mappedH + 5,
			hoveringCorner(bottomLeft, mouseX, mouseY) ? 0xFFFFFFFF : 0x7FFFFFFF);
		context.fill((int) mappedW - 5, (int) mappedH - 5, (int) mappedW + 5, (int) mappedH + 5,
			hoveringCorner(bottomRight, mouseX, mouseY) ? 0xFFFFFFFF : 0x7FFFFFFF);

		// Screen
		Vector2 screenSize = this.screenSize.get();
		double[] screenDrawSize = LetterboxingUtils.apply(screenSize.x() / screenSize.y(), mappedW, mappedH);

		context.getMatrices().translate(
			(mappedW - screenDrawSize[0]) / 2,
			(mappedH - screenDrawSize[1]) / 2,
			0);
		context.fill(0, 0, (int) screenDrawSize[0], (int) screenDrawSize[1], 0x2F7FFFFF);
		context.drawBorder(0, 0, (int) screenDrawSize[0], (int) screenDrawSize[1], 0x5C7FFFFF);
		context.drawText(
			textRenderer,
			"Screen Area",
			(int) ((screenDrawSize[0] - textRenderer.getWidth("Screen Area")) / 2d),
			(int) ((screenDrawSize[1] - textRenderer.fontHeight) / 2d),
			0x7FFFFF, false);

		context.getMatrices().pop();
		context.disableScissor();
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		if (mapping.get() == null) return;
		Vector2 fullInput = inputSize.get();
		double widgetRatio = width / (double) height;
		double tabletRatio = fullInput.x() / fullInput.y();
		double unitScale;

		if (widgetRatio > tabletRatio) {
			unitScale = fullInput.x() / (height * tabletRatio);
		} else {
			unitScale = fullInput.x() / width;
		}

		for (Entry<int[], RectangleCornerResizer> e : resizers) {
			if (hoveringCorner(e.getKey(), mouseX, mouseY)) {
				currentMappingArea = mapping.get().getTabletArea();
				widgetDragDelta = (dx, dy) -> currentMappingArea = e.getValue().resize(
					currentMappingArea,
					dx * unitScale, dy * unitScale);

				// TODO auto-snap while holding shift
				return;
			}
		}

		if (hoveringArea(mouseX, mouseY)) {
			currentMappingArea = mapping.get().getTabletArea();
			widgetDragDelta = (dx, dy) -> currentMappingArea = currentMappingArea.withTranslation(
				dx * unitScale,
				dy * unitScale);

			// TODO auto-snap while holding shift
			return;
		}
	}

	@Override
	public void tabletInputted(Tablet tablet, Packet raw, double penX, double penY, double pressure) {
		this.penX = raw.getPenPosition().x();
		this.penY = raw.getPenPosition().y();
		this.tiltX = raw.getTilt().x();
		this.tiltY = raw.getTilt().y();
		this.pressure = pressure;
		this.lastPointerNano = System.nanoTime();
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
		if (widgetDragDelta != null) widgetDragDelta.accept(deltaX, deltaY);
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		if (mapping.get() == null) return;
		if (widgetRelease != null) widgetRelease.run();

		if (currentMappingArea != null) {
			mapping.get().setTabletArea(currentMappingArea);
			onChanges.run();
			currentMappingArea = null;
		}

		widgetDragDelta = null;
		widgetRelease = null;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		// TODO
	}

	private boolean hoveringCorner(int[] coords, double mouseX, double mouseY) {
		return mouseX >= coords[0] - 5 && mouseX <= coords[0] + 5
			&& mouseY >= coords[1] - 5 && mouseY <= coords[1] + 5;
	}

	private boolean hoveringArea(double mouseX, double mouseY) {
		return mouseX >= activeArea[0] && mouseX <= activeArea[2]
			&& mouseY >= activeArea[1] && mouseY <= activeArea[3];
	}
}
