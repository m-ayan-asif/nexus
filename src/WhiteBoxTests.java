import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * White-box test suite for NEXUS Sprint 3.
 * Tests internal logic with explicit branch/statement coverage tracking.
 *
 * Modules covered:
 *   ReviewManager   — addReview, hasUserReviewed, getAverageRating, getProductReviews
 *   DatabaseManager — loadData (3 branches: missing file, valid file, corrupt file)
 *   ProductManager  — getProduct (found / not found), deleteProduct, updateProduct
 *   OrderHistory    — getUserOrders, getSellerOrders, getNextOrderId, nextOrderId restore
 *   CartManager     — addItem (new / existing), removeItem, getSubtotal
 */
public class WhiteBoxTests {

    private static int passed  = 0;
    private static int failed  = 0;
    private static int total   = 0;
    private static int covered = 0;
    private static int totalBranches = 0;

    // ── helpers ──────────────────────────────────────────────────────────────

    static void check(String id, String desc, boolean cond) {
        total++;
        if (cond) { System.out.printf("  [PASS] %s: %s%n", id, desc); passed++; }
        else       { System.out.printf("  [FAIL] %s: %s%n", id, desc); failed++; }
    }

    static void branch(String id, String desc, boolean cond) {
        totalBranches++;
        if (cond) covered++;
        check(id, desc, cond);
    }

    static void header(String s) {
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("  " + s);
        System.out.println("=".repeat(60));
    }

    static void resetSingletons() {
        for (String cls : new String[]{"ProductManager","ReviewManager","OrderHistory","CartManager"}) {
            try {
                Field f = Class.forName(cls).getDeclaredField("instance");
                f.setAccessible(true);
                f.set(null, null);
            } catch (Exception ignored) {}
        }
    }

    // reset static nextOrderId in OrderHistory
    static void resetOrderId(int val) throws Exception {
        Field f = OrderHistory.class.getDeclaredField("nextOrderId");
        f.setAccessible(true);
        f.setInt(null, val);
    }

    // ════════════════════════════════════════════════════════════════════════
    // MODULE 1 — ReviewManager
    // ════════════════════════════════════════════════════════════════════════
    //
    //  Method: hasUserReviewed(int productId, String username)
    //    Branch A: review with matching productId AND username found → return true
    //    Branch B: loop ends without match                          → return false
    //
    //  Method: getAverageRating(int productId)
    //    Branch C: productReviews.isEmpty() → return 0.0
    //    Branch D: list has entries         → calculate and return average
    //
    //  Method: addReview(Review)
    //    Statement coverage: single path (no branches)
    //
    //  Method: getProductReviews(int productId)
    //    Branch E: review.getProductId() == productId → add to result list
    //    Branch F: no match                          → skip

    static void testReviewManager() throws Exception {
        header("MODULE: ReviewManager — Branch Coverage");
        resetSingletons();
        ReviewManager rm = ReviewManager.getInstance();

        int pid = 10;

        // ── Branch C: empty list → getAverageRating returns 0.0
        branch("WB-RM-01", "[Branch C] getAverageRating returns 0.0 when no reviews exist",
            rm.getAverageRating(pid) == 0.0);

        // ── Branch B: hasUserReviewed returns false when no reviews
        branch("WB-RM-02", "[Branch B] hasUserReviewed returns false when list is empty",
            !rm.hasUserReviewed(pid, "buyer1"));

        // ── Statement coverage: addReview executes without exception
        boolean addOk = false;
        try {
            rm.addReview(new ReviewManager.Review(pid, "buyer1", 5, "Excellent"));
            addOk = true;
        } catch (Exception e) { addOk = false; }
        check("WB-RM-03", "[Stmt] addReview executes without exception", addOk);

        // ── Branch E: getProductReviews finds matching review
        branch("WB-RM-04", "[Branch E] getProductReviews returns review with matching productId",
            rm.getProductReviews(pid).size() == 1);

        // ── Branch F: getProductReviews skips non-matching productId
        branch("WB-RM-05", "[Branch F] getProductReviews returns empty for different productId",
            rm.getProductReviews(pid + 1).isEmpty());

        // ── Branch A: hasUserReviewed returns true after review added
        branch("WB-RM-06", "[Branch A] hasUserReviewed returns true when matching review exists",
            rm.hasUserReviewed(pid, "buyer1"));

        // ── Branch B (second user, same product): returns false
        branch("WB-RM-07", "[Branch B] hasUserReviewed returns false for different username",
            !rm.hasUserReviewed(pid, "buyer2"));

        // ── Branch D: average with reviews
        rm.addReview(new ReviewManager.Review(pid, "buyer2", 3, "OK"));
        double avg = rm.getAverageRating(pid);  // (5+3)/2 = 4.0
        branch("WB-RM-08", "[Branch D] getAverageRating calculates correctly: (5+3)/2 = 4.0",
            Math.abs(avg - 4.0) < 0.001);

        // ── Star string: all 5 filled
        check("WB-RM-09", "[Stmt] getStarRating produces correct string for rating 5",
            new ReviewManager.Review(pid, "x", 5, "x").getStarRating().equals("★★★★★"));

        // ── Star string: rating 1
        check("WB-RM-10", "[Stmt] getStarRating produces correct string for rating 1",
            new ReviewManager.Review(pid, "x", 1, "x").getStarRating().equals("★☆☆☆☆"));
    }

    // ════════════════════════════════════════════════════════════════════════
    // MODULE 2 — DatabaseManager
    // ════════════════════════════════════════════════════════════════════════
    //
    //  Method: loadData(String filename)  — uses private generic helper
    //    Branch A: file does not exist           → returns new ArrayList<>()
    //    Branch B: file exists, valid data       → returns deserialized list
    //    Branch C: file exists, corrupt/invalid  → catches exception, returns empty list

    static void testDatabaseManager() throws Exception {
        header("MODULE: DatabaseManager — Branch Coverage");
        resetSingletons();

        DatabaseManager.initialize();

        // ── Branch A: missing file → empty list
        File missing = new File("nexus_data/nonexistent_test.dat");
        if (missing.exists()) missing.delete();
        ArrayList<ReviewManager.Review> emptyLoad = DatabaseManager.loadReviews();
        // loadReviews reads reviews.dat; delete it first
        new File("nexus_data/reviews.dat").delete();
        emptyLoad = DatabaseManager.loadReviews();
        branch("WB-DB-01", "[Branch A] loadReviews returns empty list when file missing",
            emptyLoad.isEmpty());

        // ── Branch B: save then load (valid file)
        ArrayList<ReviewManager.Review> toSave = new ArrayList<>();
        toSave.add(new ReviewManager.Review(7, "db_tester", 4, "DB test review"));
        DatabaseManager.saveReviews(toSave);
        ArrayList<ReviewManager.Review> loaded = DatabaseManager.loadReviews();
        branch("WB-DB-02", "[Branch B] loadReviews returns correct data from valid file",
            !loaded.isEmpty() && loaded.get(0).getComment().equals("DB test review"));

        // ── Branch C: corrupt file → returns empty list (no crash)
        java.io.FileOutputStream fos = new java.io.FileOutputStream("nexus_data/reviews.dat");
        fos.write(new byte[]{0x00, 0x01, 0x02, (byte)0xFF, (byte)0xFE}); // garbage bytes
        fos.close();
        ArrayList<ReviewManager.Review> corrupt = DatabaseManager.loadReviews();
        branch("WB-DB-03", "[Branch C] loadReviews returns empty list on corrupt file (no exception)",
            corrupt.isEmpty());

        // ── initialize creates directory
        File dir = new File("nexus_data");
        branch("WB-DB-04", "[Stmt] nexus_data/ directory exists after initialize()",
            dir.exists() && dir.isDirectory());

        // ── save + load for products
        resetSingletons();
        ProductManager pm = ProductManager.getInstance();
        ArrayList<ProductManager.Product> prods = pm.getAllProducts();
        DatabaseManager.saveProducts(prods);
        ArrayList<ProductManager.Product> loadedProds = DatabaseManager.loadProducts();
        branch("WB-DB-05", "[Branch B] loadProducts returns correct count after save",
            loadedProds.size() == prods.size());
    }

    // ════════════════════════════════════════════════════════════════════════
    // MODULE 3 — ProductManager
    // ════════════════════════════════════════════════════════════════════════
    //
    //  getProduct(int productId)
    //    Branch A: product found     → return product
    //    Branch B: product not found → return null
    //
    //  updateProduct(Product)
    //    Branch A: matching id found → update and return
    //    Branch B: id not found      → loop exits without update
    //
    //  deleteProduct(int productId)
    //    Branch A: id found → remove and return
    //    Branch B: id not found → loop exits (no change)

    static void testProductManager() throws Exception {
        header("MODULE: ProductManager — Branch Coverage");
        resetSingletons();

        ProductManager pm = ProductManager.getInstance();

        // Add a dedicated test product so tests are independent of DB state
        int testId = pm.getNextProductId();
        pm.addProduct(new ProductManager.Product(testId, "WB-Test Product",
            "wb_seller", 19.99, "White-box test item", 5, "Other"));

        // ── getProduct Branch A: found
        ProductManager.Product found = pm.getProduct(testId);
        branch("WB-PM-01", "[Branch A] getProduct returns non-null for existing product",
            found != null && found.getId() == testId);

        // ── getProduct Branch B: not found
        ProductManager.Product notFound = pm.getProduct(99999);
        branch("WB-PM-02", "[Branch B] getProduct(99999) returns null",
            notFound == null);

        // ── updateProduct Branch A: matching id
        int sizeBefore = pm.getAllProducts().size();
        found.setName("WB-Updated Product");
        pm.updateProduct(found);
        ProductManager.Product updated = pm.getProduct(testId);
        branch("WB-PM-03", "[Branch A] updateProduct modifies product name",
            updated != null && updated.getName().equals("WB-Updated Product"));
        check("WB-PM-04", "[Branch A] updateProduct does not change product count",
            pm.getAllProducts().size() == sizeBefore);

        // ── updateProduct Branch B: non-existent id (no crash, no change)
        int countBefore = pm.getAllProducts().size();
        ProductManager.Product ghost = new ProductManager.Product(
            99999, "Ghost", "ghost_seller", 1.0, "ghost", 0, "Other");
        pm.updateProduct(ghost);
        branch("WB-PM-05", "[Branch B] updateProduct on missing id causes no crash and no count change",
            pm.getAllProducts().size() == countBefore);

        // ── deleteProduct Branch A: found and removed
        int newId = pm.getNextProductId();
        pm.addProduct(new ProductManager.Product(newId, "WB-Delete-Me", "s1", 5.0, "test", 1, "Other"));
        int countWithNew = pm.getAllProducts().size();
        pm.deleteProduct(newId);
        branch("WB-PM-06", "[Branch A] deleteProduct removes the product",
            pm.getProduct(newId) == null && pm.getAllProducts().size() == countWithNew - 1);

        // ── deleteProduct Branch B: id not found → no crash, count unchanged
        int countNow = pm.getAllProducts().size();
        pm.deleteProduct(99998);
        branch("WB-PM-07", "[Branch B] deleteProduct on missing id causes no crash",
            pm.getAllProducts().size() == countNow);

        // ── getSellerProducts
        ArrayList<ProductManager.Product> s1prods = pm.getSellerProducts("seller1");
        branch("WB-PM-08", "[Stmt] getSellerProducts returns only that seller's products",
            s1prods.stream().allMatch(p -> p.getSeller().equals("seller1")));

        // ── getNextProductId increases
        int id1 = pm.getNextProductId();
        pm.addProduct(new ProductManager.Product(id1, "Temp", "s1", 1.0, "t", 1, "Other"));
        int id2 = pm.getNextProductId();
        check("WB-PM-09", "[Stmt] getNextProductId increments after adding product",
            id2 > id1);
    }

    // ════════════════════════════════════════════════════════════════════════
    // MODULE 4 — OrderHistory
    // ════════════════════════════════════════════════════════════════════════
    //
    //  getUserOrders(String username)
    //    Branch A: order matches username → included
    //    Branch B: order does not match   → excluded
    //
    //  containsSellerProduct(String seller)
    //    Branch A: item seller matches → return true
    //    Branch B: no item matches     → return false
    //
    //  getNextOrderId() — increments static counter

    static void testOrderHistory() throws Exception {
        header("MODULE: OrderHistory — Branch Coverage");
        resetSingletons();
        resetOrderId(2000);

        OrderHistory oh = OrderHistory.getInstance();

        ArrayList<OrderHistory.OrderItem> items1 = new ArrayList<>();
        items1.add(new OrderHistory.OrderItem("Widget A", "sellerX"));

        ArrayList<OrderHistory.OrderItem> items2 = new ArrayList<>();
        items2.add(new OrderHistory.OrderItem("Gadget B", "sellerY"));

        int id1 = OrderHistory.getNextOrderId(); // 2000
        int id2 = OrderHistory.getNextOrderId(); // 2001

        oh.addOrder(new OrderHistory.OrderRecord(id1, "alice", "a@x.com", "1 A St", items1, 50.0, "Processing", "2026-04-24"));
        oh.addOrder(new OrderHistory.OrderRecord(id2, "bob",   "b@x.com", "2 B St", items2, 30.0, "Processing", "2026-04-24"));

        // ── getUserOrders Branch A: alice's order returned
        ArrayList<OrderHistory.OrderRecord> aliceOrders = oh.getUserOrders("alice");
        branch("WB-OH-01", "[Branch A] getUserOrders includes matching order",
            aliceOrders.size() == 1 && aliceOrders.get(0).getBuyerUsername().equals("alice"));

        // ── getUserOrders Branch B: bob's order excluded from alice's list
        branch("WB-OH-02", "[Branch B] getUserOrders excludes non-matching buyer",
            aliceOrders.stream().noneMatch(o -> o.getBuyerUsername().equals("bob")));

        // ── getSellerOrders Branch A: sellerX's order returned
        ArrayList<OrderHistory.OrderRecord> sxOrders = oh.getSellerOrders("sellerX");
        branch("WB-OH-03", "[Branch A] getSellerOrders includes order with matching seller item",
            sxOrders.size() == 1);

        // ── getSellerOrders Branch B: sellerY excluded from sellerX result
        branch("WB-OH-04", "[Branch B] getSellerOrders excludes orders without matching seller",
            sxOrders.stream().noneMatch(o -> o.containsSellerProduct("sellerY")));

        // ── containsSellerProduct Branch A
        OrderHistory.OrderRecord rec1 = oh.getAllOrders().get(0);
        branch("WB-OH-05", "[Branch A] containsSellerProduct returns true for matching seller",
            rec1.containsSellerProduct("sellerX"));

        // ── containsSellerProduct Branch B
        branch("WB-OH-06", "[Branch B] containsSellerProduct returns false for non-matching seller",
            !rec1.containsSellerProduct("sellerY"));

        // ── getNextOrderId increments
        check("WB-OH-07", "[Stmt] getNextOrderId produced sequential IDs (2000, 2001)",
            id1 == 2000 && id2 == 2001);

        // ── nextOrderId restored from saved data
        oh.addOrder(new OrderHistory.OrderRecord(3000, "carol", "c@x.com", "3 C St",
            items1, 20.0, "Processing", "2026-04-24"));
        resetSingletons();
        resetOrderId(1001); // reset counter to default
        OrderHistory oh2 = OrderHistory.getInstance(); // should restore from file
        int nextId = OrderHistory.getNextOrderId();
        branch("WB-OH-08", "[Branch] nextOrderId restored from persisted orders (>= 3001)",
            nextId >= 3001);

        // ── paymentMethod getter default
        OrderHistory.OrderRecord defaultRec = new OrderHistory.OrderRecord(
            9000, "dave", "d@x.com", "4 D St", items1, 10.0, "Processing", "2026-04-24");
        check("WB-OH-09", "[Stmt] default paymentMethod is 'Cash on Delivery'",
            "Cash on Delivery".equals(defaultRec.getPaymentMethod()));

        // ── setStatus
        defaultRec.setStatus("Shipped");
        check("WB-OH-10", "[Stmt] setStatus updates the status field",
            "Shipped".equals(defaultRec.getStatus()));
    }

    // ════════════════════════════════════════════════════════════════════════
    // MODULE 5 — CartManager
    // ════════════════════════════════════════════════════════════════════════
    //
    //  addItem(CartItem)
    //    Branch A: item already exists (same productId) → increment quantity
    //    Branch B: new item → add to list
    //
    //  removeItem(int index)
    //    Branch A: valid index → remove item
    //    Branch B: invalid index → no change
    //
    //  getSubtotal()
    //    Iterates all items: sum of (unitPrice * quantity)

    static void testCartManager() throws Exception {
        header("MODULE: CartManager — Branch Coverage");
        resetSingletons();

        CartManager cm = CartManager.getInstance();
        cm.clearCart();

        // ── addItem Branch B: new item added
        cm.addItem(new CartManager.CartItem(1, "Headphones", 79.99, 1));
        branch("WB-CM-01", "[Branch B] addItem adds new item when cart is empty",
            cm.getItemCount() == 1);

        // ── addItem Branch A: existing item quantity incremented
        cm.addItem(new CartManager.CartItem(1, "Headphones", 79.99, 2));
        branch("WB-CM-02", "[Branch A] addItem merges duplicate productId (qty becomes 3)",
            cm.getItemCount() == 1 && cm.getCartItems().get(0).getQuantity() == 3);

        // ── addItem Branch B: second distinct product
        cm.addItem(new CartManager.CartItem(2, "USB Cable", 12.99, 1));
        branch("WB-CM-03", "[Branch B] addItem adds second distinct product",
            cm.getItemCount() == 2);

        // ── getSubtotal: 79.99*3 + 12.99*1 = 252.96
        double expected = 79.99 * 3 + 12.99 * 1;
        branch("WB-CM-04", "[Stmt] getSubtotal calculates correctly (79.99*3 + 12.99 = 252.96)",
            Math.abs(cm.getSubtotal() - expected) < 0.001);

        // ── removeItem Branch A: valid index
        cm.removeItem(0);
        branch("WB-CM-05", "[Branch A] removeItem(0) reduces item count to 1",
            cm.getItemCount() == 1);

        // ── removeItem Branch B: invalid index (no crash, no change)
        int countBefore = cm.getItemCount();
        cm.removeItem(-1);
        cm.removeItem(999);
        branch("WB-CM-06", "[Branch B] removeItem with invalid index causes no change",
            cm.getItemCount() == countBefore);

        // ── clearCart
        cm.clearCart();
        check("WB-CM-07", "[Stmt] clearCart empties the cart",
            cm.getItemCount() == 0 && cm.getSubtotal() == 0.0);

        // ── username/usertype setters
        cm.setUsername("test_buyer");
        cm.setUserType("Buyer");
        check("WB-CM-08", "[Stmt] getUsername and getUserType return set values",
            "test_buyer".equals(cm.getUsername()) && "Buyer".equals(cm.getUserType()));
    }

    // ════════════════════════════════════════════════════════════════════════
    // MAIN
    // ════════════════════════════════════════════════════════════════════════

    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║       NEXUS — WHITE-BOX TEST SUITE (Sprint 3)           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        testReviewManager();
        testDatabaseManager();
        testProductManager();
        testOrderHistory();
        testCartManager();

        System.out.println();
        System.out.println("=".repeat(60));
        System.out.printf("  RESULTS:  %d / %d passed   |   %d failed%n", passed, total, failed);
        System.out.printf("  BRANCH COVERAGE:  %d / %d branches covered (%.0f%%)%n",
            covered, totalBranches, totalBranches > 0 ? 100.0 * covered / totalBranches : 0);
        System.out.println("=".repeat(60));
        System.exit(failed == 0 ? 0 : 1);
    }
}
