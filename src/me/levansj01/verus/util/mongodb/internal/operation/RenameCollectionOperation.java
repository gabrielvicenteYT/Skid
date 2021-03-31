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

import static me.levansj01.verus.util.mongodb.assertions.Assertions.notNull;
import static me.levansj01.verus.util.mongodb.internal.async.ErrorHandlingResultCallback.errorHandlingCallback;
import static me.levansj01.verus.util.mongodb.internal.operation.CommandOperationHelper.executeCommand;
import static me.levansj01.verus.util.mongodb.internal.operation.CommandOperationHelper.executeCommandAsync;
import static me.levansj01.verus.util.mongodb.internal.operation.CommandOperationHelper.writeConcernErrorTransformer;
import static me.levansj01.verus.util.mongodb.internal.operation.CommandOperationHelper.writeConcernErrorWriteTransformer;
import static me.levansj01.verus.util.mongodb.internal.operation.OperationHelper.AsyncCallableWithConnection;
import static me.levansj01.verus.util.mongodb.internal.operation.OperationHelper.CallableWithConnection;
import static me.levansj01.verus.util.mongodb.internal.operation.OperationHelper.LOGGER;
import static me.levansj01.verus.util.mongodb.internal.operation.OperationHelper.releasingCallback;
import static me.levansj01.verus.util.mongodb.internal.operation.OperationHelper.withAsyncConnection;
import static me.levansj01.verus.util.mongodb.internal.operation.OperationHelper.withConnection;
import static me.levansj01.verus.util.mongodb.internal.operation.WriteConcernHelper.appendWriteConcernToCommand;

import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonString;

import me.levansj01.verus.util.mongodb.MongoNamespace;
import me.levansj01.verus.util.mongodb.WriteConcern;
import me.levansj01.verus.util.mongodb.connection.ConnectionDescription;
import me.levansj01.verus.util.mongodb.internal.async.SingleResultCallback;
import me.levansj01.verus.util.mongodb.internal.binding.AsyncWriteBinding;
import me.levansj01.verus.util.mongodb.internal.binding.WriteBinding;
import me.levansj01.verus.util.mongodb.internal.connection.AsyncConnection;
import me.levansj01.verus.util.mongodb.internal.connection.Connection;

/**
 * An operation that renames the given collection to the new name.
 *
 * <p>If the new name is the same as an existing collection and dropTarget is true, this existing collection will be dropped. If
 * dropTarget is false and the newCollectionName is the same as an existing collection, a MongoServerException will be thrown.</p>
 *
 * @mongodb.driver.manual reference/command/renameCollection renameCollection
 * @since 3.0
 */
public class RenameCollectionOperation implements AsyncWriteOperation<Void>, WriteOperation<Void> {
    private final MongoNamespace originalNamespace;
    private final MongoNamespace newNamespace;
    private final WriteConcern writeConcern;
    private boolean dropTarget;

    /**
     * @param originalNamespace the name of the collection to rename
     * @param newNamespace      the desired new name for the collection
     */
    public RenameCollectionOperation(final MongoNamespace originalNamespace, final MongoNamespace newNamespace) {
        this(originalNamespace, newNamespace, null);
    }

    /**
     * @param originalNamespace the name of the collection to rename
     * @param newNamespace      the desired new name for the collection
     * @param writeConcern      the writeConcern
     *
     * @since 3.4
     */
    public RenameCollectionOperation(final MongoNamespace originalNamespace, final MongoNamespace newNamespace,
                                     final WriteConcern writeConcern) {
        this.originalNamespace = notNull("originalNamespace", originalNamespace);
        this.newNamespace = notNull("newNamespace", newNamespace);
        this.writeConcern = writeConcern;
    }

    /**
     * Gets the write concern.
     *
     * @return the write concern, which may be null
     *
     * @since 3.4
     */
    public WriteConcern getWriteConcern() {
        return writeConcern;
    }

    /**
     * Gets if mongod should drop the target of renameCollection prior to renaming the collection.
     *
     * @return true if mongod should drop the target of renameCollection prior to renaming the collection.
     */
    public boolean isDropTarget() {
        return dropTarget;
    }

    /**
     * Sets if mongod should drop the target of renameCollection prior to renaming the collection.
     *
     * @param dropTarget true if mongod should drop the target of renameCollection prior to renaming the collection.
     * @return this
     */
    public RenameCollectionOperation dropTarget(final boolean dropTarget) {
        this.dropTarget = dropTarget;
        return this;
    }

    /**
     * Rename the collection with {@code oldCollectionName} in database {@code databaseName} to the {@code newCollectionName}.
     *
     * @param binding the binding
     * @return a void result
     * @throws me.levansj01.verus.util.mongodb.MongoServerException if you provide a newCollectionName that is the name of an existing collection and dropTarget
     *                                          is false, or if the oldCollectionName is the name of a collection that doesn't exist
     */
    @Override
    public Void execute(final WriteBinding binding) {
        return withConnection(binding, new CallableWithConnection<Void>() {
            @Override
            public Void call(final Connection connection) {
                return executeCommand(binding, "admin", getCommand(connection.getDescription()), connection,
                        writeConcernErrorTransformer());
            }
        });
    }

    @Override
    public void executeAsync(final AsyncWriteBinding binding, final SingleResultCallback<Void> callback) {
        withAsyncConnection(binding, new AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                SingleResultCallback<Void> errHandlingCallback = errorHandlingCallback(callback, LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                } else {
                    executeCommandAsync(binding, "admin", getCommand(connection.getDescription()), connection,
                            writeConcernErrorWriteTransformer(), releasingCallback(errHandlingCallback, connection));
                }
            }
        });
    }

    private BsonDocument getCommand(final ConnectionDescription description) {
        BsonDocument commandDocument = new BsonDocument("renameCollection", new BsonString(originalNamespace.getFullName()))
                                            .append("to", new BsonString(newNamespace.getFullName()))
                                            .append("dropTarget", BsonBoolean.valueOf(dropTarget));
        appendWriteConcernToCommand(writeConcern, commandDocument, description);
        return commandDocument;
    }
}