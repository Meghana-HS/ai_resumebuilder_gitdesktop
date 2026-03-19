import axiosInstance from "../api/axios";

const STORAGE_KEY = "resume_builder_recent_activity_v1";
const MAX_ACTIVITIES = 100;

const TYPE_MAP = {
  create: "created",
  created: "created",
  new: "created",
  visit: "visited",
  visited: "visited",
  edit: "updated",
  edited: "updated",
  update: "updated",
  updated: "updated",
  download: "downloaded",
  downloaded: "downloaded",
};

const DOCUMENT_TYPE_MAP = {
  resume: "resume",
  resumes: "resume",
  cv: "cv",
  cvs: "cv",
  "cover-letter": "cover-letter",
  cover_letter: "cover-letter",
  coverletter: "cover-letter",
  ats: "ats",
  "ats-checker": "ats",
};

const normalizeType = (rawType) => {
  const type = String(rawType || "")
    .toLowerCase()
    .trim();
  return TYPE_MAP[type] || "updated";
};

const normalizeDocumentType = (rawDocumentType) => {
  const type = String(rawDocumentType || "")
    .toLowerCase()
    .trim();
  return DOCUMENT_TYPE_MAP[type] || "resume";
};

const resolveActivityType = (activity) => {
  const action = String(activity.action || "")
    .toLowerCase()
    .trim();
  if (TYPE_MAP[action]) {
    return normalizeType(action);
  }
  return normalizeType(activity.type);
};

const safeJsonParse = (value, fallback = null) => {
  try {
    return JSON.parse(value);
  } catch {
    return fallback;
  }
};

const readLocalActivities = () => {
  if (typeof window === "undefined") return [];
  const parsed = safeJsonParse(localStorage.getItem(STORAGE_KEY), []);
  return Array.isArray(parsed) ? parsed : [];
};

const writeLocalActivities = (activities) => {
  if (typeof window === "undefined") return;
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify(activities.slice(0, MAX_ACTIVITIES)),
  );
};

const toIsoString = (value) => {
  if (!value) return new Date().toISOString();
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? new Date().toISOString()
    : date.toISOString();
};

const uniqueId = () => {
  if (typeof crypto !== "undefined" && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
};

const normalizeActivity = (activity) => {
  const type = resolveActivityType(activity);
  const resumeName =
    activity.resumeName ||
    activity.docTitle ||
    activity.title ||
    activity.name ||
    "Untitled Resume";

  return {
    id: activity.id || activity._id || uniqueId(),
    type,
    resumeName,
    documentType: normalizeDocumentType(
      activity.documentType ||
        activity.module ||
        activity.typeName ||
        activity.resumeType ||
        activity.type,
    ),
    timestamp: toIsoString(
      activity.timestamp ||
        activity.time ||
        activity.createdAt ||
        activity.updatedAt,
    ),
  };
};

const mapApiActivities = (payload) => {
  if (!payload) return [];

  if (Array.isArray(payload)) {
    return payload.map(normalizeActivity);
  }

  const candidates = [
    payload.recentActivity,
    payload.activities,
    payload.history,
    payload.data?.recentActivity,
    payload.data?.activities,
    payload.data?.history,
    payload.data,
  ];

  const found = candidates.find((item) => Array.isArray(item));
  if (!found) return [];
  return found.map(normalizeActivity);
};

const mergeAndSortActivities = (apiActivities, localActivities, limit = 20) => {
  const all = [...apiActivities, ...localActivities].map(normalizeActivity);
  const seen = new Set();
  const deduped = [];

  for (const item of all) {
    const key = `${item.type}|${item.documentType}|${item.resumeName}|${item.timestamp}`;
    if (seen.has(key)) continue;
    seen.add(key);
    deduped.push(item);
  }

  return deduped
    .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
    .slice(0, limit);
};

export const trackResumeActivity = async ({
  type,
  resumeName,
  timestamp,
  documentType = "resume",
}) => {
  const normalized = normalizeActivity({
    type,
    resumeName,
    documentType,
    timestamp,
  });

  const current = readLocalActivities();
  writeLocalActivities([normalized, ...current]);

  if (typeof window !== "undefined") {
    window.dispatchEvent(new CustomEvent("resume-activity-updated"));
  }

  try {
    await axiosInstance.post("/api/activity", {
      action: normalized.type,
      type: normalized.type,
      resumeName: normalized.resumeName,
      timestamp: normalized.timestamp,
      documentType,
    });
  } catch {
    // Backend activity endpoint is optional; localStorage remains source of truth fallback.
  }
};

export const getRecentActivities = async ({ limit = 20 } = {}) => {
  const localActivities = readLocalActivities();

  const endpoints = [
    "/api/activity",
    "/api/dashboard/recent-activity",
    "/api/downloads/recent",
    "/api/resume/history",
    "/api/dashboard/summary",
  ];
  let apiActivities = [];

  for (const endpoint of endpoints) {
    try {
      const response = await axiosInstance.get(endpoint);
      const mapped = mapApiActivities(response?.data);
      if (mapped.length > 0) {
        apiActivities = mapped;
        break;
      }
    } catch {
      // Try the next endpoint.
    }
  }

  return mergeAndSortActivities(apiActivities, localActivities, limit);
};

export const getActivityLabel = (activity) => {
  const title = activity.resumeName || "Untitled Resume";
  const typeLabelMap = {
    resume: "Resume",
    cv: "CV",
    "cover-letter": "Cover Letter",
    ats: "ATS Checker",
  };
  const documentLabel =
    typeLabelMap[String(activity.documentType || "").toLowerCase()] || "Resume";

  if (activity.type === "visited") return `Visited ${documentLabel}`;
  if (activity.type === "created") return `Created ${documentLabel} "${title}"`;
  if (activity.type === "downloaded") {
    return `Downloaded ${documentLabel} "${title}"`;
  }
  return `Updated ${documentLabel} "${title}"`;
};
