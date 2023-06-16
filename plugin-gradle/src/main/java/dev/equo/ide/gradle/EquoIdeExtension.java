/*******************************************************************************
 * Copyright (c) 2022-2023 EquoTech, Inc. and others.
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

import dev.equo.ide.EquoChromium;
import dev.equo.ide.IdeHook;
import dev.equo.ide.IdeHookBranding;
import dev.equo.ide.IdeHookWelcome;
import dev.equo.ide.WorkspaceInit;
import dev.equo.solstice.p2.P2Model;
import org.gradle.api.Action;
import org.gradle.api.Project;

/** The DSL inside the equoIde block. */
public class EquoIdeExtension extends P2ModelDslWithCatalog {
	public boolean useAtomos = false;
	private final IdeHook.List ideHooks = new IdeHook.List();
	public final IdeHookBranding branding = new IdeHookBranding();

	public EquoIdeExtension(Project project) {
		super(project);
		ideHooks.add(branding);
	}

	public void useChromium() {
		ideHooks.add(new EquoChromium());
	}

	public IdeHookBranding branding() {
		return branding;
	}

	private IdeHookWelcome welcome = null;

	public IdeHookWelcome welcome() {
		if (welcome == null) {
			welcome = new IdeHookWelcome();
			ideHooks.add(welcome);
		}
		return welcome;
	}

	public IdeHook.List getIdeHooks() {
		return ideHooks;
	}

	WorkspaceInit workspace = new WorkspaceInit();

	public class SetWorkspaceFile {
		private final String subpath;

		SetWorkspaceFile(String subpath) {
			this.subpath = subpath;
		}

		public void prop(String key, String value) {
			workspace.setProperty(subpath, key, value);
		}
	}

	public void workspaceInit(String subpath, Action<SetWorkspaceFile> action) {
		var setWorkspaceFile = new SetWorkspaceFile(subpath);
		action.execute(setWorkspaceFile);
	}

	P2Model prepareModel(WorkspaceInit workspaceInit) throws Exception {
		if (hasBeenPrepared) {
			return model;
		}
		hasBeenPrepared = true;
		catalog.putInto(model, ideHooks, workspaceInit);
		model.applyNativeFilterIfNoPlatformFilter();
		return model;
	}

	private boolean hasBeenPrepared = false;
}
