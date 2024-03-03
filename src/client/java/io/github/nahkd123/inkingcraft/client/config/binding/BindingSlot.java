package io.github.nahkd123.inkingcraft.client.config.binding;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.nahkd123.inkingcraft.client.config.binding.target.BindingTarget;
import io.github.nahkd123.inkingcraft.client.config.binding.trigger.BindingTrigger;
import io.github.nahkd123.inkingcraft.client.config.binding.trigger.InkingTriggers;

public record BindingSlot(BindingTarget target, Supplier<BindingTrigger> getter, Consumer<BindingTrigger> setter) {
	public BindingTrigger get() {
		BindingTrigger trigger = getter.get();
		return trigger != null ? trigger : InkingTriggers.EMPTY.createDefault();
	}

	public void set(BindingTrigger trigger) {
		if (trigger == InkingTriggers.EMPTY.createDefault()) trigger = null;
		setter.accept(trigger);
	}
}
