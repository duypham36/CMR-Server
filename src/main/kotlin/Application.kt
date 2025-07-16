package com.example

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

// --- Các cấu trúc dữ liệu (Data Structures) ---

/**
 * Đại diện cho một kết nối của client (Cha hoặc Con) đến server.
 * @param session Đối tượng WebSocket của kết nối.
 * @param role Vai trò của client ("parent" hoặc "child").
 * @param roomId Mã phòng mà client tham gia.
 */
data class Connection(val session: DefaultWebSocketSession, val role: String, val roomId: String)

/**
 * Đại diện cho một tin nhắn điều khiển mà client gửi lên server.
 * Dùng để tham gia vào một phòng.
 */
@Serializable
data class ControlMessage(val type: String, val role: String, val roomId: String)


// --- Đối tượng quản lý chính ---

/**
 * Quản lý tất cả các phòng và các kết nối trong đó.
 * Dùng ConcurrentHashMap để đảm bảo an toàn khi có nhiều client kết nối cùng lúc.
 * - Key: String - là mã phòng (roomId).
 * - Value: MutableMap<DefaultWebSocketSession, Connection> - là một danh sách các kết nối trong phòng đó.
 */
val rooms = ConcurrentHashMap<String, MutableMap<DefaultWebSocketSession, Connection>>()


// --- Hàm Main để khởi động Server ---

fun main() {
    // Khởi động một server Netty chạy trên tất cả các địa chỉ IP (0.0.0.0) tại cổng 8080.
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}


// --- Module chính cấu hình ứng dụng Ktor ---d

fun Application.module() {
    // Cài đặt các plugin cần thiết
    install(WebSockets)
    install(ContentNegotiation) {
        json() // Cho phép server hiểu và tạo ra JSON
    }

    // Cấu hình định tuyến (routing)
    routing {
        // Tạo một endpoint WebSocket tại đường dẫn "/camera"
        webSocket("/camera") {
            var myConnection: Connection? = null

            try {
                // Vòng lặp vô hạn để lắng nghe các frame (tin nhắn) từ client
                for (frame in incoming) {
                    when (frame) {
                        // Xử lý khi nhận được tin nhắn dạng văn bản (Text)
                        is Frame.Text -> {
                            val text = frame.readText()
                            // Chuyển đổi chuỗi JSON thành đối tượng ControlMessage
                            val message = Json.decodeFromString<ControlMessage>(text)

                            // Nếu là tin nhắn yêu cầu tham gia phòng
                            if (message.type == "join") {
                                myConnection = Connection(this, message.role, message.roomId)
                                // Tạo phòng mới nếu chưa tồn tại
                                rooms.computeIfAbsent(message.roomId) { ConcurrentHashMap() }
                                // Thêm kết nối hiện tại vào phòng
                                rooms[message.roomId]?.put(this, myConnection)
                                println("Client đã tham gia: Vai trò=${myConnection.role}, Mã phòng=${myConnection.roomId}")
                            }
                        }

                        // Xử lý khi nhận được tin nhắn dạng nhị phân (Binary) - tức là frame video
                        is Frame.Binary -> {
                            myConnection?.let { conn ->
                                // Chỉ chuyển tiếp video nếu người gửi là "child"
                                if (conn.role == "child") {
                                    val videoFrame = frame.readBytes()
                                    // Tìm kết nối của "parent" trong cùng phòng
                                    val parentConnection = rooms[conn.roomId]?.values?.find { it.role == "parent" }
                                    // Gửi frame video cho "parent"
                                    parentConnection?.session?.send(Frame.Binary(true, videoFrame))
                                }
                            }
                        }
                        else -> {} // Bỏ qua các loại frame khác
                    }
                }
            } catch (e: Exception) {
                // Ghi lại lỗi nếu có sự cố
                println("Lỗi: ${e.localizedMessage}")
            } finally {
                // Khối này sẽ được thực thi khi một client ngắt kết nối
                myConnection?.let {
                    // Xóa client khỏi phòng
                    rooms[it.roomId]?.remove(it.session)
                    println("Client đã ngắt kết nối: Vai trò=${it.role}, Mã phòng=${it.roomId}")
                    // Nếu phòng không còn ai, xóa phòng đó đi
                    if (rooms[it.roomId]?.isEmpty() == true) {
                        rooms.remove(it.roomId)
                        println("Phòng ${it.roomId} đã trống và bị xóa.")
                    }
                }
            }
        }
    }
}
