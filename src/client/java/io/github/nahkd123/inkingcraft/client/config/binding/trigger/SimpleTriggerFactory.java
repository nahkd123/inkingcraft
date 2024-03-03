package io.github.nahkd123.inkingcraft.client.config.binding.trigger;

import com.mojang.serialization.Codec;

import net.minecraft.text.Text;

public class SimpleTriggerFactory implements BindingTriggerFactory {
	private Text name;
	private Text description;
	private BindingTrigger bindingTrigger;

	public SimpleTriggerFactory(Text name, Text description, Trigger trigger) {
		this.name = name;
		this.description = description;
		this.bindingTrigger = new BindingTrigger() {
			@Override
			public void onTrigger(boolean holding) {
				trigger.onTrigger(holding);
			}

			@Override
			public BindingTriggerFactory getFactory() { return SimpleTriggerFactory.this; }
		};
	}

	@Override
	public Codec<BindingTrigger> getCodec() { return Codec.unit(bindingTrigger); }

	@Override
	public BindingTrigger createDefault() {
		return bindingTrigger;
	}

	@Override
	public Text getName() { return name; }

	@Override
	public Text getDescription() { return description; }
}
