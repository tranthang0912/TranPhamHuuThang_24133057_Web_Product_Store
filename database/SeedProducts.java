import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.Properties;

/**
 * File nạp dữ liệu độc lập, KHÔNG thuộc ứng dụng Spring Boot.
 * Chạy từ thư mục dự án: ./scripts/seed-products.ps1
 * Cần chạy database/schema.sql một lần trước đó.
 * Có thể xóa file này sau khi nạp: mọi sản phẩm đã nằm trong SQL Server.
 * Chạy lại không ghi đè sản phẩm đã có hoặc đặt lại tồn kho.
 * Tên, ảnh, thông số và giá: nguồn nhà bán lẻ ngày 04/09/2026.
 * Số lượng tồn kho là dữ liệu bài tập, không phải tồn kho của nhà bán lẻ.
 */
public class SeedProducts {
    record Product(String category, String slug, String icon, String name,
            String brand, String description, long price, long originalPrice,
            int stock, String image, boolean featured, String sourceUrl,
            String retailer, String checkedDate) {}

    static final List<Product> PRODUCTS = List.of(
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím Logitech Pebble Keys 2 K380S White", "Logitech", "Số lượng phím: 78 phím; Kết nối: Bluetooth.", 790000L, 1129000L, 7, "/images/ban-phim-logitech-pebble-keys-2-k380s-white.jpg", true, "https://gearvn.com/products/ban-phim-logitech-pebble-keys-2-k380s-white", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím Rapoo V501-87", "Rapoo", "Số phím: 87 phím; Kết nối: Có dây USB; Đèn nền: RGB; Layout: TKL.", 590000L, 690000L, 22, "/images/ban-phim-rapoo-v501-87.jpg", false, "https://gearvn.com/products/ban-phim-rapoo-v501-87", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím cơ DareU không dây EK75 Pro WBC RGB Dream switch", "DareU", "Phương thức kết nối: Wireless, USB-C; Chất liệu Keycap: ABS; Đèn LED: RGB; Kích thước/Layout: 75%.", 1090000L, 1290000L, 29, "/images/ban-phim-co-dareu-khong-day-ek75-pro-wbc-rgb-dream-switch.png", false, "https://gearvn.com/products/ban-phim-co-dareu-khong-day-ek75-pro-wbc-rgb-dream-switch", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím DareU EK87V2 Black Brown Switch", "DareU", "Số lượng phím: 87 phím; Phương thức kết nối: Có dây; Chất liệu Keycap: ABS; Đèn LED: Rainbow.", 490000L, 699000L, 36, "/images/ban-phim-dareu-ek87v2-black-brown-switch.png", true, "https://gearvn.com/products/ban-phim-dareu-ek87v2-black-brown-switch", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím cơ DareU A98 Pro TM Clear Water Dream switch", "DareU", "Số lượng phím: 98 phím; Phương thức kết nối: Có dây, Bluetooth, Wireless 2.4GHz; Đèn LED: RGB; Kích thước/Layout: 98%.", 1390000L, 2350000L, 43, "/images/ban-phim-co-dareu-a98-pro-tm-clear-water-dream-switch.png", false, "https://gearvn.com/products/ban-phim-co-dareu-a98-pro-tm-clear-water-dream-switch", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím cơ DareU EK65s White Black PBT Dream Switch", "DareU", "Phương thức kết nối: Có dây; Chất liệu Keycap: PBT; Đèn LED: RGB; Kích thước/Layout: 65%.", 650000L, 899000L, 50, "/images/ban-phim-co-dareu-ek65s-white-black-pbt-dream-switch.png", false, "https://gearvn.com/products/ban-phim-co-dareu-ek65s-white-black-pbt-dream-switch", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím cơ Rapoo V500 Pro Multi-mode Wireless RGB Blue Switch", "Rapoo", "Phương thức kết nối: Wireless, Bluetooth, USB-C; Chất liệu Keycap: ABS; Đèn LED: RGB; Kích thước/Layout: Full-size (100%).", 790000L, 990000L, 57, "/images/ban-phim-co-rapoo-v500-pro-multi-mode-wireless-rgb.png", false, "https://gearvn.com/products/ban-phim-co-rapoo-v500-pro-multi-mode-wireless-rgb", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím cơ DareU EK75 Pro Sakura Pink Dream switch", "DareU", "Số lượng phím: 81 phím; Phương thức kết nối: Có dây, Bluetooth, Wireless 2.4GHz; Chất liệu Keycap: PBT Double Shot; Đèn LED: RGB.", 1490000L, 1690000L, 18, "/images/ban-phim-co-dareu-ek75-pro-sakura-pink-dream-switch.png", false, "https://gearvn.com/products/ban-phim-co-dareu-ek75-pro-sakura-pink-dream-switch", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím cơ DareU EK75 Rapid Trigger Black", "DareU", "Kết nối: USB-C; Chất liệu Keycap: PBT Double Shot; Đèn LED: RGB.", 1390000L, 2290000L, 25, "/images/ban-phim-co-dareu-ek75-rapid-trigger-black.png", false, "https://gearvn.com/products/ban-phim-co-dareu-ek75-rapid-trigger-black", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím cơ Gaming DAREU EK87 v2 Gray Black Dream Switch", "DareU", "Số lượng phím: 87 phím; Phương thức kết nối: Có dây; Chất liệu Keycap: ABS; Đèn LED: RGB.", 550000L, 699000L, 7, "/images/ban-phim-co-gaming-dareu-ek87-v2-gray-black-dream-switch.png", false, "https://gearvn.com/products/ban-phim-co-gaming-dareu-ek87-v2-gray-black-dream-switch", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím cơ gaming không dây DareU Flex 75 Black Grey 3 Mode Pegasus switch", "DareU", "Số lượng phím: 79 phím; Phương thức kết nối: Wireless 2.4GHz, Bluetooth, Có dây; Chất liệu Keycap: PBT; Đèn LED: RGB.", 690000L, 790000L, 39, "/images/ban-phim-co-gaming-khong-day-dareu-flex-75-black-grey-3-mode-pegasus-switch.jpg", false, "https://gearvn.com/products/ban-phim-co-gaming-khong-day-dareu-flex-75-black-grey-3-mode-pegasus-switch", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím DareU EK104 Rainbow White Black Dream Switch", "DareU", "Số lượng phím: 104 phím; Phương thức kết nối: USB; Đèn LED: Rainbow; Kích thước/Layout: Full-size (100%).", 720000L, 990000L, 46, "/images/ban-phim-dareu-ek104-rgb-white-black-dream-switch.jpg", false, "https://gearvn.com/products/ban-phim-dareu-ek104-rgb-white-black-dream-switch", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím Rapoo V700-A8 Grey White Red Switch", "Rapoo", "Số phím: 84 phím; Kết nối: Bluetooth, Wireless 2.4 GHz, USB-C; Pin: 4000 mAh; Đèn nền: LED trắng.", 1090000L, 1899000L, 53, "/images/ban-phim-rapoo-v700-a8-grey-white-red-switch.jpg", false, "https://gearvn.com/products/ban-phim-rapoo-v700-a8-grey-white-red-switch", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím có dây Logitech G515 RAPID TKL RGB Black", "Logitech", "Số lượng phím: 87 phím; Phương thức kết nối: Có dây; Chất liệu Keycap: PBT Double Shot; Đèn LED: LIGHTSYNC RGB.", 3790000L, 4490000L, 60, "/images/ban-phim-co-day-logitech-g515-rapid-tkl-rgb-black.png", true, "https://gearvn.com/products/ban-phim-co-day-logitech-g515-rapid-tkl-rgb-black", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím không dây Logitech MX Keys", "Logitech", "Kết nối: Bluetooth; Chất liệu Keycap: ABS; Layout: Full-size; Chiếu sáng: Đèn nền.", 2490000L, 2790000L, 21, "/images/ban-phim-khong-day-logitech-mx-keys.png", false, "https://gearvn.com/products/ban-phim-khong-day-logitech-mx-keys", "GEARVN", "2026-09-04"),
        new Product("Bàn phím", "ban-phim", "⌨", "Bàn phím Logitech G Pro X TKL Light Speed Tactile Switch Black", "Logitech", "Số lượng phím: 87 phím; Phương thức kết nối: Wireless 2.4GHz, USB, Bluetooth; Chất liệu Keycap: PBT Double Shot; Đèn LED: RGB.", 3990000L, 4790000L, 28, "/images/ban-phim-logitech-g-pro-x-tkl-light-speed-tactile-switch-black.jpg", false, "https://gearvn.com/products/ban-phim-logitech-g-pro-x-tkl-light-speed-tactile-switch-black", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột Logitech G102 LightSync Black", "Logitech", "Kết nối: Có dây USB; Độ phân giải tối đa: 8000 DPI; Cảm biến (Sensor): Quang học; Trọng lượng: 85 g.", 400000L, 599000L, 35, "/images/chuot-logitech-g102-lightsync-rgb-black.jpg", true, "https://gearvn.com/products/chuot-logitech-g102-lightsync-rgb-black", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột Logitech G304 Wireless White", "Logitech", "Kết nối: Wireless; Độ phân giải tối đa: 12000 DPI; Cảm biến (Sensor): HERO; Loại pin: Pin AA.", 725000L, 1190000L, 42, "/images/chuot-logitech-g304-lightspeed-wireless-white.jpg", true, "https://gearvn.com/products/chuot-logitech-g304-lightspeed-wireless-white", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột Razer DeathAdder Essential (RZ01-03850100-R3M1)", "Razer", "Kết nối: Có dây USB; Độ phân giải (DPI): 6400 DPI; Cảm biến (Sensor): Quang học; Trọng lượng: 96 g.", 370000L, 790000L, 7, "/images/razer-deathadder-essential.jpg", true, "https://gearvn.com/products/razer-deathadder-essential", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột Rapoo VT9 Air Gaming Không Dây Tím", "Rapoo", "Kết nối: Đa kết nối; Loại pin: Pin sạc Li-ion; Màu sắc: Tím.", 990000L, 1990000L, 56, "/images/chuot-rapoo-vt9-air-gaming-khong-day-tim.png", false, "https://gearvn.com/products/chuot-rapoo-vt9-air-gaming-khong-day-tim", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột Asus TUF Gaming M3 Gen II", "ASUS", "Kết nối: Có dây USB; Độ phân giải tối đa: 8000 DPI; Cảm biến (Sensor): PAW3318; Trọng lượng: 60 g.", 400000L, 490000L, 17, "/images/chuot-asus-tuf-gaming-m3-gen-ii.jpg", false, "https://gearvn.com/products/chuot-asus-tuf-gaming-m3-gen-ii", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột ASUS ROG Strix Impact III Wireless White", "ASUS", "Kết nối: Đa kết nối; Độ phân giải tối đa: 36000 DPI; Cảm biến (Sensor): Quang học ROG AimPoint; Loại pin: Pin AA, Pin AAA.", 1090000L, 1690000L, 24, "/images/chuot-asus-rog-strix-impact-iii-wireless-white.png", false, "https://gearvn.com/products/chuot-asus-rog-strix-impact-iii-wireless-white", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột DareU Không dây EM911T RGB Đen", "DareU", "Kết nối: Không dây; Màu sắc: Đen; Đèn LED: RGB.", 400000L, 690000L, 31, "/images/chuot-dareu-khong-day-em911t-rgb-den.jpg", false, "https://gearvn.com/products/chuot-dareu-khong-day-em911t-rgb-den", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột không dây Logitech M331 Silent Black", "Logitech", "Kết nối: Wireless; Độ phân giải tối đa: 1000 DPI; Cảm biến (Sensor): Quang học; Loại pin: Pin AA.", 360000L, 390000L, 38, "/images/chuot-khong-day-logitech-m331-silent.png", false, "https://gearvn.com/products/chuot-khong-day-logitech-m331-silent", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột không dây Logitech Signature Comfort M840 L White", "Logitech", "Kết nối: Bluetooth; Độ phân giải tối đa: 4000 DPI; Cảm biến (Sensor): Quang học; Loại pin: Pin AA.", 990000L, 1490000L, 45, "/images/chuot-khong-day-logitech-signature-comfort-m840-l-white.jpg", false, "https://gearvn.com/products/chuot-khong-day-logitech-signature-comfort-m840-l-white", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột gaming có dây Rapoo V260 Pro", "Rapoo", "Kết nối: Có dây USB; Độ phân giải tối đa: 6200 DPI; Cảm biến (Sensor): PMW3327; Màu sắc: Đen.", 329000L, 499000L, 52, "/images/chuot-gaming-co-day-rapoo-v260-pro.jpg", false, "https://gearvn.com/products/chuot-gaming-co-day-rapoo-v260-pro", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột gaming không dây Rapoo V300L", "Rapoo", "Kết nối: Không dây 2.4Ghz, USB-C to USB-A; Độ phân giải tối đa: 12000 DPI; Cảm biến (Sensor): Pixart PMW3311; Loại pin: Pin sạc.", 690000L, 790000L, 59, "/images/chuot-gaming-khong-day-rapoo-v300l.jpg", false, "https://gearvn.com/products/chuot-gaming-khong-day-rapoo-v300l", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột Rapoo MT760 Mini Không Dây Đen", "Rapoo", "Kết nối: Đa kết nối; Loại pin: Pin sạc Li-ion; Màu sắc: Đen.", 790000L, 990000L, 7, "/images/chuot-rapoo-mt760-mini-khong-day-den.png", false, "https://gearvn.com/products/chuot-rapoo-mt760-mini-khong-day-den", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột Logitech G Pro X Superlight 2 SE Black", "Logitech", "Kết nối: Không dây 2.4Ghz, USB-C to USB-A; Độ phân giải tối đa: 44000 DPI; Cảm biến (Sensor): Quang học HERO 2; Loại pin: Pin sạc.", 2690000L, 3190000L, 27, "/images/chuot-logitech-g-pro-x-superlight-2-se-black.jpg", false, "https://gearvn.com/products/chuot-logitech-g-pro-x-superlight-2-se-black", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột Razer DeathAdder V4 Pro Black", "Razer", "Kết nối: Đa kết nối; Độ phân giải tối đa: 45000 DPI; Cảm biến (Sensor): Razer Focus Pro 45K Optical Sensor Gen-2; Loại pin: Pin sạc Li-ion.", 3690000L, 4990000L, 34, "/images/chuot-razer-deathadder-v4-pro-black-1.png", false, "https://gearvn.com/products/chuot-razer-deathadder-v4-pro-black-1", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột ASUS P722 ROG KERIS II Origin WL Black", "ASUS", "Kết nối: Không dây 2.4Ghz, Bluetooth, USB-C to USB-A; Độ phân giải tối đa: 42000 DPI; Cảm biến (Sensor): Quang học ROG AimPoint Pro; Loại pin: Pin sạc.", 3350000L, 3900000L, 41, "/images/chuot-asus-p722-rog-keris-ii-origin-wl-black.png", false, "https://gearvn.com/products/chuot-asus-p722-rog-keris-ii-origin-wl-black", "GEARVN", "2026-09-04"),
        new Product("Chuột", "chuot", "◉", "Chuột HyperX Pulsefire Haste 2 Mini Wireless White", "HyperX", "Kết nối: Đa kết nối; Độ phân giải tối đa: 26000 DPI; Cảm biến (Sensor): HyperX 26K Sensor; Loại pin: Pin sạc Li-ion.", 2090000L, 2490000L, 48, "/images/chuot-hyperx-pulsefire-haste-2-mini-wireless-white.jpg", false, "https://gearvn.com/products/chuot-hyperx-pulsefire-haste-2-mini-wireless-white", "GEARVN", "2026-09-04"),
        new Product("Tai nghe & loa", "tai-nghe-loa", "♫", "Tai nghe HyperX Cloud Stinger 3 Black", "HyperX", "Kết nối: Có dây; Driver: 50 mm; Micro: Gập lên để tắt tiếng.", 1290000L, 1390000L, 55, "/images/tai-nghe-hyperx-cloud-stinger-3-black.jpg", true, "https://gearvn.com/products/tai-nghe-hyperx-cloud-stinger-3-black", "GEARVN", "2026-09-04"),
        new Product("Tai nghe & loa", "tai-nghe-loa", "♫", "Tai nghe không dây Logitech G325 LIGHTSPEED White", "Logitech", "Phương thức kết nối: Bluetooth, Wireless 2.4Ghz (USB Receiver); Kiểu tai nghe: Tai nghe Gaming; Trọng lượng: 212 g; Màu sắc: Trắng.", 2390000L, 2790000L, 16, "/images/tai-nghe-khong-day-logitech-g325-lightspeed-white.jpg", false, "https://gearvn.com/products/tai-nghe-khong-day-logitech-g325-lightspeed-white", "GEARVN", "2026-09-04"),
        new Product("Tai nghe & loa", "tai-nghe-loa", "♫", "Tai nghe HP HYPERX Cloud Earbuds III Black", "HyperX", "Phương thức kết nối: Có dây; Kiểu tai nghe: In-ear; Trọng lượng: 21.2 g; Đèn LED: Không.", 990000L, 1190000L, 23, "/images/tai-nghe-hp-hyperx-cloud-earbuds-iii-black.jpg", false, "https://gearvn.com/products/tai-nghe-hp-hyperx-cloud-earbuds-iii-black", "GEARVN", "2026-09-04"),
        new Product("Tai nghe & loa", "tai-nghe-loa", "♫", "Tai nghe Logitech G733 LIGHTSPEED Wireless Black", "Logitech", "Phương thức kết nối: Wireless 2.4Ghz (USB Receiver); Kiểu tai nghe: Over-ear; Trọng lượng: 278 g; Đèn LED: LED RGB.", 2290000L, 2890000L, 30, "/images/tai-nghe-logitech-g733-lightspeed-wireless-black.png", false, "https://gearvn.com/products/tai-nghe-logitech-g733-lightspeed-wireless-black", "GEARVN", "2026-09-04"),
        new Product("Tai nghe & loa", "tai-nghe-loa", "♫", "Tai nghe gaming không dây Logitech Astro A20 X", "Logitech", "Phương thức kết nối: Bluetooth, Wireless 2.4Ghz (USB Receiver), Có dây; Kiểu tai nghe: Gaming; Trọng lượng: 290 g; Đèn LED: LED RGB.", 3590000L, 4190000L, 7, "/images/tai-nghe-gaming-khong-day-logitech-astro-a20-x.jpg", true, "https://gearvn.com/products/tai-nghe-gaming-khong-day-logitech-astro-a20-x", "GEARVN", "2026-09-04"),
        new Product("Tai nghe & loa", "tai-nghe-loa", "♫", "Tai nghe không dây HyperX Cloud Flight 2 WL Black", "HyperX", "Phương thức kết nối: Không dây 2.4Ghz, Bluetooth; Kiểu tai nghe: Over-ear; Trọng lượng: 340 g; Đèn LED: LED RGB.", 3190000L, 3990000L, 44, "/images/tai-nghe-khong-day-hyperx-cloud-flight-2-wl-black.jpg", false, "https://gearvn.com/products/tai-nghe-khong-day-hyperx-cloud-flight-2-wl-black", "GEARVN", "2026-09-04"),
        new Product("Tai nghe & loa", "tai-nghe-loa", "♫", "Tai nghe Bluetooth chụp tai Sony WH-CH520", "Sony", "Kết nối: Bluetooth; Micro: Có; Trọng lượng: 147 gram; Điều khiển: Nút bấm vật lý.", 790000L, 1290000L, 51, "/images/sony-clean.png", false, "https://cellphones.com.vn/tai-nghe-chup-tai-sony-wh-ch520.html", "CellphoneS", "2026-09-04"),
        new Product("Tai nghe & loa", "tai-nghe-loa", "♫", "Loa Bluetooth JBL Go 4", "JBL", "Công suất: 4.2W; Chống nước: IP67; Thời lượng pin: 7 giờ; Trọng lượng: 190 g.", 1070000L, 0L, 58, "/images/jbl-clean.png", false, "https://cellphones.com.vn/loa-bluetooth-jbl-go-4.html", "CellphoneS", "2026-09-04"),
        new Product("Màn hình", "man-hinh", "▣", "Màn hình ViewSonic VA24G1-H 24\" IPS 144Hz", "ViewSonic", "Màn hình 24 inch, tấm nền IPS, tần số quét 144Hz.", 2290000L, 2590000L, 19, "/images/man-hinh-viewsonic-va24g1-h-24-ips-144hz.jpg", true, "https://gearvn.com/products/man-hinh-viewsonic-va24g1-h-24-ips-144hz", "GEARVN", "2026-09-04"),
        new Product("Màn hình", "man-hinh", "▣", "Màn hình ViewSonic VX24G30-W 24\" IPS 240Hz chuyên game", "ViewSonic", "Màn hình 24 inch, tấm nền IPS, tần số quét 240Hz.", 2990000L, 3490000L, 26, "/images/man-hinh-viewsonic-vx24g30-w-24-ips-240hz-chuyen-game.jpg", false, "https://gearvn.com/products/man-hinh-viewsonic-vx24g30-w-24-ips-240hz-chuyen-game", "GEARVN", "2026-09-04"),
        new Product("Màn hình", "man-hinh", "▣", "Màn hình AOC 24B15H3 24\" IPS 120Hz", "AOC", "Màn hình 24 inch, tấm nền IPS, tần số quét 120Hz.", 1890000L, 2290000L, 33, "/images/man-hinh-aoc-24b15h3-24-ips-120hz.jpg", true, "https://gearvn.com/products/man-hinh-aoc-24b15h3-24-ips-120hz", "GEARVN", "2026-09-04"),
        new Product("Màn hình", "man-hinh", "▣", "Màn hình Asus TUF GAMING VG249QE5A 24\" IPS 146Hz chuyên game", "ASUS", "Màn hình 24 inch, tấm nền IPS, tần số quét 146Hz.", 2590000L, 3990000L, 40, "/images/man-hinh-asus-tuf-gaming-vg249qe5a-24-ips-146hz-chuyen-game.jpg", false, "https://gearvn.com/products/man-hinh-asus-tuf-gaming-vg249qe5a-24-ips-146hz-chuyen-game", "GEARVN", "2026-09-04"),
        new Product("Màn hình", "man-hinh", "▣", "Màn hình LG 27G523B-B UltraGear 27\" IPS 200Hz Gsync chuyên game", "LG", "Màn hình 27 inch, tấm nền IPS, tần số quét 200Hz, hỗ trợ G-Sync.", 3590000L, 3990000L, 47, "/images/man-hinh-lg-27g523b-b-ultragear-27-ips-200hz-gsync-chuyen-game.jpg", false, "https://gearvn.com/products/man-hinh-lg-27g523b-b-ultragear-27-ips-200hz-gsync-chuyen-game", "GEARVN", "2026-09-04"),
        new Product("Màn hình", "man-hinh", "▣", "Màn hình LG 24U411A-B 24\" IPS 120Hz HDR10 siêu mỏng", "LG", "Màn hình 24 inch, tấm nền IPS, tần số quét 120Hz, hỗ trợ HDR10.", 2390000L, 2590000L, 7, "/images/man-hinh-lg-24u411a-b-24-ips-120hz-hdr10-sieu-mong.jpg", false, "https://gearvn.com/products/man-hinh-lg-24u411a-b-24-ips-120hz-hdr10-sieu-mong", "GEARVN", "2026-09-04"),
        new Product("Màn hình", "man-hinh", "▣", "Màn hình Samsung Odyssey G5 LS27FG502EEXXV 27\" IPS 2K 180Hz chuyên game", "Samsung", "Màn hình 27 inch, tấm nền IPS, độ phân giải 2K, tần số quét 180Hz.", 4890000L, 5590000L, 15, "/images/man-hinh-samsung-odyssey-g5-ls27fg502eexxv-27-ips-2k-180hz-chuyen-game.jpg", false, "https://gearvn.com/products/man-hinh-samsung-odyssey-g5-ls27fg502eexxv-27-ips-2k-180hz-chuyen-game", "GEARVN", "2026-09-04"),
        new Product("Màn hình", "man-hinh", "▣", "Màn Hình Samsung LS24F320GAEXXV 24\" IPS 120Hz", "Samsung", "Màn hình 24 inch, tấm nền IPS, tần số quét 120Hz.", 2490000L, 2990000L, 22, "/images/man-hinh-samsung-ls24f320gaexxv-24-ips-120hz.jpg", false, "https://gearvn.com/products/man-hinh-samsung-ls24f320gaexxv-24-ips-120hz", "GEARVN", "2026-09-04"),
        new Product("Màn hình", "man-hinh", "▣", "Màn hình BenQ Zowie XL2540X+ 24\" 280Hz chuyên game", "BenQ", "Màn hình chơi game 24 inch, tần số quét 280Hz.", 10790000L, 11990000L, 29, "/images/man-hinh-benq-zowie-xl2540x-24-280hz-chuyen-game.jpg", false, "https://gearvn.com/products/man-hinh-benq-zowie-xl2540x-24-280hz-chuyen-game", "GEARVN", "2026-09-04"),
        new Product("Màn hình", "man-hinh", "▣", "Màn hình ASUS ProArt PA249CGV 24\" IPS 144Hz USBC chuyên đồ họa", "ASUS", "Màn hình đồ họa 24 inch, tấm nền IPS, tần số quét 144Hz, kết nối USB-C.", 5490000L, 5690000L, 36, "/images/man-hinh-asus-proart-pa249cgv-24-ips-144hz-usbc-chuyen-do-hoa.jpg", false, "https://gearvn.com/products/man-hinh-asus-proart-pa249cgv-24-ips-144hz-usbc-chuyen-do-hoa", "GEARVN", "2026-09-04"),
        new Product("Màn hình", "man-hinh", "▣", "Màn hình LG 27U730B-B 27\" IPS 4K HDR10 USBC chuyên đồ họa", "LG", "Màn hình đồ họa 27 inch, tấm nền IPS, độ phân giải 4K, kết nối USB-C.", 8790000L, 0L, 43, "/images/man-hinh-lg-27u730b-b-27-ips-4k-hdr10-usbc-chuyen-do-hoa.jpg", false, "https://gearvn.com/products/man-hinh-lg-27u730b-b-27-ips-4k-hdr10-usbc-chuyen-do-hoa", "GEARVN", "2026-09-04"),
        new Product("Màn hình", "man-hinh", "▣", "Màn hình Samsung Odyssey G6 LS27HG612SEXXV QD-OLED 27\" 2K 240Hz chuyên game", "Samsung", "Màn hình 27 inch, tấm nền QD-OLED, độ phân giải 2K, tần số quét 240Hz.", 12890000L, 13990000L, 50, "/images/man-hinh-samsung-odyssey-g6-ls27hg612sexxv-qd-oled-27-2k-240hz-chuyen-game.jpg", false, "https://gearvn.com/products/man-hinh-samsung-odyssey-g6-ls27hg612sexxv-qd-oled-27-2k-240hz-chuyen-game", "GEARVN", "2026-09-04"),
        new Product("Laptop", "laptop", "▱", "Laptop Acer Aspire Lite 15 AL15-53P-56QH (Core 5-120U/ 8GB/ 512GB/ 15.6\" FHD/ Win 11)", "ACER", "Intel Core 5-120U, RAM 8GB, SSD 512GB, màn hình 15,6 inch Full HD.", 18890000L, 24990000L, 57, "/images/laptop-acer-aspire-lite-15-al15-53p-56qh.jpg", true, "https://gearvn.com/products/laptop-acer-aspire-lite-15-al15-53p-56qh", "GEARVN", "2026-09-04"),
        new Product("Laptop", "laptop", "▱", "Laptop Acer Aspire Lite 15 AL15-49P-R6XX (Ryzen 5 7430U/ 8GB/ 512GB/15.6\"/ Win 11)", "ACER", "AMD Ryzen 5 7430U, RAM 8GB, SSD 512GB, màn hình 15,6 inch.", 18590000L, 20990000L, 18, "/images/laptop-acer-aspire-lite-15-al15-49p-r6xx-ryzen-5-7430u-8gb-512gb-15-6-win-11.jpg", false, "https://gearvn.com/products/laptop-acer-aspire-lite-15-al15-49p-r6xx-ryzen-5-7430u-8gb-512gb-15-6-win-11", "GEARVN", "2026-09-04"),
        new Product("Laptop", "laptop", "▱", "Laptop ASUS ExpertBook B1 BM1403CDA-S61611W (Ryzen 5-150/ 16GB/ 512GB/ 14\" FHD/ Win 11)", "ASUS", "AMD Ryzen 5-150, RAM 16GB, SSD 512GB, màn hình 14 inch Full HD.", 18800000L, 22490000L, 7, "/images/laptop-asus-expertbook-b1-bm1403cda-s61611w.jpg", false, "https://gearvn.com/products/laptop-asus-expertbook-b1-bm1403cda-s61611w", "GEARVN", "2026-09-04"),
        new Product("Laptop", "laptop", "▱", "Laptop Acer Aspire Lite 15 AL15-46P-R73C (Ryzen 3 5400U/ 8GB/ 512GB/ 15.6\" FHD/ Win 11)", "ACER", "AMD Ryzen 3 5400U, RAM 8GB, SSD 512GB, màn hình 15,6 inch Full HD.", 15990000L, 16990000L, 32, "/images/laptop-acer-aspire-lite-15-al15-46p-r73c.jpg", false, "https://gearvn.com/products/laptop-acer-aspire-lite-15-al15-46p-r73c", "GEARVN", "2026-09-04"),
        new Product("Laptop", "laptop", "▱", "Laptop Dell 15 Pro Essential PV15250 VKVKD (I3-100U/ 8GB/ 512GB/ 15.6\" FHD 120Hz/DOS) - Nhập Khẩu Chính Hãng", "DELL", "RAM 8GB, SSD 512GB, màn hình 15,6 inch Full HD 120Hz, hệ điều hành DOS.", 14390000L, 14990000L, 39, "/images/laptop-dell-15-pro-essential-pv15250-vkvkd-i3-100u-8gb-512gb-15-6-fhd-120hz.jpg", false, "https://gearvn.com/products/laptop-dell-15-pro-essential-pv15250-vkvkd-i3-100u-8gb-512gb-15-6-fhd-120hz", "GEARVN", "2026-09-04"),
        new Product("Laptop", "laptop", "▱", "Laptop Asus Vivobook 14 X1404V (Core 5-120U/ 8GB/ 256GB/ 14\" FHD/ Win 11)", "ASUS", "Intel Core 5-120U, RAM 8GB, SSD 256GB, màn hình 14 inch Full HD.", 16990000L, 18990000L, 46, "/images/laptop-asus-vivobook-14-x1404v-core-5-120u-8gb-256gb-14-fhd-win-11.jpg", false, "https://gearvn.com/products/laptop-asus-vivobook-14-x1404v-core-5-120u-8gb-256gb-14-fhd-win-11", "GEARVN", "2026-09-04"),
        new Product("Laptop", "laptop", "▱", "Laptop Asus Vivobook 16 X1605VA-MB1826W (I3-1315U/ 8GB/ 512GB/ 16\" WUXGA/ Win 11)", "ASUS", "Intel Core i3-1315U, RAM 8GB, SSD 512GB, màn hình 16 inch WUXGA.", 15390000L, 15990000L, 53, "/images/laptop-asus-vivobook-16-x1605va-mb1826w-i3-1315u-8gb-512gb-16-wuxga-win-11.jpg", false, "https://gearvn.com/products/laptop-asus-vivobook-16-x1605va-mb1826w-i3-1315u-8gb-512gb-16-wuxga-win-11", "GEARVN", "2026-09-04"),
        new Product("Laptop", "laptop", "▱", "Laptop Dell DC15250-5434BLK M4CFY (I5-1334U/ 8GB/ 512GB/ 15.6\" FHD Touch/ Win 11) - Nhập Khẩu Chính Hãng", "DELL", "Intel Core i5-1334U, RAM 8GB, SSD 512GB, màn hình Full HD cảm ứng.", 17190000L, 17990000L, 60, "/images/laptop-dell-dc15250-5434blk-m4cfy-i5-1334u-8gb-512gb-15-6-fhd-touch-win-11.jpg", false, "https://gearvn.com/products/laptop-dell-dc15250-5434blk-m4cfy-i5-1334u-8gb-512gb-15-6-fhd-touch-win-11", "GEARVN", "2026-09-04"),
        new Product("Laptop", "laptop", "▱", "Laptop gaming Lenovo LOQ 15ARP10E 83S0006RVN (Ryzen 7-7735HS/ RTX 3050 6GB/ 16GB/ 1TB/ 15.6\" FHD/ Win 11)", "LENOVO", "AMD Ryzen 7-7735HS, RTX 3050 6GB, RAM 16GB, SSD 1TB.", 30690000L, 31990000L, 21, "/images/laptop-gaming-lenovo-loq-15arp10e-83s0006rvn-ryzen-7-7735hs-rtx-3050-6gb-16gb-1tb-15-6-fhd-win-11.png", false, "https://gearvn.com/products/laptop-gaming-lenovo-loq-15arp10e-83s0006rvn-ryzen-7-7735hs-rtx-3050-6gb-16gb-1tb-15-6-fhd-win-11", "GEARVN", "2026-09-04"),
        new Product("Laptop", "laptop", "▱", "Laptop Acer Swift Go AI OLED SFG14-75-765M (Ultra 7-258V/ 32GB/ 512GB/ 14\" FHD+ OLED/ Win11)", "ACER", "Intel Core Ultra 7-258V, RAM 32GB, SSD 512GB, màn hình 14 inch FHD+ OLED.", 39990000L, 40990000L, 28, "/images/laptop-acer-swift-go-ai-oled-sfg14-75-765m-14-fhd-oled-u7-258v-32lp-512gb-win.jpg", false, "https://gearvn.com/products/laptop-acer-swift-go-ai-oled-sfg14-75-765m-14-fhd-oled-u7-258v-32lp-512gb-win", "GEARVN", "2026-09-04"),
        new Product("Laptop", "laptop", "▱", "Laptop gaming GIGABYTE A16 CVHI3MY893SH (I7-13620H/ RTX 5060 8GB/ 16GB/ 512GB/ 16\" FHD+ 165Hz/ Win 11)", "GIGABYTE", "Intel Core i7-13620H, RTX 5060 8GB, RAM 16GB, SSD 512GB.", 36990000L, 38990000L, 35, "/images/laptop-gaming-gigabyte-a16-cvhi3my893sh.jpg", false, "https://gearvn.com/products/laptop-gaming-gigabyte-a16-cvhi3my893sh", "GEARVN", "2026-09-04"),
        new Product("Laptop", "laptop", "▱", "Laptop gaming ASUS ROG Strix G16 G614PM-TS147W (Ryzen 9-8940HX/ RTX 5060 8GB/ 16GB/ 512GB/ 16\" WQXGA 300Hz/ Win 11)", "ASUS", "AMD Ryzen 9-8940HX, RTX 5060 8GB, RAM 16GB, màn hình 16 inch WQXGA 300Hz.", 57690000L, 65290000L, 7, "/images/laptop-gaming-asus-rog-strix-g16-g614pm-ts147w.jpg", false, "https://gearvn.com/products/laptop-gaming-asus-rog-strix-g16-g614pm-ts147w", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Cổng chuyển HyperDrive PRO 8-in-2 Hub for USB-C MacBook Pro - GN28D GREY", "HyperDrive", "Hub 8-in-2 cho MacBook Pro USB-C, vỏ nhôm màu xám.", 1490000L, 2200000L, 49, "/images/cong-chuyen-hyperdrive-pro-8-in-2-hub-gn28d.jpg", true, "https://gearvn.com/products/cong-chuyen-hyperdrive-pro-8-in-2-hub-gn28d", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Cáp sạc Belkin Type C - Type C 60W 2m trắng CAB003", "Belkin", "Cáp USB-C sang USB-C, công suất 60W, dài 2m, màu trắng.", 210000L, 417000L, 56, "/images/cap-sac-belkin-type-c-type-c-60w-2m-trang-cab003.jpg", false, "https://gearvn.com/products/cap-sac-belkin-type-c-type-c-60w-2m-trang-cab003", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Củ Sạc Ugreen GaN Nexode 30W CD319 Gr 90901", "Ugreen", "Củ sạc công nghệ GaN, công suất 30W, vỏ nhựa ABS màu xám.", 190000L, 350000L, 17, "/images/cu-sac-gan-nexode-30w-ugreen-cd319-gr-90901.gif", false, "https://gearvn.com/products/cu-sac-gan-nexode-30w-ugreen-cd319-gr-90901", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Bộ Chuyển Đổi Ugreen USBA to USBC 50533", "Ugreen", "Bộ chuyển USB-A sang USB-C, vỏ nhựa ABS màu đen.", 110000L, 140000L, 24, "/images/bo-chuyen-doi-usba-to-usbc-ugreen-50533.gif", false, "https://gearvn.com/products/bo-chuyen-doi-usba-to-usbc-ugreen-50533", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Củ Sạc Ugreen GaN 100W CD226 Gr 40747", "Ugreen", "Củ sạc công nghệ GaN, công suất 100W, vỏ nhựa ABS màu xám.", 990000L, 1260000L, 31, "/images/cu-sac-gan-100w-ugreen-cd226-gr-40747.gif", false, "https://gearvn.com/products/cu-sac-gan-100w-ugreen-cd226-gr-40747", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Bộ Chuyển Đổi Ugreen 7 in 1 CM212", "Ugreen", "Hub 7-in-1, vỏ nhôm, màu xám.", 1375000L, 0L, 38, "/images/bo-chuyen-doi-ugreen-7-in-1-cm212.jpg", false, "https://gearvn.com/products/bo-chuyen-doi-ugreen-7-in-1-cm212", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Webcam Logitech Brio 100 Graphite", "Logitech", "Webcam Full HD 1080p, 30 FPS, cảm biến CMOS 2MP, tích hợp micro.", 690000L, 750000L, 45, "/images/webcam-logitech-brio-100-graphite.png", false, "https://gearvn.com/products/webcam-logitech-brio-100-graphite", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Webcam Rapoo C200 HD 720p", "Rapoo", "Webcam HD 720p, 30 FPS, tích hợp micro.", 590000L, 0L, 52, "/images/webcam-rapoo-c200-hd-720p.jpg", false, "https://gearvn.com/products/webcam-rapoo-c200-hd-720p", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "WebCam Logitech C922 Pro Stream", "Logitech", "Webcam hỗ trợ Full HD 1080p và HD 720p, tốc độ tối đa 60 FPS, tích hợp micro.", 2290000L, 2590000L, 7, "/images/webcam-logitech-c922.jpg", false, "https://gearvn.com/products/webcam-logitech-c922", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Webcam Logitech Brio 4k", "Logitech", "Webcam độ phân giải 4K, màu đen.", 4590000L, 0L, 20, "/images/webcam-logitech-brio-4k.png", false, "https://gearvn.com/products/webcam-logitech-brio-4k", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Ram Laptop Kingston 8GB CL22 3200 Sodimm (KVR32S22S8/8WP)", "Kingston", "RAM laptop DDR4 8GB, bus 3200MHz, độ trễ CL22, chuẩn SO-DIMM.", 2990000L, 0L, 27, "/images/8gb-ddr4-1x8g-3200-ram-laptop-kingston-8gb-sodimm.jpg", false, "https://gearvn.com/products/8gb-ddr4-1x8g-3200-ram-laptop-kingston-8gb-sodimm", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Ổ cứng SSD Kingston NV3 1TB M.2 PCIe NVMe Gen4", "Kingston", "SSD 1TB dạng M.2 NVMe, giao tiếp PCIe Gen 4.0 x4, tốc độ ghi 4.000MB/s.", 4990000L, 5990000L, 34, "/images/o-cung-ssd-kingston-nv3-1tb-m-2-pcie-nvme-gen4.png", false, "https://gearvn.com/products/o-cung-ssd-kingston-nv3-1tb-m-2-pcie-nvme-gen4", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Ổ cứng SSD Kingston NV3 500GB M.2 PCIe NVMe Gen4", "Kingston", "SSD 500GB dạng M.2 NVMe, giao tiếp PCIe Gen 4.0 x4, tốc độ ghi 3.000MB/s.", 3890000L, 5490000L, 41, "/images/o-cung-ssd-kingston-nv3-500gb-m-2-pcie-nvme-gen4.png", false, "https://gearvn.com/products/o-cung-ssd-kingston-nv3-500gb-m-2-pcie-nvme-gen4", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "NZXT Internal USB Hub - Gen 3 (AC-IUSBH-M3)", "NZXT", "Hub USB nội bộ NZXT, thế hệ Gen 3.", 590000L, 0L, 48, "/images/nzxt-internal-usb-hub-gen-3.png", false, "https://gearvn.com/products/nzxt-internal-usb-hub-gen-3", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Phụ kiện UGreen Mini DP to DVI converter cable 1.5M White (MD102 -10443)", "Ugreen", "Cáp chuyển Mini DisplayPort sang DVI, dài 1,5m, màu trắng.", 320000L, 0L, 55, "/images/mini-dp-to-dvi-converter-cable-md102.jpg", false, "https://gearvn.com/products/mini-dp-to-dvi-converter-cable-md102", "GEARVN", "2026-09-04"),
        new Product("Phụ kiện", "phu-kien", "⌁", "Phụ Kiện UGreen DisplayPort to VGA converter (DP109)", "Ugreen", "Bộ chuyển tín hiệu DisplayPort sang VGA, mã DP109.", 350000L, 0L, 16, "/images/dp-to-vga-female-converter-dp109.jpg", false, "https://gearvn.com/products/dp-to-vga-female-converter-dp109", "GEARVN", "2026-09-04")
    );

    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        Path configFile = Path.of(".local/application.properties");
        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                config.load(reader);
            }
        }
        String url = setting(config, "DB_URL", "spring.datasource.url",
            "jdbc:sqlserver://localhost:1433;databaseName=ProductStore;encrypt=true;trustServerCertificate=true");
        String username = setting(config, "DB_USERNAME", "spring.datasource.username", "productstore_app");
        String password = setting(config, "DB_PASSWORD", "spring.datasource.password", "");
        if (password.isBlank()) throw new IllegalStateException("Chưa có mật khẩu SQL. Chạy scripts/setup-local.ps1 trước.");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("SELECT DB_NAME() database_name, CAST(SERVERPROPERTY('ServerName') AS NVARCHAR(128)) server_name")) {
                result.next();
                String database = result.getString("database_name");
                if (!"ProductStore".equalsIgnoreCase(database)) {
                    throw new IllegalStateException("Dừng nạp: database phải là ProductStore, hiện tại là " + database);
                }
                System.out.println("[OK] ĐÃ KẾT NỐI SQL SERVER: " + result.getString("server_name"));
                System.out.println("[OK] DATABASE: " + database);
            }
            connection.setAutoCommit(false);
            int inserted = 0;
            try {
                for (Product product : PRODUCTS) {
                    long categoryId = categoryId(connection, product);
                    // Khóa theo nguồn sản phẩm giúp chạy lại không nạp trùng.
                    try (PreparedStatement check = connection.prepareStatement("SELECT id FROM Products WITH(UPDLOCK,HOLDLOCK) WHERE source_url=?")) {
                        check.setString(1, product.sourceUrl());
                        try (ResultSet result = check.executeQuery()) { if (result.next()) continue; }
                    }
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO Products(category_id,name,brand,description,price,original_price,stock,image_url,active,featured,source_url,source_retailer,price_checked_at) VALUES(?,?,?,?,?,?,?,?,1,?,?,?,?)")) {
                        insert.setLong(1, categoryId);
                        insert.setNString(2, product.name());
                        insert.setNString(3, product.brand());
                        insert.setNString(4, product.description());
                        insert.setBigDecimal(5, BigDecimal.valueOf(product.price()));
                        insert.setBigDecimal(6, BigDecimal.valueOf(product.originalPrice()));
                        insert.setInt(7, product.stock());
                        insert.setNString(8, product.image());
                        insert.setBoolean(9, product.featured());
                        insert.setNString(10, product.sourceUrl());
                        insert.setNString(11, product.retailer());
                        insert.setDate(12, Date.valueOf(product.checkedDate()));
                        inserted += insert.executeUpdate();
                    }
                }
                connection.commit();
                System.out.println("[OK] Đã thêm " + inserted + " sản phẩm; bỏ qua " + (PRODUCTS.size()-inserted) + " sản phẩm đã tồn tại.");
                try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM Products WHERE active=1")) {
                    result.next(); System.out.println("[OK] Tổng sản phẩm đang bán trong SQL Server: " + result.getInt(1));
                }
                System.out.println("Có thể xóa SeedProducts.java sau khi nạp. Website không cần file này để chạy.");
            } catch (Exception error) {
                connection.rollback();
                throw error;
            }
        } catch (SQLException error) {
            System.err.println("[LỖI] Chưa kết nối/nạp được SQL Server. Kiểm tra SQL Server, cổng 1433 và thông tin trong .local/application.properties.");
            throw error;
        }
    }

    static long categoryId(Connection connection, Product product) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("SELECT id FROM Categories WITH(UPDLOCK,HOLDLOCK) WHERE slug=?")) {
            query.setString(1, product.slug());
            try (ResultSet result = query.executeQuery()) { if (result.next()) return result.getLong(1); }
        }
        try (PreparedStatement insert = connection.prepareStatement("INSERT INTO Categories(name,slug,icon) OUTPUT INSERTED.id VALUES(?,?,?)")) {
            insert.setNString(1, product.category()); insert.setString(2, product.slug()); insert.setNString(3, product.icon());
            try (ResultSet result = insert.executeQuery()) { result.next(); return result.getLong(1); }
        }
    }

    static String setting(Properties config, String envName, String key, String fallback) {
        String environment = System.getenv(envName);
        return environment != null && !environment.isBlank() ? environment : config.getProperty(key, fallback);
    }
}
