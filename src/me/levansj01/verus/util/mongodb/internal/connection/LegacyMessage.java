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

import org.bson.io.BsonOutput;

import me.levansj01.verus.util.mongodb.internal.session.SessionContext;

abstract class LegacyMessage extends RequestMessage {
    LegacyMessage(final String collectionName, final OpCode opCode, final MessageSettings settings) {
        super(collectionName, opCode, settings);
    }

    LegacyMessage(final OpCode opCode, final MessageSettings settings) {
        super(opCode, settings);
    }

    abstract EncodingMetadata encodeMessageBodyWithMetadata(BsonOutput bsonOutput);

    protected EncodingMetadata encodeMessageBodyWithMetadata(final BsonOutput bsonOutput, final SessionContext sessionContext) {
        return encodeMessageBodyWithMetadata(bsonOutput);
    }
}