package finalgame;

import java.util.Random;

// Shuffle-bag selection: every attack appears once per bag, with no repeat at boundaries.
public class RandomAttackOrder {
    private final int[] order;
    private final Random random;
    private int index;
    private int last = -1;

    public RandomAttackOrder(int attackCount) {
        this(attackCount, new Random());
    }

    public RandomAttackOrder(int attackCount, Random random) {
        if (attackCount < 2) {
            throw new IllegalArgumentException("At least two attacks are required");
        }
        order = new int[attackCount];
        this.random = random;
        index = order.length;
    }

    public int next() {
        if (index >= order.length) {
            for (int i = 0; i < order.length; i++) {
                order[i] = i;
            }
            for (int i = order.length - 1; i > 0; i--) {
                int other = random.nextInt(i + 1);
                int temp = order[i];
                order[i] = order[other];
                order[other] = temp;
            }
            if (order[0] == last) {
                int temp = order[0];
                order[0] = order[1];
                order[1] = temp;
            }
            index = 0;
        }
        last = order[index++];
        return last;
    }

    public void reset() {
        last = -1;
        index = order.length;
    }
}
