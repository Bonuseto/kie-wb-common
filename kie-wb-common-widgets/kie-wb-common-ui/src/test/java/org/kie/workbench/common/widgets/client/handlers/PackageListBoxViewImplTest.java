/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.workbench.common.widgets.client.handlers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.widgets.client.widget.KieSelectElement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PackageListBoxViewImplTest {

    @Mock
    KieSelectElement kieSelectElement;

    PackageListBoxViewImpl packageListBoxView = new PackageListBoxViewImpl(kieSelectElement);

    @Test
    public void whenEmptyValueShouldBeEqualsTheLabel() {

        KieSelectElement.Option option = packageListBoxView.newOption("key", "value");
        assertEquals(option.label, "key");
        assertEquals(option.value, "value");

        KieSelectElement.Option anotherOption = packageListBoxView.newOption("<default>", "");
        assertEquals(anotherOption.label, "<default>");
        assertEquals(anotherOption.value, "<default>");

    }
}