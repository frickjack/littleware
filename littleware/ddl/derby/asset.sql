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
CREATE TABLE littleware.littleTran (
	i_id          INTEGER PRIMARY KEY,
	l_transaction BIGINT
);

INSERT INTO littleware.littleTran( i_id, l_transaction ) VALUES ( 1, 1 );


--
-- Record the id's associated with the
--    littleware.security.LittlePermission
-- dynamic enum here too for convenience.
--
CREATE TABLE littleware.x_permission (
	s_id          VARCHAR(32) PRIMARY KEY,
	s_name        VARCHAR(32) UNIQUE NOT NULL,
	s_comment     VARCHAR(128) NOT NULL,
	t_created     TIMESTAMP NOT NULL DEFAULT current_timestamp
);

INSERT INTO littleware.x_permission (s_id, s_name, s_comment)
    VALUES ( 'EEB72C11DE934015BE42FA6FA9423EAC', 'read', 'read permission' );

INSERT INTO littleware.x_permission (s_id, s_name, s_comment)
    VALUES ( '55D1BF9F49234D839B56354BC2F2BA90', 'write', 'write permission' );



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
    b_name_unique INTEGER NOT NULL DEFAULT 0,
    x_parent_type VARCHAR(32) REFERENCES littleware.x_asset_type( s_id ),
	t_created     TIMESTAMP NOT NULL DEFAULT current_timestamp
);



INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'A7E11221546949FAAF1E8FCC52190F1D', 'littleware.principal', 'lw_principal.principal base class', -1 );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique,x_parent_type)
    VALUES ( '2FAFD5D1074F4BF8A4F01753DBFF4CD5', 'littleware.user', 'lw_principal.principal user',
              -1, 'A7E11221546949FAAF1E8FCC52190F1D' );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique, x_parent_type)
    VALUES ( 'FAA894CEC15B49CF8F8EC5C280062776', 'littleware.group', 'lw_principal.principal group',
              -1, 'A7E11221546949FAAF1E8FCC52190F1D'  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '04E11B112526462F91152DFFB51D21C9', 'littleware.acl', 'littleware.acl asset', -1 );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '926D122F82FE4F28A8F5C790E6733665', 'littleware.link', 'Just link to some other asset id', 0  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'BA50260718204D50BAC6AC711CEE1536', 'littleware.group_member',
	'Link from a group to another group or user', 0  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'E18D1B19D9714F6F8F49CF9B431EBF23', 'littleware.generic',
	'Place-holder in asset hierarchy with free-form data', 0  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'DB437A7D9BE14087B342AD63AF86BD7D', 'littleware.storage', 'reference to external storage', 0  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '208F22A176C24D3987F7738C68ECA01E', 'littleware.option', 'Option to buy/sell/pay/whatever', 0  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'E374F91AF6ED410284507B1FA8A807D2', 'littleware.pipeline',
	'data pipeline - XML state machine in s_data', 0  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '50626FA60B75491A8A16EDADE8E77488', 'littleware.pipeline_stage',
	'pipeline stage - refers to pipeline it belongs to', 0  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( 'D23EA8B5A55F4283AEF29DFA50C12C54', 'littleware.acl_entry',
	'littleware.acl entry - links acl to principal with permission', 0  );

INSERT INTO littleware.x_asset_type (s_id, s_name, s_comment, b_name_unique)
    VALUES ( '9D0C34BA0CE14407A09138AE4BA2581D', 'littleware.negative_acl_entry',
	'littleware.acl negative-permission entry - links acl to principal', 0  );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '3F2C7DF02A9C49A3BBAB179B9E643956', 'littleware.archiver', 'labels to nodes that are ready for archive', 0  );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( 'F0063935757F4319B7D9516F56E3B7F3', 'littleware.note', 'attaches a note to some other asset', 0  );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( 'C06CC38C6BD24D48AB5E2D228612C179', 'littleware.home',
	'home server tracker that other assets associate with', -1  );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '7AC8C92F30C14AD89FA82DB0060E70C2', 'littleware.session',
	          'login session tracking asset', 0 );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '6AD504ACBB3A4A2CAB5AECE02D8E6706', 'littleware.service',
	          'assets for managing client access to littleware services', -1 );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '0897E6CF8A4C4B128ECABD92FEF793AF', 'littleware.quota',
	          'assets for managing user quotas', 0 );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( 'D430F172C2F94F76ACDA39658027D95A', 'littleware.address',
	          'assets for managing user address', 0 );

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '2EE7CCDE130D40A09184C2A3F88A6F25', 'littleware.contact',
	          'assets for managing user contact info', 0 );

-- ....

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '84F04E04DCE947B2A00294949DC38628', 'littleware.task',
	          'task tracking assets', 0);

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( 'FB8CC7B7C9324EC8953DE50A700344F3', 'littleware.comment',
	          'asset comment', 0);

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '0FE9FBED5F6846E1865526A2BFBC5182', 'littleware.queue',
	          'queue asset', 0);

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '489F21E1D19B49F3B923E7B45609A811', 'littleware.dependency',
	          'tracker dependency asset', 0);

INSERT INTO littleware.x_asset_type ( s_id, s_name, s_comment, b_name_unique )
    VALUES ( '5C52B28DA10A435B957AD5EF454F01C7', 'littleware.lock',
	          'name-unique distributed exclusion lock', -1 );



-- ..................................
--
-- Cache out the inheritance tree
--
CREATE TABLE littleware.x_asset_type_tree (
         s_ancestor_id           VARCHAR(32) NOT NULL REFERENCES littleware.x_asset_type(s_id),
         s_descendent_id     VARCHAR(32) NOT NULL REFERENCES littleware.x_asset_type( s_id ),
         PRIMARY KEY (s_ancestor_id, s_descendent_id)
         );

INSERT INTO littleware.x_asset_type_tree (s_ancestor_id, s_descendent_id)
  SELECT x_parent_type, s_id FROM littleware.x_asset_type WHERE x_parent_type IS NOT NULL;

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
	l_last_transaction  BIGINT NOT NULL,
	s_pk_type           VARCHAR(32) NOT NULL REFERENCES littleware.x_asset_type( s_id ),
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
	t_created           TIMESTAMP NOT NULL DEFAULT current_timestamp,
	t_updated           TIMESTAMP,
	t_last_accessed     TIMESTAMP,
	t_start             TIMESTAMP, 
	t_end               TIMESTAMP
);


CREATE UNIQUE INDEX asset_fromname_idx ON littleware.asset ( s_id_from, s_name );
CREATE INDEX asset_from_idx ON littleware.asset ( s_id_from, s_id_to, s_id_home );
CREATE INDEX asset_to_idx ON littleware.asset ( s_id_to, s_id_from, s_id_home );
CREATE INDEX asset_typename_idx ON littleware.asset ( s_pk_type, s_name );
CREATE INDEX asset_transaction_idx ON littleware.asset( l_last_transaction );
CREATE INDEX asset_from_type_idx ON littleware.asset ( s_id_from, s_pk_type, i_state );


------


-- ..................................................
-- littleware.home
INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'BD46E5588F9D4F41A6310100FE68DCB4', 'littleware.home',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name= 'littleware.home' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'littleware home', 'create home',
       '', NULL, NULL,
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );

-- .....
-- littleware.administrator
INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       '00000000000000000000000000000000', 'littleware.administrator',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name='littleware.user' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'root admin user', 'create admin',
       '', 'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );


-- littleware.test_user
INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       '7AC5D21049254265B224B7512EFCF0D1', 'littleware.test_user',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name='littleware.user' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '7AC5D21049254265B224B7512EFCF0D1',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'test user', 'create test user',
       '', 'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );

-- ......

INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'D589EABED8EA43C1890DBF3CF1F9689A', 'littleware.test_home',
       'D589EABED8EA43C1890DBF3CF1F9689A', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name='littleware.home' ),
       '7AC5D21049254265B224B7512EFCF0D1',
       '7AC5D21049254265B224B7512EFCF0D1',
       '7AC5D21049254265B224B7512EFCF0D1',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'test home', 'create test home',
       '', NULL, NULL,
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );

-- ......



-- ......

INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       '89A1CB79B5944447BED9F38D398A7D12',
       'group.littleware.administrator',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name='littleware.group' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'admin group', 'create admin group',
       '', 'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );

-- ......

INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'D701C9B3C9B7453299E89A0161DDC242',
       'group.littleware.everybody',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name='littleware.group' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'everybody group', 'create everybody group',
       '', 'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );

-- ......

-- admin in admin group
INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'B6ECCDEF7A8D4452AAC52716FA380795',
       'administrator',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name='littleware.group_member' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'admin in admin group', 'admin in admin group',
       '',
       '89A1CB79B5944447BED9F38D398A7D12', '00000000000000000000000000000000',
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );

-- admin in everybody group
INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'CA9FFC8762674DB8AF28DD8955B07D98',
       'administrator',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name='littleware.group_member' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'admin in everybody group', 'admin in everybody group',
       '',
       'D701C9B3C9B7453299E89A0161DDC242', '00000000000000000000000000000000',
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );



-- test_user in everybody group
INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       '6EDBC32311B943ECAE7A572F0FC2BB91',
       'test_user',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name='littleware.group_member' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'test_user in everybody group', 'test_user in everybody group',
       '',
       'D701C9B3C9B7453299E89A0161DDC242', '7AC5D21049254265B224B7512EFCF0D1',
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );

-- acl
INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'acl.littleware.everybody.read',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name='littleware.acl' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'everybody read acl', 'everybody read acl',
       '',
       'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );


INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'A20FF0A0829A433FBA7CF90B128F4FDF',
       'group.littleware.everybody.positive',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name='littleware.acl_entry' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       1.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'everybody read acl', 'everybody read acl',
		'<acl:permlist xmlns:acl="http://www.littleware.com/xml/namespace/2006/acl"><acl:perm>EEB72C11DE934015BE42FA6FA9423EAC</acl:perm></acl:permlist>'
				,
       'F4CEDAA07B574FFFA27E0BA87078DC34', 'D701C9B3C9B7453299E89A0161DDC242',
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );



-- service-type entries
INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'EDACB6E64AD54169AC72A2490D06E6B6',
       'littleware.service_home',
       'EDACB6E64AD54169AC72A2490D06E6B6', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name='littleware.home' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'service home', 'create service home',
       '', NULL, NULL,
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );

INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'FD4C5F5B4C904AC6BDC9ECA891C39543',
       'littleware.asset_manager_service',
       'EDACB6E64AD54169AC72A2490D06E6B6', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name='littleware.service' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'service', 'create service',
       '', 'EDACB6E64AD54169AC72A2490D06E6B6', NULL,
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );

INSERT INTO littleware.asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       '56A05693C0874780A716DEFA4E262F6F',
       'littleware.asset_search_service',
       'EDACB6E64AD54169AC72A2490D06E6B6', 1,
       (SELECT s_id FROM littleware.x_asset_type WHERE s_name='littleware.service' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'service', 'create service',
       '', 'EDACB6E64AD54169AC72A2490D06E6B6', NULL,
       current_timestamp, current_timestamp, current_timestamp,
       NULL, NULL
      );

