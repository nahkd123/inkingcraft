package io.github.nahkd123.inkingcraft.client.utils;

import java.util.Arrays;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import io.github.nahkd123.inking.api.util.ConstantVector2;

public class InkingCodecs {
	public static final Codec<ConstantVector2> VECTOR2 = Codec.DOUBLE.listOf().xmap(
		list -> new ConstantVector2(list.get(0), list.get(1)),
		vec -> Arrays.asList(vec.x(), vec.y()));

	public static final Codec<Integer> COLOR = Codec
		.either(Codec.INT, Codec.STRING)
		.xmap(
			either -> either.map(i -> i, s -> Integer.parseInt(s, 16)),
			i -> Either.right(padStart(Integer.toString(i, 16), '0', 6)));

	private static String padStart(String s, char ch, int maxLen) {
		while (s.length() < maxLen) s = ch + s;
		return s;
	}

	public static <E extends Enum<E>> Codec<E> enumOf(Class<E> enumClass) {
		return Codec.STRING.xmap(
			s -> Enum.valueOf(enumClass, s),
			e -> e.toString());
	}
}
