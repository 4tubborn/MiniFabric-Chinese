import json
import os
from PIL import Image, ImageFont, ImageDraw

def bake_chinese_font(json_path, ttf_path, output_png):
    print(f"正在读取文件: {json_path} ...")

    if not os.path.exists(json_path):
        print(f"找不到 JSON 文件: {json_path}")
        return

    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            lang_data = json.load(f)
    except Exception as e:
        print(f"读取 JSON 出错: {e}")
        return

    # 1. 修正字符提取：增加标点和省略号的包含
    all_text = "".join(str(value) for value in lang_data.values())
    unique_chars = ""
    for char in all_text:
        # 汉字 | 中日韩标点 | 全角符号 | 特殊省略号(\u2026)
        if ('\u4e00' <= char <= '\u9fff' or
            '\u3000' <= char <= '\u303f' or
            '\uff00' <= char <= '\uffef' or
            char == '\u2026'):
            if char not in unique_chars:
                unique_chars += char

    num_chars = len(unique_chars)
    print(f"提取到唯一字符数量: {num_chars}")

    if num_chars == 0:
        print("未提取到任何字符。")
        return

    # 2. 严格对齐配置
    font_size = 16
    grid_size = 16
    chars_per_row = 32
    rows = (num_chars + chars_per_row - 1) // chars_per_row

    # 强制关闭抗锯齿，使用二值化模式以保证采样像素绝对锐利
    canvas = Image.new("RGBA", (512, rows * grid_size), (0, 0, 0, 0))

    # 使用你认为“正确”的字号
    font = ImageFont.truetype(ttf_path, font_size)

    for i, char in enumerate(unique_chars):
            x = (i % chars_per_row) * grid_size
            y = (i // chars_per_row) * grid_size

            # 1. 使用 getmask2 获取更详细的位图信息
            # mask 包含像素数据，offset 包含 (left, top) 的偏移量
            mask, offset = font.getmask2(char, mode='1')
            m_w, m_h = mask.size
            off_x_font, off_y_font = offset

            # 2. 关键修正：不要用 (grid_size - m_h) // 2
            # 我们应该基于字体的逻辑原点来放置。
            # 对于 16x16 的点阵字体，如果 font_size 也是 16，
            # 那么字符在格子里的位置应该是：默认起始位置 + 字体自带的偏移

            # 水平方向如果想保持绝对居中可以保留计算，或者也遵循字体原有的 off_x_font
            draw_x = x + off_x_font
            draw_y = y + off_y_font

            # 转换为可以粘贴的白色像素块
            char_img = Image.new("RGBA", (m_w, m_h), (0, 0, 0, 0))
            mask_image = Image.frombytes('L', (m_w, m_h), bytes(mask))
            char_img.paste((255, 255, 255, 255), (0, 0), mask=mask_image)

            # 直接按照字体自带的偏移量贴图
            canvas.paste(char_img, (draw_x, draw_y), char_img)

    canvas.save(output_png)
    print(f"图片已保存至: {os.path.abspath(output_png)}")

    print("\n--- 请复制以下字符串到 MixinFont.java ---")
    print(unique_chars)
    print("----------------------------------------\n")

# 执行路径（不要动你的路径）
bake_chinese_font('D:/MiniFabric-Mods/MiniFabric-Chinese/src/main/resources/assets/localization/zh-cn.json', 'unifont.otf', 'D:/MiniFabric-Mods/MiniFabric-Chinese/src/main/resources/assets/chinese/textures/characters.png')
input("执行完毕，按回车退出...")