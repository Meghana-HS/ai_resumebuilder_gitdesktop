import { Plus, X } from "lucide-react";
import { useState } from "react";
import { aiService } from "../../../../services/aiService";

const SkillsForm = ({ formData, setFormData }) => {
  const [newSkill, setNewSkill] = useState("");
  const [skillType, setSkillType] = useState("technical");
  const [suggestedSkills, setSuggestedSkills] = useState({
    technicalSkills: [
      "JavaScript",
      "React.js",
      "Node.js",
      "Python",
      "SQL",
      "AWS",
    ],
    softSkills: ["Leadership", "Communication", "Teamwork", "Problem Solving"],
    keywords: [],
    atsTips: [],
  });
  const [isLoadingSuggestions, setIsLoadingSuggestions] = useState(false);
  const [aiError, setAiError] = useState("");

  const addSkill = () => {
    if (newSkill.trim()) {
      setFormData((prev) => ({
        ...prev,
        skills: {
          ...(prev?.skills ?? { technical: [], soft: [] }),
          [skillType]: [...(prev?.skills?.[skillType] ?? []), newSkill.trim()],
        },
      }));
      setNewSkill("");
    }
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
    <div className="flex flex-col gap-0.5">
      <div className="flex gap-2 p-3 rounded-xl bg-slate-900 w-fit my-2 mx-auto">
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

      <div className="flex gap-2 w-full mt-2 mb-4">
        <input
          type="text"
          className="flex-grow px-3.5 py-2.5 border border-slate-200 rounded-lg text-sm text-slate-900 focus:outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-600/10 transition-all bg-white"
          value={newSkill}
          placeholder={`Add a ${skillType} skill... (e.g., JavaScript, Leadership)`}
          onChange={(e) => setNewSkill(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter" && newSkill.trim()) {
              e.preventDefault();
              addSkill();
            }
          }}
        />
        <button
          className="bg-black text-white py-2.5 px-5 rounded-lg text-sm font-medium hover:bg-black/80 transition-colors whitespace-nowrap"
          onClick={addSkill}
        >
          Add Skill
        </button>
      </div>

      <div className="w-full flex flex-wrap gap-2 mb-6 min-h-[40px]">
        {(formData?.skills?.[skillType] ?? []).map((skill, idx) => (
          <span
            key={idx}
            className="inline-flex items-center gap-1.5 bg-blue-50 text-sm font-medium text-blue-700 border border-blue-200 rounded-md px-2.5 py-1.5"
          >
            <span>{skill}</span>
            <button
              onClick={() => removeSkill(skillType, idx)}
              className="hover:text-red-500 hover:bg-blue-100 rounded-full p-0.5 transition-colors"
            >
              <X size={14} />
            </button>
          </span>
        ))}
        {(formData?.skills?.[skillType] ?? []).length === 0 && (
          <div className="text-sm text-slate-400 italic flex items-center h-full">
            No {skillType} skills added yet.
          </div>
        )}
      </div>

      <div className="w-full">
        <div className="flex items-center justify-between mb-3">
          <p className="text-sm font-medium text-slate-700">
            Suggested {skillType} skills:
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
          {visibleSuggestedSkills.map((skill, idx) => {
            const isAdded = (formData?.skills?.[skillType] ?? []).includes(
              skill,
            );
            return (
              <button
                key={idx}
                disabled={isAdded}
                className={`flex items-center gap-1.5 px-3 py-1.5 text-sm rounded-md border transition-all ${
                  isAdded
                    ? "bg-slate-100 border-slate-200 text-slate-400 cursor-not-allowed"
                    : "bg-white border-slate-200 text-slate-700 hover:border-slate-300 hover:bg-slate-50"
                }`}
                onClick={() => addSuggestedSkill(skill)}
              >
                {!isAdded && <Plus size={14} className="text-slate-400" />}
                {skill}
              </button>
            );
          })}
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
