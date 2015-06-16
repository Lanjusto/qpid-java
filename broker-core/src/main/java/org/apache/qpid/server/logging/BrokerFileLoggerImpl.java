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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;

import org.apache.qpid.server.model.Broker;
import org.apache.qpid.server.model.ManagedAttributeField;
import org.apache.qpid.server.model.ManagedObjectFactoryConstructor;

public class BrokerFileLoggerImpl extends AbstractBrokerLogger<BrokerFileLoggerImpl> implements BrokerFileLogger<BrokerFileLoggerImpl>, FileLoggerSettings
{
    @ManagedAttributeField
    private String _layout;
    @ManagedAttributeField
    private String _fileName;
    @ManagedAttributeField
    private boolean _rollDaily;
    @ManagedAttributeField
    private boolean _rollOnRestart;
    @ManagedAttributeField
    private boolean _compressOldFiles;
    @ManagedAttributeField
    private int _maxHistory;
    @ManagedAttributeField
    private String _maxFileSize;

    @ManagedObjectFactoryConstructor
    protected BrokerFileLoggerImpl(final Map<String, Object> attributes, Broker<?> broker)
    {
        super(attributes, broker);
    }

    @Override
    public String getFileName()
    {
        return _fileName;
    }

    @Override
    public boolean isRollDaily()
    {
        return _rollDaily;
    }

    @Override
    public boolean isRollOnRestart()
    {
        return _rollOnRestart;
    }

    @Override
    public boolean isCompressOldFiles()
    {
        return _compressOldFiles;
    }

    @Override
    public int getMaxHistory()
    {
        return _maxHistory;
    }

    @Override
    public String getMaxFileSize()
    {
        return _maxFileSize;
    }

    @Override
    public String getLayout()
    {
        return _layout;
    }

    @Override
    public Appender<ILoggingEvent> asAppender(Context loggerContext)
    {
        return new RollingFileAppenderFactory().createRollingFileAppender(this, loggerContext);
    }

}