import sys

def pdf_to_text_simple(pdf_path):
    """简单的PDF文本提取"""
    try:
        with open(pdf_path, 'rb') as f:
            content = f.read()
        
        text = ""
        i = 0
        while i < len(content):
            if content[i:i+4] == b'%PDF':
                break
            i += 1
        
        i = content.find(b'tt')
        while i != -1 and i < len(content) - 100:
            if content[i+2] == 0x03:
                length = content[i+3]
                if length > 0 and length < 256:
                    text += content[i+4:i+4+length].decode('utf-8', errors='ignore') + '\n'
            i = content.find(b'tt', i+1)
        
        return text if text else "无法提取文本内容"
    except Exception as e:
        return f"读取失败: {str(e)}"

def text_to_docx(text, output_path):
    """创建简单的Word文档（XML格式）"""
    docx_content = f"""<?xml version="1.0" encoding="UTF-8"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    <w:p>
      <w:r>
        <w:t>{text}</w:t>
      </w:r>
    </w:p>
  </w:body>
</w:document>"""
    try:
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(docx_content)
        return True
    except Exception as e:
        print(f"写入失败: {str(e)}")
        return False

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("用法: python pdf2word.py <input.pdf> <output.docx>")
        sys.exit(1)
    
    pdf_path = sys.argv[1]
    output_path = sys.argv[2]
    
    print(f"正在读取PDF: {pdf_path}")
    text = pdf_to_text_simple(pdf_path)
    print(f"提取到 {len(text)} 字符")
    
    print(f"正在创建Word文档: {output_path}")
    if text_to_docx(text, output_path):
        print("转换完成！")
    else:
        print("转换失败！")