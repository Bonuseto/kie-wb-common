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

package org.kie.workbench.common.dmn.client.widgets.grid;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import javax.enterprise.event.Event;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.dmn.api.definition.HasExpression;
import org.kie.workbench.common.dmn.api.definition.HasName;
import org.kie.workbench.common.dmn.api.definition.NOPDomainObject;
import org.kie.workbench.common.dmn.api.definition.v1_1.DMNModelInstrumentedBase;
import org.kie.workbench.common.dmn.api.definition.v1_1.Expression;
import org.kie.workbench.common.dmn.client.commands.factory.DefaultCanvasCommandFactory;
import org.kie.workbench.common.dmn.client.commands.factory.canvas.SetComponentWidthCommand;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.container.CellEditorControlsView;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.HasListSelectorControl;
import org.kie.workbench.common.dmn.client.widgets.grid.model.DMNGridColumn;
import org.kie.workbench.common.dmn.client.widgets.layer.DMNGridLayer;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.event.selection.DomainObjectSelectionEvent;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommand;
import org.kie.workbench.common.stunner.core.client.command.SessionCommandManager;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.command.Command;
import org.kie.workbench.common.stunner.core.domainobject.DomainObject;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.forms.client.event.RefreshFormPropertiesEvent;
import org.uberfire.ext.wires.core.grids.client.model.GridData;
import org.uberfire.ext.wires.core.grids.client.widget.grid.impl.BaseGridWidget;
import org.uberfire.ext.wires.core.grids.client.widget.grid.renderers.grids.GridRenderer;

public abstract class BaseGrid<E extends Expression> extends BaseGridWidget implements HasListSelectorControl {

    protected Optional<String> nodeUUID;
    protected HasExpression hasExpression;
    protected Optional<HasName> hasName = Optional.empty();
    protected Optional<DomainObject> selectedDomainObject = Optional.empty();

    protected final DMNGridLayer gridLayer;
    protected final SessionManager sessionManager;
    protected final SessionCommandManager<AbstractCanvasHandler> sessionCommandManager;
    protected final DefaultCanvasCommandFactory canvasCommandFactory;

    protected final Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent;
    protected final Event<DomainObjectSelectionEvent> domainObjectSelectionEvent;
    protected final CellEditorControlsView.Presenter cellEditorControls;
    protected final TranslationService translationService;

    public BaseGrid(final DMNGridLayer gridLayer,
                    final GridData gridData,
                    final GridRenderer gridRenderer,
                    final SessionManager sessionManager,
                    final SessionCommandManager<AbstractCanvasHandler> sessionCommandManager,
                    final DefaultCanvasCommandFactory canvasCommandFactory,
                    final Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent,
                    final Event<DomainObjectSelectionEvent> domainObjectSelectionEvent,
                    final CellEditorControlsView.Presenter cellEditorControls,
                    final TranslationService translationService) {
        this(Optional.empty(),
             HasExpression.NOP,
             Optional.empty(),
             gridLayer,
             gridData,
             gridRenderer,
             sessionManager,
             sessionCommandManager,
             canvasCommandFactory,
             refreshFormPropertiesEvent,
             domainObjectSelectionEvent,
             cellEditorControls,
             translationService);
    }

    public BaseGrid(final Optional<String> nodeUUID,
                    final HasExpression hasExpression,
                    final Optional<HasName> hasName,
                    final DMNGridLayer gridLayer,
                    final GridData gridData,
                    final GridRenderer gridRenderer,
                    final SessionManager sessionManager,
                    final SessionCommandManager<AbstractCanvasHandler> sessionCommandManager,
                    final DefaultCanvasCommandFactory canvasCommandFactory,
                    final Event<RefreshFormPropertiesEvent> refreshFormPropertiesEvent,
                    final Event<DomainObjectSelectionEvent> domainObjectSelectionEvent,
                    final CellEditorControlsView.Presenter cellEditorControls,
                    final TranslationService translationService) {
        super(gridData,
              gridLayer,
              gridLayer,
              gridRenderer);
        this.nodeUUID = nodeUUID;
        this.hasExpression = hasExpression;
        this.hasName = hasName;
        this.gridLayer = gridLayer;
        this.sessionManager = sessionManager;
        this.sessionCommandManager = sessionCommandManager;
        this.canvasCommandFactory = canvasCommandFactory;

        this.refreshFormPropertiesEvent = refreshFormPropertiesEvent;
        this.domainObjectSelectionEvent = domainObjectSelectionEvent;
        this.cellEditorControls = cellEditorControls;
        this.translationService = translationService;
    }

    protected double getAndSetInitialWidth(final int uiColumnIndex,
                                           final double initialWidth) {
        if (getExpression().get().isPresent()) {
            final Expression e = getExpression().get().get();
            final List<Double> componentWidths = e.getComponentWidths();
            if (Objects.isNull(componentWidths.get(uiColumnIndex))) {
                componentWidths.set(uiColumnIndex, initialWidth);
            }
            return componentWidths.get(uiColumnIndex);
        }
        throw new IllegalStateException("Unable to initialise column width if Expression has not been set.");
    }

    public Supplier<Optional<E>> getExpression() {
        return () -> Optional.ofNullable(cast(hasExpression.getExpression()));
    }

    @SuppressWarnings("unchecked")
    private E cast(final Expression e) {
        return (E) e;
    }

    protected void fireDomainObjectSelectionEvent() {
        final Optional<CanvasHandler> canvasHandler = getCanvasHandler();
        if (!canvasHandler.isPresent()) {
            return;
        }

        final Optional<DomainObject> domainObject = getDomainObject();
        if (!domainObject.isPresent()) {
            domainObjectSelectionEvent.fire(new DomainObjectSelectionEvent(canvasHandler.get(), new NOPDomainObject()));
            return;
        }

        fireDomainObjectSelectionEvent(domainObject.get());
    }

    protected void fireDomainObjectSelectionEvent(final DomainObject domainObject) {
        final Optional<CanvasHandler> canvasHandler = getCanvasHandler();
        if (!canvasHandler.isPresent()) {
            return;
        }

        final Optional<Node> domainObjectNode = findDomainObjectNodeByDomainObject(domainObject);

        if (domainObjectNode.isPresent()) {
            refreshFormPropertiesEvent.fire(new RefreshFormPropertiesEvent(getCurrentSession(), domainObjectNode.get().getUUID()));
            selectedDomainObject = Optional.empty();
        } else {
            domainObjectSelectionEvent.fire(new DomainObjectSelectionEvent(canvasHandler.get(), domainObject));
            selectedDomainObject = Optional.of(domainObject);
        }
    }

    private Optional<DomainObject> getDomainObject() {
        if (hasExpression == null) {
            return Optional.empty();
        }
        final DMNModelInstrumentedBase base = hasExpression.asDMNModelInstrumentedBase();
        if (base instanceof DomainObject) {
            return Optional.of((DomainObject) base);
        }
        return Optional.empty();
    }

    private Optional<CanvasHandler> getCanvasHandler() {
        final Optional<ClientSession> session = Optional.ofNullable(getCurrentSession());
        return session.map(ClientSession::getCanvasHandler);
    }

    private ClientSession getCurrentSession() {
        return sessionManager.getCurrentSession();
    }

    @SuppressWarnings("unchecked")
    private Optional<Node> findDomainObjectNodeByDomainObject(final DomainObject domainObject) {
        return getCanvasHandler()
                .map(canvasHandler -> {
                    final Graph<?, Node> graph = canvasHandler.getDiagram().getGraph();
                    return StreamSupport
                            .stream(graph.nodes().spliterator(), false)
                            .filter(node -> node.getContent() instanceof Definition)
                            .filter(node -> Objects.equals(domainObject, ((Definition) node.getContent()).getDefinition()))
                            .findFirst();
                })
                .orElse(Optional.empty());
    }

    /**
     * The width of a column is updated dynamically during a resize operation. This registers a {@link Command} on
     * the {@link SessionCommandManager} at the point the column resize operation completed to support "undo'ing"
     * column resize operations.
     * @param uiColumn The column being resized.
     * @param uiColumnInitialWidth The column's initial width when the resize operation started.
     */
    public void registerColumnResizeCompleted(final DMNGridColumn uiColumn,
                                              final double uiColumnInitialWidth) {
        getCanvasHandler().ifPresent(canvasHandler -> {
            final AbstractCanvasHandler abstractCanvasHandler = (AbstractCanvasHandler) canvasHandler;
            final CanvasCommand<AbstractCanvasHandler> command = new SetComponentWidthCommand(uiColumn,
                                                                                              uiColumnInitialWidth,
                                                                                              uiColumn.getWidth());
            sessionCommandManager.execute(abstractCanvasHandler, command);
        });
    }
}
