ALTER TABLE "ip_discovery_certificate"
	DROP COLUMN IF EXISTS "discovery_source";

ALTER TABLE "ip_discovery_certificate"
	ADD IF NOT EXISTS "uuid" VARCHAR NULL;

ALTER TABLE "ip_discovery_history"
    ADD IF NOT EXISTS "uuid" VARCHAR NULL;
