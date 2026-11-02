import java.util.*;
import java.io.*;

class Product {
    int id;
    String name;
    double price;
    int stock;

    Product(int id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    void display() {
        System.out.printf(
            "%d | %s | Rs. %.2f | Stock: %d%n",
            id, name, price, stock
        );
    }
}

class CartItem {
    Product product;
    int quantity;

    CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    double getTotal() {
        return product.price * quantity;
    }
}

public class Main {

    static ArrayList<Product> products = new ArrayList<>();
    static ArrayList<CartItem> cart = new ArrayList<>();

    static final String PRODUCT_FILE = "products.csv";
    static final String ORDER_FILE = "orders.csv";

    static void loadProducts() {
        File file = new File(PRODUCT_FILE);

        if (!file.exists()) {
            System.out.println("Product file not found!");
            return;
        }

        try (Scanner reader = new Scanner(file)) {
            if (reader.hasNextLine()) {
                reader.nextLine();
            }

            while (reader.hasNextLine()) {
                String[] data = reader.nextLine().split(",", -1);

                if (data.length == 4) {
                    try {
                        products.add(new Product(
                            Integer.parseInt(data[0]),
                            data[1],
                            Double.parseDouble(data[2]),
                            Integer.parseInt(data[3])
                        ));
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping invalid product.");
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error loading products!");
        }
    }

    static void displayProducts() {
        System.out.println("\n--- Available Products ---");

        for (Product p : products) {
            p.display();
        }
    }

    static Product findProduct(int id) {
        for (Product p : products) {
            if (p.id == id) {
                return p;
            }
        }

        return null;
    }

    static void addToCart(Scanner sc) {
        System.out.print("Enter Product ID: ");

        int id;

        try {
            id = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID!");
            return;
        }

        Product product = findProduct(id);

        if (product == null) {
            System.out.println("Product not found!");
            return;
        }

        System.out.print("Enter Quantity: ");

        int quantity;

        try {
            quantity = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity!");
            return;
        }

        if (quantity <= 0) {
            System.out.println("Quantity must be positive!");
            return;
        }

        CartItem existing = null;

        for (CartItem item : cart) {
            if (item.product.id == id) {
                existing = item;
                break;
            }
        }

        int alreadyAdded = existing == null ? 0 : existing.quantity;

        if (alreadyAdded + quantity > product.stock) {
            System.out.println("Not enough stock!");
            return;
        }

        if (existing == null) {
            cart.add(new CartItem(product, quantity));
        } else {
            existing.quantity += quantity;
        }

        System.out.println("Product added to cart!");
    }

    static void showCart() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }

        double total = 0;

        System.out.println("\n--- Shopping Cart ---");

        for (CartItem item : cart) {
            item.product.display();

            System.out.println("Quantity: " + item.quantity);

            System.out.printf("Item Total: Rs. %.2f%n",
                    item.getTotal());

            total += item.getTotal();
        }

        System.out.printf("Cart Total: Rs. %.2f%n", total);
    }

    static void removeFromCart(Scanner sc) {
        System.out.print("Enter Product ID to remove: ");

        int id;

        try {
            id = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID!");
            return;
        }

        for (CartItem item : cart) {
            if (item.product.id == id) {
                cart.remove(item);
                System.out.println("Product removed!");
                return;
            }
        }

        System.out.println("Product not found in cart!");
    }

    static void checkout() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty!");
            return;
        }

        double total = 0;

        for (CartItem item : cart) {
            if (item.quantity > item.product.stock) {
                System.out.println("Stock changed. Checkout cancelled!");
                return;
            }

            total += item.getTotal();
        }

        double discount = 0;

        if (total >= 5000) {
            discount = total * 0.10;
        }

        double finalAmount = total - discount;

        try (FileWriter writer = new FileWriter(ORDER_FILE, true)) {

            File file = new File(ORDER_FILE);

            if (file.length() == 0) {
                writer.write("Product,Quantity,Amount\n");
            }

            for (CartItem item : cart) {
                writer.write(
                    item.product.name + "," +
                    item.quantity + "," +
                    item.getTotal() + "\n"
                );
            }

        } catch (IOException e) {
            System.out.println("Unable to save order!");
            return;
        }

        for (CartItem item : cart) {
            item.product.stock -= item.quantity;
        }

        System.out.println("\n===== ORDER SUMMARY =====");

        System.out.printf("Total: Rs. %.2f%n", total);
        System.out.printf("Discount: Rs. %.2f%n", discount);
        System.out.printf("Final Amount: Rs. %.2f%n", finalAmount);

        cart.clear();

        System.out.println("Order placed successfully!");
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        loadProducts();

        if (products.isEmpty()) {
            System.out.println("No products available.");
            sc.close();
            return;
        }

        while (true) {
            System.out.println("\n===== ONLINE SHOPPING CART =====");
            System.out.println("1. Display Products");
            System.out.println("2. Add to Cart");
            System.out.println("3. Show Cart");
            System.out.println("4. Remove from Cart");
            System.out.println("5. Checkout");
            System.out.println("6. Exit");

            System.out.print("Enter choice: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    displayProducts();
                    break;

                case "2":
                    addToCart(sc);
                    break;

                case "3":
                    showCart();
                    break;

                case "4":
                    removeFromCart(sc);
                    break;

                case "5":
                    checkout();
                    break;

                case "6":
                    System.out.println("Thank you for shopping!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
