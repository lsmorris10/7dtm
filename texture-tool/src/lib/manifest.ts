export interface ManifestItem {
  name: string;
  description: string;
  generatedWithAI?: boolean;
  hasTexture?: boolean;
}

export interface Manifest {
  version: "1.0";
  createdAt: string;
  settings: {
    shrinkFactor: number;
    vignetteStrength: number;
    quantize: boolean;
  };
  items: ManifestItem[];
}

export function createManifest(
  items: ManifestItem[],
  settings: Manifest["settings"]
): Manifest {
  return {
    version: "1.0",
    createdAt: new Date().toISOString(),
    settings,
    items,
  };
}

export function parseManifest(json: unknown): Manifest | null {
  try {
    const m = json as Manifest;
    if (m.version !== "1.0" || !Array.isArray(m.items)) return null;
    return m;
  } catch {
    return null;
  }
}
