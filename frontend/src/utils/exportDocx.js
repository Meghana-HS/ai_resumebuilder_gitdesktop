import htmlToDocx from "html-to-docx";

function safeFilename(name) {
  const cleaned = String(name || "")
    .replace(/[^a-z0-9_\- ]/gi, "")
    .trim()
    .replace(/\s+/g, "_");
  return cleaned || "Document";
}

function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

function inlineComputedStyles(sourceEl, targetEl) {
  const computed = window.getComputedStyle(sourceEl);
  let cssText = "";
  for (let i = 0; i < computed.length; i += 1) {
    const prop = computed[i];
    const value = computed.getPropertyValue(prop);
    if (!value) continue;
    cssText += `${prop}:${value};`;
  }

  const existing = targetEl.getAttribute("style") || "";
  targetEl.setAttribute("style", `${existing}${cssText}`);
}

function cloneWithInlineStyles(root) {
  const clone = root.cloneNode(true);

  const sourceWalker = document.createTreeWalker(root, NodeFilter.SHOW_ELEMENT);
  const targetWalker = document.createTreeWalker(clone, NodeFilter.SHOW_ELEMENT);

  let sourceNode = sourceWalker.currentNode;
  let targetNode = targetWalker.currentNode;

  while (sourceNode && targetNode) {
    inlineComputedStyles(sourceNode, targetNode);
    sourceNode = sourceWalker.nextNode();
    targetNode = targetWalker.nextNode();
  }

  return clone;
}

/**
 * Export a real .docx by inlining computed styles from a rendered DOM element.
 * This produces a Word file that matches the on-screen/PDF layout much better
 * than exporting plain HTML without styles.
 */
export async function exportDocxFromElement({
  element,
  title = "Document",
  page = { size: "A4", margin: 720 }, // TWIPs (720 = 0.5 inch)
} = {}) {
  if (!element) throw new Error("Missing element for DOCX export");

  const styledClone = cloneWithInlineStyles(element);

  const fullHtml = `<!doctype html><html><head><meta charset="utf-8" /></head><body>${styledClone.outerHTML}</body></html>`;

  const docxBuffer = await htmlToDocx(fullHtml, null, {
    pageSize: page.size,
    margins: {
      top: page.margin,
      right: page.margin,
      bottom: page.margin,
      left: page.margin,
    },
  });

  const blob = new Blob([docxBuffer], {
    type: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
  });

  downloadBlob(blob, `${safeFilename(title)}.docx`);
}

