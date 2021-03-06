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

package me.levansj01.verus.util.bson.codecs;

import static java.lang.String.format;
import static me.levansj01.verus.util.bson.codecs.NumberCodecHelper.decodeInt;

import me.levansj01.verus.util.bson.BsonInvalidOperationException;
import me.levansj01.verus.util.bson.BsonReader;
import me.levansj01.verus.util.bson.BsonWriter;

/**
 * Encodes and decodes {@code Short} objects.
 *
 * @since 3.0
 */
public class ShortCodec implements Codec<Short> {

    @Override
    public void encode(final BsonWriter writer, final Short value, final EncoderContext encoderContext) {
        writer.writeInt32(value);
    }

    @Override
    public Short decode(final BsonReader reader, final DecoderContext decoderContext) {
        int value = decodeInt(reader);
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw new BsonInvalidOperationException(format("%s can not be converted into a Short.", value));
        }
        return (short) value;
    }

    @Override
    public Class<Short> getEncoderClass() {
        return Short.class;
    }
}
