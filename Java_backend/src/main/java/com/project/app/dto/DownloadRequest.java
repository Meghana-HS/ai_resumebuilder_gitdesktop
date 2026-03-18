package com.project.app.dto;

import com.project.app.entity.Download;



public class DownloadRequest {
    private String name;
    private String type; // "resume", "cover-letter", "cv"
    private String format; // "PDF", "DOCX", "DOC"
    private String html;
    private String template;
    private String size;
    
    // Convert to Download entity
    public Download toDownload() {
        Download download = new Download();
        download.setName(this.name);
        
        // Convert string type to enum
        if (this.type != null) {
            switch (this.type.toLowerCase()) {
                case "resume":
                    download.setType(Download.DocumentType.RESUME);
                    break;
                case "cover-letter":
                    download.setType(Download.DocumentType.COVER_LETTER);
                    break;
                case "cv":
                    download.setType(Download.DocumentType.CV);
                    break;
                default:
                    download.setType(Download.DocumentType.RESUME);
            }
        }
        
        // Convert string format to enum
        if (this.format != null) {
            switch (this.format.toUpperCase()) {
                case "PDF":
                    download.setFormat(Download.Format.PDF);
                    break;
                case "DOCX":
                    download.setFormat(Download.Format.DOCX);
                    break;
                case "DOC":
                    download.setFormat(Download.Format.DOC);
                    break;
                default:
                    download.setFormat(Download.Format.PDF);
            }
        }
        
        download.setHtml(this.html);
        download.setTemplate(this.template);
        download.setSize(this.size);
        download.setAction(Download.Action.DOWNLOAD);
        
        return download;
    }
}
