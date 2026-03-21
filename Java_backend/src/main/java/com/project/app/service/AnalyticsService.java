package com.project.app.service;

import com.project.app.entity.*;
import com.project.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class AnalyticsService {

    @Autowired
    private ApiMetricRepository apiMetricRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DownloadRepository downloadRepository;

    @Autowired
    private PageViewRepository pageViewRepository;

    public void trackPageView(String page, String route, String userAgent, String ipAddress, Long userId) {
        PageView pageView = new PageView();
        pageView.setPage(page);
        pageView.setRoute(route);
        if (userId != null) {
            userRepository.findById(userId).ifPresent(pageView::setUser);
        }
        pageViewRepository.save(pageView);

        ApiMetric metric = new ApiMetric();
        metric.setEndpoint(route != null ? route : page);
        metric.setMethod("GET");
        metric.setResponseTime(0L);
        metric.setStatusCode(200);
        metric.setUserAgent(userAgent);
        metric.setIpAddress(ipAddress);
        metric.setTimestamp(LocalDateTime.now());
        if (userId != null) {
            userRepository.findById(userId).ifPresent(metric::setUser);
        }
        apiMetricRepository.save(metric);
    }

    public Map<String, Object> getUserStats(Long userId) {
        List<ApiMetric> userMetrics = apiMetricRepository.findByUserIdOrderByTimestampDesc(userId);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalPageViews", userMetrics.size());
        stats.put("uniquePages", userMetrics.stream().map(ApiMetric::getEndpoint).distinct().count());
        stats.put("lastActivity", userMetrics.isEmpty() ? null : userMetrics.get(0).getTimestamp());
        return stats;
    }

    public Map<String, Object> getAdminOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalUsers", userRepository.count());
        overview.put("totalPageViews", pageViewRepository.count());
        overview.put("activeUsers", userRepository.countByIsActiveTrue());
        overview.put("recentActivity", apiMetricRepository.findTop10ByOrderByTimestampDesc().stream().map(metric -> Map.of(
            "endpoint", metric.getEndpoint(),
            "method", metric.getMethod(),
            "timestamp", metric.getTimestamp(),
            "user", metric.getUser() != null ? metric.getUser().getUsername() : "Anonymous"
        )).toList());
        return overview;
    }

    public Map<String, Object> getAdminDashboardStats() {
        LocalDateTime lastMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastSixMonths = LocalDate.now().minusMonths(5).withDayOfMonth(1).atStartOfDay();
        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);

        List<User> users = userRepository.findAll();
        List<Resume> resumes = resumeRepository.findAll();
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        List<Payment> payments = paymentRepository.findAll();
        List<ApiMetric> apiMetrics = apiMetricRepository.findByTimestampBetweenOrderByTimestampDesc(last30Days, LocalDateTime.now());

        long totalUsers = users.size();
        long lastMonthUsers = users.stream().filter(user -> user.getCreatedAt() != null && user.getCreatedAt().isBefore(lastMonthStart)).count();
        long totalResumes = resumes.size();
        long lastMonthResumes = resumes.stream().filter(resume -> resume.getCreatedAt() != null && resume.getCreatedAt().isBefore(lastMonthStart)).count();
        long totalActiveSubs = subscriptions.stream().filter(subscription -> subscription.getStatus() == Subscription.SubscriptionStatus.ACTIVE).count();
        long lastMonthActiveSubs = subscriptions.stream()
            .filter(subscription -> subscription.getStatus() == Subscription.SubscriptionStatus.ACTIVE)
            .filter(subscription -> subscription.getCreatedAt() != null && subscription.getCreatedAt().isBefore(lastMonthStart))
            .count();

        double totalRevenue = payments.stream()
            .filter(payment -> payment.getStatus() == Payment.PaymentStatus.SUCCESS)
            .mapToDouble(Payment::getAmount)
            .sum();
        double lastMonthRevenue = payments.stream()
            .filter(payment -> payment.getStatus() == Payment.PaymentStatus.SUCCESS)
            .filter(payment -> payment.getCreatedAt() != null && payment.getCreatedAt().isBefore(lastMonthStart))
            .mapToDouble(Payment::getAmount)
            .sum();

        double userChange = lastMonthUsers == 0 ? 0 : ((double) (totalUsers - lastMonthUsers) / lastMonthUsers) * 100;
        double resumeChange = lastMonthResumes == 0 ? 0 : ((double) (totalResumes - lastMonthResumes) / lastMonthResumes) * 100;
        double subsChange = lastMonthActiveSubs == 0 ? 0 : ((double) (totalActiveSubs - lastMonthActiveSubs) / lastMonthActiveSubs) * 100;
        double revenueChange = lastMonthRevenue == 0 ? 0 : ((totalRevenue - lastMonthRevenue) / lastMonthRevenue) * 100;

        List<Map<String, Object>> resumeChart = buildMonthlyCountSeries(lastSixMonths, resumes.stream()
            .filter(resume -> resume.getCreatedAt() != null)
            .toList(), Resume::getCreatedAt, "resumes");

        List<Map<String, Object>> userGrowth = buildMonthlyCountSeries(lastSixMonths, users.stream()
            .filter(user -> user.getCreatedAt() != null)
            .toList(), User::getCreatedAt, "users");

        List<Map<String, Object>> dailyActiveUsers = buildDailyActiveUsers(users, 7);

        long successCalls = apiMetrics.stream().filter(metric -> metric.getStatusCode() != null && metric.getStatusCode() < 400).count();
        long failureCalls = apiMetrics.size() - successCalls;
        long totalCalls = successCalls + failureCalls;
        String apiSuccessRate = totalCalls > 0 ? String.format(Locale.US, "%.1f%%", (successCalls * 100.0) / totalCalls) : "100.0%";

        long freeUserCount = users.stream().filter(user -> !Boolean.TRUE.equals(user.getIsAdmin()) && Boolean.TRUE.equals(user.getIsActive()) && "free".equalsIgnoreCase(user.getPlan())).count();
        Map<String, Long> paidMap = new HashMap<>();
        subscriptions.stream()
            .filter(subscription -> subscription.getStatus() == Subscription.SubscriptionStatus.ACTIVE)
            .forEach(subscription -> paidMap.merge(normalizeSubscriptionPlan(subscription.getPlan()), 1L, Long::sum));
        double subscriptionTotal = freeUserCount + paidMap.values().stream().mapToLong(Long::longValue).sum();
        List<Map<String, Object>> subscriptionSplit = List.of(
            buildSubscriptionSplitItem("Free", freeUserCount, subscriptionTotal),
            buildSubscriptionSplitItem("Pro", paidMap.getOrDefault("Pro", 0L), subscriptionTotal),
            buildSubscriptionSplitItem("Lifetime", paidMap.getOrDefault("Lifetime", 0L), subscriptionTotal)
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("users", Map.of("total", totalUsers, "change", round1(userChange)));
        response.put("resumes", Map.of("total", totalResumes, "change", round1(resumeChange)));
        response.put("subscriptions", Map.of("total", totalActiveSubs, "change", round1(subsChange)));
        response.put("revenue", Map.of("total", Math.round(totalRevenue), "change", round1(revenueChange)));
        response.put("apiMetrics", Map.of("totalCalls", totalCalls, "successRate", apiSuccessRate));
        response.put("resumeChart", resumeChart);
        response.put("userGrowth", userGrowth);
        response.put("dailyActiveUsers", dailyActiveUsers);
        response.put("subscriptionSplit", subscriptionSplit);
        return response;
    }

    public Map<String, Object> getAnalyticsStats() {
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);

        List<User> users = userRepository.findAll();
        List<Plan> plans = planRepository.findAllByOrderByOrderAsc();
        List<Payment> payments = paymentRepository.findAll();
        List<Notification> notifications = notificationRepository.findAll();
        List<ApiMetric> metrics = apiMetricRepository.findByTimestampBetweenOrderByTimestampDesc(last30Days, LocalDateTime.now());
        List<Download> downloads = downloadRepository.findAll();

        long newUsersLast30Days = users.stream().filter(user -> user.getCreatedAt() != null && !user.getCreatedAt().isBefore(last30Days)).count();
        long activeUsersLast7Days = users.stream()
            .filter(user -> !Boolean.TRUE.equals(user.getIsAdmin()))
            .filter(user -> user.getLastLogin() != null && !user.getLastLogin().isBefore(last7Days))
            .count();
        long deletedUsersCount = notifications.stream().filter(notification -> "USER_DELETED".equals(notification.getType())).count();

        Map<String, String> canonicalPlanByKey = new LinkedHashMap<>();
        for (Plan plan : plans) {
            if (plan.getName() != null) {
                canonicalPlanByKey.put(plan.getName().trim().toLowerCase(Locale.ROOT), plan.getName().trim());
            }
        }
        canonicalPlanByKey.putIfAbsent("free", "Free");

        Map<String, Long> groupedPlanCounts = new LinkedHashMap<>();
        for (User user : users) {
            if (Boolean.TRUE.equals(user.getIsAdmin())) {
                continue;
            }
            String planName = normalizePlanName(user.getPlan(), canonicalPlanByKey);
            groupedPlanCounts.merge(planName, 1L, Long::sum);
        }

        List<String> orderedPlans = new ArrayList<>();
        orderedPlans.add("Free");
        for (Plan plan : plans) {
            String normalized = normalizePlanName(plan.getName(), canonicalPlanByKey);
            if (!orderedPlans.contains(normalized)) {
                orderedPlans.add(normalized);
            }
        }
        for (String key : groupedPlanCounts.keySet()) {
            if (!orderedPlans.contains(key)) {
                orderedPlans.add(key);
            }
        }

        List<Map<String, Object>> subscriptionBreakdown = orderedPlans.stream()
            .map(plan -> Map.<String, Object>of("plan", plan, "count", groupedPlanCounts.getOrDefault(plan, 0L)))
            .filter(item -> ((Long) item.get("count")) > 0)
            .toList();

        long totalPaidUsers = subscriptionBreakdown.stream()
            .filter(item -> !"free".equalsIgnoreCase((String) item.get("plan")))
            .mapToLong(item -> (Long) item.get("count"))
            .sum();

        long apiSuccessCount = metrics.stream().filter(metric -> metric.getStatusCode() != null && metric.getStatusCode() < 400).count();
        long apiFailureCount = metrics.size() - apiSuccessCount;
        double totalRespTime = metrics.stream().filter(metric -> metric.getResponseTime() != null).mapToLong(ApiMetric::getResponseTime).sum();
        long callsForAvg = metrics.stream().filter(metric -> metric.getResponseTime() != null).count();
        long totalApiCalls = apiSuccessCount + apiFailureCount;

        String apiSuccessRate = totalApiCalls > 0 ? String.format(Locale.US, "%.1f%%", (apiSuccessCount * 100.0) / totalApiCalls) : "100.0%";
        String apiFailureRate = totalApiCalls > 0 ? String.format(Locale.US, "%.1f%%", (apiFailureCount * 100.0) / totalApiCalls) : "0.0%";
        long avgResponseTime = callsForAvg > 0 ? Math.round(totalRespTime / callsForAvg) : 250;

        List<Map<String, Object>> chartData = buildTrendData(users, payments);

        Map<String, Integer> resumeTemplateCountMap = new HashMap<>();
        Map<String, Integer> cvTemplateCountMap = new HashMap<>();
        for (Download download : downloads) {
            if (download.getAction() != Download.Action.DOWNLOAD || download.getTemplate() == null || download.getTemplate().isBlank()) {
                continue;
            }
            String readableName = toReadableTemplateName(download.getTemplate());
            if (download.getType() == Download.DocumentType.RESUME) {
                resumeTemplateCountMap.merge(readableName, 1, Integer::sum);
            } else if (download.getType() == Download.DocumentType.CV) {
                cvTemplateCountMap.merge(readableName, 1, Integer::sum);
            }
        }

        List<Map<String, Object>> mostUsedResumeTemplates = buildTopTemplates(resumeTemplateCountMap);
        List<Map<String, Object>> mostUsedCvTemplates = buildTopTemplates(cvTemplateCountMap);

        Map<String, Integer> combined = new HashMap<>(resumeTemplateCountMap);
        cvTemplateCountMap.forEach((key, value) -> combined.merge(key, value, Integer::sum));
        List<Map<String, Object>> mostUsedTemplates = buildTopTemplates(combined);

        double uptimeDeduction = (100 - Double.parseDouble(apiSuccessRate.replace("%", ""))) * 0.01;
        String systemUptime = String.format(Locale.US, "%.2f%%", Math.max(99.90, 99.95 - uptimeDeduction));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userGrowth", Map.of("count", newUsersLast30Days, "note", "New users in last 30 days"));
        response.put("conversions", Map.of("count", totalPaidUsers, "note", "Total paid subscriptions"));
        response.put("activeUsers", Map.of("count", activeUsersLast7Days, "note", "Active last 7 days"));
        response.put("deletedUsers", Map.of("count", deletedUsersCount, "note", "Total deleted accounts"));
        response.put("mostUsedResumeTemplates", mostUsedResumeTemplates);
        response.put("mostUsedCvTemplates", mostUsedCvTemplates);
        response.put("mostUsedTemplates", mostUsedTemplates);
        response.put("chartData", chartData);
        response.put("subscriptionBreakdown", subscriptionBreakdown);
        response.put("summary", Map.of(
            "apiSuccessRate", apiSuccessRate,
            "apiFailureRate", apiFailureRate,
            "avgResponseTime", avgResponseTime + "ms",
            "totalApiCalls", totalApiCalls,
            "systemUptime", systemUptime
        ));
        return response;
    }

    public List<Map<String, Object>> getTopViewedPages() {
        Map<String, Set<Long>> uniqueUsers = new HashMap<>();
        Map<String, Integer> viewCounts = new HashMap<>();

        for (PageView pageView : pageViewRepository.findAll()) {
            viewCounts.merge(pageView.getPage(), 1, Integer::sum);
            uniqueUsers.computeIfAbsent(pageView.getPage(), key -> new HashSet<>());
            if (pageView.getUser() != null) {
                uniqueUsers.get(pageView.getPage()).add(pageView.getUser().getId());
            }
        }

        return viewCounts.entrySet().stream()
            .sorted((left, right) -> Integer.compare(right.getValue(), left.getValue()))
            .limit(5)
            .map(entry -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("page", entry.getKey());
                row.put("views", entry.getValue());
                row.put("uniqueUsers", uniqueUsers.getOrDefault(entry.getKey(), Set.of()).size());
                return row;
            })
            .toList();
    }

    private <T> List<Map<String, Object>> buildMonthlyCountSeries(LocalDateTime startMonth, List<T> source, java.util.function.Function<T, LocalDateTime> extractor, String valueKey) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            LocalDate current = startMonth.toLocalDate().plusMonths(i);
            int year = current.getYear();
            int month = current.getMonthValue();
            long count = source.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .filter(ts -> ts.getYear() == year && ts.getMonthValue() == month)
                .count();

            result.add(Map.of(
                "month", current.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                valueKey, count
            ));
        }
        return result;
    }

    private List<Map<String, Object>> buildDailyActiveUsers(List<User> users, int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            long count = users.stream()
                .filter(user -> user.getLastLogin() != null)
                .filter(user -> user.getLastLogin().toLocalDate().isEqual(day))
                .count();
            result.add(Map.of(
                "day", day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                "users", count
            ));
        }
        return result;
    }

    private Map<String, Object> buildSubscriptionSplitItem(String name, long count, double total) {
        double value = total == 0 ? 0 : Math.round(((count / total) * 100.0) * 100.0) / 100.0;
        return Map.of("name", name, "value", value);
    }

    private String normalizeSubscriptionPlan(Subscription.SubscriptionPlan plan) {
        if (plan == null) return "Unknown";
        return switch (plan) {
            case FREE -> "Free";
            case PRO -> "Pro";
            case LIFETIME -> "Lifetime";
        };
    }

    private String normalizePlanName(String rawPlan, Map<String, String> canonicalPlanByKey) {
        String raw = rawPlan == null || rawPlan.isBlank() ? "Free" : rawPlan.trim();
        String key = raw.toLowerCase(Locale.ROOT);
        if (canonicalPlanByKey.containsKey(key)) {
            return canonicalPlanByKey.get(key);
        }
        if (List.of("lifetime", "life time").contains(key)) {
            return canonicalPlanByKey.getOrDefault("ultra pro", "Ultra Pro");
        }
        return Arrays.stream(raw.split("\\s+"))
            .filter(part -> !part.isBlank())
            .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1).toLowerCase(Locale.ROOT))
            .reduce((left, right) -> left + " " + right)
            .orElse("Free");
    }

    private List<Map<String, Object>> buildTrendData(List<User> users, List<Payment> payments) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate current = LocalDate.now().minusMonths(i);
            int year = current.getYear();
            int month = current.getMonthValue();
            long userCount = users.stream()
                .filter(user -> user.getCreatedAt() != null && user.getCreatedAt().getYear() == year && user.getCreatedAt().getMonthValue() == month)
                .count();
            double revenue = payments.stream()
                .filter(payment -> payment.getStatus() == Payment.PaymentStatus.SUCCESS)
                .filter(payment -> payment.getCreatedAt() != null && payment.getCreatedAt().getYear() == year && payment.getCreatedAt().getMonthValue() == month)
                .mapToDouble(Payment::getAmount)
                .sum();
            trendData.add(Map.of(
                "month", current.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                "users", userCount,
                "revenue", revenue
            ));
        }
        return trendData;
    }

    private List<Map<String, Object>> buildTopTemplates(Map<String, Integer> countMap) {
        int total = countMap.values().stream().mapToInt(Integer::intValue).sum();
        return countMap.entrySet().stream()
            .sorted((left, right) -> Integer.compare(right.getValue(), left.getValue()))
            .limit(5)
            .map(entry -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("templateId", entry.getKey());
                item.put("count", entry.getValue());
                item.put("percentage", total > 0 ? Math.round((entry.getValue() * 100.0) / total) : 0);
                return item;
            })
            .toList();
    }

    private String toReadableTemplateName(String value) {
        if (value == null || value.isBlank()) {
            return "Standard";
        }
        Map<String, String> canonicalNames = Map.ofEntries(
            Map.entry("professional", "Professional"),
            Map.entry("modern", "Modern"),
            Map.entry("creative", "Creative"),
            Map.entry("minimal", "Minimal"),
            Map.entry("executive", "Executive"),
            Map.entry("academic", "Academic"),
            Map.entry("twocolumn", "Two Column ATS"),
            Map.entry("simple", "Simple"),
            Map.entry("academicsidebar", "Academic Sidebar"),
            Map.entry("elegant", "Clinica Elegant"),
            Map.entry("vertex", "Vertex Sidebar"),
            Map.entry("elite", "Elite Sidebar"),
            Map.entry("eclipse", "Eclipse"),
            Map.entry("eclipse1", "Eclipse Alt"),
            Map.entry("harbor", "Harbor")
        );

        String key = value.replace("-", "").replace("_", "").toLowerCase(Locale.ROOT);
        if (canonicalNames.containsKey(key)) {
            return canonicalNames.get(key);
        }

        if (value.length() > 40) {
            return "ID: " + value.substring(0, 8) + "...";
        }

        return Arrays.stream(value.replace("-", " ").replace("_", " ").trim().split("\\s+"))
            .filter(part -> !part.isBlank())
            .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1))
            .reduce((left, right) -> left + " " + right)
            .orElse("Standard");
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
