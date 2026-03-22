import React, { useEffect, useState } from "react";
import { Search, Calendar, ChevronDown, Plus, Pencil, Trash2, Image } from "lucide-react";
import axiosInstance from "../../../api/axios";
import toast, { Toaster } from "react-hot-toast";
import AdminCard from "../ui/AdminCard";
import AdminButton from "../ui/AdminButton";
import AdminInput from "../ui/AdminInput";
import AdminModal from "../ui/AdminModal";

export default function AdminBlog() {
  const [activeCategory, setActiveCategory] = useState("All Articles");
  const [searchQuery, setSearchQuery] = useState("");
  const [expandedPosts, setExpandedPosts] = useState({});
  const [blogs, setBlogs] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);

  const [formData, setFormData] = useState({
    title: "",
    excerpt: "",
    detail: "",
    category: "",
    date: "",
    image: "",
    readTime: "",
    isPublished: true,
  });

  const fetchBlogs = async () => {
    try {
      setIsLoading(true);
      setError("");
      const response = await axiosInstance.get(
        "/api/blog?includeUnpublished=true"
      );
      setBlogs(response.data?.data || []);
    } catch (apiError) {
      setError(apiError.response?.data?.message || "Failed to load blogs");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchBlogs();
  }, []);

  const categories = [
    "All Articles",
    ...Array.from(new Set(blogs.map((post) => post.category).filter(Boolean))),
  ];

  const togglePost = (id) => {
    setExpandedPosts((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  const filteredPosts = blogs.filter((post) => {
    const matchesCategory =
      activeCategory === "All Articles" || post.category === activeCategory;

    const matchesSearch =
      post.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      post.excerpt.toLowerCase().includes(searchQuery.toLowerCase());

    return matchesCategory && matchesSearch;
  });

  const resetForm = () => {
    setFormData({
      title: "",
      excerpt: "",
      detail: "",
      category: "",
      date: "",
      image: "",
      readTime: "",
      isPublished: true,
    });
    setEditingId(null);
  };

  const handleAddNew = () => {
    resetForm();
    setShowForm(true);
  };

  const handleEdit = (post) => {
    setEditingId(post._id || post.id);
    setFormData({
      title: post.title || "",
      excerpt: post.excerpt || "",
      detail: post.detail || "",
      category: post.category || "",
      date: post.date || "",
      image: post.image || "",
      readTime: post.readTime || "",
      isPublished:
        typeof post.isPublished === "boolean" ? post.isPublished : true,
    });
    setShowForm(true);
  };

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;

    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      setIsSaving(true);
      setError("");

      if (editingId) {
        await axiosInstance.put(`/api/blog/${editingId}`, formData);
        toast.success("Blog updated successfully");
      } else {
        await axiosInstance.post("/api/blog", formData);
        toast.success("Blog created successfully");
      }

      await fetchBlogs();
      setShowForm(false);
      resetForm();
    } catch (apiError) {
      const message = apiError.response?.data?.message || "Failed to save blog";
      setError(message);
      toast.error(message);
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = (id) => {
    const doDelete = async () => {
      try {
        setError("");
        await axiosInstance.delete(`/api/blog/${id}`);
        toast.success("Blog deleted");
        await fetchBlogs();
      } catch (apiError) {
        const message = apiError.response?.data?.message || "Failed to delete blog";
        setError(message);
        toast.error(message);
      }
    };

    doDelete();
  };

  return (
    <div className="min-h-screen bg-slate-50 text-[#1a2e52] px-4 sm:px-6 lg:px-8 py-6">
      <Toaster position="top-right" />

      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black">Blog Management</h1>
          <p className="text-sm text-slate-600 mt-1">
            Create, edit, and organize your content library.
          </p>
        </div>

        <AdminButton onClick={handleAddNew}>
          <Plus size={16} /> Add Blog
        </AdminButton>
      </div>

      {error && (
        <div className="mb-6 rounded-xl bg-red-50 text-red-600 px-4 py-3 font-semibold">
          {error}
        </div>
      )}

      <AdminModal
        open={showForm}
        onClose={() => {
          setShowForm(false);
          resetForm();
        }}
        title={editingId ? "Edit Blog" : "Add New Blog"}
        description="Use clear titles and concise excerpts for better engagement."
        actions={
          <div className="flex flex-wrap gap-3 justify-end">
            <AdminButton
              variant="secondary"
              onClick={() => {
                setShowForm(false);
                resetForm();
              }}
            >
              Cancel
            </AdminButton>
            <AdminButton onClick={handleSubmit} loading={isSaving}>
              {editingId ? "Update Blog" : "Create Blog"}
            </AdminButton>
          </div>
        }
      >
        <form onSubmit={handleSubmit} className="grid gap-4 md:grid-cols-2">
          <AdminInput
            name="title"
            label="Title"
            value={formData.title}
            onChange={handleChange}
            placeholder="A better resume in 10 minutes"
            required
          />
          <AdminInput
            name="category"
            label="Category"
            value={formData.category}
            onChange={handleChange}
            placeholder="Career Tips"
            required
          />
          <AdminInput
            name="date"
            label="Date"
            value={formData.date}
            onChange={handleChange}
            placeholder="2026-03-21"
          />
          <AdminInput
            name="readTime"
            label="Read Time"
            value={formData.readTime}
            onChange={handleChange}
            placeholder="5 min read"
          />
          <AdminInput
            name="image"
            label="Image URL"
            value={formData.image}
            onChange={handleChange}
            placeholder="https://..."
            required
            className="md:col-span-2"
          />

          {formData.image ? (
            <div className="md:col-span-2">
              <p className="text-xs font-semibold text-slate-600 mb-2 flex items-center gap-2">
                <Image className="h-4 w-4 text-slate-400" /> Image Preview
              </p>
              <div className="rounded-2xl border border-slate-200 overflow-hidden bg-slate-100">
                <img
                  src={formData.image}
                  alt="Preview"
                  className="h-48 w-full object-cover"
                  onError={(e) => {
                    e.currentTarget.style.display = "none";
                  }}
                />
              </div>
            </div>
          ) : null}

          <label className="md:col-span-2">
            <span className="mb-1.5 block text-xs font-semibold text-slate-600">
              Excerpt
            </span>
            <textarea
              name="excerpt"
              value={formData.excerpt}
              onChange={handleChange}
              placeholder="Short summary to show in cards"
              rows={3}
              required
              className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm text-slate-800 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
            />
          </label>

          <label className="md:col-span-2">
            <span className="mb-1.5 block text-xs font-semibold text-slate-600">
              Full Content
            </span>
            <textarea
              name="detail"
              value={formData.detail}
              onChange={handleChange}
              placeholder="Write the full blog content"
              rows={6}
              required
              className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm text-slate-800 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
            />
          </label>

          <label className="md:col-span-2 flex items-center gap-2 text-sm text-slate-600">
            <input
              type="checkbox"
              name="isPublished"
              checked={formData.isPublished}
              onChange={handleChange}
            />
            Published
          </label>
        </form>
      </AdminModal>

      <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 mb-8">
        <div className="relative w-full sm:max-w-md">
          <Search className="absolute w-5 h-5 text-gray-400 left-4 top-3.5" />
          <input
            type="text"
            placeholder="Search blogs..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-12 pr-4 py-3 border border-slate-200 rounded-xl"
          />
        </div>

        <div className="flex flex-wrap gap-3">
          {categories.map((category) => (
            <button
              key={category}
              onClick={() => setActiveCategory(category)}
              className={`px-4 py-2 rounded-xl font-semibold text-sm ${
                activeCategory === category
                  ? "bg-[#1a2e52] text-white"
                  : "bg-white border border-slate-200 text-gray-500"
              }`}
            >
              {category}
            </button>
          ))}
        </div>
      </div>

      {isLoading ? (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <div
              key={i}
              className="h-72 rounded-2xl bg-white border border-slate-200 animate-pulse"
            />
          ))}
        </div>
      ) : filteredPosts.length === 0 ? (
        <div className="text-center mt-20 text-gray-400">
          No blogs created yet.
        </div>
      ) : (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {filteredPosts.map((post) => (
            <AdminCard
              key={post._id || post.id}
              className="overflow-hidden p-0"
            >
              <div className="relative h-44 sm:h-52">
                <img
                  src={post.image}
                  alt={post.title}
                  className="w-full h-full object-cover"
                />
                <span className="absolute top-3 left-3 bg-white text-blue-600 px-3 py-1 text-xs font-bold rounded-lg shadow">
                  {post.category}
                </span>
              </div>

              <div className="p-5">
                <div className="flex items-center gap-2 text-xs text-gray-400 mb-3">
                  <Calendar size={14} />
                  {post.date} - {post.readTime}
                </div>

                <h3 className="text-base sm:text-lg font-bold mb-3">
                  {post.title}
                </h3>

                <p className="text-sm text-gray-500 mb-4 line-clamp-3">
                  {post.excerpt}
                </p>

                <div
                  className={`overflow-hidden transition-all ${
                    expandedPosts[post._id || post.id]
                      ? "max-h-40 opacity-100 mb-4"
                      : "max-h-0 opacity-0"
                  }`}
                >
                  <p className="text-sm text-gray-600">{post.detail}</p>
                </div>

                <button
                  onClick={() => togglePost(post._id || post.id)}
                  className="text-blue-600 font-semibold flex items-center gap-2 text-sm"
                >
                  {expandedPosts[post._id || post.id] ? "Show Less" : "Read More"}

                  <ChevronDown
                    className={`w-4 h-4 transition ${
                      expandedPosts[post._id || post.id] ? "rotate-180" : ""
                    }`}
                  />
                </button>

                <div className="flex justify-end gap-4 mt-6">
                  <button
                    onClick={() => handleEdit(post)}
                    className="text-blue-600 hover:text-blue-800"
                  >
                    <Pencil size={18} />
                  </button>

                  <button
                    onClick={() => handleDelete(post._id || post.id)}
                    className="text-red-500 hover:text-red-700"
                  >
                    <Trash2 size={18} />
                  </button>
                </div>
              </div>
            </AdminCard>
          ))}
        </div>
      )}
    </div>
  );
}

