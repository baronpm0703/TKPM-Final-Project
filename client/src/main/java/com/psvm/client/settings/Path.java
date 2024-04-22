package com.psvm.client.settings;

public enum Path {
	INSTANCE;

	private final String srcPath;
	private final String modelPath;
	private final String controllerPath;
	private final String viewPath;
	private final String resourcePath;

	Path() {
		this.srcPath = "client/src/";
		this.modelPath = "client/src/main/java/com/psvm/client/models/";
		this.controllerPath = "client/src/main/java/com/psvm/client/controllers/";
		this.viewPath = "client/src/main/java/com/psvm/client/views/";
		this.resourcePath = "client/src/main/resources/";
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
