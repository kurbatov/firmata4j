/* 
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Oleg Kurbatov (o.v.kurbatov@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.firmata4j.fsm;

import java.util.HashMap;
import java.util.Map;

/**
 * The event of very loose structure. It provides possibility to build an event
 * of structure that meets the needs of a particular FSM application.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class Event {

    private final long timestamp;
    private final String name;
    private final String type;
    private final Map<String, Object> body;

    /**
     * Constructs the event of unspecified type and without a name.
     */
    public Event() {
        timestamp = System.currentTimeMillis();
        name = "unspecified";
        type = "unspecified";
        body = new HashMap<>();
    }

    /**
     * Constructs the event of specified type with specified name.
     *
     * @param name the name of the event
     * @param type the type of the event
     */
    public Event(String name, String type) {
        timestamp = System.currentTimeMillis();
        this.name = name;
        this.type = type;
        body = new HashMap<>();
    }
    
    /**
     * Constructs the event of specified type with specified name.
     *
     * @param name the name of the event
     * @param type the type of the event
     */
    public Event(String name, String type, long timestamp) {
        this.timestamp = timestamp;
        this.name = name;
        this.type = type;
        body = new HashMap<>();
    }

    /**
     * Constructs the event of specified type with specified name. This
     * constructor allows to set the body of event at once.
     *
     * @param name the name of the event
     * @param type the type of the event
     * @param body the event's body
     */
    public Event(String name, String type, Map<String, Object> body) {
        timestamp = System.currentTimeMillis();
        this.name = name;
        this.type = type;
        this.body = new HashMap<>(body);
    }

    /**
     * Returns the name of the event.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the timestamp of the event.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the type of the event.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the body of the event.
     */
    public Map<String, Object> getBody() {
        return new HashMap<String, Object>(body);
    }

    /**
     * Returns the item of the event's body.
     * @param key the key of event item
     * @return the event item
     */
    public Object getBodyItem(String key) {
        return body.get(key);
    }

    /**
     * Sets the item of the event's body.
     * @param key the key of event item
     * @param value the event item
     */
    public void setBodyItem(String key, Object value) {
        body.put(key, value);
    }
}
