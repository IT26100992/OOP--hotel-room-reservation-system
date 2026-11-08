package com.hotel.admin.api;

import com.hotel.admin.dto.DashboardSummary;
import com.hotel.admin.service.AdminReportService;
import io.javalin.config.JavalinConfig;

    public class AdminReportApi {

        private final AdminReportService adminReportService;

        public AdminReportApi(
                AdminReportService adminReportService) {

            this.adminReportService = adminReportService;
        }

        public void registerRoutes(JavalinConfig config) {

            // READ - Get current hotel dashboard statistics
            config.routes.get("/api/admin/dashboard", ctx -> {

                DashboardSummary summary =
                        adminReportService.getDashboardSummary();

                ctx.status(200).json(summary);
            });
        }
    }

