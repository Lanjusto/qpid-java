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
package org.apache.qpid.test.client;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.qpid.client.AMQSession_0_8;
import org.apache.qpid.client.message.AbstractJMSMessage;
import org.apache.qpid.test.utils.QpidBrokerTestCase;

public class FlowControlTest extends QpidBrokerTestCase
{
    private static final Logger _logger = LoggerFactory.getLogger(FlowControlTest.class);

    private Connection _clientConnection;
    private Session _clientSession;
    private Queue _queue;

    /**
     * Simply
     *
     * @throws Exception
     */
    public void testBasicBytesFlowControl() throws Exception
    {
        _queue = (Queue) getInitialContext().lookup("queue");

        //Create Client
        _clientConnection = getConnection();

        _clientConnection.start();

        _clientSession = _clientConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        //Ensure _queue is created
        _clientSession.createConsumer(_queue).close();

        Connection producerConnection = getConnection();

        producerConnection.start();

        Session producerSession = producerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = producerSession.createProducer(_queue);

        sendBytesMessage(producerSession, producer, 1, 128);
        sendBytesMessage(producerSession, producer, 2, 128);
        sendBytesMessage(producerSession, producer, 3, 256);

        producer.close();
        producerSession.close();
        producerConnection.close();

        Connection consumerConnection = getConnection();
        Session consumerSession = consumerConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        ((AMQSession_0_8) consumerSession).setPrefetchLimits(0, 256);
        MessageConsumer recv = consumerSession.createConsumer(_queue);
        consumerConnection.start();

        Message r1 = recv.receive(RECEIVE_TIMEOUT);
        assertNotNull("First message not received", r1);
        assertEquals("Messages in wrong order", 1, r1.getIntProperty("msg"));

        Message r2 = recv.receive(RECEIVE_TIMEOUT);
        assertNotNull("Second message not received", r2);
        assertEquals("Messages in wrong order", 2, r2.getIntProperty("msg"));

        Message r3 = recv.receive(RECEIVE_TIMEOUT);
        assertNull("Third message incorrectly delivered", r3);

        ((AbstractJMSMessage)r1).acknowledgeThis();

        r3 = recv.receive(RECEIVE_TIMEOUT);
        assertNull("Third message incorrectly delivered", r3);

        ((AbstractJMSMessage)r2).acknowledgeThis();

        r3 = recv.receive(RECEIVE_TIMEOUT);
        assertNotNull("Third message not received", r3);
        assertEquals("Messages in wrong order", 3, r3.getIntProperty("msg"));

        ((AbstractJMSMessage)r3).acknowledgeThis();
        consumerConnection.close();
    }

    public void testTwoConsumersBytesFlowControl() throws Exception
    {
        _queue = (Queue) getInitialContext().lookup("queue");

        //Create Client
        _clientConnection = getConnection();

        _clientConnection.start();

        _clientSession = _clientConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        //Ensure _queue is created
        _clientSession.createConsumer(_queue).close();

        Connection producerConnection = getConnection();

        producerConnection.start();

        Session producerSession = producerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = producerSession.createProducer(_queue);

        sendBytesMessage(producerSession, producer, 1, 128);
        sendBytesMessage(producerSession, producer, 2, 256);
        sendBytesMessage(producerSession, producer, 3, 128);

        producer.close();
        producerSession.close();
        producerConnection.close();

        Connection consumerConnection = getConnection();
        Session consumerSession1 = consumerConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        ((AMQSession_0_8) consumerSession1).setPrefetchLimits(0, 256);
        MessageConsumer recv1 = consumerSession1.createConsumer(_queue);

        consumerConnection.start();

        Message r1 = recv1.receive(RECEIVE_TIMEOUT);
        assertNotNull("First message not received", r1);
        assertEquals("Messages in wrong order", 1, r1.getIntProperty("msg"));

        Message r2 = recv1.receive(RECEIVE_TIMEOUT);
        assertNull("Second message incorrectly delivered", r2);

        Session consumerSession2 = consumerConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        ((AMQSession_0_8) consumerSession2).setPrefetchLimits(0, 256);
        MessageConsumer recv2 = consumerSession2.createConsumer(_queue);

        r2 = recv2.receive(RECEIVE_TIMEOUT);
        assertNotNull("Second message not received", r2);
        assertEquals("Messages in wrong order", 2, r2.getIntProperty("msg"));

        Message r3 = recv2.receive(RECEIVE_TIMEOUT);
        assertNull("Third message incorrectly delivered", r3);

        r3 = recv1.receive(RECEIVE_TIMEOUT);
        assertNotNull("Third message not received", r3);
        assertEquals("Messages in wrong order", 3, r3.getIntProperty("msg"));

        r2.acknowledge();
        r3.acknowledge();                                                                 
        recv1.close();
        recv2.close();
        consumerSession1.close();
        consumerSession2.close();
        consumerConnection.close();

    }

    public void testDeliverMessageLargerThanBytesLimit() throws Exception
    {
        _queue = (Queue) getInitialContext().lookup("queue");
        Connection connection = getConnection();
        connection.start();

        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        producerSession.createConsumer(_queue).close();
        MessageProducer producer = producerSession.createProducer(_queue);

        sendBytesMessage(producerSession, producer, 1, 128);
        sendBytesMessage(producerSession, producer, 2, 256);

        Session consumerSession1 = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        ((AMQSession_0_8) consumerSession1).setPrefetchLimits(0, 64);
        MessageConsumer recv1 = consumerSession1.createConsumer(_queue);

        Message r1 = recv1.receive(RECEIVE_TIMEOUT);
        assertNotNull("First message not received", r1);
        assertEquals("Messages in wrong order", 1, r1.getIntProperty("msg"));

        Message r2 = recv1.receive(RECEIVE_TIMEOUT);
        assertNull("Second message incorrectly delivered", r2);

        r1.acknowledge();

        r2 = recv1.receive(RECEIVE_TIMEOUT);
        assertNotNull("Second message not received", r2);
        assertEquals("Wrong messages received", 2, r2.getIntProperty("msg"));

        r2.acknowledge();
    }

    private void sendBytesMessage(final Session producerSession,
                                  final MessageProducer producer,
                                  final int messageId, final int messageSize) throws Exception
    {
        BytesMessage message = producerSession.createBytesMessage();
        message.writeBytes(new byte[messageSize]);
        message.setIntProperty("msg", messageId);
        producer.send(message);
    }
}

