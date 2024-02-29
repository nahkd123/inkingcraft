package io.github.nahkd123.inkingcraft.client.mixin;

import java.util.Map;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inkingcraft.client.InkingCraftClient;
import io.github.nahkd123.inkingcraft.client.config.TabletConfiguration;
import io.github.nahkd123.inkingcraft.client.input.TabletInput;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Inject(method = "render", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/gui/DrawContext;draw()V",
		shift = At.Shift.BEFORE))
	// @formatter:off
	private void inking$renderPointers(
		float tickDelta, long startTime, boolean tick,
		CallbackInfo ci,
		// Locals
		float tickDelta2,
		boolean isFinishedLoading,
		int scaledMx,
		int scaledMy,
		Window window,
		Matrix4f viewMat,
		MatrixStack matrixStack,
		DrawContext ctx
	) {
		// @formatter:on
		Map<String, TabletInput> pointers = InkingCraftClient.getInputManager().getAll();

		for (Map.Entry<String, TabletInput> e : pointers.entrySet()) {
			TabletSpec spec = InkingCraftClient.getSpecificationsStore().getAllSpecifications().get(e.getKey());
			TabletConfiguration config = InkingCraftClient.getConfigStore().getConfigurations().get(e.getKey());
			TabletInput input = e.getValue();
			input.renderPointer(ctx, spec, config);
		}
	}
}
