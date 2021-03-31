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

package me.levansj01.verus.util.mongodb.internal.operation;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import me.levansj01.verus.util.mongodb.MongoNamespace;
import me.levansj01.verus.util.mongodb.ReadConcern;
import me.levansj01.verus.util.mongodb.ReadPreference;
import me.levansj01.verus.util.mongodb.WriteConcern;
import me.levansj01.verus.util.mongodb.bulk.BulkWriteResult;
import me.levansj01.verus.util.mongodb.client.model.BulkWriteOptions;
import me.levansj01.verus.util.mongodb.client.model.Collation;
import me.levansj01.verus.util.mongodb.client.model.CountOptions;
import me.levansj01.verus.util.mongodb.client.model.CreateIndexOptions;
import me.levansj01.verus.util.mongodb.client.model.DeleteOptions;
import me.levansj01.verus.util.mongodb.client.model.DropIndexOptions;
import me.levansj01.verus.util.mongodb.client.model.EstimatedDocumentCountOptions;
import me.levansj01.verus.util.mongodb.client.model.FindOneAndDeleteOptions;
import me.levansj01.verus.util.mongodb.client.model.FindOneAndReplaceOptions;
import me.levansj01.verus.util.mongodb.client.model.FindOneAndUpdateOptions;
import me.levansj01.verus.util.mongodb.client.model.IndexModel;
import me.levansj01.verus.util.mongodb.client.model.InsertManyOptions;
import me.levansj01.verus.util.mongodb.client.model.InsertOneOptions;
import me.levansj01.verus.util.mongodb.client.model.MapReduceAction;
import me.levansj01.verus.util.mongodb.client.model.RenameCollectionOptions;
import me.levansj01.verus.util.mongodb.client.model.ReplaceOptions;
import me.levansj01.verus.util.mongodb.client.model.UpdateOptions;
import me.levansj01.verus.util.mongodb.client.model.WriteModel;
import me.levansj01.verus.util.mongodb.internal.client.model.AggregationLevel;
import me.levansj01.verus.util.mongodb.internal.client.model.FindOptions;

import java.util.List;

/**
 * This class is NOT part of the public API. It may change at any time without notification.
 */
public final class SyncOperations<TDocument> {
    private final Operations<TDocument> operations;

    public SyncOperations(final Class<TDocument> documentClass, final ReadPreference readPreference,
                          final CodecRegistry codecRegistry) {
        this(null, documentClass, readPreference, codecRegistry, ReadConcern.DEFAULT, WriteConcern.ACKNOWLEDGED, true, true);
    }

    public SyncOperations(final Class<TDocument> documentClass, final ReadPreference readPreference,
                          final CodecRegistry codecRegistry, final boolean retryReads) {
        this(null, documentClass, readPreference, codecRegistry, ReadConcern.DEFAULT, WriteConcern.ACKNOWLEDGED, true, retryReads);
    }

    public SyncOperations(final MongoNamespace namespace, final Class<TDocument> documentClass, final ReadPreference readPreference,
                          final CodecRegistry codecRegistry) {
        this(namespace, documentClass, readPreference, codecRegistry, ReadConcern.DEFAULT, WriteConcern.ACKNOWLEDGED, true, true);
    }

    public SyncOperations(final MongoNamespace namespace, final Class<TDocument> documentClass, final ReadPreference readPreference,
                          final CodecRegistry codecRegistry, final boolean retryReads) {
        this(namespace, documentClass, readPreference, codecRegistry, ReadConcern.DEFAULT, WriteConcern.ACKNOWLEDGED, true, retryReads);
    }

    public SyncOperations(final MongoNamespace namespace, final Class<TDocument> documentClass, final ReadPreference readPreference,
                          final CodecRegistry codecRegistry, final ReadConcern readConcern, final WriteConcern writeConcern,
                          final boolean retryWrites, final boolean retryReads) {
        this.operations = new Operations<TDocument>(namespace, documentClass, readPreference, codecRegistry, readConcern, writeConcern,
                retryWrites, retryReads);
    }

    public ReadOperation<Long> countDocuments(final Bson filter, final CountOptions options) {
        return operations.countDocuments(filter, options);
    }

    public ReadOperation<Long> estimatedDocumentCount(final EstimatedDocumentCountOptions options) {
        return operations.estimatedDocumentCount(options);
    }

    public <TResult> ReadOperation<BatchCursor<TResult>> findFirst(final Bson filter, final Class<TResult> resultClass,
                                                                   final FindOptions options) {
        return operations.findFirst(filter, resultClass, options);
    }

    public <TResult> ExplainableReadOperation<BatchCursor<TResult>> find(final Bson filter, final Class<TResult> resultClass,
                                                              final FindOptions options) {
        return operations.find(filter, resultClass, options);
    }

    public <TResult> ReadOperation<BatchCursor<TResult>> find(final MongoNamespace findNamespace, final Bson filter,
                                                              final Class<TResult> resultClass, final FindOptions options) {
        return operations.find(findNamespace, filter, resultClass, options);
    }

    public <TResult> ReadOperation<BatchCursor<TResult>> distinct(final String fieldName, final Bson filter,
                                                                  final Class<TResult> resultClass, final long maxTimeMS,
                                                                  final Collation collation) {
        return operations.distinct(fieldName, filter, resultClass, maxTimeMS, collation);
    }

    public <TResult> ExplainableReadOperation<BatchCursor<TResult>> aggregate(final List<? extends Bson> pipeline,
                                                                              final Class<TResult> resultClass,
                                                                              final long maxTimeMS, final long maxAwaitTimeMS,
                                                                              final Integer batchSize,
                                                                              final Collation collation, final Bson hint,
                                                                              final String comment,
                                                                              final Boolean allowDiskUse,
                                                                              final AggregationLevel aggregationLevel) {
        return operations.aggregate(pipeline, resultClass, maxTimeMS, maxAwaitTimeMS, batchSize, collation, hint, comment, allowDiskUse,
                aggregationLevel);
    }

    public WriteOperation<Void> aggregateToCollection(final List<? extends Bson> pipeline, final long maxTimeMS,
                                                      final Boolean allowDiskUse, final Boolean bypassDocumentValidation,
                                                      final Collation collation, final Bson hint, final String comment,
                                                      final AggregationLevel aggregationLevel) {
        return operations.aggregateToCollection(pipeline, maxTimeMS, allowDiskUse, bypassDocumentValidation, collation, hint, comment,
                aggregationLevel);
    }

    public WriteOperation<MapReduceStatistics> mapReduceToCollection(final String databaseName, final String collectionName,
                                                                     final String mapFunction, final String reduceFunction,
                                                                     final String finalizeFunction, final Bson filter, final int limit,
                                                                     final long maxTimeMS, final boolean jsMode, final Bson scope,
                                                                     final Bson sort, final boolean verbose, final MapReduceAction action,
                                                                     final boolean nonAtomic, final boolean sharded,
                                                                     final Boolean bypassDocumentValidation, final Collation collation) {
        return operations.mapReduceToCollection(databaseName, collectionName, mapFunction, reduceFunction, finalizeFunction, filter, limit,
                maxTimeMS, jsMode, scope, sort, verbose, action, nonAtomic, sharded, bypassDocumentValidation, collation);
    }

    public <TResult> ReadOperation<MapReduceBatchCursor<TResult>> mapReduce(final String mapFunction, final String reduceFunction,
                                                                            final String finalizeFunction, final Class<TResult> resultClass,
                                                                            final Bson filter, final int limit,
                                                                            final long maxTimeMS, final boolean jsMode, final Bson scope,
                                                                            final Bson sort, final boolean verbose,
                                                                            final Collation collation) {
        return operations.mapReduce(mapFunction, reduceFunction, finalizeFunction, resultClass, filter, limit, maxTimeMS, jsMode, scope,
                sort, verbose, collation);
    }

    public WriteOperation<TDocument> findOneAndDelete(final Bson filter, final FindOneAndDeleteOptions options) {
        return operations.findOneAndDelete(filter, options);
    }

    public WriteOperation<TDocument> findOneAndReplace(final Bson filter, final TDocument replacement,
                                                       final FindOneAndReplaceOptions options) {
        return operations.findOneAndReplace(filter, replacement, options);
    }

    public WriteOperation<TDocument> findOneAndUpdate(final Bson filter, final Bson update, final FindOneAndUpdateOptions options) {
        return operations.findOneAndUpdate(filter, update, options);
    }

    public WriteOperation<TDocument> findOneAndUpdate(final Bson filter, final List<? extends Bson> update,
                                                      final FindOneAndUpdateOptions options) {
        return operations.findOneAndUpdate(filter, update, options);
    }

    public WriteOperation<BulkWriteResult> insertOne(final TDocument document, final InsertOneOptions options) {
        return operations.insertOne(document, options);
    }


    public WriteOperation<BulkWriteResult> replaceOne(final Bson filter, final TDocument replacement, final ReplaceOptions options) {
        return operations.replaceOne(filter, replacement, options);
    }

    public WriteOperation<BulkWriteResult> deleteOne(final Bson filter, final DeleteOptions options) {
        return operations.deleteOne(filter, options);
    }

    public WriteOperation<BulkWriteResult> deleteMany(final Bson filter, final DeleteOptions options) {
        return operations.deleteMany(filter, options);
    }

    public WriteOperation<BulkWriteResult> updateOne(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        return operations.updateOne(filter, update, updateOptions);
    }

    public WriteOperation<BulkWriteResult> updateOne(final Bson filter, final List<? extends Bson> update,
                                                     final UpdateOptions updateOptions) {
        return operations.updateOne(filter, update, updateOptions);
    }

    public WriteOperation<BulkWriteResult> updateMany(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        return operations.updateMany(filter, update, updateOptions);
    }

    public WriteOperation<BulkWriteResult> updateMany(final Bson filter, final List<? extends Bson> update,
                                                      final UpdateOptions updateOptions) {
        return operations.updateMany(filter, update, updateOptions);
    }

    public WriteOperation<BulkWriteResult> insertMany(final List<? extends TDocument> documents,
                                                      final InsertManyOptions options) {
        return operations.insertMany(documents, options);
    }

    @SuppressWarnings("unchecked")
    public WriteOperation<BulkWriteResult> bulkWrite(final List<? extends WriteModel<? extends TDocument>> requests,
                                                     final BulkWriteOptions options) {
        return operations.bulkWrite(requests, options);
    }


    public WriteOperation<Void> dropCollection() {
        return operations.dropCollection();
    }

    public WriteOperation<Void> renameCollection(final MongoNamespace newCollectionNamespace,
                                                 final RenameCollectionOptions options) {
        return operations.renameCollection(newCollectionNamespace, options);
    }

    public WriteOperation<Void> createIndexes(final List<IndexModel> indexes, final CreateIndexOptions options) {
        return operations.createIndexes(indexes, options);
    }

    public WriteOperation<Void> dropIndex(final String indexName, final DropIndexOptions options) {
        return operations.dropIndex(indexName, options);
    }

    public WriteOperation<Void> dropIndex(final Bson keys, final DropIndexOptions options) {
        return operations.dropIndex(keys, options);
    }

    public <TResult> ReadOperation<BatchCursor<TResult>> listCollections(final String databaseName, final Class<TResult> resultClass,
                                                                         final Bson filter, final boolean collectionNamesOnly,
                                                                         final Integer batchSize, final long maxTimeMS) {
        return operations.listCollections(databaseName, resultClass, filter, collectionNamesOnly, batchSize, maxTimeMS);
    }

    public <TResult> ReadOperation<BatchCursor<TResult>> listDatabases(final Class<TResult> resultClass, final Bson filter,
                                                                       final Boolean nameOnly, final long maxTimeMS,
                                                                       final Boolean authorizedDatabases) {
        return operations.listDatabases(resultClass, filter, nameOnly, maxTimeMS, authorizedDatabases);
    }

    public <TResult> ReadOperation<BatchCursor<TResult>> listIndexes(final Class<TResult> resultClass, final Integer batchSize,
                                                                     final long maxTimeMS) {
        return operations.listIndexes(resultClass, batchSize, maxTimeMS);
    }
}