package io.github.nahkd123.inkingcraft.client.config.binding.trigger;

import com.mojang.serialization.Codec;

public interface BindingTrigger extends Trigger {
	public BindingTriggerFactory getFactory();

	public static final Codec<BindingTrigger> CODEC = BindingTriggerFactory.CODEC.dispatch(
		"type",
		BindingTrigger::getFactory,
		BindingTriggerFactory::getCodec);
}
