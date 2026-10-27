/**
 * SortingAlgorithms.java
 * ----------------------
 * يحتوي على تطبيق خوارزميات الترتيب الخمس المطلوبة في النشاط:
 *   - Selection Sort  : O(N^2) في كل الحالات
 *   - Insertion Sort  : O(N^2) في الحالة العامة، O(N) إذا كانت المصفوفة مرتبة
 *   - Bubble Sort     : O(N^2) في الحالة العامة، O(N) إذا كانت مرتبة (بفضل علم swapped)
 *   - Merge Sort      : O(N log N) في كل الحالات
 *   - Quick Sort      : O(N log N) في المتوسط، O(N^2) في أسوأ حالة
 *
 * جميع الدوال ترتب المصفوفة تصاعدياً "في مكانها" (in-place) ما عدا Merge Sort
 * الذي يستخدم مصفوفة مساعدة مؤقتة.
 */
public final class SortingAlgorithms {

    private SortingAlgorithms() { }   // كلاس أدوات فقط، لا يُنشأ منه كائنات

    // =====================================================================
    // 1) Selection Sort
    // في كل دورة نبحث عن أصغر عنصر في الجزء غير المرتب ونضعه في مكانه.
    // عدد المقارنات ثابت تقريباً N(N-1)/2 مهما كانت حالة البيانات.
    // =====================================================================
    public static void selectionSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIndex]) {
                    minIndex = j;
                }
            }
            // تبديل فقط إذا وُجد عنصر أصغر
            if (minIndex != i) {
                swap(arr, i, minIndex);
            }
        }
    }

    // =====================================================================
    // 2) Insertion Sort
    // نأخذ كل عنصر ونُدخله في مكانه الصحيح داخل الجزء المرتب على يساره.
    // إذا كانت المصفوفة مرتبة فإن الحلقة الداخلية لا تُنفَّذ => O(N).
    // =====================================================================
    public static void insertionSort(int[] arr) {
        int n = arr.length;
        for (int i = 1; i < n; i++) {
            int key = arr[i];
            int j = i - 1;
            // إزاحة العناصر الأكبر من key خطوة لليمين
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    // =====================================================================
    // 3) Bubble Sort (النسخة المحسّنة)
    // نقارن كل عنصرين متجاورين ونبدّلهما إذا كانا بترتيب خاطئ.
    // العلم swapped يوقف الخوارزمية مبكراً إذا لم يحدث أي تبديل في دورة كاملة.
    // =====================================================================
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            // بعد كل دورة يستقر أكبر عنصر في آخر الجزء غير المرتب
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    swap(arr, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;  // المصفوفة أصبحت مرتبة
            }
        }
    }

    // =====================================================================
    // 4) Merge Sort
    // "فرّق تَسُد": نقسم المصفوفة إلى نصفين، نرتب كل نصف، ثم ندمجهما.
    // عمق الاستدعاء log N وكل مستوى يكلّف O(N) => O(N log N) دائماً.
    // =====================================================================
    public static void mergeSort(int[] arr, int left, int right) {
        int[] temp = new int[arr.length];   // مصفوفة مساعدة واحدة لكل عملية الترتيب
        mergeSort(arr, temp, left, right);
    }

    private static void mergeSort(int[] arr, int[] temp, int left, int right) {
        if (left >= right) {
            return;  // جزء من عنصر واحد مرتب بطبيعته
        }
        int mid = left + (right - left) / 2;   // تجنّب overflow
        mergeSort(arr, temp, left, mid);
        mergeSort(arr, temp, mid + 1, right);
        merge(arr, temp, left, mid, right);
    }

    /** دمج جزأين مرتبين [left..mid] و [mid+1..right] في جزء واحد مرتب. */
    private static void merge(int[] arr, int[] temp, int left, int mid, int right) {
        // نسخ الجزء الحالي إلى المصفوفة المساعدة
        System.arraycopy(arr, left, temp, left, right - left + 1);

        int i = left;       // مؤشر النصف الأيسر
        int j = mid + 1;    // مؤشر النصف الأيمن
        int k = left;       // مؤشر الكتابة في المصفوفة الأصلية

        while (i <= mid && j <= right) {
            if (temp[i] <= temp[j]) {   // <= تحافظ على الاستقرار (stable)
                arr[k++] = temp[i++];
            } else {
                arr[k++] = temp[j++];
            }
        }
        // نسخ ما تبقى من النصف الأيسر (النصف الأيمن في مكانه أصلاً)
        while (i <= mid) {
            arr[k++] = temp[i++];
        }
    }

    // =====================================================================
    // 5) Quick Sort (تقسيم Lomuto - المحور هو آخر عنصر)
    // نختار عنصراً محورياً (Pivot)، نضع الأصغر منه يساره والأكبر يمينه، ثم نكرر.
    // اختيار آخر عنصر كمحور عمداً لإظهار أسوأ حالة:
    // في المصفوفة المرتبة أو المعكوسة يكون المحور دائماً أكبر/أصغر عنصر،
    // فينقسم الجزء إلى (N-1) و (0) بدل نصفين => O(N^2) وعمق استدعاء N.
    // =====================================================================
    public static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int p = partition(arr, low, high);
            quickSort(arr, low, p - 1);
            quickSort(arr, p + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];   // المحور = آخر عنصر
        int i = low - 1;         // حد منطقة العناصر الأصغر من المحور
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);  // وضع المحور في مكانه النهائي
        return i + 1;
    }

    // =====================================================================
    // دوال مساعدة
    // =====================================================================
    private static void swap(int[] arr, int i, int j) {
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    /** للتحقق من صحة الترتيب بعد كل تشغيل. */
    public static boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i - 1] > arr[i]) {
                return false;
            }
        }
        return true;
    }
}
