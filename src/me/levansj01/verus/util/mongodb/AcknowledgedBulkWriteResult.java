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

package me.levansj01.verus.util.mongodb;

import static me.levansj01.verus.util.mongodb.assertions.Assertions.notNull;

import java.util.Collections;
import java.util.List;

class AcknowledgedBulkWriteResult extends BulkWriteResult {
    private int insertedCount;
    private int matchedCount;
    private int removedCount;
    private int modifiedCount;
    private final List<BulkWriteUpsert> upserts;

    AcknowledgedBulkWriteResult(final int insertedCount, final int matchedCount, final int removedCount,
                                final Integer modifiedCount, final List<BulkWriteUpsert> upserts) {
        this.insertedCount = insertedCount;
        this.matchedCount = matchedCount;
        this.removedCount = removedCount;
        this.modifiedCount = notNull("modifiedCount", modifiedCount);
        this.upserts = Collections.unmodifiableList(notNull("upserts", upserts));
    }

    @Override
    public boolean isAcknowledged() {
        return true;
    }

    @Override
    public int getInsertedCount() {
        return insertedCount;
    }

    @Override
    public int getMatchedCount() {
        return matchedCount;
    }

    @Override
    public int getRemovedCount() {
        return removedCount;
    }

    @Override
    public int getModifiedCount() {
        return modifiedCount;
    }

    @Override
    public List<BulkWriteUpsert> getUpserts() {
        return upserts;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AcknowledgedBulkWriteResult that = (AcknowledgedBulkWriteResult) o;

        if (insertedCount != that.insertedCount) {
            return false;
        }
        if (matchedCount != that.matchedCount) {
            return false;
        }
        if (removedCount != that.removedCount) {
            return false;
        }
        if (modifiedCount != that.modifiedCount) {
            return false;
        }
        if (!upserts.equals(that.upserts)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = insertedCount;
        result = 31 * result + matchedCount;
        result = 31 * result + removedCount;
        result = 31 * result + modifiedCount;
        result = 31 * result + upserts.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AcknowledgedBulkWriteResult{"
               + "insertedCount=" + insertedCount
               + ", matchedCount=" + matchedCount
               + ", removedCount=" + removedCount
               + ", modifiedCount=" + modifiedCount
               + ", upserts=" + upserts
               + '}';
    }
}