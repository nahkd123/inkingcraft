package io.github.nahkd123.inkingcraft.client.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.inking.api.util.ConstantVector2;
import io.github.nahkd123.inking.api.util.Vector2;

public record Rectangle(double x, double y, double width, double height) {
	public Rectangle(Vector2 origin, Vector2 size) {
		this(origin.x(), origin.y(), size.x(), size.y());
	}

	public ConstantVector2 getOrigin() { return new ConstantVector2(x, y); }

	public ConstantVector2 getSize() { return new ConstantVector2(width, height); }

	public Rectangle withTranslation(double x, double y) {
		return new Rectangle(this.x + x, this.y + y, width, height);
	}

	public Rectangle withTopLeftCornerResize(double x, double y) {
		return new Rectangle(this.x + x, this.y + y, width - x, height - y);
	}

	public Rectangle withTopRightCornerResize(double x, double y) {
		return new Rectangle(this.x, this.y + y, width + x, height - y);
	}

	public Rectangle withBottomLeftCornerResize(double x, double y) {
		return new Rectangle(this.x + x, this.y, width - x, height + y);
	}

	public Rectangle withBottomRightCornerResize(double x, double y) {
		return new Rectangle(this.x, this.y, width + x, height + y);
	}

	public static final Codec<Rectangle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.DOUBLE.fieldOf("x").forGetter(Rectangle::x),
		Codec.DOUBLE.fieldOf("y").forGetter(Rectangle::y),
		Codec.DOUBLE.fieldOf("width").forGetter(Rectangle::width),
		Codec.DOUBLE.fieldOf("height").forGetter(Rectangle::height))
		.apply(instance, Rectangle::new));
}
