"use strict";

// ==========================================
// ADMIN DASHBOARD & REPORTS
// ==========================================

const API_BASE = "/api/admin";

const messageElement = document.getElementById("message");
const reportsTableBody =
    document.getElementById("reportsTableBody");

const editDialog =
    document.getElementById("editReportDialog");

// Currently loaded report records
let savedReports = [];

// ==========================================
// COMMON UTILITIES
// ==========================================

function showMessage(message, type = "success") {
    messageElement.textContent = message;
    messageElement.className = `message ${type}`;
}

function clearMessage() {
    messageElement.textContent = "";
    messageElement.className = "message";
}

function setText(elementId, value) {
    const element = document.getElementById(elementId);

    if (element) {
        element.textContent = value;
    }
}

function formatMoney(amount) {
    const value = Number(amount ?? 0);

    if (!Number.isFinite(value)) {
        return "—";
    }

    return "LKR " + new Intl.NumberFormat("en-LK", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(value);
}


function formatDate(value) {

    if (!value) {
        return "—";
    }

    if (Array.isArray(value)) {

        const [year, month, day] = value;

        return `${String(day).padStart(2, "0")}/` +
            `${String(month).padStart(2, "0")}/` +
            year;
    }

    if (typeof value === "string") {

        const match =
            /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);

        if (match) {
            return `${match[3]}/${match[2]}/${match[1]}`;
        }

        return value;
    }

    return "—";
}



function formatDateTime(value) {

    if (!value) {
        return "—";
    }

    // Handle LocalDateTime as an array
    if (Array.isArray(value)) {

        const [
            year,
            month,
            day,
            hour = 0,
            minute = 0
        ] = value;

        const pad = number =>
            String(number).padStart(2, "0");

        return `${pad(day)}/${pad(month)}/${year} ` +
            `${pad(hour)}:${pad(minute)}`;
    }

    // Handle LocalDateTime as an ISO string
    if (typeof value === "string") {
        return value.replace("T", " ").split(".")[0];
    }

    return "—";
}


function setButtonLoading(button, loading, label) {
    button.disabled = loading;
    button.textContent = loading ? "Please wait..." : label;
}

// Shared helper for API requests
async function apiRequest(endpoint, options = {}) {

    const response = await fetch(
        API_BASE + endpoint,
        {
            ...options,
            headers: {
                "Accept": "application/json",
                ...(options.body
                    ? { "Content-Type": "application/json" }
                    : {}),
                ...options.headers
            }
        }
    );

    if (response.status === 204) {
        return null;
    }

    const contentType =
        response.headers.get("content-type") || "";

    let data = null;

    if (contentType.includes("application/json")) {
        data = await response.json();
    } else {
        data = await response.text();
    }

    if (!response.ok) {
        const errorMessage =
            data && typeof data === "object"
                ? (data.error || data.message)
                : null;

        throw new Error(
            errorMessage || `Request failed (${response.status})`
        );
    }

    return data;
}

// ==========================================
// DASHBOARD STATISTICS
// ==========================================

async function loadDashboard() {

    try {
        const summary = await apiRequest("/dashboard");

        setText("totalRooms", summary.totalRooms);
        setText("availableRooms", summary.availableRooms);
        setText("totalUsers", summary.totalUsers);

        setText(
            "totalReservations",
            summary.totalReservations
        );

        setText(
            "confirmedReservations",
            summary.confirmedReservations
        );

        setText(
            "pendingReservations",
            summary.pendingReservations
        );

        setText(
            "grossCompletedPayments",
            formatMoney(summary.grossCompletedPayments)
        );

        setText(
            "completedPayments",
            summary.completedPayments
        );

        renderReservationChart(summary);
        renderRoomChart(summary);

    } catch (error) {
        showMessage(
            "Unable to load dashboard: " + error.message,
            "error"
        );

        document.getElementById("reservationChart")
            .textContent = "Dashboard data unavailable.";

        document.getElementById("roomChart")
            .textContent = "Dashboard data unavailable.";
    }
}

// ==========================================
// SIMPLE CSS BAR CHARTS
// ==========================================

function renderBarChart(containerId, items) {

    const container = document.getElementById(containerId);

    // Clear old chart
    container.replaceChildren();

    const maximum = Math.max(
        1,
        ...items.map(item => Number(item.value) || 0)
    );

    items.forEach(item => {

        const row = document.createElement("div");
        row.className = "chart-row";

        const label = document.createElement("span");
        label.className = "chart-label";
        label.textContent = item.label;

        const track = document.createElement("div");
        track.className = "chart-track";

        const fill = document.createElement("div");
        fill.className = "chart-fill";

        const value = Math.max(0, Number(item.value) || 0);
        fill.style.width = `${(value / maximum) * 100}%`;

        const count = document.createElement("span");
        count.className = "chart-value";
        count.textContent = value;

        track.appendChild(fill);

        row.appendChild(label);
        row.appendChild(track);
        row.appendChild(count);

        container.appendChild(row);
    });
}

function renderReservationChart(summary) {

    renderBarChart("reservationChart", [
        {
            label: "Pending",
            value: summary.pendingReservations
        },
        {
            label: "Confirmed",
            value: summary.confirmedReservations
        },
        {
            label: "Cancelled",
            value: summary.cancelledReservations
        },
        {
            label: "Completed",
            value: summary.completedReservations
        }
    ]);
}

function renderRoomChart(summary) {

    renderBarChart("roomChart", [
        {
            label: "Available",
            value: summary.availableRooms
        },
        {
            label: "Maintenance",
            value: summary.maintenanceRooms
        },
        {
            label: "Inactive",
            value: summary.inactiveRooms
        }
    ]);
}

// ==========================================
// READ - LOAD SAVED REPORTS
// ==========================================

async function loadReports() {

    try {
        const reports = await apiRequest("/reports");

        if (!Array.isArray(reports)) {
            throw new Error("Invalid reports response.");
        }

        savedReports = reports;
        renderReportsTable();

    } catch (error) {
        savedReports = [];

        reportsTableBody.replaceChildren();

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 6;
        cell.textContent =
            "Unable to load reports: " + error.message;

        row.appendChild(cell);
        reportsTableBody.appendChild(row);

        showMessage(
            "Unable to load saved reports: " + error.message,
            "error"
        );
    }
}

// ==========================================
// DISPLAY REPORTS TABLE
// ==========================================

function renderReportsTable() {

    reportsTableBody.replaceChildren();

    if (savedReports.length === 0) {

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 6;
        cell.textContent =
            "No saved reports yet. Generate your first report.";

        row.appendChild(cell);
        reportsTableBody.appendChild(row);

        return;
    }

    // Newest generated reports first

    const sortedReports = [...savedReports].sort((a, b) => {

        function dateValue(value) {

            if (!value) return 0;

            // Jackson may return LocalDateTime as an array
            if (Array.isArray(value)) {
                const [year, month, day, hour = 0, minute = 0, second = 0] = value;

                return new Date(
                    year,
                    month - 1,
                    day,
                    hour,
                    minute,
                    second
                ).getTime();
            }

            // Also support ISO date-time strings
            if (typeof value === "string") {
                return new Date(value).getTime() || 0;
            }

            return 0;
        }

        return dateValue(b.generatedAt) -
            dateValue(a.generatedAt);
    });


    sortedReports.forEach(report => {

        const row = document.createElement("tr");

        const titleCell = document.createElement("td");
        titleCell.textContent = report.title || "Untitled";

        const datesCell = document.createElement("td");
        datesCell.textContent =
            `${formatDate(report.startDate)} - ` +
            formatDate(report.endDate);

        const reservationsCell =
            document.createElement("td");

        reservationsCell.textContent =
            report.totalReservations ?? 0;

        const revenueCell = document.createElement("td");
        revenueCell.textContent =
            formatMoney(report.totalRevenue);

        const generatedCell = document.createElement("td");
        generatedCell.textContent =
            formatDateTime(report.generatedAt);

        const actionsCell = document.createElement("td");

        const actions = document.createElement("div");
        actions.className = "table-actions";

        // Edit button
        const editButton = document.createElement("button");
        editButton.type = "button";
        editButton.className =
            "btn btn-outline btn-small";

        editButton.textContent = "Edit";

        editButton.addEventListener("click", () => {
            openEditDialog(report.id);
        });

        // Delete button
        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className =
            "btn btn-danger btn-small";

        deleteButton.textContent = "Delete";

        deleteButton.addEventListener("click", () => {
            deleteReport(report.id);
        });

        actions.appendChild(editButton);
        actions.appendChild(deleteButton);

        actionsCell.appendChild(actions);

        row.appendChild(titleCell);
        row.appendChild(datesCell);
        row.appendChild(reservationsCell);
        row.appendChild(revenueCell);
        row.appendChild(generatedCell);
        row.appendChild(actionsCell);

        reportsTableBody.appendChild(row);
    });
}

// ==========================================
// CREATE - GENERATE REPORT
// ==========================================

async function generateReport(event) {

    event.preventDefault();
    clearMessage();

    const form = document.getElementById(
        "generateReportForm"
    );

    const submitButton = form.querySelector(
        'button[type="submit"]'
    );

    const request = {
        title: document.getElementById("reportTitle")
            .value.trim(),

        startDate: document.getElementById("startDate")
            .value,

        endDate: document.getElementById("endDate")
            .value,

        notes: document.getElementById("reportNotes")
            .value.trim()
    };

    if (!request.title ||
        !request.startDate ||
        !request.endDate) {

        showMessage(
            "Please complete all required fields.",
            "error"
        );
        return;
    }

    if (request.endDate < request.startDate) {
        showMessage(
            "End date cannot be before start date.",
            "error"
        );
        return;
    }

    setButtonLoading(submitButton, true, "Generate Report");

    try {

        await apiRequest("/reports", {
            method: "POST",
            body: JSON.stringify(request)
        });

        form.reset();

        await loadReports();

        showMessage(
            "Report generated successfully.",
            "success"
        );

    } catch (error) {
        showMessage(
            "Unable to generate report: " + error.message,
            "error"
        );
    } finally {
        setButtonLoading(
            submitButton,
            false,
            "Generate Report"
        );
    }
}

// ==========================================
// UPDATE - OPEN EDIT DIALOG
// ==========================================

function openEditDialog(reportId) {

    const report = savedReports.find(
        item => item.id === reportId
    );

    if (!report) {
        showMessage("Report not found.", "error");
        return;
    }

    document.getElementById("editReportId").value =
        report.id;

    document.getElementById("editReportTitle").value =
        report.title || "";

    document.getElementById("editReportNotes").value =
        report.notes || "";

    editDialog.showModal();
}

// ==========================================
// UPDATE - SAVE CHANGES
// ==========================================

async function updateReport(event) {

    event.preventDefault();
    clearMessage();

    const form = document.getElementById(
        "editReportForm"
    );

    const submitButton = form.querySelector(
        'button[type="submit"]'
    );

    const reportId =
        document.getElementById("editReportId").value;

    const request = {
        title: document.getElementById(
            "editReportTitle"
        ).value.trim(),

        notes: document.getElementById(
            "editReportNotes"
        ).value.trim()
    };

    if (!request.title) {
        showMessage("Report title is required.", "error");
        return;
    }

    setButtonLoading(submitButton, true, "Save Changes");

    try {

        await apiRequest(
            `/reports/${encodeURIComponent(reportId)}`,
            {
                method: "PUT",
                body: JSON.stringify(request)
            }
        );

        editDialog.close();

        await loadReports();

        showMessage(
            "Report updated successfully.",
            "success"
        );

    } catch (error) {
        showMessage(
            "Unable to update report: " + error.message,
            "error"
        );
    } finally {
        setButtonLoading(
            submitButton,
            false,
            "Save Changes"
        );
    }
}

// ==========================================
// DELETE - REMOVE SAVED REPORT
// ==========================================

async function deleteReport(reportId) {

    const confirmed = window.confirm(
        "Are you sure you want to delete this report?"
    );

    if (!confirmed) {
        return;
    }

    clearMessage();

    try {

        await apiRequest(
            `/reports/${encodeURIComponent(reportId)}`,
            {
                method: "DELETE"
            }
        );

        await loadReports();

        showMessage(
            "Report deleted successfully.",
            "success"
        );

    } catch (error) {
        showMessage(
            "Unable to delete report: " + error.message,
            "error"
        );
    }
}

// ==========================================
// EVENT LISTENERS
// ==========================================

document.getElementById("refreshDashboard")
    .addEventListener("click", loadDashboard);

document.getElementById("refreshReports")
    .addEventListener("click", loadReports);

document.getElementById("generateReportForm")
    .addEventListener("submit", generateReport);

document.getElementById("editReportForm")
    .addEventListener("submit", updateReport);

document.getElementById("cancelEdit")
    .addEventListener("click", () => {
        editDialog.close();
    });

// ==========================================
// INITIAL PAGE LOADING
// ==========================================

async function initializeDashboard() {

    await Promise.all([
        loadDashboard(),
        loadReports()
    ]);
}

GrandStaySession.ready.then(user => {
    if (user?.role === "ADMIN") initializeDashboard();
});
