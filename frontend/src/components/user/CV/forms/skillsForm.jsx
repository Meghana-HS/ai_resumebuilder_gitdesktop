import { Plus, X } from "lucide-react";
import { useState } from "react";
import { aiService } from "../../../../services/aiService";

const SkillsForm = ({ formData, setFormData }) => {
  const [newSkill, setNewSkill] = useState("");
  const [skillType, setSkillType] = useState("technical");
  const [suggestedSkills, setSuggestedSkills] = useState({
    technicalSkills: ["JavaScript", "React", "Node.js", "Python", "SQL", "AWS"],
    softSkills: ["Leadership", "Communication", "Problem Solving", "Teamwork"],
    keywords: [],
    atsTips: [],
  });
  const [isLoadingSuggestions, setIsLoadingSuggestions] = useState(false);
  const [aiError, setAiError] = useState("");

  const addSkill = () => {
    if (!newSkill.trim()) return;

    setFormData((prev) => ({
      ...prev,
      skills: {
        ...(prev?.skills ?? { technical: [], soft: [] }),
        [skillType]: [...(prev?.skills?.[skillType] ?? []), newSkill.trim()],
      },
    }));

    setNewSkill("");
  };

  const removeSkill = (type, index) => {
    setFormData((prev) => ({
      ...prev,
      skills: {
        ...(prev?.skills ?? { technical: [], soft: [] }),
        [type]: (prev?.skills?.[type] ?? []).filter((_, i) => i !== index),
      },
    }));
  };

  const addSuggestedSkill = (skill) => {
    if (!(formData?.skills?.[skillType] ?? []).includes(skill)) {
      setFormData((prev) => ({
        ...prev,
        skills: {
          ...(prev?.skills ?? { technical: [], soft: [] }),
          [skillType]: [...(prev?.skills?.[skillType] ?? []), skill],
        },
      }));
    }
  };

  const visibleSuggestedSkills =
    skillType === "technical"
      ? suggestedSkills.technicalSkills
      : suggestedSkills.softSkills;

  const loadAiSuggestions = async () => {
    try {
      setIsLoadingSuggestions(true);
      setAiError("");
      const response = await aiService.suggestResumeSkills({
        fullName: formData?.fullName,
        summary: formData?.summary,
        jobTitle: formData?.targetRole,
        skills: formData?.skills,
        experience: formData?.experience,
        projects: formData?.projects,
      });
      setSuggestedSkills((prev) => ({
        technicalSkills:
          response?.technicalSkills?.length > 0
            ? response.technicalSkills
            : prev.technicalSkills,
        softSkills:
          response?.softSkills?.length > 0
            ? response.softSkills
            : prev.softSkills,
        keywords: response?.keywords ?? [],
        atsTips: response?.atsTips ?? [],
      }));
    } catch (error) {
      setAiError(aiService.getErrorMessage(error));
    } finally {
      setIsLoadingSuggestions(false);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      {/* ===== Toggle Buttons ===== */}
      <div className="flex gap-2 p-3 rounded-xl bg-slate-900 w-fit mx-auto">
        <button
          onClick={() => setSkillType("technical")}
          className={`px-5 py-2 rounded-lg text-sm font-medium transition-all duration-300
            ${
              skillType === "technical"
                ? "bg-white text-slate-900 shadow-md scale-105"
                : "bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white"
            }`}
        >
          Technical Skills
        </button>

        <button
          onClick={() => setSkillType("soft")}
          className={`px-5 py-2 rounded-lg text-sm font-medium transition-all duration-300
            ${
              skillType === "soft"
                ? "bg-white text-slate-900 shadow-md scale-105"
                : "bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white"
            }`}
        >
          Soft Skills
        </button>
      </div>

      {/* ===== Add Skill Input ===== */}
      <div className="flex gap-2 px-2">
        <input
          type="text"
          value={newSkill}
          placeholder={`Add a ${skillType} skill...`}
          onChange={(e) => setNewSkill(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") addSkill();
          }}
          className="border w-full p-2 rounded-lg outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-400"
        />

        <button
          onClick={addSkill}
          className="bg-black text-white px-4 rounded-lg hover:bg-black/80 transition"
        >
          Add
        </button>
      </div>

      {/* ===== Skills List ===== */}
      <div className="flex flex-wrap gap-2 px-2">
        {(formData?.skills?.[skillType] ?? []).map((skill, idx) => (
          <span
            key={idx}
            className="inline-flex items-center gap-2 bg-blue-200 text-blue-700 text-sm px-3 py-1 rounded-xl"
          >
            {skill}
            <button onClick={() => removeSkill(skillType, idx)}>
              <X size={14} className="hover:text-red-500 transition" />
            </button>
          </span>
        ))}
      </div>

      {/* ===== Suggested Skills ===== */}
      <div className="px-2">
        <div className="mb-2 flex items-center justify-between">
          <p className="text-sm font-medium text-slate-600">
            Suggested skills:
          </p>
          <button
            type="button"
            onClick={loadAiSuggestions}
            disabled={isLoadingSuggestions}
            className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
          >
            {isLoadingSuggestions ? "Loading..." : "Suggest with AI"}
          </button>
        </div>

        <div className="flex flex-wrap gap-2">
          {visibleSuggestedSkills.map((skill, idx) => (
            <button
              key={idx}
              onClick={() => addSuggestedSkill(skill)}
              className="flex items-center gap-1 bg-black text-white px-3 py-1.5 text-sm rounded-lg hover:bg-black/80 transition"
            >
              <Plus size={14} />
              {skill}
            </button>
          ))}
        </div>
        {suggestedSkills.keywords?.length > 0 && (
          <div className="mt-4">
            <p className="text-sm font-medium text-slate-700 mb-2">
              ATS keywords
            </p>
            <div className="flex flex-wrap gap-2">
              {suggestedSkills.keywords.map((keyword) => (
                <span
                  key={keyword}
                  className="rounded-full bg-amber-50 px-3 py-1 text-xs font-medium text-amber-700"
                >
                  {keyword}
                </span>
              ))}
            </div>
          </div>
        )}
        {suggestedSkills.atsTips?.length > 0 && (
          <div className="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-3">
            <p className="text-sm font-medium text-slate-700 mb-2">ATS tips</p>
            <ul className="list-disc pl-5 text-xs text-slate-600">
              {suggestedSkills.atsTips.map((tip) => (
                <li key={tip}>{tip}</li>
              ))}
            </ul>
          </div>
        )}
        {aiError && (
          <p className="mt-3 text-xs text-red-500 font-medium">{aiError}</p>
        )}
      </div>
    </div>
  );
};

export default SkillsForm;
