import React, { useEffect, useRef, useState } from "react";
import {
  AlertTriangle,
  ArrowLeft,
  ArrowRight,
  Award,
  Briefcase,
  CheckCircle,
  FolderKanban,
  GraduationCap,
  User,
  Zap,
  Search,
  FileText,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../../api/axios";

import FormTabs from "./FormTabs";

import PersonalInfoForm from "./forms/PersonalInfoForm";
import ExperienceForm from "./forms/ExperienceForm";
import EducationForm from "./forms/EducationForm";
import SkillsForm from "./forms/SkillsForm";
import ProjectsForm from "./forms/ProjectsForm";
import CertificationsForm from "./forms/CertificationsForm";

import LivePreview from "../Preview/LivePreview";
import TemplatesPage from "../Templates/TemplatesDashboardPage";
import { TEMPLATES } from "../Templates/TemplateRegistry";

import { getCompletionStatus } from "./completion";
import { dummyData } from "./dummyData";

import UserNavbar from "../UserNavBar/UserNavBar";
import CVBuilderTopBar from "../CV/Cvbuildernavbar";

/* ─────────────────────────────────────────────────────────
   FLOATING FORM PANEL (mirrors CVBuilder behavior)
   Anchors to its container's DOM position so the panel
   stays pinned beneath the sticky navbar while scrolling.
───────────────────────────────────────────────────────── */
const FloatingFormPanel = ({ children, topOffset, containerRef }) => {
  const panelRef = useRef(null);
  const rafRef = useRef(null);
  const currentY = useRef(0);
  const targetY = useRef(0);

  // spring animation loop
  useEffect(() => {
    const STIFFNESS = 0.12;
    const tick = () => {
      currentY.current += (targetY.current - currentY.current) * STIFFNESS;
      if (panelRef.current) {
        panelRef.current.style.transform = `translateY(${currentY.current}px)`;
      }
      rafRef.current = requestAnimationFrame(tick);
    };
    rafRef.current = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(rafRef.current);
  }, []);

  // update target on scroll — anchor to container's top in the DOM
  useEffect(() => {
    const onScroll = () => {
      if (!containerRef?.current || !panelRef?.current) {
        targetY.current = Math.max(0, window.scrollY - topOffset);
        return;
      }
      const containerRect = containerRef.current.getBoundingClientRect();
      const containerTop = containerRect.top + window.scrollY;
      const containerHeight = containerRect.height;
      const panelHeight = panelRef.current.offsetHeight;

      const desired = window.scrollY + topOffset - containerTop;
      const maxDesired = Math.max(0, containerHeight - panelHeight);

      targetY.current = Math.max(0, Math.min(desired, maxDesired));
    };
    window.addEventListener("scroll", onScroll, { passive: true });
    onScroll();
    return () => window.removeEventListener("scroll", onScroll);
  }, [topOffset, containerRef]);

  return (
    <div
      ref={panelRef}
      style={{
        willChange: "transform",
        height: `calc(100vh - ${topOffset}px)`,
      }}
      className="flex flex-col"
    >
      {children}
    </div>
  );
};

const ResumeBuilder = ({ setActivePage = () => {} }) => {
  const headerRef = useRef(null);
  const leftColRef = useRef(null);
  const formContainerRef = useRef(null);
  const [headerHeight, setHeaderHeight] = useState(64);
  /* -------------------- CORE STATE -------------------- */
  // const [formData, setFormData] = useState(dummyData);
  const [formData, setFormData] = useState(() => {
    try {
      const data = localStorage.getItem("resumeFormData");
      return data ? JSON.parse(data) : dummyData;
    } catch {
      return dummyData;
    }
  });

  useEffect(() => {
    const timeout = setTimeout(() => {
      localStorage.setItem("resumeFormData", JSON.stringify(formData));
    }, 400);
    return () => clearTimeout(timeout);
  }, [formData]);

  const navigate = useNavigate();
  const [templates, setTemplates] = useState(TEMPLATES);
  const [selectedTemplate, setSelectedTemplate] = useState(() => {
    const storedTemplate = localStorage.getItem("currentTemplate");
    return storedTemplate
      ? JSON.parse(storedTemplate)
      : TEMPLATES[0]?.id || "jessica-claire";
  });
  useEffect(() => {
    localStorage.setItem("currentTemplate", JSON.stringify(selectedTemplate));
  }, [selectedTemplate]);
  const [templateSearch, setTemplateSearch] = useState("");

  const [activeTab, setActiveTab] = useState("builder");
  const [activeSection, setActiveSection] = useState("personal");
  const [isAiMode, setIsAiMode] = useState(false);
  const [documentTitle, setDocumentTitle] = useState("");
  const [showMobilePreview, setShowMobilePreview] = useState(false);

  /*-----------To make the upload input functional-------------*/

  const input_file = useRef(null);
  const handleButtonClick = () => {
    input_file.current.click();
  };

  const handleFileChange = (e) => {
    const file = e.target.files;
    console.log(file);
  };

  /* -------------------- PREVIEW STATE -------------------- */
  const [isPreviewExpanded, setIsPreviewExpanded] = useState(false);
  const [isPreviewHidden, setIsPreviewHidden] = useState(false);

  /* -------------------- HELPERS -------------------- */
  const handleInputChange = (field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleUseSummary = (text) => {
    setFormData((prev) => ({ ...prev, summary: text }));
  };

  const handleSelectTemplate = (id) => {
    setSelectedTemplate(id);
    setActiveTab("builder");
  };

  const currentTemplate = templates?.find((t) => t.id === selectedTemplate);

  // ============== Completed Status ===========
  const [completion, setcompletion] = useState({});
  useEffect(() => {
    try {
      const statusInfo = getCompletionStatus(formData);
      setcompletion(statusInfo || {});
    } catch (error) {
      console.error("Error getting completion status:", error);
      setcompletion({ isComplete: false, missingSections: [] });
    }
  }, [formData]);

  /* ------------Input Validation ------------- */
  const [warning, setWarning] = useState(false);
  const [showCompletionPopup, setShowCompletionPopup] = useState(false);
  const isSectionValid = () => {
    switch (activeSection) {
      case "personal":
        return (
          formData?.fullName?.trim() &&
          formData?.email?.trim() &&
          formData?.phone?.trim() &&
          formData?.location?.trim()
        );

      case "work":
        return formData?.experience && formData.experience.length > 0;

      case "education":
        return formData?.education && formData.education.length > 0;

      case "skills":
        return formData?.skills && formData.skills.length > 0;

      case "projects":
        return formData?.projects && formData.projects.length > 0;

      case "certs":
        return formData?.certifications && formData.certifications.length > 0;

      default:
        return true;
    }
  };
  const isInputValid = (label) => {
    // For now, allow navigation to all sections regardless of completion status
    // Users can navigate freely and fill sections as needed
    return false; // Always return false to allow navigation
  };

  /*------------------- PREVIOUS & NEXT BUTTON ------------*/
  // PDF Generation
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const previewRef = useRef(null);

  /* Measure sticky navbar height for float offset (same as CV) */
  useEffect(() => {
    const measure = () => {
      if (headerRef.current) setHeaderHeight(headerRef.current.offsetHeight);
    };
    measure();
    const ro = new ResizeObserver(measure);
    if (headerRef.current) ro.observe(headerRef.current);
    return () => ro.disconnect();
  }, [activeTab]);

  /* Lock body scroll when mobile preview sheet is open (mobile only) */
  useEffect(() => {
    document.body.style.overflow = showMobilePreview ? "hidden" : "";
    return () => {
      document.body.style.overflow = "";
    };
  }, [showMobilePreview]);

  const GenerateResumePDF = async (resumeHtml) => {
    try {
      setLoading(true);

      // Create a temporary container to render the HTML
      const container = document.createElement("div");
      Object.assign(container.style, {
        position: "fixed",
        top: "0",
        left: "-9999px",
        width: "794px", // A4 width in pixels at 96 DPI
        background: "#ffffff",
      });
      document.body.appendChild(container);

      // Get the current template component
      const templateComponent = currentTemplate?.component;
      if (!templateComponent) {
        throw new Error("No template component found");
      }

      const { createRoot } = await import("react-dom/client");

      await new Promise((resolve) => {
        const root = createRoot(container);
        root.render(templateComponent({ data: formData }));
        setTimeout(resolve, 400);
      });

      // Use html2canvas and jsPDF like CV builder
      const html2canvas = (await import("html2canvas")).default;
      const jsPDF = (await import("jspdf")).default;

      const canvas = await html2canvas(container, {
        scale: 3,
        useCORS: true,
        windowWidth: 794,
      });

      const pdf = new jsPDF("p", "mm", "a4");
      const mmPageW = 210;
      const mmPageH = 297;
      const marginMm = 30;
      const contentW = mmPageW - 2 * marginMm;
      const contentH = mmPageH - marginMm;
      const pxPerMm = canvas.width / mmPageW;
      const pxContentH = Math.round(contentH * pxPerMm);

      let yPx = 0;
      let firstPage = true;

      while (yPx < canvas.height) {
        const sliceH = Math.min(pxContentH, canvas.height - yPx);
        const pageCanvas = document.createElement("canvas");
        pageCanvas.width = canvas.width;
        pageCanvas.height = pxContentH;
        const ctx = pageCanvas.getContext("2d");
        ctx.fillStyle = "#ffffff";
        ctx.fillRect(0, 0, pageCanvas.width, pageCanvas.height);
        ctx.drawImage(
          canvas,
          0,
          yPx,
          canvas.width,
          sliceH,
          0,
          0,
          canvas.width,
          sliceH,
        );
        const imgData = pageCanvas.toDataURL("image/jpeg", 0.96);
        if (!firstPage) pdf.addPage();
        pdf.addImage(imgData, "JPEG", marginMm, marginMm, contentW, contentH);
        yPx += sliceH;
        firstPage = false;
      }

      const clean = (str) =>
        str
          ?.replace(/[^a-z0-9_\- ]/gi, "")
          .trim()
          .replace(/\s+/g, "_");
      const name =
        clean(documentTitle) || clean(formData?.fullName) || "Resume";
      pdf.save(`${name}.pdf`);

      document.body.removeChild(container);
    } catch (error) {
      console.error("PDF generation failed:", error);
      alert("Failed to generate resume PDF");
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (e) => {
    if (exporting) return;
    try {
      setExporting(true);
      await GenerateResumePDF();

      // Save download record to database
      try {
        const nameToUse = documentTitle || formData.fullName || "Resume";
        await axiosInstance.post("/api/downloads", {
          name: `Resume - ${nameToUse}`,
          type: "resume",
          format: "PDF",
          html: "", // Empty since we're not using HTML anymore
          template: selectedTemplate,
          size: "250 KB",
        });
      } catch (err) {
        console.error("Failed to save resume download:", err);
      }
    } finally {
      setExporting(false);
    }
  };

  const handleDownloadWord = async () => {
    const templateComponent = currentTemplate?.component;
    const templateStyle = currentTemplate?.style;
    if (!templateComponent) {
      alert("No template selected");
      return;
    }

    const container = document.createElement("div");
    Object.assign(container.style, {
      position: "fixed",
      top: "0",
      left: "-9999px",
      width: "794px", // A4 width in pixels at 96 DPI
      background: "#ffffff",
    });
    document.body.appendChild(container);

    try {
      const { createRoot } = await import("react-dom/client");

      await new Promise((resolve) => {
        const root = createRoot(container);
        root.render(templateComponent({ data: formData }));
        setTimeout(resolve, 400);
      });

      const bodyHtml = container.innerHTML;

      // Enhanced Word document with better CSS preservation
      const wordHtml = `
        <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:w="urn:schemas-microsoft-com:office:word" xmlns="http://www.w3.org/TR/REC-html40">
        <head>
          <meta charset="utf-8">
          <title>Resume</title>
          <style>
            @page {
              size: A4;
              margin: 0.5in;
            }
            body {
              font-family: 'Palatino Linotype', 'Georgia', serif;
              color: #4a4a4a;
              line-height: 16px;
              background-color: white;
              margin: 0;
              padding: 0;
              -webkit-print-color-adjust: exact;
              print-color-adjust: exact;
            }

            /* Jessica Claire Template Styles - Word Optimized */
            .jessica-claire-template {
              font-family: 'Palatino Linotype', 'Georgia', serif;
              color: #4a4a4a;
              line-height: 16px;
              background-color: white;
              width: 794px;
              min-height: 1123px;
              margin: 0 auto;
              overflow: hidden;
            }

            .jessica-claire-template * {
              box-sizing: border-box;
            }

            .jessica-claire-template .paddedline {
              font-size: 12px;
              display: block;
            }

            .jessica-claire-template .txtBold {
              font-weight: bold;
            }

            .jessica-claire-template .txtItl {
              font-style: italic;
            }

            .jessica-claire-template .heading {
              padding-bottom: 10px;
              font-weight: bold;
              font-size: 18px;
            }

            .jessica-claire-template .sectiontitle {
              font-family: "Georgia", serif;
              font-style: italic;
              font-size: 18px;
              line-height: 17px;
              letter-spacing: 0.5px;
              text-transform: uppercase;
              color: #3c5769 !important;
            }

            .jessica-claire-template .section {
              border-top: 1px solid #c4c4c4;
              padding-top: 20px;
              margin-bottom: 20px;
            }

            .jessica-claire-template .firstsection {
              border: none;
              background-color: #3c5769 !important;
              color: #fff !important;
              padding: 20px;
              text-align: center;
            }

            .jessica-claire-template .monogram {
              text-align: center;
              margin-bottom: 10px;
            }

            .jessica-claire-template .monogram svg {
              text-transform: uppercase;
            }

            .jessica-claire-template .monogram svg text {
              fill: #ffffff !important;
            }

            .jessica-claire-template .name {
              font-size: 30px;
              line-height: 28px;
              font-weight: 700;
              text-align: center;
              padding-bottom: 5px;
              letter-spacing: 1.5px;
              font-family: "Georgia", serif;
              font-style: italic;
              color: #fff !important;
            }

            .jessica-claire-template .parentContainer {
              display: flex;
              width: 100%;
            }

            .jessica-claire-template .left-box {
              width: 60%;
              padding: 20px;
            }

            .jessica-claire-template .right-box {
              width: 40%;
              background-color: #f5f5f5 !important;
              padding: 20px;
            }

            .jessica-claire-template .paragraph {
              margin-top: 10px;
              font-size: 10px;
              line-height: 15px;
            }

            .jessica-claire-template ul {
              list-style: none;
              padding: 0;
              margin: 5px 0 0 10px;
            }

            .jessica-claire-template li {
              position: relative;
              margin-bottom: 2px;
            }

            .jessica-claire-template li:before {
              content: '●';
              position: absolute;
              left: -12px;
              font-size: 8px;
              top: 4px;
              color: #3c5769 !important;
            }

            .jessica-claire-template .address {
              font-size: 10px;
              line-height: 15px;
            }

            .jessica-claire-template a {
              color: inherit;
              text-decoration: none;
            }

            /* Word-specific fixes */
            * {
              -webkit-print-color-adjust: exact;
              print-color-adjust: exact;
            }
            .page-break {
              page-break-before: always;
            }
            .no-break {
              page-break-inside: avoid;
            }
          </style>
        </head>
        <body>
          ${bodyHtml}
        </body>
        </html>
      `;

      const blob = new Blob(["\uFEFF", wordHtml], {
        type: "application/msword",
      });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      const clean = (s) =>
        (s || "")
          .replace(/[^a-z0-9_\- ]/gi, "")
          .trim()
          .replace(/\s+/g, "_");
      a.download = `${clean(documentTitle) || clean(formData.fullName) || "Resume"}.doc`;
      a.click();
      URL.revokeObjectURL(url);

      // Save download record to database
      try {
        const nameToUse = documentTitle || formData.fullName || "Resume";
        await axiosInstance.post("/api/downloads", {
          name: `Resume - ${nameToUse}`,
          type: "resume",
          format: "DOCX",
          html: bodyHtml,
          template: selectedTemplate,
          size: "200 KB",
        });
      } catch (err) {
        console.error("Failed to save resume download:", err);
      }
    } catch (err) {
      console.error("Word download error:", err);
      alert("Failed to download Word.");
    } finally {
      document.body.removeChild(container);
    }
  };

  // ===============================
  // RESUME UPLOAD HANDLER
  // ===============================
  const handleResumeUpload = async (file) => {
    try {
      if (!file) return;

      const formDataUpload = new FormData();
      formDataUpload.append("resume", file);

      // required backend fields
      formDataUpload.append("jobTitle", "Resume Builder Upload");
      formDataUpload.append("templateId", selectedTemplate);
      formDataUpload.append("resumeprofileId", "000000000000000000000000");

      const res = await axiosInstance.post(
        "/api/resume/upload",
        formDataUpload,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        },
      );

      const parsed = res.data?.data?.extractedData;
      console.log("🔍 Parsed resume data:", parsed);

      if (!parsed) {
        alert("Failed to parse resume.");
        return;
      }

      // Auto-fill builder form - use correct field names from backend
      setFormData((prev) => ({
        ...prev,
        fullName: parsed.fullName || parsed.name || prev.fullName,
        email: parsed.email || prev.email,
        phone: parsed.phone || prev.phone,
        location: parsed.location || prev.location,
        summary: parsed.summary || prev.summary, // This should now work
        linkedin: parsed.linkedin || prev.linkedin,
        website: parsed.website || prev.website,
        education: parsed.education || prev.education,
        experience: parsed.experience || prev.experience,
        projects: parsed.projects || prev.projects,
        skills: parsed.skills || prev.skills,
        certifications: parsed.certifications || prev.certifications,
      }));

      console.log("📝 Summary extracted:", parsed.summary);
      alert("Resume uploaded and imported successfully!");
    } catch (error) {
      console.error("Upload failed:", error);

      // Better error handling for authentication issues
      if (error.response?.status === 401) {
        alert("Authentication required. Please log in again to upload resume.");
        // Optionally redirect to login page
        // window.location.href = "/login";
      } else {
        alert(
          `Resume upload failed: ${error.response?.data?.message || error.message}`,
        );
      }
    }
  };

  /*------------------- PREVIOUS & NEXT BUTTON ------------*/
  const tabs = [
    { id: "personal", label: "Personal", icon: User },
    { id: "education", label: "Education", icon: GraduationCap },
    { id: "work", label: "Work", icon: Briefcase },
    { id: "projects", label: "Projects", icon: FolderKanban },
    { id: "certs", label: "Certifications", icon: Award },
    { id: "skills", label: "Skills", icon: Zap },
  ];
  const currentIdx = tabs.findIndex((tab) => tab.id === activeSection);

  /* Auto-scroll form container to top on section change (like CV) */
  useEffect(() => {
    formContainerRef.current?.scrollTo({ top: 0, behavior: "smooth" });
  }, [activeSection]);

  const goLeft = () => {
    if (currentIdx > 0) {
      setActiveSection(tabs[currentIdx - 1].id);
      setWarning(false);
    }
  };

  const goRight = () => {
    if (currentIdx < tabs.length - 1) {
      setActiveSection(tabs[currentIdx + 1].id);
    }
  };

  /* -------------------- FORM RENDER -------------------- */
  const renderFormContent = () => {
    switch (activeSection) {
      case "personal":
        return (
          <PersonalInfoForm
            formData={formData}
            onInputChange={handleInputChange}
            onUseSummary={handleUseSummary}
          />
        );
      case "work":
        return <ExperienceForm formData={formData} setFormData={setFormData} />;
      case "education":
        return <EducationForm formData={formData} setFormData={setFormData} />;
      case "skills":
        return <SkillsForm formData={formData} setFormData={setFormData} />;
      case "projects":
        return <ProjectsForm formData={formData} setFormData={setFormData} />;
      case "certs":
        return (
          <CertificationsForm formData={formData} setFormData={setFormData} />
        );
      default:
        return null;
    }
  };

  /* -------------------- MAIN CONTENT -------------------- */
  const renderMainContent = () => {
    if (activeTab === "templates") {
      return (
        <TemplatesPage
          onSelectTemplate={handleSelectTemplate}
          isEmbedded={true}
          externalSearchTerm={templateSearch}
        />
      );
    }

    // BUILDER TAB – mirror CV layout with floating form + desktop preview
    return (
      <>
        {completion?.isComplete ? (
          <div className="px-4 mt-2">
            <div className="flex gap-3 p-3 bg-emerald-50 border border-emerald-200 rounded-xl shadow-sm px-2">
              <CheckCircle
                className="text-emerald-500 flex-shrink-0 mt-0.5"
                size={18}
              />
              <span className="text-sm font-medium text-emerald-800">
                Resume Ready: All necessary information has been added. You can
                now export your resume.
              </span>
            </div>
          </div>
        ) : (
          <div className="px-4 mt-2">
            <div className="flex gap-3 p-3 bg-amber-50 border border-amber-200 rounded-xl shadow-sm px-2">
              <AlertTriangle
                className="text-amber-500 flex-shrink-0 mt-0.5"
                size={18}
              />
              <span className="text-sm font-medium text-amber-800">
                Complete Your Resume: Add the missing information to enable
                export functionality.
              </span>
            </div>
          </div>
        )}
        <div className="flex gap-5 px-4 pb-20 pt-4 items-start">
          {/* Desktop floating form panel */}
          {!isPreviewExpanded && (
            <div
              ref={leftColRef}
              className="flex-shrink-0 hidden lg:block self-stretch"
              style={{ width: 480 }}
            >
              <FloatingFormPanel
                topOffset={headerHeight}
                containerRef={leftColRef}
              >
                <div
                  className="bg-white rounded-2xl flex flex-col overflow-hidden"
                  style={{
                    height: "100%",
                    boxShadow:
                      "0 4px 24px rgba(0,0,0,0.07), 0 1px 4px rgba(0,0,0,0.04)",
                  }}
                >
                  {/* Tabs + step info */}
                  <div className="flex-shrink-0 border-b border-slate-100 px-4 py-3 bg-white rounded-t-2xl">
                    <FormTabs
                      activeSection={activeSection}
                      setActiveSection={setActiveSection}
                      showPreview={showMobilePreview}
                      onTogglePreview={() => setShowMobilePreview((v) => !v)}
                    />
                  </div>

                  {/* Scrollable form content */}
                  <div
                    ref={formContainerRef}
                    className="flex-1 overflow-y-auto p-4 pb-0"
                    style={{
                      scrollbarWidth: "thin",
                      scrollbarColor: "#e2e8f0 transparent",
                    }}
                  >
                    {/* Validation warning */}
                    {warning && (
                      <div className="text-sm text-red-700 bg-yellow-100 border border-yellow-300 px-4 py-2 mb-3 rounded-lg">
                        Please fill in all required fields to continue.
                      </div>
                    )}

                    {renderFormContent()}
                  </div>

                  {/* Previous & Next Desktop */}
                  <div className="flex-shrink-0 flex items-center justify-between p-4 border-t border-slate-100 bg-white rounded-b-2xl">
                    <button
                      onClick={goLeft}
                      disabled={currentIdx === 0}
                      className="flex gap-2 items-center text-sm font-medium text-slate-600 hover:text-slate-900 bg-white border border-slate-200 hover:bg-slate-50 px-5 py-2.5 rounded-lg select-none disabled:opacity-40 disabled:cursor-not-allowed transition-all shadow-sm"
                    >
                      <ArrowLeft size={16} />
                      <span className="hidden sm:inline">Previous</span>
                    </button>
                    <button
                      onClick={() => {
                        if (completion?.isComplete) {
                          setShowCompletionPopup(true);
                        } else {
                          if (!isSectionValid()) {
                            setWarning(true);
                            window.scrollTo({ top: 0, behavior: "smooth" });
                            return;
                          }
                          setWarning(false);
                          goRight();
                        }
                      }}
                      disabled={
                        !completion?.isComplete &&
                        currentIdx === tabs.length - 1
                      }
                      className="flex gap-2 items-center text-sm font-medium bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-lg select-none disabled:opacity-40 disabled:cursor-not-allowed transition-all shadow-sm"
                    >
                      <span className="hidden sm:inline">
                        {completion?.isComplete ? "Finish" : "Next Step"}
                      </span>
                      <ArrowRight size={16} />
                    </button>
                  </div>
                </div>
              </FloatingFormPanel>
            </div>
          )}

          {/* Mobile form card (no desktop preview here) */}
          <div className="w-full lg:hidden flex flex-col">
            <div className="bg-white rounded-2xl shadow-sm border border-slate-100 flex flex-col overflow-hidden mb-4">
              <div className="flex-shrink-0">
                <FormTabs
                  activeSection={activeSection}
                  setActiveSection={setActiveSection}
                  showPreview={showMobilePreview}
                  onTogglePreview={() => setShowMobilePreview((v) => !v)}
                />
              </div>
              <div className="flex-1 min-h-[400px] overflow-y-auto p-4 pb-0">
                {warning && (
                  <div className="text-sm text-red-700 bg-yellow-100 border border-yellow-300 px-4 py-2 mb-3 rounded-lg">
                    Please fill in all required fields to continue.
                  </div>
                )}

                {renderFormContent()}
              </div>

              <div className="flex-shrink-0 flex items-center justify-between p-4 border-t border-slate-100 bg-white">
                <button
                  onClick={goLeft}
                  disabled={currentIdx === 0}
                  className="flex gap-2 items-center text-sm font-medium text-slate-600 hover:text-slate-900 bg-white border border-slate-200 hover:bg-slate-50 px-4 py-2 rounded-lg select-none disabled:opacity-40 disabled:cursor-not-allowed transition-all shadow-sm"
                >
                  <ArrowLeft size={16} />
                  <span>Previous</span>
                </button>
                <button
                  onClick={() => {
                    if (completion?.isComplete) {
                      setShowCompletionPopup(true);
                    } else {
                      if (!isSectionValid()) {
                        setWarning(true);
                        formContainerRef.current?.scrollTo({
                          top: 0,
                          behavior: "smooth",
                        });
                        return;
                      }
                      setWarning(false);
                      goRight();
                    }
                  }}
                  disabled={
                    !completion?.isComplete && currentIdx === tabs.length - 1
                  }
                  className="flex gap-2 items-center text-sm font-medium bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded-lg select-none disabled:opacity-40 disabled:cursor-not-allowed transition-all shadow-sm"
                >
                  <span>{completion?.isComplete ? "Finish" : "Next"}</span>
                  <ArrowRight size={16} />
                </button>
              </div>
            </div>
          </div>

          {/* Desktop preview panel */}
          {!isPreviewHidden && !isPreviewExpanded && (
            <div className="hidden lg:flex flex-1 flex-col min-w-0">
              <div
                className="rounded-2xl overflow-hidden border border-slate-100 bg-white"
                style={{
                  minHeight: "calc(100vh - 80px)",
                  boxShadow:
                    "0 4px 24px rgba(0,0,0,0.07), 0 1px 4px rgba(0,0,0,0.04)",
                }}
              >
                <LivePreview
                  ref={previewRef}
                  formData={formData}
                  currentTemplate={currentTemplate}
                  isExpanded={false}
                  onExpand={() => setIsPreviewExpanded(true)}
                  onCollapse={() => setIsPreviewExpanded(false)}
                  onMinimize={() => setIsPreviewHidden(true)}
                />
              </div>
            </div>
          )}
        </div>
        <div className="w-full h-4" />
      </>
    );
  };

  return (
    <div className="min-h-screen bg-[#f1f3f6] font-sans tracking-[0.01em]">
      {/* Sticky navbar like CV */}
      {!isPreviewExpanded && (
        <div ref={headerRef} className="sticky top-0 z-30 bg-[#f1f3f6]">
          <UserNavbar />
        </div>
      )}

      {/* Full-screen preview overlay (existing behavior) */}
      {isPreviewExpanded && (
        <div className="fixed inset-0 z-[99999] bg-white overflow-auto">
          <LivePreview
            ref={previewRef}
            formData={formData}
            currentTemplate={currentTemplate}
            isExpanded={true}
            onExpand={() => {}}
            onCollapse={() => setIsPreviewExpanded(false)}
            onMinimize={() => setIsPreviewHidden(true)}
          />
        </div>
      )}

      {/* Top builder bar */}
      <CVBuilderTopBar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onDownload={handleDownload}
        onDownloadWord={handleDownloadWord}
        onUpload={handleResumeUpload}
        isDownloading={loading}
        downloadDisabled={false} // Allow downloads regardless of completion status
        title={documentTitle}
        onTitleChange={(_, val) => setDocumentTitle(val)}
        titlePlaceholder="Untitled Resume"
        templatesLabel="Resume Templates"
        showDesigner={false}
        showAiToggle={true}
        isAiMode={isAiMode}
        onToggleAiMode={() => setIsAiMode((v) => !v)}
        extraButtons={
          <button
            onClick={() => navigate("/user/cover-letter")}
            className="hidden lg:flex items-center gap-2 px-4 py-2 rounded-lg border border-gray-300 bg-white text-gray-800 font-medium shadow-sm hover:bg-black hover:text-white transition-all duration-200 whitespace-nowrap select-none"
          >
            <FileText size={18} />
            Create Cover Letter
          </button>
        }
      />

      <div className="p-2.5 overflow-hidden">
        {activeTab !== "builder" && (
          <div className="relative w-full md:w-80 mb-4 px-3">
            <Search
              className="absolute left-6 top-1/2 -translate-y-1/2 text-slate-400"
              size={18}
            />
            <input
              type="text"
              placeholder="Search templates..."
              value={templateSearch}
              onChange={(e) => setTemplateSearch(e.target.value)}
              className="pl-10 pr-4 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none w-full shadow-sm"
            />
          </div>
        )}

        {renderMainContent()}

        {/* Mobile slide-up preview overlay (mirrors CV & Cover Letter) */}
        {showMobilePreview && !isPreviewExpanded && (
          <div className="lg:hidden fixed inset-0 z-50 flex flex-col">
            <div
              className="absolute inset-0 bg-black/50 backdrop-blur-sm"
              onClick={() => setShowMobilePreview(false)}
            />
            <div
              className="relative mt-auto bg-white rounded-t-2xl shadow-2xl flex flex-col"
              style={{
                height: "92dvh",
                animation:
                  "resumePreviewSlideUp 0.3s cubic-bezier(0.32,0.72,0,1)",
              }}
            >
              <div className="flex justify-center pt-3 pb-1 flex-shrink-0">
                <div className="w-10 h-1 rounded-full bg-slate-300" />
              </div>
              <div className="flex items-center justify-between px-4 pb-2 flex-shrink-0">
                <span className="text-sm font-semibold text-slate-700">
                  Resume Preview
                </span>
                <button
                  onClick={() => setShowMobilePreview(false)}
                  className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-500 transition-colors"
                >
                  <span className="text-base leading-none">×</span>
                </button>
              </div>
              <div className="flex-1 min-h-0 overflow-hidden">
                <LivePreview
                  ref={previewRef}
                  formData={formData}
                  currentTemplate={currentTemplate}
                  isExpanded={false}
                  onExpand={() => {}}
                  onCollapse={() => {}}
                  onMinimize={() => setShowMobilePreview(false)}
                />
              </div>
            </div>
          </div>
        )}

        <footer className="mt-auto text-center py-4 bg-white border-t text-sm text-gray-600">
          © {new Date().getFullYear()} ResumeAI Inc. All rights reserved.
        </footer>
      </div>

      <style>{`
        @keyframes resumePreviewSlideUp {
          from { transform: translateY(100%); opacity: 0.5; }
          to   { transform: translateY(0);    opacity: 1;   }
        }
      `}</style>

      {/* Completion Popup */}
      {showCompletionPopup && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-2xl p-8 max-w-md mx-4 shadow-2xl">
            <div className="text-center">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg
                  className="w-8 h-8 text-green-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
              <h3 className="text-2xl font-bold text-gray-900 mb-2">
                Resume Complete!
              </h3>
              <p className="text-gray-600 mb-6">
                Your resume has been successfully completed with all required
                information. You can now download or preview your resume.
              </p>
              <div className="flex gap-3 justify-center">
                <button
                  onClick={() => setShowCompletionPopup(false)}
                  className="px-6 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors font-medium"
                >
                  Continue Editing
                </button>
                <button
                  onClick={() => {
                    setShowCompletionPopup(false);
                    // Navigate to templates or download
                    setActiveTab("templates");
                  }}
                  className="px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
                >
                  View Templates
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ResumeBuilder;
