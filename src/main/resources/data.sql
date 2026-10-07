-- ========================================================
-- FreelanceHub Initial Seed Data
-- Database: freelancer_management
-- Only the skills catalog is pre-seeded.
-- Users, freelancers, clients, and projects are created by users.
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

UPDATE users
SET profile_image = NULL
WHERE profile_image = 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150';
