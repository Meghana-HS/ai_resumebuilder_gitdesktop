import React, { useEffect, useState } from "react";
import axiosInstance from "../../../api/axios";
import {
  FiFileText,
  FiFile,
  FiEdit,
  FiDownload,
  FiEye,
  FiClock,
  FiActivity,
  FiShield,
} from "react-icons/fi";
import { motion, AnimatePresence } from "framer-motion";

const ACTION_META = {
  edited: { label: "Edited", icon: <FiEdit size={10} /> },
  preview: { label: "Previewed", icon: <FiEye size={10} /> },
  visited: { label: "Viewed", icon: <FiActivity size={10} /> },
  download: { label: "Downloaded", icon: <FiDownload size={10} /> },
};

const TYPE_META = {
  resume: { label: "Resume", icon: <FiFileText /> },
  "cover-letter": { label: "Cover Letter", icon: <FiEdit /> },
  cv: { label: "CV", icon: <FiFile /> },
  "ats-checker": { label: "ATS Checker", icon: <FiShield /> },
};

const RecentDocuments = () => {
  const [recentDocs, setRecentDocs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [previewDoc, setPreviewDoc] = useState(null);
  const [previewLoading, setPreviewLoading] = useState(false);

  /* ---------------- FETCH RECENT ---------------- */
  const fetchRecent = async () => {
    try {
      const res = await axiosInstance.get(
        "/api/downloads/recent?limit=100&page=1",
      );

      const docs = (res.data.downloads || []).map((d) => ({
        id: d._id?.toString?.() || d.id,
        name: d.name,
        type: d.type,
        action: d.action || "download",
        format: (
          d.format || (d.type === "cover-letter" ? "DOCX" : "PDF")
        ).toUpperCase(),
        template: d.template,
        size: d.size || "200 KB",
        downloadDate:
          d.downloadDate ||
          d.createdAt ||
          d.updatedAt ||
          new Date().toISOString(),
      }));

      const sorted = docs.sort(
        (a, b) => new Date(b.downloadDate) - new Date(a.downloadDate),
      );

      setRecentDocs(sorted.slice(0, 10));
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRecent();
    const interval = setInterval(fetchRecent, 10000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const onFocus = () => fetchRecent();
    const onVisible = () => {
      if (!document.hidden) fetchRecent();
    };
    window.addEventListener("focus", onFocus);
    document.addEventListener("visibilitychange", onVisible);
    return () => {
      window.removeEventListener("focus", onFocus);
      document.removeEventListener("visibilitychange", onVisible);
    };
  }, []);

  /* ---------------- PREVIEW ---------------- */
  const handlePreview = async (doc) => {
    try {
      setPreviewLoading(true);
      const res = await axiosInstance.get(`/api/downloads/${doc.id}`);
      setPreviewDoc({ ...doc, html: res.data.html });
    } catch (err) {
      console.error("Preview failed:", err);
    } finally {
      setPreviewLoading(false);
    }
  };

  /* ---------------- DOWNLOAD ---------------- */
  const handleDownload = async (doc) => {
    try {
      const url =
        doc.format === "DOCX"
          ? `/api/downloads/${doc.id}/word`
          : `/api/downloads/${doc.id}/pdf`;
      const res = await axiosInstance.get(url, { responseType: "blob" });
      const blob = new Blob([res.data], {
        type:
          doc.format === "PDF"
            ? "application/pdf"
            : "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      });
      const link = document.createElement("a");
      link.href = window.URL.createObjectURL(blob);
      link.download = `${doc.name}.${doc.format.toLowerCase()}`;
      link.click();
      setTimeout(fetchRecent, 500);
    } catch {
      alert("Download failed");
    }
  };

  /* ---------------- HELPERS ---------------- */
  const formatDate = (dateStr) => {
    const date = new Date(dateStr);
    const diff = Date.now() - date;
    const m = Math.floor(diff / 60000);
    const h = Math.floor(diff / 3600000);
    const d = Math.floor(diff / 86400000);
    if (m < 60) return `${m}m ago`;
    if (h < 24) return `${h}h ago`;
    if (d < 7) return `${d}d ago`;
    return date.toLocaleDateString();
  };

  const getActionMeta = (action) => ACTION_META[action] || ACTION_META.download;
  const getTypeMeta = (type) =>
    TYPE_META[type] || { label: type || "Document", icon: <FiFile /> };

  /* ---------------- LOADING ---------------- */
  if (loading) {
    return (
      <div className="py-10 text-center text-gray-400">
        Loading recent activity...
      </div>
    );
  }

  if (!recentDocs.length) {
    return (
      <div className="bg-white border rounded-xl p-10 text-center text-gray-400">
        No recent activity yet
      </div>
    );
  }

  return (
    <>
      <div className="bg-white border border-gray-100 rounded-2xl p-6">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-lg font-semibold text-gray-900">
            Recent Activity
          </h2>
          <a
            href="/user/downloads"
            className="text-xs font-medium text-blue-600 hover:underline"
          >
            View all
          </a>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {recentDocs.map((doc, i) => {
            const typeMeta = getTypeMeta(doc.type);
            const actionMeta = getActionMeta(doc.action);
            return (
              <motion.div
                key={doc.id}
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.04 }}
                className="border border-gray-100 rounded-xl p-4 hover:shadow-md transition"
              >
                <div className="flex items-center gap-2 mb-2 text-gray-500 text-xs">
                  <span className="text-gray-700">{typeMeta.icon}</span>
                  <span className="uppercase font-semibold">
                    {typeMeta.label}
                  </span>
                  <span className="ml-auto bg-gray-100 px-2 py-0.5 rounded text-[10px] font-bold">
                    {doc.format}
                  </span>
                </div>

                <h3 className="font-semibold text-sm text-gray-900 truncate">
                  {doc.name}
                </h3>

                {doc.template && (
                  <p className="text-[11px] text-gray-400 mt-1 truncate">
                    {doc.template}
                  </p>
                )}

                <div className="flex items-center text-[11px] text-gray-500 mt-2 gap-1">
                  {actionMeta.icon}
                  <span className="font-semibold text-gray-700">
                    {actionMeta.label}
                  </span>
                  <span className="text-gray-400">
                    • {formatDate(doc.downloadDate)}
                  </span>
                </div>

                <div className="flex gap-2 mt-4">
                  <button
                    onClick={() => handlePreview(doc)}
                    className="flex-1 py-1.5 text-xs bg-gray-100 rounded-lg hover:bg-gray-200 flex items-center justify-center gap-1"
                  >
                    <FiEye size={11} />
                    Preview
                  </button>

                  <button
                    onClick={() => handleDownload(doc)}
                    className="flex-1 py-1.5 text-xs bg-black text-white rounded-lg hover:bg-gray-800 flex items-center justify-center gap-1"
                  >
                    <FiDownload size={11} />
                    Download
                  </button>
                </div>
              </motion.div>
            );
          })}
        </div>
      </div>

      <AnimatePresence>
        {previewDoc && (
          <motion.div
            className="fixed inset-0 z-50 bg-black/60 flex items-center justify-center"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setPreviewDoc(null)}
          >
            <motion.div
              className="bg-white rounded-xl shadow-xl max-w-3xl w-full max-h-[90vh] overflow-auto p-8"
              initial={{ scale: 0.9 }}
              animate={{ scale: 1 }}
              exit={{ scale: 0.9 }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-semibold">{previewDoc.name}</h3>
                <button
                  onClick={() => setPreviewDoc(null)}
                  className="text-gray-500 hover:text-black"
                >
                  ✕
                </button>
              </div>

              {previewLoading ? (
                <div className="text-center py-10 text-gray-400">
                  Loading preview...
                </div>
              ) : (
                <div
                  className="prose max-w-none"
                  dangerouslySetInnerHTML={{ __html: previewDoc.html }}
                />
              )}
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
};

export default RecentDocuments;
