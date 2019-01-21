CREATE TABLE artifacts (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    organization VARCHAR(100) NOT NULL,
    system VARCHAR(100) NOT NULL,
    subsystem VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    digest VARCHAR(50) NOT NULL,
    size BIGINT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    username VARCHAR(100),
    labels VARCHAR(500) NOT NULL,
    user_agent VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP  NOT NULL DEFAULT now()
) ;

CREATE TABLE artifact_properties (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    artifact_id VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    FOREIGN KEY (artifact_id) REFERENCES artifacts(id)
    ON DELETE CASCADE
) ;
