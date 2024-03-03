package io.github.nahkd123.inkingcraft.client.config.binding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.inkingcraft.client.config.binding.target.BindingTarget;
import io.github.nahkd123.inkingcraft.client.config.binding.trigger.BindingTrigger;

public record Binding(BindingTarget target, BindingTrigger trigger) {
	public static final Codec<Binding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BindingTarget.CODEC.fieldOf("target").forGetter(Binding::target),
		BindingTrigger.CODEC.fieldOf("trigger").forGetter(Binding::trigger))
		.apply(instance, Binding::new));
}
