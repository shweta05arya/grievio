package com.grievio;

/**
 * GrievioLauncher — Plain (non-JavaFX) entry point.
 *
 * WHY THIS EXISTS:
 * When JavaFX is bundled into a fat/shaded JAR, the JVM checks if the
 * Main-Class in MANIFEST.MF extends javafx.application.Application.
 * If it does, it throws "JavaFX runtime components are missing" even
 * though JavaFX IS bundled inside the JAR.
 *
 * The fix: make the manifest point to THIS plain class, which then
 * calls MainApp.main(). The JVM doesn't inspect classes called
 * indirectly, so JavaFX initialises correctly.
 */
public class GrievioLauncher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
