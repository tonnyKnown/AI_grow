package com.oa.system.common;

import java.util.List;

/**
 * 内存分页工具。
 *
 * <p>项目内列表接口普遍采用「全量查询 + 内存切片」的方式分页，直接使用
 * {@code subList} 时对入参没有任何防护，外部传入非法分页参数会直接抛异常：
 *
 * <pre>
 * pageNum = 0   → start = -10      → subList(-10, 0)      → IndexOutOfBoundsException
 * pageNum &lt; 0   → start 为负数    → 同上
 * pageSize = 0  → end = start      → subList 返回空，但 pageNum 越大越异常
 * pageSize &lt; 0 → end &lt; start     → IllegalArgumentException
 * 超大 pageNum  → (pageNum - 1) * pageSize 溢出为负数 → 同上
 * </pre>
 *
 * <p>本类统一做入参归一化与边界裁剪，保证任何入参都不会抛异常。
 */
public final class PageUtils {

    /** 默认页码 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 默认每页条数 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    private PageUtils() {
    }

    /**
     * 归一化页码：null 或小于 1 时返回 1。
     */
    public static int normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    /**
     * 归一化每页条数：null 或小于 1 时返回默认值。
     */
    public static int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }

    /**
     * 计算切片起始下标（从 0 开始），已做溢出与边界保护。
     *
     * @param pageNum  归一化后的页码
     * @param pageSize 归一化后的每页条数
     * @param total    数据总条数
     * @return 起始下标，最大不超过 total
     */
    public static int offset(int pageNum, int pageSize, int total) {
        long offset = (long) (pageNum - 1) * pageSize;
        if (offset >= total) {
            return total;
        }
        return (int) offset;
    }

    /**
     * 对完整列表做内存分页切片。
     *
     * @param source   全量数据，允许为 null
     * @param pageNum  页码，非法值会被归一化为 1
     * @param pageSize 每页条数，非法值会被归一化为 10
     * @return 当前页数据，永不为 null
     */
    public static <T> List<T> slice(List<T> source, Integer pageNum, Integer pageSize) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        int num = normalizePageNum(pageNum);
        int size = normalizePageSize(pageSize);

        int total = source.size();
        int start = offset(num, size, total);
        if (start >= total) {
            return List.of();
        }

        long end = (long) start + size;
        int toIndex = end >= total ? total : (int) end;
        return source.subList(start, toIndex);
    }
}
