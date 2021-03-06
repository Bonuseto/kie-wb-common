/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.cm.client.preferences;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.kie.workbench.common.stunner.cm.qualifiers.CaseManagementEditor;
import org.kie.workbench.common.stunner.core.client.preferences.DefaultPreferencesRegistry;
import org.kie.workbench.common.stunner.core.preferences.StunnerPreferences;

@ApplicationScoped
@CaseManagementEditor
public class CaseManagementPreferencesRegistry extends DefaultPreferencesRegistry {

    @Override
    public <T> T get(Class<T> preferenceType) {
        return super.get(preferenceType);
    }

    @Override
    public <T> void set(T preferences, Class<T> preferenceType) {
        super.set(preferences, preferenceType);
        applyCustomSettings(preferences);
    }

    private <T> void applyCustomSettings(T preferences) {
        Optional.of(preferences)
                .filter(p -> p instanceof StunnerPreferences)
                .map(p -> (StunnerPreferences) p)
                .map(StunnerPreferences::getDiagramEditorPreferences)
                .ifPresent(p -> p.setAutoHidePalettePanel(true));
    }
}