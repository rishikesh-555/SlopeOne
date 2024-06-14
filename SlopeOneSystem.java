import java.util.*;

public class SlopeOneSystem {

    public static class Item {
        private String itemName;

        public Item(String itemName) {
            this.itemName = itemName;
        }

        public String getItemName() {
            return itemName;
        }

        @Override
        public String toString() {
            return itemName;
        }
    }

    public static class User {
        private String username;

        public User(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public String toString() {
            return username;
        }
    }

    public static class InputData {
        public static List<Item> items = Arrays.asList(
            new Item("Candy"),
            new Item("Drink"),
            new Item("Soda"),
            new Item("Popcorn"),
            new Item("Snacks")
        );

        public static Map<User, HashMap<Item, Double>> data = new HashMap<>();

        static {
            User user1 = new User("User1");
            User user2 = new User("User2");
            User user3 = new User("User3");

            data.put(user1, new HashMap<>());
            data.put(user2, new HashMap<>());
            data.put(user3, new HashMap<>());

            data.get(user1).put(items.get(0), 1.0);
            data.get(user1).put(items.get(1), 0.5);
            data.get(user1).put(items.get(4), 0.0);

            data.get(user2).put(items.get(1), 1.0);
            data.get(user2).put(items.get(2), 0.5);
            data.get(user2).put(items.get(3), 0.0);

            data.get(user3).put(items.get(0), 0.0);
            data.get(user3).put(items.get(2), 0.5);
        }
    }

    public static class SlopeOne {
        private Map<Item, HashMap<Item, Double>> diff = new HashMap<>();
        private Map<Item, HashMap<Item, Integer>> freq = new HashMap<>();

        public void buildDifferencesMatrix(Map<User, HashMap<Item, Double>> data) {
            for (HashMap<Item, Double> user : data.values()) {
                for (Map.Entry<Item, Double> e : user.entrySet()) {
                    if (!diff.containsKey(e.getKey())) {
                        diff.put(e.getKey(), new HashMap<>());
                        freq.put(e.getKey(), new HashMap<>());
                    }

                    for (Map.Entry<Item, Double> e2 : user.entrySet()) {
                        int oldCount = freq.get(e.getKey()).getOrDefault(e2.getKey(), 0);
                        double oldDiff = diff.get(e.getKey()).getOrDefault(e2.getKey(), 0.0);

                        double observedDiff = e.getValue() - e2.getValue();
                        freq.get(e.getKey()).put(e2.getKey(), oldCount + 1);
                        diff.get(e.getKey()).put(e2.getKey(), oldDiff + observedDiff);
                    }
                }
            }

            for (Item j : diff.keySet()) {
                for (Item i : diff.get(j).keySet()) {
                    double oldValue = diff.get(j).get(i);
                    int count = freq.get(j).get(i);
                    diff.get(j).put(i, oldValue / count);
                }
            }
        }

        public void printDifferencesMatrix() {
            for (Item j : diff.keySet()) {
                System.out.println(j.getItemName() + ": ");
                for (Item i : diff.get(j).keySet()) {
                    System.out.println("  " + i.getItemName() + " -> " + diff.get(j).get(i));
                }
            }
        }
    }

    public static class SlopeOnePredictor {
        private Map<Item, HashMap<Item, Double>> diff;
        private Map<Item, HashMap<Item, Integer>> freq;

        public SlopeOnePredictor(Map<Item, HashMap<Item, Double>> diff, Map<Item, HashMap<Item, Integer>> freq) {
            this.diff = diff;
            this.freq = freq;
        }

        public Map<Item, Double> predict(User user, Map<User, HashMap<Item, Double>> data) {
            HashMap<Item, Double> uPred = new HashMap<>();
            HashMap<Item, Integer> uFreq = new HashMap<>();

            for (Map.Entry<Item, Double> e : data.get(user).entrySet()) {
                for (Item j : diff.keySet()) {
                    try {
                        double predictedValue = diff.get(j).get(e.getKey()) + e.getValue();
                        double finalValue = predictedValue * freq.get(j).get(e.getKey());
                        uPred.put(j, uPred.getOrDefault(j, 0.0) + finalValue);
                        uFreq.put(j, uFreq.getOrDefault(j, 0) + freq.get(j).get(e.getKey()));
                    } catch (NullPointerException ignored) {}
                }
            }

            HashMap<Item, Double> clean = new HashMap<>();
            for (Item j : uPred.keySet()) {
                if (uFreq.get(j) > 0) {
                    clean.put(j, uPred.get(j) / uFreq.get(j));
                }
            }
            for (Item j : InputData.items) {
                if (data.get(user).containsKey(j)) {
                    clean.put(j, data.get(user).get(j));
                } else if (!clean.containsKey(j)) {
                    clean.put(j, -1.0);
                }
            }

            return clean;
        }
    }

    public static void main(String[] args) {
        SlopeOne slopeOne = new SlopeOne();
        slopeOne.buildDifferencesMatrix(InputData.data);
        slopeOne.printDifferencesMatrix();

        SlopeOnePredictor predictor = new SlopeOnePredictor(slopeOne.diff, slopeOne.freq);
        for (User user : InputData.data.keySet()) {
            System.out.println("Predictions for " + user.getUsername() + ": ");
            Map<Item, Double> predictions = predictor.predict(user, InputData.data);
            for (Item item : predictions.keySet()) {
                System.out.println("  " + item.getItemName() + ": " + predictions.get(item));
            }
        }
    }
}
