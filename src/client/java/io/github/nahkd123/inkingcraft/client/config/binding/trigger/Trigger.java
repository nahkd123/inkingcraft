package io.github.nahkd123.inkingcraft.client.config.binding.trigger;

@FunctionalInterface
public interface Trigger {
	public void onTrigger(boolean holding);
}
