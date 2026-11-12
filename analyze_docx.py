"""
Phân tích cấu trúc file .docx
- Sections (page setup, margins)
- Styles
- Paragraphs (text, style, font, size, alignment, bold/italic/color)
- Tables
- Images
- Headers / Footers
"""
import sys
import os
from docx import Document
from docx.shared import Pt, Cm, Inches, Emu
from docx.enum.text import WD_ALIGN_PARAGRAPH


def emu_to_cm(emu):
    """Chuyển EMU sang cm"""
    if emu is None:
        return None
    return round(emu / 360000, 2)


def pt_to_cm(pt_val):
    if pt_val is None:
        return None
    return round(pt_val / 28.35, 2)


ALIGN_NAMES = {
    WD_ALIGN_PARAGRAPH.LEFT: "LEFT",
    WD_ALIGN_PARAGRAPH.CENTER: "CENTER",
    WD_ALIGN_PARAGRAPH.RIGHT: "RIGHT",
    WD_ALIGN_PARAGRAPH.JUSTIFY: "JUSTIFY",
    None: "None (default)",
}


def get_alignment_name(alignment):
    return ALIGN_NAMES.get(alignment, str(alignment))


def analyze_font(run):
    """Lấy thông tin font của một run"""
    info = {}
    info["text"] = run.text[:60] + "..." if len(run.text) > 60 else run.text
    info["font_name"] = run.font.name
    info["font_size"] = run.font.size.pt if run.font.size else None
    info["bold"] = run.font.bold
    info["italic"] = run.font.italic
    info["underline"] = run.font.underline
    info["strike"] = run.font.strike
    # Color
    if run.font.color and run.font.color.rgb:
        info["color"] = str(run.font.color.rgb)
    else:
        info["color"] = None
    return info


def analyze_paragraph(para, index):
    """Phân tích một paragraph"""
    print(f"\n{'='*70}")
    print(f"PARAGRAPH #{index}")
    print(f"  Style name : {para.style.name if para.style else 'None'}")
    print(f"  Alignment : {get_alignment_name(para.alignment)}")

    # Paragraph format
    pf = para.paragraph_format
    print(f"  Space before     : {pf.space_before}")
    print(f"  Space after      : {pf.space_after}")
    print(f"  Line spacing     : {pf.line_spacing}")
    print(f"  Line spacing rule: {pf.line_spacing_rule}")
    print(f"  Left indent      : {emu_to_cm(pf.left_indent) if pf.left_indent else None} cm")
    print(f"  Right indent     : {emu_to_cm(pf.right_indent) if pf.right_indent else None} cm")
    print(f"  First line indent: {emu_to_cm(pf.first_line_indent) if pf.first_line_indent else None} cm")

    # Runs (đoạn text có format riêng)
    runs = para.runs
    if not runs:
        print(f"  [Empty paragraph]")
        return

    print(f"  Runs ({len(runs)}):")
    for i, run in enumerate(runs):
        fi = analyze_font(run)
        print(f"    Run #{i}: {fi}")

    # Full text
    full_text = para.text
    if full_text.strip():
        preview = full_text[:100] + ("..." if len(full_text) > 100 else "")
        print(f"  Full text: \"{preview}\"")


def analyze_table(table, index):
    """Phân tích một bảng"""
    print(f"\n{'='*70}")
    print(f"TABLE #{index}")
    print(f"  Rows: {len(table.rows)}")
    print(f"  Cols: {len(table.columns)}")
    print(f"  Style: {table.style.name if table.style else 'None'}")

    for r_idx, row in enumerate(table.rows):
        for c_idx, cell in enumerate(row.cells):
            text = cell.text.strip()
            preview = text[:50] + ("..." if len(text) > 50 else "")
            print(f"  Cell[{r_idx}][{c_idx}]: \"{preview}\"")


def analyze_sections(doc):
    """Phân tích sections (page setup)"""
    print("\n" + "=" * 70)
    print("PAGE SETUP / SECTIONS")
    print(f"Total sections: {len(doc.sections)}")
    for i, section in enumerate(doc.sections):
        print(f"\n--- Section #{i} ---")
        print(f"  Page width  : {emu_to_cm(section.page_width)} cm")
        print(f"  Page height : {emu_to_cm(section.page_height)} cm")
        print(f"  Orientation : {section.orientation}")
        print(f"  Left margin : {emu_to_cm(section.left_margin)} cm")
        print(f"  Right margin: {emu_to_cm(section.right_margin)} cm")
        print(f"  Top margin  : {emu_to_cm(section.top_margin)} cm")
        print(f"  Bottom margin: {emu_to_cm(section.bottom_margin)} cm")
        print(f"  Header dist : {emu_to_cm(section.header_distance)} cm")
        print(f"  Footer dist : {emu_to_cm(section.footer_distance)} cm")


def analyze_styles(doc):
    """Phân tích các styles được dùng"""
    print("\n" + "=" * 70)
    print("STYLES USED IN DOCUMENT")
    used_styles = {}
    for para in doc.paragraphs:
        sname = para.style.name if para.style else "None"
        used_styles[sname] = used_styles.get(sname, 0) + 1
    for sname, count in sorted(used_styles.items()):
        print(f"  {sname}: {count} paragraph(s)")


def analyze_headers_footers(doc):
    """Phân tích header/footer"""
    print("\n" + "=" * 70)
    print("HEADERS / FOOTERS")
    for i, section in enumerate(doc.sections):
        header = section.header
        footer = section.footer
        header_text = "\n".join(p.text for p in header.paragraphs if p.text.strip())
        footer_text = "\n".join(p.text for p in footer.paragraphs if p.text.strip())
        print(f"\n--- Section #{i} ---")
        print(f"  Header is_linked: {header.is_linked_to_previous}")
        if header_text.strip():
            print(f"  Header text: {header_text[:200]}")
        else:
            print(f"  Header text: (empty)")
        print(f"  Footer is_linked: {footer.is_linked_to_previous}")
        if footer_text.strip():
            print(f"  Footer text: {footer_text[:200]}")
        else:
            print(f"  Footer text: (empty)")


def count_images(doc):
    """Đếm số hình ảnh trong document"""
    print("\n" + "=" * 70)
    print("IMAGES")
    image_count = 0
    for rel in doc.part.rels.values():
        if "image" in rel.reltype:
            image_count += 1
            print(f"  Image #{image_count}: {rel.target_ref}")
    if image_count == 0:
        print("  No images found.")


def main():
    if len(sys.argv) < 2:
        print("Cách dùng: python analyze_docx.py <file.docx>")
        sys.exit(1)

    filepath = sys.argv[1]
    if not os.path.exists(filepath):
        print(f"Không tìm thấy file: {filepath}")
        sys.exit(1)

    print(f"📄 Phân tích file: {filepath}")
    print(f"   Kích thước: {os.path.getsize(filepath) / 1024:.1f} KB")

    doc = Document(filepath)

    # 1. Page setup
    analyze_sections(doc)

    # 2. Styles
    analyze_styles(doc)

    # 3. Paragraphs
    print("\n" + "=" * 70)
    print("PARAGRAPHS (chi tiết)")
    print(f"Tổng số paragraphs: {len(doc.paragraphs)}")
    for i, para in enumerate(doc.paragraphs):
        analyze_paragraph(para, i)

    # 4. Tables
    print("\n" + "=" * 70)
    print(f"TABLES: {len(doc.tables)}")
    for i, table in enumerate(doc.tables):
        analyze_table(table, i)

    # 5. Headers / Footers
    analyze_headers_footers(doc)

    # 6. Images
    count_images(doc)

    print("\n" + "=" * 70)
    print("✅ Phân tích hoàn tất!")


if __name__ == "__main__":
    main()
