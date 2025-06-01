package se.reviewservice.service.strategy;

import org.springframework.stereotype.Component;
import se.reviewservice.dto.Group4Response;
import se.reviewservice.dto.Group4Review;
import se.reviewservice.model.Review;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Group4ResponseStrategy implements GroupResponseStrategy {

    @Override
    public boolean supports(String group) {
        // Normalisera gruppnamnet
        if (group == null) return false;

        String normalizedGroup = group.toLowerCase();
        if (normalizedGroup.startsWith("role_")) {
            normalizedGroup = normalizedGroup.substring(5);
        }

        return "group4".equals(normalizedGroup);
    }

    @Override
    public Object buildResponse(List<Review> reviews, String productId) {
        Group4Response response = new Group4Response();

        // Beräkna snittbetyg
        double averageRating = calculateAverageRating(reviews);

        // Formatera snittbetyg som stjärnor
        String formattedRating = formatAverageRatingAsStars(averageRating);
        response.setFormattedRating(formattedRating);

        // Sätt antal recensioner med text "st" efter
        response.setTotalReviews(reviews.size() + " st");

        // Mappa till Group4Review-objekt, bara med recensionstext
        response.setReviews(mapToGroup4Reviews(reviews));

        return response;
    }

    private double calculateAverageRating(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    private String formatAverageRatingAsStars(double averageRating) {
        // Avrundar till närmaste halva stjärna
        double roundedRating = Math.round(averageRating * 2) / 2.0;

        // Skapa en representation med stjärnor (t.ex. "★★★☆☆" för 3 av 5)
        StringBuilder stars = new StringBuilder();

        int fullStars = (int) roundedRating;
        boolean halfStar = roundedRating - fullStars >= 0.5;

        // Lägg till fyllda stjärnor
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }

        // Lägg till en halv stjärna om nödvändigt
        if (halfStar) {
            stars.append("½");
            fullStars++;
        }

        // Lägg till tomma stjärnor
        for (int i = fullStars; i < 5; i++) {
            stars.append("☆");
        }

        return stars.toString() + " (" + roundedRating + " av 5)";
    }

    private List<Group4Review> mapToGroup4Reviews(List<Review> reviews) {
        return reviews.stream()
                .map(this::mapToGroup4Review)
                .collect(Collectors.toList());
    }

    private Group4Review mapToGroup4Review(Review review) {
        Group4Review g4Review = new Group4Review();
        g4Review.setReviewContent(review.getComment());
        return g4Review;
    }
}