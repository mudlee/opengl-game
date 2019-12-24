package spck.game.ui;

import spck.engine.Engine;
import spck.engine.bus.LifeCycle;
import spck.engine.bus.MessageBus;
import spck.engine.debug.Measure;
import spck.engine.debug.Stats;
import spck.engine.ecs.ECS;
import spck.engine.framework.RGBAColor;
import spck.engine.render.camera.OrthoCamera;
import spck.engine.ui.Align;
import spck.engine.ui.Canvas;
import spck.engine.ui.Text;
import spck.engine.util.NumberFormatter;
import spck.engine.window.GLFWWindow;

public class DebugUI {
	private final GLFWWindow window;
	private final OrthoCamera camera;
	private final Text fps;
	private final Text vsync;
	private final Text renderTime;
	private final Text camPos;
	private final Text camRot;
	private final Text camSize;
	private final Text verts;
	private final Text vertsTotal;
	private final Text batchGroups;
	private final Text batches;
	private final Text numOfEntities;
	private final Text vboMemoryUsed;
	private final Text jvmMemoryFree;
	private final Text jvmMemoryAllocated;
	private final Text jvmMemoryMax;
	private final Text jvmMemoryTotalFree;
	private final Text aabbRendering;

	public DebugUI(ECS ecs, GLFWWindow window, OrthoCamera camera) {
		this.window = window;
		this.camera = camera;
		MessageBus.register(LifeCycle.UPDATE.eventID(), this::onUpdate);

		Canvas debugCanvas = (Canvas) ecs.createEntity(new Canvas());
		fps = debugCanvas.addText(Text.Builder.create().withX(10).withY(10).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		vsync = debugCanvas.addText(Text.Builder.create().withX(10).withY(45).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		renderTime = debugCanvas.addText(Text.Builder.create().withX(10).withY(80).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		camPos = debugCanvas.addText(Text.Builder.create().withX(10).withY(115).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		camRot = debugCanvas.addText(Text.Builder.create().withX(10).withY(150).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		camSize = debugCanvas.addText(Text.Builder.create().withX(10).withY(185).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		verts = debugCanvas.addText(Text.Builder.create().withX(10).withY(220).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		vertsTotal = debugCanvas.addText(Text.Builder.create().withX(10).withY(255).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		batchGroups = debugCanvas.addText(Text.Builder.create().withX(10).withY(290).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		batches = debugCanvas.addText(Text.Builder.create().withX(10).withY(325).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		numOfEntities = debugCanvas.addText(Text.Builder.create().withX(10).withY(360).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		vboMemoryUsed = debugCanvas.addText(Text.Builder.create().withX(10).withY(395).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		jvmMemoryFree = debugCanvas.addText(Text.Builder.create().withX(10).withY(430).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		jvmMemoryAllocated = debugCanvas.addText(Text.Builder.create().withX(10).withY(465).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		jvmMemoryMax = debugCanvas.addText(Text.Builder.create().withX(10).withY(500).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		jvmMemoryTotalFree = debugCanvas.addText(Text.Builder.create().withX(10).withY(535).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
		aabbRendering = debugCanvas.addText(Text.Builder.create().withX(10).withY(570).withAlign(Align.TOP_LEFT).withColor(RGBAColor.black()).build());
	}

	private void onUpdate() {
		Runtime runtime = Runtime.getRuntime();
		
		fps.setText("FPS: " + Measure.getLastFPS());
		vsync.setText("Vsync: " + String.valueOf(window.isvSync()).toUpperCase());
		renderTime.setText(String.format("Render time: %.2fms, (GFX: %.2fms)", Measure.getLastRenderTime(), Measure.getLastGraphicsRenderTime()));
		camPos.setText(String.format("Cam pos: X:%.2f Y:%.2f Z:%.2f", camera.getPosition().x, camera.getPosition().y, camera.getPosition().z));
		camRot.setText(String.format("Cam rot: X:%.2f Y:%.2f Z:%.2f", camera.getRotation().x, camera.getRotation().y, camera.getRotation().z));
		camSize.setText(String.format("Cam size: %.2f", camera.getSize()));
		verts.setText("Verts: " + NumberFormatter.formatSimple(Stats.numOfVerts));
		vertsTotal.setText("Verts total: " + NumberFormatter.formatSimple(Stats.numOfTotalVerts));
		batchGroups.setText("Batch groups: " + NumberFormatter.formatSimple(Stats.numOfBatchGroups));
		batches.setText("Batches total: " + NumberFormatter.formatSimple(Stats.numOfBatches));
		numOfEntities.setText("Num of entities: " + NumberFormatter.formatSimple(Stats.numOfEntities));
		vboMemoryUsed.setText("VBO mem used: " + NumberFormatter.formatBinaryUnit(Stats.vboMemoryUsed) + (Stats.vboMemoryMisused ? " - !!! INVALID VBO-OFFSET USAGE !!!" : ""));
		jvmMemoryFree.setText("JVM mem free: " + NumberFormatter.formatBinaryUnit(runtime.freeMemory()));
		jvmMemoryAllocated.setText("JVM mem allocated: " + NumberFormatter.formatBinaryUnit(runtime.totalMemory()));
		jvmMemoryMax.setText("JVM mem max: " + NumberFormatter.formatBinaryUnit(runtime.maxMemory()));
		jvmMemoryTotalFree.setText("JVM mem total free: " + NumberFormatter.formatBinaryUnit(runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())));
		aabbRendering.setText("Showing AABB: " + Engine.preferences.renderAABB);
	}
}
