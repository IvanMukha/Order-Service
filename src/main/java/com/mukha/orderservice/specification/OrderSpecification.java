package com.mukha.orderservice.specification;

import com.mukha.orderservice.model.BaseEntity;
import com.mukha.orderservice.model.Order;
import com.mukha.orderservice.model.status.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class OrderSpecification {
    public static Specification<Order> createdWithinRange(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get(BaseEntity.Fields.createdAt), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(BaseEntity.Fields.createdAt), startDate));
            } else if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(BaseEntity.Fields.createdAt), endDate));
            }

            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    public static Specification<Order> hasStatuses(List<OrderStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return null;
            }
            return root.get(Order.Fields.status).in(statuses);
        };
    }
    public static Specification<Order> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get(Order.Fields.userId), userId);
        };
    }
}