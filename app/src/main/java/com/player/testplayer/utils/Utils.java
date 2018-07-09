package com.player.testplayer.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {

    public static int px2dp(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int dp2px(Context context, int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, int spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    public static int getScreenWidth(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int isNetAvailable() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process p = runtime.exec("ping -c 3 www.baidu.com");
            int ret = p.waitFor();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void saveBitmap(Bitmap bitmap) {
        File file = new File(Environment.getExternalStorageDirectory() + "/image.jpg");
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void setFile(String imagePath,String path) {
//        int boderWidth = 10;
//        int boderHeight = 20;
//        PdfWriter writer = null;
//        try {
//            writer = new PdfWriter(new File(path));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        PdfDocument pdfDocument = new PdfDocument(writer);
//        Document document = new Document(pdfDocument);
//        ImageData imageData = null;
//        try {
//            imageData = ImageDataFactory.create(imagePath);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        Image image = new Image(imageData);
//        image.scaleToFit(PageSize.A4.getWidth() - boderWidth * 2,
//                PageSize.A4.getHeight() - boderWidth * 2);
//        document.add(image);
//        document.close();
//    }

    public static void setFileDocument(String imagePath, String path) {
        int boderWidth = 10;
        int boderHeight = 20;
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(new File(path)));
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        document.setPageSize(PageSize.A4);
        document.setMargins(0, 0, 0, 0);
        document.open();
        Image image = null;
        try {
            image = Image.getInstance(imagePath);
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        image.scaleToFit(PageSize.A4.getWidth() - boderWidth * 2,
//                PageSize.A4.getHeight() - boderWidth * 2);
        System.out.println("缩放:"
                + "  " + document.left() + "  "
                + document.leftMargin() + "  "
                + document.right() + "  "
                + document.rightMargin()
                + "  " + document.topMargin() + "  "
                + document.bottomMargin()
                + "  " + image.getWidth() + "  " + image.getHeight()
                + "  " + document.getPageSize().getWidth() + "  "
                + document.getPageSize().getHeight());
        image.setRotationDegrees(-90);
        image.setAlignment(Image.MIDDLE);
        try {
            document.add(image);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        try {
            document.add(new Paragraph(2, "text缩放",
                    FontFactory.getFont("GBK2K-H")));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        document.close();
    }

    public static void setInfo(String text) {

    }

    public static Font setChineseFont() {
        BaseFont base = null;
        Font fontChinese = null;
        try {
            base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",
                    BaseFont.EMBEDDED);
            fontChinese = new Font(base, 12, Font.NORMAL);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fontChinese;
    }

}
