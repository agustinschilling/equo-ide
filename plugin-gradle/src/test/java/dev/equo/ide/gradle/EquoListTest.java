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
package dev.equo.ide.gradle;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({SnapshotExtension.class})
public class EquoListTest extends GradleHarness {
	@Test
	public void help(Expect expect) throws IOException {
		setFile("build.gradle").toContent("plugins { id 'dev.equo.ide' }");
		run("-q", "help", "--task", "equoList").snapshot(expect);
	}

	@Test
	public void defaultP2(Expect expect) throws IOException {
		setFile("build.gradle")
				.toLines(
						"plugins { id 'dev.equo.ide' }",
						"equoIde {",
						"  release('4.26')",
						"  filter {",
						"    setPlatform(null)",
						"  }",
						"}");
		run("equoList", "--installed").snapshotBetween("Task :equoList", "BUILD SUCCESSFUL", expect);
	}

	@Test
	public void installedEmpty(Expect expect) throws IOException {
		setFile("build.gradle")
				.toLines(
						"plugins { id 'dev.equo.ide' }",
						"equoIde {",
						"  p2repo 'https://download.eclipse.org/eclipse/updates/4.26/'",
						"}");
		run("equoList", "--installed").snapshotBetween("Task :equoList", "BUILD SUCCESSFUL", expect);
	}

	@Test
	public void installedSwt(Expect expect) throws IOException {
		setFile("build.gradle")
				.toLines(
						"plugins { id 'dev.equo.ide' }",
						"equoIde {",
						"  p2repo 'https://download.eclipse.org/eclipse/updates/4.26/'",
						"  install 'org.eclipse.swt'",
						"  filter {",
						"    setPlatform(null)",
						"  }",
						"}");
		run("equoList", "--installed").snapshotBetween("Task :equoList", "BUILD SUCCESSFUL", expect);
	}

	@Test
	public void installedSwtCsv(Expect expect) throws IOException {
		setFile("build.gradle")
				.toLines(
						"plugins { id 'dev.equo.ide' }",
						"equoIde {",
						"  p2repo 'https://download.eclipse.org/eclipse/updates/4.26/'",
						"  install 'org.eclipse.swt'",
						"  filter {",
						"    setPlatform(null)",
						"  }",
						"}");
		run("equoList", "--installed", "--format=csv")
				.snapshotBetween("Task :equoList", "BUILD SUCCESSFUL", expect);
	}

	@Test
	public void allFeatures(Expect expect) throws IOException {
		setFile("build.gradle")
				.toLines(
						"plugins { id 'dev.equo.ide' }",
						"equoIde {",
						"  p2repo 'https://download.eclipse.org/eclipse/updates/4.26/'",
						"  install 'org.eclipse.swt'",
						"  filter {",
						"    setPlatform(null)",
						"  }",
						"}");
		run("equoList", "--all=features").snapshotBetween("Task :equoList", "BUILD SUCCESSFUL", expect);
	}

	@Test
	public void allCategories(Expect expect) throws IOException {
		setFile("build.gradle")
				.toLines(
						"plugins { id 'dev.equo.ide' }",
						"equoIde {",
						"  p2repo 'https://download.eclipse.org/eclipse/updates/4.26/'",
						"  install 'org.eclipse.swt'",
						"  filter {",
						"    setPlatform(null)",
						"  }",
						"}");
		run("equoList", "--all=categories")
				.snapshotBetween("Task :equoList", "BUILD SUCCESSFUL", expect);
	}

	@Test
	public void allJars(Expect expect) throws IOException {
		setFile("build.gradle")
				.toLines(
						"plugins { id 'dev.equo.ide' }",
						"equoIde {",
						"  p2repo 'https://download.eclipse.org/eclipse/updates/4.26/'",
						"  install 'org.eclipse.swt'",
						"  filter {",
						"    setPlatform(null)",
						"    excludeSuffix '.source'  // no source bundles",
						"    excludePrefix 'tooling'  // ignore internal tooling",
						"    exclude 'org.apache.sshd.osgi' // we don't want sshd",
						"  }",
						"}");
		run("equoList", "--all=jars", "--format=csv")
				.snapshotBetween("Task :equoList", "BUILD SUCCESSFUL", expect);
	}

	@Test
	public void detail(Expect expect) throws IOException {
		setFile("build.gradle")
				.toLines(
						"plugins { id 'dev.equo.ide' }",
						"equoIde {",
						"  p2repo 'https://download.eclipse.org/eclipse/updates/4.26/'",
						"  install 'org.eclipse.jdt.annotation'",
						"  filter {",
						"    setPlatform(null)",
						"  }",
						"}");
		run("equoList", "--detail=org.eclipse.jdt.annotation")
				.snapshotBetween("Task :equoList", "BUILD SUCCESSFUL", expect);
	}

	@Test
	public void raw(Expect expect) throws IOException {
		setFile("build.gradle")
				.toLines(
						"plugins { id 'dev.equo.ide' }",
						"equoIde {",
						"  p2repo 'https://download.eclipse.org/eclipse/updates/4.26/'",
						"  install 'org.eclipse.jdt.annotation'",
						"  filter {",
						"    setPlatform(null)",
						"  }",
						"}");
		run("equoList", "--raw=org.eclipse.jdt.annotation")
				.snapshotBetween("Task :equoList", "BUILD SUCCESSFUL", expect);
	}
}
