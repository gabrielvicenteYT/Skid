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

package me.levansj01.verus.util.mongodb.internal.connection;

import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import me.levansj01.verus.util.mongodb.UnixServerAddress;
import me.levansj01.verus.util.mongodb.connection.BufferProvider;
import me.levansj01.verus.util.mongodb.connection.SocketSettings;
import me.levansj01.verus.util.mongodb.connection.SslSettings;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.Socket;

public class UnixSocketChannelStream extends SocketStream {
    private final UnixServerAddress address;

    public UnixSocketChannelStream(final UnixServerAddress address, final SocketSettings settings, final SslSettings sslSettings,
                            final BufferProvider bufferProvider) {
        super(address, settings, sslSettings, SocketFactory.getDefault(), bufferProvider);
        this.address = address;
    }

    @Override
    protected Socket initializeSocket() throws IOException {
        return UnixSocketChannel.open((UnixSocketAddress) address.getUnixSocketAddress()).socket();
    }
}