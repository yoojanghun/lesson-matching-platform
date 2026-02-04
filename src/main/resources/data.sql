INSERT INTO category (name, created_at, created_by, modified_at, modified_by)
VALUES
('PIANO', NOW(), 'system', NULL, NULL),
('VIOLIN', NOW(), 'system', NULL, NULL),
('CELLO', NOW(), 'system', NULL, NULL),
('GUITAR', NOW(), 'system', NULL, NULL),
('DRUM', NOW(), 'system', NULL, NULL),
('VOCAL', NOW(), 'system', NULL, NULL),
('COMPOSITION', NOW(), 'system', NULL, NULL);

INSERT INTO subject (category_id, name, created_at, created_by, modified_at, modified_by)
VALUES
(1, 'PIANO_CLASSICAL', NOW(), 'admin', NULL, NULL),
(1, 'PIANO_JAZZ', NOW(), 'admin', NULL, NULL),
(1, 'PIANO_NEWAGE', NOW(), 'admin', NULL, NULL),
(1, 'PIANO_POP', NOW(), 'admin', NULL, NULL),
(1, 'PIANO_CCM', NOW(), 'admin', NULL, NULL),
(2, 'VIOLIN_CLASSICAL', NOW(), 'admin', NULL, NULL),
(2, 'VIOLIN_JAZZ', NOW(), 'system', NULL, NULL),
(2, 'VIOLIN_POP', NOW(), 'system', NULL, NULL),
(3, 'CELLO_CLASSICAL', NOW(), 'admin', NULL, NULL),
(3, 'CELLO_JAZZ', NOW(), 'admin', NULL, NULL),
(3, 'CELLO_POP', NOW(), 'admin', NULL, NULL),
(4, 'GUITAR_CLASSICAL', NOW(), 'manager', NULL, NULL),
(4, 'GUITAR_ACOUSTIC', NOW(), 'manager', NULL, NULL),
(4, 'GUITAR_ELECTRIC', NOW(), 'manager', NULL, NULL),
(4, 'GUITAR_BASS', NOW(), 'manager', NULL, NULL),
(5, 'DRUM_POP', NOW(), 'system', NULL, NULL),
(5, 'DRUM_JAZZ', NOW(), 'system', NULL, NULL),
(5, 'DRUM_METAL', NOW(), 'system', NULL, NULL),
(6, 'VOCAL_CLASSICAL', NOW(), 'admin', NULL, NULL),
(6, 'VOCAL_MUSICAL', NOW(), 'admin', NULL, NULL),
(6, 'VOCAL_JAZZ', NOW(), 'admin', NULL, NULL),
(6, 'VOCAL_POP', NOW(), 'admin', NULL, NULL),
(7, 'COMPOSITION_CLASSICAL', NOW(), 'admin', NULL, NULL),
(7, 'COMPOSITION_MIDI', NOW(), 'admin', NULL, NULL),
(7, 'COMPOSITION_JAZZ', NOW(), 'admin', NULL, NULL);

INSERT INTO user_account (user_id, user_password, name, role, gender, birth_date, phone_number, email, created_at, created_by, modified_at, modified_by)
VALUES
('kim_piano', 'pass1234', '김여익', 'TUTOR', 'MALE', '1990-05-15', '010-1111-2222', 'kim_piano@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('lee_violin', 'pass5678', '이지윤', 'TUTOR', 'FEMALE', '1992-08-20', '010-2222-3333', 'lee_violin@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('park_cello', 'pw9999', '박정헥', 'TUTOR', 'MALE', '1988-12-01', '010-3333-4444', 'park_cello@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('jung_guitar', 'gtr777', '정발산', 'TUTOR', 'MALE', '1991-07-22', '010-5555-6666', 'jung_gtr@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('kang_vocal', 'vocal000', '강희은', 'TUTOR', 'FEMALE', '1993-11-05', '010-6666-7777', 'kang_vocal@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('yoon_drum', 'drummer1', '윤희재', 'TUTOR', 'MALE', '1989-01-30', '010-7777-8888', 'yoon_drum@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('lim_bass', 'bassline2', '임재연', 'TUTOR', 'FEMALE', '1994-06-14', '010-8888-9999', 'lim_bass@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('piano_master', 'pass123', '마스터', 'TUTOR', 'FEMALE', '1991-03-22', '010-1111-0001', 'master_p@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('jazz_piano_lee', 'pass456', '이지혁', 'TUTOR', 'MALE', '1993-12-10', '010-1111-0002', 'jazz_lee@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('classic_park', 'pass789', '박남정', 'TUTOR', 'FEMALE', '1989-05-30', '010-1111-0003', 'classic_p@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('piano_teacher_choi', 'pass101', '최태민', 'TUTOR', 'MALE', '1994-07-15', '010-1111-0004', 'choi_edu@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('emotion_piano', 'pass202', '김감성', 'TUTOR', 'FEMALE', '1992-02-28', '010-1111-0005', 'emotion_p@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('violin_strad', 'vpass1', '스트라', 'TUTOR', 'MALE', '1990-11-11', '010-2222-0001', 'strad_v@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('bowing_queen', 'vpass2', '보우퀸', 'TUTOR', 'FEMALE', '1995-06-20', '010-2222-0002', 'bow_queen@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('violin_kim_v', 'vpass3', '김바이', 'TUTOR', 'MALE', '1988-08-05', '010-2222-0003', 'kim_v@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('string_expert', 'vpass4', '현전문', 'TUTOR', 'FEMALE', '1991-04-12', '010-2222-0004', 'string_e@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('violin_lesson_pro', 'vpass5', '프로바이', 'TUTOR', 'MALE', '1993-01-25', '010-2222-0005', 'v_pro@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('student_aa', 'stud123', '안소희', 'STUDENT', 'MALE', '2005-04-12', '010-1234-1111', 'aa_stud@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('student_bb', 'stud456', '배수지', 'STUDENT', 'FEMALE', '2007-09-21', '010-2345-2222', 'bb_stud@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('music_lover', 'love888', '사랑해', 'STUDENT', 'MALE', '1998-12-30', '010-3456-3333', 'lover@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('hobby_piano', 'pnpn99', '취미생', 'STUDENT', 'FEMALE', '1992-05-05', '010-4567-4444', 'hobby_p@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('beginner_v', 'begin1', '초보자', 'STUDENT', 'MALE', '2001-08-15', '010-5678-5555', 'begin_v@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('star_dust', 'star77', '별가루', 'STUDENT', 'FEMALE', '2003-01-11', '010-6789-6666', 'star_d@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('blue_note', 'blue00', '블루노', 'STUDENT', 'MALE', '1995-10-22', '010-7890-7777', 'blue_n@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('classic_fan', 'fan123', '클래식팬', 'STUDENT', 'FEMALE', '1988-07-07', '010-8901-8888', 'classic_f@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('jazz_man', 'jazzman1', '재즈맨', 'STUDENT', 'MALE', '1990-11-11', '010-9012-9999', 'jazz_m@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('dreamer_99', 'dream99', '꿈나무', 'STUDENT', 'FEMALE', '1999-03-03', '010-0123-0000', 'dreamer@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('highschool_p', 'pass111', '고딩이', 'STUDENT', 'MALE', '2008-05-15', '010-3333-1001', 'student_h@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('cello_beginner', 'pass222', '첼린이', 'STUDENT', 'FEMALE', '1996-11-02', '010-3333-1002', 'cello_b@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('winter_sonata', 'pass333', '겨울연가', 'STUDENT', 'FEMALE', '1985-01-20', '010-3333-1003', 'winter@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('rock_spirit', 'pass444', '락스피릿', 'STUDENT', 'MALE', '2002-07-07', '010-3333-1004', 'rocker@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('melody_maker', 'pass555', '멜로디', 'STUDENT', 'FEMALE', '1994-03-30', '010-3333-1005', 'melody@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('daily_music', 'pass666', '데일리', 'STUDENT', 'MALE', '1991-09-12', '010-3333-1006', 'daily@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('vocal_dream', 'pass777', '보컬꿈', 'STUDENT', 'FEMALE', '2006-12-25', '010-3333-1007', 'v_dream@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('unni_piano', 'pass888', '피아노언니', 'STUDENT', 'FEMALE', '1989-06-18', '010-3333-1008', 'unni@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL),
('mozar_k', 'pass999', '모차르트', 'STUDENT', 'MALE', '2009-02-14', '010-3333-1009', 'mozar@example.com', CURRENT_TIMESTAMP, 'system', NULL, NULL);

INSERT INTO tutor_account (tutor_id, introduction, career, title, content, created_at, created_by, modified_at, modified_by)
VALUES
(1, '기초부터 탄탄하게 가르치는 피아니스트 김철수입니다.', '한예종 졸업, 독일 유학 5년, 다수 콩쿠르 입상', '지루한 체르니는 그만! 뉴에이지부터 클래식까지 1:1 맞춤 레슨', '수강생의 수준에 맞춰 커리큘럼을 조정합니다. 성인 취미반 적극 환영합니다.', NOW(), 'system', NULL, NULL),
(2, '바이올린의 아름다운 선율을 함께 만들어봐요.', '시립교향악단 단원 역임, 레슨 경력 10년', '초보자도 3개월 만에 한 곡 마스터! 바이올린 기초 완성', '정확한 자세와 활 쓰기 기법을 중심으로 지도합니다. 악기 대여 상담도 가능해요.', NOW(), 'system', NULL, NULL),
(3, '첼로의 깊은 울림을 전달하는 박현우입니다.', '해외 음악제 초청 연주, 예술고등학교 출강', '직장인을 위한 힐링 첼로 클래스 (저녁 시간대 가능)', '바쁜 일상 속에서 악기를 배우며 힐링하고 싶은 분들을 위해 유연한 스케줄을 제공합니다.', NOW(), 'system', NULL, NULL),
(4, '통기타부터 일렉기타까지, 리듬을 즐겨보세요.', '실용음악과 졸업, 밴드 세션 활동 중', '한 달 만에 완성하는 통기타 코드와 스트로크', '자신이 좋아하는 곡을 직접 연주하며 노래할 수 있도록 실전 위주로 가르칩니다.', NOW(), 'system', NULL, NULL),
(5, '당신이 가진 최고의 악기, 목소리를 찾아드립니다.', '유명 오디션 프로그램 코칭, 보컬 트레이너 경력', '고음 불가 탈출! 호흡부터 발성까지 체계적인 보컬 트레이닝', '성대 결절 예방과 올바른 발성법을 통해 자신감 있게 노래할 수 있게 돕습니다.', NOW(), 'system', NULL, NULL),
(6, '스트레스를 한 번에 날리는 파워풀한 드럼 레슨!', '재즈 클럽 정기 공연, 드럼 전문 아카데미 운영', '박치 탈출! 기초 비트부터 화려한 필인까지', '리듬감이 없어도 괜찮습니다. 메트로놈과 친해지는 법부터 차근차근 알려드립니다.', NOW(), 'system', NULL, NULL),
(7, '음악의 뼈대, 베이스 기타의 매력에 빠져보세요.', '실용음악과 석사, 다수 인디 밴드 앨범 참여', '그루브가 살아있는 베이스 기타 중급/입문 레슨', '단순 반복이 아닌, 곡의 흐름을 이해하는 연주법을 중심으로 레슨을 진행합니다.', NOW(), 'system', NULL, NULL),
(8, '러시아 유학파 출신의 정통 클래식 피아노 레슨입니다.', '모스크바 국립 음악원 졸업, 독주회 10회 이상', '기초부터 고급 입시까지, 정통 클래식 피아노의 정석', '테크닉뿐만 아니라 곡의 해석과 감수성을 풍부하게 표현하는 방법을 지도합니다.', NOW(), 'system', NULL, NULL),
(9, '딱딱한 악보 대신 리듬을 배우는 재즈 피아노입니다.', '실용음악과 졸업, 재즈 밴드 리더', '코드 반주부터 화려한 즉흥 연주까지! 재즈 피아노 입문', '팝, 재즈, 가요 반주를 원하는 분들을 위한 맞춤형 커리큘럼을 제공합니다.', NOW(), 'system', NULL, NULL),
(10, '누구나 쉽고 재미있게 시작하는 성인 취미 피아노입니다.', '음악교육 석사, 개인 레슨 경력 8년', '퇴근 후 나만의 힐링 시간, 한 달 한 곡 완성 프로젝트', '악보를 전혀 못 보셔도 괜찮습니다. 친절하게 기초부터 알려드려요.', NOW(), 'system', NULL, NULL),
(11, '아이들의 눈높이에 맞춘 즐거운 음악 수업입니다.', '유아 음악 지도사 자격증 보유', '창의력을 키워주는 어린이 놀이 피아노 레슨', '단순 연습이 아닌 음악적 놀이를 통해 자연스럽게 피아노와 친해지게 합니다.', NOW(), 'system', NULL, NULL),
(12, '마음을 울리는 서정적인 뉴에이지 연주를 가르칩니다.', '음반 3장 발매, 작곡가 활동 중', '영화 OST, 뉴에이지 명곡 마스터 클래스', '히사이시 조, 이루마 등 감성적인 곡들을 직접 연주해보고 싶은 분들을 모십니다.', NOW(), 'system', NULL, NULL),
(13, '독일 오케스트라 수석 출신의 디테일한 레슨입니다.', '독일 베를린 국립음대 졸업, 현직 오케스트라 수석', '전공생 및 오케스트라 오디션 대비 심화 클래스', '보잉의 각도 하나까지 세밀하게 교정하여 최고의 소리를 찾아드립니다.', NOW(), 'system', NULL, NULL),
(14, '바이올린은 첫 자세가 가장 중요합니다.', '음대 바이올린 전공, 청소년 오케스트라 지도', '바이올린 기초 탄탄! 예쁜 소리 만들기 프로젝트', '어깨 통증 없는 올바른 자세와 정확한 음정을 잡는 법을 집중 교육합니다.', NOW(), 'system', NULL, NULL),
(15, '취미로 시작해서 평생 친구가 되는 바이올린 레슨', '레슨 경력 12년, 다수 동호회 지도', '성인 취미반 전문! 쉽고 빠르게 배우는 바이올린', '지루한 연습곡 대신 유명한 소품곡 위주로 쉽고 재미있게 수업합니다.', NOW(), 'system', NULL, NULL),
(16, '현악 사중주 등 앙상블 수업도 가능한 전문가입니다.', '실내악 앙상블 팀 운영, 대학 출강', '함께 연주하는 즐거움, 바이올린 앙상블 및 개인 레슨', '혼자 연주하는 것을 넘어 다른 악기와 호흡을 맞추는 방법까지 배울 수 있습니다.', NOW(), 'system', NULL, NULL),
(17, '입시와 콩쿠르 준비를 위한 확실한 성과 중심 레슨', '국내 유명 콩쿠르 심사위원, 예고 입시 합격자 다수 배출', '확실한 실력 향상! 예중/예고/음대 입시 전문 지도', '수강생의 단점을 정확히 파악하여 단기간에 실력을 끌어올리는 전략적 레슨입니다.', NOW(), 'system', NULL, NULL);

INSERT INTO category_tutor (tutor_id, category_id, created_at, created_by, modified_at, modified_by)
VALUES
(1, 1, NOW(), 'system', NULL, NULL),
(2, 2, NOW(), 'system', NULL, NULL),
(3, 3, NOW(), 'system', NULL, NULL),
(4, 4, NOW(), 'system', NULL, NULL),
(5, 6, NOW(), 'system', NULL, NULL),
(6, 5, NOW(), 'system', NULL, NULL),
(7, 4, NOW(), 'system', NULL, NULL),
(8, 1, NOW(), 'system', NULL, NULL),
(9, 1, NOW(), 'system', NULL, NULL),
(10, 1, NOW(), 'system', NULL, NULL),
(11, 1, NOW(), 'system', NULL, NULL),
(12, 1, NOW(), 'system', NULL, NULL),
(13, 2, NOW(), 'system', NULL, NULL),
(14, 2, NOW(), 'system', NULL, NULL),
(15, 2, NOW(), 'system', NULL, NULL),
(16, 2, NOW(), 'system', NULL, NULL),
(17, 2, NOW(), 'system', NULL, NULL);

INSERT INTO subject_tutor (tutor_id, subject_id, created_at, created_by, modified_at, modified_by)
VALUES
(1, 1, NOW(), 'system', NULL, NULL), (1, 3, NOW(), 'system', NULL, NULL),
(2, 6, NOW(), 'system', NULL, NULL),
(3, 9, NOW(), 'system', NULL, NULL), (3, 11, NOW(), 'system', NULL, NULL),
(4, 13, NOW(), 'system', NULL, NULL), (4, 14, NOW(), 'system', NULL, NULL),
(5, 19, NOW(), 'system', NULL, NULL), (5, 21, NOW(), 'system', NULL, NULL), (5, 22, NOW(), 'system', NULL, NULL),
(6, 16, NOW(), 'system', NULL, NULL), (6, 17, NOW(), 'system', NULL, NULL), (6, 18, NOW(), 'system', NULL, NULL),
(7, 15, NOW(), 'system', NULL, NULL),
(8, 1, NOW(), 'system', NULL, NULL),
(9, 2, NOW(), 'system', NULL, NULL), (9, 4, NOW(), 'system', NULL, NULL),
(10, 4, NOW(), 'system', NULL, NULL), (10, 5, NOW(), 'system', NULL, NULL),
(11, 1, NOW(), 'system', NULL, NULL), (11, 3, NOW(), 'system', NULL, NULL),
(12, 3, NOW(), 'system', NULL, NULL), (12, 4, NOW(), 'system', NULL, NULL),
(13, 6, NOW(), 'system', NULL, NULL),
(14, 6, NOW(), 'system', NULL, NULL), (14, 8, NOW(), 'system', NULL, NULL),
(15, 7, NOW(), 'system', NULL, NULL), (15, 8, NOW(), 'system', NULL, NULL),
(16, 6, NOW(), 'system', NULL, NULL), (16, 7, NOW(), 'system', NULL, NULL),
(17, 6, NOW(), 'system', NULL, NULL);

INSERT INTO location (name, created_at, created_by, modified_at, modified_by)
VALUES
('서울', NOW(), 'system', NULL, NULL),
('경기', NOW(), 'system', NULL, NULL),
('인천', NOW(), 'system', NULL, NULL),
('대전', NOW(), 'system', NULL, NULL),
('대구', NOW(), 'system', NULL, NULL),
('부산', NOW(), 'system', NULL, NULL),
('울산', NOW(), 'system', NULL, NULL),
('광주', NOW(), 'system', NULL, NULL),
('세종', NOW(), 'system', NULL, NULL),
('강원', NOW(), 'system', NULL, NULL),
('충북', NOW(), 'system', NULL, NULL),
('충남', NOW(), 'system', NULL, NULL),
('전북', NOW(), 'system', NULL, NULL),
('전남', NOW(), 'system', NULL, NULL),
('경북', NOW(), 'system', NULL, NULL),
('경남', NOW(), 'system', NULL, NULL),
('제주', NOW(), 'system', NULL, NULL);

INSERT INTO location_tutor (tutor_id, location_id, created_at, created_by, modified_at, modified_by)
VALUES
(1, 1, NOW(), 'system', NULL, NULL),
(2, 1, NOW(), 'system', NULL, NULL),
(3, 1, NOW(), 'system', NULL, NULL),
(4, 2, NOW(), 'system', NULL, NULL),
(5, 3, NOW(), 'system', NULL, NULL),
(6, 2, NOW(), 'system', NULL, NULL),
(7, 5, NOW(), 'system', NULL, NULL),
(8, 4, NOW(), 'system', NULL, NULL),
(9, 8, NOW(), 'system', NULL, NULL),
(10, 10, NOW(), 'system', NULL, NULL),
(11, 10, NOW(), 'system', NULL, NULL),
(12, 15, NOW(), 'system', NULL, NULL),
(13, 15, NOW(), 'system', NULL, NULL),
(14, 15, NOW(), 'system', NULL, NULL),
(15, 13, NOW(), 'system', NULL, NULL),
(16, 14, NOW(), 'system', NULL, NULL),
(17, 12, NOW(), 'system', NULL, NULL);

INSERT INTO student_account (student_id, introduction, location, created_at, created_by, modified_at, modified_by)
VALUES
(18, '음악을 전공하고 싶어 기초부터 배우려는 학생입니다.', '서울 관악구', NOW(), 'system', NULL, NULL),
(19, '방과 후 활동으로 바이올린을 배우고 싶은 학생입니다.', '경기 안양시', NOW(), 'system', NULL, NULL),
(20, '클래식 음악을 좋아해서 직접 연주해보고 싶어 가입했습니다.', '서울 서초구', NOW(), 'system', NULL, NULL),
(21, '어릴 때 그만둔 피아노를 다시 취미로 시작하려는 직장인입니다.', '서울 성동구', NOW(), 'system', NULL, NULL),
(22, '노래를 잘 부르고 싶어서 발성부터 배우고 싶은 대학생입니다.', '경기 부천시', NOW(), 'system', NULL, NULL),
(23, 'SNS에 연주 영상을 올리는 게 목표입니다. 감성적인 곡 추천 부탁드려요.', '서울 강서구', NOW(), 'system', NULL, NULL),
(24, '재즈의 즉흥 연주 매력에 빠져 재즈 피아노를 배우고 싶습니다.', '서울 용산구', NOW(), 'system', NULL, NULL),
(25, '태교를 위해 첼로 연주를 시작해보려고 합니다.', '서울 송파구', NOW(), 'system', NULL, NULL),
(26, '취미 밴드에서 드럼을 맡고 있습니다. 기본기를 더 다지고 싶네요.', '서울 은평구', NOW(), 'system', NULL, NULL),
(27, '자작곡을 만들고 싶은데 화성학 레슨도 같이 해주실 분 찾습니다.', '경기 남양주시', NOW(), 'system', NULL, NULL),
(28, '음대 입시를 준비하고 있는 고등학생입니다. 기본기부터 다시 배우고 싶어요.', '서울 강남구', NOW(), 'system', NULL, NULL),
(29, '평소에 꿈꾸던 첼로를 시작해보려 합니다. 악기 대여가 가능한 곳을 찾고 있어요.', '경기 성남시', NOW(), 'system', NULL, NULL),
(30, '취미로 피아노를 다시 시작하고 싶은 주부입니다. 오전 시간대 레슨을 선호합니다.', '서울 송파구', NOW(), 'system', NULL, NULL),
(31, '대학교 밴드 동아리에서 일렉기타를 맡게 되었습니다. 속주 테크닉을 배우고 싶습니다.', '서울 마포구', NOW(), 'system', NULL, NULL),
(32, '작곡을 전공하고 있는데, 편곡을 위해 건반 연주 실력을 키우고 싶습니다.', '서울 서대문구', NOW(), 'system', NULL, NULL),
(33, '직장인 동호회에서 활동 중인 베이스 연주자입니다. 슬랩 테크닉이 고민이라 신청합니다.', '경기 용인시', NOW(), 'system', NULL, NULL),
(34, '아이돌 지망생입니다. 고음 발성과 무대 매너를 체계적으로 배우고 싶습니다.', '서울 광진구', NOW(), 'system', NULL, NULL),
(35, '어릴 때 배웠던 피아노를 성인이 되어 다시 시작합니다. 뉴에이지 곡 위주로 배우고 싶어요.', '경기 수원시', NOW(), 'system', NULL, NULL),
(36, '클래식 작곡과 진학을 희망하는 중학생입니다. 이론과 실기를 병행하고 싶습니다.', '서울 노원구', NOW(), 'system', NULL, NULL);

INSERT INTO matching (student_id, tutor_id, request_msg, status, created_at, created_by, modified_at, modified_by)
VALUES
(18, 1, '기초부터 체계적으로 배우고 싶습니다. 주말 레슨 가능할까요?', 'ACCEPTED', NOW(), 'student_aa', NULL, NULL),
(21, 10, '직장인이라 저녁 7시 이후 수업을 희망합니다.', 'PENDING', NOW(), 'hobby_piano', NULL, NULL),
(35, 12, '뉴에이지 곡 위주로 배우고 싶어요. 잘 부탁드립니다!', 'ACCEPTED', NOW(), 'unni_piano', NULL, NULL),
(19, 2, '학교 방과후 수업이랑 병행하려고 합니다. 악기 대여 가능한가요?', 'PENDING', NOW(), 'student_bb', NULL, NULL),
(23, 14, '유튜브에 연주 영상 올리는 게 목표입니다! 자세 교정 부탁드려요.', 'REJECTED', NOW(), 'star_dust', NULL, NULL),
(31, 4, '밴드 공연이 한 달 남았습니다. 일렉기타 속주 집중 레슨 부탁드립니다.', 'ACCEPTED', NOW(), 'rock_spirit', NULL, NULL),
(22, 5, '고음 발성법이 너무 궁금합니다. 테스트 한 번 받아보고 싶어요.', 'PENDING', NOW(), 'beginner_v', NULL, NULL),
(26, 6, '기본기부터 다시 다지고 싶은 7년 차 취미 드러머입니다.', 'ACCEPTED', NOW(), 'jazz_man', NULL, NULL),
(29, 3, '첼로 소리가 너무 좋아서 시작하려 합니다. 완전 초보인데 괜찮나요?', 'PENDING', NOW(), 'cello_beginner', NULL, NULL);

INSERT INTO lesson_review (matching_id, content, rating, created_at, created_by, modified_at, modified_by)
VALUES
(1, '선생님이 정말 친절하시고 기초를 탄탄하게 잡아주셔서 좋아요. 주말 시간도 잘 맞춰주십니다!', 5, NOW(), 'student_aa', NULL, NULL),
(3, '뉴에이지 곡 위주로 배우고 싶었는데, 제가 딱 원하는 곡들로 커리큘럼을 짜주셔서 너무 즐거워요.', 5, NOW(), 'unni_piano', NULL, NULL),
(6, '공연 준비 때문에 급하게 요청드렸는데 속주 팁을 정말 잘 알려주셨어요. 덕분에 무사히 공연 마쳤습니다!', 4, NOW(), 'rock_spirit', NULL, NULL),
(8, '7년 동안 독학하면서 놓쳤던 나쁜 습관들을 바로잡아 주셨습니다. 역시 전문가는 다르네요.', 5, NOW(), 'jazz_man', NULL, NULL);