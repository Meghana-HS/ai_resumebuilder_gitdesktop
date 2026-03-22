import React, { useCallback, useEffect, useMemo, useState } from "react";
import {
  TrendingUp,
  Users,
  UserCheck,
  UserMinus,
  Activity,
  Zap,
  Shield,
  Crown,
  Award,
  Gem,
  RefreshCw,
} from "lucide-react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import axiosInstance from "../../../api/axios";
import AdminTopPagesAnalytics from "../AdminTopPagesAnalytics";
import AdminCard from "../ui/AdminCard";
import AdminButton from "../ui/AdminButton";

export default function AdminAnalytics() {
  const [userGrowth, setUserGrowth] = useState({ count: 0, note: "" });
  const [conversions, setConversions] = useState({ count: 0, note: "" });
  const [activeUsers, setActiveUsers] = useState({ count: 0, note: "" });
  const [deletedUsers, setDeletedUsers] = useState({ count: 0, note: "" });
  const [mostUsedResumeTemplates, setMostUsedResumeTemplates] = useState([]);
  const [mostUsedCvTemplates, setMostUsedCvTemplates] = useState([]);
  const [templateView, setTemplateView] = useState("resume");
  const [refreshingTemplates, setRefreshingTemplates] = useState(false);
  const [chartData, setChartData] = useState([]);
  const [subscriptionBreakdown, setSubscriptionBreakdown] = useState([]);
  const [summary, setSummary] = useState({
    apiSuccessRate: "0%",
    apiFailureRate: "0%",
    avgResponseTime: "0ms",
    totalApiCalls: 0,
    systemUptime: "99.98%",
  });
  const [loading, setLoading] = useState(true);

  const displaySubscriptionBreakdown = useMemo(() => {
    return [...subscriptionBreakdown].sort((a, b) => b.count - a.count);
  }, [subscriptionBreakdown]);

  const totalSubscriptionUsers = useMemo(() => {
    return displaySubscriptionBreakdown.reduce(
      (acc, curr) => acc + curr.count,
      0,
    );
  }, [displaySubscriptionBreakdown]);

  const fetchAnalyticsData = useCallback(async (showLoader = false) => {
    if (showLoader) setLoading(true);
    try {
      const response = await axiosInstance.get("/api/admin/analytics-stat");
      setUserGrowth(response.data.userGrowth);
      setConversions(response.data.conversions);
      setActiveUsers(response.data.activeUsers);
      setDeletedUsers(response.data.deletedUsers || { count: 0, note: "" });
      setMostUsedResumeTemplates(
        response.data.mostUsedResumeTemplates ||
          response.data.mostUsedTemplates ||
          [],
      );
      setMostUsedCvTemplates(response.data.mostUsedCvTemplates || []);
      setChartData(response.data.chartData || []);
      setSubscriptionBreakdown(response.data.subscriptionBreakdown || []);
      setSummary(
        response.data.summary || {
          apiSuccessRate: "0%",
          apiFailureRate: "0%",
          avgResponseTime: "0ms",
          totalApiCalls: 0,
          systemUptime: "99.98%",
        },
      );
    } catch (error) {
      console.error("Error fetching analytics:", error);
    } finally {
      setLoading(false);
      setRefreshingTemplates(false);
    }
  }, []);

  useEffect(() => {
    fetchAnalyticsData(true);
    const fetchInterval = setInterval(() => fetchAnalyticsData(false), 5000);
    return () => clearInterval(fetchInterval);
  }, [fetchAnalyticsData]);

  const stats = [
    {
      title: "User Growth",
      value: loading ? "..." : `${userGrowth.count} Users`,
      note: userGrowth.note,
      icon: <TrendingUp className="text-emerald-600" />,
      iconBg: "bg-emerald-50",
      valueColor: "text-slate-900",
    },
    {
      title: "Paid Conversions",
      value: loading ? "..." : `${conversions.count} Users`,
      note: conversions.note,
      icon: <Users className="text-blue-600" />,
      iconBg: "bg-blue-50",
      valueColor: "text-slate-900",
    },
    {
      title: "Active Users",
      value: loading ? "..." : `${activeUsers.count} Users`,
      note: activeUsers.note,
      icon: <UserCheck className="text-purple-600" />,
      iconBg: "bg-purple-50",
      valueColor: "text-slate-900",
    },
    {
      title: "Deleted Users",
      value: loading ? "..." : `${deletedUsers.count} Users`,
      note: deletedUsers.note,
      icon: <UserMinus className="text-rose-600" />,
      iconBg: "bg-rose-50",
      valueColor: "text-slate-900",
    },
  ];

  const total = (activeUsers.count || 0) + (deletedUsers.count || 0);
  const retentionRate =
    total > 0 ? Math.round((activeUsers.count / total) * 100) : 0;
  const retentionColor =
    retentionRate >= 80
      ? "text-indigo-600"
      : retentionRate >= 60
        ? "text-amber-500"
        : "text-rose-500";
  const retentionBar =
    retentionRate >= 80
      ? "bg-indigo-600"
      : retentionRate >= 60
        ? "bg-amber-500"
        : "bg-rose-500";

  return (
    <div className="min-h-screen flex-1 p-4 sm:p-6 bg-slate-50 text-slate-900">
      <div className="mb-6 sm:mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold">System Analytics</h1>
          <p className="text-sm sm:text-base text-slate-600 mt-1 sm:mt-2">
            Deep dive into platform performance and user engagement.
          </p>
        </div>
        <AdminButton
          variant="secondary"
          size="sm"
          onClick={() => fetchAnalyticsData(true)}
        >
          <RefreshCw className="h-4 w-4" />
          Refresh
        </AdminButton>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 sm:gap-6 mb-10">
        {stats.map((item) => (
          <AdminCard key={item.title} className="p-0" header={
            <div className="flex items-center justify-between">
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide">
                {item.title}
              </p>
              <div className={`${item.iconBg} p-2.5 rounded-xl`}>{item.icon}</div>
            </div>
          }>
            <p className={`text-2xl font-bold mt-3 ${item.valueColor}`}>{item.value}</p>
            <p className="text-slate-500 text-sm mt-2">{item.note}</p>
          </AdminCard>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        <AdminCard>
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold">Platform Health</h3>
            <div className="bg-emerald-50 p-2 rounded-full">
              <Activity className="text-emerald-600" size={20} />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-4xl font-bold text-emerald-600">
              {loading ? "..." : summary.apiSuccessRate.replace("%", "")}
            </span>
            <span className="text-slate-500">/100</span>
          </div>
          <p className="text-sm text-slate-500 mt-2">
            {parseFloat(summary.apiSuccessRate) > 95
              ? "System running smoothly"
              : "Monitoring performance"}
          </p>
          <div className="mt-4 bg-slate-100 rounded-full h-2">
            <div
              className="bg-emerald-600 h-2 rounded-full"
              style={{ width: summary.apiSuccessRate }}
            ></div>
          </div>
        </AdminCard>

        <AdminCard>
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold">User Retention</h3>
            <div className="bg-indigo-50 p-2 rounded-full">
              <RefreshCw className="text-indigo-600" size={20} />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className={`text-4xl font-bold ${retentionColor}`}>
              {loading ? "..." : `${retentionRate}%`}
            </span>
          </div>
          <p className="text-sm text-slate-500 mt-2">
            {retentionRate >= 80
              ? "Healthy retention"
              : retentionRate >= 60
                ? "Moderate retention"
                : "Needs attention"}
          </p>
          <div className="mt-4 bg-slate-100 rounded-full h-2">
            <div className={`h-2 rounded-full ${retentionBar}`} style={{ width: `${retentionRate}%` }}></div>
          </div>
          <p className="text-xs text-slate-400 mt-2">
            {loading
              ? ""
              : `${activeUsers.count} active - ${deletedUsers.count} churned`}
          </p>
        </AdminCard>

        <AdminCard>
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold">Avg Response Time</h3>
            <div className="bg-blue-50 p-2 rounded-full">
              <Zap className="text-blue-600" size={20} />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-4xl font-bold text-blue-600">
              {loading ? "..." : summary.avgResponseTime.replace("ms", "")}
            </span>
            <span className="text-slate-500">ms</span>
          </div>
          <p className="text-sm text-slate-500 mt-2">Platform latency</p>
        </AdminCard>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-6 mb-6">
        <AdminCard>
          <div className="flex items-center justify-between mb-3">
            <p className="text-sm text-slate-500">System Uptime</p>
            <div className="bg-emerald-50 p-2 rounded-full">
              <Shield className="text-emerald-600" size={16} />
            </div>
          </div>
          <p className="text-2xl font-bold text-slate-900">
            {loading ? "..." : summary.systemUptime}
          </p>
          <p className="text-xs text-slate-500 mt-1">Last 30 days</p>
        </AdminCard>

        <AdminCard>
          <div className="flex items-center justify-between mb-3">
            <p className="text-sm text-slate-500">Total API Calls</p>
            <div className="bg-purple-50 p-2 rounded-full">
              <Activity className="text-purple-600" size={16} />
            </div>
          </div>
          <p className="text-2xl font-bold text-slate-900">
            {loading ? "..." : summary.totalApiCalls}
          </p>
          <p className="text-xs text-slate-500 mt-1">Last 30 days</p>
        </AdminCard>

        <AdminCard>
          <div className="flex items-center justify-between mb-3">
            <p className="text-sm text-slate-500">API Success Rate</p>
            <div className="bg-blue-50 p-2 rounded-full">
              <Zap className="text-blue-600" size={16} />
            </div>
          </div>
          <p className="text-2xl font-bold text-slate-900">
            {loading ? "..." : summary.apiSuccessRate}
          </p>
          <p className="text-xs text-emerald-600 mt-1">Real-time health</p>
        </AdminCard>

        <AdminCard>
          <div className="flex items-center justify-between mb-3">
            <p className="text-sm text-slate-500">API Failure Rate</p>
            <div className="bg-rose-50 p-2 rounded-full">
              <Activity className="text-rose-600" size={16} />
            </div>
          </div>
          <p className="text-2xl font-bold text-slate-900">
            {loading ? "..." : summary.apiFailureRate}
          </p>
          <p className="text-xs text-rose-600 mt-1">Real-time error monitoring</p>
        </AdminCard>
      </div>

      <AdminCard className="mb-8" bodyClassName="flex flex-col">
        <h2 className="text-lg font-semibold mb-1">Platform Growth & Revenue</h2>
        <p className="text-sm text-slate-500 mb-6">User acquisition vs revenue generated</p>

        {loading ? (
          <div className="h-64 flex items-center justify-center text-slate-400">
            Loading chart data...
          </div>
        ) : chartData.length > 0 ? (
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis dataKey="month" stroke="#64748b" fontSize={12} />
              <YAxis yAxisId="left" stroke="#64748b" fontSize={12} />
              <YAxis
                yAxisId="right"
                orientation="right"
                stroke="#64748b"
                fontSize={12}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: "#fff",
                  border: "1px solid #e2e8f0",
                  borderRadius: "8px",
                }}
              />
              <Legend />
              <Line
                yAxisId="right"
                type="monotone"
                dataKey="revenue"
                stroke="#10b981"
                strokeWidth={2}
                name="Revenue (INR)"
                dot={{ fill: "#10b981", r: 4 }}
                activeDot={{ r: 6 }}
              />
              <Line
                yAxisId="left"
                type="monotone"
                dataKey="users"
                stroke="#3b82f6"
                strokeWidth={2}
                name="New Users"
                dot={{ fill: "#3b82f6", r: 4 }}
                activeDot={{ r: 6 }}
              />
            </LineChart>
          </ResponsiveContainer>
        ) : (
          <div className="h-64 flex items-center justify-center text-slate-400 border border-dashed border-slate-300 rounded-xl">
            No trend data available yet
          </div>
        )}
      </AdminCard>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        <AdminCard>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold">Most Used Templates</h2>
            <AdminButton
              variant="secondary"
              size="sm"
              onClick={() => {
                setRefreshingTemplates(true);
                fetchAnalyticsData(false);
              }}
              disabled={refreshingTemplates || loading}
            >
              <RefreshCw
                size={14}
                className={refreshingTemplates ? "animate-spin" : ""}
              />
              {refreshingTemplates ? "Refreshing" : "Refresh"}
            </AdminButton>
          </div>

          <div className="inline-flex bg-slate-100 rounded-lg p-1 mb-4">
            <button
              type="button"
              onClick={() => setTemplateView("resume")}
              className={`px-3 py-1.5 text-sm font-medium rounded-md transition ${
                templateView === "resume"
                  ? "bg-white text-slate-900 shadow-sm"
                  : "text-slate-500 hover:text-slate-700"
              }`}
            >
              Resume
            </button>
            <button
              type="button"
              onClick={() => setTemplateView("cv")}
              className={`px-3 py-1.5 text-sm font-medium rounded-md transition ${
                templateView === "cv"
                  ? "bg-white text-slate-900 shadow-sm"
                  : "text-slate-500 hover:text-slate-700"
              }`}
            >
              CV
            </button>
          </div>

          {loading ? (
            <div className="text-center text-slate-400 py-8">Loading...</div>
          ) : templateView === "resume" ? (
            mostUsedResumeTemplates.length > 0 ? (
              <div className="space-y-3 text-sm">
                {mostUsedResumeTemplates.map((template, index) => {
                  const colors = [
                    "text-blue-600",
                    "text-purple-600",
                    "text-emerald-600",
                    "text-orange-600",
                    "text-slate-500",
                  ];
                  const bgs = [
                    "bg-blue-50",
                    "bg-purple-50",
                    "bg-emerald-50",
                    "bg-orange-50",
                    "bg-slate-50",
                  ];
                  return (
                    <div
                      key={`resume-${template.templateId}-${index}`}
                      className={`flex justify-between items-center p-3 rounded-xl ${bgs[index % bgs.length]}`}
                    >
                      <span className="font-medium text-slate-700">
                        {template.templateId}
                      </span>
                      <span
                        className={`${colors[index % colors.length]} font-bold`}
                      >
                        {template.count} uses ({template.percentage}%)
                      </span>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="text-center text-slate-400 py-6 border border-dashed border-slate-200 rounded-xl">
                No resume template usage yet
              </div>
            )
          ) : mostUsedCvTemplates.length > 0 ? (
            <div className="space-y-3 text-sm">
              {mostUsedCvTemplates.map((template, index) => {
                const colors = [
                  "text-indigo-600",
                  "text-cyan-600",
                  "text-teal-600",
                  "text-amber-600",
                  "text-slate-500",
                ];
                const bgs = [
                  "bg-indigo-50",
                  "bg-cyan-50",
                  "bg-teal-50",
                  "bg-amber-50",
                  "bg-slate-50",
                ];
                return (
                  <div
                    key={`cv-${template.templateId}-${index}`}
                    className={`flex justify-between items-center p-3 rounded-xl ${bgs[index % bgs.length]}`}
                  >
                    <span className="font-medium text-slate-700">
                      {template.templateId}
                    </span>
                    <span
                      className={`${colors[index % colors.length]} font-bold`}
                    >
                      {template.count} uses ({template.percentage}%)
                    </span>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="text-center text-slate-400 py-6 border border-dashed border-slate-200 rounded-xl">
              No CV template usage yet
            </div>
          )}
        </AdminCard>

        <AdminTopPagesAnalytics />
      </div>

      <AdminCard className="mt-8" bodyClassName="flex flex-col">
        <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
          <div>
            <h2 className="text-xl font-bold text-slate-900">
              Subscription Distribution
            </h2>
            <p className="text-sm text-slate-500 mt-1">
              Breakdown of user tiers and market share
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 xl:grid-cols-12 gap-8 items-center">
          <div className="xl:col-span-4 flex justify-center">
            {loading ? (
              <div className="w-64 h-64 rounded-full bg-slate-50 animate-pulse border-4 border-slate-100 flex items-center justify-center text-slate-300">
                Charting...
              </div>
            ) : (
              <div className="relative w-full max-w-[280px]">
                <ResponsiveContainer width="100%" height={280}>
                  <PieChart>
                    <Pie
                      data={displaySubscriptionBreakdown}
                      cx="50%"
                      cy="50%"
                      innerRadius={75}
                      outerRadius={100}
                      paddingAngle={5}
                      dataKey="count"
                    >
                      {displaySubscriptionBreakdown.map((entry, index) => {
                        const colors = ["#94a3b8", "#3b82f6", "#8b5cf6", "#f59e0b", "#10b981"];
                        return <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />;
                      })}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
                <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                  <span className="text-3xl font-bold text-slate-900">
                    {totalSubscriptionUsers}
                  </span>
                  <span className="text-xs text-slate-500 font-medium uppercase tracking-widest">
                    Total Users
                  </span>
                </div>
              </div>
            )}
          </div>

          <div className="xl:col-span-8 grid grid-cols-1 md:grid-cols-2 gap-4">
            {loading ? (
              Array(4)
                .fill(0)
                .map((_, i) => (
                  <div
                    key={i}
                    className="h-28 bg-slate-50 rounded-2xl animate-pulse"
                  />
                ))
            ) : displaySubscriptionBreakdown.length > 0 ? (
              displaySubscriptionBreakdown.map((item) => {
                const key = String(item.plan || "")
                  .trim()
                  .toLowerCase();
                const getConfig = () => {
                  if (key === "free") {
                    return {
                      icon: <Users size={18} />,
                      color: "bg-slate-500",
                      light: "bg-slate-50",
                      text: "text-slate-600",
                      border: "border-slate-100",
                    };
                  }
                  if (key === "pro") {
                    return {
                      icon: <TrendingUp size={18} />,
                      color: "bg-blue-600",
                      light: "bg-blue-50",
                      text: "text-blue-700",
                      border: "border-blue-100",
                    };
                  }
                  if (key.includes("premium")) {
                    return {
                      icon: <Award size={18} />,
                      color: "bg-purple-600",
                      light: "bg-purple-50",
                      text: "text-purple-700",
                      border: "border-purple-100",
                    };
                  }
                  if (
                    key.includes("ultra") ||
                    key.includes("lifetime") ||
                    key.includes("life time")
                  ) {
                    return {
                      icon: <Crown size={18} />,
                      color: "bg-amber-500",
                      light: "bg-amber-50",
                      text: "text-amber-700",
                      border: "border-amber-100",
                    };
                  }
                  return {
                    icon: <Gem size={18} />,
                    color: "bg-slate-400",
                    light: "bg-slate-50",
                    text: "text-slate-600",
                    border: "border-slate-100",
                  };
                };

                const config = getConfig();
                const totalUsers = totalSubscriptionUsers || 1;
                const percentage = Math.round((item.count / totalUsers) * 100);

                return (
                  <div
                    key={item.plan}
                    className={`p-5 rounded-2xl border ${config.border} bg-white`}
                  >
                    <div className="flex items-center justify-between mb-3">
                      <div className="flex items-center gap-3">
                        <div
                          className={`p-2.5 rounded-xl ${config.light} ${config.text}`}
                        >
                          {config.icon}
                        </div>
                        <div>
                          <h4 className="font-bold text-slate-800">
                            {item.plan}
                          </h4>
                          <p className="text-[10px] uppercase tracking-wider text-slate-400 font-semibold">
                            Subscription Tier
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-2xl font-black text-slate-900 leading-none">
                          {item.count}
                        </p>
                        <p className="text-[10px] font-bold text-slate-400 mt-1">
                          {percentage}% SHARE
                        </p>
                      </div>
                    </div>
                    <div className="w-full bg-slate-100 rounded-full h-1.5 overflow-hidden">
                      <div
                        className={`h-full rounded-full ${config.color}`}
                        style={{ width: `${percentage}%` }}
                      ></div>
                    </div>
                  </div>
                );
              })
            ) : (
              <div className="col-span-2 text-center py-12 bg-slate-50 rounded-2xl border border-dashed border-slate-200">
                <Users size={32} className="mx-auto text-slate-300 mb-3" />
                <p className="text-slate-400 font-medium">
                  No subscription data discovered yet
                </p>
              </div>
            )}
          </div>
        </div>
      </AdminCard>

      <footer className="mt-14 text-center text-sm text-slate-500">
        Copyright {new Date().getFullYear()} AI Resume Builder - Analytics
      </footer>
    </div>
  );
}
