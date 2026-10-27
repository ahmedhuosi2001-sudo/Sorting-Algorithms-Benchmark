import java.util.Random;

/**
 * DataGenerator.java
 * ------------------
 * يولّد حالات البيانات الأربع المطلوبة في النشاط:
 *   1. Random            : أعداد عشوائية تماماً
 *   2. Reversely Sorted  : مرتبة من الأكبر إلى الأصغر
 *   3. Partially Sorted  : مرتبة بنسبة 80% تقريباً مع عناصر عشوائية متفرقة
 *   4. Fully Sorted      : مرتبة تصاعدياً
 *
 * نستخدم بذرة (seed) ثابتة حتى تكون النتائج قابلة للتكرار في كل تشغيل.
 */
public final class DataGenerator {

    private static final long SEED = 42L;
    private static final Random RANDOM = new Random(SEED);

    private DataGenerator() { }

    /** مصفوفة عشوائية تماماً بقيم بين 0 و 1,000,000. */
    public static int[] random(int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = RANDOM.nextInt(1_000_000);
        }
        return arr;
    }

    /** مصفوفة مرتبة تصاعدياً: 0, 1, 2, ..., n-1 */
    public static int[] sorted(int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i;
        }
        return arr;
    }

    /** مصفوفة مرتبة تنازلياً: n, n-1, ..., 1 */
    public static int[] reversed(int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = n - i;
        }
        return arr;
    }

    /**
     * مصفوفة مرتبة جزئياً (~80%):
     * نبدأ بمصفوفة مرتبة ثم نُجري (10% من N) عملية تبديل بين موقعين عشوائيين،
     * فيصبح حوالي 20% من العناصر في غير مكانها و 80% في أماكنها الصحيحة.
     */
    public static int[] partiallySorted(int n) {
        int[] arr = sorted(n);
        int swaps = (int) (n * 0.10);
        for (int s = 0; s < swaps; s++) {
            int i = RANDOM.nextInt(n);
            int j = RANDOM.nextInt(n);
            int t = arr[i];
            arr[i] = arr[j];
            arr[j] = t;
        }
        return arr;
    }
}
