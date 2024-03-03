package io.github.nahkd123.inkingcraft.client.config.binding.target;

import com.mojang.serialization.Codec;

import io.github.nahkd123.inkingcraft.client.utils.InkingCodecs;
import net.minecraft.text.Text;

public enum BindingPenTarget implements BindingTarget {
	TIP(Text.literal("Pen tip"), Text.literal("Triggers when the the pen is being held down.")),
	ERASER(Text.literal("Eraser"), Text.literal("Triggers when the eraser part of the pen is being held down."));

	private Text name;
	private Text description;

	private BindingPenTarget(Text name, Text description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public Text getName() { return name; }

	@Override
	public Text getDescription() { return description; }

	public static final Codec<BindingPenTarget> CODEC = InkingCodecs.enumOf(BindingPenTarget.class);
}
