-- Script để xóa các bảng tiếng Anh cũ sau khi đã đổi tên sang tiếng Việt
-- Chạy script này sau khi đã đảm bảo dữ liệu đã được migrate sang bảng mới

-- Lưu ý: Script này sẽ XÓA VĨNH VIỄN các bảng cũ và dữ liệu trong đó
-- Hãy backup database trước khi chạy!

BEGIN;

-- Xóa các bảng theo thứ tự để tránh lỗi foreign key constraint

-- 1. Xóa các bảng liên quan đến exam
DROP TABLE IF EXISTS exam_violations CASCADE;
DROP TABLE IF EXISTS exam_answers CASCADE;
DROP TABLE IF EXISTS exam_questions CASCADE;
DROP TABLE IF EXISTS exam_attempts CASCADE;
DROP TABLE IF EXISTS exam_supervisors CASCADE;
DROP TABLE IF EXISTS exam_instances CASCADE;
DROP TABLE IF EXISTS exam_structure CASCADE;
DROP TABLE IF EXISTS exam_templates CASCADE;

-- 2. Xóa các bảng liên quan đến question
DROP TABLE IF EXISTS question_answers CASCADE;
DROP TABLE IF EXISTS question_options CASCADE;
DROP TABLE IF EXISTS questions CASCADE;
DROP TABLE IF EXISTS passages CASCADE;

-- 3. Xóa các bảng liên quan đến group
DROP TABLE IF EXISTS student_group_subjects CASCADE;
DROP TABLE IF EXISTS class_student CASCADE;
DROP TABLE IF EXISTS student_groups CASCADE;

-- 4. Xóa các bảng liên quan đến subject
DROP TABLE IF EXISTS chapters CASCADE;
DROP TABLE IF EXISTS subjects CASCADE;

-- 5. Xóa bảng users (cuối cùng vì nhiều bảng khác reference đến nó)
DROP TABLE IF EXISTS users CASCADE;

COMMIT;

-- Kiểm tra xem còn bảng nào không
-- SELECT table_name 
-- FROM information_schema.tables 
-- WHERE table_schema = 'public' 
-- AND table_name IN (
--     'users', 'subjects', 'chapters', 'student_groups', 'class_student',
--     'student_group_subjects', 'passages', 'questions', 'question_options',
--     'question_answers', 'exam_templates', 'exam_structure', 'exam_instances',
--     'exam_supervisors', 'exam_attempts', 'exam_questions', 'exam_answers',
--     'exam_violations'
-- );

