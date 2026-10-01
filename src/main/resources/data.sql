-- ========================================================
-- FreelanceHub Initial Seed Data
-- Database: freelancer_management
-- Only skills catalog and default admin are provided.
-- Freelancers, clients, and projects are empty by default
-- and must be registered/created by users themselves.
-- ========================================================

-- Insert Skills Catalog
INSERT IGNORE INTO skills (id, name) VALUES
(1, 'Java'),
(2, 'Python'),
(3, 'React'),
(4, 'JavaScript'),
(5, 'SQL'),
(6, 'Machine Learning'),
(7, 'Artificial Intelligence'),
(8, 'Spring Boot'),
(9, 'UI/UX'),
(10, 'Node.js'),
(11, 'Docker'),
(12, 'Flutter'),
(13, 'AWS'),
(14, 'TypeScript'),
(15, 'Next.js');

-- System Administrator Account (Password: admin123)
INSERT IGNORE INTO users (id, name, email, password, role, profile_image, status) VALUES
(1, 'System Administrator', 'admin@freelancehub.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', NULL, 'ACTIVE');

UPDATE users
SET profile_image = NULL
WHERE profile_image = 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150';
