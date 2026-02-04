INSERT INTO category (name, created_at, created_by, modified_at, modified_by)
VALUES
('PIANO', NOW(), 'system', NULL, NULL),
('GUITAR', NOW(), 'system', NULL, NULL),
('VIOLIN', NOW(), 'system', NULL, NULL),
('DRUM', NOW(), 'system', NULL, NULL),
('VOCAL', NOW(), 'system', '2026-02-01 23:30:00', 'system');

INSERT INTO subject (category_id, name, created_at, created_by, modified_at, modified_by)
VALUES
(1, 'PIANO_CLASSICAL', NOW(), 'admin', NULL, NULL),
(1, 'PIANO_JAZZ', NOW(), 'admin', NULL, NULL),
(1, 'PIANO_NEWAGE', NOW(), 'admin', NULL, NULL),
(3, 'VIOLIN_CLASSICAL', NOW(), 'admin', NULL, NULL),
(3, 'VIOLIN_JAZZ', NOW(), 'system', NULL, NULL),
(2, 'GUITAR_CLASSICAL', NOW(), 'admin', NULL, NULL),
(2, 'GUITAR_ACOUSTIC', NOW(), 'manager', NULL, NULL),
(2, 'GUITAR_BASS', NOW(), 'manager', NULL, NULL),
(4, 'DRUM_POP', NOW(), 'system', NULL, NULL),
(5, 'VOCAL_CLASSICAL', NOW(), 'admin', NULL, NULL),
(5, 'VOCAL_POP', NOW(), 'admin', NULL, NULL),
(6, 'COMPOSITION_MIDI', NOW(), 'admin', NULL, NULL),
(6, 'COMPOSITION_CLASSICAL', NOW(), 'admin', NULL, NULL);

INSERT INTO user_account (user_id, user_password, role, gender, birth_date, phone_number, email, created_at, created_by, modified_at, modified_by)
VALUES
('tutor_kim', 'pass1234', 'TUTOR', 'MALE', '1990-05-15', '010-1234-5678', 'kim_piano@example.com', NOW(), 'system', NULL, NULL),
('tutor_lee', 'pass5678', 'TUTOR', 'FEMALE', '1992-08-20', '010-9876-5432', 'lee_violin@example.com', NOW(), 'system', NULL, NULL),
('student_park', 'stud1234', 'STUDENT', 'MALE', '2005-03-10', '010-1111-2222', 'park_student@example.com', NOW(), 'system', NULL, NULL),
('student_choi', 'stud5678', 'STUDENT', 'FEMALE', '2008-11-25', '010-3333-4444', 'choi_student@example.com', NOW(), 'system', NULL, NULL),
('asdfawe', 'fghkfgj', 'TUTOR', 'MALE', '2002-05-15', '010-1111-1111', 'asdf_piano@example.com', NOW(), 'system', NULL, NULL),
('asdgasd', 'dftjdfgh', 'TUTOR', 'FEMALE', '2021-08-20', '010-2222-2222', 'sdfg_violin@example.com', NOW(), 'system', NULL, NULL),
('sdfjhdsfg', 'kstsrtghsr', 'STUDENT', 'MALE', '2021-03-10', '010-3333-3333', 'fghj_student@example.com', NOW(), 'system', NULL, NULL),
('arjhdfgasdf', 'sfjkdfgh', 'STUDENT', 'FEMALE', '2019-11-25', '010-4444-4444', 'hjkl_student@example.com', NOW(), 'system', NULL, NULL);

INSERT INTO tutor_account (tutor_id, introduction, career, created_at, created_by, modified_at, modified_by)
VALUES
(1, '안녕하세요, 10년 경력의 클래식 피아노 전문 튜터 김피노입니다. 기초부터 입시까지 체계적으로 가르쳐 드립니다.',
'한예종 피아노과 학사 졸업\n중앙음악콩쿠르 1위\n현) OO음악학원 전임 강사',
 NOW(), 'system', NULL, NULL),
(2, '바이올린의 아름다운 선율을 함께 찾아가는 이바올입니다. 초보자분들도 즐겁게 배우실 수 있습니다.',
 '독일 베를린 국립음대 석사\n시립교향악단 객원 단원 역임\n개인 레슨 경력 7년',
 NOW(), 'system', NULL, NULL);

INSERT INTO lesson_post (tutor_id, title, location, content, created_at, created_by, modified_at, modified_by)
VALUES
(1, '체계적인 피아노 레슨', '서울 강남구/서초구',
 '안녕하세요! 기초가 부족한 분들부터 음대 입시를 준비하는 학생까지 맞춤형으로 지도합니다. 클래식 피아노를 중심으로 테크닉과 음악성을 동시에 잡아드립니다. 연습실 대여 상담도 가능합니다.',
 NOW(), 'tutor_kim', NULL, NULL),
(2, '직장인/성인 취미 바이올린', '서울 송파구/경기 성남',
 '악기를 처음 잡아보시는 분들도 환영합니다! 퇴근 후 힐링할 수 있는 취미 시간을 만들어 드릴게요. 기초 이론부터 평소 좋아하던 곡 연주까지 차근차근 도와드립니다.',
 NOW(), 'tutor_lee', NULL, NULL);

INSERT INTO post_subject(post_id, subject_id)
VALUES
(1, 1),
(1, 2),
(2, 3),
(2, 4);

INSERT INTO student_account (student_id, introduction, location, created_at, created_by, modified_at, modified_by)
VALUES
(3, '올해 음대 입시를 준비하고 있는 고3 학생입니다. 클래식 피아노 기초를 더 탄탄하게 다지고 싶어요.',
 '서울시 강남구', NOW(), 'system', NULL, NULL),
(4, '직장인 취미로 바이올린을 배우고 싶습니다. 퇴근 후에 레슨이 가능한 선생님을 찾고 있어요!',
 '경기도 성남시', NOW(), 'system', NULL, NULL);

INSERT INTO matching (student_id, post_id, request_msg, status, created_at, created_by, modified_at, modified_by)
VALUES
(3, 1, '선생님 안녕하세요! 입시 준비 중인데 주말 오전 수업이 가능할까요?', 'PENDING', NOW(), 'student_park', NULL, NULL),
(4, 2, '취미로 시작해보려 합니다. 악기가 없는데 대여도 가능할까요?', 'PENDING', NOW(), 'student_choi', NULL, NULL);

INSERT INTO lesson_review (matching_id, content, rating, created_at, created_by, modified_at, modified_by)
VALUES
(1, '입시 준비로 막막했는데 기초부터 테크닉까지 정말 꼼꼼하게 봐주셨어요. 덕분에 자신감이 생겼습니다!',
 5.0, NOW(), 'student_park', NULL, NULL),
(2, '초보자 눈높이에 맞춰서 친절하게 설명해 주셔서 즐겁게 배우고 있습니다. 퇴근 후 힐링 시간이에요.',
 4.8, NOW(), 'student_choi', NULL, NULL);