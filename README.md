# Smart Daily Expense Tracker

A comprehensive expense tracking module for small business owners built with Jetpack Compose and MVVM architecture.

## 📱 Features

- **Quick Expense Entry** - Title, amount, category, notes, receipt photos
- **Real-time Daily Total** - Live spending updates
- **Expense Lists** - View by date with filtering and grouping
- **Visual Reports** - 7-day charts and category breakdowns
- **Export & Share** - PDF/CSV reports with share integration
- **Dark/Light Theme** support   - system support

## 🏗️ Tech Stack

- **Jetpack Compose** - Modern declarative UI
- **MVVM + StateFlow** - Reactive architecture
- **Room Database** - Local data persistence
- **Navigation Component** - Screen transitions
- **Material Design 3** - Modern UI components

## 📋 Screens

1. **Expense Entry** - Form with validation, categories (Staff, Travel, Food, Utility), receipt upload
2. **Expense List** - Today's expenses, date filtering, category grouping, empty states
3. **Reports** - 7-day analysis, category charts, export options

## 🚀 Getting Started

```bash
git clone https://github.com/oyehanif/ExpenseTracker.git
cd smart-expense-tracker
```

Open in Android Studio, sync Gradle, and run.

## 🤖 AI-First Development

**AI Tools Used:** ChatGPT, Claude, UIzard

**Usage Summary:** AI tools accelerated development by 60% through architectural planning, UI component generation, ViewModel implementation, and validation logic creation. Used for MVVM structure design, Compose layouts, StateFlow management, and comprehensive documentation.


## 📱 Screenshots

| Entry | Reports | List |
|:---:|:---:|:---:|
| ![WhatsApp Image 2025-08-10 at 5 00 34 PM](https://github.com/user-attachments/assets/fdde5bab-d890-49d9-aa30-d6e1165f0335) | ![WhatsApp Image 2025-08-10 at 5 00 34 PM (1)](https://github.com/user-attachments/assets/1c8ea075-b649-4edc-b3c2-11460d802512) | ![WhatsApp Image 2025-08-10 at 5 00 34 PM (2)](https://github.com/user-attachments/assets/2eb6c0d4-c8b4-4f12-b20b-c1a19bba7b78) |

## Build/APK 
- You can find the apk build here : https://drive.google.com/file/d/1nmdQe1lKp8EUVjHvsDi5NDeGoxBsQx_6/view?usp=sharing

## 🚀 Bonus Features

- ✅ Theme switcher (Light/Dark)
- ✅ Local data persistence (Room)
- ✅ Entry animations
- ✅ Validation (amount > 0, non-empty title)
- ✅ Offline-first design
- ✅ Reusable UI components

**Development Metrics:** 4 hours total, 70% AI-assisted, 85%+ test coverage

** I understand this task was given with a 40-hour timeline, but due to a family function I could only dedicate limited time, so I’ve completed 99% of it in 4 hours, and I’ll ensure it’s fully polished and refined soon.

**Key AI Prompts:**
- "S – Situation
I am building an Android application using Jetpack Compose with MVVM architecture and Hilt for dependency injection. The app is an Expense Tracker that records and displays expenses. I want to create an Expense List Screen that is modern, clean, and optimized for performance, following Jetpack Compose and Android best practices. The data comes from a Room Database via a repository and ViewModel.

The default view should show today’s expenses but allow the user to filter by previous dates using a calendar picker or custom filter options. Users should also be able to group the list by category or by time using a toggle. The screen should show the total expense count and total amount for the selected filter. If there are no expenses, an empty state UI should be displayed with an icon and message.

T – Task
Your task is to generate full working code in Kotlin for the Expense List Screen in Jetpack Compose, following the latest Android best practices. The code should:

Use MVVM architecture with a BaseViewModel to reduce repetition in state management.

Integrate with Room Database for expense data.

Use Hilt for dependency injection.

Include state handling (loading, success, empty, error).

Provide filtering (by date, via calendar picker or filter) and grouping (by category or time) features.

Show total count and total amount dynamically based on current filter/grouping.

Display a Recycler/List view of expenses with a clean UI using Compose LazyColumn.

Show an empty state if there are no results for the applied filter.

Follow Material 3 guidelines with good spacing, typography, and accessibility.

A – Action
When generating the code, ensure you:

Create data models for Expense with fields like id, title, amount, category, date, and optional note.

Provide a DAO with queries to get:

Expenses for today.

Expenses for a selected date.

Expenses grouped by category.

Expenses grouped by time.

Implement a Repository to manage data fetching from Room.

Implement a ViewModel that:

Holds UI state in a UiState data class.

Handles date selection, grouping toggle, and calculations for total count and amount.

Exposes a StateFlow or MutableState to the UI.

Implement the Composable Screen that:

Has a TopAppBar with title "Expenses".

Shows filter buttons (Today / Calendar date picker / Group toggle).

Displays total count and total amount at the top.

Shows the expenses list or empty state.

Uses rememberLauncherForActivityResult or equivalent for the calendar picker.

Ensure the UI reacts instantly to filter changes using Compose’s state management.

R – Result
The result should be:

A fully functional Expense List Screen built with Jetpack Compose, MVVM, and Hilt that is production-ready.

Supports filtering by date, grouping by category/time, and displays total expense stats.

Clean UI that follows Material 3 design, responsive layout, and proper state handling.

All components modular and reusable, with code structured for scalability and maintainability.

######
"
- "S – Situation
I am building an Android application using Jetpack Compose with MVVM architecture and Hilt for dependency injection. The app is an Expense Tracker that records and analyzes expenses. I want to create an Expense Report Screen that shows a mock report for the last 7 days.

The report should include:

Daily totals (sum of expenses for each day).

Category-wise totals (e.g., Food, Travel, Utility, Staff).

A Bar chart or Line chart to visualize daily totals (mocked for now with sample data).

An optional Export feature that simulates generating a PDF or CSV report.

An optional Share feature to trigger Android’s share intent with a sample file or text.

This screen should follow Material 3 guidelines with a clean and professional layout.

T – Task
Your task is to generate full working Kotlin code for the Expense Report Screen using Jetpack Compose, MVVM, and Hilt, ensuring:

The screen loads mocked report data for the last 7 days.

Daily totals are shown in a list or summary cards.

Category-wise totals are displayed (can use LazyColumn, Row, or cards).

A chart component (bar or line) visualizes daily totals using a Compose chart library (like MPAndroidChart with Compose wrapper, or a mock Composable chart).

Export and Share features are simulated (no real backend, just mock file generation or static text sharing).

Proper state handling (loading, success, empty/error states).

Code is modular, reusable, and clean.

A – Action
When generating the code, ensure you:

Create a ReportData model containing:

Daily totals (date, totalAmount).

Category totals (category, totalAmount).

Mock the last 7 days’ report data inside the Repository or ViewModel for now.

Implement a ViewModel that:

Fetches (mock) report data.

Prepares chart-friendly data.

Manages export/share triggers.

Implement the Composable Screen that:

Shows a TopAppBar with title "Expense Report".

Displays daily totals in a clean list or cards.

Shows category totals in a sectioned view.

Renders a chart with daily totals.

Provides an Export button (PDF/CSV) and Share button (optional).

For chart rendering, use:

A Compose chart library (preferred)

OR mock with Canvas/Box placeholders if a library is not available.

Use Material 3 design principles for spacing, colors, and typography.

R – Result
The result should be:

A fully functional Expense Report Screen built with Jetpack Compose, MVVM, and Hilt that displays:

Daily totals for the last 7 days.

Category-wise totals.

A chart visualization (bar or line).

Optional simulated export to PDF/CSV.

Optional share intent trigger with a mock file/text.

Professional, clean UI following Material 3 guidelines, with proper state handling and reusable code structure.
"

