CREATE KEYSPACE cloudmgr
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};

// Access patterns - see Notes/explanation/cloudmgr.md
CREATE TABLE project (
  id uuid PRIMARY KEY,
  name text,
  owners frozen<set<string>>,
  apis   frozen<set<string>>,
  state text
) WITH bloom_filter_fp_chance = 0.01;

CREATE TABLE project_by_owner {
    id uuid,
    name text,
    owner text,
    state text,
    PRIMARY KEY (owner, id)
} WITH bloom_filter_fp_chance = 0.01;

CREATE TABLE session (
    id uuid PRIMARY KEY,
    project_id uuid,
    project_name text,
    cell_id text,
    user text,
    api text,
    endpoint text,
    iat date,
    exp date
) WITH bloom_filter_fp_chance = 0.01 
   AND default_time_to_live=864000
   AND compaction = { 
       'class': 'TimeWindowCompactionStrategy',
       'compaction_window_size': '12',
       'compaction_window_unit': 'HOURS' 
       };

CREATE TABLE session_by_user (
    id uuid,
    project_id uuid,
    project_name text,
    user text,
    api text,
    start_date date,
    iat timestamp,
    exp timestamp,
    PRIMARY KEY ((user, start_date), iat, id)
) WITH CLUSTERING ORDER BY (iat DESC)
   AND bloom_filter_fp_chance = 0.01 
   AND default_time_to_live=864000
   AND compaction = { 
       'class': 'TimeWindowCompactionStrategy',
       'compaction_window_size': '12',
       'compaction_window_unit': 'HOURS' 
       };

CREATE TABLE session_by_project (
    id uuid,
    project_id uuid,
    project_name text,
    user text,
    api text,
    start_date date,
    iat timestamp,
    exp timestamp,
    PRIMARY KEY ((project, start_date), iat, id)
) WITH CLUSTERING ORDER BY (iat DESC)
   AND bloom_filter_fp_chance = 0.01 
   AND default_time_to_live=864000
   AND compaction = { 
       'class': 'TimeWindowCompactionStrategy',
       'compaction_window_size': '12',
       'compaction_window_unit': 'HOURS' 
       };

CREATE TABLE session_by_api (
    id uuid,
    project_id uuid,
    project_name text,
    user text,
    api text,
    start_date date,
    iat timestamp,
    exp timestamp,
    PRIMARY KEY ((api, start_date), iat, id)
) WITH CLUSTERING ORDER BY (iat DESC)
   AND bloom_filter_fp_chance = 0.01 
   AND default_time_to_live=864000
   AND compaction = { 
       'class': 'TimeWindowCompactionStrategy',
       'compaction_window_size': '12',
       'compaction_window_unit': 'HOURS' 
       };

CREATE TABLE cell (
    id uuid PRIMARY KEY,
    api text,
    endpoint text
) WITH bloom_filter_fp_chance = 0.01;

CREATE TABLE cell_by_api_project (
    cell_id uuid,
    api text,
    project_id uuid,
    project_name string,
    endpoint text,
    PRIMARY KEY ((api, project_id), cell_id)
) WITH bloom_filter_fp_chance = 0.01;

CREATE TABLE cell_by_project (
    cell_id uuid,
    api text,
    project_id uuid,
    project_name string,
    endpoint text,
    PRIMARY KEY (project_id, api, cell_id)
) WITH CLUSTERING ORDER BY (api) 
  AND bloom_filter_fp_chance = 0.01;
