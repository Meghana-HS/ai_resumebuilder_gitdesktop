import { useState } from "react";
import { Sparkles, RefreshCw, Copy, Check, FileText } from "lucide-react";
import { aiService } from "../../../../services/aiService";

const BodyContentForm = ({ formData, onInputChange, aiTone, onToneChange }) => {
  const [generating, setGenerating] = useState({});
  const [copied, setCopied] = useState({});
  const [aiError, setAiError] = useState("");

  const handleGenerate = async (field) => {
    setGenerating((prev) => ({ ...prev, [field]: true }));

    try {
<<<<<<< HEAD
      const token = localStorage.getItem("token");
      const response = await axiosInstance.post(
        "/api/resume/cover-letter/generate",
        {
          sectionType: field,
          jobDetails: {
            jobTitle: formData.jobTitle || "Role",
            companyName: formData.companyName || "Company",
            fullName: formData.fullName || "Candidate",
            skills: formData.skills || "",
            experience: formData.experience || "",
          },
        },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        },
      );

      onInputChange(field, response.data.result);
    } catch (error) {
      console.error("Error generating content:", error);
      alert("Error processing request.");
=======
      setAiError("");
      const response = await aiService.generateCoverLetter({
        sectionType: field,
        tone: aiTone,
        jobTitle: formData.jobTitle || "Role",
        companyName: formData.companyName || "Company",
        recipientName: formData.recipientName,
        fullName: formData.fullName || "Candidate",
        skills: formData.skills || "",
        experience: formData.experience || "",
        jobDescription: formData.jobDescription || "",
      });

      onInputChange(field, response?.[field] || "");
    } catch (error) {
      console.error("Error generating content:", error);
      setAiError(aiService.getErrorMessage(error));
>>>>>>> 26bcd11 (userside)
    } finally {
      setGenerating((prev) => ({ ...prev, [field]: false }));
    }
  };

  const handleGenerateFullLetter = async () => {
    setGenerating((prev) => ({ ...prev, fullLetter: true }));

    try {
      setAiError("");
      const response = await aiService.generateCoverLetter({
        tone: aiTone,
        jobTitle: formData.jobTitle || "Role",
        companyName: formData.companyName || "Company",
        recipientName: formData.recipientName,
        fullName: formData.fullName || "Candidate",
        skills: formData.skills || "",
        experience: formData.experience || "",
        jobDescription: formData.jobDescription || "",
      });

      onInputChange("openingParagraph", response?.openingParagraph || "");
      onInputChange("bodyParagraph1", response?.bodyParagraph1 || "");
      onInputChange("bodyParagraph2", response?.bodyParagraph2 || "");
      onInputChange("closingParagraph", response?.closingParagraph || "");
    } catch (error) {
      setAiError(aiService.getErrorMessage(error));
    } finally {
      setGenerating((prev) => ({ ...prev, fullLetter: false }));
    }
  };

  const handleCopy = (field) => {
    navigator.clipboard.writeText(formData[field] || "");
    setCopied((prev) => ({ ...prev, [field]: true }));
    setTimeout(() => setCopied((prev) => ({ ...prev, [field]: false })), 2000);
  };

  const renderTextArea = (field, label, placeholder, rows = 4) => (
    <div className="flex flex-col gap-2 mb-5">
      <div className="flex items-center justify-between">
        <label className="text-sm font-semibold text-slate-700">{label}</label>
        <div className="flex gap-2">
          <button
            className="flex gap-1.5 items-center px-3 py-1.5 rounded-lg text-xs font-medium bg-blue-50 text-blue-600 hover:bg-blue-100 transition-colors disabled:opacity-50 whitespace-nowrap shrink-0"
            onClick={() => handleGenerate(field)}
            disabled={generating[field]}
          >
            {generating[field] ? (
              <>
                <RefreshCw size={14} className="animate-spin" /> Generating...
              </>
            ) : (
              <>
                <Sparkles size={14} /> Enhance with AI
              </>
            )}
          </button>
          <button
            className="flex items-center justify-center w-8 h-8 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors disabled:opacity-30"
            onClick={() => handleCopy(field)}
            disabled={!formData[field]}
          >
            {copied[field] ? <Check size={14} className="text-green-500" /> : <Copy size={14} />}
          </button>
        </div>
      </div>
      <textarea
        placeholder={placeholder}
        value={formData[field]}
        onChange={(e) => onInputChange(field, e.target.value)}
        rows={rows}
        className="w-full px-4 py-3 border border-slate-200 rounded-lg text-sm text-slate-900 focus:outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-600/10 transition-all bg-white resize-y min-h-[100px] leading-relaxed"
      />
    </div>
  );

  return (
    <div className="p-2 animate-in fade-in duration-300">
      <div className="flex items-center gap-2 mb-6 border-b border-slate-100 pb-4">
        <FileText className="text-blue-600" size={20} />
        <h3 className="text-lg font-bold text-slate-800">Letter Content</h3>
      </div>

      <p className="text-sm text-slate-500 mb-4">
        Write your cover letter content below or use AI to generate compelling paragraphs.
      </p>

      <div className="flex items-start gap-3 p-3 bg-amber-50 border border-amber-200 rounded-lg mb-6 text-sm">
        <span className="text-lg leading-none">Idea</span>
        <div className="text-slate-700">
          <strong>Pro Tip:</strong> A great cover letter has 3-4 paragraphs: an
          engaging opening, 1-2 body paragraphs highlighting your relevant
          experience, and a strong closing.
        </div>
      </div>

      <div className="mb-6 rounded-lg border border-slate-200 bg-slate-50 p-4">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-slate-700">Tone</label>
            <select
              value={aiTone}
              onChange={(e) => onToneChange(e.target.value)}
              className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900"
            >
              <option value="professional">Professional</option>
              <option value="formal">Formal</option>
              <option value="creative">Creative</option>
            </select>
          </div>
          <button
            type="button"
            onClick={handleGenerateFullLetter}
            disabled={generating.fullLetter}
            className="inline-flex items-center justify-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {generating.fullLetter ? (
              <>
                <RefreshCw size={14} className="animate-spin" />
                Generating...
              </>
            ) : (
              <>
                <Sparkles size={14} />
                Generate Full Letter
              </>
            )}
          </button>
        </div>
      </div>

      {renderTextArea(
        "openingParagraph",
        "Opening Paragraph *",
        "Start with a strong hook that mentions the specific position and company. Express your enthusiasm and briefly mention why you're a great fit...",
        4,
      )}

      {renderTextArea(
        "bodyParagraph1",
        "Body Paragraph 1 - Key Qualifications *",
        "Highlight your most relevant experience and achievements. Use specific examples and quantifiable results when possible...",
        5,
      )}

      {renderTextArea(
        "bodyParagraph2",
        "Body Paragraph 2 - Additional Value (Optional)",
        "Add more relevant skills, experiences, or explain why you're passionate about the company or industry...",
        5,
      )}

      {renderTextArea(
        "closingParagraph",
        "Closing Paragraph *",
        "Summarize your interest, express enthusiasm for an interview, and thank them for their consideration...",
        4,
      )}

      <div className="flex items-center justify-between px-3 py-2 bg-slate-50 rounded-lg text-sm text-slate-500 border border-slate-100">
        <span>
          Total Words:{" "}
          {
            [
              formData.openingParagraph,
              formData.bodyParagraph1,
              formData.bodyParagraph2,
              formData.closingParagraph,
            ]
              .filter(Boolean)
              .join(" ")
              .split(/\s+/)
              .filter(Boolean).length
          }
        </span>
        <span className="text-blue-600 font-medium">Ideal: 250-400 words</span>
      </div>
      {aiError && <p className="mt-3 text-xs text-red-500 font-medium">{aiError}</p>}
    </div>
  );
};

export default BodyContentForm;
