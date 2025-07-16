# --- Giai đoạn 1: Xây dựng ứng dụng ---
# Sử dụng một ảnh nền có sẵn Gradle và Java 11 để xây dựng
FROM gradle:7.6.1-jdk11 AS build

# Đặt thư mục làm việc
WORKDIR /home/gradle/src

# Sao chép toàn bộ mã nguồn của dự án vào container
COPY . .

# Cấp quyền thực thi cho Gradle wrapper
RUN chmod +x ./gradlew

# Chạy lệnh Gradle bằng wrapper để tạo ra file .jar
RUN ./gradlew shadowJar --no-daemon

# --- Giai đoạn 2: Chạy ứng dụng ---
# Sử dụng một ảnh nền nhẹ chỉ có Java để chạy
FROM openjdk:11-jre-slim

# Đặt thư mục làm việc
WORKDIR /app

# SỬA LỖI: Sao chép file .jar bằng ký tự đại diện (*) và đổi tên nó thành application.jar
COPY --from=build /home/gradle/src/build/libs/*.jar application.jar

# Render sẽ tự động mở cổng 10000
EXPOSE 10000

# SỬA LỖI: Chạy file application.jar đã được đổi tên
CMD ["java", "-jar", "application.jar"]
