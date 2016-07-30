package com.lee.sdk.downloads.ext;

import java.util.HashMap;

import android.text.TextUtils;

import com.lee.sdk.Configuration;

/**
 * File Classify helper class.
 */
public final class FileClassifyHelper {
    /** debug */
    private static final boolean DEBUG = true & Configuration.DEBUG;
    /** TAG */
    private static final String TAG = "FileClassifyHelper";
    /** 任何类型  */
    public static final int DOWNLOADED_TYPE_ANY = -2;
    /** 空类型*/
    public static final int DOWNLOADED_TYPE_NONE = -1;
    /** 视频   */
    public static final int DOWNLOADED_TYPE_VIDEO = 0;
    /** 音乐   */
    public static final int DOWNLOADED_TYPE_MUSIC = 1;
    /** 图片   */
    public static final int DOWNLOADED_TYPE_IMAGE = 2;
    /** 应用  */
    public static final int DOWNLOADED_TYPE_APP = 3;
    /** 文档  */
    public static final int DOWNLOADED_TYPE_DOC = 4;
    /** 其他  */
    public static final int DOWNLOADED_TYPE_OTHERS = 5;
    /** 小说 */
    public static final int DOWNLOADED_TYPE_NOVEL = 6;
    /** 目录*/
    public static final int DOWNLOADED_TYPE_DIR = 7;
    /** 压缩文件*/
    public static final int DOWNLOADED_TYPE_ZIP = 8;
    
    /** APK文件的mimetype */
    public static final String MIME_TYPE_APK = "application/vnd.android.package-archive";
    
    /**
     * 构造函数
     */
    private FileClassifyHelper() {
        
    }
    /**
     * 后缀-类别  Map
     */
    private static HashMap<String, Integer> sExtTypeMap
            = new HashMap<String, Integer>();
    /**
     * MimeType-类别  Map
     */
    private static HashMap<String, Integer> sMimeTypeMap
            = new HashMap<String, Integer>();
    /**
     * extension to mimetype map
     */
    private static HashMap<String, String> sExtensionToMimeTypeMap = new HashMap<String, String>();
    /**
     * mimetype to extension map
     */
    private static HashMap<String, String> sMimeTypeToExtensionMap = new HashMap<String, String>();
    
    static {
        add("application/andrew-inset", "ez", DOWNLOADED_TYPE_OTHERS);
        add("application/dsptype", "tsp", DOWNLOADED_TYPE_OTHERS);
        add("application/futuresplash", "spl", DOWNLOADED_TYPE_OTHERS);
        add("application/hta", "hta", DOWNLOADED_TYPE_OTHERS);
        add("application/mac-binhex40", "hqx", DOWNLOADED_TYPE_OTHERS);
        add("application/mac-compactpro", "cpt", DOWNLOADED_TYPE_OTHERS);
        add("application/mathematica", "nb", DOWNLOADED_TYPE_OTHERS);
        add("application/msaccess", "mdb", DOWNLOADED_TYPE_OTHERS);
        add("application/oda", "oda", DOWNLOADED_TYPE_OTHERS);
        add("application/ogg", "ogg", DOWNLOADED_TYPE_MUSIC);
        add("application/pdf", "pdf", DOWNLOADED_TYPE_DOC);
        add("application/pgp-keys", "key", DOWNLOADED_TYPE_OTHERS);
        add("application/pgp-signature", "pgp", DOWNLOADED_TYPE_OTHERS);
        add("application/pics-rules", "prf", DOWNLOADED_TYPE_OTHERS);
        add("application/rar", "rar", DOWNLOADED_TYPE_ZIP);
        add("application/rdf+xml", "rdf", DOWNLOADED_TYPE_OTHERS);
        add("application/rss+xml", "rss", DOWNLOADED_TYPE_OTHERS);
        add("application/zip", "zip", DOWNLOADED_TYPE_ZIP);
        add(MIME_TYPE_APK, "apk", DOWNLOADED_TYPE_APP);
        add("application/vnd.cinderella", "cdy", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.ms-pki.stl", "stl", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.oasis.opendocument.database", "odb", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.oasis.opendocument.formula", "odf", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.oasis.opendocument.graphics", "odg", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.oasis.opendocument.graphics-template", "otg", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.oasis.opendocument.image", "odi", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.oasis.opendocument.spreadsheet", "ods", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.oasis.opendocument.spreadsheet-template", "ots", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.oasis.opendocument.text", "odt", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.oasis.opendocument.text-master", "odm", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.oasis.opendocument.text-template", "ott", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.oasis.opendocument.text-web", "oth", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.google-earth.kml+xml", "kml", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.google-earth.kmz", "kmz", DOWNLOADED_TYPE_OTHERS);
        add("application/msword", "doc", DOWNLOADED_TYPE_DOC);
        add("application/msword", "dot", DOWNLOADED_TYPE_DOC);
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx", DOWNLOADED_TYPE_DOC);
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.template", "dotx", DOWNLOADED_TYPE_DOC);
        add("application/vnd.ms-excel", "xls", DOWNLOADED_TYPE_DOC);
        add("application/vnd.ms-excel", "xlt", DOWNLOADED_TYPE_DOC);
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx", DOWNLOADED_TYPE_DOC);
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.template", "xltx", DOWNLOADED_TYPE_DOC);
        add("application/vnd.ms-powerpoint", "ppt", DOWNLOADED_TYPE_DOC);
        add("application/vnd.ms-powerpoint", "pot", DOWNLOADED_TYPE_DOC);
        add("application/vnd.ms-powerpoint", "pps", DOWNLOADED_TYPE_DOC);
        add("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx", DOWNLOADED_TYPE_DOC);
        add("application/vnd.openxmlformats-officedocument.presentationml.template", "potx", DOWNLOADED_TYPE_DOC);
        add("application/vnd.openxmlformats-officedocument.presentationml.slideshow", "ppsx", DOWNLOADED_TYPE_DOC);
        add("application/vnd.rim.cod", "cod", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.smaf", "mmf", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.stardivision.calc", "sdc", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.stardivision.draw", "sda", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.stardivision.impress", "sdd", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.stardivision.impress", "sdp", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.stardivision.math", "smf", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.stardivision.writer", "sdw", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.stardivision.writer", "vor", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.stardivision.writer-global", "sgl", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.sun.xml.calc", "sxc", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.sun.xml.calc.template", "stc", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.sun.xml.draw", "sxd", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.sun.xml.draw.template", "std", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.sun.xml.impress", "sxi", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.sun.xml.impress.template", "sti", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.sun.xml.math", "sxm", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.sun.xml.writer", "sxw", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.sun.xml.writer.global", "sxg", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.sun.xml.writer.template", "stw", DOWNLOADED_TYPE_OTHERS);
        add("application/vnd.visio", "vsd", DOWNLOADED_TYPE_OTHERS);
        add("application/x-abiword", "abw", DOWNLOADED_TYPE_OTHERS);
        add("application/x-apple-diskimage", "dmg", DOWNLOADED_TYPE_OTHERS);
        add("application/x-bcpio", "bcpio", DOWNLOADED_TYPE_OTHERS);
        add("application/x-bittorrent", "torrent", DOWNLOADED_TYPE_OTHERS);
        add("application/x-cdf", "cdf", DOWNLOADED_TYPE_OTHERS);
        add("application/x-cdlink", "vcd", DOWNLOADED_TYPE_OTHERS);
        add("application/x-chess-pgn", "pgn", DOWNLOADED_TYPE_OTHERS);
        add("application/x-cpio", "cpio", DOWNLOADED_TYPE_OTHERS);
        add("application/x-debian-package", "deb", DOWNLOADED_TYPE_OTHERS);
        add("application/x-debian-package", "udeb", DOWNLOADED_TYPE_OTHERS);
        add("application/x-director", "dcr", DOWNLOADED_TYPE_OTHERS);
        add("application/x-director", "dir", DOWNLOADED_TYPE_OTHERS);
        add("application/x-director", "dxr", DOWNLOADED_TYPE_OTHERS);
        add("application/x-dms", "dms", DOWNLOADED_TYPE_OTHERS);
        add("application/x-doom", "wad", DOWNLOADED_TYPE_OTHERS);
        add("application/x-dvi", "dvi", DOWNLOADED_TYPE_OTHERS);
        add("application/x-flac", "flac", DOWNLOADED_TYPE_MUSIC);
        add("application/x-font", "pfa", DOWNLOADED_TYPE_OTHERS);
        add("application/x-font", "pfb", DOWNLOADED_TYPE_OTHERS);
        add("application/x-font", "gsf", DOWNLOADED_TYPE_OTHERS);
        add("application/x-font", "pcf", DOWNLOADED_TYPE_OTHERS);
        add("application/x-font", "pcf.Z", DOWNLOADED_TYPE_OTHERS);
        add("application/x-freemind", "mm", DOWNLOADED_TYPE_OTHERS);
        add("application/x-futuresplash", "spl", DOWNLOADED_TYPE_OTHERS);
        add("application/x-gnumeric", "gnumeric", DOWNLOADED_TYPE_OTHERS);
        add("application/x-go-sgf", "sgf", DOWNLOADED_TYPE_OTHERS);
        add("application/x-graphing-calculator", "gcf", DOWNLOADED_TYPE_OTHERS);
        add("application/x-gtar", "gtar", DOWNLOADED_TYPE_OTHERS);
        add("application/x-gtar", "tgz", DOWNLOADED_TYPE_OTHERS);
        add("application/x-gtar", "taz", DOWNLOADED_TYPE_OTHERS);
        add("application/x-hdf", "hdf", DOWNLOADED_TYPE_OTHERS);
        add("application/x-ica", "ica", DOWNLOADED_TYPE_OTHERS);
        add("application/x-internet-signup", "ins", DOWNLOADED_TYPE_OTHERS);
        add("application/x-internet-signup", "isp", DOWNLOADED_TYPE_OTHERS);
        add("application/x-iphone", "iii", DOWNLOADED_TYPE_OTHERS);
        add("application/x-iso9660-image", "iso", DOWNLOADED_TYPE_OTHERS);
        add("application/x-jmol", "jmz", DOWNLOADED_TYPE_OTHERS);
        add("application/x-kchart", "chrt", DOWNLOADED_TYPE_OTHERS);
        add("application/x-killustrator", "kil", DOWNLOADED_TYPE_OTHERS);
        add("application/x-koan", "skp", DOWNLOADED_TYPE_OTHERS);
        add("application/x-koan", "skd", DOWNLOADED_TYPE_OTHERS);
        add("application/x-koan", "skt", DOWNLOADED_TYPE_OTHERS);
        add("application/x-koan", "skm", DOWNLOADED_TYPE_OTHERS);
        add("application/x-kpresenter", "kpr", DOWNLOADED_TYPE_OTHERS);
        add("application/x-kpresenter", "kpt", DOWNLOADED_TYPE_OTHERS);
        add("application/x-kspread", "ksp", DOWNLOADED_TYPE_OTHERS);
        add("application/x-kword", "kwd", DOWNLOADED_TYPE_OTHERS);
        add("application/x-kword", "kwt", DOWNLOADED_TYPE_OTHERS);
        add("application/x-latex", "latex", DOWNLOADED_TYPE_OTHERS);
        add("application/x-lha", "lha", DOWNLOADED_TYPE_OTHERS);
        add("application/x-lzh", "lzh", DOWNLOADED_TYPE_OTHERS);
        add("application/x-lzx", "lzx", DOWNLOADED_TYPE_OTHERS);
        add("application/x-maker", "frm", DOWNLOADED_TYPE_OTHERS);
        add("application/x-maker", "maker", DOWNLOADED_TYPE_OTHERS);
        add("application/x-maker", "frame", DOWNLOADED_TYPE_OTHERS);
        add("application/x-maker", "fb", DOWNLOADED_TYPE_OTHERS);
        add("application/x-maker", "book", DOWNLOADED_TYPE_OTHERS);
        add("application/x-maker", "fbdoc", DOWNLOADED_TYPE_OTHERS);
        add("application/x-mif", "mif", DOWNLOADED_TYPE_OTHERS);
        add("application/x-ms-wmd", "wmd", DOWNLOADED_TYPE_OTHERS);
        add("application/x-ms-wmz", "wmz", DOWNLOADED_TYPE_OTHERS);
        add("application/x-msi", "msi", DOWNLOADED_TYPE_OTHERS);
        add("application/x-ns-proxy-autoconfig", "pac", DOWNLOADED_TYPE_OTHERS);
        add("application/x-nwc", "nwc", DOWNLOADED_TYPE_OTHERS);
        add("application/x-object", "o", DOWNLOADED_TYPE_OTHERS);
        add("application/x-oz-application", "oza", DOWNLOADED_TYPE_OTHERS);
        add("application/x-pkcs12", "p12", DOWNLOADED_TYPE_OTHERS);
        add("application/x-pkcs12", "pfx", DOWNLOADED_TYPE_OTHERS);
        add("application/x-pkcs7-certreqresp", "p7r", DOWNLOADED_TYPE_OTHERS);
        add("application/x-pkcs7-crl", "crl", DOWNLOADED_TYPE_OTHERS);
        add("application/x-quicktimeplayer", "qtl", DOWNLOADED_TYPE_OTHERS);
        add("application/x-shar", "shar", DOWNLOADED_TYPE_OTHERS);
        add("application/x-shockwave-flash", "swf", DOWNLOADED_TYPE_VIDEO);
        add("application/x-stuffit", "sit", DOWNLOADED_TYPE_OTHERS);
        add("application/x-sv4cpio", "sv4cpio", DOWNLOADED_TYPE_OTHERS);
        add("application/x-sv4crc", "sv4crc", DOWNLOADED_TYPE_OTHERS);
        add("application/x-tar", "tar", DOWNLOADED_TYPE_ZIP);
        add("application/x-texinfo", "texinfo", DOWNLOADED_TYPE_OTHERS);
        add("application/x-texinfo", "texi", DOWNLOADED_TYPE_OTHERS);
        add("application/x-troff", "t", DOWNLOADED_TYPE_OTHERS);
        add("application/x-troff", "roff", DOWNLOADED_TYPE_OTHERS);
        add("application/x-troff-man", "man", DOWNLOADED_TYPE_OTHERS);
        add("application/x-ustar", "ustar", DOWNLOADED_TYPE_OTHERS);
        add("application/x-wais-source", "src", DOWNLOADED_TYPE_OTHERS);
        add("application/x-wingz", "wz", DOWNLOADED_TYPE_OTHERS);
        add("application/x-webarchive", "webarchive", DOWNLOADED_TYPE_OTHERS);
        add("application/x-webarchive-xml", "webarchivexml", DOWNLOADED_TYPE_OTHERS);
        add("application/x-x509-ca-cert", "crt", DOWNLOADED_TYPE_OTHERS);
        add("application/x-x509-user-cert", "crt", DOWNLOADED_TYPE_OTHERS);
        add("application/x-xcf", "xcf", DOWNLOADED_TYPE_OTHERS);
        add("application/x-xfig", "fig", DOWNLOADED_TYPE_OTHERS);
        add("application/xhtml+xml", "xhtml", DOWNLOADED_TYPE_OTHERS);
        
        add("audio/3gpp", "3gpp", DOWNLOADED_TYPE_MUSIC);
        add("audio/amr", "amr", DOWNLOADED_TYPE_MUSIC);
        add("audio/basic", "snd", DOWNLOADED_TYPE_MUSIC);
        add("audio/midi", "mid", DOWNLOADED_TYPE_MUSIC);
        add("audio/midi", "midi", DOWNLOADED_TYPE_MUSIC);
        add("audio/midi", "kar", DOWNLOADED_TYPE_MUSIC);
        add("audio/midi", "xmf", DOWNLOADED_TYPE_MUSIC);
        add("audio/mobile-xmf", "mxmf", DOWNLOADED_TYPE_MUSIC);
        add("audio/mpeg", "mp3", DOWNLOADED_TYPE_MUSIC);
        add("audio/mpeg", "mpga", DOWNLOADED_TYPE_MUSIC);
        add("audio/mpeg", "mpega", DOWNLOADED_TYPE_MUSIC);
        add("audio/mpeg", "mp2", DOWNLOADED_TYPE_MUSIC);
        add("audio/mpeg", "m4a", DOWNLOADED_TYPE_MUSIC);
        add("audio/mpegurl", "m3u", DOWNLOADED_TYPE_MUSIC);
        add("audio/prs.sid", "sid", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-aiff", "aif", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-aiff", "aiff", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-aiff", "aifc", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-gsm", "gsm", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-mpegurl", "m3u", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-ms-wma", "wma", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-ms-wax", "wax", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-pn-realaudio", "ra", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-pn-realaudio", "rm", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-pn-realaudio", "ram", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-realaudio", "ra", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-scpls", "pls", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-sd2", "sd2", DOWNLOADED_TYPE_MUSIC);
        add("audio/x-wav", "wav", DOWNLOADED_TYPE_MUSIC);
        
        add("image/bmp", "bmp", DOWNLOADED_TYPE_IMAGE);
        add("image/gif", "gif", DOWNLOADED_TYPE_IMAGE);
        add("image/ico", "cur", DOWNLOADED_TYPE_OTHERS);
        add("image/ico", "ico", DOWNLOADED_TYPE_IMAGE);
        add("image/ief", "ief", DOWNLOADED_TYPE_OTHERS);
        add("image/jpeg", "jpeg", DOWNLOADED_TYPE_IMAGE);
        add("image/jpeg", "jpg", DOWNLOADED_TYPE_IMAGE);
        add("image/jpeg", "jpe", DOWNLOADED_TYPE_IMAGE);
        add("image/pcx", "pcx", DOWNLOADED_TYPE_OTHERS);
        add("image/png", "png", DOWNLOADED_TYPE_IMAGE);
        add("image/svg+xml", "svg", DOWNLOADED_TYPE_OTHERS);
        add("image/svg+xml", "svgz", DOWNLOADED_TYPE_OTHERS);
        add("image/tiff", "tiff", DOWNLOADED_TYPE_OTHERS);
        add("image/tiff", "tif", DOWNLOADED_TYPE_OTHERS);
        add("image/vnd.djvu", "djvu", DOWNLOADED_TYPE_OTHERS);
        add("image/vnd.djvu", "djv", DOWNLOADED_TYPE_OTHERS);
        add("image/vnd.wap.wbmp", "wbmp", DOWNLOADED_TYPE_IMAGE);
        add("image/x-cmu-raster", "ras", DOWNLOADED_TYPE_OTHERS);
        add("image/x-coreldraw", "cdr", DOWNLOADED_TYPE_OTHERS);
        add("image/x-coreldrawpattern", "pat", DOWNLOADED_TYPE_OTHERS);
        add("image/x-coreldrawtemplate", "cdt", DOWNLOADED_TYPE_OTHERS);
        add("image/x-corelphotopaint", "cpt", DOWNLOADED_TYPE_OTHERS);
        add("image/x-icon", "ico", DOWNLOADED_TYPE_IMAGE);
        add("image/x-jg", "art", DOWNLOADED_TYPE_OTHERS);
        add("image/x-jng", "jng", DOWNLOADED_TYPE_OTHERS);
        add("image/x-ms-bmp", "bmp", DOWNLOADED_TYPE_IMAGE);
        add("image/x-photoshop", "psd", DOWNLOADED_TYPE_OTHERS);
        add("image/x-portable-anymap", "pnm", DOWNLOADED_TYPE_OTHERS);
        add("image/x-portable-bitmap", "pbm", DOWNLOADED_TYPE_OTHERS);
        add("image/x-portable-graymap", "pgm", DOWNLOADED_TYPE_OTHERS);
        add("image/x-portable-pixmap", "ppm", DOWNLOADED_TYPE_OTHERS);
        add("image/x-rgb", "rgb", DOWNLOADED_TYPE_OTHERS);
        add("image/x-xbitmap", "xbm", DOWNLOADED_TYPE_OTHERS);
        add("image/x-xpixmap", "xpm", DOWNLOADED_TYPE_OTHERS);
        add("image/x-xwindowdump", "xwd", DOWNLOADED_TYPE_OTHERS);
        
        add("model/iges", "igs", DOWNLOADED_TYPE_OTHERS);
        add("model/iges", "iges", DOWNLOADED_TYPE_OTHERS);
        add("model/mesh", "msh", DOWNLOADED_TYPE_OTHERS);
        add("model/mesh", "mesh", DOWNLOADED_TYPE_OTHERS);
        add("model/mesh", "silo", DOWNLOADED_TYPE_OTHERS);
        
        add("text/calendar", "ics", DOWNLOADED_TYPE_OTHERS);
        add("text/calendar", "icz", DOWNLOADED_TYPE_OTHERS);
        add("text/comma-separated-values", "csv", DOWNLOADED_TYPE_OTHERS);
        add("text/css", "css", DOWNLOADED_TYPE_OTHERS);
        add("text/html", "htm", DOWNLOADED_TYPE_OTHERS);
        add("text/html", "html", DOWNLOADED_TYPE_OTHERS);
        add("text/h323", "323", DOWNLOADED_TYPE_OTHERS);
        add("text/iuls", "uls", DOWNLOADED_TYPE_OTHERS);
        add("text/mathml", "mml", DOWNLOADED_TYPE_OTHERS);
                
        add("text/plain-story", "txt", DOWNLOADED_TYPE_NOVEL);
        // add ".txt" first so it will be the default for ExtensionFromMimeType
        add("text/plain", "txt", DOWNLOADED_TYPE_DOC);
        add("text/plain", "asc", DOWNLOADED_TYPE_DOC);
        add("text/plain", "text", DOWNLOADED_TYPE_DOC);
        add("text/plain", "diff", DOWNLOADED_TYPE_DOC);
        add("text/plain", "po", DOWNLOADED_TYPE_DOC);     // reserve "pot" for vnd.ms-powerpoint
        add("text/richtext", "rtx", DOWNLOADED_TYPE_DOC);
        add("text/rtf", "rtf", DOWNLOADED_TYPE_DOC);
        add("text/texmacs", "ts", DOWNLOADED_TYPE_OTHERS);
        add("text/text", "phps", DOWNLOADED_TYPE_OTHERS);
        add("text/tab-separated-values", "tsv", DOWNLOADED_TYPE_OTHERS);
        add("text/xml", "xml", DOWNLOADED_TYPE_DOC);
        add("text/x-bibtex", "bib", DOWNLOADED_TYPE_OTHERS);
        add("text/x-boo", "boo", DOWNLOADED_TYPE_OTHERS);
        add("text/x-c++hdr", "h++", DOWNLOADED_TYPE_OTHERS);
        add("text/x-c++hdr", "hpp", DOWNLOADED_TYPE_OTHERS);
        add("text/x-c++hdr", "hxx", DOWNLOADED_TYPE_OTHERS);
        add("text/x-c++hdr", "hh", DOWNLOADED_TYPE_OTHERS);
        add("text/x-c++src", "c++", DOWNLOADED_TYPE_OTHERS);
        add("text/x-c++src", "cpp", DOWNLOADED_TYPE_OTHERS);
        add("text/x-c++src", "cxx", DOWNLOADED_TYPE_OTHERS);
        add("text/x-chdr", "h", DOWNLOADED_TYPE_OTHERS);
        add("text/x-component", "htc", DOWNLOADED_TYPE_OTHERS);
        add("text/x-csh", "csh", DOWNLOADED_TYPE_OTHERS);
        add("text/x-csrc", "c", DOWNLOADED_TYPE_OTHERS);
        add("text/x-dsrc", "d", DOWNLOADED_TYPE_OTHERS);
        add("text/x-haskell", "hs", DOWNLOADED_TYPE_OTHERS);
        add("text/x-java", "java", DOWNLOADED_TYPE_OTHERS);
        add("text/x-literate-haskell", "lhs", DOWNLOADED_TYPE_OTHERS);
        add("text/x-moc", "moc", DOWNLOADED_TYPE_OTHERS);
        add("text/x-pascal", "p", DOWNLOADED_TYPE_OTHERS);
        add("text/x-pascal", "pas", DOWNLOADED_TYPE_OTHERS);
        add("text/x-pcs-gcd", "gcd", DOWNLOADED_TYPE_OTHERS);
        add("text/x-setext", "etx", DOWNLOADED_TYPE_OTHERS);
        add("text/x-tcl", "tcl", DOWNLOADED_TYPE_OTHERS);
        add("text/x-tex", "tex", DOWNLOADED_TYPE_OTHERS);
        add("text/x-tex", "ltx", DOWNLOADED_TYPE_OTHERS);
        add("text/x-tex", "sty", DOWNLOADED_TYPE_OTHERS);
        add("text/x-tex", "cls", DOWNLOADED_TYPE_OTHERS);
        add("text/x-vcalendar", "vcs", DOWNLOADED_TYPE_OTHERS);
        add("text/x-vcard", "vcf", DOWNLOADED_TYPE_OTHERS);
        
        add("video/mkv", "mkv", DOWNLOADED_TYPE_VIDEO);
        add("video/3gpp", "3gpp", DOWNLOADED_TYPE_VIDEO);
        add("video/3gpp", "3gp", DOWNLOADED_TYPE_VIDEO);
        add("video/3gpp", "3g2", DOWNLOADED_TYPE_VIDEO);
        add("video/dl", "dl", DOWNLOADED_TYPE_VIDEO);
        add("video/dv", "dif", DOWNLOADED_TYPE_VIDEO);
        add("video/dv", "dv", DOWNLOADED_TYPE_VIDEO);
        add("video/fli", "fli", DOWNLOADED_TYPE_VIDEO);
        add("video/m4v", "m4v", DOWNLOADED_TYPE_VIDEO);
        add("video/mpeg", "mpeg", DOWNLOADED_TYPE_VIDEO);
        add("video/mpeg", "mpg", DOWNLOADED_TYPE_VIDEO);
        add("video/mpeg", "mpe", DOWNLOADED_TYPE_VIDEO);
        add("video/mp4", "mp4", DOWNLOADED_TYPE_VIDEO);
        add("video/mpeg", "vob", DOWNLOADED_TYPE_VIDEO);
        add("video/quicktime", "qt", DOWNLOADED_TYPE_VIDEO);
        add("video/quicktime", "mov", DOWNLOADED_TYPE_VIDEO);
        add("video/vnd.mpegurl", "mxu", DOWNLOADED_TYPE_VIDEO);
        add("video/x-la-asf", "lsf", DOWNLOADED_TYPE_VIDEO);
        add("video/x-la-asf", "lsx", DOWNLOADED_TYPE_VIDEO);
        add("video/x-mng", "mng", DOWNLOADED_TYPE_VIDEO);
        add("video/x-ms-asf", "asf", DOWNLOADED_TYPE_VIDEO);
        add("video/x-ms-asf", "asx", DOWNLOADED_TYPE_VIDEO);
        add("video/x-ms-wm", "wm", DOWNLOADED_TYPE_VIDEO);
        add("video/x-ms-wmv", "wmv", DOWNLOADED_TYPE_VIDEO);
        add("video/x-ms-wmx", "wmx", DOWNLOADED_TYPE_VIDEO);
        add("video/x-ms-wvx", "wvx", DOWNLOADED_TYPE_VIDEO);
        add("video/x-msvideo", "avi", DOWNLOADED_TYPE_VIDEO);
        add("video/x-sgi-movie", "movie", DOWNLOADED_TYPE_VIDEO);
        add("video/x-webex", "wrf", DOWNLOADED_TYPE_VIDEO);
        
        add("x-conference/x-cooltalk", "ice", DOWNLOADED_TYPE_OTHERS);
        add("x-epoc/x-sisx-app", "sisx", DOWNLOADED_TYPE_OTHERS);
        //***ADD*** 20130428
        add("video/vnd.rn-realvideo", "rmvb", DOWNLOADED_TYPE_VIDEO);
        add("video/x-flv", "flv", DOWNLOADED_TYPE_VIDEO);
        add("audio/aac", "aac", DOWNLOADED_TYPE_MUSIC);
        add("application/vnd.rn-realmedia", "rm", DOWNLOADED_TYPE_VIDEO);
        //***END***
    }
    /**
     * Mimetype、后缀，添加到Map表中
     * @param mimeType MimeType
     * @param extension 后缀,不含有“.”
     * @param fileType 文件类型
     */
    private static void add(String mimeType, String extension, int fileType) {
        sExtTypeMap.put(extension, fileType);
        sMimeTypeMap.put(mimeType, fileType);
        sExtensionToMimeTypeMap.put(extension, mimeType);
        if (!sMimeTypeToExtensionMap.containsKey(mimeType)) {
            sMimeTypeToExtensionMap.put(mimeType, extension);
        }
    }
    /**
     * 从mimetype中判断文件所属类别,若为空，则从后缀中判断。
     * @param ext 文件名后缀，不包含"."
     * @param mime mimetype
     * @return 所属类别
     */
    public static int getCategory(String ext, String mime) {
        return getCategory(ext, mime, true);
    }
    
    /**
     * 从mimetype中判断文件所属类别,若为空，则从后缀中判断。
     * @param ext 文件名后缀，不包含"."
     * @param mime mimetype
     * @param isDownloadModel 是否是下载模块使用
     * @return 所属类别
     */
    public static int getCategory(String ext, String mime, boolean isDownloadModel) {
        Integer result;
        result = sMimeTypeMap.get(mime);
        if (result == null) {
            result = sExtTypeMap.get(ext);
            if (result == null) {
                result = DOWNLOADED_TYPE_OTHERS;
            } else {
                if (isDownloadModel && result == DOWNLOADED_TYPE_ZIP) {
                    result = DOWNLOADED_TYPE_OTHERS;
                }
            }
        } else {
            if (isDownloadModel && result == DOWNLOADED_TYPE_ZIP) {
                result = DOWNLOADED_TYPE_OTHERS;
            }
        }
        return result.intValue();
    }
    
    /**
     * 从文件后缀中判断文件所属类别,若为空，则从后缀中判断。
     * @param ext 文件后缀
     * @return 所属类别
     */
    public static int getCategory(String ext) {
        return getCategory(ext, "", false);
    }
    
    /**
     * 根据后缀判断是否播放内核支持
     * @param extension 后缀
     * @return boolean isVideoKernelSupport
     */
    public static boolean isVideoKernelSupport(String extension) {
        if (TextUtils.isEmpty(extension)) {
            return false;
        }
        String lowextension = extension.toLowerCase(); 
        if ("m3u8".equals(lowextension) || "m3u".equals(lowextension)
                || "asf".equals(lowextension)
                || "wmv".equals(lowextension)
                || "avi".equals(lowextension)
                || "flv".equals(lowextension)
                || "mkv".equals(lowextension)
                || "mov".equals(lowextension) || "mp4".equals(lowextension) 
                || "3gp".equals(lowextension) || "3g2".equals(lowextension)
                || "mpeg".equals(lowextension)
                || "ts".equals(lowextension)
                || "rm".equals(lowextension) || "rmvb".equals(lowextension)
                || "webm".equals(lowextension)) {
            return true;
        }
        return false;
    }
    
    /**
     * 获取文件后缀
     * @param fileName 文件名
     * @return 文件后缀
     */
    public static String getFileSuffix(String fileName) {
        if (fileName == null) {
            return "";
        }
        int len = fileName.lastIndexOf(".");
        if (len == -1 || len == fileName.length()) {
            return "";
        }
        return fileName.substring(len + 1);
    }
    
    /**
     * The MIME type for the given extension or null if there is none.
     * @param extension A file extension without the leading '.'
     * @return Returns the MIME type for the given extension.
     */
    public static String guessMimeTypeFromExtension(String extension) {
        if (extension == null || TextUtils.isEmpty(extension)) {
            return null;
        }
        return sExtensionToMimeTypeMap.get(extension);
    }
    
    /**
     * Returns the registered extension for the given MIME type. Note that some MIME types map to multiple extensions. 
     * This call will return the most common extension for the given MIME type. 
     * @param mimeType MIME type
     * @return The extension for the given MIME type or null if there is none.
     */
    public static String guessExtensionFromMimeType(String mimeType) {
        if (mimeType == null || TextUtils.isEmpty(mimeType)) {
            return null;
        }
        return sMimeTypeToExtensionMap.get(mimeType);
    }
    
    /**
     * 去除文件名后缀的文件名
     * @param fileName 原始文件名
     * @return 去除后缀的文件名
     */
    public static String getFileNameExcludeExtension(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        String suffix = getFileSuffix(fileName);
        if (sExtTypeMap.get(suffix) != null) {
            return fileName.substring(0, fileName.length() - suffix.length() - 1);
        }
        return fileName;
    }

}
