package io.github.nahkd123.inkingcraft.client.config.binding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.inking.api.tablet.ButtonType;
import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inkingcraft.client.config.binding.target.BindingButtonTarget;
import io.github.nahkd123.inkingcraft.client.config.binding.target.BindingPenTarget;
import io.github.nahkd123.inkingcraft.client.config.binding.trigger.BindingTrigger;

public class BindingConfiguration {
	private MouseEmulation mouse;
	private BindingTrigger penTipTrigger;
	private BindingTrigger penEraserTrigger;
	private EnumMap<ButtonType, List<BindingTrigger>> buttonTriggers = new EnumMap<>(ButtonType.class);

	public BindingConfiguration(MouseEmulation mouse, Collection<Binding> binding) {
		this.mouse = mouse;
		setAllBindings(binding);
	}

	public MouseEmulation getMouse() { return mouse; }

	public BindingTrigger getPenTipTrigger() { return penTipTrigger; }

	public void setPenTipTrigger(BindingTrigger penTipTrigger) { this.penTipTrigger = penTipTrigger; }

	public BindingTrigger getPenEraserTrigger() { return penEraserTrigger; }

	public void setPenEraserTrigger(BindingTrigger penEraserTrigger) { this.penEraserTrigger = penEraserTrigger; }

	public BindingTrigger getButtonTrigger(ButtonType type, int index) {
		List<BindingTrigger> list = buttonTriggers.get(type);
		if (list == null) return null;
		return index < list.size() ? list.get(index) : null;
	}

	public void setButtonTrigger(ButtonType type, int index, BindingTrigger trigger) {
		List<BindingTrigger> list = buttonTriggers.get(type);
		if (list == null) buttonTriggers.put(type, list = new ArrayList<>());
		while (index >= list.size()) list.add(null);
		list.set(index, trigger);
	}

	public List<Binding> getAllBindings() {
		List<Binding> list = new ArrayList<>();
		if (penTipTrigger != null) list.add(new Binding(BindingPenTarget.TIP, penTipTrigger));
		if (penEraserTrigger != null) list.add(new Binding(BindingPenTarget.ERASER, penEraserTrigger));
		buttonTriggers.entrySet().forEach(e -> {
			List<BindingTrigger> triggersList = e.getValue();

			for (int i = 0; i < triggersList.size(); i++) {
				BindingTrigger trigger = triggersList.get(i);
				if (trigger == null) continue;
				list.add(new Binding(new BindingButtonTarget(e.getKey(), i), trigger));
			}
		});
		return Collections.unmodifiableList(list);
	}

	public void setBinding(Binding binding) {
		if (binding.target() instanceof BindingPenTarget pen) {
			switch (pen) {
			case TIP:
				penTipTrigger = binding.trigger();
				return;
			case ERASER:
				penEraserTrigger = binding.trigger();
				return;
			default:
				return;
			}
		}

		if (binding.target() instanceof BindingButtonTarget btn)
			setButtonTrigger(btn.buttonType(), btn.buttonIndex(), binding.trigger());
	}

	public void setAllBindings(Collection<Binding> coll) {
		coll.forEach(this::setBinding);
	}

	public void clearBindings() {
		penTipTrigger = null;
		penEraserTrigger = null;
		buttonTriggers.clear();
	}

	public List<BindingSlot> getSlots(TabletSpec spec) {
		List<BindingSlot> list = new ArrayList<>();
		list.add(new BindingSlot(BindingPenTarget.TIP, () -> penTipTrigger, t -> penTipTrigger = t));
		list.add(new BindingSlot(BindingPenTarget.ERASER, () -> penEraserTrigger, t -> penEraserTrigger = t));

		for (ButtonType type : ButtonType.values()) {
			for (int i = 0; i < spec.getButtonsCount(type); i++) {
				final int j = i;
				// @formatter:off
				list.add(new BindingSlot(
					new BindingButtonTarget(type, i),
					() -> getButtonTrigger(type, j),
					t -> setButtonTrigger(type, j, t)));
				// @formatter:on
			}
		}

		return list;
	}

	public static final Codec<BindingConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		MouseEmulation.CODEC.fieldOf("mouse").forGetter(BindingConfiguration::getMouse),
		Binding.CODEC.listOf().fieldOf("bindings").forGetter(BindingConfiguration::getAllBindings))
		.apply(instance, BindingConfiguration::new));

	public static BindingConfiguration createDefault() {
		return new BindingConfiguration(new MouseEmulation(), Collections.emptyList());
	}
}
