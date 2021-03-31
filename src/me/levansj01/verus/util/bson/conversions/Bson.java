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

package me.levansj01.verus.util.bson.conversions;

import static java.util.Arrays.asList;
import static me.levansj01.verus.util.bson.codecs.configuration.CodecRegistries.fromProviders;

import me.levansj01.verus.util.bson.BsonDocument;
import me.levansj01.verus.util.bson.codecs.BsonCodecProvider;
import me.levansj01.verus.util.bson.codecs.BsonValueCodecProvider;
import me.levansj01.verus.util.bson.codecs.DocumentCodecProvider;
import me.levansj01.verus.util.bson.codecs.IterableCodecProvider;
import me.levansj01.verus.util.bson.codecs.JsonObjectCodecProvider;
import me.levansj01.verus.util.bson.codecs.MapCodecProvider;
import me.levansj01.verus.util.bson.codecs.ValueCodecProvider;
import me.levansj01.verus.util.bson.codecs.configuration.CodecRegistry;
import me.levansj01.verus.util.bson.codecs.jsr310.Jsr310CodecProvider;

/**
 * An interface for types that are able to render themselves into a {@code BsonDocument}.
 *
 * @since 3.0
 */
public interface Bson {
    /**
     * This registry includes the following providers:
     * <ul>
     *     <li>{@link ValueCodecProvider}</li>
     *     <li>{@link BsonValueCodecProvider}</li>
     *     <li>{@link DocumentCodecProvider}</li>
     *     <li>{@link IterableCodecProvider}</li>
     *     <li>{@link MapCodecProvider}</li>
     *     <li>{@link Jsr310CodecProvider}</li>
     *     <li>{@link JsonObjectCodecProvider}</li>
     *     <li>{@link BsonCodecProvider}</li>
     * </ul>
     * <p>
     * Additional providers may be added in a future release.
     * </p>
     *
     * @since 4.2
     */
    CodecRegistry DEFAULT_CODEC_REGISTRY =
            fromProviders(asList(
                    new ValueCodecProvider(),
                    new BsonValueCodecProvider(),
                    new DocumentCodecProvider(),
                    new IterableCodecProvider(),
                    new MapCodecProvider(),
                    new Jsr310CodecProvider(),
                    new JsonObjectCodecProvider(),
                    new BsonCodecProvider()));

    /**
     * Render into a BsonDocument.
     *
     * @param documentClass the document class in scope for the collection.  This parameter may be ignored, but it may be used to alter
     *                      the structure of the returned {@code BsonDocument} based on some knowledge of the document class.
     * @param codecRegistry the codec registry.  This parameter may be ignored, but it may be used to look up {@code Codec} instances for
     *                      the document class or any other related class.
     * @param <TDocument> the type of the document class
     * @return the BsonDocument
     */
    <TDocument> BsonDocument toBsonDocument(Class<TDocument> documentClass, CodecRegistry codecRegistry);

    /**
     * Render into a BsonDocument using a document class and codec registry appropriate for the implementation.
     * <p>
     * The default implementation of this method calls {@link #toBsonDocument(Class, CodecRegistry)} with the
     * {@link BsonDocument} class as the first argument and {@link #DEFAULT_CODEC_REGISTRY} as the second argument.
     * </p>
     *
     * @return the BsonDocument
     * @since 4.2
     */
    default BsonDocument toBsonDocument() {
        return toBsonDocument(BsonDocument.class, DEFAULT_CODEC_REGISTRY);
    }
}