package com.minicloud.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Factory for creating colored vector icons as JavaFX Nodes.
 * Supports both small 16px table icons and large 52px grid icons
 * matching the GNOME Files / Windows Explorer aesthetics.
 */
public class IconFactory {

    // Everforest palette
    private static final Color YELLOW       = Color.web("#dbbc7f");
    private static final Color BLUE         = Color.web("#7daea3");
    private static final Color GREEN        = Color.web("#a7c080");
    private static final Color FOLDER_GREEN = Color.web("#64b86e");
    private static final Color FOLDER_TAB   = Color.web("#82c98a");
    private static final Color AQUA         = Color.web("#83c092");
    private static final Color RED          = Color.web("#e67e80");
    private static final Color ORANGE       = Color.web("#e69875");
    private static final Color PURPLE       = Color.web("#d699b6");
    private static final Color GREY         = Color.web("#859289");
    private static final Color FG           = Color.web("#d3c6aa");
    private static final Color WHITE        = Color.web("#ffffff");

    /**
     * Small 16px folder icon.
     */
    public static Region folderIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M2 4h6l2 2h8v10H2V4z");
        path.setFill(YELLOW);
        path.setScaleX(size / 20.0);
        path.setScaleY(size / 20.0);
        return wrapShape(path, size);
    }

    /**
     * Small 16px file/document icon.
     */
    public static Region fileIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M6 2h8l4 4v14H6V2z M14 2v4h4");
        path.setFill(BLUE);
        path.setStroke(BLUE);
        path.setStrokeWidth(0.5);
        path.setScaleX(size / 22.0);
        path.setScaleY(size / 22.0);
        return wrapShape(path, size);
    }

    /**
     * 2x2 Grid icon for view toggle button.
     */
    public static Region gridIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M4 4h6v6H4V4zm10 0h6v6h-6V4zM4 14h6v6H4v-6zm10 0h6v6h-6v-6z");
        path.setFill(FG);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    /**
     * 3-bars List icon for view toggle button.
     */
    public static Region listIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M4 6h16v2H4V6zm0 5h16v2H4v-2zm0 5h16v2H4v-2z");
        path.setFill(FG);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    /**
     * Upload arrow icon.
     */
    public static Region uploadIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M12 3l-5 5h3v6h4V8h3L12 3z M5 18h14v2H5v-2z");
        path.setFill(GREEN);
        path.setScaleX(size / 22.0);
        path.setScaleY(size / 22.0);
        return wrapShape(path, size);
    }

    /**
     * Download arrow icon.
     */
    public static Region downloadIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M12 16l5-5h-3V5h-4v6H7l5 5z M5 18h14v2H5v-2z");
        path.setFill(AQUA);
        path.setScaleX(size / 22.0);
        path.setScaleY(size / 22.0);
        return wrapShape(path, size);
    }

    /**
     * Trash/delete icon.
     */
    public static Region deleteIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M6 7h12l-1 13H7L6 7z M4 5h16v2H4V5z M9 5V3h6v2");
        path.setFill(RED);
        path.setStroke(RED);
        path.setStrokeWidth(0.3);
        path.setScaleX(size / 22.0);
        path.setScaleY(size / 22.0);
        return wrapShape(path, size);
    }

    /**
     * Refresh/sync icon.
     */
    public static Region refreshIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M17.65 6.35A7.96 7.96 0 0012 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0112 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z");
        path.setFill(GREY);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    /**
     * Search/magnifier icon.
     */
    public static Region searchIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M15.5 14h-.79l-.28-.27A6.47 6.47 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z");
        path.setFill(GREY);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    /**
     * New folder icon.
     */
    public static Region newFolderIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M2 4h6l2 2h8v10H2V4z");
        path.setFill(YELLOW);

        SVGPath plus = new SVGPath();
        plus.setContent("M10 9v2H8v2h2v2h2v-2h2v-2h-2V9h-2z");
        plus.setFill(Color.web("#2b3339"));

        Group inner = new Group(path, plus);
        inner.setScaleX(size / 20.0);
        inner.setScaleY(size / 20.0);

        Group group = new Group(inner);
        StackPane stack = new StackPane(group);
        stack.setMinSize(size, size);
        stack.setPrefSize(size, size);
        stack.setMaxSize(size, size);
        stack.setAlignment(Pos.CENTER);
        return stack;
    }

    public static Region openFolderIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M2 4h6l2 2h4v3H6l-3 7V4z M6 9h14l-3 9H3l3-9z");
        path.setFill(YELLOW);
        path.setScaleX(size / 22.0);
        path.setScaleY(size / 22.0);
        return wrapShape(path, size);
    }

    public static Region renameIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1 1 0 000-1.41l-2.34-2.34a1 1 0 00-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
        path.setFill(ORANGE);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region moveIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M14 4l-5 5h3v5h4V9h3L14 4z M2 4h6l2 2h8v10H2V4z");
        path.setFill(PURPLE);
        path.setScaleX(size / 22.0);
        path.setScaleY(size / 22.0);
        return wrapShape(path, size);
    }

    public static Region copyIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z");
        path.setFill(BLUE);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region infoIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z");
        path.setFill(BLUE);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region previewIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
        path.setFill(AQUA);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region chartIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z");
        path.setFill(GREEN);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region logoutIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5-5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z");
        path.setFill(RED);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region cloudIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M19.35 10.04A7.49 7.49 0 0012 4C9.11 4 6.6 5.64 5.35 8.04A5.994 5.994 0 000 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96z");
        path.setFill(GREEN);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region homeIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z");
        path.setFill(BLUE);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region desktopIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M21 2H3c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h7l-2 3v1h8v-1l-2-3h7c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 12H3V4h18v10z");
        path.setFill(PURPLE);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region downloadsIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z");
        path.setFill(GREEN);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region documentsIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z");
        path.setFill(BLUE);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region picturesIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z");
        path.setFill(AQUA);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region musicIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z");
        path.setFill(ORANGE);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region videosIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4z");
        path.setFill(RED);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region driveIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M19 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H7v-2h10v2zm0-4H7V8h10v2z");
        path.setFill(YELLOW);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region arrowBackIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z");
        path.setFill(FG);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region arrowForwardIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z");
        path.setFill(FG);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public static Region arrowUpIcon(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M4 12l1.41 1.41L11 7.83V20h2V7.83l5.58 5.59L20 12l-8-8-8 8z");
        path.setFill(FG);
        path.setScaleX(size / 24.0);
        path.setScaleY(size / 24.0);
        return wrapShape(path, size);
    }

    public enum FileTypeCategory {
        FOLDER,
        IMAGE,
        VIDEO,
        PDF,
        DOCX,
        XLSX,
        PPTX,
        AUDIO,
        ARCHIVE,
        CODE,
        TEXT,
        GENERIC
    }

    public static FileTypeCategory classify(String fileName, String fileType, boolean isFolder) {
        if (isFolder) return FileTypeCategory.FOLDER;

        String name = fileName != null ? fileName.toLowerCase() : "";
        String type = fileType != null ? fileType.toLowerCase() : "";

        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif")
                || name.endsWith(".webp") || name.endsWith(".svg") || name.endsWith(".bmp") || name.endsWith(".ico")
                || name.endsWith(".tiff") || name.endsWith(".heic") || type.contains("image") || type.contains("picture")) {
            return FileTypeCategory.IMAGE;
        }

        if (name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".avi") || name.endsWith(".mov")
                || name.endsWith(".wmv") || name.endsWith(".webm") || name.endsWith(".flv") || name.endsWith(".m4v")
                || name.endsWith(".mpg") || name.endsWith(".mpeg") || type.contains("video") || type.contains("movie")) {
            return FileTypeCategory.VIDEO;
        }

        if (name.endsWith(".pdf") || type.contains("pdf")) {
            return FileTypeCategory.PDF;
        }

        if (name.endsWith(".docx") || name.endsWith(".doc") || name.endsWith(".odt") || name.endsWith(".rtf")
                || name.endsWith(".pages") || type.contains("word") || type.contains("doc")) {
            return FileTypeCategory.DOCX;
        }

        if (name.endsWith(".xlsx") || name.endsWith(".xls") || name.endsWith(".csv") || name.endsWith(".ods")
                || name.endsWith(".tsv") || name.endsWith(".numbers") || type.contains("excel") || type.contains("spreadsheet") || type.contains("sheet")) {
            return FileTypeCategory.XLSX;
        }

        if (name.endsWith(".pptx") || name.endsWith(".ppt") || name.endsWith(".odp") || name.endsWith(".key")
                || type.contains("powerpoint") || type.contains("presentation")) {
            return FileTypeCategory.PPTX;
        }

        if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".ogg") || name.endsWith(".flac")
                || name.endsWith(".m4a") || name.endsWith(".aac") || name.endsWith(".wma") || name.endsWith(".opus")
                || type.contains("audio") || type.contains("music") || type.contains("sound")) {
            return FileTypeCategory.AUDIO;
        }

        if (name.endsWith(".zip") || name.endsWith(".tar") || name.endsWith(".gz") || name.endsWith(".7z")
                || name.endsWith(".rar") || name.endsWith(".bz2") || name.endsWith(".xz") || name.endsWith(".tgz")
                || type.contains("zip") || type.contains("archive") || type.contains("compress")) {
            return FileTypeCategory.ARCHIVE;
        }

        if (name.endsWith(".java") || name.endsWith(".py") || name.endsWith(".js") || name.endsWith(".ts")
                || name.endsWith(".html") || name.endsWith(".css") || name.endsWith(".json") || name.endsWith(".xml")
                || name.endsWith(".c") || name.endsWith(".cpp") || name.endsWith(".h") || name.endsWith(".cs")
                || name.endsWith(".sh") || name.endsWith(".sql") || name.endsWith(".rs") || name.endsWith(".go")
                || name.endsWith(".php") || name.endsWith(".rb") || name.endsWith(".dart") || type.contains("code") || type.contains("script")) {
            return FileTypeCategory.CODE;
        }

        if (name.endsWith(".txt") || name.endsWith(".log") || name.endsWith(".md") || name.endsWith(".markdown")
                || name.endsWith(".ini") || name.endsWith(".conf") || name.endsWith(".yaml") || name.endsWith(".yml")
                || type.contains("text")) {
            return FileTypeCategory.TEXT;
        }

        return FileTypeCategory.GENERIC;
    }

    /**
     * Small 16px type icon for table and list views, customized per file type.
     */
    public static Region iconForType(String fileName, String fileType, boolean isFolder, double size) {
        FileTypeCategory cat = classify(fileName, fileType, isFolder);
        SVGPath path = new SVGPath();

        switch (cat) {
            case FOLDER:
                return folderIcon(size);

            case IMAGE:
                // Landscape / mountain & sun
                path.setContent("M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z");
                path.setFill(AQUA);
                path.setScaleX(size / 24.0);
                path.setScaleY(size / 24.0);
                return wrapShape(path, size);

            case VIDEO:
                // Play triangle inside circle
                path.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 14.5v-9l6 4.5-6 4.5z");
                path.setFill(RED);
                path.setScaleX(size / 24.0);
                path.setScaleY(size / 24.0);
                return wrapShape(path, size);

            case PDF:
                // PDF document with folded ribbon
                path.setContent("M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm-1 9h3c.55 0 1 .45 1 1s-.45 1-1 1h-3v-2zm-5 6v-6h3c1.1 0 2 .9 2 2v2c0 1.1-.9 2-2 2H8zm8 0h-2v-6h2v6z");
                path.setFill(RED);
                path.setScaleX(size / 24.0);
                path.setScaleY(size / 24.0);
                return wrapShape(path, size);

            case DOCX:
                // Word document text lines
                path.setContent("M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z");
                path.setFill(BLUE);
                path.setScaleX(size / 24.0);
                path.setScaleY(size / 24.0);
                return wrapShape(path, size);

            case XLSX:
                // Spreadsheet table grid matrix
                path.setContent("M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H5v-4h4v4zm0-6H5V7h4v4zm6 6h-4v-4h4v4zm0-6h-4V7h4v4zm4 6h-2v-4h2v4zm0-6h-2V7h2v4z");
                path.setFill(GREEN);
                path.setScaleX(size / 24.0);
                path.setScaleY(size / 24.0);
                return wrapShape(path, size);

            case PPTX:
                // Presentation chart / pie slide
                path.setContent("M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z M12 6a6 6 0 00-6 6h6V6zm1 0v6.18l4.38 4.38A6 6 0 0013 6z");
                path.setFill(ORANGE);
                path.setScaleX(size / 24.0);
                path.setScaleY(size / 24.0);
                return wrapShape(path, size);

            case AUDIO:
                // Music note
                path.setContent("M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z");
                path.setFill(YELLOW);
                path.setScaleX(size / 24.0);
                path.setScaleY(size / 24.0);
                return wrapShape(path, size);

            case ARCHIVE:
                // Zipper box / package
                path.setContent("M20 6h-4V4c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-8-2h2v2h-2V4zm1 8h-2V9h2v3zm0 3h-2v-2h2v2zm-1 5c-.55 0-1-.45-1-1v-2h2v2c0 .55-.45 1-1 1z");
                path.setFill(PURPLE);
                path.setScaleX(size / 24.0);
                path.setScaleY(size / 24.0);
                return wrapShape(path, size);

            case CODE:
                // Angle brackets
                path.setContent("M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z");
                path.setFill(AQUA);
                path.setScaleX(size / 24.0);
                path.setScaleY(size / 24.0);
                return wrapShape(path, size);

            case TEXT:
                // Clean lined document
                path.setContent("M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 14H8v-2h8v2zm0-4H8v-2h8v2zm-3-4V3.5L18.5 9H13z");
                path.setFill(FG);
                path.setScaleX(size / 24.0);
                path.setScaleY(size / 24.0);
                return wrapShape(path, size);

            case GENERIC:
            default:
                return fileIcon(size);
        }
    }

    public static Region iconForType(String type, boolean isFolder, double size) {
        return iconForType(null, type, isFolder, size);
    }

    // ======================== LARGE 52px ICONS (GRID VIEW) ========================

    /**
     * Large grid icon matching the GNOME Files / Windows Explorer style in the screenshot.
     */
    public static Region largeIcon(String name, String type, boolean isFolder, double size) {
        if (isFolder) {
            return largeFolderIcon(name, size);
        } else {
            return largeFileIcon(name, type, size);
        }
    }

    /**
     * Creates a large green folder icon with an emblem badge (Download, Music, Video, Pictures, etc.)
     */
    public static Region largeFolderIcon(String folderName, double size) {
        // Back tab of the folder
        SVGPath backTab = new SVGPath();
        backTab.setContent("M4 6h16l4 4h26c1.1 0 2 .9 2 2v4H4V6z");
        backTab.setFill(FOLDER_TAB);

        // Front main body of the folder
        SVGPath frontBody = new SVGPath();
        frontBody.setContent("M2 13h48c1.1 0 2 .9 2 2v25c0 1.1-.9 2-2 2H2c-1.1 0-2-.9-2-2V15c0-1.1.9-2 2-2z");
        frontBody.setFill(FOLDER_GREEN);

        Group folderGroup = new Group(backTab, frontBody);

        // Add emblem badge if known folder name
        String lower = folderName != null ? folderName.toLowerCase() : "";
        SVGPath emblem = null;

        if (lower.contains("download")) {
            // Down arrow in white
            emblem = new SVGPath();
            emblem.setContent("M26 21l-7 7h4v8h6v-8h4l-7-7z M18 38h16v2H18v-2z");
            emblem.setFill(WHITE);
        } else if (lower.contains("music") || lower.contains("audio") || lower.contains("sound")) {
            // Music eighth note
            emblem = new SVGPath();
            emblem.setContent("M28 20v11.5a3.5 3.5 0 11-2-3.1V23h8v-3h-6z");
            emblem.setFill(WHITE);
        } else if (lower.contains("video") || lower.contains("movie")) {
            // Play button inside circle
            emblem = new SVGPath();
            emblem.setContent("M26 18c-5.5 0-10 4.5-10 10s4.5 10 10 10 10-4.5 10-10-4.5-10-10-10zm-2 14.5v-9l7 4.5-7 4.5z");
            emblem.setFill(WHITE);
        } else if (lower.contains("picture") || lower.contains("photo") || lower.contains("image")) {
            // Picture / landscape frame
            emblem = new SVGPath();
            emblem.setContent("M18 20h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H18c-1.1 0-2-.9-2-2V22c0-1.1.9-2 2-2zm2 12h12l-3.8-5-2.7 3.5-1.9-2.5L20 32zm3-7a1.5 1.5 0 100-3 1.5 1.5 0 000 3z");
            emblem.setFill(WHITE);
        } else if (lower.contains("doc")) {
            // Document text lines
            emblem = new SVGPath();
            emblem.setContent("M20 22h12v2H20v-2zm0 4h12v2H20v-2zm0 4h8v2H20v-2z");
            emblem.setFill(WHITE);
        } else if (lower.contains("public") || lower.contains("user")) {
            // User silhouette
            emblem = new SVGPath();
            emblem.setContent("M26 21a4 4 0 100-8 4 4 0 000 8zm0 2c-4 0-8 2-8 5v2h16v-2c0-3-4-5-8-5z");
            emblem.setFill(WHITE);
        } else if (lower.contains("template") || lower.contains("code") || lower.contains("git") || lower.contains("project")) {
            // Chip / code block
            emblem = new SVGPath();
            emblem.setContent("M21 21h10v10H21V21zm2 2v6h6v-6h-6z M19 23h2v2h-2zm0 4h2v2h-2zm12-4h2v2h-2zm0 4h2v2h-2zm-8-4h2v2h-2zm4 0h2v2h-2zm-4 12h2v2h-2zm4 0h2v2h-2z");
            emblem.setFill(WHITE);
        }

        if (emblem != null) {
            folderGroup.getChildren().add(emblem);
        }

        folderGroup.setScaleX(size / 52.0);
        folderGroup.setScaleY(size / 52.0);

        Group rootGroup = new Group(folderGroup);
        StackPane wrapper = new StackPane(rootGroup);
        wrapper.setMinSize(size, size);
        wrapper.setPrefSize(size, size);
        wrapper.setMaxSize(size, size);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    /**
     * Creates a large file icon styled according to extension/type.
     * Features distinctive emblem illustrations (Image mountains, Video play, PDF ribbon,
     * DOCX text lines, XLSX spreadsheet matrix, PPTX chart, MP3 musical notes, ZIP zipper,
     * Code angle brackets, Text lines) and a colorful bottom badge with extension text.
     */
    public static Region largeFileIcon(String fileName, String fileType, double size) {
        FileTypeCategory cat = classify(fileName, fileType, false);

        Color bodyColor = Color.web("#f4f6f8");
        Color foldColor = Color.web("#c8d1d6");
        Color accentColor;
        String badgeText;
        SVGPath emblem = new SVGPath();

        switch (cat) {
            case IMAGE:
                accentColor = AQUA;
                badgeText = extractBadge(fileName, "IMG");
                emblem.setContent("M18 19h16c1.1 0 2 .9 2 2v11c0 1.1-.9 2-2 2H18c-1.1 0-2-.9-2-2V21c0-1.1.9-2 2-2zm2 11h12l-3.8-4.8-2.7 3.3-1.9-2.4L20 30zm3-6a1.5 1.5 0 100-3 1.5 1.5 0 000 3z");
                emblem.setFill(Color.web("#509c68"));
                break;

            case VIDEO:
                accentColor = RED;
                badgeText = extractBadge(fileName, "VID");
                emblem.setContent("M26 18c-5 0-9 4-9 9s4 9 9 9 9-4 9-9-4-9-9-9zm-2 13v-8l6 4-6 4z");
                emblem.setFill(RED);
                break;

            case PDF:
                accentColor = RED;
                badgeText = "PDF";
                emblem.setContent("M21 19h10c1.1 0 2 .9 2 2v11H19V21c0-1.1.9-2 2-2zm2 3h6v2h-6v-2zm0 3h6v2h-6v-2zm0 3h4v2h-4v-2z");
                emblem.setFill(RED);
                break;

            case DOCX:
                accentColor = BLUE;
                badgeText = extractBadge(fileName, "DOCX");
                emblem.setContent("M18 20h16v2H18v-2zm0 4h16v2H18v-2zm0 4h12v2H18v-2zm0 4h16v2H18v-2z");
                emblem.setFill(BLUE);
                break;

            case XLSX:
                accentColor = GREEN;
                badgeText = extractBadge(fileName, "XLSX");
                emblem.setContent("M18 19h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H18c-1.1 0-2-.9-2-2V21c0-1.1.9-2 2-2zm2 3h4v3h-4v-3zm6 0h4v3h-4v-3zm-6 4h4v3h-4v-3zm6 0h4v3h-4v-3zm-6 4h10v1H20v-1z");
                emblem.setFill(Color.web("#689d6a"));
                break;

            case PPTX:
                accentColor = ORANGE;
                badgeText = extractBadge(fileName, "PPTX");
                emblem.setContent("M18 19h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H18c-1.1 0-2-.9-2-2V21c0-1.1.9-2 2-2zm8 3a4.5 4.5 0 00-4.5 4.5h4.5v-4.5zm1 0v5l3.5 3.5A4.5 4.5 0 0027 22z");
                emblem.setFill(ORANGE);
                break;

            case AUDIO:
                accentColor = YELLOW;
                badgeText = extractBadge(fileName, "MP3");
                emblem.setContent("M27 19v10.5a3 3 0 11-1.7-2.7V21h6v-2h-4.3z");
                emblem.setFill(Color.web("#d79921"));
                break;

            case ARCHIVE:
                accentColor = PURPLE;
                badgeText = extractBadge(fileName, "ZIP");
                emblem.setContent("M24 19h4v2h-2v2h2v2h-2v2h2v2h-2v3h-2v-3h2v-2h-2v-2h2v-2h-2v-2h2v-2h-4v-2z");
                emblem.setFill(PURPLE);
                break;

            case CODE:
                accentColor = AQUA;
                badgeText = extractBadge(fileName, "CODE");
                emblem.setContent("M23 22l-4 4 4 4 1.4-1.4L21.8 26l2.6-2.6L23 22zm6 0l-1.4 1.4 2.6 2.6-2.6 2.6L29 30l4-4-4-4z");
                emblem.setFill(Color.web("#427b58"));
                break;

            case TEXT:
                accentColor = GREY;
                badgeText = extractBadge(fileName, "TXT");
                emblem.setContent("M19 21h14v2H19v-2zm0 4h14v2H19v-2zm0 4h10v2H19v-2zm0 4h12v2H19v-2z");
                emblem.setFill(Color.web("#7a8478"));
                break;

            case GENERIC:
            default:
                accentColor = BLUE;
                badgeText = extractBadge(fileName, "FILE");
                emblem.setContent("M19 21h14v2H19v-2zm0 4h14v2H19v-2zm0 4h10v2H19v-2z");
                emblem.setFill(BLUE);
                break;
        }

        // Base document shape
        SVGPath page = new SVGPath();
        page.setContent("M10 4h20l12 12v28c0 2.2-1.8 4-4 4H10c-2.2 0-4-1.8-4-4V8c0-2.2 1.8-4 4-4z");
        page.setFill(bodyColor);

        // Folded flap top right
        SVGPath fold = new SVGPath();
        fold.setContent("M30 4v10c0 1.1.9 2 2 2h10L30 4z");
        fold.setFill(foldColor);

        // Bottom colored accent bar
        SVGPath accentBar = new SVGPath();
        accentBar.setContent("M6 37h36v7c0 1.1-.9 2-2 2H8c-1.1 0-2-.9-2-2v-7z");
        accentBar.setFill(accentColor);

        Group fileGroup = new Group(page, fold, emblem, accentBar);
        fileGroup.setScaleX(size / 48.0);
        fileGroup.setScaleY(size / 48.0);

        Label badgeLabel = new Label(badgeText);
        badgeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: " + Math.max(7.5, size * 0.16) + "px; -fx-font-family: 'System';");
        badgeLabel.setMouseTransparent(true);
        badgeLabel.setAlignment(Pos.CENTER);

        StackPane wrapper = new StackPane(new Group(fileGroup), badgeLabel);
        StackPane.setAlignment(badgeLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(badgeLabel, new Insets(0, 0, size * 0.05, 0));
        wrapper.setMinSize(size, size);
        wrapper.setPrefSize(size, size);
        wrapper.setMaxSize(size, size);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    private static String extractBadge(String fileName, String defaultBadge) {
        if (fileName == null || !fileName.contains(".")) return defaultBadge;
        int dot = fileName.lastIndexOf('.');
        String ext = fileName.substring(dot + 1).trim().toUpperCase();
        if (ext.length() >= 1 && ext.length() <= 4) {
            return ext;
        }
        return defaultBadge;
    }

    private static Region coloredFileIcon(double size, Color color) {
        SVGPath path = new SVGPath();
        path.setContent("M6 2h8l4 4v14H6V2z M14 2v4h4");
        path.setFill(color);
        path.setStroke(color);
        path.setStrokeWidth(0.5);
        path.setScaleX(size / 22.0);
        path.setScaleY(size / 22.0);
        return wrapShape(path, size);
    }

    private static Region wrapShape(SVGPath shape, double size) {
        Group group = new Group(shape);
        StackPane wrapper = new StackPane(group);
        wrapper.setMinSize(size, size);
        wrapper.setPrefSize(size, size);
        wrapper.setMaxSize(size, size);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }
}
