"""
Rewrite the use case section to match reference document structure
"""
from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH

def rewrite_usecase_section():
    doc = Document('Lý Thị Phương - Formatted.docx')
    
    # Find sections to replace
    use_case_section_start = None
    use_case_section_end = None
    
    # Find "2.1. Biểu đồ Use Case của hệ thống"
    for i, p in enumerate(doc.paragraphs):
        if '2.1. Biểu đồ Use Case của hệ thống' in p.text:
            use_case_section_start = i
            break
    
    if use_case_section_start is None:
        print("Could not find use case section")
        return
    
    # Find where use case section ends (before "2.2." or "2.3.")
    for i in range(use_case_section_start + 1, len(doc.paragraphs)):
        if doc.paragraphs[i].text.strip().startswith(('2.2.', '2.3.')):
            use_case_section_end = i
            break
    
    if use_case_section_end is None:
        # Last section
        use_case_section_end = len(doc.paragraphs)
    
    print(f"Use case section from {use_case_section_start} to {use_case_section_end}")
    
    # Keep original content up to "2.1. Biểu đồ Use Case của hệ thống"
    # Then replace everything after that with new structure
    
    # Create new paragraphs for use case section
    new_paragraphs = []
    
    # 2.1. Biểu đồ Use Case của hệ thống (keep original heading)
    new_paragraphs.append((doc.paragraphs[use_case_section_start].text, 'Heading 2'))
    
    # New content for 2.1
    new_paragraphs.append(("Biểu đồ Use case là công cụ mô hình hóa yêu cầu chức năng của hệ thống, thể hiện sự tương tác giữa các tác nhân (Actor) và hệ thống. Trong hệ thống ra quyết định cho vay tín dụng, các actor chính bao gồm:", 'Normal'))
    new_paragraphs.append(("• Khách hàng (Customer): Người sử dụng hệ thống để đăng ký vay, theo dõi hồ sơ", 'List Bullet'))
    new_paragraphs.append(("• Nhân viên tín dụng (Loan Officer): Nhân viên xử lý và phê duyệt hồ sơ", 'List Bullet'))
    new_paragraphs.append(("• Quản trị viên (Admin): Quản lý hệ thống, cấu hình tham số, xem báo cáo", 'List Bullet'))
    
    # 2.1.1. Biểu đồ Use case tổng quát
    new_paragraphs.append(("2.1.1. Biểu đồ Use case tổng quát", 'Heading 3'))
    new_paragraphs.append(("Biểu đồ Use case tổng quát thể hiện toàn bộ chức năng của hệ thống và mối quan hệ giữa các actor. Hệ thống ra quyết định cho vay tín dụng được chia thành hai nhóm chính:", 'Normal'))
    new_paragraphs.append(("• Use case phía khách hàng: Các chức năng dành cho người dùng cuối", 'List Bullet'))
    new_paragraphs.append(("• Use case phía quản trị: Các chức năng dành cho nhân viên và quản trị viên", 'List Bullet'))
    
    # Placeholder for diagram
    new_paragraphs.append(("Hình 2.1. Biểu đồ Use case tổng quát", 'Normal'))
    new_paragraphs.append(("(Hình vẽ thể hiện mối quan hệ giữa Customer, Loan Officer, Admin và các use case chính)", 'Normal'))
    
    # 2.1.2. Use case phía khách hàng
    new_paragraphs.append(("2.1.2. Use case phía khách hàng", 'Heading 3'))
    
    # Customer use cases table
    customer_use_cases = [
        ("Đăng ký tài khoản", "Khách hàng tạo tài khoản trong hệ thống", "Khách hàng mới", "Tài khoản được tạo, thông tin được lưu", "Khách hàng có thể đăng nhập"),
        ("Đăng nhập hệ thống", "Xác thực thông tin đăng nhập", "Khách hàng đã có tài khoản", "Thông tin đúng", "Khách hàng được truy cập hệ thống"),
        ("Đăng ký khoản vay", "Khách hàng điền thông tin và gửi hồ sơ vay", "Khách hàng đã đăng nhập", "Đủ thông tin cá nhân và tài chính", "Hồ sơ được gửi, trạng thái 'Chờ xử lý'"),
        ("Xem hồ sơ vay", "Theo dõi trạng thái và chi tiết hồ sơ", "Có hồ sơ đang xử lý", "Đã đăng nhập", "Hiển thị thông tin và tiến trình"),
        ("Cập nhật thông tin", "Sửa đổi thông tin cá nhân", "Thông tin thay đổi", "Đã đăng nhập", "Thông tin được cập nhật"),
        ("Xem lịch sử vay", "Xem các khoản vay đã từng đăng ký", "Có lịch sử vay", "Đã đăng nhập", "Hiển thị danh sách lịch sử"),
        ("Tính toán lãi suất", "Ước tính khoản vay và lãi suất", "Cần tính toán trước", "Nhập số tiền, thời hạn", "Hiển thị kết quả ước tính")
    ]
    
    # Add use case descriptions
    for i, (name, desc, actor, condition, result) in enumerate(customer_use_cases):
        new_paragraphs.append((f"Hình 2.{i+2}. Use case {name}", 'Normal'))
        new_paragraphs.append(("Bảng đặc tả chi tiết:", 'Normal'))
        new_paragraphs.append((f"• Tên use case: {name}", 'List Bullet'))
        new_paragraphs.append((f"• Mô tả: {desc}", 'List Bullet'))
        new_paragraphs.append((f"• Actor: {actor}", 'List Bullet'))
        new_paragraphs.append((f"• Điều kiện tiên quyết: {condition}", 'List Bullet'))
        new_paragraphs.append((f"• Kết quả: {result}", 'List Bullet'))
        if i < len(customer_use_cases) - 1:
            new_paragraphs.append(("", 'Normal'))  # empty line between use cases
    
    # 2.1.3. Use case phía quản trị
    new_paragraphs.append(("2.1.3. Use case phía quản trị", 'Heading 3'))
    
    # Admin use cases
    admin_use_cases = [
        ("Quản lý hồ sơ vay", "Xem, xử lý, phê duyệt hồ sơ vay", "Nhân viên tín dụng, Admin", "Có hồ sơ chờ xử lý", "Hồ sơ được cập nhật trạng thái"),
        ("Tính điểm tín dụng", "Hệ thống tự động tính điểm dựa trên thông tin", "Nhân viên tín dụng", "Đủ thông tin khách hàng", "Điểm tín dụng được tính"),
        ("Phê duyệt khoản vay", "Quyết định duyệt/từ chối hồ sơ", "Nhân viên tín dụng", "Có điểm tín dụng", "Quyết định được gửi đến khách hàng"),
        ("Quản lý khách hàng", "Xem danh sách, thông tin khách hàng", "Admin", "Có quyền quản trị", "Hiển thị thông tin khách hàng"),
        ("Quản lý tham số hệ thống", "Cấu hình lãi suất, điều kiện vay", "Admin", "Có quyền quản trị", "Tham số được cập nhật"),
        ("Xem báo cáo thống kê", "Báo cáo hoạt động, tỷ lệ duyệt/từ chối", "Admin, Nhân viên", "Có dữ liệu thống kê", "Hiển thị báo cáo"),
        ("Quản lý người dùng", "Thêm/xóa/sửa tài khoản nhân viên", "Admin", "Có quyền quản trị", "Tài khoản được quản lý")
    ]
    
    start_idx = len(customer_use_cases) + 2  # +2 for the first 2 diagrams
    for i, (name, desc, actor, condition, result) in enumerate(admin_use_cases):
        new_paragraphs.append((f"Hình 2.{start_idx + i}. Use case {name}", 'Normal'))
        new_paragraphs.append(("Bảng đặc tả chi tiết:", 'Normal'))
        new_paragraphs.append((f"• Tên use case: {name}", 'List Bullet'))
        new_paragraphs.append((f"• Mô tả: {desc}", 'List Bullet'))
        new_paragraphs.append((f"• Actor: {actor}", 'List Bullet'))
        new_paragraphs.append((f"• Điều kiện tiên quyết: {condition}", 'List Bullet'))
        new_paragraphs.append((f"• Kết quả: {result}", 'List Bullet'))
        if i < len(admin_use_cases) - 1:
            new_paragraphs.append(("", 'Normal'))
    
    # Remove old content and insert new
    # We'll create a new document with the modified structure
    new_doc = Document()
    
    # Copy all paragraphs before use case section
    for i in range(use_case_section_start):
        p = new_doc.add_paragraph(doc.paragraphs[i].text, style=doc.paragraphs[i].style.name)
        # Copy formatting would be more complex...
    
    # Add new use case section
    for text, style in new_paragraphs:
        p = new_doc.add_paragraph(text, style=style)
    
    # Copy remaining paragraphs after use case section
    for i in range(use_case_section_end, len(doc.paragraphs)):
        p = new_doc.add_paragraph(doc.paragraphs[i].text, style=doc.paragraphs[i].style.name)
    
    new_doc.save('Lý Thị Phương - Updated.docx')
    print("Saved updated document to 'Lý Thị Phương - Updated.docx'")
    
    # Also create a simpler patch file that just modifies the text
    create_patch_file(use_case_section_start, use_case_section_end, new_paragraphs)

def create_patch_file(start_idx, end_idx, new_paragraphs):
    """Create instructions for manual editing."""
    with open('patch_usecase.txt', 'w', encoding='utf-8') as f:
        f.write("HƯỚNG DẪN CHỈNH SỬA USE CASE SECTION\n")
        f.write("="*50 + "\n\n")
        f.write("1. Mở file 'Lý Thị Phương - Formatted.docx' trong Word\n")
        f.write("2. Tìm mục '2.1. Biểu đồ Use Case của hệ thống'\n")
        f.write("3. Xóa từ mục đó đến trước '2.2. Đặc tả Use Case chi tiết'\n")
        f.write("4. Thay thế bằng nội dung sau:\n\n")
        
        for text, style in new_paragraphs:
            if text:
                f.write(f"{text}\n")
            else:
                f.write("\n")
        
        f.write("\n\nLƯU Ý:\n")
        f.write("- Giữ nguyên formatting (Heading 2, Heading 3, Normal, List Bullet)\n")
        f.write("- Các hình 2.1, 2.2,... là placeholder, cần vẽ biểu đồ use case tương ứng\n")
        f.write("- Điều chỉnh nội dung cho phù hợp với hệ thống thực tế\n")

if __name__ == '__main__':
    rewrite_usecase_section()
