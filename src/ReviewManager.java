import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ReviewManager handles product reviews — add, retrieve, and persist them.
 */
public class ReviewManager {

    private static ReviewManager instance;
    private ArrayList<Review> reviews;

    private ReviewManager() {
        reviews = DatabaseManager.loadReviews();
    }

    public static ReviewManager getInstance() {
        if (instance == null) {
            instance = new ReviewManager();
        }
        return instance;
    }

    public void addReview(Review review) {
        reviews.add(review);
        DatabaseManager.saveReviews(reviews);
    }

    public ArrayList<Review> getProductReviews(int productId) {
        ArrayList<Review> result = new ArrayList<>();
        for (Review r : reviews) {
            if (r.getProductId() == productId) result.add(r);
        }
        return result;
    }

    public double getAverageRating(int productId) {
        ArrayList<Review> pr = getProductReviews(productId);
        if (pr.isEmpty()) return 0;
        double total = 0;
        for (Review r : pr) total += r.getRating();
        return total / pr.size();
    }

    public boolean hasUserReviewed(int productId, String username) {
        for (Review r : reviews) {
            if (r.getProductId() == productId && r.getReviewerUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static class Review implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int productId;
        private final String reviewerUsername;
        private final int rating;
        private final String comment;
        private final String date;

        public Review(int productId, String reviewerUsername, int rating, String comment) {
            this.productId = productId;
            this.reviewerUsername = reviewerUsername;
            this.rating = rating;
            this.comment = comment;
            this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        public int getProductId() { return productId; }
        public String getReviewerUsername() { return reviewerUsername; }
        public int getRating() { return rating; }
        public String getComment() { return comment; }
        public String getDate() { return date; }

        public String getStarRating() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rating; i++) sb.append("★");
            for (int i = rating; i < 5; i++) sb.append("☆");
            return sb.toString();
        }
    }
}
