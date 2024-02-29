package io.github.nahkd123.inkingcraft.client.utils;

import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;

public class PenRenderer {
	public static void renderPenAtZero(DrawContext context, double pressure, double tiltX, double tiltY, int baseColor) {
		int opaqueColor = baseColor | (0xFF << 24);
		int halfColor = baseColor | (0x7F << 24);

		Quaterniond quat = new Quaterniond()
			.rotateAxis(Math.toRadians(tiltY), new Vector3d(1, 0, 0))
			.rotateAxis(Math.toRadians(tiltX), new Vector3d(0, 0, 1))
			.normalize();
		Vector3f pen = quat.transform(new Vector3f(0, 1, 0));
		Vector3f projected = new Vector3f(pen.x, 0, pen.z).normalize();
		Vector3f up = new Vector3f(0, 1, 0);
		Vector3f side = projected.cross(up);

		VertexConsumer vc = context.getVertexConsumers().getBuffer(RenderLayer.getGui());
		Matrix4f mat = context.getMatrices().peek().getPositionMatrix();
		float upDistance = 5 + (1 - pen.y * pen.y) * 20;
		float bodyLength = 100 * (1 - pen.y * pen.y * pen.y);
		float pressureF = (float) pressure;

		// @formatter:off
		// Pen tip
		vc.vertex(mat, 0, 0, 0).color(halfColor).next();
		vc.vertex(mat, -projected.z * upDistance + side.x * 5, -projected.x * upDistance - side.z * 5, 0).color(halfColor).next();
		vc.vertex(mat, -projected.z * upDistance - side.x * 5, -projected.x * upDistance + side.z * 5, 0).color(halfColor).next();
		vc.vertex(mat, 0, 0, 0).color(halfColor).next();

		// Pen pressure
		vc.vertex(mat, 0, 0, 0).color(opaqueColor).next();
		vc.vertex(mat, -projected.z * upDistance * pressureF + side.x * 5 * pressureF, -projected.x * upDistance * pressureF - side.z * 5 * pressureF, 0).color(opaqueColor).next();
		vc.vertex(mat, -projected.z * upDistance * pressureF - side.x * 5 * pressureF, -projected.x * upDistance * pressureF + side.z * 5 * pressureF, 0).color(opaqueColor).next();
		vc.vertex(mat, 0, 0, 0).color(opaqueColor).next();

		// Pen body
		vc.vertex(mat, -projected.z * upDistance - side.x * 5, -projected.x * upDistance + side.z * 5, 0).color(halfColor).next();
		vc.vertex(mat, -projected.z * upDistance + side.x * 5, -projected.x * upDistance - side.z * 5, 0).color(halfColor).next();
		vc.vertex(mat, -projected.z * bodyLength + side.x * 5, -projected.x * bodyLength - side.z * 5, 0).color(baseColor).next();
		vc.vertex(mat, -projected.z * bodyLength - side.x * 5, -projected.x * bodyLength + side.z * 5, 0).color(baseColor).next();
		// @formatter:on
	}
}
