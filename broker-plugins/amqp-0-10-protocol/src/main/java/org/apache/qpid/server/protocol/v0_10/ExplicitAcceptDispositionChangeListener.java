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
package org.apache.qpid.server.protocol.v0_10;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.qpid.server.message.MessageInstance;
import org.apache.qpid.server.message.MessageInstanceConsumer;


class ExplicitAcceptDispositionChangeListener implements ServerSession.MessageDispositionChangeListener
{
    private static final Logger _logger = LoggerFactory.getLogger(ExplicitAcceptDispositionChangeListener.class);


    private final MessageInstance _entry;
    private final ConsumerTarget_0_10 _target;
    private final MessageInstanceConsumer _consumer;

    public ExplicitAcceptDispositionChangeListener(MessageInstance entry,
                                                   ConsumerTarget_0_10 target,
                                                   final MessageInstanceConsumer consumer)
    {
        _entry = entry;
        _target = target;
        _consumer = consumer;
    }

    public void onAccept()
    {
        _target.getSessionModel().acknowledge(_consumer, _target, _entry);
    }

    public void onRelease(boolean setRedelivered)
    {
        _target.release(_consumer, _entry, setRedelivered);
    }

    public void onReject()
    {
        _target.reject(_consumer, _entry);
    }

    public boolean acquire()
    {
        return _entry.acquire(_consumer);
    }


}
