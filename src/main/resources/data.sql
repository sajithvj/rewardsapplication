INSERT INTO TESTDB (FIRST_NAME,LAST_NAME) VALUES ('Alice','Job');

INSERT INTO transaction (transaction_id, customer_id, customer_name, amount, transaction_date) VALUES
('T0001', 'C001', 'Alice Job', 120.00, '2026-07-03'),
('T0002', 'C001', 'Alice Job', 75.50, '2026-07-20'),
('T0003', 'C001', 'Alice Job', 45.00, '2026-07-08'),
('T0004', 'C001', 'Alice Job',200.00, '2026-07-22'),
('T0005', 'C001', 'Alice Job',99.99, '2026-08-14'),
('T0006', 'C002', 'Sonu Venugopal',50.00, '2026-07-05'),
('T0007', 'C002', 'Sonu Venugopal',150.75, '2026-08-02'),
('T0008', 'C003', 'Priya Sharma',310.00, '2026-07-11'),
('T0009', 'C003', 'Priya Sharma',260.40, '2026-07-17'),
('T00010','C003', 'Priya Sharma',180.00, '2026-08-29'),
('T00011','C004', 'David John',30.00, '2026-07-09'),
('T00012','C004', 'David John',49.99, '2026-07-30'),
('T00013','C005', 'Nirmal Xavier', 120.00, '2026-07-09'),
('T00014','C005', 'Nirmal Xavier', 60.00, '2026-08-09'),
('T00015','C005', 'Nirmal Xavier', 52.00, '2026-09-03'),
('T00016','C006', 'Ravi Kumar', 80.00, '2026-07-15'),
('T00017','C006', 'Ravi Kumar', 95.50, '2026-08-05'),
('T00018','C007', 'Anita Desai', 150.00, '2026-07-20'),
('T00019','C007', 'Anita Desai', 200.00, '2026-08-10'),
('T00020','C008', 'Kiran Rao', 75.00, '2026-07-25'),
('T00021','C008', 'Kiran Rao', 120.00, '2026-08-15'),
('T00022','C009', 'Suresh Menon', 60.00, '2026-07-30'),
('T00023','C009', 'Suresh Menon', 90.00, '2026-08-20'),
('T00024','C010', 'Meera Nair', 110.00, '2026-07-12'),
('T00025','C010', 'Meera Nair', 130.00, '2026-08-02');

INSERT INTO users (username, password, enabled) VALUES
('admin', '$2b$10$OdHOzt2ndmfO5J3KAz5jpuBaO/xLIRa/R9.usDPNXvhP039zcRcoe', true),
('user1', '$2b$10$9ZqryW.VsG0kByR3I99Bvu6F3wnh/HaL4xe4FAZPu.zLkWze/RSh2', true),
('user2', '$2b$10$fwIy6D2/rLWLY1qDbAaZHOnE8tizKMihIsQW.4rvj6CEu.aVuz2pe', true);

INSERT INTO roles (name) VALUES
('ROLE_ADMIN'),
('ROLE_USER');

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 2);