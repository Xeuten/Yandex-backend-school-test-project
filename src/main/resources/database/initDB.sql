CREATE TABLE IF NOT EXISTS "public"."main" (
    "id"     TEXT,
    "url"    VARCHAR(255),
    "date"   TIMESTAMP WITH TIME ZONE NOT NULL,
    "parent_id" TEXT,
    "type"     TEXT NOT NULL,
    "size"     BIGINT,
    PRIMARY KEY ("id")

);