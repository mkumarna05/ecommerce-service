

INSERT INTO users (username, email, password, role) VALUES
('admin', 'admin@test.com', '$2a$10$rqY5YhxXZKJvLYJvJc5mF.7nIXZj5S8jS7pPWvqhZ1EjZX4LLTxiS', 'ADMIN'),
('premium', 'premium@test.com', '$2a$10$rqY5YhxXZKJvLYJvJc5mF.7nIXZj5S8jS7pPWvqhZ1EjZX4LLTxiS', 'PREMIUM_USER'),
('user', 'user@test.com', '$2a$10$rqY5YhxXZKJvLYJvJc5mF.7nIXZj5S8jS7pPWvqhZ1EjZX4LLTxiS', 'USER');

INSERT INTO products (name, description, price, quantity, deleted) VALUES
('Laptop', 'High performance laptop with 16GB RAM and 512GB SSD', 999.99, 10, FALSE),
('Mouse', 'Wireless ergonomic mouse with USB receiver', 29.99, 50, FALSE),
('Keyboard', 'Mechanical keyboard with RGB backlighting', 79.99, 30, FALSE),
('Monitor', '4K Ultra HD 27 inch monitor with HDR support', 599.99, 15, FALSE),
('Headphones', 'Noise cancelling wireless headphones', 199.99, 25, FALSE),
('Webcam', '1080p HD webcam with built-in microphone', 89.99, 20, FALSE),
('USB Hub', '7-port USB 3.0 hub with power adapter', 39.99, 40, FALSE),
('SSD', '1TB NVMe M.2 SSD with 3500MB/s read speed', 149.99, 35, FALSE),
('RAM', '16GB DDR4 3200MHz memory kit (2x8GB)', 79.99, 45, FALSE),
('Graphics Card', 'NVIDIA RTX 4060 8GB GDDR6', 399.99, 8, FALSE);