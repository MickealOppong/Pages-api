package com.pages.specs;


import com.pages.model.Match_request;
import com.pages.model.Post;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecs {

    // 1. Core Rule: Only show posts that are marked as PUBLIC
    public static Specification<Post> isPublic() {
        return (root, query, cb) -> cb.equal(root.get("visibility"), "PUBLIC");
    }

    // 2. Core Rule: Exclude the user's own posts
    public static Specification<Post> excludeSelf(Long currentUserId) {
        return (root, query, cb) -> cb.notEqual(root.get("appUser").get("id"), currentUserId);
    }

    // 3. Core Rule: Completely exclude anyone the user already has a match interaction history with
    public static Specification<Post> excludeExistingMatches(Long currentUserId) {
        return (root, query, cb) -> {
            // Get the user ID of the post author
            var authorIdExpression = root.get("appUser").get("id");

            // Subquery A: Find all users who received a request from currentUserId
            Subquery<Long> sentRequests = query.subquery(Long.class);
            Root<Match_request> mrSent = sentRequests.from(Match_request.class);
            sentRequests.select(mrSent.get("receiverId").get("id"))
                    .where(cb.equal(mrSent.get("senderId").get("id"), currentUserId));

            // Subquery B: Find all users who sent a request to currentUserId
            Subquery<Long> receivedRequests = query.subquery(Long.class);
            Root<Match_request> mrReceived = receivedRequests.from(Match_request.class);
            receivedRequests.select(mrReceived.get("senderId").get("id"))
                    .where(cb.equal(mrReceived.get("receiverId").get("id"), currentUserId));

            // Enforce that the author must NOT be in either subquery pool
            return cb.and(
                    cb.not(authorIdExpression.in(sentRequests)),
                    cb.not(authorIdExpression.in(receivedRequests))
            );
        };
    }

    // 4. Dynamic Filter: Match exact city
    public static Specification<Post> hasCity(String city) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("appUser").get("city")), city.trim().toLowerCase());
    }

    // 5. Dynamic Filter: Match exact post activity type
    public static Specification<Post> hasActivity(String activity) {
        return (root, query, cb) -> cb.equal(root.get("type"), activity.trim());
    }

    // 6. Dynamic Filter: Enforce minimum and maximum age bracket boundaries
    public static Specification<Post> isWithinAgeRange(Long fromAge, Long toAge) {
        return (root, query, cb) -> {
            var dobExpression = root.get("appUser").get("date_of_birth");

            // Calculate age directly on the SQL database level (Current Year - Birth Year)
            var currentYear = cb.function("YEAR", Integer.class, cb.currentDate());
            var birthYear = cb.function("YEAR", Integer.class, dobExpression);
            var calculatedAge = cb.diff(currentYear, birthYear);

            // Returns SQL: age >= fromAge AND age <= toAge
            return cb.between(calculatedAge.as(Long.class), fromAge, toAge);
        };
    }
}

