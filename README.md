# PoetryVerse — Luxury Editorial Desktop & Web Application for Writing and Organizing Poetry Collections

> *"Where Words Find Their Forever."*

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/SarbhjotSingh/poetryverse)

**PoetryVerse** is a complete, full-stack Java Spring Boot application engineered for poets, lyricists, and literary curators. Designed with a **Luxury Vintage Botanical & Editorial Aesthetic** (inspired by *Aubade Pages*, *Plumwood Mountain Journal*, and framed botanical editions of Constantine P. Cavafy's *Ithaca*), PoetryVerse combines a distraction-free warm ivory writing sanctuary with relational database organization, anthology publishing, mood-tracked journaling, and visual writing analytics.

---

## 1. Visual Identity & Design System

### Color Palette
| Token | Hex Code | Usage |
| :--- | :--- | :--- |
| **Deep Olive Green** | `#353626` | Primary editorial sections, workspace sidebar, buttons, chart bars |
| **Muted Olive** | `#60604D` | Panel headers, active navigation states, secondary accents |
| **Warm Ivory** | `#F5F0E5` | Main sanctuary background, manuscript paper sheets |
| **Antique Gold** | `#A18B50` | Baroque & classical borders, badges, focus rings, ornaments |
| **Dark Charcoal** | `#25251E` | High-contrast body ink, footer, nocturnal parchment |
| **Soft Beige** | `#D9D0B9` | Dashboard analytics cards, quotation marks, subtle dividers |

### Typography
- **Editorial Serif Headings & Verse:** `'Cormorant Garamond'` & `'Playfair Display'`
- **Clean UI & Controls:** `'Inter'`
- **Handwritten Script Accents:** `'La Belle Aurore'` & `'Alex Brush'`

---

## 2. Complete 15-Page Application Structure

1. **Welcome / Landing Page (`/` or `/index`)** — Botanical archway hero, baroque gold frame gallery, Plumwood Mountain Journal editorial spread, framed *Ithaca* showcase, *"The Heart Behind the Pages"* manifesto, testimonials, and footer.
2. **Login Page (`/login`)** — Username or email login, password visibility toggle, Remember-Me session option, one-click Demo Poet autofill, and Forgot Passphrase recovery modal.
3. **Registration Page (`/register`)** — Full validation, pen name selection, BCrypt password hashing, and automatic provisioning of a private library with 7 default collections.
4. **Main Dashboard (`/dashboard`)** — *"Welcome back, poet."* banner, 4 KPI cards, 14-day overview line chart, weekday bar chart, circular goal progress ring, recently edited manuscripts, daily inspirational quote, and random poem suggestion.
5. **My Poetry Library (`/library`)** — Grid and list view switcher, real-time search by title/content/tag, filter by genre/mood/tag/archived status, sort by newest/oldest/title/word count/recently edited, duplicate, archive, favorite, and `.TXT` manuscript import.
6. **Poetry Writing Editor (`/editor` & `/editor/{id}`)** — Distraction-free warm ivory paper sheet, full-screen **Zen Mode** (`Esc` to exit), auto-save & manual save (`Ctrl+S`), font family/size/line-spacing controls, Left/Center/Right alignment, bold/italic/underline/asterism tools, live word/character/line/stanza/reading-time counters, paper surface switcher, collection assignment, and framed live preview.
7. **Collections & Categories (`/collections` & `/collections/{id}`)** — Create, edit, and delete custom collections (*Love & Romance*, *Heartbreak*, *Nature & Seasons*, *Life & Philosophy*, *Dreams & Memories*, *Spirituality*, *Untitled Thoughts*), custom accent colors & epigraphs, multi-collection poem assignment, and **one-click Anthology PDF Export**.
8. **Favorite Poems (`/favorites`)** — Featured *Plumwood Mountain Journal* reading spread, search & sort favorites, framed reading modal, and one-click clipboard sharing.
9. **Writing Journal (`/journal`)** — Interactive monthly calendar view, date-based reflections, mood & weather tracking, poetic spark notes, and search.
10. **Poetry Templates (`/templates`)** — 8 ready-to-write templates (*Romantic Poetry*, *Haiku 5-7-5*, *Classical Sonnet*, *Free Verse*, *Acrostic Poetry*, *Nature & Botanical*, *Inspirational Odyssey*, *Sad & Elegiac*) with meter rules and starter scaffolds.
11. **Search & Discover (`/discover`)** — Interactive **"Inspire Me"** random writing prompt generator, daily quotes, craft challenges, genre lexicon, and public-domain anthology.
12. **Statistics & Insights (`/statistics`)** — Visual analytics powered by Chart.js: daily & monthly writing activity, most productive day of the week, favorite genre distribution, collection distribution, and streak milestones.
13. **User Profile (`/profile`)** — Antique gold oval portrait frame, pen name, biography, signature epigraph, and portfolio summary.
14. **Settings (`/settings`)** — Light/Dark/Sepia sanctuary themes (`EDITORIAL_OLIVE`, `MIDNIGHT_BOTANICAL`, `ANTIQUE_PARCHMENT`), default editor typography, auto-save interval, passphrase update, personal backup download, and account deletion.
15. **About / Help (`/about`)** — Platform manifesto, keyboard shortcuts, architecture overview, and interactive FAQ.

---

## 3. Relational Database Design (11 Tables)

The full MySQL DDL schema is located in [`database/schema.sql`](database/schema.sql) and sample seed data in [`database/seed.sql`](database/seed.sql):

1. `users` — Registered poets, BCrypt password hashes, pen names, and streak counters.
2. `user_settings` — 1-to-1 preferences for theme, default font, line spacing, and daily word goals.
3. `poems` — Core poem manuscripts, formatting metadata, and calculated word/character/line/stanza metrics.
4. `collections` — Custom thematic collections with accent colors, epigraphs, and cover art.
5. `poem_collections` — Many-to-Many join table linking `poems` and `collections`.
6. `favorites` — Starred poems per user with optional personal notes.
7. `journal_entries` — Date-based personal writing journal entries with mood and weather tracking.
8. `tags` — User-scoped poetic tags.
9. `poem_tags` — Many-to-Many join table linking `poems` and `tags`.
10. `writing_statistics` — Daily activity log tracking words written, poems created/edited, and dominant genre.
11. `writing_prompts` — Curated database of prompts, challenges, quotes, and public-domain classics.

---

## 4. Installation, Database Connection & Execution Instructions

### Prerequisites
- **Java 17 or later** (Tested with Java 17, 21, and 26)
- **MySQL 8.0+** (Optional: If local MySQL Server on `localhost:3306` is not running, `DatabaseConfig` automatically activates a persistent local MySQL-compatible database in `./data/poetryverse_db` so the application runs immediately out-of-the-box!)
- **Maven** (Included via `./mvnw.cmd` and `./mvnw` wrapper scripts — no global Maven installation required)

### Step 1: MySQL Database Setup (If using MySQL Server)
1. Start your local MySQL server on port `3306`.
2. (Optional) Execute the schema and seed scripts manually, or let Spring Boot auto-create `poetryverse_db`:
   ```sql
   SOURCE database/schema.sql;
   SOURCE database/seed.sql;
   ```
3. Configure credentials in `src/main/resources/application.properties` (or via environment variables `MYSQL_URL`, `MYSQL_USER`, `MYSQL_PASSWORD`):
   ```properties
   poetryverse.db.mysql.url=jdbc:mysql://localhost:3306/poetryverse_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   poetryverse.db.mysql.username=root
   poetryverse.db.mysql.password=your_mysql_password
   ```

### Step 2: Build & Run the Application
From the project root (`d:\java project`), run:

```powershell
.\mvnw.cmd spring-boot:run
```

### Step 3: Access PoetryVerse in Your Browser
Open **[http://localhost:8080](http://localhost:8080)**

#### Pre-Populated Demo Poet Account
- **Username:** `clara_vance` (or email: `clara@poetryverse.art`)
- **Password:** `PoetVerse2026!`
- *(Or click **"Fill Demo Poet (clara_vance)"** directly on the Sign In page, or register a brand-new poet account!)*
