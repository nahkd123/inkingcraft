package io.github.nahkd123.inkingcraft.client.mixin;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.nahkd123.inking.api.tablet.Packet;
import io.github.nahkd123.inking.api.tablet.Tablet;
import io.github.nahkd123.inkingcraft.client.InkingCraftClient;
import io.github.nahkd123.inkingcraft.client.config.TabletConfiguration;
import io.github.nahkd123.inkingcraft.client.gui.widget.TabletElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;

@Mixin(Screen.class)
public abstract class ScreenMixin implements TabletElement {
	@Shadow
	protected TextRenderer textRenderer;
	@Shadow
	@Final
	private List<Drawable> drawables;

	@Override
	public void tabletInputtedAsync(Tablet tablet, Packet raw, double penX, double penY, double pressure) {
		// We don't plan on writing anything here, so CopyOnWriteArrayList might be okay
		Iterator<Drawable> iter = new CopyOnWriteArrayList<>(drawables).iterator();
		while (iter.hasNext()) {
			Drawable next = iter.next();
			if (next instanceof TabletElement elem) elem.tabletInputtedAsync(tablet, raw, penX, penY, pressure);
		}

		// From superinterface
		// Can't use TabletElement.super.tabletInputtedAsync() because of weird Mixin
		// quirk.
		MinecraftClient.getInstance().execute(() -> tabletInputted(tablet, raw, penX, penY, pressure));
	}

	@Override
	public void tabletInputted(Tablet tablet, Packet raw, double penX, double penY, double pressure) {
		AbstractParentElement self = (AbstractParentElement) (Object) this;
		TabletConfiguration config = InkingCraftClient.getConfigStore().get(tablet);

		if (config.isInteractUI()) {
			// TODO interact the screen
		}
	}
}
