import { useState, useRef } from "react";
import JSZip from "jszip";
import { saveAs } from "file-saver";
import {
  processImageToTexture,
  loadImageFromFile,
  loadImageFromURL,
  blobToDataURL,
} from "../lib/processor";
import { createManifest, parseManifest } from "../lib/manifest";
import { generateImageFromDescription } from "../lib/aiGeneration";
import DropZone from "./DropZone";
import "./BatchMode.css";

interface BatchItem {
  id: string;
  name: string;
  description: string;
  status: "idle" | "uploading" | "generating" | "processing" | "done" | "error";
  error?: string;
  imageFile?: File;
  originalUrl?: string;
  textureUrl?: string;
  textureBlob?: Blob;
  generatedWithAI?: boolean;
}

function parseBatchText(text: string): { name: string; description: string }[] {
  return text
    .split("\n")
    .map((line) => line.trim())
    .filter((line) => line.length > 0 && !line.startsWith("#"))
    .map((line) => {
      const sep = line.indexOf("|");
      if (sep === -1) return { name: line.trim(), description: "" };
      return {
        name: line.slice(0, sep).trim(),
        description: line.slice(sep + 1).trim(),
      };
    })
    .filter((item) => item.name.length > 0);
}

const SETTINGS = { shrinkFactor: 0.85, vignetteStrength: 0.3, quantize: true };

let idCounter = 0;
const uid = () => `item-${++idCounter}`;

export default function BatchMode() {
  const [batchText, setBatchText] = useState(
    "diamond_sword | A shiny diamond sword\niron_pickaxe | An iron pickaxe\ngolden_apple | A golden apple"
  );
  const [items, setItems] = useState<BatchItem[]>([]);
  const [parsed, setParsed] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const manifestInputRef = useRef<HTMLInputElement>(null);

  const updateItem = (id: string, patch: Partial<BatchItem>) =>
    setItems((prev) => prev.map((it) => (it.id === id ? { ...it, ...patch } : it)));

  const handleParse = () => {
    const parsed = parseBatchText(batchText);
    if (parsed.length === 0) return;
    setItems(
      parsed.map((p) => ({
        id: uid(),
        name: p.name,
        description: p.description,
        status: "idle",
      }))
    );
    setParsed(true);
  };

  const processItem = async (item: BatchItem, img: HTMLImageElement, fromAI = false) => {
    const blob = await processImageToTexture(img, SETTINGS);
    const textureUrl = await blobToDataURL(blob);
    updateItem(item.id, {
      status: "done",
      textureUrl,
      textureBlob: blob,
      generatedWithAI: fromAI,
    });
  };

  const handleUploadForItem = async (item: BatchItem, file: File) => {
    updateItem(item.id, { status: "processing", imageFile: file, error: undefined });
    try {
      const img = await loadImageFromFile(file);
      const originalUrl = URL.createObjectURL(file);
      updateItem(item.id, { originalUrl });
      await processItem(item, img, false);
    } catch (e) {
      updateItem(item.id, {
        status: "error",
        error: e instanceof Error ? e.message : "Failed",
      });
    }
  };

  const handleGenerateAI = async (item: BatchItem) => {
    if (!item.description) {
      updateItem(item.id, {
        status: "error",
        error: "No description provided for AI generation.",
      });
      return;
    }
    updateItem(item.id, { status: "generating", error: undefined });
    try {
      const result = await generateImageFromDescription(
        `Minecraft item: ${item.description}. Pixel art style, simple, bright colors.`
      );
      if (!result.success || !result.dataUrl) {
        updateItem(item.id, {
          status: "error",
          error: result.error ?? "AI generation failed",
        });
        return;
      }
      updateItem(item.id, { status: "processing", originalUrl: result.dataUrl });
      const img = await loadImageFromURL(result.dataUrl);
      await processItem({ ...item, status: "processing" }, img, true);
    } catch (e) {
      updateItem(item.id, {
        status: "error",
        error: e instanceof Error ? e.message : "AI generation failed",
      });
    }
  };

  const handleProcessAll = async () => {
    const todo = items.filter(
      (it) => it.status === "idle" || it.status === "error"
    );
    for (const item of todo) {
      if (item.imageFile) {
        await handleUploadForItem(item, item.imageFile);
      }
    }
  };

  const readyCount = items.filter((it) => it.status === "done").length;

  const handleDownloadAll = async () => {
    if (readyCount === 0) return;
    setDownloading(true);
    try {
      const zip = new JSZip();
      for (const item of items) {
        if (item.status === "done" && item.textureBlob) {
          zip.file(`${item.name}.png`, item.textureBlob);
        }
      }
      const manifest = createManifest(
        items.map((it) => ({
          name: it.name,
          description: it.description,
          generatedWithAI: it.generatedWithAI,
          hasTexture: it.status === "done",
        })),
        SETTINGS
      );
      zip.file("manifest.json", JSON.stringify(manifest, null, 2));
      const blob = await zip.generateAsync({ type: "blob" });
      saveAs(blob, "minecraft_textures.zip");
    } finally {
      setDownloading(false);
    }
  };

  const handleImportManifest = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    e.target.value = "";
    const reader = new FileReader();
    reader.onload = () => {
      try {
        const json = JSON.parse(reader.result as string);
        const manifest = parseManifest(json);
        if (!manifest) {
          alert("Invalid manifest file.");
          return;
        }
        const newItems: BatchItem[] = manifest.items.map((it) => ({
          id: uid(),
          name: it.name,
          description: it.description,
          status: "idle",
        }));
        setItems(newItems);
        setBatchText(
          manifest.items
            .map((it) => `${it.name} | ${it.description}`)
            .join("\n")
        );
        setParsed(true);
      } catch {
        alert("Could not parse manifest file.");
      }
    };
    reader.readAsText(file);
  };

  const statusBadge = (item: BatchItem) => {
    const map: Record<BatchItem["status"], string> = {
      idle: "idle",
      uploading: "uploading",
      generating: "generating…",
      processing: "processing…",
      done: "✓ done",
      error: "✗ error",
    };
    return (
      <span className={`status-badge status-${item.status}`}>
        {map[item.status]}
      </span>
    );
  };

  return (
    <div className="batch-mode">
      <div className="mode-header">
        <h2>Batch Mode</h2>
        <p className="mode-desc">
          Paste a list of item names and descriptions, then upload or generate
          textures for each
        </p>
      </div>

      {!parsed ? (
        <div className="batch-input-section">
          <div className="batch-actions-top">
            <button
              className="btn-secondary"
              onClick={() => manifestInputRef.current?.click()}
            >
              📥 Import Manifest
            </button>
            <input
              ref={manifestInputRef}
              type="file"
              accept=".json"
              style={{ display: "none" }}
              onChange={handleImportManifest}
            />
          </div>
          <label className="field-label">
            Item list{" "}
            <span className="field-hint">
              (one per line: <code>item_name | description</code>)
            </span>
          </label>
          <textarea
            className="batch-textarea"
            value={batchText}
            onChange={(e) => setBatchText(e.target.value)}
            rows={10}
            placeholder={
              "diamond_sword | A shiny diamond sword\niron_pickaxe | An iron pickaxe"
            }
          />
          <button
            className="btn-primary"
            onClick={handleParse}
            disabled={!batchText.trim()}
          >
            Parse Items →
          </button>
        </div>
      ) : (
        <div className="batch-list-section">
          <div className="batch-toolbar">
            <span className="batch-count">
              {items.length} items · {readyCount} ready
            </span>
            <div className="toolbar-actions">
              <button
                className="btn-secondary"
                onClick={() => {
                  setParsed(false);
                  setItems([]);
                }}
              >
                ← Back to edit
              </button>
              <button
                className="btn-secondary"
                onClick={() => manifestInputRef.current?.click()}
              >
                📥 Import Manifest
              </button>
              <input
                ref={manifestInputRef}
                type="file"
                accept=".json"
                style={{ display: "none" }}
                onChange={handleImportManifest}
              />
              <button
                className="btn-primary"
                onClick={handleDownloadAll}
                disabled={readyCount === 0 || downloading}
              >
                {downloading
                  ? "Zipping…"
                  : `↓ Download All (${readyCount})`}
              </button>
            </div>
          </div>

          <div className="batch-list">
            {items.map((item) => (
              <BatchRow
                key={item.id}
                item={item}
                onUpload={handleUploadForItem}
                onGenerateAI={handleGenerateAI}
                statusBadge={statusBadge}
              />
            ))}
          </div>

          {items.some((it) => it.status === "idle" && it.imageFile) && (
            <button className="btn-secondary" onClick={handleProcessAll}>
              Process all pending
            </button>
          )}
        </div>
      )}
    </div>
  );
}

interface BatchRowProps {
  item: BatchItem;
  onUpload: (item: BatchItem, file: File) => void;
  onGenerateAI: (item: BatchItem) => void;
  statusBadge: (item: BatchItem) => React.ReactNode;
}

function BatchRow({ item, onUpload, onGenerateAI, statusBadge }: BatchRowProps) {
  const busy =
    item.status === "generating" ||
    item.status === "processing" ||
    item.status === "uploading";

  return (
    <div className={`batch-row${item.status === "done" ? " done" : ""}`}>
      <div className="row-main">
        <div className="row-name">
          <code>{item.name}</code>
          {statusBadge(item)}
        </div>
        {item.description && (
          <div className="row-desc">{item.description}</div>
        )}
        {item.error && (
          <div className="row-error">⚠ {item.error}</div>
        )}
      </div>

      <div className="row-actions">
        {item.status === "done" && item.textureUrl ? (
          <div className="row-result">
            <img
              src={item.textureUrl}
              alt={item.name}
              width={48}
              height={48}
              style={{ imageRendering: "pixelated" }}
              className="row-texture"
            />
            {item.generatedWithAI && (
              <span className="ai-badge">AI</span>
            )}
            <DropZone
              onFile={(f) => onUpload(item, f)}
              disabled={busy}
              label="Replace"
              compact
            />
          </div>
        ) : busy ? (
          <div className="row-spinner">
            <div className="spinner-sm" />
            <span>
              {item.status === "generating" ? "Generating…" : "Processing…"}
            </span>
          </div>
        ) : (
          <div className="row-btns">
            <DropZone
              onFile={(f) => onUpload(item, f)}
              disabled={busy}
              label="Upload photo"
              compact
            />
            {item.description && (
              <button
                className="btn-ai"
                onClick={() => onGenerateAI(item)}
                disabled={busy}
              >
                ✨ Generate with AI
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
