CREATE TABLE blog
(
    id         UUID PRIMARY KEY,
    blog_text  TEXT,
    path       VARCHAR(255),
    parse_time TIMESTAMP,
    site_id    BIGINT
);

CREATE TABLE blog_key_words
(
    blog_id  UUID         NOT NULL,
    key_word VARCHAR(255) NOT NULL,
    FOREIGN KEY (blog_id) REFERENCES blog (id) ON DELETE CASCADE
);

CREATE TABLE blog_images
(
    blog_id   UUID         NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    FOREIGN KEY (blog_id) REFERENCES blog (id) ON DELETE CASCADE
);

CREATE TABLE product
(
    id         UUID PRIMARY KEY,
    title      VARCHAR(255),
    cost       VARCHAR(255),
    path       VARCHAR(255),
    parse_time TIMESTAMP,
    site_id    BIGINT
);

CREATE TABLE product_images
(
    product_id UUID         NOT NULL,
    image_url  VARCHAR(255) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
);
