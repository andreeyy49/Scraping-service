CREATE TABLE sites
(
    id          SERIAL PRIMARY KEY,
    status      varchar(255) NOT NULL,
    status_time TIMESTAMP    NOT NULL,
    last_error  TEXT,
    url         VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL
);

CREATE TABLE pages
(
    id      UUID PRIMARY KEY,
    site_id BIGINT,
    path    VARCHAR(255) NOT NULL,
    code    INT          NOT NULL,
    content TEXT   NOT NULL,
    CONSTRAINT fk_pages_site FOREIGN KEY (site_id) REFERENCES sites (id) ON DELETE CASCADE
);