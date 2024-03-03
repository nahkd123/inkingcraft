package io.github.nahkd123.inkingcraft.client.config.binding.target;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import net.minecraft.text.Text;

public interface BindingTarget {
	public Text getName();

	public Text getDescription();

	public static final Codec<BindingTarget> CODEC = Codec
		.either(BindingButtonTarget.CODEC, BindingPenTarget.CODEC)
		.xmap(
			either -> either.left().map(v -> (BindingTarget) v).or(() -> either.right()).get(),
			binding -> binding instanceof BindingButtonTarget btn
				? Either.left(btn)
				: binding instanceof BindingPenTarget pen ? Either.right(pen)
				: null);
}
