-- Script để migrate dữ liệu từ bảng tiếng Anh sang bảng tiếng Việt
-- Chạy script này TRƯỚC khi xóa các bảng cũ
-- Lưu ý: Chỉ chạy nếu bạn muốn giữ lại dữ liệu cũ

BEGIN;

-- 1. Migrate users -> nguoi_dung
INSERT INTO nguoi_dung (id, ho_ten, email, mat_khau, vai_tro, ngay_tao, trang_thai)
SELECT id, full_name, email, password_hash, role, created_at, is_active
FROM users
ON CONFLICT (id) DO NOTHING;

-- 2. Migrate subjects -> mon_hoc
INSERT INTO mon_hoc (id, ten_mon, mo_ta, ngay_tao, trang_thai)
SELECT id, name, description, created_at, is_active
FROM subjects
ON CONFLICT (id) DO NOTHING;

-- 3. Migrate chapters -> chuong
INSERT INTO chuong (id, ma_mon, ten_chuong, mo_ta, ngay_tao)
SELECT id, subject_id, name, description, created_at
FROM chapters
ON CONFLICT (id) DO NOTHING;

-- 4. Migrate student_groups -> nhom_sinh_vien
INSERT INTO nhom_sinh_vien (id, ten_nhom, ngay_tao)
SELECT id, name, created_at
FROM student_groups
ON CONFLICT (id) DO NOTHING;

-- 5. Migrate class_student -> lop_sinh_vien
INSERT INTO lop_sinh_vien (ma_nhom, ma_sinh_vien)
SELECT student_group_id, student_id
FROM class_student
ON CONFLICT (ma_nhom, ma_sinh_vien) DO NOTHING;

-- 6. Migrate student_group_subjects -> nhom_mon_hoc
INSERT INTO nhom_mon_hoc (ma_nhom, ma_mon, ma_giao_vien, ngay_phan_cong)
SELECT student_group_id, subject_id, teacher_id, assigned_at
FROM student_group_subjects
ON CONFLICT (ma_nhom, ma_mon) DO NOTHING;

-- 7. Migrate passages -> doan_van
INSERT INTO doan_van (id, ma_chuong, noi_dung, ngay_tao)
SELECT id, chapter_id, content, created_at
FROM passages
ON CONFLICT (id) DO NOTHING;

-- 8. Migrate questions -> cau_hoi
INSERT INTO cau_hoi (id, ma_chuong, ma_doan_van, noi_dung, loai_cau_hoi, do_kho, nguoi_tao, ngay_tao, trang_thai)
SELECT id, chapter_id, passage_id, content, question_type, difficulty, created_by, created_at, is_active
FROM questions
ON CONFLICT (id) DO NOTHING;

-- 9. Migrate question_options -> lua_chon
INSERT INTO lua_chon (id, ma_cau_hoi, noi_dung, dap_an_dung, thu_tu)
SELECT id, question_id, content, is_correct, option_order
FROM question_options
ON CONFLICT (id) DO NOTHING;

-- 10. Migrate question_answers -> dap_an_cau_hoi
INSERT INTO dap_an_cau_hoi (id, ma_cau_hoi, dap_an_dung, ngay_tao)
SELECT id, question_id, correct_answer, created_at
FROM question_answers
ON CONFLICT (id) DO NOTHING;

-- 11. Migrate exam_templates -> khung_de_thi
INSERT INTO khung_de_thi (id, ma_mon, ten_de, tong_so_cau, nguoi_tao, ngay_tao)
SELECT id, subject_id, name, total_questions, created_by, created_at
FROM exam_templates
ON CONFLICT (id) DO NOTHING;

-- 12. Migrate exam_structure -> cau_truc_de
INSERT INTO cau_truc_de (id, ma_khung_de, ma_chuong, so_cau, so_cau_co_ban, so_cau_nang_cao)
SELECT id, template_id, chapter_id, num_question, num_basic, num_advanced
FROM exam_structure
ON CONFLICT (id) DO NOTHING;

-- 13. Migrate exam_instances -> ky_thi
INSERT INTO ky_thi (id, ma_khung_de, ten_ky_thi, ma_nhom, thoi_gian_bat_dau, thoi_gian_ket_thuc, thoi_luong_phut, tron_cau_hoi, tron_dap_an, tong_diem, ngay_tao)
SELECT id, template_id, name, student_group_id, start_time, end_time, duration_minutes, shuffle_questions, shuffle_options, total_marks, created_at
FROM exam_instances
ON CONFLICT (id) DO NOTHING;

-- 14. Migrate exam_supervisors -> giam_thi
INSERT INTO giam_thi (id, ma_ky_thi, ma_giam_thi, ngay_phan_cong)
SELECT id, exam_instance_id, supervisor_id, assigned_at
FROM exam_supervisors
ON CONFLICT (id) DO NOTHING;

-- 15. Migrate exam_attempts -> bai_lam
INSERT INTO bai_lam (id, ma_ky_thi, ma_sinh_vien, thoi_gian_bat_dau, thoi_gian_nop_bai, diem_so, trang_thai)
SELECT id, exam_instance_id, student_id, started_at, submitted_at, score, status
FROM exam_attempts
ON CONFLICT (id) DO NOTHING;

-- 16. Migrate exam_questions -> cau_hoi_ky_thi
INSERT INTO cau_hoi_ky_thi (ma_ky_thi, ma_cau_hoi, thu_tu)
SELECT exam_instance_id, question_id, question_order
FROM exam_questions
ON CONFLICT (ma_ky_thi, ma_cau_hoi) DO NOTHING;

-- 17. Migrate exam_answers -> dap_an_bai_lam
INSERT INTO dap_an_bai_lam (id, ma_bai_lam, ma_cau_hoi, ma_lua_chon, dap_an_dien, dap_an_dung, thoi_gian_tra_loi)
SELECT id, attempt_id, question_id, selected_option_id, fill_answer, is_correct, answered_at
FROM exam_answers
ON CONFLICT (id) DO NOTHING;

-- 18. Migrate exam_violations -> vi_pham
INSERT INTO vi_pham (id, ma_bai_lam, loai_vi_pham, so_lan_vi_pham, lan_cuoi_xay_ra, ngay_tao)
SELECT id, attempt_id, violation_type, violation_count, last_occurred_at, created_at
FROM exam_violations
ON CONFLICT (id) DO NOTHING;

-- Cập nhật sequence cho các bảng mới (nếu cần)
-- SELECT setval('nguoi_dung_id_seq', (SELECT MAX(id) FROM nguoi_dung));
-- SELECT setval('mon_hoc_id_seq', (SELECT MAX(id) FROM mon_hoc));
-- ... (lặp lại cho tất cả các bảng)

COMMIT;

