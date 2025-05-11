CREATE TABLE lemmas
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    site_id   BIGINT       NOT NULL,
    lemma     VARCHAR(255) NOT NULL,
    frequency INT          NOT NULL
);

CREATE TABLE indexes
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    page_id  UUID   NOT NULL,
    lemma_id BIGINT NOT NULL,
    rank     INT    NOT NULL,
    FOREIGN KEY (lemma_id) REFERENCES lemmas (id)
);