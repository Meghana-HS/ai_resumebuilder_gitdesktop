package com.project.app.dto;

import com.project.app.entity.Download;

public class DownloadRequest {

    private String name;
    private String type;     // "resume", "cover-letter", "cv"
    private String format;   // "PDF", "DOCX", "DOC"
    private String html;
    private String template;
    private String size;
    private String action;   // "visited", "preview", "download"

    // ── Getters ──────────────────────────────────────────────────────────

    public String getName()     { return name; }
    public String getType()     { return type; }
    public String getFormat()   { return format; }
    public String getHtml()     { return html; }
    public String getTemplate() { return template; }
    public String getSize()     { return size; }
    public String getAction()   { return action; }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setName(String name)         { this.name = name; }
    public void setType(String type)         { this.type = type; }
    public void setFormat(String format)     { this.format = format; }
    public void setHtml(String html)         { this.html = html; }
    public void setTemplate(String template) { this.template = template; }
    public void setSize(String size)         { this.size = size; }
    public void setAction(String action)     { this.action = action; }

    // ── Convert to Download entity ────────────────────────────────────────

    public Download toDownload() {
        Download download = new Download();

        // name — fall back to "Untitled" so the NOT NULL constraint is never violated
        download.setName(this.name != null && !this.name.isBlank() ? this.name : "Untitled");

        // type
        if (this.type != null) {
            switch (this.type.toLowerCase()) {
                case "cover-letter":
                    download.setType(Download.DocumentType.COVER_LETTER);
                    break;
                case "cv":
                    download.setType(Download.DocumentType.CV);
                    break;
                case "resume":
                default:
                    download.setType(Download.DocumentType.RESUME);
                    break;
            }
        } else {
            download.setType(Download.DocumentType.RESUME);
        }

        // format
        if (this.format != null) {
            switch (this.format.toUpperCase()) {
                case "DOCX":
                    download.setFormat(Download.Format.DOCX);
                    break;
                case "DOC":
                    download.setFormat(Download.Format.DOC);
                    break;
                case "PDF":
                default:
                    download.setFormat(Download.Format.PDF);
                    break;
            }
        } else {
            download.setFormat(Download.Format.PDF);
        }

        // action
        if (this.action != null) {
            switch (this.action.toLowerCase()) {
                case "visited":
                    download.setAction(Download.Action.VISITED);
                    break;
                case "preview":
                    download.setAction(Download.Action.PREVIEW);
                    break;
                case "download":
                default:
                    download.setAction(Download.Action.DOWNLOAD);
                    break;
            }
        } else {
            download.setAction(Download.Action.DOWNLOAD);
        }

        download.setHtml(this.html);
        download.setTemplate(this.template);
        download.setSize(this.size);

        return download;
    }
}
