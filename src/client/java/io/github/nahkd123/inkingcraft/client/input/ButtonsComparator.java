package io.github.nahkd123.inkingcraft.client.input;

import java.util.function.IntConsumer;

import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;

public class ButtonsComparator {
	public static void compareDifferences(int max, Int2BooleanFunction last, Int2BooleanFunction current, IntConsumer differencesFound) {
		for (int i = 0; i < max; i++) if (last.get(i) ^ current.get(i)) differencesFound.accept(i);
	}
}
