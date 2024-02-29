package io.github.nahkd123.inkingcraft.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inking.api.util.ConstantVector2;
import io.github.nahkd123.inking.api.util.Vector2;
import io.github.nahkd123.inkingcraft.client.utils.Rectangle;
import io.github.nahkd123.inkingcraft.client.utils.XYConsumer;

public class AreaMapping {
	private Rectangle tabletArea;
	private boolean letterboxing;

	public AreaMapping(Rectangle tabletArea, boolean letterboxing) {
		this.tabletArea = tabletArea;
		this.letterboxing = letterboxing;
	}

	/**
	 * <p>
	 * Get a slice of area on the tablet input surface to use. The maximum area is
	 * the tablet's input size, which can be obtained from
	 * {@link TabletSpec#getInputSize()}. The configuration screen should display
	 * physical size instead.
	 * </p>
	 * 
	 * @return Tablet area.
	 */
	public Rectangle getTabletArea() { return tabletArea; }

	public void setTabletArea(Rectangle tabletArea) { this.tabletArea = tabletArea; }

	/**
	 * <p>
	 * Check if the mapping is in letterboxing mode. In normal mode (stretching
	 * mode), an entire screen is mapped to {@link #getTabletArea()}. In
	 * letterboxing mode, the screen is resized to fit inside tablet area.
	 * </p>
	 * <p>
	 * Letterboxing mode is similar to Wacom's "Force proportion" mode, except it
	 * will be mapped to the center of tablet input area.
	 * </p>
	 * 
	 * @return Letterboxing state.
	 */
	public boolean isLetterboxing() { return letterboxing; }

	public void setLetterboxing(boolean letterboxing) { this.letterboxing = letterboxing; }

	/**
	 * <p>
	 * Map tablet input coordinates to screen coordinates from this mapping.
	 * </p>
	 * 
	 * @param inputX       The X coordinate on tablet.
	 * @param inputY       The Y coordinate on tablet.
	 * @param screenWidth  The width of the screen.
	 * @param screenHeight The height of the screen.
	 * @param setter       The screen coordinates consumer.
	 */
	public void map(double inputX, double inputY, double screenWidth, double screenHeight, XYConsumer setter) {
		double tabletX = tabletArea.x();
		double tabletY = tabletArea.y();
		double tabletWidth = tabletArea.width();
		double tabletHeight = tabletArea.height();

		if (letterboxing) {
			double areaRatio = tabletArea.width() / tabletArea.height();
			double screenRatio = screenWidth / screenHeight;

			if (areaRatio > screenRatio) {
				tabletWidth = tabletHeight / screenRatio;
				tabletX += (tabletArea.width() - tabletWidth) / 2;
			} else {
				tabletHeight = tabletWidth / screenRatio;
				tabletY += (tabletArea.height() - tabletHeight) / 2;
			}
		}

		double screenX = (inputX - tabletX) * screenWidth / tabletWidth;
		double screenY = (inputY - tabletY) * screenHeight / tabletHeight;
		setter.accept(screenX, screenY);
	}

	public void map(Vector2 input, Vector2 screenSize, XYConsumer setter) {
		map(input.x(), input.y(), screenSize.x(), screenSize.y(), setter);
	}

	public static final Codec<AreaMapping> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Rectangle.CODEC.fieldOf("tabletArea").forGetter(AreaMapping::getTabletArea),
		Codec.BOOL.fieldOf("letterboxing").forGetter(AreaMapping::isLetterboxing))
		.apply(instance, AreaMapping::new));

	public static AreaMapping createDefault(TabletSpec spec) {
		return new AreaMapping(new Rectangle(ConstantVector2.ZERO, spec.getInputSize()), true);
	}
}
