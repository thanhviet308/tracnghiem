-- ============================================================
-- SCRIPT TỔNG HỢP: MIGRATE DỮ LIỆU VÀ XÓA BẢNG TIẾNG ANH
-- ============================================================
-- Script này sẽ:
-- 1. Migrate tất cả dữ liệu từ bảng tiếng Anh sang bảng tiếng Việt
-- 2. Xóa các bảng tiếng Anh cũ
--
-- LƯU Ý: Hãy backup database trước khi chạy script này!
-- ============================================================

BEGIN;

-- ============================================================
-- PHẦN 1: MIGRATE DỮ LIỆU
-- ============================================================

-- 1. Migrate users -> nguoi_dung
INSERT INTO nguoi_dung (id, ho_ten, email, mat_khau, vai_tro, ngay_tao, trang_thai)
SELECT id, full_name, email, password_hash, role, created_at, is_active
FROM users
WHERE NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE nguoi_dung.id = users.id);

-- 2. Migrate subjects -> mon_hoc
INSERT INTO mon_hoc (id, ten_mon, mo_ta, ngay_tao, trang_thai)
SELECT id, name, description, created_at, is_active
FROM subjects
WHERE NOT EXISTS (SELECT 1 FROM mon_hoc WHERE mon_hoc.id = subjects.id);

-- 3. Migrate chapters -> chuong
INSERT INTO chuong (id, ma_mon, ten_chuong, mo_ta, ngay_tao)
SELECT id, subject_id, name, description, created_at
FROM chapters
WHERE NOT EXISTS (SELECT 1 FROM chuong WHERE chuong.id = chapters.id);

-- 4. Migrate student_groups -> nhom_sinh_vien
INSERT INTO nhom_sinh_vien (id, ten_nhom, ngay_tao)
SELECT id, name, created_at
FROM student_groups
WHERE NOT EXISTS (SELECT 1 FROM nhom_sinh_vien WHERE nhom_sinh_vien.id = student_groups.id);

-- 5. Migrate class_student -> lop_sinh_vien
INSERT INTO lop_sinh_vien (ma_nhom, ma_sinh_vien)
SELECT student_group_id, student_id
FROM class_student
WHERE NOT EXISTS (
    SELECT 1 FROM lop_sinh_vien 
    WHERE lop_sinh_vien.ma_nhom = class_student.student_group_id 
    AND lop_sinh_vien.ma_sinh_vien = class_student.student_id
);

-- 6. Migrate student_group_subjects -> nhom_mon_hoc
INSERT INTO nhom_mon_hoc (ma_nhom, ma_mon, ma_giao_vien, ngay_phan_cong)
SELECT student_group_id, subject_id, teacher_id, assigned_at
FROM student_group_subjects
WHERE NOT EXISTS (
    SELECT 1 FROM nhom_mon_hoc 
    WHERE nhom_mon_hoc.ma_nhom = student_group_subjects.student_group_id 
    AND nhom_mon_hoc.ma_mon = student_group_subjects.subject_id
);

-- 7. Migrate passages -> doan_van
INSERT INTO doan_van (id, ma_chuong, noi_dung, ngay_tao)
SELECT id, chapter_id, content, created_at
FROM passages
WHERE NOT EXISTS (SELECT 1 FROM doan_van WHERE doan_van.id = passages.id);

-- 8. Migrate questions -> cau_hoi
INSERT INTO cau_hoi (id, ma_chuong, ma_doan_van, noi_dung, loai_cau_hoi, do_kho, nguoi_tao, ngay_tao, trang_thai)
SELECT id, chapter_id, passage_id, content, question_type, difficulty, created_by, created_at, is_active
FROM questions
WHERE NOT EXISTS (SELECT 1 FROM cau_hoi WHERE cau_hoi.id = questions.id);

-- 9. Migrate question_options -> lua_chon
INSERT INTO lua_chon (id, ma_cau_hoi, noi_dung, dap_an_dung, thu_tu)
SELECT id, question_id, content, is_correct, option_order
FROM question_options
WHERE NOT EXISTS (SELECT 1 FROM lua_chon WHERE lua_chon.id = question_options.id);

-- 10. Migrate question_answers -> dap_an_cau_hoi
INSERT INTO dap_an_cau_hoi (id, ma_cau_hoi, dap_an_dung, ngay_tao)
SELECT id, question_id, correct_answer, created_at
FROM question_answers
WHERE NOT EXISTS (SELECT 1 FROM dap_an_cau_hoi WHERE dap_an_cau_hoi.id = question_answers.id);

-- 11. Migrate exam_templates -> khung_de_thi
INSERT INTO khung_de_thi (id, ma_mon, ten_de, tong_so_cau, nguoi_tao, ngay_tao)
SELECT id, subject_id, name, total_questions, created_by, created_at
FROM exam_templates
WHERE NOT EXISTS (SELECT 1 FROM khung_de_thi WHERE khung_de_thi.id = exam_templates.id);

-- 12. Migrate exam_structure -> cau_truc_de
INSERT INTO cau_truc_de (id, ma_khung_de, ma_chuong, so_cau, so_cau_co_ban, so_cau_nang_cao)
SELECT id, template_id, chapter_id, num_question, num_basic, num_advanced
FROM exam_structure
WHERE NOT EXISTS (SELECT 1 FROM cau_truc_de WHERE cau_truc_de.id = exam_structure.id);

-- 13. Migrate exam_instances -> ky_thi
INSERT INTO ky_thi (id, ma_khung_de, ten_ky_thi, ma_nhom, thoi_gian_bat_dau, thoi_gian_ket_thuc, thoi_luong_phut, tron_cau_hoi, tron_dap_an, tong_diem, ngay_tao)
SELECT id, template_id, name, student_group_id, start_time, end_time, duration_minutes, shuffle_questions, shuffle_options, total_marks, created_at
FROM exam_instances
WHERE NOT EXISTS (SELECT 1 FROM ky_thi WHERE ky_thi.id = exam_instances.id);

-- 14. Migrate exam_supervisors -> giam_thi
INSERT INTO giam_thi (id, ma_ky_thi, ma_giam_thi, ngay_phan_cong)
SELECT id, exam_instance_id, supervisor_id, assigned_at
FROM exam_supervisors
WHERE NOT EXISTS (SELECT 1 FROM giam_thi WHERE giam_thi.id = exam_supervisors.id);

-- 15. Migrate exam_attempts -> bai_lam
INSERT INTO bai_lam (id, ma_ky_thi, ma_sinh_vien, thoi_gian_bat_dau, thoi_gian_nop_bai, diem_so, trang_thai)
SELECT id, exam_instance_id, student_id, started_at, submitted_at, score, status
FROM exam_attempts
WHERE NOT EXISTS (SELECT 1 FROM bai_lam WHERE bai_lam.id = exam_attempts.id);

-- 16. Migrate exam_questions -> cau_hoi_ky_thi
INSERT INTO cau_hoi_ky_thi (ma_ky_thi, ma_cau_hoi, thu_tu)
SELECT exam_instance_id, question_id, question_order
FROM exam_questions
WHERE NOT EXISTS (
    SELECT 1 FROM cau_hoi_ky_thi 
    WHERE cau_hoi_ky_thi.ma_ky_thi = exam_questions.exam_instance_id 
    AND cau_hoi_ky_thi.ma_cau_hoi = exam_questions.question_id
);

-- 17. Migrate exam_answers -> dap_an_bai_lam
INSERT INTO dap_an_bai_lam (id, ma_bai_lam, ma_cau_hoi, ma_lua_chon, dap_an_dien, dap_an_dung, thoi_gian_tra_loi)
SELECT id, attempt_id, question_id, selected_option_id, fill_answer, is_correct, answered_at
FROM exam_answers
WHERE NOT EXISTS (SELECT 1 FROM dap_an_bai_lam WHERE dap_an_bai_lam.id = exam_answers.id);

-- 18. Migrate exam_violations -> vi_pham
INSERT INTO vi_pham (id, ma_bai_lam, loai_vi_pham, so_lan_vi_pham, lan_cuoi_xay_ra, ngay_tao)
SELECT id, attempt_id, violation_type, violation_count, last_occurred_at, created_at
FROM exam_violations
WHERE NOT EXISTS (SELECT 1 FROM vi_pham WHERE vi_pham.id = exam_violations.id);

-- Cập nhật sequence cho các bảng mới
DO $$
DECLARE
    max_id BIGINT;
BEGIN
    -- Cập nhật sequence cho nguoi_dung
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM nguoi_dung;
    EXECUTE format('SELECT setval(''nguoi_dung_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho mon_hoc
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM mon_hoc;
    EXECUTE format('SELECT setval(''mon_hoc_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho chuong
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM chuong;
    EXECUTE format('SELECT setval(''chuong_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho nhom_sinh_vien
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM nhom_sinh_vien;
    EXECUTE format('SELECT setval(''nhom_sinh_vien_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho doan_van
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM doan_van;
    EXECUTE format('SELECT setval(''doan_van_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho cau_hoi
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM cau_hoi;
    EXECUTE format('SELECT setval(''cau_hoi_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho lua_chon
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM lua_chon;
    EXECUTE format('SELECT setval(''lua_chon_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho dap_an_cau_hoi
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM dap_an_cau_hoi;
    EXECUTE format('SELECT setval(''dap_an_cau_hoi_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho khung_de_thi
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM khung_de_thi;
    EXECUTE format('SELECT setval(''khung_de_thi_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho cau_truc_de
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM cau_truc_de;
    EXECUTE format('SELECT setval(''cau_truc_de_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho ky_thi
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM ky_thi;
    EXECUTE format('SELECT setval(''ky_thi_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho giam_thi
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM giam_thi;
    EXECUTE format('SELECT setval(''giam_thi_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho bai_lam
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM bai_lam;
    EXECUTE format('SELECT setval(''bai_lam_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho dap_an_bai_lam
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM dap_an_bai_lam;
    EXECUTE format('SELECT setval(''dap_an_bai_lam_id_seq'', %s)', max_id);
    
    -- Cập nhật sequence cho vi_pham
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM vi_pham;
    EXECUTE format('SELECT setval(''vi_pham_id_seq'', %s)', max_id);
END $$;

-- ============================================================
-- PHẦN 2: XÓA CÁC BẢNG TIẾNG ANH CŨ
-- ============================================================

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

-- ============================================================
-- KIỂM TRA KẾT QUẢ
-- ============================================================

-- Kiểm tra xem còn bảng tiếng Anh nào không
DO $$
DECLARE
    old_table_count INTEGER;
    new_table_count INTEGER;
BEGIN
    -- Đếm bảng tiếng Anh còn lại
    SELECT COUNT(*) INTO old_table_count
    FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name IN (
        'users', 'subjects', 'chapters', 'student_groups', 'class_student',
        'student_group_subjects', 'passages', 'questions', 'question_options',
        'question_answers', 'exam_templates', 'exam_structure', 'exam_instances',
        'exam_supervisors', 'exam_attempts', 'exam_questions', 'exam_answers',
        'exam_violations'
    );
    
    -- Đếm bảng tiếng Việt
    SELECT COUNT(*) INTO new_table_count
    FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name IN (
        'nguoi_dung', 'mon_hoc', 'chuong', 'nhom_sinh_vien', 'lop_sinh_vien',
        'nhom_mon_hoc', 'doan_van', 'cau_hoi', 'lua_chon',
        'dap_an_cau_hoi', 'khung_de_thi', 'cau_truc_de', 'ky_thi',
        'giam_thi', 'bai_lam', 'cau_hoi_ky_thi', 'dap_an_bai_lam',
        'vi_pham'
    );
    
    RAISE NOTICE 'Số bảng tiếng Anh còn lại: %', old_table_count;
    RAISE NOTICE 'Số bảng tiếng Việt: %', new_table_count;
    
    IF old_table_count = 0 THEN
        RAISE NOTICE '✅ Đã xóa thành công tất cả bảng tiếng Anh!';
    ELSE
        RAISE WARNING '⚠️ Vẫn còn % bảng tiếng Anh chưa được xóa!', old_table_count;
    END IF;
END $$;

