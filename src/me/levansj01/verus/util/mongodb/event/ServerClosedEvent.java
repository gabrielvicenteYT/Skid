/*
 * Copyright 2008-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.levansj01.verus.util.mongodb.event;

import static me.levansj01.verus.util.mongodb.assertions.Assertions.notNull;

import me.levansj01.verus.util.mongodb.connection.ServerId;

/**
 * A server opening event.
 *
 * @since 3.3
 */
public final class ServerClosedEvent {
    private final ServerId serverId;

    /**
     * Construct an instance.
     *
     * @param serverId the non-null serverId
     */
    public ServerClosedEvent(final ServerId serverId) {
        this.serverId = notNull("serverId", serverId);
    }

    /**
     * Gets the serverId.
     *
     * @return the serverId
     */
    public ServerId getServerId() {
        return serverId;
    }

    @Override
    public String toString() {
        return "ServerClosedEvent{"
                       + "serverId=" + serverId
                       + '}';
    }
}