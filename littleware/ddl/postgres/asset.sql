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
-- Create a postgres group of users with general access
-- to the littleware schema.
-- Not necessary if create_database.sql runs ok.
-- 
--CREATE GROUP littleware_user_group;
--GRANT littleware_user_group TO littleware_user;
--GRANT littleware_user_group TO littleware;

CREATE LANGUAGE plpgsql;

CREATE SCHEMA littleware;

GRANT USAGE ON SCHEMA littleware TO GROUP littleware_user_group;

-- ......................

CREATE SEQUENCE littleware.seq_transaction_counter MINVALUE 1
   NO MAXVALUE START WITH 1000;

GRANT SELECT, UPDATE ON littleware.seq_transaction_counter TO GROUP littleware_user_group;


---
 
	
--
-- Record the id's associated with the 
--    littleware.security.LittlePermission
-- dynamic enum here too for convenience.
--
CREATE TABLE littleware.x_permission (
	s_id          VARCHAR(32) PRIMARY KEY,
	s_name        VARCHAR(32) UNIQUE NOT NULL,
	s_comment     VARCHAR(128) NOT NULL,
	t_created     TIMESTAMP NOT NULL DEFAULT now(),
	t_updated     TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO littleware.x_permission (s_id, s_name, s_comment)
    VALUES ( 'EEB72C11DE934015BE42FA6FA9423EAC', 'read', 'read permission' );
	
INSERT INTO littleware.x_permission (s_id, s_name, s_comment)
    VALUES ( '55D1BF9F49234D839B56354BC2F2BA90', 'write', 'write permission' );

GRANT SELECT ON littleware.x_permission TO GROUP littleware_user_group;


--
-- asset-type table.  Pk's 1-1000000 reserved
-- for littleware.
-- The b_name_unique column indicates whether
-- the database should attempt to ensure that
-- assets with the given type have a database-wide unique name.
--
CREATE TABLE littleware.x_asset_type (
	s_id          VARCHAR(32) PRIMARY KEY,
	s_name        VARCHAR(32) UNIQUE NOT NULL,
	s_comment     VARCHAR(128) NOT NULL,
    b_name_unique BOOLEAN NOT NULL,
    x_parent_type VARCHAR(32) REFERENCES littleware.x_asset_type( s_id ),
	t_created     TIMESTAMP NOT NULL DEFAULT now(),
	t_updated     TIMESTAMP NOT NULL DEFAULT now()
);

GRANT SELECT ON littleware.x_asset_type TO GROUP littleware_user_group;

--
-- Cache out the inheritance tree
--
CREATE TABLE littleware.x_asset_type_tree (
         s_ancestor_id           VARCHAR(32) NOT NULL REFERENCES littleware.x_asset_type(s_id),
         s_descendent_id     VARCHAR(32) NOT NULL REFERENCES littleware.x_asset_type( s_id ),
         PRIMARY KEY (s_ancestor_id, s_descendent_id)
         );
         
GRANT SELECT ON littleware.x_asset_type_tree TO littleware_user_group;


INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'A7E11221546949FAAF1E8FCC52190F1D', 'littleware.principal', 'lw_principal.principal base class', true );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique,x_parent_type)
    VALUES ( '2FAFD5D1074F4BF8A4F01753DBFF4CD5', 'littleware.user', 'lw_principal.principal user', 
              true, 'A7E11221546949FAAF1E8FCC52190F1D' );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique, x_parent_type)
    VALUES ( 'FAA894CEC15B49CF8F8EC5C280062776', 'littleware.group', 'lw_principal.principal group', 
              true, 'A7E11221546949FAAF1E8FCC52190F1D'  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '04E11B112526462F91152DFFB51D21C9', 'littleware.acl', 'littleware.acl asset', true );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '926D122F82FE4F28A8F5C790E6733665', 'littleware.link', 'Just link to some other asset id', false  );
	
INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'BA50260718204D50BAC6AC711CEE1536', 'littleware.group_member', 
	'Link from a group to another group or user', false  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'E18D1B19D9714F6F8F49CF9B431EBF23', 'littleware.generic', 
	'Place-holder in asset hierarchy with free-form data', false  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'DB437A7D9BE14087B342AD63AF86BD7D', 'littleware.storage', 'reference to external storage', false  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '208F22A176C24D3987F7738C68ECA01E', 'littleware.option', 'Option to buy/sell/pay/whatever', false  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'E374F91AF6ED410284507B1FA8A807D2', 'littleware.pipeline', 
	'data pipeline - XML state machine in s_data', false  );
	
INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '50626FA60B75491A8A16EDADE8E77488', 'littleware.pipeline_stage', 
	'pipeline stage - refers to pipeline it belongs to', false  );
	
INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'D23EA8B5A55F4283AEF29DFA50C12C54', 'littleware.acl_entry', 
	'littleware.acl entry - links acl to principal with permission', false  );
	
INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '9D0C34BA0CE14407A09138AE4BA2581D', 'littleware.negative_acl_entry', 
	'littleware.acl negative-permission entry - links acl to principal', false  );
	
INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '3F2C7DF02A9C49A3BBAB179B9E643956', 'littleware.archiver', 'labels to nodes that are ready for archive', false  );
	
INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( 'F0063935757F4319B7D9516F56E3B7F3', 'littleware.note', 'attaches a note to some other asset', false  );
	
INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( 'C06CC38C6BD24D48AB5E2D228612C179', 'littleware.home', 
	'home server tracker that other assets associate with', true  );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '7AC8C92F30C14AD89FA82DB0060E70C2', 'littleware.session', 
	          'login session tracking asset', false );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '6AD504ACBB3A4A2CAB5AECE02D8E6706', 'littleware.service', 
	          'assets for managing client access to littleware services', true );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '0897E6CF8A4C4B128ECABD92FEF793AF', 'littleware.quota', 
	          'assets for managing user quotas', false );
			  
INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( 'D430F172C2F94F76ACDA39658027D95A', 'littleware.address', 
	          'assets for managing user address', false );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '2EE7CCDE130D40A09184C2A3F88A6F25', 'littleware.contact', 
	          'assets for managing user contact info', false );

-- ....

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '84F04E04DCE947B2A00294949DC38628', 'littleware.task', 
	          'task tracking assets', false );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( 'FB8CC7B7C9324EC8953DE50A700344F3', 'littleware.comment', 
	          'asset comment', false );
              
INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '0FE9FBED5F6846E1865526A2BFBC5182', 'littleware.queue', 
	          'queue asset', false );
              
INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '489F21E1D19B49F3B923E7B45609A811', 'littleware.dependency', 
	          'tracker dependency asset', false );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '5C52B28DA10A435B957AD5EF454F01C7', 'littleware.lock',
	          'name-unique distributed exclusion lock', true );

                                                                                  

--
-- Internal utility - run after adding new asset subtypes.
-- Clear and rebuild x_asset_type_tree with
-- the appropriate ancestore/descendent relationships from
-- littleware.x_asset_type.
--
CREATE OR REPLACE FUNCTION littleware.rebuildAssetTypeTree()
    RETURNS INTEGER AS $FUNC$
    BEGIN
        DELETE FROM littleware.x_asset_type_tree;        
        INSERT INTO littleware.x_asset_type_tree (s_ancestor_id, s_descendent_id)
            SELECT x_parent_type, s_id FROM littleware.x_asset_type 
            WHERE x_parent_type IS NOT NULL;            
        --
        -- Find descendents of descendents
        --
        LOOP
          INSERT INTO littleware.x_asset_type_tree (s_ancestor_id, s_descendent_id)
            SELECT DISTINCT a.s_ancestor_id, b.s_descendent_id 
              FROM littleware.x_asset_type_tree a, littleware.x_asset_type_tree b
              WHERE a.s_descendent_id=b.s_ancestor_id
                AND (a.s_ancestor_id, b.s_descendent_id) 
                  NOT IN (SELECT s_ancestor_id, s_descendent_id FROM littleware.x_asset_type_tree);
          IF NOT FOUND THEN
              RETURN 0;
          END IF;
        END LOOP;
    END; $FUNC$
LANGUAGE plpgsql;

SELECT littleware.rebuildAssetTypeTree();


--
-- Asset table tracks asset nodes in the asset graph.
-- Note:
--    Manage asset states (ready2archive, whatever)
--        via the node-graph
--
CREATE TABLE littleware.asset (	
	s_id                VARCHAR(32) PRIMARY KEY,
	s_name              VARCHAR(80) NOT NULL,
	s_id_home           VARCHAR(32) NOT NULL,
	l_last_transaction  BIGINT NOT NULL DEFAULT nextval( 'littleware.seq_transaction_counter' ),
	s_pk_type           VARCHAR(32) REFERENCES littleware.x_asset_type( s_id ) NOT NULL,
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
	t_created           TIMESTAMP NOT NULL DEFAULT now(),
	t_updated           TIMESTAMP NOT NULL DEFAULT now(),
	t_last_accessed     TIMESTAMP NOT NULL DEFAULT now(),
	t_start             TIMESTAMP,  -- let asset correspond to timed event
	t_end               TIMESTAMP
);

CREATE UNIQUE INDEX asset_fromname_idx ON littleware.asset ( s_id_from, s_name );
CREATE INDEX asset_from_idx ON littleware.asset ( s_id_from, s_id_to, s_id_home );
CREATE INDEX asset_to_idx ON littleware.asset ( s_id_to, s_id_from, s_id_home );
CREATE INDEX asset_typename_idx ON littleware.asset ( s_pk_type, s_name );
CREATE INDEX asset_transaction_idx ON littleware.asset( l_last_transaction );

GRANT SELECT, UPDATE, INSERT, DELETE ON littleware.asset TO GROUP littleware_user_group;

--
-- Composite type for name to id maps
--
CREATE TYPE littleware.name2id AS (
    s_name    VARCHAR(80),
	s_id      VARCHAR(32)
	);

--
-- Composite type for uuid lists
--
CREATE TYPE littleware.id AS (
    s_id       VARCHAR(32)
    );
    
--
-- Track basic asset history
--
CREATE TABLE littleware.asset_history (
	s_id                VARCHAR(32) NOT NULL,
	s_name              VARCHAR(80) NOT NULL,
	s_id_home           VARCHAR(32) NOT NULL,
	l_min_transaction   BIGINT NOT NULL, -- inclusive
	l_max_transaction   BIGINT NOT NULL DEFAULT -1, -- exclusive
	s_pk_type           VARCHAR(32) REFERENCES littleware.x_asset_type( s_id ) NOT NULL,
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
	t_created           TIMESTAMP NOT NULL DEFAULT now(),
	t_updated           TIMESTAMP NOT NULL DEFAULT now(),
	t_last_accessed     TIMESTAMP NOT NULL DEFAULT now(),
	t_start             TIMESTAMP,  -- let asset correspond to timed event
	t_end               TIMESTAMP,
	t_copied            TIMESTAMP NOT NULL DEFAULT now()   -- date of transaction-history creation
);

CREATE UNIQUE INDEX asset_history_min_idx ON littleware.asset_history ( s_id, l_min_transaction );
CREATE UNIQUE INDEX asset_history_max_idx ON littleware.asset_history ( s_id, l_max_transaction );

CREATE INDEX asset_history_idx ON littleware.asset_history ( s_id, t_copied );

GRANT SELECT, INSERT, UPDATE, DELETE ON littleware.asset_history TO GROUP littleware_user_group;

-- ....................................

--
-- Valid status values
--
CREATE TABLE littleware.x_cache_status (
	i_id          INTEGER PRIMARY KEY,
	s_comment     VARCHAR(128) NOT NULL,
	t_created     TIMESTAMP NOT NULL DEFAULT now(),
	t_updated     TIMESTAMP NOT NULL DEFAULT now()
);

GRANT SELECT ON littleware.x_cache_status TO GROUP littleware_user_group;

INSERT INTO x_cache_status ( i_id, s_comment ) VALUES ( 0, 'asset in cache ok' );
INSERT INTO x_cache_status ( i_id, s_comment ) VALUES ( 1, 'asset in cache out of date' );
INSERT INTO x_cache_status ( i_id, s_comment ) VALUES ( 2, 'asset in cache deleted from repository' );

--
-- Track what each client has loaded into memory
--
CREATE TABLE littleware.client_cache (
    i_src               INTEGER NOT NULL,
	s_asset_id          VARCHAR(32) NOT NULL,
    i_status            INTEGER REFERENCES littleware.x_cache_status( i_id ) NOT NULL,
    PRIMARY KEY ( i_src, s_asset_id )
);

CREATE INDEX client_cache_asset_idx ON littleware.client_cache( s_asset_id );
GRANT SELECT, INSERT, UPDATE, DELETE ON littleware.client_cache TO GROUP littleware_user_group;


