"""Verify the formatted output file."""
from docx import Document

doc = Document('Lý Thị Phương - Formatted.docx')

print("=== SECTIONS ===")
for i, s in enumerate(doc.sections):
    print(f"Sec #{i}: W={round(s.page_width/360000,1)}cm H={round(s.page_height/360000,1)}cm "
          f"L={round(s.left_margin/360000,2)} R={round(s.right_margin/360000,2)} "
          f"T={round(s.top_margin/360000,2)} B={round(s.bottom_margin/360000,2)}")

print("\n=== STYLES USED ===")
d = {}
for p in doc.paragraphs:
    d[p.style.name] = d.get(p.style.name, 0) + 1
for k, v in sorted(d.items()):
    print(f"  {k}: {v}")

print("\n=== ALL HEADINGS ===")
for p in doc.paragraphs:
    if p.style.name.startswith('Heading'):
        print(f"[{p.style.name}] {p.text[:70]}")

print(f"\nTotal paragraphs: {len(doc.paragraphs)}")
print(f"Total tables: {len(doc.tables)}")

print("\n=== COVER PAGE (first 25 paragraphs) ===")
for i, p in enumerate(doc.paragraphs[:25]):
    text = p.text[:70] if p.text.strip() else '(empty)'
    align = p.alignment
    sizes = [r.font.size.pt if r.font.size else None for r in p.runs]
    bolds = [r.font.bold for r in p.runs]
    print(f"#{i} [{p.style.name}] align={align} sizes={sizes} bold={bolds} text={text}")

print("\n=== BODY TEXT SAMPLE ===")
for p in doc.paragraphs:
    if p.style.name == 'Normal' and p.text.strip() and len(p.text) > 50:
        pf = p.paragraph_format
        sizes = [r.font.size.pt if r.font.size else None for r in p.runs]
        fonts = [r.font.name for r in p.runs]
        print(f"Align={p.alignment} LineSpacing={pf.line_spacing} "
              f"FirstIndent={round(pf.first_line_indent/360000,2) if pf.first_line_indent else None} "
              f"Font={fonts} Size={sizes}")
        print(f"  Text: {p.text[:80]}...")
        break
