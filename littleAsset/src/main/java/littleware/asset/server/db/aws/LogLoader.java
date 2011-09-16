/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.aws;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.IdWithClock;
import littleware.base.UUIDFactory;
import littleware.base.validate.ValidationException;
import littleware.db.DbReader;

public class LogLoader implements DbReader<List<IdWithClock>, Long> {

    private static final Logger log = Logger.getLogger(LogLoader.class.getName());
    private final AmazonSimpleDB db;
    private final AwsConfig config;
    private final Provider<IdWithClock.Builder> clockFactory;
    private final UUID homeId;

    public LogLoader(AmazonSimpleDB db, AwsConfig config,
            Provider<IdWithClock.Builder> clockFactory, UUID homeId) {
        this.db = db;
        this.config = config;
        this.clockFactory = clockFactory;
        this.homeId = homeId;
    }

    @Override
    public List<IdWithClock> loadObject(Long minTimestamp) throws SQLException {
        final String query = "Select id, timestamp, fromId, homeId From " + config.getDbDomain()
                + " Where `timestamp` > '"
                + DbAssetSaver.encodeTimestamp(minTimestamp)
                + "' ORDER BY timestamp desc";
        log.log(Level.FINE, "Running query: {0}", query);
        try {
            final ImmutableList.Builder<IdWithClock> listBuilder = ImmutableList.builder();
            for (Item item : db.select(new SelectRequest(query).withConsistentRead(Boolean.TRUE)).getItems()) {
                UUID id = null;
                UUID parentId = null;
                UUID attrHomeId = null;
                long timestamp = -1L;
                for (Attribute attr : item.getAttributes()) {
                    if (attr.getName().equals("id")) {
                        id = UUIDFactory.parseUUID(attr.getValue());
                    } else if (attr.getName().equals("timestamp")) {
                        timestamp = Long.parseLong(attr.getValue());
                    } else if (attr.getName().equals("homeId")) {
                        attrHomeId = UUIDFactory.parseUUID(attr.getValue());
                    } else {
                        parentId = UUIDFactory.parseUUID(attr.getValue());
                    }
                }
                ValidationException.validate((id != null) && (timestamp >= 0) && (homeId != null), "Unexpected log data");
                if (attrHomeId.equals(this.homeId)) {
                    listBuilder.add(clockFactory.get().build(id, parentId, timestamp));
                }
            }
            return listBuilder.build().reverse();
        } catch (Exception ex) {
            throw new SQLException("Failed query", ex);
        }
    }

    public static class Builder {

        private UUID homeId = null;
        private final AmazonSimpleDB db;
        private final AwsConfig config;
        private final Provider<IdWithClock.Builder> clockFactory;

        @Inject
        public Builder(AmazonSimpleDB db, AwsConfig config, Provider<IdWithClock.Builder> clockFactory) {
            this.db = db;
            this.config = config;
            this.clockFactory = clockFactory;
        }

        public Builder homeId(UUID value) {
            homeId = value;
            return this;
        }

        public LogLoader build() {
            ValidationException.validate(homeId != null, "Must specify homeId");
            return new LogLoader(db, config, clockFactory, homeId);
        }
    }
}
