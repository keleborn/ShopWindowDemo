CREATE TABLE IF NOT EXISTS products(
    id  bigserial PRIMARY KEY,
    name    VARCHAR(255)   NOT NULL,
    description VARCHAR(255) NOT NULL,
    price DECIMAL(15, 2) NOT NULL DEFAULT 10000.0,
    is_available BOOLEAN NOT NULL DEFAULT true,
    image_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS orders(
    id  bigserial PRIMARY KEY,
    customerName    VARCHAR(255)   NOT NULL,
    createdAt DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS order_items(
    id  bigserial PRIMARY KEY,
    orderId INT NOT NULL,
    productName VARCHAR(255) NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    quantity INT NOT NULL,
    image_url VARCHAR(255)
);