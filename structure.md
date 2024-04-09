hooYah
    │
    ├── client: Người dùng
    │   └── src
    │       └── main: chứa code
    │       │   └── com.psvm.client
    │       │   │   └── models: code tương tác với database
    │       │   │   └── views: code UI
    │       │   │   └── controllers: code xử lí, biến đổi dữ liệu thu được từ DB và cập nhật tới UI, networking (nên có folder networking riêng?)
    │       └── resources: chứa tài nguyên như hình ảnh, font...
    │
    └── server : Quản trị viên
        └── src
            └── main: chứa code
            │   └── com.psvm.server
            │   │   └── models: code tương tác với database
            │   │   └── views: code UI
            │   │   └── controllers: code xử lí, biến đổi dữ liệu thu được từ DB và cập nhật tới UI, networking (nên có folder networking riêng?)
            └── resources: chứa tài nguyên như hình ảnh, font...