--
-- asset.sql
--
-- See how far we can get underpinning
-- the entries in the littleware database
-- on a master asset tree.
-- An asset is some arbitrary thing
-- in an asset tree of some kind or whatever.
--


--
-- Homebrew transaction counter.
-- Single-row table - should be accessed/updated from
-- within a transaction.
--
CREATE TABLE littleTran (
	i_id          INTEGER PRIMARY KEY,
	l_transaction BIGINT
) ENGINE INNODB CHARACTER SET UTF8;

INSERT INTO littleTran( i_id, l_transaction ) VALUES ( 1, 1 );



--
-- Record the id's associated with the
--    security.LittlePermission
-- dynamic enum here too for convenience.
--
CREATE TABLE x_permission (
	s_id          VARCHAR(32) PRIMARY KEY,
	s_name        VARCHAR(32) UNIQUE NOT NULL,
	s_comment     VARCHAR(128) NOT NULL,
	t_created     TIMESTAMP NOT NULL DEFAULT now()
) ENGINE INNODB CHARACTER SET UTF8;

INSERT INTO x_permission (s_id, s_name, s_comment)
    VALUES ( 'EEB72C11DE934015BE42FA6FA9423EAC', 'read', 'read permission' );

INSERT INTO x_permission (s_id, s_name, s_comment)
    VALUES ( '55D1BF9F49234D839B56354BC2F2BA90', 'write', 'write permission' );




--
-- asset-type table.  Pk's 1-1000000 reserved
-- for
-- The b_name_unique column indicates whether
-- the database should attempt to ensure that
-- assets with the given type have a database-wide unique name.
--
CREATE TABLE x_asset_type (
	s_id          VARCHAR(32) PRIMARY KEY,
	s_name        VARCHAR(32) UNIQUE NOT NULL,
	s_comment     VARCHAR(128) NOT NULL,
    b_name_unique BOOLEAN NOT NULL,
    x_parent_type VARCHAR(32) REFERENCES x_asset_type( s_id ),
	t_created     TIMESTAMP NOT NULL DEFAULT now()
) ENGINE INNODB CHARACTER SET UTF8;




--
-- Cache out the inheritance tree
--
CREATE TABLE x_asset_type_tree (
         s_ancestor_id           VARCHAR(32) NOT NULL REFERENCES x_asset_type(s_id),
         s_descendent_id     VARCHAR(32) NOT NULL REFERENCES x_asset_type( s_id ),
         PRIMARY KEY (s_ancestor_id, s_descendent_id)
         ) ENGINE INNODB CHARACTER SET UTF8;



INSERT INTO x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'A7E11221546949FAAF1E8FCC52190F1D', 'littleware.principal', 'lw_principal.principal base class', true );

INSERT INTO x_asset_type (s_id, s_name, s_comment, b_name_unique,x_parent_type)
    VALUES ( '2FAFD5D1074F4BF8A4F01753DBFF4CD5', 'littleware.user', 'lw_principal.principal user',
              true, 'A7E11221546949FAAF1E8FCC52190F1D' );

INSERT INTO x_asset_type (s_id, s_name, s_comment, b_name_unique, x_parent_type)
    VALUES ( 'FAA894CEC15B49CF8F8EC5C280062776', 'littleware.group', 'lw_principal.principal group',
              true, 'A7E11221546949FAAF1E8FCC52190F1D'  );

INSERT INTO x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '04E11B112526462F91152DFFB51D21C9', 'littleware.acl', 'littleware.acl asset', true );

INSERT INTO x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '926D122F82FE4F28A8F5C790E6733665', 'littleware.link', 'Just link to some other asset id', false  );

INSERT INTO x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'BA50260718204D50BAC6AC711CEE1536', 'littleware.group_member',
	'Link from a group to another group or user', false  );

INSERT INTO x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'E18D1B19D9714F6F8F49CF9B431EBF23', 'littleware.generic',
	'Place-holder in asset hierarchy with free-form data', false  );

INSERT INTO x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'DB437A7D9BE14087B342AD63AF86BD7D', 'littleware.storage', 'reference to external storage', false  );

INSERT INTO x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '208F22A176C24D3987F7738C68ECA01E', 'littleware.option', 'Option to buy/sell/pay/whatever', false  );

INSERT INTO x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'E374F91AF6ED410284507B1FA8A807D2', 'littleware.pipeline',
	'data pipeline - XML state machine in s_data', false  );

INSERT INTO x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '50626FA60B75491A8A16EDADE8E77488', 'littleware.pipeline_stage',
	'pipeline stage - refers to pipeline it belongs to', false  );

INSERT INTO x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'D23EA8B5A55F4283AEF29DFA50C12C54', 'littleware.acl_entry',
	'littleware.acl entry - links acl to principal with permission', false  );

INSERT INTO x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '9D0C34BA0CE14407A09138AE4BA2581D', 'littleware.negative_acl_entry',
	'littleware.acl negative-permission entry - links acl to principal', false  );

INSERT INTO x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '3F2C7DF02A9C49A3BBAB179B9E643956', 'littleware.archiver', 'labels to nodes that are ready for archive', false  );

INSERT INTO x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( 'F0063935757F4319B7D9516F56E3B7F3', 'littleware.note', 'attaches a note to some other asset', false  );

INSERT INTO x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( 'C06CC38C6BD24D48AB5E2D228612C179', 'littleware.home',
	'home server tracker that other assets associate with', true  );

INSERT INTO x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '7AC8C92F30C14AD89FA82DB0060E70C2', 'littleware.session',
	          'login session tracking asset', false );

INSERT INTO x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '6AD504ACBB3A4A2CAB5AECE02D8E6706', 'littleware.service',
	          'assets for managing client access to littleware services', true );

INSERT INTO x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '0897E6CF8A4C4B128ECABD92FEF793AF', 'littleware.quota',
	          'assets for managing user quotas', false );

INSERT INTO x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( 'D430F172C2F94F76ACDA39658027D95A', 'littleware.address',
	          'assets for managing user address', false );

INSERT INTO x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '2EE7CCDE130D40A09184C2A3F88A6F25', 'littleware.contact',
	          'assets for managing user contact info', false );

-- ....

INSERT INTO x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '84F04E04DCE947B2A00294949DC38628', 'littleware.task',
	          'task tracking assets', false );

INSERT INTO x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( 'FB8CC7B7C9324EC8953DE50A700344F3', 'littleware.comment',
	          'asset comment', false );

INSERT INTO x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '0FE9FBED5F6846E1865526A2BFBC5182', 'littleware.queue',
	          'queue asset', false );

INSERT INTO x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '489F21E1D19B49F3B923E7B45609A811', 'littleware.dependency',
	          'tracker dependency asset', false );

INSERT INTO x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '5C52B28DA10A435B957AD5EF454F01C7', 'littleware.lock',
	          'name-unique distributed exclusion lock', true );



--
-- Internal utility - run after adding new asset subtypes.
-- Clear and rebuild x_asset_type_tree with
-- the appropriate ancestore/descendent relationships from
-- x_asset_type.
--
delimiter //
CREATE PROCEDURE rebuildAssetTypeTree()
    BEGIN
        DELETE FROM x_asset_type_tree;
        INSERT INTO x_asset_type_tree (s_ancestor_id, s_descendent_id)
            SELECT x_parent_type, s_id FROM x_asset_type
            WHERE x_parent_type IS NOT NULL;
        --
        -- Find descendents of descendents
        --
        buildTree: LOOP
          INSERT INTO x_asset_type_tree (s_ancestor_id, s_descendent_id)
            SELECT DISTINCT a.s_ancestor_id, b.s_descendent_id
              FROM x_asset_type_tree a, x_asset_type_tree b
              WHERE a.s_descendent_id=b.s_ancestor_id
                AND (a.s_ancestor_id, b.s_descendent_id)
                  NOT IN (SELECT s_ancestor_id, s_descendent_id FROM x_asset_type_tree);
          IF ROW_COUNT() < 1 THEN
              LEAVE buildTree;
          END IF;
        END LOOP buildTree;
    END; 
//
delimiter ;

CALL rebuildAssetTypeTree();


--
-- Asset table tracks asset nodes in the asset graph.
-- Note:
--    Manage asset states (ready2archive, whatever)
--        via the node-graph
--
CREATE TABLE asset (
	s_id                VARCHAR(32) PRIMARY KEY,
	s_name              VARCHAR(80) NOT NULL,
	s_id_home           VARCHAR(32) NOT NULL,
	l_last_transaction  BIGINT NOT NULL,
	s_pk_type           VARCHAR(32) NOT NULL REFERENCES x_asset_type( s_id ),
	s_id_creator        VARCHAR(32) NOT NULL,
	s_id_updater        VARCHAR(32) NOT NULL,
	s_id_owner          VARCHAR(32) NOT NULL,
	f_value             NUMERIC(16,4),
        i_state             INTEGER DEFAULT 0,
	s_id_acl            VARCHAR(32),
	s_comment           VARCHAR(256),
	s_last_change       VARCHAR(128) NOT NULL,
	s_data              VARCHAR(1024),
	s_id_from           VARCHAR(32),
	s_id_to             VARCHAR(32),
	t_created           TIMESTAMP NOT NULL DEFAULT now(),
	t_updated           TIMESTAMP,
	t_last_accessed     TIMESTAMP,
	t_start             TIMESTAMP NULL DEFAULT NULL,  -- let asset correspond to timed event
	t_end               TIMESTAMP NULL DEFAULT NULL
) ENGINE INNODB CHARACTER SET UTF8;



CREATE UNIQUE INDEX asset_fromname_idx ON asset ( s_id_from, s_name );
CREATE INDEX asset_from_idx ON asset ( s_id_from, s_id_to, s_id_home );
CREATE INDEX asset_from_type_idx ON asset ( s_id_from, s_pk_type, i_state );
CREATE INDEX asset_to_idx ON asset ( s_id_to, s_id_from, s_id_home );
CREATE INDEX asset_typename_idx ON asset ( s_pk_type, s_name );
CREATE INDEX asset_transaction_idx ON asset( l_last_transaction );


--
-- Asset key/value attributes
--
CREATE TABLE asset_attr (
   i_id   SERIAL PRIMARY KEY,
   s_asset_id    VARCHAR(32) NOT NULL REFERENCES asset(s_id) ON DELETE CASCADE,
   s_key         VARCHAR(20) NOT NULL,
   s_value       VARCHAR(128)
) ENGINE INNODB CHARACTER SET UTF8;

CREATE UNIQUE INDEX asset_attr_idx ON asset_attr (s_asset_id, s_key );

--
-- Asset links
--
CREATE TABLE asset_link (
    i_id SERIAL PRIMARY KEY,
    s_asset_id VARCHAR(32) NOT NULL REFERENCES asset(s_id) ON DELETE CASCADE,
    s_key      VARCHAR(20) NOT NULL,
    s_value     VARCHAR(32)
) ENGINE INNODB CHARACTER SET UTF8;

CREATE UNIQUE INDEX asset_link_idx ON asset_link (s_asset_id, s_key );
CREATE INDEX link_value_idx ON asset_link( s_key, s_value );

--
-- Asset dates
--
CREATE TABLE asset_date (
    i_id SERIAL PRIMARY KEY,
    s_asset_id VARCHAR(32) NOT NULL REFERENCES asset(s_id) ON DELETE CASCADE,
    s_key      VARCHAR(20) NOT NULL,
    t_value    TIMESTAMP NULL DEFAULT NULL
) ENGINE INNODB CHARACTER SET UTF8;

CREATE UNIQUE INDEX asset_date_idx ON asset_date (s_asset_id, s_key );

--
-- Track basic asset history
--
CREATE TABLE asset_history (
	s_id                VARCHAR(32) NOT NULL,
	s_name              VARCHAR(80) NOT NULL,
	s_id_home           VARCHAR(32) NOT NULL,
	l_min_transaction   BIGINT NOT NULL, -- inclusive
	l_max_transaction   BIGINT NOT NULL DEFAULT -1, -- exclusive
	s_pk_type           VARCHAR(32) NOT NULL REFERENCES x_asset_type( s_id ),
	s_id_creator        VARCHAR(32) NOT NULL,
	s_id_updater        VARCHAR(32) NOT NULL,
	s_id_owner          VARCHAR(32) NOT NULL,
	f_value             NUMERIC(16,4),
	s_id_acl            VARCHAR(32),
	s_comment           VARCHAR(256),
	s_last_change       VARCHAR(128) NOT NULL,
	s_data              VARCHAR(1024),
	s_id_from           VARCHAR(32),
	s_id_to             VARCHAR(32),
t_copied            TIMESTAMP DEFAULT now(),   -- date of transaction-history creation
	t_created           TIMESTAMP,
	t_updated           TIMESTAMP,
	t_last_accessed     TIMESTAMP,
	t_start             TIMESTAMP,  -- let asset correspond to timed event
	t_end               TIMESTAMP
) ENGINE INNODB CHARACTER SET UTF8;



CREATE UNIQUE INDEX asset_history_min_idx ON asset_history ( s_id, l_min_transaction );
CREATE UNIQUE INDEX asset_history_max_idx ON asset_history ( s_id, l_max_transaction );
CREATE INDEX asset_history_idx ON asset_history ( s_id, t_copied );

GRANT SELECT, UPDATE ON littleTran TO 'littleware_user'@'localhost';
GRANT SELECT ON x_permission TO 'littleware_user'@'localhost';
GRANT SELECT ON x_asset_type TO 'littleware_user'@'localhost';
GRANT SELECT ON x_asset_type_tree TO 'littleware_user'@'localhost';
GRANT SELECT, UPDATE, INSERT, DELETE ON asset TO 'littleware_user'@'localhost';

GRANT SELECT, UPDATE, INSERT, DELETE ON asset_attr TO 'littleware_user'@'localhost';
GRANT SELECT, UPDATE, INSERT, DELETE ON asset_link TO 'littleware_user'@'localhost';
GRANT SELECT, UPDATE, INSERT, DELETE ON asset_date TO 'littleware_user'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE ON asset_history TO 'littleware_user'@'localhost';
