-- Chèn dữ liệu vào bảng topic
INSERT INTO topic (topic_id, title, description, long_description, create_at, update_at, is_deleted) 
VALUES 
(1, 'Giao diện người dùng', 'Các chủ đề liên quan đến phát triển phía client', 'Phát triển giao diện người dùng tập trung vào việc tạo ra giao diện và trải nghiệm người dùng cho ứng dụng web bằng các công nghệ như HTML, CSS, JavaScript và các framework như ReactJS.', NOW(), NULL, FALSE),
(2, 'Hệ thống backend', 'Lập trình và kiến trúc phía server', 'Phát triển backend liên quan đến xây dựng logic phía server, API và tương tác với cơ sở dữ liệu bằng các công nghệ như Java, Spring Boot, Node.js và cơ sở dữ liệu SQL.', NOW(), NULL, FALSE),
(3, 'Fullstack', 'Cả giao diện người dùng và hệ thống backend', 'Phát triển Fullstack bao gồm cả phát triển giao diện người dùng và hệ thống backend, yêu cầu thành thạo các framework phía client như ReactJS và công nghệ phía server như Spring Boot, SQL.', NOW(), NULL, FALSE);

-- Chèn dữ liệu vào bảng tag
INSERT INTO tag (tag_id, title, description, create_at, update_at, is_deleted) 
VALUES 
(1, 'Java', 'Ngôn ngữ lập trình', NOW(), NULL, FALSE),
(2, 'JavaScript', 'Ngôn ngữ lập trình web', NOW(), NULL, FALSE),
(3, 'Spring Boot', 'Framework backend của Java', NOW(), NULL, FALSE),
(4, 'ReactJS', 'Thư viện JavaScript để xây dựng giao diện', NOW(), NULL, FALSE),
(5, 'SQL', 'Ngôn ngữ truy vấn cơ sở dữ liệu', NOW(), NULL, FALSE),
(6, 'Cấu trúc dữ liệu', 'Kiến thức cơ bản về tổ chức dữ liệu', NOW(), NULL, FALSE);

-- Topic 1: Giao diện người dùng
INSERT INTO topic_tag (topic_id, tag_id) VALUES
(1, 2), -- JavaScript
(1, 4); -- ReactJS

-- Topic 2: Hệ thống backend
INSERT INTO topic_tag (topic_id, tag_id) VALUES
(2, 1), -- Java
(2, 3), -- Spring Boot
(2, 5), -- SQL
(2, 6); -- Cấu trúc dữ liệu

-- Topic 3: Fullstack
INSERT INTO topic_tag (topic_id, tag_id) VALUES
(3, 1), -- Java
(3, 2), -- JavaScript
(3, 3), -- Spring Boot
(3, 4), -- ReactJS
(3, 5); -- SQL

-- Chèn dữ liệu vào bảng interview_session cho Giao diện người dùng
INSERT INTO interview_session (interview_session_id, topic_id, title, description, total_question, difficulty, duration_estimate, create_at, update_at, is_deleted)
VALUES 
(1, 1, 'Phỏng vấn cơ bản về giao diện người dùng', 'Bao gồm các kiến thức cơ bản về HTML, CSS và JavaScript.', 10, 'EASY', 60, NOW(), NULL, FALSE),
(2, 1, 'Phỏng vấn lập trình viên ReactJS', 'Tập trung vào ReactJS, quản lý trạng thái và phát triển giao diện.', 10, 'MEDIUM', 75, NOW(), NULL, FALSE),
(3, 1, 'Phỏng vấn nâng cao về giao diện người dùng', 'Khám phá JavaScript nâng cao, tối ưu hiệu suất và khả năng truy cập.', 10, 'HARD', 90, NOW(), NULL, FALSE);

-- Chèn dữ liệu vào bảng interview_session cho Hệ thống backend
INSERT INTO interview_session (interview_session_id, topic_id, title, description, total_question, difficulty, duration_estimate, create_at, update_at, is_deleted)
VALUES 
(4, 2, 'Phỏng vấn cơ bản về backend', 'Bao gồm các khái niệm phía server, REST API và thao tác cơ sở dữ liệu cơ bản.', 10, 'EASY', 60, NOW(), NULL, FALSE),
(5, 2, 'Phỏng vấn lập trình viên Spring Boot', 'Tập trung vào Java, Spring Boot và dịch vụ RESTful.', 10, 'MEDIUM', 75, NOW(), NULL, FALSE),
(6, 2, 'Phỏng vấn nâng cao về backend', 'Khám phá microservices, khả năng mở rộng và tối ưu cơ sở dữ liệu.', 10, 'HARD', 90, NOW(), NULL, FALSE);

-- Chèn dữ liệu vào bảng interview_session cho Fullstack
INSERT INTO interview_session (interview_session_id, topic_id, title, description, total_question, difficulty, duration_estimate, create_at, update_at, is_deleted)
VALUES 
(7, 3, 'Phỏng vấn cơ bản về Fullstack', 'Bao gồm các khái niệm cơ bản về giao diện người dùng và backend.', 10, 'EASY', 60, NOW(), NULL, FALSE),
(8, 3, 'Phỏng vấn MERN Stack', 'Tập trung vào MongoDB, Express.js, React và Node.js.', 10, 'MEDIUM', 75, NOW(), NULL, FALSE),
(9, 3, 'Phỏng vấn nâng cao về Fullstack', 'Khám phá kiến trúc Fullstack, DevOps và khả năng mở rộng.', 10, 'HARD', 90, NOW(), NULL, FALSE);

-- Chèn câu hỏi cho Phỏng vấn cơ bản về giao diện người dùng (Session 1)
INSERT INTO question (question_id, title, content, suitable_answer1, suitable_answer2, difficulty, question_status, is_deleted, source)
VALUES 
(1, 'HTML là gì?', 'Giải thích mục đích và cấu trúc cơ bản của HTML trong phát triển web.', 'HTML là viết tắt của HyperText Markup Language, dùng để tạo cấu trúc trang web bằng các thẻ như <div>, <p>, <a>, tổ chức nội dung như văn bản, hình ảnh và liên kết để trình duyệt hiển thị.', 'HTML là nền tảng của trang web, định nghĩa cấu trúc bằng các phần tử như tiêu đề, đoạn văn, danh sách, giúp trình duyệt hiển thị nội dung chính xác.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(2, 'CSS là gì?', 'Mô tả vai trò của CSS trong phát triển web.', 'CSS (Cascading Style Sheets) dùng để định dạng giao diện trang web bằng các thuộc tính như màu sắc, phông chữ và bố cục cho các phần tử HTML.', 'CSS kiểm soát cách trình bày trực quan của các phần tử HTML, làm cho trang web trở nên hấp dẫn và responsive.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(3, 'JavaScript là gì?', 'Giải thích JavaScript là gì và các ứng dụng chính của nó.', 'JavaScript là ngôn ngữ lập trình thêm tính tương tác cho trang web, như nội dung động và xử lý sự kiện.', 'JavaScript cho phép lập trình phía client, cập nhật nội dung động mà không cần tải lại trang.', 'EASY', 'APPROVED', FALSE, 'InterviewBit'),
(4, 'Phần tử HTML ngữ nghĩa là gì?', 'Mô tả các phần tử HTML ngữ nghĩa và tầm quan trọng của chúng.', 'Phần tử HTML ngữ nghĩa như <header>, <footer>, <article> thể hiện ý nghĩa nội dung, cải thiện khả năng truy cập và SEO.', 'Chúng cung cấp ngữ cảnh cho trình duyệt và công cụ tìm kiếm, làm cho cấu trúc trang web ý nghĩa hơn và dễ điều hướng.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(5, 'Mô hình hộp trong CSS là gì?', 'Giải thích mô hình hộp CSS và các thành phần của nó.', 'Mô hình hộp CSS thể hiện cấu trúc của một phần tử, bao gồm nội dung, padding, border và margin, ảnh hưởng đến kích thước và khoảng cách.', 'Nó định nghĩa cách các phần tử được hiển thị trên trang, với padding bên trong border và margin bên ngoài, ảnh hưởng đến tính toán bố cục.', 'MEDIUM', 'APPROVED', FALSE, 'Simplilearn'),
(6, 'DOM là gì?', 'Mô tả Document Object Model và vai trò của nó trong phát triển web.', 'DOM là cấu trúc dạng cây biểu diễn các phần tử HTML, cho phép JavaScript thao tác nội dung trang web một cách động.', 'Nó đóng vai trò giao diện cho JavaScript để tương tác và sửa đổi cấu trúc, kiểu dáng, nội dung của trang web.', 'MEDIUM', 'APPROVED', FALSE, 'InterviewBit'),
(7, 'Ủy quyền sự kiện là gì?', 'Giải thích khái niệm ủy quyền sự kiện trong JavaScript.', 'Ủy quyền sự kiện gắn một trình nghe sự kiện vào phần tử cha để xử lý sự kiện của các phần tử con, cải thiện hiệu suất.', 'Nó tận dụng sự kiện bubbling để bắt sự kiện từ phần tử con, giảm số lượng trình nghe sự kiện cần thiết.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(8, 'Closure trong JavaScript là gì?', 'Định nghĩa closure và đưa ra ví dụ sử dụng.', 'Closure là một hàm giữ quyền truy cập vào các biến trong phạm vi bên ngoài ngay cả khi hàm bên ngoài đã thực thi xong.', 'Ví dụ: Một hàm đếm giữ biến đếm qua nhiều lần gọi bằng cách sử dụng closure.', 'HARD', 'APPROVED', FALSE, 'InterviewBit'),
(9, 'Thuộc tính z-index hoạt động như thế nào?', 'Giải thích cách thuộc tính z-index ảnh hưởng đến vị trí phần tử trong CSS.', 'Thuộc tính z-index kiểm soát thứ tự xếp chồng của các phần tử được định vị (relative, absolute, fixed). Giá trị cao hơn xuất hiện phía trước.', 'Nó chỉ hoạt động trên các phần tử có thuộc tính position, với giá trị âm đặt phần tử phía sau.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(10, 'Promise trong JavaScript là gì?', 'Mô tả promise và vai trò của nó trong xử lý bất đồng bộ.', 'Promise là một đối tượng biểu diễn sự hoàn thành hoặc thất bại của một thao tác bất đồng bộ, với các trạng thái như pending, fulfilled hoặc rejected.', 'Promise xử lý các tác vụ bất đồng bộ như gọi API, cho phép nối chuỗi với .then() và xử lý lỗi với .catch().', 'HARD', 'APPROVED', FALSE, 'InterviewBit');

-- Liên kết câu hỏi với Phỏng vấn cơ bản về giao diện người dùng (Session 1)
INSERT INTO interview_session_question (interview_session_id, question_id) VALUES 
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10);

-- Chèn câu hỏi cho Phỏng vấn lập trình viên ReactJS (Session 2)
INSERT INTO question (question_id, title, content, suitable_answer1, suitable_answer2, difficulty, question_status, is_deleted, source)
VALUES 
(11, 'React là gì?', 'Giải thích React là gì và các tính năng chính của nó.', 'React là thư viện JavaScript để xây dựng giao diện người dùng, sử dụng components, virtual DOM và cú pháp khai báo.', 'Nó cho phép tạo các components giao diện tái sử dụng và cập nhật DOM hiệu quả thông qua virtual DOM.', 'EASY', 'APPROVED', FALSE, 'Simplilearn'),
(12, 'Component trong React là gì?', 'Mô tả components và vai trò của chúng trong ứng dụng React.', 'Components là các khối xây dựng tái sử dụng trong React, đại diện cho các phần của giao diện như nút hoặc biểu mẫu.', 'Chúng có thể là functional hoặc class-based, nhận props và quản lý trạng thái để hiển thị nội dung động.', 'EASY', 'APPROVED', FALSE, 'InterviewBit'),
(13, 'Props trong React là gì?', 'Giải thích mục đích của props trong React.', 'Props (properties) là dữ liệu chỉ đọc được truyền từ component cha sang con để cấu hình hành vi hoặc giao diện.', 'Props cho phép components tái sử dụng bằng cách truyền dữ liệu động, như tên người dùng cho component hồ sơ.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(14, 'Trạng thái trong React là gì?', 'Mô tả khái niệm trạng thái và cách nó khác với props.', 'Trạng thái là đối tượng có thể thay đổi trong component, chứa dữ liệu ảnh hưởng đến việc hiển thị, được quản lý nội bộ.', 'Thay đổi trạng thái kích hoạt hiển thị lại, trong khi props được truyền từ component cha và không thể thay đổi.', 'MEDIUM', 'APPROVED', FALSE, 'InterviewBit'),
(15, 'React Hooks là gì?', 'Giải thích React Hooks và mục đích của chúng.', 'Hooks là các hàm như useState, useEffect cho phép components chức năng quản lý trạng thái và sự kiện vòng đời.', 'Chúng đơn giản hóa việc quản lý trạng thái và side effects, thay thế các phương thức vòng đời class-based.', 'MEDIUM', 'APPROVED', FALSE, 'Simplilearn'),
(16, 'Virtual DOM là gì?', 'Mô tả virtual DOM và lợi ích của nó trong React.', 'Virtual DOM là bản sao nhẹ của DOM thực, được React sử dụng để tối ưu hóa cập nhật bằng cách giảm thao tác trực tiếp trên DOM.', 'Nó cải thiện hiệu suất bằng cách gộp các thay đổi và chỉ cập nhật các phần cần thiết của DOM thực.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(17, 'useEffect là gì?', 'Giải thích Hook useEffect và các trường hợp sử dụng phổ biến.', 'useEffect là Hook xử lý side effects, như lấy dữ liệu hoặc cập nhật DOM, sau khi component hiển thị.', 'Nó chạy sau mỗi lần hiển thị theo mặc định nhưng có thể được cấu hình để chạy có điều kiện với mảng phụ thuộc.', 'MEDIUM', 'APPROVED', FALSE, 'InterviewBit'),
(18, 'Redux là gì?', 'Mô tả Redux và vai trò của nó trong quản lý trạng thái.', 'Redux là thư viện quản lý trạng thái toàn cục trong ứng dụng JavaScript, sử dụng store, actions và reducers.', 'Nó tập trung trạng thái, làm cho trạng thái dễ dự đoán và quản lý trong các ứng dụng React lớn.', 'HARD', 'APPROVED', FALSE, 'Simplilearn'),
(19, 'Làm thế nào để tối ưu hiệu suất React?', 'Giải thích các kỹ thuật tối ưu hiệu suất trong ứng dụng React.', 'Các kỹ thuật bao gồm sử dụng memoization (React.memo), lazy loading components và tránh hiển thị lại không cần thiết với useCallback/useMemo.', 'Tối ưu hiệu suất liên quan đến giảm cập nhật trạng thái và sử dụng công cụ như React Profiler để tìm điểm nghẽn.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(20, 'Server-side rendering trong React là gì?', 'Mô tả server-side rendering và lợi ích của nó.', 'Server-side rendering (SSR) hiển thị components React trên server, gửi HTML đến client để tăng tốc độ tải ban đầu.', 'SSR cải thiện SEO và hiệu suất bằng cách cung cấp nội dung được hiển thị sẵn, đặc biệt cho ứng dụng nặng nội dung.', 'HARD', 'APPROVED', FALSE, 'InterviewBit');

-- Liên kết câu hỏi với Phỏng vấn lập trình viên ReactJS (Session 2)
INSERT INTO interview_session_question (interview_session_id, question_id) VALUES 
(2, 11), (2, 12), (2, 13), (2, 14), (2, 15), (2, 16), (2, 17), (2, 18), (2, 19), (2, 20);

-- Chèn câu hỏi cho Phỏng vấn nâng cao về giao diện người dùng (Session 3)
INSERT INTO question (question_id, title, content, suitable_answer1, suitable_answer2, difficulty, question_status, is_deleted, source)
VALUES 
(21, 'Service worker là gì?', 'Giải thích vai trò của service worker trong ứng dụng web.', 'Service worker là các script chạy nền để hỗ trợ tính năng như hỗ trợ ngoại tuyến và thông báo đẩy.', 'Chúng hoạt động như proxy giữa ứng dụng và mạng, lưu trữ tài nguyên để tăng tốc độ tải và hỗ trợ ngoại tuyến.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(22, 'Thiết kế responsive là gì?', 'Mô tả thiết kế responsive và cách triển khai.', 'Thiết kế responsive đảm bảo trang web thích ứng với các kích thước màn hình khác nhau bằng các kỹ thuật như lưới linh hoạt và media queries.', 'Nó sử dụng CSS media queries và bố cục linh hoạt để cung cấp trải nghiệm tối ưu trên các thiết bị.', 'EASY', 'APPROVED', FALSE, 'Simplilearn'),
(23, 'Khả năng truy cập web là gì?', 'Giải thích khả năng truy cập web và tầm quan trọng của nó.', 'Khả năng truy cập đảm bảo nội dung web có thể sử dụng bởi người khuyết tật, tuân theo chuẩn như WCAG.', 'Nó bao gồm sử dụng HTML ngữ nghĩa, vai trò ARIA và điều hướng bàn phím để làm cho trang web dễ tiếp cận.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(24, 'Vòng lặp sự kiện trong JavaScript là gì?', 'Mô tả cách vòng lặp sự kiện hoạt động trong JavaScript.', 'Vòng lặp sự kiện quản lý các thao tác bất đồng bộ bằng cách xử lý ngăn xếp lệnh và hàng đợi tác vụ, đảm bảo thực thi không chặn.', 'Nó liên tục kiểm tra ngăn xếp lệnh và đẩy các tác vụ từ hàng đợi khi ngăn xếp trống.', 'MEDIUM', 'APPROVED', FALSE, 'InterviewBit'),
(25, 'Tree shaking là gì?', 'Giải thích tree shaking và vai trò của nó trong tối ưu JavaScript.', 'Tree shaking loại bỏ mã không sử dụng khỏi các gói JavaScript trong quá trình build, giảm kích thước tệp.', 'Nó dựa vào module ES6 và công cụ như Webpack để loại bỏ mã chết, cải thiện hiệu suất.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(26, 'Lazy loading là gì?', 'Mô tả lazy loading và lợi ích của nó trong phát triển web.', 'Lazy loading trì hoãn việc tải các tài nguyên không quan trọng (như hình ảnh) cho đến khi cần, cải thiện thời gian tải trang.', 'Nó tăng hiệu suất bằng cách giảm thời gian tải ban đầu và tiết kiệm băng thông.', 'MEDIUM', 'APPROVED', FALSE, 'Simplilearn'),
(27, 'Rò rỉ bộ nhớ trong JavaScript là gì?', 'Giải thích rò rỉ bộ nhớ và cách ngăn chặn chúng.', 'Rò rỉ bộ nhớ xảy ra khi các đối tượng không sử dụng vẫn tồn tại trong bộ nhớ, gây ra vấn đề hiệu suất.', 'Ngăn chặn bằng cách tránh biến toàn cục, dọn dẹp trình nghe sự kiện và sử dụng công cụ như Chrome DevTools.', 'HARD', 'APPROVED', FALSE, 'InterviewBit'),
(28, 'Webpack là gì?', 'Mô tả Webpack và vai trò của nó trong phát triển giao diện người dùng.', 'Webpack là một công cụ đóng gói module, tổng hợp các module JavaScript, tài nguyên và phụ thuộc thành một gói duy nhất.', 'Nó tối ưu hóa tài nguyên, hỗ trợ code splitting và tích hợp với các công cụ như Babel cho JavaScript hiện đại.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(29, 'Đường dẫn hiển thị quan trọng là gì?', 'Giải thích Đường dẫn hiển thị quan trọng và cách tối ưu nó.', 'Đường dẫn hiển thị quan trọng là chuỗi các bước trình duyệt thực hiện để hiển thị trang, bao gồm phân tích HTML và thực thi CSS/JS.', 'Tối ưu bằng cách giảm thiểu CSS/JS, inline CSS quan trọng và trì hoãn các script không quan trọng.', 'HARD', 'APPROVED', FALSE, 'Simplilearn'),
(30, 'TypeScript là gì?', 'Mô tả TypeScript và lợi ích của nó trong phát triển giao diện người dùng.', 'TypeScript là một siêu tập hợp của JavaScript, thêm kiểu tĩnh, cải thiện độ tin cậy và khả năng bảo trì mã.', 'Nó phát hiện lỗi tại thời điểm biên dịch, nâng cao hỗ trợ IDE và cải thiện phát triển ứng dụng quy mô lớn.', 'HARD', 'APPROVED', FALSE, 'InterviewBit');

-- Liên kết câu hỏi với Phỏng vấn nâng cao về giao diện người dùng (Session 3)
INSERT INTO interview_session_question (interview_session_id, question_id) VALUES 
(3, 21), (3, 22), (3, 23), (3, 24), (3, 25), (3, 26), (3, 27), (3, 28), (3, 29), (3, 30);

-- Chèn câu hỏi cho Phỏng vấn cơ bản về backend (Session 4)
INSERT INTO question (question_id, title, content, suitable_answer1, suitable_answer2, difficulty, question_status, is_deleted, source)
VALUES 
(31, 'REST API là gì?', 'Giải thích REST API là gì và các nguyên tắc chính của nó.', 'REST API là một phong cách kiến trúc để xây dựng API sử dụng các phương thức HTTP như GET, POST, DELETE, tuân theo các nguyên tắc như không trạng thái và URL dựa trên tài nguyên.', 'Nó sử dụng các giao thức HTTP tiêu chuẩn để thực hiện các thao tác CRUD trên các tài nguyên được xác định bởi URI.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(32, 'Cơ sở dữ liệu là gì?', 'Mô tả vai trò của cơ sở dữ liệu trong phát triển web.', 'Cơ sở dữ liệu lưu trữ, truy xuất và quản lý dữ liệu, đóng vai trò kho lưu trữ phía backend cho ứng dụng.', 'Nó tương tác với logic backend để cung cấp dữ liệu cho giao diện người dùng, đảm bảo lưu trữ lâu dài.', 'EASY', 'APPROVED', FALSE, 'UseBraintrust'),
(33, 'Middleware là gì?', 'Giải thích middleware trong bối cảnh phát triển backend.', 'Middleware là phần mềm xử lý yêu cầu và phản hồi giữa server và cơ sở dữ liệu, xử lý các tác vụ như xác thực.', 'Nó nằm giữa ứng dụng và dịch vụ, cho phép ghi log, xác thực và các thao tác khác.', 'EASY', 'APPROVED', FALSE, 'UseBraintrust'),
(34, 'HTTP là gì?', 'Mô tả giao thức HTTP và các phương thức phổ biến của nó.', 'HTTP là giao thức truyền dữ liệu qua web, sử dụng các phương thức như GET, POST, PUT và DELETE.', 'Nó cho phép giao tiếp client-server, với các phương thức xác định hành động trên tài nguyên.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(35, 'Khóa chính là gì?', 'Giải thích khái niệm khóa chính trong cơ sở dữ liệu.', 'Khóa chính là định danh duy nhất cho mỗi bản ghi trong bảng cơ sở dữ liệu, đảm bảo tính toàn vẹn dữ liệu.', 'Nó ngăn chặn các bản ghi trùng lặp và được sử dụng để tham chiếu bản ghi trong các bảng liên quan.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(36, 'SQL injection là gì?', 'Mô tả SQL injection và cách ngăn chặn nó.', 'SQL injection là một cuộc tấn công chèn mã SQL độc hại vào truy vấn, gây nguy cơ cho bảo mật cơ sở dữ liệu.', 'Ngăn chặn bằng cách sử dụng câu lệnh chuẩn bị, truy vấn tham số hóa và xác thực đầu vào.', 'MEDIUM', 'APPROVED', FALSE, 'InterviewBit'),
(37, 'ORM là gì?', 'Giải thích Object-Relational Mapping và lợi ích của nó.', 'ORM ánh xạ bảng cơ sở dữ liệu sang các đối tượng trong mã, đơn giản hóa tương tác cơ sở dữ liệu bằng các công cụ như Hibernate.', 'Nó giảm mã lặp lại và làm cho các thao tác cơ sở dữ liệu trở nên trực quan hơn.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(38, 'JWT là gì?', 'Mô tả JSON Web Tokens và cách sử dụng trong xác thực.', 'JWT là cơ chế xác thực dựa trên token chứa dữ liệu người dùng mã hóa, dùng để xác minh danh tính.', 'Nó bao gồm header, payload và chữ ký, được server xác thực để bảo mật API.', 'HARD', 'APPROVED', FALSE, 'InterviewBit'),
(39, 'Cân bằng tải là gì?', 'Giải thích cân bằng tải và vai trò của nó trong hệ thống backend.', 'Cân bằng tải phân phối lưu lượng truy cập đến trên nhiều server để cải thiện hiệu suất và độ tin cậy.', 'Nó ngăn chặn quá tải server, đảm bảo tính sẵn sàng cao và khả năng mở rộng.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(40, 'Caching là gì?', 'Mô tả caching và lợi ích của nó trong phát triển backend.', 'Caching lưu trữ dữ liệu truy cập thường xuyên trong bộ nhớ để giảm tải cơ sở dữ liệu và cải thiện thời gian phản hồi.', 'Các kỹ thuật như Redis hoặc Memcached tăng hiệu suất cho các truy vấn lặp lại.', 'HARD', 'APPROVED', FALSE, 'UseBraintrust');

-- Liên kết câu hỏi với Phỏng vấn cơ bản về backend (Session 4)
INSERT INTO interview_session_question (interview_session_id, question_id) VALUES 
(4, 31), (4, 32), (4, 33), (4, 34), (4, 35), (4, 36), (4, 37), (4, 38), (4, 39), (4, 40);

-- Chèn câu hỏi cho Phỏng vấn lập trình viên Spring Boot (Session 5)
INSERT INTO question (question_id, title, content, suitable_answer1, suitable_answer2, difficulty, question_status, is_deleted, source)
VALUES 
(41, 'Spring Boot là gì?', 'Giải thích Spring Boot là gì và các tính năng chính của nó.', 'Spring Boot là framework Java đơn giản hóa phát triển Spring với tự động cấu hình và server nhúng.', 'Nó cung cấp starters, tự động cấu hình phụ thuộc và hỗ trợ phát triển ứng dụng nhanh.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(42, 'Dependency injection là gì?', 'Mô tả dependency injection trong Spring Boot.', 'Dependency injection là một mẫu thiết kế nơi các phụ thuộc được tiêm vào đối tượng, được quản lý bởi container IoC của Spring.', 'Nó tách rời các thành phần, làm cho chúng dễ kiểm tra và bảo trì.', 'EASY', 'APPROVED', FALSE, 'InterviewBit'),
(43, 'Spring Boot starters là gì?', 'Giải thích vai trò của starters trong Spring Boot.', 'Starters là các phụ thuộc được cấu hình sẵn, đơn giản hóa việc thiết lập dự án, như spring-boot-starter-web cho ứng dụng web.', 'Chúng giảm cấu hình thủ công bằng cách bao gồm các thư viện cần thiết.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(44, 'Annotation @RestController là gì?', 'Mô tả annotation @RestController trong Spring Boot.', '@RestController đánh dấu một lớp là controller RESTful, xử lý yêu cầu HTTP và trả về phản hồi JSON.', 'Nó kết hợp @Controller và @ResponseBody để phát triển API đơn giản hơn.', 'MEDIUM', 'APPROVED', FALSE, 'InterviewBit'),
(45, 'Spring Data JPA là gì?', 'Giải thích Spring Data JPA và lợi ích của nó.', 'Spring Data JPA đơn giản hóa truy cập cơ sở dữ liệu bằng cách cung cấp các giao diện repository cho thao tác CRUD.', 'Nó giảm mã lặp lại và tích hợp với Hibernate cho ORM.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(46, 'Actuator trong Spring Boot là gì?', 'Mô tả Actuator và cách sử dụng trong Spring Boot.', 'Actuator cung cấp các endpoint để giám sát và quản lý ứng dụng Spring Boot, như /health và /metrics.', 'Nó cung cấp thông tin về sức khỏe ứng dụng, số liệu và cấu hình.', 'MEDIUM', 'APPROVED', FALSE, 'InterviewBit'),
(47, 'Spring Security là gì?', 'Giải thích Spring Security và vai trò của nó trong ứng dụng backend.', 'Spring Security là framework bảo mật ứng dụng Java, xử lý xác thực và phân quyền.', 'Nó hỗ trợ các tính năng như OAuth2, JWT và kiểm soát truy cập dựa trên vai trò.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(48, 'Làm thế nào để xử lý ngoại lệ trong Spring Boot?', 'Mô tả xử lý ngoại lệ trong Spring Boot.', 'Sử dụng @ControllerAdvice và @ExceptionHandler để xử lý ngoại lệ toàn cục, trả về phản hồi lỗi tùy chỉnh.', 'Spring Boot cung cấp xử lý lỗi mặc định, có thể tùy chỉnh qua properties hoặc trình xử lý tùy chỉnh.', 'HARD', 'APPROVED', FALSE, 'InterviewBit'),
(49, 'Microservice là gì?', 'Giải thích microservices và cách triển khai trong Spring Boot.', 'Microservices là các dịch vụ nhỏ, độc lập giao tiếp qua API, được xây dựng bằng khả năng REST của Spring Boot.', 'Spring Boot hỗ trợ microservices với các tính năng như Eureka cho khám phá dịch vụ.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(50, 'Làm thế nào để tối ưu hiệu suất Spring Boot?', 'Mô tả các kỹ thuật tối ưu hiệu suất ứng dụng Spring Boot.', 'Sử dụng caching, tối ưu truy vấn cơ sở dữ liệu, kích hoạt lazy loading và phân tích hiệu suất với Actuator.', 'Các kỹ thuật bao gồm sử dụng connection pooling và giảm chi phí tạo bean.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks');

-- Liên kết câu hỏi với Phỏng vấn lập trình viên Spring Boot (Session 5)
INSERT INTO interview_session_question (interview_session_id, question_id) VALUES 
(5, 41), (5, 42), (5, 43), (5, 44), (5, 45), (5, 46), (5, 47), (5, 48), (5, 49), (5, 50);

-- Chèn câu hỏi cho Phỏng vấn nâng cao về backend (Session 6)
INSERT INTO question (question_id, title, content, suitable_answer1, suitable_answer2, difficulty, question_status, is_deleted, source)
VALUES 
(51, 'Giao dịch cơ sở dữ liệu là gì?', 'Giải thích giao dịch cơ sở dữ liệu và các thuộc tính của nó.', 'Giao dịch là một chuỗi thao tác được xử lý như một đơn vị, tuân theo thuộc tính ACID (Nguyên tử, Nhất quán, Cô lập, Bền vững).', 'Nó đảm bảo toàn vẹn dữ liệu bằng cách hoàn tác nếu bất kỳ thao tác nào thất bại, duy trì tính nhất quán của cơ sở dữ liệu.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(52, 'Chuẩn hóa cơ sở dữ liệu là gì?', 'Mô tả chuẩn hóa cơ sở dữ liệu và mục đích của nó.', 'Chuẩn hóa tổ chức các bảng cơ sở dữ liệu để loại bỏ dư thừa và đảm bảo toàn vẹn dữ liệu.', 'Nó chia bảng thành các bảng nhỏ hơn dựa trên phụ thuộc hàm.', 'EASY', 'APPROVED', FALSE, 'InterviewBit'),
(53, 'Chỉ mục trong cơ sở dữ liệu là gì?', 'Giải thích vai trò của chỉ mục trong cơ sở dữ liệu.', 'Chỉ mục là cấu trúc dữ liệu cải thiện hiệu suất truy vấn bằng cách cho phép truy xuất dữ liệu nhanh hơn.', 'Nó hoạt động như chỉ mục của sách, trỏ đến vị trí dữ liệu trong bảng.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(54, 'GraphQL là gì?', 'Mô tả GraphQL và lợi ích của nó so với REST.', 'GraphQL là ngôn ngữ truy vấn cho API, cho phép client yêu cầu dữ liệu cụ thể, giảm việc lấy thừa hoặc thiếu dữ liệu.', 'Nó cung cấp một endpoint duy nhất và truy vấn linh hoạt, không giống REST với nhiều endpoint.', 'MEDIUM', 'APPROVED', FALSE, 'UseBraintrust'),
(55, 'WebSockets là gì?', 'Giải thích WebSockets và cách sử dụng trong phát triển backend.', 'WebSockets cung cấp kênh giao tiếp hai chiều để trao đổi dữ liệu thời gian thực giữa client và server.', 'Chúng được dùng cho các ứng dụng như chat hoặc cập nhật trực tiếp, không giống mô hình yêu cầu-phản hồi của HTTP.', 'MEDIUM', 'APPROVED', FALSE, 'UseBraintrust'),
(56, 'Sharding là gì?', 'Mô tả sharding cơ sở dữ liệu và lợi ích của nó.', 'Sharding chia nhỏ cơ sở dữ liệu thành các phần phân tán để cải thiện khả năng mở rộng và hiệu suất.', 'Nó phân phối dữ liệu trên nhiều server, giảm tải cho từng cơ sở dữ liệu.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(57, 'Hệ thống phân tán là gì?', 'Giải thích hệ thống phân tán và các thách thức của nó.', 'Hệ thống phân tán là tập hợp các máy tính độc lập làm việc cùng nhau để đạt mục tiêu chung.', 'Các thách thức bao gồm độ trễ mạng, khả năng chịu lỗi và tính nhất quán dữ liệu giữa các node.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(58, 'Tính nhất quán cuối cùng là gì?', 'Mô tả tính nhất quán cuối cùng trong cơ sở dữ liệu phân tán.', 'Tính nhất quán cuối cùng đảm bảo tất cả các node trong hệ thống phân tán cuối cùng phản ánh dữ liệu giống nhau sau khi cập nhật.', 'Nó hy sinh tính nhất quán tức thời để ưu tiên tính sẵn sàng và khả năng chịu phân vùng.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(59, 'Định lý CAP là gì?', 'Giải thích định lý CAP và ý nghĩa của nó đối với cơ sở dữ liệu.', 'Định lý CAP nói rằng hệ thống phân tán chỉ có thể đảm bảo hai trong ba yếu tố: Nhất quán, Sẵn sàng, Chịu phân vùng.', 'Nó hướng dẫn thiết kế cơ sở dữ liệu, cân bằng các lựa chọn cho các trường hợp sử dụng cụ thể.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(60, 'Circuit breaking là gì?', 'Mô tả circuit breaking trong microservices.', 'Circuit breaking ngăn chặn lỗi dây chuyền bằng cách dừng yêu cầu đến dịch vụ thất bại, cải thiện độ bền hệ thống.', 'Nó giám sát sức khỏe dịch vụ và mở mạch khi lỗi vượt ngưỡng.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks');

-- Liên kết câu hỏi với Phỏng vấn nâng cao về backend (Session 6)
INSERT INTO interview_session_question (interview_session_id, question_id) VALUES 
(6, 51), (6, 52), (6, 53), (6, 54), (6, 55), (6, 56), (6, 57), (6, 58), (6, 59), (6, 60);

-- Chèn câu hỏi cho Phỏng vấn cơ bản về Fullstack (Session 7)
INSERT INTO question (question_id, title, content, suitable_answer1, suitable_answer2, difficulty, question_status, is_deleted, source)
VALUES 
(61, 'Lập trình viên Fullstack là gì?', 'Định nghĩa vai trò của lập trình viên Fullstack.', 'Lập trình viên Fullstack làm việc trên cả phát triển giao diện người dùng (UI) và backend, xử lý toàn bộ ngăn xếp ứng dụng web.', 'Họ thành thạo các công nghệ như HTML, JavaScript và các framework backend như Spring Boot.', 'EASY', 'APPROVED', FALSE, 'UseBraintrust'),
(62, 'API là gì?', 'Giải thích API là gì và vai trò của nó trong phát triển Fullstack.', 'API (Application Programming Interface) cho phép các hệ thống hoặc thành phần khác nhau giao tiếp, thường giữa giao diện người dùng và backend.', 'Nó xác định cấu trúc yêu cầu và phản hồi, như API REST hoặc GraphQL.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(63, 'Kiểm soát phiên bản là gì?', 'Mô tả kiểm soát phiên bản và tầm quan trọng của nó trong phát triển.', 'Kiểm soát phiên bản theo dõi thay đổi mã, cho phép cộng tác và hoàn tác bằng các công cụ như Git.', 'Nó đảm bảo các nhóm có thể làm việc đồng thời và duy trì lịch sử mã.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(64, 'AJAX là gì?', 'Giải thích AJAX và cách sử dụng trong ứng dụng Fullstack.', 'AJAX (Asynchronous JavaScript and XML) cho phép lấy dữ liệu bất đồng bộ mà không cần tải lại trang, cải thiện trải nghiệm người dùng.', 'Nó cho phép giao diện người dùng yêu cầu dữ liệu từ backend một cách động, như trong gợi ý tìm kiếm.', 'MEDIUM', 'APPROVED', FALSE, 'UseBraintrust'),
(65, 'CORS là gì?', 'Mô tả CORS và vai trò của nó trong phát triển web.', 'CORS (Cross-Origin Resource Sharing) là cơ chế bảo mật kiểm soát chia sẻ tài nguyên giữa các domain khác nhau.', 'Nó cho phép hoặc hạn chế truy cập API từ giao diện người dùng được lưu trữ trên một nguồn gốc khác.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(66, 'Tệp package.json là gì?', 'Giải thích mục đích của tệp package.json trong dự án Node.js.', 'package.json xác định siêu dữ liệu, phụ thuộc và script của dự án Node.js cho các tác vụ như xây dựng hoặc chạy.', 'Nó đảm bảo cài đặt phụ thuộc nhất quán trên các môi trường.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(67, 'Xác thực trong ứng dụng web là gì?', 'Mô tả xác thực và các phương pháp phổ biến được sử dụng.', 'Xác thực xác minh danh tính người dùng, sử dụng các phương pháp như JWT, OAuth2 hoặc xác thực dựa trên phiên.', 'Nó đảm bảo chỉ người dùng được ủy quyền truy cập tài nguyên được bảo vệ trong ứng dụng.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(68, 'CDN là gì?', 'Giải thích Content Delivery Networks và lợi ích của chúng.', 'CDN phân phối nội dung qua các server toàn cầu để giảm độ trễ và cải thiện tốc độ tải trang.', 'Nó lưu trữ các tài nguyên tĩnh như hình ảnh và script, nâng cao hiệu suất.', 'HARD', 'APPROVED', FALSE, 'UseBraintrust'),
(69, 'Docker là gì?', 'Mô tả Docker và cách sử dụng trong phát triển Fullstack.', 'Docker là nền tảng container hóa ứng dụng, đóng gói mã và phụ thuộc để triển khai nhất quán.', 'Nó đảm bảo ứng dụng chạy giống nhau trên môi trường phát triển và sản xuất.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(70, 'CI/CD là gì?', 'Giải thích Continuous Integration và Continuous Deployment.', 'CI/CD tự động hóa việc xây dựng, kiểm tra và triển khai mã, đảm bảo phát hành phần mềm nhanh và đáng tin cậy.', 'CI tập trung vào tích hợp mã thường xuyên, trong khi CD tự động hóa triển khai lên sản xuất.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks');

-- Liên kết câu hỏi với Phỏng vấn cơ bản về Fullstack (Session 7)
INSERT INTO interview_session_question (interview_session_id, question_id) VALUES 
(7, 61), (7, 62), (7, 63), (7, 64), (7, 65), (7, 66), (7, 67), (7, 68), (7, 69), (7, 70);

-- Chèn câu hỏi cho Phỏng vấn MERN Stack (Session 8)
INSERT INTO question (question_id, title, content, suitable_answer1, suitable_answer2, difficulty, question_status, is_deleted, source)
VALUES 
(71, 'MERN Stack là gì?', 'Mô tả MERN Stack và các thành phần của nó.', 'MERN Stack bao gồm MongoDB (cơ sở dữ liệu), Express.js (framework backend), React (thư viện giao diện), và Node.js (môi trường runtime).', 'Nó là một ngăn xếp dựa trên JavaScript để xây dựng ứng dụng web Fullstack.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(72, 'MongoDB là gì?', 'Giải thích MongoDB và lợi ích của nó.', 'MongoDB là cơ sở dữ liệu NoSQL lưu trữ dữ liệu dưới dạng tài liệu giống JSON, cung cấp tính linh hoạt và khả năng mở rộng.', 'Nó không có schema, lý tưởng cho việc xử lý dữ liệu không cấu trúc trong ứng dụng Fullstack.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(73, 'Express.js là gì?', 'Mô tả Express.js và vai trò của nó trong MERN Stack.', 'Express.js là framework Node.js để xây dựng API RESTful và xử lý yêu cầu HTTP.', 'Nó đơn giản hóa logic và định tuyến phía server trong ứng dụng MERN.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(74, 'Node.js là gì?', 'Giải thích Node.js và cách sử dụng trong phát triển backend.', 'Node.js là môi trường runtime JavaScript để thực thi mã phía server, cho phép thao tác I/O không chặn.', 'Nó cung cấp sức mạnh cho backend của ứng dụng MERN, xử lý yêu cầu và logic nghiệp vụ.', 'MEDIUM', 'APPROVED', FALSE, 'InterviewBit'),
(75, 'Làm thế nào để kết nối React với Express?', 'Mô tả cách kết nối giao diện React với backend Express.', 'Sử dụng yêu cầu HTTP (như fetch hoặc axios) để giao tiếp với API Express, xử lý dữ liệu ở định dạng JSON.', 'Cài đặt CORS trong Express để cho phép yêu cầu từ domain của ứng dụng React.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(76, 'Mongoose là gì?', 'Giải thích Mongoose và vai trò của nó trong MongoDB.', 'Mongoose là thư viện ODM (Object Data Modeling) cho MongoDB, đơn giản hóa việc mô hình hóa dữ liệu và truy vấn.', 'Nó cung cấp schema và xác thực cho MongoDB trong ứng dụng Node.js.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(77, 'Làm thế nào để quản lý trạng thái trong ứng dụng MERN?', 'Mô tả quản lý trạng thái trong ứng dụng MERN Stack.', 'Sử dụng useState hoặc Redux cho trạng thái giao diện, và quản lý trạng thái backend qua MongoDB và logic Express.', 'Tập trung trạng thái phức tạp với Redux để có luồng dữ liệu dự đoán được giữa các components.', 'HARD', 'APPROVED', FALSE, 'UseBraintrust'),
(78, 'RESTful routing là gì?', 'Giải thích RESTful routing trong Express.js.', 'RESTful routing sử dụng các phương thức HTTP và mẫu URL để thực hiện thao tác CRUD trên tài nguyên.', 'Nó tuân theo các quy ước như GET /users để liệt kê và POST /users để tạo.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(79, 'Làm thế nào để bảo mật ứng dụng MERN?', 'Mô tả các phương pháp bảo mật cho ứng dụng MERN Stack.', 'Sử dụng JWT để xác thực, làm sạch đầu vào, kích hoạt HTTPS và sử dụng Helmet cho các tiêu đề bảo mật Express.', 'Triển khai bcrypt để băm mật khẩu và xác thực tất cả đầu vào người dùng.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(80, 'SSR trong MERN là gì?', 'Giải thích server-side rendering trong MERN Stack.', 'SSR hiển thị components React trên server, gửi HTML đến client để tăng tốc độ tải ban đầu.', 'Nó cải thiện SEO và hiệu suất, sử dụng các framework như Next.js trong ứng dụng MERN.', 'HARD', 'APPROVED', FALSE, 'InterviewBit');

-- Liên kết câu hỏi với Phỏng vấn MERN Stack (Session 8)
INSERT INTO interview_session_question (interview_session_id, question_id) VALUES 
(8, 71), (8, 72), (8, 73), (8, 74), (8, 75), (8, 76), (8, 77), (8, 78), (8, 79), (8, 80);

-- Chèn câu hỏi cho Phỏng vấn nâng cao về Fullstack (Session 9)
INSERT INTO question (question_id, title, content, suitable_answer1, suitable_answer2, difficulty, question_status, is_deleted, source)
VALUES 
(81, 'Kiến trúc monolithic là gì?', 'Giải thích kiến trúc monolithic và ưu/nhược điểm của nó.', 'Kiến trúc monolithic kết hợp tất cả thành phần ứng dụng vào một đơn vị duy nhất, triển khai cùng nhau.', 'Ưu: Phát triển đơn giản; Nhược: Khó mở rộng và bảo trì so với microservices.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(82, 'Kiến trúc microservices là gì?', 'Mô tả microservices và lợi ích của chúng.', 'Microservices là các dịch vụ nhỏ, độc lập giao tiếp qua API, cho phép mở rộng và linh hoạt.', 'Chúng cho phép triển khai độc lập và dễ bảo trì nhưng tăng độ phức tạp.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(83, 'DevOps là gì?', 'Giải thích DevOps và vai trò của nó trong phát triển Fullstack.', 'DevOps là phương pháp kết hợp phát triển và vận hành để tự động hóa và tối ưu hóa việc cung cấp phần mềm.', 'Nó sử dụng pipeline CI/CD và các công cụ như Jenkins để cải thiện tốc độ triển khai.', 'EASY', 'APPROVED', FALSE, 'GeeksForGeeks'),
(84, 'Reverse proxy là gì?', 'Mô tả reverse proxy và cách sử dụng trong ứng dụng web.', 'Reverse proxy chuyển tiếp yêu cầu của client đến các server backend, cung cấp cân bằng tải và bảo mật.', 'Nó che giấu chi tiết server và có thể lưu trữ phản hồi để cải thiện hiệu suất.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(85, 'API Gateway là gì?', 'Giải thích vai trò của API Gateway trong ứng dụng Fullstack.', 'API Gateway là điểm vào cho yêu cầu client, định tuyến chúng đến các microservices phù hợp.', 'Nó xử lý xác thực, giới hạn tốc độ và cân bằng tải cho API.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(86, 'Chỉ mục cơ sở dữ liệu là gì?', 'Mô tả chỉ mục cơ sở dữ liệu và tác động của nó đến hiệu suất.', 'Chỉ mục tạo cấu trúc dữ liệu để tăng tốc thực thi truy vấn bằng cách giảm việc quét dữ liệu.', 'Nó cải thiện hiệu suất đọc nhưng có thể làm chậm thao tác ghi do cập nhật chỉ mục.', 'MEDIUM', 'APPROVED', FALSE, 'GeeksForGeeks'),
(87, 'Tính nhất quán cuối cùng trong Fullstack là gì?', 'Giải thích tính nhất quán cuối cùng trong bối cảnh ứng dụng Fullstack.', 'Tính nhất quán cuối cùng đảm bảo các hệ thống phân tán như cơ sở dữ liệu cuối cùng đồng bộ sau khi cập nhật.', 'Nó được sử dụng trong các hệ thống có khả năng mở rộng để ưu tiên tính sẵn sàng hơn tính nhất quán tức thời.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(88, 'Triển khai blue-green là gì?', 'Mô tả triển khai blue-green và lợi ích của nó.', 'Triển khai blue-green chạy hai môi trường giống nhau, chuyển lưu lượng sang môi trường mới sau khi kiểm tra.', 'Nó giảm thiểu thời gian ngừng hoạt động và cho phép hoàn tác dễ dàng nếu có vấn đề.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(89, 'Kubernetes là gì?', 'Giải thích Kubernetes và cách sử dụng trong phát triển Fullstack.', 'Kubernetes là nền tảng điều phối container, tự động hóa triển khai, mở rộng và quản lý.', 'Nó đảm bảo tính sẵn sàng cao và khả năng mở rộng cho ứng dụng Fullstack.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks'),
(90, 'GraphQL Federation là gì?', 'Mô tả GraphQL Federation và vai trò của nó trong ứng dụng Fullstack.', 'GraphQL Federation kết hợp nhiều dịch vụ GraphQL thành một endpoint duy nhất, đơn giản hóa API phức tạp.', 'Nó cho phép phát triển mô-đun, giúp các nhóm quản lý các dịch vụ độc lập.', 'HARD', 'APPROVED', FALSE, 'GeeksForGeeks');

-- Liên kết câu hỏi với Phỏng vấn nâng cao về Fullstack (Session 9)
INSERT INTO interview_session_question (interview_session_id, question_id) VALUES 
(9, 81), (9, 82), (9, 83), (9, 84), (9, 85), (9, 86), (9, 87), (9, 88), (9, 89), (9, 90);

-- Chèn tag cho câu hỏi
INSERT INTO question_tag (question_id, tag_id) VALUES 
-- Phỏng vấn cơ bản về giao diện người dùng (1-10): JavaScript, Cấu trúc dữ liệu
(1, 2), (2, 2), (3, 2), (4, 2), (5, 2), (6, 2), (7, 2), (8, 2), (9, 2), (10, 2),
(8, 6), (10, 6),
-- Phỏng vấn lập trình viên ReactJS (11-20): ReactJS, JavaScript
(11, 4), (12, 4), (13, 4), (14, 4), (15, 4), (16, 4), (17, 4), (18, 4), (19, 4), (20, 4),
(11, 2), (12, 2), (13, 2), (14, 2), (15, 2), (16, 2), (17, 2), (18, 2), (19, 2), (20, 2),
-- Phỏng vấn nâng cao về giao diện người dùng (21-30): JavaScript, Cấu trúc dữ liệu
(21, 2), (22, 2), (23, 2), (24, 2), (25, 2), (26, 2), (27, 2), (28, 2), (29, 2), (30, 2),
(25, 6), (27, 6),
-- Phỏng vấn cơ bản về backend (31-40): Java, SQL
(31, 1), (32, 5), (33, 1), (34, 1), (35, 5), (36, 5), (37, 1), (38, 1), (39, 1), (40, 5),
-- Phỏng vấn lập trình viên Spring Boot (41-50): Java, Spring Boot
(41, 3), (42, 3), (43, 3), (44, 3), (45, 3), (46, 3), (47, 3), (48, 3), (49, 3), (50, 3),
(41, 1), (42, 1), (43, 1), (44, 1), (45, 1), (46, 1), (47, 1), (48, 1), (49, 1), (50, 1),
-- Phỏng vấn nâng cao về backend (51-60): SQL, Cấu trúc dữ liệu
(51, 5), (52, 5), (53, 5), (54, 1), (55, 1), (56, 5), (57, 6), (58, 5), (59, 5), (60, 1),
-- Phỏng vấn cơ bản về Fullstack (61-70): JavaScript, Java, SQL
(61, 2), (62, 1), (63, 1), (64, 2), (65, 1), (66, 1), (67, 1), (68, 1), (69, 1), (70, 1),
(62, 5), (67, 5),
-- Phỏng vấn MERN Stack (71-80): ReactJS, JavaScript
(71, 4), (72, 5), (73, 1), (74, 1), (75, 4), (76, 5), (77, 4), (78, 1), (79, 1), (80, 4),
(71, 2), (73, 2), (74, 2), (75, 2), (77, 2), (78, 2), (79, 2), (80, 2),
-- Phỏng vấn nâng cao về Fullstack (81-90): Java, SQL, Cấu trúc dữ liệu
(81, 1), (82, 1), (83, 1), (84, 1), (85, 1), (86, 5), (87, 5), (88, 1), (89, 1), (90, 1),
(81, 6), (82, 6), (87, 6);

-- Chèn tag cho interview session
INSERT INTO interview_session_tag (interview_session_id, tag_id) VALUES 
(1, 2), (1, 6), -- Phỏng vấn cơ bản về giao diện người dùng: JavaScript, Cấu trúc dữ liệu
(2, 2), (2, 4), -- Phỏng vấn lập trình viên ReactJS: JavaScript, ReactJS
(3, 2), (3, 6), -- Phỏng vấn nâng cao về giao diện người dùng: JavaScript, Cấu trúc dữ liệu
(4, 1), (4, 5), -- Phỏng vấn cơ bản về backend: Java, SQL
(5, 1), (5, 3), -- Phỏng vấn lập trình viên Spring Boot: Java, Spring Boot
(6, 5), (6, 6), -- Phỏng vấn nâng cao về backend: SQL, Cấu trúc dữ liệu
(7, 1), (7, 2), (7, 5), -- Phỏng vấn cơ bản về Fullstack: Java, JavaScript, SQL
(8, 2), (8, 4), (8, 5), -- Phỏng vấn MERN Stack: JavaScript, ReactJS, SQL
(9, 1), (9, 5), (9, 6); -- Phỏng vấn nâng cao về Fullstack: Java, SQL, Cấu trúc dữ liệu

-- Thêm Package
INSERT INTO package(package_id, create_at, description, is_deleted, package_name, price, update_at, cv_analyze_count, interview_count, jd_analyze_count)
VALUES
    (1, NOW(), 'Gói chào mừng miễn phí cho người mới bắt đầu luyện phỏng vấn', false, 'Welcome', 0, NOW(), 10, 10, 5),
    (2, NOW(), 'Gói luyện phỏng vấn cơ bản với số lượng câu hỏi và phân tích giới hạn', false, 'Beginner', 99000, NOW(), 25, 25, 15),
    (3, NOW(), 'Gói luyện phỏng vấn chuyên sâu, không giới hạn câu hỏi và phân tích chi tiết', false, 'Professional', 299000, NOW(), 60, 60, 35);


-- Đặt lại Sequence IDs
SELECT setval('interview_session_interview_session_id_seq', (SELECT MAX(interview_session_id) FROM interview_session) + 1);
SELECT setval('topic_topic_id_seq', (SELECT MAX(topic_id) FROM topic) + 1);
SELECT setval('tag_tag_id_seq', (SELECT MAX(tag_id) FROM tag) + 1);
SELECT setval('question_question_id_seq', (SELECT MAX(question_id) FROM question) + 1);