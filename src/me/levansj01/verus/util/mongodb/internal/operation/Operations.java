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

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.BsonJavaScript;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import me.levansj01.verus.util.mongodb.MongoNamespace;
import me.levansj01.verus.util.mongodb.ReadConcern;
import me.levansj01.verus.util.mongodb.ReadPreference;
import me.levansj01.verus.util.mongodb.WriteConcern;
import me.levansj01.verus.util.mongodb.client.model.BulkWriteOptions;
import me.levansj01.verus.util.mongodb.client.model.Collation;
import me.levansj01.verus.util.mongodb.client.model.CountOptions;
import me.levansj01.verus.util.mongodb.client.model.CreateIndexOptions;
import me.levansj01.verus.util.mongodb.client.model.DeleteManyModel;
import me.levansj01.verus.util.mongodb.client.model.DeleteOneModel;
import me.levansj01.verus.util.mongodb.client.model.DeleteOptions;
import me.levansj01.verus.util.mongodb.client.model.DropIndexOptions;
import me.levansj01.verus.util.mongodb.client.model.EstimatedDocumentCountOptions;
import me.levansj01.verus.util.mongodb.client.model.FindOneAndDeleteOptions;
import me.levansj01.verus.util.mongodb.client.model.FindOneAndReplaceOptions;
import me.levansj01.verus.util.mongodb.client.model.FindOneAndUpdateOptions;
import me.levansj01.verus.util.mongodb.client.model.IndexModel;
import me.levansj01.verus.util.mongodb.client.model.InsertManyOptions;
import me.levansj01.verus.util.mongodb.client.model.InsertOneModel;
import me.levansj01.verus.util.mongodb.client.model.InsertOneOptions;
import me.levansj01.verus.util.mongodb.client.model.MapReduceAction;
import me.levansj01.verus.util.mongodb.client.model.RenameCollectionOptions;
import me.levansj01.verus.util.mongodb.client.model.ReplaceOneModel;
import me.levansj01.verus.util.mongodb.client.model.ReplaceOptions;
import me.levansj01.verus.util.mongodb.client.model.ReturnDocument;
import me.levansj01.verus.util.mongodb.client.model.UpdateManyModel;
import me.levansj01.verus.util.mongodb.client.model.UpdateOneModel;
import me.levansj01.verus.util.mongodb.client.model.UpdateOptions;
import me.levansj01.verus.util.mongodb.client.model.WriteModel;
import me.levansj01.verus.util.mongodb.internal.bulk.DeleteRequest;
import me.levansj01.verus.util.mongodb.internal.bulk.IndexRequest;
import me.levansj01.verus.util.mongodb.internal.bulk.InsertRequest;
import me.levansj01.verus.util.mongodb.internal.bulk.UpdateRequest;
import me.levansj01.verus.util.mongodb.internal.bulk.WriteRequest;
import me.levansj01.verus.util.mongodb.internal.client.model.AggregationLevel;
import me.levansj01.verus.util.mongodb.internal.client.model.FindOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static me.levansj01.verus.util.mongodb.assertions.Assertions.notNull;

public final class Operations<TDocument> {
    private final MongoNamespace namespace;
    private final Class<TDocument> documentClass;
    private final ReadPreference readPreference;
    private final CodecRegistry codecRegistry;
    private final ReadConcern readConcern;
    private final WriteConcern writeConcern;
    private final boolean retryWrites;
    private boolean retryReads;

    public Operations(final MongoNamespace namespace, final Class<TDocument> documentClass, final ReadPreference readPreference,
            final CodecRegistry codecRegistry, final ReadConcern readConcern, final WriteConcern writeConcern, final boolean retryWrites,
            final boolean retryReads) {
        this.namespace = namespace;
        this.documentClass = documentClass;
        this.readPreference = readPreference;
        this.codecRegistry = codecRegistry;
        this.readConcern = readConcern;
        this.writeConcern = writeConcern;
        this.retryWrites = retryWrites;
        this.retryReads = retryReads;
    }

    public MongoNamespace getNamespace() {
        return namespace;
    }

    public Class<TDocument> getDocumentClass() {
        return documentClass;
    }

    public ReadPreference getReadPreference() {
        return readPreference;
    }

    public CodecRegistry getCodecRegistry() {
        return codecRegistry;
    }

    public ReadConcern getReadConcern() {
        return readConcern;
    }

    public WriteConcern getWriteConcern() {
        return writeConcern;
    }

    public boolean isRetryWrites() {
        return retryWrites;
    }

    public boolean isRetryReads() {
        return retryReads;
    }

    public CountDocumentsOperation countDocuments(final Bson filter, final CountOptions options) {
        CountDocumentsOperation operation = new CountDocumentsOperation(namespace)
                .retryReads(retryReads)
                .filter(toBsonDocument(filter))
                .skip(options.getSkip())
                .limit(options.getLimit())
                .maxTime(options.getMaxTime(MILLISECONDS), MILLISECONDS)
                .collation(options.getCollation());
        if (options.getHint() != null) {
            operation.hint(toBsonDocument(options.getHint()));
        } else if (options.getHintString() != null) {
            operation.hint(new BsonString(options.getHintString()));
        }
        return operation;
    }

    public EstimatedDocumentCountOperation estimatedDocumentCount(final EstimatedDocumentCountOptions options) {
        return new EstimatedDocumentCountOperation(namespace)
                .retryReads(retryReads)
                .maxTime(options.getMaxTime(MILLISECONDS), MILLISECONDS);
    }

    public <TResult> FindOperation<TResult> findFirst(final Bson filter, final Class<TResult> resultClass,
                                                      final FindOptions options) {
        return createFindOperation(namespace, filter, resultClass, options).batchSize(0).limit(-1);
    }

    public <TResult> FindOperation<TResult> find(final Bson filter, final Class<TResult> resultClass,
                                                 final FindOptions options) {
        return createFindOperation(namespace, filter, resultClass, options);
    }

    public <TResult> FindOperation<TResult> find(final MongoNamespace findNamespace, final Bson filter,
                                                 final Class<TResult> resultClass, final FindOptions options) {
        return createFindOperation(findNamespace, filter, resultClass, options);
    }

    private <TResult> FindOperation<TResult> createFindOperation(final MongoNamespace findNamespace, final Bson filter,
                                                                 final Class<TResult> resultClass, final FindOptions options) {
        FindOperation<TResult> operation = new FindOperation<>(findNamespace, codecRegistry.get(resultClass))
                .retryReads(retryReads)
                .filter(filter == null ? new BsonDocument() : filter.toBsonDocument(documentClass, codecRegistry))
                .batchSize(options.getBatchSize())
                .skip(options.getSkip())
                .limit(options.getLimit())
                .maxTime(options.getMaxTime(MILLISECONDS), MILLISECONDS)
                .maxAwaitTime(options.getMaxAwaitTime(MILLISECONDS), MILLISECONDS)
                .projection(toBsonDocumentOrNull(options.getProjection()))
                .sort(toBsonDocumentOrNull(options.getSort()))
                .cursorType(options.getCursorType())
                .noCursorTimeout(options.isNoCursorTimeout())
                .oplogReplay(options.isOplogReplay())
                .partial(options.isPartial())
                .slaveOk(readPreference.isSlaveOk())
                .collation(options.getCollation())
                .comment(options.getComment())
                .min(toBsonDocumentOrNull(options.getMin()))
                .max(toBsonDocumentOrNull(options.getMax()))
                .returnKey(options.isReturnKey())
                .showRecordId(options.isShowRecordId())
                .allowDiskUse(options.isAllowDiskUse());

        if (options.getHint() != null) {
            operation.hint(toBsonDocument(options.getHint()));
        } else if (options.getHintString() != null) {
            operation.hint(new BsonString(options.getHintString()));
        }
        return operation;
    }

    public <TResult> DistinctOperation<TResult> distinct(final String fieldName, final Bson filter,
                                                         final Class<TResult> resultClass, final long maxTimeMS,
                                                         final Collation collation) {
        return new DistinctOperation<TResult>(namespace, fieldName, codecRegistry.get(resultClass))
                .retryReads(retryReads)
                .filter(filter == null ? null : filter.toBsonDocument(documentClass, codecRegistry))
                .maxTime(maxTimeMS, MILLISECONDS)
                .collation(collation);

    }

    public <TResult> AggregateOperation<TResult> aggregate(final List<? extends Bson> pipeline, final Class<TResult> resultClass,
                                                    final long maxTimeMS, final long maxAwaitTimeMS, final Integer batchSize,
                                                    final Collation collation, final Bson hint, final String comment,
                                                    final Boolean allowDiskUse, final AggregationLevel aggregationLevel) {
        return new AggregateOperation<TResult>(namespace, toBsonDocumentList(pipeline), codecRegistry.get(resultClass), aggregationLevel)
                .retryReads(retryReads)
                .maxTime(maxTimeMS, MILLISECONDS)
                .maxAwaitTime(maxAwaitTimeMS, MILLISECONDS)
                .allowDiskUse(allowDiskUse)
                .batchSize(batchSize)
                .collation(collation)
                .hint(hint == null ? null : hint.toBsonDocument(documentClass, codecRegistry))
                .comment(comment);

    }

    public AggregateToCollectionOperation aggregateToCollection(final List<? extends Bson> pipeline, final long maxTimeMS,
                                                         final Boolean allowDiskUse, final Boolean bypassDocumentValidation,
                                                         final Collation collation, final Bson hint, final String comment,
                                                         final AggregationLevel aggregationLevel) {
        return new AggregateToCollectionOperation(namespace, toBsonDocumentList(pipeline), readConcern, writeConcern, aggregationLevel)
                .maxTime(maxTimeMS, MILLISECONDS)
                .allowDiskUse(allowDiskUse)
                .bypassDocumentValidation(bypassDocumentValidation)
                .collation(collation)
                .hint(hint == null ? null : hint.toBsonDocument(documentClass, codecRegistry))
                .comment(comment);
    }

    public MapReduceToCollectionOperation mapReduceToCollection(final String databaseName, final String collectionName,
                                                                final String mapFunction, final String reduceFunction,
                                                                final String finalizeFunction, final Bson filter, final int limit,
                                                                final long maxTimeMS, final boolean jsMode, final Bson scope,
                                                                final Bson sort, final boolean verbose, final MapReduceAction action,
                                                                final boolean nonAtomic, final boolean sharded,
                                                                final Boolean bypassDocumentValidation, final Collation collation) {
        MapReduceToCollectionOperation operation = new MapReduceToCollectionOperation(namespace, new BsonJavaScript(mapFunction),
                new BsonJavaScript(reduceFunction), collectionName, writeConcern)
                .filter(toBsonDocumentOrNull(filter))
                .limit(limit)
                .maxTime(maxTimeMS, MILLISECONDS)
                .jsMode(jsMode)
                .scope(toBsonDocumentOrNull(scope))
                .sort(toBsonDocumentOrNull(sort))
                .verbose(verbose)
                .action(action.getValue())
                .nonAtomic(nonAtomic)
                .sharded(sharded)
                .databaseName(databaseName)
                .bypassDocumentValidation(bypassDocumentValidation)
                .collation(collation);

        if (finalizeFunction != null) {
            operation.finalizeFunction(new BsonJavaScript(finalizeFunction));
        }
        return operation;
    }

    public <TResult> MapReduceWithInlineResultsOperation<TResult> mapReduce(final String mapFunction, final String reduceFunction,
                                                                            final String finalizeFunction, final Class<TResult> resultClass,
                                                                            final Bson filter, final int limit,
                                                                            final long maxTimeMS, final boolean jsMode, final Bson scope,
                                                                            final Bson sort, final boolean verbose,
                                                                            final Collation collation) {
        MapReduceWithInlineResultsOperation<TResult> operation =
                new MapReduceWithInlineResultsOperation<TResult>(namespace,
                        new BsonJavaScript(mapFunction),
                        new BsonJavaScript(reduceFunction),
                        codecRegistry.get(resultClass))
                        .filter(toBsonDocumentOrNull(filter))
                        .limit(limit)
                        .maxTime(maxTimeMS, MILLISECONDS)
                        .jsMode(jsMode)
                        .scope(toBsonDocumentOrNull(scope))
                        .sort(toBsonDocumentOrNull(sort))
                        .verbose(verbose)
                        .collation(collation);
        if (finalizeFunction != null) {
            operation.finalizeFunction(new BsonJavaScript(finalizeFunction));
        }
        return operation;
    }

    public FindAndDeleteOperation<TDocument> findOneAndDelete(final Bson filter, final FindOneAndDeleteOptions options) {
        return new FindAndDeleteOperation<TDocument>(namespace, writeConcern, retryWrites, getCodec())
                .filter(toBsonDocument(filter))
                .projection(toBsonDocument(options.getProjection()))
                .sort(toBsonDocument(options.getSort()))
                .maxTime(options.getMaxTime(MILLISECONDS), MILLISECONDS)
                .collation(options.getCollation())
                .hint(options.getHint())
                .hintString(options.getHintString());
    }

    public FindAndReplaceOperation<TDocument> findOneAndReplace(final Bson filter, final TDocument replacement,
                                                                final FindOneAndReplaceOptions options) {
        return new FindAndReplaceOperation<TDocument>(namespace, writeConcern, retryWrites, getCodec(),
                documentToBsonDocument(replacement))
                .filter(toBsonDocument(filter))
                .projection(toBsonDocument(options.getProjection()))
                .sort(toBsonDocument(options.getSort()))
                .returnOriginal(options.getReturnDocument() == ReturnDocument.BEFORE)
                .upsert(options.isUpsert())
                .maxTime(options.getMaxTime(MILLISECONDS), MILLISECONDS)
                .bypassDocumentValidation(options.getBypassDocumentValidation())
                .collation(options.getCollation())
                .hint(options.getHint())
                .hintString(options.getHintString());
    }

    public FindAndUpdateOperation<TDocument> findOneAndUpdate(final Bson filter, final Bson update, final FindOneAndUpdateOptions options) {
        return new FindAndUpdateOperation<TDocument>(namespace, writeConcern, retryWrites, getCodec(),
                toBsonDocument(update))
                .filter(toBsonDocument(filter))
                .projection(toBsonDocument(options.getProjection()))
                .sort(toBsonDocument(options.getSort()))
                .returnOriginal(options.getReturnDocument() == ReturnDocument.BEFORE)
                .upsert(options.isUpsert())
                .maxTime(options.getMaxTime(MILLISECONDS), MILLISECONDS)
                .bypassDocumentValidation(options.getBypassDocumentValidation())
                .collation(options.getCollation())
                .arrayFilters(toBsonDocumentList(options.getArrayFilters()))
                .hint(options.getHint())
                .hintString(options.getHintString());
    }

    public FindAndUpdateOperation<TDocument> findOneAndUpdate(final Bson filter, final List<? extends Bson> update,
                                                       final FindOneAndUpdateOptions options) {
        return new FindAndUpdateOperation<TDocument>(namespace, writeConcern, retryWrites, getCodec(),
                toBsonDocumentList(update))
                .filter(toBsonDocument(filter))
                .projection(toBsonDocument(options.getProjection()))
                .sort(toBsonDocument(options.getSort()))
                .returnOriginal(options.getReturnDocument() == ReturnDocument.BEFORE)
                .upsert(options.isUpsert())
                .maxTime(options.getMaxTime(MILLISECONDS), MILLISECONDS)
                .bypassDocumentValidation(options.getBypassDocumentValidation())
                .collation(options.getCollation())
                .arrayFilters(toBsonDocumentList(options.getArrayFilters()))
                .hint(options.getHint())
                .hintString(options.getHintString());
    }


    public MixedBulkWriteOperation insertOne(final TDocument document, final InsertOneOptions options) {
        return bulkWrite(singletonList(new InsertOneModel<TDocument>(document)),
                new BulkWriteOptions().bypassDocumentValidation(options.getBypassDocumentValidation()));
    }


    public MixedBulkWriteOperation replaceOne(final Bson filter, final TDocument replacement, final ReplaceOptions options) {
        return bulkWrite(singletonList(new ReplaceOneModel<TDocument>(filter, replacement, options)),
                new BulkWriteOptions().bypassDocumentValidation(options.getBypassDocumentValidation()));
    }

    public MixedBulkWriteOperation deleteOne(final Bson filter, final DeleteOptions options) {
        return bulkWrite(singletonList(new DeleteOneModel<TDocument>(filter, options)), new BulkWriteOptions());
    }

    public MixedBulkWriteOperation deleteMany(final Bson filter, final DeleteOptions options) {
        return bulkWrite(singletonList(new DeleteManyModel<TDocument>(filter, options)), new BulkWriteOptions());
    }

    public MixedBulkWriteOperation updateOne(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        return bulkWrite(singletonList(new UpdateOneModel<TDocument>(filter, update, updateOptions)),
                new BulkWriteOptions().bypassDocumentValidation(updateOptions.getBypassDocumentValidation()));
    }

    public MixedBulkWriteOperation updateOne(final Bson filter, final List<? extends Bson> update, final UpdateOptions updateOptions) {
        return bulkWrite(singletonList(new UpdateOneModel<TDocument>(filter, update, updateOptions)),
                new BulkWriteOptions().bypassDocumentValidation(updateOptions.getBypassDocumentValidation()));
    }

    public MixedBulkWriteOperation updateMany(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        return bulkWrite(singletonList(new UpdateManyModel<TDocument>(filter, update, updateOptions)),
                new BulkWriteOptions().bypassDocumentValidation(updateOptions.getBypassDocumentValidation()));
    }

    public MixedBulkWriteOperation updateMany(final Bson filter, final List<? extends Bson> update, final UpdateOptions updateOptions) {
        return bulkWrite(singletonList(new UpdateManyModel<TDocument>(filter, update, updateOptions)),
                new BulkWriteOptions().bypassDocumentValidation(updateOptions.getBypassDocumentValidation()));
    }

    public MixedBulkWriteOperation insertMany(final List<? extends TDocument> documents,
                                              final InsertManyOptions options) {
        notNull("documents", documents);
        List<InsertRequest> requests = new ArrayList<InsertRequest>(documents.size());
        for (TDocument document : documents) {
            if (document == null) {
                throw new IllegalArgumentException("documents can not contain a null value");
            }
            if (getCodec() instanceof CollectibleCodec) {
                document = ((CollectibleCodec<TDocument>) getCodec()).generateIdIfAbsentFromDocument(document);
            }
            requests.add(new InsertRequest(documentToBsonDocument(document)));
        }

        return new MixedBulkWriteOperation(namespace, requests, options.isOrdered(), writeConcern, retryWrites)
                .bypassDocumentValidation(options.getBypassDocumentValidation());
    }

    @SuppressWarnings("unchecked")
    public MixedBulkWriteOperation bulkWrite(final List<? extends WriteModel<? extends TDocument>> requests,
                                             final BulkWriteOptions options) {
        notNull("requests", requests);
        List<WriteRequest> writeRequests = new ArrayList<WriteRequest>(requests.size());
        for (WriteModel<? extends TDocument> writeModel : requests) {
            WriteRequest writeRequest;
            if (writeModel == null) {
                throw new IllegalArgumentException("requests can not contain a null value");
            } else if (writeModel instanceof InsertOneModel) {
                TDocument document = ((InsertOneModel<TDocument>) writeModel).getDocument();
                if (getCodec() instanceof CollectibleCodec) {
                    document = ((CollectibleCodec<TDocument>) getCodec()).generateIdIfAbsentFromDocument(document);
                }
                writeRequest = new InsertRequest(documentToBsonDocument(document));
            } else if (writeModel instanceof ReplaceOneModel) {
                ReplaceOneModel<TDocument> replaceOneModel = (ReplaceOneModel<TDocument>) writeModel;
                writeRequest = new UpdateRequest(toBsonDocument(replaceOneModel.getFilter()), documentToBsonDocument(replaceOneModel
                        .getReplacement()),
                        WriteRequest.Type.REPLACE)
                        .upsert(replaceOneModel.getReplaceOptions().isUpsert())
                        .collation(replaceOneModel.getReplaceOptions().getCollation())
                        .hint(replaceOneModel.getReplaceOptions().getHint())
                        .hintString(replaceOneModel.getReplaceOptions().getHintString());
            } else if (writeModel instanceof UpdateOneModel) {
                UpdateOneModel<TDocument> updateOneModel = (UpdateOneModel<TDocument>) writeModel;
                BsonValue update = updateOneModel.getUpdate() != null ? toBsonDocument(updateOneModel.getUpdate())
                        : new BsonArray(toBsonDocumentList(updateOneModel.getUpdatePipeline()));
                writeRequest = new UpdateRequest(toBsonDocument(updateOneModel.getFilter()), update, WriteRequest.Type.UPDATE)
                        .multi(false)
                        .upsert(updateOneModel.getOptions().isUpsert())
                        .collation(updateOneModel.getOptions().getCollation())
                        .arrayFilters(toBsonDocumentList(updateOneModel.getOptions().getArrayFilters()))
                        .hint(updateOneModel.getOptions().getHint())
                        .hintString(updateOneModel.getOptions().getHintString());
            } else if (writeModel instanceof UpdateManyModel) {
                UpdateManyModel<TDocument> updateManyModel = (UpdateManyModel<TDocument>) writeModel;
                BsonValue update = updateManyModel.getUpdate() != null ? toBsonDocument(updateManyModel.getUpdate())
                        : new BsonArray(toBsonDocumentList(updateManyModel.getUpdatePipeline()));
                writeRequest = new UpdateRequest(toBsonDocument(updateManyModel.getFilter()), update, WriteRequest.Type.UPDATE)
                        .multi(true)
                        .upsert(updateManyModel.getOptions().isUpsert())
                        .collation(updateManyModel.getOptions().getCollation())
                        .arrayFilters(toBsonDocumentList(updateManyModel.getOptions().getArrayFilters()))
                        .hint(updateManyModel.getOptions().getHint())
                        .hintString(updateManyModel.getOptions().getHintString());
            } else if (writeModel instanceof DeleteOneModel) {
                DeleteOneModel<TDocument> deleteOneModel = (DeleteOneModel<TDocument>) writeModel;
                writeRequest = new DeleteRequest(toBsonDocument(deleteOneModel.getFilter())).multi(false)
                        .collation(deleteOneModel.getOptions().getCollation())
                        .hint(deleteOneModel.getOptions().getHint())
                        .hintString(deleteOneModel.getOptions().getHintString());
            } else if (writeModel instanceof DeleteManyModel) {
                DeleteManyModel<TDocument> deleteManyModel = (DeleteManyModel<TDocument>) writeModel;
                writeRequest = new DeleteRequest(toBsonDocument(deleteManyModel.getFilter())).multi(true)
                        .collation(deleteManyModel.getOptions().getCollation())
                        .hint(deleteManyModel.getOptions().getHint())
                        .hintString(deleteManyModel.getOptions().getHintString());
            } else {
                throw new UnsupportedOperationException(format("WriteModel of type %s is not supported", writeModel.getClass()));
            }
            writeRequests.add(writeRequest);
        }

        return new MixedBulkWriteOperation(namespace, writeRequests, options.isOrdered(), writeConcern, retryWrites)
                .bypassDocumentValidation(options.getBypassDocumentValidation());
    }


    public DropCollectionOperation dropCollection() {
        return new DropCollectionOperation(namespace, writeConcern);
    }


    public RenameCollectionOperation renameCollection(final MongoNamespace newCollectionNamespace,
                                                      final RenameCollectionOptions renameCollectionOptions) {
        return new RenameCollectionOperation(namespace, newCollectionNamespace, writeConcern)
                .dropTarget(renameCollectionOptions.isDropTarget());
    }

    @SuppressWarnings("deprecation")
    public CreateIndexesOperation createIndexes(final List<IndexModel> indexes, final CreateIndexOptions createIndexOptions) {
        notNull("indexes", indexes);
        notNull("createIndexOptions", createIndexOptions);
        List<IndexRequest> indexRequests = new ArrayList<IndexRequest>(indexes.size());
        for (IndexModel model : indexes) {
            if (model == null) {
                throw new IllegalArgumentException("indexes can not contain a null value");
            }
            indexRequests.add(new IndexRequest(toBsonDocument(model.getKeys()))
                    .name(model.getOptions().getName())
                    .background(model.getOptions().isBackground())
                    .unique(model.getOptions().isUnique())
                    .sparse(model.getOptions().isSparse())
                    .expireAfter(model.getOptions().getExpireAfter(TimeUnit.SECONDS), TimeUnit.SECONDS)
                    .version(model.getOptions().getVersion())
                    .weights(toBsonDocument(model.getOptions().getWeights()))
                    .defaultLanguage(model.getOptions().getDefaultLanguage())
                    .languageOverride(model.getOptions().getLanguageOverride())
                    .textVersion(model.getOptions().getTextVersion())
                    .sphereVersion(model.getOptions().getSphereVersion())
                    .bits(model.getOptions().getBits())
                    .min(model.getOptions().getMin())
                    .max(model.getOptions().getMax())
                    .bucketSize(model.getOptions().getBucketSize())
                    .storageEngine(toBsonDocument(model.getOptions().getStorageEngine()))
                    .partialFilterExpression(toBsonDocument(model.getOptions().getPartialFilterExpression()))
                    .collation(model.getOptions().getCollation())
                    .wildcardProjection(toBsonDocument(model.getOptions().getWildcardProjection()))
                    .hidden(model.getOptions().isHidden())
            );
        }
        return new CreateIndexesOperation(namespace, indexRequests, writeConcern)
                .maxTime(createIndexOptions.getMaxTime(MILLISECONDS), MILLISECONDS)
                .commitQuorum(createIndexOptions.getCommitQuorum());
    }

    public DropIndexOperation dropIndex(final String indexName, final DropIndexOptions dropIndexOptions) {
        return new DropIndexOperation(namespace, indexName, writeConcern)
                .maxTime(dropIndexOptions.getMaxTime(MILLISECONDS), MILLISECONDS);
    }

    public DropIndexOperation dropIndex(final Bson keys, final DropIndexOptions dropIndexOptions) {
        return new DropIndexOperation(namespace, keys.toBsonDocument(BsonDocument.class, codecRegistry), writeConcern)
                .maxTime(dropIndexOptions.getMaxTime(MILLISECONDS), MILLISECONDS);
    }

    public <TResult> ListCollectionsOperation<TResult> listCollections(final String databaseName, final Class<TResult> resultClass,
                                                                final Bson filter, final boolean collectionNamesOnly,
                                                                final Integer batchSize, final long maxTimeMS) {
        return new ListCollectionsOperation<TResult>(databaseName, codecRegistry.get(resultClass))
                .retryReads(retryReads)
                .filter(toBsonDocumentOrNull(filter))
                .nameOnly(collectionNamesOnly)
                .batchSize(batchSize == null ? 0 : batchSize)
                .maxTime(maxTimeMS, MILLISECONDS);
    }

    public <TResult> ListDatabasesOperation<TResult> listDatabases(final Class<TResult> resultClass, final Bson filter,
                                                            final Boolean nameOnly, final long maxTimeMS,
                                                            final Boolean authorizedDatabasesOnly) {
        return new ListDatabasesOperation<TResult>(codecRegistry.get(resultClass)).maxTime(maxTimeMS, MILLISECONDS)
                .retryReads(retryReads)
                .filter(toBsonDocumentOrNull(filter))
                .nameOnly(nameOnly)
                .authorizedDatabasesOnly(authorizedDatabasesOnly);
    }

    public <TResult> ListIndexesOperation<TResult> listIndexes(final Class<TResult> resultClass, final Integer batchSize,
                                                               final long maxTimeMS) {
        return new ListIndexesOperation<TResult>(namespace, codecRegistry.get(resultClass))
                .retryReads(retryReads)
                .batchSize(batchSize == null ? 0 : batchSize)
                .maxTime(maxTimeMS, MILLISECONDS);
    }

    private Codec<TDocument> getCodec() {
        return codecRegistry.get(documentClass);
    }

    private BsonDocument documentToBsonDocument(final TDocument document) {
        return BsonDocumentWrapper.asBsonDocument(document, codecRegistry);
    }

    private BsonDocument toBsonDocument(final Bson bson) {
        return bson == null ? null : bson.toBsonDocument(documentClass, codecRegistry);
    }

    private List<BsonDocument> toBsonDocumentList(final List<? extends Bson> bsonList) {
        if (bsonList == null) {
            return null;
        }
        List<BsonDocument> bsonDocumentList = new ArrayList<BsonDocument>(bsonList.size());
        for (Bson cur : bsonList) {
            bsonDocumentList.add(toBsonDocument(cur));
        }
        return bsonDocumentList;
    }

    BsonDocument toBsonDocumentOrNull(final Bson document) {
        return document == null ? null : document.toBsonDocument(documentClass, codecRegistry);
    }

}