-- Sample seed data for initial testing
INSERT INTO books (isbn, title, author, category, price, stock_quantity, description, cover_image_url, created_at, updated_at)
VALUES 
('9780134685991', 'Effective Java (3rd Edition)', 'Joshua Bloch', 'Technology', 45.00, 20, 'The definitive guide to Java platform best practices.', 'https://example.com/effective-java.jpg', NOW(), NOW()),
('9780132350884', 'Clean Code', 'Robert C. Martin', 'Technology', 39.99, 15, 'A handbook of agile software craftsmanship.', 'https://example.com/clean-code.jpg', NOW(), NOW()),
('9780321125217', 'Domain-Driven Design', 'Eric Evans', 'Technology', 55.50, 10, 'Tackling complexity in the heart of software.', 'https://example.com/ddd.jpg', NOW(), NOW()),
('9780201633610', 'Design Patterns', 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', 'Technology', 49.99, 8, 'Elements of Reusable Object-Oriented Software.', 'https://example.com/design-patterns.jpg', NOW(), NOW()),
('9781449373320', 'Designing Data-Intensive Applications', 'Martin Kleppmann', 'Technology', 42.00, 25, 'The big ideas behind reliable, scalable, and maintainable systems.', 'https://example.com/ddia.jpg', NOW(), NOW());

INSERT INTO customers (email, first_name, last_name, phone, address_line1, address_line2, city, postal_code, country, created_at, updated_at)
VALUES 
('john.doe@example.com', 'John', 'Doe', '+1-555-123-4567', '123 Main St', 'Apt 4B', 'New York', '10001', 'USA', NOW(), NOW()),
('jane.smith@example.com', 'Jane', 'Smith', '+1-555-987-6543', '456 Market St', '', 'San Francisco', '94105', 'USA', NOW(), NOW());
