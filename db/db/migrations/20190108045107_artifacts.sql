-- migrate:up

CREATE TABLE IF NOT EXISTS fs.artifacts (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    organization VARCHAR(100) NOT NULL,
    system VARCHAR(100) NOT NULL,
    subsystem VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    digest VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    platform VARCHAR(100),
    content_type VARCHAR(100),
    username VARCHAR(100),
    user_agent VARCHAR(100),
    labels VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP  NOT NULL DEFAULT now()
) ;

CREATE UNIQUE INDEX IF NOT EXISTS xartifact_id ON fs.artifacts(organization, system, subsystem, name);
CREATE INDEX IF NOT EXISTS xartifact_org_system ON fs.artifacts(organization, system, subsystem);
CREATE INDEX IF NOT EXISTS xartifact_org ON fs.artifacts(organization);
CREATE INDEX IF NOT EXISTS xartifact_digest ON fs.artifacts(digest);
CREATE INDEX IF NOT EXISTS xartifact_uagent ON fs.artifacts(user_agent);
CREATE INDEX IF NOT EXISTS xartifact_username ON fs.artifacts(username);

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

DROP INDEX IF EXISTS xartifact_id;
DROP INDEX IF EXISTS xartifact_org_system;
DROP INDEX IF EXISTS xartifact_org;
DROP INDEX IF EXISTS xartifact_digest;
DROP INDEX IF EXISTS xartifact_uagent;
DROP INDEX IF EXISTS xartifact_username;
DROP TABLE IF EXISTS fs.artifacts CASCADE;

