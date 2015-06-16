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
package org.apache.qpid.test.unit.client.destinationurl;

import org.apache.qpid.test.utils.QpidTestCase;

import org.apache.qpid.url.AMQBindingURL;
import org.apache.qpid.url.BindingURL;

import java.net.URISyntaxException;

public class BindingURLTest extends QpidTestCase
{

    public void testFullURL() throws Exception
    {
        String url = "exchange.Class://exchangeName/Destination/Queue";

        BindingURL burl = new AMQBindingURL(url);

        assertEquals(url, burl.toString());
        assertEquals("exchange.Class", burl.getExchangeClass());
        assertEquals("exchangeName", burl.getExchangeName());
        assertEquals("Destination", burl.getDestinationName());
        assertEquals("Queue", burl.getQueueName());
        assertEquals(1, burl.getBindingKeys().length);
        assertEquals("Destination", burl.getRoutingKey());
    }

    public void testDestinationAbsent() throws Exception
    {
        String url = "exchangeClass://exchangeName//Queue";

        BindingURL burl = new AMQBindingURL(url);

        assertEquals(url, burl.toString());
        assertEquals("exchangeClass", burl.getExchangeClass());
        assertEquals("exchangeName", burl.getExchangeName());
        assertEquals("", burl.getDestinationName());
        assertEquals("Queue", burl.getQueueName());
    }

    public void testQueueAbsent() throws Exception
    {
        String url = "exchangeClass://exchangeName/Destination/";

        BindingURL burl = new AMQBindingURL(url);

        assertEquals(url, burl.toString());
        assertEquals("exchangeClass", burl.getExchangeClass());
        assertEquals("exchangeName", burl.getExchangeName());
        assertEquals("Destination", burl.getDestinationName());
        assertEquals("", burl.getQueueName());
    }

    public void testDestinationAndQueueAbsent() throws Exception
    {
        String url = "exchangeClass://exchangeName//";

        BindingURL burl = new AMQBindingURL(url);

        assertEquals(url, burl.toString());
        assertEquals("exchangeClass", burl.getExchangeClass());
        assertEquals("exchangeName", burl.getExchangeName());
        assertEquals("", burl.getDestinationName());
        assertEquals("", burl.getQueueName());
    }

    public void testQueueOnly() throws Exception
    {
        String url = "exchangeClass://exchangeName/Queue?option='x'";

        BindingURL burl = new AMQBindingURL(url);

        assertEquals("exchangeClass://exchangeName//Queue?option='x'", burl.toString());
        assertEquals("exchangeClass", burl.getExchangeClass());
        assertEquals("exchangeName", burl.getExchangeName());
        assertEquals("", burl.getDestinationName());
        assertEquals("Queue", burl.getQueueName());
    }


    public void testQueueWithOption() throws Exception
    {
        String url = "exchangeClass://exchangeName//Queue?option='value'";

        BindingURL burl = new AMQBindingURL(url);

        assertEquals(url, burl.toString());
        assertEquals("exchangeClass", burl.getExchangeClass());
        assertEquals("exchangeName", burl.getExchangeName());
        assertEquals("", burl.getDestinationName());
        assertEquals("Queue", burl.getQueueName());
        assertEquals("value", burl.getOption("option"));
    }

    public void testRoutingKey() throws Exception
    {
        String url = "exchangeClass://exchangeName//Queue?routingkey='Queue1'";

        BindingURL burl = new AMQBindingURL(url);

        assertEquals(url, burl.toString());
        assertEquals("exchangeClass", burl.getExchangeClass());
        assertEquals("exchangeName", burl.getExchangeName());
        assertEquals("Queue1", burl.getRoutingKey());
        assertEquals("Queue", burl.getQueueName());
    }

    public void testWithSingleOption() throws Exception
    {
        String url = "exchangeClass://exchangeName/Destination/?option='value'";

        BindingURL burl = new AMQBindingURL(url);

        assertEquals(url, burl.toString());

        assertEquals("exchangeClass", burl.getExchangeClass());
        assertEquals("exchangeName", burl.getExchangeName());
        assertEquals("Destination", burl.getDestinationName());
        assertEquals("", burl.getQueueName());
        assertEquals("value", burl.getOption("option"));
    }

    public void testWithMultipleOptions() throws Exception
    {
        String url = "exchangeClass://exchangeName/Destination/?option='value',option2='value2'";

        BindingURL burl = new AMQBindingURL(url);

        assertEquals("exchangeClass", burl.getExchangeClass());
        assertEquals("exchangeName", burl.getExchangeName());
        assertEquals("Destination", burl.getDestinationName());
        assertEquals("", burl.getQueueName());
        assertEquals("value", burl.getOption("option"));
        assertEquals("value2", burl.getOption("option2"));
    }

    public void testRoutingKeyDefaulting_NonDirectExchangeClass() throws Exception
    {

        String url = "exchangeClass://exchangeName/Destination/Queue";

        BindingURL dest = new AMQBindingURL(url);

        assertEquals("exchangeClass", dest.getExchangeClass());
        assertEquals("exchangeName", dest.getExchangeName());
        assertEquals("Destination", dest.getDestinationName());
        assertEquals("Queue", dest.getQueueName());
        assertEquals(dest.getDestinationName(), dest.getRoutingKey());

    }

    public void testRoutingKeyDefaulting_DirectExchangeClass() throws Exception
    {
        String url = "direct://exchangeName/Destination/Queue";

        BindingURL dest = new AMQBindingURL(url);

        assertEquals("direct", dest.getExchangeClass());
        assertEquals("exchangeName", dest.getExchangeName());
        assertEquals("Destination", dest.getDestinationName());
        assertEquals("Queue", dest.getQueueName());
        assertEquals(dest.getQueueName(), dest.getRoutingKey());

    }

    public void testSingleBindingKeys() throws Exception
    {
        String url = "exchangeClass://exchangeName/Destination/?bindingkey='key'";

        BindingURL dest = new AMQBindingURL(url);

        assertEquals("exchangeClass", dest.getExchangeClass());
        assertEquals("exchangeName", dest.getExchangeName());
        assertEquals("Destination", dest.getDestinationName());
        assertEquals("", dest.getQueueName());
        assertEquals(1, dest.getBindingKeys().length);
        assertEquals("key", dest.getBindingKeys()[0]);
    }

    public void testMultipleBindingKeys() throws Exception
    {
        String url = "exchangeClass://exchangeName/Destination/?bindingkey='key1',bindingkey='key2'";

        BindingURL dest = new AMQBindingURL(url);

        assertEquals("exchangeClass", dest.getExchangeClass());
        assertEquals("exchangeName", dest.getExchangeName());
        assertEquals("Destination", dest.getDestinationName());
        assertEquals("", dest.getQueueName());
        assertEquals(2, dest.getBindingKeys().length);
        assertEquals("key1", dest.getBindingKeys()[0]);
        assertEquals("key2", dest.getBindingKeys()[1]);
    }

    public void testAnonymousExchange() throws Exception
    {
        String url = "exchangeClass:////Queue";

        BindingURL burl = new AMQBindingURL(url);

        assertEquals(url, burl.toString());

        assertEquals("exchangeClass", burl.getExchangeClass());
        assertEquals("", burl.getExchangeName());
        assertEquals("", burl.getDestinationName());
        assertEquals("Queue", burl.getQueueName());
    }

    // You can only specify only a routing key or binding key, but not both.
    public void testRoutingKeyAndBindingKeyAreMutuallyExclusive() throws Exception
    {
        String url = "exchangeClass://exchangeName/Destination/?bindingkey='key1',routingkey='key2'";
        try
        {
            new AMQBindingURL(url);
            fail("Exception not thrown");
        }
        catch(URISyntaxException e)
        {
            // PASS
        }
    }

    public void testBareNameTreatedAsQueue() throws Exception
    {
        String url = "IBMPerfQueue1?durable='true'";
        BindingURL burl = new AMQBindingURL(url);

        assertEquals("direct:////IBMPerfQueue1?durable='true'", burl.toString());

        assertEquals("direct", burl.getExchangeClass());
        assertEquals("", burl.getExchangeName());
        assertEquals("", burl.getDestinationName());
        assertEquals("IBMPerfQueue1", burl.getQueueName());
        assertEquals("IBMPerfQueue1", burl.getRoutingKey());
        assertEquals("IBMPerfQueue1", burl.getBindingKeys()[0]);
    }

    public void testBareNameContainingSlashesTreatedAsQueueWithOddName() throws Exception
    {
        String url = "/a/b/c/d";
        BindingURL burl = new AMQBindingURL(url);

        assertEquals("direct://///a/b/c/d", burl.toString());

        assertEquals("direct", burl.getExchangeClass());
        assertEquals("", burl.getExchangeName());
        assertEquals("", burl.getDestinationName());
        assertEquals("/a/b/c/d", burl.getQueueName());
        assertEquals("/a/b/c/d", burl.getRoutingKey());
        assertEquals("/a/b/c/d", burl.getBindingKeys()[0]);
    }

}