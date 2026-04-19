# Tasks — Android Todo App

A native Android task manager with automatic keyword-based categorization and fully local storage.

## Features

- **Keyword auto-categorization** — title scanned on save; category overridable
- **6 built-in categories** — Personal, Shopping, Work, Health, Home, Finance
- **Custom categories** — add categories with custom keywords and colors
- **Due dates + exact alarms** — local notification at exact due time
- **Recurrence** — Daily / Weekly; on completion, next instance created automatically
- **Swipe gestures** — right to complete, left to delete (with 5-second Undo)
- **3 tabs** — Today (grouped by category), All (collapsible), Completed (last 14 days)
- **Category detail screen** — tap a category header
- **Home screen widget** — shows today's tasks, updates when tasks change
- **Theme** — Light / Dark / Follow system; dynamic color on Android 12+
- **14-day auto-cleanup** — WorkManager daily job, no user setting

## Tech Stack

| Layer | Library |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt 2.51.1 |
| Database | Room 2.6.1 (SQLite) |
| Background | WorkManager 2.9.0 |
| Notifications | NotificationManagerCompat + AlarmManager |
| Widget | Jetpack Glance 1.0.0 |
| Build | Gradle 8.7 · AGP 8.4.1 · Kotlin DSL |

**Package:** `com.ael.todo` · **Min SDK:** 26 · **Target SDK:** 35

## Opening in Android Studio

1. **File → Open** and select the `tasks-app/` folder.
2. Android Studio will detect the Gradle project and download the Gradle wrapper + dependencies automatically (first sync may take a few minutes).
3. Set up your Android SDK in **File → Project Structure → SDK Location** if not already configured.

> **Note:** `gradle/wrapper/gradle-wrapper.jar` is not included. Android Studio downloads it automatically on first sync. Alternatively run `gradle wrapper --gradle-version 8.7` from the project root if you have Gradle installed.

## Building the APK

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Installing via ADB

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Running Tests

```bash
./gradlew test
```

## Runtime Permissions

| Permission | Why |
|---|---|
| `POST_NOTIFICATIONS` | Show task-due notifications (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Fire exact-time alarms for due tasks (Android 12+) |
| `USE_EXACT_ALARM` | Fallback for exact alarms (Android 13+) |

The app checks `AlarmManager.canScheduleExactAlarms()` before scheduling; if denied, alarms are silently skipped and the user can grant the permission in Settings → Apps → Tasks → Permissions.

## Design Decisions (spec was silent)

| Decision | Choice |
|---|---|
| Default category for new tasks | Personal (id=1, seeded first) |
| Category seed order | Personal → Shopping → Work → Health → Home → Finance |
| Completed tab "last week" range | 8–14 days ago |
| Widget max tasks shown | 10 |
| Widget update trigger | Broadcast on every task mutation + `onUpdate()` |
| Subtask undo | Not included (only top-level task delete is undoable) |
| App icon | System default placeholder (replace `mipmap/ic_launcher`) |
| Notification icon | `android.R.drawable.ic_lock_idle_alarm` (replace with custom drawable) |
| Color picker for new categories | Fixed slate-grey default (0xFF607D8B); extend `AddCategoryDialog` for a real picker |
| Settings entry point | Not in bottom nav — accessible via TopAppBar in individual screens |
