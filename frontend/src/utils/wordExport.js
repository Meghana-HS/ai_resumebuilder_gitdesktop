/**
 * wordExport.js
 *
 * Shared utility for generating Word (.docx) files that visually match
 * the PDF output exactly.
 *
 * Strategy
 * ────────
 * 1. Render the supplied DOM container with html2canvas at 3× scale
 *    (identical to the PDF pipeline).
 * 2. Slice the canvas into A4-page-height segments so multi-page
 *    documents get a separate page for each segment.
 * 3. Pack each slice into a docx ImageRun inside a full-bleed page
 *    (zero margins) so the image fills every page perfectly.
 * 4. Return the Blob AND save it with file-saver.
 *
 * The result is a pixel-perfect replica of the PDF inside a real .docx
 * file that opens cleanly in Microsoft Word, LibreOffice, Google Docs, etc.
 */

import html2canvas from "html2canvas";
import {
  Document,
  Packer,
  Paragraph,
  ImageRun,
  PageBreak,
} from "docx";
import { saveAs } from "file-saver";

// ─── Constants ────────────────────────────────────────────────────────────

/** A4 render width in CSS pixels at 96 DPI */
const A4_PX_W = 794;

/** A4 render height in CSS pixels at 96 DPI */
const A4_PX_H = 1123;

/**
 * A4 page size in twips (1 inch = 1 440 twips).
 *  210 mm = 8.268 in → 11 906 twips
 *  297 mm = 11.693 in → 16 838 twips
 */
const A4_TWIPS_W = 11906;
const A4_TWIPS_H = 16838;

// ─── Helpers ──────────────────────────────────────────────────────────────

/**
 * Sanitise a string so it is safe to use as a filename.
 * Strips characters that are invalid on Windows / macOS / Linux,
 * collapses whitespace to underscores, and trims the result.
 *
 * @param {string} s
 * @returns {string}
 */
export const sanitiseFilename = (s) =>
  (s || "")
    .replace(/[^a-z0-9_\- ]/gi, "")
    .trim()
    .replace(/\s+/g, "_");

/**
 * Convert a canvas element to an ArrayBuffer (PNG).
 *
 * @param {HTMLCanvasElement} canvas
 * @returns {Promise<ArrayBuffer>}
 */
const canvasToArrayBuffer = (canvas) =>
  new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (!blob) { reject(new Error("canvas.toBlob returned null")); return; }
      blob.arrayBuffer().then(resolve).catch(reject);
    }, "image/png");
  });

// ─── Core export function ─────────────────────────────────────────────────

/**
 * Render `container` to a high-resolution canvas and save a .docx file
 * where each A4 page contains one image slice, giving a pixel-perfect
 * replica of the PDF version.
 *
 * @param {HTMLElement} container
 *   A DOM node that is already in the document (may be off-screen).
 *   It should be 794 px wide and have white background.
 *
 * @param {string} filename
 *   Desired filename WITHOUT extension. Will be sanitised automatically.
 *   Example: "resume_John_Doe"  →  saves as  "resume_John_Doe.docx"
 *
 * @param {object}  [opts]
 * @param {number}  [opts.scale=3]
 *   html2canvas capture scale.  3 = 3× retina quality (matches PDF pipeline).
 * @param {number}  [opts.pageWidthPx=794]
 *   The logical CSS pixel width of one page.  Keep at 794 for A4.
 * @param {boolean} [opts.save=true]
 *   When true (default) the file is saved immediately via file-saver.
 *   Pass false if you only need the returned Blob.
 *
 * @returns {Promise<Blob>}  The raw .docx Blob (useful for storing / uploading).
 */
export const generateWordFromElement = async (
  container,
  filename,
  { scale = 3, pageWidthPx = A4_PX_W, save = true } = {},
) => {
  if (!container) throw new Error("wordExport: container is null");

  // ── 1. Capture the full content as a high-resolution PNG ───────────────
  const canvas = await html2canvas(container, {
    scale,
    useCORS: true,
    logging: false,
    windowWidth: pageWidthPx,
    backgroundColor: "#ffffff",
  });

  // Logical pixel dimensions of the full rendered output
  const fullW = canvas.width;   // = pageWidthPx × scale
  const fullH = canvas.height;

  // Height of one A4 page in canvas pixels
  const pageSliceH = Math.round(A4_PX_H * scale);

  // ── 2. Slice canvas into A4-page-height segments ───────────────────────
  const pages = [];
  let yOffset = 0;

  while (yOffset < fullH) {
    const sliceH  = Math.min(pageSliceH, fullH - yOffset);

    const slice   = document.createElement("canvas");
    slice.width   = fullW;
    slice.height  = pageSliceH;   // keep all slices the same height (pad with white)

    const ctx = slice.getContext("2d");
    ctx.fillStyle = "#ffffff";
    ctx.fillRect(0, 0, slice.width, slice.height);
    ctx.drawImage(canvas, 0, yOffset, fullW, sliceH, 0, 0, fullW, sliceH);

    pages.push(await canvasToArrayBuffer(slice));
    yOffset += pageSliceH;
  }

  // ── 3. Build the docx Document ─────────────────────────────────────────
  //
  //  • Page margins are all 0 so the image fills the entire page.
  //  • Image transformation width / height are in CSS pixels; the docx
  //    library converts them to EMU internally.
  //  • Each page slice becomes: ImageRun  + optional PageBreak paragraph.

  const pageChildren = [];

  pages.forEach((imgBuffer, idx) => {
    // The image should display at exactly one A4 page wide × one A4 page tall.
    pageChildren.push(
      new Paragraph({
        spacing: { before: 0, after: 0 },
        children: [
          new ImageRun({
            data: imgBuffer,
            transformation: {
              width:  A4_PX_W,
              height: A4_PX_H,
            },
          }),
        ],
      }),
    );

    // Insert a page break between slices (not after the last one)
    if (idx < pages.length - 1) {
      pageChildren.push(
        new Paragraph({
          spacing: { before: 0, after: 0 },
          children: [new PageBreak()],
        }),
      );
    }
  });

  const doc = new Document({
    styles: {
      default: {
        document: {
          run: { font: "Arial", size: 20 },
          paragraph: { spacing: { before: 0, after: 0 } },
        },
      },
    },
    sections: [
      {
        properties: {
          page: {
            size:   { width: A4_TWIPS_W, height: A4_TWIPS_H },
            margin: { top: 0, bottom: 0, left: 0, right: 0 },
          },
        },
        children: pageChildren,
      },
    ],
  });

  // ── 4. Pack to Blob and optionally save ───────────────────────────────
  const blob = await Packer.toBlob(doc);

  if (save) {
    const safeName = sanitiseFilename(filename) || "document";
    saveAs(blob, `${safeName}.docx`);
  }

  return blob;
};

// ─── Convenience wrappers ─────────────────────────────────────────────────

/**
 * Render a React component into a temporary off-screen container,
 * then export the result as a Word document.
 *
 * Useful when you have a React element (e.g. a CV template) that hasn't
 * been mounted yet but you want to export it without touching the visible DOM.
 *
 * @param {React.ReactElement} element   The React element to render.
 * @param {string}             filename  Desired filename without extension.
 * @param {object}             [opts]    Forwarded to generateWordFromElement.
 * @returns {Promise<Blob>}
 */
export const generateWordFromReactElement = async (
  element,
  filename,
  opts = {},
) => {
  const container = document.createElement("div");
  Object.assign(container.style, {
    position:   "fixed",
    top:        "0",
    left:       "-9999px",
    width:      `${A4_PX_W}px`,
    background: "#ffffff",
    zIndex:     "-1",
  });
  document.body.appendChild(container);

  try {
    const { createRoot } = await import("react-dom/client");

    await new Promise((resolve) => {
      const root = createRoot(container);
      root.render(element);
      // Give the component time to paint (fonts, images, etc.)
      setTimeout(resolve, 500);
    });

    return await generateWordFromElement(container, filename, opts);
  } finally {
    // Always clean up even if an error is thrown
    if (container.parentNode) document.body.removeChild(container);
  }
};

/**
 * Generate a Word document from a raw HTML string.
 *
 * This is used by the Downloads page to re-create a .docx from
 * the stored HTML snapshot of a previously downloaded document.
 *
 * @param {string} html      Full or partial HTML string.
 * @param {string} filename  Desired filename without extension.
 * @param {object} [opts]    Forwarded to generateWordFromElement.
 * @returns {Promise<Blob>}
 */
export const generateWordFromHtml = async (html, filename, opts = {}) => {
  if (!html || html.trim() === "") {
    throw new Error(
      "wordExport: html is empty — the document has no stored preview data.",
    );
  }

  const container = document.createElement("div");
  Object.assign(container.style, {
    position:   "fixed",
    top:        "0",
    left:       "-9999px",
    width:      `${A4_PX_W}px`,
    background: "#ffffff",
    zIndex:     "-1",
  });

  // Inject the stored HTML as-is (it is the rendered template output)
  container.innerHTML = html;
  document.body.appendChild(container);

  try {
    // Give the browser a moment to apply styles
    await new Promise((resolve) => setTimeout(resolve, 350));
    return await generateWordFromElement(container, filename, opts);
  } finally {
    if (container.parentNode) document.body.removeChild(container);
  }
};
