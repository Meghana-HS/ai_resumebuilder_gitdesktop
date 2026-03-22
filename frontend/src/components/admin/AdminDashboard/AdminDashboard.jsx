import React, { useEffect, useState, useCallback } from "react";
import {
  Users,
  FileText,
  CreditCard,
  IndianRupee,
  RefreshCw,
} from "lucide-react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Cell,
  LineChart,
  Line,
  PieChart,
  Pie,
} from "recharts";
import axiosInstance from "../../../api/axios";
import AdminCard from "../ui/AdminCard";
import AdminButton from "../ui/AdminButton";

export default function AdminDashboard() {
  const [totalUser, setTotalUser] = useState(0);
  const [totalUserChange, setTotalUserChange] = useState(0);
  const [totalActiveSub, setTotalActiveSub] = useState(0);
  const [totalActiveSubChange, setTotalActiveSubChange] = useState(0);
  const [totalRevenue, setTotalRevenue] = useState(0);
  const [totalRevenueChange, setTotalRevenueChange] = useState(0);
  const [totalResumeGen, setResumeGen] = useState(0);
  const [totalResumeGenChange, setTotalResumeGenChange] = useState(0);
  const [resumeChart, setResumeChart] = useState([]);

  const [subscriptionSplit, setSubscriptionSplit] = useState([]);
  const [userGrowth, setUserGrowth] = useState([]);
  const [dailyActivity, setDailyActivity] = useState([]);
  const [loading, setLoading] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);

  const colors = ["#3b82f6", "#10b981", "#8b5cf6", "#f59e0b"];

  const stats = [
    {
      title: "Total Users",
      value: totalUser,
      change: totalUserChange,
      icon: Users,
      iconBg: "bg-blue-50",
      iconColor: "text-blue-600",
    },
    {
      title: "Resumes Generated",
      value: totalResumeGen,
      change: totalResumeGenChange,
      icon: FileText,
      iconBg: "bg-indigo-50",
      iconColor: "text-indigo-600",
    },
    {
      title: "Active Subscriptions",
      value: totalActiveSub,
      change: totalActiveSubChange,
      icon: CreditCard,
      iconBg: "bg-purple-50",
      iconColor: "text-purple-600",
    },
    {
      title: "Total Revenue",
      value: `INR ${totalRevenue}`,
      change: totalRevenueChange,
      icon: IndianRupee,
      iconBg: "bg-emerald-50",
      iconColor: "text-emerald-600",
    },
  ];

  const fetchTotalUser = useCallback(async () => {
    try {
      setLoading(true);
      const result = await axiosInstance.get("/api/admin/dashboard-stat");
      setTotalUser(result.data?.users?.total || 0);
      setTotalUserChange(result.data?.users?.change || 0);
      setTotalActiveSub(result.data?.subscriptions?.total || 0);
      setTotalActiveSubChange(result.data?.subscriptions?.change || 0);
      setTotalRevenue(result.data?.revenue?.total || 0);
      setTotalRevenueChange(result.data?.revenue?.change || 0);
      setResumeGen(result.data?.resumes?.total || 0);
      setTotalResumeGenChange(result.data?.resumes?.change || 0);
      setResumeChart(result.data?.resumeChart || []);
      setSubscriptionSplit(result.data?.subscriptionSplit || []);
      setUserGrowth(result.data?.userGrowth || []);
      setDailyActivity(result.data?.dailyActiveUsers || []);
      setLastUpdated(new Date());
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTotalUser();
    const fetchInterval = setInterval(() => {
      fetchTotalUser();
    }, 4000);
    return () => clearInterval(fetchInterval);
  }, [fetchTotalUser]);

  return (
    <div className="bg-slate-50 p-4 sm:p-6">
      <div className="mb-6 sm:mb-8 rounded-3xl border border-slate-200 bg-white px-6 py-6 sm:px-8 sm:py-8 shadow-sm">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-xs uppercase tracking-[0.25em] text-slate-400">
              Executive Overview
            </p>
            <h1 className="text-2xl sm:text-3xl font-semibold mt-2 text-slate-900">
              Dashboard Overview
            </h1>
            <p className="text-sm text-slate-500 mt-2 max-w-xl">
              Unified snapshot of growth, subscriptions, and engagement.
            </p>
            <p className="text-[11px] text-slate-400 mt-3">
              {lastUpdated
                ? `Last updated ${lastUpdated.toLocaleTimeString()}`
                : "Fetching latest metrics..."}
            </p>
          </div>
          <AdminButton
            variant="secondary"
            size="sm"
            onClick={fetchTotalUser}
            className="border border-slate-300 w-full sm:w-auto"
          >
            <RefreshCw className="h-4 w-4" />
            Refresh
          </AdminButton>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 sm:gap-6">
        {loading
          ? Array.from({ length: 4 }).map((_, i) => (
              <div
                key={i}
                className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm animate-pulse"
              >
                <div className="h-4 w-24 rounded bg-slate-200" />
                <div className="mt-4 h-8 w-28 rounded bg-slate-200" />
                <div className="mt-3 h-4 w-16 rounded bg-slate-200" />
              </div>
            ))
          : stats.map((item) => {
              const Icon = item.icon;
              const isPositive = Number(item.change) >= 0;
              const changeLabel = `${isPositive ? "+" : ""}${item.change}%`;
              return (
                <AdminCard
                  key={item.title}
                  className="p-0"
                  header={
                    <div className="flex items-center justify-between">
                      <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-[0.2em]">
                        {item.title}
                      </p>
                      <div
                        className={`p-2.5 rounded-xl ${item.iconBg} ${item.iconColor}`}
                      >
                        <Icon size={18} />
                      </div>
                    </div>
                  }
                >
                  <div className="flex items-end justify-between">
                    <div>
                      <p className="text-2xl font-semibold text-slate-900">
                        {item.value}
                      </p>
                      <p className="mt-2 text-xs font-semibold text-slate-700">
                        {changeLabel} vs last period
                      </p>
                    </div>
                  </div>
                </AdminCard>
              );
            })}
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-4 sm:gap-6 mt-10">
        <div className="flex flex-col gap-3 sm:gap-6">
          <AdminCard
            className="h-[260px] sm:h-[350px] min-w-0 flex flex-col border-slate-200"
            bodyClassName="flex-1 flex flex-col"
          >
            <h3 className="text-xs sm:text-base font-semibold mb-4 text-center sm:text-left text-slate-800">
              Resume Generation
            </h3>
            <div className="flex-1 w-full min-h-0">
              {resumeChart.length === 0 ? (
                <div className="h-full flex items-center justify-center text-sm text-slate-400">
                  No resume data yet
                </div>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={resumeChart}
                    margin={{ top: 5, right: 0, left: -20, bottom: 0 }}
                  >
                    <XAxis
                      dataKey="month"
                      fontSize={10}
                      tickLine={false}
                      axisLine={false}
                      interval="preserveStartEnd"
                    />
                    <YAxis fontSize={10} tickLine={false} axisLine={false} />
                    <Tooltip />
                    <Bar dataKey="resumes" radius={[4, 4, 0, 0]}>
                      {resumeChart.map((_, i) => (
                        <Cell key={i} fill={colors[i % colors.length]} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </AdminCard>

          <AdminCard
            className="h-[260px] sm:h-[350px] min-w-0 flex flex-col border-slate-900/10"
            bodyClassName="flex-1 flex flex-col"
          >
            <h3 className="text-xs sm:text-base font-semibold mb-4 text-center sm:text-left text-slate-800">
              User Growth
            </h3>
            <div className="flex-1 w-full min-h-0">
              {userGrowth.length === 0 ? (
                <div className="h-full flex items-center justify-center text-sm text-slate-400">
                  No growth data yet
                </div>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart
                    data={userGrowth}
                    margin={{ top: 5, right: 0, left: -20, bottom: 0 }}
                  >
                    <XAxis
                      dataKey="month"
                      fontSize={10}
                      tickLine={false}
                      axisLine={false}
                      interval="preserveStartEnd"
                    />
                    <YAxis fontSize={10} tickLine={false} axisLine={false} />
                    <Tooltip />
                    <Line
                      type="monotone"
                      dataKey="users"
                      stroke="#0f172a"
                      strokeWidth={2}
                      dot={{ r: 2, fill: "#3b82f6" }}
                      activeDot={{ r: 4 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              )}
            </div>
          </AdminCard>
        </div>

        <div className="flex flex-col gap-3 sm:gap-6">
          <AdminCard
            className="h-[260px] sm:h-[350px] min-w-0 flex flex-col border-slate-200"
            bodyClassName="flex-1 flex flex-col"
          >
            <h3 className="text-xs sm:text-base font-semibold mb-4 text-center text-slate-800">
              Subscriptions
            </h3>

            <div className="flex-1 w-full min-h-0">
              {subscriptionSplit.length === 0 ? (
                <div className="h-full flex items-center justify-center text-sm text-slate-400">
                  No subscription data yet
                </div>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={subscriptionSplit}
                      dataKey="value"
                      innerRadius="40%"
                      outerRadius="70%"
                      paddingAngle={4}
                    >
                      {subscriptionSplit.map((_, i) => (
                        <Cell key={i} fill={colors[i % colors.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              )}
            </div>

            {subscriptionSplit.length > 0 ? (
              <div className="flex flex-wrap justify-center gap-2 sm:gap-6 mt-2 sm:mt-4">
                {subscriptionSplit.map((item, i) => (
                  <div
                    key={item.name}
                    className="flex items-center gap-1 sm:gap-2 text-[10px] sm:text-sm"
                  >
                    <span
                      className="w-2 h-2 sm:w-3 sm:h-3 rounded-full"
                      style={{ backgroundColor: colors[i] }}
                    />
                    <span className="text-gray-600 whitespace-nowrap">
                      {item.name} ({item.value}%)
                    </span>
                  </div>
                ))}
              </div>
            ) : null}
          </AdminCard>

          <AdminCard
            className="h-[260px] sm:h-[350px] min-w-0 flex flex-col border-slate-200"
            bodyClassName="flex-1 flex flex-col"
          >
            <h3 className="text-xs sm:text-base font-semibold mb-4 text-center sm:text-left text-slate-800">
              Active Users
            </h3>
            <div className="flex-1 w-full min-h-0">
              {dailyActivity.length === 0 ? (
                <div className="h-full flex items-center justify-center text-sm text-slate-400">
                  No active user data yet
                </div>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={dailyActivity}
                    margin={{ top: 5, right: 0, left: -20, bottom: 0 }}
                  >
                    <XAxis
                      dataKey="day"
                      fontSize={10}
                      tickLine={false}
                      axisLine={false}
                    />
                    <YAxis fontSize={10} tickLine={false} axisLine={false} />
                    <Tooltip />
                    <Bar dataKey="users" fill="#8b5cf6" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </AdminCard>
        </div>
      </div>
    </div>
  );
}
