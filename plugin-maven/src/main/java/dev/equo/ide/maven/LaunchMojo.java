/*******************************************************************************
 * Copyright (c) 2022 EquoTech, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     EquoTech, Inc. - initial API and implementation
 *******************************************************************************/
package dev.equo.ide.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;

import com.diffplug.common.swt.os.OS;

import dev.equo.ide.BuildPluginIdeMain;
import dev.equo.ide.IdeHook;
import dev.equo.ide.IdeHookBranding;
import dev.equo.ide.IdeHookWelcome;
import dev.equo.solstice.NestedJars;
import dev.equo.solstice.p2.P2Client;
import dev.equo.solstice.p2.P2Unit;

/** Launches an Eclipse-based IDE for this project. */
@Mojo(name = "launch")
public class LaunchMojo extends AbstractP2Mojo {
	@Parameter(required = false)
	private Branding branding = new Branding();

	@Parameter(required = false)
	private Welcome welcome = new Welcome();

	/** Wipes all IDE settings and state before rebuilding and launching. */
	@Parameter(property = "clean", defaultValue = "false")
	private boolean clean;

	/** Initializes the runtime to check for errors then exits. */
	@Parameter(property = "initOnly", defaultValue = "false")
	private boolean initOnly;

	/** Adds a visible console to the launched application. */
	@Parameter(property = "showConsole", defaultValue = "false")
	private boolean showConsole;

	/** Dumps the classpath (in order) without starting the application. */
	@Parameter(property = "debugClasspath", defaultValue = "disabled")
	private BuildPluginIdeMain.DebugClasspath debugClasspath;

	/** Determines whether to use Solstice's built-in OSGi runtime or instead Atomos+Equinox. */
	@Parameter(property = "useAtomos", defaultValue = "false")
	private boolean useAtomos;

	/** Blocks IDE startup to help you attach a debugger. */
	@Parameter(property = "debugIde", defaultValue = "false")
	private boolean debugIde;

	@Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
	protected File baseDir;

	@Component protected RepositorySystem repositorySystem;

	@Parameter(defaultValue = "${repositorySystemSession}", required = true, readonly = true)
	protected RepositorySystemSession repositorySystemSession;

	@Parameter(defaultValue = "${project.remotePluginRepositories}", required = true, readonly = true)
	protected List<RemoteRepository> repositories;

	private static final List<Exclusion> EXCLUDE_ALL_TRANSITIVES =
			Collections.singletonList(new Exclusion("*", "*", "*", "*"));

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			var caller = BuildPluginIdeMain.Caller.forProjectDir(baseDir, clean);

			List<Dependency> deps = new ArrayList<>();
			deps.add(
					new Dependency(
							new DefaultArtifact("dev.equo.ide:solstice:" + NestedJars.solsticeVersion()), null));
			for (var dep : NestedJars.transitiveDeps(useAtomos, NestedJars.CoordFormat.MAVEN)) {
				deps.add(new Dependency(new DefaultArtifact(dep), null, null, EXCLUDE_ALL_TRANSITIVES));
			}
			var query = query();
			for (var dep : query.getJarsOnMavenCentral()) {
				deps.add(new Dependency(new DefaultArtifact(dep), null, null, EXCLUDE_ALL_TRANSITIVES));
			}
			CollectRequest collectRequest = new CollectRequest(deps, null, repositories);
			DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
			DependencyResult dependencyResult =
					repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);

			var files = new ArrayList<File>();
			for (var artifact : dependencyResult.getArtifactResults()) {
				files.add(artifact.getArtifact().getFile());
			}
			try (var client = new P2Client()) {
				for (P2Unit unit : query.getJarsNotOnMavenCentral()) {
					files.add(client.download(unit));
				}
			}

			var ideHooks = new IdeHook.List();
			ideHooks.add(
					new IdeHookBranding().title(branding.title).icon(branding.icon).splash(branding.splash));
			if (welcome != null) {
				var welcomeHook = new IdeHookWelcome();
				welcomeHook.openUrl(welcome.openUrl);
				ideHooks.add(welcomeHook);
			}

			if (!OS.getNative().isMac()) {
				System.setProperty("equo-ide-maven-workarounds", "true");
			}
			caller.ideHooks = ideHooks;
			caller.classpath = files;
			caller.debugClasspath = debugClasspath;
			caller.initOnly = initOnly;
			caller.showConsole = showConsole;
			caller.useAtomos = useAtomos;
			caller.debugIde = debugIde;
			caller.showConsoleFlag = "-DshowConsole";
			caller.cleanFlag = "-Dclean";
			caller.launch();
		} catch (DependencyResolutionException | IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
