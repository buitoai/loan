"""
Transform 'Lý Thị Phương - Copy.docx' to match the structure/format of
'5_NguyenXuanTruong_B22DTCN156.docx'

Changes:
1. Reformat cover page (add faculty, separator, proper font sizes)
2. Add LỜI CẢM ƠN section
3. Add LỜI MỞ ĐẦU section
4. Add MỤC LỤC heading
5. Renumber all headings (1.1, 1.1.1 ...)
6. Fix chapter names (CHƯƠNG 1: -> CHƯƠNG 1.)
7. Reformat all body text (TNR 13pt, JUSTIFY, 1.5 line spacing, indent 1.27cm)
8. Reformat all headings (proper font size, bold, alignment)
9. Add KẾT LUẬN section
10. Add TÀI LIỆU THAM KHẢO section
11. Fix page margins (left 3.0cm for content section)
12. Add section break after cover page
"""
from docx import Document
from docx.shared import Pt, Cm, RGBColor, Emu
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_LINE_SPACING
from docx.enum.section import WD_ORIENT
from docx.oxml.ns import qn
from copy import deepcopy
import re

SOURCE = 'Lý Thị Phương - Copy.docx'
OUTPUT = 'Lý Thị Phương - Formatted.docx'

# ── Constants from reference document ──────────────────────────────
FONT_NAME = 'Times New Roman'
BODY_SIZE = Pt(13)
H1_CENTER_SIZE = Pt(15)   # LỜI CẢM ƠN, LỜI MỞ ĐẦU, MỤC LỤC
H1_CHAPTER_SIZE = Pt(14)  # CHƯƠNG 1., CHƯƠNG 2., CHƯƠNG 3.
H1_END_SIZE = Pt(13)      # KẾT LUẬN, TÀI LIỆU THAM KHẢO
H2_SIZE = Pt(13)
H3_SIZE = Pt(13)
COLOR_HEADING = RGBColor(0x1F, 0x4E, 0x79)

BODY_INDENT = Cm(1.27)
SPACE_AFTER_BODY = Pt(6)
LINE_SPACING = 1.5

COVER_LEFT_MARGIN = Cm(2.54)
CONTENT_LEFT_MARGIN = Cm(3.0)
STD_MARGIN = Cm(2.54)


# ── Helper functions ──────────────────────────────────────────────
def set_run_font(run, size=None, bold=None, italic=None, name=FONT_NAME, color=None):
    """Apply font settings to a run."""
    run.font.name = name
    # Set East Asian font too (for Vietnamese)
    rpr = run._element.get_or_add_rPr()
    rfonts = rpr.find(qn('w:rFonts'))
    if rfonts is None:
        rfonts = run._element.makeelement(qn('w:rFonts'), {})
        rpr.insert(0, rfonts)
    rfonts.set(qn('w:ascii'), name)
    rfonts.set(qn('w:hAnsi'), name)
    rfonts.set(qn('w:cs'), name)
    if size is not None:
        run.font.size = size
    if bold is not None:
        run.font.bold = bold
    if italic is not None:
        run.font.italic = italic
    if color is not None:
        run.font.color.rgb = color


def set_paragraph_format(para, alignment=None, space_before=None, space_after=None,
                          line_spacing=None, first_line_indent=None,
                          left_indent=None, right_indent=None):
    """Apply paragraph formatting."""
    pf = para.paragraph_format
    if alignment is not None:
        para.alignment = alignment
    if space_before is not None:
        pf.space_before = space_before
    if space_after is not None:
        pf.space_after = space_after
    if line_spacing is not None:
        pf.line_spacing = line_spacing
    if first_line_indent is not None:
        pf.first_line_indent = first_line_indent
    if left_indent is not None:
        pf.left_indent = left_indent
    if right_indent is not None:
        pf.right_indent = right_indent


def remove_paragraph(para):
    """Remove a paragraph from the document."""
    p = para._element
    p.getparent().remove(p)


def add_heading_paragraph(doc, text, style_name, size, bold=True, alignment=WD_ALIGN_PARAGRAPH.LEFT,
                          color=COLOR_HEADING, space_before=None, space_after=None):
    """Add a heading paragraph at the end of the document."""
    p = doc.add_paragraph(style=style_name)
    set_paragraph_format(p, alignment=alignment, space_before=space_before, space_after=space_after)
    run = p.add_run(text)
    set_run_font(run, size=size, bold=bold, color=color)
    return p


def add_body_paragraph(doc, text, alignment=WD_ALIGN_PARAGRAPH.JUSTIFY):
    """Add a body text paragraph at the end of the document."""
    p = doc.add_paragraph()
    set_paragraph_format(p, alignment=alignment, space_after=SPACE_AFTER_BODY,
                         line_spacing=LINE_SPACING, first_line_indent=BODY_INDENT)
    run = p.add_run(text)
    set_run_font(run, size=BODY_SIZE)
    return p


def add_centered_paragraph(doc, text, size, bold=False, italic=False, space_after=None):
    """Add a centered paragraph (for cover page)."""
    p = doc.add_paragraph()
    set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_after=space_after)
    run = p.add_run(text)
    set_run_font(run, size=size, bold=bold, italic=italic)
    return p


def add_empty_paragraph(doc, alignment=WD_ALIGN_PARAGRAPH.CENTER):
    """Add an empty paragraph."""
    p = doc.add_paragraph()
    set_paragraph_format(p, alignment=alignment)
    return p


# ── Main transformation ────────────────────────────────────────────
def main():
    doc = Document(SOURCE)

    # ── Step 1: Remove old cover page (paragraphs before first CHƯƠNG heading)
    print("Step 1: Removing old cover page...")
    first_chapter_idx = None
    for i, p in enumerate(doc.paragraphs):
        if p.style.name == 'Heading 1' and p.text.strip().startswith('CHƯƠNG'):
            first_chapter_idx = i
            break

    if first_chapter_idx is None:
        print("ERROR: Could not find first CHƯƠNG heading")
        return

    # Remove all paragraphs before the first chapter
    old_cover = list(doc.paragraphs[:first_chapter_idx])
    for p in old_cover:
        remove_paragraph(p)

    # Also remove the empty Heading 1 that was before CHƯƠNG 1
    # (it's now the first paragraph if it's an empty Heading 1)
    if doc.paragraphs and doc.paragraphs[0].style.name == 'Heading 1' and not doc.paragraphs[0].text.strip():
        if not doc.paragraphs[0].text.strip().startswith('CHƯƠNG'):
            remove_paragraph(doc.paragraphs[0])

    # Re-find the first chapter paragraph (indices shifted after removal)
    first_chapter_para = None
    for p in doc.paragraphs:
        if p.style.name == 'Heading 1' and p.text.strip().startswith('CHƯƠNG'):
            first_chapter_para = p
            break

    # ── Step 2: Insert cover page before first chapter
    print("Step 2: Inserting new cover page...")
    insert_before = first_chapter_para

    def insert_para_before(ref_para, text='', style=None, size=None, bold=None,
                           italic=None, alignment=WD_ALIGN_PARAGRAPH.CENTER,
                           space_after=None, line_spacing=None,
                           first_line_indent=None, left_indent=None,
                           color=None):
        """Insert a paragraph before ref_para with formatting."""
        p = ref_para.insert_paragraph_before(text, style)
        set_paragraph_format(p, alignment=alignment, space_after=space_after,
                             line_spacing=line_spacing,
                             first_line_indent=first_line_indent,
                             left_indent=left_indent)
        if text and (size or bold or italic or color):
            run = p.runs[0] if p.runs else p.add_run(text)
            if not p.runs:
                run = p.add_run(text)
            set_run_font(run, size=size, bold=bold, italic=italic, color=color)
        elif text:
            run = p.add_run(text)
            set_run_font(run, size=size, bold=bold, italic=italic, color=color)
        return p

    # Cover page content (insert before first chapter, in reverse order so they appear correctly)
    # We insert in forward order using insert_paragraph_before on the same reference
    # Actually, insert_paragraph_before inserts right before ref_para, so each new one
    # goes between the previously inserted one and ref_para. We need to insert in order.

    # Let's collect the cover paragraphs and insert them one by one
    cover_items = [
        # (text, size, bold, italic, alignment, space_after, style)
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),  # empty
        ('HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG', None, True, None, WD_ALIGN_PARAGRAPH.CENTER, Pt(12), None),
        ('KHOA CÔNG NGHỆ THÔNG TIN', Pt(14), True, None, WD_ALIGN_PARAGRAPH.CENTER, Pt(10), None),
        ('━━━━━━━━━━━━━━━', Pt(12), None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('CHUYÊN ĐỀ CÔNG NGHỆ PHẦN MỀM', Pt(22), True, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('ĐỀ TÀI:', Pt(18), True, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('Hệ thống ra quyết định cho vay tín dụng', Pt(18), True, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('', None, None, None, WD_ALIGN_PARAGRAPH.CENTER, None, None),
        ('Hà Nội, 01/2026', Pt(13), True, True, WD_ALIGN_PARAGRAPH.CENTER, None, None),
    ]

    # Insert cover paragraphs before first chapter
    cover_start_idx = 0  # will track via paragraph list later
    for item in cover_items:
        text, size, bold, italic, alignment, space_after, style = item
        p = insert_before.insert_paragraph_before('', style)
        set_paragraph_format(p, alignment=alignment, space_after=space_after)
        if text:
            run = p.add_run(text)
            set_run_font(run, size=size, bold=bold, italic=italic)

    # Add a page break after cover page (before LỜI CẢM ƠN)
    p_break = insert_before.insert_paragraph_before()
    set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.CENTER)

    # ── Step 3: Insert LỜI CẢM ƠN
    print("Step 3: Inserting LỜI CẢM ƠN...")
    cam_on_heading = insert_before.insert_paragraph_before('LỜI CẢM ƠN', 'Heading 1')
    set_paragraph_format(cam_on_heading, alignment=WD_ALIGN_PARAGRAPH.CENTER,
                         space_before=Pt(12), space_after=Pt(18))
    run = cam_on_heading.runs[0]
    set_run_font(run, size=H1_CENTER_SIZE, bold=True, color=COLOR_HEADING)

    cam_on_texts = [
        'Trong suốt quá trình học tập và thực hiện chuyên đề công nghệ phần mềm, em đã nhận được sự hướng dẫn và giúp đỡ tận tình của các thầy cô trong Khoa Công nghệ Thông tin, Học viện Công nghệ Bưu chính Viễn thông.',
        'Đặc biệt, em xin gửi lời cảm ơn sâu sắc đến giảng viên hướng dẫn đã tận tình chỉ bảo, định hướng và cung cấp những kiến thức quý báu để em có thể hoàn thành chuyên đề này.',
        'Em cũng xin cảm ơn các bạn bè, người thân đã luôn động viên và hỗ trợ em trong suốt thời gian qua.',
        'Do thời gian và năng lực còn hạn chế, chuyên đề khó tránh khỏi thiếu sót. Em rất mong nhận được ý kiến đóng góp từ thầy cô để bài báo cáo được hoàn thiện hơn.',
    ]
    for text in cam_on_texts:
        p = insert_before.insert_paragraph_before()
        set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.JUSTIFY,
                             space_after=SPACE_AFTER_BODY, line_spacing=LINE_SPACING,
                             first_line_indent=BODY_INDENT)
        run = p.add_run(text)
        set_run_font(run, size=BODY_SIZE)

    p = insert_before.insert_paragraph_before()
    set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.CENTER,
                         space_before=Pt(12), space_after=Pt(12))
    run = p.add_run('Em xin chân thành cảm ơn!')
    set_run_font(run, size=BODY_SIZE, italic=True)

    p = insert_before.insert_paragraph_before()
    set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.CENTER)

    # ── Step 4: Insert LỜI MỞ ĐẦU
    print("Step 4: Inserting LỜI MỞ ĐẦU...")
    mo_dau_heading = insert_before.insert_paragraph_before('LỜI MỞ ĐẦU', 'Heading 1')
    set_paragraph_format(mo_dau_heading, alignment=WD_ALIGN_PARAGRAPH.CENTER,
                         space_before=Pt(12), space_after=Pt(18))
    run = mo_dau_heading.runs[0]
    set_run_font(run, size=H1_CENTER_SIZE, bold=True, color=COLOR_HEADING)

    mo_dau_texts = [
        'Trong bối cảnh nền kinh tế số đang phát triển mạnh mẽ, các tổ chức tín dụng đối mặt với bài toán quan trọng là tối ưu hóa quy trình ra quyết định cho vay. Phương pháp truyền thống dựa trên đánh giá thủ công gặp nhiều hạn chế về thời gian, tính nhất quán và chi phí vận hành.',
        'Xuất phát từ nhu cầu thực tế đó, em lựa chọn đề tài "Hệ thống ra quyết định cho vay tín dụng" nhằm xây dựng một ứng dụng web hỗ trợ tự động hóa quy trình thẩm định, tính điểm tín dụng và phê duyệt khoản vay.',
        'Chuyên đề được tổ chức thành ba chương. Chương 1 trình bày giới thiệu đề tài, mục tiêu, phạm vi và các công nghệ được sử dụng. Chương 2 phân tích và thiết kế hệ thống bao gồm biểu đồ use case, biểu đồ lớp, thiết kế cơ sở dữ liệu. Chương 3 trình bày kết quả cài đặt, giao diện và đánh giá kết quả đạt được.',
        'Mục tiêu của chuyên đề là trình bày mạch lạc bài toán, phương án giải quyết và kết quả thực tế đạt được sau quá trình thực hiện.',
    ]
    for text in mo_dau_texts:
        p = insert_before.insert_paragraph_before()
        set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.JUSTIFY,
                             space_after=SPACE_AFTER_BODY, line_spacing=LINE_SPACING,
                             first_line_indent=BODY_INDENT)
        run = p.add_run(text)
        set_run_font(run, size=BODY_SIZE)

    p = insert_before.insert_paragraph_before()
    set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.CENTER)

    # ── Step 5: Insert MỤC LỤC heading
    print("Step 5: Inserting MỤC LỤC heading...")
    muc_luc_heading = insert_before.insert_paragraph_before('MỤC LỤC', 'Heading 1')
    set_paragraph_format(muc_luc_heading, alignment=WD_ALIGN_PARAGRAPH.CENTER,
                         space_before=Pt(12), space_after=Pt(18))
    run = muc_luc_heading.runs[0]
    set_run_font(run, size=H1_CENTER_SIZE, bold=True, color=COLOR_HEADING)

    p = insert_before.insert_paragraph_before()
    set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.CENTER)
    run = p.add_run('(Cập nhật mục lục bằng cách chọn References → Table of Contents → Update Table trong Word)')
    set_run_font(run, size=Pt(11), italic=True)

    p = insert_before.insert_paragraph_before()
    set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.CENTER)

    # ── Step 6: Renumber headings
    print("Step 6: Renumbering headings...")
    chapter_num = 0
    h2_num = 0
    h3_num = 0

    for p in doc.paragraphs:
        style = p.style.name
        text = p.text.strip()

        if style == 'Heading 1' and text.startswith('CHƯƠNG'):
            chapter_num += 1
            h2_num = 0
            h3_num = 0
            # Fix: CHƯƠNG 1: → CHƯƠNG 1.
            # Extract the roman numeral and rest
            new_text = re.sub(r'(CHƯƠNG\s+\d+)\s*[:.]?\s*', r'\1. ', text)
            new_text = re.sub(r'\s+', ' ', new_text).strip()
            # Update text
            for run in p.runs:
                run.text = ''
            if p.runs:
                p.runs[0].text = new_text
            else:
                run = p.add_run(new_text)
            print(f"  H1: {new_text}")

        elif style == 'Heading 2':
            h2_num += 1
            h3_num = 0
            # Remove existing numbering (e.g., "3.1 Về chức năng" → "Về chức năng")
            clean_text = re.sub(r'^\d+\.\d+\.?\s*', '', text)
            new_text = f"{chapter_num}.{h2_num}. {clean_text}"
            for run in p.runs:
                run.text = ''
            if p.runs:
                p.runs[0].text = new_text
            else:
                run = p.add_run(new_text)
            print(f"  H2: {new_text}")

        elif style == 'Heading 3':
            h3_num += 1
            # Remove existing numbering
            clean_text = re.sub(r'^\d+\.\d+\.?\d*\.?\s*', '', text)
            new_text = f"{chapter_num}.{h2_num}.{h3_num}. {clean_text}"
            for run in p.runs:
                run.text = ''
            if p.runs:
                p.runs[0].text = new_text
            else:
                run = p.add_run(new_text)
            print(f"  H3: {new_text}")

    # ── Step 7: Reformat all paragraphs
    print("Step 7: Reformatting all paragraphs...")

    # Find the index of LỜI CẢM ƠN to skip cover page paragraphs before it
    cover_end_idx = 0
    for i, p in enumerate(doc.paragraphs):
        if p.style.name == 'Heading 1' and 'LỜI CẢM ƠN' in p.text:
            cover_end_idx = i
            break

    for idx, p in enumerate(doc.paragraphs):
        style = p.style.name
        text = p.text.strip()

        # Skip cover page paragraphs (before LỜI CẢM ƠN)
        if idx < cover_end_idx:
            continue

        if not text:
            continue

        if style == 'Heading 1':
            if text.startswith('CHƯƠNG'):
                set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.LEFT,
                                     space_before=Pt(12), space_after=Pt(18))
                for run in p.runs:
                    set_run_font(run, size=H1_CHAPTER_SIZE, bold=True, color=COLOR_HEADING)
            elif text in ('LỜI CẢM ƠN', 'LỜI MỞ ĐẦU', 'MỤC LỤC'):
                set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.CENTER,
                                     space_before=Pt(12), space_after=Pt(18))
                for run in p.runs:
                    set_run_font(run, size=H1_CENTER_SIZE, bold=True, color=COLOR_HEADING)
            else:
                # KẾT LUẬN, TÀI LIỆU THAM KHẢO
                set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.LEFT,
                                     space_before=Pt(12), space_after=Pt(18))
                for run in p.runs:
                    set_run_font(run, size=H1_END_SIZE, bold=True, color=COLOR_HEADING)

        elif style == 'Heading 2':
            set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.LEFT,
                                 space_before=Pt(6), space_after=Pt(6),
                                 line_spacing=LINE_SPACING)
            for run in p.runs:
                set_run_font(run, size=H2_SIZE, bold=True, color=COLOR_HEADING)

        elif style == 'Heading 3':
            set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.LEFT,
                                 space_before=Pt(6), space_after=Pt(6),
                                 line_spacing=LINE_SPACING)
            for run in p.runs:
                set_run_font(run, size=H3_SIZE, bold=True, color=COLOR_HEADING)

        elif style in ('Normal', 'List Paragraph'):
            # Skip paragraphs that are CENTER aligned (cover note, "Em xin chân thành cảm ơn!", etc.)
            if p.alignment == WD_ALIGN_PARAGRAPH.CENTER:
                continue
            # Skip cover page paragraphs (centered, large fonts)
            has_large_font = False
            for run in p.runs:
                if run.font.size and run.font.size > Pt(16):
                    has_large_font = True
                    break
            if has_large_font:
                continue

            # Body text formatting
            set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.JUSTIFY,
                                 space_after=SPACE_AFTER_BODY,
                                 line_spacing=LINE_SPACING,
                                 first_line_indent=BODY_INDENT)
            for run in p.runs:
                set_run_font(run, size=BODY_SIZE)

        elif style in ('List Bullet', 'List Number'):
            set_paragraph_format(p, space_after=SPACE_AFTER_BODY,
                                 line_spacing=LINE_SPACING)
            for run in p.runs:
                set_run_font(run, size=BODY_SIZE)

    # ── Step 8: Fix page margins
    print("Step 8: Fixing page margins...")
    if len(doc.sections) >= 1:
        section = doc.sections[0]
        section.page_width = Cm(21.0)
        section.page_height = Cm(29.7)
        section.orientation = WD_ORIENT.PORTRAIT
        section.left_margin = CONTENT_LEFT_MARGIN
        section.right_margin = STD_MARGIN
        section.top_margin = STD_MARGIN
        section.bottom_margin = STD_MARGIN
        section.header_distance = Cm(1.25)
        section.footer_distance = Cm(1.25)

    # ── Step 9: Append KẾT LUẬN
    print("Step 9: Adding KẾT LUẬN...")
    add_empty_paragraph(doc)
    ket_luan_heading = add_heading_paragraph(
        doc, 'KẾT LUẬN', 'Heading 1', H1_END_SIZE, bold=True,
        alignment=WD_ALIGN_PARAGRAPH.LEFT, space_before=Pt(12), space_after=Pt(18))

    ket_luan_texts = [
        'Qua quá trình thực hiện chuyên đề "Hệ thống ra quyết định cho vay tín dụng", em đã xây dựng được một ứng dụng web hoàn chỉnh với các chức năng cơ bản đáp ứng yêu cầu đặt ra.',
        'Hệ thống cho phép quản lý quy trình cho vay từ đăng ký hồ sơ, tính điểm tín dụng, đến phê duyệt khoản vay. Các thuật toán đánh giá tín dụng được triển khai giúp tự động hóa một phần quy trình thẩm định, giảm thời gian xử lý và tăng tính nhất quán.',
        'Tuy nhiên, hệ thống vẫn còn một số hạn chế như chưa tích hợp các mô hình học máy nâng cao, giao diện cần tối ưu thêm cho thiết bị di động, và chưa có tính năng thông báo realtime.',
        'Trong tương lai, hệ thống có thể phát triển thêm các tính năng như tích hợp mô hình AI để dự đoán rủi ro tín dụng, hỗ trợ đa ngôn ngữ, và triển khai trên nền tảng cloud để đảm bảo khả năng mở rộng.',
    ]
    for text in ket_luan_texts:
        add_body_paragraph(doc, text)

    # ── Step 10: Append TÀI LIỆU THAM KHẢO
    print("Step 10: Adding TÀI LIỆU THAM KHẢO...")
    add_empty_paragraph(doc)
    tltk_heading = add_heading_paragraph(
        doc, 'TÀI LIỆU THAM KHẢO', 'Heading 1', H1_END_SIZE, bold=True,
        alignment=WD_ALIGN_PARAGRAPH.LEFT, space_before=Pt(12), space_after=Pt(18))

    references = [
        '1. Hoàng Đức Hải (2021), "Spring Boot in Action", NXB Thống Kê, Hà Nội.',
        '2. Craig Walls (2022), "Spring in Action, Sixth Edition", Manning Publications.',
        '3. Robert C. Martin (2017), "Clean Architecture: A Craftsman\'s Guide to Software Structure and Design", Prentice Hall.',
        '4. Martin Fowler (2002), "Patterns of Enterprise Application Architecture", Addison-Wesley Professional.',
        '5. Học viện Công nghệ Bưu chính Viễn thông (2024), "Giáo trình Công nghệ phần mềm", Hà Nội.',
        '6. Tài liệu chính thức Spring Framework: https://docs.spring.io/spring-framework/',
        '7. Tài liệu chính thức Spring Boot: https://docs.spring.io/spring-boot/',
        '8. Tài liệu chính thức Thymeleaf: https://www.thymeleaf.org/documentation.html',
    ]
    for ref in references:
        p = doc.add_paragraph()
        set_paragraph_format(p, alignment=WD_ALIGN_PARAGRAPH.JUSTIFY,
                             space_after=SPACE_AFTER_BODY, line_spacing=LINE_SPACING,
                             left_indent=Cm(1.27))
        p.paragraph_format.first_line_indent = Cm(-0.5)  # hanging indent
        run = p.add_run(ref)
        set_run_font(run, size=BODY_SIZE)

    # ── Step 11: Fix table fonts
    print("Step 11: Fixing table fonts...")
    for table in doc.tables:
        for row in table.rows:
            for cell in row.cells:
                for para in cell.paragraphs:
                    for run in para.runs:
                        set_run_font(run, size=BODY_SIZE)
                    set_paragraph_format(para, line_spacing=LINE_SPACING)

    # ── Save
    print(f"\nSaving to {OUTPUT}...")
    doc.save(OUTPUT)
    print("✅ Done! File saved as:", OUTPUT)


if __name__ == '__main__':
    main()
