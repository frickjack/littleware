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
import com.amazonaws.services.simpledb.model.SelectResult;
import com.google.inject.Inject;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.base.AssertionFailedException;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;
import littleware.base.validate.ValidationException;
import littleware.db.DbReader;

/**
 * Load a specific child of a given parent
 */
public class DbByParentLoader implements DbReader<Option<Asset>, String> {

  private static final Logger log = Logger.getLogger(DbByNameLoader.class.getName());
  private final AmazonSimpleDB db;
  private final AwsConfig config;
  private final DbAssetLoader loader;
  private final String query;

  public DbByParentLoader(AmazonSimpleDB db, AwsConfig config, DbAssetLoader loader,
                          String query
  ) {
    this.db = db;
    this.config = config;
    this.loader = loader;
    this.query = query;
  }

  @Override
  public Option<Asset> loadObject(String ignore) throws SQLException {
    try {
      // We shouldn't have to worry about multiple pages of results ...
      // Try inconsistent read first ...
      for( int attempt = 0; attempt < 1; ++attempt ) {
        log.log(Level.FINE, "Running query: {0}, attempt {1}", new Object[]{query, attempt});
        final SelectResult result = db.select(new SelectRequest(query).withConsistentRead( attempt > 0 ));
        Whatever.get().check("Query overflow ...", null == result.getNextToken());
        for (Item item : result.getItems()) {
          UUID id = null;

          for (Attribute attr : item.getAttributes()) {
            if (attr.getName().equals("id")) {
              id = UUIDFactory.parseUUID(attr.getValue());
              break;
            } 
          }
          if ( null == id ) {
            throw new AssertionFailedException("Unexpected query result");
          }
          final Asset asset = loader.loadObject(id);
          if ( null != asset ) {
            return Maybe.something( asset );
          }
        }
      }
      return Maybe.empty();
    } catch (Exception ex) {
      throw new SQLException("Failed query", ex);
    }
  }

  public static class Builder {
    private UUID parentId = null;
    private String name = null;
    private final AmazonSimpleDB db;
    private final AwsConfig config;
    private final DbAssetLoader loader;

    @Inject
    public Builder(AmazonSimpleDB db, AwsConfig config, DbAssetLoader loader ) {
      this.db = db;
      this.config = config;
      this.loader = loader;
    }

    public Builder name(String value) {
      name = value;
      return this;
    }

    public Builder parentId(UUID value) {
      parentId = value;
      return this;
    }



    public DbByParentLoader build() {
      ValidationException.validate(name != null, "Must specify name");
      ValidationException.validate(parentId != null, "Must specify asset parent");
      final String query = "Select id From " + config.getDbDomain() + " Where `name`='"
      + name
      + "' intersection `fromId`='" + UUIDFactory.makeCleanString( parentId ) + "'";

      return new DbByParentLoader(db, config, loader, query );
    }
  }
}
