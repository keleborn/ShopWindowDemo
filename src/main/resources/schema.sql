CREATE TABLE IF NOT EXISTS products(
    id  bigserial PRIMARY KEY,
    name    VARCHAR(255)   NOT NULL,
    description VARCHAR(255) NOT NULL,
    price DECIMAL(15, 2) NOT NULL DEFAULT 10000.0,
    is_available BOOLEAN NOT NULL DEFAULT true,
    image_url VARCHAR(255)
);