/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.dmn.client.editors.included.imports.persistence;

import java.util.List;
import java.util.Objects;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.kie.workbench.common.dmn.api.definition.v1_1.Import;
import org.kie.workbench.common.dmn.api.property.dmn.Name;
import org.kie.workbench.common.dmn.client.editors.common.messages.FlashMessage;
import org.kie.workbench.common.dmn.client.editors.common.persistence.RecordEngine;
import org.kie.workbench.common.dmn.client.editors.included.IncludedModel;
import org.kie.workbench.common.dmn.client.editors.included.imports.IncludedModelsIndex;
import org.kie.workbench.common.dmn.client.editors.included.imports.IncludedModelsPageStateProviderImpl;
import org.kie.workbench.common.dmn.client.editors.included.imports.messages.IncludedModelErrorMessageFactory;

import static java.util.Collections.singletonList;
import static org.kie.workbench.common.stunner.core.util.StringUtils.isEmpty;

public class ImportRecordEngine implements RecordEngine<IncludedModel> {

    private final IncludedModelsPageStateProviderImpl stateProvider;

    private final IncludedModelsIndex includedModelsIndex;

    private final IncludedModelErrorMessageFactory messageFactory;

    private final Event<FlashMessage> flashMessageEvent;

    @Inject
    public ImportRecordEngine(final IncludedModelsPageStateProviderImpl stateProvider,
                              final IncludedModelsIndex includedModelsIndex,
                              final IncludedModelErrorMessageFactory messageFactory,
                              final Event<FlashMessage> flashMessageEvent) {
        this.stateProvider = stateProvider;
        this.includedModelsIndex = includedModelsIndex;
        this.messageFactory = messageFactory;
        this.flashMessageEvent = flashMessageEvent;
    }

    @Override
    public List<IncludedModel> update(final IncludedModel record) {
        if (!record.isValid()) {
            throw new UnsupportedOperationException("An invalid Included Model cannot be updated.");
        }
        getImport(record).setName(new Name(record.getName()));
        return singletonList(record);
    }

    @Override
    public List<IncludedModel> destroy(final IncludedModel record) {
        stateProvider.getImports().remove(getImport(record));
        return singletonList(record);
    }

    @Override
    public List<IncludedModel> create(final IncludedModel record) {
        // TODO: https://issues.jboss.org/browse/DROOLS-3719
        return singletonList(record);
    }

    @Override
    public boolean isValid(final IncludedModel record) {

        if (!isUniqueName(record)) {
            flashMessageEvent.fire(messageFactory.getNameIsNotUniqueFlashMessage(record));
            return false;
        }

        if (isBlankName(record)) {
            flashMessageEvent.fire(messageFactory.getNameIsBlankFlashMessage(record));
            return false;
        }

        return true;
    }

    private boolean isUniqueName(final IncludedModel record) {
        return stateProvider
                .getImports()
                .stream()
                .noneMatch(anImport -> !sameImport(record, anImport) && sameName(record, anImport));
    }

    private boolean isBlankName(final IncludedModel record) {
        return isEmpty(record.getName());
    }

    private boolean sameName(final IncludedModel record, final Import anImport) {
        return Objects.equals(record.getName(), anImport.getName().getValue());
    }

    private boolean sameImport(final IncludedModel record, final Import anImport) {
        final Import recordImport = getImport(record);
        return Objects.equals(recordImport, anImport);
    }

    private Import getImport(final IncludedModel record) {
        return includedModelsIndex.getImport(record);
    }
}
