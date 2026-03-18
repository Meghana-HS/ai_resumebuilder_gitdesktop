import {
  Document,
  Packer,
  Paragraph,
  TextRun,
  Table,
  TableRow,
  TableCell,
  WidthType,
  BorderStyle,
  AlignmentType,
  ShadingType,
  VerticalAlign,
} from "docx";
import { saveAs } from "file-saver";

// ── Brand colours (match the dark-teal PDF theme) ──────────────────────────
const PRIMARY  = "2D4A5A";   // dark teal header / section titles
const WHITE    = "FFFFFF";
const DARK     = "1A1A1A";   // body text
const GRAY     = "64748B";   // secondary / dates
const LIGHT_BG = "EFF4F7";   // right-sidebar background
const DIVIDER  = "CBD5E1";   // rule under section headings

// ── Unit helpers ───────────────────────────────────────────────────────────
const mm  = (n) => Math.round(n * 36000);  // millimetres → EMU
const pt  = (n) => n * 2;                  // points → half-points (docx size unit)

// ── Invisible table borders ────────────────────────────────────────────────
const NO_BORDER = {
  top:              { style: BorderStyle.NONE, size: 0, color: WHITE },
  bottom:           { style: BorderStyle.NONE, size: 0, color: WHITE },
  left:             { style: BorderStyle.NONE, size: 0, color: WHITE },
  right:            { style: BorderStyle.NONE, size: 0, color: WHITE },
  insideHorizontal: { style: BorderStyle.NONE, size: 0, color: WHITE },
  insideVertical:   { style: BorderStyle.NONE, size: 0, color: WHITE },
};

// ── Bottom-rule border (used under section headings) ──────────────────────
const BOTTOM_RULE = {
  ...NO_BORDER,
  bottom: { style: BorderStyle.SINGLE, size: 6, color: PRIMARY },
};

// ── Shared run defaults ────────────────────────────────────────────────────
const baseRun = (extra = {}) => ({
  font:  "Garamond",
  color: DARK,
  size:  pt(10.5),
  ...extra,
});

// ═══════════════════════════════════════════════════════════════════════════
//  SHARED BLOCK BUILDERS
// ═══════════════════════════════════════════════════════════════════════════

/** Italic, teal, all-caps section heading with bottom rule */
const sectionHeading = (text) =>
  new Paragraph({
    children: [
      new TextRun({
        ...baseRun({
          text:    text.toUpperCase(),
          bold:    true,
          italics: true,
          color:   PRIMARY,
          size:    pt(11),
        }),
      }),
    ],
    border:  BOTTOM_RULE,
    spacing: { before: 240, after: 100 },
  });

/** Small italic secondary label (location, dates, issuer …) */
const secondaryPara = (text, spacingAfter = 40) =>
  new Paragraph({
    children: [
      new TextRun({
        ...baseRun({ text, italics: true, color: GRAY, size: pt(9.5) }),
      }),
    ],
    spacing: { before: 0, after: spacingAfter },
  });

/** Plain body paragraph */
const bodyPara = (text, spacingBefore = 40, spacingAfter = 60) =>
  new Paragraph({
    children: [new TextRun({ ...baseRun({ text }) })],
    spacing: { before: spacingBefore, after: spacingAfter },
  });

/** Bullet list item */
const bulletPara = (text) =>
  new Paragraph({
    children: [new TextRun({ ...baseRun({ text }) })],
    bullet:  { level: 0 },
    spacing: { before: 20, after: 30 },
  });

/** Bold job / project / cert title */
const titlePara = (text) =>
  new Paragraph({
    children: [
      new TextRun({ ...baseRun({ text, bold: true, size: pt(10.5) }) }),
    ],
    spacing: { before: 140, after: 30 },
  });

/** Spacer paragraph */
const spacer = (before = 80) =>
  new Paragraph({ children: [], spacing: { before, after: 0 } });

// ═══════════════════════════════════════════════════════════════════════════
//  LEFT COLUMN  —  Summary · Experience · Projects · Certifications
// ═══════════════════════════════════════════════════════════════════════════
const buildLeft = (d) => {
  const blocks = [];

  // ── Professional Summary ────────────────────────────────────────────────
  if (d.summary) {
    blocks.push(sectionHeading("Professional Summary"));
    blocks.push(bodyPara(d.summary, 60, 180));
  }

  // ── Work History ────────────────────────────────────────────────────────
  const jobs = (d.experience || []).filter((e) => e.title || e.company);
  if (jobs.length > 0) {
    blocks.push(sectionHeading("Work History"));
    jobs.forEach((job) => {
      const titleStr = [job.title, job.company].filter(Boolean).join(" - ");
      blocks.push(titlePara(titleStr));

      if (job.location)
        blocks.push(secondaryPara(job.location, 10));

      const dateStr = [job.startDate, job.endDate || "Present"]
        .filter(Boolean)
        .join(" - ");
      if (dateStr)
        blocks.push(secondaryPara(dateStr, 60));

      if (job.description) {
        job.description
          .split("\n")
          .map((l) => l.replace(/^[•\-]\s*/, "").trim())
          .filter(Boolean)
          .forEach((line) => blocks.push(bulletPara(line)));
      }
    });
    blocks.push(spacer(60));
  }

  // ── Projects ────────────────────────────────────────────────────────────
  const projects = (d.projects || []).filter((p) => p.name || p.title);
  if (projects.length > 0) {
    blocks.push(sectionHeading("Projects"));
    projects.forEach((proj) => {
      blocks.push(titlePara(proj.name || proj.title || ""));

      const tech = proj.technologies || proj.tech || "";
      if (tech)
        blocks.push(
          new Paragraph({
            children: [
              new TextRun({
                ...baseRun({ text: `Tech: ${tech}`, italics: true, color: GRAY, size: pt(9.5) }),
              }),
            ],
            spacing: { before: 0, after: 30 },
          })
        );

      if (proj.description)
        blocks.push(bodyPara(proj.description, 0, 30));

      // link(s)
      const linkRaw = proj.link || proj.links || "";
      const liveLink =
        typeof linkRaw === "object"
          ? linkRaw?.liveLink || linkRaw?.live || ""
          : "";
      const ghLink =
        typeof linkRaw === "object" ? linkRaw?.github || "" : "";
      const plainLink =
        typeof linkRaw === "string" ? linkRaw : "";

      const linkStyle = { ...baseRun({ color: "1D4ED8", size: pt(9.5) }) };
      if (liveLink)
        blocks.push(
          new Paragraph({
            children: [new TextRun({ ...linkStyle, text: `Live: ${liveLink}` })],
            spacing: { before: 0, after: 20 },
          })
        );
      if (ghLink)
        blocks.push(
          new Paragraph({
            children: [new TextRun({ ...linkStyle, text: `GitHub: ${ghLink}` })],
            spacing: { before: 0, after: 20 },
          })
        );
      if (plainLink)
        blocks.push(
          new Paragraph({
            children: [new TextRun({ ...linkStyle, text: plainLink })],
            spacing: { before: 0, after: 20 },
          })
        );
    });
    blocks.push(spacer(60));
  }

  // ── Certifications ──────────────────────────────────────────────────────
  const certs = (d.certifications || []).filter((c) => c.name);
  if (certs.length > 0) {
    blocks.push(sectionHeading("Certifications"));
    certs.forEach((cert) => {
      blocks.push(titlePara(cert.name));
      if (cert.issuer) blocks.push(secondaryPara(cert.issuer, 20));
      if (cert.date)
        blocks.push(
          new Paragraph({
            children: [new TextRun({ ...baseRun({ text: cert.date, color: GRAY, size: pt(9) }) })],
            spacing: { before: 0, after: 20 },
          })
        );
      if (cert.link)
        blocks.push(
          new Paragraph({
            children: [
              new TextRun({ ...baseRun({ text: `Credential Link: ${cert.link}`, color: "1D4ED8", size: pt(9) }) }),
            ],
            spacing: { before: 0, after: 40 },
          })
        );
    });
  }

  // Guarantee at least one child so the cell is never empty
  if (blocks.length === 0) blocks.push(new Paragraph({ children: [] }));
  return blocks;
};

// ═══════════════════════════════════════════════════════════════════════════
//  RIGHT COLUMN (SIDEBAR)  —  Contact · Skills · Education
// ═══════════════════════════════════════════════════════════════════════════
const buildRight = (d) => {
  const blocks = [];

  // ── Contact ─────────────────────────────────────────────────────────────
  const contactFields = [
    d.email,
    d.phone,
    d.location,
    d.linkedin,
    d.website,
    d.github,
  ].filter(Boolean);

  contactFields.forEach((val) =>
    blocks.push(
      new Paragraph({
        children: [new TextRun({ ...baseRun({ text: val, size: pt(9.5) }) })],
        spacing: { before: 20, after: 20 },
      })
    )
  );
  if (contactFields.length > 0) blocks.push(spacer(120));

  // ── Skills ──────────────────────────────────────────────────────────────
  const techSkills = Array.isArray(d.skills) ? d.skills : (d.skills?.technical || []);
  const softSkills = Array.isArray(d.skills) ? [] : (d.skills?.soft || []);
  const allSkills  = [...techSkills, ...softSkills];

  if (allSkills.length > 0) {
    blocks.push(sectionHeading("Skills"));
    allSkills.forEach((skill) =>
      blocks.push(
        new Paragraph({
          children: [new TextRun({ ...baseRun({ text: String(skill), size: pt(9.5) }) })],
          bullet:  { level: 0 },
          spacing: { before: 20, after: 20 },
        })
      )
    );
    blocks.push(spacer(120));
  }

  // ── Education ───────────────────────────────────────────────────────────
  const edu = (d.education || []).filter((e) => e.school);
  if (edu.length > 0) {
    blocks.push(sectionHeading("Education"));
    edu.forEach((e) => {
      blocks.push(
        new Paragraph({
          children: [new TextRun({ ...baseRun({ text: e.school, bold: true, size: pt(10) }) })],
          spacing: { before: 120, after: 20 },
        })
      );
      if (e.location)  blocks.push(secondaryPara(e.location, 20));

      const degreeStr = [e.degree, e.field ? `in ${e.field}` : ""]
        .filter(Boolean)
        .join(" ");
      if (degreeStr)   blocks.push(bodyPara(degreeStr, 0, 20));

      const dateStr = e.graduationDate || e.year || "";
      if (dateStr)
        blocks.push(
          new Paragraph({
            children: [new TextRun({ ...baseRun({ text: dateStr, color: GRAY, size: pt(9) }) })],
            spacing: { before: 0, after: 20 },
          })
        );
      if (e.gpa)
        blocks.push(
          new Paragraph({
            children: [new TextRun({ ...baseRun({ text: `GPA: ${e.gpa}`, color: GRAY, size: pt(9) }) })],
            spacing: { before: 0, after: 40 },
          })
        );
    });
  }

  if (blocks.length === 0) blocks.push(new Paragraph({ children: [] }));
  return blocks;
};

// ═══════════════════════════════════════════════════════════════════════════
//  MAIN EXPORT
// ═══════════════════════════════════════════════════════════════════════════
export const generateResumeDocx = async (formData) => {
  const name     = formData?.fullName || "Resume";
  const initials = name
    .split(/\s+/)
    .map((w) => w[0])
    .join("")
    .toUpperCase()
    .slice(0, 2);

  // ── Header table (full-width dark-teal banner) ───────────────────────────
  const headerTable = new Table({
    width:   { size: 100, type: WidthType.PERCENTAGE },
    borders: NO_BORDER,
    rows: [
      new TableRow({
        height: { value: mm(20), rule: "exact" },
        children: [
          // Initials box
          new TableCell({
            width:         { size: 11, type: WidthType.PERCENTAGE },
            shading:       { fill: PRIMARY, type: ShadingType.SOLID, color: PRIMARY },
            borders:       NO_BORDER,
            verticalAlign: VerticalAlign.CENTER,
            margins:       { top: mm(2), bottom: mm(2), left: mm(4), right: mm(2) },
            children: [
              new Paragraph({
                alignment: AlignmentType.CENTER,
                children: [
                  new TextRun({
                    text:  initials,
                    bold:  true,
                    size:  pt(20),
                    color: WHITE,
                    font:  "Garamond",
                  }),
                ],
              }),
            ],
          }),
          // Name + optional title
          new TableCell({
            width:         { size: 89, type: WidthType.PERCENTAGE },
            shading:       { fill: PRIMARY, type: ShadingType.SOLID, color: PRIMARY },
            borders:       NO_BORDER,
            verticalAlign: VerticalAlign.CENTER,
            margins:       { top: mm(2), bottom: mm(2), left: mm(5), right: mm(4) },
            children: [
              new Paragraph({
                children: [
                  new TextRun({
                    text:    name,
                    bold:    true,
                    italics: true,
                    size:    pt(24),
                    color:   WHITE,
                    font:    "Garamond",
                  }),
                ],
                spacing: { before: 0, after: formData?.title ? 40 : 0 },
              }),
              ...(formData?.title
                ? [
                    new Paragraph({
                      children: [
                        new TextRun({
                          text:  formData.title,
                          size:  pt(11),
                          color: "C4D9E8",
                          font:  "Garamond",
                        }),
                      ],
                    }),
                  ]
                : []),
            ],
          }),
        ],
      }),
    ],
  });

  // ── Body: two-column table ───────────────────────────────────────────────
  const bodyTable = new Table({
    width:   { size: 100, type: WidthType.PERCENTAGE },
    borders: NO_BORDER,
    rows: [
      new TableRow({
        children: [
          // Left main column (~62 %)
          new TableCell({
            width:   { size: 6200, type: WidthType.DXA },
            borders: NO_BORDER,
            margins: { top: mm(6), bottom: mm(6), left: mm(8), right: mm(5) },
            children: buildLeft(formData),
          }),
          // Thin visual divider
          new TableCell({
            width:   { size: 50, type: WidthType.DXA },
            borders: {
              ...NO_BORDER,
              left:  { style: BorderStyle.SINGLE, size: 6, color: DIVIDER },
              right: { style: BorderStyle.SINGLE, size: 6, color: DIVIDER },
            },
            shading: { fill: DIVIDER, type: ShadingType.SOLID, color: DIVIDER },
            children: [new Paragraph({ children: [] })],
          }),
          // Right sidebar column (~38 %)
          new TableCell({
            width:   { size: 3700, type: WidthType.DXA },
            borders: NO_BORDER,
            shading: { fill: LIGHT_BG, type: ShadingType.SOLID, color: LIGHT_BG },
            margins: { top: mm(6), bottom: mm(6), left: mm(5), right: mm(6) },
            children: buildRight(formData),
          }),
        ],
      }),
    ],
  });

  // ── Assemble document ────────────────────────────────────────────────────
  const doc = new Document({
    styles: {
      default: {
        document: {
          run: { font: "Garamond", size: pt(10.5), color: DARK },
        },
      },
    },
    sections: [
      {
        properties: {
          page: {
            margin: { top: mm(0), bottom: mm(10), left: mm(0), right: mm(0) },
          },
        },
        children: [headerTable, bodyTable],
      },
    ],
  });

  const blob = await Packer.toBlob(doc);

  const safeName = name
    .replace(/[^a-z0-9_\- ]/gi, "")
    .trim()
    .replace(/\s+/g, "_") || "Resume";

  saveAs(blob, `${safeName}_Resume.docx`);
};
