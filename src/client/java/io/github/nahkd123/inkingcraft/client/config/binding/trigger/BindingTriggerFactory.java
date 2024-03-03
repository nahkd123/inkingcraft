package io.github.nahkd123.inkingcraft.client.config.binding.trigger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public interface BindingTriggerFactory {
	public Codec<BindingTrigger> getCodec();

	public BindingTrigger createDefault();

	public Text getName();

	public Text getDescription();

	public static final BiMap<Identifier, BindingTriggerFactory> REGISTRY = HashBiMap.create();

	default void register(Identifier id) {
		if (REGISTRY.containsKey(id)) throw new IllegalStateException("ID " + id + " is already registered!");
		REGISTRY.put(id, this);
	}

	public static final Codec<BindingTriggerFactory> CODEC = Identifier.CODEC.xmap(
		id -> REGISTRY.get(id),
		factory -> REGISTRY.inverse().get(factory));
}
