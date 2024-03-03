package io.github.nahkd123.inkingcraft.client.config.binding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Mouse;

/**
 * <p>
 * Hold configuration for emulating in-game mouse.
 * </p>
 */
public class MouseEmulation {
	private boolean enabled;
	private boolean ignoringSystemMouse;
	private boolean alwaysRelative;
	private boolean emulateClicks;
	private int maximumHoverDistance;

	public MouseEmulation(boolean enabled, boolean ignoringSystemMouse, boolean alwaysRelative, boolean emulateClicks, int maximumHoverDistance) {
		this.enabled = enabled;
		this.ignoringSystemMouse = ignoringSystemMouse;
		this.alwaysRelative = alwaysRelative;
		this.emulateClicks = emulateClicks;
		this.maximumHoverDistance = maximumHoverDistance;
	}

	public MouseEmulation() {
		this(false, false, false, true, 1024);
	}

	public boolean isEnabled() { return enabled; }

	public void setEnabled(boolean enabled) { this.enabled = enabled; }

	/**
	 * <p>
	 * Check whether the game should ignore mouse input from GLFW when the pen is
	 * hovering above the tablet. If you have standalone OpenTabletDriver running in
	 * background, you might want to enable this.
	 * </p>
	 * 
	 * @return Should the game ignore system mouse input?
	 */
	public boolean isIgnoringSystemMouse() { return ignoringSystemMouse; }

	public void setIgnoringSystemMouse(boolean ignoringSystemMouse) { this.ignoringSystemMouse = ignoringSystemMouse; }

	public int getMaximumHoverDistance() { return maximumHoverDistance; }

	public void setMaximumHoverDistance(int minimumHoverDistance) { this.maximumHoverDistance = minimumHoverDistance; }

	/**
	 * <p>
	 * Check if the mouse emulation mode should always enter relative mode. The
	 * default value is {@code false}. If the value is {@code true}, user can use
	 * the pen like how they can use laptop touchpad or regular mouse. If the value
	 * is {@code false}, the mouse emulator only enters relative mode when
	 * {@link Mouse#isCursorLocked()} is {@code true}, and it will be in absolute
	 * mode when {@link Mouse#isCursorLocked()} is {@code false}, like opening GUI
	 * for example.
	 * 
	 * @return Should the mouse emulation layer always put this tablet into relative
	 *         mode all the time?
	 */
	public boolean isAlwaysRelative() { return alwaysRelative; }

	public void setAlwaysRelative(boolean alwaysRelative) { this.alwaysRelative = alwaysRelative; }

	public boolean isEmulateClicks() { return emulateClicks; }

	public void setEmulateClicks(boolean emulateClicks) { this.emulateClicks = emulateClicks; }

	public static final Codec<MouseEmulation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("enabled", false).forGetter(MouseEmulation::isEnabled),
		Codec.BOOL.optionalFieldOf("ignoringSystemMouse", false).forGetter(MouseEmulation::isIgnoringSystemMouse),
		Codec.BOOL.optionalFieldOf("alwaysRelative", false).forGetter(MouseEmulation::isAlwaysRelative),
		Codec.BOOL.optionalFieldOf("emulateClicks", true).forGetter(MouseEmulation::isEmulateClicks),
		Codec.INT.optionalFieldOf("maxHoverDistance", 1024).forGetter(MouseEmulation::getMaximumHoverDistance))
		.apply(instance, MouseEmulation::new));
}
