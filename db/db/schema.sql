SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: fs; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA fs;


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: artifacts; Type: TABLE; Schema: fs; Owner: -
--

CREATE TABLE fs.artifacts (
    id character varying(255) NOT NULL,
    organization character varying(100) NOT NULL,
    system character varying(100) NOT NULL,
    subsystem character varying(100) NOT NULL,
    name character varying(100) NOT NULL,
    digest character varying(100) NOT NULL,
    size bigint NOT NULL,
    platform character varying(100),
    content_type character varying(100),
    username character varying(100),
    user_agent character varying(100),
    labels character varying(500) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: properties; Type: TABLE; Schema: fs; Owner: -
--

CREATE TABLE fs.properties (
    id character varying(255) NOT NULL,
    artifact_id character varying(255) NOT NULL,
    name character varying(100) NOT NULL,
    value character varying(255) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: schema_migrations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.schema_migrations (
    version character varying(255) NOT NULL
);


--
-- Name: artifacts artifacts_pkey; Type: CONSTRAINT; Schema: fs; Owner: -
--

ALTER TABLE ONLY fs.artifacts
    ADD CONSTRAINT artifacts_pkey PRIMARY KEY (id);


--
-- Name: properties properties_pkey; Type: CONSTRAINT; Schema: fs; Owner: -
--

ALTER TABLE ONLY fs.properties
    ADD CONSTRAINT properties_pkey PRIMARY KEY (id);


--
-- Name: schema_migrations schema_migrations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schema_migrations
    ADD CONSTRAINT schema_migrations_pkey PRIMARY KEY (version);


--
-- Name: xartifact_digest; Type: INDEX; Schema: fs; Owner: -
--

CREATE INDEX xartifact_digest ON fs.artifacts USING btree (digest);


--
-- Name: xartifact_id; Type: INDEX; Schema: fs; Owner: -
--

CREATE UNIQUE INDEX xartifact_id ON fs.artifacts USING btree (organization, system, subsystem, name);


--
-- Name: xartifact_org; Type: INDEX; Schema: fs; Owner: -
--

CREATE INDEX xartifact_org ON fs.artifacts USING btree (organization);


--
-- Name: xartifact_org_system; Type: INDEX; Schema: fs; Owner: -
--

CREATE INDEX xartifact_org_system ON fs.artifacts USING btree (organization, system, subsystem);


--
-- Name: xartifact_props_fk; Type: INDEX; Schema: fs; Owner: -
--

CREATE INDEX xartifact_props_fk ON fs.properties USING btree (artifact_id);


--
-- Name: xartifact_props_key; Type: INDEX; Schema: fs; Owner: -
--

CREATE UNIQUE INDEX xartifact_props_key ON fs.properties USING btree (artifact_id, name);


--
-- Name: xartifact_uagent; Type: INDEX; Schema: fs; Owner: -
--

CREATE INDEX xartifact_uagent ON fs.artifacts USING btree (user_agent);


--
-- Name: xartifact_username; Type: INDEX; Schema: fs; Owner: -
--

CREATE INDEX xartifact_username ON fs.artifacts USING btree (username);


--
-- Name: properties properties_artifact_id_fkey; Type: FK CONSTRAINT; Schema: fs; Owner: -
--

ALTER TABLE ONLY fs.properties
    ADD CONSTRAINT properties_artifact_id_fkey FOREIGN KEY (artifact_id) REFERENCES fs.artifacts(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--


--
-- Dbmate schema migrations
--

INSERT INTO public.schema_migrations (version) VALUES
    ('20190108044725'),
    ('20190108044844'),
    ('20190108045107');
