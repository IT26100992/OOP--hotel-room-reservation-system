package com.hotel.admin.api;

import com.hotel.admin.dto.DashboardSummary;
import com.hotel.admin.service.AdminReportService;
import io.javalin.config.JavalinConfig;
import com.hotel.admin.dto.GenerateReportRequest;
import com.hotel.admin.dto.UpdateReportRequest;
import com.hotel.admin.model.Report;
import com.hotel.common.AccessControl;

    public class AdminReportApi {

        private final AdminReportService adminReportService;
        private final AccessControl accessControl;

        public AdminReportApi(
                AdminReportService adminReportService,
                AccessControl accessControl) {

            this.adminReportService = adminReportService;
            this.accessControl = accessControl;
        }

        public void registerRoutes(JavalinConfig config) {

            // READ - Get current hotel dashboard statistics
            config.routes.get("/api/admin/dashboard", ctx -> {
                accessControl.requireAdmin(ctx);
                DashboardSummary summary =
                        adminReportService.getDashboardSummary();

                ctx.status(200).json(summary);
            });


            // CREATE - Generate and save a report
            config.routes.post("/api/admin/reports", ctx -> {
                accessControl.requireAdmin(ctx);
                GenerateReportRequest request =
                        ctx.bodyAsClass(GenerateReportRequest.class);

                Report report =
                        adminReportService.generateReport(request);

                ctx.status(201).json(report);
            });

            // READ - List saved reports
            config.routes.get("/api/admin/reports", ctx -> {
                accessControl.requireAdmin(ctx);
                ctx.json(adminReportService.getAllReports());
            });

            // READ - Get one report
            config.routes.get("/api/admin/reports/{id}", ctx -> {
                accessControl.requireAdmin(ctx);
                ctx.json(adminReportService.getReportById(
                        ctx.pathParam("id")
                ));
            });

            // UPDATE - Edit report metadata
            config.routes.put("/api/admin/reports/{id}", ctx -> {
                accessControl.requireAdmin(ctx);
                UpdateReportRequest request =
                        ctx.bodyAsClass(UpdateReportRequest.class);

                ctx.json(adminReportService.updateReport(
                        ctx.pathParam("id"), request
                ));
            });

            // DELETE - Remove report
            config.routes.delete("/api/admin/reports/{id}", ctx -> {
                accessControl.requireAdmin(ctx);
                adminReportService.deleteReport(ctx.pathParam("id"));
                ctx.status(204);
            });

        }
    }

