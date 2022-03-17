package com.ffcc66.diary.util;

import android.support.annotation.NonNull;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64;
import com.itextpdf.text.pdf.codec.PngImage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfItextUtil {

    private Document document;

    // savePath:保存pdf的路径
    public PdfItextUtil(String savePath) throws FileNotFoundException, DocumentException {
        //创建新的PDF文档：A4大小，左右上下边框均为0
        document = new Document(PageSize.A4, 50, 50, 30, 30);
        //获取PDF书写器
        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(savePath));
        //图片精准放置
        pdfWriter.setStrictImageSequence(true);
        //打开文档
        document.open();
    }

    public void close() {
        if (document.isOpen()) {
            document.close();
        }
    }

    // 添加图片到pdf中，这张图片在pdf中居中显示
    // imgPath:图片的路径，我使用的是sdcard中图片
    // imgWidth：图片在pdf中所占的宽
    // imgHeight：图片在pdf中所占的高
    public PdfItextUtil addImageToPdfCenterH(@NonNull String imgPath) throws IOException, DocumentException {
        //获取图片
        Image img = Image.getInstance(imgPath);
        img.setAlignment(Element.ALIGN_CENTER);
        float hight = img.getHeight() * (500 / img.getWidth());
//        img.scaleToFit(500, hight);
        img.scalePercent(getPercent(img.getHeight(), img.getWidth()));
        //添加到PDF文档
        document.add(img);

        return this;
    }

    public PdfItextUtil addPngToPdf(Base64.InputStream inputStream) throws DocumentException, IOException {
        Image img = PngImage.getImage(inputStream);
        img.setAlignment(Element.ALIGN_CENTER);
        //添加到PDF文档
        document.add(img);
        return this;
    }

    // 添加文本到pdf中
    public PdfItextUtil addTextToPdf(String content) throws DocumentException {
        Paragraph elements = new Paragraph(content, setChineseFont(14));
        elements.setAlignment(Element.ALIGN_BASELINE);
        document.add(elements); // result为保存的字符串
        return this;
    }

    // 给pdf添加个标题，居中黑体
    public PdfItextUtil addTitleToPdf(String title) {
        try {
            Paragraph elements = new Paragraph(title, setChineseTiltleFont(18));
            elements.setAlignment(Element.ALIGN_LEFT);
            document.add(elements); // result为保存的字符串
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return this;
    }

    private Font setChineseFont(int size) {
        BaseFont bf;
        Font fontChinese = null;
        try {
            // STSong-Light : Adobe的字体
            // UniGB-UCS2-H : pdf 字体
            bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            fontChinese = new Font(bf, size, Font.NORMAL);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fontChinese;
    }

    private Font setChineseTiltleFont(int size) {
        BaseFont bf;
        Font fontChinese = null;
        try {
            // STSong-Light : Adobe的字体
            // UniGB-UCS2-H : pdf 字体
            bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            fontChinese = new Font(bf, size, Font.BOLD);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fontChinese;
    }

    /**
     * 图片等比例缩放
     * @param h
     * @param w
     * @return
     */
    public int getPercent(float h, float w) {
        int p = 0;
        float p2 = 0.0f;
        if (h > w) {
            p2 = 594 / h * 100;
        } else {
            p2 = 420 / w * 100;
        }
        p = Math.round(p2);
        return p;
    }
}
