INSERT IGNORE INTO category (name, display_order, created_at, created_by)
VALUES
('PIANO', 1, NOW(), 'SYSTEM'),
('VIOLIN', 2, NOW(), 'SYSTEM'),
('CELLO', 3, NOW(), 'SYSTEM'),
('GUITAR', 4, NOW(), 'SYSTEM'),
('DRUM', 5, NOW(), 'SYSTEM'),
('VOCAL', 6, NOW(), 'SYSTEM'),
('COMPOSITION', 7, NOW(), 'SYSTEM');

INSERT IGNORE INTO subject (category_id, name, display_order, created_at, created_by)
VALUES
(1, 'PIANO_CLASSICAL', 1, NOW(), 'SYSTEM'),
(1, 'PIANO_JAZZ', 2, NOW(), 'SYSTEM'),
(1, 'PIANO_NEWAGE', 3, NOW(), 'SYSTEM'),
(1, 'PIANO_POP', 4, NOW(), 'SYSTEM'),
(1, 'PIANO_CCM', 5, NOW(), 'SYSTEM'),
(2, 'VIOLIN_CLASSICAL', 1, NOW(), 'SYSTEM'),
(2, 'VIOLIN_JAZZ', 2, NOW(), 'SYSTEM'),
(2, 'VIOLIN_POP', 3, NOW(), 'SYSTEM'),
(3, 'CELLO_CLASSICAL', 1, NOW(), 'SYSTEM'),
(3, 'CELLO_JAZZ', 2, NOW(), 'SYSTEM'),
(3, 'CELLO_POP', 3, NOW(), 'SYSTEM'),
(4, 'GUITAR_CLASSICAL', 1, NOW(), 'SYSTEM'),
(4, 'GUITAR_ACOUSTIC', 2, NOW(), 'SYSTEM'),
(4, 'GUITAR_ELECTRIC', 3, NOW(), 'SYSTEM'),
(4, 'GUITAR_BASS', 4, NOW(), 'SYSTEM'),
(5, 'DRUM_POP', 1, NOW(), 'SYSTEM'),
(5, 'DRUM_JAZZ', 2, NOW(), 'SYSTEM'),
(5, 'DRUM_METAL', 3, NOW(), 'SYSTEM'),
(6, 'VOCAL_CLASSICAL', 1, NOW(), 'SYSTEM'),
(6, 'VOCAL_MUSICAL', 2, NOW(), 'SYSTEM'),
(6, 'VOCAL_JAZZ', 3, NOW(), 'SYSTEM'),
(6, 'VOCAL_POP', 4, NOW(), 'SYSTEM'),
(7, 'COMPOSITION_CLASSICAL', 1, NOW(), 'SYSTEM'),
(7, 'COMPOSITION_MIDI', 2, NOW(), 'SYSTEM'),
(7, 'COMPOSITION_JAZZ', 3, NOW(), 'SYSTEM');

DELETE FROM user_account WHERE user_id LIKE 'student%@test.com' OR user_id LIKE 'tutor%@test.com';

-- 모두 비밀번호는: testaccount123
INSERT INTO user_account (user_id, user_password, name, gender, birth_date, phone_number, email, created_at, created_by)
VALUES
('tutor1@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터1',  'MALE',   '1995-01-01', '010-1001-0001', 'tutor1@test.com',  NOW(), 'SYSTEM'),
('tutor2@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터2',  'FEMALE', '1996-02-02', '010-1002-0002', 'tutor2@test.com',  NOW(), 'SYSTEM'),
('tutor3@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터3',  'MALE',   '1997-03-03', '010-1003-0003', 'tutor3@test.com',  NOW(), 'SYSTEM'),
('tutor4@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터4',  'FEMALE', '1998-04-04', '010-1004-0004', 'tutor4@test.com',  NOW(), 'SYSTEM'),
('tutor5@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터5',  'MALE',   '1999-05-05', '010-1005-0005', 'tutor5@test.com',  NOW(), 'SYSTEM'),
('tutor6@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터6',  'MALE',   '1995-01-01', '010-1006-0006', 'tutor6@test.com',  NOW(), 'SYSTEM'),
('tutor7@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터7',  'FEMALE', '1996-02-02', '010-1007-0007', 'tutor7@test.com',  NOW(), 'SYSTEM'),
('tutor8@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터8',  'MALE',   '1997-03-03', '010-1008-0008', 'tutor8@test.com',  NOW(), 'SYSTEM'),
('tutor9@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터9',  'FEMALE', '1998-04-04', '010-1009-0009', 'tutor9@test.com',  NOW(), 'SYSTEM'),
('tutor10@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터10', 'MALE',   '1999-05-05', '010-1010-0010', 'tutor10@test.com', NOW(), 'SYSTEM'),
('tutor11@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터11', 'MALE',   '1995-01-01', '010-1011-0011', 'tutor11@test.com', NOW(), 'SYSTEM'),
('tutor12@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터12', 'FEMALE', '1996-02-02', '010-1012-0012', 'tutor12@test.com', NOW(), 'SYSTEM'),
('tutor13@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터13', 'MALE',   '1997-03-03', '010-1013-0013', 'tutor13@test.com', NOW(), 'SYSTEM'),
('tutor14@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터14', 'FEMALE', '1998-04-04', '010-1014-0014', 'tutor14@test.com', NOW(), 'SYSTEM'),
('tutor15@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터15', 'MALE',   '1999-05-05', '010-1015-0015', 'tutor15@test.com', NOW(), 'SYSTEM'),
('tutor16@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터16', 'MALE',   '1995-01-01', '010-1016-0016', 'tutor16@test.com', NOW(), 'SYSTEM'),
('tutor17@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트튜터17', 'FEMALE', '1996-02-02', '010-1017-0017', 'tutor17@test.com', NOW(), 'SYSTEM'),
('student1@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생1',  'MALE',   '1995-01-01', '010-2001-0001', 'student1@test.com',  NOW(), 'SYSTEM'),
('student2@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생2',  'FEMALE', '1996-02-02', '010-2002-0002', 'student2@test.com',  NOW(), 'SYSTEM'),
('student3@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생3',  'MALE',   '1997-03-03', '010-2003-0003', 'student3@test.com',  NOW(), 'SYSTEM'),
('student4@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생4',  'FEMALE', '1998-04-04', '010-2004-0004', 'student4@test.com',  NOW(), 'SYSTEM'),
('student5@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생5',  'MALE',   '1999-05-05', '010-2005-0005', 'student5@test.com',  NOW(), 'SYSTEM'),
('student6@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생6',  'MALE',   '1997-03-03', '010-2006-0006', 'student6@test.com',  NOW(), 'SYSTEM'),
('student7@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생7',  'FEMALE', '1998-04-04', '010-2007-0007', 'student7@test.com',  NOW(), 'SYSTEM'),
('student8@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생8',  'MALE',   '1999-05-05', '010-2008-0008', 'student8@test.com',  NOW(), 'SYSTEM'),
('student9@test.com',  '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생9',  'MALE',   '1995-01-01', '010-2009-0009', 'student9@test.com',  NOW(), 'SYSTEM'),
('student10@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생10', 'FEMALE', '1996-02-02', '010-2010-0010', 'student10@test.com', NOW(), 'SYSTEM'),
('student11@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생11', 'MALE',   '1997-03-03', '010-2011-0011', 'student11@test.com', NOW(), 'SYSTEM'),
('student12@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생12', 'FEMALE', '1998-04-04', '010-2012-0012', 'student12@test.com', NOW(), 'SYSTEM'),
('student13@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생13', 'MALE',   '1999-05-05', '010-2013-0013', 'student13@test.com', NOW(), 'SYSTEM'),
('student14@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생14', 'MALE',   '1995-01-01', '010-2014-0014', 'student14@test.com', NOW(), 'SYSTEM'),
('student15@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생15', 'FEMALE', '1996-02-02', '010-2015-0015', 'student15@test.com', NOW(), 'SYSTEM'),
('student16@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생16', 'MALE',   '1997-03-03', '010-2016-0016', 'student16@test.com', NOW(), 'SYSTEM'),
('student17@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생17', 'FEMALE', '1998-04-04', '010-2017-0017', 'student17@test.com', NOW(), 'SYSTEM'),
('student18@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생18', 'MALE',   '1999-05-05', '010-2018-0018', 'student18@test.com', NOW(), 'SYSTEM'),
('student19@test.com', '{bcrypt}$2a$10$mr58N9D/.RfsyxIhxcSXB.RWt7RE5upuOBVAsdSmcE5UgRjwQe.wm', '테스트학생19', 'MALE',   '1995-01-01', '010-2019-0019', 'student19@test.com', NOW(), 'SYSTEM');

INSERT IGNORE INTO role (role_type, created_at, created_by)
VALUES
('TUTOR', NOW(), 'SYSTEM'),
('STUDENT', NOW(), 'SYSTEM'),
('GUEST', NOW(), 'SYSTEM');

INSERT IGNORE INTO user_role (user_id, role_id, created_at, created_by)
SELECT ua.id, r.role_id, NOW(), 'SYSTEM'
FROM user_account ua
JOIN role r ON r.role_type = 'TUTOR'
WHERE ua.email LIKE 'tutor%@test.com';

-- Assign STUDENT role to all student accounts
INSERT IGNORE INTO user_role (user_id, role_id, created_at, created_by)
SELECT ua.id, r.role_id, NOW(), 'SYSTEM'
FROM user_account ua
JOIN role r ON r.role_type = 'STUDENT'
WHERE ua.email LIKE 'student%@test.com';

-- ──────────────────────────────────────────────────────────────────────────────
-- tutor_account: profile_status = COMPLETED 으로 설정
-- COMPLETED 조건: title + introduction + name 있고 goal_tutor 1개 이상 존재
-- lesson_type: OFFLINE(대면), ONLINE(온라인), BOTH(둘 다)
-- ──────────────────────────────────────────────────────────────────────────────
INSERT IGNORE INTO tutor_account (tutor_id, introduction, experiences, title, educations, average_rating, review_count, matching_count, lesson_type, profile_status, is_birth_date_public, is_email_public, is_phone_number_public, created_at, created_by)
VALUES
(1,  '기초부터 탄탄하게 가르치는 피아니스트입니다. 서울 강남구 기반으로 레슨합니다.',    '["한예종 졸업", "독일 유학 5년", "다수 콩쿠르 입상"]',          '지루한 체르니는 그만! 뉴에이지부터 클래식까지 1:1 맞춤 레슨',     '["음악대학 피아노 전공"]',         0, 0, 10, 'BOTH',    'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(2,  '바이올린의 아름다운 선율을 함께 만들어봐요. 서울 마포구 홍대 인근 레슨 가능.',     '["시립교향악단 단원 역임", "레슨 경력 10년"]',                   '초보자도 3개월 만에 한 곡 마스터! 바이올린 기초 완성',             '["한국 예술 종합학교 음악원 졸업"]', 0, 0, 1,  'OFFLINE', 'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(3,  '첼로의 깊은 울림을 전달하는 강사입니다. 직장인 위한 저녁 시간대 수업 가능.',       '["해외 음악제 초청 연주", "예술고등학교 출강"]',                  '직장인을 위한 힐링 첼로 클래스 (저녁 시간대 가능)',                '["서울대학교 음악대학 졸업"]',      0, 0, 1,  'BOTH',    'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(4,  '통기타부터 일렉기타까지, 리듬을 즐겨보세요. 경기 분당 지역 레슨 가능합니다.',       '["실용음악과 졸업", "밴드 세션 활동 중"]',                       '한 달 만에 완성하는 통기타 코드와 스트로크',                        '["실용음악학과 기타 전공 졸업"]',   0, 0, 1,  'BOTH',    'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(5,  '당신이 가진 최고의 악기, 목소리를 찾아드립니다. 인천/온라인 레슨 가능.',           '["유명 오디션 프로그램 코칭", "보컬 트레이너 경력"]',             '고음 불가 탈출! 호흡부터 발성까지 체계적인 보컬 트레이닝',          '["음악대학 성악과 졸업"]',         0, 0, 1,  'BOTH',    'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(6,  '스트레스를 한 번에 날리는 파워풀한 드럼 레슨! 경기 수원 재즈 클럽 운영 중.',       '["재즈 클럽 정기 공연", "드럼 전문 아카데미 운영"]',              '박치 탈출! 기초 비트부터 화려한 필인까지',                          '["실용음악학과 드럼 전공 졸업"]',   0, 0, 1,  'OFFLINE', 'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(7,  '음악의 뼈대, 베이스 기타의 매력에 빠져보세요. 대구 중구 인근 레슨.',               '["실용음악과 석사", "다수 인디 밴드 앨범 참여"]',                 '그루브가 살아있는 베이스 기타 중급/입문 레슨',                      '["실용음악학과 베이스 전공 졸업"]', 0, 0, 0,  'BOTH',    'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(8,  '러시아 유학파 출신의 정통 클래식 피아노 레슨입니다. 대전 서구 레슨 가능.',         '["모스크바 국립 음악원 졸업", "독주회 10회 이상"]',               '기초부터 고급 입시까지, 정통 클래식 피아노의 정석',                 '["러시아 모스크바 국립 음악원 졸업"]', 0, 0, 0, 'BOTH',   'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(9,  '딱딱한 악보 대신 리듬을 배우는 재즈 피아노입니다. 광주 동구 재즈 바 운영.',        '["실용음악과 졸업", "재즈 밴드 리더"]',                           '코드 반주부터 화려한 즉흥 연주까지! 재즈 피아노 입문',              '["버클리 음악대학 온라인 수료"]',   0, 0, 0,  'ONLINE',  'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(10, '누구나 쉽고 재미있게 시작하는 성인 취미 피아노입니다. 강원 춘천 레슨 가능.',       '["음악교육 석사", "개인 레슨 경력 8년"]',                         '퇴근 후 나만의 힐링 시간, 한 달 한 곡 완성 프로젝트',              '["교육대학원 음악교육 석사"]',     0, 0, 1,  'BOTH',    'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(11, '아이들의 눈높이에 맞춘 즐거운 음악 수업입니다. 강원 속초 유아 음악 전문.',         '["유아 음악 지도사 자격증 보유"]',                                 '창의력을 키워주는 어린이 놀이 피아노 레슨',                         '["유아 음악 교육 전문 자격 취득"]', 0, 0, 0,  'OFFLINE', 'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(12, '마음을 울리는 서정적인 뉴에이지 연주를 가르칩니다. 경북 포항 레슨 / 온라인 가능.', '["음반 3장 발매", "작곡가 활동 중"]',                             '영화 OST, 뉴에이지 명곡 마스터 클래스',                             '["한국 음악 협회 정회원"]',        0, 0, 1,  'BOTH',    'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(13, '독일 오케스트라 수석 출신의 디테일한 레슨입니다. 경북 구미 레슨 가능.',            '["독일 베를린 국립음대 졸업", "현직 오케스트라 수석"]',           '전공생 및 오케스트라 오디션 대비 심화 클래스',                      '["베를린 예술 대학교 바이올린 전공 졸업"]', 0, 0, 0, 'OFFLINE', 'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(14, '바이올린은 첫 자세가 가장 중요합니다. 경북 안동 레슨, 예중/예고 입시 준비 가능.', '["음대 바이올린 전공", "청소년 오케스트라 지도"]',                '바이올린 기초 탄탄! 예쁜 소리 만들기 프로젝트',                     '["경북대학교 음악학과 바이올린 전공"]', 0, 0, 1, 'OFFLINE', 'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(15, '취미로 시작해서 평생 친구가 되는 바이올린 레슨. 전북 전주 레슨 가능.',             '["레슨 경력 12년", "다수 동호회 지도"]',                          '성인 취미반 전문! 쉽고 빠르게 배우는 바이올린',                     '["전북대학교 음악대학 졸업"]',     0, 0, 0,  'BOTH',    'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(16, '현악 사중주 등 앙상블 수업도 가능한 전문가입니다. 전남 순천 레슨.',                 '["실내악 앙상블 팀 운영", "대학 출강"]',                          '함께 연주하는 즐거움, 바이올린 앙상블 및 개인 레슨',                '["순천향대학교 음악학과 강사"]',   0, 0, 0,  'OFFLINE', 'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM'),
(17, '입시와 콩쿠르 준비를 위한 확실한 성과 중심 레슨. 충남 천안/아산 레슨 가능.',       '["국내 유명 콩쿠르 심사위원", "예고 입시 합격자 다수 배출"]',    '확실한 실력 향상! 예중/예고/음대 입시 전문 지도',                   '["연세대학교 음악대학 바이올린 전공 졸업"]', 0, 0, 0, 'BOTH', 'COMPLETED', 0, 0, 0, NOW(), 'SYSTEM');

INSERT IGNORE INTO category_tutor (tutor_id, category_id, created_at, created_by)
VALUES
(1, 1, NOW(), 'SYSTEM'),
(2, 2, NOW(), 'SYSTEM'),
(3, 3, NOW(), 'SYSTEM'),
(4, 4, NOW(), 'SYSTEM'),
(5, 6, NOW(), 'SYSTEM'),
(6, 5, NOW(), 'SYSTEM'),
(7, 4, NOW(), 'SYSTEM'),
(8, 1, NOW(), 'SYSTEM'),
(9, 1, NOW(), 'SYSTEM'),
(10, 1, NOW(), 'SYSTEM'),
(11, 1, NOW(), 'SYSTEM'),
(12, 1, NOW(), 'SYSTEM'),
(13, 2, NOW(), 'SYSTEM'),
(14, 2, NOW(), 'SYSTEM'),
(15, 2, NOW(), 'SYSTEM'),
(16, 2, NOW(), 'SYSTEM'),
(17, 2, NOW(), 'SYSTEM');

INSERT IGNORE INTO subject_tutor (tutor_id, subject_id, created_at, created_by)
VALUES
(1, 1, NOW(), 'SYSTEM'), (1, 3, NOW(), 'SYSTEM'),
(2, 6, NOW(), 'SYSTEM'),
(3, 9, NOW(), 'SYSTEM'), (3, 11, NOW(), 'SYSTEM'),
(4, 13, NOW(), 'SYSTEM'), (4, 14, NOW(), 'SYSTEM'),
(5, 19, NOW(), 'SYSTEM'), (5, 21, NOW(), 'SYSTEM'), (5, 22, NOW(), 'SYSTEM'),
(6, 16, NOW(), 'SYSTEM'), (6, 17, NOW(), 'SYSTEM'), (6, 18, NOW(), 'SYSTEM'),
(7, 15, NOW(), 'SYSTEM'),
(8, 1, NOW(), 'SYSTEM'),
(9, 2, NOW(), 'SYSTEM'), (9, 4, NOW(), 'SYSTEM'),
(10, 4, NOW(), 'SYSTEM'), (10, 5, NOW(), 'SYSTEM'),
(11, 1, NOW(), 'SYSTEM'), (11, 3, NOW(), 'SYSTEM'),
(12, 3, NOW(), 'SYSTEM'), (12, 4, NOW(), 'SYSTEM'),
(13, 6, NOW(), 'SYSTEM'),
(14, 6, NOW(), 'SYSTEM'), (14, 8, NOW(), 'SYSTEM'),
(15, 7, NOW(), 'SYSTEM'), (15, 8, NOW(), 'SYSTEM'),
(16, 6, NOW(), 'SYSTEM'), (16, 7, NOW(), 'SYSTEM'),
(17, 6, NOW(), 'SYSTEM');

-- ──────────────────────────────────────────────────────────────
-- Location (시/도 및 시/군/구 계층 데이터)
-- 1. 최상위 시/도 등록 (parent_id = NULL)
-- ──────────────────────────────────────────────────────────────
INSERT IGNORE INTO location (location_id, parent_id, name, created_at, created_by)
VALUES
(1, NULL, '서울', NOW(), 'SYSTEM'),
(2, NULL, '경기', NOW(), 'SYSTEM'),
(3, NULL, '인천', NOW(), 'SYSTEM'),
(4, NULL, '대전', NOW(), 'SYSTEM'),
(5, NULL, '대구', NOW(), 'SYSTEM'),
(6, NULL, '부산', NOW(), 'SYSTEM'),
(7, NULL, '울산', NOW(), 'SYSTEM'),
(8, NULL, '광주', NOW(), 'SYSTEM'),
(9, NULL, '세종', NOW(), 'SYSTEM'),
(10, NULL, '강원', NOW(), 'SYSTEM'),
(11, NULL, '충북', NOW(), 'SYSTEM'),
(12, NULL, '충남', NOW(), 'SYSTEM'),
(13, NULL, '전북', NOW(), 'SYSTEM'),
(14, NULL, '전남', NOW(), 'SYSTEM'),
(15, NULL, '경북', NOW(), 'SYSTEM'),
(16, NULL, '경남', NOW(), 'SYSTEM'),
(17, NULL, '제주', NOW(), 'SYSTEM');

-- ──────────────────────────────────────────────────────────────
-- 2. 하위 세부 지역 및 '전체' 옵션 등록 (parent_id 연결)
-- ──────────────────────────────────────────────────────────────
INSERT IGNORE INTO location (location_id, parent_id, name, created_at, created_by)
VALUES
-- 서울 (id: 1)
(18, 1, '서울 전체', NOW(), 'SYSTEM'),
(19, 1, '종로구', NOW(), 'SYSTEM'),
(20, 1, '중구', NOW(), 'SYSTEM'),
(21, 1, '용산구', NOW(), 'SYSTEM'),
(22, 1, '성동구', NOW(), 'SYSTEM'),
(23, 1, '광진구', NOW(), 'SYSTEM'),
(24, 1, '동대문구', NOW(), 'SYSTEM'),
(25, 1, '중랑구', NOW(), 'SYSTEM'),
(26, 1, '성북구', NOW(), 'SYSTEM'),
(27, 1, '강북구', NOW(), 'SYSTEM'),
(28, 1, '도봉구', NOW(), 'SYSTEM'),
(29, 1, '노원구', NOW(), 'SYSTEM'),
(30, 1, '은평구', NOW(), 'SYSTEM'),
(31, 1, '서대문구', NOW(), 'SYSTEM'),
(32, 1, '마포구', NOW(), 'SYSTEM'),
(33, 1, '양천구', NOW(), 'SYSTEM'),
(34, 1, '강서구', NOW(), 'SYSTEM'),
(35, 1, '구로구', NOW(), 'SYSTEM'),
(36, 1, '금천구', NOW(), 'SYSTEM'),
(37, 1, '영등포구', NOW(), 'SYSTEM'),
(38, 1, '동작구', NOW(), 'SYSTEM'),
(39, 1, '관악구', NOW(), 'SYSTEM'),
(40, 1, '서초구', NOW(), 'SYSTEM'),
(41, 1, '강남구', NOW(), 'SYSTEM'),
(42, 1, '송파구', NOW(), 'SYSTEM'),
(43, 1, '강동구', NOW(), 'SYSTEM'),

-- 경기 (id: 2)
(44, 2, '경기 전체', NOW(), 'SYSTEM'),
(45, 2, '수원시', NOW(), 'SYSTEM'),
(46, 2, '수원시 장안구', NOW(), 'SYSTEM'),
(47, 2, '수원시 권선구', NOW(), 'SYSTEM'),
(48, 2, '수원시 팔달구', NOW(), 'SYSTEM'),
(49, 2, '수원시 영통구', NOW(), 'SYSTEM'),
(50, 2, '성남시', NOW(), 'SYSTEM'),
(51, 2, '성남시 수정구', NOW(), 'SYSTEM'),
(52, 2, '성남시 중원구', NOW(), 'SYSTEM'),
(53, 2, '성남시 분당구', NOW(), 'SYSTEM'),
(54, 2, '의정부시', NOW(), 'SYSTEM'),
(55, 2, '안양시', NOW(), 'SYSTEM'),
(56, 2, '안양시 만안구', NOW(), 'SYSTEM'),
(57, 2, '안양시 동안구', NOW(), 'SYSTEM'),
(58, 2, '부천시', NOW(), 'SYSTEM'),
(59, 2, '부천시 원미구', NOW(), 'SYSTEM'),
(60, 2, '부천시 소사구', NOW(), 'SYSTEM'),
(61, 2, '부천시 오정구', NOW(), 'SYSTEM'),
(62, 2, '광명시', NOW(), 'SYSTEM'),
(63, 2, '평택시', NOW(), 'SYSTEM'),
(64, 2, '동두천시', NOW(), 'SYSTEM'),
(65, 2, '안산시', NOW(), 'SYSTEM'),
(66, 2, '안산시 상록구', NOW(), 'SYSTEM'),
(67, 2, '안산시 단원구', NOW(), 'SYSTEM'),
(68, 2, '고양시', NOW(), 'SYSTEM'),
(69, 2, '고양시 덕양구', NOW(), 'SYSTEM'),
(70, 2, '고양시 일산동구', NOW(), 'SYSTEM'),
(71, 2, '고양시 일산서구', NOW(), 'SYSTEM'),
(72, 2, '과천시', NOW(), 'SYSTEM'),
(73, 2, '구리시', NOW(), 'SYSTEM'),
(74, 2, '남양주시', NOW(), 'SYSTEM'),
(75, 2, '오산시', NOW(), 'SYSTEM'),
(76, 2, '시흥시', NOW(), 'SYSTEM'),
(77, 2, '군포시', NOW(), 'SYSTEM'),
(78, 2, '의왕시', NOW(), 'SYSTEM'),
(79, 2, '하남시', NOW(), 'SYSTEM'),
(80, 2, '용인시', NOW(), 'SYSTEM'),
(81, 2, '용인시 처인구', NOW(), 'SYSTEM'),
(82, 2, '용인시 기흥구', NOW(), 'SYSTEM'),
(83, 2, '용인시 수지구', NOW(), 'SYSTEM'),
(84, 2, '파주시', NOW(), 'SYSTEM'),
(85, 2, '이천시', NOW(), 'SYSTEM'),
(86, 2, '안성시', NOW(), 'SYSTEM'),
(87, 2, '김포시', NOW(), 'SYSTEM'),
(88, 2, '화성시', NOW(), 'SYSTEM'),
(89, 2, '화성시 만세구', NOW(), 'SYSTEM'),
(90, 2, '화성시 효행구', NOW(), 'SYSTEM'),
(91, 2, '화성시 병점구', NOW(), 'SYSTEM'),
(92, 2, '화성시 동탄구', NOW(), 'SYSTEM'),
(93, 2, '광주시', NOW(), 'SYSTEM'),
(94, 2, '양주시', NOW(), 'SYSTEM'),
(95, 2, '포천시', NOW(), 'SYSTEM'),
(96, 2, '여주시', NOW(), 'SYSTEM'),
(97, 2, '연천군', NOW(), 'SYSTEM'),
(98, 2, '가평군', NOW(), 'SYSTEM'),
(99, 2, '양평군', NOW(), 'SYSTEM'),

-- 인천 (id: 3)
(100, 3, '인천 전체', NOW(), 'SYSTEM'),
(101, 3, '제물포구', NOW(), 'SYSTEM'),
(102, 3, '영종구', NOW(), 'SYSTEM'),
(103, 3, '미추홀구', NOW(), 'SYSTEM'),
(104, 3, '연수구', NOW(), 'SYSTEM'),
(105, 3, '남동구', NOW(), 'SYSTEM'),
(106, 3, '부평구', NOW(), 'SYSTEM'),
(107, 3, '계양구', NOW(), 'SYSTEM'),
(108, 3, '서해구', NOW(), 'SYSTEM'),
(109, 3, '검단구', NOW(), 'SYSTEM'),
(110, 3, '강화군', NOW(), 'SYSTEM'),
(111, 3, '옹진군', NOW(), 'SYSTEM'),

-- 대전 (id: 4)
(112, 4, '대전 전체', NOW(), 'SYSTEM'),
(113, 4, '동구', NOW(), 'SYSTEM'),
(114, 4, '중구', NOW(), 'SYSTEM'),
(115, 4, '서구', NOW(), 'SYSTEM'),
(116, 4, '유성구', NOW(), 'SYSTEM'),
(117, 4, '대덕구', NOW(), 'SYSTEM'),

-- 대구 (id: 5)
(118, 5, '대구 전체', NOW(), 'SYSTEM'),
(119, 5, '중구', NOW(), 'SYSTEM'),
(120, 5, '동구', NOW(), 'SYSTEM'),
(121, 5, '서구', NOW(), 'SYSTEM'),
(122, 5, '남구', NOW(), 'SYSTEM'),
(123, 5, '북구', NOW(), 'SYSTEM'),
(124, 5, '수성구', NOW(), 'SYSTEM'),
(125, 5, '달서구', NOW(), 'SYSTEM'),
(126, 5, '달성군', NOW(), 'SYSTEM'),
(127, 5, '군위군', NOW(), 'SYSTEM'),

-- 부산 (id: 6)
(128, 6, '부산 전체', NOW(), 'SYSTEM'),
(129, 6, '중구', NOW(), 'SYSTEM'),
(130, 6, '서구', NOW(), 'SYSTEM'),
(131, 6, '동구', NOW(), 'SYSTEM'),
(132, 6, '영도구', NOW(), 'SYSTEM'),
(133, 6, '부산진구', NOW(), 'SYSTEM'),
(134, 6, '동래구', NOW(), 'SYSTEM'),
(135, 6, '남구', NOW(), 'SYSTEM'),
(136, 6, '북구', NOW(), 'SYSTEM'),
(137, 6, '해운대구', NOW(), 'SYSTEM'),
(138, 6, '사하구', NOW(), 'SYSTEM'),
(139, 6, '금정구', NOW(), 'SYSTEM'),
(140, 6, '강서구', NOW(), 'SYSTEM'),
(141, 6, '연제구', NOW(), 'SYSTEM'),
(142, 6, '수영구', NOW(), 'SYSTEM'),
(143, 6, '사상구', NOW(), 'SYSTEM'),
(144, 6, '기장군', NOW(), 'SYSTEM'),

-- 울산 (id: 7)
(145, 7, '울산 전체', NOW(), 'SYSTEM'),
(146, 7, '중구', NOW(), 'SYSTEM'),
(147, 7, '남구', NOW(), 'SYSTEM'),
(148, 7, '동구', NOW(), 'SYSTEM'),
(149, 7, '북구', NOW(), 'SYSTEM'),
(150, 7, '울주군', NOW(), 'SYSTEM'),

-- 광주 (id: 8)
(151, 8, '광주 전체', NOW(), 'SYSTEM'),
(152, 8, '동구', NOW(), 'SYSTEM'),
(153, 8, '서구', NOW(), 'SYSTEM'),
(154, 8, '남구', NOW(), 'SYSTEM'),
(155, 8, '북구', NOW(), 'SYSTEM'),
(156, 8, '광산구', NOW(), 'SYSTEM'),

-- 세종 (id: 9)
(157, 9, '세종 전체', NOW(), 'SYSTEM'),

-- 강원 (id: 10)
(158, 10, '강원 전체', NOW(), 'SYSTEM'),
(159, 10, '춘천시', NOW(), 'SYSTEM'),
(160, 10, '원주시', NOW(), 'SYSTEM'),
(161, 10, '강릉시', NOW(), 'SYSTEM'),
(162, 10, '동해시', NOW(), 'SYSTEM'),
(163, 10, '태백시', NOW(), 'SYSTEM'),
(164, 10, '속초시', NOW(), 'SYSTEM'),
(165, 10, '삼척시', NOW(), 'SYSTEM'),
(166, 10, '홍천군', NOW(), 'SYSTEM'),
(167, 10, '횡성군', NOW(), 'SYSTEM'),
(168, 10, '영월군', NOW(), 'SYSTEM'),
(169, 10, '평창군', NOW(), 'SYSTEM'),
(170, 10, '정선군', NOW(), 'SYSTEM'),
(171, 10, '철원군', NOW(), 'SYSTEM'),
(172, 10, '화천군', NOW(), 'SYSTEM'),
(173, 10, '양구군', NOW(), 'SYSTEM'),
(174, 10, '인제군', NOW(), 'SYSTEM'),
(175, 10, '고성군', NOW(), 'SYSTEM'),
(176, 10, '양양군', NOW(), 'SYSTEM'),

-- 충북 (id: 11)
(177, 11, '충북 전체', NOW(), 'SYSTEM'),
(178, 11, '청주시', NOW(), 'SYSTEM'),
(179, 11, '청주시 상당구', NOW(), 'SYSTEM'),
(180, 11, '청주시 서원구', NOW(), 'SYSTEM'),
(181, 11, '청주시 흥덕구', NOW(), 'SYSTEM'),
(182, 11, '청주시 청원구', NOW(), 'SYSTEM'),
(183, 11, '충주시', NOW(), 'SYSTEM'),
(184, 11, '제천시', NOW(), 'SYSTEM'),
(185, 11, '보은군', NOW(), 'SYSTEM'),
(186, 11, '옥천군', NOW(), 'SYSTEM'),
(187, 11, '영동군', NOW(), 'SYSTEM'),
(188, 11, '증평군', NOW(), 'SYSTEM'),
(189, 11, '진천군', NOW(), 'SYSTEM'),
(190, 11, '괴산군', NOW(), 'SYSTEM'),
(191, 11, '음성군', NOW(), 'SYSTEM'),
(192, 11, '단양군', NOW(), 'SYSTEM'),

-- 충남 (id: 12)
(193, 12, '충남 전체', NOW(), 'SYSTEM'),
(194, 12, '천안시', NOW(), 'SYSTEM'),
(195, 12, '천안시 동남구', NOW(), 'SYSTEM'),
(196, 12, '천안시 서북구', NOW(), 'SYSTEM'),
(197, 12, '공주시', NOW(), 'SYSTEM'),
(198, 12, '보령시', NOW(), 'SYSTEM'),
(199, 12, '아산시', NOW(), 'SYSTEM'),
(200, 12, '서산시', NOW(), 'SYSTEM'),
(201, 12, '논산시', NOW(), 'SYSTEM'),
(202, 12, '계룡시', NOW(), 'SYSTEM'),
(203, 12, '당진시', NOW(), 'SYSTEM'),
(204, 12, '금산군', NOW(), 'SYSTEM'),
(205, 12, '부여군', NOW(), 'SYSTEM'),
(206, 12, '서천군', NOW(), 'SYSTEM'),
(207, 12, '청양군', NOW(), 'SYSTEM'),
(208, 12, '홍성군', NOW(), 'SYSTEM'),
(209, 12, '예산군', NOW(), 'SYSTEM'),
(210, 12, '태안군', NOW(), 'SYSTEM'),

-- 전북 (id: 13)
(211, 13, '전북 전체', NOW(), 'SYSTEM'),
(212, 13, '전주시', NOW(), 'SYSTEM'),
(213, 13, '전주시 완산구', NOW(), 'SYSTEM'),
(214, 13, '전주시 덕진구', NOW(), 'SYSTEM'),
(215, 13, '군산시', NOW(), 'SYSTEM'),
(216, 13, '익산시', NOW(), 'SYSTEM'),
(217, 13, '정읍시', NOW(), 'SYSTEM'),
(218, 13, '남원시', NOW(), 'SYSTEM'),
(219, 13, '김제시', NOW(), 'SYSTEM'),
(220, 13, '완주군', NOW(), 'SYSTEM'),
(221, 13, '진안군', NOW(), 'SYSTEM'),
(222, 13, '무주군', NOW(), 'SYSTEM'),
(223, 13, '장수군', NOW(), 'SYSTEM'),
(224, 13, '임실군', NOW(), 'SYSTEM'),
(225, 13, '순창군', NOW(), 'SYSTEM'),
(226, 13, '고창군', NOW(), 'SYSTEM'),
(227, 13, '부안군', NOW(), 'SYSTEM'),

-- 전남 (id: 14)
(228, 14, '전남 전체', NOW(), 'SYSTEM'),
(229, 14, '목포시', NOW(), 'SYSTEM'),
(230, 14, '여수시', NOW(), 'SYSTEM'),
(231, 14, '순천시', NOW(), 'SYSTEM'),
(232, 14, '나주시', NOW(), 'SYSTEM'),
(233, 14, '광양시', NOW(), 'SYSTEM'),
(234, 14, '담양군', NOW(), 'SYSTEM'),
(235, 14, '곡성군', NOW(), 'SYSTEM'),
(236, 14, '구례군', NOW(), 'SYSTEM'),
(237, 14, '고흥군', NOW(), 'SYSTEM'),
(238, 14, '보성군', NOW(), 'SYSTEM'),
(239, 14, '화순군', NOW(), 'SYSTEM'),
(240, 14, '장흥군', NOW(), 'SYSTEM'),
(241, 14, '강진군', NOW(), 'SYSTEM'),
(242, 14, '해남군', NOW(), 'SYSTEM'),
(243, 14, '영암군', NOW(), 'SYSTEM'),
(244, 14, '무안군', NOW(), 'SYSTEM'),
(245, 14, '함평군', NOW(), 'SYSTEM'),
(246, 14, '영광군', NOW(), 'SYSTEM'),
(247, 14, '장성군', NOW(), 'SYSTEM'),
(248, 14, '완도군', NOW(), 'SYSTEM'),
(249, 14, '진도군', NOW(), 'SYSTEM'),
(250, 14, '신안군', NOW(), 'SYSTEM'),

-- 경북 (id: 15)
(251, 15, '경북 전체', NOW(), 'SYSTEM'),
(252, 15, '포항시', NOW(), 'SYSTEM'),
(253, 15, '포항시 남구', NOW(), 'SYSTEM'),
(254, 15, '포항시 북구', NOW(), 'SYSTEM'),
(255, 15, '경주시', NOW(), 'SYSTEM'),
(256, 15, '김천시', NOW(), 'SYSTEM'),
(257, 15, '안동시', NOW(), 'SYSTEM'),
(258, 15, '구미시', NOW(), 'SYSTEM'),
(259, 15, '영주시', NOW(), 'SYSTEM'),
(260, 15, '영천시', NOW(), 'SYSTEM'),
(261, 15, '상주시', NOW(), 'SYSTEM'),
(262, 15, '문경시', NOW(), 'SYSTEM'),
(263, 15, '경산시', NOW(), 'SYSTEM'),
(264, 15, '의성군', NOW(), 'SYSTEM'),
(265, 15, '청송군', NOW(), 'SYSTEM'),
(266, 15, '영양군', NOW(), 'SYSTEM'),
(267, 15, '영덕군', NOW(), 'SYSTEM'),
(268, 15, '청도군', NOW(), 'SYSTEM'),
(269, 15, '고령군', NOW(), 'SYSTEM'),
(270, 15, '성주군', NOW(), 'SYSTEM'),
(271, 15, '칠곡군', NOW(), 'SYSTEM'),
(272, 15, '예천군', NOW(), 'SYSTEM'),
(273, 15, '봉화군', NOW(), 'SYSTEM'),
(274, 15, '울진군', NOW(), 'SYSTEM'),
(275, 15, '울릉군', NOW(), 'SYSTEM'),

-- 경남 (id: 16)
(276, 16, '경남 전체', NOW(), 'SYSTEM'),
(277, 16, '창원시', NOW(), 'SYSTEM'),
(278, 16, '창원시 의창구', NOW(), 'SYSTEM'),
(279, 16, '창원시 성산구', NOW(), 'SYSTEM'),
(280, 16, '창원시 마산합포구', NOW(), 'SYSTEM'),
(281, 16, '창원시 마산회원구', NOW(), 'SYSTEM'),
(282, 16, '창원시 진해구', NOW(), 'SYSTEM'),
(283, 16, '진주시', NOW(), 'SYSTEM'),
(284, 16, '통영시', NOW(), 'SYSTEM'),
(285, 16, '사천시', NOW(), 'SYSTEM'),
(286, 16, '김해시', NOW(), 'SYSTEM'),
(287, 16, '밀양시', NOW(), 'SYSTEM'),
(288, 16, '거제시', NOW(), 'SYSTEM'),
(289, 16, '양산시', NOW(), 'SYSTEM'),
(290, 16, '의령군', NOW(), 'SYSTEM'),
(291, 16, '함안군', NOW(), 'SYSTEM'),
(292, 16, '창녕군', NOW(), 'SYSTEM'),
(293, 16, '고성군', NOW(), 'SYSTEM'),
(294, 16, '남해군', NOW(), 'SYSTEM'),
(295, 16, '하동군', NOW(), 'SYSTEM'),
(296, 16, '산청군', NOW(), 'SYSTEM'),
(297, 16, '함양군', NOW(), 'SYSTEM'),
(298, 16, '거창군', NOW(), 'SYSTEM'),
(299, 16, '합천군', NOW(), 'SYSTEM'),

-- 제주 (id: 17)
(300, 17, '제주 전체', NOW(), 'SYSTEM'),
(301, 17, '제주시', NOW(), 'SYSTEM'),
(302, 17, '서귀포시', NOW(), 'SYSTEM');

INSERT IGNORE INTO location_tutor (tutor_id, location_id, created_at, created_by)
VALUES
(1, 1, NOW(), 'SYSTEM'),
(2, 1, NOW(), 'SYSTEM'),
(3, 1, NOW(), 'SYSTEM'),
(4, 2, NOW(), 'SYSTEM'),
(5, 3, NOW(), 'SYSTEM'),
(6, 2, NOW(), 'SYSTEM'),
(7, 5, NOW(), 'SYSTEM'),
(8, 4, NOW(), 'SYSTEM'),
(9, 8, NOW(), 'SYSTEM'),
(10, 10, NOW(), 'SYSTEM'),
(11, 10, NOW(), 'SYSTEM'),
(12, 15, NOW(), 'SYSTEM'),
(13, 15, NOW(), 'SYSTEM'),
(14, 15, NOW(), 'SYSTEM'),
(15, 13, NOW(), 'SYSTEM'),
(16, 14, NOW(), 'SYSTEM'),
(17, 12, NOW(), 'SYSTEM');

INSERT IGNORE INTO student_account (student_id, introduction, created_at, created_by)
VALUES
(18, '음악을 전공하고 싶어 기초부터 배우려는 학생입니다.', NOW(), 'SYSTEM'),
(19, '방과 후 활동으로 바이올린을 배우고 싶은 학생입니다.', NOW(), 'SYSTEM'),
(20, '클래식 음악을 좋아해서 직접 연주해보고 싶어 가입했습니다.', NOW(), 'SYSTEM'),
(21, '어릴 때 그만둔 피아노를 다시 취미로 시작하려는 직장인입니다.', NOW(), 'SYSTEM'),
(22, '노래를 잘 부르고 싶어서 발성부터 배우고 싶은 대학생입니다.', NOW(), 'SYSTEM'),
(23, 'SNS에 연주 영상을 올리는 게 목표입니다. 감성적인 곡 추천 부탁드려요.', NOW(), 'SYSTEM'),
(24, '재즈의 즉흥 연주 매력에 빠져 재즈 피아노를 배우고 싶습니다.', NOW(), 'SYSTEM'),
(25, '태교를 위해 첼로 연주를 시작해보려고 합니다.', NOW(), 'SYSTEM'),
(26, '취미 밴드에서 드럼을 맡고 있습니다. 기본기를 더 다지고 싶네요.', NOW(), 'SYSTEM'),
(27, '자작곡을 만들고 싶은데 화성학 레슨도 같이 해주실 분 찾습니다.', NOW(), 'SYSTEM'),
(28, '음대 입시를 준비하고 있는 고등학생입니다. 기본기부터 다시 배우고 싶어요.', NOW(), 'SYSTEM'),
(29, '평소에 꿈꾸던 첼로를 시작해보려 합니다. 악기 대여가 가능한 곳을 찾고 있어요.', NOW(), 'SYSTEM'),
(30, '취미로 피아노를 다시 시작하고 싶은 주부입니다. 오전 시간대 레슨을 선호합니다.', NOW(), 'SYSTEM'),
(31, '대학교 밴드 동아리에서 일렉기타를 맡게 되었습니다. 속주 테크닉을 배우고 싶습니다.', NOW(), 'SYSTEM'),
(32, '작곡을 전공하고 있는데, 편곡을 위해 건반 연주 실력을 키우고 싶습니다.', NOW(), 'SYSTEM'),
(33, '직장인 동호회에서 활동 중인 베이스 연주자입니다. 슬랩 테크닉이 고민이라 신청합니다.', NOW(), 'SYSTEM'),
(34, '아이돌 지망생입니다. 고음 발성과 무대 매너를 체계적으로 배우고 싶습니다.', NOW(), 'SYSTEM'),
(35, '어릴 때 배웠던 피아노를 성인이 되어 다시 시작합니다. 뉴에이지 곡 위주로 배우고 싶어요.', NOW(), 'SYSTEM'),
(36, '클래식 작곡과 진학을 희망하는 중학생입니다. 이론과 실기를 병행하고 싶습니다.', NOW(), 'SYSTEM');

INSERT IGNORE INTO matching (student_id, tutor_id, request_msg, status, price_per_lesson, created_at, created_by)
VALUES
(18, 1, '기초부터 체계적으로 배우고 싶습니다.', 'ACCEPTED', 30000, NOW(), 'SYSTEM'),
(20, 1, '기초부터 체계적으로 배우고 싶습니다. 주말 레슨 가능할까요?', 'ACCEPTED', 30000, NOW(), 'SYSTEM'),
(24, 1, '기초부터 체계적으로 배우고 싶습니다. 주말 레슨', 'ACCEPTED', 30000, NOW(), 'SYSTEM'),
(25, 1, '기초부터 체계적으', 'ACCEPTED', 30000, NOW(), 'SYSTEM'),
(27, 1, '기초부터 체계적으로 배우고 싶습니다. 주말 레슨 가능', 'ACCEPTED', 30000, NOW(), 'SYSTEM'),
(28, 1, '기초부터 체계적', 'ACCEPTED', 30000, NOW(), 'SYSTEM'),
(30, 1, '기초부터 체계적으로 배우고 싶습니다. 주', 'ACCEPTED', 30000, NOW(), 'SYSTEM'),
(32, 1, '기초부터 체계적으로 배우고 싶습니다. 주말 레슨 가', 'ACCEPTED', 30000, NOW(), 'SYSTEM'),
(33, 1, '기초부터 체계적으로 배우고', 'ACCEPTED', 30000, NOW(), 'SYSTEM'),
(34, 1, '기초부터 체계적으로 배우고 싶습니다. 주말 레슨 가능?', 'ACCEPTED', 30000, NOW(), 'SYSTEM'),
(21, 10, '직장인이라 저녁 7시 이후 수업을 희망합니다.', 'PENDING', 30000, NOW(), 'SYSTEM'),
(35, 12, '뉴에이지 곡 위주로 배우고 싶어요. 잘 부탁드립니다!', 'ACCEPTED', 30000, NOW(), 'SYSTEM'),
(19, 2, '학교 방과후 수업이랑 병행하려고 합니다. 악기 대여 가능한가요?', 'PENDING', 30000, NOW(), 'SYSTEM'),
(23, 14, '유튜브에 연주 영상 올리는 게 목표입니다! 자세 교정 부탁드려요.', 'REJECTED', 30000, NOW(), 'SYSTEM'),
(31, 4, '밴드 공연이 한 달 남았습니다. 일렉기타 속주 집중 레슨 부탁드립니다.', 'ACCEPTED', 30000, NOW(), 'SYSTEM'),
(22, 5, '고음 발성법이 너무 궁금합니다. 테스트 한 번 받아보고 싶어요.', 'PENDING', 30000, NOW(), 'SYSTEM'),
(26, 6, '기본기부터 다시 다지고 싶은 7년 차 취미 드러머입니다.', 'ACCEPTED', 30000, NOW(), 'SYSTEM'),
(29, 3, '첼로 소리가 너무 좋아서 시작하려 합니다. 완전 초보인데 괜찮나요?', 'PENDING', 30000, NOW(), 'SYSTEM');

INSERT IGNORE INTO lesson_review (matching_id, content, rating, created_at, created_by)
VALUES
(1, '선생님이 정말 친절하시고 기초를 탄', 5, NOW(), 'SYSTEM'),
(2, '선생님이 정말 친절하시고 기초를 탄탄하게 잡아주셔서 좋아요. ', 5, NOW(), 'SYSTEM'),
(3, '선생님이 정말', 5, NOW(), 'SYSTEM'),
(4, '선생님이 정말 친절하시고 기초를 탄탄하게 잡아주셔서 좋아요. 주말 시간도 잘', 5, NOW(), 'SYSTEM'),
(5, '선생님이 정말 친절하시고 기초를 탄탄하게', 5, NOW(), 'SYSTEM'),
(6, '선생님이 정말 친절하시고 기초를 탄탄하게 잡아주셔서 좋아요.', 5, NOW(), 'SYSTEM'),
(7, '선생님이 정말 친절하시고', 5, NOW(), 'SYSTEM'),
(8, '선생님이 정말 친절하시고 기초를 탄탄하게 잡아주셔', 5, NOW(), 'SYSTEM'),
(9, '선생님이 정말 친절하시고 기초를 탄탄하게 잡아주셔서 좋아요. 주말 시간도 잘 맞', 5, NOW(), 'SYSTEM'),
(10, '선생님이 정말 친절하시고 기초를 탄탄하게 잡아주셔서 좋아요. 주말 시간도 잘 맞춰주십니다!', 5, NOW(), 'SYSTEM'),
(12, '뉴에이지 곡 위주로 배우고 싶었는데, 제가 딱 원하는 곡들로 커리큘럼을 짜주셔서 너무 즐거워요.', 5, NOW(), 'SYSTEM'),
(15, '공연 준비 때문에 급하게 요청드렸는데 속주 팁을 정말 잘 알려주셨어요. 덕분에 무사히 공연 마쳤습니다!', 4, NOW(), 'SYSTEM'),
(17, '7년 동안 독학하면서 놓쳤던 나쁜 습관들을 바로잡아 주셨습니다. 역시 전문가는 다르네요.', 5, NOW(), 'SYSTEM');

INSERT IGNORE INTO tutor_style (style_type, created_at, created_by)
VALUES
('KIND_AND_WARM', NOW(), 'SYSTEM'),
('STRUCTURED_AND_STRICT', NOW(), 'SYSTEM'),
('FREE_AND_CREATIVE', NOW(), 'SYSTEM'),
('COMMUNICATION_AND_FEEDBACK', NOW(), 'SYSTEM'),
('RESULT_AND_SKILL', NOW(), 'SYSTEM'),
('HUMOROUS_AND_FUN', NOW(), 'SYSTEM'),
('THEORY_AND_PRINCIPLE', NOW(), 'SYSTEM'),
('ANY', NOW(), 'SYSTEM');

INSERT IGNORE INTO lesson_goal (lesson_goal_type)
VALUES
('HOBBY'),
('COMPETITION'),
('EXAM'),
('CERTIFICATE'),
('SHORT_TERM'),
('CREATION');

-- ──────────────────────────────────────────────────────────────
-- goal_tutor: 튜터별 레슨 목표 설정 (COMPLETED 조건 충족용)
-- lesson_goal id: 1=HOBBY, 2=COMPETITION, 3=EXAM, 4=CERTIFICATE, 5=SHORT_TERM, 6=CREATION
-- ──────────────────────────────────────────────────────────────
INSERT IGNORE INTO goal_tutor (tutor_id, goal_id)
VALUES
(1, 1), (1, 3),
(2, 1), (2, 5),
(3, 1), (3, 5),
(4, 1), (4, 5),
(5, 1), (5, 2), (5, 3),
(6, 1), (6, 5),
(7, 1), (7, 6),
(8, 2), (8, 3),
(9, 1), (9, 6),
(10, 1), (10, 5),
(11, 1), (11, 4),
(12, 1), (12, 6),
(13, 2), (13, 3),
(14, 3), (14, 5),
(15, 1), (15, 5),
(16, 1), (16, 6),
(17, 2), (17, 3);

-- ──────────────────────────────────────────────────────────────
-- style_tutor: 튜터별 레슨 스타일
-- tutor_style id: 1=KIND_AND_WARM, 2=STRUCTURED_AND_STRICT, 3=FREE_AND_CREATIVE,
--                 4=COMMUNICATION_AND_FEEDBACK, 5=RESULT_AND_SKILL, 6=HUMOROUS_AND_FUN,
--                 7=THEORY_AND_PRINCIPLE, 8=ANY
-- ──────────────────────────────────────────────────────────────
INSERT IGNORE INTO style_tutor (tutor_id, style_id, created_at, created_by)
VALUES
(1,  1, NOW(), 'SYSTEM'), (1,  4, NOW(), 'SYSTEM'),
(2,  1, NOW(), 'SYSTEM'), (2,  2, NOW(), 'SYSTEM'),
(3,  1, NOW(), 'SYSTEM'), (3,  3, NOW(), 'SYSTEM'),
(4,  3, NOW(), 'SYSTEM'), (4,  6, NOW(), 'SYSTEM'),
(5,  4, NOW(), 'SYSTEM'), (5,  1, NOW(), 'SYSTEM'),
(6,  6, NOW(), 'SYSTEM'), (6,  4, NOW(), 'SYSTEM'),
(7,  3, NOW(), 'SYSTEM'), (7,  6, NOW(), 'SYSTEM'),
(8,  2, NOW(), 'SYSTEM'), (8,  7, NOW(), 'SYSTEM'),
(9,  3, NOW(), 'SYSTEM'), (9,  6, NOW(), 'SYSTEM'),
(10, 1, NOW(), 'SYSTEM'), (10, 4, NOW(), 'SYSTEM'),
(11, 1, NOW(), 'SYSTEM'), (11, 6, NOW(), 'SYSTEM'),
(12, 3, NOW(), 'SYSTEM'), (12, 1, NOW(), 'SYSTEM'),
(13, 2, NOW(), 'SYSTEM'), (13, 5, NOW(), 'SYSTEM'),
(14, 2, NOW(), 'SYSTEM'), (14, 4, NOW(), 'SYSTEM'),
(15, 1, NOW(), 'SYSTEM'), (15, 6, NOW(), 'SYSTEM'),
(16, 4, NOW(), 'SYSTEM'), (16, 3, NOW(), 'SYSTEM'),
(17, 5, NOW(), 'SYSTEM'), (17, 2, NOW(), 'SYSTEM');

-- ──────────────────────────────────────────────────────────────
-- tutor_lesson_price: 레슨 가격 정보 (min/maxPrice ES 색인용)
-- ──────────────────────────────────────────────────────────────
INSERT IGNORE INTO tutor_lesson_price (tutor_id, class_name, price, created_at, created_by)
VALUES
(1,  '입문 클래스',   30000, NOW(), 'SYSTEM'),
(1,  '심화 클래스', 45000, NOW(), 'SYSTEM'),
(2,  '기초 클래스',   35000, NOW(), 'SYSTEM'),
(2,  '중급 클래스',   45000, NOW(), 'SYSTEM'),
(3,  '취미 클래스',   40000, NOW(), 'SYSTEM'),
(4,  '통기타 기초',   25000, NOW(), 'SYSTEM'),
(4,  '일렉기타 중급', 35000, NOW(), 'SYSTEM'),
(5,  '보컬 기초',     30000, NOW(), 'SYSTEM'),
(5,  '보컬 심화',     50000, NOW(), 'SYSTEM'),
(6,  '드럼 기초',     30000, NOW(), 'SYSTEM'),
(7,  '베이스 입문',   35000, NOW(), 'SYSTEM'),
(8,  '클래식 기초',   50000, NOW(), 'SYSTEM'),
(8,  '클래식 심화',  70000, NOW(), 'SYSTEM'),
(9,  '재즈 입문',     35000, NOW(), 'SYSTEM'),
(10, '취미 피아노',   25000, NOW(), 'SYSTEM'),
(11, '어린이 클래스',  20000, NOW(), 'SYSTEM'),
(12, '뉴에이지 클래스', 35000, NOW(), 'SYSTEM'),
(13, '심화/입시 클래스', 60000, NOW(), 'SYSTEM'),
(14, '바이올린 기초', 40000, NOW(), 'SYSTEM'),
(15, '바이올린 취미', 30000, NOW(), 'SYSTEM'),
(16, '앙상블 클래스', 45000, NOW(), 'SYSTEM'),
(17, '입시 전문',     70000, NOW(), 'SYSTEM'),
(17, '입시 집중',    120000, NOW(), 'SYSTEM');