# --- Giai đoạn 1: Xây dựng ứng dụng ---
# Sử dụng một ảnh nền có sẵn Gradle và Java 11 để xây dựng
FROM gradle:7.6.1-jdk11 AS build

# Đặt thư mục làm việc
WORKDIR /home/gradle/src

# Sao chép toàn bộ mã nguồn của dự án vào container
COPY . .

# Chạy lệnh Gradle để tạo ra file .jar duy nhất (fat jar)
# Lệnh này sẽ tự động tải các thư viện cần thiết
RUN gradle shadowJar --no-daemon

# --- Giai đoạn 2: Chạy ứng dụng ---
# Sử dụng một ảnh nền nhẹ chỉ có Java để chạy
FROM openjdk:11-jre-slim

# Đặt thư mục làm việc
WORKDIR /app

# Sao chép file .jar đã được tạo ra ở giai đoạn 1 vào container này
COPY --from=build /home/gradle/src/build/libs/CMR-Server-0.0.1-all.jar .

# Mở cổng 10000 để nhận kết nối (Render yêu cầu cổng này)
EXPOSE 10000

# Lệnh để khởi động máy chủ khi container chạy
CMD ["java", "-jar", "CMR-Server-0.0.1-all.jar"]
