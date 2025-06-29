// file: common/src/main/aidl/icu/nullptr/hidemyapplist/common/IAPFService.aidl

package icu.nullptr.hidemyapplist.common;

interface IAPFService {
    /**
     * Đồng bộ hóa chuỗi JSON cấu hình từ UI đến Service.
     */
    void syncConfig(String json);

    /**
     * Lấy phiên bản của service.
     */
    int getServiceVersion();

    /**
     * Lấy toàn bộ nội dung file log.
     */
    String getLogs();

    /**
     * Xóa nội dung file log.
     */
    void clearLogs();

    /**
     * Dừng service (dùng để gỡ lỗi).
     */
    void stopService(); // Xóa tham số boolean không cần thiết
}