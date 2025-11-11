-- db/migration/V1__Init_schema.sql
CREATE TABLE category (
                          id BIGSERIAL PRIMARY KEY,
                          code VARCHAR(32) UNIQUE NOT NULL,
                          name VARCHAR(128) NOT NULL,
                          updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE item (
                      id BIGSERIAL PRIMARY KEY,
                      sku VARCHAR(64) UNIQUE NOT NULL,
                      name VARCHAR(128) NOT NULL,
                      price NUMERIC(10,2) NOT NULL,
                      stock INT NOT NULL,
                      description TEXT,
                      category_id BIGINT NOT NULL REFERENCES category(id),
                      updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_item_category ON item(category_id);
CREATE INDEX idx_item_updated_at ON item(updated_at);