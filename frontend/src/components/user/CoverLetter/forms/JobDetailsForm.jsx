import { useState, useEffect } from "react";
import { Briefcase, RefreshCw, Sparkles } from "lucide-react";
import { aiService } from "../../../../services/aiService";

const JobDetailsForm = ({ formData, onInputChange, aiTone }) => {
  const whereFoundOptions = [
    "Company Website",
    "LinkedIn",
    "Indeed",
    "Glassdoor",
    "Referral",
    "Job Fair",
    "Recruiter",
    "Other",
  ];

  const [localData, setLocalData] = useState({
    jobTitle: "",
    jobReference: "",
    whereFound: "",
    jobDescription: "",
    skills: "",
    experience: "",
  });
  const [isEnhancing, setIsEnhancing] = useState(false);
  const [aiError, setAiError] = useState("");
  const [isAutoUpdating, setIsAutoUpdating] = useState(false);
  const [lastAutoSignature, setLastAutoSignature] = useState("");

  const handleEnhanceJobDescription = async () => {
    if (!localData.jobTitle?.trim()) {
      setAiError("Please enter a job role to generate job description.");
      return;
    }
    try {
      setIsEnhancing(true);
      setAiError("");
      const response = await aiService.generateCoverLetter({
        sectionType: "jobDescription",
        tone: aiTone,
        jobTitle: localData.jobTitle || "Role",
        companyName: formData.companyName || "Company",
        fullName: formData.fullName || "Candidate",
        skills: localData.skills || "",
        experience: localData.experience || "",
        jobDescription: localData.jobDescription,
      });
      handleChange(
        "jobDescription",
        response.jobDescription || localData.jobDescription,
      );
      setLastAutoSignature(`${localData.jobTitle}||${formData.companyName}`);
    } catch (error) {
      console.error("Error enhancing job description:", error);
      setAiError(aiService.getErrorMessage(error));
    } finally {
      setIsEnhancing(false);
    }
  };

  useEffect(() => {
    setLocalData({
      jobTitle: formData.jobTitle || "",
      jobReference: formData.jobReference || "",
      whereFound: formData.whereFound || "",
      jobDescription: formData.jobDescription || "",
      skills: formData.skills || "",
      experience: formData.experience || "",
    });
  }, [formData]);

  const handleChange = (field, value) => {
    const safeValue = value || "";
    setLocalData((prev) => ({ ...prev, [field]: safeValue }));
    onInputChange(field, safeValue);
  };

  // Auto-generate job description when key inputs change and the field is empty or AI-controlled.
  useEffect(() => {
    if (
      !localData.jobTitle?.trim() ||
      !formData.companyName?.trim() ||
      !formData.fullName?.trim()
    ) {
      return;
    }

    const signature = `${localData.jobTitle}||${formData.companyName}`;
    const shouldAutofill =
      isAutoUpdating ||
      !localData.jobDescription?.trim() ||
      signature !== lastAutoSignature;

    if (!shouldAutofill) return;

    const controller = new AbortController();
    const timer = setTimeout(async () => {
      try {
        setIsAutoUpdating(true);
        setAiError("");
        const response = await aiService.generateCoverLetter({
          sectionType: "jobDescription",
          tone: aiTone,
          jobTitle: localData.jobTitle,
          companyName: formData.companyName,
          fullName: formData.fullName,
          skills: localData.skills || "",
          experience: localData.experience || "",
          jobDescription: localData.jobDescription,
        });
        if (!controller.signal.aborted) {
          handleChange(
            "jobDescription",
            response.jobDescription || localData.jobDescription,
          );
          setLastAutoSignature(signature);
        }
      } catch (error) {
        if (!controller.signal.aborted) {
          setAiError(aiService.getErrorMessage(error));
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsAutoUpdating(false);
        }
      }
    }, 500);

    return () => {
      controller.abort();
      clearTimeout(timer);
    };
  }, [
    localData.jobTitle,
    formData.companyName,
    formData.fullName,
    aiTone,
    localData.skills,
    localData.experience,
    localData.jobDescription,
    lastAutoSignature,
  ]);

  return (
    <div className="p-2 animate-in fade-in duration-300">
      <div className="flex items-center gap-2 mb-6 border-b border-slate-100 pb-4">
        <Briefcase className="text-blue-600" size={20} />
        <h3 className="text-lg font-bold text-slate-800">Job Details</h3>
      </div>

      <p className="text-sm text-slate-500 mb-5">
        Provide details about the position you're applying for.
      </p>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-x-5 gap-y-4">
        <div className="flex flex-col gap-1.5">
          <label className="block text-sm font-semibold text-slate-700">
            Job Title <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            placeholder="Software Engineer"
            className="w-full px-3.5 py-2.5 border border-slate-200 rounded-lg text-sm text-slate-900 focus:outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-600/10 transition-all bg-white"
            value={localData.jobTitle}
            onChange={(e) => handleChange("jobTitle", e.target.value)}
          />
        </div>

        <div className="flex flex-col gap-1.5">
          <label className="block text-sm font-semibold text-slate-700">
            Job Reference Number
          </label>
          <input
            type="text"
            placeholder="REF-12345"
            className="w-full px-3.5 py-2.5 border border-slate-200 rounded-lg text-sm text-slate-900 focus:outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-600/10 transition-all bg-white"
            value={localData.jobReference}
            onChange={(e) => handleChange("jobReference", e.target.value)}
          />
          <small className="text-xs text-slate-400">
            If provided in the job listing
          </small>
        </div>

        <div className="flex flex-col gap-1.5 md:col-span-2">
          <label className="block text-sm font-semibold text-slate-700">
            Where did you find this job?
          </label>
          <select
            className="w-full px-3.5 py-2.5 border border-slate-200 rounded-lg text-sm text-slate-900 focus:outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-600/10 transition-all bg-white"
            value={localData.whereFound}
            onChange={(e) => handleChange("whereFound", e.target.value)}
          >
            <option value="">Select an option</option>
            {whereFoundOptions.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="flex flex-col gap-1.5 mt-6">
        <div className="w-full flex items-center justify-between mb-1">
          <label className="text-sm font-semibold text-slate-700">
            Job Description{" "}
            <span className="text-slate-400 font-normal">(Optional)</span>
          </label>
          <button
            className="flex gap-2 ml-2 p-2 rounded-lg text-xs bg-blue-100 text-blue-600 hover:bg-blue-200 hover:text-blue-800 whitespace-nowrap shrink-0"
            onClick={handleEnhanceJobDescription}
            disabled={isEnhancing}
          >
            {isEnhancing ? (
              <RefreshCw size={15} className="ml-1 animate-spin" />
            ) : (
              <Sparkles size={14} />
            )}
            Enhance with AI
          </button>
        </div>
        <textarea
          placeholder="Paste job description for better AI suggestions..."
          className="w-full px-4 py-3 border border-slate-200 rounded-lg text-sm text-slate-900 focus:outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-600/10 transition-all bg-white resize-y min-h-[140px] leading-relaxed"
          value={localData.jobDescription}
          onChange={(e) => handleChange("jobDescription", e.target.value)}
          rows={6}
        />
        <div className="flex items-center justify-between text-xs text-slate-500 mt-1">
          <span>{isAutoUpdating ? "Updating with AI..." : ""}</span>
          {aiError && (
            <span className="text-red-500 font-medium">{aiError}</span>
          )}
        </div>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-x-5 gap-y-4 mt-6">
        <div className="flex flex-col gap-1.5">
          <label className="block text-sm font-semibold text-slate-700">
            Key Skills
          </label>
          <textarea
            placeholder="Example: React, Java, SQL, stakeholder communication"
            className="w-full px-4 py-3 border border-slate-200 rounded-lg text-sm text-slate-900 focus:outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-600/10 transition-all bg-white resize-y min-h-[110px] leading-relaxed"
            value={localData.skills}
            onChange={(e) => handleChange("skills", e.target.value)}
            rows={4}
          />
        </div>
        <div className="flex flex-col gap-1.5">
          <label className="block text-sm font-semibold text-slate-700">
            Relevant Experience
          </label>
          <textarea
            placeholder="Summarize the experience you want the cover letter to emphasize"
            className="w-full px-4 py-3 border border-slate-200 rounded-lg text-sm text-slate-900 focus:outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-600/10 transition-all bg-white resize-y min-h-[110px] leading-relaxed"
            value={localData.experience}
            onChange={(e) => handleChange("experience", e.target.value)}
            rows={4}
          />
        </div>
      </div>
      {aiError && (
        <p className="mt-3 text-xs text-red-500 font-medium">{aiError}</p>
      )}
    </div>
  );
};

export default JobDetailsForm;
