package io.github.nahkd123.inkingcraft.client.config.binding.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.inking.api.tablet.ButtonType;
import io.github.nahkd123.inkingcraft.client.utils.InkingCodecs;
import net.minecraft.text.Text;

public record BindingButtonTarget(ButtonType buttonType, int buttonIndex) implements BindingTarget {
	public static final Codec<BindingButtonTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		InkingCodecs.enumOf(ButtonType.class).fieldOf("buttonType").forGetter(BindingButtonTarget::buttonType),
		Codec.INT.fieldOf("buttonIndex").forGetter(BindingButtonTarget::buttonIndex))
		.apply(instance, BindingButtonTarget::new));

	@Override
	public Text getName() {
		return Text.literal((buttonType == ButtonType.PEN ? "Pen" : "Tablet")
			+ " button #" + (buttonIndex + 1));
	}

	@Override
	public Text getDescription() {
		return Text.literal("Triggers when " + (buttonType == ButtonType.PEN ? "pen" : "tablet")
			+ " button #" + (buttonIndex + 1) + " is pressed.");
	}
}
