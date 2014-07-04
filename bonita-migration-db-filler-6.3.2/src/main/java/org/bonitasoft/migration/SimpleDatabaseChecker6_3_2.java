/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandSearchDescriptor;
import org.bonitasoft.engine.command.DefaultCommandProvider;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.junit.Test;


/**
 * Check that the migrated database is ok
 * @author Elias Ricken de Medeiros
 *
 */
public class SimpleDatabaseChecker6_3_2 extends SimpleDatabaseChecker6_3_1 {

    @Test
    public void commands_are_up_to_date() throws Exception {
        //given
        final TenantServiceAccessor serviceAccessor = ServiceAccessorFactory.getInstance().createTenantServiceAccessor(session.getTenantId());
        final DefaultCommandProvider commandProvider = serviceAccessor.getDefaultCommandProvider();

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1000);
        builder.filter(CommandSearchDescriptor.SYSTEM, true);
        builder.sort(CommandSearchDescriptor.ID, Order.ASC);

        //when
        final List<SimpleCommandDescriptor> configCommands = toSimpleCommandDescriptors(commandProvider.getDefaultCommands());
        final List<SimpleCommandDescriptor> dbCommands = toSimpleCommandDescriptors(commandApi.searchCommands(builder.done()).getResult());

        //then
        assertThat(dbCommands).containsAll(configCommands);
        assertThat(configCommands).containsAll(dbCommands);
    }

    private List<SimpleCommandDescriptor> toSimpleCommandDescriptors(final List<CommandDescriptor> commandDescriptors) {
        final List<SimpleCommandDescriptor> simpleConfigCommands = new ArrayList<SimpleCommandDescriptor>(commandDescriptors.size());
        for (final CommandDescriptor configCommand : commandDescriptors) {
            simpleConfigCommands.add(new SimpleCommandDescriptor(configCommand.getName(), configCommand.getImplementation(), configCommand.getDescription()));
        }
        return simpleConfigCommands;
    }

}
