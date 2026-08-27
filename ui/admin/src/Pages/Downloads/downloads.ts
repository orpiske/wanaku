/**
 * Static, front-end model of the CLI packages that Wanaku publishes.
 *
 * Wanaku ships its CLI in more than one flavour: a self-contained native
 * binary for a specific platform and a portable Java package that runs on any
 * operating system that has a compatible JVM installed. A single download link
 * cannot represent all of those options, so the page below lists every package
 * and lets the user pick the one that matches their environment.
 *
 * The CLI is released from the companion `wanaku-barn` repository. The version
 * and release channel are resolved at runtime from the management info API
 * (see {@link buildCliDownloadInfo}), so the links always match the running
 * server. The entries are intentionally kept as a plain, extensible array:
 * adding support for new platforms (for example native macOS or Linux aarch64
 * builds) is just a matter of adding new objects in {@link buildCliDownloadInfo}.
 */

/** Distinguishes portable Java packages from platform-specific native binaries. */
export type CliPackageKind = "native" | "java";

export interface CliDownload {
  /** Stable identifier, also used as the table row id and in tests. */
  id: string;
  /** Human friendly package name. */
  name: string;
  /** Whether the package is a native binary or a Java (JVM) package. */
  kind: CliPackageKind;
  /** The runtime the package needs, e.g. "Native (no runtime required)". */
  runtime: string;
  /** The supported operating system / architecture. */
  platform: string;
  /** The name of the artifact as published in the release. */
  fileName: string;
  /** Direct download URL for the artifact. */
  downloadUrl: string;
  /** Optional extra guidance shown to the user. */
  notes?: string;
}

/** The GitHub repository that publishes the Wanaku CLI packages. */
export const CLI_REPO = "wanaku-ai/wanaku-barn";

/**
 * The release channel the downloads point at.
 *
 * - `early-access` tracks the latest pre-release build. It is published under
 *   the fixed `early-access` tag, and its artifacts carry the `-SNAPSHOT`
 *   suffix (for example `wanaku-cli-0.3.0-SNAPSHOT.zip`).
 * - `stable` points at the tagged release for the resolved version. It is
 *   published under the `v<version>` tag (for example `v0.3.0`), and its
 *   artifacts use the plain version (for example `wanaku-cli-0.3.0.zip`).
 */
export type ReleaseChannel = "early-access" | "stable";

/**
 * Fallback version used before the management info API responds, or if it is
 * unavailable. Keep this in sync with the workspace `Cargo.toml` and
 * `ui/admin/package.json`.
 */
export const DEFAULT_CLI_VERSION = "0.3.0";

/** Fallback release channel used before the info API responds. */
export const DEFAULT_RELEASE_CHANNEL: ReleaseChannel = "early-access";

/** Coerces an arbitrary string from the info API into a known {@link ReleaseChannel}. */
export const normalizeReleaseChannel = (value: string | undefined): ReleaseChannel =>
  value === "stable" ? "stable" : "early-access";

/** Everything the Downloads page needs to render for a given version and channel. */
export interface CliDownloadInfo {
  version: string;
  channel: ReleaseChannel;
  /** Release tag the downloads point at (`early-access` or `v<version>`). */
  releaseTag: string;
  /** Page that lists every asset of the referenced release. */
  releasePage: string;
  /** The list of CLI packages available for download. */
  downloads: CliDownload[];
}

/**
 * Builds the download model for a given CLI version and release channel.
 *
 * Add new native builds (macOS, Linux aarch64, Windows, ...) to the `downloads`
 * array below as they become available in the release.
 */
export const buildCliDownloadInfo = (
  version: string = DEFAULT_CLI_VERSION,
  channel: ReleaseChannel = DEFAULT_RELEASE_CHANNEL,
): CliDownloadInfo => {
  const isEarlyAccess = channel === "early-access";
  const releaseTag = isEarlyAccess ? "early-access" : `v${version}`;
  const artifactVersion = isEarlyAccess ? `${version}-SNAPSHOT` : version;
  const releasePage = `https://github.com/${CLI_REPO}/releases/tag/${releaseTag}`;
  const downloadUrl = (fileName: string): string =>
    `https://github.com/${CLI_REPO}/releases/download/${releaseTag}/${fileName}`;

  const downloads: CliDownload[] = [
    {
      id: "java-universal",
      name: "Wanaku CLI (Java)",
      kind: "java",
      runtime: "Java 21+",
      platform: "Any (JVM)",
      fileName: `wanaku-cli-${artifactVersion}.zip`,
      downloadUrl: downloadUrl(`wanaku-cli-${artifactVersion}.zip`),
      notes: "Portable package. Requires a Java 21+ runtime installed on your machine.",
    },
    {
      id: "native-linux-x86_64",
      name: "Wanaku CLI (native)",
      kind: "native",
      runtime: "Native (no runtime required)",
      platform: "Linux x86_64",
      fileName: `wanaku-cli-${artifactVersion}-linux-x86_64.zip`,
      downloadUrl: downloadUrl(`wanaku-cli-${artifactVersion}-linux-x86_64.zip`),
      notes: "Self-contained binary. Unzip, make it executable and move it into your PATH.",
    },
  ];

  return {version, channel, releaseTag, releasePage, downloads};
};
