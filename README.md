**GlassFish 5**
**Apache NetBeans IDE 29**
**JDK 8**
**Mac OS X**

CAT - папка на проекта - https://github.com/viptr20/game-localization-cat 
User/pass за тест: admin/ admin123

# Game Localization CAT Tool

A Java EE web application for managing game localization projects with a multilingual user interface, dashboard analytics, translation editing, and review workflows. The UI is built with JSF and PrimeFaces 7, and the app uses JSF resource bundles plus a locale bean for full i18n and RTL support.

## Features

- Multilingual UI (Bulgarian, English, Portuguese, Arabic, Armenian, Hebrew) via JSF resource bundles.
- Locale switching with a session-scoped `LocaleBean`, including RTL layout for Arabic and Hebrew.
- Dashboard with:
  - project filter
  - summary statistics
  - radar / bar / pie charts for segment status, language split, and project profile
  - detailed project table.
- Project pages:
  - list of localization projects
  - project details
  - segment editor with semantic disctionary integration with Wikidata
  - translation review.
- User-facing pages:
  - index, help, login, login error, forgot password, user profile.

## Technology stack

- **Backend:** Java, JSF managed beans, DAO layer.
- **Frontend:** XHTML Facelets, PrimeFaces, CSS.
- **I18n:** JSF resource bundles (`messages_*.properties`), `faces-config.xml`, `LocaleBean`.
- **Charts:** PrimeFaces chart models (bar, pie, radar).
- **Scopes:** `@ViewScoped`, `@SessionScoped` beans.

#### Database

The application uses **MariaDB** as its relational database, typically running in a Docker container. DAOs (e.g. `DashboardDAO`) connect through a JDBC data source configured on the application server.

Example Docker command:

```bash
docker run --name cat-mariadb \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=catdb \
  -e MYSQL_USER=root \
  -e MYSQL_PASSWORD=rootpass \
  -p 3306:3306 \
  -d mariadb:11
```

- Database: `catdb`
- User: `root`
- Password: `rootpass`

Configuration: Configure a JDBC resource in your application server (GlassFish/Payara/Tomcat) pointing to:

```jdbc:mariadb://localhost:3306/catdb```

and update the JNDI name in the DAO / configuration to match your environment. 

Data: real data can be loaded from the Data folder either through the database or through the import option on the UI.

## PrimeFaces components

The UI relies heavily on PrimeFaces for rich JSF widgets:

- `p:panel` for framing forms and dashboard sections.
- `p:toolbar`, `p:toolbarGroup` for top toolbars and language selector.
- `p:commandButton`, `p:button` for actions and navigation.
- `p:selectOneMenu` for language selection and project filters.
- `p:messages`, `p:growl` for validation and feedback messages.
- `p:outputLabel`, `p:inputText`, `p:password`, `p:inputTextarea` in login, forgot password, editor, and review pages.
- `p:dataTable` for project lists and segment tables with pagination.
- `p:tabView`, `p:tab` for the Overview / Details tabs on the dashboard.
- `p:chart`, `p:radarChart` (bar, pie, radar chart models) for visualizing status and language split.
- `p:mindmap`

All components are wired to the resource bundles and locale bean so that labels and layout react to the selected language.

## Main pages

- `index.xhtml` – landing page with language selector and entry buttons.
- `login.xhtml`, `loginError.xhtml`, `forgot.xhtml` – authentication and password reset.
- `help.xhtml` – help and quick usage overview.
- `dashboard.xhtml` – main dashboard with charts and stats.
- `projects.xhtml` – paginated list of projects.
- `projectDetails.xhtml` – project details.
- `projectEditor.xhtml` – segment editor.
- `review.xhtml` – translation review.
- `userProfile.xhtml` – user profile.

## Project structure (simplified)

```text
src/java/
  cat/dao/...
  cat/model/...
  cat/view/...
  i18n/
    messages.properties
    messages_bg.properties
    messages_en.properties
    messages_pt.properties
    messages_ar.properties
    messages_hy.properties
    messages_he.properties
    messages_iw.properties

web/
  *.xhtml
  resources/css/layout.css
  WEB-INF/faces-config.xml
  WEB-INF/web.xml
```

## Running the project

1. Import the project into NetBeans (or another Java EE IDE).
2. Configure an application server (GlassFish/Payara/Tomcat with JSF).
3. Ensure JSF and PrimeFaces libraries are available on the classpath.
4. Clean and build the project.
5. Deploy to the server and open `index.xhtml`/run in the browser. Local run example URL: http://localhost:9080/index/ (glassfish).

After changes to resource bundles or resources under `web/resources`, do a clean build and redeploy so the server picks up the latest i18n and CSS changes.
