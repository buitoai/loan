from docx import Document
import re

doc = Document('5_NguyenXuanTruong_B22DTCN156.docx')
print(f"Total tables: {len(doc.tables)}")

# Find tables related to use case
for i, table in enumerate(doc.tables):
    rows = len(table.rows)
    cols = len(table.columns)
    
    # Check first few rows for use case keywords
    header_found = False
    for r in range(min(2, rows)):
        row_texts = [table.rows[r].cells[c].text.strip() for c in range(min(3, cols))]
        row_text = ' '.join(row_texts)
        
        keywords = ['Tên use case', 'Tên Use case', 'Tên Use Case', 'Mô tả', 'Actor', 
                   'Điều kiện', 'Kết quả', 'use case', 'Use case', 'USE CASE']
        
        if any(kw.lower() in row_text.lower() for kw in keywords):
            header_found = True
            print(f"\nTable #{i}: {rows}x{cols} (Use Case table)")
            print(f"  Header row: {row_texts}")
            
            # Show next few rows
            for r2 in range(r, min(r+4, rows)):
                cells = [table.rows[r2].cells[c].text[:60].strip() for c in range(min(3, cols))]
                print(f"  Row {r2}: {cells}")
            break
    
    if header_found:
        # Try to find which section this table belongs to
        table_pos = None
        # This is simplified - actual implementation would need to find paragraph positions
        print(f"  (Table likely in Use Case section)")

# Let's also look at the structure around use case paragraphs
print("\n\nUse Case sections structure:")
found_use_case_section = False
for p in doc.paragraphs:
    text = p.text.strip()
    if not text:
        continue
        
    if '2.2. Biểu đồ Use case' in text:
        found_use_case_section = True
        print(f"\n=== {text} ===")
    elif found_use_case_section and text.startswith('2.3.'):
        break  # Next section
    elif found_use_case_section:
        # Show the structure
        if p.style.name.startswith('Heading'):
            print(f"\n{text}")
        elif any(keyword in text.lower() for keyword in ['hình', 'hình 2.', 'hình 2.']):
            print(f"  {text}")
        elif text.startswith('Bảng'):
            print(f"  {text}")
