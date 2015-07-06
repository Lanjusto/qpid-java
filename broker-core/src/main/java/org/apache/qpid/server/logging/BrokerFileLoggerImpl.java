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

import java.security.AccessControlException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.rolling.RollingFileAppender;

import org.apache.qpid.server.logging.logback.RollingPolicyDecorator;
import org.apache.qpid.server.logging.logback.RolloverWatcher;
import org.apache.qpid.server.model.Broker;
import org.apache.qpid.server.model.ManagedAttributeField;
import org.apache.qpid.server.model.ManagedObjectFactoryConstructor;
import org.apache.qpid.server.model.Content;
import org.apache.qpid.server.model.Param;
import org.apache.qpid.server.util.DaemonThreadFactory;

public class BrokerFileLoggerImpl extends AbstractBrokerLogger<BrokerFileLoggerImpl> implements BrokerFileLogger<BrokerFileLoggerImpl>, FileLoggerSettings
{
    private RolloverWatcher _rolloverWatcher;
    private ScheduledExecutorService _rolledPolicyExecutor;

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
    protected void postResolveChildren()
    {
        _rolloverWatcher = new RolloverWatcher(getFileName());
        _rolledPolicyExecutor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("RolledFileScanner-" + getName()));

        super.postResolveChildren();
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
    public Collection<LogFileDetails> getLogFiles()
    {
        return _rolloverWatcher.getLogFileDetails();
    }

    @Override
    public Content getFile(final String fileName)
    {
        if (!getSecurityManager().authoriseLogsAccess(this))
        {
            throw new AccessControlException("Permission denied to access log content");
        }

        return _rolloverWatcher.getFileContent(fileName);
    }

    @Override
    public Content getFiles(@Param(name = "fileName") Set<String> fileName)
    {
        if (!getSecurityManager().authoriseLogsAccess(this))
        {
            throw new AccessControlException("Permission denied to access log content");
        }

        return _rolloverWatcher.getFilesAsZippedContent(fileName);
    }

    @Override
    public Content getAllFiles()
    {
        if (!getSecurityManager().authoriseLogsAccess(this))
        {
            throw new AccessControlException("Permission denied to access log content");
        }

        return _rolloverWatcher.getAllFilesAsZippedContent();
    }

    @Override
    public void stopLogging()
    {
        super.stopLogging();
        _rolledPolicyExecutor.shutdown();
    }

    @Override
    public RollingPolicyDecorator.RolloverListener getRolloverListener()
    {
        return _rolloverWatcher;
    }

    @Override
    public ScheduledExecutorService getExecutorService()
    {
        return _rolledPolicyExecutor;
    }

    @Override
    protected Appender<ILoggingEvent> createAppenderInstance(Context loggerContext)
    {
        final RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<ILoggingEvent>()
                                                            {
                                                                @Override
                                                                protected void append(final ILoggingEvent eventObject)
                                                                {
                                                                    super.append(eventObject);
                                                                    switch(eventObject.getLevel().toInt())
                                                                    {
                                                                        case Level.ERROR_INT:
                                                                            incrementErrorCount();
                                                                            break;
                                                                        case Level.WARN_INT:
                                                                            incrementWarnCount();
                                                                    }
                                                                }
                                                            };
        AppenderUtils.configureRollingFileAppender(this, loggerContext, appender);
        return appender;
    }

}
