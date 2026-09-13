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

DELETE FROM user_account WHERE user_id LIKE 'student%@test.com';

INSERT INTO user_account (user_id, user_password, name, gender, birth_date, phone_number, email, created_at, created_by)
VALUES
('kim_piano', '{noop}pass1234', '김여익', 'MALE', '1990-05-15', '010-1111-2222', 'kim_piano@example.com', NOW(), 'SYSTEM'),
('lee_violin', '{noop}pass5678', '이지윤', 'FEMALE', '1992-08-20', '010-2222-3333', 'lee_violin@example.com', NOW(), 'SYSTEM'),
('park_cello', '{noop}pw9999', '박정헥', 'MALE', '1988-12-01', '010-3333-4444', 'park_cello@example.com', NOW(), 'SYSTEM'),
('jung_guitar', '{noop}gtr777', '정발산', 'MALE', '1991-07-22', '010-5555-6666', 'jung_gtr@example.com', NOW(), 'SYSTEM'),
('kang_vocal', '{noop}vocal000', '강희은', 'FEMALE', '1993-11-05', '010-6666-7777', 'kang_vocal@example.com', NOW(), 'SYSTEM'),
('yoon_drum', '{noop}drummer1', '윤희재', 'MALE', '1989-01-30', '010-7777-8888', 'yoon_drum@example.com', NOW(), 'SYSTEM'),
('lim_bass', '{noop}bassline2', '임재연', 'FEMALE', '1994-06-14', '010-8888-9999', 'lim_bass@example.com', NOW(), 'SYSTEM'),
('piano_master', '{noop}pass123', '마스터', 'FEMALE', '1991-03-22', '010-1111-0001', 'master_p@example.com', NOW(), 'SYSTEM'),
('jazz_piano_lee', '{noop}pass456', '이지혁', 'MALE', '1993-12-10', '010-1111-0002', 'jazz_lee@example.com', NOW(), 'SYSTEM'),
('classic_park', '{noop}pass789', '박남정', 'FEMALE', '1989-05-30', '010-1111-0003', 'classic_p@example.com', NOW(), 'SYSTEM'),
('piano_teacher_choi', '{noop}pass101', '최태민', 'MALE', '1994-07-15', '010-1111-0004', 'choi_edu@example.com', NOW(), 'SYSTEM'),
('emotion_piano', '{noop}pass202', '김감성', 'FEMALE', '1992-02-28', '010-1111-0005', 'emotion_p@example.com', NOW(), 'SYSTEM'),
('violin_strad', '{noop}vpass1', '스트라', 'MALE', '1990-11-11', '010-2222-0001', 'strad_v@example.com', NOW(), 'SYSTEM'),
('bowing_queen', '{noop}vpass2', '보우퀸', 'FEMALE', '1995-06-20', '010-2222-0002', 'bow_queen@example.com', NOW(), 'SYSTEM'),
('violin_kim_v', '{noop}vpass3', '김바이', 'MALE', '1988-08-05', '010-2222-0003', 'kim_v@example.com', NOW(), 'SYSTEM'),
('string_expert', '{noop}vpass4', '현전문', 'FEMALE', '1991-04-12', '010-2222-0004', 'string_e@example.com', NOW(), 'SYSTEM'),
('violin_lesson_pro', '{noop}vpass5', '프로바이', 'MALE', '1993-01-25', '010-2222-0005', 'v_pro@example.com', NOW(), 'SYSTEM'),
('student_aa', '{noop}stud123', '안소희', 'MALE', '2005-04-12', '010-1234-1111', 'aa_stud@example.com', NOW(), 'SYSTEM'),
('student_bb', '{noop}stud456', '배수지', 'FEMALE', '2007-09-21', '010-2345-2222', 'bb_stud@example.com', NOW(), 'SYSTEM'),
('music_lover', '{noop}love888', '사랑해', 'MALE', '1998-12-30', '010-3456-3333', 'lover@example.com', NOW(), 'SYSTEM'),
('hobby_piano', '{noop}pnpn99', '취미생', 'FEMALE', '1992-05-05', '010-4567-4444', 'hobby_p@example.com', NOW(), 'SYSTEM'),
('beginner_v', '{noop}begin1', '초보자', 'MALE', '2001-08-15', '010-5678-5555', 'begin_v@example.com', NOW(), 'SYSTEM'),
('star_dust', '{noop}star77', '별가루', 'FEMALE', '2003-01-11', '010-6789-6666', 'star_d@example.com', NOW(), 'SYSTEM'),
('blue_note', '{noop}blue00', '블루노', 'MALE', '1995-10-22', '010-7890-7777', 'blue_n@example.com', NOW(), 'SYSTEM'),
('classic_fan', '{noop}fan123', '클래식팬', 'FEMALE', '1988-07-07', '010-8901-8888', 'classic_f@example.com', NOW(), 'SYSTEM'),
('jazz_man', '{noop}jazzman1', '재즈맨', 'MALE', '1990-11-11', '010-9012-9999', 'jazz_m@example.com', NOW(), 'SYSTEM'),
('dreamer_99', '{noop}dream99', '꿈나무', 'FEMALE', '1999-03-03', '010-0123-0000', 'dreamer@example.com', NOW(), 'SYSTEM'),
('highschool_p', '{noop}pass111', '고딩이', 'MALE', '2008-05-15', '010-3333-1001', 'student_h@example.com', NOW(), 'SYSTEM'),
('cello_beginner', '{noop}pass222', '첼린이', 'FEMALE', '1996-11-02', '010-3333-1002', 'cello_b@example.com', NOW(), 'SYSTEM'),
('winter_sonata', '{noop}pass333', '겨울연가', 'FEMALE', '1985-01-20', '010-3333-1003', 'winter@example.com', NOW(), 'SYSTEM'),
('rock_spirit', '{noop}pass444', '락스피릿', 'MALE', '2002-07-07', '010-3333-1004', 'rocker@example.com', NOW(), 'SYSTEM'),
('melody_maker', '{noop}pass555', '멜로디', 'FEMALE', '1994-03-30', '010-3333-1005', 'melody@example.com', NOW(), 'SYSTEM'),
('daily_music', '{noop}pass666', '데일리', 'MALE', '1991-09-12', '010-3333-1006', 'daily@example.com', NOW(), 'SYSTEM'),
('vocal_dream', '{noop}pass777', '보컬꿈', 'FEMALE', '2006-12-25', '010-3333-1007', 'v_dream@example.com', NOW(), 'SYSTEM'),
('unni_piano', '{noop}pass888', '피아노언니', 'FEMALE', '1989-06-18', '010-3333-1008', 'unni@example.com', NOW(), 'SYSTEM'),
('mozar_k', '{noop}pass999', '모차르트', 'MALE', '2009-02-14', '010-3333-1009', 'mozar@example.com', NOW(), 'SYSTEM'),
('student1@test.com', '{noop}password123', '테스트학생1', 'MALE',   '1995-01-01', '010-0001-0001', 'student1@test.com', NOW(), 'SYSTEM'),
('student2@test.com', '{noop}password123', '테스트학생2', 'FEMALE', '1996-02-02', '010-0002-0002', 'student2@test.com', NOW(), 'SYSTEM'),
('student3@test.com', '{noop}password123', '테스트학생3', 'MALE',   '1997-03-03', '010-0003-0003', 'student3@test.com', NOW(), 'SYSTEM'),
('student4@test.com', '{noop}password123', '테스트학생4', 'FEMALE', '1998-04-04', '010-0004-0004', 'student4@test.com', NOW(), 'SYSTEM'),
('student5@test.com', '{noop}password123', '테스트학생5', 'MALE',   '1999-05-05', '010-0005-0005', 'student5@test.com', NOW(), 'SYSTEM');

INSERT IGNORE INTO role (role_type, created_at, created_by)
VALUES
('TUTOR', NOW(), 'SYSTEM'),
('STUDENT', NOW(), 'SYSTEM'),
('GUEST', NOW(), 'SYSTEM');

INSERT IGNORE INTO user_role (user_id, role_id, created_at, created_by)
VALUES
    (1, 1, NOW(), 'SYSTEM'),
    (2, 1, NOW(), 'SYSTEM'),
    (3, 1, NOW(), 'SYSTEM'),
    (4, 1, NOW(), 'SYSTEM'),
    (5, 1, NOW(), 'SYSTEM'),
    (6, 1, NOW(), 'SYSTEM'),
    (7, 1, NOW(), 'SYSTEM'),
    (8, 1, NOW(), 'SYSTEM'),
    (9, 1, NOW(), 'SYSTEM'),
    (10, 1, NOW(), 'SYSTEM'),
    (11, 1, NOW(), 'SYSTEM'),
    (12, 1, NOW(), 'SYSTEM'),
    (13, 1, NOW(), 'SYSTEM'),
    (14, 1, NOW(), 'SYSTEM'),
    (15, 1, NOW(), 'SYSTEM'),
    (16, 1, NOW(), 'SYSTEM'),
    (17, 1, NOW(), 'SYSTEM');
-- Assign STUDENT role to all student accounts
INSERT IGNORE INTO user_role (user_id, role_id, created_at, created_by)
SELECT user_id, 2, NOW(), 'SYSTEM' FROM user_account WHERE email LIKE 'student%@test.com';

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

INSERT IGNORE INTO location (name, created_at, created_by)
VALUES
('서울', NOW(), 'SYSTEM'),
('경기', NOW(), 'SYSTEM'),
('인천', NOW(), 'SYSTEM'),
('대전', NOW(), 'SYSTEM'),
('대구', NOW(), 'SYSTEM'),
('부산', NOW(), 'SYSTEM'),
('울산', NOW(), 'SYSTEM'),
('광주', NOW(), 'SYSTEM'),
('세종', NOW(), 'SYSTEM'),
('강원', NOW(), 'SYSTEM'),
('충북', NOW(), 'SYSTEM'),
('충남', NOW(), 'SYSTEM'),
('전북', NOW(), 'SYSTEM'),
('전남', NOW(), 'SYSTEM'),
('경북', NOW(), 'SYSTEM'),
('경남', NOW(), 'SYSTEM'),
('제주', NOW(), 'SYSTEM');

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
-- 1번 강사 (피아노 - 취미/입시)
(1, 1), (1, 3),
-- 2번 강사 (바이올린 - 취미/단기)
(2, 1), (2, 5),
-- 3번 강사 (첼로 - 취미/단기)
(3, 1), (3, 5),
-- 4번 강사 (기타 - 취미/단기)
(4, 1), (4, 5),
-- 5번 강사 (보컬 - 취미/입시/콩쿠르)
(5, 1), (5, 2), (5, 3),
-- 6번 강사 (드럼 - 취미/단기)
(6, 1), (6, 5),
-- 7번 강사 (베이스 - 취미/창작)
(7, 1), (7, 6),
-- 8번 강사 (피아노 클래식 - 입시/콩쿠르)
(8, 2), (8, 3),
-- 9번 강사 (재즈 피아노 - 취미/창작)
(9, 1), (9, 6),
-- 10번 강사 (성인 취미 피아노 - 취미/단기)
(10, 1), (10, 5),
-- 11번 강사 (어린이 피아노 - 취미/자격증)
(11, 1), (11, 4),
-- 12번 강사 (뉴에이지 - 취미/창작)
(12, 1), (12, 6),
-- 13번 강사 (바이올린 심화 - 입시/콩쿠르)
(13, 2), (13, 3),
-- 14번 강사 (바이올린 기초 - 입시/단기)
(14, 3), (14, 5),
-- 15번 강사 (바이올린 취미 - 취미/단기)
(15, 1), (15, 5),
-- 16번 강사 (앙상블 - 취미/창작)
(16, 1), (16, 6),
-- 17번 강사 (입시 전문 - 입시/콩쿠르)
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
(1,  '입문 클래스 (1시간)',   30000, NOW(), 'SYSTEM'),
(1,  '심화 클래스 (1.5시간)', 45000, NOW(), 'SYSTEM'),
(2,  '기초 클래스 (1시간)',   35000, NOW(), 'SYSTEM'),
(2,  '중급 클래스 (1시간)',   45000, NOW(), 'SYSTEM'),
(3,  '취미 클래스 (1시간)',   40000, NOW(), 'SYSTEM'),
(4,  '통기타 기초 (1시간)',   25000, NOW(), 'SYSTEM'),
(4,  '일렉기타 중급 (1시간)', 35000, NOW(), 'SYSTEM'),
(5,  '보컬 기초 (1시간)',     30000, NOW(), 'SYSTEM'),
(5,  '보컬 심화 (1시간)',     50000, NOW(), 'SYSTEM'),
(6,  '드럼 기초 (1시간)',     30000, NOW(), 'SYSTEM'),
(7,  '베이스 입문 (1시간)',   35000, NOW(), 'SYSTEM'),
(8,  '클래식 기초 (1시간)',   50000, NOW(), 'SYSTEM'),
(8,  '클래식 심화 (1.5시간)',  70000, NOW(), 'SYSTEM'),
(9,  '재즈 입문 (1시간)',     35000, NOW(), 'SYSTEM'),
(10, '취미 피아노 (1시간)',   25000, NOW(), 'SYSTEM'),
(11, '어린이 클래스 (45분)',  20000, NOW(), 'SYSTEM'),
(12, '뉴에이지 클래스 (1시간)', 35000, NOW(), 'SYSTEM'),
(13, '심화/입시 클래스 (1시간)', 60000, NOW(), 'SYSTEM'),
(14, '바이올린 기초 (1시간)', 40000, NOW(), 'SYSTEM'),
(15, '바이올린 취미 (1시간)', 30000, NOW(), 'SYSTEM'),
(16, '앙상블 클래스 (1시간)', 45000, NOW(), 'SYSTEM'),
(17, '입시 전문 (1시간)',     70000, NOW(), 'SYSTEM'),
(17, '입시 집중 (2시간)',    120000, NOW(), 'SYSTEM');

-- ──────────────────────────────────────────────────────────────
-- student_account: nGrinder 부하 테스트 학생 계정 프로필 (id 37~41)
-- user_account와 @MapsId 관계이므로 student_id = user_account.id
-- ──────────────────────────────────────────────────────────────
INSERT IGNORE INTO student_account (student_id, introduction, created_at, created_by)
VALUES
(37, 'nGrinder 부하 테스트용 학생 계정입니다. (1)', NOW(), 'SYSTEM'),
(38, 'nGrinder 부하 테스트용 학생 계정입니다. (2)', NOW(), 'SYSTEM'),
(39, 'nGrinder 부하 테스트용 학생 계정입니다. (3)', NOW(), 'SYSTEM'),
(40, 'nGrinder 부하 테스트용 학생 계정입니다. (4)', NOW(), 'SYSTEM'),
(41, 'nGrinder 부하 테스트용 학생 계정입니다. (5)', NOW(), 'SYSTEM');