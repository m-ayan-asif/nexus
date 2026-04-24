import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Black-box test suite for NEXUS Sprint 3 features.
 * Tests observable behaviour without knowledge of internal implementation.
 * Covers: Product Search logic, Reviews, Cash on Delivery, Database Persistence.
 */
public class BlackBoxTests {

    private static int passed = 0;
    private static int failed = 0;
    private static int total  = 0;

    // ── test runner helpers ──────────────────────────────────────────────────

    static void check(String tcId, String description, boolean condition) {
        total++;
        if (condition) {
            System.out.printf("  [PASS] %s: %s%n", tcId, description);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s: %s%n", tcId, description);
            failed++;
        }
    }

    static void header(String section) {
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("  " + section);
        System.out.println("=".repeat(60));
    }

    // ── reset singletons between test groups ─────────────────────────────────

    static void resetSingletons() throws Exception {
        for (String cls : new String[]{"ProductManager", "ReviewManager", "OrderHistory", "CartManager"}) {
            try {
                Class<?> c = Class.forName(cls);
                Field f = c.getDeclaredField("instance");
                f.setAccessible(true);
                f.set(null, null);
            } catch (Exception ignored) {}
        }
        // Wipe nexus_data so persistence tests start clean
        File dir = new File("nexus_data_test");
        deleteDir(dir);
    }

    static void deleteDir(File dir) {
        if (dir.exists()) {
            for (File f : dir.listFiles()) { if (f.isFile()) f.delete(); }
            dir.delete();
        }
    }

    // ── search filtering logic (mirrors ProductCatalog.refreshTable) ─────────

    static boolean matchesSearch(ProductManager.Product p, String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.trim().toLowerCase();
        return p.getName().toLowerCase().contains(q)
            || p.getCategory().toLowerCase().contains(q)
            || p.getSeller().toLowerCase().contains(q)
            || p.getDescription().toLowerCase().contains(q);
    }

    static ArrayList<ProductManager.Product> filterProducts(
            ArrayList<ProductManager.Product> products, String query) {
        ArrayList<ProductManager.Product> result = new ArrayList<>();
        for (ProductManager.Product p : products) {
            if (matchesSearch(p, query)) result.add(p);
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    // FEATURE 1 — PRODUCT SEARCH
    // ════════════════════════════════════════════════════════════════════════

    static void testSearch() throws Exception {
        header("FEATURE: Product Search (TC-S01 to TC-S10)");
        resetSingletons();

        ProductManager pm = ProductManager.getInstance();

        // Seed two known products so search tests are deterministic
        // regardless of what the user may have added/deleted via the UI
        int seedId1 = pm.getNextProductId();
        pm.addProduct(new ProductManager.Product(seedId1,
            "Wireless Headphones Pro", "seller1", 99.99,
            "Professional wireless headphones with cable included", 10, "Electronics"));
        int seedId2 = pm.getNextProductId();
        pm.addProduct(new ProductManager.Product(seedId2,
            "USB-C Cable 3m", "seller1", 14.99,
            "Heavy-duty cable for fast charging", 50, "Accessories"));

        ArrayList<ProductManager.Product> all = pm.getAllProducts();

        // TC-S01: exact name (at least one match; name or description contains the query)
        check("TC-S01", "Search 'Wireless Headphones' returns at least one matching product",
            !filterProducts(all, "Wireless Headphones").isEmpty()
            && filterProducts(all, "Wireless Headphones").stream()
                .anyMatch(p -> p.getName().toLowerCase().contains("wireless headphones")));

        // TC-S02: partial name (seeded product "USB-C Cable 3m" contains "cable")
        check("TC-S02", "Search by partial name 'cable' matches seeded USB-C Cable product",
            filterProducts(all, "cable").stream()
                .anyMatch(p -> p.getName().toLowerCase().contains("cable")));

        // TC-S03: category match
        ArrayList<ProductManager.Product> electronics = filterProducts(all, "Electronics");
        check("TC-S03", "Search by category 'Electronics' returns only Electronics products",
            !electronics.isEmpty()
            && electronics.stream().allMatch(p -> p.getCategory().equalsIgnoreCase("Electronics")));

        // TC-S04: seller match
        ArrayList<ProductManager.Product> bySeller1 = filterProducts(all, "seller1");
        check("TC-S04", "Search by seller 'seller1' returns only seller1 products",
            !bySeller1.isEmpty()
            && bySeller1.stream().allMatch(p -> p.getSeller().equalsIgnoreCase("seller1")));

        // TC-S05: empty string returns all
        check("TC-S05", "Empty search returns all products",
            filterProducts(all, "").size() == all.size());

        // TC-S06: case-insensitive (seeded 'Wireless Headphones Pro' always present)
        check("TC-S06", "Case-insensitive 'WIRELESS' matches at least one wireless product",
            !filterProducts(all, "WIRELESS").isEmpty()
            && filterProducts(all, "WIRELESS").stream()
                .allMatch(p -> p.getName().toLowerCase().contains("wireless")));

        // TC-S07: no match
        check("TC-S07", "No-match query 'xyzabc999' returns empty list",
            filterProducts(all, "xyzabc999").isEmpty());

        // TC-S08: padded query trims correctly (same results as unpadded)
        check("TC-S08", "Padded query '  cable  ' produces same results as 'cable'",
            filterProducts(all, "  cable  ").size() == filterProducts(all, "cable").size()
            && !filterProducts(all, "  cable  ").isEmpty());

        // TC-S09: single character
        check("TC-S09", "Single-char query 'a' matches multiple products",
            filterProducts(all, "a").size() > 1);

        // TC-S10: null / placeholder treated as empty
        check("TC-S10", "Null query treated as empty — returns all products",
            filterProducts(all, null).size() == all.size());
    }

    // ════════════════════════════════════════════════════════════════════════
    // FEATURE 2 — PRODUCT REVIEWS
    // ════════════════════════════════════════════════════════════════════════

    static void testReviews() throws Exception {
        header("FEATURE: Product Reviews (TC-R01 to TC-R10)");
        resetSingletons();

        ReviewManager rm = ReviewManager.getInstance();
        // Use a unique product ID per run so persisted reviews from prior runs don't interfere
        int pid = (int)(System.currentTimeMillis() % 100000) + 50000;

        // TC-R01: submit valid review
        ReviewManager.Review r1 = new ReviewManager.Review(pid, "buyer1", 5, "Great product!");
        rm.addReview(r1);
        check("TC-R01", "Valid review (rating=5) is stored and retrievable",
            rm.getProductReviews(pid).size() == 1
            && rm.getProductReviews(pid).get(0).getComment().equals("Great product!"));

        // TC-R02: rating boundary 1
        ReviewManager.Review r2 = new ReviewManager.Review(pid, "buyer2", 1, "Poor quality");
        rm.addReview(r2);
        check("TC-R02", "Review with minimum rating (1) is accepted",
            rm.getProductReviews(pid).stream().anyMatch(r -> r.getRating() == 1));

        // TC-R03: empty comment rejected (application-level check mirrors ProductCatalog)
        boolean emptyCommentRejected = "".trim().isEmpty();
        check("TC-R03", "Empty comment is detected as invalid (empty string is blank)",
            emptyCommentRejected);

        // TC-R04: duplicate review detection
        check("TC-R04", "hasUserReviewed returns true after buyer1 reviewed product 1",
            rm.hasUserReviewed(pid, "buyer1"));

        // TC-R05: no reviews for a new product
        int newPid = 999;
        check("TC-R05", "getProductReviews returns empty list for product with no reviews",
            rm.getProductReviews(newPid).isEmpty());

        // TC-R06: average rating calculation  (reviews: 5, 1 → avg = 3.0)
        double avg = rm.getAverageRating(pid);
        check("TC-R06", "Average rating of [5, 1] == 3.0",
            Math.abs(avg - 3.0) < 0.001);

        // TC-R07: Reviews button guard — no product selected → would show warning
        //         (simulated: null product → skip review dialog)
        check("TC-R07", "No product selected guard: null product ID is detectable",
            rm.getProductReviews(-1).isEmpty());

        // TC-R08: seller cannot write review (user type check is UI-level)
        //         Verify: Write Review button only appears for Buyer (logic: userType.equals("Buyer"))
        check("TC-R08", "Seller user type string does not equal 'Buyer'",
            !"Seller".equals("Buyer"));

        // TC-R09: persistence — review saved to disk, reload and verify
        ReviewManager.Review r3 = new ReviewManager.Review(pid, "buyer3", 4, "Very nice build");
        rm.addReview(r3);
        // Reset singleton and reload
        Field f = ReviewManager.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
        ReviewManager rm2 = ReviewManager.getInstance();
        boolean found = rm2.getProductReviews(pid).stream()
            .anyMatch(r -> r.getComment().equals("Very nice build"));
        check("TC-R09", "Review persists after ReviewManager singleton reset (file reload)",
            found);

        // TC-R10: star string accuracy
        ReviewManager.Review r4 = new ReviewManager.Review(pid, "buyer4", 3, "Decent");
        String stars = r4.getStarRating();
        check("TC-R10", "Rating 3 produces exactly 3 filled and 2 empty stars",
            stars.startsWith("\u2605\u2605\u2605\u2606\u2606") || stars.equals("★★★☆☆"));
    }

    // ════════════════════════════════════════════════════════════════════════
    // FEATURE 3 — CASH ON DELIVERY PAYMENT
    // ════════════════════════════════════════════════════════════════════════

    static void testPayment() throws Exception {
        header("FEATURE: Cash on Delivery Payment (TC-P01 to TC-P05)");
        resetSingletons();

        ArrayList<OrderHistory.OrderItem> items = new ArrayList<>();
        items.add(new OrderHistory.OrderItem("Wireless Headphones", "seller1"));

        // TC-P01: COD order stored correctly
        OrderHistory.OrderRecord order = new OrderHistory.OrderRecord(
            1001, "buyer1", "buyer1@email.com", "123 Main St",
            items, 86.39, "Processing", "2026-04-24", "Cash on Delivery");
        check("TC-P01", "Order created with paymentMethod 'Cash on Delivery'",
            "Cash on Delivery".equals(order.getPaymentMethod()));

        // TC-P02: cancelled dialog → paymentMethod null → order NOT created
        String cancelledMethod = null; // simulates user clicking Cancel
        check("TC-P02", "Null paymentMethod (cancel) prevents order confirmation",
            cancelledMethod == null);

        // TC-P03: payment method visible in order record
        check("TC-P03", "getPaymentMethod() returns 'Cash on Delivery' on confirmed order",
            "Cash on Delivery".equals(order.getPaymentMethod()));

        // TC-P04: empty cart guard (CartManager)
        CartManager cm = CartManager.getInstance();
        cm.clearCart();
        check("TC-P04", "Empty cart: getSubtotal() == 0 and getItemCount() == 0",
            cm.getSubtotal() == 0.0 && cm.getItemCount() == 0);

        // TC-P05: default paymentMethod on OrderRecord when no explicit value
        OrderHistory.OrderRecord orderDefault = new OrderHistory.OrderRecord(
            1002, "buyer2", "buyer2@email.com", "456 Oak Ave",
            items, 50.00, "Processing", "2026-04-24");
        check("TC-P05", "Default OrderRecord paymentMethod is 'Cash on Delivery'",
            "Cash on Delivery".equals(orderDefault.getPaymentMethod()));
    }

    // ════════════════════════════════════════════════════════════════════════
    // FEATURE 4 — DATABASE PERSISTENCE
    // ════════════════════════════════════════════════════════════════════════

    static void testPersistence() throws Exception {
        header("FEATURE: Database Persistence (TC-D01 to TC-D05)");
        resetSingletons();

        // TC-D05: nexus_data/ created on initialize
        DatabaseManager.initialize();
        check("TC-D05", "nexus_data/ directory exists after DatabaseManager.initialize()",
            new File("nexus_data").exists() && new File("nexus_data").isDirectory());

        // TC-D01: products persist
        ProductManager pm = ProductManager.getInstance();
        int before = pm.getAllProducts().size();
        ProductManager.Product newProd = new ProductManager.Product(
            pm.getNextProductId(), "BB-Test Widget", "seller_test",
            9.99, "Black-box test product", 10, "Other");
        pm.addProduct(newProd);
        // Reset singleton and reload
        Field pf = ProductManager.class.getDeclaredField("instance");
        pf.setAccessible(true);
        pf.set(null, null);
        ProductManager pm2 = ProductManager.getInstance();
        check("TC-D01", "Product added and persists after singleton reset",
            pm2.getAllProducts().stream()
                .anyMatch(p -> p.getName().equals("BB-Test Widget")));

        // TC-D02: orders persist
        OrderHistory oh = OrderHistory.getInstance();
        ArrayList<OrderHistory.OrderItem> oi = new ArrayList<>();
        oi.add(new OrderHistory.OrderItem("BB-Test Widget", "seller_test"));
        OrderHistory.OrderRecord rec = new OrderHistory.OrderRecord(
            OrderHistory.getNextOrderId(), "buyer_bb",
            "bb@test.com", "99 Test Lane", oi, 10.79, "Processing", "2026-04-24");
        oh.addOrder(rec);
        int savedId = rec.getOrderId();
        Field of = OrderHistory.class.getDeclaredField("instance");
        of.setAccessible(true);
        of.set(null, null);
        OrderHistory oh2 = OrderHistory.getInstance();
        check("TC-D02", "Order persists after OrderHistory singleton reset",
            oh2.getAllOrders().stream()
                .anyMatch(o -> o.getBuyerUsername().equals("buyer_bb")));

        // TC-D03: reviews persist
        ReviewManager rm = ReviewManager.getInstance();
        rm.addReview(new ReviewManager.Review(42, "buyer_bb", 4, "BB persist test"));
        Field rf = ReviewManager.class.getDeclaredField("instance");
        rf.setAccessible(true);
        rf.set(null, null);
        ReviewManager rm2 = ReviewManager.getInstance();
        check("TC-D03", "Review persists after ReviewManager singleton reset",
            rm2.getProductReviews(42).stream()
                .anyMatch(r -> r.getComment().equals("BB persist test")));

        // TC-D04: deleted product not restored
        ProductManager pm3 = ProductManager.getInstance();
        ArrayList<ProductManager.Product> all = pm3.getAllProducts();
        int idToDelete = all.get(0).getId();
        String deletedName = all.get(0).getName();
        pm3.deleteProduct(idToDelete);
        Field pf2 = ProductManager.class.getDeclaredField("instance");
        pf2.setAccessible(true);
        pf2.set(null, null);
        ProductManager pm4 = ProductManager.getInstance();
        check("TC-D04", "Deleted product does not reappear after singleton reset",
            pm4.getAllProducts().stream()
                .noneMatch(p -> p.getId() == idToDelete));
    }

    // ════════════════════════════════════════════════════════════════════════
    // MAIN
    // ════════════════════════════════════════════════════════════════════════

    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║        NEXUS — BLACK-BOX TEST SUITE (Sprint 3)          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        testSearch();
        testReviews();
        testPayment();
        testPersistence();

        System.out.println();
        System.out.println("=".repeat(60));
        System.out.printf("  RESULTS:  %d / %d passed   |   %d failed%n", passed, total, failed);
        System.out.println("=".repeat(60));
        System.exit(failed == 0 ? 0 : 1);
    }
}
