import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * SortingBenchmark.java
 * ---------------------
 * البرنامج الرئيسي: يقيس زمن تنفيذ خوارزميات الترتيب الخمس على 4 حالات بيانات
 * بحجم N = 100,000 عنصر، ويطبع النتائج بالميكروثانية (µs) في جدول منظم.
 *
 * قواعد القياس (حسب متطلبات النشاط):
 *   - نستخدم System.nanoTime() ثم نقسم على 1000 للتحويل إلى ميكروثانية.
 *   - نأخذ نسخة مستقلة من المصفوفة الأصلية قبل كل خوارزمية (تكافؤ الفرص).
 *   - نقيس زمن الترتيب فقط؛ النسخ والتعبئة خارج منطقة القياس.
 *
 * ملاحظة: Quick Sort في الحالة المرتبة/المعكوسة يصل عمق الاستدعاء فيه إلى N،
 * لذلك نشغّل البرنامج داخل Thread بمكدس (stack) كبير لتفادي StackOverflowError.
 */
public class SortingBenchmark {

    private static final int N = 100_000;

    private static final String[] CASE_NAMES = {
        "Random", "Reversed", "Partially Sorted", "Fully Sorted"
    };

    private static final String[] ALGO_NAMES = {
        "Selection Sort", "Insertion Sort", "Bubble Sort", "Merge Sort", "Quick Sort"
    };

    // كل خوارزمية مغلّفة كـ Consumer<int[]> ليسهل استدعاؤها بنفس الطريقة
    @SuppressWarnings("unchecked")
    private static final Consumer<int[]>[] ALGORITHMS = new Consumer[] {
        (Consumer<int[]>) SortingAlgorithms::selectionSort,
        (Consumer<int[]>) SortingAlgorithms::insertionSort,
        (Consumer<int[]>) SortingAlgorithms::bubbleSort,
        (Consumer<int[]>) a -> SortingAlgorithms.mergeSort(a, 0, a.length - 1),
        (Consumer<int[]>) a -> SortingAlgorithms.quickSort(a, 0, a.length - 1)
    };

    public static void main(String[] args) throws InterruptedException {
        // إجبار الـ Console على UTF-8 حتى يظهر الرمز µ بشكل صحيح
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        // مكدس بحجم 1GB لاستيعاب عمق الاستدعاء في أسوأ حالة لـ Quick Sort
        Thread worker = new Thread(null, SortingBenchmark::runBenchmark, "benchmark", 1L << 30);
        worker.start();
        worker.join();
    }

    private static void runBenchmark() {
        // 1) تجهيز البيانات الأصلية للحالات الأربع (مرة واحدة فقط)
        int[][] datasets = {
            DataGenerator.random(N),
            DataGenerator.reversed(N),
            DataGenerator.partiallySorted(N),
            DataGenerator.sorted(N)
        };

        // 2) إحماء الـ JVM (Warm-up) على مصفوفات صغيرة حتى يُترجم الكود بالـ JIT
        //    قبل القياس الفعلي، فلا تظلم النتائج أول خوارزمية يتم قياسها.
        warmUp();

        long[][] results = new long[ALGO_NAMES.length][CASE_NAMES.length];

        System.out.printf("Running benchmark with N = %,d elements...%n%n", N);

        // 3) القياس: كل خوارزمية × كل حالة
        for (int a = 0; a < ALGORITHMS.length; a++) {
            for (int c = 0; c < datasets.length; c++) {
                // نسخة مستقلة من البيانات الأصلية (خارج منطقة القياس)
                int[] testArray = Arrays.copyOf(datasets[c], datasets[c].length);

                long startTime = System.nanoTime();
                ALGORITHMS[a].accept(testArray);              // الترتيب فقط
                long endTime = System.nanoTime();

                long durationInMicroseconds = (endTime - startTime) / 1000;
                results[a][c] = durationInMicroseconds;

                // التحقق من صحة الترتيب (خارج منطقة القياس)
                if (!SortingAlgorithms.isSorted(testArray)) {
                    System.err.println("ERROR: " + ALGO_NAMES[a] + " failed on " + CASE_NAMES[c]);
                }

                System.out.printf("  %-15s | %-17s : %,15d µs%n",
                        ALGO_NAMES[a], CASE_NAMES[c], durationInMicroseconds);
            }
        }

        // 4) طباعة الجدول النهائي
        printTable(results);

        // 5) حفظ النتائج في ملف CSV لاستخدامها في الرسم البياني
        saveCsv(results, "results.csv");
    }

    /** تشغيل كل الخوارزميات عدة مرات على بيانات صغيرة لتسخين الـ JIT. */
    private static void warmUp() {
        int[] small = DataGenerator.random(2_000);
        for (int round = 0; round < 5; round++) {
            for (Consumer<int[]> algo : ALGORITHMS) {
                algo.accept(Arrays.copyOf(small, small.length));
            }
        }
    }

    /** طباعة جدول النتائج في الـ Console. */
    private static void printTable(long[][] results) {
        String line = "+" + "-".repeat(17) + ("+" + "-".repeat(20)).repeat(CASE_NAMES.length) + "+";

        System.out.println();
        System.out.println("Sorting Algorithms Benchmark  (N = 100,000, time in microseconds)");
        System.out.println(line);
        System.out.printf("| %-15s ", "Algorithm");
        for (String c : CASE_NAMES) {
            System.out.printf("| %18s ", c);
        }
        System.out.println("|");
        System.out.println(line);

        for (int a = 0; a < ALGO_NAMES.length; a++) {
            System.out.printf("| %-15s ", ALGO_NAMES[a]);
            for (int c = 0; c < CASE_NAMES.length; c++) {
                System.out.printf("| %15s µs ", String.format("%,d", results[a][c]));
            }
            System.out.println("|");
        }
        System.out.println(line);
    }

    /** حفظ النتائج في ملف CSV. */
    private static void saveCsv(long[][] results, String fileName) {
        try (PrintWriter out = new PrintWriter(new FileWriter(fileName))) {
            out.println("Algorithm," + String.join(",", CASE_NAMES));
            for (int a = 0; a < ALGO_NAMES.length; a++) {
                StringBuilder row = new StringBuilder(ALGO_NAMES[a]);
                for (long r : results[a]) {
                    row.append(',').append(r);
                }
                out.println(row);
            }
            System.out.println("\nResults saved to " + fileName);
        } catch (IOException e) {
            System.err.println("Could not write CSV: " + e.getMessage());
        }
    }
}
