package com.lessonmatchingplatform.lesson_matching_platform.chat.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.UserRepository;
import com.lessonmatchingplatform.lesson_matching_platform.chat.domain.ChatMessageDocument;
import com.lessonmatchingplatform.lesson_matching_platform.chat.dto.ChatRoomSummaryDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatRoomService {

        private final MongoTemplate mongoTemplate;
        private final UserRepository userRepository;

        public List<ChatRoomSummaryDto> getRooms(Long currentUserId) {
                // MongoDB Aggregation Pipeline:
                // - $match: currentUserId가 studentId이거나 tutorId인 메시지
                // - $sort: created_at 내림차순 (최신 메시지가 맨 위에 오도록)
                // - $group: studentId, tutorId 기준 단일 방 그룹화 (사전 문의 + 매칭 통합)
                // latestMessage($first), unreadCount($sum: 상대방이 보냈고 !is_read인 경우 1, 아니면 0)
                // - $sort: 방의 최신 메시지 시각(lastMessageAt) 내림차순 정렬

                MatchOperation match = Aggregation.match(
                                new Criteria().orOperator(
                                                Criteria.where("student_id").is(currentUserId),
                                                Criteria.where("tutor_id").is(currentUserId)));

                SortOperation sortCreatedAtDesc = Aggregation.sort(Sort.Direction.DESC, "created_at");

                // unreadCount 계산 조건식: 상대방이 발신자이고 is_read == false 일 때 1, 아니면 0
                ConditionalOperators.Cond unreadCond = ConditionalOperators.when(
                                BooleanOperators.And.and(
                                                ComparisonOperators.Ne.valueOf("sender_id")
                                                                .notEqualToValue(currentUserId),
                                                ComparisonOperators.Eq.valueOf("is_read").equalToValue(false)))
                                .then(1).otherwise(0);

                GroupOperation group = Aggregation.group("student_id", "tutor_id")
                                .first("student_id").as("studentId")
                                .first("tutor_id").as("tutorId")
                                .first("matching_id").as("latestMatchingId")
                                .first("message").as("lastMessage")
                                .first("created_at").as("lastMessageAt")
                                .sum(unreadCond).as("unreadCount");

                SortOperation sortRoomsDesc = Aggregation.sort(Sort.Direction.DESC, "lastMessageAt");

                Aggregation aggregation = Aggregation.newAggregation(
                                match,
                                sortCreatedAtDesc,
                                group,
                                sortRoomsDesc);

                AggregationResults<ChatRoomAggResult> results = mongoTemplate.aggregate(
                                aggregation,
                                ChatMessageDocument.class,
                                ChatRoomAggResult.class);

                List<ChatRoomAggResult> roomAggList = results.getMappedResults();
                if (roomAggList.isEmpty()) {
                        return Collections.emptyList();
                }

                // 2. N+1 문제 해결: 대화 상대(partnerId) 목록을 Set으로 수집 후 1회의 IN 쿼리(findAllById)로 조회
                Set<Long> partnerIds = roomAggList.stream()
                                .map(agg -> agg.getPartnerId(currentUserId))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());

                Map<Long, UserAccount> users = userRepository.findAllById(partnerIds).stream()
                                .collect(Collectors.toMap(UserAccount::getId, Function.identity()));

                // 3. ChatRoomSummaryDto 목록 조립
                return roomAggList.stream()
                                .map(agg -> agg.toDto(currentUserId, users.get(agg.getPartnerId(currentUserId))))
                                .toList();
        }

        @Getter
        @Setter
        public static class ChatRoomAggResult {
                private Long studentId;
                private Long tutorId;
                private Long latestMatchingId;
                private String lastMessage;
                private Object lastMessageAt;
                private long unreadCount;

                public Long getPartnerId(Long currentUserId) {
                        return currentUserId.equals(studentId) ? tutorId : studentId;
                }

                private LocalDateTime resolveLastMessageAt() {
                        if (lastMessageAt instanceof LocalDateTime ldt) {
                                return ldt;
                        } else if (lastMessageAt instanceof Date date) {
                                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                        }
                        return null;
                }

                public ChatRoomSummaryDto toDto(Long currentUserId, UserAccount partner) {
                        Long partnerId = getPartnerId(currentUserId);
                        String partnerName = partner != null ? partner.getName() : null;
                        String roomId = "inquiry:" + studentId + ":" + tutorId;

                        return new ChatRoomSummaryDto(
                                        roomId,
                                        latestMatchingId,
                                        studentId,
                                        tutorId,
                                        partnerId,
                                        partnerName,
                                        lastMessage,
                                        resolveLastMessageAt(),
                                        unreadCount);
                }
        }
}
