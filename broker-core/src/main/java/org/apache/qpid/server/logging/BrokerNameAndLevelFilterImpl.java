/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.server.logging;

import java.util.Map;
import java.util.Set;

import org.apache.qpid.server.configuration.IllegalConfigurationException;
import org.apache.qpid.server.model.BrokerLogger;
import org.apache.qpid.server.model.ConfiguredObject;
import org.apache.qpid.server.model.ManagedObjectFactoryConstructor;

public class BrokerNameAndLevelFilterImpl extends AbstractNameAndLevelFilter<BrokerNameAndLevelFilterImpl>
        implements BrokerNameAndLevelFilter<BrokerNameAndLevelFilterImpl>
{

    @ManagedObjectFactoryConstructor
    protected BrokerNameAndLevelFilterImpl(final Map<String, Object> attributes, BrokerLogger<?> logger)
    {
        super(parentsMap(logger), attributes);
    }

    @Override
    protected void validateChange(ConfiguredObject<?> proxyForValidation, Set<String> changedAttributes)
    {
        super.validateChange(proxyForValidation, changedAttributes);
        BrokerNameAndLevelFilter proxyFilter = (BrokerNameAndLevelFilter)proxyForValidation;
        if (changedAttributes.contains(LOGGER_NAME) &&
            ((getLoggerName() != null && !getLoggerName().equals(proxyFilter.getLoggerName())) ||
             (getLoggerName() == null && proxyFilter.getLoggerName() != null)))
        {
            throw new IllegalConfigurationException("Attribute '" + LOGGER_NAME + " cannot be changed");
        }
    }


}