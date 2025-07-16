# Sử dụng một ảnh nền có sẵn Java 11
FROM openjdk:11-jre-slim

# Tạo một thư mục làm việc bên trong container
WORKDIR /app

# Sao chép file .jar đã được xây dựng vào trong container
COPY build/libs/CMR-Server-0.0.1-all.jar .

# Mở cổng 8080 để nhận kết nối (Render sẽ tự động dùng cổng 10000)
EXPOSE 10000

# Lệnh để khởi động máy chủ khi container chạy
CMD ["java", "-jar", "CMR-Server-0.0.1-all.jar"]
