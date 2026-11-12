"""
Enhance use case section with proper structure and content
"""
from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH

def apply_heading_format(p, level=2):
    """Apply consistent heading formatting."""
    if level == 1:
        p.style = 'Heading 1'
    elif level == 2:
        p.style = 'Heading 2'
    elif level == 3:
        p.style = 'Heading 3'

def apply_normal_format(p):
    """Apply normal text formatting."""
    p.style = 'Normal'

def apply_list_format(p):
    """Apply list bullet formatting."""
    p.style = 'List Bullet'

def enhance_usecase():
    doc = Document('Lý Thị Phương - Formatted.docx')
    
    # Find and replace use case section
    # We'll locate the heading and replace subsequent content
    for i, para in enumerate(doc.paragraphs):
        if '2.1. Biểu đồ Use Case của hệ thống' in para.text:
            print(f"Found use case heading at index {i}")
            
            # Clear all paragraphs after this until next heading at same or higher level
            to_remove = []
            for j in range(i + 1, len(doc.paragraphs)):
                if doc.paragraphs[j].style.name.startswith('Heading'):
                    if doc.paragraphs[j].text.startswith('2.2.'):
                        break
                to_remove.append(j)
            
            # Remove in reverse order
            for j in reversed(to_remove):
                p = doc.paragraphs[j]._element
                p.getparent().remove(p)
            
            # Insert new content after the heading
            insert_index = i + 1
            
            # Define new content
            content = [
                ("Biểu đồ Use case là công cụ mô hình hóa yêu cầu chức năng của hệ thống, thể hiện sự tương tác giữa các tác nhân (Actor) và hệ thống. Trong hệ thống ra quyết định cho vay tín dụng, các actor chính bao gồm:", 'Normal'),
                ("• Khách hàng (Customer): Người sử dụng hệ thống để đăng ký vay, theo dõi hồ sơ", 'List Bullet'),
                ("• Nhân viên tín dụng (Loan Officer): Nhân viên xử lý và phê duyệt hồ sơ", 'List Bullet'),
                ("• Quản trị viên (Admin): Quản lý hệ thống, cấu hình tham số, xem báo cáo", 'List Bullet'),
                ("", 'Normal'),  # Empty
                ("2.1.1. Biểu đồ Use case tổng quát", 'Heading 3'),
                ("Biểu đồ Use case tổng quát thể hiện toàn bộ chức năng của hệ thống và mối quan hệ giữa các actor. Hệ thống ra quyết định cho vay tín dụng được chia thành hai nhóm chính:", 'Normal'),
                ("• Use case phía khách hàng: Các chức năng dành cho người dùng cuối", 'List Bullet'),
                ("• Use case phía quản trị: Các chức năng dành cho nhân viên và quản trị viên", 'List Bullet'),
                ("", 'Normal'),
                ("Hình 2.1. Biểu đồ Use case tổng quát", 'Normal'),
                ("(Hình vẽ thể hiện mối quan hệ giữa Customer, Loan Officer, Admin và các use case chính)", 'Normal'),
                ("", 'Normal'),
                ("2.1.2. Use case phía khách hàng", 'Heading 3'),
            ]
            
            # Insert basic content first
            for text, style in content:
                p = doc.paragraphs[insert_index].insert_paragraph_before(text)
                if style == 'Heading 3':
                    p.style = 'Heading 3'
                elif style == 'List Bullet':
                    p.style = 'List Bullet'
                else:
                    p.style = 'Normal'
                insert_index += 1
            
            # Add customer use cases
            customer_cases = [
                ("Đăng ký tài khoản", "Khách hàng tạo tài khoản trong hệ thống để sử dụng dịch vụ"),
                ("Đăng nhập hệ thống", "Xác thực thông tin đăng nhập để truy cập hệ thống"),
                ("Đăng ký khoản vay", "Điền thông tin cá nhân, tài chính và gửi hồ sơ vay"),
                ("Xem trạng thái hồ sơ", "Theo dõi tiến trình xử lý hồ sơ đã gửi"),
                ("Cập nhật thông tin cá nhân", "Chỉnh sửa thông tin khi có thay đổi"),
                ("Xem lịch sử vay", "Xem các khoản vay đã từng đăng ký trong quá khứ"),
                ("Tính toán lãi suất ước tính", "Dự tính khoản vay và lãi suất trước khi đăng ký")
            ]
            
            for idx, (name, desc) in enumerate(customer_cases):
                # Add diagram reference
                p = doc.paragraphs[insert_index].insert_paragraph_before(f"Hình 2.{idx+2}. Use case {name}")
                p.style = 'Normal'
                insert_index += 1
                
                # Add description
                p = doc.paragraphs[insert_index].insert_paragraph_before(desc)
                p.style = 'Normal'
                insert_index += 1
                
                # Add empty line between use cases (except last)
                if idx < len(customer_cases) - 1:
                    p = doc.paragraphs[insert_index].insert_paragraph_before("")
                    p.style = 'Normal'
                    insert_index += 1
            
            # Add admin use cases section
            p = doc.paragraphs[insert_index].insert_paragraph_before("")
            p.style = 'Normal'
            insert_index += 1
            
            p = doc.paragraphs[insert_index].insert_paragraph_before("2.1.3. Use case phía quản trị")
            p.style = 'Heading 3'
            insert_index += 1
            
            admin_cases = [
                ("Quản lý hồ sơ vay", "Xem, phân loại và xử lý các hồ sơ vay đang chờ"),
                ("Tính điểm tín dụng", "Hệ thống tự động tính điểm dựa trên thuật toán xác định"),
                ("Phê duyệt khoản vay", "Đưa ra quyết định duyệt/từ chối dựa trên điểm tín dụng"),
                ("Quản lý khách hàng", "Xem thông tin, lịch sử và phân loại khách hàng"),
                ("Cấu hình tham số hệ thống", "Thiết lập lãi suất, điều kiện vay, ngưỡng điểm"),
                ("Xem báo cáo thống kê", "Báo cáo tổng hợp, phân tích dữ liệu hoạt động"),
                ("Quản lý người dùng hệ thống", "Thêm/sửa/xóa tài khoản nhân viên")
            ]
            
            start_idx = len(customer_cases) + 2  # +2 for initial diagrams
            for idx, (name, desc) in enumerate(admin_cases):
                # Add diagram reference
                p = doc.paragraphs[insert_index].insert_paragraph_before(f"Hình 2.{start_idx + idx}. Use case {name}")
                p.style = 'Normal'
                insert_index += 1
                
                # Add description
                p = doc.paragraphs[insert_index].insert_paragraph_before(desc)
                p.style = 'Normal'
                insert_index += 1
                
                # Add empty line between use cases (except last)
                if idx < len(admin_cases) - 1:
                    p = doc.paragraphs[insert_index].insert_paragraph_before("")
                    p.style = 'Normal'
                    insert_index += 1
            
            # Now handle 2.2. Đặc tả Use Case chi tiết - rename to 2.2. Đặc tả chi tiết các Use Case chính
            for para in doc.paragraphs:
                if '2.2. Đặc tả Use Case chi tiết' in para.text:
                    # Update the heading text
                    for run in para.runs:
                        run.text = run.text.replace('2.2. Đặc tả Use Case chi tiết', '2.2. Đặc tả chi tiết các Use Case chính')
                    break
            
            # Save the document
            doc.save('Lý Thị Phương - Enhanced.docx')
            print("Enhanced document saved as 'Lý Thị Phương - Enhanced.docx'")
            
            # Also create a detailed manual guide
            create_manual_guide()
            break

def create_manual_guide():
    """Create a detailed manual for use case enhancement."""
    guide = """HƯỚNG DẪN CHI TIẾT VẼ USE CASE CHO HỆ THỐNG RA QUYẾT ĐỊNH CHO VAY

PHẦN 1: BIỂU ĐỒ USE CASE TỔNG QUÁT (Hình 2.1)
──────────────────────────────────────────────

1. Vẽ 3 actor (tác nhân) bên trái:
   • Customer (Khách hàng) - hình người
   • Loan Officer (Nhân viên tín dụng) - hình người  
   • Admin (Quản trị viên) - hình người

2. Vẽ hệ thống (hình chữ nhật ở giữa) ghi "Loan Decision System"

3. Kết nối các actor với use case:

KHÁCH HÀNG (Customer):
├── Đăng ký tài khoản
├── Đăng nhập hệ thống
├── Đăng ký khoản vay
├── Xem trạng thái hồ sơ
├── Cập nhật thông tin cá nhân
├── Xem lịch sử vay
└── Tính toán lãi suất ước tính

NHÂN VIÊN TÍN DỤNG (Loan Officer):
├── Quản lý hồ sơ vay
├── Tính điểm tín dụng
├── Phê duyệt khoản vay
└── Xem báo cáo thống kê

QUẢN TRỊ VIÊN (Admin):
├── Quản lý khách hàng
├── Cấu hình tham số hệ thống
├── Xem báo cáo thống kê
└── Quản lý người dùng hệ thống

PHẦN 2: USE CASE CHI TIẾT CHO TỪNG CHỨC NĂNG
─────────────────────────────────────────────

Vẽ 14 biểu đồ use case chi tiết (Hình 2.2 đến 2.15):

KHÁCH HÀNG (7 use case):
1. Hình 2.2: Đăng ký tài khoản
   • Actor: Customer
   • Hệ thống: Xác thực email, tạo tài khoản
   • Kết quả: Tài khoản được tạo, email xác nhận

2. Hình 2.3: Đăng nhập hệ thống
   • Actor: Customer
   • Hệ thống: Kiểm tra username/password
   • Kết quả: Truy cập vào dashboard

3. Hình 2.4: Đăng ký khoản vay
   • Actor: Customer
   • Hệ thống: Validate form, lưu hồ sơ
   • Kết quả: Hồ sơ ở trạng thái "Chờ xử lý"

4. Hình 2.5: Xem trạng thái hồ sơ
   • Actor: Customer
   • Hệ thống: Truy vấn database
   • Kết quả: Hiển thị trạng thái (Chờ, Đang xử lý, Duyệt, Từ chối)

5. Hình 2.6: Cập nhật thông tin cá nhân
   • Actor: Customer
   • Hệ thống: Validate và cập nhật
   • Kết quả: Thông tin mới được lưu

6. Hình 2.7: Xem lịch sử vay
   • Actor: Customer
   • Hệ thống: Truy vấn lịch sử
   • Kết quả: Danh sách các khoản vay trước

7. Hình 2.8: Tính toán lãi suất ước tính
   • Actor: Customer
   • Hệ thống: Áp dụng công thức tính
   • Kết quả: Hiển thị số tiền phải trả

QUẢN TRỊ (7 use case):
8. Hình 2.9: Quản lý hồ sơ vay
   • Actor: Loan Officer
   • Hệ thống: Hiển thị danh sách hồ sơ
   • Kết quả: Có thể xem chi tiết từng hồ sơ

9. Hình 2.10: Tính điểm tín dụng
   • Actor: Loan Officer (hoặc tự động)
   • Hệ thống: Chạy thuật toán tính điểm
   • Kết quả: Điểm số từ 300-850

10. Hình 2.11: Phê duyệt khoản vay
    • Actor: Loan Officer
    • Hệ thống: Ghi nhận quyết định
    • Kết quả: Cập nhật trạng thái, gửi thông báo

11. Hình 2.12: Quản lý khách hàng
    • Actor: Admin
    • Hệ thống: Hiển thị danh sách khách hàng
    • Kết quả: Có thể xem thông tin chi tiết

12. Hình 2.13: Cấu hình tham số hệ thống
    • Actor: Admin
    • Hệ thống: Lưu cấu hình
    • Kết quả: Tham số mới có hiệu lực

13. Hình 2.14: Xem báo cáo thống kê
    • Actor: Admin/Loan Officer
    • Hệ thống: Tổng hợp dữ liệu
    • Kết quả: Biểu đồ, bảng số liệu

14. Hình 2.15: Quản lý người dùng hệ thống
    • Actor: Admin
    • Hệ thống: CRUD operations
    • Kết quả: Tài khoản được quản lý

PHẦN 3: CÔNG CỤ VẼ BIỂU ĐỒ
─────────────────────────
• Microsoft Visio (chuyên nghiệp nhất)
• draw.io (online, miễn phí)
• Lucidchart (online)
• PlantUML (text-based)
• PowerPoint/Word Shapes (đơn giản)

PHẦN 4: QUY TẮC TRÌNH BÀY
─────────────────────────
1. Màu sắc thống nhất:
   • Customer: Xanh dương (#1F4E79)
   • Loan Officer: Xanh lá (#2E7D32)
   • Admin: Tím (#6A1B9A)
   • Hệ thống: Xám (#616161)

2. Kích thước: 
   • Actor: 1.5cm x 2cm
   • Use Case: Hình bầu dục 3cm x 1.5cm
   • Hệ thống: Hình chữ nhật bao quanh use cases

3. Đường kết nối:
   • Liên kết thường: ───
   • Include: <<include>> 
   • Extend: <<extend>>

PHẦN 5: MẪU BẢNG ĐẶC TẢ USE CASE
─────────────────────────────────
Tên use case: Đăng ký khoản vay
Mô tả: Khách hàng điền thông tin và gửi hồ sơ vay
Actor: Customer
Điều kiện tiên quyết: 
  • Đã đăng nhập hệ thống
  • Có đủ thông tin cá nhân
  • Có thông tin tài chính cơ bản
Luồng sự kiện chính:
  1. Khách hàng chọn "Đăng ký vay"
  2. Hệ thống hiển thị form đăng ký
  3. Khách hàng điền thông tin
  4. Hệ thống validate dữ liệu
  5. Nếu hợp lệ → lưu hồ sơ
  6. Hiển thị thông báo thành công
Kết quả:
  • Hồ sơ được lưu với trạng thái "Chờ xử lý"
  • ID hồ sơ được tạo
  • Email xác nhận được gửi
"""
    
    with open('usecase_drawing_guide.txt', 'w', encoding='utf-8') as f:
        f.write(guide)
    print("Use case drawing guide saved as 'usecase_drawing_guide.txt'")

if __name__ == '__main__':
    enhance_usecase()
