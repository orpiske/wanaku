# Kaoto Integration into Wanaku UI — Plan

## Context

Users currently design service catalogs (Camel YAML routes + rules) outside Wanaku, then upload ZIP archives via CLI or UI. Embedding Kaoto — a visual drag-and-drop designer for Apache Camel routes — would let users design catalogs directly in the Wanaku admin UI.

## Domain Alignment

**Perfect format match.** Wanaku service catalogs contain `*.camel.yaml` files (Camel YAML DSL routes). Kaoto's entire purpose is visually editing Camel YAML DSL. The route format is identical — e.g., `from: uri: direct:wanaku` + `steps: [...]`.

**What Kaoto edits = what catalogs contain:**
- `ftp/ftp.camel.yaml` → Kaoto can open, edit, and save this visually
- Kaoto outputs standard Camel YAML DSL, which is exactly what the catalog ZIP packages

## Kaoto as a Library

**Package:** `@kaoto/kaoto` (v2.11.0-RC2 on npm)

**Exported components** (from `public-api`):
- `Canvas` — drag-and-drop route designer
- `Visualization` — main container
- `Catalog` — 300+ Camel components browser
- `RouteVisualization` — external-facing wrapper
- `PropertiesModal`, `MetadataEditor`, `Settings`

**Peer dependencies:** React 19.2.4 (matches Wanaku), PatternFly 6.4+, Monaco editor, monaco-yaml

**Critical gap:** No public documentation for component props, onChange handlers, or how to pass/receive YAML content. The embedding API exists but is undocumented — usage patterns must be reverse-engineered from source or the VS Code extension.

## Decisions

| Question | Decision |
|---|---|
| Initial scope | Both editing existing routes AND creating new catalogs from scratch |
| Rules YAML generation | Separate rules designer/editor will come later |
| Template `{{placeholder}}` handling | **TODO: Investigate** — unclear how Kaoto handles Wanaku-specific property interpolation |
| Deployment model | Bundled with the router backend (same Docker image) |
| Kaoto catalog filtering | No filtering — show all 300+ components |

## Recommended Approach: iframe Isolation

Host Kaoto as a separate lightweight app (same domain, different path e.g. `/kaoto/`). Embed via `<iframe>` in the Wanaku catalog editor page. Communicate via `postMessage`.

**Why iframe over direct embed:**
- Complete CSS isolation — Carbon (Wanaku) and PatternFly (Kaoto) never conflict
- Independent build/deploy cycles
- No dependency version coupling
- Kaoto updates don't break Wanaku

### Flow

1. User clicks "Design Route" on a catalog/template or "Create New Catalog"
2. Wanaku opens a modal/page with an iframe pointing to `/kaoto/`
3. Wanaku sends the current YAML content to the iframe via `postMessage`
4. User edits visually in Kaoto
5. On save, Kaoto posts the updated YAML back via `postMessage`
6. Wanaku packages the updated YAML into the catalog ZIP and deploys

## Implementation

### Backend Changes
- **New endpoint** `GET /api/v1/service-catalog/{name}/route/{system}` — returns the raw Camel YAML for a specific system's route file
- **New endpoint** `PUT /api/v1/service-catalog/{name}/route/{system}` — updates a single route file within a catalog ZIP (extract → replace → repackage)

### Kaoto Wrapper App
- Minimal React app (separate Vite project) at `apps/ui/kaoto-editor/`
- Imports `@kaoto/kaoto` components with full PatternFly deps
- Implements `postMessage` listener: receives YAML, loads into Kaoto, sends back on save
- Served at `/kaoto/` path by the router backend

### Wanaku UI Changes
- New `<KaotoEditorModal>` component — renders an iframe to the Kaoto wrapper
- "Edit Route" button on each system card in `ServiceCatalogCards.tsx`
- "Create Catalog" button that opens Kaoto with empty canvas
- `postMessage` protocol: `{ type: 'load', yaml: '...' }` / `{ type: 'save', yaml: '...' }`
- On save: call the new PUT endpoint to update the catalog

### Key Files to Modify/Create

| File | Change |
|---|---|
| `apps/ui/admin/src/Pages/ServiceCatalog/ServiceCatalogCards.tsx` | Add "Edit Route" button per system |
| `apps/ui/admin/src/components/KaotoEditorModal.tsx` | New: iframe + postMessage wrapper |
| `apps/ui/kaoto-editor/` | New: standalone Kaoto wrapper app |
| `apps/wanaku-router-backend/.../ServiceCatalogResource.java` | New route-level GET/PUT endpoints |
| `core/core-services-api/.../ServiceCatalogService.java` | New interface methods |

---

## Implementation Notes & Hurdles

### Hurdle 1: Kaoto embedding API is undocumented (BLOCKER)

The `@kaoto/kaoto` npm package exports React components (`Canvas`, `Visualization`, `RouteVisualization`, `Catalog`, etc.) but there is **zero public documentation** for:
- Component props (how to pass YAML in, receive changes out)
- State management integration (Kaoto uses Zustand internally)
- Lifecycle hooks (onChange, onSave callbacks)
- How to initialize with existing YAML content

**Fallback implemented:** Monaco YAML editor with Camel schema validation. This provides a functional code editor but lacks Kaoto's visual drag-and-drop canvas.

**Resolved:** Reverse-engineered the embedding API from Kaoto's source. The `RouteVisualization` component (exported from `@kaoto/kaoto/components`) is the purpose-built external embedding component:

```tsx
<RouteVisualization
  catalogUrl="/camel-catalog/index.json"
  code={yamlString}
  codeChange={(newCode) => setYaml(newCode)}
/>
```

It handles all internal providers (EntitiesProvider, RuntimeProvider, SchemasLoaderProvider, CatalogLoaderProvider, VisualizationProvider, VisibleFlowsProvider) automatically. The `@kaoto/camel-catalog` package must be served statically at the `catalogUrl` path. Monaco fallback has been replaced with the real Kaoto visual designer.

### Hurdle 2: CSS isolation required (Carbon vs PatternFly)

Wanaku uses IBM Carbon Design System. Kaoto uses Red Hat PatternFly. Both are global CSS design systems with conflicting CSS custom properties (`--cds-*` vs `--pf-*`), global resets, and class naming. They **cannot coexist in the same DOM**.

**Resolution:** iframe isolation approach was used. The Kaoto wrapper is a separate Vite app served at `/kaoto/`, embedded via `<iframe>` in the admin UI.

### Hurdle 3: postMessage save-flow race condition (BUG)

The `KaotoEditorModal` has a timing issue in its save flow:
1. Modal's "Save" button calls `handleRequestSave()` which sends `{ type: 'request-save' }` to the iframe
2. Then immediately calls `handleSave()` after a 100ms timeout
3. But the iframe's `App.tsx` doesn't handle `request-save` — it only handles `load` and `load-new`
4. So the modal saves whatever `currentYaml` state it had from the last auto-save, not the current editor content

**Fix needed:** Either (a) the iframe should handle `request-save` by responding with `{ type: 'save', yaml }`, or (b) the modal should track YAML state differently. The iframe already sends `save` events when the user clicks its own Save button — the protocol just needs to be consistent.

### Hurdle 4: "Create Catalog" flow is incomplete

Currently, clicking "Create Catalog" opens the editor with empty YAML. After the user designs a route, only a success message is shown. There's no workflow to:
- Name the catalog
- Set up `index.properties` metadata
- Define systems and rules mappings
- Package into a ZIP and deploy

This needs a multi-step wizard (similar to the existing DeploymentWizard) that collects metadata after route design.

### Hurdle 5: Template `{{placeholder}}` handling — TODO

Still uninvestigated. When editing templates (not catalogs), route YAML contains `{{property.name}}` Camel property placeholders. Both Kaoto and Monaco may misinterpret these as invalid syntax. Options to evaluate:
- Pre-process: resolve placeholders before editing, re-inject after
- Escape: use a different syntax during editing
- Ignore: let the editor show them as-is (may trigger validation warnings)

### Hurdle 6: Static file serving — Quinoa only supports one UI

The admin UI is served via **Quarkus Quinoa** (`quarkus.quinoa.ui-dir=../ui/admin`), which only supports a single UI directory per application. The Kaoto wrapper cannot be served the same way.

**Resolved:** Added `copy-kaoto-editor` execution to `maven-resources-plugin` in the router backend's `pom.xml`. Copies `apps/ui/kaoto-editor/dist/` into `target/classes/META-INF/resources/kaoto/` during `generate-resources` phase. Quarkus serves these automatically. Auth config updated to include `/kaoto/*` in the authenticated web paths. The copy step silently skips if `dist/` doesn't exist, so the main build doesn't break if the Kaoto editor hasn't been built yet. **Prerequisite:** `cd apps/ui/kaoto-editor && yarn install && yarn build` must run before `mvn verify`.
