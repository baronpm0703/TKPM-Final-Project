package com.psvm.server.settings;

public enum Path {
	INSTANCE;

	private final String srcPath;
	private final String modelPath;
	private final String controllerPath;
	private final String viewPath;
	private final String resourcePath;

	Path() {
		this.srcPath = "server/src/";
		this.modelPath = "server/src/main/java/com/psvm/server/models/";
		this.controllerPath = "server/src/main/java/com/psvm/server/controllers/";
		this.viewPath = "server/src/main/java/com/psvm/server/views/";
		this.resourcePath = "server/src/main/resources/";
	}

	public Path getInstance() {
		return INSTANCE;
	}

	// Getters

	public static String getSrcPath() {
		return INSTANCE.srcPath;
	}
	public static String getModelPath() {
		return INSTANCE.modelPath;
	}
	public static String getControllerPath() {
		return INSTANCE.controllerPath;
	}
	public static String getViewPath() {
		return INSTANCE.viewPath;
	}
	public static String getResourcePath() {
		return INSTANCE.resourcePath;
	}
}
