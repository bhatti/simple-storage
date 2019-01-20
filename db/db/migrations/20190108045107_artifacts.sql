-- migrate:up

CREATE TABLE IF NOT EXISTS fs.artifacts (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    application VARCHAR(100) NOT NULL,
    job VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    digest VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    platform VARCHAR(100),
    content_type VARCHAR(100),
    user_agent VARCHAR(100),
    labels VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP  NOT NULL DEFAULT now()
) ;

CREATE UNIQUE INDEX IF NOT EXISTS xartifact_app_job_name ON fs.artifacts(application, job, name);
CREATE INDEX IF NOT EXISTS xartifact_app_job ON fs.artifacts(application, job);
CREATE INDEX IF NOT EXISTS xartifact_app ON fs.artifacts(application);
CREATE INDEX IF NOT EXISTS xartifact_digest ON fs.artifacts(digest);
CREATE INDEX IF NOT EXISTS xartifact_uagent ON fs.artifacts(user_agent);

CREATE TABLE IF NOT EXISTS fs.properties (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    artifact_id VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    FOREIGN KEY (artifact_id) REFERENCES fs.artifacts(id)
    ON DELETE CASCADE
) ;

CREATE UNIQUE INDEX IF NOT EXISTS xartifact_props_key ON fs.properties(artifact_id, name);
CREATE INDEX IF NOT EXISTS xartifact_props_fk ON fs.properties(artifact_id);


-- migrate:down
DELETE FROM fs.properties;
DELETE FROM fs.artifacts;
DROP INDEX IF EXISTS xartifact_props_key;
DROP INDEX IF EXISTS xartifact_props_fk;
DROP TABLE IF EXISTS fs.properties;

DROP INDEX IF EXISTS xartifact_app_job_name;
DROP INDEX IF EXISTS xartifact_app_job;
DROP INDEX IF EXISTS xartifact_app;
DROP INDEX IF EXISTS xartifact_digest;
DROP INDEX IF EXISTS xartifact_uagent;
DROP TABLE IF EXISTS fs.artifacts CASCADE;

